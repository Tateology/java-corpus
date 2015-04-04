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
/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
 *     UNITED KINDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 */
package org.geotools.gui.swing;


// JTS dependencies
import java.awt.geom.Rectangle2D;

import org.geotools.cs.CoordinateSystem;
import org.geotools.map.MapContext;
import org.geotools.renderer.j2d.Renderer;
import org.geotools.renderer.j2d.StyledMapRenderer;

import com.vividsolutions.jts.geom.Envelope;


/**
 * A map pane which support styling.
 *
 * @author Martin Desruisseaux
 * @version $Id: NDITStyledMapPane.java,v 1.5 2004/03/26 19:06:50 aaime Exp $
 */
public class WCTStyledMapPane extends WCTMapPane {
    /** The model which stores a list of layers and bounding box. */
    private MapContext context;

    /**
     * Construct a default map panel.
     */
    public WCTStyledMapPane() {
        super();
    }

    /**
     * Construct a map panel using the specified coordinate system.
     *
     * @param cs The rendering coordinate system.
     */
    public WCTStyledMapPane(final CoordinateSystem cs) {
        super(cs);
    }

    /**
     * Create the renderer for this map pane. This method is invoked by the constructor at creation
     * time only.
     *
     * @return DOCUMENT ME!
     */
    Renderer createRenderer() {
        return new StyledMapRenderer(this);
    }

    /**
     * Returns the last context set with {@link #setContext}, or <code>null</code> if none.
     *
     * @return The map context.
     */
    public MapContext getMapContext() {
        return context;
    }

    /**
     * Set a new context as the current one. Invoking this method will remove all layers in the
     * {@linkPlain #getRenderer renderer} and replace them with new layers from the given context.
     *
     * @param context The new context, or <code>null</code> for removing any previous context.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void setMapContext(final MapContext context)
        throws Exception {
        this.context = context;
        ((StyledMapRenderer) getRenderer()).setMapContext(context);
        reset();
    }

    /**
     * Returns a bounding box representative of the geographic area to drawn. This method returns
     * the first of the following area available:
     * 
     * <ul>
     * <li>
     * If a {@linkPlain Context context} is set, then the context's {@linkPlain
     * Context#getBoundingBox bounding box} is returned.
     * </li>
     * <li>
     * Otherwise, the area of interest is computed from the layers currently registered in the
     * {@linkPlain #getRenderer renderer}.
     * </li>
     * </ul>
     * 
     *
     * @return The enclosing area to be drawn with the default zoom, or <code>null</code> if this
     *         area can't be computed.
     *
     * @see #setPreferredArea
     * @see Context#getBoundingBox
     * @see Renderer#getPreferredArea
     */
    public Rectangle2D getArea() {
        if (context != null) {
            final Envelope envelope = context.getAreaOfInterest();
            if (envelope != null) {
                return new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(),
                    envelope.getWidth(), envelope.getHeight());
            }
        }

        return super.getArea();
    }

    /**
     * Returns the {@linkPlain StyledRenderer styled renderer} for this map pane.
     *
     * @return DOCUMENT ME!
     *
     * @task TODO: Change the returns type to StyledRenderer when we will be allowed to compile
     *       with J2SE 1.5.
     */
    public Renderer getRenderer() {
        return super.getRenderer();
    }
}
