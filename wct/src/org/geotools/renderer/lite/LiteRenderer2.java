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
package org.geotools.renderer.lite;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gc.GridCoverage;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.Renderer;
import org.geotools.renderer.Renderer2D;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleAttributeExtractor;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbol;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextMark;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;


/**
 * A lite implementation of the Renderer and Renderer2D interfaces. Lite means
 * that:
 * 
 * <ul>
 * <li>
 * The code is relatively simple to understand, so it can be used as a simple
 * example of an SLD compliant rendering code
 * </li>
 * <li>
 * Uses as few memory as possible
 * </li>
 * </ul>
 * 
 * Use this class if you need a stateless renderer that provides low memory
 * footprint and  decent rendering performance on the first call but don't
 * need good optimal performance on subsequent calls on the same data. Notice:
 * for the time being, this class doesn't support GridCoverage stylers, that
 * will be rendered using the non geophisics version of the GridCoverage, if
 * available, with the geophisics one, otherwise.
 *
 * @author James Macgill
 * @author Andrea Aime
 * @version $Id$
 */
public class LiteRenderer2 implements Renderer, Renderer2D {
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.rendering");

    /** where the centre of an untransormed mark is */
    private static Point markCentrePoint;

    /** Set containing the font families known of this machine */
    private static Set fontFamilies = null;

    /** Fonts already loaded */
    private static Map loadedFonts = new HashMap();

    /** Observer for image loading */
    private static Canvas obs = new Canvas();

    /** Tolerance used to compare doubles for equality */
    private static final double TOLERANCE = 1e-6;

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final Map joinLookup = new HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final Map capLookup = new HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final Map fontStyleLookup = new HashMap();

    /** Holds a list of well-known marks. */
    private static Set wellKnownMarks = new HashSet();

    /** Set of graphic formats supported for image loading */
    private static Set supportedGraphicFormats;

    /** The image loader */
    private static ImageLoader imageLoader = new ImageLoader();

    /** DOCUMENT ME! */
    private static final Composite DEFAULT_COMPOSITE = AlphaComposite
        .getInstance(AlphaComposite.SRC_OVER, 1.0f);

    /** DOCUMENT ME! */
    private static final java.awt.Stroke DEFAULT_STROKE = new BasicStroke();

    /** DOCUMENT ME! */
    private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    static { //static block to populate the lookups
        joinLookup.put("miter", new Integer(BasicStroke.JOIN_MITER));
        joinLookup.put("bevel", new Integer(BasicStroke.JOIN_BEVEL));
        joinLookup.put("round", new Integer(BasicStroke.JOIN_ROUND));

        capLookup.put("butt", new Integer(BasicStroke.CAP_BUTT));
        capLookup.put("round", new Integer(BasicStroke.CAP_ROUND));
        capLookup.put("square", new Integer(BasicStroke.CAP_SQUARE));

        fontStyleLookup.put("normal", new Integer(java.awt.Font.PLAIN));
        fontStyleLookup.put("italic", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("oblique", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("bold", new Integer(java.awt.Font.BOLD));

        /**
         * A list of wellknownshapes that we know about: square, circle,
         * triangle, star, cross, x. Note arrow is an implementation specific
         * mark.
         */
        wellKnownMarks.add("Square");
        wellKnownMarks.add("Triangle");
        wellKnownMarks.add("Cross");
        wellKnownMarks.add("Circle");
        wellKnownMarks.add("Star");
        wellKnownMarks.add("X");
        wellKnownMarks.add("Arrow");
        wellKnownMarks.add("square");
        wellKnownMarks.add("triangle");
        wellKnownMarks.add("cross");
        wellKnownMarks.add("circle");
        wellKnownMarks.add("star");
        wellKnownMarks.add("x");
        wellKnownMarks.add("arrow");

        Coordinate c = new Coordinate(100, 100);
        GeometryFactory fac = new GeometryFactory();
        markCentrePoint = fac.createPoint(c);
    }

    /** Filter factory for creating bounding box filters */
    private FilterFactory filterFactory = FilterFactory.createFilterFactory();

    /**
     * Context which contains the layers and the bouning box which needs to be
     * rendered.
     */
    private MapContext context;

    /**
     * Flag which determines if the renderer is interactive or not. An
     * interactive renderer will return rather than waiting for time consuming
     * operations to complete (e.g. Image Loading). A non-interactive renderer
     * (e.g. a SVG or PDF renderer) will block for these operations.
     */
    private boolean interactive = true;

    /**
     * Flag which controls behaviour for applying affine transformation to the
     * graphics object.  If true then the transform will be concatenated to
     * the existing transform.  If false it will be replaced.
     */
    private boolean concatTransforms = false;

    /** Geographic map extent */
    private Envelope mapExtent = null;

    /** Graphics object to be rendered to. Controlled by set output. */
    private Graphics2D graphics;

    /** The size of the output area in output units. */
    private Rectangle screenSize;

    /**
     * Activates bbox and attribute filtering optimization, that works properly
     * only if the input feature sources really contain just one feature type.
     * This may not be the case if the feature source is based on a generic
     * feature collection
     */
    private boolean optimizedDataLoadingEnabled;

    /**
     * This flag is set to false when starting rendering, and will be checked
     * during the rendering loop in order to make it stop forcefully
     */
    private boolean renderingStopRequested;

    /**
     * The ratio required to scale the features to be rendered so that they fit
     * into the output space.
     */
    private double scaleDenominator;

    /** Maximun displacement for generalization during rendering */
    private double generalizationDistance = 1.0;

    /**
     * Creates a new instance of LiteRenderer without a context. Use it only to
     * gain access to utility methods of this class TODO: it's probably better
     * to factor out those methods in an utility class
     */
    public LiteRenderer2() {
        LOGGER.fine("creating new lite renderer");
    }

    /**
     * Creates a new instance of Java2DRenderer.
     *
     * @param context Contains pointers to layers, bounding box, and style
     *        required for rendering.
     */
    public LiteRenderer2(MapContext context) {
        this.context = context;
    }

    /**
     * Returns the set of supported graphics formats and lazy loads it as
     * needed
     *
     * @return
     */
    private static Set getSupportedGraphicFormats() {
        if (supportedGraphicFormats == null) {
            supportedGraphicFormats = new java.util.HashSet();

            String[] types = ImageIO.getReaderMIMETypes();

            for (int i = 0; i < types.length; i++) {
                supportedGraphicFormats.add(types[i]);
            }
        }

        return supportedGraphicFormats;
    }

    /**
     * Sets the flag which controls behaviour for applying affine
     * transformation to the graphics object.
     *
     * @param flag If true then the transform will be concatenated to the
     *        existing transform.  If false it will be replaced.
     */
    public void setConcatTransforms(boolean flag) {
        concatTransforms = flag;
    }

    /**
     * Returns the amount of time the renderer waits for loading an external
     * image before giving up and examining the other images in the Graphic
     * object
     *
     * @return the timeout in milliseconds
     */
    public static long getImageLoadingTimeout() {
        return ImageLoader.getTimeout();
    }

    /**
     * Sets the maximum time to wait for getting an external image. Set it to
     * -1 to wait undefinitely. The default value is 10 seconds
     *
     * @param newTimeout the new timeout value in milliseconds
     */
    public static void setImageLoadingTimeout(long newTimeout) {
        ImageLoader.setTimeout(newTimeout);
    }

    /**
     * Flag which controls behaviour for applying affine transformation to the
     * graphics object.
     *
     * @return a boolean flag. If true then the transform will be concatenated
     *         to the existing transform.  If false it will be replaced.
     */
    public boolean getConcatTransforms() {
        return concatTransforms;
    }

    /**
     * Called before {@link render}, this sets where any output will be sent.
     *
     * @param g A graphics object for future rendering to be sent to.  Note:
     *        must be an instance of lite renderer.
     * @param bounds The size of the output area, required so that scale can be
     *        calculated.
     *
     * @deprecated Graphics and bounds is to be set in renderer().
     */
    public void setOutput(Graphics g, Rectangle bounds) {
        graphics = (Graphics2D) g;
        screenSize = bounds;
    }

    /**
     * Setter for property scaleDenominator.
     *
     * @param scaleDenominator New value of property scaleDenominator.
     */
    protected void setScaleDenominator(double scaleDenominator) {
        this.scaleDenominator = scaleDenominator;
    }

    /**
     * If you call this method from another thread than the one that called
     * <code>paint</code> or <code>render</code> the rendering will be
     * forcefully stopped before termination
     */
    public void stopRendering() {
        renderingStopRequested = true;
    }

    /**
     * Render features based on the LayerList, BoundBox and Style specified in
     * this.context. Don't mix calls to paint and setOutput, when calling this
     * method the graphics set in the setOutput method is discarded.
     *
     * @param graphics The graphics object to draw to.
     * @param paintArea The size of the output area in output units (eg:
     *        pixels).
     * @param transform A transform which converts World coordinates to Screen
     *        coordinates.
     *
     * @task Need to check if the Layer CoordinateSystem is different to the
     *       BoundingBox rendering CoordinateSystem and if so, then transform
     *       the coordinates.
     */
    public void paint(Graphics2D graphics, Rectangle paintArea,
        AffineTransform transform) {
        if ((graphics == null) || (paintArea == null)) {
            LOGGER.info("renderer passed null arguments");

            return;
        }

        // reset the abort flag
        renderingStopRequested = false;

        AffineTransform at = transform;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Affine Transform is " + at);
        }

        /*
         * If we are rendering to a component which has already set up some form
         * of transformation then we can concatenate our transformation to it.
         * An example of this is the ZoomPane component of the swinggui module.
         */
        if (concatTransforms) {
            AffineTransform atg = graphics.getTransform();
            atg.concatenate(at);
            graphics.setTransform(atg);
        } else {
            graphics.setTransform(at);
        }

        setScaleDenominator(1 / graphics.getTransform().getScaleX());

        try {
            // set the passed graphic as the current graphic but
            // be sure to release it before we end up with this request
            this.graphics = graphics;

            MapLayer[] layers = context.getLayers();

            // Consider the geometries, they should lay inside or
            // overlap with the bbox indicated by the painting area.
            // First, create the bbox in real world coordinates
            AffineTransform pixelToWorld = null;

            try {
                pixelToWorld = transform.createInverse();
            } catch (NoninvertibleTransformException e) {
                LOGGER.warning("Can't create pixel to world transform: "
                    + e.getMessage());
                e.printStackTrace();
            }

            Point2D p1 = new Point2D.Double();
            Point2D p2 = new Point2D.Double();
            pixelToWorld.transform(new Point2D.Double(paintArea.getMinX(),
                    paintArea.getMinY()), p1);
            pixelToWorld.transform(new Point2D.Double(paintArea.getMaxX(),
                    paintArea.getMaxY()), p2);

            double x1 = p1.getX();
            double y1 = p1.getY();
            double x2 = p2.getX();
            double y2 = p2.getY();
            Envelope envelope = new Envelope(Math.min(x1, x2),
                    Math.max(x1, x2), Math.min(y1, y2), Math.max(y1, y2));

            for (int i = 0; i < layers.length; i++) {
                MapLayer currLayer = layers[i];

                if (!currLayer.isVisible()) {
                    // Only render layer when layer is visible
                    continue;
                }

                if (renderingStopRequested) {
                    return;
                }

                try {
                    // mapExtent = this.context.getAreaOfInterest();
                    FeatureResults results = queryLayer(currLayer, envelope);

                    // extract the feature type stylers from the style object
                    // and process them
                    processStylers(results,
                        currLayer.getStyle().getFeatureTypeStyles());
                } catch (Exception exception) {
                    LOGGER.warning("Exception " + exception
                        + " rendering layer " + currLayer);
                    exception.printStackTrace();
                }
            }
        } finally {
            this.graphics = null;
        }
    }

    /**
     * Queries a given layer's features to be rendered based on the target
     * rendering bounding box.
     * 
     * <p>
     * If <code>optimizedDataLoadingEnabled</code> attribute has been set to
     * <code>true</code>, the following optimization will be performed in
     * order to limit the number of features returned:
     * 
     * <ul>
     * <li>
     * Just the features whose geometric attributes lies within
     * <code>envelope</code> will be queried
     * </li>
     * <li>
     * The queried attributes will be limited to just those needed to perform
     * the rendering, based on the requiered geometric and non geometric
     * attributes found in the Layer's style rules
     * </li>
     * <li>
     * If a <code>Query</code> has been set to limit the resulting layer's
     * features, the final filter to obtain them will respect it. This means
     * that the bounding box filter and the Query filter will be combined,
     * also including maxFeatures from Query
     * </li>
     * <li>
     * At least that the layer's definition query explicitly says to retrieve
     * some attribute, no attributes will be requested from it, for
     * performance reassons. So it is desirable to not use a Query for
     * filtering a layer wich includes attributes. Note that including the
     * attributes in the result is not necessary for the query's filter to get
     * properly processed.
     * </li>
     * </ul>
     * </p>
     * 
     * <p>
     * <b>NOTE</b>: This is an internal method and should only be called by
     * <code>paint(Graphics2D, Rectangle, AffineTransform)</code>. It is
     * package protected just to allow unit testing it.
     * </p>
     *
     * @param currLayer the actually processing layer for renderition
     * @param envelope the spatial extent wich is the target area fo the
     *        rendering process
     *
     * @return the set of features resulting from <code>currLayer</code> after
     *         quering its feature source
     *
     * @throws IllegalFilterException if something goes wrong constructing the
     *         bbox filter
     * @throws IOException if something goes wrong consulting the map layer's
     *         feature source or the mixing the bbox and the layer's
     *         definition query
     *
     * @see MapLayer#setQuery(org.geotools.data.Query)
     */
    FeatureResults queryLayer(MapLayer currLayer, Envelope envelope)
        throws IllegalFilterException, IOException {
        FeatureResults results = null;
        FeatureSource featureSource = currLayer.getFeatureSource();
        FeatureType schema = featureSource.getSchema();
        Query query = Query.ALL;

        if (optimizedDataLoadingEnabled) {
            // see what attributes we really need by exploring the styles
            String[] attributes = findStyleAttributes(currLayer, schema);

            // Then create the geometry filters. We have to create one for each
            // geometric
            // attribute used during the rendering as the feature may have more
            // than one
            // and the styles could use non default geometric ones
            BBoxExpression rightBBox = filterFactory.createBBoxExpression(envelope);
            Filter filter = createBBoxFilters(schema, attributes, rightBBox);

            // now build the query using only the attributes and the bounding
            // box needed
            query = new DefaultQuery(schema.getTypeName(), filter,
                    Integer.MAX_VALUE, attributes, "");
        }

        //now, if a definition query has been established for this layer, be
        // sure to respect it by combining it with the bounding box one.
        Query definitionQuery = currLayer.getQuery();

        if (definitionQuery != Query.ALL) {
            if (query == Query.ALL) {
                query = definitionQuery;
            } else {
                query = DataUtilities.mixQueries(definitionQuery, query,
                        "liteRenderer");
            }
        }

        results = featureSource.getFeatures(query);

        return results;
    }

    /**
     * Inspects the <code>MapLayer</code>'s style and retrieves it's needed
     * attribute names, returning at least the default geometry attribute
     * name.
     *
     * @param layer the <code>MapLayer</code> to determine the needed
     *        attributes from
     * @param schema the <code>layer</code>'s featuresource schema
     *
     * @return the minimun set of attribute names needed to render
     *         <code>layer</code>
     */
    private String[] findStyleAttributes(MapLayer layer, FeatureType schema) {
        StyleAttributeExtractor sae = new StyleAttributeExtractor();
        sae.visit(layer.getStyle());

        String[] ftsAttributes = sae.getAttributeNames();

        /*GR: if as result of sae.getAttributeNames() ftsAttributes already
         * contains geometry attribue names, they gets duplicated, wich
         * produces an error in AbstracDatastore when trying to create
         * a derivate FeatureType. So I'll add the default geometry only
         * if it is not already present, but: should all the geometric
         * attributes be added by default? I will add them, but don't really
         * know what's the expected behavior
         */
        List atts = new LinkedList(Arrays.asList(ftsAttributes));
        AttributeType[] attTypes = schema.getAttributeTypes();
        String attName;

        for (int i = 0; i < attTypes.length; i++) {
            attName = attTypes[i].getName();

            if (attTypes[i].isGeometry() && !atts.contains(attName)) {
                atts.add(attName);
                LOGGER.fine("added attribute " + attName);
            }
        }

        ftsAttributes = new String[atts.size()];
        atts.toArray(ftsAttributes);

        return ftsAttributes;
    }

    /**
     * Creates the bounding box filters (one for each geometric attribute)
     * needed to query a <code>MapLayer</code>'s feature source to return just
     * the features for the target rendering extent
     *
     * @param schema the layer's feature source schema
     * @param attributes set of needed attributes
     * @param bbox the expression holding the target rendering bounding box
     *
     * @return an or'ed list of bbox filters, one for each geometric attribute
     *         in <code>attributes</code>. If there are just one geometric
     *         attribute, just returns its corresponding
     *         <code>GeometryFilter</code>.
     *
     * @throws IllegalFilterException if something goes wrong creating the
     *         filter
     */
    private Filter createBBoxFilters(FeatureType schema, String[] attributes,
        BBoxExpression bbox) throws IllegalFilterException {
        Filter filter = null;

        for (int j = 0; j < attributes.length; j++) {
            AttributeType attType = schema.getAttributeType(attributes[j]);

            if (attType.isGeometry()) {
                GeometryFilter gfilter = filterFactory.createGeometryFilter(Filter.GEOMETRY_BBOX);

                // TODO: how do I get the full xpath of an attribute should
                // feature composition be used?
                Expression left = filterFactory.createAttributeExpression(schema,
                        attType.getName());
                gfilter.addLeftGeometry(left);
                gfilter.addRightGeometry(bbox);

                if (filter == null) {
                    filter = gfilter;
                } else {
                    filter = filter.or(gfilter);
                }
            }
        }

        return filter;
    }

    /**
     * Performs the actual rendering process to the graphics context set in
     * setOutput.
     * 
     * <p>
     * The style parameter controls the appearance features.  Rules within the
     * style object may cause some features to be rendered multiple times or
     * not at all.
     * </p>
     *
     * @param features the feature collection to be rendered
     * @param map Controls the full extent of the input space.  Used in the
     *        calculation of scale.
     * @param s A style object.  Contains a set of FeatureTypeStylers that are
     *        to be applied in order to control the rendering process.
     */
    public void render(FeatureCollection features, Envelope map, Style s) {
        if (graphics == null) {
            LOGGER.info("renderer passed null graphics");

            return;
        }

        // reset the abort flag
        renderingStopRequested = false;

        long startTime = 0;

        if (LOGGER.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
        }

        mapExtent = map;

        //set up the affine transform and calculate scale values
        AffineTransform at = worldToScreenTransform(mapExtent, screenSize);

        /* If we are rendering to a component which has already set up some form
         * of transformation then we can concatenate our transformation to it.
         * An example of this is the ZoomPane component of the swinggui module.*/
        if (concatTransforms) {
            graphics.getTransform().concatenate(at);
        } else {
            graphics.setTransform(at);
        }

        scaleDenominator = 1 / graphics.getTransform().getScaleX();

        //extract the feature type stylers from the style object and process them
        FeatureTypeStyle[] featureStylers = s.getFeatureTypeStyles();

        try {
            processStylers(DataUtilities.results(features), featureStylers);
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "I/O error while rendering the layer", ioe);
        } catch (IllegalAttributeException iae) {
            LOGGER.log(Level.SEVERE,
                "Illegal attribute exception while rendering the layer", iae);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            long endTime = System.currentTimeMillis();
            double elapsed = (endTime - startTime) / 1000.0;
        }
    }

    /**
     * Sets up the affine transform
     *
     * @param mapExtent the map extent
     * @param screenSize the screen size
     *
     * @return a transform that maps from real world coordinates to the screen
     */
    public AffineTransform worldToScreenTransform(Envelope mapExtent,
        Rectangle screenSize) {
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();

        double tx = -mapExtent.getMinX() * scaleX;
        double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight();

        AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY,
                tx, ty);

        return at;
    }

    /**
     * Converts a coordinate expressed on the device space back to real world
     * coordinates
     *
     * @param x horizontal coordinate on device space
     * @param y vertical coordinate on device space
     * @param map The map extent
     *
     * @return The correspondent real world coordinate
     */
    public Coordinate pixelToWorld(int x, int y, Envelope map) {
        if (graphics == null) {
            LOGGER.info("no graphics yet deffined");

            return null;
        }

        //set up the affine transform and calculate scale values
        AffineTransform at = worldToScreenTransform(map, screenSize);

        /* If we are rendering to a component which has already set up some form
         * of transformation then we can concatenate our transformation to it.
         * An example of this is the ZoomPane component of the swinggui module.*/
        if (concatTransforms) {
            graphics.getTransform().concatenate(at);
        } else {
            graphics.setTransform(at);
        }

        try {
            Point2D result = at.inverseTransform(new java.awt.geom.Point2D.Double(
                        x, y), new java.awt.geom.Point2D.Double());
            Coordinate c = new Coordinate(result.getX(), result.getY());

            return c;
        } catch (Exception e) {
            LOGGER.warning(e.toString());
        }

        return null;
    }

    /**
     * Checks if a rule can be triggered at the current scale level
     *
     * @param r The rule
     *
     * @return true if the scale is compatible with the rule settings
     */
    private boolean isWithInScale(Rule r) {
        return ((r.getMinScaleDenominator() - TOLERANCE) <= scaleDenominator)
        && ((r.getMaxScaleDenominator() + TOLERANCE) > scaleDenominator);
    }

    /**
     * Applies each feature type styler in turn to all of the features. This
     * perhaps needs some explanation to make it absolutely clear.
     * featureStylers[0] is applied to all features before featureStylers[1]
     * is applied.  This can have important consequences as regards the
     * painting order.
     * 
     * <p>
     * In most cases, this is the desired effect.  For example, all line
     * features may be rendered with a fat line and then a thin line.  This
     * produces a 'cased' effect without any strange overlaps.
     * </p>
     * 
     * <p>
     * This method is internal and should only be called by render.
     * </p>
     * 
     * <p></p>
     *
     * @param features An array of features to be rendered
     * @param featureStylers An array of feature stylers to be applied
     *
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    private void processStylers(final FeatureResults features,
        final FeatureTypeStyle[] featureStylers)
        throws IOException, IllegalAttributeException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("processing " + featureStylers.length + " stylers");
        }

        for (int i = 0; i < featureStylers.length; i++) {
            FeatureTypeStyle fts = featureStylers[i];

            // get applicable rules at the current scale
            Rule[] rules = fts.getRules();
            List ruleList = new ArrayList();
            List elseRuleList = new ArrayList();

            for (int j = 0; j < rules.length; j++) {
                Rule r = rules[j];

                if (isWithInScale(r)) {
                    if (r.hasElseFilter()) {
                        elseRuleList.add(r);
                    } else {
                        ruleList.add(r);
                    }
                }
            }

            // process the features according to the rules
            FeatureReader reader = features.reader();

            while (reader.hasNext()) {
                boolean doElse = true;
                Feature feature = reader.next();

                if (renderingStopRequested) {
                    return;
                }

                String typeName = feature.getFeatureType().getTypeName();

                if ((typeName != null)
                        && (feature.getFeatureType().isDescendedFrom(null,
                            fts.getFeatureTypeName())
                        || typeName.equalsIgnoreCase(fts.getFeatureTypeName()))) {
                    // applicable rules
                    for (Iterator it = ruleList.iterator(); it.hasNext();) {
                        Rule r = (Rule) it.next();

                        // if this rule applies
                        if (isWithInScale(r) && !r.hasElseFilter()) {
                            Filter filter = r.getFilter();

                            if ((filter == null) || filter.contains(feature)) {
                                doElse = false;

                                Symbolizer[] symbolizers = r.getSymbolizers();
                                processSymbolizers(feature, symbolizers);
                            }
                        }
                    }

                    if (doElse) {
                        // rules with an else filter
                        for (Iterator it = elseRuleList.iterator();
                                it.hasNext();) {
                            Rule r = (Rule) it.next();
                            Symbolizer[] symbolizers = r.getSymbolizers();
                            processSymbolizers(feature, symbolizers);
                        }
                    }
                }
            }
        reader.close();
        }
    }

    /**
     * Applies each of a set of symbolizers in turn to a given feature.
     * 
     * <p>
     * This is an internal method and should only be called by processStylers.
     * </p>
     *
     * @param feature The feature to be rendered
     * @param symbolizers An array of symbolizers which actually perform the
     *        rendering.
     */
    private void processSymbolizers(final Feature feature,
        final Symbolizer[] symbolizers) {
        for (int m = 0; m < symbolizers.length; m++) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("applying symbolizer " + symbolizers[m]);
            }

            resetFill(graphics);
            resetStroke(graphics);

            if (symbolizers[m] instanceof PolygonSymbolizer) {
                renderPolygon(feature, (PolygonSymbolizer) symbolizers[m]);
            } else if (symbolizers[m] instanceof LineSymbolizer) {
                renderLine(feature, (LineSymbolizer) symbolizers[m]);
            } else if (symbolizers[m] instanceof PointSymbolizer) {
                renderPoint(feature, (PointSymbolizer) symbolizers[m]);
            } else if (symbolizers[m] instanceof TextSymbolizer) {
                renderText(feature, (TextSymbolizer) symbolizers[m]);
            } else if (symbolizers[m] instanceof RasterSymbolizer) {
                renderRaster(feature, (RasterSymbolizer) symbolizers[m]);
            }
        }
    }

    /**
     * Renders the given feature as a polygon using the specified symbolizer.
     * Geometry types other than inherently area types can be used. If a line
     * is used then the line string is closed for filling (only) by connecting
     * its end point to its start point. This is an internal method that
     * should only be called by processSymbolizers.
     *
     * @param feature The feature to render
     * @param symbolizer The polygon symbolizer to apply
     *
     * @todo REVISIT: No attempt is made to render 0 dimension (point)
     *       geometries
     */
    private void renderPolygon(Feature feature, PolygonSymbolizer symbolizer) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("rendering polygon with a scale of "
                + this.scaleDenominator);
        }

        Fill fill = symbolizer.getFill();
        String geomName = symbolizer.getGeometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            return;
        }

        if (geom.getDimension() < 1) {
            return;
        }

        Shape path = createPath(geom);

        if (fill != null) {
            applyFill(graphics, fill, feature);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("paint in renderPoly: " + graphics.getPaint());
            }

            graphics.fill(path);

            // shouldn't we reset the graphics when we return finished?
            resetFill(graphics);
        }

        if (symbolizer.getStroke() != null) {
            Stroke stroke = symbolizer.getStroke();
            applyStroke(graphics, stroke, feature);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("path is "
                    + graphics.getTransform().createTransformedShape(path)
                              .getBounds2D().toString());
            }

            if (stroke.getGraphicStroke() == null) {
                graphics.draw(path);
            } else {
                // set up the graphic stroke
                drawWithGraphicStroke(graphics, path,
                    stroke.getGraphicStroke(), feature);
            }
        }
    }

    /**
     * Renders the given feature as a line using the specified symbolizer. This
     * is an internal method that should only be called by processSymbolizers
     * Geometry types other than inherently linear types can be used. If a
     * point geometry is used, it should be interpreted as a line of zero
     * length and two end caps.  If a polygon is used (or other "area" type)
     * then its closed outline will be used as the line string (with no end
     * caps).
     *
     * @param feature The feature to render
     * @param symbolizer The polygon symbolizer to apply
     */
    private void renderLine(Feature feature, LineSymbolizer symbolizer) {
        if (symbolizer.getStroke() == null) {
            return;
        }

        Stroke stroke = symbolizer.getStroke();
        applyStroke(graphics, stroke, feature);

        String geomName = symbolizer.getGeometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);

        if ((geom == null) || geom.isEmpty()) {
            return;
        }

        Shape path = createPath(geom);

        if (stroke.getGraphicStroke() == null) {
            graphics.draw(path);
        } else {
            // set up the graphic stroke
            drawWithGraphicStroke(graphics, path, stroke.getGraphicStroke(),
                feature);
        }
    }

    /**
     * Renders a point on the screen according to the symbolizer
     *
     * @param feature containing the point geometry
     * @param symbolizer the style specification
     */
    private void renderPoint(Feature feature, PointSymbolizer symbolizer) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("rendering a point from " + feature);
        }

        Graphic sldgraphic = symbolizer.getGraphic();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("sldgraphic = " + sldgraphic);
        }

        String geomName = symbolizer.getGeometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("empty geometry");
            }

            return;
        }

        // TODO: consider if mark and externalgraphic should share an ancestor?

        /*
           if (null != (Object)sldgraphic.getExternalGraphics()){
               LOGGER.finer("rendering External graphic");
               renderExternalGraphic(geom, sldgraphic, feature);
           } else{
               LOGGER.finer("rendering mark");
               renderMark(geom, sldgraphic, feature);
           }
         */
        Symbol[] symbols = sldgraphic.getSymbols();
        boolean flag = false;

        for (int i = 0; i < symbols.length; i++) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("trying to render symbol " + i);
            }

            if (symbols[i] instanceof ExternalGraphic) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering External graphic");
                }

                flag = renderExternalGraphic(geom, sldgraphic,
                        (ExternalGraphic) symbols[i], feature);

                if (flag) {
                    return;
                }
            }

            if (symbols[i] instanceof Mark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering mark @ PointRenderer "
                        + symbols[i].toString());
                }

                flag = renderMark(geom, sldgraphic, feature, (Mark) symbols[i]);

                if (flag) {
                    return;
                }
            }

            if (symbols[i] instanceof TextMark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering text symbol");
                }

                flag = renderTextSymbol(geom, sldgraphic, feature,
                        (TextMark) symbols[i]);

                if (flag) {
                    return;
                }
            }
        }
    }

    /**
     * Renders a text along the feature
     *
     * @param feature The feature to be painted
     * @param symbolizer The style used to paint the feature
     */
    private void renderText(Feature feature, TextSymbolizer symbolizer) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rendering text");
        }

        String geomName = symbolizer.getGeometryPropertyName();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("geomName = " + geomName);
        }

        Geometry geom = findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("empty geometry");
            }

            return;
        }

        Object obj = symbolizer.getLabel().getValue(feature);

        if (obj == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Null label in render text");
            }

            return;
        }

        String label = obj.toString();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("label is " + label);
        }

        if (label == null) {
            return;
        }

        Font[] fonts = symbolizer.getFonts();
        java.awt.Font javaFont = getFont(feature, fonts);

        // fall back on a default font to avoid null pointer exception
        if (javaFont == null) {
            javaFont = graphics.getFont();
        }

        LabelPlacement placement = symbolizer.getLabelPlacement();

        // TextLayout tl = new TextLayout(label, graphics.getFont(), graphics.getFontRenderContext());
        AffineTransform oldTx = graphics.getTransform();
        graphics.setTransform(IDENTITY_TRANSFORM);

        GlyphVector gv = javaFont.createGlyphVector(graphics
                .getFontRenderContext(), label);
        graphics.setTransform(oldTx);

        Rectangle2D textBounds = gv.getVisualBounds();
        double x = 0;
        double y = 0;
        double rotation = 0;
        double tx = 0;
        double ty = 0;

        if (placement instanceof PointPlacement) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting pointPlacement");
            }

            // if it's a point, get its coordinate, otherwise compute the
            // centroid
            if (geom instanceof Point) {
                tx = ((Point) geom).getX();
                ty = ((Point) geom).getY();
            } else {
                Point centroid = geom.getCentroid();
                tx = centroid.getX();
                ty = centroid.getY();
            }

            PointPlacement p = (PointPlacement) placement;
            x = ((Number) p.getAnchorPoint().getAnchorPointX().getValue(feature))
                .doubleValue() * -textBounds.getWidth();
            y = ((Number) p.getAnchorPoint().getAnchorPointY().getValue(feature))
                .doubleValue() * textBounds.getHeight();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("anchor point (" + x + "," + y + ")");
            }

            x += ((Number) p.getDisplacement().getDisplacementX().getValue(feature))
            .doubleValue();
            y += ((Number) p.getDisplacement().getDisplacementY().getValue(feature))
            .doubleValue();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("total displacement (" + x + "," + y + ")");
            }

            if ((p.getRotation() == null)
                    || (p.getRotation().getValue(feature) == null)) {
                rotation = 0;
            } else {
                rotation = ((Number) p.getRotation().getValue(feature))
                    .doubleValue();
            }

            rotation *= (Math.PI / 180.0);
        } else if (placement instanceof LinePlacement
                && geom instanceof LineString) {
            // @TODO: if the geometry is a ring or a polygon try to find out
            // some "axis" to follow in the label placement
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting line placement");
            }

            LinePlacement lp = (LinePlacement) placement;
            double offset = 0;

            if ((lp.getPerpendicularOffset() != null)
                    && (lp.getPerpendicularOffset().getValue(feature) != null)) {
                offset = ((Number) lp.getPerpendicularOffset().getValue(feature))
                    .doubleValue();
            }

            LineString line = (LineString) geom;
            Point start = line.getStartPoint();
            Point end = line.getEndPoint();
            double dx = end.getX() - start.getX();
            double dy = end.getY() - start.getY();
            rotation = Math.atan2(dx, dy) - (Math.PI / 2.0);
            tx = (dx / 2.0) + start.getX();
            ty = (dy / 2.0) + start.getY();
            x = -textBounds.getWidth() / 2.0;

            y = 0;

            if (offset > 0.0) { // to the left of the line
                y = -offset;
            } else if (offset < 0) {
                y = -offset + textBounds.getHeight();
            } else {
                y = textBounds.getHeight() / 2;
            }

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("offset = " + offset + " x = " + x + " y " + y);
            }
        }

        Halo halo = symbolizer.getHalo();

        if (halo != null) {
            drawHalo(halo, tx, ty, x, y, gv, feature, rotation);
        }

        renderString(graphics, tx, ty, x, y, gv, feature, symbolizer.getFill(),
            rotation);
    }

    /**
     * Returns the first font associated to the feature that can be found on
     * the current machine
     *
     * @param feature The feature whose font is to be found
     * @param fonts An array of fonts dependent of the feature, the first that
     *        is found on the current machine is returned
     *
     * @return The first of the specified fonts found on this machine or null
     *         if none found
     */
    private java.awt.Font getFont(Feature feature, Font[] fonts) {
        if (fontFamilies == null) {
            GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
            fontFamilies = new HashSet();

            List f = Arrays.asList(ge.getAvailableFontFamilyNames());
            fontFamilies.addAll(f);

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("there are " + fontFamilies.size()
                    + " fonts available");
            }
        }

        java.awt.Font javaFont = null;

        int styleCode = 0;
        int size = 6;
        String requestedFont = "";

        for (int k = 0; k < fonts.length; k++) {
            requestedFont = fonts[k].getFontFamily().getValue(feature).toString();

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("trying to load " + requestedFont);
            }

            if (loadedFonts.containsKey(requestedFont)) {
                javaFont = (java.awt.Font) loadedFonts.get(requestedFont);

                String reqStyle = (String) fonts[k].getFontStyle().getValue(feature);

                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle))
                        .intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }

                String reqWeight = (String) fonts[k].getFontWeight().getValue(feature);

                if (reqWeight.equalsIgnoreCase("Bold")) {
                    styleCode = styleCode | java.awt.Font.BOLD;
                }

                if ((fonts[k].getFontSize() == null)
                        || (fonts[k].getFontSize().getValue(feature) == null)) {
                    size = 10;
                } else {
                    size = ((Number) fonts[k].getFontSize().getValue(feature))
                        .intValue();
                }

                return javaFont.deriveFont(styleCode, size);
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("not already loaded");
            }

            if (fontFamilies.contains(requestedFont)) {
                String reqStyle = (String) fonts[k].getFontStyle().getValue(feature);

                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle))
                        .intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }

                String reqWeight = (String) fonts[k].getFontWeight().getValue(feature);

                if (reqWeight.equalsIgnoreCase("Bold")) {
                    styleCode = styleCode | java.awt.Font.BOLD;
                }

                if ((fonts[k].getFontSize() == null)
                        || (fonts[k].getFontSize().getValue(feature) == null)) {
                    size = 10;
                } else {
                    size = ((Number) fonts[k].getFontSize().getValue(feature))
                        .intValue();
                }

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("requesting " + requestedFont + " "
                        + styleCode + " " + size);
                }

                javaFont = new java.awt.Font(requestedFont, styleCode, size);
                loadedFonts.put(requestedFont, javaFont);

                return javaFont;
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("not a system font");
            }

            // may be its a file or url
            InputStream is = null;

            if (requestedFont.startsWith("http")
                    || requestedFont.startsWith("file:")) {
                try {
                    URL url = new URL(requestedFont);
                    is = url.openStream();
                } catch (MalformedURLException mue) {
                    // this may be ok - but we should mention it
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Bad url in java2drenderer" + requestedFont
                            + "\n" + mue);
                    }
                } catch (IOException ioe) {
                    // we'll ignore this for the moment
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("IO error in java2drenderer "
                            + requestedFont + "\n" + ioe);
                    }
                }
            } else {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("not a URL");
                }

                File file = new File(requestedFont);

                //if(file.canRead()){
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException fne) {
                    // this may be ok - but we should mention it
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Bad file name in java2drenderer"
                            + requestedFont + "\n" + fne);
                    }
                }
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("about to load");
            }

            if (is == null) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("null input stream");
                }

                continue;
            }

            try {
                javaFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
                        is);
            } catch (FontFormatException ffe) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Font format error in java2drender "
                        + requestedFont + "\n" + ffe);
                }

                continue;
            } catch (IOException ioe) {
                // we'll ignore this for the moment
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("IO error in java2drenderer " + requestedFont
                        + "\n" + ioe);
                }

                continue;
            }

            loadedFonts.put(requestedFont, javaFont);

            return javaFont;
        }

        // if everything fails fall back on a font distributed with the jdk
        return java.awt.Font.getFont("Lucida Sans");
    }

    /**
     * Renders a string accordint the the given parameters
     *
     * @param graphic graphics context used for drawing
     * @param x anchor point x coordinate
     * @param y anchor point y coordinate
     * @param dx horizontal offset
     * @param dy vertical offset
     * @param gv used to draw the string
     * @param feature the feature whose string is associated
     * @param fill eventual feature filling
     * @param rotation text rotation
     */
    void renderString(Graphics2D graphic, double x, double y, double dx,
        double dy, GlyphVector gv, Feature feature, Fill fill, double rotation) {
        AffineTransform temp = graphic.getTransform();
        AffineTransform labelAT = new AffineTransform();

        Point2D mapCentre = new java.awt.geom.Point2D.Double(x, y);
        Point2D graphicCentre = new java.awt.geom.Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        labelAT.translate(graphicCentre.getX(), graphicCentre.getY());

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rotation " + rotation);
        }

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();
        double scaleX = temp.getScaleX();
        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        labelAT.rotate(rotation - originalRotation);

        double xToyRatio = Math.abs(scaleX / scaleY);
        labelAT.scale(xToyRatio, 1.0 / xToyRatio);
        graphic.setTransform(labelAT);

        applyFill(graphic, fill, feature);

        // we move this to the centre of the image.
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("about to draw at " + x + "," + y
                + " with the start of the string at " + (x + dx) + ","
                + (y + dy));
        }

        graphic.drawGlyphVector(gv, (float) dx, (float) dy);

        // tl.draw(graphic, (float) dx, (float) dy);
        //graphics.drawString(label,(float)x,(float)y);
        resetFill(graphic);
        graphic.setTransform(temp);

        return;
    }

    /**
     * Draws an halo around a string in order to make it more visible
     *
     * @param halo the halo styling specification
     * @param x anchor point x coordinate
     * @param y anchor point y coordinate
     * @param dx horizontal offset
     * @param dy vertical offset
     * @param gv used to draw the string
     * @param feature the feature whose string is associated
     * @param rotation text rotation
     */
    private void drawHalo(Halo halo, double x, double y, double dx, double dy,
        GlyphVector gv, Feature feature, double rotation) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("doing halo");
        }

        /*
         * Creates an outline shape from the TextLayout.
         */
        AffineTransform temp = graphics.getTransform();
        AffineTransform labelAT = new AffineTransform();
        float radius = ((Number) halo.getRadius().getValue(feature)).floatValue();

        Point2D mapCentre = new java.awt.geom.Point2D.Double(x, y);
        Point2D graphicCentre = new java.awt.geom.Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        labelAT.translate(graphicCentre.getX(), graphicCentre.getY());

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rotation " + rotation);
        }

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();
        double scaleX = temp.getScaleX();
        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        labelAT.rotate(rotation - originalRotation);

        double xToyRatio = Math.abs(scaleX / scaleY);
        labelAT.scale(xToyRatio, 1.0 / xToyRatio);

        graphics.setTransform(labelAT);

        AffineTransform at = new AffineTransform();
        at.translate(dx, dy);

        // Shape sha = tl.getOutline(at);
        applyFill(graphics, halo.getFill(), feature);

        Shape haloShape = new BasicStroke(2f * radius, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND).createStrokedShape(gv.getOutline(
                    (float) dx, (float) dy));
        graphics.fill(haloShape);
        resetFill(graphics);
        graphics.setTransform(temp);
    }

    /**
     * Renders external images on the passed graphic object
     *
     * @param geom the geometry that specifies the image rendering location
     * @param graphic the external graphic
     * @param eg the image to be rendered on the geometry (?)
     * @param feature the feature whose the external graphic is associated
     *
     * @return true if the image is not null
     */
    private boolean renderExternalGraphic(Geometry geom, Graphic graphic,
        ExternalGraphic eg, Feature feature) {
        BufferedImage img = null;
        List glyphRenderers = new ArrayList();

        glyphRenderers.add(new CustomGlyphRenderer());

        try {
            glyphRenderers.add(new SVGGlyphRenderer());
        } catch (Exception e) {
            LOGGER.warning("Will not support SVG External Graphics " + e);
        }

        Iterator it = glyphRenderers.iterator();

        while (it.hasNext() && (img == null)) {
            GlyphRenderer r = (GlyphRenderer) it.next();

            if (r.canRender(eg.getFormat())) {
                img = r.render(graphic, eg, feature);
            }
        }

        if (img == null) {
            img = getImage((ExternalGraphic) eg);
        }

        if (img != null) {
            int size = ((Number) graphic.getSize().getValue(feature)).intValue();
            double rotation = ((Number) graphic.getRotation().getValue(feature))
                .doubleValue();

            if (geom instanceof Point) {
                renderImage((Point) geom, img, size, rotation);
	    } else if (geom instanceof MultiPoint) {
		MultiPoint multi = (MultiPoint) geom;
	        for (int i = 0; i < multi.getNumGeometries(); i++) {
		renderImage((Point) multi.getGeometryN(i), img, size, 
                            rotation);
		}
            } else {
                renderImage(geom.getCentroid(), img, size, rotation);
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves an external graphic into a buffered image
     *
     * @param graphic the external graphic
     *
     * @return a buffered image representing the external graphic
     */
    private BufferedImage getExternalGraphic(Graphic graphic) {
        ExternalGraphic[] extgraphics = graphic.getExternalGraphics();

        if (extgraphics != null) {
            for (int i = 0; i < extgraphics.length; i++) {
                ExternalGraphic eg = extgraphics[i];
                BufferedImage img = getImage(eg);

                if (img != null) {
                    return img;
                }
            }
        }

        return null;
    }

    /**
     * Tryies to retrieve a specific external graphic into a buffered image
     *
     * @param eg the external graphic
     *
     * @return the buffered image representing the external image, or null if
     *         not found
     */
    private BufferedImage getImage(ExternalGraphic eg) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("got a " + eg.getFormat());
        }

        if (getSupportedGraphicFormats().contains(eg.getFormat().toLowerCase())) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("a java supported format");
            }

            try {
                BufferedImage img = imageLoader.get(eg.getLocation(),
                        isInteractive());

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("Image return = " + img);
                }

                return img;
            } catch (MalformedURLException e) {
                LOGGER.warning("ExternalGraphicURL was badly formed");
            }
        }

        return null;
    }

    /**
     * Renders a mark according to the styling specification
     *
     * @param geom the location where the mark will be rendered
     * @param graphic
     * @param feature the feature to which mark is associated
     * @param mark the mark to be rendered
     *
     * @return true if the mark has been rendered
     */
    private boolean renderMark(Geometry geom, Graphic graphic, Feature feature,
        Mark mark) {
        if (mark == null) {
            return false;
        }

        if ((mark.getWellKnownName() == null)
                || (mark.getWellKnownName().getValue(feature) == null)) {
            return false;
        }

        String name = mark.getWellKnownName().getValue(feature).toString();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rendering mark " + name);
        }

        if (!wellKnownMarks.contains(name)) {
            return false;
        }

        int size = 6; // size in pixels
        double rotation = 0.0; // rotation in degrees
        size = ((Number) graphic.getSize().getValue(feature)).intValue();
        rotation = (((Number) graphic.getRotation().getValue(feature))
            .doubleValue() * Math.PI) / 180d;
	if (geom instanceof Point) {
           fillDrawMark(graphics, (Point) geom, mark, size, rotation, feature);
	} else if (geom instanceof MultiPoint) {
	    MultiPoint multi = (MultiPoint) geom;
	    for (int i = 0; i < multi.getNumGeometries(); i++) {
		fillDrawMark(graphics, (Point) multi.getGeometryN(i), mark, 
                             size, rotation, feature);
            }
        } else {
	    //TODO: Would be nice to throw an exception earlier, like
	    //at configuration time or something?
	    throw new RuntimeException("Can not render a mark on a non-point" +
				       " geometry: " + geom.getClass());
            //Or should we just return false?    
	}
        return true;
    }

    /**
     * Computes which mark can be applied to the feature
     *
     * @param graphic The graphic object containing the mark list
     * @param feature The feature that will be depicted with the mark
     *
     * @return the Mark, if found, or null
     */
    private Mark getMark(Graphic graphic, Feature feature) {
        Mark[] marks = graphic.getMarks();
        Mark mark = null;

        for (int i = 0; i < marks.length; i++) {
            String name = marks[i].getWellKnownName().getValue(feature)
                                  .toString();

            if (wellKnownMarks.contains(name)) {
                mark = marks[i];

                break;
            }
        }
      
        return mark;
    }

    /**
     * Renders an image on the device
     *
     * @param point the image location on the screen
     * @param img the image
     * @param size the image size
     * @param rotation the image rotatation
     */
    private void renderImage(Point point, BufferedImage img, int size,
        double rotation) {
        renderImage(point.getX(), point.getY(), img, size, rotation);
    }

    /**
     * Renders an image on the device
     *
     * @param tx the image location on the screen, x coordinate
     * @param ty the image location on the screen, y coordinate
     * @param img the image
     * @param size the image size
     * @param rotation the image rotatation
     */
    private void renderImage(double tx, double ty, BufferedImage img, int size,
        double rotation) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("drawing Image @" + tx + "," + ty);
        }

        final AffineTransform old = applyTransform(tx, ty, img, size, rotation);

        // we moved the origin to the centre of the image.
        graphics.drawImage(img, -img.getWidth() / 2, -img.getHeight() / 2, obs);

        graphics.setTransform(old);

        return;
    }

    /**
     * DOCUMENT ME!
     *
     * @param tx
     * @param ty
     * @param img
     * @param size
     * @param rotation
     *
     * @return
     */
    private AffineTransform applyTransform(final double tx, final double ty,
        final BufferedImage img, final int size, final double rotation) {
        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Point2D mapCentre = new java.awt.geom.Point2D.Double(tx, ty);
        Point2D graphicCentre = new java.awt.geom.Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        markAT.rotate(rotation - originalRotation);

        double unitSize = Math.max(img.getWidth(), img.getHeight());

        double drawSize = (double) size / unitSize;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("unitsize " + unitSize + " size = " + size
                + " -> scale " + drawSize);
        }

        markAT.scale(drawSize, drawSize);
        graphics.setTransform(markAT);

        return temp;
    }

    /**
     * Draws a filled well known mark on the screen
     *
     * @param graphic the graphic associated to the mark
     * @param point mark location
     * @param mark the mark
     * @param size mark size
     * @param rotation mark location
     * @param feature the feature associated to the mark
     */
    private void fillDrawMark(Graphics2D graphic, Point point, Mark mark,
        int size, double rotation, Feature feature) {
        fillDrawMark(graphic, point.getX(), point.getY(), mark, size, rotation,
            feature);
    }

    /**
     * Draws a filled well known mark on the screen
     *
     * @param graphic the graphic associated to the mark
     * @param tx mark location, x coordinate
     * @param ty mark location, y coordinate
     * @param mark the mark
     * @param size mark size
     * @param rotation mark location
     * @param feature the feature associated to the mark
     */
    private void fillDrawMark(Graphics2D graphic, double tx, double ty,
        Mark mark, int size, double rotation, Feature feature) {
        AffineTransform temp = graphic.getTransform();
        AffineTransform markAT = new AffineTransform();
        Shape shape = Java2DMark.getWellKnownMark(mark.getWellKnownName()
                                                      .getValue(feature)
                                                      .toString());

        Point2D mapCentre = new java.awt.geom.Point2D.Double(tx, ty);
        Point2D graphicCentre = new java.awt.geom.Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        markAT.rotate(rotation - originalRotation);

        Rectangle2D bounds = shape.getBounds2D();

        // getbounds is broken, but getBounds2D is not :-)
        double unitSize = Math.max(bounds.getWidth(), bounds.getHeight());
        double drawSize = (double) size / unitSize;
        markAT.scale(drawSize, -drawSize);

        graphic.setTransform(markAT);

        if (mark.getFill() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying fill to mark");
            }

            applyFill(graphic, mark.getFill(), feature);
            graphic.fill(shape);
        }

        if (mark.getStroke() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying stroke to mark");
            }

            //CHANGED null TO feature IN THE FOLLOWING LINE!!
            applyStroke(graphic, mark.getStroke(), feature);
            graphic.draw(shape);
        }

        graphic.setTransform(temp);

        if (mark.getFill() != null) {
            resetFill(graphic);
        }

        return;
    }

    /**
     * Renders a text symbol on the device
     *
     * @param geom the real world location
     * @param graphic the graphic associated to the text symbol
     * @param feature the feature associated to the text symbol
     * @param mark the text mark to be depicted
     *
     * @return true if the text symbol has been rendered
     */
    private boolean renderTextSymbol(Geometry geom, Graphic graphic,
        Feature feature, TextMark mark) {
        int size = 6; // size in pixels
        double rotation = 0.0; // rotation in degrees
        size = ((Number) graphic.getSize().getValue(feature)).intValue();
        rotation = (((Number) graphic.getRotation().getValue(feature))
            .doubleValue() * Math.PI) / 180d;

        if (!(geom instanceof Point)) {
            geom = geom.getCentroid();
        }
	if (geom instanceof Point) {
           return fillDrawTextMark(graphics, (Point) geom, mark, size, rotation, feature);
	} else if (geom instanceof MultiPoint) {
	    MultiPoint multi = (MultiPoint) geom;
	    for (int i = 0; i < multi.getNumGeometries(); i++) {
		return fillDrawTextMark(graphics, (Point) multi.getGeometryN(i),
                                        mark, size, rotation, feature);
            }
        } else {
	    //TODO: Would be nice to throw an exception earlier, like
	    //at configuration time or something?
	    throw new RuntimeException("Can not render a text symbol on a " + 
				       "non-point geometry: " + geom.getClass());
	}
        return fillDrawTextMark(graphics, (Point) geom, mark, size, rotation,
	  feature);
    }

    /**
     * Renders a text symbol on the device
     *
     * @param graphic the graphic associated to the text symbol
     * @param point the real world location
     * @param mark the text mark to be depicted
     * @param size the text mark size
     * @param rotation the text mark rotation
     * @param feature the feature associated to the text symbol
     *
     * @return true if the text symbol has been rendered
     */
    private boolean fillDrawTextMark(Graphics2D graphic, Point point,
        TextMark mark, int size, double rotation, Feature feature) {
        return fillDrawTextMark(graphic, point.getX(), point.getY(), mark,
            size, rotation, feature);
    }

    /**
     * Renders a text symbol on the device
     *
     * @param graphic the graphic associated to the text symbol
     * @param tx the real world location, x coordinate
     * @param ty the real world location, y coordinate
     * @param mark the text mark to be depicted
     * @param size the text mark size
     * @param rotation the text mark rotation
     * @param feature the feature associated to the text symbol
     *
     * @return true if the text symbol has been rendered
     */
    private boolean fillDrawTextMark(Graphics2D graphic, double tx, double ty,
        TextMark mark, int size, double rotation, Feature feature) {
        java.awt.Font javaFont = getFont(feature, mark.getFonts());

        if (javaFont != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("found font " + javaFont.getFamily());
            }

            graphic.setFont(javaFont);
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("failed to find font ");
            }

            return false;
        }

        String symbol = mark.getSymbol().getValue(feature).toString();

        // TextLayout tl = new TextLayout(symbol, javaFont, graphic.getFontRenderContext());
        AffineTransform oldTx = graphic.getTransform();
        graphic.setTransform(IDENTITY_TRANSFORM);

        GlyphVector gv = javaFont.createGlyphVector(graphic
                .getFontRenderContext(), symbol);
        graphic.setTransform(oldTx);

        Rectangle2D textBounds = gv.getVisualBounds();

        // TODO: consider if symbols should carry an offset
        double dx = textBounds.getWidth() / 2.0;
        double dy = textBounds.getHeight() / 2.0;
        renderString(graphic, tx, ty, dx, dy, gv, feature, mark.getFill(),
            rotation);

        return true;
    }

    /**
     * Renders a filled feature
     *
     * @param graphic graphics context used for drawing
     * @param fill how to fill the feature
     * @param feature the feature to be filled
     */
    public void applyFill(Graphics2D graphic, Fill fill, Feature feature) {
        if (fill == null) {
            return;
        }

        graphic.setColor(Color.decode(
                (String) fill.getColor().getValue(feature)));

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Setting fill: " + graphic.getColor().toString());
        }

        Number value = (Number) fill.getOpacity().getValue(feature);
        float opacity = value.floatValue();
        graphic.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacity));

        Graphic grd = fill.getGraphicFill();

        if (grd != null) {
            setTexture(graphic, grd, feature);
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("no graphic fill set");
            }
        }
    }

    /**
     * Sets a texture on the current graphics2D
     *
     * @param graphic graphics context used for drawing
     * @param gr the graphic specifiying the texture
     * @param feature the feature to be painted
     */
    public void setTexture(Graphics2D graphic, Graphic gr, Feature feature) {
        BufferedImage image = getExternalGraphic(gr);

        if (image != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("got an image in graphic fill");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }

            Mark mark = getMark(gr, feature);
            if(mark == null) {
                mark = StyleFactory.createStyleFactory().getDefaultMark();
            }
            int size = 200;

            image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g1 = image.createGraphics();
            double rotation = 0.0;

            rotation = ((Number) gr.getRotation().getValue(feature))
                .doubleValue();

            fillDrawMark(g1, markCentrePoint, mark, (int) (size * .9),
                rotation, feature);

            MediaTracker track = new MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }

        double width = image.getWidth();
        double height = image.getHeight();
        double unitSize = Math.max(width, height);
        int size = 6;

        size = ((Number) gr.getSize().getValue(feature)).intValue();

        double drawSize = (double) size / unitSize;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("size = " + size + " unitsize " + unitSize
                + " drawSize " + drawSize);
        }

        AffineTransform at = graphic.getTransform();
        double scaleX = drawSize / at.getScaleX();
        double scaleY = drawSize / -at.getScaleY();

        /* This is needed because the image must be a fixed size in pixels
         * but when the image is used as the fill it is transformed by the
         * current transform.
         * However this causes problems as the image size can become very
         * small e.g. 1 or 2 pixels when the drawScale is large, this makes
         * the image fill look very poor - I have no idea how to fix this.
         */
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("scale " + scaleX + " " + scaleY);
        }

        width *= scaleX;
        height *= scaleY;

        Double rect = new Double(0.0, 0.0, width, height);
        TexturePaint imagePaint = new TexturePaint(image, rect);
        graphic.setPaint(imagePaint);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("applied TexturePaint " + imagePaint);
        }
    }

    /**
     * Resets the current graphics2D fill
     *
     * @param graphic graphics context used for drawing
     */
    private void resetFill(Graphics2D graphic) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("reseting the graphics");
        }

        graphic.setComposite(DEFAULT_COMPOSITE);
    }

    /**
     * Resets the current graphics2D fill
     *
     * @param graphic graphics context used for drawing
     */
    private void resetStroke(Graphics2D graphic) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("reseting the graphics");
        }

        graphic.setStroke(DEFAULT_STROKE);
    }

    /**
     * Convenience method for applying a geotools Stroke object as a Graphics2D
     * Stroke object.
     *
     * @param graphic
     * @param stroke the Stroke to apply.
     * @param feature The feature to be stroked
     */
    private void applyStroke(Graphics2D graphic, Stroke stroke, Feature feature) {
        if (stroke == null) {
            return;
        }

        double scale = graphic.getTransform().getScaleX();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("line join = " + stroke.getLineJoin());
        }

        String joinType;

        if (stroke.getLineJoin() == null) {
            joinType = "miter";
        } else {
            joinType = (String) stroke.getLineJoin().getValue(feature);
        }

        if (joinType == null) {
            joinType = "miter";
        }

        int joinCode;

        if (joinLookup.containsKey(joinType)) {
            joinCode = ((Integer) joinLookup.get(joinType)).intValue();
        } else {
            joinCode = BasicStroke.JOIN_MITER;
        }

        String capType;

        if (stroke.getLineCap() != null) {
            capType = (String) stroke.getLineCap().getValue(feature);
        } else {
            capType = "square";
        }

        if (capType == null) {
            capType = "square";
        }

        int capCode;

        if (capLookup.containsKey(capType)) {
            capCode = ((Integer) capLookup.get(capType)).intValue();
        } else {
            capCode = BasicStroke.CAP_SQUARE;
        }

        float[] dashes = stroke.getDashArray();

        if (dashes != null) {
            for (int i = 0; i < dashes.length; i++) {
                dashes[i] = (float) Math.max(1, dashes[i] / (float) scale);
            }
        }

        Number value = (Number) stroke.getWidth().getValue(feature);
        float width = value.floatValue();
        value = (Number) stroke.getDashOffset().getValue(feature);

        float dashOffset = value.floatValue();
        value = (Number) stroke.getOpacity().getValue(feature);

        float opacity = value.floatValue();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("width, dashoffset, opacity " + width + " "
                + dashOffset + " " + opacity);
        }

        BasicStroke stroke2d;

        //TODO: It should not be necessary to divide each value by scale.
        if ((dashes != null) && (dashes.length > 0)) {
            if (width <= 1.0) {
                width = 0;
            }

            stroke2d = new BasicStroke(width / (float) scale, capCode,
                    joinCode, (float) (Math.max(1, 10 / scale)), dashes,
                    dashOffset / (float) scale);
        } else {
            if (width <= 1.0) {
                width = 0;
            }

            stroke2d = new BasicStroke(width / (float) scale, capCode,
                    joinCode, (float) (Math.max(1, 10 / scale)));
        }

        graphic.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacity));

        if (!graphic.getStroke().equals(stroke2d)) {
            graphic.setStroke(stroke2d);
        }

        Color color = Color.decode((String) stroke.getColor().getValue(feature));

        if (!graphic.getColor().equals(color)) {
            graphic.setColor(color);
        }

        Graphic gr = stroke.getGraphicFill();

        if (gr != null) {
            setTexture(graphic, gr, feature);
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("no graphic fill set");
            }
        }
    }

    /**
     * A method to draw the path with a graphic stroke.
     *
     * @param graphic the Graphics2D to draw on
     * @param path the general path to be drawn
     * @param gFill the graphic fill to be used to draw the stroke
     * @param feature The feature to be drawn with the graphic stroke
     */
    private void drawWithGraphicStroke(Graphics2D graphic, Shape path,
        Graphic gFill, Feature feature) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("drawing a graphicalStroke");
        }

        int trueImageHeight = 0;
        int trueImageWidth = 0;

        // get the image to draw
        BufferedImage image = getExternalGraphic(gFill);

        if (image != null) {
            trueImageWidth = image.getWidth();
            trueImageHeight = image.getHeight();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("got an image in graphic fill");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }

            Mark mark = getMark(gFill, feature);
            int size = 200;
            trueImageWidth = size;
            trueImageHeight = size;

            image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g1 = image.createGraphics();
            double rotation = 0.0;
            rotation = ((Number) gFill.getRotation().getValue(feature))
                .doubleValue();
            fillDrawMark(g1, markCentrePoint, mark, (int) (size * .9),
                rotation, feature);

            MediaTracker track = new MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }

        int size = 6;

        size = ((Number) gFill.getSize().getValue(feature)).intValue();

        int imageWidth = size; //image.getWidth();
        int imageHeight = size; //image.getHeight();

        PathIterator pi = path.getPathIterator(null, 10.0);
        double[] coords = new double[6];
        int type;

        double[] first = new double[2];
        double[] previous = new double[2];
        type = pi.currentSegment(coords);
        first[0] = coords[0];
        first[1] = coords[1];
        previous[0] = coords[0];
        previous[1] = coords[1];

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("starting at " + first[0] + "," + first[1]);
        }

        pi.next();

        while (!pi.isDone()) {
            type = pi.currentSegment(coords);

            switch (type) {
            case PathIterator.SEG_MOVETO:

                // nothing to do?
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("moving to " + coords[0] + "," + coords[1]);
                }

                break;

            case PathIterator.SEG_CLOSE:

                // draw back to first from previous
                coords[0] = first[0];
                coords[1] = first[1];

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("closing from " + previous[0] + ","
                        + previous[1] + " to " + coords[0] + "," + coords[1]);
                }

            // no break here - fall through to next section
            case PathIterator.SEG_LINETO:

                // draw from previous to coords
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("drawing from " + previous[0] + ","
                        + previous[1] + " to " + coords[0] + "," + coords[1]);
                }

                double dx = coords[0] - previous[0];
                double dy = coords[1] - previous[1];
                double len = Math.sqrt((dx * dx) + (dy * dy)); // - imageWidth;

                double theta = Math.atan2(dx, dy);
                dx = (Math.sin(theta) * imageWidth);
                dy = (Math.cos(theta) * imageHeight);

                //int dx2 = (int)Math.round(dy/2d);
                //int dy2 = (int)Math.round(dx/2d);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("dx = " + dx + " dy " + dy + " step = "
                        + Math.sqrt((dx * dx) + (dy * dy)));
                }

                double rotation = theta - (Math.PI / 2d);
                double x = previous[0] + (dx / 2.0);
                double y = previous[1] + (dy / 2.0);

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("len =" + len + " imageWidth " + imageWidth);
                }

                double dist = 0;

                for (dist = 0; dist < (len - imageWidth); dist += imageWidth) {
                    /*graphic.drawImage(image2,(int)x-midx,(int)y-midy,null); */
                    renderImage(x, y, image, size, rotation);

                    x += dx;
                    y += dy;
                }

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("loop end dist " + dist + " len " + len + " "
                        + (len - dist));
                }

                double remainder = len - dist;
                int remainingWidth = (int) remainder;

                if (remainingWidth > 0) {
                    //clip and render image
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("about to use clipped image " + remainder);
                    }

                    BufferedImage img = new BufferedImage(trueImageHeight,
                            trueImageWidth, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D ig = img.createGraphics();
                    ig.setClip(0, 0,
                        (int) (((double) trueImageWidth * remainder) / (double) size),
                        trueImageHeight);

                    ig.drawImage(image, 0, 0, trueImageWidth, trueImageHeight,
                        obs);

                    renderImage(x, y, img, size, rotation);
                }

                break;

            default:
                LOGGER.warning(
                    "default branch reached in drawWithGraphicStroke");
            }

            previous[0] = coords[0];
            previous[1] = coords[1];
            pi.next();
        }
    }

    /**
     * Renders a grid coverage on the device. At the time being, the symbolizer
     * is ignored and the renderer tries to depict the non geophysics version
     * of the grid coverage on the device, and falls back on the geophysics
     * one if the former fails
     *
     * @param feature the feature that contains the GridCoverage. The grid
     *        coverage must be contained in the "grid" attribute
     * @param symbolizer The raster symbolizer
     *
     * @task make it follow the symbolizer
     */
    private void renderRaster(Feature feature, RasterSymbolizer symbolizer) {
        GridCoverage grid = (GridCoverage) feature.getAttribute("grid");
        GridCoverageRenderer gcr = new GridCoverageRenderer(grid);
        gcr.paint(graphics);
        LOGGER.finest("Raster rendered");
    }

    /**
     * Convenience method.  Converts a Geometry object into a Shape
     *
     * @param geom The Geometry object to convert
     *
     * @return A GeneralPath that is equivalent to geom
     */
    private Shape createPath(final Geometry geom) {
        if (generalizationDistance > 0) {
            return new LiteShape(geom, true, generalizationDistance);
        } else {
            return new LiteShape(geom, false);
        }
    }

    /**
     * Extracts the named geometry from feature. If geomName is null then the
     * feature's default geometry is used. If geomName cannot be found in
     * feature then null is returned.
     *
     * @param feature The feature to find the geometry in
     * @param geomName The name of the geometry to find: null if the default
     *        geometry should be used.
     *
     * @return The geometry extracted from feature or null if this proved
     *         impossible.
     */
    private Geometry findGeometry(final Feature feature, final String geomName) {
        Geometry geom = null;

        if (geomName == null) {
            geom = feature.getDefaultGeometry();
        } else {
            geom = (Geometry) feature.getAttribute(geomName);
        }

        return geom;
    }

    /**
     * Getter for property interactive.
     *
     * @return Value of property interactive.
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Sets the interactive status of the renderer. An interactive renderer
     * won't wait for long image loading, preferring an alternative mark
     * instead
     *
     * @param interactive new value for the interactive property
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * <p>
     * Returns true if the optimized data loading is enabled, false otherwise.
     * </p>
     * 
     * <p>
     * When optimized data loading is enabled, lite renderer will try to load
     * only the needed feature attributes (according to styles) and to load
     * only the features that are in (or overlaps with)the bounding box
     * requested for painting
     * </p>
     *
     * @return
     */
    public boolean isOptimizedDataLoadingEnabled() {
        return optimizedDataLoadingEnabled;
    }

    /**
     * Enables/disable optimized data loading
     *
     * @param b
     */
    public void setOptimizedDataLoadingEnabled(boolean b) {
        optimizedDataLoadingEnabled = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public double getGeneralizationDistance() {
        return generalizationDistance;
    }

    /**
     * <p>
     * Sets the generalizazion distance in the screen space.
     * </p>
     * 
     * <p>
     * Default value is 1, meaning that two subsequent points are collapsed to
     * one if their on screen distance is less than one pixel
     * </p>
     * 
     * <p>
     * Set the distance to 0 if you don't want any kind of generalization
     * </p>
     *
     * @param d
     */
    public void setGeneralizationDistance(double d) {
        generalizationDistance = d;
    }
}

