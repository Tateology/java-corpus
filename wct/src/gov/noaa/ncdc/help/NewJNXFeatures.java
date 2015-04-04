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

package gov.noaa.ncdc.help;

import java.awt.Frame;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXDialog;

public class NewJNXFeatures extends JXDialog {

    private static final long serialVersionUID = 1L;

    private JEditorPane editPane = new JEditorPane();
    
    private final static String URLSTRING = "/helphtml/newfeatures.html";

    public NewJNXFeatures(Frame parent, String version) {      
        super(parent, new JScrollPane(getEditorPane(URLSTRING)));

        setTitle("WCT Version "+version+"  -- New Features");
        setModal(false);
        setSize(780,400);

    }




    private static JComponent getEditorPane(String urlString) {

        JEditorPane editPane = new JEditorPane();
        editPane.setContentType("text/html");      

        try {
            URL url = JNXHelp.class.getResource(urlString);
            editPane.setPage(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return editPane;
    }

}


