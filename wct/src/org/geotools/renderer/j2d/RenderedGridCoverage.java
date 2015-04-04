/*
 * Geotools2 - OpenSource GIS mapping toolkit http://geotools.org
 * (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * File may contain documentation and or interfaces derived from Open Geospatial 
 * Consortium, Inc. (OGC) specifications. The work of the OGC is acknowledged.
 */
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.TileObserver;
import java.awt.image.WritableRenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.cs.CompoundCoordinateSystem;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cv.CannotEvaluateException;
import org.geotools.cv.PointOutsideCoverageException;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.geotools.gc.GridRange;
import org.geotools.gp.CannotReprojectException;
import org.geotools.gp.GridCoverageProcessor;
import org.geotools.pt.Envelope;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.LegacyGCSUtilities;
import org.geotools.resources.Utilities;
import org.geotools.resources.geometry.XAffineTransform;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.resources.image.DeferredPlanarImage;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.resources.renderer.Resources;
import org.opengis.referencing.operation.TransformException;


/**
 * A layer for rendering a {@linkplain GridCoverage grid coverage}. More than one
 * <code>RenderedGridCoverage</code> can share the same grid coverage, for example
 * in order to display an image in many {@link org.geotools.gui.swing.MapPane} with
 * different zoom.
 *
 * @version $Id: RenderedGridCoverage.java 10254 2005-01-05 02:53:49Z jmacgill $
 * @author Martin Desruisseaux
 */
public class RenderedGridCoverage extends RenderedLayer implements TileObserver {
    /**
     * Tells if the deferred painting of tiles is enabled. When enabled, tiles will be painted
     * only if they are already computed. If they are not, tile computation will be trigged in
     * a background thread and the layer will be repainted later.
     * <br><br>
     * Note: Deferred painting work only if {@link #USE_PYRAMID} is <code>true</code>.
     */
//    private static final boolean ENABLE_DEFFERED_PAINTING = true;
    private static final boolean ENABLE_DEFFERED_PAINTING = false;

    /**
     * Tells if we should try an optimisation using pyramidal images.
     */
//    private static final boolean USE_PYRAMID = true;
    private static final boolean USE_PYRAMID = false;

    /**
     * Decimation factor for image. A value of 0.5 means that each
     * level in the image pyramid will contains an image with half
     * the resolution of previous level. This value is used only if
     * {@link #USE_PYRAMID} is <code>true</code>.
     */
    private static final float DOWN_SAMPLER = 0.5f;

    /**
     * Natural logarithm of {@link #DOWN_SAMPLER}. Used
     * only if {@link #USE_PYRAMID} is <code>true</code>.
     */
    private static final double LOG_DOWN_SAMPLER = Math.log(DOWN_SAMPLER);

    /**
     * Minimum size (in pixel) for use of pyramidal image. Images smaller
     * than this size will not use pyramidal images, since it would not
     * give many visible benefict. Used only if {@link #USE_PYRAMID} is
     * <code>true</code>.
     */
    private static final int MIN_SIZE = 512;

    /**
     * The minimum tile size in scaled image.
     */
    private static final int MIN_TILE_SIZE = 128;

    /**
     * Default value for the {@linkplain #getZOrder z-order}.
     */
    private static final float DEFAULT_Z_ORDER = Float.NEGATIVE_INFINITY;

    /**
     * A multi-resolution array <code>RenderedImage[]</code> previously created for each
     * grid coverages.  Keys are weak references to {@link GridCoverage}, and values are
     * <code>RenderedImage[]</code>. This map will be created only when first needed.
     *
     * Note: Two coverages could be backed by the same {@link RenderedImage}. Consequently, it
     *       would be more efficient to use the grid coverage's {@link RenderedImage} as a key
     *       rather than the grid coverage itself. Unfortunatly, we can't because the rendered
     *       image is referenced by values,  both directly in the array and indirectly through
     *       the operation chain.  The solution to this problem required the implementation of
     *       the following RFE ("add joined weak references"):
     *
     *       http://developer.java.sun.com/developer/bugParade/bugs/4630118.html
     */
    private static Map sharedImages;

    /**
     * The coverage given to {@link #setGridCoverage}. This coverage is
     * either the same than {@link #coverage}, or one of its sources.
     *
     * @see #getGridCoverage
     * @see #setGridCoverage
     */
    private GridCoverage sourceCoverage;

    /**
     * The grid coverage projected in this {@linkplain #getCoordinateSystem rendering
     * coordinate system}. It may or may not be the same than {@link #sourceCoverage}.
     */
    private GridCoverage coverage;

    /**
     * A list of multi-resolution images. Image at index 0 is identical to
     * {@link GridCoverage#getRenderedImage()}.   Other index contains the
     * image at lower resolution for faster rendering.
     */
    private PlanarImage[] images;

    /**
     * The transform from the image to the coordinate system used the last
     * time the image was rendered. Note that this transform apply to the
     * image <code>images[level]</code>, which may or may not be the same
     * than <code>coverage.getRenderedImage()</code>.
     */
    private final AffineTransform gridToCS = new AffineTransform();

    /**
     * The index of the image from {@link #images} used during the last rendering.
     * Used together with {@link #gridToCS} in order to transform tile indices in
     * geographical coordinates.
     */
    private int level;

    /**
     * The default {@linkplain #getZOrder z-order} for this layer.
     * Used only if the user didn't set explicitely a z-order.
     */
    private float zOrder = DEFAULT_Z_ORDER;

    /**
     * The default {@linkplain #getPreferredArea preferred area} for this layer.
     * Used only if the user didn't set explicitely a preferred area.
     */
    private Rectangle2D preferredArea;

    /**
     * The default {@linkplain #getPreferredPixelSize preferred pixel size} for this layer.
     * Used only if the user didn't set explicitely a preferred pixel size.
     */
    private Dimension2D preferredPixelSize;

    /**
     * Point dans lequel mémoriser les coordonnées logiques d'un pixel
     * de l'image. Cet objet est utilisé temporairement pour obtenir la
     * valeur du paramètre géophysique d'un pixel.
     *
     * @see #formatValue
     */
    private transient Point2D point;

    /**
     * Valeurs sous le curseur de la souris. Ce tableau sera créé
     * une fois pour toute la première fois où il sera nécessaire.
     *
     * @see #formatValue
     */
    private transient double[] values;

    /**
     * Liste des bandes. Cette liste ne sera créée
     * que la première fois où elle sera nécessaire.
     *
     * @see #formatValue
     */
    private transient SampleDimension[] bands;

    /**
     * Construct a new layer for the specified grid coverage.
     * It is legal to construct many layers for the same grid
     * coverage.
     *
     * @param coverage The grid coverage, or <code>null</code> if none.
     */
    public RenderedGridCoverage(final GridCoverage coverage) {
        if (coverage == null) {
            setZOrder(DEFAULT_Z_ORDER);
        } else try {
            setCoordinateSystem(coverage.getCoordinateSystem());
            setGridCoverage(coverage);
        } catch (TransformException exception) {
            // Should not happen in most cases, since the GridCoverage's
            // coordinate system usually has a two-dimensional head CS.
            final IllegalArgumentException e;
            e = new IllegalArgumentException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Set the grid coverage to renderer. This grid coverage will be automatically projected
     * to the {@linkplain #getCoordinateSystem rendering coordinate system}, if needed.
     *
     * @param  newCoverage The new grid coverage, or <code>null</code> if none.
     * @throws TransformException if the specified coverage can't be projected to
     *         the current {@linkplain #getCoordinateSystem  rendering coordinate
     *         system}.
     */
    public void setGridCoverage(final GridCoverage newCoverage) throws TransformException {
        final GridCoverage oldCoverage;
        synchronized (getTreeLock()) {
            oldCoverage    = coverage;
            coverage       = project(newCoverage, getCoordinateSystem(), renderer);
            sourceCoverage = newCoverage; // Must be set after 'coverage'.
            if (coverage != oldCoverage) {
                initialize();
            }
        }
        listeners.firePropertyChange("gridCoverage", oldCoverage, coverage);
        repaint();
    }

    /**
     * Project the specified coverage.
     *
     * @param  coverage The grid coverage to project, or <code>null</code> if none.
     * @param  targetCS The target coordinate system for the coverage.
     * @param  renderer The renderer to query for rendering hints.
     * @throws TransformException if the specified coverage can't be projected to
     *         the specified coordinate system.
     */
    private static GridCoverage project(GridCoverage     coverage,
                                        CoordinateSystem targetCS,
                                        final Renderer   renderer)
            throws TransformException
    {
        if (coverage != null) {
            GridCoverageProcessor processor = null;
            if (renderer != null) {
                final Object candidate = renderer.getRenderingHint(Hints.GRID_COVERAGE_PROCESSOR);
                if (candidate instanceof GridCoverageProcessor) {
                    processor = (GridCoverageProcessor) candidate;
                }
            }
            if (processor == null) {
                processor = GridCoverageProcessor.getDefault();
            }
            /*
             * If the coverage uses an index color model, select only the visible band.
             * IndexColorModel usually allow only one band, but a custom implementation
             * (MultiBandsIndexColorModel) allows more bands to be specified, with only
             * one of them to be displayed. This is used for remote sensing images with
             * an arbitrary amount of chanels, sometime much more than 3. Selecting the
             * right band now allows faster rendering (the J2SE buildin IndexColorModel
             * is much faster than our custom implementation), and avoid the "Resample"
             * operation to reproject useless bands.
             */
            coverage = coverage.geophysics(false);
            final ColorModel colorModel = coverage.getRenderedImage().getColorModel();
            if (colorModel instanceof IndexColorModel) {
                final int[] visibleBands = new int[] {
                    LegacyGCSUtilities.getVisibleBand(coverage.getRenderedImage())
                };
                coverage = processor.doOperation("SelectSampleDimension", coverage,
                                                 "SampleDimensions", visibleBands);
            }
            // TODO: add more special cases when needed, for example a ComponentColorModel with
            //       more than 3 or 4 bands. This is needed for rendering remote sensing images
            //       with 5 bands (SPOT), while selecting 3 at a given time for RGB composition.
            /*
             * Now check for coordinate systems, and reproject if needed.
             * The target CS should always be 2D, but the source CS may not.
             */
            final CoordinateSystem sourceCS = coverage.getCoordinateSystem();
            if (!CTSUtilities.getCoordinateSystem2D(sourceCS).equals(
                 CTSUtilities.getCoordinateSystem2D(targetCS), false))
            {
                final int sourceDim = sourceCS.getDimension();
                final int targetDim = targetCS.getDimension();
                if (sourceDim > targetDim) {
                    /*
                     * The display CS is always 2D. But the underlying coverage CS could be
                     * 3D, 4D, etc. in which only the first 2 dimensions are displayed.  If
                     * such a case occurs, then copy the "tail CS" from source CS to target
                     * CS in order to make sure that both source and target CS have the same
                     * dimension, as required by the "Resample" operation.
                     */
                    final CoordinateSystem tailCS = CTSUtilities.getSubCoordinateSystem(
                                                    sourceCS, targetDim, sourceDim);
                    if (tailCS != null) {
                        targetCS = new CompoundCoordinateSystem(
                                       targetCS.getName(null), targetCS, tailCS);
                    }
                }
                try {
                    coverage = processor.doOperation("Resample",         coverage,
                                                     "CoordinateSystem", targetCS);
                } catch (CannotReprojectException exception) {
                    throw new TransformException(exception.getLocalizedMessage(), exception);
                }
            }
        }
        return coverage;
    }

    /**
     * Register or unregister <code>this</code> as a {@link TileObserver}.
     */
    private void setTileObserver(final boolean register) {
        assert Thread.holdsLock(getTreeLock());
        if (images != null) {
            for (int i=0; i<images.length; i++) {
                if (images[i] instanceof WritableRenderedImage) {
                    final WritableRenderedImage writable = (WritableRenderedImage) images[i];
                    if (register) {
                        writable.addTileObserver(this);
                    } else {
                        writable.removeTileObserver(this);
                    }
                }
            }
        }
    }

    /**
     * Initialize this object after a change of <strong>rendering</strong> grid coverage. This
     * change may occurs as a result of {@link #setGridCoverage} or {@link #setCoordinateSystem}.
     * This method constructs a chain of images with lower resolution.  Together with tiling,
     * decimation is an attempt to speed up the rendering of large images. This method try to
     * constructs a shared instance of decimated images,  in order to allow many views of the
     * same grid coverage without duplicating the decimated images.
     *
     * @task REVISIT: <code>RenderedImage[]</code> caching may not be wanted if the user provided
     *                custom rendering hints, since the cached images may use different ones.
     */
    private void initialize() {
        assert Thread.holdsLock(getTreeLock());
        setTileObserver(false);
        clearCache();
        if (coverage == null) {
            images             = null;
            preferredArea      = null;
            preferredPixelSize = null;
            zOrder  = DEFAULT_Z_ORDER;
            return;
        }
        final Envelope envelope = coverage.getEnvelope();
        final GridRange   range = coverage.getGridGeometry().getGridRange();
        preferredArea = envelope.getSubEnvelope(0, 2).toRectangle2D();
        zOrder = envelope.getDimension()>=3 ? (float)envelope.getCenter(2) : DEFAULT_Z_ORDER;
        preferredPixelSize = new XDimension2D.Double(envelope.getLength(0)/range.getLength(0),
                                                     envelope.getLength(1)/range.getLength(1));
        /*
         * The rest of this method compute the chain of decimated images.
         * In input,  this method use the following fields: {@link #coverage}, {@link #renderer}.
         * In output, this method ovewrite the following field: {@link #images}.
         */
        if (!USE_PYRAMID) {
            images = null;
            level  = 0;
            return;
        }
        /*
         * Compute the number of levels-1 (i.e. compute the maximal level allowed).
         * If the result is 0, then there is no point to construct a chain of images.
         */
        final PlanarImage image = PlanarImage.wrapRenderedImage(coverage.getRenderedImage());
        final int maxLevel = Math.max(0, (int)(Math.log((double)MIN_SIZE/
                             Math.max(image.getWidth(), image.getHeight()))/LOG_DOWN_SAMPLER));

        synchronized (RenderedGridCoverage.class) {
            /*
             * Verify if a chain of images already exists. This chain
             * can be shared among many 'RenderedGridCoverage' instances.
             */
            if (sharedImages != null) {
                images = (PlanarImage[]) sharedImages.get(coverage);
                if (images != null) {
                    assert images.length == maxLevel+1;
                    setTileObserver(true);
                    return;
                }
            }
            /*
             * Set the rendering hints and gets the JAI instance to use for the chain of scaled
             * images.  Reminder: JAI will differ the execution of "Scale" operation until they
             * are requested, and only requested tiles will be computed.
             */
            RenderingHints hints = ImageUtilities.getRenderingHints(image);
            if (renderer!=null && renderer.hints!=null) {
                if (hints != null) {
                    hints.add(renderer.hints);
                } else {
                    hints = new RenderingHints(null);
                }
                if (hints.get(JAI.KEY_IMAGE_LAYOUT) == null) {
                    hints.put(JAI.KEY_IMAGE_LAYOUT, new ImageLayout());
                }
            } else if (hints==null) {
                hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout());
            }
            final ImageLayout layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
            final JAI jai  = (JAI) getJAI(hints);
            this.images    = new PlanarImage[maxLevel+1];
            this.images[0] = getDeferredImage(image);
            this.level     = 0;
            float scale    = DOWN_SAMPLER;
            /*
             * Set the parameters and loop over each level in the pyramid.
             */
            final ParameterBlock parameters = new ParameterBlock();
            parameters.addSource(image);
            for (int i=1; i<=maxLevel; i++) {
                parameters.removeParameters();
                parameters.add(scale); // xScale
                parameters.add(scale); // yScale
                /*
                 * Set the tile layout and create the scaled image.
                 */
                final ImageLayout ly = (ImageLayout) layout.clone();
                ly.setTileWidth (getTileSize(ly.getTileWidth (image), image.getWidth(),  scale));
                ly.setTileHeight(getTileSize(ly.getTileHeight(image), image.getHeight(), scale));
                hints.put(JAI.KEY_IMAGE_LAYOUT, ly);
                images[i] = getDeferredImage(jai.createNS("Scale", parameters, hints));
                assert Math.max(images[i].getWidth(), images[i].getHeight()) >= MIN_SIZE;
                scale *= DOWN_SAMPLER;
            }
            if (sharedImages == null) {
                sharedImages = new WeakHashMap();
            }
            sharedImages.put(coverage, images);
            setTileObserver(true);
        }
    }

    /**
     * Returns the value for the key {@link Hints#JAI_INSTANCE}.  If no value is found
     * for this key in the specified rendering hints, the value will be inherited from
     * {@link Hints#GRID_COVERAGE_PROCESSOR}. If no value is defined their neither,
     * then this method returns {@link JAI#getDefaultInstance()}.
     *
     * @param  hints The rendering hints, or <code>null</code>.
     * @return The {@link JAI} instance as an object in order to avoid too early class loading.
     */
    private static Object getJAI(final RenderingHints hints) {
        if (hints != null) {
            Object jai = hints.get(Hints.JAI_INSTANCE);
            if (jai != null) {
                return jai;
            }
            final GridCoverageProcessor processor;
            processor = (GridCoverageProcessor) hints.get(Hints.GRID_COVERAGE_PROCESSOR);
            if (processor != null) {
                jai = processor.getRenderingHint(Hints.JAI_INSTANCE);
                if (jai != null) {
                    return jai;
                }
            }
        }
        return JAI.getDefaultInstance();
    }

    /**
     * Suggest a tile size for the specified scale factor. This method performs the following steps:
     * <ul>
     *   <li>Scale the tile as of <code>tileSize*scale</code>.</li>
     *   <li>Find the closest multiple of the original <code>tileSize</code>.</li>
     *   <li>If necessary, reduces the tile size in order to gets an integer number
     *       of tiles in <code>imageSize*scale</code>.</li>
     * </ul>
     */
    private static int getTileSize(int tileSize, int imageSize, final float scale) {
        imageSize   = Math.round(imageSize*scale);
        int reduced = Math.round(tileSize*scale);
        reduced *= Math.max(Math.round((float)tileSize/(float)reduced), 1);
        if (reduced >= imageSize) {
            return imageSize;
        }
        tileSize = reduced;
        while (reduced >= MIN_TILE_SIZE) {
            if ((imageSize % reduced) == 0) {
                return reduced;
            }
            reduced--;
        }
        return tileSize;
    }

    /**
     * Returns a deferred view of the specified image.
     */
    private static PlanarImage getDeferredImage(PlanarImage image) {
        if (ENABLE_DEFFERED_PAINTING) {
            if (!(image instanceof DeferredPlanarImage)) {
                image = new DeferredPlanarImage(image);
            }
        }
        return image;
    }

    /**
     * Returns the grid coverage, or <code>null</code> if none. This is the grid coverage
     * given to the last call of {@link #setGridCoverage}. The rendered grid coverage may
     * not be the same, since a map projection may be applied at rendering time.
     */
    public GridCoverage getGridCoverage() {
        return sourceCoverage;
    }

    /**
     * Returns the name of this layer.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return This layer's name.
     */
    public String getName(final Locale locale) {
        synchronized (getTreeLock()) {
            if (coverage == null) {
                return super.getName(locale);
            }
            return coverage.getName(locale) + " (" + super.getName(locale) + ')';
        }
    }

    /**
     * Set the rendering coordinate system for this layer.
     *
     * @param  cs The coordinate system.
     * @throws TransformException If <code>cs</code> if the grid coverage
     *         can't be resampled to the specified coordinate system.
     */
    protected void setCoordinateSystem(final CoordinateSystem cs) throws TransformException {
        final GridCoverage newCoverage = project(sourceCoverage, cs, renderer);
        synchronized (getTreeLock()) {
            super.setCoordinateSystem(cs);
            // Change the coverage only after the projection succed.
            coverage = newCoverage;
            initialize();
        }
    }

    /**
     * Returns the preferred area for this layer. If no preferred area has been explicitely
     * set, then this method returns the grid coverage's bounding box.
     */
    public Rectangle2D getPreferredArea() {
        synchronized (getTreeLock()) {
            final Rectangle2D area = super.getPreferredArea();
            if (area != null) {
                return area;
            }
            return (preferredArea!=null) ? (Rectangle2D) preferredArea.clone() : null;
        }
    }

    /**
     * Returns the preferred pixel size in rendering coordinates. If no preferred pixel size
     * has been explicitely set, then this method returns the grid coverage's pixel size.
     */
    public Dimension2D getPreferredPixelSize() {
        synchronized (getTreeLock()) {
            final Dimension2D size = super.getPreferredPixelSize();
            if (size != null) {
                return size;
            }
            return (preferredPixelSize!=null) ? (Dimension2D) preferredPixelSize.clone() : null;
        }
    }

    /**
     * Returns the <var>z-order</var> for this layer. If the grid coverage
     * has at least 3 dimension, then the default <var>z-order</var> is
     *
     * <code>gridCoverage.getEnvelope().getCenter(2)</code>.
     *
     * Otherwise, the default value is {@link Float#NEGATIVE_INFINITY} in order to paint
     * the coverage under anything else.  The default value can be overriden with a call
     * to {@link #setZOrder}.
     */
    public float getZOrder() {
        synchronized (getTreeLock()) {
            if (isZOrderSet()) {
                return super.getZOrder();
            }
            return zOrder;
        }
    }

    /**
     * Hints that this layer might be painted in the near future. The default implementation
     * determines which tiles are going to be drawn and invokes {@link PlanarImage#prefetchTiles}.
     *
     * @param  context Information relatives to the rendering context.
     */
    protected void prefetch(final RenderingContext context) {
        assert Thread.holdsLock(getTreeLock());
        if (coverage!=null) try {
            if (USE_PYRAMID) {
                paint(context, false);
            } else {
                Rectangle2D area = context.getPaintingArea(coverage.getCoordinateSystem()).getBounds2D();
                if (area!=null && !area.isEmpty()) {
                    coverage.prefetch(area);
                }
            }
        } catch (TransformException exception) {
            Renderer.handleException("RenderedGridCoverage", "prefetch", exception);
            // Not a big deal, since this method is just a hint. Ignore...
        }
    }

    /**
     * Paint the grid coverage.
     *
     * @param  context Information relatives to the rendering context.
     * @throws TransformException If a coordinate transformation failed
     *         during the rendering process.
     */
    protected void paint(final RenderingContext context) throws TransformException {
        paint(context, true);
    }

    /**
     * Implementation of the <code>paint</code> method. If <code>doDraw</code> is false,
     * then this method invokes {@link PlanarImage#prefetchTiles} rather than painting
     * the image now.
     *
     * @param  context Information relatives to the rendering context.
     * @param  doDraw <code>true</code> for performing the actual drawing,
     *         or <code>false</code> for prefetching tiles.
     * @throws TransformException If a coordinate transformation failed
     *         during the rendering process.
     */
    private void paint(final RenderingContext context, final boolean doDraw)
            throws TransformException
    {
        assert Thread.holdsLock(getTreeLock());
        if (coverage == null) {
            return;
        }
        assert CTSUtilities.getCoordinateSystem2D(coverage.getCoordinateSystem())
                           .equals(context.mapCS, false) : coverage;
        /*
         * Computes the 'gridToCS' transform to use for rendering the image.  This is the
         * "grid to coordinat system" transform from the coverage, except if we are using
         * a decimated image for faster rendering.  In the later case, the transform will
         * have to be scaled in order to match the image scaling. In any case, a 1/2 pixel
         * translation is performed last in order to maps pixels upper-left corner (the
         * difference is insignifiant under wide zoom, but huge on close zoom).
         */
        try {
            gridToCS.setTransform((AffineTransform) coverage.getGridGeometry()
                                                    .getGridToCoordinateSystem2D());
        } catch (ClassCastException exception) {
            throw new TransformException(Resources.getResources(getLocale()).getString(
                                         ResourceKeys.ERROR_NON_AFFINE_TRANSFORM), exception);
        }
        final Graphics2D graphics = context.getGraphics();
        PlanarImage image; // The image to display (will be computed below).
        if (images == null) {
            image = PlanarImage.wrapRenderedImage(coverage.getRenderedImage());
        } else {
            /*
             * Compute which level (i.e. which decimated image)  is more appropriate for the
             * current zoom. If level different than 0 (the original image) is choosen, then
             * then the 'gridToCS' transform will need to be adjusted.
             */
            final AffineTransform gridToDevice = graphics.getTransform();
            gridToDevice.concatenate(gridToCS);
            final int level = Math.max(0,
                              Math.min(images.length-1,
                              (int)(Math.log(Math.max(XAffineTransform.getScaleX0(gridToDevice),
                              XAffineTransform.getScaleY0(gridToDevice)))/LOG_DOWN_SAMPLER)));
            if (level != 0) {
                final double scale = Math.pow(DOWN_SAMPLER, -level);
                gridToCS.scale(scale, scale);
            }
            if (level != this.level) {
                this.level = level;
                levelChanged();
            }
            image = images[level];
        }
        gridToCS.translate(-0.5, -0.5); // Map to upper-left corner.
        /*
         * If this method is invoked from the 'prefetch' method, then prefetch tiles in
         * a background thread but do not paint them yet. This block may be run a few
         * milliseconds before the actual rendering occurs.
         */
        if (!doDraw) {
            final Shape clip = graphics.getClip();
            if (clip != null) {
                Rectangle2D bounds = clip.getBounds2D();
                final AffineTransform gridToDevice = graphics.getTransform();
                gridToDevice.concatenate(gridToCS);
                try {
                    bounds = XAffineTransform.inverseTransform(gridToDevice, bounds, bounds);
                    Rectangle tileIndices = new Rectangle();
                    tileIndices.setRect(bounds);
                    final Point[] indices = image.getTileIndices(tileIndices);
                    if (indices != null) {
                        image.prefetchTiles(indices);
                    }
                } catch (NoninvertibleTransformException exception) {
                    // Should not happen in most cases
                    throw new TransformException(exception.getLocalizedMessage(), exception);
                }
            }
            return;
        }
        /*
         * Paint the image. If printing, then the image should be fully painted
         * immediately (no deferred painting).
         */
        if (context.isPrinting()) {
            while (image instanceof DeferredPlanarImage) {
                image = ((DeferredPlanarImage) image).getSourceImage(0);
            }
        }
        graphics.drawRenderedImage(image, gridToCS);
        context.addPaintedArea(preferredArea, context.mapCS);
    }

    /**
     * Notify that a tile in the {@link GridCoverage#getRenderedImage coverage's image} has
     * been updated. The default implementation {@link #repaint repaint} the modified tiles
     * if <code>willBeWritable</code> is <code>false</code> (i.e. the update is finished).
     *
     * @param   source The image that owns the tile.
     * @param   tileX  The X index of the tile that is being updated.
     * @param   tileY  The Y index of the tile that is being updated.
     * @param   willBeWritable If true, the tile will be grabbed for writing;
     *          otherwise it is being released.
     */
    public void tileUpdate(final WritableRenderedImage source,
                           final int tileX, final int tileY,
                           final boolean willBeWritable)
    {
        if (!willBeWritable) {
            Rectangle2D bounds;
            if (images!=null && images.length>level) {
                final PlanarImage image = images[level];
                bounds = new Rectangle2D.Double(image.tileXToX(tileX  ), image.tileYToY(tileY  ),
                                                image.tileXToX(tileX+1), image.tileYToY(tileY+1));
                bounds = XAffineTransform.transform(gridToCS, bounds, bounds);
            } else {
                bounds = null;
            }
            repaint(bounds);
        }
    }

    /**
     * Invoked when the level changed during a rendering operation. The <code>paint</code> method
     * takes care of updating important fields. This method just log a message to the user.  This
     * message help to track performance issues.
     */
    private void levelChanged() {
        final Logger logger = Renderer.LOGGER;
        if (logger.isLoggable(Level.FINE)) {
            final Locale locale = getLocale();
            final LogRecord record = Resources.getResources(locale).getLogRecord(
                               Level.FINE, ResourceKeys.RESSAMPLING_RENDERED_IMAGE_$3,
                               coverage.getName(locale),
                               new Integer(level), new Integer(images.length));
            record.setSourceClassName(Utilities.getShortClassName(this));
            record.setSourceMethodName("paint");
            logger.log(record);
        }
    }

    /**
     * Format a value for the current mouse position. This method append in
     * <code>toAppendTo</code> the value in each bands for the pixel at the
     * mouse position. For example if the current image show Sea Surface
     * Temperature (SST), then this method will format the temperature in
     * geophysical units (e.g. "12°C").
     *
     * @param  event The mouse event.
     * @param  toAppendTo The destination buffer for formatting a value.
     * @return <code>true</code> if this method has formatted a value, or <code>false</code>
     *         otherwise.
     */
    boolean formatValue(final GeoMouseEvent event, final StringBuffer toAppendTo) {
        synchronized (getTreeLock()) {
            if (sourceCoverage == null) {
                return false;
            }
            try {
                point = event.getCoordinate(sourceCoverage.getCoordinateSystem(), point);
            } catch (TransformException exception) {
                // Can't transform the point. It may occurs if the mouse cursor is
                // far away from the area of coordinate system validity. Ignore...
                return false;
            }
            final Locale locale = getLocale();
            try {
                values = sourceCoverage.evaluate(point, values);
            } catch (PointOutsideCoverageException exception) {
                // Point is outside grid coverage. This is normal and we should not print any
                // message here. We could test if the point is inside the grid coverage before
                // invoking the 'evaluate' method, but it would slow down the normal case where
                // the point is inside. Catching the exception slow down the case where the point
                // is outside instead. It is one or the other, we had to choose...
                return false;
            } catch (CannotEvaluateException exception) {
                // The point can't be evaluated for some other reason. Append an error flag.
                toAppendTo.append(Resources.getResources(locale).getString(ResourceKeys.ERROR));
                return true;
            }
            if (bands == null) {
                bands = sourceCoverage.getSampleDimensions();
            }
            boolean modified = false;
            for (int i=0; i<values.length; i++) {
                final String text = bands[i].getLabel(values[i], locale);
                if (text != null) {
                    if (modified) {
                        toAppendTo.append(", ");
                    }
                    toAppendTo.append(text);
                    modified = true;
                }
            }
            return modified;
        }
    }

    /**
     * Efface les informations qui avaient été
     * sauvegardées dans la cache interne.
     */
    void clearCache() {
        point  = null;
        values = null;
        bands  = null;
        super.clearCache();
    }

    /**
     * Provides a hint that a layer will no longer be accessed from a reference in user
     * space. The results are equivalent to those that occur when the program loses its
     * last reference to this layer, the garbage collector discovers this, and finalize
     * is called. This can be used as a hint in situations where waiting for garbage
     * collection would be overly conservative.
     */
    public void dispose() {
        synchronized (getTreeLock()) {
            super.dispose();
            setTileObserver(false);
            if (coverage != null) {
                /*
                 * We will not dispose the planar image if there is any
                 * chance that it is referenced outside of this class.
                 */
                final RenderedImage image = coverage.getRenderedImage();
                if (!LegacyGCSUtilities.uses(sourceCoverage.geophysics(false), image)) {
                    if (images != null) {
                        synchronized (RenderedGridCoverage.class) {
                            sharedImages.remove(coverage);
                        }
                        for (int i=0; i<images.length; i++) {
                            images[i].dispose();
                        }
                    }
                    if (image instanceof PlanarImage) {
                        ((PlanarImage) image).dispose();
                    }
                }
            }
            // Do not dispose images, since they may be used
            // by an other RenderedGridCoverage instance.
            coverage           = null;
            sourceCoverage     = null;
            preferredArea      = null;
            preferredPixelSize = null;
            images             = null;
            level              = 0;
            zOrder = DEFAULT_Z_ORDER;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    




    // STEVE ANSARI stuff

    public void paint(final Graphics2D graphics, final Rectangle2D renderExtent) throws TransformException
    {
    	/*
    	 * Computes the 'gridToCS' transform to use for rendering the image.  This is the
    	 * "grid to coordinat system" transform from the coverage, except if we are using
    	 * a decimated image for faster rendering.  In the later case, the transform will
    	 * have to be scaled in order to match the image scaling. In any case, a 1/2 pixel
    	 * translation is performed last in order to maps pixels upper-left corner (the
    	 * difference is insignifiant under wide zoom, but huge on close zoom).
    	 */
    	try {
    		gridToCS.setTransform((AffineTransform) coverage.getGridGeometry()
    				.getGridToCoordinateSystem2D());
    	} catch (ClassCastException exception) {
    		exception.printStackTrace();
    		throw new TransformException(Resources.getResources(getLocale()).getString(
    				ResourceKeys.ERROR_NON_AFFINE_TRANSFORM), exception);
    	}
//    	PlanarImage image = PlanarImage.wrapRenderedImage(coverage.getRenderedImage());
    	
    	gridToCS.translate(-0.5, -0.5); // Map to upper-left corner.
    	/*
    	 * If this method is invoked from the 'prefetch' method, then prefetch tiles in
    	 * a background thread but do not paint them yet. This block may be run a few
    	 * milliseconds before the actual rendering occurs.
    	 */
//    	if (!doDraw) {
//    		final Shape clip = graphics.getClip();
//    		if (clip != null) {
//    			Rectangle2D bounds = clip.getBounds2D();
//    			final AffineTransform gridToDevice = graphics.getTransform();
//    			gridToDevice.concatenate(gridToCS);
//    			try {
//    				bounds = XAffineTransform.inverseTransform(gridToDevice, bounds, bounds);
//    				Rectangle tileIndices = new Rectangle();
//    				tileIndices.setRect(bounds);
//    				final Point[] indices = image.getTileIndices(tileIndices);
//    				if (indices != null) {
//    					image.prefetchTiles(indices);
//    				}
//    			} catch (NoninvertibleTransformException exception) {
//    				// Should not happen in most cases
//    				throw new TransformException(exception.getLocalizedMessage(), exception);
//    			}
//    		}
//    		return;
//    	}
    	/*
    	 * Paint the image. If printing, then the image should be fully painted
    	 * immediately (no deferred painting).
    	 */
//    	if (context.isPrinting()) {
//    		while (image instanceof DeferredPlanarImage) {
//    			image = ((DeferredPlanarImage) image).getSourceImage(0);
//    		}
//    	}
    	
    	
    	
//    	AffineTransform at = XAffineTransform.transform(
//    			AffineTransform.getTranslateInstance(0, 0), 
//    			coverage.getEnvelope().toRectangle2D(), 
//    			renderExtent);
    	AffineTransform at = new AffineTransform();
    	int zoom = 2;
//    	at.translate(
//    		    (coverage.getRenderedImage().getWidth()/2.0) - (coverage.getRenderedImage().getWidth()*(zoom))/2.0,
//    		    (coverage.getRenderedImage().getHeight()/2.0) - (coverage.getRenderedImage().getHeight()*(zoom))/2.0
//    		);
//    	at.scale(zoom,zoom);


    	System.out.println("AFFINE TRANSFORM: : : : : : : : : : : : "+at);
    	
    	

    	
//    	graphics.drawRenderedImage(image, gridToCS);
//    	graphics.drawRenderedImage(coverage.getRenderedImage(), gridToCS);
    	
    	graphics.drawRenderedImage(coverage.getRenderedImage(), at);
    	
    	
//    	context.addPaintedArea(preferredArea, context.mapCS);
    }

}
