package steve.test.netcdf;

import java.util.Date;

import ucar.nc2.NetcdfFile;

/**
 * This program will accept input of lat/lon/time(/runtime/height) and values.  The program will 
 * keep track of if the data are regularly or irregularly spaced.  If the data are regularly
 * spaced, 1-dimensional lat/lon coordinate axes will be created.
 * If the data are irregularly spaced, 2-dimensional lat/lon axes will be created.
 * To keep the memory use low, the input data must be sorted by time (and optionally height and runtime).
 * As a new time slice
 * @author steve.ansari
 *
 */
public interface GridProducer {
	
	/**
	 * Sets the active variable for which 'addCell' adds cells to.  A second
	 * call to this method for a different variable will finish all population  
	 * of the previous variable and will write/stream that variable to disk.
	 * The dimensions for the variable are read and used to determine if 
	 * any new dimensions need to be added to the underlying NetCDF file.
	 * @param var
	 */
	public void setActiveVariable(String varName);
	
	
	public void addCell(double[] lat, double[] lon, Date[] time, double[] value) throws GridProducerException;
	public void addCell(double[] lat, double[] lon, Date[] time, double[] height, double[] value) throws GridProducerException;
	public void addCell(double[] lat, double[] lon, Date[] time, Date[] runtime, double[] height, double[] value) throws GridProducerException;
	
	/**
	 * The directory for the output temporary netcdf 3 file
	 * @param outputFile
	 */
	public void setOutputDirectory(String outputDir);
	
	
	/**
	 * Get underlying NetcdfFile object.  Data is streamed to a temporary NetCDF file
	 * @return
	 */
	public NetcdfFile getNetcdfFile();
	
	
	/**
	 * Exception thrown if the data is not ordered by time and height/runtime (if applicable).
	 * @author steve.ansari
	 *
	 */
	public class GridProducerException extends Exception {
		public GridProducerException(String message) {
			super(message);
		}
	}
}
