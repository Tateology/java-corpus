package gov.noaa.ncdc.wct.decoders.cdm.conv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordSysBuilder;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.util.CancelTask;

/**
 * CF-Convention translator for PATMOS HDF-4 files.
 * @author steve.ansari
 *
 */
public class PATMOSConvention extends CoordSysBuilder {

    /*
     *  Original CDL

     netcdf /E:/work/patmos/patmosx_n07_asc_1982_011.level2b.hdf {
 dimensions:
   fakeDim0 = 3601;
   fakeDim1 = 1801;
   latitude_index = 1801;
   longitude_index = 3601;
 variables:
   float scan_line_time(latitude_index=1801, longitude_index=3601);
     :SCALED = 0B; // byte
     :UNITS = "hours";
     :STANDARD_NAME = "";
     :LONG_NAME = "time for the scan line in fractional hours";
     :RANGE_MISSING = -999.0f; // float
   byte anchor_sensor_zenith(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "degrees";
     :STANDARD_NAME = "sensor_zenith";
     :LONG_NAME = "sensor_zenith";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.70866144f; // float
     :ADD_OFFSET = 90.0f; // float
     :RANGE_MIN = 0.0f; // float
     :RANGE_MAX = 180.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte anchor_solar_zenith(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "degrees";
     :STANDARD_NAME = "solar_zenith";
     :LONG_NAME = "solar_zenith_angle";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.70866144f; // float
     :ADD_OFFSET = 90.0f; // float
     :RANGE_MIN = 0.0f; // float
     :RANGE_MAX = 180.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte cloud_probability(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "none";
     :STANDARD_NAME = "cloud_probability";
     :LONG_NAME = "probability of a pixel being cloudy from the Bayesian cloud mask";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.003937008f; // float
     :ADD_OFFSET = 0.5f; // float
     :RANGE_MIN = 0.0f; // float
     :RANGE_MAX = 1.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte anchor_relative_azimuth(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "degrees";
     :STANDARD_NAME = "relative_azimuth";
     :LONG_NAME = "relative_azimuth_angle";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.70866144f; // float
     :ADD_OFFSET = 90.0f; // float
     :RANGE_MIN = 0.0f; // float
     :RANGE_MAX = 180.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte anchor_solar_azimuth(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "degrees";
     :STANDARD_NAME = "solar_azimuth";
     :LONG_NAME = "solar_azimuth";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 1.4173229f; // float
     :ADD_OFFSET = 180.0f; // float
     :RANGE_MIN = 0.0f; // float
     :RANGE_MAX = 360.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte packed_pixel_meta_data(latitude_index=1801, longitude_index=3601);
     :SCALED = 0B; // byte
     :UNITS = "none";
     :STANDARD_NAME = "packed_pixel_meta_data";
     :LONG_NAME = "pixel quality flags packed into one byte";
     :RANGE_MISSING = -999.0f; // float
   byte packed_land_cover(latitude_index=1801, longitude_index=3601);
     :SCALED = 0B; // byte
     :UNITS = "none";
     :STANDARD_NAME = "packed_land_cover";
     :LONG_NAME = "land cover, snow and coast values packed into one byte";
     :RANGE_MISSING = -999.0f; // float
   short ch1_reflectance(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "%";
     :STANDARD_NAME = "toa_bidirectional_reflectance_0_63_micron";
     :LONG_NAME = "observed reflectance at 0.63_micron as a percentage using PATMOS-x calibration";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.0018616291f; // float
     :ADD_OFFSET = 59.0f; // float
     :RANGE_MIN = -2.0f; // float
     :RANGE_MAX = 120.0f; // float
     :SCALED_MIN = -32767; // int
     :SCALED_MAX = 32767; // int
     :SCALED_MISSING = -32768; // int
     :_FILLVALUE = -32768; // int
     :FCDR = "none";
   short ch2_reflectance(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "%";
     :STANDARD_NAME = "toa_bidirectional_reflectance_0_86_micron";
     :LONG_NAME = "observed_reflectance_0.86_micron expressed as a percentage using PATMOS-x calibration";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.0018616291f; // float
     :ADD_OFFSET = 59.0f; // float
     :RANGE_MIN = -2.0f; // float
     :RANGE_MAX = 120.0f; // float
     :SCALED_MIN = -32767; // int
     :SCALED_MAX = 32767; // int
     :SCALED_MISSING = -32768; // int
     :_FILLVALUE = -32768; // int
     :FCDR = "none";
   byte ch3a_reflectance(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "%";
     :STANDARD_NAME = "toa_bidirectional_reflectance_1_60_micron";
     :LONG_NAME = "observed_reflectance_1.60_micron expressed as a percentage using PATMOS-x calibration";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.48031497f; // float
     :ADD_OFFSET = 59.0f; // float
     :RANGE_MIN = -2.0f; // float
     :RANGE_MAX = 120.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte ch3b_reflectance(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "%";
     :STANDARD_NAME = "toa_bidirectional_pseudo_reflectance_3_75_micron";
     :LONG_NAME = "observed_pseudo_reflectance_3.75_micron expressed as a percentage using PATMOS-x calibration";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.39370078f; // float
     :ADD_OFFSET = 30.0f; // float
     :RANGE_MIN = -20.0f; // float
     :RANGE_MAX = 80.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte ch3b-ch4_temperature_difference(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "K";
     :STANDARD_NAME = "toa_brightness_temperature_difference_3_75_minus_11_micron";
     :LONG_NAME = "toa brightness temperature difference between 3.75 and 11 micron using PATMOS-x calibration";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.47244096f; // float
     :ADD_OFFSET = 40.0f; // float
     :RANGE_MIN = -20.0f; // float
     :RANGE_MAX = 100.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   short ch4_temperature(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "K";
     :STANDARD_NAME = "toa_brightness_temperature_11_micron";
     :LONG_NAME = "toa_brightness_temperature_11_micron using PATMOS-x calibration";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.0024414808f; // float
     :ADD_OFFSET = 260.0f; // float
     :RANGE_MIN = 180.0f; // float
     :RANGE_MAX = 340.0f; // float
     :SCALED_MIN = -32767; // int
     :SCALED_MAX = 32767; // int
     :SCALED_MISSING = -32768; // int
     :_FILLVALUE = -32768; // int
   byte ch4-ch5_temperature_difference(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "K";
     :STANDARD_NAME = "toa_brightness_temperature_difference_11_minus_12_micron";
     :LONG_NAME = "toa_brightness_temperature_difference_between_11_and_12_microns using PATMOS-x calibration";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.062992126f; // float
     :ADD_OFFSET = 4.0f; // float
     :RANGE_MIN = -4.0f; // float
     :RANGE_MAX = 12.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte ch1_reflectance_stddev_3x3(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "%";
     :STANDARD_NAME = "ibrationectance_0_63_micron_3x3_standard_deviation";
     :LONG_NAME = "standard_deviation of the 0.63 micron reflectance computed over a 3x3 pixel array using PATMOS-x cal";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.39370078f; // float
     :ADD_OFFSET = 50.0f; // float
     :RANGE_MIN = 0.0f; // float
     :RANGE_MAX = 100.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   byte ch4_temperature_stddev_3x3(latitude_index=1801, longitude_index=3601);
     :SCALED = 1B; // byte
     :UNITS = "K";
     :STANDARD_NAME = "toa_brightness_temperature_11_micron_3x3_standard_deviation";
     :LONG_NAME = "standard_deviation of the 11 micron BT computed over a 3x3 pixel array using PATMOS-x calibration";
     :RANGE_MISSING = -999.0f; // float
     :SCALE_FACTOR = 0.07874016f; // float
     :ADD_OFFSET = 10.0f; // float
     :RANGE_MIN = 0.0f; // float
     :RANGE_MAX = 20.0f; // float
     :SCALED_MIN = -127; // int
     :SCALED_MAX = 127; // int
     :SCALED_MISSING = -128; // int
     :_FILLVALUE = -128; // int
   float longitude(fakeDim0=3601);
     :SCALED = 0B; // byte
     :UNITS = "degrees";
     :RANGE_MISSING = -999.0f; // float
     :_CoordinateAxisType = "Lon";
   float latitude(fakeDim1=1801);
     :SCALED = 0B; // byte
     :UNITS = "degrees";
     :RANGE_MISSING = -999.0f; // float
     :_CoordinateAxisType = "Lat";

 :PROCESSOR = "CLAVR-x + PATMOS-x 5.2.0";
 :CREATED = "2010-08-30T14:08:53-05:00";
 :HDF_LIB_VERSION = "HDF Version 4.2 Release 4, January 25, 2009";
 :MACHINE = "loki";
 :PROGLANG = "F90";
 :FILENAME = "NSS.GHRR.NC.D82010.S2229.E0016.B0284445.GC.level2.hdf";
 :L1B = "NSS.GHRR.NC.D82010.S2229.E0016.B0284445.GC";
 :START_YEAR = 1982S; // short
 :START_DAY = 10S; // short
 :START_TIME = 22.497766f; // float
 :END_YEAR = 1982S; // short
 :END_DAY = 11S; // short
 :END_TIME = 0.27040473f; // float
 :SPACECRAFT_ID = 7; // int
 :DATA_TYPE = "PIXEL";
 :USE_1B_THERMAL_CALIBRATION_FLAG = 0; // int
 :USE_1B_REFLECTANCE_CALIBRATION_FLAG = 0; // int
 :RENAVIGATION_FLAG = 0; // int
 :USE_SST_ANALYSIS_FLAG = 1; // int
 :CLOUD_TYPING_FLAG = 0; // int
 :SST_ANALYSIS_SOURCE_FLAG = 0; // int
 :NWP_FLAG = 2; // int
 :MODIS_CLEAR_SKY_REFLECTANCE_FLAG = 1; // int
 :CH1_GAIN_LOW = 0.11935909f; // float
 :CH1_GAIN_HIGH = 0.11935909f; // float
 :CH1_SWITCH_COUNT = 500.0f; // float
 :CH1_DARK_COUNT = 35.977192f; // float
 :CH2_GAIN_LOW = 0.12308936f; // float
 :CH2_GAIN_HIGH = 0.12308936f; // float
 :CH2_SWITCH_COUNT = 500.0f; // float
 :CH2_DARK_COUNT = 37.962788f; // float
 :CH3A_GAIN_LOW = 0.0f; // float
 :CH3A_GAIN_HIGH = 0.0f; // float
 :CH3A_SWITCH_COUNT = 500.0f; // float
 :CH3A_DARK_COUNT = 993.9443f; // float
 :SUN_EARTH_DISTANCE = 0.98336005f; // float
 :C1 = 1.191062E-5f; // float
 :C2 = 1.4387863f; // float
 :A_3B = -1.9488269f; // float
 :B_3B = 1.002926f; // float
 :NU_3B = 2684.5232f; // float
 :A_4 = -0.52808f; // float
 :B_4 = 1.0014039f; // float
 :NU_4 = 928.23755f; // float
 :A_5 = -0.40557027f; // float
 :B_5 = 1.0011789f; // float
 :NU_5 = 841.52136f; // float
 :SOLAR_3B_NU = 15.899662f; // float
 :TIME_ERROR_SECONDS = 0.0f; // float
 :NUMBER_OF_ELEMENTS = 409; // int
 :NUMBER_OF_ANCHORS = 51; // int
 :NUMBER_OF_SCANS_LEVEL1B = 12746; // int
 :NUMBER_OF_SCANS_LEVEL2 = 12746; // int
 :NUMBER_OF_LONGITUDES = 3601; // int
 :NUMBER_OF_LATITUDES = 1801; // int
 :LONGITUDE_SPACING = 0.1f; // float
 :LATITUDE_SPACING = 0.1f; // float
 :WESTERN_MOST_LONGITUDE = -180.0f; // float
 :EASTERN_MOST_LONGITUDE = 180.0f; // float
 :NORTHERN_MOST_LATITUDE = 90.0f; // float
 :SOUTHERN_MOST_LATITUDE = -90.0f; // float
 :FRACTION_WITH_VALID_DATA = 0.9878407f; // float
 :FRACTION_WITH_DAYTIME_DATA = 0.86876816f; // float
 :FRACTION_WITH_CLOUDY_DATA = 0.6693429f; // float
 :_History = "Direct read of HDF4 file through CDM library";
 :HDF4_Version = "4.2.4 (HDF Version 4.2 Release 4, January 25, 2009)";
}

      
     *
     */
    
    
    /**
     * Is this my file?
     * TODO LINK TO DOCS HERE:
     *
     * @param ncfile test this NetcdfFile
     * @return true if we think this is a PATMOS HDF File
     */
    public static boolean isMine(NetcdfFile ncfile) {
        
//        System.out.println("in 'isMine' for "+ncfile.getLocation());
        
        String cs = ncfile.findAttValueIgnoreCase(null, "Conventions", null);
        if (cs != null) return false;

        List<Variable> varList = ncfile.getVariables();
        ArrayList<String> varNameList = new ArrayList<String>(varList.size());
        for (Variable var : varList) {
            varNameList.add(var.getName());
        }
        
        

        // patmosx_n07_asc_1982_011.level2b.hdf
        boolean fileNameMatch = (
        		(ncfile.getLocation().contains(".hdf") || ncfile.getLocation().contains(".hdf.gz"))
        		&& ncfile.getLocation().contains("patmosx_"));
        
        
        if (fileNameMatch && 
                varNameList.contains("ch1_reflectance") && 
                varNameList.contains("ch2_reflectance")) {
            return true;
        }

        return false;
    }

    
    
    @Override
    public void augmentDataset(NetcdfDataset ncDataset, CancelTask cancelTask) throws IOException {

        System.out.println("Using CoordSysBuilder: "+this.getClass());

//        someday maybe convert global attributes to lower case?
//        for (Attribute att : ncDataset.getGlobalAttributes()) {
//            ncDataset.addAttribute(null, new Attribute(att.getName().toLowerCase(), att));
//            ncDataset.addAttribute(null, nu)
//        }
        
        
        // convert variable attributes to lower case
        List<Variable> varList = ncDataset.getVariables();
        for (Variable var : varList) {
            
            // this sequence avoids a concurrent modification error on the attribute list
            // 1. assemble new attributes
            List<Attribute> newAttList = new ArrayList<Attribute>();
            for (Attribute att : var.getAttributes()) {
//                System.out.println("var: "+var.getName()+" att: "+att);
                
                if (att.getName().equalsIgnoreCase("_FILLVALUE")) {
                    Attribute newAtt = new Attribute("_FillValue", att);
                    newAttList.add(newAtt);
                }
                else if (! att.getName().equals("_CoordinateAxisType")) {
                    Attribute newAtt = new Attribute(att.getName().toLowerCase(), att);
                    newAttList.add(newAtt);
                }
            }
            
            // 2. remove old attributes and add new ones
            for (Attribute att : newAttList) {
//                System.out.println("new att: "+att);
                var.removeAttribute(att.getName().toUpperCase());
                var.addAttribute(att);
            }
            
            // if no _FillValue exists and range_missing attribute exists, create new _FillValue attribute
            if (var.findAttributeIgnoreCase("range_missing") != null && var.findAttributeIgnoreCase("_FillValue") == null) {
                var.addAttribute(new Attribute("_FillValue", var.findAttributeIgnoreCase("range_missing")));
            }
            
            // 3. add coordinates attribute
            if (var.getRank() == 2) {
                var.addAttribute(new Attribute("coordinates", "latitude longitude"));
            }
            
            if (var.getName().equals("latitude")) {
                var.addAttribute(new Attribute("_CoordinateAxisType", "Lat"));
            }
            if (var.getName().equals("longitude")) {
                var.addAttribute(new Attribute("_CoordinateAxisType", "Lon"));
            }
            
//            System.out.println("var: "+var);

        }

//        dimensions:
//            fakeDim0 = 3601;
//            fakeDim1 = 1801;
//            latitude_index = 1801;
//            longitude_index = 3601;

        // rename and remove dimensions
        ncDataset.removeDimension(null, "fakeDim0");
        ncDataset.removeDimension(null, "fakeDim1");
        ncDataset.findDimension("latitude_index").setName("latitude");
        ncDataset.findDimension("longitude_index").setName("longitude");
        
        
        
        // get dimensions assembled correctly
        
//        float longitude(fakeDim0=3601);
//        :SCALED = 0B; // byte
//        :UNITS = "degrees";
//        :RANGE_MISSING = -999.0f; // float
//        :_CoordinateAxisType = "Lon";
//      float latitude(fakeDim1=1801);
//        :SCALED = 0B; // byte
//        :UNITS = "degrees";
//        :RANGE_MISSING = -999.0f; // float
//        :_CoordinateAxisType = "Lat";        
        ncDataset.findVariable("latitude").setDimensions("latitude");
        ncDataset.findVariable("longitude").setDimensions("longitude");
        
        
    }
    
    
    
}
