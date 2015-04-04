
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


import gov.noaa.ncdc.wct.ui.WCTTextPanel.SearchBarProperties;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class WCTTextDialog extends JDialog {
	private static final long serialVersionUID = -1359251781392169138L;
	
	private WCTTextPanel wctTextPanel;




    public WCTTextDialog(Frame parent, Vector<String> supplementalData) {
        this(parent, supplementalData, "Alphanumeric Supplemental Data"); 
    }
    public WCTTextDialog(Frame parent, Vector<String> supplementalData, String title) {
        this(parent, WCTTextPanel.parseVector(supplementalData), title);
    }
    public WCTTextDialog(Frame parent, String supplementalData) {
        this(parent, supplementalData, "Alphanumeric Supplemental Data");
    }
    public WCTTextDialog(Frame parent, String supplementalData, String title) {
        this(parent, supplementalData, title, true, SearchBarProperties.SEARCH_BAR_FULL);
    }     
    public WCTTextDialog(Frame parent, String supplementalData, String title, boolean visible) {
        this(parent, supplementalData, title, visible, SearchBarProperties.SEARCH_BAR_FULL);
    }     
    public WCTTextDialog(Frame parent, String supplementalData, String title, boolean visible, SearchBarProperties sbp) {
        super(parent, title, false);     
        
        wctTextPanel = new WCTTextPanel(parent, supplementalData, sbp);
        
        createGUI();

        pack();
        this.setSize(new Dimension((int)getPreferredSize().getWidth()+50, 500));
        setVisible(visible);
    }

    public WCTTextDialog(Dialog parent, String supplementalData, String title, boolean visible) {
    	this(parent, supplementalData, title, visible, SearchBarProperties.SEARCH_BAR_FULL);
    }
    
    public WCTTextDialog(Dialog parent, String supplementalData, String title, boolean visible, SearchBarProperties sbp) {
        super(parent, title, false);     
    
        wctTextPanel = new WCTTextPanel(parent, supplementalData, sbp);
        
        createGUI();

        pack();
        this.setSize(new Dimension((int)getPreferredSize().getWidth()+50, 500));
        setVisible(visible);
    }

    private void createGUI() {

    	wctTextPanel.addCloseButtonActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
    	});
    	
    	
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(wctTextPanel, BorderLayout.CENTER);


    }
 
    
    public JScrollPane getScrollPane() {
    	return wctTextPanel.getScrollPane();
    }
    
    public void setText(String text) {
    	wctTextPanel.setText(text);
    }
    
    public JTextArea getTextArea() {
    	return wctTextPanel.getTextArea();
    }
    
    public void setTextArray(String[] textLines) {
    	wctTextPanel.setTextArray(textLines);
    }

}