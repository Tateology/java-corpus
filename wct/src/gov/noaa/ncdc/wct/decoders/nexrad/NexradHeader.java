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

package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;

import java.net.URL;

import com.vividsolutions.jts.geom.Coordinate;

/**
 *  Description of the Interface
 *
 * @author     steve.ansari
 * @created    October 22, 2004
 */
public interface NexradHeader {

   /**
    *  Used when for lat/lon/alt values when product is
    *  not data from a single NEXRAD site, such as
    *  Stage-III regional mosaic.
    */
   public final static int NO_SITE_DEFINED = -999999;
   
   /**
    *  XMRG Stage-III / MPE Data (Product Type)
    */
   public final static int XMRG = -50;

   
   /**
    *  General Radial Data (Product Type)
    */
   public final static int GENERIC_RADIAL = -200;

   /**
    *  General Level-2 Data (Product Type)
    */
   public final static int LEVEL2 = -20;
   /**
    *  Reflectivity moment of Level-2 Data <br>
    *  Returned as product code 
    */
   public final static int LEVEL2_REFLECTIVITY = -21;
   /**
    *  Velocity moment of Level-2 Data <br>
    *  Returned as product code 
    */
   public final static int LEVEL2_VELOCITY = -22;
   /**
    *  Spectrum Width moment of Level-2 Data <br>
    *  Returned as product code 
    */
   public final static int LEVEL2_SPECTRUMWIDTH = -23;

   
   public final static int LEVEL2_DIFFERENTIALREFLECTIVITY = -24;
   public final static int LEVEL2_CORRELATIONCOEFFICIENT = -25;
   public final static int LEVEL2_DIFFERENTIALPHASE = -26;
   
   // General subtypes
   /**
    *  General Level-3 Radial Data (Any radial product)  (Product Type)
    */
   public final static int L3RADIAL = -31;
   /**
    *  General Level-3 Raster Data (Any raster product)  (Product Type)
    */
   public final static int L3RASTER = -32;
   /**
    *  General Level-3 Alphanumeric Data (Product Type)
    */
   public final static int L3ALPHA = -33;
   /**
    *  General Level-3 DPA Data (Product Type)
    */
   public final static int L3DPA = -34;
   /**
    *  General Level-3 VAD Data  (Product Type)
    */
   public final static int L3VAD = -35;

   /**
    *  General Level-3 GSM Data  (Product Type)
    */
   public final static int L3GSM = -36;
   
   /**
    *  General Level-3 RSL Data  (Radar Status Log a.k.a Archive Status Product)
    */
   public final static int L3RSL = -37;
   
   /**
    *  General Level-3 Radial Data (Any radial product)  (Product Type)
    */
   public final static int L3RADIAL_8BIT = -38;
   
   /**
    * General Level-3 Generic Radial Data (using XDR)  (Product Type)
    */
//   public final static int L3GENERIC_RADIAL = -39;
   
   
   
   
   
   // Actual Level-3 Product Codes (L3PC)


   /**
    *  Actual Level-3 GSM Product Code <br>
    *  AWIPS HEADER CODE: GSM <br>
    *  PRODUCT CODE: 2
    */
   public final static int L3PC_GSM = 2;

   /**
    *  Actual Level-3 DPA Product Code <br>
    *  AWIPS HEADER CODE: DPA <br>
    *  PRODUCT CODE: 81
    */
   public final static int L3PC_DPA = 81;
   
   /**
    *  Actual Level-3 Base Radial Velocity 32 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: N0W <br>
    *  PRODUCT CODE: 25
    */
   public final static int L3PC_VELOCITY_32NM = 25;
   
   /**
    *  Actual Level-3 Base Radial Velocity 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: N*V <br>
    *  PRODUCT CODE: 27
    */
   public final static int L3PC_VELOCITY_124NM = 27;
   
   /**
    *  Actual Level-3 Storm Relative Mean Radial Velocity 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: N*S <br>
    *  PRODUCT CODE: 56
    */
   public final static int L3PC_STORM_RELATIVE_VELOCITY_124NM = 56;
   
   /**
    *  Actual Level-3 Spectrum Width 32 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: NSP <br>
    *  PRODUCT CODE: 28
    */
   public final static int L3PC_SPECTRUM_WIDTH_32NM = 28;
   
   /**
    *  Actual Level-3 Spectrum Width 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: NSW <br>
    *  PRODUCT CODE: 30
    */
   public final static int L3PC_SPECTRUM_WIDTH_124NM = 30;
   
   /**
    *  Actual Level-3 One Hour Precipitation Total 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: N1P <br>
    *  PRODUCT CODE: 78
    */
   public final static int L3PC_ONE_HOUR_PRECIP = 78;
   
   /**
    *  Actual Level-3 Three Hour Precipitation Total 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: N3P <br>
    *  PRODUCT CODE: 79
    */
   public final static int L3PC_THREE_HOUR_PRECIP = 79;
   
   /**
    *  Actual Level-3 Storm Total Precipitation Total 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: NTP <br>
    *  PRODUCT CODE: 80 
    */
   public final static int L3PC_STORM_TOTAL_PRECIP = 80;
   
   /**
    *  Actual Level-3 Clutter Filter Control Product Code <br>
    *  AWIPS HEADER CODE: NCF <br>
    *  PRODUCT CODE: 34
    */
   public final static int L3PC_CLUTTER_FILTER_CONTROL = 34;

   /**
    *  Actual Level-3 Composite Reflectivity 248 nmi Range 8 Levels Product Code <br>
    *  AWIPS HEADER CODE: NCO <br>
    *  PRODUCT CODE: 36
    */
   public final static int L3PC_COMPOSITE_REFLECTIVITY_248NM_8LVL = 36;

   /**
    *  Actual Level-3 Composite Reflectivity 124 nmi Range 16 Levels Product Code <br>
    *  AWIPS HEADER CODE: NCR <br>
    *  PRODUCT CODE: 37
    */
   public final static int L3PC_COMPOSITE_REFLECTIVITY_124NM_16LVL = 37;

   /**
    *  Actual Level-3 Composite Reflectivity 248 nmi Range 16 Level Product Code <br>
    *  AWIPS HEADER CODE: NCZ <br>
    *  PRODUCT CODE: 38
    */
   public final static int L3PC_COMPOSITE_REFLECTIVITY_248NM_16LVL = 38;
   
   /**
    *  Actual Level-3 Vertical Wind Profile (VAD) Product Code <br>
    *  AWIPS HEADER CODE: NVW <br>
    *  PRODUCT CODE: 48
    */
   public final static int L3PC_VERTICAL_WIND_PROFILE = 48;
   
   /**
    *  Actual Level-3 Low-Layer Composite Reflectivity 124 nmi Range 16 Level Product Code <br>
    *  AWIPS HEADER CODE: NLL <br>
    *  PRODUCT CODE: 65
    */
   public final static int L3PC_LOW_LAYER_COMP_REFLECTIVITY = 65;

   /**
    *  Actual Level-3 Mid-Layer Composite Reflectivity 124 nmi Range 16 Level Product Code <br>
    *  AWIPS HEADER CODE: NML <br>
    *  PRODUCT CODE: 66
    */
   public final static int L3PC_MID_LAYER_COMP_REFLECTIVITY = 66;

   /**
    *  Actual Level-3 High-Layer Composite Reflectivity 124 nmi Range 16 Level Product Code <br>
    *  AWIPS HEADER CODE: NHL <br>
    *  PRODUCT CODE: 90
    */
   public final static int L3PC_HIGH_LAYER_COMP_REFLECTIVITY = 90;
   
   /**
    *  Actual Level-3 Vertically Integrated Liquid 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: NVL <br>
    *  PRODUCT CODE: 57
    */
   public final static int L3PC_VERT_INT_LIQUID = 57;
   
   /**
    *  Actual Level-3 Echo Tops 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: NET <br>
    *  PRODUCT CODE: 41
    */
   public final static int L3PC_ECHO_TOPS = 41;

   /**
    *  Actual Level-3 Base Reflectivity 124 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: N*R <br>
    *  PRODUCT CODE: 19
    */
   public final static int L3PC_BASE_REFLECTIVITY_124NM = 19;

   /**
    *  Actual Level-3 Base Reflectivity 248 nmi Range Product Code <br>
    *  AWIPS HEADER CODE: N0Z <br>
    *  PRODUCT CODE: 20
    */
   public final static int L3PC_BASE_REFLECTIVITY_248NM = 20;

   /**
    *  Actual Level-3 Archive Status Product / Radar Status Log Product Code <br>
    *  AWIPS HEADER CODE: RSL <br>
    *  PRODUCT CODE: 152
    */
   public final static int L3PC_RADAR_STATUS_LOG = 152;
   
   /**
    *  Actual Level-3 Mesocyclone Detection Algorithm Product Code <br>
    *  AWIPS HEADER CODE: NMD <br>
    *  PRODUCT CODE: 141
    */
   public final static int L3PC_DIGITAL_MESOCYCLONE = 141;
   
   /**
    *  Actual Level-3 Digital Hybrid Scan Reflectivity Product Code <br>
    *  AWIPS HEADER CODE: DHR <br>
    *  PRODUCT CODE: 32
    */
   public final static int L3PC_DIGITAL_HYBRID_SCAN = 32;

   
   /**
    *  Actual Level-3 8-bit Reflectivity Product Code <br>
    *  AWIPS HEADER CODE: N0Q,NAQ,N1Q,NBQ,N2Q,N3Q <br>
    *  SDUS5I N0Q  BASE REFLECTIVITY 94/DR 256LVL 0.5DEG  DS.P94R0 <br>
    *  SDUS5I NAQ  BASE REFLECTIVITY 94/DR 256LVL 0.9DEG  DS.P94RA <br>
    *  SDUS2I N1Q  BASE REFLECTIVITY 94/DR 256LVL 1.5DEG  DS.P94R1 <br>
    *  SDUS2I NBQ  BASE REFLECTIVITY 94/DR 256LVL 1.8DEG  DS.P94RB <br>
    *  SDUS2I N2Q  BASE REFLECTIVITY 94/DR 256LVL 2.4DEG  DS.P94R2 <br>
    *  SDUS2I N3Q  BASE REFLECTIVITY 94/DR 256LVL 3.4DEG  DS.P94R3 <br>
    *  PRODUCT CODE: 94
    */
   public final static int L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT = 94;

   
   /**
    *  Actual Level-3 8-bit Velocity Product Code <br>
    *  AWIPS HEADER CODE: N0U,NAU,N1U,NBU,N2U,N3U <br>
    *  SDUS5I N0U  BASE VELOCITY     99/DV 256LVL 0.5DEG  DS.P99V0 <br>
    *  SDUS5I NAU  BASE VELOCITY     99/DV 256LVL 0.9DEG  DS.P99VA <br>
    *  SDUS2I N1U  BASE VELOCITY     99/DV 256LVL 1.5DEG  DS.P99V1 <br>
    *  SDUS2I NBU  BASE VELOCITY 99/DV 256LVL 1.8DEG      DS.P99VB <br>
    *  SDUS2I N2U  BASE VELOCITY 99/DV 256LVL 2.4DEG      DS.P99V2 <br>
    *  SDUS2I N3U  BASE VELOCITY 99/DV 256LVL 3.4DEG      DS.P99V3 <br>
    *  PRODUCT CODE: 99
    */
   public final static int L3PC_LONG_RANGE_BASE_VELOCITY_8BIT = 99;

   
   /**
    *  Actual Level-3 Digital Vertically Integrated Liquid Product Code <br>
    *  AWIPS HEADER CODE: DVL <br>
    *  PRODUCT CODE: 134
    */
   public final static int L3PC_DIGITAL_VERT_INT_LIQUID = 134;

   
   /**
    *  Actual Level-3 8-bit Enhanced Echo Tops Product Code <br>
    *  AWIPS HEADER CODE: EET <br>
    *  PRODUCT CODE: 135
    */
   public final static int L3PC_ENHANCED_ECHO_TOPS = 135;


   
   /**
    *  Actual Level-3 Digital Hybrid Scan based Precipitation Product Code <br>
    *  AWIPS HEADER CODE: DSP <br>
    *  PRODUCT CODE: 138
    */
   public final static int L3PC_DIGITAL_HYBRID_PRECIP = 138;
   
   
   
   
   
   /**
    *  Actual Level-3 Digital Differential Reflectivity code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 159
    */
   public final static int L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY = 159;
   
   /**
    *  Actual Level-3 Digital Correlation Coefficient Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 161
    */
   public final static int L3PC_DIGITAL_CORRELATION_COEFFICIENT = 161;
   
   /**
    *  Actual Level-3 Digital Differential Phase Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 163
    */
   public final static int L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE = 163;
   
   
   
   /**
    *  Actual Level-3 Digital HYDROMETEOR CLASSIFICATION Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 165
    */
   public final static int L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION = 165;
   
   
   
   
   

   /**
    *  Actual Level-3 Dual-Pole One Hour Precip Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 169
    */
   public final static int L3PC_ONE_HOUR_ACCUMULATION = 169;
   
   /**
    *  Actual Level-3 Digital Dual-Pole One Hour Precip Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 170
    */
   public final static int L3PC_DIGITAL_ONE_HOUR_ACCUMULATION = 170;

   /**
    *  Actual Level-3 Digital Dual-Pole Storm Total Precip Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 172
    */
   public final static int L3PC_STORM_TOTAL_ACCUMULATION = 171;
   
   /**
    *  Actual Level-3 Digital Dual-Pole Storm Total Precip Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 172
    */
   public final static int L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION = 172;
   
   
   /**
    *  Actual Level-3 Digital Dual-Pole User Selectable / Variable (such as 3 and 24 Hour)
    *  Precip Accumulation Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 173
    */
   public final static int L3PC_DIGITAL_USER_SELECTABLE_ACCUMULATION = 173;
   
   
   /**
    *  Actual Level-3 Digital Dual-Pole One Hour Precip Difference Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 174
    */
   public final static int L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE = 174;
   
   /**
    *  Actual Level-3 Digital Dual-Pole Storm Total Precip Difference Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 175
    */
   public final static int L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE = 175;
   
   /**
    *  Actual Level-3 Digital Dual-Pole Instantaneous Precip Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 176
    */
   public final static int L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE = 176;
   
   /**
    *  Actual Level-3 Digital Dual-Pole Hybrid Hydrometer Product Code<br>
    *  AWIPS HEADER CODE: ??? <br>
    *  PRODUCT CODE: 176
    */
   public final static int L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION = 177;
   
   
   
   
   
   
   /**
    *  Actual Level-3 TDWR Base Reflectivity Product Code <br>
    *  AWIPS HEADER CODE: TLZ <br>
    *  PRODUCT CODE: 186
    */
   public final static int L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT = 186;
   
   /**
    *  Actual Level-3 TDWR Base Reflectivity Product Code <br>
    *  AWIPS HEADER CODE: TR[0-3] <br>
    *  PRODUCT CODE: 180
    */
   public final static int L3PC_TDWR_BASE_REFLECTIVITY_8BIT = 180;
   
   /**
    *  Actual Level-3 TDWR Base Reflectivity Product Code <br>
    *  AWIPS HEADER CODE: TR[0-3] <br>
    *  PRODUCT CODE: 181
    */
   public final static int L3PC_TDWR_BASE_REFLECTIVITY = 181;
   
   /**
    *  Actual Level-3 TDWR Base Reflectivity Product Code <br>
    *  AWIPS HEADER CODE: TV[0-3] <br>
    *  PRODUCT CODE: 182
    */
   public final static int L3PC_TDWR_BASE_VELOCITY_8BIT = 182;
   
   /**
    *  Description of the Field
    */
   public final static int UNKNOWN = -1;

   
   
   public final static int Q2_NATL_MOSAIC_3DREFL = 6910;
   

   /**
    *  Gets the Radar ID (ICAO) attribute of the NexradHeader object
    *
    * @return    The icao value
    */
   public String getICAO();
   
   /**
    *  Gets the lat attribute of the NexradHeader object
    *
    * @return    The lat value
    */
   public double getLat();


   /**
    *  Gets the lon attribute of the NexradHeader object
    *
    * @return    The lon value
    */
   public double getLon();


   /**
    *  Gets the alt attribute of the NexradHeader object
    *
    * @return    The alt value
    */
   public double getAlt();

   
   /**
    * Returns Date of product creation in milliseconds from 1970-1-1 GMT
    *
    * @return    The date value
    */
   public long getMilliseconds();
   

   /**
    *  Gets the date attribute of the NexradHeader object
    *
    * @return    The date value
    */
   public int getDate();


   /**
    *  Gets the hour attribute of the NexradHeader object
    *
    * @return    The hour value
    */
   public short getHour();


   /**
    *  Gets the hourString attribute of the NexradHeader object
    *
    * @return    The hourString value
    */
   public String getHourString();


   /**
    *  Gets the minute attribute of the NexradHeader object
    *
    * @return    The minute value
    */
   public short getMinute();


   /**
    *  Gets the minuteString attribute of the NexradHeader object
    *
    * @return    The minuteString value
    */
   public String getMinuteString();


   /**
    *  Gets the second attribute of the NexradHeader object
    *
    * @return    The second value
    */
   public short getSecond();


   /**
    *  Gets the secondString attribute of the NexradHeader object
    *
    * @return    The secondString value
    */
   public String getSecondString();


   /**
    *  Gets the productCode attribute of the NexradHeader object
    *
    * @return    The productCode value
    */
   public short getProductCode();


   /**
    *  Gets the opMode attribute of the NexradHeader object
    *
    * @return    The opMode value
    */
   public char getOpMode();


   /**
    *  Gets the vCP attribute of the NexradHeader object
    *
    * @return    The vCP value
    */
   public short getVCP();


   /**
    *  Gets the dataThresholdString attribute of the NexradHeader object
    *
    * @param  index  Description of the Parameter
    * @return        The dataThresholdString value
    */
   public String getDataThresholdString(int index);


   /**
    *  Gets the numberOfLevels attribute of the NexradHeader object
    *
    * @return    The numberOfLevels value
    */
   public int getNumberOfLevels(boolean classify);


   /**
    *  Description of the Method
    *
    * @param  url  Description of the Parameter
    */
   public void decodeHeader(URL url) throws DecodeException;


   /**
    *  Gets the nexradURL attribute of the NexradHeader object
    *
    * @return    The nexradURL value
    */
   public URL getDataURL();


   /**
    *  Gets the randomAccessFile attribute of the NexradHeader object
    *
    * @return    The randomAccessFile value
    */
   public ucar.unidata.io.RandomAccessFile getRandomAccessFile();


   /**
    *  Gets the productType attribute of the NexradHeader object
    *
    * @return    The productType value
    */
   public int getProductType();


   /**
    *  Gets the nexradHashtables attribute of the NexradHeader object
    *
    * @return    The nexradHashtables value
    */
   public RadarHashtables getNexradHashtables();
   
   
   /**
    *  Gets the max bounds for this Nexrad file
    *
    * @return    The bounds value
    */
   public java.awt.geom.Rectangle2D.Double getNexradBounds();
   
   
   
   /**
    *  Gets lon, lat location of Radar site as Coordinate 
    *
    * @return                 The radar location as a Coordinate object
    */
   public Coordinate getRadarCoordinate();
   
   
   public float getVersion();
   
}

