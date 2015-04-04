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
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import org.opengis.referencing.operation.TransformException;


/**
 * Base class for legends painted at fixed location in the widget area. Contrary to others
 * kinds of {@linkplain RenderedLayer layers}, legends location are not zoom-dependent. The
 * legend location is specified by two quantities: a {@linkplain #getPosition compass-direction}
 * relative to widget's bounds (for example {@linkplain LegendPosition#NORTH_EAST North-East})
 * and a {@linkplain #getMargin margin} in dots (or pixels) to keep between the legend and
 * widget's borders.
 *
 * @version $Id: RenderedLegend.java,v 1.4 2003/08/12 17:05:50 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RenderedLegend extends RenderedLayer {
    /**
     * The legend text, of <code>null</code> if none.
     */
    private String text;

    /**
     * Position où placer la légende.
     */
    private LegendPosition position = LegendPosition.NORTH_WEST;

    /**
     * Espace à laisser entre le haut de la fenêtre et le haut de la légende.
     */
    private short top = 15;

    /**
     * Espace à laisser entre le bord gauche de la la fenêtre et le bord gauche de la légende.
     */
    private short left = 15;

    /**
     * Espace à laisser entre le bas de la fenêtre et le bas de la légende.
     */
    private short bottom = 15;

    /**
     * Espace à laisser entre le bord droit de la la fenêtre et le bord droit de la légende.
     */
    private short right = 15;

    /**
     * Construct a new legend located in the upper left corner ({@link LegendPosition#NORTH_WEST}).
     * The space between legend and widget's bounds default to 15 pixels.
     */
    public RenderedLegend() {
    }

    /**
     * Construct a new legend with the specified text. By default, the legend is located in the
     * upper left corner ({@link LegendPosition#NORTH_WEST}). The space between legend and
     * widget's bounds default to 15 pixels.
     *
     * @param text The legend text, or <code>null</code> if none.
     */
    public RenderedLegend(final String text) {
        this.text = text;
    }

    /**
     * Returns the legend text, or <code>null</code> if none.
     */
    public String getText() {
        return text;
    }

    /**
     * Set the legend text.
     *
     * @param text The new legend text, or <code>null</code> if none.
     */
    public void setText(final String text) {
        final String oldText = this.text;
        synchronized (getTreeLock()) {
            this.text = text;
        }
        listeners.firePropertyChange("text", oldText, text);
    }

    /**
     * Returns the legend position.
     */
    public LegendPosition getPosition() {
        return position;
    }

    /**
     * Set the legend position. This position is relative to the painting area's bounds.
     */
    public void setPosition(final LegendPosition position) {
        if (position == null) {
            throw new IllegalArgumentException();
        }
        final LegendPosition oldPosition = this.position;
        synchronized (getTreeLock()) {
            this.position = position;
        }
        listeners.firePropertyChange("position", oldPosition, position);
    }

    /**
     * @deprecated Use {@link #getInsets} instead.
     */
    public Insets getMargin() {
        return getInsets();
    }

    /**
     * @deprecated Use {@link #setInsets} instead.
     */
    public void setMargin(final Insets insets) {
        setInsets(insets);
    }

    /**
     * Returns the space in pixels between the legend and the painting area's bounds.
     */
    public Insets getInsets() {
        synchronized (getTreeLock()) {
            return new Insets(top, left, bottom, right);
        }
    }

    /**
     * Set the space in pixels between the legend and the painting area's bounds.
     */
    public void setInsets(final Insets insets) {
        final Insets oldInsets;
        synchronized (getTreeLock()) {
            oldInsets = getInsets();
            top    = (short) insets.top;
            left   = (short) insets.left;
            bottom = (short) insets.bottom;
            right  = (short) insets.right;
        }
        listeners.firePropertyChange("insets", oldInsets, insets);
    }

    /**
     * Paint the {@linkplain #getText text} in the legend area.
     *
     * @param  context Information relatives to the rendering context.
     * @throws TransformException If a coordinate transformation failed
     *         during the rendering process.
     */
    protected void paint(final RenderingContext context) throws TransformException {
        paint(context, text);
    }

    /**
     * Convenience method for painting a text in the legend area.
     *
     * @param  context Information relatives to the rendering context.
     * @param  text The text to paint in the legend area.
     * @throws TransformException If a coordinate transformation failed
     *         during the rendering process.
     */
    final void paint(final RenderingContext context, final String text) throws TransformException {
        context.setCoordinateSystem(context.textCS);
        final Graphics2D  graphics = context.getGraphics();
        final Stroke     oldStroke = graphics.getStroke();
        final FontRenderContext fc = graphics.getFontRenderContext();
        final GlyphVector   glyphs = graphics.getFont().createGlyphVector(fc, text);
        final Rectangle2D   bounds = glyphs.getVisualBounds();
        LegendPosition.SOUTH_WEST.setLocation(bounds, 0, 0);
        translate(context, bounds, graphics);
        graphics.setStroke(DEFAULT_STROKE);
        graphics.drawGlyphVector(glyphs, 0, 0);
        graphics.setStroke(oldStroke);
        bounds.setRect(bounds.getX()-1, bounds.getY()-1, bounds.getWidth()+2, bounds.getHeight()+2);
        context.addPaintedArea(bounds, context.textCS);
        context.setCoordinateSystem(context.mapCS);
    }

    /**
     * Translate the specified rectangle in such a way that it appears at the {@link #getPosition}
     * location. If the <code>toTranslate</code> argument is non-null, then the same translation
     * is applied to the specified {@link Graphics2D} as well. This is a helper method for
     * implementing the {@link #paint} method in subclasses. Example:
     *
     * <blockquote><pre>
     * &nbsp;protected void paint(RenderingContext context) throws TransformException {
     * &nbsp;    Graphics2D graphics  = context.getGraphics();
     * &nbsp;    context.setCoordinateSystem(context.textCS);
     * &nbsp;    Rectangle bounds = new Rectangle(0, 0, <font face="Arial" size=2><i>legend_width</i></font>, <font face="Arial" size=2><i>legend_height</i></font>);
     * &nbsp;    <strong>translate(context, bounds, graphics);</strong>
     * &nbsp;    // <font face="Arial" size=2>draw legend now ...</font>
     * &nbsp;
     * &nbsp;    context.setCoordinateSystem(context.mapCS);
     * &nbsp;    context.addPaintedArea(bounds, context.textCS);
     * &nbsp;}
     * </pre></blockquote>
     *
     * @param  context Information relatives to the rendering context.
     * @param  bounds The legend dimension, in units of {@linkplain RenderingContext#textCS
     *         Java2D coordinate system}. The ({@link Rectangle#x x},{@link Rectangle#y y})
     *         coordinates are usually (but not always) set to (0,0). This rectangle will be
     *         translated to the location where it should be drawn.
     * @param  graphics If non-null, a graphics on which to apply the same translation than
     *         <code>bounds</code>. It allows painting in "legend" coordinate system no matter
     *         where the legend is actually located. The graphics origin (0,0) while always be
     *         located in the lower-left corner of the legend area.
     */
    final void translate(final RenderingContext context,
                         final Rectangle2D      bounds,
                         final Graphics2D       toTranslate)
    {
        final double x, y;
        final double width  = bounds.getWidth();
        final double height = bounds.getHeight();
        final Rectangle  ws = context.getPaintingArea();
        switch (position.getHorizontalAlignment()) {
            case SwingConstants.LEFT:   x=left; break;
            case SwingConstants.RIGHT:  x=ws.width-(width+right); break;
            case SwingConstants.CENTER: x=0.5*(ws.width-(width+right+left)) + left; break;
            default: throw new IllegalStateException();
        }
        switch (position.getVerticalAlignment()) {
            case SwingConstants.TOP:    y=top; break;
            case SwingConstants.BOTTOM: y=ws.height-(height+bottom); break;
            case SwingConstants.CENTER: y=0.5*(ws.height-(height+bottom+top)) + top; break;
            default: throw new IllegalStateException();
        }
        if (toTranslate != null) {
            toTranslate.translate(x-bounds.getX(), y-bounds.getY());
        }
        bounds.setRect(x, y, width, height);
        // No addition of (x,y) in 'bounds', since (x,y) were
        // taken in account in Graphics2D.translate(...) above.
    }

    /**
     * Overrides <code>zoomChanged</code> as a no-operation. This is because label are paint at
     * fixed location relative to widget's bounds. Since label location are not zoom-dependent,
     * we do not want to change the painted area after a zoom change event.
     */
    void zoomChanged(final AffineTransform change) {
    }
}

