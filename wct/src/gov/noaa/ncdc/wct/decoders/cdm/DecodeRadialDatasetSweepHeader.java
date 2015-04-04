package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;

import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.unidata.io.RandomAccessFile;

import com.vividsolutions.jts.geom.Coordinate;

public class DecodeRadialDatasetSweepHeader implements NexradHeader {

    private static final Logger logger = Logger.getLogger(DecodeRadialDatasetSweepHeader.class.getName());


    private RadialDatasetSweep radialDataset;
    private URL dataURL;
    private Date datasetDate;
    // :time_coverage_start = "2005-08-29T13:54:51Z";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private HashMap<String, String> attributeMap;
    private String[] dataThresholdStringArray = new String[] { "N/A - steve" };

    private String icao;
    private double lat, lon, alt;
    private double minValue = Double.POSITIVE_INFINITY;
    private double maxValue = Double.NEGATIVE_INFINITY;

    public void decodeHeader(URL url) throws DecodeException {
        throw new DecodeException("THIS IS AN ADAPTER TO THE CDM RadialDatasetSweep OBJECT ONLY."+
        "USE setRadialDatasetSweep(...) INSTEAD.");

    }


    public void setRadialDatasetSweep(RadialDatasetSweep radialDataset) throws ParseException {
        this.radialDataset = radialDataset;

        // :time_coverage_start = "2005-08-29T13:54:51Z";
        List<Attribute> attList = radialDataset.getGlobalAttributes();
        attributeMap = new HashMap<String, String>();
        for (Attribute att : attList) {
            if (att.getDataType() == DataType.STRING || att.getDataType() == DataType.CHAR) {
                attributeMap.put(att.getName(), att.getStringValue());
                logger.fine("attributeMap: put - "+att.getName()+"="+att.getStringValue());
            }
            else {
                attributeMap.put(att.getName(), att.getNumericValue().toString());
                logger.fine("attributeMap: put - "+att.getName()+"="+att.getNumericValue());
            }
        }

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        // allow this attribute to be null
        if (attributeMap.get("time_coverage_start") != null) {
        	this.datasetDate = dateFormat.parse(attributeMap.get("time_coverage_start"));
        }
        else {
        	this.datasetDate = radialDataset.getStartDate();
        }

        try {
        	dataURL = new URL(radialDataset.getLocationURI());
        } catch (Exception e) {
        	System.out.println(radialDataset.getLocationURI());
        	e.printStackTrace();
        }
        
        try {


            this.icao = radialDataset.getRadarID();

            this.alt = RadarHashtables.getSharedInstance().getElev(radialDataset.getRadarID()); 
            this.lat = RadarHashtables.getSharedInstance().getLat(radialDataset.getRadarID()); 
            this.lon = RadarHashtables.getSharedInstance().getLon(radialDataset.getRadarID()); 

            if (this.alt == -999 || this.lat == -999 || this.lon == -999) {
                this.alt = radialDataset.getCommonOrigin().getAltitude()*3.28083989501312;
                this.lat = radialDataset.getCommonOrigin().getLatitude();
                this.lon = radialDataset.getCommonOrigin().getLongitude();
            }

        } catch (Exception e) {
            this.alt = RadarHashtables.getSharedInstance().getElev(radialDataset.getRadarID()); 
            this.lat = RadarHashtables.getSharedInstance().getLat(radialDataset.getRadarID()); 
            this.lon = RadarHashtables.getSharedInstance().getLon(radialDataset.getRadarID()); 
        }

    }

    public RadialDatasetSweep getRadialDatasetSweep() {
        return this.radialDataset;
    }

    /**
     * Gets the altitude in feet - uses altitude data in NetCDF for Java API converted to feet
     */
    public double getAlt() {
        return this.alt;
    }

    public String getDataThresholdString(int index) {
        //		logger.fine("GETTING dataThresholdStringArray: "+Arrays.deepToString(dataThresholdStringArray));

        try {
            if (index >= 0 && index < dataThresholdStringArray.length) {
                return dataThresholdStringArray[index];
            }
            else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public void setDataThresholdStringArray(String[] categories) {
        //		logger.fine("SETTING dataThresholdStringArray: "+Arrays.deepToString(categories));
        this.dataThresholdStringArray = categories;
    }

    public int getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String yyyymmdd = sdf.format(datasetDate);
        return new Integer(yyyymmdd).intValue();
    }

    public short getHour() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        //        System.out.println(datasetDate+" ::::::: "+new Short(sdf.format(datasetDate).substring(8, 10)).shortValue());
        return new Short(sdf.format(datasetDate)).shortValue();
    }

    public String getHourString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        //        System.out.println(datasetDate+" :::::: "+sdf.format(datasetDate).substring(8, 10));
        return sdf.format(datasetDate);
    }

    public String getICAO() {
        return this.icao;
    }

    public double getLat() {
        return this.lat;
    }

    public double getLon() {
        return this.lon;
    }




    /**
     * This is needed for data that does not have the station info encoded in the file.
     * @param icao
     * @param lat
     * @param lon
     * @param alt in feet
     */
    public void setStationInfo(String icao, double lat, double lon, double alt) {
        this.icao = icao;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }



    public long getMilliseconds() {
        return datasetDate.getTime();
    }

    public short getMinute() {
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new Short(sdf.format(datasetDate)).shortValue();
    }

    public String getMinuteString() {
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(datasetDate);
    }

    public Rectangle2D.Double getNexradBounds() {
        return MaxGeographicExtent.getNexradExtent(getLat(), getLon());
    }

    public RadarHashtables getNexradHashtables() {
        return RadarHashtables.getSharedInstance();
    }

    public URL getDataURL() {
        return dataURL;
    }

    public int getNumberOfLevels(boolean classify) {
        return NexradColorFactory.getColors(getProductCode(), classify).length;
    }

    public char getOpMode() {
        return 'X';
    }

    /**
     * This can be overridden if more product types are needed
     */
    public short getProductCode() {
        return NexradHeader.LEVEL2_REFLECTIVITY;
    }

    public int getProductType() {
        return NexradHeader.LEVEL2;
    }

    public Coordinate getRadarCoordinate() {
        return new Coordinate(getLon(), getLat());
    }

    public RandomAccessFile getRandomAccessFile() {
        System.err.println("THIS METHOD IS NOT SUPPORTED");
        return null;
    }

    public short getSecond() {
        SimpleDateFormat sdf = new SimpleDateFormat("ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new Short(sdf.format(datasetDate)).shortValue();
    }

    public String getSecondString() {
        SimpleDateFormat sdf = new SimpleDateFormat("ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(datasetDate);
    }

    public short getVCP() {
        //	    System.err.println("VCP::::: "+attributeMap.get("VolumeCoveragePattern"));
        try {
            return new Short(attributeMap.get("VolumeCoveragePattern")).shortValue();
        } catch (Exception e) {
            return -999;
        }
    }

    public float getVersion() {
        // TODO Auto-generated method stub
        return 0;
    }


    public HashMap<String, String> getAttributeMap() {
        return attributeMap;
    }


    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }


    public double getMinValue() {
        return minValue;
    }


    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }


    public double getMaxValue() {
        return maxValue;
    }



    
    
    

}
