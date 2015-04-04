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
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;

import javax.swing.ImageIcon;
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
 * @version    $Id: RenderedLogo.java,v 1.4 2003/08/12 17:05:50 desruisseaux Exp $
 */
public class RenderedLogo extends RenderedLayer {
   /**
    * The legend text, of <code>null</code> if none.
    */
   private String text = "";

   //private LegendPosition position = LegendPosition.NORTH_WEST;
   private LegendPosition position = LegendPosition.SOUTH_WEST;
   private short top = 15;
   private short left = 15;
   private short bottom = 15;
   private short right = 15;

   private Image logo = null;
   private float alpha = 1.0f;
   
   public RenderedLogo() {
   }
   
   /**
    * Construct a new legend located in the upper left corner ({@link LegendPosition#NORTH_WEST}).
    * The space between legend and widget's bounds default to 15 pixels.
    */

   public RenderedLogo(URL imageURL) {

      this(new ImageIcon(imageURL).getImage());
   }
   
   public RenderedLogo(Image logo) {

      this.logo = logo;

      /*
             //blogo = gov.noaa.ncdc.nexradiv.AWTImageExport.toBufferedImage(logo, true);

             // Get all the pixels
             int w = blogo.getWidth(null);
             int h = blogo.getHeight(null);
             
             // Set a pixel
             int rgb, gb;
             int alpha = 10;
             int pixel;
             int r = 255, g=0, b=0;
             int argb = 0x3300FF00; // green
             for (int x=0; x<w; x++) {
                for (int y=0; y<h; y++) {
                   //rgb = rgbs[(h-1)*x + x];
                   //argb = alpha << 24 | r << 16 | g << 8 | b;
                   //pixel = blogo.getRGB(x, y);
                   //alpha = pixel | 0x55000000;
                   
                   argb = blogo.getRGB(x, y);
                   // Isolate the first 8 bits by knocking off the last 24
                   alpha = (int) argb >> 24;
                   // Isolate the last 24 bits by adding 24 blank bits and subtracting
                   rgb = (int) argb - (alpha << 24);
                   // Isolate the first 8 bits by knocking off the last 16
                   r = (int) rgb >> 16;
                   // Isolate the last 16 bits by adding 16 blank bits and subtracting
                   gb = (int) rgb - (r << 16);
                   // Isolate the first 8 bits by knocking off the last 8
                   g = (int) gb >> 8;
                   // Isolate the last 8 bits by adding 8 blank bits and subtracting
                   b = (int) gb - (g << 8);
                  
//System.out.println(alpha+" , "+r+" , "+g+" , "+b);                   
                   
                   pixel = alpha >> 24 | rgb;
                   blogo.setRGB(x, y, pixel);
                }
             }
             
         } else {
             System.err.println("Logo image not found");
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
      */
   }


   /**
    * Construct a new legend with the specified text. By default, the legend is located in the
    * upper left corner ({@link LegendPosition#NORTH_WEST}). The space between legend and
    * widget's bounds default to 15 pixels.
    *
    * @param  text  The legend text, or <code>null</code> if none.
    */
   public RenderedLogo(final String text) {
      this.text = text;
   }
   
   
   public void setImage(Image image) {
       this.logo = image;
       this.repaint();
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
    * @return        The margin value
    * @deprecated    Use {@link #getInsets} instead.
    */
   public Insets getMargin() {
      return getInsets();
   }


   /**
    * @param  insets  The new margin value
    * @deprecated     Use {@link #setInsets} instead.
    */
   public void setMargin(final Insets insets) {
      setInsets(insets);
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
       if (logo == null) {
           return;
       }
       
      context.setCoordinateSystem(context.textCS);
      final Graphics2D graphics = context.getGraphics();
      final Stroke oldStroke = graphics.getStroke();
      final FontRenderContext fc = graphics.getFontRenderContext();
      final GlyphVector glyphs = graphics.getFont().createGlyphVector(fc, text);
      Rectangle2D bounds = glyphs.getVisualBounds();

      
      
      LegendPosition.SOUTH_WEST.setLocation(bounds, 0, 0);
      translate(context, bounds, graphics);
      
      
      if (graphics == null) 
         System.out.println("))))))))))))))))))))))) GRAPHICS NULL (((((((((((((((((((((((((((");
      if (logo == null) 
         System.out.println("))))))))))))))))))))))) LOGO NULL (((((((((((((((((((((((((((");
      
      
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
      
      graphics.drawImage(logo, -10, -1*(-10+logo.getHeight(null)), null);
      bounds = new Rectangle(-10, -1*(-10+logo.getHeight(null)), logo.getWidth(null), logo.getHeight(null));
      
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

public void setAlpha(float alpha) {
	this.alpha = alpha;
}

public float getAlpha() {
	return alpha;
}



}

