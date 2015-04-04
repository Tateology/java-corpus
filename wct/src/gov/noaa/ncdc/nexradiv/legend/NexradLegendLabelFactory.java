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

package gov.noaa.ncdc.nexradiv.legend;

import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGData;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeXMRGHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.geotools.styling.StyleBuilder;

import ucar.unidata.io.InMemoryRandomAccessFile;
import ucar.unidata.io.RandomAccessFile;

/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * @created    September 23, 2004
 */
public class NexradLegendLabelFactory {

    final static DecimalFormat fmt0 = new DecimalFormat("0");
    final static DecimalFormat fmt00 = new DecimalFormat("00");
    final static DecimalFormat fmt1 = new DecimalFormat("0.0");
    final static DecimalFormat fmt2 = new DecimalFormat("0.00");
    /**
     *  Description of the Field
     */
    public final static int DMS = 1, DECI = 2;


    /**
     * 
     * @param legend
     * @param header
     * @param nexradFile
     * @param geoUnits  DMS or DECI
     */
    public static void setStandardLegendLabels(CategoryLegendImageProducer legend, NexradHeader header,
            String nexradFile, int geoUnits) {

        try {

            String fileDate, fileHour, fileMin, fileSec;
            RadarHashtables nxhash = header.getNexradHashtables();

            int x = 0;
            if (nexradFile.substring(0, 4).equals("7000") || nexradFile.substring(0, 4).equals("6500")) {
                x = 4;
            }

            
            String metadata4 = "";
            if (header.getProductType() == NexradHeader.LEVEL2) {

//                fileDate = nexradFile.substring(x + 4, x + 12);
//                fileHour = nexradFile.substring(x + 13, x + 15);
//                fileMin = nexradFile.substring(x + 15, x + 17);
//                fileSec = nexradFile.substring(x + 17, x + 19);

                fileDate = String.valueOf(header.getDate());
                fileHour = header.getHourString();
                fileMin = header.getMinuteString();
                fileSec = header.getSecondString();
                
                // SET UP METADATA PANEL
//                String icao = nexradFile.substring(x + 0, x + 4).toUpperCase();
                String icao = header.getICAO();
                String[] dataDescription = new String[] {icao};
                if (nxhash.getLocation(icao).trim().length() > 0 && nxhash.getState(icao).trim().length() > 0) {
                    dataDescription[0] += " - " + nxhash.getLocation(icao) + ", " + nxhash.getState(icao);
                }
                else if (nxhash.getLocation(icao).trim().length() > 0) {
                    dataDescription[0] += " - " + nxhash.getLocation(icao);
                }
                legend.setDataDescription(dataDescription);
                
                //metaLabel[2].setText(fileDate.substring(4, 6) + "/" + fileDate.substring(6, 8) + "/" +
                //   fileDate.substring(0, 4) + " " + fileHour + ":" + fileMin + ":"+ fileSec +" GMT");
//              String dateString = new Integer(header.getDate()).toString();
//              metaLabel[2].setText(dateString.substring(4, 6) + "/" + dateString.substring(6, 8) + "/" +
//              dateString.substring(0, 4) + " " + header.getHourString() + ":" + 
//              header.getMinuteString() + ":"+ header.getSecondString() +" GMT");
                
                legend.setDateTimeInfo(fileDate.substring(4, 6) + "/" + fileDate.substring(6, 8) + "/" +
                        fileDate.substring(0, 4) + " " + fileHour + ":" + fileMin + ":"+ fileSec +" GMT");
                
                //metaLabel[6].setText("VCP: " + header.getVCP());
            }
            else if (header.getProductType() == NexradHeader.XMRG) {
                

                //01234567890123456789012345
                //xmrg_08132004_03z_SE

                // SET UP METADATA PANEL
                String[] dataDescription = new String[] {"", ""};                    
                
                try {
                    String region = nexradFile.substring(18, 20);
                    dataDescription[1] = nxhash.getLocation(region);


                    legend.setDateTimeInfo(nexradFile.substring(5, 7) + "/" + nexradFile.substring(7, 9) + "/" +
                            nexradFile.substring(9, 13) + " " + nexradFile.substring(14, 16) + ":00:00 GMT");

                } catch (Exception e) {
//                    metaLabel[1].setText(" ");
//                    metaLabel[2].setText(" ");
                    System.err.println("UNKNOWN XMRG NAMING CONVENTION: "+nexradFile);
                }
                // don't print any of the lat/lon/elev info
//                for (int n=3; n<metaLabel.length; n++) {
//                    metaLabel[n].setText(" ");
//                }
                legend.setDataDescription(dataDescription);
                return;
            }
            else {


                //String file = nexradFile.substring(nexradFile.length()-35, nexradFile.length());
                //fileDate = nexradFile.substring(x + 19, x + 27);
                //fileHour = nexradFile.substring(x + 27, x + 29);
                //fileMin = nexradFile.substring(x + 29, x + 31);


                // SET UP METADATA PANEL
                //String icao = nexradFile.charAt(x + 0) + nexradFile.substring(x + 15, x + 18);
                String icao = ((DecodeL3Header)header).getICAO();
                String[] dataDescription = legend.getDataDescription();
                if (dataDescription == null) {
                    dataDescription = new String[] {"", ""};                    
                }
                else if (dataDescription.length == 1) {
                	dataDescription = new String[] {dataDescription[0], ""};
                }
                
                if (nxhash.getLocation(icao).trim().length() > 0 && nxhash.getState(icao).trim().length() > 0) {
                    dataDescription[1] = icao + " - " + nxhash.getLocation(icao).toUpperCase() + ", " + nxhash.getState(icao).toUpperCase();
                }
                else if (nxhash.getLocation(icao).trim().length() > 0) {
                    dataDescription[1] = icao + " - " + nxhash.getLocation(icao).toUpperCase();
                }
                else {
                    dataDescription[1] = icao;
                }
                legend.setDataDescription(dataDescription);


                
                //metaLabel[2].setText(fileDate.substring(4, 6) + "/" + fileDate.substring(6, 8) + "/" +
                //   fileDate.substring(0, 4) + " " + fileHour + ":" + fileMin +" GMT");
                String dateString = new Integer(header.getDate()).toString();
                legend.setDateTimeInfo(dateString.substring(4, 6) + "/" + dateString.substring(6, 8) + "/" +
                        dateString.substring(0, 4) + " " + header.getHourString() + ":" + 
                        header.getMinuteString() + ":"+ header.getSecondString() +" GMT");
                metadata4 = "MODE/VCP: " + header.getOpMode() + " / " + header.getVCP();
            }


            String metadata1 = "";
            String metadata2 = "";
            String metadata3 = "";
            if (geoUnits == DMS) {
                char hemi = 'N';
                double dlat = header.getLat();
                short deg = (short) dlat;
                short min = (short) ((dlat - deg) * 60.0);
                short sec = (short) ((((dlat - deg) * 60.0) - min) * 60.0);
                if (dlat < 0) {
                    hemi = 'S';
                    deg *= -1;
                    min *= -1;
                    sec *= -1;
                }
                metadata1 = "LAT: " + deg + "/" + fmt00.format(min) + "/" + fmt00.format(sec) + " " + hemi;

                hemi = 'E';
                double dlon = header.getLon();
                deg = (short) dlon;
                min = (short) ((dlon - deg) * 60.0);
                sec = (short) ((((dlon - deg) * 60.0) - min) * 60.0);
                if (dlon < 0) {
                    hemi = 'W';
                    deg *= -1;
                    min *= -1;
                    sec *= -1;
                }
                metadata2 = "LON: " + deg + "/" + fmt00.format(min) + "/" + fmt00.format(sec) + " " + hemi;

            }
            else {
                metadata1 = "LAT: " + header.getLat();
                metadata2 = "LON: " + header.getLon();
            }
            metadata3 = "ELEV: " + fmt00.format(header.getAlt()) + " FT";

            
            legend.setMainMetadata(new String[] {
               metadata1, metadata2, metadata3, metadata4     
            });
            
        } catch (Exception e) {
            System.out.println(nexradFile);
            e.printStackTrace();
        }
    }


    /**
     *  Sets the specialLevel2LegendLabels attribute of the NexradLegendLabelFactory class
     *
     * @param  metaLabel          The new specialLegendLabels value
     * @param  header             The new specialLegendLabels value
     * @param  decoder            The new specialLegendLabels value
     */
    public static void setSpecialLevel2LegendLabels(CategoryLegendImageProducer legend,
//            DecodeRadialDatasetSweepHeader header, DecodeRadialDatasetSweep decoder) {
            DecodeRadialDatasetSweepHeader header, RadialDatasetSweepRemappedRaster raster) {


        try {
            RadarHashtables nxhash = header.getNexradHashtables();            
            String state = nxhash.getState(header.getICAO());
            
//            System.out.println("ICAO: "+header.getICAO());
            
            if (state.equals("UN")) {
                legend.setDataType("RADIAL DATA");
            }
            else if (header.getICAO().matches("^[kKpPtT][a-zA-Z]{3}")){
                legend.setDataType("NEXRAD LEVEL-II");
            }
            else if (header.getICAO().matches("^[cC][a-zA-Z]{3}")){
                legend.setDataType("CANADIAN RADAR");
            }
            else {
                legend.setDataType("RADIAL DATA");
            }
            
            if (header.getICAO().equals("XXXX")) {
            	legend.getDataDescription()[0] = "";
            }
            
            String[] mainMetadata = legend.getMainMetadata();
            mainMetadata[3] = "VCP: " + header.getVCP();
            legend.setMainMetadata(mainMetadata);

                
            String[] l2Metadata = new String[3];
            l2Metadata[0] = separateCamelCase(raster.getLastDecodedVariableName()).toUpperCase();
//            l2Metadata[0] = decoder.getLastDecodedMoment();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            
//            int sweepNum = Integer.parseInt(decoder.getDecodeHints().get("startSweep").toString().trim());
            if (raster.getLastDecodedCappiHeightInMeters() == null ||
            		Double.isNaN(raster.getLastDecodedCappiHeightInMeters()[0])) { 
            	l2Metadata[1] = "ELEV ANGLE: "+fmt2.format(raster.getLastDecodedElevationAngle());
            	if (raster.getLastDecodedSweepTime() != null) {
            		SimpleDateFormat sdfHHMMSS = new SimpleDateFormat("HH:mm:ss");
            		sdfHHMMSS.setTimeZone(TimeZone.getTimeZone("GMT"));
            		System.out.println(raster.getLastDecodedSweepTime());
            		l2Metadata[2] = "SWEEP TIME: "+sdfHHMMSS.format(raster.getLastDecodedSweepTime())+" GMT";
            	}
            }
            else {
//            	System.out.println(raster.getLastDecodedCappiHeightInMeters()+" ------------------------------------------");
            	l2Metadata[1] = "CAPPI: "+fmt00.format(raster.getLastDecodedCappiHeightInMeters()[0])+" METERS";
            }
//            l2Metadata[1] = "ELEV ANGLE: "+fmt2.format(decoder.getLastDecodedCutElevation());
//            l2Metadata[2] = "SWEEP TIME: "+dateFormat.format(decoder.getSweepDateTime()[0])+" GMT";

            legend.setSpecialMetadata(l2Metadata);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     *  Sets the specialLevel2LegendLabels attribute of the NexradLegendLabelFactory class
     *
     * @param  metaLabel          The new specialLegendLabels value
     * @param  header             The new specialLegendLabels value
     * @param  decoder            The new specialLegendLabels value
     */
    public static void setSpecialXMRGLegendLabels(CategoryLegendImageProducer legend,
            DecodeXMRGHeader header, DecodeXMRGData decoder) {


        try {
            legend.setDataType("NEXRAD XMRG MPE");

            legend.setMainMetadata(new String[] {
                    "",
                    "VALID: " + header.getValidDateTime(),
                    "SAVED: " + header.getSaveDateTime(),
                    "PROCESS FLAG: " + header.getProcessFlag(),
                    "USER ID: " + header.getUserID()
            });
            
            legend.setSpecialMetadata(new String[] { "" });
            
//            metaLabel[4].setText("MAX: " + header.getMaxValue());
//            metaLabel[5].setText("VALID: " + header.getValidDateTime());
//            metaLabel[6].setText("SAVED: " + header.getSaveDateTime());
//            metaLabel[7].setText("PROCESS FLAG: " + header.getProcessFlag());
//            specialMetadata[0] = "USER ID: " + header.getUserID());




        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     *  Sets the specialLegendLabels attribute of the NexradLegendLabelFactory class
     *
     * @param  metaLabel          The new specialLegendLabels value
     * @param  header             The new specialLegendLabels value
     * @param  isAlphaBackground  The new specialLegendLabels value
     */
    public static void setSpecialLevel3LegendLabels(CategoryLegendImageProducer legend,
            NexradHeader header, boolean isAlphaBackground) {

        try {

            if (header.getICAO().startsWith("T") && ! header.getICAO().equals("TJUA")) {
                legend.setDataType("TDWR LEVEL-III");
            }
            else {
                legend.setDataType("NEXRAD LEVEL-III");
            }
            
            
            String[] dataDescription = legend.getDataDescription();
            if (dataDescription == null) {
                dataDescription = new String[] { "" };
            }
            String[] specialMetadata = new String[] {"", "", ""};
            
            int pcode = header.getProductCode();
            
            if (isAlphaBackground) {
                specialMetadata[0] = "BACKGROUND:";
            }

            if (pcode == NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE REFLECTIVITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " DBZ";
                    specialMetadata[2] = "RANGE: 48 NM";
                }
                else {
                    specialMetadata[1] = "TDWR BASE REFL. 48 NM";
                }
            }
            else if (pcode == NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY_8BIT) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE REFLECTIVITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " DBZ";
                    specialMetadata[2] = "RANGE: 48 NM";
                }
                else {
                    specialMetadata[1] = "TDWR BASE REFL. 48 NM";
                }
            }
            else if (pcode == NexradHeader.L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE REFLECTIVITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " DBZ";
                    specialMetadata[2] = "RANGE: 225 NM";
                }
                else {
                    specialMetadata[1] = "TDWR BASE REFL. 225 NM";
                }
            }
            else if (pcode == NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE VELOCITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
//                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " DBZ";
                    specialMetadata[2] = "RANGE: 48 NM";
                }
                else {
                    specialMetadata[1] = "TDWR BASE VEL. 48 NM";
                }
            }
            else if (pcode == NexradHeader.L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE REFLECTIVITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " DBZ";
                    specialMetadata[2] = "RANGE: 248 NM";
                }
                else {
                    specialMetadata[1] = "BASE REFL. 248 NM";
                }
            }
            else if (pcode == NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE VELOCITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
//                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " DBZ";
                    specialMetadata[2] = "RANGE: 248 NM";
                }
                else {
                    specialMetadata[1] = "BASE REFL. 248 NM";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_HYBRID_SCAN) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "DIG. HYB. SCAN REFLECT.";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " DBZ";
//                    specialMetadata[1] = "BIAS: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(4) / 100.0);
//                    specialMetadata[2] = "ERROR VAR: " + ((DecodeL3Header)header).getProductSpecificValue(5) / 100.0;
                }
                else {
                    specialMetadata[1] = "DIG. HYB. SCAN REFLECT.";
                }
            }
            // DSP
            else if (pcode == NexradHeader.L3PC_DIGITAL_HYBRID_PRECIP) {
                RandomAccessFile raf = new InMemoryRandomAccessFile("Data Threshold Bytes", ((DecodeL3Header)header).getDataThresholdBytes());
                raf.order(RandomAccessFile.BIG_ENDIAN);
                raf.seek(0);
                int hw31 = raf.readShort(); // halfword 31
                int hw32 = raf.readShort(); // halfword 32
                int hw33 = raf.readShort(); // halfword 32
                raf.close();
                
                if (!isAlphaBackground) {
                    
//                	System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
//                	System.out.println(Arrays.toString(((DecodeL3Header)header).getProductSpecificValueArray()));
//                	System.out.println(hw31 + " , "+hw32+" , "+hw33);
                	
                    specialMetadata = new String[4];
                    
                    dataDescription[0] = "DIG. STORM TOT. (PPS)";
//                    specialMetadata[0] = "MAX: " + fmt2.format(((DecodeL3Header)header).getProductSpecificValue(3)*hw32*0.01) + " IN";
                    specialMetadata[0] = "MAX: " +(0 + (1*0.01)*((DecodeL3Header)header).getProductSpecificValue(3)) + " IN";

                    
                    specialMetadata[1] = "BEG: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(0)) +
                        " " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) / 60) +
                        ":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) % 60) + " GMT";
                    specialMetadata[2] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                        " " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                        ":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";
//                    specialMetadata[3] = "BIAS: "+fmt2.format(((DecodeL3Header)header).getProductSpecificValue(4)*0.01) + " / " +
//                        "SAMP: "+fmt2.format(((DecodeL3Header)header).getProductSpecificValue(6)*0.01);
                    
                }
                else {
                    specialMetadata[1] = "DIG. STORM TOT. (PPS)";
                }
            }
            // DIGITAL PRECIPITATION ARRAY
            else if (pcode == 81) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "DPA (1 HR)";
                    specialMetadata[0] = "MAX: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 100.0) + " IN";
                    specialMetadata[1] = "BIAS: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(4) / 100.0);
                    specialMetadata[2] = "ERROR VAR: " + ((DecodeL3Header)header).getProductSpecificValue(5) / 100.0;
                }
                else {
                    specialMetadata[1] = "DPA (1 HR)";
                }
            }
            // BASE REFLECTIVITY
            else if (pcode == 19 || pcode == 20) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE REFLECTIVITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " dBZ";
                    if (pcode == 20) {
                        specialMetadata[2] = "RANGE 248 NM";
                    }
                    else {
                        specialMetadata[2] = " ";
                    }
                }
                else {
                    specialMetadata[1] = "BASE REFLECTIVITY";
                }
            }

            // BASE VELOCITY
            else if (pcode == 25) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE VELOCITY 32 NM";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX NEG: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KT";
                    specialMetadata[2] = "MAX POS: " + ((DecodeL3Header)header).getProductSpecificValue(4) + " KT";
                }
                else {
                    specialMetadata[1] = "BASE VELOCITY 32 NM";
                }
            }
            // BASE VELOCITY
            else if (pcode == 27) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "BASE VELOCITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX NEG: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KT";
                    specialMetadata[2] = "MAX POS: " + ((DecodeL3Header)header).getProductSpecificValue(4) + " KT";
                }
                else {
                    specialMetadata[1] = "BASE VELOCITY";
                }
            }
            // 32 nm SPECTRUM WIDTH
            else if (pcode == 28) {

                if (!isAlphaBackground) {
                    dataDescription[0] = "SPECTRUM WIDTH";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KT";
                    specialMetadata[2] = "RANGE 32 NM";
                }
                else {
                    specialMetadata[1] = "SPECTRUM WIDTH";
                }
            }
            // SPECTRUM WIDTH
            else if (pcode == 30) {

                if (!isAlphaBackground) {
                    dataDescription[0] = "SPECTRUM WIDTH";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KT";
                    specialMetadata[2] = "RANGE 124 NM";
                }
                else {
                    specialMetadata[1] = "SPECTRUM WIDTH";
                }
            }



            // CLUTTER FILTER
            else if (pcode == 34) {
                /*
            -- Old legacy description --
            Defines the clutter map channel type and segment number.  
            Bit 15 (LSB) defines the channel type.  If bit 15 is 0, 
            then it is a clutter filter control product for the 
            surveillance channel.  If bit 15 is 1, then it is the 
            Doppler channel clutter filter control product.  Bits 14 
            through 10 specify elevation segment numbers 1 through 5, 
            respectively.  Segment 1 is the lowest elevation clutter 
            filter map, segment 2 is the upper elevation clutter 
            filter map.  Segments 3 through 5 are for future expansion.
                 */         

                if (!isAlphaBackground) {
                    dataDescription[0] = "CLUTTER FILTER";

                    /*
               From 2620001J.pdf ROC ICD:
               Note 8. This halfword defines the clutter map channel type (Version 0 only) 
               and segment number (Version 0 and Version 1). For Version 0, bit 15 (LSB) 
               defines the channel type. If bit 15 is 0, then it is a clutter filter control 
               product for the surveillance channel. If bit 15 is 1, then it is the Doppler 
               channel clutter filter control product. For both Version 0 and Version 1, 
               bits 14 through 10 specify elevation segment numbers 1 through 5, respectively. 
               Segment 1 is the lowest elevation clutter filter map, segment 5 is the upper 
               elevation clutter filter map.
                     */               

                    int version = (int)((DecodeL3Header)header).getVersion(); 

                    if (version == 0) {

                        short bitMap = (short)((DecodeL3Header)header).getProductSpecificValue(0);
                        String binaryString = Integer.toBinaryString(bitMap);
                        // Add enough zeros so binaryString represents all 8 bits
                        int binaryLength = binaryString.length();
                        for (int n = 0; n < 16 - binaryLength; n++) {
                            binaryString = "0" + binaryString;
                        }
//                        System.out.println("CLUTTER FILTER INFO (BITS): 0123456789012345");
//                        System.out.println("CLUTTER FILTER INFO (BITS): "+binaryString);


                        String segment = "";
                        if (binaryString.charAt(14) == '1') {
                            segment = "1";
                        }
                        if (binaryString.charAt(13) == '1') {
                            segment = "2";
                        }
                        if (binaryString.charAt(12) == '1') {
                            segment = "3";
                        }
                        if (binaryString.charAt(11) == '1') {
                            segment = "4";
                        }
                        if (binaryString.charAt(10) == '1') {
                            segment = "5";
                        }

                        if (binaryString.charAt(15) == '1') {
                            specialMetadata[0] = "SURV. CH. - ELEV SEG. "+segment;
                        }
                        else {
                            specialMetadata[0] = "DOPPLER CH. - ELEV SEG. "+segment;
                        }

                        String yyyymmdd = NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4));
                        String hhmmss = NexradEquations.convertMinutesFormatted(((DecodeL3Header)header).getProductSpecificValue(5));
                        specialMetadata[1] = "BYP.:   "+yyyymmdd+" "+hhmmss;
                        yyyymmdd = NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(6));
                        hhmmss = NexradEquations.convertMinutesFormatted(((DecodeL3Header)header).getProductSpecificValue(7));
                        specialMetadata[2] = "NWID.: "+yyyymmdd+" "+hhmmss;
                    }
                    else {

                        // From 2620001J.pdf


                        short bitMap = (short)((DecodeL3Header)header).getProductSpecificValue(0);
                        String binaryString = Integer.toBinaryString(bitMap);
                        // Add enough zeros so binaryString represents all 8 bits
                        int binaryLength = binaryString.length();
                        for (int n = 0; n < 16 - binaryLength; n++) {
                            binaryString = "0" + binaryString;
                        }
//                        System.out.println("CLUTTER FILTER INFO (BITS): 0123456789012345");
//                        System.out.println("CLUTTER FILTER INFO (BITS): "+binaryString);


                        String segment = "";
                        if (binaryString.charAt(14) == '1') {
                            segment = "1";
                        }
                        if (binaryString.charAt(13) == '1') {
                            segment = "2";
                        }
                        if (binaryString.charAt(12) == '1') {
                            segment = "3";
                        }
                        if (binaryString.charAt(11) == '1') {
                            segment = "4";
                        }
                        if (binaryString.charAt(10) == '1') {
                            segment = "5";
                        }


                        specialMetadata[0] = "ELEV SEG. "+segment;

                        String yyyymmdd = NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4));
                        String hhmmss = NexradEquations.convertMinutesFormatted(((DecodeL3Header)header).getProductSpecificValue(5));
                        specialMetadata[1] = "BYP.:   "+yyyymmdd+" "+hhmmss;
                        yyyymmdd = NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(6));
                        hhmmss = NexradEquations.convertMinutesFormatted(((DecodeL3Header)header).getProductSpecificValue(7));
                        specialMetadata[2] = "C.F.M: "+yyyymmdd+" "+hhmmss;


                    }

                }
                else {
                    specialMetadata[1] = "CLUTTER FILTER";
                }
            }



            // 248 nm 16 level COMPOSITE REFLECTIVITY
            else if (pcode == 36) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "COMPOSITE REF. 248NM";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " dBZ";
                    specialMetadata[1] = "BOT: " + ((DecodeL3Header)header).getProductSpecificValue(4) + " KFT";
                    specialMetadata[2] = "TOP: " + ((DecodeL3Header)header).getProductSpecificValue(5) + " KFT";
                }
                else {
                    specialMetadata[1] = "COMPOSITE REF. 248NM";
                }
            }
            // 124 nm 16 level COMPOSITE REFLECTIVITY
            else if (pcode == 37) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "COMPOSITE REF. 124NM";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " dBZ";
                    specialMetadata[1] = "BOT: " + ((DecodeL3Header)header).getProductSpecificValue(4) + " KFT";
                    specialMetadata[2] = "TOP: " + ((DecodeL3Header)header).getProductSpecificValue(5) + " KFT";
                }
                else {
                    specialMetadata[1] = "COMPOSITE REF. 124NM";
                }
            }
            // 248 nm 16 level COMPOSITE REFLECTIVITY
            else if (pcode == 38) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "COMPOSITE REF. 248NM";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " dBZ";
                    specialMetadata[1] = "BOT: " + ((DecodeL3Header)header).getProductSpecificValue(4) + " KFT";
                    specialMetadata[2] = "TOP: " + ((DecodeL3Header)header).getProductSpecificValue(5) + " KFT";
                }
                else {
                    specialMetadata[1] = "COMPOSITE REF. 248NM";
                }
            }

            // VERTICAL AZIMUTH DISPLAY (VAD) WIND PROFILE
            else if (pcode == 48) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "VAD WIND PROFILE";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KT"
                            + " , " + ((DecodeL3Header)header).getProductSpecificValue(4) + " °";
                    specialMetadata[1] = "ALT: " + ((DecodeL3Header)header).getProductSpecificValue(5) * 10 + " FT";
                    specialMetadata[2] = " ";
                }
            }
            // BASE VELOCITY
            else if (pcode == 56) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "STORM REL. VELOCITY";
                    specialMetadata[0] = "ELEV ANGLE: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KT , " +
                            ((DecodeL3Header)header).getProductSpecificValue(4) + " KT";
                    specialMetadata[2] = "AVG: " + fmt1.format(((DecodeL3Header)header).getProductSpecificValue(7) / 10.0) + " KT , " +
                            fmt1.format(((DecodeL3Header)header).getProductSpecificValue(8) / 10.0) + " °";
                }
                else {
                    specialMetadata[1] = "STORM REL. VELOCITY";
                }
            }
            // VERTICALLY INTEGRATED LIQUID
            else if (pcode == 57) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "VERT. INT. LIQUID";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KG/M2";
                    specialMetadata[1] = " ";
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "VERT. INT. LIQUID";
                }
            }
            // ECHO TOPS
            else if (pcode == 41) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "ECHO TOPS";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KFT";
                    specialMetadata[1] = " ";
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "ECHO TOPS";
                }
            }

            // ONE HOUR TOTAL PRECIPITATION
            else if (pcode == 78) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "ONE HOUR PRECIP";
                    specialMetadata[0] = "MAX: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(6)) +
                            " " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(7) / 60) +
                            ":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(7) % 60);
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "ONE HOUR PRECIP";
                }
            }
            // THREE HOUR TOTAL PRECIPITATION
            else if (pcode == 79) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "THREE HOUR PRECIP";
                    specialMetadata[0] = "MAX: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(6)) +
                            " " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(7) / 60) +
                            ":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(7) % 60);
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "THREE HOUR PRECIP";
                }
            }
            // STORM TOTAL PRECIPITATION
            else if (pcode == 80) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "STORM TOTAL PRECIP";
                    specialMetadata[0] = "MAX: " + fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "BEG: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                            " " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                            ":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";
                    specialMetadata[2] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(6)) +
                            " " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(7) / 60) +
                            ":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(7) % 60) + " GMT";
                }
                else {
                    specialMetadata[1] = "STORM TOTAL PRECIP";
                }
            }
            // LOW LAYER COMPOSITE REFLECTIVITY
            else if (pcode == 65) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "LOW LAYER COMP. REF.";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " dBZ";
                    specialMetadata[1] = "BOT: " + ((DecodeL3Header)header).getProductSpecificValue(4) + " KFT";
                    specialMetadata[2] = "TOP: " + ((DecodeL3Header)header).getProductSpecificValue(5) + " KFT";
                }
                else {
                    specialMetadata[1] = "LOW LAYER COMP. REF.";
                }
            }
            // MID LAYER COMPOSITE REFLECTIVITY
            else if (pcode == 66) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "MID LAYER COMP. REF.";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " dBZ";
                    specialMetadata[1] = "BOT: " + ((DecodeL3Header)header).getProductSpecificValue(4) + " KFT";
                    specialMetadata[2] = "TOP: " + ((DecodeL3Header)header).getProductSpecificValue(5) + " KFT";
                }
                else {
                    specialMetadata[1] = "MID LAYER COMP. REF.";
                }
            }
            // HIGH LAYER COMPOSITE REFLECTIVITY
            else if (pcode == 90) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "HIGH LAYER COMP. REF.";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " dBZ";
                    specialMetadata[1] = "BOT: " + ((DecodeL3Header)header).getProductSpecificValue(4) + " KFT";
                    specialMetadata[2] = "TOP: " + ((DecodeL3Header)header).getProductSpecificValue(5) + " KFT";
                }
                else {
                    specialMetadata[1] = "HIGH LAYER COMP. REF.";
                }
            }
            // ECHO TOPS
            else if (pcode == NexradHeader.L3PC_ENHANCED_ECHO_TOPS) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "ENHANCED ECHO TOPS";
                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KFT";
                    specialMetadata[1] = " ";
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "ENHANCED ECHO TOPS";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_VERT_INT_LIQUID) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "DIG. VERT. INT. LIQUID";
//                    specialMetadata[0] = "MAX: " + ((DecodeL3Header)header).getProductSpecificValue(3) + " KG/M2";
                    specialMetadata[0] = " ";
                    specialMetadata[1] = " ";
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "DIG. VERT. INT. LIQUID";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "DIFFERENTIAL REFLECTIVITY";
                    specialMetadata[0] = "ELEV ANGLE: " + 
                    	fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MIN/MAX: " + 
                    	fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " , " +
                    	fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(4) / 10.0) + " dB";
                    specialMetadata[2] = "RANGE: 162 NM";
                }
                else {
                    specialMetadata[1] = "DIFFERENTIAL REFLECTIVITY";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "CORRELATION COEFFICIENT";
                    specialMetadata[0] = "ELEV ANGLE: " + 
                    	fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MIN/MAX: " + 
                    	fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) * 0.00333) + " , " +
                    	fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(4) * 0.00333) + " ";
                    specialMetadata[2] = "RANGE: 162 NM";
                }
                else {
                    specialMetadata[1] = "CORRELATION COEFFICIENT";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "SPECIFIC DIFF. PHASE";
                    specialMetadata[0] = "ELEV ANGLE: " + 
                		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = "MIN/MAX: " + 
                		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) * 0.05) + " , " +
                		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(4) * 0.05) + " DEG/KM";
                    specialMetadata[2] = "RANGE: 162 NM";
                }
                else {
                    specialMetadata[1] = "SPECIFIC DIFF. PHASE";
                }
            }
            else if (pcode == NexradHeader.L3PC_ONE_HOUR_ACCUMULATION) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "ONE HOUR PRECIP (D.P.)";
                    specialMetadata[0] = "MAX: " + 
                		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                    	" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                    	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";                    
                    specialMetadata[2] = "MEAN-FIELD BIAS: " + 
            			fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(6) / 100.0);
                }
                else {
                    specialMetadata[1] = "ONE HOUR PRECIP (D.P.)";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "DIG. ONE HOUR PCP. (D.P.)";
                    specialMetadata[0] = "MAX: " + 
                		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                    	" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                    	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";                 
                    specialMetadata[2] = "MEAN-FIELD BIAS: " + 
            			fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(6) / 100.0);
                }
                else {
                    specialMetadata[1] = "DIG. ONE HOUR PCP. (D.P.)";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "HYDROMETEOR CLASS.";
                    specialMetadata[0] = "ELEV ANGLE: " + 
                		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(2) / 10.0) + " °";
                    specialMetadata[1] = " ";
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "HYDROMETEOR CLASS.";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "HYBRID HYDROMET. CLASS.";
                    specialMetadata[0] = " ";
                    specialMetadata[1] = " ";
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "HYBRID HYDROMET. CLASS.";
                }
            }
            else if (pcode == NexradHeader.L3PC_STORM_TOTAL_ACCUMULATION) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "STORM TOTAL (D.P.)";
                    specialMetadata[0] = "MAX: " + 
            		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "BEG: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(0)) +
                		" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) / 60) +
                		":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) % 60) + " GMT";
                    specialMetadata[2] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                		" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                		":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";
                }
                else {
                    specialMetadata[1] = "STORM TOTAL (D.P.)";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "DIG. STORM TOT. (D.P.)";
                    specialMetadata[0] = "MAX: " + 
            		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "BEG: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(0)) +
                    	" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) / 60) +
                    	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) % 60) + " GMT";
                    specialMetadata[2] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                    	" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                    	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";
                }
                else {
                    specialMetadata[1] = "DIG. STORM TOT. (D.P.)";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "DIG. ONE HR PCP DIFF (D.P.)";
                    specialMetadata[0] = "MIN DIFF: " + 
            		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(6) / 10.0) + " IN";
                    specialMetadata[1] = "MAX DIFF: " + 
            		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[2] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                    	" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                    	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";
                }
                else {
                    specialMetadata[1] = "DIG. ONE HR PCP DIFF (D.P.)";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "DIG. STORM TOT. DIFF (D.P.)";
                    specialMetadata[0] = "MIN/MAX: " + 
            		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(6) / 10.0) + " / "+
            		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "BEG: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(0)) +
                	" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) / 60) +
                	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) % 60) + " GMT";
                    specialMetadata[2] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                	" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";
                }
                else {
                    specialMetadata[1] = "DIG. STORM TOT. DIFF (D.P.)";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_USER_SELECTABLE_ACCUMULATION) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "USER SELECT. ACCUM. (D.P.)";
                    specialMetadata[0] = "MAX: " + 
            		fmt2.format((double) ((DecodeL3Header)header).getProductSpecificValue(3) / 10.0) + " IN";
                    specialMetadata[1] = "ACCUM. HRS: " + 
                    	fmt0.format(((DecodeL3Header)header).getProductSpecificValue(1) / 60) +
                    	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(1) % 60);
                    specialMetadata[2] = "END: " + NexradEquations.convertJulianDateFormatted(((DecodeL3Header)header).getProductSpecificValue(4)) +
                    	" " + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) / 60) +
                    	":" + fmt00.format(((DecodeL3Header)header).getProductSpecificValue(5) % 60) + " GMT";
                }
                else {
                    specialMetadata[1] = "USER SELECT. ACCUM. (D.P.)";
                }
            }
            else if (pcode == NexradHeader.L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE) {
                if (!isAlphaBackground) {
                    dataDescription[0] = "INSTANTANEOUS PRECIP. RATE";
                    
                    
//                    short s = ((DecodeL3Header)header).getProductSpecificValue(3);
//                    byte[] bytes = new byte[2];
//                    bytes[0] = (byte)(s & 0xff);
//                    bytes[1] = (byte)((s >> 8) & 0xff);                   
//                    char us = (char) (bytes[0] << 8 | bytes[1]);
//                    int firstByte = (0x000000FF & ((int)bytes[1]));
//                    int secondByte = (0x000000FF & ((int)bytes[0]));
//                    char us = (char) (firstByte << 8 | secondByte);
                    
                    
//                    ByteBuffer myBuffer = ByteBuffer.allocate(4);
//                    myBuffer.putChar((char) ((DecodeL3Header)header).getProductSpecificValue(3));
                    specialMetadata[0] = "MAX: " + 
            		fmt2.format((double) (((DecodeL3Header)header).getProductSpecificValue(3))  / 1000.0) + " IN/HR";
//                    fmt2.format((double) us / 100.0) + " IN/HR";
                    specialMetadata[1] = " "; 
                    specialMetadata[2] = " ";
                  
                }
                else {
                    specialMetadata[1] = "INSTANTANEOUS PRECIP. RATE";
                }
            }
            else {
                if (!isAlphaBackground) {
                    dataDescription[0] = "UNREGISTERED LEGEND";
                    specialMetadata[0] = "PRODUCT CODE: " + pcode;
                    specialMetadata[1] = " ";
                    specialMetadata[2] = " ";
                }
                else {
                    specialMetadata[1] = "UNREGISTERED LEGEND";
                }
            }


//          dataDescription[0] = metaLabel[0].getText()+" "+pcode);         

            legend.setDataDescription(dataDescription);
            legend.setSpecialMetadata(specialMetadata);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     *  Sets the alphaLegendLabels attribute of the NexradLegendLabelFactory class
     *
     * @param  metaLabel      The new alphaLegendLabels value
     * @param  header         The new alphaLegendLabels value
     * @param  alpha_decoder  The new alphaLegendLabels value
     * @return                Description of the Return Value
     */
    public static String setAlphaLegendLabels(CategoryLegendImageProducer legend,
            DecodeL3Header header, DecodeL3Alpha alpha_decoder) {

        int pcode = header.getProductCode();

        try {
            
            legend.setDataType("NEXRAD LEVEL-III");
            String[] specialMetadata = legend.getSpecialMetadata();
            if (specialMetadata == null) {
                specialMetadata = new String[4];
            }

            specialMetadata[0] = alpha_decoder.getMetaLabel(0);
            specialMetadata[1] = alpha_decoder.getMetaLabel(1);
            specialMetadata[2] = alpha_decoder.getMetaLabel(2);

            legend.setSpecialMetadata(specialMetadata);

            
//            String[] dataDescription = legend.getDataDescription();
            String[] dataDescription = new String[1];
            
            if (pcode == 58) {
                dataDescription[0] = "STORM TRACKING";
                legend.setDataDescription(dataDescription);
                return StyleBuilder.MARK_CROSS;
            }
            else if (pcode == 59) {
                dataDescription[0] = "HAIL INDEX";
                legend.setDataDescription(dataDescription);
                return StyleBuilder.MARK_TRIANGLE;
            }
            else if (pcode == 60) {
                dataDescription[0] = "MESOCYCLONE";
                legend.setDataDescription(dataDescription);
                return StyleBuilder.MARK_CIRCLE;
            }
            else if (pcode == 61) {
                dataDescription[0] = "TORNADO VTX. SIG.";
                legend.setDataDescription(dataDescription);
                return StyleBuilder.MARK_CIRCLE;
            }
            else if (pcode == 62) {
                dataDescription[0] = "STORM STRUCTURE";
                legend.setDataDescription(dataDescription);
                return StyleBuilder.MARK_CIRCLE;
            }
            else if (pcode == 141) {
                dataDescription[0] = "MESOCYCLONE (MDA)";
                legend.setDataDescription(dataDescription);
                return StyleBuilder.MARK_CIRCLE;
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }

        return StyleBuilder.MARK_CIRCLE;
    }


    
    
    
    private static String separateCamelCase(String str) {
//    	String[] words = str.split("[a-z][A-Z]");
    	String[] words = str.split("(?<!^)(?=[A-Z])");
    	StringBuilder sb = new StringBuilder();
    	for (String word : words) {
    		sb.append(word);
    		sb.append(" ");
    	}
    	return sb.toString().trim();
    }
    
}

