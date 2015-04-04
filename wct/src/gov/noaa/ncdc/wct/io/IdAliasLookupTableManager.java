package gov.noaa.ncdc.wct.io;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class IdAliasLookupTableManager {
    
    private static IdAliasLookupTableManager singleton;    
    private static ArrayList<String> loadedTableList = new ArrayList<String>();
    private static HashMap<String, String> lutMap = new HashMap<String, String>();
    
    private IdAliasLookupTableManager() {        
    }
    
    public static IdAliasLookupTableManager getInstance() {
        if (singleton == null) {
            singleton = new IdAliasLookupTableManager();
        }
        return singleton;
    }
    
    
    
    /**
     * Adds lookup table into internal hashmap.  Key-value pairs must be separated by '|' in flat file.
     * @param path
     * @throws IOException
     */
    public void addLookupTable(String path) throws IOException {
        if (! loadedTableList.contains(path)) {
            
//            System.out.println("ProductLookupTableManager: LOADING: '"+path+"'");
            URL url = ResourceUtils.getInstance().getJarResource(
                    new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, path, null);
            
            
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            if (br.readLine() == null) {
                throw new IOException("UNABLE TO READ HEADER LINE FOR PRODUCT LOOKUP TABLE: "+path);
            }
            while ( (str = br.readLine()) != null) {
                String[] cols = str.split("\\|");
                lutMap.put(cols[0], cols[1]);
            }
            br.close();
            
            loadedTableList.add(path);
        }
        else {
//            System.out.println("ProductLookupTableManager: " + path + " IS ALREADY LOADED");
        }
        
    }
    
    
    
    public String getIdAlias(String idKey) {
        return lutMap.get(idKey);
    }

}
