/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.ui.filter.WCTFilterGUI;
import gov.noaa.ncdc.wms.WMSPanel;

import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.geotools.gui.swing.event.ZoomChangeEvent;
import org.geotools.gui.swing.event.ZoomChangeListener;

public class WCTMapPaneZoomChange implements ZoomChangeListener {

    private WCTViewer viewer;

    private WMSPanel wmsPanel;
    private WCTFilterGUI nxfilterGUI;

    private int waitTime;   
    private Timer timer = new Timer();
    private TimerTask task;

    private boolean active = true;
    private boolean wmsActive = true;
    private boolean radarActive = true;
    private boolean gridSatelliteActive = true;

    private Rectangle2D.Double lastExtent = null;
    
    final private Vector<WmsResource> wmsResourceList = new Vector<WmsResource>();
    
    
    public WCTMapPaneZoomChange(WCTViewer viewer) {
        this(viewer, 1000);
    }

    public WCTMapPaneZoomChange(WCTViewer viewer, int waitTime) {
        this.waitTime = waitTime;
        this.viewer = viewer;
    }

    
    public void addWmsResource(WmsResource wmsResource) {
        wmsResourceList.add(wmsResource);
    }
    public void removeWmsResource(WmsResource wmsResource) {
        wmsResourceList.remove(wmsResource);
    }
    public Vector<WmsResource> getWmsResources() {
        return wmsResourceList;
    }
    
    
    
    /**
     * Set false to disable any action as a result of a zoom event
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Set false to disable any WMS action as a result of a zoom event
     */
    public void setWMSActive(boolean wmsActive) {
        this.wmsActive = wmsActive;
//        System.out.println("SET ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ WMS ACTIVE?    :+"+this.wmsActive);      
    }

    /**
     * Set false to disable any Nexrad refresh action as a result of a zoom event
     */
    public void setRadarActive(boolean radarActive) {
        this.radarActive = radarActive;
//        System.out.println("SET ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ NEXRAD ACTIVE? :+"+this.nexradActive);
    }


    public boolean isGridSatelliteActive() {
        return gridSatelliteActive;
    }

    public void setGridSatelliteActive(boolean gridSatelliteActive) {
//        System.out.println("SETTING SatelliteActive: "+satelliteActive);
        this.gridSatelliteActive = gridSatelliteActive;
    }

    
    public void setWMSPanel(WMSPanel wmsPanel) {
        this.wmsPanel = wmsPanel;
    }

    public void setNexradFilterGUI(WCTFilterGUI nxfilterGUI) {
        this.nxfilterGUI = nxfilterGUI;
        nxfilterGUI.getSpatialFilterPanel().setSpatialFilterExtent(viewer.getCurrentExtent());
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void zoomChanged(ZoomChangeEvent evt) {
        //System.out.println("ZOOM CHANGE EVENT "+ evt.getChange().toString());
//    	System.out.println("SCALE: "+viewer.getMapPane().getScaleFactor());
        
        if (lastExtent != null && viewer.getCurrentExtent().equals(lastExtent)) {
            return;
        }
        else {
            lastExtent = viewer.getCurrentExtent();
        }
        
        
        

        // Set spatial extent in Filter
        if (nxfilterGUI != null) {
            if (nxfilterGUI.getSpatialFilterPanel().isSpatialFilterLockedToViewer()) {
                nxfilterGUI.getSpatialFilterPanel().setSpatialFilterExtent(viewer.getCurrentExtent());
            }
        }



        // Return right away if not active
        if (! active || (! wmsActive && ! radarActive && ! gridSatelliteActive)) {
            return;
        }

        if (task != null) {
            try {
                task.cancel();
            } catch (Exception e) {
            }
        }
        startTimer(radarActive, gridSatelliteActive, wmsActive);
        
    }
    
    
    private synchronized void startTimer(final boolean radarActiveSwitch, final boolean gridSatelliteActiveSwitch, final boolean wmsActiveSwitch) {
        
        task = new TimerTask() {
            public void run() {
                
                boolean hasRendered = false;
                
                if (viewer.getMapPane().isMouseDragging()) {
                    System.out.println("REFRESH ABORTED: MOUSE IS DRAGGING!!!");
                    return;
                }
                
                
//                System.out.println(" EXECUTING ZOOM CHANGE EVENT ");

//                System.out.print("  -- wmsActive? "+wmsActive);
//                System.out.print("  -- nexradActive? "+nexradActive);
//                System.out.println("  -- satelliteActive? "+satelliteActive);

                viewer.getFrameTools().addZoomExtent(viewer.getCurrentExtent());

//                System.out.println("SCALE DENOMINATOR: " + nexview.getMapPane().getScaleFactor());

                try {

                // REFRESH WMS LAYERS
                if (wmsActiveSwitch && wmsPanel != null && wmsPanel.isAutoRefresh()) {
                    System.out.println("REFRESHING WMS");
                    wmsPanel.refreshWMS();
                }
                else {
//                  System.out.println("NO WMS - AUTO REFRESH");
//                  System.out.println("WMSPANEL: "+wmsPanel);
                  if (wmsPanel != null) System.out.println("AUTOREFRESH: "+wmsPanel.isAutoRefresh());
                }

                } catch (Exception e) {
                    e.printStackTrace();
                }


                boolean doFireRenderComplete = true;

                // REFRESH NEXRAD LAYERS
                if (radarActiveSwitch && viewer.getRadarRenderedGridCoverage().isVisible()) {
                    viewer.refreshRadarData();
                    doFireRenderComplete = false;
                }

                // REFRESH SATELLITE LAYERS
                if (gridSatelliteActiveSwitch && viewer.getGridSatelliteRenderedGridCoverage().isVisible()) {
//                    System.out.println("REFRESHING SAT LAYER !!!!!!!!!!!!!!!!!!!!!!");
                    viewer.refreshSatellite();
                    doFireRenderComplete = false;
                }

//                if (viewer.getRadarGridCoverage() == null && viewer.getGridSatelliteGridCoverage() == null) {
//                    viewer.fireRenderCompleteEvent();
//                }
                
                if (doFireRenderComplete) {
                    viewer.fireRenderCompleteEvent();
                }
                    
                    
                    
                


                
                // REFRESH GENERIC WMS LAYERS
//                System.out.println("1: "+nexview.getCurrentExtent());
                for (int n=0; n<wmsResourceList.size(); n++) {
                    WmsResource wms = wmsResourceList.get(n);
                    try {
                        viewer.displayWMS(wms.getName(), wms.getWmsUrl(viewer.getCurrentExtent()),
                                viewer.getCurrentExtent(), wms.getZIndex());
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                System.out.println("2: "+nexview.getCurrentExtent());
            }
        };

        timer.purge();
        try {
            timer.schedule(task , waitTime);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Creating new zoom timer object...");
            timer = new Timer();
            timer.schedule(task , waitTime);
        }
    }

}
