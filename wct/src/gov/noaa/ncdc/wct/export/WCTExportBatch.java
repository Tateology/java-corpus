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

package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.common.Debug;
import gov.noaa.ncdc.nexradexport.ExtractCAPPI;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTIospManager;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.event.DataExportEvent;
import gov.noaa.ncdc.wct.event.DataExportListener;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.ui.AbstractKmzUtilities.AltitudeMode;
import gov.noaa.ncdc.wct.ui.JVMPropertyChecker;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;
import gov.noaa.ncdc.wct.ui.WCTViewerSplash;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import thredds.inventory.bdb.MetadataManager;


/**
 *  Description of the Class
 *
 * @author    steve.ansari
 */
public class WCTExportBatch {


    private static final Logger logger = Logger.getLogger(WCTExportBatch.class.getName());
    
    private HashMap<String, String> configReplacementsMap = new HashMap<String, String>();


    
    public WCTExportBatch() {
    	init();
    }
    
    
    private void init() {
    	
    	JVMPropertyChecker propChecker = new JVMPropertyChecker();
    	propChecker.checkUserHome();

    	
        
        // Set Unidata metadata cache Berkley DB location
        // This has to be different than the default - otherwise the db will be locked when using ToolsUI (and IDV?),
        // causing problems opening NcML files.
        try {
        	MetadataManager.setCacheDirectory(
        			WCTConstants.getInstance().getCacheLocation()+File.separator+"unidata-bdb",
        			10*1024*1024, 50);
        } catch (Exception e) {
            logger.fine(Debug.getStackTraceString(e));
        }
        
        // Disable geotools warning message
		Logger.getLogger("org.geotools.gp").setLevel(Level.SEVERE);
	
		
    }


    /**
     *  Description of the Method
     *
     * @param  args  Description of the Parameter
     */
    public void runBatchMode(String[] args) {
        
        try {
        	if (args.length == 1 && args[0].equals("-version")) {
                checkVersion();
                return;
        	}
        	
        	

//          System.out.println(":::::::::::::: OUTPUT FORMAT: " + exporter.getOutputFormat());
            
            // METADATA EXPORT
            if (args.length == 3) {
                WCTMetaExport metaExporter = new WCTMetaExport();
                WCTIospManager.registerWctIOSPs();
                URL inurl;
                File infile;
                File outfile = new File(args[1]);
                // Check if URL or File supplied for input file
                if (args[0].startsWith("http://") ||
                        args[0].startsWith("ftp://") ||
                        args[0].startsWith("file://")) {

                    inurl = new URL(args[0]);
                    doMetaExport(metaExporter, inurl, outfile);
                }
                else {
                    infile = new File(args[0]);
                    doMetaExport(metaExporter, infile, outfile);
                }
            }
            // DATA EXPORT
            else if (args.length == 4) {
                WCTExport exporter = new WCTExport();
                WCTIospManager.registerWctIOSPs();
                exporter.setAutoRenameOutput(false);
                exporter.addDataExportListener(new BatchExportListener());
                exporter.setOutputFormat(readOutputFormat(args[2]));

                URL inurl;
                File infile;
                File outfile = new File(args[1]);

                args[3] = parseConfigPathAndReplacements(args[3], configReplacementsMap);

                // Check if URL or File supplied for config file
                if (args[3].startsWith("http://") ||
                        args[3].startsWith("ftp://") ||
                        args[3].startsWith("file://")) {

                    processConfigFile(exporter, new URL(args[3]), configReplacementsMap);
                }
                else {
                    processConfigFile(exporter, new File(args[3]), configReplacementsMap);
                }

                // Check if URL or File supplied for input file
                if (args[0].startsWith("http://") ||
                        args[0].startsWith("ftp://") ||
                        args[0].startsWith("file://")) {

                    inurl = new URL(args[0]);
                    doBatchExport(exporter, inurl, outfile);
                }
                else {
                    infile = new File(args[0]);
                    doBatchExport(exporter, infile, outfile);
                }
            }
            else if (args.length == 2) {
                WCTExport exporter = new WCTExport();
                WCTIospManager.registerWctIOSPs();
                exporter.setAutoRenameOutput(false);
                exporter.addDataExportListener(new BatchExportListener());

                exporter.setOutputFormat(readOutputFormat(args[1]));
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
                    String str;
                    String[] strArray;
                    while ((str = reader.readLine()) != null) {
                        if (str.trim().length() != 0 && str.charAt(0) != '#') {
                            strArray = str.split(",");
                            strArray[2] = parseConfigPathAndReplacements(strArray[2], configReplacementsMap);
                            // Check if URL or File supplied for config file
                            if (strArray[2].startsWith("http://") ||
                                    strArray[2].startsWith("ftp://") ||
                                    strArray[2].startsWith("file://")) {

                                processConfigFile(exporter, new URL(strArray[2]), configReplacementsMap);
                            }
                            else {
                                processConfigFile(exporter, new File(strArray[2]), configReplacementsMap);
                            }

                            // Check if URL or File
                            if (strArray[0].startsWith("http://") ||
                                    strArray[0].startsWith("ftp://") ||
                                    strArray[0].startsWith("file://")) {

                                doBatchExport(exporter, new URL(strArray[0]), new File(strArray[1]));
                            }
                            else {
                                doBatchExport(exporter, new File(strArray[0]), new File(strArray[1]));
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.fine(Debug.getStackTraceString(e));
                    printUsage();
                }
            }
            else {
                printUsage();
            }
        } catch (Exception e) {
            logger.warning("WEATHER AND CLIMATE TOOLKIT - DATA EXPORTER: BATCH PROCESSING ERROR: \n" + e);
            logger.fine(Debug.getStackTraceString(e));
        } finally {
//            checkVersion();
        }
    }


    /**
     * Parse the config file location string, which can optionally contain extra replacement key-value pairs.
     * The keys are defined anywhere in the config file, using the ${KEY} syntax.
     * Example) wctBatchConfig.xml:::KEY1=VALUE1;KEY2=VALUE2;KEY3=VALUE3;
     * @param configPathWithReplacements
     * @param configReplacementsMap
     * @return
     */
    public static String parseConfigPathAndReplacements(String configPathWithReplacements, HashMap<String, String> configReplacementsMap) {
    	if (! configPathWithReplacements.contains(":::")) {
    		return configPathWithReplacements;
    	}
    	
    	String[] pathAndReplacement = configPathWithReplacements.split(":::");
    	String replacementString = pathAndReplacement[1];
    	String[] pairArray = replacementString.split(";");
    	for (String pair : pairArray) {
    		if (pair.trim().length() > 0) {
    			String[] keyAndValue = pair.split("=");
    			if (keyAndValue.length == 2) {
    				configReplacementsMap.put(keyAndValue[0], keyAndValue[1]);
    			}
    		}
    	}
    	return pathAndReplacement[0];
    }



    /**
     *  Run the batch export given a local input file
     *
     * @param  exporter  WCTExport object
     * @param  infile    Input file or directory of input files (will process all files in directory)
     * @param  outfile   Output file
     */
    public static void doBatchExport(WCTExport exporter, File infile, File outfile) {

        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        logger.info(" NEXRAD EXPORT:");
        logger.info("   CUT: " + exporter.getExportCut());
        logger.info("   MOMENT: " + exporter.getExportMoment());
        logger.info("   CLASSIFY: " + exporter.getExportClassify());
        logger.info("   USE-RF-VALUES: " + exporter.getExportUseRF());
        logger.info("   EXPORT-POINTS: " + exporter.getExportPoints());
        logger.info("   EXPORT-ALL-POINTS: " + exporter.getExportAllPoints());
        logger.info("   REDUCE-POLYGONS: " + exporter.getExportReducePolys());
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        logger.info("   L2FILTER: " + exporter.getExportL2Filter());
        logger.info("   L3FILTER: " + exporter.getExportL3Filter());
        logger.info("   XMRGFILTER: " + exporter.getExportXMRGFilter());
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        logger.info("   GRID SIZE: " + exporter.getExportGridSize());
        logger.info("   NO DATA: " + exporter.getExportGridNoData());
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");




        if (infile.isDirectory()) {
            File[] files = infile.listFiles();
            Rectangle2D.Double l3Bounds = exporter.getExportL3Filter().getExtentFilter();
            Rectangle2D.Double l2Bounds = exporter.getExportL2Filter().getExtentFilter();
            Rectangle2D.Double gridBounds = exporter.getExportGridFilter().getExtentFilter();
            Rectangle2D.Double xmrgBounds = exporter.getExportXMRGFilter().getExtentFilter();
            if (l3Bounds != null) l3Bounds = new Rectangle2D.Double(l3Bounds.getX(), l3Bounds.getY(), l3Bounds.getWidth(), l3Bounds.getHeight());
            if (l2Bounds != null) l2Bounds = new Rectangle2D.Double(l2Bounds.getX(), l2Bounds.getY(), l2Bounds.getWidth(), l2Bounds.getHeight());
            if (gridBounds != null) gridBounds = new Rectangle2D.Double(gridBounds.getX(), gridBounds.getY(), gridBounds.getWidth(), gridBounds.getHeight());
            if (xmrgBounds != null) xmrgBounds = new Rectangle2D.Double(xmrgBounds.getX(), xmrgBounds.getY(), xmrgBounds.getWidth(), xmrgBounds.getHeight());
            
            
            
            // Loop through all files -- name output same as input but with extension
            for (int n = 0; n < files.length; n++) {
                if (!files[n].isDirectory()) {
                    if (outfile.isDirectory()) {

                    	String ext = WCTExportUtils.getExportFileExtension(exporter.getOutputFormat());
                        
                        System.out.println("PROCESSING: "+files[n]+" -> "+
                        		new File(outfile.toString() + File.separator + files[n].getName() + ext) +
                                " , FORMAT '"+exporter.getOutputFormat()+"'");                  
                    	
                        try {
                            // reset extent filter if it was adjusted during previous processing - new objects must be created
                            if (l3Bounds != null) exporter.getExportL3Filter().setExtentFilter(new Rectangle2D.Double(l3Bounds.getX(), l3Bounds.getY(), l3Bounds.getWidth(), l3Bounds.getHeight()));
                            if (l2Bounds != null) exporter.getExportL2Filter().setExtentFilter(new Rectangle2D.Double(l2Bounds.getX(), l2Bounds.getY(), l2Bounds.getWidth(), l2Bounds.getHeight()));
                            if (gridBounds != null) exporter.getExportGridFilter().setExtentFilter(new Rectangle2D.Double(gridBounds.getX(), gridBounds.getY(), gridBounds.getWidth(), gridBounds.getHeight()));
                            if (xmrgBounds != null) exporter.getExportXMRGFilter().setExtentFilter(new Rectangle2D.Double(xmrgBounds.getX(), xmrgBounds.getY(), xmrgBounds.getWidth(), xmrgBounds.getHeight()));
                            exporter.exportData(files[n].toURI().toURL(), new File(outfile.toString() + 
                            		File.separator + WCTExportUtils.getOutputFilename(files[n].getName(), exporter.getOutputFormat()) ) );

                        } catch (WCTExportNoDataException e) {
                            logger.warning("NO DATA PRESENT IN NEXRAD FILE: "+files[n]);
                        } catch (WCTExportException nee) {
                            logger.severe("NEXRAD EXPORT ERROR IN NEXRAD FILE: "+files[n]+"\nERROR = "+nee);
                        } catch (ConnectException ce) {
                            logger.severe("CONNECTION ERROR WITH NEXRAD FILE: "+files[n]);
                        } catch (Exception e) {
                        	e.printStackTrace();
                            logger.severe("GENERAL BATCH PROCESSING ERROR: " + e);
                            logger.fine(Debug.getStackTraceString(e));
                        }

                    }
                    else {

                    	String ext = WCTExportUtils.getExportFileExtension(exporter.getOutputFormat());
                        System.out.println("PROCESSING: "+files[n]+" -> "+outfile+
                                (outfile.toString().endsWith(ext) ? "" : ext )+
                                " , FORMAT '"+exporter.getOutputFormat()+"'");                  

                    	
                        try {
                            // reset extent filter if it was adjusted during previous processing - new objects must be created
                            if (l3Bounds != null) exporter.getExportL3Filter().setExtentFilter(new Rectangle2D.Double(l3Bounds.getX(), l3Bounds.getY(), l3Bounds.getWidth(), l3Bounds.getHeight()));
                            if (l2Bounds != null) exporter.getExportL2Filter().setExtentFilter(new Rectangle2D.Double(l2Bounds.getX(), l2Bounds.getY(), l2Bounds.getWidth(), l2Bounds.getHeight()));
                            if (gridBounds != null) exporter.getExportGridFilter().setExtentFilter(new Rectangle2D.Double(gridBounds.getX(), gridBounds.getY(), gridBounds.getWidth(), gridBounds.getHeight()));
                            if (xmrgBounds != null) exporter.getExportXMRGFilter().setExtentFilter(new Rectangle2D.Double(xmrgBounds.getX(), xmrgBounds.getY(), xmrgBounds.getWidth(), xmrgBounds.getHeight()));

                            exporter.exportData(files[n].toURI().toURL(), outfile);

                        } catch (WCTExportNoDataException e) {
                            logger.warning(" NO DATA PRESENT IN NEXRAD FILE: "+files[n]);
                        } catch (WCTExportException nee) {
                            logger.severe(" NEXRAD EXPORT ERROR IN NEXRAD FILE: "+files[n]+"\nERROR = "+nee);
                        } catch (ConnectException ce) {
                            logger.severe(" CONNECTION ERROR WITH NEXRAD FILE: "+files[n]);
                        } catch (Exception e) {
                            logger.severe(" GENERAL BATCH PROCESSING ERROR: " + e);
                            logger.fine(Debug.getStackTraceString(e));
                        }

                    }
                }
            }
        }
        else {

            try {

                if (outfile.isDirectory()) {
                    System.out.println("PROCESSING: "+infile+" -> "+
                    		new File(outfile.toString() + File.separator + infile.getName())+
                            " , FORMAT '"+exporter.getOutputFormat()+"'");                  
                    
                    exporter.exportData(infile.toURI().toURL(), new File(outfile.toString() + File.separator + infile.getName()));
                }
                else {
                	String ext = WCTExportUtils.getExportFileExtension(exporter.getOutputFormat());
                    System.out.println("PROCESSING: "+infile+" -> "+outfile+
                            (outfile.toString().endsWith(ext) ? "" : ext )+
                            " , FORMAT '"+exporter.getOutputFormat()+"'");                  
                	
                    exporter.exportData(infile.toURI().toURL(), outfile);
                }

            } catch (WCTExportNoDataException e) {
                logger.warning(" NO DATA PRESENT IN NEXRAD FILE: "+infile);
            } catch (WCTExportException nee) {
                logger.severe(" NEXRAD EXPORT ERROR IN NEXRAD FILE: "+infile+"\nERROR = "+nee);
            } catch (ConnectException ce) {
                logger.severe(" CONNECTION ERROR WITH NEXRAD FILE: "+infile);
            } catch (Exception e) {
            	e.printStackTrace();
                logger.severe(" GENERAL BATCH PROCESSING ERROR: " + e);
                logger.fine(Debug.getStackTraceString(e));
            }

        }
    }
    
    
    
    /**
     *  Run the batch export given a local input file
     *
     * @param  exporter  WCTExport object
     * @param  infile    Input file or directory of input files (will process all files in directory)
     * @param  outfile   Output file
     * @throws IOException 
     */
    public static void doMetaExport(WCTMetaExport metaExporter, File infile, File outfile) throws IOException {

        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        logger.info("   METADATA EXPORT.... ");
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");



        StringBuilder sb = new StringBuilder();
        File outputCsvFile = null;

        if (infile.isDirectory()) {
            File[] files = infile.listFiles();
            // Loop through all files -- name output same as input but with extension
            for (int n = 0; n < files.length; n++) {
                if (!files[n].isDirectory()) {
                	
                    System.out.println("PROCESSING: "+files[n]+" -> "+
                    		(outfile.isDirectory() ? "metadata.csv" : outfile)+
                            " , FORMAT 'CSV'");                  

                	
                    if (outfile.isDirectory()) {

                        try {

                            outputCsvFile = new File(outfile.toString() + File.separator + "metadata.csv");
                            sb.append(metaExporter.getMetadata(files[n].toURI().toURL())+"\n");

                        } catch (WCTExportNoDataException e) {
                            logger.warning("NO DATA PRESENT IN FILE: "+files[n]);
                        } catch (WCTExportException nee) {
                            logger.severe("EXPORT ERROR IN FILE: "+files[n]+"\nERROR = "+nee);
                        } catch (ConnectException ce) {
                            logger.severe("CONNECTION ERROR WITH FILE: "+files[n]);
                        } catch (Exception e) {
                            logger.severe("GENERAL BATCH PROCESSING ERROR: " + e);
                            logger.fine(Debug.getStackTraceString(e));
                        }

                    }
                    else {


                        try {
                            outputCsvFile = outfile;
                            sb.append(metaExporter.getMetadata(files[n].toURI().toURL())+"\n");

                        } catch (WCTExportNoDataException e) {
                            logger.warning(" NO DATA PRESENT IN FILE: "+files[n]);
                        } catch (WCTExportException nee) {
                            logger.severe(" EXPORT ERROR IN FILE: "+files[n]+"\nERROR = "+nee);
                        } catch (ConnectException ce) {
                            logger.severe(" CONNECTION ERROR WITH FILE: "+files[n]);
                        } catch (Exception e) {
                            logger.severe(" GENERAL BATCH PROCESSING ERROR: " + e);
                            logger.fine(Debug.getStackTraceString(e));
                        }

                    }
                }
            }
        }
        else {

            try {
                System.out.println("PROCESSING: "+infile+" -> "+
                		(outfile.isDirectory() ? "metadata.csv" : outfile)+
                        " , FORMAT 'CSV'");                  


            	

                if (outfile.isDirectory()) {
                    outputCsvFile = new File(outfile.toString() + File.separator + "metadata.csv");
                    sb.append(metaExporter.getMetadata(infile.toURI().toURL())+"\n");
                }
                else {
                    outputCsvFile = outfile;
                    sb.append(metaExporter.getMetadata(infile.toURI().toURL())+"\n");
                }

            } catch (WCTExportNoDataException e) {
                logger.warning(" NO DATA PRESENT IN FILE: "+infile);
            } catch (WCTExportException nee) {
                logger.severe(" EXPORT ERROR IN FILE: "+infile+"\nERROR = "+nee);
            } catch (ConnectException ce) {
                logger.severe(" CONNECTION ERROR WITH FILE: "+infile);
            } catch (Exception e) {
                logger.severe(" GENERAL BATCH PROCESSING ERROR: " + e);
                logger.fine(Debug.getStackTraceString(e));
            }

        }

        if ((! outputCsvFile.toString().endsWith(".csv") && (! outputCsvFile.toString().endsWith(".txt")))) {
            outputCsvFile = new File(outputCsvFile.toString()+".csv");
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputCsvFile));
        bw.write(sb.toString());
        bw.close();

    }


    

    
    
    
    
    
    
    /**
     *  Run the batch export given a local input file
     *
     * @param  exporter  WCTExport object
     * @param  infile    Input file or directory of input files (will process all files in directory)
     * @param  outfile   Output file
     * @throws IOException 
     */
    public static void doMetaExport(WCTMetaExport metaExporter, URL inurl, File outfile) throws IOException {

        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        logger.info("   METADATA EXPORT.... ");
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");

        System.out.println("PROCESSING: "+inurl+" -> "+
        		(outfile.isDirectory() ? "metadata.csv" : outfile)+
                " , FORMAT 'CSV'");                  

        
        


        StringBuilder sb = new StringBuilder();
        File outputCsvFile = null;

        try {

            if (outfile.isDirectory()) {
                outputCsvFile = new File(outfile.toString() + File.separator + "metadata.csv");
                sb.append(metaExporter.getMetadata(inurl)+"\n");
            }
            else {
                outputCsvFile = outfile;
                sb.append(metaExporter.getMetadata(inurl)+"\n");
            }

        } catch (WCTExportNoDataException e) {
            logger.warning(" NO DATA PRESENT IN FILE: "+inurl);
        } catch (WCTExportException nee) {
            logger.severe(" EXPORT ERROR IN FILE: "+inurl+"\nERROR = "+nee);
        } catch (ConnectException ce) {
            logger.severe(" CONNECTION ERROR WITH FILE: "+inurl);
        } catch (Exception e) {
            logger.severe(" GENERAL BATCH PROCESSING ERROR: " + e);
            logger.fine(Debug.getStackTraceString(e));
        }


        if ((! outputCsvFile.toString().endsWith(".csv") && (! outputCsvFile.toString().endsWith(".txt")))) {
            outputCsvFile = new File(outputCsvFile.toString()+".csv");
        }
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputCsvFile));
        bw.write(sb.toString());
        bw.close();

    }



    
    
    
    
    
    
    
    
    

    /**
     *  Run the batch export given a local input file
     *
     * @param  exporter  WCTExport object
     * @param  inurl     Input url of file
     * @param  outfile   Output file
     */
    public static void doBatchExport(WCTExport exporter, URL inurl, File outfile) {

    	String ext = WCTExportUtils.getExportFileExtension(exporter.getOutputFormat());
        System.out.println("PROCESSING: "+inurl+" -> "+outfile+
                (outfile.toString().endsWith(ext) ? "" : ext )+
                " , FORMAT '"+exporter.getOutputFormat()+"'");                  

        
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        logger.info(" NEXRAD EXPORT:");
        logger.info("   CUT: " + exporter.getExportCut());
        logger.info("   MOMENT: " + exporter.getExportMoment());
        logger.info("   CLASSIFY: " + exporter.getExportClassify());
        logger.info("   USE-RF-VALUES: " + exporter.getExportUseRF());
        logger.info("   EXPORT-POINTS: " + exporter.getExportPoints());
        logger.info("   EXPORT-ALL-POINTS: " + exporter.getExportAllPoints());
        logger.info("   REDUCE-POLYGONS: " + exporter.getExportReducePolys());
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        logger.info("   L2FILTER: " + exporter.getExportL2Filter());
        logger.info("   L3FILTER: " + exporter.getExportL3Filter());
        logger.info("   XMRGFILTER: " + exporter.getExportXMRGFilter());
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        logger.info("   GRID SIZE: " + exporter.getExportGridSize());
        logger.info("   NO DATA: " + exporter.getExportGridNoData());
        logger.info(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        
        if (outfile.isDirectory()) {
            // strip off filename from url to form output filename if outfile is a directory
            String urlString = inurl.toString();
            String outname = urlString.substring(urlString.lastIndexOf("/")+1, urlString.length());

            File filename = new File(outfile.toString() + File.separator + outname);

            try {
                
                exporter.exportData(inurl, filename);

            } catch (WCTExportNoDataException e) {
                logger.warning(" NO DATA PRESENT IN FILE: "+inurl);
            } catch (WCTExportException nee) {
                logger.severe(" EXPORT ERROR IN FILE: "+inurl+"\nERROR = "+nee);
            } catch (ConnectException ce) {
                logger.severe(" CONNECTION ERROR WITH FILE: "+inurl);
            } catch (Exception e) {
                logger.severe(" GENERAL BATCH PROCESSING ERROR: " + e);
                logger.fine(Debug.getStackTraceString(e));
            }


        }
        else {

            try {
                
                exporter.exportData(inurl, outfile);

            } catch (WCTExportNoDataException e) {
                logger.warning(" NO DATA PRESENT IN FILE: "+inurl);
            } catch (WCTExportException nee) {
                logger.severe(" EXPORT ERROR IN FILE: "+inurl+"\nERROR = "+nee);
            } catch (ConnectException ce) {
                logger.severe(" CONNECTION ERROR WITH FILE: "+inurl);
            } catch (Exception e) {
            	e.printStackTrace();
                logger.severe(" GENERAL BATCH PROCESSING ERROR: " + e);
                logger.fine(Debug.getStackTraceString(e));
            }

        }


    }



    /**
     *  Set up WCTExport object
     *
     * @param  exporter    WCTExport object
     * @param  configFile  XML Config file
     * @throws XPathExpressionException 
     */
    public static void processConfigFile(WCTExport exporter, File configFile, HashMap<String, String> replacementMap) 
    throws java.net.MalformedURLException, XPathExpressionException {
        processConfigFile(exporter, configFile.toURI().toURL(), replacementMap);
    }

    /**
     *  Set up WCTExport object
     *
     * @param  exporter    WCTExport object
     * @param  configFile  XML Config URL
     * @throws XPathExpressionException 
     */
    public static void processConfigFile(WCTExport exporter, URL configURL, HashMap<String, String> replacementMap) throws XPathExpressionException {
        
        // set up default filters
        exporter.setExportRadialFilter(new WCTFilter());
        exporter.setExportL3Filter(new WCTFilter());
        exporter.setExportGridSatelliteFilter(new WCTFilter());
        
        // Read xml file
        try {

        	// Load XML file into string and apply replacements
        	String xmlConfigString = IOUtils.toString(configURL.openStream());
        	for (String replacementKey : replacementMap.keySet()) {
        		xmlConfigString = xmlConfigString.replace("${"+replacementKey+"}", replacementMap.get(replacementKey));
        	}
        	
        	
            // Parse file to check for XML errors
            
            // Load in DOM
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//            Document doc = docBuilder.parse(configURL.toString());
            Document doc = docBuilder.parse(new ByteArrayInputStream(xmlConfigString.getBytes()));

            // set up XPATH
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            
            
            // normalize text representation
            doc.getDocumentElement().normalize();
            
            // ---------------------------------------------------------------------------------------------
            // Get Logging options
            // ---------------------------------------------------------------------------------------------
            String logging = WCTUtils.getXPathValue(doc, xpath, "//logging/text()");
            if (logging == null) {
                logger.warning(" The logging section is missing.  Default values (SEVERE and WARNING) will be used."+
                        "\nPlease refer to the Batch Processing documentation for more information.");
            }
            else  {

                String str = logging.trim();
                
                Level logLevel = Level.WARNING;
//                org.apache.log4j.Level l4jLevel = org.apache.log4j.Level.WARN;
                if (str.trim().equalsIgnoreCase("SEVERE")) {
                    logLevel = Level.SEVERE;
//                    l4jLevel = org.apache.log4j.Level.ERROR;
                }
                else if (str.trim().equalsIgnoreCase("WARNING")) {
                    logLevel = Level.WARNING;
//                    l4jLevel = org.apache.log4j.Level.WARN;
                }
                else if (str.trim().equalsIgnoreCase("INFO")) {
                    logLevel = Level.INFO;
//                    l4jLevel = org.apache.log4j.Level.INFO;
                }
                else if (str.trim().equalsIgnoreCase("DEBUG")) {
                    logLevel = Level.FINE;
//                    l4jLevel = org.apache.log4j.Level.INFO;
                }
                logger.setLevel(logLevel);
                Logger.getLogger("gov.noaa.ncdc").setLevel(logLevel);
                
                // log4j logging - used with NCJ api
//                BasicConfigurator.configure();
//                org.apache.log4j.Logger.getLogger("ucar").setLevel(l4jLevel);
//                org.apache.log4j.Logger.getLogger("httpclient").setLevel(l4jLevel);
//                org.apache.log4j.Logger.getLogger("org.apache.commons").setLevel(l4jLevel);
//                
//                org.apache.log4j.Logger.getLogger("org").setLevel(org.apache.log4j.Level.OFF);
                Logger.getLogger("org").setLevel(Level.OFF);
            }
            
            
            logger.info(doc.getDocumentElement().getNodeName()+" version: "+doc.getDocumentElement().getAttribute("version"));
            
            
            // ---------------------------------------------------------------------------------------------
            // Get Cache options
            // ---------------------------------------------------------------------------------------------
            String useCache = WCTUtils.getXPathValue(doc, xpath, "//useCache/text()");
            if (useCache == null) {
                logger.warning(" The <useCache> section is missing.  Default value of TRUE will be used."+
                        "\nPlease refer to the Batch Processing documentation for more information.");
            }
            else  {
            	try {
            		exporter.setUseWctCache(Boolean.parseBoolean(useCache));
            	} catch (Exception e) {
            		 logger.warning(" The <useCache> section could not be set from the provided \n" +
            		 		"value of '"+useCache+"'.  Default value of TRUE will be used."+
                             "\nPlease refer to the Batch Processing documentation for more information.");
            	}
            }

            // ---------------------------------------------------------------------------------------------
            // Get grid options
            // ---------------------------------------------------------------------------------------------
            String gridOptions = WCTUtils.getXPathValue(doc, xpath, "//gridOptions/text()");
            if (gridOptions == null) {
                logger.warning(" The gridOptions section is missing.  Default values will be used."+
                        "\nPlease refer to the Batch Processing documentation for more information.");
            }
            else {
                
//                exporter.setGridExportGridIndex(gridProps.getSelectedGridIndex());
//                exporter.setGridExportZIndex(gridProps.getSelectedGridIndex());
//                exporter.setGridExportRuntimeIndex(gridProps.getSelectedGridIndex());
//                exporter.setGridExportTimeIndex(gridProps.getSelectedTimeIndex());


                // Set variable value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//gridOptions/variable/@name").trim();
                    exporter.setGridExportGridVariableName(str);
                } catch (Exception e) {
                    logger.warning("COULD NOT SET GRID variable VALUE");
                }

                // Set time index
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//gridOptions/time/@lookup").trim();
//                    System.out.println(str + "----------------------------");
                    exporter.setGridExportTimeIndex(Integer.parseInt(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET //gridOptions/time, lookup attribute VALUE");
                }

                // Set runtime index
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//gridOptions/runtime/@lookup").trim();
//                    System.out.println(str + "----------------------------");
                    exporter.setGridExportRuntimeIndex(Integer.parseInt(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET //gridOptions/runtime, lookup attribute VALUE");
                }

                // Set height index
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//gridOptions/height/@lookup").trim();
//                    System.out.println(str + "----------------------------");
                    exporter.setGridExportZIndex(Integer.parseInt(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET //gridOptions/height, lookup attribute VALUE");
                }
                
            }
            
            
            

            // ---------------------------------------------------------------------------------------------
            // Get Radialoptions
            // ---------------------------------------------------------------------------------------------
            String radialOptions = WCTUtils.getXPathValue(doc, xpath, "//radialOptions/text()");
            if (radialOptions == null) {
                logger.warning(" The radialOptions section is missing.  Default values will be used."+
                        "\nPlease refer to the Batch Processing documentation for more information.");
            }
            else {

                // Set moment value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialOptions/variable/text()").trim();
                    exporter.setExportVariable(str);
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL variable VALUE");
                }

                // Set cut value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialOptions/sweep/text()").trim();
                    exporter.setExportCut(Integer.parseInt(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL sweep VALUE");
                }

                // Set UseRF value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialOptions/useRFvalues/text()").trim();
                    exporter.setExportUseRF(Boolean.valueOf(str).booleanValue());
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL useRFvalues VALUE");
                }

                // Set classify value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialOptions/classify/text()").trim();
                    exporter.setExportClassify(Boolean.valueOf(str).booleanValue());
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL classify VALUE");
                }

                // Set exportPoints value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialOptions/exportPoints/text()").trim();
                    exporter.setExportPoints(Boolean.valueOf(str).booleanValue());
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL exportPoints VALUE");
                }

                // Set exportAllPoints value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialOptions/exportAllPoints/text()").trim();
                    exporter.setExportAllPoints(Boolean.valueOf(str).booleanValue());
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL exportAllPoints VALUE");
                }
            }

            // ---------------------------------------------------------------------------------------------
            // Get Level-3 options
            // ---------------------------------------------------------------------------------------------

            String level3Options = WCTUtils.getXPathValue(doc, xpath, "//level3Options/text()");
            if (level3Options == null) {
                logger.warning(" The level3Options section is missing.  Default values will be used."+
                        "\nPlease refer to the Batch Processing documentation for more information.");
            }
            else {
                // Set moment value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//level3Options/reducePolygons/text()").trim();
                    exporter.setExportReducePolys(Boolean.valueOf(str).booleanValue());
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL reducePolys VALUE");
                }

            }

            // ---------------------------------------------------------------------------------------------
            // Get Level-2 Filter
            // ---------------------------------------------------------------------------------------------
            String radialFilter = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/text()");
            if (radialFilter != null) {

                WCTFilter nxfilter = new WCTFilter();
                // Set minDistance
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/minRange/text()").trim();
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMinDistance(WCTFilter.NO_MIN_DISTANCE);
                    }
                    else {
                        nxfilter.setMinDistance(Double.parseDouble(str));
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER minRange VALUE");
                }
                // Set maxDistance
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/maxRange/text()").trim();
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMaxDistance(WCTFilter.NO_MAX_DISTANCE);
                    }
                    else {
                        nxfilter.setMaxDistance(Double.parseDouble(str));
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER maxRange VALUE");
                }
                // Set minAzimuth
                try {
                    double minAzimuth;
                    double maxAzimuth;
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/minAzimuth/text()").trim();
                    if (str.toUpperCase().equals("NONE")) {
                        minAzimuth = WCTFilter.NO_MIN_AZIMUTH;
                    }
                    else {
                        minAzimuth = Double.parseDouble(str);
                    }
                    str = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/maxAzimuth/text()").trim();
                    if (str.toUpperCase().equals("NONE")) {
                        maxAzimuth = WCTFilter.NO_MAX_AZIMUTH;
                    }
                    else {
                        maxAzimuth = Double.parseDouble(str);
                    }

                    nxfilter.setAzimuthRange(minAzimuth, maxAzimuth);
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER minAzimuth VALUE");
                }
                // Set minValue
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/minValue/text()").trim();
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMinValue(WCTFilter.NO_MIN_VALUE);
                    }
                    else {
                        String[] valArray = str.split(",");
                        double[] values = new double[valArray.length];
                        for (int n=0; n<valArray.length; n++) {
                            values[n] = Double.parseDouble(valArray[n]);
                        }
                        nxfilter.setMinValue(values);
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER minValue VALUE");
                }
                // Set maxValue
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/maxValue/text()").trim();
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMaxValue(WCTFilter.NO_MAX_VALUE);
                    }
                    else {
                        String[] valArray = str.split(",");
                        double[] values = new double[valArray.length];
                        for (int n=0; n<valArray.length; n++) {
                            values[n] = Double.parseDouble(valArray[n]);
                        }
                        nxfilter.setMaxValue(values);
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER maxValue VALUE");
                }
                // Set geographic bounds
                try {
                    String sMinLat = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/minLat/text()").trim();
                    String sMaxLat = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/maxLat/text()").trim();
                    String sMinLon = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/minLon/text()").trim();
                    String sMaxLon = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/maxLon/text()").trim();
  
                    if (!(sMinLat.trim().toUpperCase().equals("NONE") ||
                            sMaxLat.trim().toUpperCase().equals("NONE") ||
                            sMinLon.trim().toUpperCase().equals("NONE") ||
                            sMaxLon.trim().toUpperCase().equals("NONE"))) {

                        double minLat = Double.parseDouble(sMinLat);
                        double maxLat = Double.parseDouble(sMaxLat);
                        double minLon = Math.min(Double.parseDouble(sMinLon), Double.parseDouble(sMaxLon));
                        double maxLon = Math.max(Double.parseDouble(sMaxLon), Double.parseDouble(sMinLon));

                        nxfilter.setExtentFilter(new java.awt.geom.Rectangle2D.Double(minLon, minLat, maxLon - minLon, maxLat - minLat));
                    }

                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER geographic bounds");
                }
                // Set minValue
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/minHeight/text()").trim();
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMinHeight(WCTFilter.NO_MIN_HEIGHT);
                    }
                    else {
                        nxfilter.setMinHeight(Double.parseDouble(str));
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER minHeight VALUE");
                }
                // Set maxValue
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//radialFilter/maxHeight/text()").trim();                   
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMaxHeight(WCTFilter.NO_MAX_HEIGHT);
                    }
                    else {
                        nxfilter.setMaxHeight(Double.parseDouble(str));
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER maxHeight VALUE");
                }

                exporter.setExportRadialFilter(nxfilter);   
            }
            else {
                logger.warning("COULD NOT SET radialFilter");
                exporter.setExportRadialFilter(new WCTFilter());
            }


            // ---------------------------------------------------------------------------------------------
            // Get Level-3 Filter
            // ---------------------------------------------------------------------------------------------
            String level3Filter = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/text()"); 
            if (level3Filter != null) {

                WCTFilter nxfilter = new WCTFilter();

                // Set minDistance
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/minRange/text()").trim(); 
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMinDistance(WCTFilter.NO_MIN_DISTANCE);
                    }
                    else {
                        nxfilter.setMinDistance(Double.parseDouble(str));
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET LEVEL-III FILTER minRange VALUE");
                }
                // Set maxDistance
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/maxRange/text()").trim(); 
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMaxDistance(WCTFilter.NO_MAX_DISTANCE);
                    }
                    else {
                        nxfilter.setMaxDistance(Double.parseDouble(str));
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET LEVEL-III FILTER maxRange VALUE");
                }
                // Set value categories
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/minValue/text()").trim(); 
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMinValue(WCTFilter.NO_MIN_VALUE);
                    }
                    else {
                        String[] valArray = str.split(",");
                        double[] values = new double[valArray.length];
                        for (int n=0; n<valArray.length; n++) {
                            values[n] = Double.parseDouble(valArray[n]);
                        }
                        nxfilter.setMinValue(values);
                    }                    
                } catch (Exception e) {
                    logger.warning("COULD NOT SET LEVEL-III FILTER minValue VALUE");
                }
                // Set max values
                try {
                    
                    String str = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/maxValue/text()").trim(); 
                    if (str.toUpperCase().equals("NONE")) {
                        nxfilter.setMaxValue(WCTFilter.NO_MAX_VALUE);
                    }
                    else {
                        String[] valArray = str.split(",");
                        double[] values = new double[valArray.length];
                        for (int n=0; n<valArray.length; n++) {
                            values[n] = Double.parseDouble(valArray[n]);
                        }
                        nxfilter.setMaxValue(values);
                    }
                    
                } catch (Exception e) {
                    logger.warning("COULD NOT SET LEVEL-III FILTER maxValue VALUE");
                }
                
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/categoryOverrides/text()").trim(); 
                    if (! str.toUpperCase().equals("NONE")) {
                        String[] categoryOverrides = str.split(",");
                        nxfilter.setCategoryOverrides(categoryOverrides);
                    }
                    
                } catch (Exception e) {
                    logger.warning("COULD NOT SET LEVEL-III FILTER categoryOverrides VALUE");
                }
                
                // Set geographic bounds
                try {
                    String sMinLat = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/minLat/text()").trim();
                    String sMaxLat = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/maxLat/text()").trim();
                    String sMinLon = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/minLon/text()").trim();
                    String sMaxLon = WCTUtils.getXPathValue(doc, xpath, "//level3Filter/maxLon/text()").trim();

                    if (!(sMinLat.trim().toUpperCase().equals("NONE") ||
                            sMaxLat.trim().toUpperCase().equals("NONE") ||
                            sMinLon.trim().toUpperCase().equals("NONE") ||
                            sMaxLon.trim().toUpperCase().equals("NONE"))) {

                        double minLat = Double.parseDouble(sMinLat);
                        double maxLat = Double.parseDouble(sMaxLat);
                        double minLon = Math.min(Double.parseDouble(sMinLon), Double.parseDouble(sMaxLon));
                        double maxLon = Math.max(Double.parseDouble(sMaxLon), Double.parseDouble(sMinLon));

                        nxfilter.setExtentFilter(new java.awt.geom.Rectangle2D.Double(minLon, minLat, maxLon - minLon, maxLat - minLat));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warning("COULD NOT SET LEVEL-III FILTER geographic bounds");
                }

                exporter.setExportL3Filter(nxfilter);
            }
            else {
                logger.warning("COULD NOT SET level3Filter");
                exporter.setExportL3Filter(new WCTFilter());
            }


            
            
            
            
            
            
            
            
            
            
            
            // ---------------------------------------------------------------------------------------------
            // Get Gridded Filter
            // ---------------------------------------------------------------------------------------------
            String gridFilter = WCTUtils.getXPathValue(doc, xpath, "//gridFilter/text()");
            if (gridFilter != null) {

                WCTFilter filter = new WCTFilter();
                // Set geographic bounds
                try {
                    String sMinLat = WCTUtils.getXPathValue(doc, xpath, "//gridFilter/minLat/text()").trim();
                    String sMaxLat = WCTUtils.getXPathValue(doc, xpath, "//gridFilter/maxLat/text()").trim();
                    String sMinLon = WCTUtils.getXPathValue(doc, xpath, "//gridFilter/minLon/text()").trim();
                    String sMaxLon = WCTUtils.getXPathValue(doc, xpath, "//gridFilter/maxLon/text()").trim();
  
                    if (!(sMinLat.trim().toUpperCase().equals("NONE") ||
                            sMaxLat.trim().toUpperCase().equals("NONE") ||
                            sMinLon.trim().toUpperCase().equals("NONE") ||
                            sMaxLon.trim().toUpperCase().equals("NONE"))) {

                        double minLat = Double.parseDouble(sMinLat);
                        double maxLat = Double.parseDouble(sMaxLat);
                        double minLon = Math.min(Double.parseDouble(sMinLon), Double.parseDouble(sMaxLon));
                        double maxLon = Math.max(Double.parseDouble(sMaxLon), Double.parseDouble(sMinLon));

                        filter.setExtentFilter(new java.awt.geom.Rectangle2D.Double(minLon, minLat, maxLon - minLon, maxLat - minLat));
                    }

                } catch (Exception e) {
                    logger.warning("COULD NOT SET RADIAL FILTER geographic bounds");
                }
                
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//gridFilter/minValue/text()").trim(); 
                    if (str.toUpperCase().equals("NONE")) {
                        filter.setMinValue(WCTFilter.NO_MIN_VALUE);
                    }
                    else {
                        filter.setMinValue(Double.parseDouble(str));
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET GRID FILTER minValue VALUE");
                }
                
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//gridFilter/maxValue/text()").trim(); 
                    if (str.toUpperCase().equals("NONE")) {
                        filter.setMaxValue(WCTFilter.NO_MAX_VALUE);
                    }
                    else {
                        filter.setMaxValue(Double.parseDouble(str));
                    }
                } catch (Exception e) {
                    logger.warning("COULD NOT SET GRID FILTER maxValue VALUE");
                }
                
                exporter.setExportGridSatelliteFilter(filter);   
            }
            else {
                logger.warning("COULD NOT SET gridFilter");
                exporter.setExportGridSatelliteFilter(new WCTFilter());
            }

            

            
            

            // ---------------------------------------------------------------------------------------------
            // Set up exportRasterOptions
            // ---------------------------------------------------------------------------------------------

            String exportRasterOptions = WCTUtils.getXPathValue(doc, xpath, "//exportGridOptions/text()");
            if (exportRasterOptions != null) {

                // Set squareGridSize value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportGridOptions/squareGridSize/text()").trim();
                    exporter.setExportGridSize(Integer.parseInt(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET squareGridSize VALUE");
                }
                // Set gridCellResolution value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportGridOptions/gridCellResolution/text()").trim();
                    exporter.setExportGridCellSize(Double.parseDouble(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET gridCellResolution VALUE");
                }
                // Set noData value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportGridOptions/noDataValue/text()").trim();
                    exporter.setExportGridNoData(Float.parseFloat(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET noDataValue VALUE");
                }
                // Set smoothFactor value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportGridOptions/smoothFactor/text()").trim();
                    exporter.setExportGridSmoothFactor(Integer.valueOf(str).intValue());
                } catch (Exception e) {
                    logger.warning("COULD NOT SET smoothFactor VALUE");
                }

            }
            else {
                logger.warning("COULD NOT SET exportGridOptions - USING DEFAULTS");
            }



            
            
            
            
            
            
            
            
            
            

            // ---------------------------------------------------------------------------------------------
            // Set up exportKmlOptions
            // ---------------------------------------------------------------------------------------------

            String exportKmlOptions = WCTUtils.getXPathValue(doc, xpath, "//exportKmlOptions/text()");
            if (exportKmlOptions != null) {

            	ExportKmlOptions kmlOptions = new ExportKmlOptions();
            	
                // Set altMode value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportKmlOptions/altMode/text()").trim();
                    kmlOptions.setAltMode(AltitudeMode.valueOf(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET exportKmlOptions/altMode VALUE");
                    System.err.println(e);
                }
                // Set elevationExaggeration value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportKmlOptions/elevationExaggeration/text()").trim();
                    kmlOptions.setElevationExaggeration(Integer.parseInt(str));
                } catch (Exception e) {
                    logger.warning("COULD NOT SET exportKmlOptions/elevationExaggeration VALUE");
                }
                // Set palette value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportKmlOptions/palette/text()").trim();
                    URL url = null;
                    if (str.trim().length() == 0 || str.trim().equalsIgnoreCase("NONE")) {
                    	url = null;
                    }
                    else if (str.startsWith("wct://")) {
                    	String paletteName = str.substring(6);
                		url = ResourceUtils.getInstance().getJarResource(
                                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                                "/config/colormaps/"+paletteName, null);                    	
                    }
                    else if (str.startsWith("http://") || str.startsWith("ftp://")) {
                    	url = new URL(str);
                    }
                    else {
                    	url = new File(str).getCanonicalFile().toURI().toURL();
                    }
                    
                    kmlOptions.setPaletteURL(url);
                } catch (Exception e) {
                	System.out.println(e);
                    logger.warning("COULD NOT SET exportKmlOptions/palette VALUE");
                }
//                // Set minMaxOverride value
//                try {
//                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportKmlOptions/minMaxOverride/text()").trim();
//                    if (str.trim().length() > 0) {
//                    	double[] minMaxOverride = new double[] {
//                    		Double.parseDouble(str.split("[\\s]+")[0]),
//                    		Double.parseDouble(str.split("[\\s]+")[1])
//                    	};
//                    	kmlOptions.setMinMaxOverride(minMaxOverride);
//                    }
//                } catch (Exception e) {
//                    logger.warning("COULD NOT SET exportKmlOptions/minMaxOverride VALUE");
//                }

                // Set logoURL value
                try {
                    String str = WCTUtils.getXPathValue(doc, xpath, "//exportKmlOptions/logo/@url").trim();
                    
                    URL url = null;
                    if (str.trim().length() == 0) {
                    	url = null;
                    }
                    else if (str.equalsIgnoreCase("NOAA")) {
                		url = WCTExportBatch.class.getResource("/images/noaa_logo_50x50.png");                    	
                    }
                    else if (str.startsWith("wct://")) {
                    	String paletteName = str.substring(6);
                		url = ResourceUtils.getInstance().getJarResource(
                                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                                "/images/"+paletteName, null);                    	
                    }
                    else if (str.startsWith("http://") || str.startsWith("ftp://")) {
                    	url = new URL(str);
                    }
                    else {
                    	url = new File(str).toURI().toURL();
                    }                    

                    kmlOptions.setLogoURL(url);
                    
                } catch (Exception e) {
                    logger.warning("COULD NOT SET exportKmlOptions/logoURL VALUE");
                }
                
                
                // Set legend location value
                try {                	
                	InputSource input = new InputSource(configURL.openStream());
                	String str = XPathFactory.newInstance().newXPath().evaluate("//exportKmlOptions/legend/location", input);
//                	System.out.println(str);
                	kmlOptions.setLegendLocationKML(str);
                } catch (Exception e) {
                	e.printStackTrace();
                    logger.warning("COULD NOT SET exportKmlOptions/legendLocation VALUE -- using default value...");
                }
                
                // Set logo location value
                try {                	
                	InputSource input = new InputSource(configURL.openStream());
                	String str = XPathFactory.newInstance().newXPath().evaluate("//exportKmlOptions/logo/location", input);
//                	System.out.println(str);
                	kmlOptions.setLogoLocationKML(str);
                } catch (Exception e) {
                	e.printStackTrace();
                    logger.warning("COULD NOT SET exportKmlOptions/logoLocation VALUE -- using default value...");
                }
                
                // Set legend transparent background pattern value
                try {                	
                	InputSource input = new InputSource(configURL.openStream());
                	String str = XPathFactory.newInstance().newXPath().evaluate("//exportKmlOptions/legend/@drawColorMapTransparentBackgroundPattern", input);
//                	System.out.println(str);
                	kmlOptions.setDrawColorMapTransparentBackgroundPattern(Boolean.parseBoolean(str));
                } catch (Exception e) {
                	e.printStackTrace();
                    logger.warning("COULD NOT SET exportKmlOptions/legend/@drawColorMapTransparentBackgroundPattern VALUE -- using default value...");
                }
                
                
                
                
                exporter.setExportKmlOptions(kmlOptions);

            }
            else {
                logger.warning("COULD NOT SET exportKmlOptions - USING DEFAULTS");
            }








            // ---------------------------------------------------------------------------------------------
            // CAPPI options
            // ---------------------------------------------------------------------------------------------
//            String cappiOptions = WCTUtils.getXPathValue(doc, xpath, "//cappiOptions/text()");
//            if (cappiOptions != null) {
//                // Get status attribute
//                String status = WCTUtils.getXPathValue(doc, xpath, "//cappiOptions/@status/text()").trim();
//                logger.info("cappiOptions STATUS: "+status);
//                if (status.trim().equalsIgnoreCase("on")) {
//                    ExtractCAPPI cappi = processCAPPIOptions(doc, xpath);
//                    cappi.setNexradFilter(exporter.getExportL2Filter());
////                    exporter.setExtractCAPPI(cappi);
//                }
//                else {
//                    exporter.setExtractCAPPI(null);
//                }
//
//            }








        } catch (SAXParseException spe) {

            // Error generated by the parser
            logger.severe("\n** Parsing error"
                    + ", line " + spe.getLineNumber()
                    + ", uri " + spe.getSystemId());
            logger.severe("   " + spe.getMessage());

            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null) {
                x = spe.getException();
            }
            x.printStackTrace();

        } catch (SAXException sxe) {
            // Error generated during parsing
            Exception x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            x.printStackTrace();

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();

        } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
        }

    }





    public static ExtractCAPPI processCAPPIOptions(Document doc, XPath xpath) 
    throws SAXParseException, SAXException, ParserConfigurationException {



        ExtractCAPPI cappi = new ExtractCAPPI();


        // Set numHeightIntervals value
        try {
            String str = WCTUtils.getXPathValue(doc, xpath, "//cappiOptions/numHeightInterval/text()").trim();
            logger.info("numHeightInterval: "+str);
            cappi.setNumHeightIntervals(Integer.parseInt(str));

            //exporter.setExportMoment(Integer.parseInt(str));
        } catch (Exception e) {
            logger.warning("COULD NOT SET CAPPI numHeightIntervals VALUE");
        }

        // Set startHeight value
        try {
            String str = WCTUtils.getXPathValue(doc, xpath, "//cappiOptions/startHeight/text()").trim();
            logger.info("startHeight: "+str);
            cappi.setStartHeight(Double.parseDouble(str));

            //exporter.setExportMoment(Integer.parseInt(str));
        } catch (Exception e) {
            logger.warning("COULD NOT SET CAPPI startHeight VALUE");
        }

        // Set endHeight value
        try {
            String str = WCTUtils.getXPathValue(doc, xpath, "//cappiOptions/endHeight/text()").trim();
            logger.info("endHeight: "+str);
            cappi.setEndHeight(Double.parseDouble(str));

            //exporter.setExportMoment(Integer.parseInt(str));
        } catch (Exception e) {
            logger.warning("COULD NOT SET CAPPI endHeight VALUE");
        }

        // Set overlap value
        try {
            String str = WCTUtils.getXPathValue(doc, xpath, "//cappiOptions/overlap/text()").trim();
            logger.info("overlap: "+str);
            cappi.setOverlap(Double.parseDouble(str));

            //exporter.setExportMoment(Integer.parseInt(str));
        } catch (Exception e) {
            logger.warning("COULD NOT SET CAPPI overlap VALUE");
        }


        return cappi;


    }   

















    /**
     *  Description of the Method
     */
    public static void checkVersion() {
        BufferedReader in = null;
        try {
        	System.out.println("Weather and Climate Toolit: Current Version = "+WCTUiUtils.getVersion());
        	
            URL url = new URL("http://www.ncdc.noaa.gov/wct/app/version-stable.dat");
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String currentVersion;
            if ((currentVersion = in.readLine()) == null) {
                logger.warning("WEATHER AND CLIMATE TOOLKIT - DATA EXPORTER: ERROR READING REMOTE VERSION FILE");
            }
            else {
                // Check version
                // Split major.minor version from bugfix version
                String[] onlineVersionArray = currentVersion.split("\\."); // regex
                String onlineMajorMinorVersion = onlineVersionArray[0]+"."+onlineVersionArray[1];
//                String bugfixVersion = versionArray[2];

                String[] wctVersionArray = WCTUiUtils.getVersion().split("\\."); // regex
                String wctMajorMinorVersion = wctVersionArray[0]+"."+wctVersionArray[1];

//                System.out.println("checking versions: "+onlineMajorMinorVersion+" to "+wctMajorMinorVersion);
                
                if (Double.parseDouble(onlineMajorMinorVersion) > Double.parseDouble(wctMajorMinorVersion)) {
                    logger.warning("WEATHER AND CLIMATE TOOLKIT - DATA EXPORTER: WARNING - " +
                            "THIS VERSION ("+WCTUiUtils.getVersion().trim()+
                            ") IS NOT CURRENT - PLEASE UPDATE TO VERSION " + currentVersion);
                }
//                if (!bugfixVersion.trim().equals(NexradInterface.JNX_VERSION_BUGFIX.trim())) {
//                    logger.warning("WEATHER AND CLIMATE TOOLKIT - DATA EXPORTER: NOTICE - " +
//                            "THERE IS A NEWER 'BUGFIX' RELEASE AVAILABLE - VERSION " + currentVersion);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("WEATHER AND CLIMATE TOOLKIT - DATA EXPORTER: CONNECTION ERROR WHILE CHECKING CURRENT VERSION");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {}
            }         
        }
    }


    /**
     *  Description of the Method
     *
     * @param  outputFormat  Description of the Parameter
     * @return               Description of the Return Value
     */
    public static ExportFormat readOutputFormat(String outputFormat) {
        if (outputFormat.trim().toUpperCase().equals("RNC")) {
            return ExportFormat.RAW_NETCDF;
        }
        else if (outputFormat.trim().toUpperCase().equals("SHP")) {
            return ExportFormat.SHAPEFILE;
        }
        else if (outputFormat.trim().toUpperCase().equals("WKT")) {
            return ExportFormat.WKT;
        }
        else if (outputFormat.trim().toUpperCase().equals("CSV")) {
            return ExportFormat.CSV;
        }
        else if (outputFormat.trim().toUpperCase().equals("GML")) {
            return ExportFormat.GML;
        }
        else if (outputFormat.trim().toUpperCase().equals("ASC")) {
            return ExportFormat.ARCINFOASCII;
        }
        else if (outputFormat.trim().toUpperCase().equals("FLT")) {
            return ExportFormat.ARCINFOBINARY;
        }
        else if (outputFormat.trim().toUpperCase().equals("NC")) {
            return ExportFormat.GRIDDED_NETCDF;
        }
        else if (outputFormat.trim().toUpperCase().equals("TIF")) {
            return ExportFormat.GEOTIFF_GRAYSCALE_8BIT;
        }
        else if (outputFormat.trim().toUpperCase().equals("TIF32")) {
            return ExportFormat.GEOTIFF_32BIT;
        }
        else if (outputFormat.trim().toUpperCase().equals("VTK")) {
            return ExportFormat.VTK;
        }
        else if (outputFormat.trim().toUpperCase().equals("KMZ")) {
        	return ExportFormat.KMZ;
        }
        else {
            logger.warning("WEATHER AND CLIMATE TOOLKIT - DATA EXPORTER: ERROR - COULD NOT DETERMINE OUTPUT FORMAT");
            printOutputFormats();
            System.exit(1);
            return null;
        }
    }


    /**
     *  Description of the Method
     */
    public static void printUsage() {
        System.err.println("===================================================");
        System.err.println("WEATHER AND CLIMATE TOOLKIT - DATA EXPORTER: USAGE:");
        System.err.println(" ");
        System.err.println(" ");
        System.err.println(" infile/url - means a file or URL");
        System.err.println(" ");
        System.err.println(" SINGLE BATCH PROCESS: Arg1 = infile/url, Arg2 = outfile, Arg3 = output format, Arg4 = config xml file/url");
        System.err.println(" MULIPLE BATCH PROCESS 1: Arg1 = indir, Arg2 = outdir, Arg3 = output format, Arg4 = config xml file/url");
        System.err.println(" MULIPLE BATCH PROCESS 2: Arg1 = listfile, Arg2 = output format");
        System.err.println("   Listfile format = infile/url,outdir,config-xml-file/url  OR indir,outdir,config-xml-file/url");
        System.err.println("===================================================");
    }


    /**
     *  Description of the Method
     */
    public static void printOutputFormats() {
        System.out.println("WEATHER AND CLIMATE TOOLKIT - DATA EXPORTER: OUTPUT FORMAT CODES");
        System.out.println("  rnc - Raw NetCDF");
        System.out.println("  shp - ESRI Shapefile (Vector)");
        System.out.println("  wkt - OpenGIS Well-Known-Text (Vector)");
        System.out.println("  csv - Comma Separated (Delimited) Text File (Alphanumeric Products Only)");
        System.out.println("  gml - Geographic Markup Language (Vector)");
        System.out.println("  asc - Arc/Info ASCII Grid");
        System.out.println("  flt - Arc/Info Binary Grid");
//        System.out.println("  grd - GrADS Binary Grid"); -- removed
        System.out.println("  nc  - Gridded NetCDF");
        System.out.println("  tif - GeoTIFF");
        System.out.println("  kmz - (BETA) KMZ file for Google Earth or other Virtual Globes");
    }




    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------









    /**
     *  Description of the Method
     *
     * @param  args  Description of the Parameter
     */
    public static void main(String args[]) {
//        System.out.println(Arrays.deepToString(args));
        if (args.length >= 1 && args[0].trim().equalsIgnoreCase("-viewer")) {
            WCTViewerSplash.main(args);
        }
        else if (args.length != 0) {
            
            Logger.getLogger("gov.noaa.ncdc").setLevel(Level.WARNING);
            

            // Start in batch mode
            WCTExportBatch batchExport = new WCTExportBatch();
            batchExport.runBatchMode(args);
        }
        else {
            printUsage();
        }
    }




    
    
    
    
    class BatchExportListener implements DataExportListener {

        private int lastProgress = 0;
        
        public void exportEnded(DataExportEvent event) {
            System.out.println("100%");            
        }

        public void exportProgress(DataExportEvent event) {
            int progress = event.getProgress();
            if (progress != lastProgress) {
                if (progress % 10 == 0 && progress > 0 && progress < 100) {
                    System.out.print(progress+"%");
                }
                else if (progress % 2 == 0 && progress > 0) {
                    System.out.print(".");
                }
                lastProgress = progress;
            }
        }

        public void exportStarted(DataExportEvent event) {
//            System.out.println(event.getDataURL()+" ---> "+event.getOutputFile());
            System.out.print("0%");
        }

        public void exportStatus(DataExportEvent event) {
        }
        
    }


}

