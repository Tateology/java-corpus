package gov.noaa.ncdc.wct.ui.ge;

import gov.noaa.ncdc.wct.ui.WCTViewer;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

public class NCSWebBrowserListener implements WebBrowserListener {

    private WCTViewer viewer;
    
    public NCSWebBrowserListener(WCTViewer viewer) {
        this.viewer = viewer;
    }
    
    @Override
    public void commandReceived(WebBrowserCommandEvent e) {
        System.out.println("WEB BROWSER EVENT -- commandReceived:  "+e.getCommand());
        if (e.getCommand().equals("ncs-js load complete")) {
            try {
                System.out.println("adding click event listener");
                e.getWebBrowser().executeJavascript("mapClickEvent = dojo.connect(map, \"onClick\", function(event) { window.location = 'command://'+ encodeURIComponent('ncs-js click'); });");
                System.out.println("adding zoom event listener");
                e.getWebBrowser().executeJavascript("mapZoomEvent = dojo.connect(map, \"onZoomStart\", function(event) { window.location = 'command://'+ encodeURIComponent('ncs-js zoom'); });");
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    @Override
    public void loadingProgressChanged(WebBrowserEvent e) {
//        System.out.println("WEB BROWSER EVENT -- loadingProgressChanged:  "+e.getWebBrowser().getLoadingProgress());
//        e.getWebBrowser().executeJavascript("dojo.connect(map, \"onLoad\", function() { window.location = 'command://'+ encodeURIComponent('ncs-js load complete') });");
//        viewer.setWctViewExtent(viewer.getCurrentExtent());
    }
    @Override
    public void locationChangeCanceled(WebBrowserNavigationEvent arg0) {
    }
    @Override
    public void locationChanged(WebBrowserNavigationEvent arg0) {
    }
    @Override
    public void locationChanging(WebBrowserNavigationEvent arg0) {
    }
    @Override
    public void statusChanged(WebBrowserEvent e) {
//        System.out.println("WEB BROWSER EVENT -- statusChange:  "+e.getWebBrowser().getStatusText());
    }
    @Override
    public void titleChanged(WebBrowserEvent arg0) {
    }
    @Override
    public void windowClosing(WebBrowserEvent arg0) {
    }
    @Override
    public void windowOpening(WebBrowserWindowOpeningEvent arg0) {
    }
    @Override
    public void windowWillOpen(WebBrowserWindowWillOpenEvent arg0) {
    }

}
