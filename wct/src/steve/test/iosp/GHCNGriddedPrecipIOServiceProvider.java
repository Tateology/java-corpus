package steve.test.iosp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.FileWriter;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

/**
 * Overview and Description of Data
 * This data set contains gridded precipitation anomalies calculated 
 * from the GHCN V2 monthly precipitation data set. 2064 homogeneity 
 * adjusted precipitation stations (from the U.S., Canada, and Former 
 * Soviet Union) were combined with a data set containing 20590 raw 
 * precipitation stations throughout the world to create these gridded 
 * fields. In grid boxes with no homogeneity adjusted data, GHCN raw 
 * data was used to provide the greatest possible global coverage. 
 * Each month of data consists of 2592 gridded data points produced 
 * on a 5 X 5 degree basis for the entire globe (72 longitude X 36 
 * latitude grid boxes).
 * 
 * Gridded data for every month from January 1900 to the most recent 
 * month is available. The data are precipitation anomalies in 
 * millimeters. Each gridded value was multiplied by 100 and written 
 * to file as an integer. Missing values are represented by the value 
 * -32768.
 * 
 * @author Steve.Ansari@noaa.gov
 *
 */
public class GHCNGriddedPrecipIOServiceProvider extends AbstractIOServiceProvider {
	
	
	
	
	@Override
	public String getFileTypeDescription() {
		return "Overview and Description of Data: "+
			"This data set contains gridded precipitation anomalies calculated "+
			"from the GHCN V2 monthly precipitation data set. 2064 homogeneity "+ 
			"adjusted precipitation stations (from the U.S., Canada, and Former "+ 
			"Soviet Union) were combined with a data set containing 20590 raw "+ 
			"precipitation stations throughout the world to create these gridded "+ 
			"fields. In grid boxes with no homogeneity adjusted data, GHCN raw "+ 
			"data was used to provide the greatest possible global coverage. "+ 
			"Each month of data consists of 2592 gridded data points produced "+ 
			"on a 5 X 5 degree basis for the entire globe (72 longitude X 36 "+ 
			"latitude grid boxes). \n\n"+
			"Gridded data for every month from January 1900 to the most recent "+ 
			"month is available. The data are precipitation anomalies in "+
			"millimeters. Each gridded value was multiplied by 100 and written "+
			"to file as an integer. Missing values are represented by the value "+
			"-32768.";
	}

	@Override
	public String getFileTypeId() {
		return "GHCN-Gridded";
	}

	@Override
	public boolean isValidFile(RandomAccessFile raf) throws IOException {
		String path = raf.getLocation();
		String filename = path.substring(path.lastIndexOf("/")+1);
		if (filename.equals("grid_prcp_1900-2009.dat")) {
			return (raf.readLine().trim().equals("1 1900"));
		}
		else {
			return false;
		}
	}

	@Override
	public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {

				
		Array dataArray = Array.factory(DataType.INT, new int[]{ 1320, 36, 72});
		Index dataIndex = dataArray.getIndex();
		Array timeArray = Array.factory(DataType.INT, new int[]{ 1320 });
		Index timeIndex = timeArray.getIndex();
		Array latArray = Array.factory(DataType.FLOAT, new int[]{ 36 });
		Index latIndex = latArray.getIndex();
		Array lonArray = Array.factory(DataType.FLOAT, new int[]{ 72 });
		Index lonIndex = lonArray.getIndex();
		
		
		raf.order(RandomAccessFile.LITTLE_ENDIAN);
		raf.seek(0);

		
		SimpleDateFormat sdf = new SimpleDateFormat("MM yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		int t=0;
		while (! raf.isAtEndOfFile() && raf != null) {

			String dateString = raf.readLine();
			if (dateString == null) {
				break;
			}
			System.out.println(dateString);

			for (int y=0; y<36; y++) {
				for (int x=0; x<6; x++) {
					String str = raf.readLine();
					if (str == null) {
						break;
					}
					String[] cols = str.trim().split("\\s+");
					if (cols.length != 12) {
						throw new IOException("12 data columns - not found... - found "+cols.length+"\n"+str);
					}
//					System.out.println(str);
					for (int c=0; c<cols.length; c++) {
//						dataCache[pos++] = Short.parseShort(cols[c]);
						dataArray.setInt(dataIndex.set(t, y, x*cols.length+c), Integer.parseInt(cols[c]));
					}
				}
			}
			try {
//				System.out.println(sdf.parse(dateString.trim()).getTime()/1000/60/60/24);
				timeArray.setInt(timeIndex.set(t), (int)(sdf.parse(dateString.trim()).getTime()/1000/60/60/24));
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			
			t++;
			
			
		}

		for (int y=0; y<36; y++) {
			latArray.setFloat(latIndex.set(y), 87.5f-(y*5.0f));
		}
		for (int x=0; x<72; x++) {
			lonArray.setFloat(lonIndex.set(x), -177.5f+(x*5.0f));
		}
		
		
		ncfile.addDimension(null, new Dimension("time", 1320));
		ncfile.addDimension(null, new Dimension("lat", 36));
		ncfile.addDimension(null, new Dimension("lon", 72));
		
		Variable v = new Variable(ncfile, null, null, "precipitation", DataType.INT, "time lat lon");
		v.addAttribute(new Attribute("scale_factor", 0.01));
		v.addAttribute(new Attribute("add_offset", 0));
		v.addAttribute(new Attribute("_FillValue", -32768));
		v.addAttribute(new Attribute("units", "mm"));
		v.addAttribute(new Attribute("standard_name", "precipitation_amount_anomaly"));
		v.addAttribute(new Attribute("coordinates", "time lat lon"));
		v.setCachedData(dataArray);
		ncfile.addVariable(null, v);
		
		Variable vTime = new Variable(ncfile, null, null, "time", DataType.INT, "time");
		vTime.addAttribute(new Attribute("long_name", "Year and Month"));
		vTime.addAttribute(new Attribute("units", "days since 1970-01-01"));
		vTime.setCachedData(timeArray);
		ncfile.addVariable(null, vTime);
		
		Variable vLat = new Variable(ncfile, null, null, "lat", DataType.FLOAT, "lat");
		vLat.addAttribute(new Attribute("long_name", "Latitude"));
		vLat.addAttribute(new Attribute("units", "degrees_north"));
		vLat.setCachedData(latArray);
		ncfile.addVariable(null, vLat);

		Variable vLon = new Variable(ncfile, null, null, "lon", DataType.FLOAT, "lon");
		vLon.addAttribute(new Attribute("long_name", "Longitude"));
		vLon.addAttribute(new Attribute("units", "degrees_east"));
		vLon.setCachedData(lonArray);
		ncfile.addVariable(null, vLon);
		
		
		ncfile.finish();
		
	}
	
	/**
	 * Overview and Description of Data
	 * This data set contains gridded precipitation anomalies calculated 
	 * from the GHCN V2 monthly precipitation data set. 2064 homogeneity 
	 * adjusted precipitation stations (from the U.S., Canada, and Former 
	 * Soviet Union) were combined with a data set containing 20590 raw 
	 * precipitation stations throughout the world to create these gridded 
	 * fields. In grid boxes with no homogeneity adjusted data, GHCN raw 
	 * data was used to provide the greatest possible global coverage. 
	 * Each month of data consists of 2592 gridded data points produced 
	 * on a 5 X 5 degree basis for the entire globe (72 longitude X 36 
	 * latitude grid boxes).
	 * 
	 * Gridded data for every month from January 1900 to the most recent 
	 * month is available. The data are precipitation anomalies in 
	 * millimeters. Each gridded value was multiplied by 100 and written 
	 * to file as an integer. Missing values are represented by the value 
	 * -32768.
	 * 
	 * The data are formatted by year, month, latitude and longitude. 
	 * There are twelve longitude grid values per line, so there are 6 
	 * lines (72/12 = 6) for each of the 36 latitude bands. Longitude 
	 * values are written from 180 W to 180 E, and latitude values from 
	 * 90 N to 90 S. Data for each month is preceded by a label containing 
	 * the month and year of the gridded data.
	 * 
	 * <pre>
	 * for year = begyr to endyr
     *  for month = 1 to 12
     *   format(2i7) month,year
     *    for ylat = 1 to 36 (85-90N,80-85N,...,80-85S,85-90S)
     *     format(12i7) 180-175W,175-170W,...,130-125W,125-120W
     *     format(12i7) 120-115W,175-170W,...,70-65W,65-60W
     *     format(12i7) 60-55W,55-50W,...,10-5W,5-0W
     *     format(12i7) 0-5E,5-10E,...,50-55E,55-60E
     *     format(12i7) 60-65E,65-70E,...,110-115E,115-120E
     *     format(12i7) 120-125E,125-130E,...,170-175E,175-180E
     * </pre>
	 */
	@Override
	public Array readData(Variable var, Section section) throws IOException,
			InvalidRangeException {

		// not needed because we are using the setCachedData stuff in the open method
		return null;
	}

	
	public static void main(String[] args) {
		try {
			NetcdfFile.registerIOProvider(GHCNGriddedPrecipIOServiceProvider.class);
			String infile = "E:\\work\\station-data\\grid_prcp_1900-2009.dat";
			NetcdfFile ncfile=NetcdfFile.open(infile);
			FileWriter.writeToFile(ncfile, "E:\\work\\station-data\\grid_prcp_1900-2009.nc").close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
