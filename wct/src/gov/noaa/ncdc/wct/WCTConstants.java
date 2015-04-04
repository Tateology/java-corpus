package gov.noaa.ncdc.wct;

import java.io.File;
import java.io.IOException;

import ucar.nc2.util.DiskCache;

public class WCTConstants {

	/*
	 * The build.properties file changes what version of MapData is created.  This property below determines which
	 * version is actually used.  This allows us to roll out new map data to production before actually configuring 
	 * the Toolkit to use it.
	 */
    public final static String MAP_DATA_JAR_URL = "http://www1.ncdc.noaa.gov/pub/data/wct/dist/wct-MapData-1.4.jar";
    public final static String CONFIG_JAR_URL = "http://www1.ncdc.noaa.gov/pub/data/wct/dist/wct-Config-1.0.jar"; 

    public final static String DEFAULT_CACHE_LOCATION = System.getProperty("user.home") + File.separator + ".wct-cache";
    public final static String DEFAULT_DATA_CACHE_LOCATION = System.getProperty("user.home") + File.separator + ".wct-cache"+ File.separator + "data";    
    public final static long DEFAULT_DATA_CACHE_SIZE_LIMIT = 1024*1024*1024L; // 1 gig

    
    
    private static WCTConstants wctConstants = null;
    private String cacheLocation = DEFAULT_CACHE_LOCATION;
    private String dataCacheLocation = DEFAULT_DATA_CACHE_LOCATION;
    private long dataCacheSizeLimit = DEFAULT_DATA_CACHE_SIZE_LIMIT;
    
    
    private WCTConstants() {
        
        try {
            String userCacheLocation = WCTProperties.getWCTProperty("cacheLocation"); 
            if (userCacheLocation != null) {
                setCacheLocation(userCacheLocation);
            }
        } catch (Exception e) {
            System.err.println("UNABLE TO READ USER CACHE LOCATION - USING DEFAULT: "+e.getMessage());
            cacheLocation = DEFAULT_CACHE_LOCATION;
        }

        try {
            String userDataCacheLocation = WCTProperties.getWCTProperty("dataCacheLocation"); 
            if (userDataCacheLocation != null) {
                setDataCacheLocation(userDataCacheLocation);
            }
        } catch (Exception e) {
            System.err.println("UNABLE TO READ USER DATA CACHE LOCATION - USING DEFAULT: "+e.getMessage());
            dataCacheLocation = DEFAULT_DATA_CACHE_LOCATION;
        }
        
        try {
            String userDataCacheSizeLimit = WCTProperties.getWCTProperty("dataCacheSizeLimit"); 
            if (userDataCacheSizeLimit != null) {
                setDataCacheSizeLimit(Long.parseLong(userDataCacheSizeLimit));
            }
        } catch (Exception e) {
            System.err.println("UNABLE TO READ USER DATA CACHE SIZE LIMIT - USING DEFAULT: "+e.getMessage());
            dataCacheSizeLimit = DEFAULT_DATA_CACHE_SIZE_LIMIT;
        }
        
        
        DiskCache.setRootDirectory(dataCacheLocation);
        DiskCache.setCachePolicy(true);
        new File(cacheLocation).mkdirs();        
        new File(dataCacheLocation).mkdirs();        
    }
    
    
    
    
    public static WCTConstants getInstance() {
        if (wctConstants == null) {
            wctConstants = new WCTConstants();            
        }
        return wctConstants;
    }
    
    
    
    /**
     * Gets the default root directory for the WCT cache.  This root directory will include
     * folders for 'resources' (jar files with shapefile map data), 'config' (jar files
     * with XML configuration data and color maps), 'objdata' (serialized objects that save 
     * settings such as bookmarks and markers) and optionally 'data', which contains the 
     * cache of data files.  This 'data cache' may be in another location, as specified
     * with the 'setDataCacheLocation' method.
     * @return
     */
    public String getCacheLocation() {
        return cacheLocation;
    }

    
    /**
     * Sets the default root directory for the WCT cache.  This root directory will include
     * folders for 'resources' (jar files with shapefile map data), 'config' (jar files
     * with XML configuration data and color maps), 'objdata' (serialized objects that save 
     * settings such as bookmarks and markers) and optionally 'data', which contains the 
     * cache of data files.  This 'data cache' may be in another location, as specified
     * with the 'setDataCacheLocation' method.
     * @throws IOException 
     * 
     */
    public void setCacheLocation(String cacheLocation) throws IOException {
        this.cacheLocation = cacheLocation;
        
        initDir(cacheLocation);
    }

    
    /**
     * Gets the location of the data cache folder.  This folder contains all cached data files
     * including files cached from remote locations, files decompressed and currently all temp
     * files created during KMZ and GeoTIFF export, and more...  Note that the temp file cache
     * may be separated in the future.  This cache is managed with the Unidata DiskCache object
     * and the size limit is set by the 'setDataCacheSizeLimit' method.
     * @return
     */
    public String getDataCacheLocation() {
        return dataCacheLocation;
    }
    
    /**
     * Sets the location of the data cache folder.  This folder contains all cached data files
     * including files cached from remote locations, files decompressed and currently all temp
     * files created during KMZ and GeoTIFF export, and more...  Note that the temp file cache
     * may be separated in the future.  This cache is managed with the Unidata DiskCache object
     * and the size limit is set by the 'setDataCacheSizeLimit' method.
     * @throws IOException 
     */
    public void setDataCacheLocation(String dataCacheLocation) throws IOException {
        this.dataCacheLocation = dataCacheLocation;
        
        initDir(dataCacheLocation);
    }

    
    /**
     * Gets the data cache size limit in bytes, which is managed with the Unidata DiskCache
     * object. 
     * @return
     */
    public long getDataCacheSizeLimit() {
        return dataCacheSizeLimit;
    }

    
    /**
     * Sets the data cache size limit in bytes, which is managed with the Unidata DiskCache
     * object. 
     */
    public void setDataCacheSizeLimit(long dataCacheSizeLimit) {
        this.dataCacheSizeLimit = dataCacheSizeLimit;
    }
    
    
 
    
    private void initDir(String dirString) throws IOException {
        File dir = new File(dirString);
        if (! dir.exists()) {
            if (! dir.mkdirs()) {
                throw new IOException("Unable to create directory: "+dir);
            }
        }
        
        if (! dir.canWrite()) {
            throw new IOException("Directory exists, but do not have write permissions: "+dir);
        }
    }
}
