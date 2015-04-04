package gov.noaa.ncdc.wct;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class WCTProperties {

    private static WCTProperties singleton;

    private Properties props = new Properties();

    private WCTProperties() {
        
        try {
            migrateProperties();
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void migrateProperties() throws IOException {        
        File jnxPropFile = new File(System.getProperty("user.home")+File.separator+".jnx");
        File wctPropFile = new File(System.getProperty("user.home")+File.separator+".wct");
        if (! wctPropFile.exists() && jnxPropFile.exists()) {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(jnxPropFile);
            props.load(fis);
            fis.close();
            FileOutputStream fos = new FileOutputStream(wctPropFile);
            props.store(fos, "WCT USER PROPERTIES");
            fos.close();
        }
    }
    
    
    private void init() throws IOException {
        
        File propFile = new File(System.getProperty("user.home")+File.separator+".wct");
        if (propFile.exists()) {
            FileInputStream fis = new FileInputStream(propFile);
            props.load(fis);
            fis.close();
        }
    }
    
    
    public static WCTProperties getInstance() {
        if (singleton == null) {
            singleton = new WCTProperties();
        }
        return singleton;
    }
    
    public Properties getProperties() {
        return props;
    }



    public static String getWCTProperty(String key) {
        return getWCTProperty(new String[] {key})[0];
    }

    public static String[] getWCTProperty(String[] key) {

        String[] str = new String[key.length];
        try {
            Properties props = getInstance().getProperties();
            for (int i=0; i<key.length; i++) {
                str[i] = props.getProperty(key[i]);
            }
        } catch (Exception e) {
        }
        return str;
    }


    public static void setWCTProperty(String key, String property) {
        setWCTProperty(new String[] {key}, new String[] {property});
    }

    public static void setWCTProperty(String[] key, String[] property) {

        try {
            File propFile = new File(System.getProperty("user.home")+File.separator+".wct");
            Properties props = getInstance().getProperties();
            // Load pre-existing properties
            if (propFile.exists()) {
                FileInputStream fis = new FileInputStream(propFile);
                props.load(fis);
                fis.close();
            }
            // Set desired properties
            for (int i=0; i<key.length; i++) {
                props.setProperty(key[i], property[i]);
            }

            FileOutputStream fos = new FileOutputStream(propFile);
            props.store(fos, "WCT USER PROPERTIES");
            fos.close();
        } catch (Exception e) {
        }

    }







}
