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
package org.geotools.gc;

// Images
import java.awt.Color;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.media.jai.ImageFunction;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.NullOpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.LookupDescriptor;
import javax.media.jai.operator.PiecewiseDescriptor;
import javax.media.jai.operator.RescaleDescriptor;
import javax.media.jai.remote.SerializableRenderedImage;
import javax.media.jai.util.CaselessStringKey;

import org.geotools.cs.AxisOrientation;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform1D;
import org.geotools.cv.CannotEvaluateException;
import org.geotools.cv.Category;
import org.geotools.cv.Coverage;
import org.geotools.cv.PointOutsideCoverageException;
import org.geotools.cv.SampleDimension;
import org.geotools.cv.SampleDimensionType;
import org.geotools.io.LineWriter;
import org.geotools.pt.CoordinatePoint;
import org.geotools.pt.Envelope;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.resources.LegacyGCSUtilities;
import org.geotools.resources.XArray;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.units.Unit;
import org.geotools.util.NumberRange;
import org.geotools.util.WeakHashSet;
import org.opengis.gc.GC_GridCoverage;
import org.opengis.gc.GC_GridGeometry;
import org.opengis.gc.GC_GridPacking;
import org.opengis.gc.GC_GridRange;
import org.opengis.referencing.operation.TransformException;
// Java Advanced Imaging
// For Javadoc
// For Javadoc
// For Javadoc
// For Javadoc
// For Javadoc
// For Javadoc
// Geometry
// Miscellaneous
// For Javadoc
// For Javadoc
// OpenGIS dependencies
// OpenGIS dependencies
// Geotools dependencies
// For Javadoc
// Resources


/**
 * Basic access to grid data values. Grid coverages are backed by
 * {@link RenderedImage}. Each band in an image is represented as
 * a sample dimension.
 * <br><br>
 * Grid coverages are usually two-dimensional. However, their envelope may
 * have more than two dimensions.  For example, a remote sensing image may
 * be valid only over some time range (the time of satellite pass over the
 * observed area). Envelope for such grid coverage may have three dimensions:
 * the two usual ones (horizontal extends along <var>x</var> and <var>y</var>),
 * and a third one for start time and end time (time extends along <var>t</var>).
 *
 * @version $Id: GridCoverage.java 10249 2005-01-05 01:38:37Z jmacgill $
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see GC_GridCoverage
 */
public class GridCoverage extends Coverage {
    /**
     * Slight number for rounding errors in floating point comparaison.
     */
    private static final float EPS = 1E-5f;

    /**
     * <code>true</code> if we should apply a conservative policy for the "piecewise" operation.
     * The conservative policy is to apply "piecewise" only if there is no ambiguity about what
     * the user wants.
     */
    private static final boolean CONSERVATIVE_PIECEWISE = true;

    /**
     * Axis orientation of image's coordinate systems. In most images, <var>x</var> values are
     * increasing toward the right (<code>EAST</code>)  and <var>y</var> values are increasing
     * toward the bottom (<code>SOUTH</code>). This is different to many geographic coordinate
     * systems, which have <var>y</var> values increasing <code>NORTH</code>. The grid coverage
     * constructor will compare the geographic axis orientations to this
     * <code>IMAGE_ORIENTATION</code> and inverse the <var>y</var> axis if necessary. The axis
     * inversions are handle by {@link GridGeometry#getGridToCoordinateSystem()}.
     */
    private static final AxisOrientation[] IMAGE_ORIENTATION = {
        AxisOrientation.EAST,
        AxisOrientation.SOUTH
    };
    
    /**
     * Pool of created object. Objects in this pool must be immutable.
     * Those objects will be shared among many grid coverages.
     */
    private static final WeakHashSet pool = new WeakHashSet();
    
    /**
     * An empty list of grid coverage.
     */
    private static final GridCoverage[] EMPTY_LIST = new GridCoverage[0];
    
    /**
     * Sources grid coverage, or <code>null</code> if none.
     * This information is lost during serialization.
     */
    private final transient GridCoverage[] sources;

    /**
     * A grid coverage using the sample dimensions <code>SampleDimension.inverse</code>.
     * This object is constructed and returned by {@link #geophysics}. Constructed when
     * first needed. May appears also in the <code>sources</code> list.
     */
    private transient GridCoverage inverse;
    
    /**
     * The raster data.
     *
     * @todo This field should be final. It is not only for internal reason (namely:
     *       deserialization). Consider making this field private in a future version.
     */
    protected transient PlanarImage image;

    /**
     * The serialized image, as an instance of {@link SerializableRenderedImage}.
     * This image will be created only when first needed during serialization.
     */
    private RenderedImage serializedImage;
    
    /**
     * The grid geometry.
     */
    protected final GridGeometry gridGeometry;
    
    /**
     * The image's envelope. This envelope must have at least two
     * dimensions. It may have more dimensions if the image have
     * some extend in other dimensions (for example a depth, or
     * a start and end time).
     */
    private final Envelope envelope;
    
    /**
     * List of sample dimension information for the grid coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension. A coverage must have at least one sample dimension.
     */
    private final SampleDimension[] sampleDimensions;

    /**
     * <code>true</code> is all sample in the image are geophysics values.
     */
    private final boolean isGeophysics;
    
    /**
     * Construct a new grid coverage with the same parameter than the specified
     * coverage. This constructor is useful when creating a coverage with
     * identical data, but in which some method has been overriden in order to
     * process data differently (e.g. interpolating them).
     *
     * @param coverage The source grid coverage.
     */
    protected GridCoverage(final GridCoverage coverage) {
        super(coverage);
        image            = coverage.image;
        gridGeometry     = coverage.gridGeometry;
        envelope         = coverage.envelope;
        sampleDimensions = coverage.sampleDimensions;
        isGeophysics     = coverage.isGeophysics;
        sources          = new GridCoverage[] {coverage};
    }
    
    /**
     * Construt a grid coverage from an image function.
     *
     * @param name         The grid coverage name.
     * @param function     The image function.
     * @param cs           The coordinate system. This specifies the coordinate system used
     *                     when accessing a grid coverage with the "evaluate" methods.  The
     *                     number of dimensions must matches the number of dimensions for
     *                     the grid range in <code>gridGeometry</code>.
     * @param gridGeometry The grid geometry. The grid range must contains the expected
     *                     image size (width and height).
     * @param bands        Sample dimensions for each image band, or <code>null</code> for
     *                     default sample dimensions. If non-null, then this array's length
     *                     must matches the number of bands in <code>image</code>.
     * @param properties The set of properties for this coverage, or <code>null</code>
     *        if there is none. "Properties" in <em>Java Advanced Imaging</em> is what
     *        OpenGIS calls "Metadata".  There is no <code>getMetadataValue(...)</code>
     *        method in this implementation. Use {@link #getProperty} instead. Keys may
     *        be {@link String} or {@link CaselessStringKey} objects,  while values may
     *        be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the grid range's dimension
     *         is not the same than the coordinate system's dimension.
     */
    public GridCoverage(final String             name, final ImageFunction    function,
                        final CoordinateSystem     cs, final GridGeometry gridGeometry,
                        final SampleDimension[] bands, final Map properties)
        throws MismatchedDimensionException
    {
        this(name, getImage(function, gridGeometry),
             cs, gridGeometry, null, bands, null, properties);
    }
    
    /**
     * Create an image from an image function.  Translation and scale
     * factors are fetched from the grid geometry, which must have an
     * affine transform.
     *
     * @task TODO: We could support shear in affine transform.
     * @task TODO: Should be inlined in the above constructor if only Sun was to fix RFE #4093999
     *             ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static PlanarImage getImage(final ImageFunction function,
                                        final GridGeometry gridGeometry)
    {
        final MathTransform transform = gridGeometry.getGridToCoordinateSystem2D();
        if (!(transform instanceof AffineTransform)) {
            throw new IllegalArgumentException(org.geotools.resources.cts.Resources.format(
                    org.geotools.resources.cts.ResourceKeys.ERROR_NOT_AN_AFFINE_TRANSFORM));
        }
        final AffineTransform at = (AffineTransform) transform;
        if (at.getShearX()!=0 || at.getShearY()!=0) {
            // TODO: We may support that in a future version.
            //       1) Create a copy with shear[X/Y] set to 0. Use the copy.
            //       2) Compute the residu with createInverse() and concatenate().
            //       3) Apply the residu with JAI.create("Affine").
            throw new IllegalArgumentException("Shear and rotation not supported");
        }
        final double xScale =  at.getScaleX();
        final double yScale =  at.getScaleY();
        final double xTrans = -at.getTranslateX()/xScale;
        final double yTrans = -at.getTranslateY()/yScale;
        final GridRange      range = gridGeometry.getGridRange();
        final ParameterBlock param = new ParameterBlock().add(function)
                                                         .add(range.getLength(0)) // width
                                                         .add(range.getLength(1)) // height
                                                         .add((float) xScale)
                                                         .add((float) yScale)
                                                         .add((float) xTrans)
                                                         .add((float) yTrans);
        return JAI.create("ImageFunction", param);
    }

    /**
     * Constructs a grid coverage from a raster and an envelope in
     * <var>longitude</var>,<var>latitude</var> coordinates. The coordinate system is assumed to
     * be based on {@linkplain GeographicCoordinateSystem#WGS84 WGS84}. A default color palette
     * is built from the minimal and maximal values found in the raster.
     *
     * @param name     The grid coverage name.
     * @param raster   The data (may be floating point numbers). {@linkplain Float#NaN NaN}
     *                 values are mapped to a transparent color.
     * @param envelope The envelope in geographic (<var>longitude</var>,<var>latitude</var>)
     *                 coordinates.
     *
     * @throws MismatchedDimensionException If the envelope's dimension is not 2.
     */
    public GridCoverage(final String name, final WritableRaster raster, final Envelope envelope)
            throws MismatchedDimensionException
    {
        this(name, raster, GeographicCoordinateSystem.WGS84, envelope, null, null, null, null, null);
    }

    /**
     * Constructs a grid coverage from a {@linkplain Raster raster} with the specified
     * {@linkplain Envelope envelope}.
     *
     * @param name        The grid coverage name.
     * @param raster      The data (may be floating point numbers). {@linkplain Float#NaN NaN}
     *                    values are mapped to a transparent color.
     * @param cs          The coordinate system. This specifies the coordinate system used
     *                    when accessing a grid coverage with the "evaluate" methods.  The
     *                    number of dimensions must matches the number of dimensions for
     *                    <code>envelope</code>.
     * @param envelope    The grid coverage cordinates. This envelope must have at least two
     *                    dimensions.   The two first dimensions describe the image location
     *                    along <var>x</var> and <var>y</var> axis. The other dimensions are
     *                    optional and may be used to locate the image on a vertical axis or
     *                    on the time axis.
     * @param minValues   The minimal value for each bands in the raster, or <code>null</code>
     *                    for computing it automatically.
     * @param maxValues   The maximal value for each bands in the raster, or <code>null</code>
     *                    for computing it automatically.
     * @param units       The units of sample values, or <code>null</code> if unknow.
     * @param colors      The colors to use for values from <code>minValues</code> to
     *                    <code>maxValues</code> for each bands, or <code>null</code> for a
     *                    default color palette. If non-null, each arrays <code>colors[b]</code>
     *                    may have any length; colors will be interpolated as needed.
     * @param hints       An optional set of rendering hints, or <code>null</code> if none.
     *                    Those hints will not affect the grid coverage to be created. However,
     *                    they may affect the grid coverage to be returned by
     *                    <code>{@link #geophysics geophysics}(false)</code>, i.e.
     *                    the view to be used at rendering time. The optional hint
     *                    {@link org.geotools.gp.Hints#SAMPLE_DIMENSION_TYPE} specifies the
     *                    {@link SampleDimensionType} to be used at rendering time, which can be
     *                    one of {@link SampleDimensionType#UBYTE UBYTE} or
     *                    {@link SampleDimensionType#USHORT USHORT}.
     *
     * @throws MismatchedDimensionException If the envelope's dimension
     *         is not the same than the coordinate system's dimension.
     * @throws IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage(final String name,         final WritableRaster raster,
                        final CoordinateSystem cs, final Envelope       envelope,
                        final double[] minValues,  final double[]       maxValues,
                        final Unit       units,
                        final Color[][]  colors,   final RenderingHints hints)
    {
        this(name, raster, cs, null, (Envelope)envelope.clone(),
             GridSampleDimension.create(name, raster, minValues, maxValues, units, colors, hints));
    }

    /**
     * Constructs a grid coverage from a {@linkplain Raster raster} with the specified
     * "{@linkplain GridGeometry#getGridToCoordinateSystem grid to coordinate system}"
     * transform.
     *
     * @param name        The grid coverage name.
     * @param raster      The data (may be floating point numbers). {@linkplain Float#NaN NaN}
     *                    values are mapped to a transparent color.
     * @param cs          The coordinate system. This specifies the coordinate system used
     *                    when accessing a grid coverage with the "evaluate" methods.
     * @param gridToCS    The math transform from grid to coordinate system.
     * @param minValues   The minimal value for each bands in the raster, or <code>null</code>
     *                    for computing it automatically.
     * @param maxValues   The maximal value for each bands in the raster, or <code>null</code>
     *                    for computing it automatically.
     * @param units       The units of sample values, or <code>null</code> if unknow.
     * @param colors      The colors to use for values from <code>minValues</code> to
     *                    <code>maxValues</code> for each bands, or <code>null</code> for a
     *                    default color palette. If non-null, each arrays <code>colors[b]</code>
     *                    may have any length; colors will be interpolated as needed.
     * @param hints       An optional set of rendering hints, or <code>null</code> if none.
     *                    Those hints will not affect the grid coverage to be created. However,
     *                    they may affect the grid coverage to be returned by
     *                    <code>{@link #geophysics geophysics}(false)</code>, i.e.
     *                    the view to be used at rendering time. The optional hint
     *                    {@link org.geotools.gp.Hints#SAMPLE_DIMENSION_TYPE} specifies the
     *                    {@link SampleDimensionType} to be used at rendering time, which can be
     *                    one of {@link SampleDimensionType#UBYTE UBYTE} or
     *                    {@link SampleDimensionType#USHORT USHORT}.
     *
     * @throws MismatchedDimensionException If the <code>gridToCS</code> dimension
     *         is not the same than the coordinate system's dimension.
     * @throws IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage(final String name,         final WritableRaster raster,
                        final CoordinateSystem cs, final MathTransform  gridToCS,
                        final double[] minValues,  final double[]       maxValues,
                        final Unit       units,
                        final Color[][]  colors,   final RenderingHints hints)
    {
        this(name, raster, cs, new GridGeometry(null, gridToCS), null,
             GridSampleDimension.create(name, raster, minValues, maxValues, units, colors, hints));
    }

    /**
     * Helper constructor for public constructors expecting a {@link Raster} argument.
     * 
     * NOTE - Steve.Ansari modification - made public 
     *
     * @task TODO: Should be inlined in the above constructor if only Sun was to fix RFE #4093999
     *             ("Relax constraint on placement of this()/super() call in constructors").
     */
    public GridCoverage(final String               name,
                         final WritableRaster     raster,
                         final CoordinateSystem       cs,
                               GridGeometry gridGeometry, // ONE and  only  one of those
                               Envelope         envelope, // two arguments should be non-null.
                         final SampleDimension[]   bands)
    {
        this(name,
             PlanarImage.wrapRenderedImage(
                new BufferedImage(bands[0].getColorModel(0, bands.length), raster, false, null)),
             cs, gridGeometry, envelope, bands, null, null);
    }
    
    /**
     * Construct a grid coverage with the specified envelope. A default set of
     * {@linkplain SampleDimension sample dimensions} is used.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param cs           The coordinate system. This specifies the coordinate system used
     *                     when accessing a grid coverage with the "evaluate" methods.  The
     *                     number of dimensions must matches the number of dimensions for
     *                     <code>envelope</code>.
     * @param envelope     The grid coverage cordinates. This envelope must have at least two
     *                     dimensions.   The two first dimensions describe the image location
     *                     along <var>x</var> and <var>y</var> axis. The other dimensions are
     *                     optional and may be used to locate the image on a vertical axis or
     *                     on the time axis.
     *
     * @throws MismatchedDimensionException If the envelope's dimension
     *         is not the same than the coordinate system's dimension.
     */
    public GridCoverage(final String         name, final RenderedImage  image,
                        final CoordinateSystem cs, final Envelope    envelope)
        throws MismatchedDimensionException
    {
        this(name, image, cs, envelope, null, null, null);
    }
    
    /**
     * Construct a grid coverage with the specified envelope and sample dimensions.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param cs           The coordinate system. This specifies the coordinate system used
     *                     when accessing a grid coverage with the "evaluate" methods.  The
     *                     number of dimensions must matches the number of dimensions for
     *                     <code>envelope</code>.
     * @param envelope     The grid coverage cordinates. This envelope must have at least two
     *                     dimensions.   The two first dimensions describe the image location
     *                     along <var>x</var> and <var>y</var> axis. The other dimensions are
     *                     optional and may be used to locate the image on a vertical axis or
     *                     on the time axis.
     * @param bands        Sample dimensions for each image band, or <code>null</code> for
     *                     default sample dimensions. If non-null, then this array's length
     *                     must matches the number of bands in <code>image</code>.
     * @param sources      The sources for this grid coverage, or <code>null</code> if none.
     * @param properties The set of properties for this coverage, or <code>null</code>
     *        if there is none. "Properties" in <em>Java Advanced Imaging</em> is what
     *        OpenGIS calls "Metadata".  There is no <code>getMetadataValue(...)</code>
     *        method in this implementation. Use {@link #getProperty} instead. Keys may
     *        be {@link String} or {@link CaselessStringKey} objects,  while values may
     *        be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the envelope's dimension
     *         is not the same than the coordinate system's dimension.
     * @throws IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage(final String             name, final RenderedImage    image,
                        final CoordinateSystem     cs, final Envelope      envelope,
                        final SampleDimension[] bands, final GridCoverage[] sources,
                        final Map properties)
        throws MismatchedDimensionException
    {
        this(name, PlanarImage.wrapRenderedImage(image), cs, null,
             (Envelope)envelope.clone(), bands, sources, properties);
    }
    
    /**
     * Construct a grid coverage with the specified transform and sample dimension.
     * This is the most general constructor, the one that gives the maximum control
     * on the grid coverage to be created.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param cs           The coordinate system. This specifies the coordinate system used
     *                     when accessing a grid coverage with the "evaluate" methods.  The
     *                     number of dimensions must matches the number of dimensions for
     *                     <code>gridToCS</code>.
     * @param gridToCS     The math transform from grid to coordinate system.
     * @param bands        Sample dimensions for each image band, or <code>null</code> for
     *                     default sample dimensions. If non-null, then this array's length
     *                     must matches the number of bands in <code>image</code>.
     * @param sources      The sources for this grid coverage, or <code>null</code> if none.
     * @param properties The set of properties for this coverage, or <code>null</code>
     *        if there is none. "Properties" in <em>Java Advanced Imaging</em> is what
     *        OpenGIS calls "Metadata".  There is no <code>getMetadataValue(...)</code>
     *        method in this implementation. Use {@link #getProperty} instead. Keys may
     *        be {@link String} or {@link CaselessStringKey} objects,  while values may
     *        be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the transform's dimension
     *         is not the same than the coordinate system's dimension.
     * @throws IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage(final String             name, final RenderedImage    image,
                        final CoordinateSystem     cs, final MathTransform gridToCS,
                        final SampleDimension[] bands, final GridCoverage[] sources,
                        final Map properties)
        throws MismatchedDimensionException
    {
        this(name, PlanarImage.wrapRenderedImage(image), cs,
             new GridGeometry(null, gridToCS),
             null, bands, sources, properties);
    }
    
    /**
     * Construct a grid coverage. This private constructor expect an envelope
     * (<code>envelope</code>) or a grid geometry (<code>gridGeometry</code>).
     * <strong>One and only one of those argument</strong> should be non-null.
     * The null arguments will be computed from the non-null argument.
     */
    private GridCoverage(final String               name,
                         final PlanarImage         image,
                         final CoordinateSystem       cs,
                               GridGeometry gridGeometry, // ONE and  only  one of those
                               Envelope         envelope, // two arguments should be non-null.
                         final SampleDimension[] sdBands,
                         final GridCoverage[]    sources,
                         final Map            properties)
        throws MismatchedDimensionException
    {
        super(name, cs, image, properties);
        if ((gridGeometry==null) == (envelope==null)) {
            // Should not happen
            throw new AssertionError();
        }
        if (sources != null) {
            this.sources = (GridCoverage[]) sources.clone();
        } else {
            this.sources = null;
        }
        this.image = image;
        /*
         * Check sample dimensions. The number of SampleDimensions must matches the
         * number of image's bands (this is checked by GridSampleDimension.create).
         */
        sampleDimensions = new SampleDimension[image.getNumBands()];
        isGeophysics     = GridSampleDimension.create(name, image, sdBands, sampleDimensions);
        /*
         * Constructs the grid range and the envelope if they were not explicitly provided.
         * The envelope computation (if needed)  requires  a valid 'gridToCoordinateSystem'
         * transform in the GridGeometry.  Otherwise, no transform are required.  The range
         * will be inferred from the image size, if needed. In any cases, the envelope must
         * be non-empty and its dimension must matches the coordinate system's dimension. A
         * pool of shared envelopes will be used in order to recycle existing envelopes.
         */
        final GridRange gridRange;
        if (LegacyGCSUtilities.hasGridRange(gridGeometry)) {
            gridRange = gridGeometry.getGridRange();
        } else {
            gridRange = new GridRange(image, cs.getDimension());
            if (LegacyGCSUtilities.hasTransform(gridGeometry)) {
                gridGeometry=new GridGeometry(gridRange, gridGeometry.getGridToCoordinateSystem());
            }
        }
        // Check the GridRange...
        if (true) {
            final String error = checkConsistency(image, gridRange);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        }
        // Check the Envelope...
        if (envelope == null) {
            envelope = gridGeometry.getEnvelope();
        }
        final int dimension = envelope.getDimension();
        if (envelope.isEmpty() || dimension<2) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_EMPTY_ENVELOPE));
        }
        if (dimension != cs.getDimension()) {
            throw new MismatchedDimensionException(cs, envelope);
        }
        this.envelope = (Envelope)pool.canonicalize(envelope);
        /*
         * Compute the grid geometry. The math transform will be computed from the envelope.
         * A pool of shared grid geometries will be used in order to recycle existing objects.
         *
         * Note: Should we invert some axis? For example, the 'y' axis is often inversed
         *       (since image use a downward 'y' axis). If all source grid coverages use
         *       the same axis orientations, we will reuse those orientations. Otherwise,
         *       we will use default orientations where only the 'y' axis is inversed.
         */
        if (gridGeometry == null) {
            boolean[] inverse = null;
            if (sources != null) {
                for (int i=0; i<sources.length; i++) {
                    boolean check[] = sources[i].gridGeometry.areAxisInverted();
                    check = XArray.resize(check, dimension);
                    if (inverse!=null) {
                        if (!Arrays.equals(check, inverse)) {
                            inverse = null;
                            break;
                        }
                    } else {
                        inverse = check;
                    }
                }
            }
            if (inverse == null) {
                inverse = new boolean[dimension];
                for (int i=Math.min(IMAGE_ORIENTATION.length, dimension); --i>=0;) {
                    final AxisOrientation toInverse = IMAGE_ORIENTATION[i].inverse();
                    inverse[i] = toInverse.equals(cs.getAxis(1).orientation);
                }
            }
            gridGeometry = new GridGeometry(gridRange, envelope, inverse);
        }
        this.gridGeometry = (GridGeometry)pool.canonicalize(gridGeometry);
    }

    /**
     * Check if the boundix box of the specified image is consistents with the specified
     * grid range. If an inconsistency has been found, then an error string is returned.
     * This string will be typically used as a message in an exception to be thrown.
     * <br><br>
     * Note that a succesful check at construction time may fails later if the image is part
     * of a JAI chain (i.e. is a {@link RenderedOp}) and its bounds has been edited (i.e the
     * image node as been re-rendered). Since <code>GridCoverage</code> are immutable by design,
     * we are not allowed to propagate the image change here. The {@link #getGridGeometry} method
     * will thrown an {@link IllegalStateException} in this case.
     */
    private static String checkConsistency(final RenderedImage image, final GridRange range) {
        for (int i=0; i<=1; i++) {
            final int min, length;
            final Object label;
            switch (i) {
                case 0:  min=image.getMinX(); length=image.getWidth();  label="\"X\"";        break;
                case 1:  min=image.getMinY(); length=image.getHeight(); label="\"Y\"";        break;
                default: min=0;               length=1;                 label=new Integer(i); break;
            }
            if (range.getLower(i)!=min || range.getLength(i)!=length) {
                return Resources.format(ResourceKeys.ERROR_BAD_GRID_RANGE_$3, label,
                                        new Integer(min), new Integer(min+length));
            }
        }
        return null;
    }
    
    /**
     * Returns <code>true</code> if grid data can be edited. The default
     * implementation returns <code>true</code>  if  {@link #image} is an
     * instance of {@link WritableRenderedImage}.
     *
     * @see GC_GridCoverage#isDataEditable
     */
    public boolean isDataEditable() {
        return (image instanceof WritableRenderedImage);
    }
    
    /**
     * Returns the source data for a grid coverage. If the <code>GridCoverage</code>
     * was produced from an underlying dataset, the returned list is an empty list.
     * If the <code>GridCoverage</code> was produced using
     * {@link org.geotools.gp.GridCoverageProcessor} then it should return the source
     * grid coverage of the one used as input to <code>GridCoverageProcessor</code>.
     * In general the <code>getSources()</code> method is intended to return the original
     * <code>GridCoverage</code> on which it depends. This is intended to allow applications
     * to establish what <code>GridCoverage</code>s will be affected when others are updated,
     * as well as to trace back to the "raw data".
     */
    public GridCoverage[] getSources() {
        return (sources!=null) ? (GridCoverage[]) sources.clone() : EMPTY_LIST;
    }
    
    /**
     * Returns information for the grid coverage geometry. Grid geometry
     * includes the valid range of grid coordinates and the georeferencing.
     *
     * @see GC_GridCoverage#getGridGeometry
     */
    public GridGeometry getGridGeometry() {
        final String error = checkConsistency(image, gridGeometry.getGridRange());
        if (error != null) {
            throw new IllegalStateException(error);
        }
        return gridGeometry;
    }
    
    /**
     * Returns The bounding box for the coverage domain in coordinate
     * system coordinates.
     */
    public Envelope getEnvelope() {
        return (Envelope) envelope.clone();
    }
    
    /**
     * Retrieve sample dimension information for the coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension. A coverage must have at least one sample dimension.
     */
    public SampleDimension[] getSampleDimensions() {
        return (SampleDimension[]) sampleDimensions.clone();
    }

    /**
     * Returns the interpolation used for all <code>evaluate(...)</code> methods.
     * The default implementation returns {@link InterpolationNearest}.
     *
     * @return The interpolation.
     */
    public Interpolation getInterpolation() {
        return Interpolation.getInstance(Interpolation.INTERP_NEAREST);
    }
    
    /**
     * Returns a sequence of integer values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public int[] evaluate(final CoordinatePoint coord, final int[] dest)
        throws CannotEvaluateException
    {
        return evaluate(new Point2D.Double(coord.ord[0], coord.ord[1]), dest);
    }
    
    /**
     * Returns a sequence of float values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public float[] evaluate(final CoordinatePoint coord, final float[] dest)
        throws CannotEvaluateException
    {
        return evaluate(new Point2D.Double(coord.ord[0], coord.ord[1]), dest);
    }
    
    /**
     * Returns a sequence of double values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public double[] evaluate(final CoordinatePoint coord, final double[] dest)
        throws CannotEvaluateException
    {
        return evaluate(new Point2D.Double(coord.ord[0], coord.ord[1]), dest);
    }
    
    /**
     * Returns a sequence of integer values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public int[] evaluate(final Point2D coord, final int[] dest)
        throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(coord);
    }
    
    /**
     * Returns a sequence of float values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public float[] evaluate(final Point2D coord, final float[] dest)
        throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(coord);
    }
    
    /**
     * Returns a sequence of double values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or <code>null</code>.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public double[] evaluate(final Point2D coord, final double[] dest)
        throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(coord);
    }
    
    /**
     * Returns a debug string for the specified coordinate.   This method produces a
     * string with pixel coordinates and pixel values for all bands (with geophysics
     * values or category name in parenthesis). Example for a 1-banded image:
     *
     * <blockquote><pre>(1171,1566)=[196 (29.6 °C)]</pre></blockquote>
     *
     * @param  coord The coordinate point where to evaluate.
     * @return A string with pixel coordinates and pixel values at the specified location,
     *         or <code>null</code> if <code>coord</code> is outside coverage.
     */
    public synchronized String getDebugString(final CoordinatePoint coord) {
        Point2D pixel = new Point2D.Double(coord.ord[0], coord.ord[1]);
        pixel         = gridGeometry.inverseTransform(pixel);
        final int   x = (int)Math.round(pixel.getX());
        final int   y = (int)Math.round(pixel.getY());
        if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
            final int  numBands = image.getNumBands();
            final Raster raster = image.getTile(image.XToTileX(x), image.YToTileY(y));
            final int  datatype = image.getSampleModel().getDataType();
            final StringBuffer  buffer = new StringBuffer();
            buffer.append('(');
            buffer.append(x);
            buffer.append(',');
            buffer.append(y);
            buffer.append(")=[");
            for (int band=0; band<numBands; band++) {
                if (band!=0) {
                    buffer.append(";\u00A0");
                }
                final double sample = raster.getSampleDouble(x, y, band);
                switch (datatype) {
                    case DataBuffer.TYPE_DOUBLE: buffer.append((double)sample); break;
                    case DataBuffer.TYPE_FLOAT : buffer.append( (float)sample); break;
                    default                    : buffer.append(   (int)sample); break;
                }
                final String formatted = sampleDimensions[band].getLabel(sample, null);
                if (formatted != null) {
                    buffer.append("\u00A0(");
                    buffer.append(formatted);
                    buffer.append(')');
                }
            }
            buffer.append(']');
            return buffer.toString();
        }
        return null;
    }
    
    /**
     * Return a sequence of strongly typed values for a block.
     * A value for each sample dimension will be returned. The return value is an
     * <CODE>N+1</CODE> dimensional array, with dimensions. For 2 dimensional
     * grid coverages, this array will be accessed as (sample dimension, column,
     * row). The index values will be based from 0. The indices in the returned
     * <CODE>N</CODE> dimensional array will need to be offset by grid range
     * minimum coordinates to get equivalent grid coordinates.
     */
    //  public abstract DoubleMultiArray getDataBlockAsDouble(final GridRange range)
    //  {
    // TODO: Waiting for multiarray package (JSR-083)!
    //       Same for setDataBlock*
    //  }

    /**
     * Returns grid data as a rendered image.
     */
    public RenderedImage getRenderedImage() {
        return image;
    }
    
    /**
     * Hints that the given area may be needed in the near future. Some implementations
     * may spawn a thread or threads to compute the tiles while others may ignore the hint.
     *
     * @param area A rectangle indicating which geographic area to prefetch.
     *             This area's coordinates must be expressed according the
     *             grid coverage's coordinate system, as given by
     *             {@link #getCoordinateSystem}.
     */
    public void prefetch(final Rectangle2D area) {
        final Point[] tileIndices=image.getTileIndices(gridGeometry.inverseTransform(area));
        if (tileIndices!=null) {
            image.prefetchTiles(tileIndices);
        }
    }

    /**
     * If <code>true</code>, returns the geophysics companion of this grid coverage. In a
     * <cite>geophysics grid coverage</cite>, all sample values are equals to geophysics
     * ("real world") values without the need for any transformation. In such geophysics
     * coverage, the {@linkplain SampleDimension#getSampleToGeophysics sample to geophysics}
     * transform is the identity transform for all sample dimensions. "No data" values are
     * expressed by {@linkplain Float#NaN NaN} numbers.
     * <br><br>
     * This method may be understood as applying the JAI's {@linkplain PiecewiseDescriptor
     * piecewise} operation with breakpoints specified by the {@link Category} objects in
     * each sample dimension. However, it is more general in that the transformation specified
     * with each breakpoint doesn't need to be linear. On an implementation note, this method
     * will really try to use the first of the following operations which is found applicable:
     * <cite>identity</cite>, {@linkplain LookupDescriptor lookup}, {@linkplain RescaleDescriptor
     * rescale}, {@linkplain PiecewiseDescriptor piecewise} and in last ressort a more general
     * (but slower) <cite>sample transcoding</cite> algorithm.
     * <br><br>
     * <code>GridCoverage</code> objects live by pair: a <cite>geophysics</cite> one (used for
     * computation) and a <cite>non-geophysics</cite> one (used for packing data, usually as
     * integers). The <code>geo</code> argument specifies which object from the pair is wanted,
     * regardless if this method is invoked on the geophysics or non-geophysics instance of the
     * pair. In other words, the result of <code>geophysics(b1).geophysics(b2).geophysics(b3)</code>
     * depends only on the value in the last call (<code>b3</code>).
     *
     * @param  geo <code>true</code> to get a grid coverage with sample values equals to geophysics
     *         values, or <code>false</code> to get the packed version.
     * @return The grid coverage. Never <code>null</code>, but may be <code>this</code>.
     *
     * @see SampleDimension#geophysics
     * @see Category#geophysics
     * @see LookupDescriptor
     * @see RescaleDescriptor
     * @see PiecewiseDescriptor
     */
    public GridCoverage geophysics(final boolean geo) {
        if (geo == isGeophysics) {
            return this;
        }
        if (inverse != null) {
            return inverse;
        }
        if (!LegacyGCSUtilities.hasTransform(sampleDimensions)) {
            return inverse=this;
        }
        synchronized (this) {
            inverse = createGeophysics(geo);
            inverse = interpolate(inverse);
            if (inverse.inverse == null) {
                inverse.inverse = this;
            } else if (inverse.inverse != this) {
                final Locale locale = null;
                throw new RasterFormatException(Resources.getResources(locale).getString(
                          ResourceKeys.ERROR_COVERAGE_ALREADY_BOUND_$2,
                          "createGeophysics", inverse.inverse.getName(locale)));
            }
            return inverse;
        }
    }

    /**
     * Invoked by {@link #geophysics(boolean)} when the packed or geophysics companion of this
     * grid coverage need to be created. Subclasses may override this method in order to modify
     * the object to be created.
     *
     * @param  geo <code>true</code> to get a grid coverage with sample values equals to
     *         geophysics values, or <code>false</code> to get the packed version.
     * @return The newly created grid coverage.
     *
     * @task HACK: IndexColorModel seems to badly choose its sample model. As of JDK 1.4-rc1, it
     *             construct a ComponentSampleModel, which is drawn very slowly to the screen. A
     *             much faster sample model is PixelInterleavedSampleModel,  which is the sample
     *             model used by BufferedImage for TYPE_BYTE_INDEXED. We should check if this is
     *             fixed in future J2SE release.
     *
     * @task HACK: This method provides an optimisation for the case of a linear transformations:
     *             it use the JAI's "Rescale" or "Piecewise" operations, which may be hardware
     *             accelerated. Unfortunatly, bug #4726416 prevent us to use this optimisation
     *             with JAI 1.1.1. The optimisation is enabled only if we are running JAI 1.1.2.
     *             This hack should be removed when JAI 1.1.2 will be widely available.
     *
     * @task HACK: The "Piecewise" operation is disabled because javac 1.4.1_01 generate illegal
     *             bytecode. This bug is fixed in javac 1.4.2-beta. However, we still have an
     *             ArrayIndexOutOfBoundsException in JAI code...
     *
     * @task REVISIT: A special case (exactly one linear relationship with one NaN value mapping
     *                exactly to the index value 0) was optimized to the "Rescale" operation in
     *                previous version. This case is very common, which make this optimization a
     *                usefull one. Unfortunatly, it had to be disabled because there is nothing
     *                in the "Rescale" preventing some real number (not NaN) to maps to 0 through
     *                the normal linear relationship. Note that the optimization worked well in
     *                previous version except for the above-cited problem. We can very easily re-
     *                enable it later if we know the range of values really stored in the image
     *                (as of JAI's "extrema" operation). If would suffice to add a check making
     *                sure that the range of transformed values doesn't contains 0.
     */
    protected GridCoverage createGeophysics(final boolean geo) {
        /*
         * STEP 1 - Gets the source image and prepare the target sample dimensions.
         *          As a slight optimisation, we skip the "Null" operations since
         *          such image may be the result of some "Colormap" operation.
         */
        PlanarImage image = this.image;
        while (image instanceof NullOpImage) {
            final NullOpImage op = (NullOpImage) image;
            if (op.getNumSources() != 1) {
                break;
            }
            image = op.getSourceImage(0);
        }
        final int                  numBands = image.getNumBands();
        final int              visibleBand  = LegacyGCSUtilities.getVisibleBand(image);
        final SampleDimension[] targetBands = (SampleDimension[]) sampleDimensions.clone();
        assert targetBands.length == numBands : targetBands.length;
        for (int i=0; i<targetBands.length; i++) {
            targetBands[i] = targetBands[i].geophysics(geo);
        }
        /*
         * STEP 2 - Compute the layout for the destination RenderedImage. We will use the same
         *          layout than the parent image, except for tile size if the parent image had
         *          only one big tile, and for the color model and sample model  (since we are
         *          reformating data in the process of this operation).
         */
        ImageLayout layout = ImageUtilities.getImageLayout(image);
        ColorModel  colors = targetBands[visibleBand].getColorModel(visibleBand, numBands);
        SampleModel  model = colors.createCompatibleSampleModel(layout.getTileWidth (image),
                                                                layout.getTileHeight(image));
        if (colors instanceof IndexColorModel && model.getClass().equals(ComponentSampleModel.class))
        {
            // There is the 'IndexColorModel' hack (see method description).
            final int w = model.getWidth();
            final int h = model.getHeight();
            model = new PixelInterleavedSampleModel(colors.getTransferType(), w,h,1,w, new int[1]);
        }
        layout = layout.setSampleModel(model).setColorModel(colors);
        ParameterBlock param = new ParameterBlock().addSource(image);
        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        boolean JAI_112;
        try {
            // HACK for JAI 1.1.1 compatibility
            final Object key = JAI.class.getField("KEY_REPLACE_INDEX_COLOR_MODEL").get(null);
            hints.put(key, Boolean.FALSE);
            JAI_112 = true;
        } catch (Exception exception) {
            JAI_112 = false;
            // We are not running JAI 1.1.2. Ignore this exception,
            // since this key in know to not exist in earlier version.
        }
        String operation = null; // Will be set in step 3 or 4.
        /*
         * STEP 3 - Check if the transcoding could be done with the JAI's "Lookup" operation.
         *          This is probably the fatest operation available for 'geophysics(true)'.
         */
        try {
            final int sourceType = image.getSampleModel().getDataType();
            final int targetType = model.getDataType();
            final MathTransform1D[] transforms = new MathTransform1D[numBands];
            for (int i=0; i<numBands; i++) {
                transforms[i] = sampleDimensions[i].geophysics(false).getSampleToGeophysics();
                if (transforms[i]!=null && !geo) {
                    // We are going to convert geophysics values to packed one.
                    transforms[i] = (MathTransform1D) transforms[i].inverse();
                }
            }
            LookupTableJAI table = LookupTableFactory.create(sourceType, targetType, transforms);
            if (table != null) {
                operation = "Lookup";
                param = param.add(table);
            }
        } catch (TransformException exception) {
            // A value can't be transformed. Fallback on a more general operation.
            // REVISIT: the more general operations are likely to fail too...
        }
        /*
         * STEP 4 - Check if the transcoding could be done with a JAI's "Rescale" or "Piecewise"
         *          operations. The "Rescale" operation requires a completly linear relationship
         *          between the source and the destination sample values. The "Piecewise" operation
         *          is less strict: piecewise breakpoints are very similar to categories, but the
         *          transformation for all categories still have to be linear.
         */
        if (operation==null && JAI_112) try {
            boolean     canRescale   = true; // 'true' if the "Rescale"   operation can be applied.
            boolean     canPiecewise = true; // 'true' if the "Piecewise" operation can be applied.
            double[]    scales       = null; // The first  argument for "Rescale".
            double[]    offsets      = null; // The second argument for "Rescale".
            float[][][] breakpoints  = null; // The only   argument for "Piecewise".
testLinear: for (int i=0; i<numBands; i++) {
                final SampleDimension  sd = sampleDimensions[i];
                final List     categories = sd.getCategories();
                final int   numCategories = categories.size();
                float[] sourceBreakpoints = null;
                float[] targetBreakpoints = null;
                double expectedSource = Double.NaN;
                double expectedTarget = Double.NaN;
                int jbp = 0; // Break point index (vary with j)
                for (int j=0; j<numCategories; j++) {
                    final Category category = (Category) categories.get(j);
                    MathTransform1D transform = category.geophysics(false).getSampleToGeophysics();
                    if (transform == null) {
                        // A "qualitative" category was found. Those categories maps NaN values,
                        // which need the special processing by our "SampleTranscode" operation.
                        canPiecewise = false;
                        if (false) {
                            // As a special case, the "Rescale" operation  could continue to work
                            // if the NaN value maps to 0. Unfortunatly, this optimization had to
                            // be disabled for now for the reason explained in the REVISIT tag in
                            // method's comments.
                            if (category.geophysics(geo).getRange().getMinimum(true) == 0) {
                                assert Double.isNaN(category.getRange().getMinimum()) : category;
                                continue;
                            }
                        }
                        canRescale = false;
                        break testLinear;
                    }
                    if (!geo) {
                        // We are going to convert geophysics values to packed one.
                        transform = (MathTransform1D) transform.inverse();
                    }
                    final double offset = transform.transform(0);
                    final double scale  = transform.derivative(Double.NaN);
                    if (Double.isNaN(scale) || Double.isNaN(offset)) {
                        // One category doesn't use a linear transformation. We can't deal with
                        // that with "Rescale" or "Piecewise". Fallback on our "SampleTranscode".
                        canRescale   = false;
                        canPiecewise = false;
                        break testLinear;
                    }
                    // Allocate arrays the first time the loop is run up to this point.
                    // Store scale and offset, and check if they still the same.
                    if (j == 0) {
                        if (i == 0) {
                            scales      = new double[numBands];
                            offsets     = new double[numBands];
                            breakpoints = new float [numBands][][];
                        }
                        sourceBreakpoints = new float[numCategories * 2];
                        targetBreakpoints = new float[numCategories * 2];
                        breakpoints[i] = new float[][] {sourceBreakpoints, targetBreakpoints};
                        offsets    [i] = offset;
                        scales     [i] = scale;
                    }
                    if (offset!=offsets[i] || scale!=scales[i]) {
                        canRescale = false;
                    }
                    // Compute breakpoints.
                    final NumberRange range = category.getRange();
                    final double    minimum = range.getMinimum(true);
                    final double    maximum = range.getMaximum(true);
                    final float   sourceMin = (float) minimum;
                    final float   sourceMax = (float) maximum;
                    final float   targetMin = (float)(minimum * scale + offset);
                    final float   targetMax = (float)(maximum * scale + offset);
                    assert sourceMin <= sourceMax : range;
                    if (Math.abs(minimum - expectedSource) <= EPS) {
                        if (Math.abs(targetMin - expectedTarget) <= EPS) {
                            // This breakpoint is identical to the previous one. Do not
                            // duplicate; overwrites the previous one since this one is
                            // likely to be more accurate.
                            jbp--;
                        } else {
                            // Found a discontinuity!!! The "piecewise" operation is not really
                            // designed for such case. The behavior between the last breakpoint
                            // and the current one may not be what the user expected.
                            assert sourceBreakpoints[jbp-1] < sourceMin : expectedSource;
                            if (CONSERVATIVE_PIECEWISE) {
                                canPiecewise = false;
                            }
                        }
                    } else if (j!=0) {
                        // Found a gap between the last category and the current one. The
                        // "piecewise" operation may not behave as the user expected  for
                        // sample values falling in this gap.
                        assert !(expectedSource > sourceMin) : expectedSource;
                        if (CONSERVATIVE_PIECEWISE) {
                            canPiecewise = false;
                        }
                    }
                    sourceBreakpoints[jbp  ] = sourceMin;
                    sourceBreakpoints[jbp+1] = sourceMax;
                    targetBreakpoints[jbp  ] = targetMin;
                    targetBreakpoints[jbp+1] = targetMax;
                    jbp += 2;
                    expectedSource = range.getMaximum(false);
                    expectedTarget = expectedSource * scale + offset;
                }
                if (false) {
                    // HACK: temporarily disabled because 'javac' 1.4.1_02 produces invalid
                    //       bytecode. This bug is fixed in 'java' 1.4.2-beta. Furthermore,
                    //       the "piecewise" operation throws an ArrayIndexOutOfBoundsException
                    //       in JAI code for an unknow reason...
                    breakpoints[i][0] = sourceBreakpoints = XArray.resize(sourceBreakpoints, jbp);
                    breakpoints[i][1] = targetBreakpoints = XArray.resize(targetBreakpoints, jbp);
                    assert XArray.isSorted(sourceBreakpoints);
                } else {
                    canPiecewise = false;
                }
            }
            if (canRescale && scales!=null) {
                operation = "Rescale";
                param = param.add(scales).add(offsets);
            } else if (canPiecewise && breakpoints!=null) {
                operation = "Piecewise";
                param = param.add(breakpoints);
            }
        } catch (TransformException exception) {
            // At least one category doesn't use a linear relation.
            // Ignore the exception and fallback on the next case.
        }
        /*
         * STEP 5 - Transcode the image sample values. The "SampleTranscode" operation is
         *          registered in the org.geotools.cv package in the SampleDimension class.
         */
        if (operation == null) {
            param = param.add(sampleDimensions);
            operation = "org.geotools.SampleTranscode";
        }
        final Logger logger = Logger.getLogger("org.geotools.gc");
        if (logger.isLoggable(Level.FINE)) {
            // Log a message using the same level (FINE) than GridCoverageProcessor.
            final int        index = operation.lastIndexOf('.');
            final String shortName = (index>=0) ? operation.substring(index+1) : operation;
            final Locale    locale = null;
            final LogRecord record = Resources.getResources(locale).getLogRecord(Level.FINE,
                                     ResourceKeys.SAMPLE_TRANSCODE_$3, new Object[] {
                                     getName(locale), new Integer(geo ? 1 : 0), shortName});
            record.setSourceClassName("GridCoverage");
            record.setSourceMethodName("geophysics");
            logger.log(record);
        }
        return new GridCoverage(getName(null), JAI.create(operation, param, hints),
                                coordinateSystem, getGridGeometry(), null,
                                targetBands, new GridCoverage[]{this}, null);
    }

    /**
     * Apply to the specified grid coverage the same interpolation than this
     * grid coverage. This method is invoked internally by {@link #geophysics}.
     *
     * @param  coverage The coverage for which to apply the interpolation.
     * @return A coverage with the same data than <code>coverage</code> but
     *         the same interpolation than <code>this</code>.
     *
     * @deprecated Override {@link #createGeophysics} instead.
     *             This method will be removed in a future version.
     */
    protected GridCoverage interpolate(final GridCoverage coverage) {
        // This method is overriden by org.geotools.gp.Interpolator
        return coverage;
    }

    /**
     * Construct the {@link PlanarImage} from the {@linkplain SerializableRenderedImage}
     * after deserialization.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        image = PlanarImage.wrapRenderedImage(serializedImage);
    }

    /**
     * Serialize this grid coverage. Before serialization, a {@linkplain SerializableRenderedImage
     * serializable rendered image} is created if it was not already done.
     *
     * @todo Check image's sources in case the planar image is just a wrapper around
     *       an existing {@linkplain SerializableRenderedImage}.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        if (serializedImage == null) {
            serializedImage = new SerializableRenderedImage(image, false, null, "gzip", null, null);
        }
        out.defaultWriteObject();
    }

    /**
     * Returns a string représentation of this coverage. This string is
     * for debugging purpose only and may change in future version.
     */
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringWriter  buffer = new StringWriter();
        buffer.write(super.toString());
        final LineWriter filter = new LineWriter(buffer, lineSeparator+"    ");
        try {
            filter.write(lineSeparator);
            for (int i=0; i<sampleDimensions.length; i++) {
                filter.write(sampleDimensions[i].toString());
            }
            filter.flush();
        } catch (IOException exception) {
            // Should not happen
            throw new AssertionError(exception);
        }
        return buffer.toString();
    }




    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * Interface for OpenGIS's {@link GC_GridCoverage} capable to produce a
     * {@link RenderedImage}. This interface can be used in order to export
     * some {@link GridCoverage} functionalities over the network using RMI
     * (<cite>Remote Method Invocation</cite>).  Socket connection are used
     * for sending the rendered image through the network.
     *
     * @version $Id: GridCoverage.java 10249 2005-01-05 01:38:37Z jmacgill $
     * @author Martin Desruisseaux
     */
    public static interface Remote extends GC_GridCoverage {
        /**
         * Returns the underlying {@link RenderedImage} for this {@link GC_GridCoverage}.
         * This method usually returns an instance of {@link SerializableRenderedImage},
         * which is appropriate for use through a RMI API.
         *
         * @throws RemoteException if the remote connection failed.
         * @throws NotSerializableException if the image is not serializable.
         */
        public abstract RenderedImage getRenderedImage() throws IOException;

        /**
         * Returns the interpolation used for <code>evaluate(...)</code> methods.
         *
         * @return The interpolation.
         * @throws RemoteException if the remote connection failed.
         */
        public abstract Interpolation getInterpolation() throws RemoteException;
    }

    /**
     * Export a Geotools {@link GridCoverage} as an OpenGIS {@link GC_GridCoverage}
     * object. This class is suitable for RMI use. User should not create instance
     * of this class directly. The method {@link Adapters#export(GridCoverage)} should
     * be used instead.
     *
     * @version $Id: GridCoverage.java 10249 2005-01-05 01:38:37Z jmacgill $
     * @author Martin Desruisseaux
     */
    protected class Export extends Coverage.Export implements GC_GridCoverage, Remote {
        /**
         * The serialized {@link RenderedImage}, or <code>null</code> if this image
         * is not yet serialized.
         */
        private SerializableRenderedImage serialized;

        /**
         * Constructs a remote object.  This object is automatically added to the cache
         * in the enclosing {@link GridCoverage} object. The cached <code>Export</code>
         * instance can be queried with {@link Adapters#export(GridCoverage)}.
         *
         * @param  adapters The originating adapter.
         * @throws RemoteException if this object can't be exported through RMI.
         */
        protected Export(final Adapters adapters) throws RemoteException {
            super(adapters);
        }

        /**
         * Returns the number of grid coverages which the grid coverage was derived from.
         * The default implementation invokes {@link GridCoverage#getSources}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int getNumSources() throws RemoteException {
            return GridCoverage.this.getSources().length;
        }

        /**
         * Returns the source data for a grid coverage.
         * The default implementation invokes {@link GridCoverage#getSources}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridCoverage getSource(int sourceDataIndex) throws RemoteException {
            return ((Adapters) adapters).export(GridCoverage.this.getSources()[sourceDataIndex]);
        }

        /**
         * Returns <code>true</code> if grid data can be edited.
         * The default implementation invokes {@link GridCoverage#isDataEditable}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public boolean isDataEditable() throws RemoteException {
            return GridCoverage.this.isDataEditable();
        }

        /**
         * Returns information for the packing of grid coverage values.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridPacking getGridPacking() throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Returns information for the grid coverage geometry.
         * The default implementation invokes {@link GridCoverage#getGridGeometry}.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridGeometry getGridGeometry() throws RemoteException {
            return ((Adapters) adapters).export(GridCoverage.this.getGridGeometry());
        }

        /**
         * Return the grid geometry for an overview.
         * The default implementation throws an {@link ArrayIndexOutOfBoundsException},
         * since {@link #getNumOverviews} returned 0.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridGeometry getOverviewGridGeometry(int overviewIndex) throws RemoteException {
            throw new ArrayIndexOutOfBoundsException(overviewIndex);
        }

        /**
         * Returns the number of predetermined overviews for the grid.
         * The default implementation returns 0, since this feature is not yet
         * implemented. It may be implemented in a future version.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int getNumOverviews() throws RemoteException {
            return 0;
        }

        /**
         * Returns a pre-calculated overview for a grid coverage.
         * The default implementation throws an {@link ArrayIndexOutOfBoundsException},
         * since {@link #getNumOverviews} returned 0.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public GC_GridCoverage getOverview(int overviewIndex) throws RemoteException {
            throw new ArrayIndexOutOfBoundsException(overviewIndex);
        }

        /**
         * Returns the optimal size to use for each dimension when accessing grid values.
         * The default implementation returns the image's tiles size.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int[] getOptimalDataBlockSizes() throws RemoteException {
            final int[] size = new int[getDimension()];
            switch (size.length) {
                default: Arrays.fill(size, 1);            // Fall through
                case 2:  size[1] = image.getTileHeight(); // Fall through
                case 1:  size[0] = image.getTileWidth();  // Fall through
                case 0:  break;
            }
            return size;
        }

        /**
         * Return a sequence of boolean values for a block.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public boolean[] getDataBlockAsBoolean(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Return a sequence of byte values for a block.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public byte[] getDataBlockAsByte(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Return a sequence of int values for a block.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public int[] getDataBlockAsInteger(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Return a sequence of double values for a block.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public double[] getValueBlockAsDouble(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Return a block of grid coverage data for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public byte[] getPackedDataBlock(GC_GridRange gridRange) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of boolean values for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setDataBlockAsBoolean(GC_GridRange gridRange, boolean[] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of byte values for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setDataBlockAsByte(GC_GridRange gridRange, byte[] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of bint values for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setDataBlockAsInteger(GC_GridRange gridRange, int[] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of double values for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setDataBlockAsDouble(GC_GridRange gridRange, double[] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Set a block of grid coverage data for all sample dimensions.
         *
         * @throws RemoteException if a remote call failed. More specifically, the exception will
         *         be an instance of {@link ServerException} if an error occurs on the server side.
         */
        public void setPackedDataBlock(GC_GridRange gridRange, byte [] values) throws RemoteException {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        /**
         * Returns the underlying {@link RenderedImage} for this {@link GC_GridCoverage}.
         * This method usually returns an instance of {@link SerializableRenderedImage},
         * which is appropriate for use through a RMI API.
         *
         * @throws RemoteException if the remote connection failed.
         * @throws NotSerializableException if the image is not serializable.
         */
        public synchronized RenderedImage getRenderedImage() throws IOException {
            if (serialized == null) {
                serialized = new SerializableRenderedImage(GridCoverage.this.getRenderedImage(),
                                                           false, null, "gzip", null, null);
            }
            return serialized;
        }

        /**
         * Returns the interpolation used for <code>evaluate(...)</code> methods.
         *
         * @return The interpolation.
         * @throws RemoteException if the remote connection failed.
         */
        public Interpolation getInterpolation() throws RemoteException {
            return GridCoverage.this.getInterpolation();
        }
    }
}
