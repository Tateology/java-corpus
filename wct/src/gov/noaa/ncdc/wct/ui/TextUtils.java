package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.wct.WCTProperties;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

public class TextUtils {


    private static TextUtils utils = null;
    private TextUtils() {        
    }
    public static TextUtils getInstance() {
        if (utils == null) {
            utils = new TextUtils();
        }
        return utils;
    }
    

    
    
    
    public void copyToClipboard(final String text) {
        Transferable t = new TextSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
    }
    
    
//    public void print(String title, String text) throws JetException {
//        
////        try {
//
//            JetWriter jw = new JetWriter(true, false);
//            Report rep = new Report(title);
//            boolean go = jw.jetPrintDialog();
//            if (go) {
//                jw.jetRunReport(rep);    // The report's jetPrint() 
//                // function will be invoked in 
//                // a thread to generate the report.
//            }            
//
////        } catch (Exception e) {
////            e.printStackTrace();
////            JOptionPane.showMessageDialog(this, e);
////        }
//
//    }
    
    public boolean print(JTextComponent textComp) throws PrinterException {
    	
    	float oldSize = textComp.getFont().getSize2D();
    	textComp.setFont(textComp.getFont().deriveFont(oldSize-2));
    	boolean complete = textComp.print();
    	textComp.setFont(textComp.getFont().deriveFont(oldSize));
    	
    	return complete;
    }
    
    
    public void save(Component parent, String text, String extension, String formatDescription) throws IOException {
        
        // Set up File Chooser
        String saveDirectory = WCTProperties.getWCTProperty("text_export_dir");
        JFileChooser fc = new JFileChooser(saveDirectory);
        fc.setDialogTitle("Enter Filename to Write Data");
        OpenFileFilter ff = new OpenFileFilter(extension, true, formatDescription);
        fc.addChoosableFileFilter(ff);
        fc.setFileFilter(ff);

        int returnVal = fc.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File saveFile = fc.getSelectedFile();
            if (! saveFile.toString().endsWith(extension)) {
                saveFile = new File(saveFile.toString()+"."+extension);
            }

            int choice = JOptionPane.YES_OPTION;
            // Check to see if file exists
            if (saveFile.exists()) {
                String message = "The output file \n" +
                "<html><font color=red>" + saveFile + "</font></html>\n" +
                "already exists.\n\n" +
                "Do you want to proceed and OVERWRITE?";
                choice = JOptionPane.showConfirmDialog(null, (Object) message,
                        "OVERWRITE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            }

            if (choice == JOptionPane.YES_OPTION) {
                saveDirectory = saveFile.getParent().toString();
                WCTProperties.setWCTProperty("text_export_dir", saveDirectory);

                BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
                bw.write(text.replaceAll("\n", System.getProperty("line.separator")));
                bw.flush();
                bw.close();
            }
        }

    }

    
    
    
    
    
    
    
    
    private class TextSelection implements Transferable {

        private ArrayList<DataFlavor> textFlavors = new ArrayList<DataFlavor>();
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

    
    
//    private class Report extends JetGraphics2DReport { 
//
//        private String title;
//
//        public Report(String title) {         
//            super("Alphanumeric Report", title);
//            this.title = title;         
//        }
//
//        public void jetPrint(String text) throws JetException { 
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
//            String[] lines = text.split("\n");
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


}
