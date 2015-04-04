package gov.noaa.ncdc.wct.ui.browser;

import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.WCTViewer.RenderCompleteListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

public class NCDCFlexBrowser extends JPanel {

    final private JWebBrowser webBrowser = new JWebBrowser();
    private WCTViewer viewer = null;

    public NCDCFlexBrowser(WCTViewer viewer) {
        setViewer(viewer);
        init();
    }

    private void init() {
        NativeInterface.open();
        createUI();
        try {
            NativeInterface.runEventPump();
        } catch (Exception e) {
            ;
        }
    }

    private void createUI() {
        this.setLayout(new BorderLayout());
        
        webBrowser.setBackground(Color.BLACK);
        webBrowser.setBorder(BorderFactory.createEmptyBorder());
        webBrowser.setButtonBarVisible(false);
        webBrowser.setBarsVisible(false);
        webBrowser.setStatusBarVisible(false);
        webBrowser.addWebBrowserListener(new WebBrowserListener() {
            @Override
            public void commandReceived(WebBrowserCommandEvent e) {
                System.out.println("WEB BROWSER EVENT -- commandReceived:  "+e.getCommand());
                if (viewer != null && e.getCommand().equals("ge load complete")) {
                    try {
                        viewer.getStatusBar().setProgressText("Flash Map Initialization...");
//                        System.out.println("ge load : viewer extent: "+viewer.getCurrentExtent());
                        flyToExtent(viewer.getCurrentExtent());
                        refreshView();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
//                System.out.println("WEB BROWSER EVENT -- ladingProgressChanged:  "+e.getWebBrowser().getLoadingProgress());
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
//                System.out.println("WEB BROWSER EVENT -- statusChange:  "+e.getWebBrowser().getStatusText());
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
        });


        webBrowser.navigate("http://nidis-d.ncdc.noaa.gov/imageserver/NIDIS/test/nidis-viewer");

        
        this.add(webBrowser, BorderLayout.CENTER);
    }

    private void setViewer(WCTViewer viewer) {
        this.viewer = viewer;
        
        viewer.addRenderCompleteListener(new RenderCompleteListener() {
            @Override
            public void renderProgress(int progressPercent) {
            }
            @Override
            public void renderComplete() {
                refreshView();
            }
        });
        
    }
    
    
    public void flyToExtent(final Rectangle2D extent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                webBrowser.executeJavascript("NIDIS_Viewer.setExtentFromWCT("+
                        extent.getMinX()+","+extent.getMinY()+","+extent.getMaxX()+","+extent.getMaxY()+",4326);");
            }
        });
    }


    public void refreshView() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println("CALLING refreshWCT();");
                webBrowser.executeJavascript("NIDIS_Viewer.refreshWCT();");
            }
        });
    }

    public JWebBrowser getWebBrowser() {
        return webBrowser;
    }
    
}
