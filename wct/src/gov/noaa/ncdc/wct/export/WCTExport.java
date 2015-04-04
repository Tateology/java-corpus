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

import gov.noaa.ncdc.nexradexport.alpha.NexradAlphaExport;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.WCTDataUtils;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetDecoder;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetNativeRaster;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetUtils;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster.CAPPIType;
import gov.noaa.ncdc.wct.decoders.goes.GoesDecoder;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeHail;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeMDA;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeMeso;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeRSL;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormStructure;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormTracking;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeTVS;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeVADText;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGData;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.event.DataExportEvent;
import gov.noaa.ncdc.wct.event.DataExportListener;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport.GeoTiffType;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;
import gov.noaa.ncdc.wct.export.vector.StreamingCsvExport;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;
import gov.noaa.ncdc.wct.export.vector.StreamingWKTExport;
import gov.noaa.ncdc.wct.export.vector.WCTVectorExport;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.io.WCTTransfer;
import gov.noaa.ncdc.wct.ui.AbstractKmzUtilities.AltitudeMode;
import gov.noaa.ncdc.wct.ui.animation.ExportKMZThread;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.gc.GridCoverage;
import org.xml.sax.SAXException;

import ucar.nc2.FileWriter2.FileWriterProgressEvent;
import ucar.nc2.FileWriter2.FileWriterProgressListener;
import ucar.nc2.NetcdfFile;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.nc2.dt.grid.NetcdfCFWriter;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *  Description of the Class
 *
 * @author    steve.ansari
 */
public class WCTExport
implements DataDecodeListener, GeneralProgressListener {

    private static final Logger logger = Logger.getLogger(WCTExport.class.getName());

    public static enum ExportFormatType { UNKNOWN, NATIVE, VECTOR, RASTER }; 
    public static enum ExportFormat {
        NATIVE, RAW_NETCDF,
        SHAPEFILE, GML, WKT, CSV,  
        ARCINFOASCII, ARCINFOBINARY, GEOTIFF_GRAYSCALE_8BIT, GEOTIFF_32BIT, 
        GRIDDED_NETCDF, VTK, WCT_RASTER_OBJECT_ONLY,
        KMZ
    }

    private FileScanner scannedFile;
    private NexradHeader header;
    private StreamingRadialDecoder decoder;
    private RadialDatasetSweep radialDataset;
    private DecodeRadialDatasetSweepHeader radialDatasetHeader;
    private DecodeRadialDatasetSweep radialDatasetDecoder;
    private GoesRemappedRaster goes;
    private GridDatasetRemappedRaster grid;
    private RadialDatasetSweepRemappedRaster radialDatasetRaster;
    
    
    private DecodeL3Header level3Header;
    private DecodeXMRGHeader xmrgHeader;
    private DecodeL3Nexrad level3Decoder;
    private DecodeXMRGData xmrgDecoder;
    private DecodeL3Alpha alphaDecoder;
    private WCTFilter wctFilter;


    private WCTRasterizer rasterizer;
    private WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();


    private WCTVectorExport vectorExport;
    private WCTRasterExport rasterExport;


    // RADIAL properties
    private boolean radialReducePolys = false;
    private boolean radialExportClassify = false;
    private String radialExportVariable = null;
    private int radialExportCut = 0;
    private boolean radialExportUseRF = false;
    private double radialExportCappiHeightInMeters = Double.NaN;
    private CAPPIType radialExportCappiInterpolationType = CAPPIType.NEAREST_SWEEP;
    
    // GRID properties
    private int gridExportGridIndex = -1;
    private String gridExportGridVariableName = null;
    private int gridExportZIndex = -1;
    private int gridExportTimeIndex = -1;
    private int gridExportRuntimeIndex = -1;
    
    
    private WCTFilter exportRadialFilter = new WCTFilter();
    private WCTFilter exportL3Filter = new WCTFilter();
    private WCTFilter exportGridSatelliteFilter = new WCTFilter();
    

    private boolean autoRenameOutput = true;
    private boolean useWctCache = true;
    private boolean exportPoints = false;
    private boolean exportAllPoints = false;
    private int exportGridSize = 1200;
    private float exportGridNoData = -999.0f;
    private int exportGridSmoothFactor = 0;
    private double exportGridCellSize = -1.0;
    private java.awt.geom.Rectangle2D.Double bounds = null;
    private DecimalFormat exportAsciiFormat = WCTUtils.DECFMT_pDpppp;
    
    private boolean forceResample = false;
    
    private ExportKmlOptions exportKmlOptions = null;

    private ExportFormatType outputType;
    private ExportFormat outputFormat;
    
    // default save directory
    private String saveDirectory = ".";

    // holder for last decoded raster
    private WCTRaster genericRaster = null;

    // The list of event listeners.
    private Vector<DataExportListener> listeners = new Vector<DataExportListener>();
    private ArrayList<GeneralProgressListener> generalProgressListeners = new ArrayList<GeneralProgressListener>();

    private DataExportEvent event = null;

    private SupportedDataType currentDataType = null;


    /**
     * Constructor for the WCTExport object
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public WCTExport() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException { 
        scannedFile = new FileScanner();
    }


    /**
     *  Description of the Method
     *
     * @param  dataURL                           Description of the Parameter
     * @param  file                                Description of the Parameter
     * @exception  WCTExportException           Description of the Exception
     * @exception  WCTExportNoDataException     Description of the Exception
     * @exception  DecodeException           Description of the Exception
     * @exception  FeatureRasterizerException       Description of the Exception
     * @exception  IllegalAttributeException       Description of the Exception
     * @exception  ConnectException                Description of the Exception
     * @exception  FileNotFoundException           Description of the Exception
     * @exception  IOException                     Description of the Exception
     * @exception  ucar.ma2.InvalidRangeException  Description of the Exception
     * @throws DecodeHintNotSupportedException 
     * @throws URISyntaxException 
     * @throws ParseException 
     */
    public void exportData(URL dataURL, File file)
        throws WCTExportException, WCTExportNoDataException, DecodeException,
        FeatureRasterizerException, IllegalAttributeException,
        ConnectException, FileNotFoundException, IOException,
        ucar.ma2.InvalidRangeException, DecodeHintNotSupportedException, URISyntaxException,
        ParseException, Exception {


        exportData(dataURL, file, null, true);

    }
    
    /**
     * 
     * @param dataURL - input data url
     * @param file - output file
     * @param dataTypeOverride - may be null if no override is specified
     * @param checkForOpendap - if true, a DODS OPENDAP request will be initiated for the URL
     * @throws WCTExportException
     * @throws WCTExportNoDataException
     * @throws DecodeException
     * @throws FeatureRasterizerException
     * @throws IllegalAttributeException
     * @throws ConnectException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ucar.ma2.InvalidRangeException
     * @throws DecodeHintNotSupportedException
     * @throws URISyntaxException
     * @throws ParseException
     * @throws Exception
     */
    public void exportData(URL dataURL, File file, SupportedDataType dataTypeOverride, boolean checkForOpendap)
        throws WCTExportException, WCTExportNoDataException, DecodeException,
        FeatureRasterizerException, IllegalAttributeException,
        ConnectException, FileNotFoundException, IOException,
        ucar.ma2.InvalidRangeException, DecodeHintNotSupportedException, URISyntaxException,
        ParseException, Exception {

    	

//        logger.fine("OUTPUT FORMAT: "+outputFormat+" INURL="+dataURL+" OUTFILE="+file);




        //------------------------------------------------------------------------------------------------------------------
        // 2) Download and decode data into GeoTools OGC Features
        //------------------------------------------------------------------------------------------------------------------
        URL remoteURL = dataURL;
        if (outputFormat != ExportFormat.NATIVE) {
        	dataURL = WCTDataUtils.scan(dataURL, scannedFile, useWctCache, checkForOpendap, dataTypeOverride, null, generalProgressListeners);
        }
        
        
        

  
        
        //================================================================================

        if (file == null) {
            throw new WCTExportException("No output file provided");
        }
        if (file.isDirectory()) {
        	if (outputFormat == ExportFormat.NATIVE) {
        		file = new File(file.toString() + File.separator + dataURL.toString().substring(dataURL.toString().lastIndexOf("/")+1));
        	}
        	else {
        		
        		String saveName = scannedFile.getSaveName();
        		if (saveName == null || saveName.trim().equalsIgnoreCase("null") || saveName.trim().length() == 0) {
        			saveName = scannedFile.getLastScanResult().getFileName();
        		}
        		if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL &&
                    outputType == ExportFormatType.VECTOR && exportPoints) {
                
        			file = new File(file.toString() + File.separator + saveName + 
                        "_pnt" + WCTExportUtils.getExportFileExtension(outputFormat));
        		}
        		else {
        			file = new File(file.toString() + File.separator + saveName + 
        					WCTExportUtils.getExportFileExtension(outputFormat));
        		}
        	}
        }

        event = new DataExportEvent(this, dataURL, file);

        // Start export
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).exportStarted(event);
        }

        
        

        // delete output file if it exists - automatic overwrite for simple native transfers
    	if (outputFormat == ExportFormat.NATIVE) {
    		if (file.exists()) {
    			file.delete();
    		}
    	}
        
        
        
        
        

        // If we are just converting to NetCDF data structure, stop here:
        if (outputFormat == ExportFormat.RAW_NETCDF) {

            String fileString = file.toString();
            
            // Start export
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setStatus("Saving: " + fileString);
                event.setProgress(0);
                listeners.get(i).exportStatus(event);
            }

            
            NetcdfFile ncIn = null;
            try {
                
                FileWriterProgressListener progressListener = new FileWriterProgressListener() {                    
                    @Override
                    public void writeProgress(FileWriterProgressEvent writeEvent) {
//                        System.out.println("data exporter "+writeEvent.getProgressPercent());
                        for (int i = 0; i < listeners.size(); i++) {
                            event.setProgress((int)Math.round(writeEvent.getProgressPercent()));                    
                            listeners.get(i).exportProgress(event);
                        }                        
                    }
                    @Override
                    public void writeStatus(FileWriterProgressEvent writeEvent) {
                        for (int i = 0; i < listeners.size(); i++) {
                            event.setStatus("Processing " + writeEvent.getStatus());
                            listeners.get(i).exportStatus(event);   
                        }                        
                    }
                };
                ArrayList<FileWriterProgressListener> writeListeners = new ArrayList<FileWriterProgressListener>();
                writeListeners.add(progressListener);
                
                String ext = WCTExportUtils.getExportFileExtension(outputFormat);
                if (! fileString.endsWith(ext)) {
                	fileString = fileString+ext;
                }
                
                ncIn = NetcdfFile.open(dataURL.toString());
                NetcdfFile ncOut = ucar.nc2.FileWriter2.writeToFile(ncIn, fileString, writeListeners);

                ncOut.close();
//                System.out.println("CLOSED RAW NC OUTPUT NETCDF FILE?  ");
                ncIn.close();
//                System.out.println("CLOSED INPUT FILE.  ");
                
                for (int i = 0; i < listeners.size(); i++) {
//                    event.setStatus("Saving: " + fileString);
                    event.setProgress(100);
                    listeners.get(i).exportEnded(event);
                }
                
                
                
                
                
//        		if (WCTTransfer.isInCache(remoteURL)) {
//        			System.out.println(remoteURL+" :::: IS IN CACHE, deleting file...");
//        		
//        			// delete cache file
//        			File cacheFile = WCTTransfer.getFileInCache(remoteURL);
//        			if (! cacheFile.delete()) {
//        				throw new Exception("Could not delete cache file!");
//        			}
//        			
//        		}

                
                

                
                
                
                
            } catch (OutOfMemoryError me) {
                ncIn.close();
                me.printStackTrace();
                throw new WCTExportException("Raw Netcdf output consumes too much memory for this file.");
            } catch (Exception e) {
//                System.out.println("1) CLOSED RAW NC OUTPUT NETCDF FILE?  "+ncIn.isClosed());
                if (ncIn != null) {
                    ncIn.close();
                }
//                System.out.println("2) NOW CLOSED RAW NC OUTPUT NETCDF FILE?  "+ncIn.isClosed());
                new File(fileString).delete();
                e.printStackTrace();
                throw new WCTExportException("Raw Netcdf output is not supported for this product.");
            } finally {
                try {
                   ncIn.close();
//                   System.out.println("CLOSED INPUT FILE.  ");
                } catch (Exception e) {

                }
                
                // End export
                // --------------
                for (int i = 0; i < listeners.size(); i++) {
                    event.setStatus("Complete: " + fileString);
                    event.setProgress(100);                    
                    listeners.get(i).exportStatus(event);
                }

            }

            return;
        }


        // If we are just copying the data, stop here:
        if (outputFormat == ExportFormat.NATIVE) {

            // Start export
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setStatus("Saving: " + event.getOutputFile().getName() + 
                        WCTExportUtils.getExportFileExtension(this));
                event.setProgress(10);                    
                listeners.get(i).exportStatus(event);
            }

            String fileString = file.toString();
            try {
                
                WCTTransfer.getURL(dataURL, file.getParentFile(), true, generalProgressListeners);
                
            } catch (Exception e) {
                new File(fileString).delete();
                e.printStackTrace();
                throw new WCTExportException("Error copying "+dataURL+" to "+file);
            } finally {
                // End export
                // --------------
                for (int i = 0; i < listeners.size(); i++) {
                    event.setStatus("Saving: " + event.getOutputFile().getName());
                    event.setProgress(100);                    
                    listeners.get(i).exportStatus(event);
                }
            }
            return;
        }


























    	if (dataTypeOverride != null) {
    		currentDataType = dataTypeOverride; 
    	}
    	else {
    		currentDataType = scannedFile.getLastScanResult().getDataType();
    	}
        
        
        if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
        	
        	
            //================================================================================
            // SATELLITE EXPORT
            //================================================================================
            
            bounds = GoesRemappedRaster.GOES_DEFAULT_EXTENT;
            if (exportGridCellSize > 0.0) {
                // calculate the raster size needed in decimal degrees
                // find number grid cells needed for 'long' side of grid
                double longSide = ( bounds.getWidth() > bounds.getHeight() ) ? bounds.getWidth() : bounds.getHeight();
                this.exportGridSize = (int) (longSide/exportGridCellSize);
                double boundsDiff = longSide - (exportGridSize*exportGridCellSize);
                bounds.setRect(bounds.getX()-boundsDiff/2.0, bounds.getY()+boundsDiff/2.0, bounds.getWidth()+boundsDiff, bounds.getHeight()+boundsDiff); 

                logger.fine("SETTING GRID CELL SIZE: "+ exportGridCellSize);
                logger.fine("SETTING GRID SIZE: "+ exportGridSize);
                logger.fine("BOUNDS DIFF: "+ boundsDiff);
            }

            
            
            if (outputType == ExportFormatType.VECTOR) {

                GoesDecoder goes = new GoesDecoder();
                goes.setSource(dataURL.toString());
                

                if (exportGridSatelliteFilter.getExtentFilter() != null) {
                    goes.setBounds(exportGridSatelliteFilter.getExtentFilter());
                }
                
                
                StreamingProcess streamingExportProcess = null;
                if (outputFormat == ExportFormat.SHAPEFILE) {
                    //logger.fine("Saving: " + file + ".shp , " + file + ".shx , " + file + ".dbf , " + file + ".prj");
                    streamingExportProcess = new StreamingShapefileExport(file);
                }
                else if (outputFormat == ExportFormat.WKT) {
                    streamingExportProcess = new StreamingWKTExport(file);
                }
                else if (outputFormat == ExportFormat.GML) {
//                  vectorExport.saveGML(file, decoder);
                    throw new WCTExportException("GML IS NOT SUPPORTED");
                }
                else if (outputFormat == ExportFormat.CSV) {
//                    throw new WCTExportException("CSV EXPORT IS NOT SUPPORTED FOR THIS FILE TYPE");
                    streamingExportProcess = new StreamingCsvExport(file);

                }
//                streamingExportProcess.addGeneralProgressListener(this);
                goes.addDataDecodeListener(this);
                goes.decodeData(new StreamingProcess[] { streamingExportProcess });
//                streamingExportProcess.removeGeneralProgressListener(this);                                
                goes.removeDataDecodeListener(this);
                
                
                
            }            
            else if (outputType == ExportFormatType.RASTER) {
            
                goes = new GoesRemappedRaster();
                goes.addDataDecodeListener(this);

                goes.setHeight(exportGridSize);
                goes.setWidth(exportGridSize);

                
                goes.setMinValueFilter(exportGridSatelliteFilter.getMinValue()[0]);
                goes.setMaxValueFilter(exportGridSatelliteFilter.getMaxValue()[0]);
                if (exportGridSatelliteFilter.getExtentFilter() == null) {
                    goes.process(dataURL.toString());
                }
                else {            
                    logger.fine(exportGridSatelliteFilter.toString());

                    goes.process(dataURL.toString(), exportGridSatelliteFilter.getExtentFilter());
                }

                genericRaster = goes;

                logger.fine("RASTER AFTER: "+goes.toString());


                goes.removeDataDecodeListener(this);

                //====================================================================
                //----------- EXPORT DATA! -------------------------------------------
                //====================================================================
                // Lazy object creation
                if (rasterExport == null) {
                    rasterExport = new WCTRasterExport();
                    rasterExport.addGeneralProgressListener(this);
                }

                if (outputFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT) {
                    rasterExport.saveGeoTIFF(file, genericRaster, GeoTiffType.TYPE_8_BIT);
                }
                else if (outputFormat == ExportFormat.GEOTIFF_32BIT) {
                    rasterExport.saveGeoTIFF(file, genericRaster, GeoTiffType.TYPE_32_BIT);
                }
                else if (outputFormat == ExportFormat.ARCINFOASCII) {
                    rasterExport.saveAsciiGrid(file, genericRaster, exportAsciiFormat);
                }
                else if (outputFormat == ExportFormat.ARCINFOBINARY) {
                    rasterExport.saveBinaryGrid(file, genericRaster);
                }
                else if (outputFormat == ExportFormat.GRIDDED_NETCDF) {
                    rasterExport.saveNetCDF(file, genericRaster);
                }
                else if (outputFormat == ExportFormat.WCT_RASTER_OBJECT_ONLY) {
                	; // do nothing, object already exists
                }
                else {
                    throw new WCTExportException("This output format is not supported for this datatype");
                }

            }
            else {
                throw new WCTExportException("Unsupported export format type (Vector or Raster ONLY)");
            }
            
        }
        
        
        
        
        
        
        
        else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {
            //================================================================================
            // GRID EXPORT
            //================================================================================
            
//            bounds = GoesRemappedRaster.GOES_DEFAULT_EXTENT;
            bounds = exportGridSatelliteFilter.getExtentFilter();
            if (exportGridCellSize > 0.0) {
                // calculate the raster size needed in decimal degrees
                // find number grid cells needed for 'long' side of grid
                double longSide = ( bounds.getWidth() > bounds.getHeight() ) ? bounds.getWidth() : bounds.getHeight();
                this.exportGridSize = (int) (longSide/exportGridCellSize);
                double boundsDiff = longSide - (exportGridSize*exportGridCellSize);
                bounds.setRect(bounds.getX()-boundsDiff/2.0, bounds.getY()+boundsDiff/2.0, bounds.getWidth()+boundsDiff, bounds.getHeight()+boundsDiff); 

                logger.fine("SETTING GRID CELL SIZE: "+ exportGridCellSize);
                logger.fine("SETTING GRID SIZE: "+ exportGridSize);
                logger.fine("BOUNDS DIFF: "+ boundsDiff);
            }
            


            
            String gridSuffix = "";
            
            if (autoRenameOutput) {
            	if (gridExportGridVariableName != null) {
            		gridSuffix += "-var_" + gridExportGridVariableName;
            	}
            	else {
            		gridSuffix += "-var" + gridExportGridIndex;
            	}
            	if (gridExportZIndex >= 0) {
            		gridSuffix += "-z" + gridExportZIndex;
            	}
            	if (gridExportRuntimeIndex >= 0) {
            		gridSuffix += "-rt" + gridExportRuntimeIndex;
            	}
            	if (gridExportTimeIndex >= 0) {
            		gridSuffix += "-t" + gridExportTimeIndex;
            	}
            }

                
//System.out.println(file);


            String s = file.toString();
            file = new File(s.substring(0, s.lastIndexOf(".")) +
                    gridSuffix + WCTExportUtils.getExportFileExtension(outputFormat));

            
            
            if (outputType == ExportFormatType.VECTOR) {

                GridDatasetDecoder gridDecoder = new GridDatasetDecoder();
                gridDecoder.setSource(dataURL.toString());
                
//                if (exportGridFilter.getExtentFilter() != null) {
                    gridDecoder.setDecodeHint("gridFilter", exportGridSatelliteFilter);
//                }
                gridDecoder.setGridIndex(gridExportGridIndex);
                if (gridExportGridVariableName != null) {
                    gridDecoder.setGridVariableName(gridExportGridVariableName);
                }
                gridDecoder.setZIndex(gridExportZIndex);
                gridDecoder.setRuntimeIndex(gridExportRuntimeIndex);
                gridDecoder.setTimeIndex(gridExportTimeIndex);
                
                
                
                
                System.out.println(exportGridSatelliteFilter.toString());
                
                
                StreamingProcess streamingExportProcess = null;
                if (outputFormat == ExportFormat.SHAPEFILE) {
                    //logger.fine("Saving: " + file + ".shp , " + file + ".shx , " + file + ".dbf , " + file + ".prj");
                    streamingExportProcess = new StreamingShapefileExport(file);
                }
                else if (outputFormat == ExportFormat.WKT) {
                    streamingExportProcess = new StreamingWKTExport(file);
                }
                else if (outputFormat == ExportFormat.GML) {
//                  vectorExport.saveGML(file, decoder);
                    throw new WCTExportException("GML IS NOT SUPPORTED");
                }
                else if (outputFormat == ExportFormat.CSV) {
//                    throw new WCTExportException("CSV EXPORT IS NOT SUPPORTED FOR THIS FILE TYPE");
                    streamingExportProcess = new StreamingCsvExport(file);
                }
                gridDecoder.addDataDecodeListener(this);
                gridDecoder.decodeData(new StreamingProcess[] { streamingExportProcess });
                gridDecoder.removeDataDecodeListener(this);
//                                
            }            
            else if (outputType == ExportFormatType.RASTER) {
            
                grid = new GridDatasetRemappedRaster();
                grid.addDataDecodeListener(this);

                grid.setGridIndex(gridExportGridIndex);
                if (gridExportGridVariableName != null) {
                    grid.setVariableName(gridExportGridVariableName);
                }
                grid.setZIndex(gridExportZIndex);
                grid.setRuntimeIndex(gridExportRuntimeIndex);
                grid.setTimeIndex(gridExportTimeIndex);

                grid.setHeight(exportGridSize);
                grid.setWidth(exportGridSize);

                if (exportGridSatelliteFilter.getExtentFilter() == null) {
                    if (grid.isNative() && grid.isRegularLatLon()) {
                        GridDatasetNativeRaster nativeGrid = new GridDatasetNativeRaster();
                        nativeGrid.setGridIndex(gridExportGridIndex);
                        if (gridExportGridVariableName != null) {
                            nativeGrid.setGridVariableName(gridExportGridVariableName);
                        }
                        nativeGrid.setZIndex(gridExportZIndex);
                        nativeGrid.setRuntimeIndex(gridExportRuntimeIndex);
                        nativeGrid.setTimeIndex(gridExportTimeIndex);
                        nativeGrid.setMinValueFilter(wctFilter.getMinValue()[0]);
                        nativeGrid.setMaxValueFilter(wctFilter.getMaxValue()[0]);
                        nativeGrid.process(dataURL.toString());
                        genericRaster = nativeGrid;
                    }
                    else {                   
                        //  no spatial subset
                        grid.setMinValueFilter(wctFilter.getMinValue());
                        grid.setMaxValueFilter(wctFilter.getMaxValue());
                        grid.process(dataURL.toString());
                        genericRaster = grid;
                    }
                }
                else {            
                    // yes - spatial subset!
                    logger.fine(exportGridSatelliteFilter.toString());
                    
                    
                    grid.scan(dataURL.toString());
                    if ((! forceResample) &&  grid.isNative() && grid.isRegularLatLon()) {
                        System.out.println("Native regular lat/lon grid found in WCTExport");
                        
                        if (outputFormat == ExportFormat.GRIDDED_NETCDF) {

                            LatLonRect subsetBbox = new LatLonRect(new LatLonPointImpl(bounds.getMinY(), bounds.getMinX()), 
                                    new LatLonPointImpl(bounds.getMaxY(), bounds.getMaxX()));

//                            System.out.println(Arrays.asList(new String[] { grid.getLastProcessedGridDatatype().getName() }));

                            StringBuilder errlog = new StringBuilder();
                            GridDataset gds = GridDatasetUtils.openGridDataset(dataURL.toString(), errlog);
                            if (gds == null) { 
                                throw new Exception("Can't open Grid Dataset at location= "+dataURL.toString()+"; error message= "+errlog);
                            }
                            

                            NetcdfCFWriter writer = new NetcdfCFWriter();
                            writer.makeFile(file.toString(), 
                                    gds, 
                                    Arrays.asList(new String[] { grid.getLastProcessedGridDatatype().getName() }), 
                                    subsetBbox, 
                                    ( grid.getLastProcessedDateTime() != null) ? 
                                    		new CalendarDateRange(
                                    				CalendarDate.of(grid.getLastProcessedDateTime()), 0) : null, 
                                    false, 1, 1, 1);
                            gds.close();
                            
                            return;
                        
                        }
                        else {
//                            throw new WCTExportException("For lat/lon datasets, currently only Gridded NetCDF export is supported");
                            
                            GridDatasetNativeRaster nativeGrid = new GridDatasetNativeRaster();
                            nativeGrid.setGridIndex(gridExportGridIndex);
                            if (gridExportGridVariableName != null) {
                                nativeGrid.setGridVariableName(gridExportGridVariableName);
                            }
                            nativeGrid.setZIndex(gridExportZIndex);
                            nativeGrid.setRuntimeIndex(gridExportRuntimeIndex);
                            nativeGrid.setTimeIndex(gridExportTimeIndex);
                            nativeGrid.process(dataURL.toString(), exportGridSatelliteFilter.getExtentFilter());
                            genericRaster = nativeGrid;                            
                        }
                    }
                    else {
                        grid.process(dataURL.toString(), exportGridSatelliteFilter.getExtentFilter());
                        genericRaster = grid;
                    }
                    
                }


                logger.fine("RASTER AFTER: "+grid.toString());


                grid.removeDataDecodeListener(this);

                //====================================================================
                //----------- EXPORT DATA! -------------------------------------------
                //====================================================================
                // Lazy object creation
                if (rasterExport == null) {
                    rasterExport = new WCTRasterExport();
                    rasterExport.addGeneralProgressListener(this);
                }

                if (outputFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT) {
                    rasterExport.saveGeoTIFF(file, genericRaster, GeoTiffType.TYPE_8_BIT);
                }
                else if (outputFormat == ExportFormat.GEOTIFF_32BIT) {
                    rasterExport.saveGeoTIFF(file, genericRaster, GeoTiffType.TYPE_32_BIT);
                }
                else if (outputFormat == ExportFormat.ARCINFOASCII) {
                    rasterExport.saveAsciiGrid(file, genericRaster, exportAsciiFormat);
                }
                else if (outputFormat == ExportFormat.ARCINFOBINARY) {
                    rasterExport.saveBinaryGrid(file, genericRaster);
                }
                else if (outputFormat == ExportFormat.GRIDDED_NETCDF) {
                    rasterExport.saveNetCDF(file, genericRaster);
                }
                else if (outputFormat == ExportFormat.WCT_RASTER_OBJECT_ONLY) {
                	; // do nothing, object already exists
                }
                else if (outputFormat == ExportFormat.KMZ) {
                	BatchKmzUtilities kmzBatchUtils = new BatchKmzUtilities();
                	kmzBatchUtils.saveKmz(file, grid, this.exportKmlOptions);
                }
                else {
                    throw new WCTExportException("This output format is not supported for this datatype");
                }

            }
            else {
                throw new WCTExportException("Unsupported export format type (Vector or Raster ONLY)");
            }   
            
        }
        else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {
            throw new WCTExportException("This is not a known supported data type or format: " + dataURL);
        }
        else {
            
            
            
            
            
            
            
            
            
            
            
            
            
            //================================================================================
            // RADAR EXPORT
            //================================================================================


                        
            //================================================================================
            // DECODE HEADER
            //================================================================================

            if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {

            	

                radialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(
                          FeatureType.RADIAL, 
                          dataURL.toString(), WCTUtils.getSharedCancelTask(), new StringBuilder());

                
                
              if (radialDatasetHeader == null) {
                  radialDatasetHeader = new DecodeRadialDatasetSweepHeader();
              }
              radialDatasetHeader.setRadialDatasetSweep(radialDataset);
              header = radialDatasetHeader;
              
              

              String urlString = dataURL.toString();
              if (radialDatasetHeader.getICAO().equals("XXXX")) {
                  int idx = urlString.lastIndexOf('/');
                  String icao = urlString.substring(idx+1, idx+5);
                  if (icao.equals("6500")) {
                      icao = urlString.substring(idx+5, idx+9); 
                  }

                  System.err.println("SETTING SITE FROM FILENAME FOR: "+icao);

                  RadarHashtables nxhash = RadarHashtables.getSharedInstance();
                  radialDatasetHeader.setStationInfo(icao, nxhash.getLat(icao), nxhash.getLon(icao), nxhash.getElev(icao));
              }

              
              
            	
                if (outputType == ExportFormatType.RASTER) {
                    if (radialDatasetRaster == null) {
                        radialDatasetRaster = new RadialDatasetSweepRemappedRaster();
                        radialDatasetRaster.addDataDecodeListener(this);
                    }

                    
            
                    String variableName = radialExportVariable == null ? radialDataset.getDataVariables().get(0).toString() : radialExportVariable;
                    if (exportRadialFilter.getExtentFilter() == null) {
                        bounds = header.getNexradBounds();
                    }
                    else {
                        bounds = exportRadialFilter.getExtentFilter();
                    }

                    if (exportGridCellSize > 0.0) {

                        // calculate the raster size needed in decimal degrees
                        // find number grid cells needed for 'long' side of grid
                        double longSide = ( bounds.getWidth() > bounds.getHeight() ) ? bounds.getWidth() : bounds.getHeight();
                        this.exportGridSize = (int) Math.round(longSide/exportGridCellSize);
//                        rasterizer.setSize(new Dimension(exportGridSize, exportGridSize));
                        double boundsDiff = longSide - (exportGridSize*exportGridCellSize);
                        bounds.setRect(bounds.getX()-boundsDiff/2.0, bounds.getY()+boundsDiff/2.0, bounds.getWidth()+boundsDiff, bounds.getHeight()+boundsDiff); 

                        logger.fine("SETTING GRID CELL SIZE: "+ exportGridCellSize);
                        logger.fine("SETTING GRID SIZE: "+ exportGridSize);
                        logger.fine("BOUNDS DIFF: "+ boundsDiff);
                    }
                    radialDatasetRaster.setHeight(exportGridSize);
                    radialDatasetRaster.setWidth(exportGridSize);
                    
                    radialDatasetRaster.setVariableName(variableName);
                    radialDatasetRaster.setSweepIndex(radialExportCut);
                    radialDatasetRaster.setWctFilter(wctFilter);
                    dataURL = WCTDataUtils.scan(dataURL, scannedFile, useWctCache, true, SupportedDataType.RADIAL);
                    if (Double.isNaN(radialExportCappiHeightInMeters)) {
                    	radialDatasetRaster.process(dataURL.toString(), bounds);
                    }
                    else {
                    	radialDatasetRaster.processCAPPI(dataURL.toString(), bounds, 
                    			new double[] { radialExportCappiHeightInMeters }, radialExportCappiInterpolationType);
                    }
                    
                    
                    genericRaster = radialDatasetRaster;

                    

                    if (exportGridSmoothFactor > 0) {
                    	radialDatasetRaster.setSmoothingFactor(exportGridSmoothFactor);
                        GridCoverage gc = radialDatasetRaster.getGridCoverage(0);
                        java.awt.image.RenderedImage renderedImage = gc.getRenderedImage();
                        Raster raster = renderedImage.getData();

                        radialDatasetRaster.setWritableRaster((WritableRaster)(renderedImage.getData()));
                        rasterizer.setWritableRaster((WritableRaster)raster);
                    }
                    
                    
                    
                    //====================================================================
                    //----------- EXPORT DATA! -------------------------------------------
                    //====================================================================
                    // Lazy object creation
                    if (rasterExport == null) {
                        rasterExport = new WCTRasterExport();
                        rasterExport.addGeneralProgressListener(this);
                    }

                    if (outputFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT) {
                        rasterExport.saveGeoTIFF(file, genericRaster, GeoTiffType.TYPE_8_BIT);
                    }
                    else if (outputFormat == ExportFormat.GEOTIFF_32BIT) {
                        rasterExport.saveGeoTIFF(file, genericRaster, GeoTiffType.TYPE_32_BIT);
                    }
                    else if (outputFormat == ExportFormat.ARCINFOASCII) {
                        rasterExport.saveAsciiGrid(file, genericRaster, exportAsciiFormat);
                    }
                    else if (outputFormat == ExportFormat.ARCINFOBINARY) {
                        rasterExport.saveBinaryGrid(file, genericRaster);
                    }
                    else if (outputFormat == ExportFormat.GRIDDED_NETCDF) {
                        rasterExport.saveNetCDF(file, genericRaster);
                    }
                    else if (outputFormat == ExportFormat.WCT_RASTER_OBJECT_ONLY) {
                        ; // do nothing, the object is ready
                    }
                    else if (outputFormat == ExportFormat.KMZ) {
                    	// this is null when run from the WCTExportDialog
                    	if (this.exportKmlOptions == null) {
                            
                    		this.exportKmlOptions = new ExportKmlOptions();
                            this.exportKmlOptions.setDrawColorMapTransparentBackgroundPattern(true);
                            URL paletteURL = ResourceUtils.getInstance().getJarResource(
                    				new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                    				"/config/colormaps/"+NexradSampleDimensionFactory.getDefaultPaletteName(radialDatasetHeader.getProductCode()), null);

                            this.exportKmlOptions.setPaletteURL(paletteURL);
                            this.exportKmlOptions.setAltMode(AltitudeMode.CLAMPED_TO_GROUND);
                            this.exportKmlOptions.setLogoURL(WCTExport.class.getResource("/images/noaa_logo_50x50.png"));

                    	}
                    	
                    	BatchKmzUtilities kmzBatchUtils = new BatchKmzUtilities();
                    	kmzBatchUtils.saveKmz(file, radialDatasetRaster, 
                    			radialDatasetHeader, this.exportKmlOptions);
                    }
                    
                    for (int i = 0; i < listeners.size(); i++) {
                        event.setProgress(100);
                        listeners.get(i).exportEnded(event);
                    }

                    radialDataset.close();
                    return;

                }
                

                
                
                
                

            }
            else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
                    scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
                // Lazy object creation
                if (level3Header == null) {
                    level3Header = new DecodeL3Header();
                }
                level3Header.decodeHeader(dataURL);
                header = level3Header;

                // Automatically generate output filename
                if (file == null) {
                    file = new File(saveDirectory + "/" + scannedFile.getSaveName());
                }

//              if (header.getProductType() == NexradHeader.L3VAD) {
//              throw new NexradExportException("The VAD Wind Profile Product (NVW) cannot be exported to other formats.");
//              }
            }
            else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
                // Lazy object creation
                if (xmrgHeader == null) {
                    xmrgHeader = new DecodeXMRGHeader();
                }
                xmrgHeader.decodeHeader(dataURL);
                header = xmrgHeader;

                // Automatically generate output filename
                if (file == null) {
                    file = new File(saveDirectory + "/" + scannedFile.getSaveName());
                }

            }
            else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {
                throw new WCTExportException("This data type is not supported with the Weather and Climate Toolkit.");
            }

            
            
            
            //================================================================================
            // DECODE DATA
            //================================================================================

            
            if (header.getProductType() == NexradHeader.LEVEL2) {

                if (radialDatasetDecoder == null) {
                    radialDatasetDecoder = new DecodeRadialDatasetSweep((DecodeRadialDatasetSweepHeader)header);
                    radialDatasetDecoder.addDataDecodeListener(this);

                    radialDatasetDecoder.setDecodeHint("attributes", DecodeRadialDatasetSweep.EXPORT_POLY_ATTRIBUTES);       
                }

                String variableName = radialExportVariable == null ? radialDataset.getDataVariables().get(0).toString() : radialExportVariable;
                RadialDatasetSweep.RadialVariable radialVar = (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable(variableName);
                radialDatasetDecoder.setRadialVariable(radialVar);

                radialDatasetDecoder.setDecodeHint("classify", new Boolean(radialExportClassify));
                radialDatasetDecoder.setDecodeHint("nexradFilter", exportRadialFilter);
                radialDatasetDecoder.setDecodeHint("startSweep", new Integer(radialExportCut));
                radialDatasetDecoder.setDecodeHint("endSweep", new Integer(radialExportCut));

                wctFilter = exportRadialFilter;
                decoder = radialDatasetDecoder;

                if (exportRadialFilter.getExtentFilter() == null) {
                    bounds = header.getNexradBounds();
                }
                else {
                    bounds = exportRadialFilter.getExtentFilter();
                }



                if ((exportPoints || exportAllPoints) && outputType == WCTExport.ExportFormatType.RASTER) {
                    throw new WCTExportException("The ExportPoints option is only for Vector export formats.");
                }
                
                // Check for attempt to export to raster with 'points' option selected
                if (exportAllPoints) {
                    radialDatasetDecoder.setDecodeHint("attributes", DecodeRadialDatasetSweep.EXPORT_POINT_ATTRIBUTES);     
                    radialDatasetDecoder.setDecodeHint("startSweep", new Integer(0));
                    radialDatasetDecoder.setDecodeHint("endSweep", new Integer(radialVar.getNumSweeps()));
                }
                else if (exportPoints) {
                    radialDatasetDecoder.setDecodeHint("attributes", DecodeRadialDatasetSweep.EXPORT_POINT_ATTRIBUTES);        
                }
                else {
                    radialDatasetDecoder.setDecodeHint("attributes", DecodeRadialDatasetSweep.EXPORT_POLY_ATTRIBUTES);       
                }
            }
            //================================================================================
            else if (header.getProductType() == NexradHeader.L3RADIAL ||
                    header.getProductType() == NexradHeader.L3RASTER ||
                    header.getProductType() == NexradHeader.L3RADIAL_8BIT ||
                    header.getProductType() == NexradHeader.L3DPA) {

                // initiate lazy object creation
                if (level3Decoder == null) {
                    level3Decoder = new DecodeL3Nexrad(level3Header);
                    level3Decoder.addDataDecodeListener(this);

                }
                
                // calculate the categories indices to include, now that we have the Header decoded
                if (level3Header.getDataThresholdStringArray() != null) {
                    exportL3Filter.setValueIndices(level3Header.getDataThresholdStringArray());
                }


                if (exportL3Filter.getExtentFilter() == null) {
                    bounds = header.getNexradBounds();
                }
                else {
                    bounds = exportL3Filter.getExtentFilter();
                }
                
                level3Decoder.setDecodeHint("nexradFilter", exportL3Filter);
                if (outputType == ExportFormatType.RASTER) {
                    level3Decoder.setDecodeHint("reducePolygons", new Boolean(false));
                }
                else {
                    level3Decoder.setDecodeHint("reducePolygons", new Boolean(radialReducePolys));
                }
//              level3Decoder.decodeData();

                wctFilter = exportL3Filter;
                decoder = level3Decoder;


                if (file == null) {
                    throw new WCTExportException("No output file provided ( file == NULL )");
                }

            }
            //================================================================================
            else if (header.getProductType() == NexradHeader.L3ALPHA || header.getProductType() == NexradHeader.L3VAD) {
                int pcode = header.getProductCode();

                if (file == null) {
                    throw new WCTExportException("No output file provided ( file == NULL )");
                }
                if (pcode == 58) {
                    alphaDecoder = new DecodeStormTracking(level3Header);
                }
                else if (pcode == 59) {
                    alphaDecoder = new DecodeHail(level3Header);
                }
                else if (pcode == 60) {
                    alphaDecoder = new DecodeMeso(level3Header);
                }
                else if (pcode == 61) {
                    alphaDecoder = new DecodeTVS(level3Header);
                }
                else if (pcode == 62) {
                    alphaDecoder = new DecodeStormStructure(level3Header);
                }
                else if (pcode == 141) {
                    alphaDecoder = new DecodeMDA(level3Header);
                }
                else if (pcode == 48) {
                    alphaDecoder = new DecodeVADText(level3Header);
                }
                else {
                    throw new WCTExportException("This product (code=" + pcode + ") is not yet supported!");
                }

                // We can only export to vector format
                if (outputType == ExportFormatType.RASTER) {

                    throw new WCTExportException("Alphanumeric products may not be exported in raster format");
                }

//                logger.fine("Writing: " + file);

                // 1. Write supplemental data to file+"_sup.txt"
                NexradAlphaExport.saveSupplementalData(file, alphaDecoder);

                // 2. Write to specified file format
                if (outputFormat == ExportFormat.SHAPEFILE) {
                    NexradAlphaExport.saveShapefile(file, alphaDecoder);
                    if (pcode == 58) {
                        // only do lines for storm tracking (NST)
                        NexradAlphaExport.saveLineShapefile(file, alphaDecoder);
                    }
                }
                else if (outputFormat == ExportFormat.WKT) {
                    NexradAlphaExport.saveWKT(file, alphaDecoder);
                    if (pcode == 58) {
                        // only do lines for storm tracking (NST)
                        NexradAlphaExport.saveLineWKT(file, alphaDecoder);
                    }
                }
                else if (outputFormat == ExportFormat.GML) {
                    NexradAlphaExport.saveGML(file, alphaDecoder);
                }
                else if (outputFormat == ExportFormat.CSV) {
                    NexradAlphaExport.saveCSV(file, alphaDecoder);
                }
                else {
                    throw new WCTExportException("UNKNOWN ERROR");
                }


                // End alphanumeric export!
                return;
            }
            //================================================================================
            else if (header.getProductType() == NexradHeader.XMRG) {
                // initiate lazy object creation
                if (xmrgDecoder == null) {
                    xmrgDecoder = new DecodeXMRGData(xmrgHeader);
                    xmrgDecoder.addDataDecodeListener(this);
                }


                if (exportGridSatelliteFilter.getExtentFilter() == null) {
                    bounds = header.getNexradBounds();
                }
                else {
                    bounds = exportGridSatelliteFilter.getExtentFilter();
                }
                xmrgDecoder.setDecodeHint("nexradFilter", exportGridSatelliteFilter);
                xmrgDecoder.setDecodeHint("classify", new Boolean(true));
                if (outputType == ExportFormatType.RASTER) {
                    xmrgDecoder.setDecodeHint("reducePolys", new Boolean(false));
                }
                else {
                    xmrgDecoder.setDecodeHint("reducePolys", new Boolean(radialReducePolys));
                }
//              xmrgDecoder.decodeData();

                wctFilter = exportGridSatelliteFilter;
                decoder = xmrgDecoder;


                if (file == null) {
                    throw new WCTExportException("No output file provided ( file == NULL )");
                }
            }
            else if (header.getProductType() == NexradHeader.L3GSM) {
                // Save GSM product to a text file
                String gsmString = level3Header.getGsmDisplayString(scannedFile.toString());
                BufferedWriter bw = new BufferedWriter(new FileWriter(file+".txt"));
                bw.write(gsmString);
                bw.flush();
                bw.close();
                return;
            }
            else if (header.getProductType() == NexradHeader.L3RSL) {
                // Save GSM product to a text file
                DecodeRSL rslDecoder = new DecodeRSL(level3Header);
                String rslString = rslDecoder.getRSLDisplayData();
                BufferedWriter bw = new BufferedWriter(new FileWriter(file+".txt"));
                bw.write(rslString);
                bw.flush();
                bw.close();
                return;
            }
            else {
                throw new WCTExportException("This Level-III NEXRAD Product is not supported with the Weather and Climate Toolkit: " + dataURL);
            }






























            //------------------------------------------------------------------------------------------------------------------
            // 3) Export data to selected format (includes rasterization for raster formats)
            //------------------------------------------------------------------------------------------------------------------



//            logger.fine("Writing: " + file);

            // Lazy object creation
            if (vectorExport == null) {
                vectorExport = new WCTVectorExport();
                vectorExport.addGeneralProgressListener(this);
            }


            // 1. Write supplemental data to file+"_sup.txt"
            try {
                NexradAlphaExport.saveSupplementalData(file, decoder);
            } catch (Exception e) {
            	e.printStackTrace();
                logger.severe(e.toString());
                logger.severe("ERROR SAVING SUPPLEMENTAL DATA");
            }



            // 2. Write graphical data


            if (outputFormat == ExportFormat.SHAPEFILE) {
                //logger.fine("Saving: " + file + ".shp , " + file + ".shx , " + file + ".dbf , " + file + ".prj");
                StreamingShapefileExport streamingProcess = new StreamingShapefileExport(file);
                streamingProcess.addGeneralProgressListener(this);
                decoder.decodeData(new StreamingProcess[] { streamingProcess });
            }
            else if (outputFormat == ExportFormat.WKT) {
                StreamingWKTExport streamingProcess = new StreamingWKTExport(file);
                streamingProcess.addGeneralProgressListener(this);
                decoder.decodeData(new StreamingProcess[] { streamingProcess });
            }
            else if (outputFormat == ExportFormat.GML) {
//              vectorExport.saveGML(file, decoder);
                throw new WCTExportException("GML IS NOT SUPPORTED");
            }
            else if (outputFormat == ExportFormat.CSV) {
                throw new WCTExportException("CSV EXPORT IS NOT SUPPORTED FOR THIS FILE TYPE");
            }
            else if (outputFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT ||
                    outputFormat == ExportFormat.GEOTIFF_32BIT ||
                    outputFormat == ExportFormat.ARCINFOASCII ||
                    outputFormat == ExportFormat.ARCINFOBINARY ||
                    outputFormat == ExportFormat.VTK ||
                    outputFormat == ExportFormat.GRIDDED_NETCDF ||
                    outputFormat == ExportFormat.WCT_RASTER_OBJECT_ONLY ||
                    outputFormat == ExportFormat.KMZ) {


                genericRaster = null;


                // Only create rasterizer if we are not using a native Level-3 raster format
                if (header.getProductType() != NexradHeader.L3DPA && header.getProductType() != NexradHeader.XMRG) {


                    if (rasterizer == null) {
                        rasterizer = new WCTRasterizer(1200, 1200, -999.0f);
                        rasterizer.addGeneralProgressListener(this);
                    }


                    logger.fine("------------------------------- EXPORT GRID CELL SIZE: "+exportGridCellSize);




                    if (exportGridCellSize > 0.0) {

                        // calculate the raster size needed in decimal degrees
                        // find number grid cells needed for 'long' side of grid
                        double longSide = ( bounds.getWidth() > bounds.getHeight() ) ? bounds.getWidth() : bounds.getHeight();
                        this.exportGridSize = (int) Math.round(longSide/exportGridCellSize);
                        rasterizer.setSize(new Dimension(exportGridSize, exportGridSize));
                        double boundsDiff = longSide - (exportGridSize*exportGridCellSize);
                        bounds.setRect(bounds.getX()-boundsDiff/2.0, bounds.getY()+boundsDiff/2.0, bounds.getWidth()+boundsDiff, bounds.getHeight()+boundsDiff); 

                        logger.fine("SETTING GRID CELL SIZE: "+ exportGridCellSize);
                        logger.fine("SETTING GRID SIZE: "+ exportGridSize);
                        logger.fine("BOUNDS DIFF: "+ boundsDiff);
                    }

                    rasterizer.setLongName(NexradUtilities.getLongName(header));
                    rasterizer.setUnits(NexradUtilities.getUnits(header));
                    rasterizer.setDateInMilliseconds(header.getMilliseconds());
                    rasterizer.setStandardName("");
                    rasterizer.setVariableName(NexradUtilities.getVariableName(header));

//                    System.out.println("setting variable name for radial data");
                    
                    // Set smoothing
                    //rasterizer.setSmoothing((exportGridSmoothFactor > 0));
                    //rasterizer.setSmoothFactor(exportGridSmoothFactor);


//                  rasterizer.rasterize(decoder.getFeatures(), bounds, exportGridVariableRes, "value", NDITRasterizer.FLOAT);

                    // ---- BELOW -- new implementation of StreamingProcess - reduces memory consumption by using streaming decoders
                    rasterizer.setBounds(bounds); // set up bounds to fit around features
                    rasterizer.setAttName("value");

                    rasterizer.clearRaster();
                    decoder.decodeData(new StreamingProcess[] { rasterizer });




                    if (exportGridSmoothFactor > 0) {
                        java.awt.Color[] colors = new Color[] { Color.black };
                        double[] maxmin = new double[] { 0.0, 1.0 };

                        gcSupport.setSmoothFactor(exportGridSmoothFactor);

                        GridCoverage gc = gcSupport.getGridCoverage(rasterizer, header, false, colors, maxmin);
                        java.awt.image.RenderedImage renderedImage = gc.getRenderedImage();
                        Raster raster = renderedImage.getData();

                        rasterizer.setWritableRaster((WritableRaster)raster);
                    }

                    genericRaster = rasterizer;
                }
                if (header.getProductType() == NexradHeader.L3DPA ||
                        header.getProductType() == NexradHeader.XMRG) {

                    // Check for native rasters - native raster export not yet supported for NetCDF and GeoTIFF
                    if (outputFormat == ExportFormat.GEOTIFF_32BIT || 
                            outputFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT ||
                            outputFormat == ExportFormat.GRIDDED_NETCDF) {

                        throw new WCTExportException("Export of this product is not available for GeoTIFF and NetCDF formats due the native non-WGS84 projection of the product.");
                    }

                    // Data must be decoded to have populate native raster
                    decoder.decodeData(new StreamingProcess[] {});

                    if (header.getProductType() == NexradHeader.L3DPA) {
                        genericRaster = ((DecodeL3Nexrad) decoder).getDPARaster();
                    }
                    else if (header.getProductType() == NexradHeader.XMRG) {
                        genericRaster = ((DecodeXMRGData) decoder).getXMRGRaster();
                    }

                }





                //====================================================================
                //----------- EXPORT DATA! -------------------------------------------
                //====================================================================
                // Lazy object creation
                if (rasterExport == null) {
                    rasterExport = new WCTRasterExport();
                    rasterExport.addGeneralProgressListener(this);
                }

                if (outputFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT) {
                    rasterExport.saveGeoTIFF(file, genericRaster, GeoTiffType.TYPE_8_BIT);
                }
                else if (outputFormat == ExportFormat.GEOTIFF_32BIT) {
                    rasterExport.saveGeoTIFF(file, genericRaster, GeoTiffType.TYPE_32_BIT);
                }
                else if (outputFormat == ExportFormat.ARCINFOASCII) {
                    rasterExport.saveAsciiGrid(file, genericRaster, exportAsciiFormat);
                }
                else if (outputFormat == ExportFormat.ARCINFOBINARY) {
                    rasterExport.saveBinaryGrid(file, genericRaster);
                }
                else if (outputFormat == ExportFormat.GRIDDED_NETCDF) {
                    rasterExport.saveNetCDF(file, genericRaster);
                }
                else if (outputFormat == ExportFormat.WCT_RASTER_OBJECT_ONLY) {
                	; // do nothing, the object is ready
                }
                else if (outputFormat == ExportFormat.KMZ) {
                	// this is null when run from the WCTExportDialog
                	if (this.exportKmlOptions == null) {
                        
                		this.exportKmlOptions = new ExportKmlOptions();
                        this.exportKmlOptions.setDrawColorMapTransparentBackgroundPattern(true);
                        URL paletteURL = ResourceUtils.getInstance().getJarResource(
                				new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                				"/config/colormaps/"+NexradSampleDimensionFactory.getDefaultPaletteName(level3Header.getProductCode()), null);

                        this.exportKmlOptions.setPaletteURL(paletteURL);
                        this.exportKmlOptions.setAltMode(AltitudeMode.CLAMPED_TO_GROUND);
                        this.exportKmlOptions.setLogoURL(WCTExport.class.getResource("/images/noaa_logo_50x50.png"));
                        
                	}
                	
                	
                	
                	BatchKmzUtilities kmzBatchUtils = new BatchKmzUtilities();
                	kmzBatchUtils.saveKmz(file, genericRaster, 
                			level3Header, this.exportKmlOptions);
                }

            }


        }



        if (radialDataset != null) {
        	radialDataset.close();
        }

        // Export done!
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(100);
            listeners.get(i).exportEnded(event);
        }

        //} catch (Exception e) {
        //
        //}

    }


    
    
    
    
    
    
//    /**
//     * Set a exportHint.  To get a list of supported hints and default values,
//     * use 'getExportHints()'.  The currently supported hints are as follows: <br><br>
//     * <ol>
//     *  <li> <b>startSweep</b>: 
//     *          integer zero-based sweep number.  If less than 0, it is set to 0.  If
//     *          startSweep > totalNumSweeps, then startSweep is set to the last sweep present.</li>
//     *  <li> <b>endSweep</b>: 
//     *          integer zero-based sweep number.  If endSweep < startSweep, then endSweep is
//     *          set to the value of startSweep.  If endSweep = startSweep, then only that one
//     *          sweep is decoded.  If endSweep > totalNumSweeps, then endSweep is set to the 
//     *          last sweep present.  An easy catch-all to decode all sweeps for any NEXRAD VCP 
//     *          would be to set the startSweep = 0 and endSweep = 1000 (for example). </li>
//     *  <li> <b>attributes</b>: 
//     *          AttributeType[] object that determines which set of attributes to produce.  
//     *          Use the static arrays in this class - they are the only ones supported.
//     *  <li> <b>Filter</b>: 
//     *          WCTFilter object that defines filtering options on range, azimuth, 
//     *          height (if radial), values and geographic bounds.
//     * @param hintsMap
//     */
//    public void setExportHint(String hintKey, Object hintValue) throws DecodeHintNotSupportedException {
//        if (! hintsMap.keySet().contains(hintKey)) {
//            throw new ExportHintNotSupportedException(this.getClass().toString(), hintKey, hintsMap);
//        }
//
//        hintsMap.put(hintKey, hintValue);
//    }
//
//    /**
//     * Get the key-value pairs for the current decode hints.  
//     * If no hints have been set, this will return the supported
//     * hints with default values.
//     * @return
//     */
//    public Map<String, Object> getDecodeHints() {
//        return hintsMap;
//    }


    
    
    
    
    
    
    
    
    
    
    
    public WCTRaster getLastProcessedRaster() {
    	return genericRaster;
    }



    //-----------------------------------------------------------
    // Implementation of DataDecodeListener interface
    //-----------------------------------------------------------

    /**
     *  Implementation of DataDecodeListener interface
     *
     * @param  decodeEvent  Description of the Parameter
     */
    public void decodeStarted(DataDecodeEvent decodeEvent) {
        String s = event.getDataURL().toString();
        event.setStatus("Decoding " + s.substring(s.lastIndexOf("/")+1));
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).exportStatus(event);
        }
    }


    /**
     *  Implementation of DataDecodeListener interface
     *
     * @param  decodeEvent  Description of the Parameter
     */
    public void decodeProgress(DataDecodeEvent decodeEvent) {

        // set overall progress for NexradExportEvent listeners
        if (exportAllPoints) {
        	
//        	System.out.println(decodeEvent.getProgress());
//        	System.out.println(decodeEvent.getDecodeMetadataMap().toString());
        	
            event.setProgress(decodeEvent.getProgress());
        }
        else {
            event.setProgress(decodeEvent.getProgress());
//          event.setProgress(decodeEvent.getProgress() / 2);
        }
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).exportProgress(event);
        }
    }


    /**
     *  Implementation of DataDecodeListener interface
     *
     * @param  decodeEvent  Description of the Parameter
     */
    public void decodeEnded(DataDecodeEvent decodeEvent) {
        event.setStatus("Decoding NEXRAD Complete");
//        logger.fine("DECODE ENDED EVENT");
    }
    
    public void metadataUpdate(DataDecodeEvent decodeEvent) {
    }


    //-----------------------------------------------------------
    // Implementation of GeneralProgressListener interface
    //-----------------------------------------------------------

    /**
     *  Implementation of GeneralProgressListener interface
     *
     * @param  generalEvent  Description of the Parameter
     */
    public void started(GeneralProgressEvent generalEvent) {
        // set overall progress for NexradExportEvent listeners
        if (generalEvent.getSource() == vectorExport) {
            event.setStatus("Saving: " + event.getOutputFile().getName());
        }
        else if (generalEvent.getSource() == rasterizer) {
            event.setStatus("Rasterizing " + event.getOutputFile().getName());
        }
        else if (generalEvent.getSource() == rasterExport) {
            event.setStatus("Saving: " + event.getOutputFile().getName());
        }

        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).exportStatus(event);
        }
    }


    /**
     *  Implementation of GeneralProgressListener interface
     *
     * @param  generalEvent  Description of the Parameter
     */
    public void progress(GeneralProgressEvent generalEvent) {

//      if (generalEvent.getSource() == vectorExport) {
//      // Rasterizer progress - 50% to 100% of total
//      event.setProgress(50 + generalEvent.getProgress() / 2);
//      //logger.fine("VECTOR EXPORT EVENT");
//      // set overall progress for NexradExportEvent listeners
//      }
//      else if (generalEvent.getSource() == rasterizer) {
//      // Rasterizer progress - 50% to 75% of total
//      event.setProgress(50 + generalEvent.getProgress() / 4);
//      //logger.fine("RASTERIZER EXPORT EVENT");
//      }
//      else if (generalEvent.getSource() == rasterExport) {
//      // Rasterizer progress - 50% to 75% of total
//      event.setProgress(75 + generalEvent.getProgress() / 4);
//      //logger.fine("RASTER EXPORT EVENT");
//      }
//      else if (generalEvent.getSource() == cappi) {
//      // Rasterizer progress - 0% to 100% of total
//      event.setProgress(0 + generalEvent.getProgress());
//      //logger.fine("CAPPI EXPORT EVENT");
//      }

//      for (int i = 0; i < listeners.size(); i++) {
//      listeners.get(i).exportProgress(event);
//      }
    }


    /**
     *  Implementation of GeneralProgressListener interface
     *
     * @param  generalEvent  Description of the Parameter
     */
    public void ended(GeneralProgressEvent generalEvent) {
        if (generalEvent.getSource() == vectorExport) {
            event.setStatus("NEXRAD Export Complete");
        }
    }




























    /**
     *  Gets the outputFormat attribute of the WCTExport object
     *
     * @return    The outputFormat value
     */
    public ExportFormat getOutputFormat() {
        return outputFormat;
    }


    /**
     *  Sets the outputFormat attribute of the WCTExport object
     *
     * @param  outputFormat  The new outputFormat value
     */
    public void setOutputFormat(ExportFormat outputFormat) {
        this.outputFormat = outputFormat;
        setOutputType();
    }


    /**
     *  Sets the outputType attribute of the WCTExport object
     */
    private void setOutputType() {
        outputType = getExportFormatType(outputFormat);
    }

    public static ExportFormatType getExportFormatType(ExportFormat exportFormat) {
        if (exportFormat == ExportFormat.SHAPEFILE ||
                exportFormat == ExportFormat.WKT ||
                exportFormat == ExportFormat.CSV) {

            return ExportFormatType.VECTOR;
        }
        else if (exportFormat == ExportFormat.GEOTIFF_GRAYSCALE_8BIT ||
                exportFormat == ExportFormat.GEOTIFF_32BIT ||
                exportFormat == ExportFormat.ARCINFOASCII ||
                exportFormat == ExportFormat.ARCINFOBINARY ||
                exportFormat == ExportFormat.VTK ||
                exportFormat == ExportFormat.GRIDDED_NETCDF ||
                exportFormat == ExportFormat.WCT_RASTER_OBJECT_ONLY ||
                exportFormat == ExportFormat.KMZ) {

            return ExportFormatType.RASTER;
        }
        else {
            return ExportFormatType.UNKNOWN;
        }
    }
    

    /**
     *  Sets the exportClassify attribute of the WCTExport object
     *
     * @param  exportClassify  The new exportClassify value
     */
    public void setExportClassify(boolean exportClassify) {
        this.radialExportClassify = exportClassify;
    }


    /**
     *  Gets the exportClassify attribute of the WCTExport object
     *
     * @return    The exportClassify value
     */
    public boolean getExportClassify() {
        return radialExportClassify;
    }


    /**
     *  Sets the exportVariable attribute of the WCTExport object
     *
     * @param  exportVariable  The new exportMoment value
     */
    public void setExportVariable(String exportVariable) {
        this.radialExportVariable = exportVariable;
    }


    /**
     *  Gets the exportMoment attribute of the WCTExport object
     *
     * @return    The exportMoment value
     */
    public String getExportMoment() {
        return radialExportVariable;
    }


    /**
     *  Sets the exportCut attribute of the WCTExport object
     *
     * @param  exportCut  The new exportCut value
     */
    public void setExportCut(int exportCut) {
        this.radialExportCut = exportCut;
    }


    /**
     *  Gets the exportCut attribute of the WCTExport object
     *
     * @return    The exportCut value
     */
    public int getExportCut() {
        return radialExportCut;
    }


    /**
     *  Sets the exportUseRF attribute of the WCTExport object
     *
     * @param  exportUseRF  The new exportUseRF value
     */
    public void setExportUseRF(boolean exportUseRF) {
        this.radialExportUseRF = exportUseRF;
    }


    /**
     *  Gets the exportUseRF attribute of the WCTExport object
     *
     * @return    The exportUseRF value
     */
    public boolean getExportUseRF() {
        return radialExportUseRF;
    }


    /**
     * Set the WCTFilter for Level-II Data
     *
     * @param  exportRadialFilter  The new exportL2Filter value
     */
    public void setExportRadialFilter(WCTFilter exportRadialFilter) {
        this.exportRadialFilter = exportRadialFilter;
        this.wctFilter = exportRadialFilter;
    }


    /**
     * Set the WCTFilter for Level-III Data
     *
     * @param  exportL3Filter  The new exportL3Filter value
     */
    public void setExportL3Filter(WCTFilter exportL3Filter) {
        this.exportL3Filter = exportL3Filter;
        this.wctFilter = exportL3Filter;
    }


    /**
     * Set the WCTFilter for Grid or Satellite Data
     *
     * @param  exportGridSatelliteFilter 
     */
    public void setExportGridSatelliteFilter(WCTFilter exportGridSatelliteFilter) {
        this.exportGridSatelliteFilter = exportGridSatelliteFilter;
        this.wctFilter = exportGridSatelliteFilter;
    }



    /**
     * Gets the WCTFilter for Level-II Data
     *
     * @return    The exportL2Filter value
     */
    public WCTFilter getExportL2Filter() {
        return exportRadialFilter;
    }


    /**
     * Gets the WCTFilter for Level-III Data
     *
     * @return    The exportL3Filter value
     */
    public WCTFilter getExportL3Filter() {
        return exportL3Filter;
    }


    /**
     * Gets the WCTFilter for XMRG Data
     *
     * @return    The exportXMRGFilter value
     */
    public WCTFilter getExportXMRGFilter() {
        return exportGridSatelliteFilter;
    }



    /**
     * Gets the WCTFilter for Grid Data
     *
     * @return    The exportGridFilter value
     */
    public WCTFilter getExportGridFilter() {
        return exportGridSatelliteFilter;
    }







    /**
     *  Sets the exportPoints attribute of the WCTExport object
     *
     * @param  exportPoints  The new exportPoints value
     */
    public void setExportPoints(boolean exportPoints) {
        this.exportPoints = exportPoints;
    }


    /**
     *  Gets the exportPoints attribute of the WCTExport object
     *
     * @return    The exportPoints value
     */
    public boolean getExportPoints() {
        return exportPoints;
    }


    /**
     *  Sets the exportAllPoints attribute of the WCTExport object
     *
     * @param  exportAllPoints  The new exportAllPoints value
     */
    public void setExportAllPoints(boolean exportAllPoints) {
        this.exportAllPoints = exportAllPoints;
    }


    /**
     *  Gets the exportAllPoints attribute of the WCTExport object
     *
     * @return    The exportAllPoints value
     */
    public boolean getExportAllPoints() {
        return exportAllPoints;
    }


    /**
     *  Sets the exportReducePolys attribute of the WCTExport object
     *
     * @param  reducePolys  The new exportReducePolys value
     */
    public void setExportReducePolys(boolean reducePolys) {
        this.radialReducePolys = reducePolys;
    }


    /**
     *  Gets the exportReducePolys attribute of the WCTExport object
     *
     * @return    The exportReducePolys value
     */
    public boolean getExportReducePolys() {
        return radialReducePolys;
    }









    /**
     *  Sets the exportGridSize attribute of the WCTExport object.  Use of this
     *  method will set the exportGridCellSize property to 'auto'.
     *
     * @param  exportGridSize  The new exportGridSize value
     */
    public void setExportGridSize(int exportGridSize) {
        this.exportGridSize = exportGridSize;
        if (rasterizer == null) {
            rasterizer = new WCTRasterizer(exportGridSize, exportGridSize, exportGridNoData);
            rasterizer.addGeneralProgressListener(this);
        }
        else {
            rasterizer.setSize(new Dimension(exportGridSize, exportGridSize));
        }

        exportGridCellSize = -1;
    }

    /**
     *  Gets the exportGridSize attribute of the WCTExport object
     *
     * @return    The exportGridSize value
     */
    public int getExportGridSize() {
        return exportGridSize;
    }


    public void setExportGridCellSize(double exportGridCellSize) {
        this.exportGridCellSize = exportGridCellSize;      
    }



    public double getExportGridCellSize() {
        return exportGridCellSize;
    }


    /**
     *  Sets the exportGridNoData attribute of the WCTExport object
     *
     * @param  exportGridNoData  The new exportGridNoData value
     * @throws InstantiationException 
     * @throws IllegalAccessException 
     */
    public void setExportGridNoData(float exportGridNoData) throws IllegalAccessException, InstantiationException {
        this.exportGridNoData = exportGridNoData;
        if (rasterizer == null) {
            rasterizer = new WCTRasterizer(exportGridSize, exportGridSize, exportGridNoData);
            rasterizer.addGeneralProgressListener(this);
        }
        rasterizer.setNoDataValue((double)exportGridNoData);
        

        if (radialDatasetRaster == null) {
            radialDatasetRaster = new RadialDatasetSweepRemappedRaster();
            radialDatasetRaster.addDataDecodeListener(this);
        }
       	radialDatasetRaster.setNoDataValue(exportGridNoData);
        
    }


    /**
     *  Gets the exportGridNoData attribute of the WCTExport object
     *
     * @return    The exportGridNoData value
     */
    public float getExportGridNoData() {
        return exportGridNoData;
    }




    /**
     *  Sets the exportGridSmoothFactor attribute of the WCTExport object
     *
     * @param  smoothFactor  The new exportGridSmoothFactor value
     */
    public void setExportGridSmoothFactor(int smoothFactor) {
        this.exportGridSmoothFactor = smoothFactor;
        gcSupport.setSmoothFactor(smoothFactor);
    }


    /**
     *  Gets the exportGridSmoothFactor attribute of the WCTExport object
     *
     * @return    The exportGridSmoothFactor value
     */
    public int getExportGridSmoothFactor() {
        return exportGridSmoothFactor;
    }



    public void setExportAsciiFormat(DecimalFormat exportAsciiFormat) {
		this.exportAsciiFormat = exportAsciiFormat;
	}


	public DecimalFormat getExportAsciiFormat() {
		return exportAsciiFormat;
	}


	public void setExportKmlOptions(ExportKmlOptions exportKmlOptions) {
		this.exportKmlOptions = exportKmlOptions;
	}


	public ExportKmlOptions getExportKmlOptions() {
		return exportKmlOptions;
	}


	/**
     *  Sets the defaultSaveDirectory attribute of the WCTExport object
     *
     * @param  saveDirectory  The new defaultSaveDirectory value
     */
    public void setDefaultSaveDirectory(String saveDirectory) {
        this.saveDirectory = saveDirectory;
    }


    /**
     *  Gets the defaultSaveDirectory attribute of the WCTExport object
     *
     * @return    The defaultSaveDirectory value
     */
    public String getDefaultSaveDirectory() {
        return saveDirectory;
    }



//    /**
//     * Set to 'null' to disengage the override
//     *
//     * @param  cappi  The new extractCAPPI value
//     */
//    public void setExtractCAPPI(ExtractCAPPI cappi) {
//        this.cappi = cappi;
//    }
//
//
//    /**
//     *  Gets the exportCAPPI attribute of the WCTExport object
//     *
//     * @return    The exportCAPPI value
//     */
//    public ExtractCAPPI getExportCAPPI() {
//        return cappi;
//    }




    public SupportedDataType getLastProcessedDataType() {
    	return this.currentDataType;
    }
    
    public GridDatasetRemappedRaster getLastProcessedGridDatasetRemappedRaster() {
    	return this.grid;
    }
    
    public GoesRemappedRaster getLastProcessedGoesRemappedRaster() {
    	return this.goes;
    }

    public RadialDatasetSweepRemappedRaster getLastProcessedRadialRemappedRaster() {
    	return this.radialDatasetRaster;
    }


    /**
     *  Implementation of NexradInterface
     *
     * @return    The nexradFile value
     */
    public FileScanner getFileScanner() {
        return scannedFile;
    }


    /**
     *  Implementation of NexradInterface
     *
     * @return    The level2Header value
     */
    public DecodeRadialDatasetSweepHeader getDecodeRadialDatasetSweepHeader() {
        return radialDatasetHeader;
    }


    /**
     *  Implementation of NexradInterface
     *
     * @return    The level3Header value
     */
    public DecodeL3Header getLevel3Header() {
        return level3Header;
    }


    /**
     *  Implementation of NexradInterface
     *
     * @return    The xMRGHeader value
     */
    public DecodeXMRGHeader getXMRGHeader() {
        return xmrgHeader;
    }


    /**
     *  Implementation of NexradInterface
     *
     * @return    The level2Decoder value
     */
    public DecodeRadialDatasetSweep getRadialDatasetDecoder() {
        return radialDatasetDecoder;
    }


    /**
     *  Implementation of NexradInterface
     *
     * @return    The level3Decoder value
     */
    public DecodeL3Nexrad getLevel3Decoder() {
        return level3Decoder;
    }


    /**
     *  Implementation of NexradInterface
     *
     * @return    The xMRGDecoder value
     */
    public DecodeXMRGData getXMRGDecoder() {
        return xmrgDecoder;
    }


    /**
     *  Implementation of NexradInterface
     *  Gets the WCTFilter attribute of the NexradIAViewer object
     *
     * @return    nxfilter  The current WCTFilter
     */
    public WCTFilter getFilter() {
        return wctFilter;
    }

























    /**
     * Adds a DataExportListener to the list.
     *
     * @param  listener  The feature to be added to the DataExportListener attribute
     */
    public void addDataExportListener(DataExportListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }


    /**
     * Removes a DataExportListener from the list.
     *
     * @param  listener  DataExportListener to remove.
     */
    public void removeDataExportListener(DataExportListener listener) {
        listeners.remove(listener);
    }



    /**
     * Adds a GeneralProgressListener to the list.
     *
     * @param  listener  The feature to be added to the GeneralProgressListener attribute
     */
    public void addGeneralProgressListener(GeneralProgressListener listener) {
        if (!generalProgressListeners.contains(listener)) {
            generalProgressListeners.add(listener);
        }
    }


    /**
     * Removes a GeneralProgressListener from the list.
     *
     * @param  listener  GeneralProgressListener to remove.
     */
    public void removeDataExportListener(GeneralProgressListener listener) {
    	generalProgressListeners.remove(listener);
    }









    /**
     * Overrides 'setGridExportGridIndex'
     * @param gridExportGridVariableName
     */
    public void setGridExportGridVariableName(String gridExportGridVariableName) {
        this.gridExportGridVariableName = gridExportGridVariableName;
    }

    
    /**
     * Overrides 'setGridExportGridVariableName'
     * @param gridExportGridIndex
     */
    public void setGridExportGridIndex(int gridExportGridIndex) {
        this.gridExportGridVariableName = null;
        this.gridExportGridIndex = gridExportGridIndex;
    }


    public int getGridExportGridIndex() {
        return gridExportGridIndex;
    }


    public void setGridExportZIndex(int gridExportZIndex) {
        this.gridExportZIndex = gridExportZIndex;
    }


    public int getGridExportZIndex() {
        return gridExportZIndex;
    }


    public void setGridExportTimeIndex(int gridExportTimeIndex) {
        this.gridExportTimeIndex = gridExportTimeIndex;
    }


    public int getGridExportTimeIndex() {
        return gridExportTimeIndex;
    }


    public void setGridExportRuntimeIndex(int gridExportRuntimeIndex) {
        this.gridExportRuntimeIndex = gridExportRuntimeIndex;
    }


    public int getGridExportRuntimeIndex() {
        return gridExportRuntimeIndex;
    }


    public void setRadialExportCappiHeightInMeters(
			double radialExportCappiHeightInMeters) {
		this.radialExportCappiHeightInMeters = radialExportCappiHeightInMeters;
	}


	public double getRadialExportCappiHeightInMeters() {
		return radialExportCappiHeightInMeters;
	}


	public CAPPIType getRadialExportCappiInterpolationType() {
		return radialExportCappiInterpolationType;
	}


	public void setRadialExportCappiInterpolationType(
			CAPPIType radialExportCappiInterpolationType) {
		this.radialExportCappiInterpolationType = radialExportCappiInterpolationType;
	}


	public void setUseWctCache(boolean useWctCache) {
        this.useWctCache = useWctCache;
    }


    public boolean isUseWctCache() {
        return useWctCache;
    }


	public void setForceResample(boolean forceResample) {
		this.forceResample = forceResample;
	}


	public boolean isForceResample() {
		return forceResample;
	}


	public boolean isAutoRenameOutput() {
		return autoRenameOutput;
	}


	public void setAutoRenameOutput(boolean autoRenameOutput) {
		this.autoRenameOutput = autoRenameOutput;
	}


















}

