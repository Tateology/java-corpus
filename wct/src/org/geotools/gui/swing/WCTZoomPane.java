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
package org.geotools.gui.swing;

// Events and action
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoundedRangeModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import org.geotools.gui.swing.event.ZoomChangeEvent;
import org.geotools.gui.swing.event.ZoomChangeListener;
import org.geotools.renderer.DeformableViewer;
import org.geotools.resources.Utilities;
import org.geotools.resources.geometry.XAffineTransform;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.resources.gui.ResourceKeys;
import org.geotools.resources.gui.Resources;


/**
 * Base class for widget with a zoomable content. User can perform zooms using
 * keyboard, menu or mouse. <code>WCTZoomPane</code> is an abstract class.
 * Subclass must override at least two methods:
 *
 * <ul>
 *   <li>{@link #getArea()}, which must return a bounding box for the content
 *       to paint. This area can be expressed in arbitrary units. For example,
 *       an object wanting to display a geographic map with a content ranging
 *       from 10° to 15°E and 40° to 45°N should override this method as
 *       follows:
 *
 *       <pre>
 *       &nbsp;public Rectangle2D getArea() {
 *       &nbsp;    return new Rectangle2D.Double(10, 40, 5, 5);
 *       &nbsp;}
 *       </pre></li>
 *
 *   <li>{@link #paintComponent(Graphics2D)}, which must paint the widget
 *       content. Implementation must invoke
 *
 *                    <code>graphics.transform({link #zoom})</code>
 *
 *       somewhere in its code in order to perform the zoom. Note that, by
 *       default, the {@linkplain #zoom} is initialized in such a way that
 *       the <var>y</var> axis points upwards, like the convention in
 *       geometry. This is as opposed to the default Java2D axis orientation,
 *       where the <var>y</var> axis points downwards. If the implementation
 *       wants to paint text, it should do this with the default transform.
 *       Example:
 *
 *       <pre>
 *       &nbsp;protected void paintComponent(final Graphics2D graphics) {
 *       &nbsp;    graphics.clip({link #getZoomableBounds getZoomableBounds}(null));
 *       &nbsp;    final AffineTransform textTr = graphics.getTransform();
 *       &nbsp;    graphics.transform({link #zoom});
 *       &nbsp;    <strong>
 *       &nbsp;    // Paint the widget here, using logical coordinates. The
 *       &nbsp;    // coordinate system is the same as {@link #getArea()}'s one.
 *       &nbsp;    </strong>
 *       &nbsp;    graphics.setTransform(textTr);
 *       &nbsp;    <strong>
 *       &nbsp;    // Paint any text here, in <em>pixel</em> coordinates.
 *       &nbsp;    </strong>
 *       &nbsp;}
 *       </pre></li>
 * </ul>
 *
 * Subclass can also override {@link #reset}, which sets up the initial
 * {@linkplain #zoom}. The default implementation sets up the initial zoom
 * in such a way that the following relations are approximately held:
 *
 * <blockquote><cite>
 * Logical coordinates provided by {@link #getPreferredArea()},
 * after an affine transform described by {@link #zoom},
 * match pixel coordinates provided by {@link #getZoomableBounds(Rectangle)}.
 * </cite></blockquote>
 *
 * The "preferred area" is initially the same as {@link #getArea()}. The user
 * can specify a different preferred area with {@link #setPreferredArea}. The
 * user can also reduce zoomable bounds by inserting an empty border around the
 * widget,
 * e.g.:
 *
 * <pre>
 * &nbsp;setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
 * </pre>
 *
 * <p>&nbsp;</p>
 * <h2>Zoom actions</h2>
 * Whatever action is performed by the user, all zoom commands are translated
 * by calls to {@link #transform}. Derived classes can redefine this method
 * if they want to take particular actions during zooms, for example, modifying
 * the minimum and maximum of a graph's axes.  The table below shows the 
 * keyboard presses assigned to each zoom:
 *
 * <P><TABLE ALIGN="CENTER" BORDER="2">
 * <TR BGCOLOR="#CCCCFF"><TH>Key</TH>                      <TH>Purpose</TH>     <TH>{@link Action} name</TH></TR>
 * <TR><TD><IMG SRC="doc-files/keyboard/up.png"></TD>      <TD>Scroll up</TD>   <TD><code>"Up"</code></TD></TR>
 * <TR><TD><IMG SRC="doc-files/keyboard/down.png"></TD>    <TD>Scroll down</TD> <TD><code>"Down"</code></TD></TR>
 * <TR><TD><IMG SRC="doc-files/keyboard/left.png"></TD>    <TD>Scroll left</TD> <TD><code>"Left"</code></TD></TR>
 * <TR><TD><IMG SRC="doc-files/keyboard/right.png"></TD>   <TD>Scroll right</TD><TD><code>"Right"</code></TD></TR>
 * <TR><TD><IMG SRC="doc-files/keyboard/pageDown.png"></TD><TD>Zoom in</TD>     <TD><code>"ZoomIn"</code></TD></TR>
 * <TR><TD><IMG SRC="doc-files/keyboard/pageUp.png"></TD>  <TD>Zoom out</TD>    <TD><code>"ZoomOut"</code></TD></TR>
 * <TR><TD><IMG SRC="doc-files/keyboard/end.png"></TD>     <TD>Zoom</TD>        <TD><code>"Zoom"</code></TD></TR>
 * <TR><TD><IMG SRC="doc-files/keyboard/home.png"></TD>    <TD>Default zoom</TD><TD><code>"Reset"</code></TD></TR>
 *
 * <TR><TD>Ctrl+<IMG SRC="doc-files/keyboard/left.png"></TD> <TD>Anti-clockwise rotation</TD><TD><code>"RotateLeft"</code></TD></TR>
 * <TR><TD>Ctrl+<IMG SRC="doc-files/keyboard/right.png"></TD><TD>Clockwise rotation</TD>     <TD><code>"RotateRight"</code></TD></TR>
 * </TABLE></P>
 *
 * In this table, the last column gives the Strings by which the different
 * actions which manage the zooms. For example, to zoom in, we must write
 * <code>{@link #getActionMap() getActionMap()}.get("ZoomIn")</code>.
 *
 * <p><strong>Note: {@link JScrollPane} objects are not suitable for adding
 * scrollbars to a <code>WCTZoomPane</code>object.</strong>
 * Instead, use {@link #createScrollPane}. Once more, all movements performed
 * by the user through the scrollbars will be translated by calls to
 * {@link #transform}.</p>
 *
 * @version $Id: WCTZoomPane.java,v 1.16 2004/02/16 20:49:35 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class WCTZoomPane extends JComponent implements DeformableViewer {
    
    
    
    public static enum ZoomMode { ZOOMIN, ZOOMOUT, PAN }
    
    private ZoomMode currentZoomMode = ZoomMode.ZOOMIN;
    private Shape mouseSelectionShape = new Rectangle2D.Float();
    
    private WCTMouseSelectionTracker mouseListener = null;
    
    
    
    /**
     * Number of pixels in x and y needed to spawn a 'selectionPerformed' event.
     */
    private static final int MIN_NUM_PIXELS_FOR_SELECTION = 10;
    
    /**
     * Minimum width and height of this component.
     */
    private static final int MINIMUM_SIZE = 10;

    /**
     * Default width and height of this component.
     */
    private static final int DEFAULT_SIZE = 400;

    /**
     * Default width and height of the magnifying glass.
     */
    private static final int DEFAULT_MAGNIFIER_SIZE = 150;

    /**
     * Default color with which to tint magnifying glass.
     */
    private static final Paint DEFAULT_MAGNIFIER_GLASS = new Color(197, 204, 221);

    /**
     * Default color of the magnifying glass's border.
     */
    private static final Paint DEFAULT_MAGNIFIER_BORDER = new Color(102, 102, 153);

    /**
     * Constant indicating the scale changes on the <var>x</var> axis.
     */
    public static final int SCALE_X = (1 << 0);

    /**
     * Constant indicating the scale changes on the <var>y</var> axis.
     */
    public static final int SCALE_Y = (1 << 1);

    /**
     * Constant indicating the scale changes on the <var>x</var> and
     * <var>y</var> axes, with the added condition that these changes
     * must be uniform.  This flag combines {@link #SCALE_X} and
     * {@link #SCALE_Y}. The inverse, however, 
     * (<code>{@link #SCALE_X}|{@link #SCALE_Y}</code>) doesn't involve
     * <code>UNIFORM_SCALE</code>.
     */
    public static final int UNIFORM_SCALE = SCALE_X | SCALE_Y | (1 << 2);

    /**
     * Constant indicating the translations on the <var>x</var> axis.
     */
    public static final int TRANSLATE_X = (1 << 3);

    /**
     * Constant indicating the translations on the <var>y</var> axis.
     */
    public static final int TRANSLATE_Y = (1 << 4);

    /**
     * Constant indicating a rotation.
     */
    public static final int ROTATE  = (1 << 5);

    /**
     * Constant indicating the resetting of scale, rotation and translation
     * to a default value which makes the whole graphic appear in a window.
     * This command is translated by a call to {@link #reset}.
     */
    public static final int RESET = (1 << 6);

    /**
     * Constant indicating default zoom close to the maximum permitted zoom.
     * This zoom should allow details of the graphic to be seen without being
     * overly big.
     * Note: this flag will only have any effect if at least one of the
     * {@link #SCALE_X} and {@link #SCALE_Y} flags is not also specified.
     */
    public static final int DEFAULT_ZOOM = (1 << 7);

    /**
     * Mask representing the combination of all flags.
     */
    private static final int MASK = SCALE_X | SCALE_Y | UNIFORM_SCALE | TRANSLATE_X | TRANSLATE_Y | ROTATE | RESET | DEFAULT_ZOOM;

    /**
     * Number of pixels by which to move the content of
     * <code>WCTZoomPane</code> during translations.
     */
    private static final double AMOUNT_TRANSLATE = 10;

    /**
     * Zoom factor.  This factor must be greater than 1.
     */
    private static final double AMOUNT_SCALE = 1.03125;

    /**
     * Rotation angle.
     */
    private static final double AMOUNT_ROTATE = Math.PI / 90;

    /**
     * Factor by which to multiply the {@link #ACTION_AMOUNT} numbers
     * when the "Shift" key is kept pressed.
     */
    private static final double ENHANCEMENT_FACTOR = 7.5;

    /**
     * Flag indicating that a paint is in progress.
     */
    private static final int IS_PAINTING = 0;

    /**
     * Flag indicating that a paint of the magnifying glass is in progress.
     */
    private static final int IS_PAINTING_MAGNIFIER = 1;

    /**
     * Flat indicating that a print is in progress.
     */
    private static final int IS_PRINTING = 2;

    /**
     * List of keys which will identify the zoom actions.
     * These keys also identify the resources to use in order to make the 
     * description appear in the user's language.
     */
    private static final String[] ACTION_ID = {
        /*[0] Left        */ "Left",
        /*[1] Right       */ "Right",
        /*[2] Up          */ "Up",
        /*[3] Down        */ "Down",
        /*[4] ZoomIn      */ "ZoomIn",
        /*[5] ZoomOut     */ "ZoomOut",
        /*[6] ZoomMax     */ "ZoomMax",
        /*[7] Reset       */ "Reset",
        /*[8] RotateLeft  */ "RotateLeft",
        /*[9] RotateRight */ "RotateRight"
    };

    /**
     * List of resource keys, to construct the menus
     * in the user's language.
     */
    private static final int[] RESOURCE_ID = {
        /*[0] Left        */ ResourceKeys.LEFT,
        /*[1] Right       */ ResourceKeys.RIGHT,
        /*[2] Up          */ ResourceKeys.UP,
        /*[3] Down        */ ResourceKeys.DOWN,
        /*[4] ZoomIn      */ ResourceKeys.ZOOM_IN,
        /*[5] ZoomOut     */ ResourceKeys.ZOOM_OUT,
        /*[6] ZoomMax     */ ResourceKeys.ZOOM_MAX,
        /*[7] Reset       */ ResourceKeys.RESET,
        /*[8] RotateLeft  */ ResourceKeys.ROTATE_LEFT,
        /*[9] RotateRight */ ResourceKeys.ROTATE_RIGHT
    };

    /**
     * The logger for zoom events.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.gui.swing");

    /**
     * List of default keystrokes used to perform zooms.
     * The elements of this table go in pairs.  The even indexes indicate
     * the keystroke whilst the odd indexes indicate the modifier
     * (CTRL or SHIFT for example). To obtain the {@link KeyStroke} object
     * for a numbered action <var>i</var>, we can use the following code:
     * 
     * <blockquote><pre>
     * final int key=DEFAULT_KEYBOARD[(i << 1)+0];
     * final int mdf=DEFAULT_KEYBOARD[(i << 1)+1];
     * KeyStroke stroke=KeyStroke.getKeyStroke(key, mdf);
     * </pre></blockquote>
     */
    private static final int[] ACTION_KEY = {
        /*[0] Left        */ KeyEvent.VK_LEFT,      0,
        /*[1] Right       */ KeyEvent.VK_RIGHT,     0,
        /*[2] Up          */ KeyEvent.VK_UP,        0,
        /*[3] Down        */ KeyEvent.VK_DOWN,      0,
        /*[4] ZoomIn      */ KeyEvent.VK_PAGE_UP,   0,
        /*[5] ZoomOut     */ KeyEvent.VK_PAGE_DOWN, 0,
        /*[6] ZoomMax     */ KeyEvent.VK_END,       0,
        /*[7] Reset       */ KeyEvent.VK_HOME,      0,
        /*[8] RotateLeft  */ KeyEvent.VK_LEFT,      KeyEvent.CTRL_MASK,
        /*[9] RotateRight */ KeyEvent.VK_RIGHT,     KeyEvent.CTRL_MASK
    };

    /**
     * Connstants indicating the type of action to perform:
     * translation, zoom or rotation.
     */
    private static final short[] ACTION_TYPE = {
        /*[0] Left        */ (short) TRANSLATE_X,
        /*[1] Right       */ (short) TRANSLATE_X,
        /*[2] Up          */ (short) TRANSLATE_Y,
        /*[3] Down        */ (short) TRANSLATE_Y,
        /*[4] ZoomIn      */ (short) SCALE_X | SCALE_Y,
        /*[5] ZoomOut     */ (short) SCALE_X | SCALE_Y,
        /*[6] ZoomMax     */ (short) DEFAULT_ZOOM,
        /*[7] Reset       */ (short) RESET,
        /*[8] RotateLeft  */ (short) ROTATE,
        /*[9] RotateRight */ (short) ROTATE
    };

    /**
     * Amounts by which to translate, zoom or rotate
     * the contents of the window.
     */
    private static final double[] ACTION_AMOUNT = {
        /*[0] Left        */  +AMOUNT_TRANSLATE,
        /*[1] Right       */  -AMOUNT_TRANSLATE,
        /*[2] Up          */  +AMOUNT_TRANSLATE,
        /*[3] Down        */  -AMOUNT_TRANSLATE,
        /*[4] ZoomIn      */   AMOUNT_SCALE,
        /*[5] ZoomOut     */ 1/AMOUNT_SCALE,
        /*[6] ZoomMax     */   Double.NaN,
        /*[7] Reset       */   Double.NaN,
        /*[8] RotateLeft  */  -AMOUNT_ROTATE,
        /*[9] RotateRight */  +AMOUNT_ROTATE
    };

    /**
     * List of operation types forming a group.  During creation of the
     * menus, the different groups will be separated by a menu separator.
     */
    private static final int[] GROUP = {
        TRANSLATE_X | TRANSLATE_Y,
        SCALE_X | SCALE_Y | DEFAULT_ZOOM | RESET,
        ROTATE
    };

    /**
     * <code>ComponentUI</code> object in charge of obtaining the preferred
     * size of a <code>WCTZoomPane</code> object as well as drawing it.
     */
    private static final ComponentUI UI = new ComponentUI() {
        /**
         * Returns a default minimum size.
         */
        public Dimension getMinimumSize(final JComponent c) {
            return new Dimension(MINIMUM_SIZE, MINIMUM_SIZE);
        }

        /**
         * Returns the maximum size. We use the preferred
         * size as a default maximum size.
         */
        public Dimension getMaximumSize(final JComponent c) {
            return getPreferredSize(c);
        }

        /**
         * Returns the default preferred size. User can override this
         * preferred size by invoking {@link JComponent#setPreferredSize}.
         */
        public Dimension getPreferredSize(final JComponent c) {
            return ((WCTZoomPane) c).getDefaultSize();
        }

        /**
         * Override {@link ComponentUI#update} in order to handle painting of
         * magnifying glass, which is a special case. Since the magnifying
         * glass is painted just after the normal component, we don't want to
         * clear the background before painting it.
         */
        public void update(final Graphics g, final JComponent c) {
            switch (((WCTZoomPane) c).flag) {
                case IS_PAINTING_MAGNIFIER: paint(g, c); break; // Avoid background clearing
                default:             super.update(g, c); break;
            }
        }

        /**
         * Paint the component. This method basically delegates the
         * work to {@link WCTZoomPane#paintComponent(Graphics2D)}.
         */
        public void paint(final Graphics g, final JComponent c) {
            final WCTZoomPane pane = (WCTZoomPane)   c;
            final Graphics2D gr = (Graphics2D) g;
            switch (pane.flag) {
                case IS_PAINTING:           pane.paintComponent(gr); break;
                case IS_PAINTING_MAGNIFIER: pane.paintMagnifier(gr); break;
                case IS_PRINTING:           pane.printComponent(gr); break;
                default: throw new IllegalStateException(Integer.toString(pane.flag));
            }
        }
    };

    /**
     * Object in charge of drawing a box representing the user's selection.  We
     * retain a reference to this object in order to be able to register it and
     * extract it at will from the list of objects interested in being notified
     * of the mouse movements.
     */
    private final WCTMouseSelectionTracker mouseSelectionTracker = new WCTMouseSelectionTracker()
    {
        /**
         * Returns the selection shape. This is usually a rectangle, but could
         * very well be an ellipse or any other kind of geometric shape. This
         * method asks {@link WCTZoomPane#getMouseSelectionShape} for the shape.
         */
        protected Shape getModel(final MouseEvent event) {
            final Point2D point = new Point2D.Double(event.getX(), event.getY());
            if (getZoomableBounds().contains(point)) try {
                return getMouseSelectionShape(zoom.inverseTransform(point, point));
            } catch (NoninvertibleTransformException exception) {
                unexpectedException("getModel", exception);
            }
            return null;
        }

        /**
         * Invoked when the user finishes the selection. This method will
         * delegate the action to {@link WCTZoomPane#mouseSelectionPerformed}.
         * Default implementation will perform a zoom.
         */
        protected void selectionPerformed(int ox, int oy, int px, int py) {
            try {
                
//                System.out.println(ox+","+oy+" | "+px+","+py);
                // ignore if the selection is 'accidental', i.e. very, very small
                if (Math.abs(ox-px) <= MIN_NUM_PIXELS_FOR_SELECTION || Math.abs(oy-ox) <= MIN_NUM_PIXELS_FOR_SELECTION) {
                    return;
                }
                
                
                final Shape selection = getSelectedArea(zoom);
                if (selection != null) {
                    mouseSelectionPerformed(selection);
                }
            } catch (NoninvertibleTransformException exception) {
                unexpectedException("selectionPerformed", exception);
            }
        }
    };

    /**
     * Class responsible for listening out for the different events necessary
     * for the smooth working of {@link WCTZoomPane}. This class will listen out
     * for mouse clicks (in order to eventually claim the focus or make a
     * contextual menu appear).  It will listen out for changes in the size
     * of the component (to adjust the zoom), etc.
     *
     * @version $Id: WCTZoomPane.java,v 1.16 2004/02/16 20:49:35 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class Listeners extends MouseAdapter implements MouseWheelListener,
                                                                  ComponentListener, Serializable
    {
        public void mouseWheelMoved (final MouseWheelEvent event) {WCTZoomPane.this.mouseWheelMoved (event);}
        public void mousePressed    (final MouseEvent      event) {WCTZoomPane.this.mayShowPopupMenu(event);}
        public void mouseReleased   (final MouseEvent      event) {WCTZoomPane.this.mayShowPopupMenu(event);}
        public void componentResized(final ComponentEvent  event) {WCTZoomPane.this.processSizeEvent(event);}
        public void componentMoved  (final ComponentEvent  event) {}
        public void componentShown  (final ComponentEvent  event) {}
        public void componentHidden (final ComponentEvent  event) {}
    }

    /**
     * Affine transform containing zoom factors, translations and rotations.
     * During the painting of a component, this affine transform should be
     * combined with a call to 
     * <code>{@link Graphics2D#transform(AffineTransform) Graphics2D.transform}(zoom)</code>.
     */
    protected final AffineTransform zoom = new AffineTransform();

    /**
     * Indicates whether the zoom is the result of a {@link #reset} operation.
     */
    private boolean zoomIsReset;

    /**
     * Types of zoom permitted.  This field should be a combination of the
     * constants {@link #SCALE_X}, {@link #SCALE_Y}, {@link #TRANSLATE_X},
     * {@link #TRANSLATE_Y}, {@link #ROTATE}, {@link #RESET} and
     * {@link #DEFAULT_ZOOM}.
     */
    private final int type;

    /**
     * Strategy to follow in order to calculate the initial affine transform.
     * The value <code>true</code> indicates that the content should fill the
     * entire panel, even if it means losing some of the edges. The value
     * <code>false</code> indicates, on the contrary, that we should display
     * the entire contents, even if it means leaving blank spaces in the panel.
     */
    private boolean fillPanel = false;

    /**
     * Rectangle representing the logical coordinates of the visible region.
     * This information is used to keep the same region when the size or 
     * position of the component changes.  Initially, this rectangle is empty.
     * It will only stop being empty if {@link #reset} is called and 
     * {@link #getPreferredArea} and {@link #getZoomableBounds} have both 
     * returned valid coordinates.
     *
     * @see #getVisibleArea
     * @see #setVisibleArea
     */
    private final Rectangle2D visibleArea = new Rectangle2D.Double();

    /**
     * Rectangle representing the logical coordinates of the region to 
     * display initially, the first time that the window is displayed.
     * The value <code>null</code> indicates a call to {@link #getArea}.
     *
     * @see #getPreferredArea
     * @see #setPreferredArea
     */
    private Rectangle2D preferredArea;

    /**
     * Menu to display when the user right clicks with their mouse.  This menu
     * will contain the navigation options.
     *
     * @see #getPopupMenu
     */
    private transient PointPopupMenu navigationPopupMenu;

    /**
     * Flag indicating which part of the paint is in progress.  The permitted
     * values are {@link #IS_PAINTING}, {@link #IS_PAINTING_MAGNIFIER} and
     * {@link #IS_PRINTING}.
     */
    private transient int flag;

    /**
     * Indicates if this <code>WCTZoomPane</code> object should be repainted when
     * the user adjusts the scrollbars.  The default value is <code>false</code>,
     * which means that <code>WCTZoomPane</code> will wait until the user has 
     * released the scrollbar before repainting the component.
     *
     * @see #isPaintingWhileAdjusting
     * @see #setPaintingWhileAdjusting
     */
    private boolean paintingWhileAdjusting;

    /**
     * Rectangle in which to place the coordinates returned by
     * {@link #getZoomableBounds}. This object is defined in order to avoid
     * allocating objects too often {@link Rectangle}.
     */
    private transient Rectangle cachedBounds;

    /**
     * Object in which to record the result of {@link #getInsets}.
     * Used in order to avoid {@link #getZoomableBounds} allocating
     * {@link Insets} objects too often.
     */
    private transient Insets cachedInsets;

    /**
     * Indicates whether the user is authorised to display
     * the magnifying glass.  The default value is <code>true</code>.
     */
    private boolean magnifierEnabled = true;

    /**
     * Magnification factor inside the magnifying glass.
     * This factor must be greater than 1.
     */
    private double magnifierPower = 4;

    /**
     * Geometric shape in which to magnify.  The coordinates of this
     * shape should be expressed in pixels.  The value <code>null</code>
     * means that no magnifying glass will be drawn.
     */
    private transient MouseReshapeTracker magnifier;

    /**
     * Colour with which to tint magnifying glass.
     */
    private Paint magnifierGlass = DEFAULT_MAGNIFIER_GLASS;

    /**
     * Colour of the magnifying glass's border.
     */
    private Paint magnifierBorder = DEFAULT_MAGNIFIER_BORDER;

    /**
     * Construct a <code>WCTZoomPane</code>.
     *
     * @param  type Allowed zoom type. It can be a bitwise combination of the
     *         following constants:
     *             {@link #SCALE_X}, {@link #SCALE_Y}, {@link #UNIFORM_SCALE},
     *             {@link #TRANSLATE_X}, {@link #TRANSLATE_Y},
     *             {@link #ROTATE}, {@link #RESET} and {@link #DEFAULT_ZOOM}.
     * @throws IllegalArgumentException If <code>type</code> is invalid.
     */
    public WCTZoomPane(final int type) throws IllegalArgumentException {
        if ((type & ~MASK) != 0) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        final Resources resources = Resources.getResources(null);
        final InputMap   inputMap = getInputMap();
        final ActionMap actionMap = getActionMap();
        for (int i = 0; i < ACTION_ID.length; i++) {
            final short actionType = ACTION_TYPE[i];
            if ((actionType & type) != 0) {
                final String  actionID = ACTION_ID[i];
                final double    amount = ACTION_AMOUNT[i];
                final int     keyboard = ACTION_KEY[(i << 1) + 0];
                final int     modifier = ACTION_KEY[(i << 1) + 1];
                final KeyStroke stroke = KeyStroke.getKeyStroke(keyboard, modifier);
                final Action    action = new AbstractAction() {
                    /*
                     * Action to perform when a key has been hit
                     * or the mouse clicked.
                     */
                    public void actionPerformed(final ActionEvent event) {
                        Point          point = null;
                        final Object  source = event.getSource();
                        final boolean button = (source instanceof AbstractButton);
                        if (button) {
                            for (Container c = (Container) source; c != null; c = c.getParent()) {
                                if (c instanceof PointPopupMenu) {
                                    point = ((PointPopupMenu) c).point;
                                    break;
                                }
                            }
                        }
                        double m = amount;
                        if (button || (event.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                            if ((actionType & UNIFORM_SCALE) != 0) {
                                m = (m >= 1) ? 2.0 : 0.5;
                            }
                            else {
                                m *= ENHANCEMENT_FACTOR;
                            }
                        }
                        transform(actionType & type, m, point);
                    }
                };
                action.putValue(Action.NAME,               resources.getString(RESOURCE_ID[i]));
                action.putValue(Action.ACTION_COMMAND_KEY, actionID);
                action.putValue(Action.ACCELERATOR_KEY,    stroke);
                actionMap.put(actionID, action);
                inputMap .put(stroke, actionID);
                inputMap .put(KeyStroke.getKeyStroke(keyboard, modifier | KeyEvent.SHIFT_MASK), actionID);
            }
        }
        /*
         * Adds an object which will be in charge of listening
         * for mouse clicks in order to display a contextual
         * menu, as well as an object which will be in charge of
         * listening for mouse movements in order to perform
         * zooms.
         */
        final Listeners listeners = new Listeners();
        addComponentListener  (listeners);
        super.addMouseListener(listeners);
        if ((type & (SCALE_X | SCALE_Y)) != 0) {
            super.addMouseWheelListener(listeners);
        }
        
        this.mouseListener = mouseSelectionTracker;
        
        super.addMouseListener(mouseSelectionTracker);
        setAutoscrolls(true);
        setFocusable(true);
        setOpaque(true);
        setUI(UI);
        
        
        setDoubleBuffered(true);
    }

    /**
     * Reinitializes the affine transform {@link #zoom} in order to cancel
     * any zoom, rotation or translation.  The default implementation 
     * initializes the affine transform {@link #zoom} in order to make the
     * <var>y</var> axis point upwards and make the whole of the region covered
     * by the {@link #getPreferredArea} logical coordinates appear in the panel.
     *<br><br>
     * Note: for the derived classes: <code>reset()</code> is <u>the only</u>
     * method of <code>WCTZoomPane</code> which doesn't have to pass through
     * {@link #transform(AffineTransform)} to modify the zoom.  This exception
     * is necessary to avoid falling into an infinite loop.
     */
    public void reset() {
        reset(getZoomableBounds(), true);
    }

    /**
     * Reinitializes the affine transform {@link #zoom} in order to cancel
     * any zoom, rotation or translation. The argument <code>yAxisUpward</code>
     * indicates whether the <var>y</var> axis should point upwards.  The value
     * <code>false</code> lets it point downwards. This method is offered
     * for convenience sake for derived classes which want to redefine
     * {@link #reset()}.
     *
     * @param zoomableBounds Coordinates, in pixels, of the screen space in
     *                       which to draw.  This argument will usually be
     *                       <code>{@link #getZoomableBounds(Rectangle)
     *                                     getZoomableBounds}(null)</code>.
     * @param yAxisUpward    <code>true</code> if the <var>y</var> axis should
     *                       point upwards rather than downwards.
     */
    protected final void reset(final Rectangle zoomableBounds,
                               final boolean yAxisUpward) {
        if (!zoomableBounds.isEmpty()) {
            final Rectangle2D preferredArea = getPreferredArea();
            if (isValid(preferredArea)) {
                final AffineTransform change;
                try {
                    change = zoom.createInverse();
                } catch (NoninvertibleTransformException exception) {
                    unexpectedException("reset", exception);
                    return;
                }
                if (yAxisUpward) {
                    zoom.setToScale(+1, -1);
                }
                else {
                    zoom.setToIdentity();
                }
                final AffineTransform transform = setVisibleArea(preferredArea, zoomableBounds,
                                                  SCALE_X | SCALE_Y | TRANSLATE_X | TRANSLATE_Y);
                change.concatenate(zoom);
                zoom  .concatenate(transform);
                change.concatenate(transform);
                getVisibleArea(zoomableBounds); // Force update of 'visibleArea'
                /*
                 * The three private versions 'fireZoomPane0', 'getVisibleArea'
                 * and 'setVisibleArea' avoid calling other methods of ZoomPane
                 * so as not to end up in an infinite loop.
                 */
                if (!change.isIdentity()) {
                    fireZoomChanged0(change);
                    repaint(zoomableBounds);
                }
                zoomIsReset = true;
                log("reset", visibleArea);
            }
        }
    }

    /**
     * Set the policy for the zoom when the content is initially drawn
     * or when the user resets the zoom. Value <code>true</code> means that
     * the panel should initially be completely filled, even if the content
     * partially falls outside the panel's bounds. Value <code>false</code>
     * means that the full content should appear in the panel, even if some
     * space is not used. Default value is <code>false</code>.
     */
    protected void setResetPolicy(final boolean fill) {
        fillPanel = fill;
    }

    /**
     * Returns a bounding box that contains the logical coordinates of all
     * data that may be displayed in this <code>WCTZoomPane</code>. For example,
     * if this <code>WCTZoomPane</code> is to display a geographic map, then
     * this method should return the map's bounds in degrees of latitude
     * and longitude. This bounding box is completely independent of any
     * current zoom setting and will change only if the content changes.
     *
     * @return A bounding box for the logical coordinates of all contents
     *         that are going to be drawn in this <code>WCTZoomPane</code>. If
     *         this bounding box is unknown, then this method can return
     *         <code>null</code> (but this is not recommended).
     */
    public abstract Rectangle2D getArea();

    /**
     * Indicates whether the logical coordinates of a region have been defined.
     * This method returns <code>true</code> if {@link #setPreferredArea} has
     * been called with a non null argument.
     */
    public final boolean hasPreferredArea() {
        return preferredArea != null;
    }

    /**
     * Returns the logical coordinates of the region that we want to see
     * displayed the first time that <code>WCTZoomPane</code> appears on the
     * screen.  This region will also be displayed each time the method
     * {link #reset} is called. The default implementation goes as follows:
     *
     * <ul>
     *   <li>If a region has already been defined by a call to
     *       {@link #setPreferredArea}, this region will be returned.</li>
     *   <li>If not, the whole region {@link #getArea} will be returned.</li>
     * </ul>
     *
     * @return The logical coordinates of the region to be initially displayed,
     *         or <code>null</code> if these coordinates are unknown.
     */
    public final Rectangle2D getPreferredArea() {
        return (preferredArea != null) ? (Rectangle2D) preferredArea.clone() : getArea();
    }

    /**
     * Specifies the logical coordinates of the region that we want to see
     * displayed the first time that <code>WCTZoomPane</code> appears on the screen.
     * This region will also be displayed the first time that the method
     * {link #reset} is called.
     */
    public final void setPreferredArea(final Rectangle2D area) {
        if (area != null) {
            if (isValid(area)) {
                final Object oldArea;
                if (preferredArea == null) {
                    oldArea = null;
                    preferredArea = new Rectangle2D.Double();
                }
                else oldArea = preferredArea.clone();
                preferredArea.setRect(area);
                firePropertyChange("preferredArea", oldArea, area);
                log("setPreferredArea", area);
            } else {
                throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_RECTANGLE_$1, area));
            }
        }
        else preferredArea = null;
    }

    /**
     * Returns the logical coordinates of the region visible on the screen.
     * In the case of a geographic map, for example, the logical coordinates
     * can be expressed in degrees of latitude/longitude or even in metres
     * if a cartographic projection has been defined.
     */
    public final Rectangle2D getVisibleArea() {
        return getVisibleArea(getZoomableBounds());
    }

    /**
     * Implementation of {@link #getVisibleArea()}.
     */
    private Rectangle2D getVisibleArea(final Rectangle zoomableBounds) {
        if (zoomableBounds.isEmpty()) {
            return (Rectangle2D) visibleArea.clone();
        }
        Rectangle2D visible;
        try {
            visible = XAffineTransform.inverseTransform(zoom, zoomableBounds, null);
        } catch (NoninvertibleTransformException exception) {
            unexpectedException("getVisibleArea", exception);
            visible = new Rectangle2D.Double(zoomableBounds.getCenterX(),
                                             zoomableBounds.getCenterY(), 0, 0);
        }
        visibleArea.setRect(visible);
        return visible;
    }

    /**
     * Defines the limits of the visible part, in logical coordinates.  This
     * method will modify the zoom and the translation in order to display the
     * specified region. If {@link #zoom} contains a rotation, this rotation
     * will not be modified.
     *
     * @param  logicalBounds Logical coordinates of the region to be displayed.
     * @throws IllegalArgumentException if <code>source</code> is empty.
     */
    public void setVisibleArea(final Rectangle2D logicalBounds) {
    	setVisibleArea(logicalBounds, true);
    }

    
    /**
     * Defines the limits of the visible part, in logical coordinates.  This
     * method will modify the zoom and the translation in order to display the
     * specified region. If {@link #zoom} contains a rotation, this rotation
     * will not be modified.
     *
     * @param  logicalBounds Logical coordinates of the region to be displayed.
     * @param  fireEvent should an zoom change event be fired?
     * @throws IllegalArgumentException if <code>source</code> is empty.
     */
    public void setVisibleArea(final Rectangle2D logicalBounds, final boolean fireEvent) 
           throws IllegalArgumentException {
        log("setVisibleArea", logicalBounds);
        transform(setVisibleArea(logicalBounds, getZoomableBounds(), 0), fireEvent);
    }

    /**
     * Defines the limits of the visible part, in logical coordinates.  This
     * method will modify the zoom and the translation in order to display the
     * specified region.  If {@link #zoom} contains a rotation, this rotation
     * will not be modified.
     *
     * @param  source Logical coordinates of the region to be displayed.
     * @param  dest Pixel coordinates of the region of the window in which to 
     *         draw (normally {@link #getZoomableBounds()}).
     * @param  mask A mask to <code>OR</code> with the {@link #type} for determining which
     *         kind of transformation are allowed. The {@link #type} is not modified.
     * @return Change to apply to the affine transform {@link #zoom}.
     * @throws IllegalArgumentException if <code>source</code> is empty.
     */
    private AffineTransform setVisibleArea(Rectangle2D source, Rectangle2D dest, int mask)
                                           throws IllegalArgumentException {
        /*
         * Verifies the validity of the rectangle <code>source</code>. An
         * invalid rectangle will be rejected. However, we will be more
         * flexible for <code>dest</code> since the window could have been
         * reduced by the user.
         */
        if (!isValid(source)) {
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_BAD_RECTANGLE_$1, source));
        }
        if (!isValid(dest)) {
            return new AffineTransform();
        }
        /*
         * Converts the destination into logical coordinates.  We can
         * then perform a zoom and a translation which would put
         * <code>source</code> in <code>dest</code>.
         */
        try {
            dest = XAffineTransform.inverseTransform(zoom, dest, null);
        } catch (NoninvertibleTransformException exception) {
            unexpectedException("setVisibleArea", exception);
            return new AffineTransform();
        }
        final double sourceWidth  = source.getWidth ();
        final double sourceHeight = source.getHeight();
        final double   destWidth  =   dest.getWidth ();
        final double   destHeight =   dest.getHeight();
              double           sx = destWidth / sourceWidth;
              double           sy = destHeight / sourceHeight;
        /*
         * Standardizes the horizontal and vertical scales,
         * if such a standardization has been requested.
         */
        mask |= type;
        if ((mask & UNIFORM_SCALE) == UNIFORM_SCALE) {
            if (fillPanel) {
                if (sy * sourceWidth  > destWidth ) {
                    sx = sy;
                } else if (sx * sourceHeight > destHeight) {
                    sy = sx;
                }
            } else {
                if (sy * sourceWidth  < destWidth ) {
                    sx = sy;
                } else if (sx * sourceHeight < destHeight) {
                    sy = sx;
                }
            }
        }
        final AffineTransform change = AffineTransform.getTranslateInstance(
                         (mask & TRANSLATE_X) != 0 ? dest.getCenterX()    : 0,
                         (mask & TRANSLATE_Y) != 0 ? dest.getCenterY()    : 0);
        change.scale    ((mask & SCALE_X    ) != 0 ? sx                   : 1,
                         (mask & SCALE_Y    ) != 0 ? sy                   : 1);
        change.translate((mask & TRANSLATE_X) != 0 ? -source.getCenterX() : 0,
                         (mask & TRANSLATE_Y) != 0 ? -source.getCenterY() : 0);
        XAffineTransform.round(change);
        return change;
    }

    /**
     * Returns the bounding box (in pixel coordinates) of the zoomable area.
     * <strong>For performance reasons, this method reuses an internal cache.
     * Never modify the returned rectangle!</strong>. This internal method
     * is invoked by every method looking for this <code>WCTZoomPane</code>
     * dimension.
     *
     * @return The bounding box of the zoomable area, in pixel coordinates
     *         relative to this <code>WCTZoomPane</code> widget. <strong>Do not
     *         change the returned rectangle!</strong>
     */
    private final Rectangle getZoomableBounds() {
        return cachedBounds = getZoomableBounds(cachedBounds);
    }
    
    /**
     * Returns the bounding box (in pixel coordinates) of the zoomable area.
     * This method is similar to {@link #getBounds(Rectangle)}, except that
     * the zoomable area may be smaller than the whole widget area.
     * For example, a chart needs to keep some space for axes around the
     * zoomable area. Another difference is that pixel coordinates are
     * relative to the widget, i.e. the (0,0) coordinate lies on the
     * <code>WCTZoomPane</code> upper left corner, no matter what its location
     * on screen.<br>
     * <br>
     * <code>WCTZoomPane</code> invokes <code>getZoomableBounds</code> when it
     * needs to set up an initial {@link #zoom} value. Subclasses should also
     * set the clip area to this bounding box in their
     * {@link #paintComponent(Graphics2D)} method <em>before</em> setting the
     * graphics transform. For example:
     *
     * <blockquote><pre>
     * graphics.clip(getZoomableBounds(null));
     * graphics.transform({@link #zoom});
     * </pre></blockquote>
     *
     * @param  bounds An optional pre-allocated rectangle, or <code>null</code>
     *                to create a new one. This argument is useful if the caller
     *                wants to avoid allocating a new object on the heap.
     * @return The bounding box of the zoomable area, in pixel coordinates
     *         relative to this <code>WCTZoomPane</code> widget.
     */
    protected Rectangle getZoomableBounds(Rectangle bounds) {
        Insets insets;
        bounds = getBounds(bounds); insets=cachedInsets;
        insets = getInsets(insets); cachedInsets=insets;
        if (bounds.isEmpty()) {
            final Dimension size = getPreferredSize();
            bounds.width  = size.width;
            bounds.height = size.height;
        }
        bounds.x       =  insets.left;
        bounds.y       =  insets.top;
        bounds.width  -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);
        return bounds;
    }

    /**
     * Returns the default size for this component.  This is the size
     * returned by {@link #getPreferredSize} if no preferred size has
     * been explicitly set with {@link #setPreferredSize}.
     *
     * @return The default size for this component.
     */
    protected Dimension getDefaultSize() {
        return getViewSize();
    }

    /**
     * Returns the preferred pixel size for a close zoom. For image rendering,
     * the preferred pixel size is the image's pixel size in logical units. For
     * other kinds of rendering, this "pixel" size should be some reasonable
     * resolution. The default implementation computes a default value from
     * {@link #getArea}.
     */
    protected Dimension2D getPreferredPixelSize() {
        final Rectangle2D area = getArea();
        if (isValid(area)) {
            return new XDimension2D.Double(area.getWidth () / (10 * getWidth ()),
                                           area.getHeight() / (10 * getHeight()));
        }
        else {
            return new Dimension(1, 1);
        }
    }

    /**
     * Returns the current {@linkplain #zoom} scale factor. A value of
     * 1/100 means that 100 metres are displayed as 1 pixel (provided
     * that the logical coordinates of {@link #getArea} are expressed
     * in metres). Scale factors for X and Y axes can be computed
     * separately using the following equations:
     *
     * <table cellspacing=3><tr>
     * <td width=50%><IMG src="doc-files/scaleX.png"></td>
     * <td width=50%><IMG src="doc-files/scaleY.png"></td>
     * </tr></table>
     *
     * This method combines scale along both axes, which is correct
     * if this <code>WCTZoomPane</code> has been constructed with the
     * {@link #UNIFORM_SCALE} type.
     */
    public double getScaleFactor() {
        final double m00 = zoom.getScaleX();
        final double m11 = zoom.getScaleY();
        final double m01 = zoom.getShearX();
        final double m10 = zoom.getShearY();
        return Math.sqrt(m00 * m00 + m11 * m11 + m01 * m01 + m10 * m10);
    }

    /**
     * Changes the {@linkplain #zoom} by applying an affine transform. The
     * <code>change</code> transform must express a change in logical units,
     * for example, a translation in metres. This method is conceptually
     * similar to the following code:
     *
     * <pre>
     * {@link #zoom}.{@link AffineTransform#concatenate(AffineTransform) concatenate}(change);
     * {@link #fireZoomChanged(AffineTransform) fireZoomChanged}(change);
     * {@link #repaint() repaint}({@link #getZoomableBounds getZoomableBounds}(null));
     * </pre>
     *
     * @param  change The zoom change, as an affine transform in logical
     *         coordinates. If <code>change</code> is the identity transform,
     *         then this method does nothing and listeners are not notified.
     */
    public void transform(final AffineTransform change) {
    	transform(change, true);
    }
    
    /**
     * Changes the {@linkplain #zoom} by applying an affine transform. The
     * <code>change</code> transform must express a change in logical units,
     * for example, a translation in metres. This method is conceptually
     * similar to the following code:
     *
     * <pre>
     * {@link #zoom}.{@link AffineTransform#concatenate(AffineTransform) concatenate}(change);
     * {@link #fireZoomChanged(AffineTransform) fireZoomChanged}(change);
     * {@link #repaint() repaint}({@link #getZoomableBounds getZoomableBounds}(null));
     * </pre>
     *
     * @param  change The zoom change, as an affine transform in logical
     *         coordinates. If <code>change</code> is the identity transform,
     *         then this method does nothing and listeners are not notified.
     * @param fireEvent should an zoom change event be fired?
     */
    public void transform(final AffineTransform change, final boolean fireEvent) {
        if (!change.isIdentity()) {
            zoom.concatenate(change);
            XAffineTransform.round(zoom);
            if (fireEvent) {
            	fireZoomChanged(change);
            }
            repaint(getZoomableBounds());
            zoomIsReset = false;
        }
    }

    /**
     * Carries out a zoom, a translation or a rotation on the contents of
     * <code>WCTZoomPane</code>. The type of operation to carry out depends on the
     * <code>operation</code> argument:
     *
     * <ul>
     *   <li>{@link #TRANSLATE_X} carries out a translation along the
     *       <var>x</var> axis. The <code>amount</code> argument specifies the
     *       transformation to perform in number of pixels. A negative value
     *       moves to the left whilst a positive value moves to the right.</li>
     *   <li>{@link #TRANSLATE_Y} carries out a translation along the 
     *       <var>y</var> axis. The <code>amount</code> argument specifies the
     *       transformation to perform in number of pixels. A negative value
     *       moves upwards whilst a positive value moves downwards.</li>
     *   <li>{@link #UNIFORM_SCALE} carries out a zoom. The <code>zoom</code>
     *       argument specifies the type of zoom to perform. A value greater
     *       than 1 will perform a zoom in whilst a value between 0 and 1 will
     *       perform a zoom out.</li>
     *   <li>{@link #ROTATE} carries out a rotation. The <code>zoom</code>
     *       argument specifies the rotation angle in radians.</li>
     *   <li>{@link #RESET} Redefines the zoom to a default scale, rotation
     *       and translation. This operation displays all, or almost all, the
     *       contents of <code>WCTZoomPane</code>.</li>
     *   <li>{@link #DEFAULT_ZOOM} Carries out a default zoom, close to the
     *       maximum zoom, which shows the details of the contents of 
     *       <code>WCTZoomPane</code> but without enlarging them too much.</li>
     * </ul>
     *
     * @param  operation Type of operation to perform.
     * @param  amount ({@link #TRANSLATE_X} and
     *         {@link #TRANSLATE_Y}) translation in pixels, ({@link #SCALE_X}
     *         and {@link #SCALE_Y}) scale factor or ({@link #ROTATE}) rotation
     *         angle in radians.
     *         In other cases, this argument is ignored and can be 
     *         {@link Double#NaN}.
     * @param  center Zoom centre ({@link #SCALE_X} and {@link #SCALE_Y}) or
     *         rotation centre ({@link #ROTATE}), in pixel coordinates. The
     *         value <code>null</code> indicates a default value, more often
     *         not the centre of the window.
     * @throws UnsupportedOperationException if the <code>operation</code>
     *         <code>operation</code> argument isn't recognized.
     */
    private void transform(final int operation,
                           final double amount,
                           final Point2D center) throws UnsupportedOperationException {
        if ((operation & (RESET)) != 0) {
            /////////////////////
            ////    RESET    ////
            /////////////////////
            if ((operation & ~(RESET)) != 0) {
                throw new UnsupportedOperationException();
            }
            reset();
            return;
        }
        final AffineTransform change;
        try {
            change = zoom.createInverse();
        } catch (NoninvertibleTransformException exception) {
            unexpectedException("transform", exception);
            return;
        }
        if ((operation & (TRANSLATE_X | TRANSLATE_Y)) != 0) {
            /////////////////////////
            ////    TRANSLATE    ////
            /////////////////////////
            
            System.out.println("Scaling: "+amount+" ::: "+TRANSLATE_X+" - "+TRANSLATE_Y);
            
            
            
            if ((operation & ~(TRANSLATE_X | TRANSLATE_Y)) != 0) {
                throw new UnsupportedOperationException();
            }
            change.translate(((operation & TRANSLATE_X) != 0) ? amount : 0,
                             ((operation & TRANSLATE_Y) != 0) ? amount : 0);
        } else {
            /*
             * Obtains the coordinates (in pixels) of the rotation or zoom centre.
             */
            final double centerX;
            final double centerY;
            if (center != null) {
                centerX = center.getX();
                centerY = center.getY();
            } else {
                final Rectangle bounds = getZoomableBounds();
                if (bounds.width >= 0 && bounds.height >= 0) {
                    centerX = bounds.getCenterX();
                    centerY = bounds.getCenterY();
                } else {
                    return;
                }
                /*
                 * Zero lengths and widths are accepted.  If, however, the
                 * rectangle isn't valid (negative length or width) then the
                 * method will end without doing anything. No zoom will be
                 * performed.
                 */
            }
            if ((operation & (ROTATE)) != 0) {
                //////////////////////
                ////    ROTATE    ////
                //////////////////////
                if ((operation & ~(ROTATE)) != 0) {
                    throw new UnsupportedOperationException();
                }
                change.rotate(amount, centerX, centerY);
            } else if ((operation & (SCALE_X | SCALE_Y)) != 0) {
                /////////////////////
                ////    SCALE    ////
                /////////////////////
                
//                System.out.println("Scaling: "+amount+" ::: "+centerX+" , "+centerY);
                
                if ((operation & ~(UNIFORM_SCALE)) != 0) {
                    throw new UnsupportedOperationException();
                }
                change.translate(+centerX, +centerY);
                change.scale(((operation & SCALE_X) != 0) ? amount : 1,
                             ((operation & SCALE_Y) != 0) ? amount : 1);
                change.translate(-centerX, -centerY);
            } else if ((operation & (DEFAULT_ZOOM)) != 0) {
                ////////////////////////////
                ////    DEFAULT_ZOOM    ////
                ////////////////////////////
                if ((operation & ~(DEFAULT_ZOOM)) != 0) {
                    throw new UnsupportedOperationException();
                }
                final Dimension2D size = getPreferredPixelSize();
                double sx = 1 / (size.getWidth() * XAffineTransform.getScaleX0(zoom));
                double sy = 1 / (size.getHeight() * XAffineTransform.getScaleY0(zoom));
                if ((type & UNIFORM_SCALE) == UNIFORM_SCALE) {
                    if (sx > sy) sx = sy;
                    if (sy > sx) sy = sx;
                }
                if ((type & SCALE_X) == 0) sx = 1;
                if ((type & SCALE_Y) == 0) sy = 1;
                change.translate(+centerX, +centerY);
                change.scale    ( sx     ,  sy     );
                change.translate(-centerX, -centerY);
            }
            else {
                throw new UnsupportedOperationException();
            }
        }
        change.concatenate(zoom);
        XAffineTransform.round(change);
        transform(change);
    }

    /**
     * Adds an object to the list of objects interested in being notified
     * about zoom changes.
     */
    public void addZoomChangeListener(final ZoomChangeListener listener) {
        listenerList.add(ZoomChangeListener.class, listener);
    }

    /**
     * Removes an object from the list of objects interested in being notified
     * about zoom changes.
     */
    public void removeZoomChangeListener(final ZoomChangeListener listener) {
        listenerList.remove(ZoomChangeListener.class, listener);
    }

    /**
     * Adds an object to the list of objects interested in being notified
     * about mouse events.
     */
    public void addMouseListener(final MouseListener listener) {
        super.removeMouseListener(mouseSelectionTracker);
        super.addMouseListener   (listener);
        super.addMouseListener   (mouseSelectionTracker); // MUST be last!
    }

    /**
     * Signals that a zoom change has taken place. Every object registered by
     * the {@link #addZoomChangeListener} method will be notified of the change
     * as soon as possible.
     *
     * @param change Affine transform which represents the change in the zoom.
     *               That is <code>oldZoom</code> and <code>newZoom</code> are
     *               the affine transforms of the old and new zoom respectively.
     *               Therefore, the relation
     * <code>newZoom=oldZoom.{@link AffineTransform#concatenate concatenate}(change)</code>
     *               must be respected (to within rounding errors).
     *               <strong>Note: This method can modify 
     *               <code>change</code></strong> to combine several
     *               consecutive calls of 
     *               <code>fireZoomChanged</code> in a single transformation.
     */
    protected void fireZoomChanged(final AffineTransform change) {
        visibleArea.setRect(getVisibleArea());
        fireZoomChanged0(change);
    }

    /**
     * Notifies derived classes that the zoom has changed. Unlike the
     * protected {@link #fireZoomChanged} method, this private method doesn't
     * modify any internal field and doesn't attempt to call other
     * <code>WCTZoomPane> methods such as {@link #getVisibleArea}. An infinite
     * loop is thereby avoided as this method is called by {@link #reset}.
     */
    private void fireZoomChanged0(final AffineTransform change) {
        /*
         * Note: the event must be fired even if the transformation 
         *       is the identity matrix, because certain classes use
         *       this to update scrollbars.
         */
        if (change==null) {
            throw new NullPointerException();
        }
        ZoomChangeEvent event = null;
        final Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length; (i -= 2) >= 0;) {
            if (listeners[i] == ZoomChangeListener.class) {
                if (event == null) {
                    event = new ZoomChangeEvent(this, change);
                }
                try {
                    ((ZoomChangeListener) listeners[i+1]).zoomChanged(event);
                } catch (RuntimeException exception) {
                    unexpectedException("fireZoomChanged", exception);
                }
            }
        }
    }

    /**
     * Method called automatically after the user selects an area with the
     * mouse. The default implementation zooms to the selected
     * <code>area</code>. Derived classes can redefine this method in order 
     * to carry out another action. 
     *
     * @param area Area selected by the user, in logical coordinates.
     */
    protected void mouseSelectionPerformed(final Shape area) {
        final Rectangle2D rect = (area instanceof Rectangle2D) ? (Rectangle2D) area : area.getBounds2D();
        if (isValid(rect)) {
            switch (this.currentZoomMode) {
                case ZOOMIN: 
                    setVisibleArea(rect);
                    break;
                case ZOOMOUT:
                    Rectangle2D curArea = getVisibleArea();
                    double widthRatio = rect.getWidth() / curArea.getWidth();
                    double heightRatio = rect.getHeight() / curArea.getHeight();
                    double newWidth = curArea.getWidth() / widthRatio;
                    double newHeight = curArea.getHeight() / heightRatio;

                    Rectangle2D newArea = new Rectangle2D.Double(
                            rect.getCenterX() - newWidth/2.0,
                            rect.getCenterY() - newHeight/2.0,
                            newWidth, newHeight);
                    
                    setVisibleArea(newArea);
                    break;
                case PAN:
                    break;
            
            }
            
        }
    }

    
    
    
    
    /**
     * Sets current zoom mode to ZoomIn, ZoomOut, Pan
     * @param zoomMode
     */
    public void setZoomMode(ZoomMode zoomMode) {
        this.currentZoomMode = zoomMode;
    }
    
    public ZoomMode getZoomMode() {
        return this.currentZoomMode;
    }
    
    
    /**
     * Returns the geometric shape to be used to delimitate an area.
     * This shape is generally a rectangle but could also be an ellipse,
     * an arrow or another shape. The coordinates of the returned shape
     * won't be taken into account. In fact, these coordinates will often
     * be destroyed. The only things which count are the class of the
     * returned shape (e.g. {@link java.awt.geom.Ellipse2D} vs
     * {@link java.awt.geom.Rectangle2D}) and any of its parameters not
     * related to its position (e.g. corner rounding in a rectangle
     * {@link java.awt.geom.RoundRectangle2D}).
     *
     * The returned shape will generally be from a class derived from
     * {@link RectangularShape}, but can also be from the class
     * {@link Line2D}. <strong>Any other class risks firing a
     * {@link ClassCastException} at execution</strong>.
     *
     * The default implementation always returns a
     * {@link java.awt.geom.Rectangle2D} object.
     *
     * @param  point Logical coordinates of the mouse at the moment the button
     *         is pressed. This information can be used by derived classes
     *         that wish to consider the mouse position before choosing a
     *         geometric shape.
     * @return Shape from the class {link RectangularShape} or {link Line2D},
     *         or <code>null</code> to indicate that we do not want to select
     *         with the mouse.
     */
    protected Shape getMouseSelectionShape(final Point2D point) {
//        return new Rectangle2D.Float();
        return this.mouseSelectionShape;
    }

    
    
    /**
     * STEVE ADDITION:
     */
    public void setMouseSelectionShape(Shape shape) {
        this.mouseSelectionShape = shape;
    }
    
    
    
    /**
     * Indicates whether or not the magnifying glass is allowed to be
     * displayed on this component.  By default, it is allowed.
     */
    public boolean isMagnifierEnabled() {
        return magnifierEnabled;
    }

    /**
     * Specifies whether or not the magnifying glass is allowed to be displayed
     * on this component. Calling this method with the value <code>false</code>
     * will hide the magnifying glass, delete the choice "Display magnifying
     * glass" from the contextual menu and lead to all calls to 
     * <code>{@link #setMagnifierVisible setMagnifierVisible}(true)</code>
     * being ignored.
     */
    public void setMagnifierEnabled(final boolean enabled) {
        magnifierEnabled=enabled;
        navigationPopupMenu=null;
        if (!enabled) {
            setMagnifierVisible(false);
        }
    }

    /**
     * Indicates whether or not the magnifying glass is visible.  By default,
     * it is not visible. Call {@link #setMagnifierVisible(boolean)} to make it
     * appear.
     */
    public boolean isMagnifierVisible() {
        return magnifier != null;
    }

    /**
     * Displays or hides the magnifying glass. If the magnifying glass is not
     * visible and this method is called with the argument <code>true</code>,
     * the magnifying glass will appear at the centre of the window.
     */
    public void setMagnifierVisible(final boolean visible) {
        setMagnifierVisible(visible, null);
    }

    /**
     * Returns the color with which to tint magnifying glass.
     */
    public Paint getMagnifierGlass() {
        return magnifierGlass;
    }

    /**
     * Set the color with which to tint magnifying glass.
     */
    public void setMagnifierGlass(final Paint color) {
        final Paint old = magnifierGlass;
        magnifierGlass = color;
        firePropertyChange("magnifierGlass", old, color);
    }

    /**
     * Returns the color of the magnifying glass's border.
     */
    public Paint getMagnifierBorder() {
        return magnifierBorder;
    }

    /**
     * Set the color of the magnifying glass's border.
     */
    public void setMagnifierBorder(final Paint color) {
        final Paint old = magnifierBorder;
        magnifierBorder = color;
        firePropertyChange("magnifierBorder", old, color);
    }

    /**
     * Corrects a pixel's coordinates to take into account the presence of the
     * magnifying glass. The point <code>point</code> must contain the
     * coordinates of a pixel on the screen. If the magnifying glass is visible
     * and <code>point</code> falls within it, point's coordinates will be
     * corrected to make as if it pointed at the pixel itself, but in the
     * absence of the magnifying glass. In effect, the presence of the
     * magnifying glass can move the apparent position of the pixels.
     *
     * @deprecated Use {@link #correctApparentPixelPosition} instead.
     *             This is use a method's name change; the behavior is the same.
     */
    public final void correctPointForMagnifier(final Point2D point) {
        correctApparentPixelPosition(point);
    }

    /**
     * Corrects a pixel's coordinates for removing the effect of the magnifying glass.
     * Without this method, transformations from pixels to geographic coordinates would
     * not give accurate results for pixels inside the magnifier since the magnifier moves
     * the pixel's apparent position. Invoking this method will remove deformation
     * effects using the following steps:
     * <ul>
     *   <li>If the pixel's coordinate <code>point</code> is outside the magnifier,
     *       then this method do nothing.</li>
     *   <li>Otherwise, if the pixel's coordinate is inside the magnifier, then this
     *       method update <code>point</code> in such a way that it contains the
     *       position that the exact same pixel would have in the absence of magnifier.</li>
     * </ul>
     *
     * @param point In input, a pixel's coordinate as it appears on the screen.
     *              In output, the coordinate that the same pixel would have if
     *              the magnifier wasn't presents.
     */
    public final void correctApparentPixelPosition(final Point2D point) {
        if (magnifier != null && magnifier.contains(point)) {
            final double centerX = magnifier.getCenterX();
            final double centerY = magnifier.getCenterY();
            /*
             * The following code is equivalent to the following
             * transformations.
             * These transformations must be identical to those which
             * are applied in {@link #paintMagnifier}.
             *
             *         translate(+centerX, +centerY);
             *         scale    (magnifierPower, magnifierPower);
             *         translate(-centerX, -centerY);
             *         inverseTransform(point, point);
             */
            point.setLocation((point.getX() - centerX) / magnifierPower + centerX,
                              (point.getY() - centerY) / magnifierPower + centerY);
        }
    }

    /**
     * Displays or hides the magnifying glass. If the magnifying glass isn't
     * visible and this method is called with the argument <code>true</code>,
     * the magnifying glass will be displayed centred on the specified
     * coordinate.
     *
     * @param visible <code>true</code> to display the magnifying glass or
     *                <code>false</code> to hide it.
     * @param center  Central coordinate on which to display the magnifying
     *                glass.  If the magnifying glass was initially invisible,
     *                it will appear centred on this coordinate (or in the
     *                centre of the screen if <code>center</code> is null). If
     *                the magnifying glass was already visible and
     *                <code>center</code> is not null, it will be moved to 
     *                centre it on the specified coordinate.
     */
    private void setMagnifierVisible(final boolean visible, final Point center) {
        if (visible && magnifierEnabled) {
            if (magnifier == null) {
                Rectangle bounds = getZoomableBounds(); // Do not modify the Rectangle!
                if (bounds.isEmpty()) bounds = new Rectangle(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
                final int size=Math.min(Math.min(bounds.width, bounds.height), DEFAULT_MAGNIFIER_SIZE);
                final int centerX, centerY;
                if (center != null) {
                    centerX = center.x - size / 2;
                    centerY = center.y - size / 2;
                } else {
                    centerX = bounds.x + (bounds.width - size) / 2;
                    centerY = bounds.y + (bounds.height - size) / 2;
                }
                magnifier = new MouseReshapeTracker(new Ellipse2D.Float(centerX, centerY, size, size))
                {
                    protected void stateWillChange(final boolean isAdjusting) {repaintMagnifier();}
                    protected void stateChanged   (final boolean isAdjusting) {repaintMagnifier();}
                };
                magnifier.setClip(bounds);
                magnifier.setAdjustable(SwingConstants.NORTH, true);
                magnifier.setAdjustable(SwingConstants.SOUTH, true);
                magnifier.setAdjustable(SwingConstants.EAST , true);
                magnifier.setAdjustable(SwingConstants.WEST , true);

                addMouseListener      (magnifier);
                addMouseMotionListener(magnifier);
                firePropertyChange("magnifierVisible", Boolean.FALSE, Boolean.TRUE);
                repaintMagnifier();
            }
            else if (center != null) {
                final Rectangle2D frame = magnifier.getFrame();
                final double width  = frame.getWidth();
                final double height = frame.getHeight();
                magnifier.setFrame(center.x - 0.5 * width,
                                   center.y - 0.5 * height, width, height);
            }
        }
        else if (magnifier != null) {
            repaintMagnifier();
            removeMouseMotionListener(magnifier);
            removeMouseListener      (magnifier);
            setCursor(null);
            magnifier = null;
            firePropertyChange("magnifierVisible", Boolean.TRUE, Boolean.FALSE);
        }
    }

    /**
     * Adds navigation options to the specified menu. Menus such as
     * "Zoom in" and "Zoom out" will be automatically added to the menu
     * together with the appropriate short-cut keys.
     */
    public void buildNavigationMenu(final JMenu menu) {
        buildNavigationMenu(menu, null);
    }

    /**
     * Adds navigation options to the specified menu. Menus such as
     * "Zoom in" and "Zoom out" will be automatically added to the menu
     * together with the appropriate short-cut keys.
     */
    private void buildNavigationMenu(final JMenu menu, final JPopupMenu popup) {
        int groupIndex = 0;
        boolean firstMenu = true;
        final ActionMap actionMap = getActionMap();
        for (int i=0; i<ACTION_ID.length; i++) {
            final Action action = actionMap.get(ACTION_ID[i]);
            if (action!=null && action.getValue(Action.NAME)!=null) {
                /*
                 * Checks whether the next item belongs to a new group.
                 * If this is the case, it will be necessary to add a separator
                 * before the next menu.
                 */
                final int lastGroupIndex = groupIndex;
                while ((ACTION_TYPE[i] & GROUP[groupIndex]) == 0) {
                    groupIndex = (groupIndex+1) % GROUP.length;
                    if (groupIndex == lastGroupIndex) {
                        break;
                    }
                }
                /*
                 * Adds an item to the menu.
                 */
                if (menu != null) {
                    if (groupIndex!=lastGroupIndex && !firstMenu) {
                        menu.addSeparator();
                    }
                    final JMenuItem item = new JMenuItem(action);
                    item.setAccelerator((KeyStroke) action.getValue(Action.ACCELERATOR_KEY));
                    menu.add(item);
                }
                if (popup != null) {
                    if (groupIndex!=lastGroupIndex && !firstMenu) {
                        popup.addSeparator();
                    }
                    final JMenuItem item = new JMenuItem(action);
                    item.setAccelerator((KeyStroke) action.getValue(Action.ACCELERATOR_KEY));
                    popup.add(item);
                }
                firstMenu = false;
            }
        }
    }

    /**
     * Menu with a position.  This class retains the exact coordinates of the 
     * place the user clicked when this menu was invoked.
     *
     * @author Martin Desruisseaux
     * @version $Id: WCTZoomPane.java,v 1.16 2004/02/16 20:49:35 desruisseaux Exp $
     */
    private static final class PointPopupMenu extends JPopupMenu {
        /**
         * Coordinates of the point the user clicked on.
         */
        public final Point point;

        /**
         * Constructs a menu, retaining the specified coordinate.
         */
        public PointPopupMenu(final Point point) {
            this.point = point;
        }
    }

    /**
     * Method called automatically when the user clicks on the right mouse
     * button.  The default implementation displays a contextual menu 
     * containing navigation options.
     *
     * @param  event Mouse event containing amongst others, the 
     *         coordinates pointées???????????.
     * @return The contextual menu, or <code>null</code> to avoid displaying
     *         the menu.
     */
    protected JPopupMenu getPopupMenu(final MouseEvent event) {
        if (getZoomableBounds().contains(event.getX(), event.getY())) {
            if (navigationPopupMenu == null) {
                navigationPopupMenu = new PointPopupMenu(event.getPoint());
                if (magnifierEnabled) {
                    final Resources resources = Resources.getResources(getLocale());
                    final JMenuItem item = new JMenuItem(resources.getString(ResourceKeys.SHOW_MAGNIFIER));
                    item.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(final ActionEvent event)
                        {setMagnifierVisible(true, navigationPopupMenu.point);}
                    });
                    navigationPopupMenu.add(item);
                    navigationPopupMenu.addSeparator();
                }
                buildNavigationMenu(null, navigationPopupMenu);
            } else {
                navigationPopupMenu.point.x = event.getX();
                navigationPopupMenu.point.y = event.getY();
            }
            return navigationPopupMenu;
        }
        else return null;
    }

    /**
     * Method called automatically when the user clicks on the right mouse
     * button inside the magnifying glass. The default implementation displays
     * a contextual menu which contains magnifying glass options.
     *
     * @param  event Mouse event containing amongst others, the 
     *         coordinates ???? pointées.
     * @return The contextual menu, or <code>null</code> to avoid displaying
     *         the menu.
     */
    protected JPopupMenu getMagnifierMenu(final MouseEvent event) {
        final Resources resources = Resources.getResources(getLocale());
        final JPopupMenu menu = new JPopupMenu(resources.getString(ResourceKeys.MAGNIFIER));
        final JMenuItem  item = new JMenuItem (resources.getString(ResourceKeys.HIDE));
        item.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent event)
            {setMagnifierVisible(false);}
        });
        menu.add(item);
        return menu;
    }

    /**
     * Displays the navigation contextual menu, provided the mouse event is
     * in fact the one which normally displays this menu.
     */
    private void mayShowPopupMenu(final MouseEvent event) {
        if ( event.getID()       == MouseEvent.MOUSE_PRESSED &&
            (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
        {
            requestFocus();
        }
        if (event.isPopupTrigger()) {
            final Point point      = event.getPoint();
            final JPopupMenu popup = (magnifier != null && magnifier.contains(point)) ? getMagnifierMenu(event) : getPopupMenu(event);
            if (popup != null) {
                final Component source  = event.getComponent();
                final Window    window  = SwingUtilities.getWindowAncestor(source);
                if (window != null) {
                    final Toolkit   toolkit = source.getToolkit();
                    final Insets    insets  = toolkit.getScreenInsets(window.getGraphicsConfiguration());
                    final Dimension screen  = toolkit.getScreenSize();
                    final Dimension size    = popup.getPreferredSize();
                    SwingUtilities.convertPointToScreen(point, source);
                    screen.width  -= (size.width  + insets.right);
                    screen.height -= (size.height + insets.bottom);
                    if (point.x > screen.width) {
                        point.x = screen.width;
                    }
                    if (point.y > screen.height) {
                        point.y = screen.height;
                    }
                    if (point.x < insets.left) {
                        point.x = insets.left;
                    }
                    if (point.y < insets.top) {
                        point.y = insets.top;
                    }
                    SwingUtilities.convertPointFromScreen(point, source);
                    popup.show(source, point.x, point.y);
                }
            }
        }
    }

    /**
     * Method called automatically when user moves the mouse wheel. This method
     * performs a zoom centred on the mouse position.
     */
    private final void mouseWheelMoved(final MouseWheelEvent event) {
        if (event.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int rotation  = event.getUnitsToScroll();
            double scale  = 1 + (AMOUNT_SCALE - 1) * Math.abs(rotation);
            Point2D point = new Point2D.Double(event.getX(), event.getY());
            if (rotation > 0) {
                scale = 1 / scale;
            }
            if (magnifier != null && magnifier.contains(point)) {
                magnifierPower *= scale;
                repaintMagnifier();
            } else {
                correctApparentPixelPosition(point);
                transform(UNIFORM_SCALE & type, scale, point);
            }
            event.consume();
        }
    }

    /**
     * Method called each time the size or the position of the component changes.
     */
    private final void processSizeEvent(final ComponentEvent event) {
        if (!isValid(visibleArea) || zoomIsReset) {
            reset();
        }
        if (magnifier != null) {
            magnifier.setClip(getZoomableBounds());
        }
        /*
         * {@link #repaint} isn't called because there is already a 
         * {@link #repaint} command in the queue.  Therefore, the redraw will
         * be twice as quick under JDK 1.3. {@link #transform} isn't called
         * either because the zoom hasn't really changed; we have simply
         * discovered a part of the window which was hidden before. However,
         * we still need to adjust the scrollbars.
         */
        final Object[] listeners=listenerList.getListenerList();
        for (int i = listeners.length; (i-=2) >= 0;) {
            if (listeners[i] == ZoomChangeListener.class) {
                if (listeners[i + 1] instanceof Synchronizer) try {
                    ((ZoomChangeListener) listeners[i + 1]).zoomChanged(null);
                } catch (RuntimeException exception) {
                    unexpectedException("processSizeEvent", exception);
                }
            }
        }
    }

    /**
     * Returns an object which displays this <code>WCTZoomPane</code>
     * with the scrollbars.
     */
    public JComponent createScrollPane() {
        return new ScrollPane();
    }

    /**
     * The scroll panel for {@link WCTZoomPane}. The standard {@link JScrollPane}
     * class is not used because it is difficult to get {@link JViewport} to
     * cooperate with transformations already handled by {@link WCTZoomPane#zoom}.
     *
     * @version $Id: WCTZoomPane.java,v 1.16 2004/02/16 20:49:35 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class ScrollPane extends JComponent implements PropertyChangeListener {
        /**
         * The horizontal scrollbar, or <code>null</code> if none.
         */
        private final JScrollBar scrollbarX;

        /**
         * The vertical scrollbar, or <code>null</code> if none.
         */
        private final JScrollBar scrollbarY;

        /**
         * Constructs a scroll pane for the enclosing {@link WCTZoomPane}.
         */
        public ScrollPane() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            /*
             * Sets up the scrollbars.
             */
            if ((type & TRANSLATE_X) != 0) {
                scrollbarX = new JScrollBar(JScrollBar.HORIZONTAL);
                scrollbarX.setUnitIncrement ((int) (AMOUNT_TRANSLATE));
                scrollbarX.setBlockIncrement((int) (AMOUNT_TRANSLATE * ENHANCEMENT_FACTOR));
            } else {
                scrollbarX  = null;
            }
            if ((type & TRANSLATE_Y) != 0) {
                scrollbarY = new JScrollBar(JScrollBar.VERTICAL);
                scrollbarY.setUnitIncrement ((int) (AMOUNT_TRANSLATE));
                scrollbarY.setBlockIncrement((int) (AMOUNT_TRANSLATE * ENHANCEMENT_FACTOR));
            } else {
                scrollbarY  = null;
            }
            /*
             * Adds the scrollbars in the scroll pane.
             */
            final GridBagConstraints c = new GridBagConstraints();
            if (scrollbarX != null) {
                c.gridx = 0; c.weightx = 1;
                c.gridy = 1; c.weighty = 0;
                c.fill = c.HORIZONTAL;
                add(scrollbarX, c);
            }
            if (scrollbarY != null) {
                c.gridx = 1; c.weightx = 0;
                c.gridy = 0; c.weighty = 1;
                c.fill = c.VERTICAL;
                add(scrollbarY, c);
            }
            if (scrollbarX != null && scrollbarY != null) {
                final JComponent corner = new JPanel();
                corner.setOpaque(true);
                c.gridx = 1; c.weightx = 0;
                c.gridy = 1; c.weighty = 0;
                c.fill = c.BOTH;
                add(corner, c);
            }
            c.fill = c.BOTH;
            c.gridx = 0; c.weightx = 1;
            c.gridy = 0; c.weighty = 1;
            add(WCTZoomPane.this, c);
        }

        /**
         * Convenience method which fetches a scrollbar model.
         * Should be a static method, but compiler doesn't
         * allow this.
         */
        private /*static*/ BoundedRangeModel getModel(final JScrollBar bar) {
            return (bar != null) ? bar.getModel() : null;
        }

        /**
         * Invoked when this <code>ScrollPane</code>  is added in
         * a {@link Container}. This method registers all required
         * listeners.
         */
        public void addNotify() {
            super.addNotify();
            tieModels(getModel(scrollbarX), getModel(scrollbarY));
            WCTZoomPane.this.addPropertyChangeListener("zoom.insets", this);
        }

        /**
         * Invoked when this <code>ScrollPane</code> is removed from
         * a {@link Container}. This method unregisters all listeners.
         */
        public void removeNotify() {
            WCTZoomPane.this.removePropertyChangeListener("zoom.insets", this);
            untieModels(getModel(scrollbarX), getModel(scrollbarY));
            super.removeNotify();
        }

        /**
         * Invoked when the zoomable area changes. This method will adjust
         * scrollbar's insets in order to keep scrollbars aligned in front
         * of the zoomable area.
         *
         * Note: in the current version, this is an undocumented capability.
         *       Class {@link RangeBar} uses it, but it is experimental.
         *       It may change in a future version.
         */
        public void propertyChange(final PropertyChangeEvent event) {
            final Insets old    = (Insets) event.getOldValue();
            final Insets insets = (Insets) event.getNewValue();
            final GridBagLayout layout = (GridBagLayout) getLayout();
            if (scrollbarX != null && (old.left != insets.left || old.right != insets.right)) {
                final GridBagConstraints c = layout.getConstraints(scrollbarX);
                c.insets.left  = insets.left;
                c.insets.right = insets.right;
                layout.setConstraints(scrollbarX, c);
                scrollbarX.invalidate();
            }
            if (scrollbarY != null && (old.top != insets.top || old.bottom != insets.bottom)) {
                final GridBagConstraints c = layout.getConstraints(scrollbarY);
                c.insets.top    = insets.top;
                c.insets.bottom = insets.bottom;
                layout.setConstraints(scrollbarY, c);
                scrollbarY.invalidate();
            }
        }
    }

    /**
     * Synchronises the position and the range of the models <var>x</var> and
     * <var>y</var> with the position of the zoom.  The models <var>x</var>
     * and <var>y</var> are generally associated with horizontal and vertical
     * scrollbars.  When the position of a scrollbar is adjusted, the zoom is
     * consequently adjusted. Inversely, when the zoom is modified, the
     * positions and ranges of the scrollbars are consequently adjusted.
     *
     * @param x Model of the horizontal scrollbar or <code>null</code> if there
     *          isn't one.
     * @param y Model of the vertical scrollbar or <code>null</code> if there
     *          isn't one.
     */
    public void tieModels(final BoundedRangeModel x, final BoundedRangeModel y) {
        if (x != null || y != null) {
            final Synchronizer listener = new Synchronizer(x, y);
            addZoomChangeListener(listener);
            if (x != null) x.addChangeListener(listener);
            if (y != null) y.addChangeListener(listener);
        }
    }

    /**
     * Cancels the synchronisation between the specified <var>x</var> and
     * <var>y</var> models and the zoom of this <code>WCTZoomPane</code> object.
     * The {@link ChangeListener} and {@link ZoomChangeListener} objects that
     * were created are deleted.
     *
     * @param x Model of the horizontal scrollbar or <code>null</code> if there
     *          isn't one.
     * @param y Model of the vertical scrollbar or <code>null</code> if there
     *          isn't one.
     */
    public void untieModels(final BoundedRangeModel x, final BoundedRangeModel y) {
        final EventListener[] listeners = getListeners(ZoomChangeListener.class);
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof Synchronizer) {
                final Synchronizer s = (Synchronizer) listeners[i];
                if (s.xm == x && s.ym == y) {
                    removeZoomChangeListener(s);
                    if (x != null) x.removeChangeListener(s);
                    if (y != null) y.removeChangeListener(s);
                }
            }
        }
    }

    /**
     * Object responsible for synchronizing a {@link JScrollPane} object with
     * scrollbars.  Whilst not generally useful, it would be possible to 
     * synchronize several pairs of {@link BoundedRangeModel} objects on one
     * <code>WCTZoomPane</code> object.
     *
     * @author Martin Desruisseaux
     * @version $Id: WCTZoomPane.java,v 1.16 2004/02/16 20:49:35 desruisseaux Exp $
     */
    private final class Synchronizer implements ChangeListener, ZoomChangeListener {
        /**
         * Model to synchronize with {@link WCTZoomPane}.
         */
        public final BoundedRangeModel xm, ym;

        /**
         * Indicates whether the scrollbars are being adjusted in
         * response to {@link #zoomChanged}.
         * If this is the case, {@link #stateChanged} mustn't make any
         * other adjustments.
         */
        private transient boolean isAdjusting;

        /**
         * Cached <code>WCTZoomPane</code> bounds. Used in order
         * to avoid too many object allocations on the heap.
         */
        private transient Rectangle bounds;

        /**
         * Constructs an object which synchronises a pair of 
         * {@link BoundedRangeModel} with {@link WCTZoomPane}.
         */
        public Synchronizer(final BoundedRangeModel xm, final BoundedRangeModel ym) {
            this.xm = xm;
            this.ym = ym;
        }

        /**
         * Method called automatically each time the position of one of the
         * scrollbars changes.
         */
        public void stateChanged(final ChangeEvent event) {
            if (!isAdjusting) {
                final boolean valueIsAdjusting = ((BoundedRangeModel) event.getSource()).getValueIsAdjusting();
                if (paintingWhileAdjusting || !valueIsAdjusting) {
                    /*
                     * Scroll view coordinates are computed using the
                     * following steps:
                     *
                     *   1) Get the logical coordinates for the whole area.
                     *   2) Transform to pixel space using current zoom.
                     *   3) Clip to the scrollbar's position (in pixels).
                     *   4) Transform back to the logical space.
                     *   5) Set the visible area to the resulting rectangle.
                     */
                    Rectangle2D area = getArea();
                    if (isValid(area)) {
                        area = XAffineTransform.transform(zoom, area, null);
                        double x = area.getX();
                        double y = area.getY();
                        double width, height;
                        if (xm != null) {
                            x    += xm.getValue();
                            width = xm.getExtent();
                        } else {
                            width = area.getWidth();
                        }
                        if (ym != null) {
                            y     += ym.getValue();
                            height = ym.getExtent();
                        } else {
                            height = area.getHeight();
                        }
                        area.setRect(x, y, width, height);
                        bounds = getBounds(bounds);
                        try {
                            area = XAffineTransform.inverseTransform(zoom, area, area);
                            try {
                                isAdjusting = true;
                                transform(setVisibleArea(area, bounds=getBounds(bounds), 0));
                            } finally {
                                isAdjusting = false;
                            }
                        } catch (NoninvertibleTransformException exception) {
                            unexpectedException("stateChanged", exception);
                        }
                    }
                }
                if (!valueIsAdjusting) {
                    zoomChanged(null);
                }
            }
        }

        /**
         * Method called each time the zoom changes.
         *
         * @param change Ignored. Can be null and will effectively sometimes
         *               be null.
         */
        public void zoomChanged(final ZoomChangeEvent change) {
            if (!isAdjusting) {
                Rectangle2D area = getArea();
                if (isValid(area)) {
                    area = XAffineTransform.transform(zoom, area, null);
                    try {
                        isAdjusting = true;
                        setRangeProperties(xm, area.getX(), getWidth(),  area.getWidth());
                        setRangeProperties(ym, area.getY(), getHeight(), area.getHeight());
                    }
                    finally {
                        isAdjusting = false;
                    }
                }
            }
        }
    }

    /**
     * Adjusts the values of a model.  The minimums and maximums are adjusted 
     * as needed in order to include the value and its range. This adjustment
     * is necessary in order to avoid chaotic behaviour when the user 
     * drags the slider whilst a part of the graphic is outside the zone
     * initially planned for {@link #getArea}.
     */
    private static void setRangeProperties(final BoundedRangeModel model,
                                           final double value, final int extent,
                                           final double max) {
        if (model != null) {
            final int pos = (int) Math.round(-value);
            model.setRangeProperties(pos, extent, Math.min(0, pos),
                                     Math.max((int) Math.round(max), pos + extent), false);
        }
    }

    /**
     * Modifies the position in pixels of the visible part of
     * <code>WCTZoomPane</code>. <code>viewSize</code> is the size 
     * <code>WCTZoomPane</code> would be (in pixels) if its visible surface
     * covered the whole of the {@link #getArea} region with the current
     * zoom (Note: <code>viewSize</code> can be obtained by {@link
     * #getPreferredSize} if {@link #setPreferredSize} hasn't been called
     * with a non-null value). Therefore, by definition, the region
     * {@link #getArea} converted into pixel space would give the rectangle
     * <code>bounds=Rectangle(0,&nbsp;0,&nbsp;,viewSize.width,&nbsp;,viewSize.height)</code>.
     *
     * This <code>scrollRectToVisible</code> method allows us to define the 
     * sub-region of <code>bounds</code> which must appear in the
     * <code>WCTZoomPane</code> window.
     */
    public void scrollRectToVisible(final Rectangle rect) {
        Rectangle2D area = getArea();
        if (isValid(area)) {
            area = XAffineTransform.transform(zoom, area, null);
            area.setRect(area.getX() + rect.getX(), area.getY() + rect.getY(),
                         rect.getWidth(), rect.getHeight());
            try {
                setVisibleArea(XAffineTransform.inverseTransform(zoom, area, area));
            } catch (NoninvertibleTransformException exception) {
                unexpectedException("scrollRectToVisible", exception);
            }
        }
    }

    /**
     * Indicates whether or not this <code>WCTZoomPane</code> object should be
     * repainted when the user moves the scrollbar slider. The scrollbars (or
     * other models) involved are those which have been synchronised with
     * this <code>WCTZoomPane</code> object through the {@link #tieModels} method.
     * The default value is <code>false</code>, which means that
     * <code>WCTZoomPane</code> will wait until the user releases the slider
     * before repainting.
     */
    public boolean isPaintingWhileAdjusting() {
        return paintingWhileAdjusting;
    }

    /**
     * Defines whether or not this <code>WCTZoomPane</code> object should repaint
     * the map when the user moves the scrollbar slider. 
     * A fast computer is recommended if this flag is to be set to
     * <code>true</code>.
     */
    public void setPaintingWhileAdjusting(final boolean flag) {
        paintingWhileAdjusting = flag;
    }

    /**
     * Declares that a part of this pane needs to be repainted. This method
     * simply redefines the method of the parent class in order to take into
     * account a case where the magnifying glass is displayed.
     */
    public void repaint(final long tm, final int x, final int y,
                        final int width, final int height) {
        super.repaint(tm, x, y, width, height);
        if (magnifier != null && magnifier.intersects(x, y, width, height)) {
            // If the part to paint is inside the magnifying glass,
            // the fact that the magnifying glass is zooming in means
            // we have to repaint a little more than that which was requested.
            repaintMagnifier();
        }
    }

    /**
     * Declares that the magnifying glass needs to be repainted. A
     * {@link #repaint()} command is sent with the bounds of the
     * magnifying glass as coordinates (taking into account its
     * outline).
     */
    private void repaintMagnifier() {
        final Rectangle bounds = magnifier.getBounds();
        bounds.x      -= 4;
        bounds.y      -= 4;
        bounds.width  += 8;
        bounds.height += 8;
        super.repaint(0, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Paints the magnifying glass. This method is invoked after
     * {@link #paintComponent(Graphics2D)} if a magnifying glass
     * is visible.
     */
    protected void paintMagnifier(final Graphics2D graphics) {
        final double centerX = magnifier.getCenterX();
        final double centerY = magnifier.getCenterY();
        final Stroke  stroke =  graphics.getStroke();
        final Paint    paint =  graphics.getPaint();
        graphics.setStroke(new BasicStroke(6));
        graphics.setPaint (magnifierBorder);
        graphics.draw     (magnifier);
        graphics.setStroke(stroke);
        graphics.clip     (magnifier); // Coordinates in pixels!
        graphics.setPaint (magnifierGlass);
        graphics.fill     (magnifier.getBounds2D());
        graphics.setPaint (paint);
        graphics.translate(+centerX, +centerY);
        graphics.scale    (magnifierPower, magnifierPower);
        graphics.translate(-centerX, -centerY);
        // Note: the transformations performed here must be identical to those
        //       performed in {@link #pixelToLogical}.
        paintComponent    (graphics);
    }

    /**
     * Paints this component. Subclass must override this method in order to
     * draw the <code>WCTZoomPane</code> content. For most implementations, the
     * first line in this method will be
     *
     * <code>graphics.transform({@link #zoom})</code>.
     */
    protected abstract void paintComponent(final Graphics2D graphics);

    /**
     * Prints this component. The default implementation
     * invokes {@link #paintComponent(Graphics2D)}.
     */
    protected void printComponent(final Graphics2D graphics) {
        paintComponent(graphics);
    }

    /**
     * Paints this component. This method is declared <code>final</code>
     * in order to avoid unintentional overriding. Override
     * {@link #paintComponent(Graphics2D)} instead.
     */
    protected final void paintComponent(final Graphics graphics) {
        flag = IS_PAINTING;
        super.paintComponent(graphics);
        /*
         * The <code>JComponent.paintComponent(...)</code> method creates a
         * temporary <code>Graphics2D</code> object, then calls 
         * <code>ComponentUI.update(...)</code> with this graphic as a
         * parameter.  This method clears the screen background then calls
         * <code>ComponentUI.paint(...)</code>.
         * This last method has been redefined further up (our {@link #UI})
         * object in such a way that it calls itself
         * {@link #paintComponent(Graphics2D)}. A complicated path, but we
         * don't have much choice and it is, after all, quite efficient.
         */
        if (magnifier != null) {
            flag = IS_PAINTING_MAGNIFIER;
            super.paintComponent(graphics);
        }
    }

    /**
     * Prints this component. This method is declared <code>final</code>
     * in order to avoid unintentional overriding. Override
     * {@link #printComponent(Graphics2D)} instead.
     */
    protected final void printComponent(final Graphics graphics) {
        flag = IS_PRINTING;
        super.paintComponent(graphics);
        /*
         * Ne pas appeller 'super.printComponent' parce qu'on ne
         * veut pas qu'il appelle notre 'paintComponent' ci-haut.
         */
    }

    /**
     * Returns the size (in pixels) that <code>WCTZoomPane</code> would have if 
     * it displayed the whole of the {@link #getArea} region with the current
     * zoom ({@link #zoom}). This method is practical for determining the
     * maximum values to assign to the scrollbars. For example, the horizontal
     * bar could cover the range <code>[0..viewSize.width]</code>
     * whilst the vertical bar could cover the range
     * <code>[0..viewSize.height]</code>.
     */
    private final Dimension getViewSize() {
        if (!visibleArea.isEmpty()) {
            Rectangle2D area = getArea();
            if (isValid(area)) {
                area = XAffineTransform.transform(zoom, area, null);
                return new Dimension((int) Math.rint(area.getWidth()),
                                     (int) Math.rint(area.getHeight()));
            }
            return getSize();
        }
        return new Dimension(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Returns the Insets of this component. This method works like 
     * <code>super.getInsets(insets)</code>, but accepts a null argument. This
     * method can be redefined if it is necessary to perform zooms on a
     * part of the graphic rather than the whole thing.
     */
    public Insets getInsets(final Insets insets) {
        return super.getInsets((insets != null) ? insets : new Insets(0, 0, 0, 0));
    }

    /**
     * Returns the Insets of this component.  This method is declared final in
     * order to avoid confusion. If you want to return other Insets you must 
     * redefine {@link #getInsets(Insets)}.
     */
    public final Insets getInsets() {
        return getInsets(null);
    }

    /**
     * Informs <code>WCTZoomPane</code> that the GUI has changed. The user
     * doesn't have to call this method directly.
     */
    public void updateUI() {
        navigationPopupMenu = null;
        super.updateUI();
        setUI(UI);
    }

    /**
     * Invoked when an affine transform that should be invertible is not.
     * Default implementation logs the stack trace and resets the zoom.
     *
     * @param methodName The caller's method name.
     * @param exception  The exception.
     */
    private void unexpectedException(final String methodName,
                                     final NoninvertibleTransformException exception) {
        zoom.setToIdentity();
        Utilities.unexpectedException("org.geotools.gui",
                                      "org.geotools.swing.WCTZoomPane",
                                      methodName, exception);
    }

    /**
     * Invoked when an unexpected exception occurs.
     * Default implementation logs the stack trace.
     *
     * @param methodName The caller's method name.
     * @param exception  The exception.
     */
    private static void unexpectedException(final String methodName,
                                            final RuntimeException exception) {
        Utilities.unexpectedException("org.geotools.gui",
                                      "org.geotools.swing.WCTZoomPane",
                                      methodName, exception);
    }

    /**
     * Convenience method logging an area setting from the
     * <code>WCTZoomPane</code> class. This method is invoked
     * from {@link #setPreferredArea} and {@link #setVisibleArea}.
     *
     * @param methodName The caller's method name
     *                  (e.g. <code>"setArea"</code>).
     * @param area The coordinates to log (may be <code>null</code>).
     */
    private static void log(final String methodName, final Rectangle2D area) {
        log("WCTZoomPane", methodName, area);
    }

    /**
     * Convenience method for logging events related to area setting.
     * Events are logged in the <code>"org.geotools.gui"</code> logger
     * with {@link Level#FINER}. <code>WCTZoomPane</code> invokes this method
     * for logging any [@link #setPreferredArea} and {@link #setVisibleArea}
     * invocations. Subclasses may invoke this method for logging some other
     * kinds of area changes.
     *
     * @param  className The fully qualified caller's class name
     *                   (e.g. <code>"org.geotools.swing.WCTZoomPane"</code>).
     * @param methodName The caller's method name (e.g. <code>"setArea"</code>).
     * @param       area The coordinates to log (may be <code>null</code>).
     */
    static void log(final String className,
                    final String methodName,
                    final Rectangle2D area) {
        if (LOGGER.isLoggable(Level.FINER)) {
            final Double[] areaBounds;
            if (area != null) {
                areaBounds = new Double[] {
                    new Double(area.getMinX()), new Double(area.getMaxX()),
                    new Double(area.getMinY()), new Double(area.getMaxY())
                };
            } else {
                areaBounds = new Double[4];
                Arrays.fill(areaBounds, new Double(Double.NaN));
            }
            final Resources resources = Resources.getResources(null);
            final LogRecord record = resources.getLogRecord(Level.FINER,
                                                            ResourceKeys.RECTANGLE_$4,
                                                            areaBounds);
            record.setSourceClassName (className);
            record.setSourceMethodName(methodName);
            LOGGER.log(record);
        }
    }

    /**
     * Checks whether the rectangle <code>rect</code> is valid.  The rectangle
     * is considered invalid if its length or width is less than or equal to 0,
     * or if one of its coordinates is infinite or NaN.
     */
    private static boolean isValid(final Rectangle2D rect) {
        if (rect == null) {
            return false;
        }
        final double x = rect.getX();
        final double y = rect.getY();
        final double w = rect.getWidth();
        final double h = rect.getHeight();
        return (x > Double.NEGATIVE_INFINITY && x < Double.POSITIVE_INFINITY &&
                y > Double.NEGATIVE_INFINITY && y < Double.POSITIVE_INFINITY &&
                w > 0                        && w < Double.POSITIVE_INFINITY &&
                h > 0                        && h < Double.POSITIVE_INFINITY);
    }
    
    
    
    
    public boolean isMouseDragging() {
        return mouseListener.isDragging();
    }
}
