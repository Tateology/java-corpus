package gov.noaa.ncdc.wct;

import gov.noaa.ncdc.common.URLTransfer;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTTransferProgressDialog;

import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

public final class ResourceUtils {

    private static final Logger logger = Logger.getLogger(ResourceUtils.class.getName());

    
    public final static File RESOURCE_CACHE_DIR = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+"resources"); 
    public final static File CONFIG_CACHE_DIR = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+"config"); 
    
    private static ResourceUtils singleton = null;
    private HashMap<URL, Long> urlLastModMap = new HashMap<URL, Long>();
    private HashMap<URL, Long> urlContentLengthMap = new HashMap<URL, Long>();

    private ResourceUtils() {        
    }

    public static ResourceUtils getInstance() {
        if (singleton == null) {
            singleton = new ResourceUtils();
        }
        return singleton;
    }


    


    public URL getResource(URL rmtUrl, File cacheDir, Frame parent) throws IOException {

        if (! cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        String filename = rmtUrl.toString().substring(rmtUrl.toString().lastIndexOf("/")+1);
        File locFile = new File(cacheDir+File.separator+filename);
        
        
        boolean doTryTransfer = true;

        // see if we even have a local cached version
        if (! locFile.exists()) {
            doTryTransfer = true;
        }
        // check hashmaps to see if we have already connected and cached the info, then check mod time and length
        else if (urlLastModMap.containsKey(rmtUrl) && urlContentLengthMap.containsKey(rmtUrl)) {
            long rmtLastMod = urlLastModMap.get(rmtUrl);
            long rmtContentLength = urlContentLengthMap.get(rmtUrl);
            logger.info("hashmap rmt size="+rmtContentLength+" mod="+new Date(rmtLastMod));
            logger.info("hashmap loc size="+locFile.length()+" mod="+new Date(locFile.lastModified()));
            if (rmtLastMod > locFile.lastModified() || rmtContentLength != locFile.length()) {
                doTryTransfer = true;
            }
            else {
                doTryTransfer = false;
            }
        }
        
        
        
        try {

        if (doTryTransfer) {

            File tmpFile = new File(locFile.toString()+".tmp");
            URLTransfer transfer = new URLTransfer(rmtUrl, new FileOutputStream(tmpFile));

            URLConnection conn = transfer.getURLConnection();
            long rmtLastMod = conn.getLastModified();
            long rmtContentLength = conn.getContentLength();


            logger.info("rmt size="+rmtContentLength+" mod="+new Date(rmtLastMod));
            logger.info("loc size="+locFile.length()+" mod="+new Date(locFile.lastModified()));

            if (rmtLastMod > locFile.lastModified() || rmtContentLength != locFile.length()) {
                locFile.delete();
                
                if (parent != null) {
                    WCTTransferProgressDialog progress = new WCTTransferProgressDialog(parent, transfer, rmtContentLength);
                    progress.setProgressBarInfo(filename);
                    progress.setTitleLabelInfo("Transferring file to local cache");
                    transfer.addDataTransferListener(progress);
                    progress.setVisible(true);
                    
                    transfer.run();

                	progress.dispose();
                }
                else {
                	transfer.run();
                }
                
                transfer.close();
                tmpFile.renameTo(locFile);
                

            }
            else {
                transfer.close();
                tmpFile.delete(); // just in case it is still around
            }
            
            // add to hashmap
            urlLastModMap.put(rmtUrl, rmtLastMod);
            urlContentLengthMap.put(rmtUrl, rmtContentLength);

        }
        
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }


        //        URL jarUrl = WCTTransfer.getURL(rmtJarUrl, resourceDir, force, parent);
        URL locUrl = locFile.toURI().toURL();
        logger.info("resource url: "+locUrl);

        return locUrl;
        
    }





    public URL getJarResource(URL rmtJarUrl, File cacheDir, String path, Frame parent) throws IOException {

        URL dataUrl = null;
        
        // 1. Try to get from classpath first
        dataUrl = ResourceUtils.class.getResource(path);
        logger.info("CLASSPATH RESOURCE URL: "+dataUrl);
        
        // 2. Try to open stream just to make sure URL can be resolved
        try {
            InputStream in = dataUrl.openStream();
            in.close();
        } catch (Exception e) {
            logger.info("CLASSPATH RESOURCE ERROR: "+e);
            logger.info("USING REMOTE/LOCAL CACHE JAR");
            dataUrl = null;
        }
        
        // 3. Load remote or local cached resource jar
        if (dataUrl == null) {
            
            // 2. If we don't have it in the classpath, then load into user resource cache        

            URL jarUrl = getResource(rmtJarUrl, cacheDir, parent);
//            logger.info("jar resource url: "+jarUrl);

            dataUrl = new URL("jar:"+jarUrl.toString()+"!"+path);
//            logger.info("    data url: "+dataUrl);
        }
        else {
            logger.info("FOUND RESOURCE IN CLASSPATH: "+path);
        }


        logger.info("RESOURCE URL: "+dataUrl);
        
        return dataUrl;
    }

















    //    From: http://www.objectdefinitions.com/odblog/2008/workaround-for-bug-id-6753651-find-path-to-jar-in-cache-under-webstart/
    //
    //    public static URL getResource(Class clazz, String name) {
    //        // Get the URL for the resource using the standard behavior
    //        URL result = clazz.getResource(name);
    //
    //        // Check to see that the URL is not null and that it's a JAR URL.
    //        if (result != null && "jar".equalsIgnoreCase(result.getProtocol())) {
    //            // Get the URL to the "clazz" itself.  In a JNLP environment, the "getProtectionDomain" call should succeed only with properly signed JARs.
    //            URL classSourceLocationURL = clazz.getProtectionDomain().getCodeSource().getLocation();
    //            // Create a String which embeds the classSourceLocationURL in a JAR URL referencing the desired resource.
    ////            String urlString = MessageFormat.format("jar:{0}!/{1}/{2}", classSourceLocationURL.toExternalForm(), packageNameOfClass(clazz).replaceAll("\\.", "/"), name);
    //            String urlString = MessageFormat.format("jar:{0}!/{1}", classSourceLocationURL.toExternalForm(), name);
    //
    //            // Check to see that new URL differs.  There's no reason to instantiate a new URL if the external forms are identical (as happens on pre-1.5.0_16 builds of the JDK).
    //            if (urlString.equals(result.toExternalForm()) == false) {
    //                // The URLs are different, try instantiating the new URL.
    //                try {
    //                    result = new URL(urlString);
    //                } catch (MalformedURLException malformedURLException) {
    //                    throw new RuntimeException(malformedURLException);
    //                }
    //            }
    //        }
    //        return result;
    //    }
    //
    //    public static String packageNameOfClass(Class clazz) {
    //        String result = "";
    //        String className = clazz.getName();
    //        int lastPeriod = className.lastIndexOf(".");
    //
    //        if (lastPeriod > -1) {
    //            result = className.substring(0, lastPeriod);
    //        }
    //        return result;
    //    }

}
