/*
 *  Geotools 2 - OpenSource mapping toolkit
 *  (C) 2003, Geotools Project Managment Committee (PMC)
 *  (C) 2003, Institut de Recherche pour le Développement
 *  (C) 1998, Pêches et Océans Canada
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *  Contacts:
 *  UNITED KINGDOM: James Macgill
 *  mailto:j.macgill@geog.leeds.ac.uk
 *
 *  FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *  Institut de Recherche pour le Développement / US-Espace
 *  mailto:seasnet@teledetection.fr
 *
 *  CANADA: Observatoire du Saint-Laurent
 *  Institut Maurice-Lamontagne
 *  mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

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
 * @author     Martin Desruisseaux
 * @created    June 28, 2004
 * @version    $Id: RenderedMarineLegend.java,v 1.4 2003/08/12 17:05:50 desruisseaux Exp $
 */
public class RenderedCategoryLegend extends RenderedLayer {
    
    
    
   /**
    * The legend text, of <code>null</code> if none.
    */
   private String text;

   /**
    * Default position
    */
   private LegendPosition position = LegendPosition.NORTH_WEST;

   /**
    * Default offset
    */
   private short top = 15;

   /**
    * Default offset
    */
   private short left = 15;

   /**
    * Default offset
    */
   private short bottom = 15;

   /**
    * Default offset
    */
   private short right = 15;


   private double[] elementIntervals = {-200.0, -30.0, -20.0, -10.0, 0.0, 10.0, 20.0, 30.0, 40.0, 200.0};
   private Color[] elementColors = {new Color(0, 0, 255), new Color(45, 0, 225), new Color(75, 0, 195),
         new Color(105, 0, 165), new Color(135, 0, 125), new Color(165, 0, 95),
         new Color(195, 0, 65), new Color(225, 0, 35), new Color(255, 0, 0)};
   private String elementName = "ELEMENT_NAME";
   private DecimalFormat fmt1 = new DecimalFormat("0.0");
   private boolean isGeneralInterval = true;
   private boolean highValueOnTop = true;
   private boolean useSetRanges = false;
   private String lowRange;
   private String highRange;


   /**
    * Construct a new legend located in the upper left corner ({@link LegendPosition#NORTH_WEST}).
    * The space between legend and widget's bounds default to 15 pixels.
    */
   public RenderedCategoryLegend() {
      this("Legend");
   }


   /**
    * Construct a new legend with the specified text. By default, the legend is located in the
    * upper left corner ({@link LegendPosition#NORTH_WEST}). The space between legend and
    * widget's bounds default to 15 pixels.
    *
    * @param  text  The legend text, or <code>null</code> if none.
    */
   public RenderedCategoryLegend(final String text) {
      this.text = text;
   }


   /**
    * Returns the legend text, or <code>null</code> if none.
    *
    * @return    The text value
    */
   public String getText() {
      return text;
   }


   /**
    * Set the legend text.
    *
    * @param  text  The new legend text, or <code>null</code> if none.
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
    *
    * @return    The position value
    */
   public LegendPosition getPosition() {
      return position;
   }


   /**
    * Set the legend position. This position is relative to the painting area's bounds.
    *
    * @param  position  The new position value
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
    * Returns the space in pixels between the legend and the painting area's bounds.
    *
    * @return    The insets value
    */
   public Insets getInsets() {
      synchronized (getTreeLock()) {
         return new Insets(top, left, bottom, right);
      }
   }


   /**
    * Set the space in pixels between the legend and the painting area's bounds.
    *
    * @param  insets  The new insets value
    */
   public void setInsets(final Insets insets) {
      final Insets oldInsets;
      synchronized (getTreeLock()) {
         oldInsets = getInsets();
         top = (short) insets.top;
         left = (short) insets.left;
         bottom = (short) insets.bottom;
         right = (short) insets.right;
      }
      listeners.firePropertyChange("insets", oldInsets, insets);
   }


   /**
    * Paint the {@linkplain #getText text} in the legend area.
    *
    * @param  context              Information relatives to the rendering context.
    * @throws  TransformException  If a coordinate transformation failed
    *         during the rendering process.
    */
   protected void paint(final RenderingContext context) throws TransformException {
      paint(context, text);
   }


   /**
    * Convenience method for painting a text in the legend area.
    *
    * @param  context              Information relatives to the rendering context.
    * @param  text                 The text to paint in the legend area.
    * @throws  TransformException  If a coordinate transformation failed
    *         during the rendering process.
    */
   final void paint(final RenderingContext context, final String text) throws TransformException {
      context.setCoordinateSystem(context.textCS);
      final Graphics2D graphics = context.getGraphics();
      final Stroke oldStroke = graphics.getStroke();
      final FontRenderContext fc = graphics.getFontRenderContext();
      final GlyphVector glyphs = graphics.getFont().createGlyphVector(fc, text);
      final Rectangle2D bounds = glyphs.getVisualBounds();

      LegendPosition.SOUTH_WEST.setLocation(bounds, 0, 0);
      translate(context, bounds, graphics);

      // Determine the max size of the glyph vector labels for determined the legend width
      int maxTextWidth = 0;

      FontRenderContext testFc = graphics.getFontRenderContext();
      GlyphVector testGlyphs = graphics.getFont().createGlyphVector(testFc, elementName);
      int textWidth = testGlyphs.getPixelBounds(testFc, 20f, 20f).width;
      if (textWidth > maxTextWidth) {
         maxTextWidth = textWidth;
      }

      String[] label = new String[elementColors.length];
      int flipCorrection;

      if (! useSetRanges) {

         for (int n = 0; n < label.length; n++) {

            if (highValueOnTop) {
               flipCorrection = elementColors.length - 1 - n;
            }
            else {
               flipCorrection = n;
            }

            if (isGeneralInterval) {
               if (flipCorrection == 0) {
                  label[n] = "< " + fmt1.format(elementIntervals[flipCorrection + 1]);
               }
               else if (flipCorrection == elementColors.length - 1) {
                  label[n] = "> " + fmt1.format(elementIntervals[flipCorrection - 1]);
               }
               else {
                  label[n] = fmt1.format(elementIntervals[flipCorrection]) + " - " + fmt1.format(elementIntervals[flipCorrection + 1]);
               }
            }
            else {
               label[n] = fmt1.format(elementIntervals[flipCorrection]) + " - " + fmt1.format(elementIntervals[flipCorrection + 1]);
            }

            testGlyphs = graphics.getFont().createGlyphVector(testFc, label[n]);
            textWidth = testGlyphs.getPixelBounds(testFc, 20f, 20f).width + 30;
            if (textWidth > maxTextWidth) {
               maxTextWidth = textWidth;
            }

         }
      }
      else {
         testGlyphs = graphics.getFont().createGlyphVector(testFc, lowRange);
         textWidth = testGlyphs.getPixelBounds(testFc, 20f, 20f).width + 30;
         if (textWidth > maxTextWidth) {
            maxTextWidth = textWidth;
         }

         testGlyphs = graphics.getFont().createGlyphVector(testFc, highRange);
         textWidth = testGlyphs.getPixelBounds(testFc, 20f, 20f).width + 30;
         if (textWidth > maxTextWidth) {
            maxTextWidth = textWidth;
         }

      }

      final int LEGEND_X = 0;
      final int LEGEND_Y = 0;
      //final int LEGEND_Y = -160;
      final int LEGEND_HEIGHT = 160;
      final int LEGEND_WIDTH = maxTextWidth + 10;

      //Color background = new Color(32,32,64,164);
      //Color background = new Color(255,255,255,164);
      //Color background = new Color(176, 196, 222, 164);
      //Color background = new Color(196, 216, 242, 224);
      //Color background = new Color(173, 218, 173, 224);
      Color background = new Color(210, 180, 140, 224);

      graphics.setPaint(background);
      graphics.fillRect(LEGEND_X, LEGEND_Y, LEGEND_WIDTH, LEGEND_HEIGHT);
      //graphics.setPaint(Color.black);
      //graphics.setPaint(new Color(0, 100, 0));
      graphics.setPaint(new Color(184, 134, 11));
      graphics.drawRect(LEGEND_X, LEGEND_Y, LEGEND_WIDTH, LEGEND_HEIGHT);

      graphics.setFont(new Font("Arial", Font.PLAIN, 10));
      // Draw the color boxes in interval text
      for (int n = 0; n < elementColors.length; n++) {

         if (highValueOnTop) {
            flipCorrection = elementColors.length - 1 - n;
         }
         else {
            flipCorrection = n;
         }

         graphics.setPaint(elementColors[flipCorrection]);
         graphics.fillRect(LEGEND_X + 5, LEGEND_Y + 55 + (n * 12) - 10, 15, 10);
         graphics.setPaint(Color.black);
         graphics.drawRect(LEGEND_X + 5, LEGEND_Y + 55 + (n * 12) - 10, 15, 10);

         final FontRenderContext intervalFc = graphics.getFontRenderContext();
         if (useSetRanges) {
            if (flipCorrection == 0) {
               final GlyphVector intervalGlyphs = graphics.getFont().createGlyphVector(intervalFc, lowRange);
               graphics.setPaint(Color.black);
               graphics.drawGlyphVector(intervalGlyphs, LEGEND_X + 35, LEGEND_Y + 55 + (n * 12));
            }
            else if (flipCorrection == elementColors.length - 1) {
               final GlyphVector intervalGlyphs = graphics.getFont().createGlyphVector(intervalFc, highRange);
               graphics.setPaint(Color.black);
               graphics.drawGlyphVector(intervalGlyphs, LEGEND_X + 35, LEGEND_Y + 55 + (n * 12));
            }
         }
         else {
            final GlyphVector intervalGlyphs = graphics.getFont().createGlyphVector(intervalFc, label[n]);
            graphics.setPaint(Color.black);
            graphics.drawGlyphVector(intervalGlyphs, LEGEND_X + 35, LEGEND_Y + 55 + (n * 12));
         }

      }

      final FontRenderContext elementFc = graphics.getFontRenderContext();
      final GlyphVector elementGlyphs = graphics.getFont().createGlyphVector(elementFc, elementName);
      graphics.setPaint(Color.black);
      graphics.drawGlyphVector(elementGlyphs, LEGEND_X + 5, LEGEND_Y + 40);


      graphics.setPaint(Color.black);
      graphics.setStroke(DEFAULT_STROKE);

      graphics.drawGlyphVector(glyphs, LEGEND_X + 30, LEGEND_Y + 20);
      //graphics.drawGlyphVector(glyphs, 0, 0);
      graphics.setStroke(oldStroke);
      bounds.setRect(bounds.getX() - 1, bounds.getY() - 1, bounds.getWidth() + 2, bounds.getHeight() + 2);
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
    * @param  context      Information relatives to the rendering context.
    * @param  bounds       The legend dimension, in units of {@linkplain RenderingContext#textCS
    *         Java2D coordinate system}. The ({@link Rectangle#x x},{@link Rectangle#y y})
    *         coordinates are usually (but not always) set to (0,0). This rectangle will be
    *         translated to the location where it should be drawn.
    * @param  toTranslate  Description of the Parameter
    */
   final void translate(final RenderingContext context,
         final Rectangle2D bounds,
         final Graphics2D toTranslate) {
      final double x;
      final double y;
      final double width = bounds.getWidth();
      final double height = bounds.getHeight();
      final Rectangle ws = context.getPaintingArea();
      switch (position.getHorizontalAlignment()) {
          case SwingConstants.LEFT:
             x = left;
             break;
          case SwingConstants.RIGHT:
             x = ws.width - (width + right);
             break;
          case SwingConstants.CENTER:
             x = 0.5 * (ws.width - (width + right + left)) + left;
             break;
          default:
             throw new IllegalStateException();
      }
      switch (position.getVerticalAlignment()) {
          case SwingConstants.TOP:
             y = top;
             break;
          case SwingConstants.BOTTOM:
             y = ws.height - (height + bottom);
             break;
          case SwingConstants.CENTER:
             y = 0.5 * (ws.height - (height + bottom + top)) + top;
             break;
          default:
             throw new IllegalStateException();
      }
      if (toTranslate != null) {
         toTranslate.translate(x - bounds.getX(), y - bounds.getY());
      }
      bounds.setRect(x, y, width, height);
      // No addition of (x,y) in 'bounds', since (x,y) were
      // taken in account in Graphics2D.translate(...) above.
   }


   /**
    * Overrides <code>zoomChanged</code> as a no-operation. This is because label are paint at
    * fixed location relative to widget's bounds. Since label location are not zoom-dependent,
    * we do not want to change the painted area after a zoom change event.
    *
    * @param  change  Description of the Parameter
    */
   void zoomChanged(final AffineTransform change) {
   }


   /**
    *  Sets the elementColors attribute of the RenderedMarineLegend object
    *
    * @param  colors  The new elementColors value
    */
   public void setElementColors(Color[] colors) {
      elementColors = colors;
   }


   /**
    *  Sets the elementIntervals attribute of the RenderedMarineLegend object
    *
    * @param  intervals  The new elementIntervals value
    */
   public void setElementIntervals(double[] intervals) {
      for (int i = 0; i < intervals.length - 1; i++) {
         System.out.print(intervals[i] + " ,");
      }
      System.out.println(intervals[intervals.length - 1]);
      elementIntervals = intervals;
   }


   /**
    *  Sets the elementName attribute of the RenderedMarineLegend object
    *
    * @param  name  The new elementName value
    */
   public void setElementName(String name) {
      elementName = name;
   }


   /**
    *  Sets the isGeneralInterval attribute of the RenderedMarineLegend object
    *
    * @param  b  The new isGeneralInterval value
    */
   public void setIsGeneralInterval(boolean b) {
      isGeneralInterval = b;
   }


   /**
    *  Sets the isHighValueOnTop attribute of the RenderedMarineLegend object
    *
    * @param  b  The new isHighValueOnTop value
    */
   public void setIsHighValueOnTop(boolean b) {
      highValueOnTop = b;
   }


   /**
    *  Sets the useSetRanges attribute of the RenderedMarineLegend object
    *
    * @param  low   The new useSetRanges value
    * @param  high  The new useSetRanges value
    */
   public void setUseSetRanges(String low, String high) {
      useSetRanges = true;
      lowRange = low;
      highRange = high;
   }


   /**
    *  Sets the useSetRangesFalse attribute of the RenderedMarineLegend object
    */
   public void setUseSetRangesFalse() {
      useSetRanges = false;
   }

}

