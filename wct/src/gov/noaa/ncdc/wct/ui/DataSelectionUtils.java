package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.WCTDataUtils;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.WCTTransfer;
import gov.noaa.ngdc.nciso.ThreddsTranslatorUtil;
import gov.noaa.ngdc.nciso.ThreddsUtilitiesException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ucar.nc2.NCdumpW;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dods.DODSNetcdfFile;

public class DataSelectionUtils {
	
	public static enum Type { NCML, CDL };

	
	
	


	public static void showUddc(final WCTViewer viewer, final DataSelector dataSelect, final URL url) 
		throws ThreddsUtilitiesException, MalformedURLException, IOException {
		
//		SupplementalDialog dialog = new SupplementalDialog(dataSelect, "ISO 19115 metadata from "+url);	        
//	    dialog.setText(getUddcString(viewer, dataSelect, url));
//	    dialog.setVisible(true);
		
		JEditorPane editPane = new JEditorPane();
        editPane.setContentType("text/html");
        editPane.setText(getUddcString(viewer, dataSelect, url));
        // the next 2 lines scroll the edit pane to the top
        editPane.setSelectionStart(0);
        editPane.setSelectionEnd(0);
        
//        System.out.println(editPane.getText());
        

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JScrollPane scrollPane = new JScrollPane(editPane);
        scrollPane.setAutoscrolls(false);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JDialog dialog = new JDialog(dataSelect);
        dialog.setTitle("NetCDF Attribute Convention for Dataset Discovery Report");
        dialog.add(mainPanel);
        dialog.pack();
        
        dialog.setSize(new Dimension(750, 500));

        dialog.setVisible(true);
        
	}
	
	
	public static String getUddcString(final WCTViewer viewer, final DataSelector dataSelect, final URL url) 
		throws ThreddsUtilitiesException, MalformedURLException, IOException {
		
		String ncml = getDumpString(Type.NCML, viewer, dataSelect, url, true);
		StringWriter sw = new StringWriter();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(ncml.getBytes());
		URL xslUrl = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                "/config/xsl/UnidataDDCount-HTML.xsl", null);
		ThreddsTranslatorUtil.doTransform(xslUrl.openStream(), bais, sw);
		sw.flush();

		return sw.toString();
	}
	
	public static void showIso(final WCTViewer viewer, final DataSelector dataSelect, final URL url) 
		throws ThreddsUtilitiesException, MalformedURLException, IOException {
		
		WCTTextDialog dialog = new WCTTextDialog(dataSelect, "", "ISO 19115 metadata from "+url, true);	        
	    dialog.setText(getIsoString(viewer, dataSelect, url));
	    dialog.setVisible(true);
	}
	
	
	public static String getIsoString(final WCTViewer viewer, final DataSelector dataSelect, final URL url) 
		throws ThreddsUtilitiesException, MalformedURLException, IOException {
		
		String ncml = getDumpString(Type.NCML, viewer, dataSelect, url, true);
		StringWriter sw = new StringWriter();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(ncml.getBytes());
		URL xslUrl = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                "/config/xsl/UnidataDD2MI.xsl", null);
		ThreddsTranslatorUtil.doTransform(xslUrl.openStream(), bais, sw);
		sw.flush();

		return sw.toString();
	}
	
	
	
    public static void showNcmlDump(final WCTViewer viewer, final DataSelector dataSelect, final URL url) {
    	showDump(Type.NCML, viewer, dataSelect, url);
    }
    
    
    public static void showDump(final WCTViewer viewer, final DataSelector dataSelect, final URL url) {
    	showDump(Type.CDL, viewer, dataSelect, url);
    }
    

    private static void showDump(final Type dumpType, final WCTViewer viewer, 
    		final DataSelector dataSelect, final URL url) {
    	
    	
    	if (dumpType == Type.NCML) {
    		NcMLEditor ncmlEditor = new NcMLEditor("NcML Editor", viewer);
    		ncmlEditor.setSize(600, 600);
    		ncmlEditor.setLocationRelativeTo(viewer);
    		ncmlEditor.setVisible(true);
    		return;
    	}
    	
    	
    	
    	
    	
    	
    	String title = (dumpType == Type.CDL) ? "ncdump" : "NcML dump";

    	WCTTextDialog dialog = new WCTTextDialog(dataSelect, "", title+" "+url, true);
        
        dialog.setText(getDumpString(dumpType, viewer, dataSelect, url, true));
        
        dialog.setVisible(true);
    }
    
    
    
    public static String getDumpString(final Type dumpType, final WCTViewer viewer,
    		final DataSelector dataSelect, URL url, final boolean useCache) {
    	
    	
    	String dumpString = null;
  
        try {
        	
        	
        	if (useCache) {
        		FileScanner scannedFile = new FileScanner();
        		scannedFile.scanURL(url);
        		url = WCTDataUtils.scan(url, scannedFile, true, true, null, dataSelect);
        	}
        	
        	
        	
        	
        	

        	ByteArrayOutputStream out = new ByteArrayOutputStream();
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(out);

//            if (url.getProtocol().equals("ftp") || url.getProtocol().startsWith("http")) {
            if (url.getProtocol().equals("ftp")) {
            	
//                String cacheUrlString = WCTTransfer.getURL(url, false, viewer).toString().replaceAll("file:/", "").replace('/', File.separatorChar).replaceAll(" ", "%20");
//                String cacheUrlString = WCTTransfer.getURL(url, false, viewer).toString();
                
            	
            	final URL dataURL = url;
                String cacheUrlString = (String)foxtrot.ConcurrentWorker.post(new foxtrot.Task() {
                    @Override
                    public Object run() throws Exception {                        
                        String s = WCTTransfer.getURL(dataURL, false, viewer).toString();
                        dataSelect.checkCacheStatus();
                        return s;
                    }
                });

                
                System.out.println(cacheUrlString);
//                NCdump.print(cacheUrlString, out);
//                NCdumpW.printHeader(cacheUrlString, writer);
//                dialog.setText(writer.toString());
                
                NetcdfDataset ncd = NetcdfDataset.acquireDataset(cacheUrlString.toString(), WCTUtils.getSharedCancelTask());
                
                if (dumpType == Type.NCML) {
                    NCdumpW.writeNcML(ncd, pw, false, null);
                    dumpString = out.toString();
                }
                else {
                	dumpString = ncd.toString();
                }
                ncd.close();

            }
            else {
                try {
//                    NCdump.print(url.toString(), out);
//                    NCdumpW.printHeader(url.toString(), writer);
                    
//                    dialog.setText(writer.toString());
                    
//                    NetcdfDataset ncd = NetcdfDataset.acquireDataset(url.toString(), WCTUtils.getEmptyCancelTask());
//                    NetcdfDataset ncd = NetcdfDataset.openDataset(url.toString(), false, WCTUtils.getEmptyCancelTask());
//                    dialog.setText(ncd.toString());
//                    ncd.close();
                    
                    StringWriter sw = new StringWriter();
                    if (dumpType == Type.NCML) {
                        NetcdfDataset ncd = NetcdfDataset.acquireDataset(url.toString(), WCTUtils.getSharedCancelTask());
                        NCdumpW.writeNcML(ncd, pw, false, null);
                        dumpString = out.toString();
                        ncd.close();
                    }
                    else {
                    	NetcdfFile ncfile = NetcdfDataset.open(url.toString(), WCTUtils.getSharedCancelTask());
                        NCdumpW.print(ncfile, "-h", sw, WCTUtils.getSharedCancelTask());
                        dumpString = sw.toString();   	
                        ncfile.close();
                    }
                    
                } catch (Exception e) {
                	e.printStackTrace();
                	System.out.println("http request error: "+e.getMessage());
                    System.out.println("ncdump attempt 2 - attempting opendap connection...");
//                    NCdumpW.print(DODSNetcdfFile.open(url.toString()), "", writer, WCTUtils.getEmptyCancelTask());
                    DODSNetcdfFile dodsFile = new DODSNetcdfFile(url.toString());
                    if (dumpType == Type.NCML) {
                    	dodsFile.writeNcML(out, null);
                    }
                    else {
                    	dodsFile.writeCDL(pw, false);
                    }
                    dodsFile.close();
                    
                    dumpString = out.toString();
                }
            }


        }
        catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(dataSelect, "Unable to dump netcdf structure for: "+url, 
                    "DATA LOAD ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);

        }
        
        return dumpString;
    }

}
