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

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.nexradiv.legend.NexradLegendLabelFactory;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.Level2Transfer;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.io.WCTTransfer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.feature.IllegalAttributeException;
import org.xml.sax.SAXException;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

/**
 *  Description of the Class
 *
 * @author    steve.ansari
 */
public class WCTMetaExport {


    private FileScanner scannedFile;
    private NexradHeader header;
    private RadialDatasetSweep radialDataset;
    private DecodeRadialDatasetSweepHeader radialDatasetHeader;
    private DecodeL3Header level3Header;
    private DecodeXMRGHeader xmrgHeader;



    private StringBuilder metadata = new StringBuilder();
    




    /**
     *Constructor for the NexradExport object
     */
    public WCTMetaExport() { 
    }


    /**
     *  Description of the Method
     *
     * @param  dataURL                           Description of the Parameter
     * @exception  WCTExportException           Description of the Exception
     * @throws DecodeException 
     * @exception  WCTExportNoDataException     Description of the Exception
     * @exception  DecodeException           Description of the Exception
     * @throws IOException 
     * @exception  FeatureRasterizerException       Description of the Exception
     * @exception  IllegalAttributeException       Description of the Exception
     * @exception  ConnectException                Description of the Exception
     * @exception  FileNotFoundException           Description of the Exception
     * @exception  IOException                     Description of the Exception
     * @throws ParseException 
     * @exception  ucar.ma2.InvalidRangeException  Description of the Exception
     * @throws DecodeHintNotSupportedException 
     * @throws URISyntaxException 
     * @throws ParseException 
     * @throws URISyntaxException 
     * @throws SQLException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public String getMetadata(URL dataURL)
        throws WCTExportException, DecodeException, IOException, ParseException, URISyntaxException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {

    	if (scannedFile == null) {
    		scannedFile = new FileScanner();
    	}
    	


        // clear the buffer
        metadata.setLength(0);

        // set up date formatter
        SimpleDateFormat metadataDateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss'Z'");
        metadataDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        

        //------------------------------------------------------------------------------------------------------------------
        // 1) Download the data
        //------------------------------------------------------------------------------------------------------------------

        // Transfer Level-2 to temp directory for decompression if necessary
        scannedFile.scanURL(dataURL);
        // Check for file compression
        if (scannedFile.isZCompressed()) {
            dataURL = Level2Transfer.getNCDCLevel2UNIXZ(dataURL);
            scannedFile.scanURL(dataURL);
        }
        else if (scannedFile.isGzipCompressed()) {
            dataURL = Level2Transfer.getNCDCLevel2GZIP(dataURL);
            scannedFile.scanURL(dataURL);
        }
        else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
            dataURL = WCTTransfer.getURL(dataURL);
            // HARD CODE - for right now, all Level2 files will be checked for "AR2V0001" partial BZIP format
            dataURL = Level2Transfer.decompressAR2V0001(dataURL);
            scannedFile.scanURL(dataURL);
        }
        else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
                scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {

            // Transfer file to local tmp area -- force overwrite if NWS
            if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
                dataURL = WCTTransfer.getURL(dataURL, true);
            }               
            else {
                dataURL = WCTTransfer.getURL(dataURL);
            }
            scannedFile.scanURL(dataURL);
        }
//        else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//
//            scannedFile.scanURL(dataURL);
//        }
        else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
            scannedFile.scanURL(dataURL);
        }
        else {
            throw new WCTExportException("Invalid file or unrecognized file type.  Please review list of supported file types and naming conventions.");
        }





























        //------------------------------------------------------------------------------------------------------------------
        // 2) Read header info
        //------------------------------------------------------------------------------------------------------------------
        
        
        if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
            //================================================================================
            // SATELLITE EXPORT
            //================================================================================
                throw new WCTExportException("This output format is not supported for this datatype");
                
                
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


                CancelTask emptyCancelTask = new CancelTask() {
					@Override
                    public boolean isCancel() {
                        return false;
                    }
					@Override
                    public void setError(String arg0) {
                    }
					@Override
					public void setProgress(String arg0, int arg1) {						
					}
                };


                if (radialDataset == null || 
                        ! radialDataset.getLocationURI().equals(dataURL.toURI().toString())) {

                    radialDataset = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
                            FeatureType.RADIAL, 
                            dataURL.toString(), emptyCancelTask, new Formatter());

                }


//                NCdump.print(radialDataset.getNetcdfFile(), "", System.out, null);


                if (radialDatasetHeader == null) {
                    radialDatasetHeader = new DecodeRadialDatasetSweepHeader();
                }
                radialDatasetHeader.setRadialDatasetSweep(radialDataset);
                header = radialDatasetHeader;

                // if the file does not have lat/lon/site info encoded inside, set it here based on lookup table

                String urlString = dataURL.toString();
                if (radialDatasetHeader.getICAO().equals("XXXX")) {
                    int idx = urlString.lastIndexOf('/');
                    String icao = urlString.substring(idx+1, idx+5);
                    if (icao.equals("6500")) {
                        icao = urlString.substring(idx+5, idx+9); 
                    }

                    System.err.println("SETTING SITE MANUALLY FOR: "+icao);

                    RadarHashtables nxhash = RadarHashtables.getSharedInstance();
                    radialDatasetHeader.setStationInfo(icao, nxhash.getLat(icao), nxhash.getLon(icao), nxhash.getElev(icao));
                }
                
                
                
                metadata.append(dataURL.toString()).append(",");
                metadata.append(radialDatasetHeader.getICAO()).append(",");
                metadata.append(radialDatasetHeader.getLat()).append(",");
                metadata.append(radialDatasetHeader.getLon()).append(",");
                metadata.append(radialDatasetHeader.getAlt()).append(",");
                metadata.append(metadataDateFormat.format(new Date(radialDatasetHeader.getMilliseconds()))).append(",");
                metadata.append(radialDatasetHeader.getOpMode()).append(",");
                metadata.append(radialDatasetHeader.getVCP()).append(",");
                
                
                
            }
            else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
                    scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
                // Lazy object creation
                if (level3Header == null) {
                    level3Header = new DecodeL3Header();
                }
                level3Header.decodeHeader(dataURL);
                header = level3Header;

                metadata.append(dataURL.toString()).append(",");
                metadata.append(level3Header.getICAO()).append(",");
                metadata.append(level3Header.getLat()).append(",");
                metadata.append(level3Header.getLon()).append(",");
                metadata.append(level3Header.getAlt()).append(",");
                metadata.append(metadataDateFormat.format(new Date(level3Header.getMilliseconds()))).append(",");
                metadata.append(level3Header.getOpMode()).append(",");
                metadata.append(level3Header.getVCP()).append(",");
                
                CategoryLegendImageProducer legend = new CategoryLegendImageProducer();
                NexradLegendLabelFactory.setSpecialLevel3LegendLabels(legend, header, false);
                String[] specialMetadata = legend.getSpecialMetadata();
                for (String meta : specialMetadata) {
                    metadata.append(meta).append(",");
                }

                
            }
            else if (scannedFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
                // Lazy object creation
                if (xmrgHeader == null) {
                    xmrgHeader = new DecodeXMRGHeader();
                }
                xmrgHeader.decodeHeader(dataURL);
                header = xmrgHeader;

                metadata.append(dataURL.toString()).append(",");
                metadata.append(xmrgHeader.getICAO()).append(",");
                metadata.append(xmrgHeader.getLat()).append(",");
                metadata.append(xmrgHeader.getLon()).append(",");
                metadata.append(xmrgHeader.getAlt()).append(",");
                metadata.append(metadataDateFormat.format(new Date(xmrgHeader.getMilliseconds()))).append(",");
                metadata.append(xmrgHeader.getOpMode()).append(",");
                metadata.append(xmrgHeader.getVCP()).append(",");
                
                CategoryLegendImageProducer legend = new CategoryLegendImageProducer();
                NexradLegendLabelFactory.setSpecialLevel3LegendLabels(legend, header, false);
                String[] specialMetadata = legend.getSpecialMetadata();
                for (String meta : specialMetadata) {
                    metadata.append(meta).append(",");
                }

            }
            
        }

        return metadata.toString();
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



}

