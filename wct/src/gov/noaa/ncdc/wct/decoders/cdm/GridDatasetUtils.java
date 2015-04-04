package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.WCTUtils;

import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;

import opendap.dap.DAP2Exception;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class GridDatasetUtils {
	
	
	private static boolean globalCaching = true;
	

	private static HashMap<String, GridDataset> gdCache = new HashMap<String, GridDataset>();
	

	/**
	 * Open a GridDataset.  May return a dataset with no grids.  IOException thrown if the dataset
	 * cannot be opened.
	 * @param gridDatasetURL
	 * @param errlog
	 * @return
	 * @throws IOException
	 */
	public static GridDataset openGridDataset(String source, StringBuilder errlog) throws DAP2Exception, IOException {
		
        GridDataset gds = null;     
        
        
//        NetcdfDataset.initNetcdfFileCache(0, 2, 2, 60*10);
        
    	// check if this GridDataset has been closed
    	GridDataset gd = gdCache.get(source);
    	if (gd != null && gd.getNetcdfFile() == null) {        	
    		gdCache.remove(source);
    	}
    	
        // get the dataset if cached
        if (isGlobalCaching() && gdCache.containsKey(source)) {
        	return gdCache.get(source);
        }
        
        
        Formatter fmter = new Formatter();
        
		// 1. Try to open the 'standard' way
        try {
        	gds = (GridDataset) FeatureDatasetFactoryManager.open(ucar.nc2.constants.FeatureType.GRID, source, WCTUtils.getSharedCancelTask(), fmter);
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        	System.out.println("Trying to manually open via OPeNDAP");
        
        	// 2. Try to open manually via OPeNDAP
        	if (source.startsWith("http:")) {
        		source = source.replace("http:", "dods:");
        		gds = (GridDataset) FeatureDatasetFactoryManager.open(ucar.nc2.constants.FeatureType.GRID, source, WCTUtils.getSharedCancelTask(), fmter);
        	}
        }

        errlog.append(fmter.toString());
        
        if (gds == null) { 
            throw new IOException("Can't open GRID at location= "+source+"; error message= "+errlog);
        }
        
        
        
        if (isGlobalCaching()) {
        	for (String key : gdCache.keySet()) {
        		gdCache.get(key).close();
        	}
        	
        	gdCache.clear();
        	gdCache.put(source, gds);
        }
        
        
        
        return gds;
	}



	
	
	
	

	public static void setGlobalCaching(boolean globalCaching) {
		GridDatasetUtils.globalCaching = globalCaching;
	}


	public static boolean isGlobalCaching() {
		return globalCaching;
	}








	public static void scheduleToClose(GridDataset gds) {
		// previous file is closed when a new file is requested
	}
	
	
}
