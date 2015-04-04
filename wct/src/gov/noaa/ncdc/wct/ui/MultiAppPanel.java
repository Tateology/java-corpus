package gov.noaa.ncdc.wct.ui;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;

import com.jidesoft.swing.JideSplitPane;

public class MultiAppPanel extends JPanel {

	private JideSplitPane splitPane = new JideSplitPane();
	
	public MultiAppPanel() {
		init();
	}
	
	
	private void init() {
		this.setLayout(new BorderLayout());
		this.add(splitPane);
	}
	
	public void addApp(AppPanel app) {
		splitPane.addPane(app);
	}
	
	
	
	
	
	public static void main(String[] args) {
		try {
			UIUtils.setPreferredLookAndFeel();
			NativeInterface.open();
//			final URL url = new URL("http://wlos.com/newsroom/wx/radar/");
			final URL url = new URL("http://imp.wxc.com/main.html?" +
					"host=wlos&width=1010&height=620&template=ir&" +
					"view=map&registration=0&links=0"); 
			
			final URL url2 = new URL("http://wdssii.nssl.noaa.gov/maps/");
			
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JFrame frame = new WCTFrame("WCT App View");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					MultiAppPanel mapp = new MultiAppPanel();
					AppPanel app = new AppPanel();
					AppPanel app2 = new AppPanel();
					app.navigate(url);
					app.navigate(url2);
					mapp.addApp(app);
					mapp.addApp(app2);


					frame.getContentPane().add(mapp, BorderLayout.CENTER);
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
