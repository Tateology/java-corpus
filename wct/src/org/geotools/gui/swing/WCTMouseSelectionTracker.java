/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.gui.swing;

// Geometry
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.event.MouseInputAdapter;


/**
 * Controller which allows the user to select a region of a component.
 * The user must click on a point in the component, then drag the mouse
 * pointer whilst keeping the button pressed. During the dragging,
 * the shape which is drawn will normally be a rectangle.  Other shapes could
 * always be used such as, for example, an ellipse.  To use this class, it
 * is necessary to create a derived class which defines the following
 * methods:
 *
 * <ul>
 *   <li>{@link #selectionPerformed} (obligatory)</li>
 *   <li>{@link #getModel} (optional)</li>
 * </ul>
 *
 * This controller should then be registered with one, and only one, component
 * using the following syntax:
 *
 * <blockquote><pre>
 * {@link Component} component=...
 * MouseSelectionTracker control=...
 * component.addMouseListener(control);
 * </pre></blockquote>
 *
 * @version $Id: MouseSelectionTracker.java,v 1.4 2003/05/13 11:01:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class WCTMouseSelectionTracker extends MouseInputAdapter {
    
    
    
    /**
     * Stippled rectangle representing the region which the user is currently
     * selecting.  This rectangle can be empty.  These coordinates are only
     * significant in the period between the user pressing the mouse button
     * and then releasing it to outline a region. Conventionally, the
     * <code>null</code> value indicates that a line should be used instead of
     * a rectangular shape.  The coordinates are always expressed in pixels.
     */
    private transient RectangularShape mouseSelectedArea;

    /**
     * Colour to replace during XOR drawings on a graphic.
     * This colour is specified in {@link Graphics2D#setColor}.
     */
    private Color backXORColor = Color.white;

    /**
     * Colour to replace with during the XOR drawings on a graphic.
     * This colour is specified in {@link Graphics2D#setXORMode}.
     */
    private Color lineXORColor = Color.black;

    /**
     * <var>x</var> coordinate of the mouse when the button is pressed.
     */
    private transient int ox;

    /**
     * <var>y</var> coordinate of the mouse when the button is pressed.
     */
    private transient int oy;

    /**
     * <var>x</var> coordinate of the mouse during the last drag.
     */
    private transient int px;

    /**
     * <var>y</var> coordinate of the mouse during the last drag.
     */
    private transient int py;

    /**
     * Indicates whether a selection is underway.
     */
    private transient boolean isDragging;

    /**
     * Constructs an object which will allow rectangular regions to be selected
     * using the mouse.
     */
    public WCTMouseSelectionTracker() {
    }

    /**
     * Specifies the colours to be used for drawing the outline of a box when
     * the user selects a region.  All <code>a</code> colours will be replaced
     * by <code>b</code> colours and vice versa.
     */
    public void setXORColors(final Color a, final Color b) {
        backXORColor = a;
        lineXORColor = b;
    }

    /**
     * Returns the geometric shape to use for marking the boundaries of a
     * region.  This shape is normally a rectangle but could also be an
     * ellipse, an arrow or even other shapes. The coordinates of the
     * returned shape will not be taken into account. In fact, these
     * coordinates will regularly be discarded. Only the class of the returned
     * shape will count (for example, {@link java.awt.geom.Ellipse2D}
     * vs {@link java.awt.geom.Rectangle2D}) and their parameters which are not
     * linked to their position (for example, the rounding of a rectangle's
     * corners).
     * The shape returned will normally be from a class derived from
     * {@link RectangularShape}, but could also be from the {@link Line2D}
     * class. <strong>Any other class risks throwing a
     * {@link ClassCastException} when executed</strong>.
     *
     * The default implementation always returns an object {@link Rectangle}.
     *
     * @param  event Mouse coordinate when the button is pressed.  This
     *         information can be used by the derived classes which like to
     *         be informed of the position of the mouse before chosing a 
     *         geometric shape.
     * @return Shape from the class {link RectangularShape} or {link Line2D}, or
     *         <code>null</code> to indicate that we do not want to make a
     *         selection.
     */
    protected Shape getModel(final MouseEvent event) {
        return new Rectangle();
    }

    /**
     * Method which is automatically called after the user selects
     * a region with the mouse.  All coordinates passed in as parameters
     * are expressed in pixels.
     *
     * @param ox <var>x</var> coordinate of the mouse when the user
     *        pressed the mouse button.
     * @param oy <var>y</var> coordinate of the mouse when the user
     *        pressed the mouse button.
     * @param px <var>x</var> coordinate of the mouse when the user
     *        released the mouse button.
     * @param py <var>y</var> coordinate of the mouse when the user
     *        released the mouse button.
     */
    protected abstract void selectionPerformed(int ox, int oy, int px, int py);

    /**
     * Returns the geometric shape surrounding the last region to be selected
     * by the user. An optional affine transform can be specified to convert
     * the region selected by the user into logical coordinates. The class
     * of the shape returned depends on the model returned by
     * {@link #getModel}:
     *
     * <ul>
     *   <li>If the model is null (which means that this
     *       <code>MouseSelectionTracker</code> object only draws a line
     *       between points), the object returned will belong to the
     *       {@link Line2D} class.</li>
     *   <li>If the model is not null, the object returned can be from the
     *       same class (most often {@link java.awt.geom.Rectangle2D}).
     *       There could always be situations where the object returned is from
     *       another class, for example if the affine transform
     *       <code>transform</code> carries out a rotation.</li>
     * </ul>
     *
     * @param  transform Affine transform which converts logical coordinates
     *         into pixel coordinates.  It is usually an affine transform which
     *         is used in a <code>paint(...)</code> method to draw shapes
     *         expressed in logical coordinates.
     * @return A geometric shape enclosing the last region to be selected by
     *         the user, or <code>null</code> if no selection has yet been
     *         made.
     * @throws NoninvertibleTransformException If the affine transform 
     *         <code>transform</code> can't be inverted.
     */
    public Shape getSelectedArea(final AffineTransform transform)
                                 throws NoninvertibleTransformException {
        if (ox == px && oy == py) return null;
        RectangularShape shape = mouseSelectedArea;
        if (transform != null && !transform.isIdentity()) {
            if (shape == null) {
                final Point2D.Float po = new Point2D.Float(ox, oy);
                final Point2D.Float pp = new Point2D.Float(px, py);
                transform.inverseTransform(po, po);
                transform.inverseTransform(pp, pp);
                return new Line2D.Float(po, pp);
            } else {
                if (canReshape(shape, transform)) {
                    final Point2D.Double point = new Point2D.Double();
                    double xmin = Double.POSITIVE_INFINITY;
                    double ymin = Double.POSITIVE_INFINITY;
                    double xmax = Double.NEGATIVE_INFINITY;
                    double ymax = Double.NEGATIVE_INFINITY;
                    for (int i = 0; i < 4; i++) {
                        point.x = (i&1) == 0 ? shape.getMinX() : shape.getMaxX();
                        point.y = (i&2) == 0 ? shape.getMinY() : shape.getMaxY();
                        transform.inverseTransform(point, point);
                        if (point.x < xmin) xmin = point.x;
                        if (point.x > xmax) xmax = point.x;
                        if (point.y < ymin) ymin = point.y;
                        if (point.y > ymax) ymax = point.y;
                    }
                    if (shape instanceof Rectangle) {
                        return new Rectangle2D.Float((float) xmin,
                                                     (float) ymin,
                                                     (float) (xmax - xmin),
                                                     (float) (ymax - ymin));
                    } else {
                        shape = (RectangularShape) shape.clone();
                        shape.setFrame(xmin, ymin, xmax - xmin, ymax - ymin);
                        return shape;
                    }
                }
                else {
                    return transform.createInverse().createTransformedShape(shape);
                }
            }
        }
        else {
            return (shape != null) ? (Shape) shape.clone() : new Line2D.Float(ox, oy, px, py);
        }
    }

    /**
     * Indicates whether we can transform <code>shape</code> simply
     * by calling its <code>shape.setFrame(...)</code> method rather
     * than by using the heavy artillery that is the
     * <code>transform.createTransformedShape(shape)</code> method.
     */
    private static boolean canReshape(final RectangularShape shape,
                                      final AffineTransform transform) {
        final int type=transform.getType();
        if ((type & AffineTransform.TYPE_GENERAL_TRANSFORM) != 0) return false;
        if ((type & AffineTransform.TYPE_MASK_ROTATION)     != 0) return false;
        if ((type & AffineTransform.TYPE_FLIP)              != 0) {
            if (shape instanceof Rectangle2D)      return true;
            if (shape instanceof Ellipse2D)        return true;
            if (shape instanceof RoundRectangle2D) return true;
            return false;
        }
        return true;
    }

    /**
     * Returns a {@link Graphics2D} object to be used for drawing in the 
     * specified component.  We must not forget to call
     * {@link Graphics2D#dispose} when the graphics object is no longer
     * needed.
     */
    private Graphics2D getGraphics(final Component c) {
        final Graphics2D graphics = (Graphics2D) c.getGraphics();
        graphics.setXORMode(lineXORColor);
        graphics.setColor  (backXORColor);
        graphics.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        graphics.setColor(Color.RED);
        return graphics;
    }

    /**
     * Informs this controller that the mouse button has been pressed.
     * The default implementation retains the mouse coordinate (which will
     * become one of the corners of the future rectangle to be drawn) 
     * and prepares <code>this</code> to observe the mouse movements.
     *
     * @throws ClassCastException if {@link #getModel} doesn't return a shape
     *         from the class {link RectangularShape} or {link Line2D}.
     */
    public void mousePressed(final MouseEvent event) throws ClassCastException {
        if (!event.isConsumed() && (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            final Component source = event.getComponent();
            if (source != null) {
                Shape model = getModel(event);
                if (model != null) {
                    isDragging = true;
                    ox = px = event.getX();
                    oy = py = event.getY();
                    if (model instanceof Line2D) {
                        model = null;
                    }
                    mouseSelectedArea = (RectangularShape) model;
                    if (mouseSelectedArea != null) {
                        mouseSelectedArea.setFrame(ox, oy, 0, 0);
                    }
                    source.addMouseMotionListener(this);
                }
                source.requestFocus();
                event.consume();
            }
        }
    }

    /**
     * Informs this controller that the mouse has been dragged.  The default
     * implementation uses this to move a corner of the rectangle used to
     * select the region. The other corner remains fixed at the point
     * where the mouse was at the moment its button was pressed..
     */
    public void mouseDragged(final MouseEvent event) {
        if (isDragging) {
            final Graphics2D graphics = getGraphics(event.getComponent());
            if (mouseSelectedArea == null) {
                graphics.drawLine(ox, oy, px, py);
                px = event.getX();
                py = event.getY();
                graphics.drawLine(ox, oy, px, py);
            } else {
                // 1. - draw last rectangle again to erase
                graphics.draw(mouseSelectedArea);
                
                int xmin = this.ox;
                int ymin = this.oy;
                int xmax = px = event.getX();
                int ymax = py = event.getY();
                if (xmin > xmax) {
                    final int xtmp = xmin;
                    xmin = xmax; xmax = xtmp;
                }
                if (ymin > ymax) {
                    final int ytmp = ymin;
                    ymin = ymax; ymax = ytmp;
                }
                
                
                // 2. - draw new rectangle 
                mouseSelectedArea.setFrame(xmin, ymin, xmax - xmin, ymax - ymin);                
                graphics.draw(mouseSelectedArea);
                
            }
            graphics.dispose();
            event.consume();
        }
    }

    /**
     * Informs this controller that the mouse button has been released.
     * The default implementation calls {@link #selectionPerformed} with
     * the bounds of the selected region as parameters.
     */
    public void mouseReleased(final MouseEvent event) {
        if (isDragging && (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            isDragging = false;
            final Component component = event.getComponent();
            component.removeMouseMotionListener(this);

            final Graphics2D graphics = getGraphics(event.getComponent());
            if (mouseSelectedArea == null) {
                graphics.drawLine(ox, oy, px, py);
            } else {
                graphics.draw(mouseSelectedArea);
            }
            graphics.dispose();
            px = event.getX();
            py = event.getY();
            selectionPerformed(ox, oy, px, py);
            event.consume();
        }
    }

    /**
     * Informs this controller that the mouse has been moved but not as a
     * result of the user selecting a region.  The default implementation
     * signals to the source component that <code>this</code> is no longer
     * interested in being informed about mouse movements.
     */
    public void mouseMoved(final MouseEvent event) {
        // Normally not necessary, but it seems that this "listener"
        // sometimes stays in place when it shouldn't.
        event.getComponent().removeMouseMotionListener(this);
    }
    
    
    public boolean isDragging() {
        return this.isDragging;
    }
}

