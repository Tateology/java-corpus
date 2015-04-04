/*  
 * Christopher Deckers (chrriis@nextencia.net)  
 * http://www.nextencia.net  
 *  
 * See the file "readme.txt" for information on usage and redistribution of  
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.  
 */  
package steve.test;   
  
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
/**  
 * @author Christopher Deckers  
 */  
public class SimpleWebBrowserExample extends JPanel {   
  
  public SimpleWebBrowserExample() {   
    super(new BorderLayout());   
    JPanel webBrowserPanel = new JPanel(new BorderLayout());   
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));   
    final JWebBrowser webBrowser = new JWebBrowser();   
    webBrowser.navigate("http://code.google.com/apis/earth/");   
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);   
//    add(webBrowserPanel, BorderLayout.CENTER);   
    
    JPanel p1 = new JPanel(new BorderLayout());
    p1.add(webBrowserPanel, BorderLayout.CENTER);
    JPanel p2 = new JPanel(new BorderLayout());
    p2.add(p1, BorderLayout.CENTER);
    JPanel p3 = new JPanel(new BorderLayout());
    p3.add(p2, BorderLayout.CENTER);
    add(p3, BorderLayout.CENTER);
    
    
    
    // Create an additional bar allowing to show/hide the menu bar of the web browser.   
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));   
    JCheckBox menuBarCheckBox = new JCheckBox("Menu Bar", webBrowser.isMenuBarVisible());   
    menuBarCheckBox.addItemListener(new ItemListener() {   
      public void itemStateChanged(ItemEvent e) {   
        webBrowser.setMenuBarVisible(e.getStateChange() == ItemEvent.SELECTED);   
      }   
    });   
    buttonPanel.add(menuBarCheckBox);   
    add(buttonPanel, BorderLayout.SOUTH);   
  }   
  
  /* Standard main method to try that test as a standalone application. */  
  public static void main(String[] args) {   
	    NativeInterface.open();   
  
	  JFrame frame = new JFrame("TEST");
	  JButton testButton = new JButton("test");
	  testButton.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			run(new String[]{});
		}
	  });
	  frame.getContentPane().add(testButton);
	  frame.pack();
	  frame.setVisible(true);
	  
//    UIUtils.setPreferredLookAndFeel();   
//    NativeInterface.open();   
//    SwingUtilities.invokeLater(new Runnable() {   
//      public void run() {   
//        JFrame frame = new JFrame("DJ Native Swing Test");   
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
//        frame.getContentPane().add(new SimpleWebBrowserExample(), BorderLayout.CENTER);   
//        frame.setSize(800, 600);   
//        frame.setLocationByPlatform(true);   
//        frame.setVisible(true);   
//      }   
//    });   
//    NativeInterface.runEventPump();
	  
	  
//    run(args);
	  
	  
	  
	  
	    NativeInterface.runEventPump();   

  }   

  
  /* Standard main method to try that test as a standalone application. */  
  public static void run(String[] args) {   
//    UIUtils.setPreferredLookAndFeel();   
//    NativeInterface.open();   
    SwingUtilities.invokeLater(new Runnable() {   
      public void run() {   
        JFrame frame = new JFrame("DJ Native Swing Test");   
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
        frame.getContentPane().add(new SimpleWebBrowserExample(), BorderLayout.CENTER);   
        frame.setSize(800, 600);   
        frame.setLocationByPlatform(true);   
        frame.setVisible(true);   
      }   
    });   
//    NativeInterface.runEventPump();   
  }   

}  
