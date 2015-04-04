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

package gov.noaa.ncdc.nexradexport;

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.nexradiv.legend.NexradLegendLabelFactory;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeHail;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeMeso;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormStructure;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormTracking;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeTVS;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;

import java.awt.Component;
import java.net.URL;
import java.util.Formatter;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

/**
 *  Description of the Class
 *
 * @author    steve.ansari
 */
public class NexradMetaDataExtract {

    private FileScanner dataFile;
    private DecodeRadialDatasetSweepHeader radialDatasetHeader;
    private DecodeL3Header level3Header;
    private DecodeXMRGHeader xmrgHeader;

    private Component parent;


    /**
     *  Description of the Method
     *
     * @param  nexrad_url  Description of the Parameter
     */
    public void showDescribeNexradDialog(URL nexrad_url, Component parent) {

        String message = describeNexrad(nexrad_url);
        if (message != null) {
            JOptionPane.showMessageDialog(parent, message,
                    "NEXRAD File Description", JOptionPane.INFORMATION_MESSAGE);
        }

    }


    /**
     * Create JOptionPane that displays Nexrad Header Information
     *
     * @param  nexrad_url  Description of the Parameter
     * @return             Description of the Return Value
     */
    private String describeNexrad(URL nexrad_url) {

        try {

        	if (dataFile == null) {
        		dataFile = new FileScanner();
        	}
        	
            CategoryLegendImageProducer legend = new CategoryLegendImageProducer();
            
            dataFile.scanURL(nexrad_url);

            JLabel[] metaLabel = new JLabel[16];
            for (int i = 0; i < metaLabel.length; i++) {
                metaLabel[i] = new JLabel();
            }

            // Get header
            if (dataFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
                
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

                // Lazy object creation
                RadialDatasetSweep radialDataset = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
                        FeatureType.RADIAL, 
                        nexrad_url.toString(), emptyCancelTask, new Formatter());

                if (radialDatasetHeader == null) {
                    radialDatasetHeader = new DecodeRadialDatasetSweepHeader();
                }
                radialDatasetHeader.setRadialDatasetSweep(radialDataset);
            }
            else if (dataFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
                    dataFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
                // Lazy object creation
                if (level3Header == null) {
                    level3Header = new DecodeL3Header();
                }
                level3Header.decodeHeader(nexrad_url);
            }
            else if (dataFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_XMRG) {
                // Lazy object creation
                if (xmrgHeader == null) {
                    xmrgHeader = new DecodeXMRGHeader();
                }
                xmrgHeader.decodeHeader(nexrad_url);
            }
            else if (dataFile.getLastScanResult().getDataType() == SupportedDataType.UNKNOWN) {
                JOptionPane.showMessageDialog(null, "This Level-III NEXRAD Product is not supported with the Weather and Climate Toolkit.",
                        "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            else {
                JOptionPane.showMessageDialog(null, "This is not WSR-88D Level-II, Level-III or XMRG NEXRAD Data.",
                        "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            String urlString = nexrad_url.toString();
            int index = urlString.lastIndexOf('/');
            String nexradFile = urlString.substring(index + 1, urlString.length());
            String message = "";
            // Check for unsupported level-3 product and return if unknown
            if (dataFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3 ||
                    dataFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
                if (level3Header.getProductType() == NexradHeader.UNKNOWN) {
                    JOptionPane.showMessageDialog(parent, "This product (code=" + level3Header.getProductCode() + ") is not yet supported!",
                            "NEXRAD LOADING ERROR", JOptionPane.ERROR_MESSAGE);
                    return null;
                }

                if (level3Header.getProductType() == NexradHeader.L3GSM) {

                    java.util.HashMap gsmHashMap = level3Header.getGsmHashMap();

                    message = "<html><b>NEXRAD Level-III File</b></html>\n\n" +
                    "File: " + dataFile.getLastScanResult().getDisplayName() + "\n\n";

                    message += "LEVEL-III PRODUCT VERSION: "+gsmHashMap.get("buildVersion")+"\n";         
                    message += "MODE: "+gsmHashMap.get("opMode")+"\n";
                    message += "VCP: "+gsmHashMap.get("vcp")+"\n";
                    message += "RDA OP. STATUS: "+gsmHashMap.get("rdaOpStatus")+"\n";
                    message += "RDA STATUS: "+gsmHashMap.get("rdaStatus")+"\n";
                    message += "RDA ALARMS: "+gsmHashMap.get("rdaAlarms")+"\n";
                    message += "RPG OP. STATUS: "+gsmHashMap.get("rpgOpStatus")+"\n";
                    message += "RPG STATUS: "+gsmHashMap.get("rpgStatus")+"\n";
                    message += "RPG ALARMS: "+gsmHashMap.get("rpgAlarms")+"\n\n";

                    return message;

                }
                else if (level3Header.getProductType() == NexradHeader.L3ALPHA) {
                    DecodeL3Alpha alpha_decoder = null;
                    int pcode = level3Header.getProductCode();
                    if (pcode == 58) {
                        alpha_decoder = new DecodeStormTracking(level3Header);
                    }
                    else if (pcode == 59) {
                        alpha_decoder = new DecodeHail(level3Header);
                    }
                    else if (pcode == 60) {
                        alpha_decoder = new DecodeMeso(level3Header);
                    }
                    else if (pcode == 61) {
                        alpha_decoder = new DecodeTVS(level3Header);
                    }
                    else if (pcode == 62) {
                        alpha_decoder = new DecodeStormStructure(level3Header);
                    }

                    NexradLegendLabelFactory.setAlphaLegendLabels(legend, level3Header, alpha_decoder);
                }
                else {
                    NexradLegendLabelFactory.setSpecialLevel3LegendLabels(legend, level3Header, false);
                }
                metaLabel[0].setText(legend.getDataDescription()[0]+" ("+level3Header.getProductCode()+")");         

                NexradLegendLabelFactory.setStandardLegendLabels(legend, level3Header, nexradFile, NexradLegendLabelFactory.DMS);
                message = "<html><b>NEXRAD Level-III File</b></html>\n\n" +
                "File: " + dataFile.getLastScanResult().getDisplayName() + "\n\n";

                message += "LEVEL-III PRODUCT VERSION: "+level3Header.getVersion()+"\n";         


            }
            // END if (LEVEL-3 FILE)
            // LEVEL-2 FILE:
            else {
                NexradLegendLabelFactory.setStandardLegendLabels(legend, radialDatasetHeader, nexradFile, NexradLegendLabelFactory.DMS);
                message = "<html><b>NEXRAD Level-II File</b></html>\n\n" +
                "File: " + dataFile.getLastScanResult().getDisplayName() + "\n\n";
            }

            for (int i = 0; i < metaLabel.length - 4; i++) {
                message += metaLabel[i].getText() + "\n";
            }



            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }






}

