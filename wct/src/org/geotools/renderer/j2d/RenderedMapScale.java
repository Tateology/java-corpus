/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.ct.MathTransform2D;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.XMath;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.resources.renderer.Resources;
import org.geotools.units.Unit;
import org.geotools.units.UnitException;
import org.opengis.referencing.operation.TransformException;


/**
 * A map scale in linear units (for example kilometres) to be painted over others layers.
 * The map scale can be draw on top of {@linkplain ProjectedCoordinateSystem projected} or
 * {@linkplain GeographicCoordinateSystem geographic} coordinate system. Note that because
 * of deformations related to the projection of a curved surface on a flat screen, the map
 * scale is not valid everywhere in the widget area. More specifically:
 *
 * <ul>
 *   <li>In the particular case of {@linkplain ProjectedCoordinateSystem projected coordinate
 *       system}, the map scale is precise only at the latitude of "true scale", which is
 *       projection-dependent. This behavior can be changed with a call to
 *       {@link #setForceGeodesic}.</li>
 *   <li>In the particular case of {@linkplain GeographicCoordinateSystem geographic coordinate
 *       system}, the map scale is precise only at the position where it is drawn. The map scale
 *       is determined using orthodromic distance computation.</li>
 * </ul>
 *
 * @version $Id: RenderedMapScale.java,v 1.10 2003/10/01 20:13:36 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedMapScale extends RenderedLegend {
    /**
     * Round numbers for map scale, between 1 and 10. The map scale length in "real world"
     * units will be rounded to one of those numbers at rendering time.
     */
    private static final double[] SNAP = {1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 7.5, 10.0};

    /**
     * Tells if the map scale should use orthodromic distance computation even for
     * {@linkplain ProjectedCoordinateSystem projected coordinate system}. Default
     * value is <code>false</code>.
     */
    private boolean forceGeodesic = false;

    /**
     * Unit of length to be used for the map scale.
     * The default one is kilometers ("km").
     */
    private Unit units = Unit.KILOMETRE;

    /**
     * The format to use for formatting numbers. If <code>null</code>, then the format
     * will be computed when first needed.
     */
    private NumberFormat format;

    /**
     * <code>true</code> if the map scale is horizontal, or <code>false</code> if it is vertical.
     *
     * @task TODO: Only horizontal map scale has been tested up to now.
     *             Implementation for vertical map scale is not finished.
     */
    private static final boolean horizontal = true;

    /**
     * The amount of sub-division (or outer rectangles) to draw in the map scale.
     */
    private int subDivisions = 3;

    /**
     * The maximum length of map scale in pixels. The actual length may be smaller,
     * since <code>RenderedMapScale</code> will try to round the logical length
     * (e.g. in order to format round labels in kilometres). The logical length
     * is zoom dependent and will be computed at rendering time from the length
     * in pixels.
     */
    private int maximumLength = 300;

    /**
     * The map scale thickness. This is the height of rectangles making
     * horizontal map scale (the "outer" rectangles). This thickness do
     * not depends on the scale value.
     */
    private int thickness = 10;

    /**
     * The thickness of "inner" rectangles.
     */
    private int thicknessSub = 3;

    /**
     * The color for map scale label and the legend.
     */
    private Paint foreground = Color.WHITE;

    /**
     * The color for "outer" rectangle. A few of those rectangle (usually 3)
     * side-by-side make up the map scale.
     */
    private Paint outerRectColor = new Color(215, 235, 255);

    /**
     * The color for "inner" rectangle. This is the rectangle
     * to be drawn inside 1 over 2 "outer" rectangle.
     */
    private Paint innerRectColor = Color.BLACK;

    /**
     * The background color, or <code>null</code> null if none.
     * Default to a slightly transparent blue-gray color.
     */
    private Paint background = new Color(32,32,64,64);

    /**
     * Number of pixels between the background {@link #background} and the map scale.
     */
    private static final int backgroundMargin = 3;

    /**
     * The glyph vectors for labels and the title. Will be constructed
     * during the first rendering and reused as much as possible.
     */
    private transient GlyphVector[] tickGlyphs;

    /**
     * The bounds for each gylph vector in {@link #tickGlyphs}.
     */
    private transient Rectangle2D[] tickBounds;

    /**
     * The logical length computed during the last rendering. Used in order to
     * determine if {@link #tickGlyphs} and {@link #tickBounds} still valids.
     */
    private transient double lastLogicalLength = Double.NaN;

    /**
     * The visual length (in pixels) during the last rendering. Used in order
     * to make adjustement to {@link #tickBounds} during next rendering.
     */
    private transient double lastVisualLength = Double.NaN;

    /**
     * Construct a new map scale located in the lower left corner. The scale
     * position can be changed with {@link #setPosition} and {@link #setMargin}.
     */
    public RenderedMapScale() {
        setPosition(LegendPosition.SOUTH_WEST);
        setInsets(new Insets(8,16,8,16)); // top, left, bottom, right
    }

    /**
     * Returns this layer's name. The default implementation returns the scale factor.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return This layer's name.
     */
    public String getName(final Locale locale) {
        if (renderer != null) {
            float scale = 1/renderer.getScale();
            if (!Float.isNaN(scale)) {
                if (true) {
                    // Keep only 3 significant digits. Our scale is
                    // not accurate enough for displaying all digits.
                    final double factor = XMath.pow10((int)Math.floor(XMath.log10(scale))-2);
                    scale = (float) (Math.rint(scale/factor) * factor);
                }
                return Resources.getResources(locale).getString(ResourceKeys.SCALE_$1,
                                                                     new Float(scale));
            }
        }
        return null;
    }

    /**
     * Tells if the map scale should use orthodromic distance computation even for
     * {@linkplain ProjectedCoordinateSystem projected coordinate system}. Default
     * value is <code>false</code>.
     */
    public boolean getForceGeodesic() {
        return forceGeodesic;
    }

    /**
     * Tells if the map scale should use orthodromic distance computation even for
     * {@linkplain ProjectedCoordinateSystem projected coordinate system}.
     * This method has no effect if the rendering coordinate system is
     * {@linkplain GeographicCoordinateSystem geographic}.
     */
    public void setForceGeodesic(final boolean forceGeodesic) {
        final boolean old;
        synchronized (getTreeLock()) {
            old = forceGeodesic;
            this.forceGeodesic = forceGeodesic;
        }
        listeners.firePropertyChange("forceGeodesic", old, forceGeodesic);
    }

    /**
     * Returns the map scale units. This is the units to be used for displaying numbers
     * on the map scale. The conversion from rendering units is automatically performed
     * at rendering time. Default map scale units are kilometres.
     *
     * @return The map scale units (default to kilometres).
     */
    public Unit getUnits() {
        return units;
    }

    /**
     * Set the map scale units. This unit must be linear, even if the rendering coordinate
     * system is geographic. Conversion from angular to linear unit will be performed at
     * rendering time through orthodromic distance computation.
     *
     * @param  units New map scale units. Must not be <code>null</code>.
     * @throws UnitException if <code>units</code> is not compatible with the map scale.
     */
    public void setUnits(final Unit units) throws UnitException {
        if (units==null || !Unit.METRE.canConvert(units)) {
            throw new UnitException(Resources.getResources(getLocale()).getString(
                                    ResourceKeys.ERROR_BAD_ARGUMENT_$2, "units", units));
        }
        final Unit old;
        synchronized (getTreeLock()) {
            old = this.units;
            this.units = units;
            repaint();
        }
        listeners.firePropertyChange("units", old, units);
    }

    /**
     * Returns the maximum length of map scale in pixels. The actual length may be smaller,
     * since <code>RenderedMapScale</code> will try to round the logical length (e.g. in
     * order to format round labels in kilometres). The logical length is zoom dependent
     * and will be computed at rendering time from the length in pixels.
     *
     * @return The maximum length for the map scale, in pixels (or dots).
     */
    public int getMaximumLength() {
        return maximumLength;
    }

    /**
     * Set the maximum length of map scale in pixels.
     *
     * @param maximumLength The maximum length for the map scale, in pixels (or dots).
     */
    public void setMaximumLength(final int maximumLength) {
        final int old;
        synchronized (getTreeLock()) {
            old = this.maximumLength;
            this.maximumLength = maximumLength;
            repaint();
        }
        listeners.firePropertyChange("maximumLength", old, maximumLength);
    }

    /**
     * Returns the map scale thickness. This is the height of rectangles making
     * horizontal map scale. This thickness do not depends on the scale value.
     *
     * @return The map scale thickness, in dots.
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Sets the map scale thickness. This is the height of rectangles making
     * horizontal map scale. This thickness do not depends on the scale value.
     *
     * @param thickness The new map scale thickness, in dots.
     */
    public void setThickness(final int thickness) {
        final int old;
        synchronized (getTreeLock()) {
            old = this.thickness;
            this.thickness = thickness;
            repaint();
        }
        listeners.firePropertyChange("thickness", old, thickness);
    }

    /**
     * Gets the background color.
     *
     * @return The background color, or <code>null</code> for a completly transparent background.
     */
    public Paint getBackground() {
        return background;
    }

    /**
     * Sets the background color.
     *
     * @param background The new background color, or <code>null</code>
     *                   for a completly transparent background.
     */
    public void setBackground(final Paint background) {
        final Paint old;
        synchronized (getTreeLock()) {
            old = background;
            this.background = background;
        }
        listeners.firePropertyChange("background", old, background);
    }

    /**
     * Gets the foreground color.
     *
     * @return The foreground color.
     */
    public Paint getForeground() {
        return foreground;
    }

    /**
     * Sets the foreground color.
     *
     * @param foreground The new foreground color.
     */
    public void setForeground(final Paint foreground) {
        final Paint old;
        synchronized (getTreeLock()) {
            old = foreground;
            this.foreground = foreground;
        }
        listeners.firePropertyChange("foreground", old, foreground);
    }

    /**
     * Returns the title to paint with the map scale. Default implementation
     * returns the name of the supplied coordinate system.
     *
     * @param  cs The rendering coordinate system, or <code>null</code>.
     * @return The title for the map scale, or <code>null</code> if none.
     */
    protected String getTitle(final CoordinateSystem cs) {
        return (cs!=null) ? cs.getName(getLocale()) : null;
    }

    /**
     * Returns the format to use for formatting numbers.
     */
    private NumberFormat getFormat() {
        if (format == null) {
            format = NumberFormat.getNumberInstance(getLocale());
        }
        return format;
    }

    /**
     * Paint an error message. This method is invoked when the rendering coordinate
     * system uses unknow units.
     *
     * @param  context Information relatives to the rendering context.
     * @throws TransformException If a coordinate transformation failed
     *         during the rendering process.
     */
    private void paintError(final RenderingContext context) throws TransformException {
        context.getGraphics().setPaint(foreground);
        paint(context, Resources.getResources(getLocale()).getString(ResourceKeys.ERROR));
    }

    /**
     * Draw the map scale. This map scale is build from some amount of rectangle drawn side
     * by side vertically or horizontally. Each rectangle is for a distance like 100 meters
     * or 1 kilometer, depending the current map scale (from the zoom).
     *
     * @param  context Information relatives to the rendering context.
     * @throws TransformException If a coordinate transformation failed
     *         during the rendering process.
     */
    protected void paint(final RenderingContext context) throws TransformException {
//        assert Thread.holdsLock(getTreeLock());
        ////////////////////////////////////////////////////////////////////////////
        //////                                                                  ////
        //////    BLOCK 1 - Compute the map scale length. No painting occurs    ////
        //////              here. No Graphics2D modification for now.           ////
        //////                                                                  ////
        ////////////////////////////////////////////////////////////////////////////
        /*
         * Gets an estimation of the map scale in linear units (usually kilometers). First,
         * we get an estimation of the map scale position in screen coordinates. We use a
         * coordinate system local to the legend in which the upper-left corner of the map
         * scale is located at (0,0). Ticks labels and scale title locations will be relative
         * to the map scale.
         */
        final Rectangle bounds;
        if (horizontal) {
            bounds = new Rectangle(0, 0, maximumLength, thickness);
        } else {
            bounds = new Rectangle(0, 0, thickness, maximumLength);
        }
        translate(context, bounds, null);
        Point2D P1,P2;
        if (horizontal) {
            final double center = bounds.getCenterY();
            P1 = new Point2D.Double(bounds.getMinX(), center);
            P2 = new Point2D.Double(bounds.getMaxX(), center);
        } else {
            final double center = bounds.getCenterX();
            P1 = new Point2D.Double(center, bounds.getMinY());
            P2 = new Point2D.Double(center, bounds.getMaxY());
        }
        CoordinateSystem mapCS = context.mapCS;
        if (forceGeodesic && (mapCS instanceof ProjectedCoordinateSystem)) {
            mapCS = ((ProjectedCoordinateSystem) mapCS).getGeographicCoordinateSystem();
        }
        final MathTransform2D toMap = (MathTransform2D)
                                      context.getMathTransform(context.textCS, mapCS);
        P1 = toMap.transform(P1, P1);
        P2 = toMap.transform(P2, P2);
        /*
         * Convert the position from pixels to "real world" coordinates. Then, measures its length
         * using orthodromic distance computation if the rendering units were angular units. Then,
         * "snap" the length to some number easier to read. For example the length 2371 will be
         * snapped to 2500. Finally, the new "snapped" length will be converted bach to pixel units.
         */
        final Unit mapUnitX = mapCS.getUnits(0);
        final Unit mapUnitY = mapCS.getUnits(1);
        if (mapUnitX==null || mapUnitY==null) {
            paintError(context);
            return;
        }
        double logicalLength;
        final Ellipsoid ellipsoid = CTSUtilities.getHeadGeoEllipsoid(mapCS);
        try {
            if (ellipsoid != null) {
                P1.setLocation(Unit.DEGREE.convert(P1.getX(), mapUnitX),
                               Unit.DEGREE.convert(P1.getY(), mapUnitY));
                P2.setLocation(Unit.DEGREE.convert(P2.getX(), mapUnitX),
                               Unit.DEGREE.convert(P2.getY(), mapUnitY));
                logicalLength = ellipsoid.orthodromicDistance(P1, P2);
                logicalLength = units.convert(logicalLength, ellipsoid.getAxisUnit());
            } else {
                P1.setLocation(units.convert(P1.getX(), mapUnitX),
                               units.convert(P1.getY(), mapUnitY));
                P2.setLocation(units.convert(P2.getX(), mapUnitX),
                               units.convert(P2.getY(), mapUnitY));
                logicalLength = P1.distance(P2);
            }
        } catch (UnitException exception) {
            // Should not occurs, unless the user is using a very particular coordinate system.
            final LogRecord record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
            record.setSourceClassName("RenderedMapScale");
            record.setSourceMethodName("paint");
            record.setThrown(exception);
            Renderer.LOGGER.log(record);
            paintError(context);
            return;
        }
        final double scaleFactor = logicalLength / maximumLength;
        logicalLength /= subDivisions;
        if (true) {
            // If the current logical length is between two values in the SNAP array, then select
            // the lowest value. It produces a more compact scale than selecting the highest value.
            final double factor = XMath.pow10((int)Math.floor(XMath.log10(logicalLength)));
            logicalLength /= factor;
            int index = Arrays.binarySearch(SNAP, logicalLength);
            if (index < 0) {
                index = ~index;  // Highest value (really ~, not -)
                if (index > 0) {
                    index--; // Choose lowest value instead, if such a value exists.
                }
            }
            logicalLength = SNAP[index];
            logicalLength *= factor;
        }
        final int visualLength = (int)Math.ceil(logicalLength / scaleFactor);

        ////////////////////////////////////////////////////////////////////////////
        //////                                                                  ////
        //////    BLOCK 2 - Compute the content. No painting occurs here.       ////
        //////              No Graphics2D modification, except through          ////
        //////              RenderingContext.setCoordinateSystem(...).          ////
        //////                                                                  ////
        ////////////////////////////////////////////////////////////////////////////
        if (tickGlyphs == null) {
            tickGlyphs = new GlyphVector[subDivisions+2];
            tickBounds = new Rectangle2D[subDivisions+2];
        }
        if (horizontal) {
            bounds.setRect(0, 0, visualLength*subDivisions, thickness);
        } else {
            bounds.setRect(0, 0, thickness, visualLength*subDivisions);
        }
        context.setCoordinateSystem(context.textCS);
        final Graphics2D graphics = context.getGraphics();
        if (lastLogicalLength != logicalLength) {
            /*
             * If the tick labels and/or the scale title changed, recreate them.
             * Glyph vectors will be saved for faster rendering during the next
             * paint event.
             */
            logUpdateCache("RenderedMapScale");
            final FontRenderContext fontContext = graphics.getFontRenderContext();
            final Font           font = graphics.getFont();
            final StringBuffer buffer = new StringBuffer(16);
            final FieldPosition   pos = new FieldPosition(0);
            for (int i=0; i<=subDivisions; i++) {
                String text = getFormat().format(logicalLength*i, buffer, pos).toString();
                GlyphVector glyphs = font.createGlyphVector(fontContext, text);
                Rectangle2D rect   = glyphs.getVisualBounds();
                LegendPosition.NORTH.setLocation(rect, visualLength*i, thickness+3);
                if (i == subDivisions) {
                    buffer.append(' ');
                    buffer.append(units);
                    final double anchorX = rect.getMinX();
                    final double anchorY = rect.getMaxY();
                    text   = buffer.toString();
                    glyphs = font.createGlyphVector(fontContext, text);
                    rect   = glyphs.getVisualBounds();
                    LegendPosition.SOUTH_WEST.setLocation(rect, anchorX, anchorY);
                }
                bounds.add(rect);
                tickBounds[i] = rect;
                tickGlyphs[i] = glyphs;
                buffer.setLength(0);
            }
            String title = getText();
            if (title == null) {
                title = getTitle(mapCS);
            }
            if (title != null) {
                final Font        lfont  = font.deriveFont(Font.BOLD | Font.ITALIC);
                final GlyphVector glyphs = lfont.createGlyphVector(fontContext, title);
                final Rectangle2D rect   = glyphs.getVisualBounds();
                LegendPosition.SOUTH.setLocation(rect, 0.5*subDivisions*visualLength, -3);
                tickGlyphs[subDivisions+1] = glyphs;
                tickBounds[subDivisions+1] = rect;
                bounds.add(rect);
            }
            lastLogicalLength = logicalLength;
        } else {
            /*
             * The cached glyphs are still valids. However, the labels may have been
             * translated because of a different visual length. Update the labels positions.
             */
            final double adjust = visualLength-lastVisualLength;
            for (int i=0; i<tickBounds.length; i++) {
                final Rectangle2D tick = tickBounds[i];
                if (adjust != 0) {
                    double x = tick.getX();
                    double y = tick.getY();
                    final double delta = adjust * (i==tickBounds.length-1 ? subDivisions*0.5 : i);
                    if (horizontal) {
                        x += delta;
                    } else {
                        y += delta;
                    }
                    tick.setRect(x, y, tick.getWidth(), tick.getHeight());
                }
                bounds.add(tick);
            }
        }
        lastVisualLength = visualLength;

        ////////////////////////////////////////////////////////////////////////////
        //////                                                                  ////
        //////    BLOCK 3 - Paint the content.                                  ////
        //////                                                                  ////
        ////////////////////////////////////////////////////////////////////////////
        int minX = bounds.x;
        int minY = bounds.y;
        translate(context, bounds, graphics);
        final Stroke oldStroke = graphics.getStroke();
        final Paint   oldPaint = graphics.getPaint();
        graphics.setStroke(DEFAULT_STROKE);
        if (background != null) {
            minX          -=   backgroundMargin;
            minY          -=   backgroundMargin;
            bounds.x      -=   backgroundMargin;
            bounds.y      -=   backgroundMargin;
            bounds.width  += 2*backgroundMargin;
            bounds.height += 2*backgroundMargin;
            graphics.setPaint(background);
            graphics.fillRect(minX, minY, bounds.width, bounds.height);
        }
        final Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, visualLength, thickness);
        for (int i=0; i<subDivisions; i++) {
            graphics.setPaint(outerRectColor);
            graphics.fill(rect);
            graphics.setPaint(innerRectColor);
            graphics.draw(rect);
            if ((i&1) != 0) {
                /*
                 * Dans un rectangle sur deux, on dessinera un
                 * rectangle noir à l'intérieur du rectangle blanc.
                 */
                int space   = thickness-thicknessSub;
                rect.height = thicknessSub;
                rect.y      = space >> 1;
                if ((space&1)!=0) rect.y++;
                else rect.height++;
                graphics.fill(rect);
                rect.height=thickness;
                rect.y = 0;
            }
            rect.x += visualLength;
        }
        /*
         * Writes tick labels, units and map scale legend. All those
         * texts has been prepared and cached as glyph vectors.
         */
        graphics.setPaint(foreground);
        for (int i=0; i<tickGlyphs.length; i++) {
            if (tickGlyphs[i] != null) {
                final Rectangle2D pos = tickBounds[i];
                graphics.drawGlyphVector(tickGlyphs[i], (float)pos.getMinX(),
                                                        (float)pos.getMaxY());
            }
        }
        context.setCoordinateSystem(context.mapCS);
        graphics.setStroke(oldStroke);
        graphics.setPaint(oldPaint);
        bounds.setBounds(bounds.x-1, bounds.y-1, bounds.width+2, bounds.height+2);
        context.addPaintedArea(bounds, context.textCS);
    }

    /**
     * Clear the memory cache.
     */
    void clearCache() {
        lastLogicalLength = Double.NaN;
        lastVisualLength  = Double.NaN;
        format     = null;
        tickGlyphs = null;
        tickBounds = null;
        super.clearCache();
    }

    /**
     * Returns the map scale as a tool tip text.
     */
    String getToolTipText(final GeoMouseEvent event) {
        final String name = getName(getLocale());
        if (name != null) {
            return name;
        }
        return super.getToolTipText(event);
    }
}

