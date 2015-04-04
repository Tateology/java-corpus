package gov.noaa.ncdc.wsdemo;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;



public class Stage4Extract {


    public static enum AccumulationType { TYPE_DAILY, TYPE_6HOURLY, TYPE_HOURLY };

    public static final int ACCESS_NCSERVER = 101;

    private String basePath; 
    private String gridName;

    private boolean verbose = false;
    private boolean stopProcessing = false;

    private int countLimit = -1;


    /**
     * Extracts Stage-IV grid cell data for a specified lat/lon using the
     * THREDDS Data Server at http://nomads.ncdc.noaa.gov:8085/thredds/ and
     * the "Total_precipitation" variable.
     *
     */
    public Stage4Extract() {
        this("http://nomads.ncdc.noaa.gov:8085/thredds/", "Total_precipitation");
    }

    /**
     * Extracts Stage-IV grid cell data for a specified lat/lon using a
     * supplied THREDDS Data Server root and grid name.
     * 
     * @param basePath
     * @param gridName
     */
    public Stage4Extract(String basePath, String gridName) {
        this.basePath = basePath;
        this.gridName = gridName;
    }

    /**
     * Gets data for a specified lat/lon, data type and time range using default NetCDF Server access type
     * @param type TYPE_DAILY, TYPE_6HOURLY or TYPE_HOURLY
     * @param lat decimal degrees
     * @param lon decimal degrees
     * @param begYYYYMMDD
     * @param endYYYYMMDD
     * @return
     */
    public String getDailyData(AccumulationType type, double lat, double lon, String begYYYYMMDD, String endYYYYMMDD) {
        return getDailyData(null, null, type, lat, lon, begYYYYMMDD, endYYYYMMDD, ACCESS_NCSERVER);
    }


    /**
     * Gets data for a specified lat/lon, data type and time range using default NetCDF Server access type
     * @param type TYPE_DAILY, TYPE_6HOURLY or TYPE_HOURLY
     * @param lat decimal degrees
     * @param lon decimal degrees
     * @param begYYYYMMDD
     * @param endYYYYMMDD
     * @param accessType
     * @return
     */
    public String getDailyData(AccumulationType type, double lat, double lon, String begYYYYMMDD, String endYYYYMMDD, int accessType) {
        return getDailyData(null, null, type, lat, lon, begYYYYMMDD, endYYYYMMDD, accessType);
    }
//    /**
//     * Gets data for a specified lat/lon, data type and time range using default NetCDF Server access type
//     * @param out OutputStream to write data to as it is received.  If null, this is ignored.
//     * @param type TYPE_DAILY, TYPE_6HOURLY or TYPE_HOURLY
//     * @param lat decimal degrees
//     * @param lon decimal degrees
//     * @param begYYYYMMDD
//     * @param endYYYYMMDD
//     * @param accessType
//     * @return
//     */
//    public String getDailyData(OutputStream out, int type, double lat, double lon, String begYYYYMMDD, String endYYYYMMDD) {
//        return getDailyData(out, type, lat, lon, begYYYYMMDD, endYYYYMMDD, ACCESS_NCSERVER);
//    }

    
    
    
    public String getDailyData(TimeSeries timeSeries, AccumulationType type, double lat, double lon, String begYYYYMMDD, String endYYYYMMDD) {
        return getDailyData(null, timeSeries, type, lat, lon, begYYYYMMDD, endYYYYMMDD, ACCESS_NCSERVER);
    }
    public String getDailyData(TimeSeries timeSeries, TimeSeries totalSeries, AccumulationType type, 
            double lat, double lon, String begYYYYMMDD, String endYYYYMMDD) {
        
        return getDailyData(null, timeSeries, totalSeries, type, lat, lon, begYYYYMMDD, endYYYYMMDD, ACCESS_NCSERVER);
    }

    public String getDailyData(OutputStream out, AccumulationType type, double lat, double lon, String begYYYYMMDD, String endYYYYMMDD) {
        return getDailyData(out, null, type, lat, lon, begYYYYMMDD, endYYYYMMDD, ACCESS_NCSERVER);
    }
    
    
    /**
     * Gets data for a specified lat/lon, data type and time range using default NetCDF Server access type
     * @param out OutputStream to write data to as it is received.  If null, this is ignored.
     * @param type TYPE_DAILY, TYPE_6HOURLY or TYPE_HOURLY
     * @param lat decimal degrees
     * @param lon decimal degrees
     * @param begYYYYMMDD
     * @param endYYYYMMDD
     * @param accessType
     * @return
     */
    private String getDailyData(OutputStream out, TimeSeries timeSeries, AccumulationType type, double lat, double lon, String begYYYYMMDD, String endYYYYMMDD, int accessType) {
        return getDailyData(out, timeSeries, null, type, lat, lon, begYYYYMMDD, endYYYYMMDD, accessType);
    }
    /**
     * Gets data for a specified lat/lon, data type and time range using default NetCDF Server access type
     * @param out OutputStream to write data to as it is received.  If null, this is ignored.
     * @param type TYPE_DAILY, TYPE_6HOURLY or TYPE_HOURLY
     * @param lat decimal degrees
     * @param lon decimal degrees
     * @param begYYYYMMDD
     * @param endYYYYMMDD
     * @param accessType
     * @return
     */
    private String getDailyData(OutputStream out, TimeSeries timeSeries, TimeSeries totalSeries, 
            AccumulationType type, double lat, double lon, String begYYYYMMDD, String endYYYYMMDD, int accessType) {
        
        try {
            
            double total = 0;
            

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(Integer.parseInt(begYYYYMMDD.substring(0, 4)), 
                    Integer.parseInt(begYYYYMMDD.substring(4, 6))-1,
                    Integer.parseInt(begYYYYMMDD.substring(6, 8)));
            Calendar endcal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            endcal.set(Integer.parseInt(endYYYYMMDD.substring(0, 4)), 
                    Integer.parseInt(endYYYYMMDD.substring(4, 6))-1,
                    Integer.parseInt(endYYYYMMDD.substring(6, 8)));

            DecimalFormat fmt02 = new DecimalFormat("00");
            DecimalFormat fmt3 = new DecimalFormat("0.000");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");        

            double unitsFactor = 0.0394;

            StringBuffer sb = new StringBuffer();
            
            while (cal.getTimeInMillis() <= endcal.getTimeInMillis()) {

                String yyyy = String.valueOf(cal.get(Calendar.YEAR));
                String mm = fmt02.format(cal.get(Calendar.MONTH)+1);
                String dd = fmt02.format(cal.get(Calendar.DAY_OF_MONTH));


                if (type == AccumulationType.TYPE_DAILY) {

                    if (stopProcessing) {
                        stopProcessing = false;
                        return sb.toString();
                    }
                    
                    try {

                        String dayURL = basePath+"ncServer/radar/StIV/day/ST4."+yyyy+mm+dd+"12.24h";
                        String dataString = yyyy+mm+dd+"12,"+fmt3.format(getDataValue(dayURL, lat, lon))+"\n"; 
                        sb.append(dataString);
                        if (out != null) {
                            out.write(dataString.getBytes());
                            out.flush();
                        }
                        if (timeSeries != null) {
                            String[] cols = dataString.split(",");
                            timeSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), Double.parseDouble(cols[1])*unitsFactor);
                        }
                        if (totalSeries != null) {
                            String[] cols = dataString.split(",");
                            total += Double.parseDouble(cols[1])*unitsFactor;
                            totalSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), total);
                        }

                        if (verbose) System.out.println(basePath);
                        if (verbose) System.out.println("NCSERVER: "+dayURL);
                        if (verbose) System.out.println(yyyy+mm+dd+"12,"+fmt3.format(getDataValue(dayURL, lat, lon)));

                    } catch (Exception e) {
                        System.out.println(yyyy+mm+dd+",NaN -- EXCEPTION: "+e);
                        e.printStackTrace();
                        String dataString = yyyy+mm+dd+"12,NaN\n"; 
                        sb.append(dataString);
                        if (out != null) {
                            out.write(dataString.getBytes());
                            out.flush();
                        }
                        if (timeSeries != null) {
                            String[] cols = dataString.split(",");
                            timeSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), Double.NaN);
                        }
                    } 
                }
                else if (type == AccumulationType.TYPE_6HOURLY) {
                    for (int n=0; n<4; n++) {

                        if (stopProcessing) {
                            stopProcessing = false;
                            return sb.toString();
                        }
                        try {

                            String url = basePath+"ncServer/radar/StIV/6hr/ST4."+yyyy+mm+dd+fmt02.format(n*6)+".06h";
                            String dataString = yyyy+mm+dd+fmt02.format(n*6)+","+fmt3.format(getDataValue(url, lat, lon))+"\n"; 
                            sb.append(dataString);
                            if (out != null) {
                                out.write(dataString.getBytes());
                                out.flush();
                            }
                            if (timeSeries != null) {
                                String[] cols = dataString.split(",");
                                timeSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), Double.parseDouble(cols[1])*unitsFactor);
                            }
                            if (totalSeries != null) {
                                String[] cols = dataString.split(",");
                                total += Double.parseDouble(cols[1])*unitsFactor;
                                totalSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), total);
                            }

                        } catch (Exception e) {
                            //System.out.println(yyyy+mm+dd+fmt02.format(n*6)+",NaN -- EXCEPTION: "+e);
                            //e.printStackTrace();
                            String dataString = yyyy+mm+dd+fmt02.format(n*6)+",NaN\n";
                            sb.append(dataString);
                            if (out != null) {
                                out.write(dataString.getBytes());
                                out.flush();
                            }
                            if (timeSeries != null) {
                                String[] cols = dataString.split(",");
                                timeSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), Double.NaN);
                            }
                            if (totalSeries != null) {
                                String[] cols = dataString.split(",");
                                total += Double.parseDouble(cols[1])*unitsFactor;
                                totalSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), total);
                            }
                        } 
                    }
                }
                else if (type == AccumulationType.TYPE_HOURLY) {
                    for (int n=0; n<24; n++) {

                        if (stopProcessing) {
                            stopProcessing = false;
                            return sb.toString();
                        }
                        try {

                            String url = basePath+"ncServer/radar/StIV/"+yyyy+"/1hr/ST4."+yyyy+mm+dd+fmt02.format(n)+".01h";
                            String dataString = yyyy+mm+dd+fmt02.format(n)+","+fmt3.format(getDataValue(url, lat, lon))+"\n"; 
                            sb.append(dataString);
                            if (out != null) {
                                out.write(dataString.getBytes());
                                out.flush();
                            }
                            if (timeSeries != null) {
                                String[] cols = dataString.split(",");
                                timeSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), Double.parseDouble(cols[1])*unitsFactor);
                            }

                        } catch (Exception e) {
                            System.out.println(yyyy+mm+dd+fmt02.format(n)+",NaN -- EXCEPTION: "+e);
                            //e.printStackTrace();
                            String dataString = yyyy+mm+dd+fmt02.format(n)+",NaN\n"; 
                            sb.append(dataString);
                            if (out != null) {
                                out.write(dataString.getBytes());
                                out.flush();
                            }
                            if (timeSeries != null) {
                                String[] cols = dataString.split(",");
                                timeSeries.add(new FixedMillisecond(dateFormat.parse(cols[0])), Double.NaN);
                            }
                        } 
                    }
                }


                cal.add(Calendar.DAY_OF_MONTH, 1);


            }

            return sb.toString();

        } catch (Exception e) {
            return "<pre>"+e.toString()+"</pre>";
        }

    }


    private double getDataValue(String opendapURL, int hrapY, int hrapX) throws Exception {


        GridDataset dataset = GridDataset.open(opendapURL);


        GeoGrid grid = dataset.findGridByName(gridName);
        Array temp_data = grid.readDataSlice(-1, -1, hrapY, hrapX);
        IndexIterator indexIter = temp_data.getIndexIteratorFast();

        double val = Double.NaN;
        if (indexIter.hasNext()) {
            val = indexIter.getDoubleNext();
        }
        return val;        


//      } catch (Exception e) {
//      System.out.println("CAUGHT IT!!!!");
//      return Double.NaN;
//      }

    }



    private double getDataValue(String ncServerURL, double lat, double lon) throws Exception {

//      System.out.println("STEVE 22");

        System.out.println("NetcdfFile.open("+ncServerURL+"?grid="+gridName+"&west="+lon+"&east="+lon+"&north="+lat+"&south="+lat+"&addLatLon=true\");");

        NetcdfFile dataset = NetcdfFile.open(ncServerURL+"?grid="+gridName+"&west="+lon+"&east="+lon+"&north="+lat+"&south="+lat+"&addLatLon=true");

//      System.out.println(dataset);

        Variable gridVar = dataset.findVariable(gridName);

//      System.out.println("gridVar="+gridVar);
        Array temp_data = gridVar.read();
        IndexIterator indexIter = temp_data.getIndexIteratorFast();
//      http://nomads.ncdc.noaa.gov:8085/thredds/ncServer/radar/StIV/2005/07/28/day/ST4.2005072812.24h.Z?name=Total_precipitation&west=-82.6142&east=-82.614&north=35.4945&south=35.4945&addLatLon=true


        double val = Double.NaN;
        if (indexIter.hasNext()) {
            val = indexIter.getDoubleNext();
        }
        return val;        




    }


    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getGridName() {
        return gridName;
    }

    public void setGridName(String gridName) {
        this.gridName = gridName;
    }

    public int getCountLimit() {
        return countLimit;
    }
    /**
     * Sets the countLimit value, which limits the number of URLs/grids to open.
     * A value of '-1' means unlimited.  A value > 0 will cause an exception
     * to be thrown if the supplied date range exceeds the count limit.
     *   
     *  
     * @param countLimit
     */
    public void setCountLimit(int countLimit) {
        this.countLimit = countLimit;
    }

    
    
    
    public void stopProcessing() {
        System.out.println("ST4 STOPPING PROCESSING");
        stopProcessing = true;
    }

}
