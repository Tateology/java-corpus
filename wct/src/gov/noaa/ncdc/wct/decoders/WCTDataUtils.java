package gov.noaa.ncdc.wct.decoders;

import gov.noaa.ncdc.common.Debug;
import gov.noaa.ncdc.wct.decoders.nexrad.Level2Transfer;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.io.WCTTransfer;

import java.awt.Dialog;
import java.awt.Frame;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

import opendap.dap.DAP2Exception;
import ucar.nc2.dods.DODSNetcdfFile;

public class WCTDataUtils {
	
    private static final Logger logger = Logger.getLogger(WCTDataUtils.class.getName());

	
	

    public static URL scan(URL dataURL, FileScanner scannedFile, boolean useWctCache, boolean checkForOpendap, SupportedDataType dataTypeOverride) 
        throws ConnectException, DAP2Exception, IOException, DecodeException, SQLException, ParseException {

        return scan(dataURL, scannedFile, useWctCache, checkForOpendap, dataTypeOverride, null, null, new ArrayList<GeneralProgressListener>());
    }
    
    public static URL scan(URL dataURL, FileScanner scannedFile, boolean useWctCache, boolean checkForOpendap, 
            SupportedDataType dataTypeOverride, Frame owner) 
    	throws ConnectException, DAP2Exception, IOException, DecodeException, SQLException, ParseException {

    	return scan(dataURL, scannedFile, useWctCache, checkForOpendap, dataTypeOverride, owner, new ArrayList<GeneralProgressListener>());
    }

    public static URL scan(URL dataURL, FileScanner scannedFile, boolean useWctCache, boolean checkForOpendap, 
            SupportedDataType dataTypeOverride, Dialog owner) 
    	throws ConnectException, DAP2Exception, IOException, DecodeException, SQLException, ParseException {

    	return scan(dataURL, scannedFile, useWctCache, checkForOpendap, dataTypeOverride, null, owner, new ArrayList<GeneralProgressListener>());
    }

    public static URL scan(URL dataURL, FileScanner scannedFile, boolean useWctCache, boolean checkForOpendap, 
            SupportedDataType dataTypeOverride, Frame owner, ArrayList<GeneralProgressListener> progressListeners) 
    	throws ConnectException, DAP2Exception, IOException, DecodeException, SQLException, ParseException {

    	return scan(dataURL, scannedFile, useWctCache, checkForOpendap, dataTypeOverride, owner, null, progressListeners);
    }
    
    
    private static URL scan(URL dataURL, FileScanner scannedFile, boolean useWctCache, boolean checkForOpendap, 
            SupportedDataType dataTypeOverride, Frame owner, Dialog dialogOwner, ArrayList<GeneralProgressListener> progressListeners) 
    	throws ConnectException, DAP2Exception, IOException, DecodeException, SQLException, ParseException {

        scannedFile.scanURL(dataURL);

     // Check for file compression / opendap
//        boolean isOpendap = false;
//        if (checkForOpendap) {
//            DODSNetcdfFile dodsNc = null;
//            try {
//                dodsNc = new DODSNetcdfFile(dataURL.toString());
//                System.out.println( "dodsNc.getFileTypeDescription  " + dodsNc.getFileTypeDescription() );
//                if (dodsNc != null) {
//                    isOpendap = true;
//                }
//              System.out.println( "DODSNetcdfFile.canOpen  " + DODSNetcdfFile.canOpen(dataURL.toString()) );
//                System.out.println("Found OPeNDAP file!");
//                
////                dodsNc.writeCDL(System.out, false);
//                
//            } catch (Exception e) {
////            e.printStackTrace();
//            } finally {
//                try {
//                    if (dodsNc != null) {
//                        dodsNc.close();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        
//        if (! isOpendap) {
        try {

            if (scannedFile.isZCompressed()) {
                dataURL = Level2Transfer.getNCDCLevel2UNIXZ(dataURL, ! useWctCache, owner);
                scannedFile.scanURL(dataURL);
                System.out.println("DOWNLOADING .Z : "+dataURL);               
            }
            else if (scannedFile.isGzipCompressed()) {
                dataURL = Level2Transfer.getNCDCLevel2GZIP(dataURL, ! useWctCache, owner);
                scannedFile.scanURL(dataURL);
                System.out.println("DOWNLOADING .GZ : "+dataURL);               
            }
            else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
                dataURL = WCTTransfer.getURL(dataURL, null, ! useWctCache, owner, progressListeners);  
                // HARD CODE - for right now, all Level2 files will be checked for "AR2V0001" partial BZIP format
                dataURL = Level2Transfer.decompressAR2V0001(dataURL, ! useWctCache);
                scannedFile.scanURL(dataURL);
            }
            else {
                // Transfer file to local tmp area -- force overwrite if NWS
                if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
                    dataURL = WCTTransfer.getURL(dataURL, null, true, owner, progressListeners);
                }               
                else {
                    dataURL = WCTTransfer.getURL(dataURL, null, ! useWctCache, owner, progressListeners);
                }
            }

            scannedFile.scanURL(dataURL);
            
        } catch (IOException ioe) {
        	
        	if (ioe.getMessage().startsWith("FTP Transfer Aborted")) {
        		return null;
        	}
        	
        	
        	// check for opendap resource
        	if (checkForOpendap) {
//        		DODSNetcdfFile dodsNc = null;
//        		try {
//        			dodsNc = new DODSNetcdfFile(dataURL.toString());
//        			logger.info( "dodsNc.getFileTypeDescription  " + dodsNc.getFileTypeDescription() );
//        			if (dodsNc != null) {
////        				isOpendap = true;
//        			}
//        			logger.info( "DODSNetcdfFile.canOpen  " + DODSNetcdfFile.canOpen(dataURL.toString()) );
//        			logger.info("Found OPeNDAP file!");
//
//        			//              dodsNc.writeCDL(System.out, false);
//
//        		} catch (IOException e) {
//        			logger.fine("OPeNDAP Connection Error: "+e);
//                    logger.fine(Debug.getStackTraceString(e));
//        			logger.fine("HTTP/FTP Connection Error: "+ioe);
//                    logger.fine(Debug.getStackTraceString(e));
////                    throw e;
//        		} finally {
//        			try {
//        				if (dodsNc != null) {
//        					dodsNc.close();
//        				}
//        			} catch (Exception e) {
//                        logger.fine(Debug.getStackTraceString(e));
//        			}
//        		}
        	}
        	else {
                logger.fine(Debug.getStackTraceString(ioe));
                throw ioe;
        	}
        }
        
        
        
        // set all unknowns to gridded unless otherwise specified in data selector
        // - many aggregations and iosp-driven files have wierd or no file extensions.
        if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN && dataTypeOverride != null) {                
            scannedFile.getLastScanResult().setDataType(dataTypeOverride);
        }
        
        // default all unknowns to GRIDDED type
        if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {                
            scannedFile.getLastScanResult().setDataType(SupportedDataType.GRIDDED);
        }

//        if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {
//            throw new DecodeException("This data type is not supported.");
//        }
        
        return dataURL;
    }
}
