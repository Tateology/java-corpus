package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.io.WCTDataSourceDB;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


class WCTDropTargetHandler implements DropTargetListener {
    
    private static WCTDropTargetHandler singleton = null;    
    private WCTViewer viewer = null;
    
    private WCTDropTargetHandler() {
    }
    
    public static WCTDropTargetHandler getInstance() {
        if (singleton == null) {
            singleton = new WCTDropTargetHandler();
        }
        return singleton;
    }
    
    public void registerViewer(WCTViewer viewer) {
        this.viewer = viewer;
    }
    
    @Override
    public void drop(DropTargetDropEvent event) {
        
        Transferable transferable = event.getTransferable();
        
        if (viewer == null) {
            System.err.println("No viewer is registered");            
        }
        
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            event.acceptDrop(DnDConstants.ACTION_COPY);
            try {
                String s = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                System.out.println(s);
                
                
                if (s.startsWith("http://") || s.startsWith("ftp://")) {
                    s = s.split("\n")[0];
                }
                
                if (s.startsWith("http://www.ncdc.noaa.gov/cgi-bin/good-bye.pl?src=")) {
                    s = s.replaceAll("http://www.ncdc.noaa.gov/cgi-bin/good-bye.pl\\?src=", "");
                }
                
                if (s.contains(".html") && ! s.contains("/thredds/dodsC/")) {
                    s = s.replaceAll(".html", ".xml");
                }
                
                if (s.startsWith("HAS") && s.length() == 12) {
                    viewer.getDataSelector().setVisible(true);
                    viewer.getDataSelector().getDataSourcePanel().setDataType(WCTDataSourceDB.NCDC_HAS_FTP);
                    viewer.getDataSelector().getDataSourcePanel().setDataLocation(WCTDataSourceDB.NCDC_HAS_FTP, s);
                    viewer.getDataSelector().submitListFiles();
                }
                else if (s.startsWith("http://") && s.contains("/thredds/dodsC/") && s.endsWith(".html")) {
                    viewer.getDataSelector().setVisible(true);
                    viewer.getDataSelector().getDataSourcePanel().setDataType(WCTDataSourceDB.SINGLE_FILE);
                    String opendapLocation = s.substring(0, s.length()-5);
                    viewer.getDataSelector().getDataSourcePanel().setDataLocation(WCTDataSourceDB.SINGLE_FILE, opendapLocation);
                    viewer.getDataSelector().loadData();
                }
                else if (s.contains("catalog.xml") || (s.contains("/thredds/") && s.endsWith("xml"))) {
                    viewer.getDataSelector().setVisible(true);
                    viewer.getDataSelector().getDataSourcePanel().setDataType(WCTDataSourceDB.THREDDS);
                    viewer.getDataSelector().getDataSourcePanel().setDataLocation(WCTDataSourceDB.THREDDS, s);
                    viewer.getDataSelector().submitListFiles();
                }
                else if (s.startsWith("http://") || s.startsWith("ftp://")) {
                    viewer.getDataSelector().setVisible(true);
                    viewer.getDataSelector().getDataSourcePanel().setDataType(WCTDataSourceDB.URL_DIRECTORY);
                    viewer.getDataSelector().getDataSourcePanel().setDataLocation(WCTDataSourceDB.URL_DIRECTORY, s);
                    viewer.getDataSelector().submitListFiles();
                }
                
                
                
                event.dropComplete(true);
            } catch (UnsupportedFlavorException flavorException) {
                flavorException.printStackTrace();
                event.dropComplete(false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                event.dropComplete(false);
            } catch (Exception e) {
                e.printStackTrace();
                event.dropComplete(false);
            }
        }
        else if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            event.acceptDrop(DnDConstants.ACTION_COPY);
            try {
                List fileList = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                // take first entry
                if (iterator.hasNext()) {
//                    System.out.println("javaFileListFlavor: "+iterator.next().toString());
                    File file = (File) iterator.next();
                    if (! file.isDirectory()) {
                        file = file.getParentFile();
                    }

                    viewer.getDataSelector().setVisible(true);
                    viewer.getDataSelector().getDataSourcePanel().setDataType(WCTDataSourceDB.LOCAL_DISK);
                    viewer.getDataSelector().getDataSourcePanel().setDataLocation(WCTDataSourceDB.LOCAL_DISK, file.toString());
                    viewer.getDataSelector().submitListFiles();
                        
                    
                    System.out.println(file);
                }
                event.dropComplete(true);
            } catch (UnsupportedFlavorException flavorException) {
                flavorException.printStackTrace();
                event.dropComplete(false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                event.dropComplete(false);
            } catch (Exception e) {
                e.printStackTrace();
                event.dropComplete(false);
            }
        }
        else {
            event.rejectDrop();
        }
        
    }

    @Override
    public void dragEnter(DropTargetDragEvent event) {
        if (viewer != null && (
                event.isDataFlavorSupported(DataFlavor.stringFlavor) || 
                event.isDataFlavorSupported(DataFlavor.javaFileListFlavor) )) {
            
            event.acceptDrag(DnDConstants.ACTION_COPY);
        }
        else {
            event.rejectDrag();
        }
    }

    @Override
    public void dragExit(DropTargetEvent event) {
    }

    @Override
    public void dragOver(DropTargetDragEvent event) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent event) {
    }

}
