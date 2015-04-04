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
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.media.jai.GraphicsJAI;
import javax.media.jai.PlanarImage;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import opendap.util.Tools;

import org.geotools.cs.AxisInfo;
import org.geotools.cs.AxisOrientation;
import org.geotools.cs.CompoundCoordinateSystem;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.FittedCoordinateSystem;
import org.geotools.cs.LocalCoordinateSystem;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransformFactory;
import org.geotools.gp.GridCoverageProcessor;
import org.geotools.renderer.Renderer2D;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.GraphicsUtilities;
import org.geotools.resources.Utilities;
import org.geotools.resources.XArray;
import org.geotools.resources.geometry.XAffineTransform;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.resources.renderer.Resources;
import org.geotools.units.Unit;
import org.geotools.units.UnitException;
import org.geotools.util.RangeSet;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;


/**
 * A renderer for drawing map objects into a {@link Graphics2D}. A newly constructed
 * <code>Renderer</code> is initially empty. To make something appears, {@link RenderedLayer}s
 * must be added using one of <code>addLayer(...)</code> methods. The visual content depends of
 * the <code>RenderedLayer</code> subclass. It may be an isoline ({@link RenderedGeometries}),
 * a remote sensing image ({@link RenderedGridCoverage}), a set of arbitrary marks
 * ({@link RenderedMarks}), a map scale ({@link RenderedMapScale}), etc.
 *
 * @version $Id: Renderer.java 10254 2005-01-05 02:53:49Z jmacgill $
 * @author Martin Desruisseaux
 */
public class Renderer implements Renderer2D {
    /**
     * The logger for the Java2D renderer module.
     */
    static final Logger LOGGER = Logger.getLogger("org.geotools.renderer.j2d");

    /**
     * Small value for avoiding rounding error.
     */
    private static final double EPS = 1E-6;



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////         RenderedLayers (a.k.a "Layers")          ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Objet utilisé pour comparer deux objets {@link RenderedLayer}.
     * Ce comparateur permettra de classer les {@link RenderedLayer}
     * par ordre croissant d'ordre <var>z</var>.
     */
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(final Object layer1, final Object layer2) {
            return Float.compare(((RenderedLayer)layer1).getZOrder(),
                                 ((RenderedLayer)layer2).getZOrder());
        }
    };

    /**
     * An empty list of layers.
     */
    private static final RenderedLayer[] EMPTY = new RenderedLayer[0];

    /**
     * The set of {@link RenderedLayer} to display. Named "layers" here because each
     * {@link RenderedLayer} has its own <var>z</var> value and layer are painted in
     * increasing <var>z</var> order (i.e. layers with a hight <var>z</var> value are
     * painted on top of layers with a low <var>z</var> value).
     */
    private RenderedLayer[] layers;

    /**
     * The number of valid elements in {@link #layers}.
     */
    private int layerCount;

    /**
     * Tells if elements in {@link #layers} are sorted in increasing <var>z</var> value.
     * If <code>false</code>, then <code>Arrays.sort(layers, COMPARATOR)</code> need to
     * be invoked.
     */
    private boolean layerSorted;

    /**
     * The offscreen buffers.  There is one {@link VolatileImage} or {@link BufferedImage}
     * for each range in the {@link #offscreenZRanges} set. If non-null, this array length
     * must be equals to the size of {@link #offscreenZRanges}.
     *
     * @see #setOffscreenBuffered
     */
    private transient Image[] offscreenBuffers;

    /**
     * Tells if an offscreen buffer is of type {@link VolatileImage}.
     * If <code>false</code>, then it is of type {@link BufferedImage}.
     */
    private boolean[] offscreenIsVolatile;

    /**
     * Tells if an offscreen buffer need to be repaint. This array length must be
     * equals to the length of {@link #offscreenBuffers}.
     */
    private transient boolean[] offscreenNeedRepaint;

    /**
     * The ranges of {@linkplain RenderedLayer#getZOrder z-order} for which to create an
     * offscreen buffer. There is one offscreen buffer (usually a {@link VolatileImage})
     * for each range of z-orders.
     *
     * @see #setOffscreenBuffered
     */
    private RangeSet offscreenZRanges;



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////        Coordinate systems and transforms         ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Axis orientations in the Java2D space. Coordinates are in "dots" (about 1/72 of inch),
     * <var>x</var> values increasing right and <var>y</var> values increasing down. East and
     * South directions are relative to the screen (like {@link java.awt.BorderLayout}), not
     * geographic directions.
     */
    private static final AxisInfo[] TEXT_AXIS = new AxisInfo[] {
        new AxisInfo("Column", AxisOrientation.EAST),
        new AxisInfo("Line",   AxisOrientation.SOUTH)
    };

    /**
     * The coordinate systems for rendering. This object contains the following:
     * <ul>
     *   <li>The "real world" coordinate system ({@link RenderingContext#mapCS mapCS}).
     *       Should never be <code>null</code>, since it is the viewer coordinate system
     *       as returned by {@link #getCoordinateSystem}.</li>
     *   <li>The Java2D coordinate system ({@link RenderingContext#textCS textCS}), which
     *       is rendering-dependent. This CS must be the one used for the last rendering
     *       ({@link GeoMouseEvent rely on it}).</li>
     *   <li>The device coordinate system ({@link RenderingContext#deviceCS deviceCS}), which
     *       is rendering-dependent. This CS must be the one used for the last rendering.</li>
     * </ul>
     *
     * @see RenderingContext#mapCS
     * @see RenderingContext#textCS
     * @see RenderingContext#deviceCS
     */
    private RenderingContext context;

    /**
     * The same context than {@link #context}, except that the {@link RenderingContext#deviceCS}
     * may includes a supplementary translation term set by <cite>Swing</code>. This translation
     * term is encapsulated in <code>clippedContext.deviceCS</code> when rendering a clipped area
     * without offscreen buffering.
     */
    private RenderingContext clippedContext;

    /**
     * The scale factor from the last rendering, or <code>null</code> if unknow.
     * This scale factor is computed at rendering time after a zoom change. All
     * registered listeners will be notified of any scale change.
     */
    private Float scaleFactor;

    /**
     * The affine transform from the units used in {@link RenderingContext#mapCS mapCS} to
     * "dots" units. A dots is equals to 1/72 of inch. This transform is basically nothing
     * else than a unit conversion;  we still in "real world" CS  (not yet the screen CS).
     * This transform is used as a convenient step in the computation of a realistic scale
     * factor.
     */
    private final AffineTransform normalizeToDots = new AffineTransform();

    /**
     * The affine transform from {@link RenderingContext#mapCS mapCS} to
     * {@link RenderingContext#textCS textCS} used in the last rendering.
     * This is the <code>zoom</code> argument given to {@link #paint}
     * during the last rendering.
     */
    private final AffineTransform mapToText = new AffineTransform();

    /**
     * The affine transform from {@link RenderingContext#textCS textCS} to
     * {@link RenderingContext#deviceCS deviceCS} used in the last rendering.
     * This transform is set as if no clipping were performed by <cite>Swing</cite>.
     * This transform should contains only <var>x</var> and <var>y</var> translation
     * terms (usually 0). They are the {@link Rectangle#x} and {@link Rectangle#y}
     * values of the <code>zoomableBounds</code> argument given to {@link #paint}
     * during the last rendering.
     */
    private final AffineTransform textToDevice = new AffineTransform();

    /**
     * A set of {@link MathTransform}s from various source CS. The target CS must be
     * {@link RenderingContext#mapCS} for all entries. Keys are source CS.  This map
     * is used only in order to avoid the costly call to
     * {@link CoordinateTransformationFactory#createFromCoordinateSystems} as much as
     * possible. If a transformation is not available in this collection, then the usual
     * {@link #factory} will be used.
     */
    private final Map transforms = new WeakHashMap();

    /**
     * The factory to use for creating {@link CoordinateTransformation} objects.
     * This factory can be set by {@link #setRenderingHint}.
     */
    private CoordinateTransformationFactory factory = CoordinateTransformationFactory.getDefault();



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////        Rendering hints (e.g. resolution)         ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * A set of rendering hints. Recognized hints include
     * {@link Hints#COORDINATE_TRANSFORMATION_FACTORY} and
     * any of {@link Hints#FINEST_RESOLUTION}.
     *
     * @see Hints#FINEST_RESOLUTION
     * @see Hints#REQUIRED_RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     * @see Hints#PREFETCH
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    protected final RenderingHints hints = new RenderingHints(null);

    /**
     * <code>true</code> if the renderer is allowed to prefetch data before to
     * paint layers.   Prefetching data may speed up rendering on machine with
     * more than one processor.
     */
    private boolean prefetch = (Runtime.getRuntime().availableProcessors() >= 2);

    /**
     * The rendering resolutions, in units of {@link RenderingContext#textCS} coordinate system
     * (usually 1/72 of inch). A larger resolution speed up rendering, while a smaller resolution
     * draw more precise map. The value can be set with {@link #setRenderingHint}. They are read
     * only by {@link RenderedGeometries}.
     */
    float minResolution, maxResolution;

    /**
     * The bounding box of all {@link RenderedLayer} in the {@link RenderingContext#mapCS}
     * coordinate system. This box is computed from {@link RenderedLayer#getPreferredArea}.
     * A <code>null</code> value means that none of them returned a non-null value.
     */
    private Rectangle2D preferredArea;

    /**
     * Statistics about rendering. Used for logging messages only.
     */
    final RenderingStatistics statistics = new RenderingStatistics();



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////         AWT and Swing container / events         ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * The component owner, or <code>null</code> if none. This is used for managing
     * repaint request (see {@link RenderedLayer#repaint}) or mouse events.
     */
    final Component mapPane;

    /**
     * Listeners to be notified about any changes in this layer's properties.
     * Examples of properties that may change:
     * <code>"preferredArea"</code>.
     */
    protected final PropertyChangeSupport listeners;

    /**
     * Listener for events of interest to this renderer. Events may come
     * from any {@link RenderedLayer} or from the {@link Component}.
     */
    private final ListenerProxy listenerProxy = new ListenerProxy();

    /**
     * Classe ayant la charge de réagir aux différents événements qui intéressent cet
     * objet <code>Renderer</code>. Cette classe réagira entre autres aux changements
     * de l'ordre <var>z</var> ainsi qu'aux changements des coordonnées géographiques
     * d'une couche.
     */
    private final class ListenerProxy extends ComponentAdapter implements PropertyChangeListener {
        /** Invoked when the component's size changes. */
        public void componentResized(final ComponentEvent event) {
            synchronized (Renderer.this) {
                zoomChanged(null);
            }
        }

        /** Invoked when the component's position changes. */
        public void componentMoved(final ComponentEvent event) {
            synchronized (Renderer.this) {
                zoomChanged(null); // Translation term has changed.
            }
        }

        /** Invoked when the component has been made invisible. */
        public void componentHidden(final ComponentEvent event) {
            synchronized (Renderer.this) {
                clearCache();
            }
            // As a symetrical approach, it would be nice to invokes 'prefetch(...)' inside
            // 'componentShown(...)' too. But we don't know for sure what the widget bounds
            // and the zoom will be. We are better to wait until 'paint(...)' is invoked.
        }

        /** Invoked when a {@link RenderedLayer}'s property is changed. */
        public void propertyChange(final PropertyChangeEvent event) {
            final String propertyName = event.getPropertyName();
            synchronized (Renderer.this) {
                if (propertyName.equalsIgnoreCase("preferredArea")) {
                    changePreferredArea((Rectangle2D)    event.getOldValue(),
                                        (Rectangle2D)    event.getNewValue(),
                                        ((RenderedLayer) event.getSource()).getCoordinateSystem(),
                                        "RenderedLayer", "setPreferredArea");
                    return;
                }
                if (propertyName.equalsIgnoreCase("zOrder")) {
                    layerSorted = false;
                    return;
                }
                if (propertyName.equalsIgnoreCase("coordinateSystem")) {
                    computePreferredArea("RenderedLayer", "setCoordinateSystem");
                    return;
                }
                if (propertyName.equalsIgnoreCase("gridCoverage")) {
                    computePreferredArea("RenderedLayer", "setGridCoverage");
                    return;
                }
            }
        }
    }



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////      Constructors and essential properties       ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Construct a new renderer for the specified component.
     *
     * @param owner The widget that own this renderer, or <code>null</code> if none.
     */
    public Renderer(final Component owner) {
        final CoordinateSystem cs = LocalCoordinateSystem.PROMISCUOUS;
        context        = new RenderingContext(this, cs, cs, cs);
        clippedContext = context;
        listeners      = new PropertyChangeSupport(this);
        this.mapPane = owner;
        if (mapPane != null) {
            mapPane.addComponentListener(listenerProxy);
        }
        updateNormalizationFactor(cs);
    }

    /**
     * Returns this renderer's name. The default implementation returns the title
     * of the window which contains the map pane.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return This renderer's name, or <code>null</code> if none.
     *
     * @see #getLocale
     * @see Component#getName
     * @see RenderedLayer#getName
     */
    public synchronized String getName(final Locale locale) {
        for (Component c=mapPane; c!=null; c=c.getParent()) {
            if (c instanceof Frame) {
                return ((Frame) c).getTitle();
            }
            if (c instanceof Dialog) {
                return ((Dialog) c).getTitle();
            }
            if (c instanceof JInternalFrame) {
                return ((JInternalFrame) c).getTitle();
            }
        }
        return null;
    }

    /**
     * Returns to locale for this renderer. The renderer will inherit
     * the locale of its {@link Component}, if it have one. Otherwise,
     * a default locale will be returned.
     *
     * @see Component#getLocale
     * @see JComponent#getDefaultLocale
     * @see Locale#getDefault
     */
    public Locale getLocale() {
        if (mapPane != null) try {
            return mapPane.getLocale();
        } catch (IllegalComponentStateException exception) {
            // Not yet added to a containment hierarchy. Ignore...
            if (mapPane instanceof JComponent) {
                return JComponent.getDefaultLocale();
            }
        }
        return Locale.getDefault();
    }

    /**
     * Returns the current rendering context. Used by {@link GeoMouseEvent}
     * in order to gets a "snapshot" of current coordinate system states.
     */
    final RenderingContext getRenderingContext() {
        // Use the offscreen context, since it doesn't includes the translation
        // term that Swing may have applied when rendering a clipped area.
        assert context.getGraphics() == null;
        return context;
    }

    /**
     * Returns the view coordinate system. This is the "real world" coordinate system
     * used for displaying all {@link RenderedLayer}s. Note that underlying data in
     * <code>RenderedLayer</code>s doesn't need to be in this coordinate system:
     * transformations will performed on the fly as needed at rendering time.
     *
     * @return The two dimensional coordinate system used for display.
     */
    public CoordinateSystem getCoordinateSystem() {
        return context.mapCS;
    }

    /**
     * Set the view coordinate system. This is the "real world" coordinate system
     * to use for displaying all {@link RenderedLayer}s. Layers are notified of the
     * coordinate system change with {@link RenderedLayer#setCoordinateSystem}.
     *
     * @param cs The view coordinate system. If the specified coordinate system has more
     *           than two dimensions, then it must be a {@link CompoundCoordinateSystem}
     *           with a two dimensional {@link CompoundCoordinateSystem#getHeadCS headCS}.
     * @throws TransformException If <code>cs</code> can't be reduced to a two-dimensional
     *         coordinate system, or if data can't be transformed for some other reason.
     */
    public void setCoordinateSystem(CoordinateSystem cs) throws TransformException {
        cs = CTSUtilities.getCoordinateSystem2D(cs);
        final CoordinateSystem oldCS;
        synchronized (this) {
            int changed = 0;
            oldCS = getCoordinateSystem();
            if (!cs.equals(oldCS, false)) {
                try {
                    while (changed < layerCount) {
                        layers[changed].setCoordinateSystem(cs);
                        changed++; // Increment only if previous call succeed.
                    }
                } catch (TransformException exception) {
                    // Roll back all CS changes.
                    while (--changed >= 0) {
                        try {
                            layers[changed].setCoordinateSystem(oldCS);
                        } catch (TransformException unexpected) {
                            // Should not happen, since we are rolling
                            // back to an old CS that previously worked.
                            handleException("Renderer", "setCoordinateSystem", unexpected);
                        }
                    }
                    clearCache();
                    throw exception;
                }
                clearCache();
                CoordinateSystem textCS = createFittedCoordinateSystem("textCS", cs, mapToText);
                clippedContext = context = new RenderingContext(this, cs, textCS, textCS);
                computePreferredArea("Renderer", "setCoordinateSystem");
                updateNormalizationFactor(cs);
                scaleFactor = null;
            }
        }
        listeners.firePropertyChange("coordinateSystem", oldCS, cs);
    }

    /**
     * Update {@link #normalizeToDots} for the specified coordinate system.
     */
    private void updateNormalizationFactor(final CoordinateSystem cs) {
        final Ellipsoid ellipsoid = CTSUtilities.getHeadGeoEllipsoid(cs);
        normalizeToDots.setToScale(getNormalizationFactor(cs.getUnits(0), ellipsoid),
                                   getNormalizationFactor(cs.getUnits(1), ellipsoid));
    }

    /**
     * Returns the amount of "dots" in one unit of the specified unit. There is 72 dots in one
     * inch, and 2.54/100 inchs in one metre.  The <code>unit</code> argument must be a linear
     * or an angular unit.
     *
     * @param unit The unit. If <code>null</code>, then the unit will be assumed to be metres or
     *        degrees depending of whatever <code>ellipsoid</code> is <code>null</code> or not.
     * @param ellipsoid The ellipsoid if the coordinate system is geographic, or <code>null</code>
     *        otherwise.
     */
    private double getNormalizationFactor(Unit unit, final Ellipsoid ellipsoid) {
        double m = 1;
        try {
            if (ellipsoid != null) {
                if (unit == null) {
                    unit = Unit.DEGREE;
                }
                /*
                 * Converts an angular unit to a linear one.   An ellipsoid has two axis that we
                 * could use. For the WGS84 ellipsoid, the semi-major axis results in a nautical
                 * mile of 1855.32 metres  while  the semi-minor axis results in a nautical mile
                 * of 1849.10 metres. The average of semi-major and semi-minor axis results in a
                 * nautical mile of 1852.21 metres, which is pretty close to the internationaly
                 * agreed length (1852 metres). This is consistent with the definition of nautical
                 * mile, which is the length of an angle of 1 minute along the meridian at 45° of
                 * latitude.
                 */
                m = Unit.RADIAN.convert(m, unit) * 0.5*(ellipsoid.getSemiMajorAxis() +
                                                        ellipsoid.getSemiMinorAxis());
                unit = ellipsoid.getAxisUnit();
            }
            if (unit != null) {
                m = Unit.METRE.convert(m, unit);
            }
        } catch (UnitException exception) {
            /*
             * A unit conversion failed. Since this normalizing factor is used only for computing a
             * scale, it is not crucial to the renderer working. Log a warning message and continue.
             * We keep the m value computed so far, which will be assumed to be a length in metres.
             */
            final LogRecord record = Resources.getResources(getLocale()).getLogRecord(Level.WARNING,
                                                     ResourceKeys.WARNING_UNEXPECTED_UNIT_$1, unit);
            record.setSourceClassName ("Renderer");
            record.setSourceMethodName("setCoordinateSystem");
            record.setThrown(exception);
            LOGGER.log(record);
        }
        return 7200/2.54 * m;
    }

    /**
     * Returns a bounding box that completely encloses all layer's {@linkplain
     * RenderedLayer#getPreferredArea preferred area}, visible or not. This
     * bounding box should be representative of the geographic area to drawn.
     * Coordinates are expressed in this {@linkplain #getCoordinateSystem
     * renderer's coordinate system}.
     *
     * @return The enclosing area computed from available data, or <code>null</code>
     *         if this area can't be computed.
     */
    public Rectangle2D getPreferredArea() {
        final Rectangle2D area = this.preferredArea;
        return (area!=null) ? (Rectangle2D) area.clone() : null;
    }

    /**
     * Set the geographic area. This is method is invoked as a result of
     * internal computation. User should not call this method directly.
     *
     * @param newArea The new preferred area (will not be cloned).
     */
    private void setPreferredArea(final Rectangle2D newArea) {
        final Rectangle2D oldArea;
        synchronized (this) {
            oldArea = preferredArea;
            preferredArea = newArea;
        }
        listeners.firePropertyChange("preferredArea", oldArea, newArea);
    }

    /**
     * Recompute inconditionnaly the {@link #preferredArea}. Will be computed
     * from the value returned by {@link RenderedLayer#getPreferredArea} for
     * all layers.
     *
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     */
    private void computePreferredArea(final String sourceClassName, final String sourceMethodName) {
        assert Thread.holdsLock(this);
        Rectangle2D         newArea = null;
        CoordinateSystem lastSystem = null;
        MathTransform2D   transform = null;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            Rectangle2D bounds = layer.getPreferredArea();
            if (bounds != null) {
                // In theory, the RendererLayer should use the same CS than this Renderer.
                // However, as a safety, we will check for coordinate transformations anyway.
                final CoordinateSystem coordinateSystem = layer.getCoordinateSystem();
                try {
                    if (lastSystem==null || !lastSystem.equals(coordinateSystem, false)) {
                        transform  = (MathTransform2D) getMathTransform(
                                                       coordinateSystem, getCoordinateSystem(),
                                                       sourceClassName, sourceMethodName);
                        lastSystem = coordinateSystem;
                    }
                    bounds = CTSUtilities.transform(transform, bounds, null);
                    if (newArea == null) {
                        newArea = bounds;
                    } else {
                        newArea.add(bounds);
                    }
                } catch (TransformException exception) {
                    handleException(sourceClassName, sourceMethodName, exception);
                    // Continue. The preferred area for this layer will be ignored.
                }
            }
        }
        setPreferredArea(newArea);
    }

    /**
     * Remplace un rectangle par un autre dans le calcul de {@link #preferredArea}. Si ça a eu
     * pour effet de changer les coordonnées géographiques couvertes, un événement approprié
     * sera lancé. Cette méthode est plus économique que {@link #computePreferredArea} du fait
     * qu'elle essaie de ne pas tout recalculer. Si on n'a pas pu faire l'économie d'un recalcul
     * toutefois, alors {@link #computePreferredArea} sera appelée.
     *
     * @param  oldSubArea       The old preferred area of the layer that changed.
     * @param  newSubArea       The new preferred area of the layer that changed.
     * @param  coordinateSystem The coordinate system for <code>[old|new]SubArea</code>.
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     */
    private void changePreferredArea(Rectangle2D oldSubArea,
                                     Rectangle2D newSubArea,
                                     final CoordinateSystem coordinateSystem,
                                     final String sourceClassName,
                                     final String sourceMethodName)
    {
        assert Thread.holdsLock(this);
        try {
            final MathTransform2D transform = (MathTransform2D) getMathTransform(
                    coordinateSystem, getCoordinateSystem(), sourceClassName, sourceMethodName);
            oldSubArea = CTSUtilities.transform(transform, oldSubArea, null);
            newSubArea = CTSUtilities.transform(transform, newSubArea, null);
        } catch (TransformException exception) {
            handleException(sourceClassName, sourceMethodName, exception);
            computePreferredArea(sourceClassName, sourceMethodName);
            return;
        }
        final Rectangle2D expandedArea = changeArea(preferredArea, oldSubArea, newSubArea);
        if (expandedArea != null) {
            setPreferredArea(expandedArea);
        } else {
            computePreferredArea(sourceClassName, sourceMethodName);
        }
    }

    /**
     * Agrandi (si nécessaire) une région géographique en fonction de l'ajout, la supression ou
     * la modification des coordonnées d'une sous-région. Cette méthode est appelée lorsque les
     * coordonnées de la sous-région <code>oldSubArea</code> ont changées pour devenir
     * <code>newSubArea</code>. Si ce changement s'est traduit par un agrandissement de
     * <code>area</code>, alors le nouveau rectangle agrandi sera retourné. Si le changement
     * n'a aucun impact sur <code>area</code>, alors <code>area</code> sera retourné tel quel.
     * Si le changement PEUT avoir diminué la dimension de <code>area</code>, alors cette méthode
     * retourne <code>null</code> pour indiquer qu'il faut recalculer <code>area</code> à partir
     * de zéro.
     *
     * @param  area       Région géographique qui pourrait être affectée par le changement de
     *                    coordonnées d'une sous-région. En aucun cas ce rectangle <code>area</code>
     *                    ne sera directement modifié. Si une modification est nécessaire, elle sera
     *                    faite sur un clone qui sera retourné. Cet argument peut être
     *                    <code>null</code> si aucune région n'était précédemment définie.
     * @param  oldSubArea Anciennes coordonnées de la sous-région, ou <code>null</code> si la
     *                    sous-région n'existait pas avant l'appel de cette méthode. Ce rectangle
     *                    ne sera jamais modifié ni retourné.
     * @param  newSubArea Nouvelles coordonnées de la sous-région, ou <code>null</code> si la
     *                    sous-région est supprimée. Ce rectangle ne sera jamais modifié ni
     *                    retourné.
     *
     * @return Un rectangle contenant les coordonnées mises-à-jour de <code>area</code>, si cette
     *         mise-à-jour a pu se faire. Si elle n'a pas pu être faite faute d'informations, alors
     *         cette méthode retourne <code>null</code>. Dans ce dernier cas, il faudra recalculer
     *         <code>area</code> à partir de zéro.
     */
    private static Rectangle2D changeArea(Rectangle2D area,
                                          final Rectangle2D oldSubArea,
                                          final Rectangle2D newSubArea)
    {
        if (area == null) {
            /*
             * Si aucune région n'avait été définie auparavant. La sous-région
             * "newSubArea" représente donc la totalité de la nouvelle région
             * "area". On construit un nouveau rectangle plutôt que de faire un
             * clone pour être certain d'avoir un type d'une précision suffisante.
             */
            if (newSubArea != null) {
                area = new Rectangle2D.Double();
                area.setRect(newSubArea);
            }
            return area;
        }
        if (newSubArea == null) {
            /*
             * Une sous-région a été supprimée ("newSubArea" est nulle). Si la sous-région supprimée ne
             * touchait pas au bord de "area",  alors sa suppression ne peut pas avoir diminuée "area":
             * on retournera alors area. Si au contraire "oldSubArea" touchait au bord de "area", alors
             * on ne sait pas si la suppression de "oldSubArea" a diminué "area".  Il faudra recalculer
             * "area" à partir de zéro, ce que l'on indique en retournant NULL.
             */
            if (               oldSubArea==null  ) return area;
            if (contains(area, oldSubArea, false)) return area;
            return null;
        }
        if (oldSubArea != null) {
            /*
             * Une sous-région a changée ("oldSubArea" est devenu "newSubArea"). Si on détecte que ce
             * changement PEUT diminuer la superficie totale de "area", il faudra recalculer "area" à
             * partir de zéro pour en être sur. On retourne donc NULL.  Si au contraire la superficie
             * totale de "area" ne peut pas avoir diminuée, elle peut avoir augmentée. Ce calcul sera
             * fait à la fin de cette méthode, qui poursuit son cours.
             */
            double t;
            if (((t=oldSubArea.getMinX()) <= area.getMinX() && t < newSubArea.getMinX()) ||
                ((t=oldSubArea.getMaxX()) >= area.getMaxX() && t > newSubArea.getMaxX()) ||
                ((t=oldSubArea.getMinY()) <= area.getMinY() && t < newSubArea.getMinY()) ||
                ((t=oldSubArea.getMaxY()) >= area.getMaxY() && t > newSubArea.getMaxY()))
            {
                return null; // Le changement PEUT avoir diminué "area".
            }
        }
        /*
         * Une nouvelle sous-région est ajoutée. Si elle était déjà
         * entièrement comprise dans "area", alors son ajout n'aura
         * aucun impact sur "area" et peut être ignoré.
         */
        if (!contains(area, newSubArea, true)) {
            // Cloner est nécessaire pour que "firePropertyChange"
            // puisse connaître l'ancienne valeur de "area".
            area = (Rectangle2D) area.clone();
            area.add(newSubArea);
        }
        return area;
    }

    /**
     * Indique si la région géographique <code>big</code> contient entièrement la sous-région
     * <code>small</code> spécifiée. Un cas particulier survient si un ou plusieurs bords de
     * <code>small</code> coïncide avec les bords correspondants de <code>big</code>. L'argument
     * <code>edge</code> indique si on considère qu'il y a inclusion ou pas dans ces circonstances.
     *
     * @param big   Région géographique dont on veut vérifier s'il contient une sous-région.
     * @param small Sous-région géographique dont on veut vérifier l'inclusion dans <code>big</code>.
     * @param edge <code>true</code> pour considérer qu'il y a inclusion si ou ou plusieurs bords
     *        de <code>big</code> et <code>small</code> conïncide, ou <code>false</code> pour exiger
     *        que <code>small</code> ne touche pas aux bords de <code>big</code>.
     */
    static boolean contains(final Rectangle2D big, final Rectangle2D small, final boolean edge) {
        return edge ? (small.getMinX()>=big.getMinX() && small.getMaxX()<=big.getMaxX() && small.getMinY()>=big.getMinY() && small.getMaxY()<=big.getMaxY()):
                      (small.getMinX()> big.getMinX() && small.getMaxX()< big.getMaxX() && small.getMinY()> big.getMinY() && small.getMaxY()< big.getMaxY());
    }

    /**
     * Returns the preferred pixel size in "real world" coordinates. For image layers, this is
     * the size of image's pixels. For other kind of layers, "pixel size" are to be understood
     * as some dimension representative of the layer's resolution.  This method invokes {@link
     * RenderedLayer#getPreferredPixelSize} for each layers and returns the finest resolution.
     *
     * @return The preferred pixel size in "real world" coordinates (in this 
     *         {@linkplain #getCoordinateSystem renderer's coordinate system})
     *         or <code>null</code> if no layer provided a preferred pixel size.
     *
     * @task TODO: Transformations should use MathTransform.derivative(...)
     *             instead, but it is not yet implemented for projections.
     */
    public Dimension2D getPreferredPixelSize() {
        double minWidth  = Double.POSITIVE_INFINITY;
        double minHeight = Double.POSITIVE_INFINITY;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            final Dimension2D size = layer.getPreferredPixelSize();
            if (size!=null) try {
                double width  = size.getWidth();
                double height = size.getHeight();
                final MathTransform2D transform = (MathTransform2D) getMathTransform(
                                                        layer.getCoordinateSystem(),
                                                        this. getCoordinateSystem(),
                                                        "Renderer", "getPreferredPixelSize");
                if (!transform.isIdentity()) {
                    /*
                     * In theory, the RendererLayer should use the same CS than this Renderer.
                     * However, as a safety, we will check for coordinate transformations anyway.
                     * We create a pixel in the midle of the preferred area and transform it to
                     * this coordinate system.  TODO: we should use MathTransform.derivative
                     * instead, but is is not yet implemented for projections.
                     */
                    Rectangle2D area = layer.getPreferredArea();
                    if (area == null) {
                        area = new Rectangle2D.Double();
                    }
                    area.setRect(area.getCenterX()-0.5*width,
                                 area.getCenterY()-0.5*height,
                                 width, height);
                    area   = CTSUtilities.transform(transform, area, area);
                    width  = area.getWidth();
                    height = area.getHeight();
                }
                if (width  < minWidth ) minWidth =width;
                if (height < minHeight) minHeight=height;
            } catch (TransformException exception) {
                handleException("Renderer", "getPreferredPixelSize", exception);
                // Not a big deal. Continue...
            }
        }
        if (minWidth >0 && !Double.isInfinite(minWidth) &&
            minHeight>0 && !Double.isInfinite(minHeight))
        {
            return new XDimension2D.Double(minWidth, minHeight);
        } else {
            return null;
        }
    }

    /**
     * Returns the scale factor, or {@link Float#NaN} if the scale is unknow.
     * The scale factor is usually smaller than 1. For example for a 1:1000 scale,
     * the scale factor will be 0.001. This scale factor takes in account the physical
     * size of the rendering device (e.g. the screen size) if such information is available.
     * Note that this scale can't be more accurate than the
     * {@linkplain GraphicsConfiguration#getNormalizingTransform() information supplied
     * by the underlying system}.
     *
     * @return The rendering scale factor as a number between 0 and 1, or {@link Float#NaN}.
     * @see RenderingContext#getScale
     */
    public float getScale() {
        final Float scaleFactor = this.scaleFactor; // Avoid the need for synchronization.
        return (scaleFactor!=null) ? scaleFactor.floatValue() : Float.NaN;
    }



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////          add/remove/get rendered layers          ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Add a new layer to this renderer. A <code>Renderer</code> do not draw anything as long
     * as at least one layer hasn't be added.    A {@link RenderedLayer} can be anything like
     * an isobath, a remote sensing image, city locations, map scale, etc.  The drawing order
     * (relative to other layers) is determined by the {@linkplain RenderedLayer#getZOrder
     * z-order} property. A {@link RenderedLayer} object can be added to only one
     * <code>Renderer</code> object.
     *
     * @param  layer Layer to add to this <code>Renderer</code>. This method call
     *         will be ignored if <code>layer</code> has already been added to this
     *         <code>Renderer</code>.
     * @throws IllegalArgumentException If <code>layer</code> has already been added
     *         to an other <code>Renderer</code>, or if the layer can't be added for
     *         some other reason.
     *
     * @see #removeLayer
     * @see #removeAllLayers
     * @see #getLayers
     * @see #getLayerCount
     */
    public synchronized void addLayer(final RenderedLayer layer) throws IllegalArgumentException {
        synchronized (layer.getTreeLock()) {
            if (layer.renderer == this) {
                return;
            }
            if (layer.renderer != null) {
                throw new IllegalArgumentException(
                            Resources.format(ResourceKeys.ERROR_RENDERER_NOT_OWNER_$1, layer));
            }
            layer.renderer = this;
            /*
             * HEURISTIC RULE: If this is the first layer to be added, and if this layer uses
             * an incompatible CS, set the renderer CS to the layer CS. This case occurs when
             * the renderer is constructed with the default CS (CARTESIAN)  and the layers to
             * be added use a geographic CS (e.g. WGS84), or the converse.
             */
            if (layerCount == 0) {
                final CoordinateSystem layerCS = layer.getCoordinateSystem();
                try {
                    getMathTransform(layerCS, getCoordinateSystem(), "Renderer", "addLayer");
                } catch (TransformException exception) {
                    final StringBuffer buffer = new StringBuffer(Resources.getResources(getLocale())
                                                .getString(ResourceKeys.CHANGED_COORDINATE_SYSTEM));
                    buffer.append(System.getProperty("line.separator", "\n"));
                    buffer.append(exception.getLocalizedMessage());
                    LOGGER.info(buffer.toString());
                    try {
                        setCoordinateSystem(layerCS);
                    } catch (TransformException unexpected) {
                        // Should not happen, since this renderer do not yet have any layer.
                        throw new AssertionError(unexpected);
                    }
                }
            }
            /*
             * Now set the layer coordinate system to this renderer CS.
             */
            try {
                layer.setCoordinateSystem(getCoordinateSystem());
            } catch (TransformException exception) {
                layer.renderer = null;
                final IllegalArgumentException e;
                e = new IllegalArgumentException(exception.getLocalizedMessage());
                e.initCause(exception);
                throw e;
            }
            /*
             * Add the new layer in the {@link #layers} array. The array will growth as
             * needed and  the 'layerSorted' flag is set to true so that the array will
             * be resorted when needed.
             */
            if (layers == null) {
                layers = new RenderedLayer[16];
            }
            if (layerCount >= layers.length) {
                layers = (RenderedLayer[]) XArray.resize(layers, Math.max(layerCount,8) << 1);
            }
            layers[layerCount++] = layer;
            layerSorted = false;
            layer.setVisible(true);
            changePreferredArea(null, layer.getPreferredArea(), layer.getCoordinateSystem(),
                                "Renderer", "addLayer");
            layer.addPropertyChangeListener(listenerProxy);
            flushOffscreenBuffer(layer.getZOrder());
        }
        repaint(); // Must be invoked last
        listeners.firePropertyChange("layers", (layerCount==1) ? EMPTY : null, null);
    }

    /**
     * Remove a layer from this renderer. Note that if the layer is going to
     * be added back to the same renderer later, then it is more efficient to invoke
     * <code>{@link RenderedLayer#setVisible RenderedLayer.setVisible}(false)</code>.
     *
     * @param  layer The layer to remove. This method call will be ignored
     *         if <code>layer</code> has already been removed from this
     *         <code>Renderer</code>.
     * @throws IllegalArgumentException If <code>layer</code> is owned by
     *         an other <code>Renderer</code> than <code>this</code>.
     *
     * @see #addLayer
     * @see #removeAllLayers
     * @see #getLayers
     * @see #getLayerCount
     */
    public synchronized void removeLayer(final RenderedLayer layer) throws IllegalArgumentException
    {
        assert Thread.holdsLock(layer.getTreeLock());
        if (layer.renderer == null) {
            return;
        }
        if (layer.renderer != this) {
            throw new IllegalArgumentException(
                        Resources.format(ResourceKeys.ERROR_RENDERER_NOT_OWNER_$1, layer));
        }
        repaint(); // Must be invoked first
        flushOffscreenBuffer(layer.getZOrder());
        layer.removePropertyChangeListener(listenerProxy);
        final CoordinateSystem layerCS = layer.getCoordinateSystem();
        final Rectangle2D    layerArea = layer.getPreferredArea();
        layer.setVisible(false);
        layer.clearCache();
        layer.renderer = null;
        /*
         * Retire cette couche de la liste {@link #layers}. On recherchera
         * toutes les occurences de cette couche, même si en principe elle ne
         * devrait apparaître qu'une et une seule fois.
         */
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer scan = layers[i];
            if (scan == layer) {
                System.arraycopy(layers, i+1, layers, i, (--layerCount)-i);
                layers[layerCount] = null;
            }
        }
        changePreferredArea(layerArea, null, layerCS, "Renderer", "removeLayer");
        listeners.firePropertyChange("layers", null, (layerCount!=0) ? null : EMPTY);
    }

    /**
     * Remove all layers from this renderer.
     *
     * @see #addLayer
     * @see #removeLayer
     * @see #getLayers
     * @see #getLayerCount
     */
    public synchronized void removeAllLayers() {
        repaint(); // Must be invoked first
        flushOffscreenBuffers();
        while (--layerCount>=0) {
            final RenderedLayer layer = layers[layerCount];
            assert Thread.holdsLock(layer.getTreeLock());
            layer.removePropertyChangeListener(listenerProxy);
            layer.setVisible(false);
            layer.clearCache();
            layer.renderer = null;
            layers[layerCount] = null;
        }
        layerCount = 0;
        setPreferredArea(null);
        clearCache();
        listeners.firePropertyChange("layers", null, EMPTY);
    }

    /**
     * Returns all registered layers. The returned array is sorted in increasing
     * {@linkplain RenderedLayer#getZOrder z-order}: element at index 0 contains
     * the first layer to be drawn.
     *
     * @return The sorted array of layers. May have a 0 length, but will never
     *         be <code>null</code>. Change to this array, will not affect this
     *         <code>Renderer</code>.
     *
     * @see #addLayer
     * @see #removeLayer
     * @see #removeAllLayers
     * @see #getLayerCount
     */
    public synchronized RenderedLayer[] getLayers() {
        sortLayers();
        if (layers != null) {
            final RenderedLayer[] array = new RenderedLayer[layerCount];
            System.arraycopy(layers, 0, array, 0, layerCount);
            return array;
        } else {
            return EMPTY;
        }
    }

    /**
     * Returns the number of layers in this renderer.
     *
     * @see #getLayers
     * @see #addLayer
     * @see #removeLayer
     * @see #removeAllLayers
     */
    public int getLayerCount() {
        return layerCount;
    }

    /**
     * Procède au classement immédiat des couches, si ce n'était pas déjà fait.
     */
    private void sortLayers() {
        assert Thread.holdsLock(this);
        if (!layerSorted && layers!=null) {
            layers = (RenderedLayer[]) XArray.resize(layers, layerCount);
            Arrays.sort(layers, COMPARATOR);
            layerSorted = true;
        }
    }

    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////                    Rendering                     ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Returns a rendering hint.
     *
     * @param  key The hint key (e.g. {@link Hints#FINEST_RESOLUTION}).
     * @return The hint value for the specified key, or <code>null</code> if there is no
     *         hint for the specified key.
     *
     * @see Hints#FINEST_RESOLUTION
     * @see Hints#REQUIRED_RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     * @see Hints#PREFETCH
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    public synchronized Object getRenderingHint(final RenderingHints.Key key) {
        return hints.get(key);
    }

    /**
     * Add a rendering hint. Hints provides optional information used by some rendering code.
     *
     * @param key   The hint key (e.g. {@link Hints#FINEST_RESOLUTION}).
     * @param value The hint value. A <code>null</code> value remove the hint.
     *
     * @see Hints#FINEST_RESOLUTION
     * @see Hints#REQUIRED_RESOLUTION
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     * @see Hints#PREFETCH
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    public synchronized void setRenderingHint(RenderingHints.Key key, Object value) {
        if (value != null) {
            hints.put(key, value);
        } else {
            hints.remove(key);
        }
        if (Hints.FINEST_RESOLUTION.equals(key)) {
            if (value != null) {
                minResolution = ((Number) hints.get(key)).floatValue();
                if (minResolution >= 0) {
                    if (minResolution > maxResolution) {
                        maxResolution = minResolution;
                    }
                    return;
                }
            }
            minResolution = 0;
            return;
        }
        if (Hints.REQUIRED_RESOLUTION.equals(key)) {
            if (value != null) {
                maxResolution = ((Number) hints.get(key)).floatValue();
                if (maxResolution >= 0) {
                    if (maxResolution < minResolution) {
                        minResolution = maxResolution;
                    }
                    return;
                }
            }
            maxResolution = minResolution;
            return;
        }
        if (Hints.PREFETCH.equals(key)) {
            if (value != null) {
                prefetch = ((Boolean) value).booleanValue();
            } else {
                prefetch = (Runtime.getRuntime().availableProcessors() >= 2);
            }
            return;
        }
        if (Hints.GRID_COVERAGE_PROCESSOR.equals(key) &&
            !hints.containsKey(Hints.COORDINATE_TRANSFORMATION_FACTORY))
        {
            key = Hints.COORDINATE_TRANSFORMATION_FACTORY;
            value = (value!=null) ? ((GridCoverageProcessor) value).getRenderingHint(key) : null;
            // Fall through
        }
        if (Hints.COORDINATE_TRANSFORMATION_FACTORY.equals(key)) {
            factory = (value!=null) ? (CoordinateTransformationFactory) value :
                                       CoordinateTransformationFactory.getDefault();
            clearCache();
            return;
        }
    }

    /**
     * Returns the offscreen buffer type for the given {@linkplain RenderedLayer#getZOrder z-order}.
     * This is the value of the <code>type</code> argument given to the last call to
     * {@link #setOffscreenBuffered setOffscreenBuffered(...)} for a range containing
     * <code>zOrder</code>.
     *
     * @param  zOrder The z-order to query.
     * @return One of {@link ImageType#NONE}, {@link ImageType#VOLATILE} or
     *         {@link ImageType#BUFFERED} enumeration.
     */
    public synchronized ImageType getOffscreenBuffered(final float zOrder) {
        if (offscreenZRanges != null) {
            final int index = offscreenZRanges.indexOfRange(new Float(zOrder));
            if (index >= 0) {
                return offscreenIsVolatile[index] ? ImageType.VOLATILE : ImageType.BUFFERED;
            }
        }
        return ImageType.NONE;
    }

    /**
     * Set the offscreen buffer type for the range that contains the given
     * {@linkplain RenderedLayer#getZOrder z-order}.
     */
    private void setOffscreenBuffered(final float zOrder, final ImageType type) {
        final int index = offscreenZRanges.indexOfRange(new Float(zOrder));
        if (index >= 0) {
            offscreenIsVolatile[index] = ImageType.VOLATILE.equals(type);
        }
    }

    /**
     * Enable or disable the use of offscreen buffer for all {@linkplain RenderedLayer layers}
     * in the given range of {@linkplain RenderedLayer#getZOrder z-orders}. When enabled, all
     * layers in the given range will be rendered once in an offscreen buffer (for example an
     * {@link VolatileImage}); the image will then been reused as much as possible. The offscreen
     * buffer may be invalidate at any time by some external event (including a call to any of
     * {@link RenderedLayer#repaint()} methods) and will be recreated as needed. Using offscreen
     * buffer for background layers that do not change often (e.g. a background map) help to make
     * the GUI more responsive to frequent changes in foreground layers (e.g. a glass pane with
     * highlighted selections).
     * <br><br>
     * An arbitrary amount of ranges can be specified. Each <strong>distinct</strong> range will
     * use its own offscreen buffer. This means that if this method is invoked twice for enabling
     * buffering in overlapping range of z-values, then the union of the two ranges will shares
     * the same offscreen image.
     *
     * @param lower The lower z-order, inclusive.
     * @param upper The upper z-order, inclusive.
     * @param type  {@link ImageType#VOLATILE} for enabling offscreen buffering for the specified
     *              range, or {@link ImageType#NONE} for disabling it.
     */
    public synchronized void setOffscreenBuffered(final float lower,
                                                  final float upper,
                                                  ImageType type)
    {
        /*
         * Save the references to the old images and their status (type, need repaint, etc.).
         * We will try to reuse existing images after the range set has been updated.
         */
        final Map       oldIndexMap;
        final Image[]   oldBuffers = offscreenBuffers;
        final boolean[] oldTypes   = offscreenIsVolatile;
        final boolean[] oldNeeds   = offscreenNeedRepaint;
        if (offscreenZRanges == null) {
            if (ImageType.NONE.equals(type)) {
                return;
            }
            offscreenZRanges = new RangeSet(Float.class);
            oldIndexMap      = Collections.EMPTY_MAP;
        } else {
            int index=0;
            oldIndexMap = new HashMap();
            for (final Iterator it=offscreenZRanges.iterator(); it.hasNext();) {
                if (oldIndexMap.put(it.next(), new Integer(index++)) != null) {
                    throw new AssertionError(); // Should not happen
                }
            }
            assert index == offscreenBuffers.length : index;
        }
        /*
         * Update the range set, and rebuild the offscreen buffers array.
         */
        final ImageType lowerType;
        final ImageType upperType;
        if (ImageType.NONE.equals(type)) {
            lowerType = getOffscreenBuffered(lower);
            upperType = getOffscreenBuffered(upper);
            offscreenZRanges.remove(lower, upper);
        } else {
            lowerType = upperType = type;
            offscreenZRanges.add(lower, upper);
        }
        offscreenBuffers     = new Image[offscreenZRanges.size()];
        offscreenIsVolatile  = new boolean[offscreenBuffers.length];
        offscreenNeedRepaint = new boolean[offscreenBuffers.length];
        int index = 0;
        for (final Iterator it=offscreenZRanges.iterator(); it.hasNext(); index++) {
            final Integer oldInteger = (Integer) oldIndexMap.remove(it.next());
            if (oldInteger != null) {
                final int oldIndex = oldInteger.intValue();
                offscreenBuffers    [index] = oldBuffers[oldIndex];
                offscreenIsVolatile [index] = oldTypes  [oldIndex];
                offscreenNeedRepaint[index] = oldNeeds  [oldIndex];
            }
        }
        assert index == offscreenBuffers.length : index;
        setOffscreenBuffered(lower, lowerType);
        setOffscreenBuffered(upper, upperType);
        /*
         * Release resources used by remaining (now unused) images.
         */
        for (final Iterator it=oldIndexMap.values().iterator(); it.hasNext();) {
            final Image image = oldBuffers[((Integer) it.next()).intValue()];
            if (image != null) {
                image.flush();
            }
        }
    }

    /**
     * Signal that a layer at the given {@linkplain RenderedLayer#getZOrder z-order} need
     * to be repainted. If an offscreen buffer were allocated for this z-order, it may be
     * flushed. This method is invoked by any of {@link RenderedLayer#repaint} methods.
     *
     * @param zOrder The z-order of the offscreen buffer to flush.
     */
    final synchronized void flushOffscreenBuffer(final float zOrder) {
        if (offscreenZRanges != null) {
            final int index = offscreenZRanges.indexOfRange(new Float(zOrder));
            if (index >= 0) {
                if (false) {
                    // In previous version, we flushed the whole image. Now try
                    // to preserve the image and repaint only the damaged area.
                    final Image image = offscreenBuffers[index];
                    if (image != null) {
                        image.flush();
                    }
                }
                offscreenNeedRepaint[index] = true;
            }
        }
    }

    /**
     * Flush all offscreen buffers.
     */
    private void flushOffscreenBuffers() {
        assert Thread.holdsLock(this);
        if (offscreenBuffers != null) {
            for (int i=0; i<offscreenBuffers.length; i++) {
                final Image image = offscreenBuffers[i];
                if (image != null) {
                    image.flush();
                }
            }
            Arrays.fill(offscreenNeedRepaint, true);
        }
    }

    /**
     * Checks the state of the given image. If <code>image</code> is null, then this method returns
     * {@link VolatileImage#IMAGE_INCOMPATIBLE}. Otherwise, if <code>image</code> is an instance of
     * {@link VolatileImage}, then this method invokes {@link VolatileImage#validate}.   Otherwise,
     * (usually the {@link BufferedImage} case) this method returns {@link VolatileImage#IMAGE_OK}.
     *
     * @param  image The image.
     * @param  config The graphics configuration.
     * @return The state, as one of {@link VolatileImage} constants.
     */
    private static int validate(final Image image, final GraphicsConfiguration config) {
        if (image == null) {
            return VolatileImage.IMAGE_INCOMPATIBLE;
        }
        if (image instanceof VolatileImage) {
            return ((VolatileImage) image).validate(config);
        }
        return VolatileImage.IMAGE_OK;
    }

    /**
     * Returns <code>true</code> if rendering data was lost since last validate call.
     */
    private static boolean contentsLost(final Image image) {
        if (image instanceof VolatileImage) {
            return ((VolatileImage) image).contentsLost();
        }
        return false;
    }

    /**
     * Returns a string representation of a coordinate system. This method is
     * used for formatting a logging message in {@link #getMathTransform}.
     */
    private static String toString(final CoordinateSystem cs) {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(cs));
        buffer.append('[');
        final String name = cs.getName(null);
        if (name != null) {
            buffer.append('"');
            buffer.append(name);
            buffer.append('"');
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Set the {@link #textToDevice} transform to the specified translation.
     * Returns <code>true</code> if the transform changed as a result of this call.
     */
    private boolean setTextToDevice(final int tx, final int ty) {
        assert (textToDevice.getType() & ~AffineTransform.TYPE_TRANSLATION) == 0 : textToDevice;
        if (textToDevice.getTranslateX() != tx || textToDevice.getTranslateY() != ty) {
            textToDevice.setToTranslation(tx, ty);
            return true;
        }
        return false;
    }

    /**
     * Returns the math transform factory associated to this renderer.
     */
    final synchronized MathTransformFactory getMathTransformFactory() {
        return factory.getMathTransformFactory();
    }

    /**
     * Construct a transform between two coordinate systems. If a {@link
     * Hints#COORDINATE_TRANSFORMATION_FACTORY} has been provided, the
     * specified {@link CoordinateTransformationFactory} will be used.
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     * @return A transformation from <code>sourceCS</code> to <code>targetCS</code>.
     * @throws CannotCreateTransformException if the transformation can't be created.
     *
     * @see #getRenderingHint
     * @see #setRenderingHint
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    final synchronized MathTransform getMathTransform(final CoordinateSystem sourceCS,
                                                      final CoordinateSystem targetCS,
                                                      final String sourceClassName,
                                                      final String sourceMethodName)
            throws CannotCreateTransformException
    {
        if (sourceCS == targetCS) {
            // Fast check for a very common case. We will use the more
            // general (but slower) 'equals(..., false)' version later.
            return MathTransform2D.IDENTITY;
        }
        MathTransform tr;
        /*
         * Check if the math transform is available in the cache. A majority of transformations
         * will be from 'layerCS' to 'mapCS' to 'textCS'.  The cache looks for the 'layerCS' to
         * to 'mapCS' transform.
         */
        final boolean cachedTransform = targetCS.equals(context.mapCS, false);
        if (cachedTransform) {
            tr = (MathTransform) transforms.get(sourceCS);
            if (tr != null) {
                return tr;
            }
        }
        /*
         * If one of the CS is a FittedCoordinateSystem, then check if we can use directly
         * its 'toBase' transform without using the costly CoordinateTransformationFactory.
         * This check is worth to be done since it is a very common situation.  A majority
         * of transformations will be from 'mapCS' to 'textCS',  which is the case we test
         * first. The converse (transformations from 'textCS' to 'mapCS') is less frequent
         * and can be catched by the 'transform' cache, which is why we test it last.
         */
        if (targetCS instanceof FittedCoordinateSystem) {
            final FittedCoordinateSystem fittedCS = (FittedCoordinateSystem) targetCS;
            if (sourceCS.equals(fittedCS.getBaseCoordinateSystem(), false)) try {
                return fittedCS.getToBase().inverse();
            } catch (NoninvertibleTransformException exception) {
                throw new CannotCreateTransformException(sourceCS, targetCS, exception);
            }
        }
        if (sourceCS instanceof FittedCoordinateSystem) {
            final FittedCoordinateSystem fittedCS = (FittedCoordinateSystem) sourceCS;
            if (targetCS.equals(fittedCS.getBaseCoordinateSystem(), false)) {
                tr = fittedCS.getToBase();
                if (cachedTransform) {
                    transforms.put(sourceCS, tr);
                }
                return tr;
            }
        }
        if (sourceCS.equals(targetCS, false)) {
            return MathTransform2D.IDENTITY;
        }
        /*
         * Now that we failed to reuse a pre-existing transform, ask to the factory
         * to create a new one. A message is logged in order to trace down the amount
         * of coordinate transformations created.
         */
        if (LOGGER.isLoggable(Level.FINER)) {
            // FINER is the default level for entering, returning, or throwing an exception.
            final LogRecord record = Resources.getResources(null).getLogRecord(Level.FINER,
                                               ResourceKeys.INITIALIZING_TRANSFORMATION_$2,
                                               toString(sourceCS), toString(targetCS));
            record.setSourceClassName (sourceClassName);
            record.setSourceMethodName(sourceMethodName);
            LOGGER.log(record);
        }
        tr = factory.createFromCoordinateSystems(sourceCS, targetCS).getMathTransform();
        if (cachedTransform) {
            transforms.put(sourceCS, tr);
        }
        return tr;
    }

    /**
     * Returns a fitted coordinate system for {@link RenderingContext#textCS} and
     * {@link RenderingContext#deviceCS}.
     *
     * @param  name     The coordinate system name (e.g. "text" or "device").
     * @param  base     The base coordinate system (e.g. {@link RenderingContext#mapCS}).
     * @param  fromBase The transform from the base CS to the fitted CS.  Note that this is the
     *                  opposite of the usual {@link FittedCoordinateSystem} constructor. We do
     *                  it that way because it is the way we usually gets affine transform from
     *                  {@link Graphics2D}.
     * @return The fitted coordinate system, or <code>base</code> if the transform is the identity
     *         transform.
     * @throws NoninvertibleTransformException if the affine transform is not invertible.
     */
    private CoordinateSystem createFittedCoordinateSystem(final String           name,
                                                          final CoordinateSystem base,
                                                          final AffineTransform fromBase)
            throws NoninvertibleTransformException
    {
        if (fromBase.isIdentity()) {
            return base;
        }
        /*
         * Inverse the MathTransform rather than the AffineTransform because MathTransform
         * keep a reference to its inverse. It avoid the need for re-inversing it again later,
         * which help to avoid rounding errors.
         */
        final MathTransform toBase;
        toBase = factory.getMathTransformFactory().createAffineTransform(fromBase).inverse();
        return new FittedCoordinateSystem(name, base, toBase, TEXT_AXIS);
    }

    /**
     * Paint this <code>Renderer</code> and all visible layers it contains.
     * This method invokes {@link RenderedLayer#paint} for each layer.
     *
     * @param graph  The graphics handler.
     * @param zoom   The zoom (usually provided by {@link org.geotools.gui.swing.ZoomPane#zoom}.
     * @param zoomableBounds The bounds of drawing area (usually provided by
     *               {@link org.geotools.gui.swing.ZoomPane#getZoomableBounds}).
     *
     * @deprecated Use {@link #paint(Graphics2D, Rectangle, AffineTransform)} instead.
     */
    public void paint(final Graphics2D         graph,
                      final AffineTransform     zoom,
                      final Rectangle zoomableBounds)
    {
        paint(graph, zoomableBounds, zoom);
    }

    /**
     * Paint this <code>Renderer</code> and all visible layers it contains.
     *
     * @deprecated Use {@link #paint(Graphics2D,Rectangle,AffineTransform,boolean)} instead.
     */
    public void paint(final Graphics2D         graph,
                      final Rectangle zoomableBounds,
                      final AffineTransform     zoom)
    {
        paint(graph, zoomableBounds, zoom, mapPane==null);
    }

    /**
     * Paint this <code>Renderer</code> and all visible layers it contains.
     * This method invokes {@link RenderedLayer#paint} for each layer.
     *
     * @param graph  The graphics handler to draw to.
     * @param zoomableBounds The bounds of the output area in output units (usually pixels).
     *               Those bounds are usually provided by
     *               {@link org.geotools.gui.swing.ZoomPane#getZoomableBounds}).
     * @param zoom   A transform which converts "World coordinates" to
     *               output coordinates. This transform is usually provided by
     *               {@link org.geotools.gui.swing.ZoomPane#zoom}.
     * @param isPrinting <code>true</code> if the map is printed instead of painted on screen.
     *               When printing, layers like {@link RenderedGridCoverage} should block until
     *               all data are available instead of painting only available data and invokes
     *               {@link RenderedLayer#repaint()} later.
     */
    public synchronized void paint(final Graphics2D         graph,
                                   final Rectangle zoomableBounds,
                                   final AffineTransform     zoom,
                                   final boolean       isPrinting)
    {
        statistics.init();
        sortLayers();
        // Copy reference to 'context' in order to avoid change to instance field if printing.
        RenderingContext           context = this.context;
        RenderingContext    clippedContext = this.clippedContext;
        final GraphicsJAI         graphics = GraphicsJAI.createGraphicsJAI(graph, mapPane);
        final GraphicsConfiguration config = graphics.getDeviceConfiguration();
              Rectangle         clipBounds = graphics.getClipBounds();
        final AffineTransform     toDevice = graphics.getTransform();
        final boolean             toScreen = toDevice.isIdentity();
        final boolean             sameZoom = mapToText.equals(zoom);
        final int           offscreenCount = (!isPrinting && offscreenZRanges!=null) ?
                                                             offscreenZRanges.size() : 0;
        try {
            /*
             * Set a flag for avoiding some 'repaint' events while we are actually painting.
             * For example some implementations of the RenderedLayer.paint(...) method may
             * detect changes since the last rendering and invokes some kind of invalidate(...)
             * methods before the layer's rendering begin. Invoking those methods may cause in
             * some indirect way a call to RenderedLayer.repaint(), which will trig an other
             * widget's repaint. This second repaint is usually not needed, since RenderedLayers
             * usually managed to update their informations before they start their rendering.
             * Consequently, disabling 'repaint' events while we are painting help to reduces
             * duplicated rendering.
             */
            Rectangle2D dirtyArea = XRectangle2D.INFINITY;
            if (clipBounds == null) {
                clipBounds = zoomableBounds;
            } else if (zoomableBounds.contains(clipBounds)) {
                dirtyArea = clipBounds;
            }
            RenderedLayer.setDirtyArea(layers, layerCount, dirtyArea);
            /*
             * If the zoom has changed, send a notification to all layers before to start the
             * rendering. Layers will update their cache, which is used in order to decide if
             * a layer need to be repainted or not. Note that some layers may change their state,
             * which may results in a new 'paint' event to be fired. But because of the 'dirtyArea'
             * flag above, some 'paint' event will be intercepted in order to avoid repainting the
             * same area twice.
             */
            if (!sameZoom) {
                /*
                 * Compute the change as an affine transform, and sent the notification.
                 */
                try {
                    final AffineTransform change = mapToText.createInverse();
                    change.preConcatenate(zoom);
                    if (true) {
                        // Scale slightly the zoom in order to avoid rounding errors in Area.
                        final double centerX = zoomableBounds.getCenterX();
                        final double centerY = zoomableBounds.getCenterY();
                        change.translate( centerX,  centerY);
                        change.scale    (   1+EPS,    1+EPS);
                        change.translate(-centerX, -centerY);
                    }
                    zoomChanged(change);
                } catch (java.awt.geom.NoninvertibleTransformException exception) {
                    // Should not happen. If it happen anyway, declare that everything must be
                    // repainted. It will be slower, but will not prevent the renderer to work.
                    Utilities.unexpectedException("org.geotools.renderer.j2d",
                                                  "Renderer", "paint", exception);
                    zoomChanged(null);
                }
                try {
                    /*
                     * Compute the new scale factor. This scale factor take in account the real
                     * size of the rendering device (e.g. the screen),  but is only as accurate
                     * as the information supplied by the underlying system.
                     */
                    final AffineTransform normalize = zoom.createInverse();
                    normalize.concatenate(config.getNormalizingTransform());
                    normalize.preConcatenate(normalizeToDots);
                    scaleChanged((float)(1/XAffineTransform.getScale(normalize)));
                } catch (java.awt.geom.NoninvertibleTransformException exception) {
                    Utilities.unexpectedException("org.geotools.renderer.j2d",
                                                  "Renderer", "paint", exception);
                }
            }
            /*
             * If the zoom or the device changed, then the 'textCS' and 'deviceCS' must be
             * recreated. Note that we first change only the local reference to 'context';
             * the object reference will be changed only if we are rendering to screen. We
             * also check for offscreen buffers only when rendering to screen.
             */
            if (!sameZoom || !toScreen || setTextToDevice(-zoomableBounds.x, -zoomableBounds.y)) try
            {
                final CoordinateSystem mapCS, textCS, deviceCS, clippedCS;
                mapCS    = context.mapCS;
                textCS   = createFittedCoordinateSystem("textCS",    mapCS, zoom);
                deviceCS = createFittedCoordinateSystem("deviceCS", textCS, textToDevice);
                context  = new RenderingContext(this, mapCS, textCS, deviceCS);
                if (textToDevice.equals(toDevice)) {
                    clippedCS      = deviceCS;
                    clippedContext = context;
                } else {
                    clippedCS      = createFittedCoordinateSystem("deviceCS", textCS, toDevice);
                    clippedContext = new RenderingContext(this, mapCS, textCS, clippedCS);
                }
                if (toScreen) {
                    mapToText.setTransform(zoom);
                    this.context        = context;
                    this.clippedContext = clippedContext;
                }
            } catch (TransformException exception) {
                // Impossible to process to the rendering. Paint the stack
                // trace right into the component and exit from this method.
                GraphicsUtilities.paintStackTrace(graphics, zoomableBounds, exception);
                return;
            }
            for (int i=0; i<offscreenCount; i++) {
                final Image buffer = offscreenBuffers[i];
                if (buffer != null) {
                    if (buffer.getWidth (mapPane) != zoomableBounds.width ||
                        buffer.getHeight(mapPane) != zoomableBounds.height)
                    {
                        buffer.flush();
                        offscreenBuffers    [i] = null;
                        offscreenNeedRepaint[i] = true;
                    }
                }
            }
            /*
             * Draw all layers, starting with the one with the lowest <var>z</var> value. Before
             * to start the actual drawing,  we will notify all layers that they are about to be
             * drawn. Some layers may spend one or two threads for pre-computing data.
             */
            graphics.addRenderingHints(hints);
            graphics.transform(zoom); // Must be invoked before 'init'
            clippedContext.init(graphics, zoomableBounds, isPrinting);
            boolean contextInitialized = (clippedContext == context);
            if (prefetch) {
                // Prepare data in background threads. While we are painting
                // one layer, next layers will be pre-computed.
                for (int i=0; i<layerCount; i++) {
                    layers[i].prefetch(clippedContext);
                }
            }
            int     offscreenIndex = -1;               // Index of current offscreen buffer.
            boolean graphicsZoomed = true;             // Is graphics.transform(zoom) applied?
            float minZOrder = Float.NEGATIVE_INFINITY; // The minimum z-value for current buffer.
            float maxZOrder = Float.NaN;               // The maximum z-value for current buffer.
            for (int layerIndex=0; layerIndex<layerCount; layerIndex++) {
                int          layerIndexUp = layerIndex;
                final RenderedLayer layer = layers[layerIndex];
                final float        zOrder = layer.getZOrder();
                while (zOrder >= minZOrder) {
                    if (!(zOrder <= maxZOrder)) {
                        if (++offscreenIndex < offscreenCount) {
                            minZOrder = (float)offscreenZRanges.getMinValueAsDouble(offscreenIndex);
                            maxZOrder = (float)offscreenZRanges.getMaxValueAsDouble(offscreenIndex);
                            continue;
                        } else {
                            minZOrder = Float.NaN;
                            maxZOrder = Float.NaN;
                            break;
                        }
                    }
                    assert offscreenZRanges.indexOfRange(new Float(zOrder)) == offscreenIndex;
                    /*
                     * We have found the begining of a range to be rendered using offscreen
                     * buffer. Search the index of the last layer in this range, exclusive.
                     */
                    while (++layerIndexUp < layerCount) {
                        if (!(layers[layerIndexUp].getZOrder() <= maxZOrder)) {
                            break;
                        }
                    }
                    break;
                }
                /*
                 * If the current layer is not part of any offscreen buffer,
                 * render it directly in the Swing's graphics handler.
                 */
                if (layerIndex == layerIndexUp) {
                    if (!graphicsZoomed) {
                        graphics.transform(zoom);
                        graphicsZoomed = true;
                    }
                    if (clippedContext.getGraphics() == null) {
                        clippedContext.setGraphics(graphics);
                        // Must be invoked *after* the 'graphicsZoomed' check.
                    }
                    try {
                        layer.update(clippedContext, clipBounds);
                    } catch (TransformException exception) {
                        handleException("RenderedLayer", "paintComponent", exception);
                    } catch (RuntimeException exception) {
                        Utilities.unexpectedException("org.geotools.renderer.j2d",
                                                      "RenderedLayer", "paint", exception);
                    }
                    continue;
                }
                /*
                 * The range of layer index to render offscreen goes from 'layerIndex'
                 * inclusive to 'layerIndexUp' exclusive. If the image is still valid,
                 * paint it immediately. Otherwise, performs the rendering offscreen.
                 */
                boolean createFromComponent = (mapPane != null);
                Rectangle bufferClip = clipBounds;
                Image buffer = offscreenBuffers[offscreenIndex];
renderOffscreen:while (true) {
                    switch (validate(buffer, config)) {
                        //
                        // Image incompatible or inexistant: recreates an empty
                        // one and restarts the loop from the 'validate' check.
                        //
                        case VolatileImage.IMAGE_INCOMPATIBLE: {
                            if (buffer != null) {
                                buffer.flush();
                                buffer = null;
                            }
                            if (createFromComponent) {
                                createFromComponent = false;
                                if (offscreenIsVolatile[offscreenIndex]) {
                                    buffer = mapPane.createVolatileImage(
                                                    zoomableBounds.width, zoomableBounds.height);
                                }
                            }
                            if (buffer == null) {
                                if (offscreenIsVolatile[offscreenIndex]) {
                                    buffer = config.createCompatibleVolatileImage(
                                                    zoomableBounds.width, zoomableBounds.height);
                                } else {
                                    buffer = config.createCompatibleImage(
                                                    zoomableBounds.width, zoomableBounds.height,
                                                    Transparency.TRANSLUCENT);
                                }
                            }
                            offscreenBuffers[offscreenIndex] = buffer;
                            bufferClip = zoomableBounds;
                            continue;
                        }
                        //
                        // Image preserved: paint it immediately if no layer changed
                        // since last rendering, or repaint only the damaged area.
                        //
                        case VolatileImage.IMAGE_OK: {
                            if (!offscreenNeedRepaint[offscreenIndex]) {
                                if (graphicsZoomed) {
                                    graphics.setTransform(toDevice);
                                    graphicsZoomed = false;
                                }
                                // REVISIT: Which AlphaComposite to use here?
                                graphics.drawImage(buffer, zoomableBounds.x,
                                                           zoomableBounds.y, mapPane);
                                if (contentsLost(buffer)) {
                                    // Upcomming 'validate' will falls in IMAGE_RESTORED case.
                                    continue;
                                }
                                // Rendering finished for this offscreen buffer.
                                break renderOffscreen;
                            }
                            // Repaint only the damaged area.
                            break;
                        }
                        //
                        // Contents has been lost: we need to repaint the whole image.
                        //
                        case VolatileImage.IMAGE_RESTORED: {
                            bufferClip = zoomableBounds;
                            break;
                        }
                    }
                    /*
                     * At this point, we know that some area need to be repainted.
                     * Reset a transparent background on the area to be repainted,
                     * and invokes RenderedLayer.update(...) for each layers to be
                     * included in this offscreen buffer.
                     */
                    final Graphics2D graphicsOff = (Graphics2D) buffer.getGraphics();
                    final Composite oldComposite = graphicsOff.getComposite();
                    graphicsOff.addRenderingHints(hints);
                    graphicsOff.translate(-zoomableBounds.x, -zoomableBounds.y);
                    if (offscreenIsVolatile[offscreenIndex]) {
                        // HACK: We should use the transparent color in all cases. However,
                        //       as of J2SE 1.4, VolatileImage doesn't supports transparency.
                        //       We have to use some opaque color in the main time.
                        // TODO: Delete this hack when J2SE 1.5 will be available.
                        //       Avoid filling if the image has just been created.
                        graphicsOff.setComposite(AlphaComposite.Src);
                    } else {
                        graphicsOff.setComposite(AlphaComposite.Clear);
                    }
                    graphicsOff.setColor(mapPane!=null ? mapPane.getBackground() : Color.WHITE);
                    graphicsOff.fill(bufferClip);
                    graphicsOff.setComposite(oldComposite); // REVISIT: is it the best composite?
                    graphicsOff.setColor(mapPane!=null ? mapPane.getForeground() : Color.BLACK);
                    graphicsOff.clip(bufferClip); // Information needed by some layers.
                    graphicsOff.transform(zoom);
                    if (contextInitialized) {
                        context.setGraphics(graphicsOff);
                    } else {
                        context.init(graphicsOff, zoomableBounds, isPrinting);
                        contextInitialized = true;
                    }
                    offscreenNeedRepaint[offscreenIndex] = false;
                    for (int i=layerIndex; i<layerIndexUp; i++) {
                        try {
                            layers[i].update(context, bufferClip);
                        } catch (Exception exception) {
                            /*
                             * An exception occured in user code. Do not try anymore to use
                             * offscreen buffer for this layer; render it in the "ordinary"
                             * loop instead. If this exception still occurs, it will be the
                             * "ordinary" loop's job to handle it.
                             */
                            context.disposeGraphics();
                            buffer.flush();
                            offscreenBuffers[offscreenIndex] = buffer = null;
                            layerIndexUp = layerIndex; // Force re-rendering of this layer.
                            minZOrder    = Float.NaN;  // Disable offscreen for all layers.
                            handleOffscreenException(layers[i], exception);
                            break renderOffscreen;
                        }
                        if (contentsLost(buffer)) {
                            break;
                        }
                    }
                    context.disposeGraphics();
                }
                /*
                 * The offscreen buffer has been successfully rendered (or we failed because
                 * of an exception in user's code). If the ordinary (clipped) context was
                 * modified, it will be restored in the "ordinary" loop when first needed.
                 */
                layerIndex = layerIndexUp-1;
            }
        } finally {
            context       .init(null, null, false);
            clippedContext.init(null, null, false);
            RenderedLayer .setDirtyArea(layers, layerCount, null);
            graphics      .setTransform(toDevice);
        }
        /*
         * If this map took a long time to render, log a message.
         */
        statistics.finish(this);
    }

    /**
     * Declares that the {@link Component} need to be repainted. This method can be invoked
     * from any thread (it doesn't need to be the <cite>Swing</cite> thread). Note that this
     * method doesn't invoke any {@link #flushOffscreenBuffer} method; this is up to the caller
     * to invokes the appropriate method.
     */
    private void repaint() {
        final Component mapPane = this.mapPane;
        if (mapPane != null) {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        repaint();
                    }
                });
                return;
            }
            mapPane.repaint();
        }
    }

    /**
     * Transform the specified rectangle from the rendering coordinate system to the "dot"
     * coordinate system used by Java2D. The transformation used is the one used the last
     * time the {@link #paint} method was invoked.
     *
     * @param  bounds The rectangle in "real world" rendering coordinates.
     * @return The rectangle in "dot" (or Java2D) coordinates.
     */
    final Rectangle mapToText(final Rectangle2D bounds) {
        return (Rectangle) XAffineTransform.transform(mapToText, bounds, new Rectangle());
    }

    /**
     * Méthode appelée lorsqu'une exception {@link TransformException} non-gérée
     * s'est produite. Cette méthode peut être appelée pendant le traçage de la carte
     * où les mouvements de la souris. Elle ne devrait donc pas utiliser une boîte de
     * dialogue pour reporter l'erreur, et retourner rapidement pour éviter de bloquer
     * la queue des événements de <i>Swing</i>.
     *
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     * @param  exception        The exception.
     */
    static void handleException(final String className,
                                final String methodName,
                                final Exception exception)
    {
        Utilities.unexpectedException("org.geotools.renderer.j2d", className, methodName, exception);
    }

    /**
     * Log a warning message when the offscreen rendering failed for some layer.
     * The message is logged with a low level (FINE rather than WARNING) because
     * an other attempt will be done using the default rendering loop (without
     * offscreen buffer).
     *
     * @param layer The layer for which the offscreen rendering failed.
     * @param exception The exception.
     */
    private void handleOffscreenException(final RenderedLayer layer, final Exception exception) {
        final Locale locale = getLocale();
        final LogRecord record = Resources.getResources(locale).getLogRecord(Level.FINE,
                ResourceKeys.WARNING_OFFSCREEN_RENDERING_FAILED_$1, layer.getName(locale));
        record.setSourceClassName("Renderer");
        record.setSourceMethodName("paint");
        record.setThrown(exception);
        LOGGER.log(record);
    }



    //////////////////////////////////////////////////////////////////
    ////////                                                  ////////
    ////////                     Events                       ////////
    ////////                                                  ////////
    //////////////////////////////////////////////////////////////////
    /**
     * Returns the string to be used as the tooltip for a given mouse event.
     * This method queries registered {@linkplain RenderedLayer layers} in decreasing
     * {@linkplain RenderedLayer#getZOrder z-order} until one is found to returns a
     * non-null string.
     *
     * <strong>Note: This method is not a commited part of the API.
     *         It may moves elsewhere in a future version.</strong>
     *
     * @param  event The mouse event.
     * @return The tool tip text, or <code>null</code> if there is no tool tip for the given
     *         mouse location.
     */
    public synchronized String getToolTipText(final GeoMouseEvent event) {
        sortLayers();
        final int x = event.getX();
        final int y = event.getY();
        final RenderedLayer[] layers = this.layers;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            if (layer.contains(x,y)) {
                final String tooltip = layer.getToolTipText(event);
                if (tooltip != null) {
                    return tooltip;
                }
            }
        }
        return null;
    }

    /**
     * Returns the action to be used for a given mouse event.
     * This method queries registered {@linkplain RenderedLayer layers} in decreasing
     * {@linkplain RenderedLayer#getZOrder z-order} until one is found to returns a
     * non-null action.
     *
     * <strong>Note: This method is not a commited part of the API.
     *         It may moves elsewhere in a future version.</strong>
     *
     * @param  event The mouse event.
     * @return The action, or <code>null</code> if there is no action for the given mouve event.
     */
    public synchronized Action getAction(final GeoMouseEvent event) {
        sortLayers();
        final int x = event.getX();
        final int y = event.getY();
        final RenderedLayer[] layers = this.layers;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            if (layer.contains(x,y)) {
                final Action action = layer.getAction(event);
                if (action != null) {
                    return action;
                }
            }
        }
        return null;
    }

    /**
     * Format a value for the current mouse position. This method invokes
     * {@link RenderedLayer#formatValue} for registered {@linkplain RenderedLayer
     * layers} in decreasing {@linkplain RenderedLayer#getZOrder z-order} until
     * one is found to returns <code>true</code>.
     *
     * @param  event The mouse event.
     * @param  toAppendTo The destination buffer for formatting a value.
     * @return <code>true</code> if this method has formatted a value,
     *         or <code>false</code> otherwise.
     *
     * @see Tools#formatValue
     * @see MouseCoordinateFormat#format(GeoMouseEvent)
     */
    final synchronized boolean formatValue(final GeoMouseEvent event,
                                           final StringBuffer toAppendTo)
    {
        sortLayers();
        final int x = event.getX();
        final int y = event.getY();
        final RenderedLayer[] layers = this.layers;
        for (int i=layerCount; --i>=0;) {
            final RenderedLayer layer = layers[i];
            if (layer.contains(x,y) && ! layer.getName(getLocale()).startsWith("hidden__")) {
                if (layer.formatValue(event, toAppendTo)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Invoked when the zoom change. This method update geographic
     * coordinates of cached shapes contained in {@link RenderedLayer}.
     *
     * @param change The zoom <strong>change</strong> in <strong>device</strong> coordinate
     *        system, or <code>null</code> if unknow. If <code>null</code>, then all layers
     *        will be fully redrawn during the next rendering.
     */
    private void zoomChanged(final AffineTransform change) {
        if (change!=null && change.isIdentity()) {
            return;
        }
        assert Thread.holdsLock(this);
        flushOffscreenBuffers();
        if (change == null) {
            // Paranoiac clean only if there is a major change in display.
            if (offscreenBuffers != null) {
                Arrays.fill(offscreenBuffers, null);
            }
        }
        for (int i=layerCount; --i>=0;) {
            layers[i].zoomChanged(change);
        }
    }

    /**
     * Invoked automatically everytime the scale changed. This method is usually (but not always)
     * invoked together with {@link #zoomChanged}. Note that some zoom changes do not imply a
     * scale change. For example the zoom change may be just a translation or a rotation.
     *
     * @param scaleFactor The new scale factor.
     */
    private void scaleChanged(final float scaleFactor) {
        final Float oldScale = this.scaleFactor;
        if (oldScale==null || oldScale.floatValue()!=scaleFactor) {
            final Float newScale = new Float(scaleFactor);
            this.scaleFactor = newScale;
            for (int i=layerCount; --i>=0;) {
                layers[i].listeners.firePropertyChange("scale", oldScale, newScale);
            }
            listeners.firePropertyChange("scale", oldScale, newScale);
        }
    }

    /**
     * Add a property change listener to the listener list. The listener is
     * registered for all properties. For example, adding or removing layers
     * may fire a <code>"preferredArea"</code> change events.
     *
     * @param listener The property change listener to be added
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Add a <code>PropertyChangeListener</code> for a specific property.
     * The listener will be invoked only when that specific property changes.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener(final String propertyName,
                                          final PropertyChangeListener listener)
    {
        listeners.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove a property change listener from the listener list.
     * This removes a <code>PropertyChangeListener</code> that
     * was registered for all properties.
     *
     * @param listener The property change listener to be removed
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param propertyName The name of the property that was listened on.
     * @param listener     The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(final String propertyName,
                                             final PropertyChangeListener listener)
    {
        listeners.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Clear all cached data. Invoking this method may help to release some resources for other
     * applications. It should be invoked when we know that the map is not going to be rendered
     * for a while. For example it should be invoked from {@link java.applet.Applet#stop}. Note
     * that this method doesn't changes the renderer setting; it will just slow down the first
     * rendering after this method call.
     *
     * @see #dispose
     */
    private void clearCache() {
        assert Thread.holdsLock(this);
        flushOffscreenBuffers();
        if (offscreenBuffers != null) {
            Arrays.fill(offscreenBuffers, null);
        }
        for (int i=layerCount; --i>=0;) {
            layers[i].clearCache();
        }
        transforms.clear();
    }

    /**
     * Provides a hint that a renderer will no longer be accessed from a reference in user
     * space. The results are equivalent to those that occur when the program loses its
     * last reference to this renderer, the garbage collector discovers this, and finalize
     * is called. This can be used as a hint in situations where waiting for garbage
     * collection would be overly conservative.
     * <br><br>
     * <code>Renderer</code> defines this method to invoke {@link RenderedLayer#dispose} for
     * all layers. The results of referencing a renderer or any of its layers after a call to
     * <code>dispose()</code> are undefined. However, invoking this method more than once is
     * safe.
     *
     * @see RenderedLayer#dispose
     * @see PlanarImage#dispose
     */
    public synchronized void dispose() {
        flushOffscreenBuffers();
        offscreenZRanges     = null;
        offscreenBuffers     = null;
        offscreenIsVolatile  = null;
        offscreenNeedRepaint = null;
        final RenderedLayer[] layers = new RenderedLayer[layerCount];
        if (layerCount != 0) {
            System.arraycopy(this.layers, 0, layers, 0, layerCount);
        }
        removeAllLayers();
        for (int i=layerCount; --i>=0;) {
            layers[i].dispose();
        }
        clearCache();
        final PropertyChangeListener[] list = listeners.getPropertyChangeListeners();
        for (int i=list.length; --i>=0;) {
            listeners.removePropertyChangeListener(list[i]);
        }
    }

    /**
     * Returns a string representation of this renderer and all its {@link RenderedLayer}s.
     * The {@linkplain #getOffscreenBuffered offscreen buffer type}, if any, appears in the
     * right column. This method is for debugging purpose only and may change in any future
     * version.
     */
    public synchronized String toString() {
        sortLayers();
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append("[\"");
        buffer.append(getName(null));
        buffer.append("\", ");
        buffer.append(layerCount);
        buffer.append(" layers]");
        buffer.append(lineSeparator);
        int maxLength = 0;
        final String[] names = new String[layerCount];
        for (int i=0; i<layerCount; i++) {
            final int length = (names[i] = String.valueOf(layers[i]).trim()).length();
            if (length > maxLength) {
                maxLength = length;
            }
        }
        for (int i=0; i<layerCount; i++) {
            buffer.append("    ");
            buffer.append(names[i]);
            final ImageType type = getOffscreenBuffered(layers[i].getZOrder());
            if (!ImageType.NONE.equals(type)) {
                buffer.append(Utilities.spaces(maxLength-names[i].length() + 3));
                buffer.append('(');
                buffer.append(type.getName());
                buffer.append(')');
            }
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }
}
