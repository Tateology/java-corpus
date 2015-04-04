package gov.noaa.ncdc.wct.decoders.goes;

public class SatelliteUtilities {
    
    public static String getLongName(GoesRemappedRaster.Band band) {
        if (band == GoesRemappedRaster.Band.BAND1) {
            return "Visible";
        }
        else if (band == GoesRemappedRaster.Band.BAND2) {
            return "Short Wave";
        }
        else if (band == GoesRemappedRaster.Band.BAND3) {
            return "Infrared Water Vapor";
        }
        else if (band == GoesRemappedRaster.Band.BAND4) {
            return "Infrared Window";
        }
        else if (band == GoesRemappedRaster.Band.BAND5) {
            return "Infrared Split Window";
        }
        else if (band == GoesRemappedRaster.Band.BAND6) {
            return "Infrared CO2";
        }
        else {
            return "";
        }
    }

    
    public static String getLongName(String band) {
        if (band.equalsIgnoreCase("BAND_01")) {
            return "Visible";
        }
        else if (band.equalsIgnoreCase("BAND_02")) {
            return "Short Wave";
        }
        else if (band.equalsIgnoreCase("BAND_03")) {
            return "Infrared Water Vapor";
        }
        else if (band.equalsIgnoreCase("BAND_04")) {
            return "Infrared Window";
        }
        else if (band.equalsIgnoreCase("BAND_05")) {
            return "Infrared Split Window";
        }
        else if (band.equalsIgnoreCase("BAND_06")) {
            return "Infrared CO2";
        }
        else {
            return "";
        }
    }
    
}
