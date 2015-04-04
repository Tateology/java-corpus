package gov.noaa.ncdc.wct.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

public class AppPanel extends JPanel {

	private URL appURL = null;
	final private JWebBrowser webBrowser = new JWebBrowser();
    
	
	
	public AppPanel() {
		init();
		
	}
	
	
	public void navigate(URL appURL) {
		this.appURL = appURL;
		
		webBrowser.navigate(appURL.toString());
	}
	
	
	
	
	private void init() {
		
		this.setLayout(new BorderLayout());
		
	    
        webBrowser.setBackground(Color.BLACK);
        webBrowser.setBorder(BorderFactory.createEmptyBorder());
        webBrowser.setBarsVisible(true);
        webBrowser.setMenuBarVisible(false);
        webBrowser.setButtonBarVisible(false);
        webBrowser.setStatusBarVisible(true);
        webBrowser.addWebBrowserListener(new WebBrowserListener() {
            @Override
            public void commandReceived(WebBrowserCommandEvent e) {
            }
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
//                System.out.println("WEB BROWSER EVENT -- ladingProgressChanged:  "+e.getWebBrowser().getLoadingProgress());
            }
            @Override
            public void locationChangeCanceled(WebBrowserNavigationEvent arg0) {
            }
            @Override
            public void locationChanged(WebBrowserNavigationEvent e) {
                System.out.println("WEB BROWSER EVENT -- locationChanged:  "+e.getWebBrowser().getStatusText());
                if (! e.getNewResourceLocation().endsWith(".gov")) {
                	e.getWebBrowser().stopLoading();
                }
            }
            @Override
            public void locationChanging(WebBrowserNavigationEvent e) {
              System.out.println("WEB BROWSER EVENT -- locationChanging:  "+e.getWebBrowser().getStatusText());
              if (! e.getNewResourceLocation().endsWith(".gov")) {
              	e.getWebBrowser().stopLoading();
              }
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
        
        
        this.add(new JScrollPane(webBrowser), BorderLayout.CENTER);
		
	}
	
	
	public static void main(String[] args) {
		try {
			UIUtils.setPreferredLookAndFeel();
			NativeInterface.open();
//			final URL url = new URL("http://wlos.com/newsroom/wx/radar/");
			final URL url = new URL("http://imp.wxc.com/main.html?" +
					"host=wlos&width=1010&height=620&template=ir&" +
					"view=map&registration=0&links=0"); 
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JFrame frame = new WCTFrame("WCT App View");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					AppPanel app = new AppPanel();
					app.navigate(url);


					frame.getContentPane().add(app, BorderLayout.CENTER);
					frame.setSize(800, 600);
					frame.setLocationByPlatform(true);
					frame.setVisible(true);
				}
			});
			NativeInterface.runEventPump();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
