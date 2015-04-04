
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


import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import com.jidesoft.swing.Searchable;
import com.jidesoft.swing.SearchableBar;
import com.jidesoft.swing.SearchableUtils;

public class WCTTextPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -7134768157747603740L;

	public static enum SearchBarProperties { NO_SEARCH_BAR, SEARCH_BAR_FULL, SEARCH_BAR_COMPACT }; 

	private String supplementalData;

    private JButton printButton, closeButton, saveButton, copyButton;
    private JTextArea textArea;
    private JScrollPane scrollPane;

    private SearchBarProperties searchBarProps = SearchBarProperties.SEARCH_BAR_FULL;
    
    public WCTTextPanel(Frame parent, Vector<String> supplementalData) {
        this(parent, parseVector(supplementalData), SearchBarProperties.SEARCH_BAR_FULL);
    }
    public WCTTextPanel(Frame parent, String supplementalData, SearchBarProperties sbp) {
        this.supplementalData = supplementalData;
        this.searchBarProps = sbp;
        createGUI();
    }

    
    public WCTTextPanel(Dialog parent, String supplementalData, SearchBarProperties sbp) {
        this.supplementalData = supplementalData;
        this.searchBarProps = sbp;
        createGUI();

        this.setSize(new Dimension((int)getPreferredSize().getWidth()+50, 500));
    }

    public static String parseVector(Vector<String> vector) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<vector.size(); i++) {
            sb.append((String)vector.elementAt(i));
            sb.append("\n");         
        }
        return sb.toString();
    }

    /**
     * Set the supplemental data text in the text area
     * @param supplementalData
     */
    public void setText(String supplementalData) {
        
        if (supplementalData == null) {
            textArea.setText("NO SUPPLEMENTAL DATA AVAILABLE");
            return;
        }

        textArea.setText(supplementalData);
        textArea.setCaretPosition(0);
        
//        pack();
        this.setSize(new Dimension(500, 500));
        validate();
        repaint();

    }


    /**
     * Set the supplemental data text in the text area
     * @param supplementalData
     */
    public void setTextArray(String[] supplementalData) {
        if (supplementalData == null) {
            textArea.setText("NO SUPPLEMENTAL DATA AVAILABLE");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int n=0; n<supplementalData.length; n++) {
            sb.append(supplementalData[n]+"\n");
        }
        
        this.supplementalData = sb.toString();
        
        textArea.setText(sb.toString());       
        textArea.setCaretPosition(0);

//        pack();
        this.setSize(new Dimension(500, 500));
        validate();
        repaint();

    }
    

    

    private void createGUI() {

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        textArea.setText(supplementalData);


        this.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        copyButton = new JButton("Copy");
        printButton = new JButton("Print");
        saveButton = new JButton("Save");
        closeButton = new JButton("Close");
        copyButton.addActionListener(this);
        printButton.addActionListener(this);
        saveButton.addActionListener(this);
        closeButton.addActionListener(this);
        buttonPanel.add(copyButton);
        buttonPanel.add(printButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

//        getContentPane().add(new JScrollPane(getTextArea()), "Center");
        this.add(createSearchableTextArea(textArea), "Center");
        this.add(buttonPanel, "South");


        getTextArea().setSelectionStart(0);
        getTextArea().setSelectionEnd(0);

    }
    
    
    private JPanel createSearchableTextArea(final JTextArea textArea) {
        final JPanel panel = new JPanel(new BorderLayout());
        scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        if (searchBarProps != SearchBarProperties.NO_SEARCH_BAR) {
        
        	Searchable searchable = SearchableUtils.installSearchable(textArea);
        	searchable.setRepeats(true);
        	SearchableBar textAreaSearchableBar = SearchableBar.install(searchable, 
        			KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), new SearchableBar.Installer() {
        		public void openSearchBar(SearchableBar searchableBar) {
        			String selectedText = textArea.getSelectedText();
        			if (selectedText != null && selectedText.length() > 0) {
        				searchableBar.setSearchingText(selectedText);
        			}
        			panel.add(searchableBar, BorderLayout.AFTER_LAST_LINE);
        			panel.invalidate();
        			panel.revalidate();
        		}

        		public void closeSearchBar(SearchableBar searchableBar) {
        			panel.remove(searchableBar);
        			panel.invalidate();
        			panel.revalidate();
        		}
        	});
        	if (searchBarProps == SearchBarProperties.SEARCH_BAR_COMPACT) {
        		textAreaSearchableBar.setCompact(true);
        	}
        	
        	textAreaSearchableBar.getInstaller().openSearchBar(textAreaSearchableBar);
        
        }
        return panel;
    }

    public void addCloseButtonActionListener(ActionListener l) {
    	closeButton.addActionListener(l);
    }
    
    
    

    // Implementation of ActionListener interface.
    public void actionPerformed(ActionEvent event) {

        Object source = event.getSource();

        if (source == closeButton) {
        	
        }
        else if (source == copyButton) {
//            Transferable t = new TextSelection(getTextArea().getText());
//            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
            
            TextUtils.getInstance().copyToClipboard(supplementalData);
            
        }
        else if (source == printButton) {

            try {

//                TextUtils.getInstance().print(title, supplementalData);
                
            	TextUtils.getInstance().print(textArea);
            	
            } catch (PrinterException e) {
    			e.printStackTrace();
                JOptionPane.showMessageDialog(this, e);
			}


        }
        else if (source == saveButton) {

            try {

                TextUtils.getInstance().save(this, supplementalData, "txt", "Text File");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e);
            }
        }

    }



    public JTextArea getTextArea() {
        return textArea;
    }
    
    public JScrollPane getScrollPane() {
    	return scrollPane;
    }










//    class Report extends JetGraphics2DReport { 
//
//        private String title;
//
//        public Report(String title) {         
//            super("Alphanumeric Report", title);
//            this.title = title;         
//        }
//
//        public void jetPrint() throws JetException { 
//            // this member function will be called to generate the report.  
//
//            // initialization.         
//            jetSetInternalMargins(50, 0, 50, 0);
//
//            // actual printing begins.         
//            Graphics2D g = jetPrintBegin();
//
//            g.setFont(new Font("Monospaced", Font.PLAIN, 10));
//
//            String[] lines = supplementalData.split("\n");
//
//            int pageCnt = 1;
//            int y=0;
//            for (int i=0; i<lines.length; i++) {
//                //g.drawString((String)supplementalData.elementAt(i), 75, 105+y*10);
//                g.drawString(lines[i], 0, y*10);
//                if (i>0 && i%54 == 0) {
//                    //g.drawString("     Page "+pageCnt++, 75, 105+(y+2)*10);
//                    jetNewPage();
//                    g.setFont(new Font("Monospaced", Font.PLAIN, 10));
//                    y=0;
//                }
//                else {
//                    y++;
//                }
//            }
//
//            //jw.jetCloseReport();
//
//
//        }
//
//        public void jetPrintHeader(Graphics2D g, PageFormat pf) { 
//            // called at the beginning of printing a new report page.
//
//            g.drawString(title, 150, 20);         
//            g.drawLine(18, 23, 450, 23);
//        }
//
//        public void jetPrintFooter(Graphics2D g, PageFormat pf) { 
//            // called before a new report page is supplied.
//
//            g.drawString("Page " + (jetGetPageNumber() + 1), 230, 25);
//
//        }
//
//    }



//    private class TextSelection implements Transferable {
//
//        private ArrayList textFlavors = new ArrayList();
//        private String text;
//
//        public TextSelection(String text) {
//            this.text = text;
//
//
//            try {
//                textFlavors.add(new DataFlavor("text/plain;class=java.lang.String"));
//                textFlavors.add(new DataFlavor("text/plain;class=java.io.Reader"));
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        public DataFlavor[] getTransferDataFlavors() {
//            return (DataFlavor[]) textFlavors.toArray(new DataFlavor[textFlavors.size()]);
//        }
//
//        public boolean isDataFlavorSupported(DataFlavor flavor) {
//            return textFlavors.contains(flavor);
//        }
//
//        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
//
//            if (String.class.equals(flavor.getRepresentationClass())) {
//                return text;
//            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
//                return new StringReader(text);
//            }
//            throw new UnsupportedFlavorException(flavor);
//
//        }
//
//    } 



}



