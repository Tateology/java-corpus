//$Id: AreaIosp.java,v 1.1.1.1 2006/10/23 15:47:56 kknapp Exp $
package gov.noaa.ncdc.iosp.area;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

/**
 * This routine allows ncjava to access McIdas AREA files
 * specifically, GOES Area files.
 *
 * @author Ken Knapp
 * @since 2006-10-16
 */
public class AreaIosp extends AbstractIOServiceProvider {

    private static final Logger logger = Logger.getLogger(AreaIosp.class.getName());

    

    protected AreaIo localFile = null;
    private ucar.unidata.io.RandomAccessFile raFile = null;


    public boolean isValidFile( ucar.unidata.io.RandomAccessFile raf ) {
        return AreaIo.isValidFile( raf );
    }

    public void open( RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask )
        throws IOException
    {
        this.raFile = raf;
        localFile = new AreaIo(); 
        try {
            localFile.setAttributesAndVariables( raf, ncfile );
        } catch ( Exception e ) { logger.severe(e.toString()); }
    }

    public ucar.ma2.Array readData( Variable v2, Section section ) 
    throws IOException, InvalidRangeException
    {
        ucar.ma2.Array dataArray = null;

        try {
            dataArray = localFile.getVariableValues( v2, section );
        } catch (InvalidRangeException e ) { logger.severe(e.toString()); }

        return dataArray;
    }



    //methods not currently developed.
	public Array readNestedData(Variable v2, List section) throws IOException, InvalidRangeException {
		throw (new IllegalStateException("No nested variables in GOES datasets."));
	}

	public String getDetailInfo() {
		return "";
	}

	public boolean syncExtend() {
		return false;
	}

	public String toStringDebug(Object o) {
		return null;
	}

	public boolean sync() {
		return false;
	}

	public void close() throws IOException {
		logger.fine("Closing area file-->");
		localFile.close();
		logger.fine("Closing " + this.localFile);
		if (this.raFile != null) {
			logger.fine("Releasing " + this.raFile.getLocation());
			this.raFile.close();
			this.raFile = null;
		}
		this.localFile = null;

		return;
	}
    

	
	
    public void setProperties( List iospProperties) { return; }

    public void setSpecial(Object arg0) {
      
    }

    public String getFileTypeDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getFileTypeId() {
        // TODO Auto-generated method stub
        return null;
    }

    
    
    
    
    
    
}

