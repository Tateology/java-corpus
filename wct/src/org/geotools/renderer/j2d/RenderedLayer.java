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

// Geometric shapes
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.media.jai.PlanarImage;
import javax.swing.Action;

import org.geotools.cs.CompoundCoordinateSystem;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.LocalCoordinateSystem;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.Utilities;
import org.geotools.resources.XMath;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.renderer.ResourceKeys;
import org.geotools.resources.renderer.Resources;
import org.opengis.referencing.operation.TransformException;


/**
 * Base class for layers to be rendered using the {@linkplain Renderer renderer for Java2D}.
 * When a layer is being {@linkplain Renderer#addLayer(RenderedLayer) added} to a renderer,
 * the following methods are automatically invoked:
 *
 * <blockquote><pre>
 * {@link #setCoordinateSystem setCoordinateSystem}({@link Renderer#getCoordinateSystem renderingCS});
 * {@link #setVisible setVisible}(true);
 * </pre></blockquote>
 *
 * @version $Id: RenderedLayer.java 10254 2005-01-05 02:53:49Z jmacgill $
 * @author Martin Desruisseaux
 *
 * @see Renderer
 * @see RenderingContext
 */
public abstract class RenderedLayer {
    /**
     * The default {@linkplain #getZOrder z-order}.
     */
    private static final float DEFAULT_Z_ORDER = Float.POSITIVE_INFINITY;

    /**
     * The default stroke to use if no stroke can be infered from
     * {@link #getPreferredPixelSize}.
     */
    static final Stroke DEFAULT_STROKE = new BasicStroke(0);

    /**
     * The renderer that own this layer, or <code>null</code> if this layer has not yet
     * been added to a renderer. This field is modified by {@link Renderer} only when
     * this layer is added or removed to the renderer.
     */
    transient Renderer renderer;

    /**
     * The coordinate system for this layer. Methods {@link #getPreferredArea} and
     * {@link #setPreferredArea} uses this coordinate system. This field must never
     * be null. The default constructor initialize it to a
     * {@linkplain LocalCoordinateSystem#CARTESIAN cartesian coordinate system}.
     */
    private CoordinateSystem coordinateSystem;

    /**
     * The widget area (in screen coordinates) enqueued for painting, or <code>null</code>
     * if no painting is in process. This field is set by the {@link Renderer} only when its
     * {@link Renderer#paint} method is begining its work, and reset to <code>null</code> as
     * soon as this layer has been painted. This information is used by {@link #repaintComponent}
     * in order to avoid repainting twice the same area.
     */
    private transient Shape dirtyArea;

    /**
     * Forme géométrique englobant la région dans laquelle la couche a été dessinée lors du
     * dernier appel de la méthode {@link #paint}.  Les coordonnées de cette région doivent
     * être en exprimées en coordonnées de Java2D ({@link RenderingContext#textCS}).
     * La valeur <code>null</code> signifie qu'on peut considérer que cette couche occupe la
     * totalité de la surface dessinable.
     */
    private transient Shape paintedArea;

    /**
     * Coordonnées géographiques couvertes par cette couche. Ces coordonnées doivent
     * être expriméees selon le système de coordonnées <code>coordinateSystem</code>.
     * Une valeur nulle signifie que cette couche n'a pas de limites bien délimitées.
     *
     * @see #getPreferredArea
     * @see #setPreferredArea
     */
    private Rectangle2D preferredArea;

    /**
     * Dimension préférée des pixels pour un zoom rapproché. Une valeur
     * nulle signifie qu'aucune dimension préférée n'a été spécifiée.
     *
     * @see #getPreferredPixelSize
     * @see #setPreferredPixelSize
     */
    private Dimension2D preferredPixelSize;

    /**
     * Largeur par défaut des lignes à tracer. La valeur <code>null</code>
     * signifie que cette largeur doit être recalculée. Cette largeur sera
     * déterminée à partir de la valeur de {@link #preferredPixelSize}.
     */
    private transient Stroke stroke;

    /**
     * Indique si cette couche est visible. Les couches sont invisibles par défaut.
     * L'appel de {@link Renderer#addLayer(RenderedLayer)} appelera systématiquement
     * <code>setVisible(true)</code>.
     *
     * @see #setVisible
     */
    private boolean visible;

    /**
     * Ordre <var>z</var> à laquelle cette couche doit être dessinée. Les couches avec un
     * <var>z</var> élevé seront dessinées par dessus les couches avec un <var>z</var> bas.
     * Typiquement, cet ordre <var>z</var> devrait être l'altitude en mètres de la couche
     * (par exemple -30 pour l'isobath à 30 mètres de profondeur). La valeur
     * {@link Float#POSITIVE_INFINITY} fait dessiner une couche par dessus tout le reste,
     * tandis que la valeur {@link Float#NEGATIVE_INFINITY} la fait dessiner en dessous.
     * La valeur {@link Float#NaN} signifie que l'ordre z n'a pas encore été défini.
     *
     * @see #getZOrder
     * @see #setZOrder
     */
    private float zOrder = Float.NaN;

    /**
     * Listeners to be notified about any changes in this layer's properties.
     * Examples of properties that may change are:
     * <code>"coordinateSystem"</code>,
     * <code>"preferredArea"</code>,
     * <code>"preferredPixelSize"</code>,
     * <code>"zOrder"</code> and
     * <code>"visible"</code>.
     */
    protected final PropertyChangeSupport listeners;

    /**
     * The format used during the last call to {@link #getName}. We use only one instance for
     * all layers, since an application is likely to use only one locale. However, more locales
     * are allowed; it will just be slower.
     */
    private static Format format;

    /**
     * Convenience class for {@link RenderedLayer#getName}.
     * This class should be immutable and thread-safe.
     */
    private static final class Format {
        /** The locale of the {@link #format}. */
        public final Locale locale;

        /** The format in the {@link #locale}. */
        public final NumberFormat format;

        /** Construct a format for the given locale. */
        public Format(final Locale locale) {
            this.locale = locale;
            this.format = NumberFormat.getNumberInstance(locale);
        }
    }

    /**
     * Construct a new rendered layer. The {@linkplain #getCoordinateSystem coordinate system}
     * default to a {@linkplain LocalCoordinateSystem#PROMISCUOUS local cartesian} one and the
     * {@linkplain #getZOrder z-order} default to positive infinity (i.e. this layer is drawn
     * on top of everything else). Subclasses should invokes <code>setXXX</code> methods in
     * order to define properly this layer's properties.
     *
     * @see #setCoordinateSystem
     * @see #setPreferredArea
     * @see #setPreferredPixelSize
     * @see #setZOrder
     */
    public RenderedLayer() {
        this(LocalCoordinateSystem.PROMISCUOUS);
    }

    /**
     * Construct a new rendered layer using the specified coordinate system. The
     * {@linkplain #getZOrder z-order} default to positive infinity (i.e. this layer
     * is drawn on top of everything else). Subclasses should invokes <code>setXXX</code>
     * methods in order to define properly this layer's properties.
     *
     * @param  cs The coordinate system. If the specified coordinate system has more than
     *            two dimensions, then it must be a {@link CompoundCoordinateSystem} with
     *            a two dimensional {@link CompoundCoordinateSystem#getHeadCS headCS}.
     * @throws IllegalArgumentException if <code>cs</code> is nul.
     *
     * @see #setCoordinateSystem
     * @see #setPreferredArea
     * @see #setPreferredPixelSize
     * @see #setZOrder
     */
    public RenderedLayer(final CoordinateSystem cs) {
        if (cs == null) {
            throw new IllegalArgumentException(Resources.getResources(getLocale())
                      .getString(ResourceKeys.ERROR_BAD_ARGUMENT_$2, "cs", cs));
        }
        coordinateSystem = cs;
        listeners = new PropertyChangeSupport(this);
    }

    /**
     * Returns this layer's name. The default implementation returns
     * only the {@linkplain #getZOrder z-order}  formatted according
     * the given locale.
     *
     * @param  locale The desired locale, or <code>null</code> for a default locale.
     * @return This layer's name.
     *
     * @see #getLocale
     * @see Renderer#getName
     */
    public String getName(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        Format format = this.format; // Avoid the need for synchronization.
        if (format==null || !format.locale.equals(locale)) {
            this.format = format = new Format(locale);
        }
        final StringBuffer buffer = new StringBuffer("z=");
        return format.format.format(getZOrder(), buffer, new FieldPosition(0)).toString();
    }

    /**
     * Returns to locale for this layer. The default implementation inherit the locale
     * of its {@link Renderer}, if it has one. Otherwise, a default locale is returned.
     *
     * @see Renderer#getLocale
     * @see Component#getLocale
     */
    public Locale getLocale() {
        final Renderer renderer = this.renderer;
        return (renderer!=null) ? renderer.getLocale() : Locale.getDefault();
    }

    /**
     * Returns the renderer which own this layer.
     *
     * @return The renderer (never <code>null</code>).
     * @throws IllegalStateException if this layer has not been added to any renderer.
     */
    public Renderer getRenderer() throws IllegalStateException {
        final Renderer renderer = this.renderer;
        if (renderer != null) {
            return renderer;
        }
        throw new IllegalStateException(); // TODO: add a localized message.
    }

    /**
     * Returns the two-dimensional rendering coordinate system. This is usually the
     * {@linkplain Renderer#getCoordinateSystem renderer's coordinate system}. This
     * CS is always two-dimensional and is used by most methods like {@link #getPreferredArea}
     * and {@link #getPreferredPixelSize}.
     *
     * @return The coordinate system for this rendered layer.
     *
     * @see #setCoordinateSystem
     * @see #getPreferredArea
     * @see #getPreferredPixelSize
     * @see RenderingContext#mapCS
     */
    public final CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set the rendering coordinate system for this layer. This method is usually invoked
     * in any of the following cases:
     * <ul>
     *   <li>From this <code>RenderedLayer</code>'s constructor.</li>
     *   <li>When this layer has just been added to a {@link Renderer}.</li>
     *   <li>When {@link Renderer#setCoordinateSystem} has been invoked.</li>
     * </ul>
     * This method transforms the {@linkplain #getPreferredArea preferred area} if needed.
     * Subclasses should overrides this method and transform their internal data here.
     *
     * @param  cs The coordinate system. If the specified coordinate system has more than
     *            two dimensions, then it must be a {@link CompoundCoordinateSystem} with
     *            a two dimensional {@link CompoundCoordinateSystem#getHeadCS headCS}.
     * @throws TransformException If <code>cs</code> can't be reduced to a two-dimensional
     *         coordinate system, or if this method do not accept the new coordinate system
     *         for some other reason. In case of failure, this method should keep the old CS
     *         and leave this layer in a consistent state.
     */
    protected void setCoordinateSystem(final CoordinateSystem cs) throws TransformException {
        if (cs == null) {
            throw new IllegalArgumentException(Resources.getResources(getLocale())
                      .getString(ResourceKeys.ERROR_BAD_ARGUMENT_$2, "cs", cs));
        }
        final CoordinateSystem oldCS;
        synchronized (getTreeLock()) {
            oldCS = coordinateSystem;
            final CoordinateSystem coordinateSystem = CTSUtilities.getCoordinateSystem2D(cs);
            if (!oldCS.equals(coordinateSystem, false)) {
                if (preferredArea != null) {
                    /*
                     * If the preferred area need to be updated, update it now.
                     * The preferred pixel size will be updated in same time.
                     */
                    final MathTransform2D transform;
                    if (renderer != null) {
                        transform = (MathTransform2D)renderer.getMathTransform(
                                   oldCS, coordinateSystem, "RenderedLayer", "setCoordinateSystem");
                    } else {
                        transform = (MathTransform2D)CoordinateTransformationFactory.getDefault()
                           .createFromCoordinateSystems(oldCS, coordinateSystem).getMathTransform();
                    }
                    final Point2D origin = new Point2D.Double(preferredArea.getCenterX(),
                                                              preferredArea.getCenterY());
                    preferredArea = CTSUtilities.transform(transform, preferredArea, preferredArea);
                    if (preferredPixelSize != null) {
                        Point2D pt = new Point2D.Double(preferredPixelSize.getWidth(),
                                                        preferredPixelSize.getHeight());
                        pt = CTSUtilities.deltaTransform(transform, origin, pt, pt);
                        preferredPixelSize.setSize(pt.getX(), pt.getY());
                    }
                } else {
                    preferredPixelSize = null;
                }
                clearCache();
            }
            /*
             * Really changes the coordinate system only once we know that
             * the transformation was okay.
             */
            this.coordinateSystem = coordinateSystem;
        }
        listeners.firePropertyChange("coordinateSystem", oldCS, cs);
        repaint();
    }

    /**
     * Returns the preferred area for this layer. This is the default area to show before any
     * zoom is applied. This is usually (but not always) the bounding box of the underlying data.
     *
     * @return The preferred area in the {@linkplain #getCoordinateSystem rendering coordinate
     *         system}, or <code>null</code> if unknow or not applicable.
     *
     * @see #getPreferredPixelSize
     * @see #getCoordinateSystem
     */
    public Rectangle2D getPreferredArea() {
        final Rectangle2D preferredArea = this.preferredArea;
        return (preferredArea!=null) ? (Rectangle2D) preferredArea.clone() : null;
    }

    /**
     * Set the preferred area for this layer. This method do not change the georeferencing of
     * the layer data. The preferred area change only the default area to be shown in a window.
     *
     * @see #getPreferredArea
     * @see #setPreferredPixelSize
     * @see #getCoordinateSystem
     */
    public void setPreferredArea(final Rectangle2D area) {
        final Rectangle2D oldArea;
        synchronized (getTreeLock()) {
            paintedArea = null;
            oldArea = preferredArea;
            preferredArea = (area!=null) ? (Rectangle2D)area.clone() : null;
        }
        listeners.firePropertyChange("preferredArea", oldArea, area);
    }

    /**
     * Returns the preferred pixel size in rendering coordinates. For image layers, this is
     * the size of image's pixels. For other kind of layers, "pixel size" are to be understood
     * as some dimension representative of the layer's resolution.
     *
     * @return The preferred pixel size in this {@linkplain #getCoordinateSystem rendering
     *         coordinate system}, or <code>null</code> if none.
     *
     * @see #getPreferredArea
     * @see #getCoordinateSystem
     */
    public Dimension2D getPreferredPixelSize() {
        final Dimension2D preferredPixelSize = this.preferredPixelSize;
        return (preferredPixelSize!=null) ? (Dimension2D) preferredPixelSize.clone() : null;
    }

    /**
     * Set the preferred pixel size in "real world" coordinates. For images, this is the
     * size of image's pixels in units of {@link #getCoordinateSystem}. For other kind of
     * layers, "pixel size" is to be understood as some raisonable resolution for the
     * underlying data. For example a geometry layer may returns the geometry's mean resolution.
     *
     * @param size The preferred pixel size, or <code>null</code> if there is none.
     *
     * @see #getPreferredPixelSize
     * @see #setPreferredArea
     * @see #getCoordinateSystem
     */
    public void setPreferredPixelSize(final Dimension2D size) {
        final Dimension2D oldSize;
        synchronized (getTreeLock()) {
            stroke = null;
            oldSize = preferredPixelSize;
            preferredPixelSize = (size!=null) ? (Dimension2D)size.clone() : null;
        }
        listeners.firePropertyChange("preferredPixelSize", oldSize, size);
    }

    /**
     * Returns the <var>z-order</var> for this layer. Layers with highest <var>z-order</var>
     * will be painted on top of layers with lowest <var>z-order</var>. The default value is
     * {@link Float#POSITIVE_INFINITY}.
     *
     * @see #setZOrder
     */
    public float getZOrder() {
        final float zOrder = this.zOrder; // Avoid synchronization
        return Float.isNaN(zOrder) ? DEFAULT_Z_ORDER : zOrder;
    }

    /**
     * Set the <var>z-order</var> for this layer. Layers with highest <var>z-order</var>
     * will be painted on top of layers with lowest <var>z-order</var>. The specified
     * <var>z-order</var> replaces the default value returned by {@link #getZOrder}.
     *
     * @throws IllegalArgumentException if the specified <code>zOrder</code> is {@link Float#NaN}.
     */
    public void setZOrder(final float zOrder) throws IllegalArgumentException {
        if (Float.isNaN(zOrder)) {
            throw new IllegalArgumentException(String.valueOf(zOrder));
        }
        final float oldZOrder;
        synchronized (getTreeLock()) {
            oldZOrder = this.zOrder;
            if (zOrder == oldZOrder) {
                return;
            }
            this.zOrder = zOrder;
            if (renderer != null) {
                renderer.flushOffscreenBuffer(oldZOrder);
            }
            repaint();
        }
        listeners.firePropertyChange("zOrder", Float.isNaN(oldZOrder) ? null :
                                     new Float(oldZOrder), new Float(zOrder));
    }

    /**
     * Returns <code>true</code> if the {@linkplain #getZOrder z-order} has been explicitely set.
     */
    final boolean isZOrderSet() {
        return !Float.isNaN(zOrder);
    }

    /**
     * Determines whether this layer should be visible when its container is visible.
     *
     * @return <code>true</code> if the layer is visible, <code>false</code> otherwise.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Spécifie si cette couche doit être visible ou non. Cette méthode peut être
     * appelée pour cacher momentanément une couche. Elle est aussi appelée de
     * façon systématique lorsque cette couche est ajoutée ou retirée d'un
     * {@link Renderer}:
     *
     * <ul>
     *   <li><code>{@link Renderer#addLayer(RenderedLayer) Renderer.addLayer}(this)</code>
     *       appelera <code>setVisible(true)</code>.</li>
     *   <li><code>{@link Renderer#removeLayer(RenderedLayer) Renderer.removeLayer}(this)</code>
     *       appelera <code>setVisible(false)</code>.</li>
     * </ul>
     */
    public void setVisible(final boolean visible) {
        synchronized (getTreeLock()) {
            if (visible == this.visible) {
                return;
            }
            this.visible = visible;
            repaint();
        }
        listeners.firePropertyChange("visible", !visible, visible);
    }

    /**
     * Set the dirty area for all layers. This method is invoked by {@link Renderer#paint} only.
     */
    static void setDirtyArea(final RenderedLayer[] layers, int count, final Shape area) {
        while (--count >= 0) {
            layers[count].dirtyArea = area;
        }
    }

    /**
     * Advises that this layer need to be repainted. The layer will not be repainted immediately,
     * but at some later time depending on the widget implementation (e.g. <cite>Swing</cite>).
     * This <code>repaint()</code> method can be invoked from any thread; it doesn't need to be
     * the <cite>Swing</cite> thread.
     * <br><br>
     * Note that this method repaint only the area rendered during the last to {@link #paint}.
     * If this layer now cover a wider area, then the area to repaint must be specified with
     * a call to {@link #repaint(Rectangle2D)} instead.
     */
    public void repaint() {
        repaintComponent(paintedArea!=null ? paintedArea.getBounds() : null);
    }

    /**
     * Advises that some region need to be repainted. This layer will not be repainted immediately,
     * but at some later time depending on the widget implementation (e.g. <cite>Swing</cite>).
     * This <code>repaint(...)</code> method can be invoked from any thread; it doesn't need to
     * be the <cite>Swing</cite> thread.
     *
     * @param bounds The dirty region to repaint, in the "real world"
     *        {@linkplain #getCoordinateSystem rendering coordinate system}. A
     *        <code>null</code> value repaint everything.
     */
    public void repaint(final Rectangle2D bounds) {
        if (bounds != null) {
            final Renderer renderer = this.renderer;
            if (renderer != null) {
                repaintComponent(renderer.mapToText(bounds));
                return;
            }
        }
        repaint();
    }

    /**
     * Indique qu'une partie de cette couche a besoin d'être redéssinée.
     * Cette méthode peut être appelée à partir de n'importe quel thread
     * (pas nécessairement celui de <i>Swing</i>).
     *
     * @param bounds Coordonnées (en points) de la partie à redessiner.
     */
    final void repaintComponent(final Rectangle bounds) {
        /*
         * Implementation note: this method copy references 'dirtyArea', 'renderer' and
         * 'mapPane' in order to avoid the need for synchronization. According the Java
         * language specification, copying 32 bits is always thread-safe.
         */
        final Shape dirtyArea = this.dirtyArea;
        if (dirtyArea!=null && dirtyArea.contains(bounds!=null ? bounds : XRectangle2D.INFINITY)) {
            /*
             * If this layer is already scheduled for painting, do not enqueu an other 'paint'
             * request. This slight optimization may occurs when this layer changed its state
             * after the renderer started to paint but before the paint process reach this layer.
             * This layer may have changed its state as a result of a "scale" event.
             */
            return;
        }
        final Renderer renderer = this.renderer;
        if (renderer == null) {
            return;
        }
        renderer.flushOffscreenBuffer(zOrder);
        final Component mapPane = renderer.mapPane;
        if (mapPane == null) {
            return;
        }
        if (EventQueue.isDispatchThread()) {
            if (bounds == null) {
                mapPane.repaint();
            } else {
                mapPane.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    repaintComponent(bounds);
                }
            });
        }
        if (Renderer.LOGGER.isLoggable(Level.FINEST)) {
            final Locale locale = getLocale();
            final LogRecord record = Resources.getResources(locale).getLogRecord(Level.FINEST,
                                     ResourceKeys.SEND_REPAINT_EVENT_$5, new Object[]{getName(locale),
                                     new Integer(bounds.x), new Integer(bounds.x+bounds.width-1),
                                     new Integer(bounds.y), new Integer(bounds.y+bounds.height-1)});
            record.setSourceClassName("RenderedLayer");
            record.setSourceMethodName("repaint");
            Renderer.LOGGER.log(record);
        }
    }

    /**
     * Paint this object. This method is invoked by {@link Renderer} every time this layer needs
     * to be repainted. By default, painting is done in the {@linkplain RenderingContext#mapCS
     * rendering coordinate system} (usually "real world" metres). This method is responsible for
     * transformations from its own underlying data CS to the {@linkplain RenderingContext#mapCS
     * rendering CS} if needed. The {@link RenderingContext} object provides informations for such
     * transformations:
     *
     * <ul>
     * <li><p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(underlyingCS,
     *                      context.{@link RenderingContext#mapCS mapCS} )</code><br>
     * Returns a transform from the underlying CS to the rendering CS.</p></li>
     *
     * <li><p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(
     *                      context.{@link RenderingContext#mapCS mapCS},
     *                      context.{@link RenderingContext#textCS textCS} )</code><br>
     * Returns a transform from the rendering CS to the Java2D CS in "dots" units
     * (usually 1/72 of inch). This transformation is zoom dependent.</p></li>
     *
     * <li><p><code>context.{@link RenderingContext#getMathTransform getMathTransform}(
     *                      context.{@link RenderingContext#textCS textCS},
     *                      context.{@link RenderingContext#deviceCS deviceCS} )</code><br>
     * Returns a transform from the Java2D CS to the device CS. This transformation is
     * device dependent, but not zoom sensitive. When the output device is the screen,
     * then this is the identity transform (except if the rendering occurs in a clipped
     * area of the widget).</p></li>
     * </ul>
     *
     * <p>The {@link RenderingContext} object can takes care of configuring {@link Graphics2D}
     * with the right transform for a limited set of particular CS (namely, only CS leading to
     * an {@linkplain AffineTransform affine transform}). This is convenient for switching between
     * {@linkplain RenderingContext#mapCS rendering CS} (the one used for drawing map features)
     * and {@linkplain RenderingContext#textCS Java2D CS} (the one used for rendering texts and
     * labels). Example:</p>
     *
     * <blockquote><pre>
     * &nbsp;Shape paint(RenderingContext context) {
     * &nbsp;    Graphics2D graphics = context.getGraphics();
     * &nbsp;    // <cite>Paint here map features in geographic coordinates (usually m or °)</cite>
     * &nbsp;    context.addPaintedArea(...); // Optional
     * &nbsp;
     * &nbsp;    context.setCoordinateSystem(context.textCS);
     * &nbsp;    // <cite>Write here text or label. Coordinates are in <u>dots</u>.</cite>
     * &nbsp;    context.addPaintedArea(...); // Optional
     * &nbsp;
     * &nbsp;    context.setCoordinateSystem(context.mapCS);
     * &nbsp;    // <cite>Continue here the rendering of map features in geographic coordinates</cite>
     * &nbsp;    context.addPaintedArea(...); // Optional
     * &nbsp;}
     * </pre></blockquote>
     *
     * During the rendering process, implementations are encouraged to declare a (potentially
     * approximative) bounding shape of their painted area with calls to
     * {@link RenderingContext#addPaintedArea(Shape)}. This is an optional operation: providing
     * those hints only help {@link Renderer} to speed up future rendering and events processing.
     *
     * @param  context Information relatives to the rendering context. This object ontains the
     *         {@link Graphics2D} to use and methods for getting {@link MathTransform} objects.
     *         This temporary object will be destroy once the rendering is completed. Consequently,
     *         do not keep a reference to it outside this <code>paint</code> method.
     * @throws TransformException If a coordinate transformation failed during the rendering
     *         process.
     */
    protected abstract void paint(final RenderingContext context) throws TransformException;

    /**
     * Paint this layer and update the {@link #paintedArea} field. If this layer is not visible
     * or if <code>clipBounds</code> doesn't intersect {@link #paintedArea}, then this method do
     * nothing.
     *
     * @param context Information relatives to the rendering context. Will be passed
     *        unchanged to {@link #paint}.
     * @param clipBounds The area to paint, in Java2D coordinates ({@link RenderingContext#textCS}).
     */
    final void update(final RenderingContext context, final Rectangle clipBounds)
            throws TransformException
    {
//        assert Thread.holdsLock(getTreeLock());
        if (visible) {
            //if (paintedArea==null || clipBounds==null || paintedArea.intersects(clipBounds)) {
            if (true) {  // steve.ansari addition from GT user forum to fix marker bug
                if (stroke == null) {
                    final Dimension2D s = getPreferredPixelSize();
                    if (s != null) {
                        stroke = new BasicStroke((float)XMath.hypot(s.getWidth(), s.getHeight()));
                    } else {
                        stroke = DEFAULT_STROKE;
                    }
                }
                context.getGraphics().setStroke(stroke);
                context.paintedArea = null;
                this.dirtyArea      = null;
                paint(context);
                this.paintedArea    = context.paintedArea;
                context.paintedArea = null;
            }
        }
    }

    /**
     * Hints that this layer might be painted in the near future. Some implementations may
     * spawn a thread to compute the data while others may ignore the hint. The default
     * implementation does nothing.
     *
     * @param  context Information relatives to the rendering context. This object contains
     *         methods for querying the area to be painted in arbitrary coordinate system.
     *         This temporary object will be destroy once the rendering is completed.
     *         Consequently, do not keep a reference to it outside this <code>prefetch</code>
     *         method.
     *
     * @see PlanarImage#prefetchTiles
     */
    protected void prefetch(final RenderingContext context) {
    }

    /**
     * Format a value for the current mouse position. This method doesn't have to format the
     * mouse coordinate (this is {@link MouseCoordinateFormat#format(GeoMouseEvent)} business).
     * Instead, it is invoked for formatting a value at current mouse position. For example a
     * remote sensing image of Sea Surface Temperature (SST) can format the temperature in
     * geophysical units (e.g. "12°C"). The default implementation do nothing and returns
     * <code>false</code>.
     *
     * @param  event The mouse event.
     * @param  toAppendTo The destination buffer for formatting a value.
     * @return <code>true</code> if this method has formatted a value, or <code>false</code>
     *         otherwise. If this method returns <code>true</code>, then the next layers (with
     *         smaller {@linkplain RenderedLayer#getZOrder z-order}) will not be queried.
     *
     * @see MouseCoordinateFormat#format(GeoMouseEvent)
     */
    boolean formatValue(final GeoMouseEvent event, final StringBuffer toAppendTo) {
        return false;
    }

    /**
     * Retourne le texte à afficher dans une bulle lorsque la souris traîne sur la couche.
     * L'implémentation par défaut retourne toujours <code>null</code>, ce qui signifie que
     * cette couche n'a aucun texte à afficher (les autres couches seront alors interrogées).
     * Les classes dérivées peuvent redéfinir cette méthode pour retourner un texte après avoir
     * vérifié que les coordonnées de <code>event</code> correspondent bien à un point de la
     * couche.
     *
     * <strong>Note: This method is not a commited part of the API.
     *         It may moves elsewhere in a future version.</strong>
     *
     * @param  event Coordonnées du curseur de la souris.
     * @return Le texte à afficher lorsque la souris traîne sur la couche.
     *         Ce texte peut être nul pour signifier qu'il ne faut rien écrire.
     *
     * @see Renderer#getToolTipText
     */
    String getToolTipText(final GeoMouseEvent event) {
        return null;
    }

    /**
     * Returns the action to run when some mouse action occured over this layer. The default
     * implementation return always <code>null</code>, which means that no action is defined
     * for this layer. Subclasses which override this method should check if the mouse cursor
     * is really over a component of this layer (for example over a geometry).
     *
     * <strong>Note: This method is not a commited part of the API.
     *         It may moves elsewhere in a future version.</strong>
     *
     * @param  event The mouse event.
     * @return The action for the layer, or <code>null</code> if none.
     */
    Action getAction(final GeoMouseEvent event) {
        return null;
    }

    /**
     * Tells if this layer <strong>may</strong> contains the specified point. This method
     * performs only a fast check. Subclasses will have to perform a more exhautive check
     * in their event processing methods. The coordinate system is the
     * {@link RenderingContext#textCS} used the last time this layer was rendered.
     *
     * @param  x <var>x</var> coordinate.
     * @param  y <var>y</var> coordinate.
     * @return <code>true</code> if this layer is visible and may contains the specified point.
     */
    final boolean contains(final int x, final int y) {
//        assert Thread.holdsLock(getTreeLock());
        if (visible) {
            final Shape paintedArea = this.paintedArea;
            return (paintedArea==null) || paintedArea.contains(x,y);
        }
        return false;
    }

    /**
     * Invoked every time the {@link Renderer}'s zoom changed. A zoom change require two
     * updates to {@link #paintedArea}:
     * <ul>
     *   <li>Since <code>paintedArea</code> is in {@link RenderingContext#textCS} and since
     *       the transform between the Java2D and the rendering CS is zoom-dependent, a change
     *       of zoom requires a change of <code>paintedArea</code>.</li>
     * <li>Since the zoom change may bring some new area inside the widget bounds, this new
     *     area may need to be rendered and should be added to <code>paintedArea</code>.</li>
     * </ul>
     *
     * Note: The <code>change</code> affine transform must be a change in the <strong>Java2D
     *       coordinate space</strong> ({@link RenderingContext#textCS}). But the transform
     *       given by {@link org.geotools.gui.swing.ZoomPane#fireZoomChange} is in the rendering
     *       coordinate space ({@link RenderingContext#mapCS}). Conversion can be performed
     *       as this:
     *
     *       Lets <var>C</var> by the change in Java2D space, and <var>Z</var> be the
     *       {@linkplain org.geotools.gui.swing.ZoomPane#zoom zoom}.  Then the change
     *       in Java2D space is <code>ZCZ<sup>-1</sup></code>.
     *
     *       Additionnaly, in order to avoir rounding error, it may be safe to expand slightly
     *       the transformed shape. It may be done with the following operations on the change
     *       matrix, where (x,y) is the widget center:
     *       <blockquote><pre>
     *          translate(x, y);           // final translation
     *          scale(1.00001, 1.00001);   // scale around anchor
     *          translate(-x, -y);         // translate anchor to origin
     *       </pre></blockquote>
     *
     * @param change The zoom <strong>change</strong> in <strong>Java2D</strong> coordinate
     *        system, or <code>null</code> if unknow. If <code>null</code>, then this layer
     *        will be fully redrawn during the next rendering.
     */
    void zoomChanged(final AffineTransform change) {
//        assert Thread.holdsLock(getTreeLock());
        if (paintedArea == null) {
            return;
        }
        if (change!=null && renderer!=null) {
            final Component mapPane = renderer.mapPane;
            if (mapPane != null) {
                final Area newArea = new Area(mapPane.getBounds());
                newArea.subtract(newArea.createTransformedArea(change));
                final Area area = (paintedArea instanceof Area) ?    (Area)paintedArea
                                                                : new Area(paintedArea);
                area.transform(change);
                area.add(newArea);
                paintedArea = area;
                return;
            }
        }
        paintedArea = null;
    }

    /**
     * Add a property change listener to the listener list.  The listener is registered for
     * all properties. For example, methods {@link #setVisible}, {@link #setZOrder},
     * {@link #setPreferredArea} and {@link #setPreferredPixelSize} will fire
     * <code>"visible"</code>, <code>"zOrder"</code>, <code>"preferredArea"</code>
     * and <code>"preferredPixelSize"</code> change events respectively.
     * <br><br>
     * A particular event, namely <code>"scale"</code>, is also fired everytime
     * the zoom changes. It is particular in that this event results from a change in the
     * {@linkplain Renderer renderer} state rather than a change applied directly on this
     * layer.  However, since {@linkplain Renderer#getScale scale} changes are propagated
     * to all layers at rendering time, it makes sense to notify layer's listeners as well.
     * A layer can changes its own state as a result of a scale change; for example a layer
     * may hide or show more features. The scale factor is usually smaller than 1. For example
     * for a 1:1000 scale, the scale factor will be 0.001. This scale factor takes in account
     * the physical size of the rendering device (e.g. the screen size) if such information is
     * available. Note that this scale can't be more accurate than the
     * {@linkplain GraphicsConfiguration#getNormalizingTransform() information supplied
     * by the underlying system}.
     *
     * @param listener The property change listener to be added.
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
     * Returns the lock for synchronisation.
     */
    protected final Object getTreeLock() {
        final Renderer renderer = this.renderer;
        return (renderer!=null) ? (Object)renderer : (Object)this;
    }

    /**
     * Log a message saying that this layer is rebuilding its cache.
     *
     * @param classname The caller class name.
     */
    final void logUpdateCache(final String classname) {
        if (Renderer.LOGGER.isLoggable(Level.FINER)) {
            final Locale locale = getLocale();
            final LogRecord record = Resources.getResources(locale).getLogRecord(Level.FINER,
                                              ResourceKeys.UPDATE_CACHE_$1, getName(locale));
            record.setSourceClassName(classname);
            record.setSourceMethodName("paint");
            Renderer.LOGGER.log(record);
        }
    }

    /**
     * Efface les données qui avaient été conservées dans une cache interne. L'appel
     * de cette méthode permettra de libérer un peu de mémoire à d'autres fins. Elle
     * sera appelée lorsque qu'on aura déterminé que la couche <code>this</code>  ne
     * sera plus affichée avant un certain temps.  Cette méthode ne doit pas changer
     * le paramétrage de cette couche; son seul impact sera de rendre le prochain
     * traçage un peu plus lent.
     */
    void clearCache() {
//        assert Thread.holdsLock(getTreeLock());
        // Do not clear 'dirtyArea'; this is Renderer's job.
        paintedArea = null;
        stroke      = null;
    }

    /**
     * Provides a hint that a layer will no longer be accessed from a reference in user
     * space. The results are equivalent to those that occur when the program loses its
     * last reference to this layer, the garbage collector discovers this, and finalize
     * is called. This can be used as a hint in situations where waiting for garbage
     * collection would be overly conservative.
     * <br><br>
     * The results of referencing a layer after a call to <code>dispose()</code> are undefined.
     * However, invoking this method more than once is safe.  Note that this method is invoked
     * automatically by {@link Renderer#dispose}, but not from any {@link Renderer#removeLayer
     * remove(...)} method (in order to allow moving layers between different renderers).
     *
     * @see Renderer#dispose
     * @see PlanarImage#dispose
     */
    public void dispose() {
        synchronized (getTreeLock()) {
            if (renderer != null) {
                renderer.removeLayer(this);
            }
            clearCache();
            preferredArea      = null;
            preferredPixelSize = null;
            visible            = false;
            zOrder             = Float.NaN;
            coordinateSystem   = LocalCoordinateSystem.PROMISCUOUS;
            final PropertyChangeListener[] list = listeners.getPropertyChangeListeners();
            for (int i=list.length; --i>=0;) {
                listeners.removePropertyChangeListener(list[i]);
            }
        }
    }

    /**
     * Returns a string representation of this layer. This method is for debugging purpose
     * only and may changes in any future version.
     */
    public String toString() {
        return Utilities.getShortClassName(this) + '[' + getName(null) + ']';
    }
}

