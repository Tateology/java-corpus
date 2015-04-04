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

package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeRSL;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jetbee.report.JetException;
import com.jetbee.report.JetGraphics2DReport;
import com.jetbee.report.JetWriter;

public class RSLDisplayDialog extends JDialog {
    
    private DecodeRSL decoder;

    private JCheckBox jcbInfo, jcbStatus, jcbError, jcbWarning, jcbAlarm, jcbComms;

    private JButton printButton, closeButton, saveButton, copyButton, copyTextButton;

    private JEditorPane editPane;
    
    private JScrollPane scrollPane;

    private String title;

    private final static DecimalFormat fmt3 = new DecimalFormat("0.000");

    public RSLDisplayDialog(Frame parent, DecodeRSL decoder) {
        super(parent, "ARCHIVE STATUS PRODUCT / RADAR STATUS MESSAGE", false);
        this.decoder = decoder;
        createGUI();

        pack();
        this.setSize(new Dimension((int) getPreferredSize().getWidth() + 50, 500));
        setVisible(true);
    }

    private void createGUI() {

        final JDialog thisFrame = this;
        
        jcbInfo = new JCheckBox("Info", true);
        jcbStatus = new JCheckBox("Status", true);
        jcbError = new JCheckBox("Error", true);
        jcbWarning = new JCheckBox("Warning", true);
        jcbAlarm = new JCheckBox("Alarm", true);
        jcbComms = new JCheckBox("Comms", true);

        JPanel togglePanel = new JPanel();
        togglePanel.add(jcbInfo);        
        togglePanel.add(jcbStatus);
        togglePanel.add(jcbError);
        togglePanel.add(jcbWarning);
        togglePanel.add(jcbAlarm);
        togglePanel.add(jcbComms);

        editPane = new JEditorPane();
        editPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        editPane.setCaretPosition(0);
        editPane.setEditable(false);

        editPane.setContentType("text/html");
        editPane.setText(getHtml());

        getContentPane().setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        copyButton = new JButton("Copy HTML");
        copyTextButton = new JButton("Copy Text");
        printButton = new JButton("Print");
        saveButton = new JButton("Save");
        closeButton = new JButton("Close");
        buttonPanel.add(copyButton);
        buttonPanel.add(copyTextButton);
        //buttonPanel.add(printButton);  DISABLE THE PRINT OPTION DUE TO UNRESOLVED ISSUES AND EXCEPTIONS IN JETBEE
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        scrollPane = new JScrollPane(editPane);
        scrollPane.setAutoscrolls(false);
        //scrollPane.setViewportView(editPane);
        
        getContentPane().add(togglePanel, "North");
        getContentPane().add(scrollPane, "Center");
        getContentPane().add(buttonPanel, "South");

        

        // Manage anonomous listener classes
        ActionListener jcbListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // refresh html
//                int scrollValue = scrollPane.getVerticalScrollBar().getValue();
//                Point viewPos = scrollPane.getViewport().getViewPosition();
                editPane.setText(getHtml());
                //scrollPane.getViewport().setViewPosition(viewPos);
                //scrollPane.getVerticalScrollBar().setValue(scrollValue);
                //scrollPane.getViewport().scrollRectToVisible(new Rectangle(viewPos));
                // below is workaround because previous 3 lines of code don't work
                editPane.setSelectionStart(0);
                editPane.setSelectionEnd(0);
            }
        };
        
        jcbInfo.addActionListener(jcbListener);
        jcbStatus.addActionListener(jcbListener);
        jcbError.addActionListener(jcbListener);
        jcbWarning.addActionListener(jcbListener);
        jcbAlarm.addActionListener(jcbListener);
        jcbComms.addActionListener(jcbListener);
        
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        copyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Transferable t = new HtmlSelection(editPane.getText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
            }
        });
        copyTextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Transferable t = new TextSelection(getFilteredText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
            }
        });
        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {

                    JetWriter jw = new JetWriter(true, false);
                    Report rep = new Report(title, decoder);
                    boolean go = jw.jetPrintDialog();
                    if (go) {
                        jw.jetRunReport(rep); // The report's jetPrint() 
                        // function will be invoked in 
                        // a thread to generate the report.
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(thisFrame, e);
                }
            }
        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                try {

                    // Set up File Chooser
                    String saveDirectory = WCTProperties.getWCTProperty("jne_export_dir");
                    JFileChooser fc = new JFileChooser(saveDirectory);
                    fc.setDialogTitle("Enter Filename to Write Data");
                    //fc.addChoosableFileFilter(new OpenFileFilter("shp", true, "ESRI Shapefiles (.shp/.shx/.dbf/.prj) "));

                    int returnVal = fc.showSaveDialog(saveButton);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File saveFile = fc.getSelectedFile();

                        int choice = JOptionPane.YES_OPTION;
                        // Check to see if file exists
                        if (saveFile.exists()) {
                            String message = "The output file \n" + "<html><font color=red>" + saveFile
                                    + "</font></html>\n" + "already exists.\n\n" + "Do you want to proceed and OVERWRITE?";
                            choice = JOptionPane.showConfirmDialog(null, (Object) message, "OVERWRITE FILE",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                        }

                        if (choice == JOptionPane.YES_OPTION) {
                            saveDirectory = saveFile.getParent().toString();
                            WCTProperties.setWCTProperty("jne_export_dir", saveDirectory);

                            BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
                            bw.write(getFilteredText());
                            bw.flush();
                            bw.close();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(thisFrame, e);
                }
            }
        });
        
        
    }

    private String getHtml() {

        StringBuffer html = new StringBuffer();
        html.append("<html><head>");
        html.append("<STYLE type='text/css'>");
        html.append("TABLE.main{border: 1px #d3d3d3 solid; cellspacing: 2px;}");
        html.append("TR.header{background-color:#e3e3e3; color:black}");
        html.append("TR.info{background-color:green; color:white}");
        html.append("TR.status{background-color:#006400; color:white}");
        html.append("TR.error{background-color:purple; color:white}");
        html.append("TR.warning{background-color:#ee7600; color:white}");
        html.append("TR.alarm{background-color:red; color:white}");
        html.append("TR.comms{background-color:blue; color:white}");
        html.append("TR.unknown{background-color:#e8e8e8; color:black}");
        html.append("</STYLE>");
        html.append("</head><body>");

        html.append("<center><b>"+decoder.getHeader().toString()+"</b></center>");
        html.append("<table cellspacing='2' cellpadding='2' border='0'>");
        html.append("<tr class='header'><th>Message<br>Number</th><th>Message Type</th><th>Message Text</th></tr>");

        ArrayList rslTextCompList = decoder.getTextCompList();
        for (int n = 0; n < rslTextCompList.size(); n++) {
            DecodeRSL.TextComponent rslTextComp = (DecodeRSL.TextComponent) rslTextCompList.get(n);
            String messageType = rslTextComp.getParamAttributeHashMap().get("Value").toString();

            if (messageType.indexOf("INFO") != -1 && jcbInfo.isSelected()) {
                html.append("<tr class='info'>");
                html.append("<td>" + (n+1) + "</td><td>" + messageType + "</td>");
                html.append("<td>" + rslTextComp.getText() + "</td>");
                html.append("</tr>");
            }
            else if (messageType.indexOf("ERROR") != -1 && jcbError.isSelected()) {
                html.append("<tr class='error'>");
                html.append("<td>" + (n+1) + "</td><td>" + messageType + "</td>");
                html.append("<td>" + rslTextComp.getText() + "</td>");
                html.append("</tr>");
            }
            else if (messageType.indexOf("STATUS") != -1 && jcbStatus.isSelected()) {
                html.append("<tr class='status'>");
                html.append("<td>" + (n+1) + "</td><td>" + messageType + "</td>");
                html.append("<td>" + rslTextComp.getText() + "</td>");
                html.append("</tr>");
            }
            else if (messageType.indexOf("WARNING") != -1 && jcbWarning.isSelected()) {
                html.append("<tr class='warning'>");
                html.append("<td>" + (n+1) + "</td><td>" + messageType + "</td>");
                html.append("<td>" + rslTextComp.getText() + "</td>");
                html.append("</tr>");
            }
            else if (messageType.indexOf("ALARM") != -1 && jcbAlarm.isSelected()) {
                html.append("<tr class='alarm'>");
                html.append("<td>" + (n+1) + "</td><td>" + messageType + "</td>");
                html.append("<td>" + rslTextComp.getText() + "</td>");
                html.append("</tr>");
            }
            else if (messageType.indexOf("COMMS") != -1 && jcbComms.isSelected()) {
                html.append("<tr class='comms'>");
                html.append("<td>" + (n+1) + "</td><td>" + messageType + "</td>");
                html.append("<td>" + rslTextComp.getText() + "</td>");
                html.append("</tr>");
            }
            else if (messageType.indexOf("INFO") == -1 && messageType.indexOf("ERROR") == -1
                    && messageType.indexOf("STATUS") == -1 && messageType.indexOf("WARNING") == -1
                    && messageType.indexOf("ALARM") == -1 && messageType.indexOf("COMMS") == -1){
                
                html.append("<tr class='unknown'>");
                html.append("<td>" + (n+1) + "</td><td>" + messageType + "</td>");
                html.append("<td>" + rslTextComp.getText() + "</td>");
                html.append("</tr>");
            }

        }

        html.append("</table>");

        return html.toString();

    }

    
    
    
    
    private String getFilteredText() {
        
        
        StringBuffer sb = new StringBuffer();
        
        sb.append(decoder.getHeader().toString()+"\n");
        
        ArrayList rslTextCompList = decoder.getTextCompList();
        for (int n = 0; n < rslTextCompList.size(); n++) {
            DecodeRSL.TextComponent rslTextComp = (DecodeRSL.TextComponent) rslTextCompList.get(n);
            String messageType = rslTextComp.getParamAttributeHashMap().get("Value").toString();

            if (messageType.indexOf("INFO") != -1 && jcbInfo.isSelected()) {
                sb.append((n+1)+"\t"+messageType+"\t"+rslTextComp.getText() +"\n");
            }
            else if (messageType.indexOf("ERROR") != -1 && jcbError.isSelected()) {
                sb.append((n+1)+"\t"+messageType+"\t"+rslTextComp.getText() +"\n");
            }
            else if (messageType.indexOf("STATUS") != -1 && jcbStatus.isSelected()) {
                sb.append((n+1)+"\t"+messageType+"\t"+rslTextComp.getText() +"\n");
            }
            else if (messageType.indexOf("WARNING") != -1 && jcbWarning.isSelected()) {
                sb.append((n+1)+"\t"+messageType+"\t"+rslTextComp.getText() +"\n");
            }
            else if (messageType.indexOf("ALARM") != -1 && jcbAlarm.isSelected()) {
                sb.append((n+1)+"\t"+messageType+"\t"+rslTextComp.getText() +"\n");
            }
            else if (messageType.indexOf("COMMS") != -1 && jcbComms.isSelected()) {
                sb.append((n+1)+"\t"+messageType+"\t"+rslTextComp.getText() +"\n");
            }
            else if (messageType.indexOf("INFO") == -1 && messageType.indexOf("ERROR") == -1
                    && messageType.indexOf("STATUS") == -1 && messageType.indexOf("WARNING") == -1
                    && messageType.indexOf("ALARM") == -1 && messageType.indexOf("COMMS") == -1){
                
                sb.append((n+1)+"\t"+messageType+"\t"+rslTextComp.getText() +"\n");
            }

        }
        
        return sb.toString();

    }
    
    


    private class Report extends JetGraphics2DReport {

        private String title;
        private DecodeRSL decoder;

        public Report(String title, DecodeRSL decoder) {
            super("Alphanumeric Report", title);
            this.title = title;
            this.decoder = decoder;
        }

        public void jetPrint() throws JetException {
            // this member function will be called to generate the report.  

            // initialization.         
            jetSetInternalMargins(50, 0, 50, 0);

            // actual printing begins.         
            Graphics2D g = jetPrintBegin();

            g.setFont(new Font("Monospaced", Font.PLAIN, 10));

            //String[] lines = decoder.getRSLDisplayData().split("\n");
            String[] lines = new String[] { "THIS IS A TEST", "A REALLY BIG TEST", "LINE 3" };
            
            
            int pageCnt = 1;
            int y = 0;
            for (int i = 0; i < lines.length; i++) {
                //g.drawString((String)supplementalData.elementAt(i), 75, 105+y*10);
                if (lines[i] != null) {
                    g.drawString(lines[i], 0, y * 10);
                }

                if (i > 0 && i % 54 == 0) {
                    //g.drawString("     Page "+pageCnt++, 75, 105+(y+2)*10);
                    jetNewPage();
                    g.setFont(new Font("Monospaced", Font.PLAIN, 10));
                    y = 0;
                }
                else {
                    y++;
                }
            }

            //jw.jetCloseReport();

        }

        public void jetPrintHeader(Graphics2D g, PageFormat pf) {
            // called at the beginning of printing a new report page.

            g.drawString(title, 150, 20);
            g.drawLine(18, 23, 450, 23);
        }

        public void jetPrintFooter(Graphics2D g, PageFormat pf) {
            // called before a new report page is supplied.

            g.drawString("Page " + (jetGetPageNumber() + 1), 230, 25);

        }

    }

    
    private class HtmlSelection implements Transferable {

        private ArrayList htmlFlavors = new ArrayList();
        private String html;

        public HtmlSelection(String html) {
            this.html = html;
            
            
            try {
                htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
                htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }

        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return htmlFlavors.contains(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

            if (String.class.equals(flavor.getRepresentationClass())) {
                return html;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(html);
            }
            throw new UnsupportedFlavorException(flavor);

        }

    } 
    
    private class TextSelection implements Transferable {

        private ArrayList textFlavors = new ArrayList();
        private String text;

        public TextSelection(String text) {
            this.text = text;
            
            
            try {
                textFlavors.add(new DataFlavor("text/plain;class=java.lang.String"));
                textFlavors.add(new DataFlavor("text/plain;class=java.io.Reader"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }

        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) textFlavors.toArray(new DataFlavor[textFlavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return textFlavors.contains(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

            if (String.class.equals(flavor.getRepresentationClass())) {
                return text;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(text);
            }
            throw new UnsupportedFlavorException(flavor);

        }

    } 
    
}
