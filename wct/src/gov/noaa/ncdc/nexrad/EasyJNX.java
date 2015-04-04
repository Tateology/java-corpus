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

package gov.noaa.ncdc.nexrad;

import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeHail;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeMeso;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormStructure;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormTracking;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeTVS;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGData;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.ui.WCTUiInterface;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public class EasyJNX implements WCTUiInterface {

   private FileScanner nexradFile;
   private NexradHeader header;
   private StreamingRadialDecoder decoder;         
   private DecodeRadialDatasetSweepHeader radialDatasetHeader;
   private DecodeL3Header level3Header;
   private DecodeXMRGHeader xmrgHeader;
   private DecodeRadialDatasetSweep radialDatasetDecoder;
   private DecodeL3Nexrad level3Decoder;
   private DecodeL3Alpha alphaDecoder;
   private DecodeXMRGData xmrgDecoder;
   
   
   public EasyJNX() {
   }
   
   
   /**
    * Set up decoder for NEXRAD or XMRG data at provided URL.  Only the Level-III Alphanumeric
    * products will be automatically decoded.  All other products must have the "decodeData()"
    * called to actually decode the data.   
 * @throws IOException 
 * @throws SQLException 
 * @throws ParserConfigurationException 
 * @throws SAXException 
 * @throws XPathExpressionException 
 * @throws NumberFormatException 
 * @throws ParseException 
    */
   public StreamingRadialDecoder getDecoder(URL nexradURL) 
      throws DecodeException, IOException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException, ParseException {
         
      nexradFile = new FileScanner();
      nexradFile.scanURL(nexradURL);

      if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
            // Lazy object creation
            if (radialDatasetHeader == null) {
                radialDatasetHeader = new DecodeRadialDatasetSweepHeader();
            }
            radialDatasetHeader.decodeHeader(nexradURL);
            header = radialDatasetHeader;
         }
         else if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
            nexradFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
            // Lazy object creation
            if (level3Header == null) {
               level3Header = new DecodeL3Header();
            }
            level3Header.decodeHeader(nexradURL);
            header = level3Header;
         }
         else if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
            // Lazy object creation
            if (xmrgHeader == null) {
               xmrgHeader = new DecodeXMRGHeader();
            }
            xmrgHeader.decodeHeader(nexradURL);
            header = xmrgHeader;
         }
         else if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {
            throw new DecodeException("This data type is not supported with the Weather and Climate Toolkit.", nexradURL);
         }
         else {
            throw new DecodeException("This is not WSR-88D Level-II, Level-III or XMRG NEXRAD Data", nexradURL);
         }
    
         // Decode Data -----------------------------------------------------------
         
         int pcode = header.getProductCode();
         if (header.getProductType() == NexradHeader.LEVEL2) {
            // initiate lazy object creation
            if (radialDatasetDecoder == null) {
                radialDatasetDecoder = new DecodeRadialDatasetSweep(radialDatasetHeader);
            }
            decoder = radialDatasetDecoder;
         }
         else if (header.getProductType() == NexradHeader.L3RADIAL || 
            header.getProductType() == NexradHeader.L3RASTER ||
            header.getProductType() == NexradHeader.L3DPA ) {
               
            // initiate lazy object creation
            if (level3Decoder == null) {
               level3Decoder = new DecodeL3Nexrad(level3Header);
            }
            decoder = level3Decoder;
         }
         else if (header.getProductType() == NexradHeader.L3ALPHA) {
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
            else {
               throw new DecodeException("This product (code=" + pcode + ") is not yet supported!", nexradURL);
            }
            decoder = alphaDecoder;
         }
         
      return decoder;
   }
   

   
   
   
   
   
   
   
   
   public NexradHeader getMostRecentHeader() {
      return header;
   }
   public StreamingRadialDecoder getMostRecentDecoder() {
      return decoder;
   }
   
   /**
    *  Implementation of NexradInterface
    */
   public FileScanner getFileScanner() {
      return nexradFile;
   }
   /**
    *  Implementation of NexradInterface
    */
   public DecodeRadialDatasetSweepHeader getRadialDatasetHeader() {
      return radialDatasetHeader;
   }
   /**
    *  Implementation of NexradInterface
    */
   public DecodeL3Header getLevel3Header() {
      return level3Header;
   }
   /**
    *  Implementation of NexradInterface
    */
   public DecodeXMRGHeader getXMRGHeader() {
      return xmrgHeader;
   }
   /**
    *  Implementation of NexradInterface
    */
   public DecodeRadialDatasetSweep getRadialDatasetDecoder() {
      return radialDatasetDecoder;
   }
   /**
    *  Implementation of NexradInterface
    */
   public DecodeL3Nexrad getLevel3Decoder() {
      return level3Decoder;
   }
   /**
    *  Implementation of NexradInterface
    */
   public DecodeXMRGData getXMRGDecoder() {
      return xmrgDecoder;
   }
    
   /**
    *  Implementation of NexradInterface (not yet Implemented)
    *
    * @return  null at the moment
    */
   public WCTFilter getFilter() { 
      //return nxfilter;
      return null;
   }

   /**
    *  Implementation of NexradInterface (not yet Implemented)
    *
    * @return  null at the moment
    */
   public gov.noaa.ncdc.wct.ui.filter.WCTFilterGUI getFilterGUI() { 
      //return nxfilterGUI;
      return null;
   }

   
    /**
     * Define implementation of NexradInterface
     */    
    public int getWCTType() {
       return WCTUiInterface.EXPORT;
    }


    public DecodeRadialDatasetSweep getRadialDecoder() {
        // TODO Auto-generated method stub
        return null;
    }


    public DecodeRadialDatasetSweepHeader getRadialHeader() {
        // TODO Auto-generated method stub
        return null;
    }
    
   
   

}
