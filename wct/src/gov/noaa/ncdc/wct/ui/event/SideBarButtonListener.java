package gov.noaa.ncdc.wct.ui.event;

import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.ui.ViewerKmzUtilities;
import gov.noaa.ncdc.wct.ui.ViewerUtilities;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.animation.ExportKMZThread;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class SideBarButtonListener implements ActionListener {
    
    private WCTViewer viewer;
    private ViewerKmzUtilities kmzUtil;
    
    public SideBarButtonListener(WCTViewer viewer) {
        this.viewer = viewer;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("Services")) {
//            nexview.showDataServicesPopUpMenu();
        }
        else if (e.getActionCommand().equalsIgnoreCase("Data")) {
            viewer.toggleDataSelector();
        }
        else if (e.getActionCommand().equalsIgnoreCase("Layers")) {
            viewer.toggleMapSelector();
        }
        else if (e.getActionCommand().equalsIgnoreCase("Capture")) {
            viewer.screenCapture();
        }
        else if (e.getActionCommand().equalsIgnoreCase("Save Image")) {
            ViewerUtilities.saveImage(viewer);   
        }
        else if (e.getActionCommand().equalsIgnoreCase("Save Kmz")) {
            try {
                if (kmzUtil == null) {
                    kmzUtil = new ViewerKmzUtilities(viewer);
                }
                kmzUtil.saveKmz();
            } catch (Exception ex) {
            	ex.printStackTrace();
                JOptionPane.showMessageDialog(viewer, ex.getMessage(),
                        "KMZ CREATION ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }    
        else if (e.getActionCommand().equalsIgnoreCase("Launch Kmz")) {
            try {
                if (kmzUtil == null) {
                    kmzUtil = new ViewerKmzUtilities(viewer);
                }
                if (! kmzUtil.showConfigurationDialog()) {
                	return;
                }
                String timestamp = new SimpleDateFormat("hh.mm.ss").format(new Date());
                File kmzFile = new File(WCTConstants.getInstance().getDataCacheLocation()+File.separator+"wct-snapshot-"+timestamp+".kmz");
                
                if (kmzUtil.getKmzExportDialog() != null && 
                		( kmzUtil.getKmzExportDialog().getNumberOfSweepsToProcess().toUpperCase().startsWith("ALL") ||
                		Integer.parseInt(kmzUtil.getKmzExportDialog().getNumberOfSweepsToProcess().split(" ")[0]) > 1)) {
                
                	ExportKMZThread kmzExportThread = new ExportKMZThread(
                			viewer, 
                			null, 
                			new URL[] { viewer.getCurrentDataURL() }, 
                			true
                		);
                	kmzUtil.getKmzExportDialog().setOutputFile(kmzFile.toString());
                	kmzExportThread.setKmzExportDialog(kmzUtil.getKmzExportDialog());
                	kmzExportThread.setAutoOpenKMZ(true);
                	kmzExportThread.start();
                }
                else {                
                	kmzUtil.saveKmz(kmzFile, false);
                    Desktop.getDesktop().open(kmzFile);
                }
            } catch (Exception ex) {
            	ex.printStackTrace();
                JOptionPane.showMessageDialog(viewer, "Only loaded data layers (not services) may exported "+
                		"to KMZ.", "KMZ CREATION ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (e.getActionCommand().equalsIgnoreCase("Copy Image")) {
            viewer.copyViewToClipboard();
        }       
    }
    
    
}
