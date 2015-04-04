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
 * JNXMapContext.java
 *
 * Created on 22 novembre 2003, 15.22
 */
package org.geotools.map;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.cs.LocalCoordinateSystem;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.event.MapBoundsEvent;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListener;
import org.geotools.styling.Style;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class WCTMapContext implements MapContext {
    /** The logger for the map module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.map");
    List layerList = new ArrayList();
    CoordinateReferenceSystem crs = LocalCoordinateSystem.PROMISCUOUS;
    Envelope areaOfInterest = null;

    /** Utility field used by event firing mechanism. */
    protected javax.swing.event.EventListenerList listenerList = null;

    /** DOCUMENT ME! */
    protected MapLayerListener layerListener = new MapLayerListener() {
            public void layerChanged(MapLayerEvent event) {
                fireAsListEvent(event);
            }

            public void layerShown(MapLayerEvent event) {
                fireAsListEvent(event);
            }

            public void layerHidden(MapLayerEvent event) {
                fireAsListEvent(event);
            }

            private void fireAsListEvent(MapLayerEvent event) {
                MapLayer layer = (MapLayer) event.getSource();
                int position = layerList.indexOf(layer);
                fireMapLayerListListenerLayerChanged(new MapLayerListEvent(WCTMapContext.this,
                        layer, position, event));
            }
        };

    /** Holds value of property abstracts. */
    protected String abstracts;

    /** Utility field used by bound properties. */
    protected java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    /** Holds value of property contactInformation. */
    protected String contactInformation;

    /** Holds value of property keywords. */
    protected String[] keywords;

    /** Holds value of property title. */
    protected String title;

    /**
     * Creates a default empty map context
     */
    public WCTMapContext() {
        this.title = "";
        this.abstracts = "";
        this.contactInformation = "";
        this.keywords = new String[0];
    }

    /**
     * Creates a map context with the provided layers and title
     *
     * @param layers DOCUMENT ME!
     */
    public WCTMapContext(MapLayer[] layers) {
        this();
        setTitle(title);
        addLayers(layers);
    }

    /**
     * Creates a map context
     *
     * @param layers DOCUMENT ME!
     * @param title DOCUMENT ME!
     * @param contextAbstract DOCUMENT ME!
     * @param contactInformation DOCUMENT ME!
     * @param keywords DOCUMENT ME!
     */
    public WCTMapContext(MapLayer[] layers, String title, String contextAbstract,
        String contactInformation, String[] keywords) {
        this(layers);
        setTitle(title);
        setAbstract(contextAbstract);
        setContactInformation(contactInformation);
        setKeywords(keywords);
    }

    /**
     * Add a new layer if not already present and trigger a {@link LayerListEvent}.
     *
     * @param index DOCUMENT ME!
     * @param layer Then new layer that has been added.
     *
     * @return DOCUMENT ME!
     */
    public boolean addLayer(int index, MapLayer layer) {
        if (layerList.contains(layer)) {
            return false;
        }
        
        
        //if (index > layerList.size()) {
        //  ((ArrayList)layerList).ensureCapacity(index+1);
        //}
        //javax.swing.JOptionPane.showMessageDialog(null, "SIZE: "+layerList.size(), 
        // "MARKER", javax.swing.JOptionPane.ERROR_MESSAGE);
        

        layerList.add(index, layer);
        layer.addMapLayerListener(layerListener);
        fireMapLayerListListenerLayerAdded(new MapLayerListEvent(this, layer, index));

        return true;
    }

    /**
     * Add a new layer and trigger a {@link LayerListEvent}.
     *
     * @param layer Then new layer that has been added.
     *
     * @return DOCUMENT ME!
     */
    public boolean addLayer(MapLayer layer) {
        if (layerList.contains(layer)) {
            return false;
        }

        layerList.add(layer);
        layer.addMapLayerListener(layerListener);
        fireMapLayerListListenerLayerAdded(new MapLayerListEvent(this, layer, indexOf(layer)));

        return true;
    }

    /**
     * Add a new layer and trigger a {@link LayerListEvent}.
     *
     * @param featureSource Then new layer that has been added.
     * @param style DOCUMENT ME!
     */
    public void addLayer(FeatureSource featureSource, Style style) {
        DefaultMapLayer layer = new DefaultMapLayer(featureSource, style, "");
        this.addLayer(layer);
    }

    /**
     * Add a new layer and trigger a {@link LayerListEvent}.
     *
     * @param collection Then new layer that has been added.
     * @param style DOCUMENT ME!
     */
    public void addLayer(FeatureCollection collection, Style style) {
        DefaultMapLayer layer = new DefaultMapLayer(collection, style, "");
        this.addLayer(layer);
    }

    /**
     * Remove a layer and trigger a {@link LayerListEvent}.
     *
     * @param layer Then new layer that has been removed.
     *
     * @return DOCUMENT ME!
     */
    public boolean removeLayer(MapLayer layer) {
        int index = indexOf(layer);

        if (index == -1) {
            return false;
        } else {
            removeLayer(index);

            return true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public MapLayer removeLayer(int index) {
        MapLayer layer = (MapLayer) layerList.remove(index);

        layer.removeMapLayerListener(layerListener);
        fireMapLayerListListenerLayerRemoved(new MapLayerListEvent(this, layer, index));

        return layer;
    }

    /**
     * Add an array of new layers and trigger a {@link LayerListEvent}.
     *
     * @param layers The new layers that are to be added.
     *
     * @return DOCUMENT ME!
     */
    public int addLayers(MapLayer[] layers) {
        if ((layers == null) || (layers.length == 0)) {
            return 0;
        }

        int layerAdded = 0;
        MapLayer lastLayer = null;

        for (int i = 0; i < layers.length; i++) {
            if (!layerList.contains(layers[i])) {
                layerList.add(layers[i]);
                layerAdded++;
                lastLayer = layers[i];
                layers[i].addMapLayerListener(layerListener);
            }
        }

        if (layerAdded > 0) {
            int fromIndex = layerList.size() - layerAdded;
            int toIndex = layerList.size() - 1;

            if (layerAdded == 1) {
                fireMapLayerListListenerLayerAdded(new MapLayerListEvent(this, lastLayer,
                        fromIndex, toIndex));
            } else {
                fireMapLayerListListenerLayerAdded(new MapLayerListEvent(this, null, fromIndex,
                        toIndex));
            }
        }

        return layerAdded;
    }

    /**
     * Remove an array of layers and trigger a {@link LayerListEvent}.
     *
     * @param layers The layers that are to be removed.
     */
    public void removeLayers(MapLayer[] layers) {
        if ((layers == null) || (layers.length == 0) || (layerList.size() == 0)) {
            return;
        }

        // compute minimum and maximum index changed
        int fromIndex = layerList.size();
        int toIndex = 0;
        int layersRemoved = 0;

        for (int i = 0; i < layers.length; i++) {
            int index = layerList.indexOf(layers[i]);

            if (index == -1) {
                continue;
            }

            layersRemoved++;

            if (index < fromIndex) {
                fromIndex = index;
            }

            if (index > toIndex) {
                toIndex = index;
            }
        }

        // compute the maximum index when the layers will be removed
        toIndex = toIndex;

        // remove layers
        for (int i = 0; i < layers.length; i++) {
            if (layerList.remove(layers[i])) {
                layers[i].removeMapLayerListener(layerListener);
            }
        }

        // fire event
        fireMapLayerListListenerLayerRemoved(new MapLayerListEvent(this, null, fromIndex, toIndex));
    }

    /**
     * Return this model's list of layers.  If no layers are present, then an empty array is
     * returned.
     *
     * @return This model's list of layers.
     */
    public MapLayer[] getLayers() {
        MapLayer[] layers = new MapLayer[layerList.size()];

        return (MapLayer[]) layerList.toArray(layers);
    }

    /**
     * Return the requested layer.
     *
     * @param index index of layer to return.
     *
     * @return the layer at the specified position
     *
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public MapLayer getLayer(int index) throws IndexOutOfBoundsException {
        return (MapLayer) layerList.get(index);
    }

    /**
     * @see org.geotools.map.MapContext#indexOf(org.geotools.map.MapLayer)
     */
    public int indexOf(MapLayer layer) {
        return layerList.indexOf(layer);
    }

    /**
     * Returns an iterator over the layers in this context in proper sequence.
     *
     * @return an iterator over the layers in this context in proper sequence.
     */
    public Iterator iterator() {
        return layerList.iterator();
    }

    /**
     * Get the bounding box of all the layers in this MapContext. If all the layers cannot
     * determine the bounding box in the speed required for each layer, then null is returned. The
     * bounds will be expressed in the MapContext coordinate system.
     *
     * @return The bounding box of the features or null if unknown and too expensive for the method
     *         to calculate. TODO: when coordinate system information will be added reproject the
     *         bounds according to the current coordinate system
     *
     * @throws IOException DOCUMENT ME!
     */
    public Envelope getLayerBounds() throws IOException {
        Envelope result = null;

        for (int i = 0; i < layerList.size(); i++) {
            MapLayer layer = (MapLayer) layerList.get(i);
            FeatureSource fs = layer.getFeatureSource();
            Envelope env = fs.getBounds();

            if (env == null) {
                continue;
            } else {
                // TODO: reproject envelope here if needed
                if (result == null) {
                    result = env;
                } else {
                    result.expandToInclude(env);
                }
            }
        }

        return result;
    }

    /**
     * Set a new area of interest and trigger a {@link BoundingBoxEvent}. Note that this is the
     * only method to change coordinate system.  A <code>setCoordinateReferenceSystem</code>
     * method is not provided to ensure this class is not dependant on transform classes.
     *
     * @param areaOfInterest The new areaOfInterest.
     * @param coordinateReferenceSystem The coordinate system being using by this model.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setAreaOfInterest(Envelope areaOfInterest,
        CoordinateReferenceSystem coordinateReferenceSystem)
        throws IllegalArgumentException {
        if ((areaOfInterest == null) || (coordinateReferenceSystem == null)) {
            throw new NullPointerException();
        }

        Envelope oldAreaOfInterest = this.areaOfInterest;
        CoordinateReferenceSystem oldCrs = this.crs;

        this.areaOfInterest = new Envelope(areaOfInterest);
        this.crs = coordinateReferenceSystem;

        int type = MapBoundsEvent.AREA_OF_INTEREST_MASK | MapBoundsEvent.COORDINATE_SYSTEM_MASK;
        fireMapBoundsListenerMapBoundsChanged(new MapBoundsEvent(this, type, oldAreaOfInterest,
                oldCrs));
    }

    /**
     * Set a new area of interest and trigger an {@link BoundingBoxEvent}.
     *
     * @param areaOfInterest The new area of interest.
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setAreaOfInterest(Envelope areaOfInterest) {
        if (areaOfInterest == null) {
            throw new NullPointerException();
        }

        Envelope oldAreaOfInterest = this.areaOfInterest;
        this.areaOfInterest = areaOfInterest;

        fireMapBoundsListenerMapBoundsChanged(new MapBoundsEvent(this,
                MapBoundsEvent.AREA_OF_INTEREST_MASK, oldAreaOfInterest, this.crs));
    }

    /**
     * Gets the current area of interest. If no area of interest is the, the default is to fall
     * back on the layer bounds
     *
     * @return Current area of interest
     */
    public Envelope getAreaOfInterest() {
        if (areaOfInterest == null) {
            try {
                areaOfInterest = getLayerBounds();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Can't get layer bounds, and area of interest is not set",
                    e);

                return null;
            }
        }

        if (areaOfInterest == null) {
            return null;
        } else {
            return new Envelope(this.areaOfInterest);
        }
    }

    /**
     * Get the current coordinate system.
     *
     * @return the coordinate system of this box.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.crs;
    }

    /**
     * Transform the coordinates according to the provided transform. Useful for zooming and
     * panning processes.
     *
     * @param transform The transform to change area of interest.
     */
    public void transform(AffineTransform transform) {
        Envelope oldAreaOfInterest = this.areaOfInterest;

        double[] coords = new double[4];
        coords[0] = areaOfInterest.getMinX();
        coords[1] = areaOfInterest.getMinY();
        coords[2] = areaOfInterest.getMaxX();
        coords[3] = areaOfInterest.getMaxY();
        transform.transform(coords, 0, coords, 0, 2);
        this.areaOfInterest = new Envelope(coords[0], coords[2], coords[1], coords[3]);

        fireMapBoundsListenerMapBoundsChanged(new MapBoundsEvent(this,
                MapBoundsEvent.AREA_OF_INTEREST_MASK, oldAreaOfInterest, this.crs));
    }

    /**
     * DOCUMENT ME!
     *
     * @param sourcePosition DOCUMENT ME!
     * @param destPosition DOCUMENT ME!
     *
     * @throws IndexOutOfBoundsException DOCUMENT ME!
     */
    public void moveLayer(int sourcePosition, int destPosition) {
        if ((sourcePosition < 0) || (sourcePosition >= layerList.size())) {
            throw new IndexOutOfBoundsException("Source position " + sourcePosition
                + " out of bounds");
        }

        if ((destPosition < 0) || (destPosition >= layerList.size())) {
            throw new IndexOutOfBoundsException("Destination position " + destPosition
                + " out of bounds");
        }

        MapLayer layer = (MapLayer) layerList.remove(sourcePosition);
        layerList.add(destPosition, layer);
        fireMapLayerListListenerLayerMoved(new MapLayerListEvent(this, layer, Math.min(sourcePosition, destPosition),
            Math.max(sourcePosition, destPosition)));
    }

    /**
     * DOCUMENT ME!
     */
    public void clearLayerList() {
        for (int i = 0; i < layerList.size(); i++) {
            MapLayer layer = (MapLayer) layerList.get(i);
            layer.removeMapLayerListener(layerListener);
        }

        layerList.clear();

        fireMapLayerListListenerLayerRemoved(new MapLayerListEvent(this, null, 0, 1));
    }

    /**
     * Returns the number of layers in this map context
     *
     * @return the number of layers in this map context
     */
    public int getLayerCount() {
        return layerList.size();
    }

    /**
     * Getter for property abstracts.
     *
     * @return Value of property abstracts.
     */
    public String getAbstract() {
        return this.abstracts;
    }

    /**
     * Setter for property abstracts.
     *
     * @param abstractValue New value of property abstracts.
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setAbstract(String abstractValue) {
        if (abstractValue == null) {
            throw new NullPointerException();
        }

        String oldAbstracts = this.abstracts;
        this.abstracts = abstractValue;
        propertyChangeSupport.firePropertyChange("abstract", oldAbstracts, abstracts);
    }

    /**
     * Getter for property contactInformation.
     *
     * @return Value of property contactInformation.
     */
    public String getContactInformation() {
        return this.contactInformation;
    }

    /**
     * Setter for property contactInformation.
     *
     * @param contactInformation New value of property contactInformation.
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setContactInformation(String contactInformation) {
        if (contactInformation == null) {
            throw new NullPointerException();
        }

        String oldContactInformation = this.contactInformation;
        this.contactInformation = contactInformation;
        propertyChangeSupport.firePropertyChange("contactInformation", oldContactInformation,
            contactInformation);
    }

    /**
     * Getter for property keywords.
     *
     * @return Value of property keywords.
     */
    public String[] getKeywords() {
        if (this.keywords.length == 0) {
            return this.keywords;
        } else {
            String[] copy = new String[keywords.length];
            System.arraycopy(keywords, 0, copy, 0, keywords.length);

            return copy;
        }
    }

    /**
     * Setter for property keywords.
     *
     * @param keywords New value of property keywords.
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setKeywords(String[] keywords) {
        if (keywords == null) {
            throw new NullPointerException();
        }

        String[] oldKeywords = this.keywords;
        this.keywords = keywords;
        propertyChangeSupport.firePropertyChange("keywords", oldKeywords, keywords);
    }

    /**
     * Getter for property title.
     *
     * @return Value of property title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for property title.
     *
     * @param title New value of property title.
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public void setTitle(String title) {
        if (title == null) {
            throw new NullPointerException();
        }

        String oldTitle = this.title;
        this.title = title;
        propertyChangeSupport.firePropertyChange("title", oldTitle, title);
    }

    // ------------------------------------------------------------------------
    // EVENT LISTENERS AND EVENT FIRING CODE
    // ------------------------------------------------------------------------

    /**
     * Registers MapLayerListListener to receive events.
     *
     * @param listener The listener to register.
     */
    public synchronized void addMapLayerListListener(
        org.geotools.map.event.MapLayerListListener listener) {
        if (listenerList == null) {
            listenerList = new javax.swing.event.EventListenerList();
        }

        listenerList.add(org.geotools.map.event.MapLayerListListener.class, listener);
    }

    /**
     * Removes MapLayerListListener from the list of listeners.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeMapLayerListListener(
        org.geotools.map.event.MapLayerListListener listener) {
        if (listenerList == null) {
            return;
        }

        listenerList.remove(org.geotools.map.event.MapLayerListListener.class, listener);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireMapLayerListListenerLayerAdded(org.geotools.map.event.MapLayerListEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == org.geotools.map.event.MapLayerListListener.class) {
                ((org.geotools.map.event.MapLayerListListener) listeners[i + 1]).layerAdded(event);
            }
        }
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireMapLayerListListenerLayerRemoved(
        org.geotools.map.event.MapLayerListEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == org.geotools.map.event.MapLayerListListener.class) {
                ((org.geotools.map.event.MapLayerListListener) listeners[i + 1]).layerRemoved(event);
            }
        }
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireMapLayerListListenerLayerChanged(
        org.geotools.map.event.MapLayerListEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == org.geotools.map.event.MapLayerListListener.class) {
                ((org.geotools.map.event.MapLayerListListener) listeners[i + 1]).layerChanged(event);
            }
        }
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireMapLayerListListenerLayerMoved(org.geotools.map.event.MapLayerListEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == org.geotools.map.event.MapLayerListListener.class) {
                ((org.geotools.map.event.MapLayerListListener) listeners[i + 1]).layerMoved(event);
            }
        }
    }

    /**
     * Registers PropertyChangeListener to receive events.
     *
     * @param listener The listener to register.
     */
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes PropertyChangeListener from the list of listeners.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removePropertyChangeListener(
        java.beans.PropertyChangeListener listener) {
        if (listenerList == null) {
            return;
        }

        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Registers MapBoundsListener to receive events.
     *
     * @param listener The listener to register.
     */
    public synchronized void addMapBoundsListener(org.geotools.map.event.MapBoundsListener listener) {
        if (listenerList == null) {
            listenerList = new javax.swing.event.EventListenerList();
        }

        listenerList.add(org.geotools.map.event.MapBoundsListener.class, listener);
    }

    /**
     * Removes MapBoundsListener from the list of listeners.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeMapBoundsListener(
        org.geotools.map.event.MapBoundsListener listener) {
        if (listenerList == null) {
            return;
        }

        listenerList.remove(org.geotools.map.event.MapBoundsListener.class, listener);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireMapBoundsListenerMapBoundsChanged(org.geotools.map.event.MapBoundsEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == org.geotools.map.event.MapBoundsListener.class) {
                ((org.geotools.map.event.MapBoundsListener) listeners[i + 1]).mapBoundsChanged(event);
            }
        }
    }
    
    
    
    
    
    
    //--------------------------------------------------------------------------------------
    public void setSize(int size) {
       org.geotools.styling.StyleBuilder sb = new org.geotools.styling.StyleBuilder();
       for (int i=0; i<size; i++) {
          layerList.add(new DefaultMapLayer(org.geotools.feature.FeatureCollections.newCollection(), sb.createStyle()));
       }
       //((ArrayList)layerList).ensureCapacity(size);
    }
    
    
    
    
    
    
    
    
    
}

