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

// J2SE dependencies
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.renderer.j2d.GeoMouseEvent;
import org.geotools.renderer.j2d.Hints;
import org.geotools.renderer.j2d.RenderedLayer;
import org.geotools.renderer.j2d.Renderer;
import org.opengis.referencing.operation.TransformException;


/**
 * A <cite>Swing</cite> component for displaying geographic features.
 * Since this class extends {@link WCTZoomPane},  the user can use mouse and keyboard
 * to zoom, translate and rotate around the map (Remind: <code>WCTMapPane</code> has
 * no scrollbar. To display scrollbars, use {@link #createScrollPane}).
 *
 * @version $Id: WCTMapPane.java,v 1.23 2003/08/22 12:43:04 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class WCTMapPane extends WCTZoomPane {
    /**
     * The maximal number of {@link JPopupMenu}s to cache in {@link cachedMenus}.
     */
    private static final int MAX_CACHED_MENUS = 5;

    /**
     * Default value for the {@link Hints#FINEST_RESOLUTION} rendering hint. This is the
     * finest resolution wanted, in pixel. If an isoline has a finest resolution than this,
     * it will be decimated before rendering. This value should not be to close from {@link
     * #REQUIRED_RESOLUTION} in order to avoir too frequent recomputation when zoom change.
     */
    private static final Float FINEST_RESOLUTION = new Float(0.25);

    /**
     * Default value for the {@link Hints#REQUIRED_RESOLUTION} rendering hint. This is the
     * resolution required, in pixel.   If an isoline has a too strong decimation for this
     * resolution, then it will be resampled. This value should not be to close from {@link
     * #FINEST_RESOLUTION} in order to avoir too frequent recomputation when zoom change.
     */
    private static final Float REQUIRED_RESOLUTION = new Float(2);

    /**
     * The renderer targeting Java2D. If the map pane is actually an instance
     * of {@link StyledMapPane}, then the renderer will be an instance of
     * {@link org.geotools.renderer.j2d.StyledRenderer}.
     */
    private final Renderer renderer;

    /**
     * List of popup menus created lately. The last menu used must appears first in the list.
     * This is used only as a cache for avoiding creating a popup menu too often. The maximal
     * number of popup menus to cache is {@link #MAX_CACHED_MENUS}.
     */
    private transient LinkedList cachedMenus;

    /**
     * A listener for various event of interest to this <code>WCTMapPane</code>.
     * We use a private inner class in order to avoid public access to listener methods.
     */
    private final ListenerProxy listenerProxy = new ListenerProxy();

    /**
     * Listeners for various event of interest to this <code>WCTMapPane</code>.
     * We use a private inner class in order to avoid public access to listener methods.
     */
    private final class ListenerProxy implements PropertyChangeListener {
        /** Invoked when a {@link Renderer} property is changed. */
        public void propertyChange(final PropertyChangeEvent event) {
            // Make sure we are running in the AWT thread.
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        propertyChange(event);
                    }
                });
                return;
            }
            final String propertyName = event.getPropertyName();
            if (propertyName.equalsIgnoreCase("preferredArea")) {
                fireZoomChanged(new AffineTransform()); // Update scrollbars
                log("WCTMapPane", "setArea", (Rectangle2D) event.getNewValue());
                return;
            }
            if (propertyName.equalsIgnoreCase("layers")) {
                final RenderedLayer[] layers = (RenderedLayer[]) event.getOldValue();
                if (layers!=null && layers.length==0) {
                    reset();
                }
                return;
            }
        }
    }

    /**
     * Construct a default map panel.
     */
    public WCTMapPane() {
//        super(TRANSLATE_X | TRANSLATE_Y | UNIFORM_SCALE | DEFAULT_ZOOM | ROTATE | RESET);
        super(TRANSLATE_X | TRANSLATE_Y | UNIFORM_SCALE | DEFAULT_ZOOM | RESET);
        setResetPolicy(true);
        renderer = createRenderer();
        renderer.setRenderingHint(Hints.  FINEST_RESOLUTION,   FINEST_RESOLUTION);
        renderer.setRenderingHint(Hints.REQUIRED_RESOLUTION, REQUIRED_RESOLUTION);
        renderer.addPropertyChangeListener(listenerProxy);
        ToolTipManager.sharedInstance().registerComponent(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Construct a map panel using the specified coordinate system.
     *
     * @param cs The rendering coordinate system.
     */
    public WCTMapPane(final CoordinateSystem cs) {
        this();
        try {
            setCoordinateSystem(cs);
        } catch (TransformException exception) {
            // Since the Renderer doesn't yet contains any layer, this error can happen only
            // if the specified coordinate system can't be reduced to a two dimensional CS.
            final IllegalArgumentException e;
            e = new IllegalArgumentException(exception.getLocalizedMessage());
            e.initCause(exception);
        }
    }

    /**
     * Create the renderer for this map pane. This method
     * is invoked by the constructor at creation time only.
     * Class {@link StyledRenderer} will overrides this method.
     */
    Renderer createRenderer() {
        return new Renderer(this);
    }

    /**
     * Returns the view coordinate system. This is the "real world" coordinate system
     * used for displaying all features. Note that underlying data doesn't need to be
     * in this coordinate system: transformations will performed on the fly as needed
     * at rendering time.
     *
     * @return The two dimensional coordinate system used for display.
     *         Default to {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984}.
     */
    public CoordinateSystem getCoordinateSystem() {
        return renderer.getCoordinateSystem();
    }

    /**
     * Set the view coordinate system. This is the "real world" coordinate system to use for
     * displaying all features. Default is {@linkplain GeographicCoordinateSystem#WGS84  WGS
     * 1984}.   Changing this coordinate system has no effect on any underlying data,  since
     * transformation are performed only at rendering time.
     *
     * @param cs The view coordinate system. If this coordinate system has
     *           more than 2 dimensions, then only the 2 first will be retained.
     * @throws TransformException If <code>cs</code> can't be reduced to a two-dimensional
     *         coordinate system., or if data can't be transformed for some other reason.
     */
    public void setCoordinateSystem(CoordinateSystem cs) throws TransformException {
        renderer.setCoordinateSystem(cs);
    }

    /**
     * Returns a bounding box representative of the geographic area to drawn.
     * The default implementation computes the area from all layers currently
     * registered in the {@linkplain #getRenderer renderer}.
     *
     * @return The enclosing area to be drawn with the default zoom,
     *         or <code>null</code> if this area can't be computed.
     *
     * @see #setPreferredArea
     * @see Renderer#getPreferredArea
     */
    public Rectangle2D getArea() {
        return renderer.getPreferredArea();
    }

    /**
     * Returns the preferred pixel size in "real world" coordinates. When user ask for a
     * "close zoom", <code>WCTZoomPane</code> will adjusts the map scale in such a way that
     * structures of <code>preferredPixelSize</code> will appears as one pixel on the
     * screen device.
     *
     * @see Renderer#getPreferredPixelSize
     */
    protected Dimension2D getPreferredPixelSize() {
        final Dimension2D size = renderer.getPreferredPixelSize();
        if (size != null) {
            return size;
        }
        return super.getPreferredPixelSize();
    }

    /**
     * Returns the default size for this component.  This is the size
     * returned by {@link #getPreferredSize} if no preferred size has
     * been explicitly set.
     */
    protected Dimension getDefaultSize() {
        return new Dimension(512,512);
    }

    /**
     * Returns the {@linkplain Renderer renderer} for this map pane.
     *
     * @see Renderer#addLayer
     * @see Renderer#removeLayer
     * @see Renderer#removeAllLayers
     * @see Renderer#getLayers
     * @see Renderer#getLayerCount
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Paint this <code>WCTMapPane</code> and all visible layers.
     *
     * @param graphics The graphics context.
     */
    protected void paintComponent(final Graphics2D graphics) {
        renderer.paint(graphics, getZoomableBounds(null), zoom, false);
    }

    /**
     * Print this <code>WCTMapPane</code> and all visible layers.
     *
     * @param graphics The graphics context.
     */
    protected void printComponent(final Graphics2D graphics) {
        renderer.paint(graphics, getZoomableBounds(null), zoom, true);
    }

    /**
     * Returns the default tools to use when no {@linkplain RendererLayer#getTools layer's tools}
     * can do the job. If no default tools has been set, then returns <code>null</code>.
     *
     * @see Tools#getToolTipText
     * @see Tools#getPopupMenu
     * @see Tools#mouseClicked
     */
//    public Tools getTools() {
//        return renderer.getTools();
//    }

    /**
     * Set the default tools to use when no {@linkplain RendererLayer#getTools layer's tools}
     * can do the job.
     *
     * @param tools The new tools, or <code>null</code> for removing any set of tools.
     */
//    public void setTools(final Tools tools) {
//        renderer.setTools(tools);
//    }

    /**
     * Registers the default text to display in a tool tip. The text displays
     * when the cursor lingers over the component and no layer has proposed a
     * tool tip (i.e. {@link Renderer#getToolTipText} returned <code>null</code>
     * for all registered layers).
     *
     * @param tooltip The default tooltip, or <code>null</code> if none.
     */
    public void setToolTipText(final String tooltip) {
        super.setToolTipText(tooltip);
        if (tooltip == null) {
            /*
             * If the tool tip text is set to null, then JComponent unregister itself.
             * We need to be re-registered it if we want tool tips for rendered layers.
             */
            ToolTipManager.sharedInstance().registerComponent(this);
        }
    }

    /**
     * Returns the string to be used as the tooltip for a given mouse event.
     * The default implementation delegates to {@link Renderer#getToolTipText}.
     *
     * @param  event The mouse event.
     * @return The tool tip text, or <code>null</code> if there is no tool tip for this location.
     *
     * @see #setToolTipText
     * @see Renderer#getToolTipText
     */
    public String getToolTipText(final MouseEvent event) {
        final String text = renderer.getToolTipText((GeoMouseEvent)event);
        if (text != null) {
            return text;
        }
        return super.getToolTipText(event);
    }

    /**
     * Returns the popup menu to appears for a given mouse event. This method invokes
     * {@link ContextualMenuTool#getPopupMenu} in decreasing
     * {@linkplain RenderedLayer#getZOrder z-order} until one is found to
     * returns a non-null menu. If no layer has a popup menu for this event,
     * then this method returns {@link #getDefaultPopupMenu}.
     *
     * @param  event The mouse event.
     * @return The popup menu for this event, or <code>null</code> if there is none.
     *
     * @see #getDefaultPopupMenu
     */
    protected JPopupMenu getPopupMenu(final MouseEvent event) {
        final Action[] actions = null; // TODO: search for actions here.
        if (actions == null) {
            return getDefaultPopupMenu((GeoMouseEvent) event);
        }
        /*
         * Check if a menu exists for the specified actions. Most recently
         * used menus appears first in the cache and are check first.
         */
        if (cachedMenus != null) {
            for (final Iterator it=cachedMenus.iterator(); it.hasNext();) {
                final JPopupMenu menu = (JPopupMenu) it.next();
                final Object prop = menu.getClientProperty("LayerActions");
                if (!(prop instanceof Action[])) {
                    it.remove();
                    continue;
                }
                if (Arrays.equals((Action[])prop, actions)) {
                    if (menu != cachedMenus.getFirst()) {
                        it.remove();
                        cachedMenus.addFirst(menu);
                    }
                    return menu;
                }
            }
        } else {
            cachedMenus = new LinkedList();
        }
        /*
         * The menu was not in the cache. Built it now and add it to the cache.
         */
        final JPopupMenu menu = new JPopupMenu();
        for (int i=0; i<actions.length; i++) {
            final Action action = actions[i];
            if (action != null) {
                menu.add(action);
            } else {
                menu.addSeparator();
            }
        }
        menu.putClientProperty("LayerActions", actions);
        cachedMenus.addFirst(menu);
        while (cachedMenus.size() > MAX_CACHED_MENUS) {
            // Should not loops more than 1 time.
            cachedMenus.removeLast();
        }
        return menu;
    }

    /**
     * Returns a default popup menu for the given mouse event. This method
     * is invoked when no layers proposed a popup menu for this event. The
     * default implementation returns a menu with navigation options.
     *
     * @see #getPopupMenu
     * @see WCTZoomPane#getPopupMenu
     */
    protected JPopupMenu getDefaultPopupMenu(final GeoMouseEvent event) {
        return super.getPopupMenu(event);
    }

    /**
     * Processes mouse events occurring on this component. This method overrides the
     * default AWT's implementation in order to wrap the <code>MouseEvent</code> into
     * a {@link GeoMouseEvent}. Then, the default AWT's implementation is invoked in
     * order to pass this event to any registered {@link MouseListener} objects.
     */
    protected void processMouseEvent(final MouseEvent event) {
        super.processMouseEvent(new GeoMouseEvent(event, renderer));
    }

    /**
     * Processes mouse motion events occurring on this component. This method overrides
     * the default AWT's implementation in order to wrap the <code>MouseEvent</code> into
     * a {@link GeoMouseEvent}. Then, the default AWT's implementation is invoked in
     * order to pass this event to any registered {@link MouseMotionListener} objects.
     */
    protected void processMouseMotionEvent(final MouseEvent event) {
        super.processMouseMotionEvent(new GeoMouseEvent(event, renderer));
    }
}
