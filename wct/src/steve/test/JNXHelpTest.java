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

package steve.test;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


public class JNXHelpTest extends JFrame {
   
   private JEditorPane editPane = new JEditorPane();
   
   public JNXHelpTest() {
      super("Java NEXRAD Viewer Help Test");
      
      JMenuBar menuBar = new JMenuBar();
      JMenu menu = new JMenu("Help");
      JMenuItem menuItem = new JMenuItem("Start Java Help");
      menuBar.add(menu);
      menu.add(menuItem);
      this.setJMenuBar(menuBar);
      
//      try {
//         String helpHS = "/docs/help/Help.hs";
//         URL hsURL = JNXHelpTest.class.getResource(helpHS);
//         HelpSet hs = new HelpSet(null, hsURL);
//         HelpBroker hb = hs.createHelpBroker();
//         menuItem.addActionListener(new CSH.DisplayHelpFromSource(hb));
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
      
   }
   


   public static void main(String[] args) {
      JNXHelpTest jnxHelpTest = new JNXHelpTest();
      jnxHelpTest.pack();
      jnxHelpTest.show();
   }   
   
   
   
   
   
}
