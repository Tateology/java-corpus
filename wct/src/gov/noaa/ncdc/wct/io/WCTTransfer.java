/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 *
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE. 
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.io;


import ftp.FtpBean;
import ftp.FtpException;
import gov.noaa.ncdc.common.URLTransfer;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTTransferProgressBatch;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTTransferProgressDialog;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;

import java.awt.Dialog;
import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * created    November 10, 2004
 */
public class WCTTransfer {
   
    private static final Logger logger = Logger.getLogger(WCTTransfer.class.getName());

   

    /**
     *  Gets a file and saves to WCTConstants.TMP_LOCATION
     *
     * @param  dataURL  URL of data file
     * @return             URL for downloaded file
     * @throws IOException 
     */
    public static URL getURL(URL dataURL) throws IOException {
        return getURL(dataURL, false);
    }
   
    /**
     *  Gets a file and saves to savedir, if it exists, then there is NO overwrite
     *
     * @param  dataURL  URL of data file
     * @param  saveDir    Directory to save file
     * @return             URL for downloaded file
     * @throws IOException 
     */
    public static URL getURL(URL dataURL, final File saveDir) throws IOException {
        return getURL(dataURL, saveDir, false, null, null, new ArrayList<GeneralProgressListener>());
    }

    /**
     *  Gets a file and saves to savedir, if it exists, then there is NO overwrite
     *
     * @param  dataURL  URL of data file
     * @param  saveDir    Directory to save file
     * @param  force       Perform operation if file already exists?
     * @return             URL for downloaded file
     * @throws IOException 
     */
    public static URL getURL(URL dataURL, final File saveDir, boolean force) throws IOException {
        return getURL(dataURL, saveDir, force, null, null, new ArrayList<GeneralProgressListener>());
    }

    /**
     *  Gets a file and saves to WCTConstants.TMP_LOCATION - defaults to non-GUI batch mode
     *
     * @param  dataURL  URL of data file
     * @param  force       Perform operation if file already exists?
     * @return             URL for downloaded file
     * @throws IOException 
     */
    public static URL getURL(final URL dataURL, boolean force) throws IOException {
        return getURL(dataURL, force, null);
    }

    /**
     *  Gets a file and saves to WCTConstants.TMP_LOCATION
     *
     * @param  dataURL  URL of data file
     * @param  force       Perform operation if file already exists?
     * @param  owner       The parent frame for interactive non-batch operations. 
     * If null, we default to batch mode and no pop-up.  Otherwise we get the
     * download progress pop-up.
     * @return             URL for downloaded file
     * @throws IOException 
     */
    public static URL getURL(final URL dataURL, boolean force, Frame owner) throws IOException {
        return getURL(dataURL, null, force, owner);
    }
   
    /**
     *  Gets a file and saves to saveDir
     *
     * @param  dataURL  URL of data file
     * @param  saveDir    Directory to save file
     * @param  force       Perform operation if file already exists?
     * @param  owner       The parent frame for interactive non-batch operations. 
     * If null, we default to batch mode and no pop-up.  Otherwise we get the
     * download progress pop-up.
     * @return             URL for downloaded file
     * @throws IOException 
     */
    public static URL getURL(final URL dataURL, final File saveDir, boolean force, Frame owner) throws IOException {
        return getURL(dataURL, saveDir, force, owner, new ArrayList<GeneralProgressListener>());
    }
   
    /**
     *  Gets a file and saves to saveDir
     *
     * @param  dataURL  URL of data file
     * @param  saveDir    Directory to save file
     * @param  force       Perform operation if file already exists?
     * @param  owner       The parent frame for interactive non-batch operations. 
     * If null, we default to batch mode and no pop-up.  Otherwise we get the
     * download progress pop-up.
     * @return             URL for downloaded file
     * @throws IOException 
     */
    public static URL getURL(final URL dataURL, final File saveDir, boolean force,
            Frame owner, ArrayList<GeneralProgressListener> progressListeners) throws IOException {

        return getURL(dataURL, saveDir, force, owner, null, progressListeners);
    }
   
    /**
     *  Gets a file and saves to saveDir
     *
     * @param  dataURL  URL of data file
     * @param  saveDir    Directory to save file
     * @param  force       Perform operation if file already exists?
     * @param  owner       The parent frame for interactive non-batch operations. 
     * If null, we default to batch mode and no pop-up.  Otherwise we get the
     * download progress pop-up.
     * @return             URL for downloaded file
     * @throws IOException 
     */
    public static URL getURL(final URL dataURL, final File saveDir, boolean force,
            Dialog owner, ArrayList<GeneralProgressListener> progressListeners) throws IOException {

        return getURL(dataURL, saveDir, force, null, owner, progressListeners);
    }
   
   

    public static URL getURL(final URL dataURL, final File saveDir, boolean force,
            ArrayList<GeneralProgressListener> progressListeners) throws IOException {
       
        return getURL(dataURL, saveDir, force, null, null, progressListeners);
    }
   
    private static URL getURL(final URL dataURL, final File saveDir, boolean force,
            Frame frameOwner, Dialog dialogOwner, ArrayList<GeneralProgressListener> progressListeners) throws IOException {
       
        boolean batch = (frameOwner == null && dialogOwner == null);


       
        try {


            if (saveDir == null && dataURL.getProtocol().equals("file")) {
                return dataURL;
            }


            // ---------------------------------------------------------
            // Get file and save to local tmp.dir
            // ---------------------------------------------------------
            String urlString = dataURL.toString();
            int index = urlString.lastIndexOf('/') + 1;
            String filename = urlString.substring(index, urlString.length());
//            filename = filename.replaceAll(":", "-").replaceAll("/", "_");
//            String cacheFilename = URLEncoder.encode(urlString, "UTF-8");
//            String displayFilename = urlString.substring(urlString.lastIndexOf('/') + 1, urlString.length());
           

           
           
            File dir = null;
            if (saveDir == null) {
                String urlPath = urlString.substring(0, urlString.lastIndexOf('/'));
                urlPath = encode(urlPath);
                dir = new File(WCTConstants.getInstance().getDataCacheLocation() + File.separator + urlPath);
            }
            else {
                dir = saveDir;
            }
           
//            File dir = (saveDir == null) ? new File(WCTConstants.getInstance().getDataCacheLocation()) : saveDir;
           
            dir.mkdirs();

            // SPECIAL CASE: Check for NWS Real-Time files
            // --- If they are present, build a unique directory for the file
            // save to a unique directory within the general cache root dir outside
            // of Unidata DiskCache, which doesn't handle directories.
//            if (displayFilename.startsWith("sn.")) {
            if (filename.startsWith("sn.")) {
//                String urlPath = urlString.substring(0, urlString.lastIndexOf('/'));
//                urlPath = urlPath.replaceAll(":", "-").replaceAll("/", "_");
////                dir = new File(dir + File.separator + urlPath);
//                dir = new File(WCTConstants.getInstance().getDataCacheLocation() + File.separator + urlPath);
//                dir.mkdirs();
               
               
//                cacheFilename = cacheFilename + "." + new SimpleDateFormat("ddHHmm").format(new Date());
                filename = filename + "." + new SimpleDateFormat("ddHHmm").format(new Date());
//                System.out.println("NEW NWS FILENAME " + cacheFilename );
                System.out.println("NEW NWS FILENAME " + filename );
            }
           
           
            File file = null;
//            String plainFilename = "";
//            if (dataURL.getProtocol().equals("file")) {
//                String dirString = FileUtils.toFile(new URL(urlString.substring(0, urlString.lastIndexOf('/')))).toString();
//                if (dirString.equals(WCTConstants.getInstance().getDataCacheLocation())) {
//                    String ds = URLDecoder.decode(displayFilename, "UTF-8");
//                    plainFilename = ds.substring(ds.lastIndexOf('/')+1);
//                    file = new File(dir + File.separator + plainFilename);
//                }
//                else {
//                    file = new File(dir + File.separator + displayFilename);
//                }
//            }
//            else {
//                file = new File(dir + File.separator + cacheFilename);
//            }
            file = new File(dir + File.separator + filename);
            

            
            // TODO - build out checking against size and dates
            boolean checkSize = true;
            boolean checkDate = true;
            Date localFileTimestamp = null;
            long localFileSize = 0L;
            if (file.exists()) {
            	localFileTimestamp = new Date(file.lastModified());
            	localFileSize = file.length();
            }
           
           
           

            if (! file.exists() || force || file.length() == 0) {

                System.out.println("WCT TRANSFER **************** "+dataURL+" \n    ----> "+file);              
                logger.info("WCT TRANSFER **************** "+dataURL);              

                if (dataURL.getProtocol().equals("file")) {
                	
//                	if (checkSize) {
//                		if (localFileSize == FileUtils.toFile(dataURL).length()) {
//                			return new URL(URLDecoder.decode(file.toURI().toURL().toString(), "UTF-8"));
//                		}
//                	}
                	
                	
                	
                    // convert custom partially encoded URL
//                    String dirString = urlString.substring(0, urlString.lastIndexOf('/')).toString();
//                    dirString = FileUtils.toFile(new URL(dirString.substring(0, dirString.lastIndexOf('/')))).toString();
//                    FileUtils.copyFile(new File(dirString + File.separator + displayFilename), file, true);
//                    FileUtils.copyFile(new File(dirString + File.separator + filename), file, true);
                    FileUtils.copyFile(FileUtils.toFile(dataURL), file, true);
                   
//                    if (dirString.equals(WCTConstants.getInstance().getDataCacheLocation())) {
//                        FileUtils.copyFile(new File(dirString + File.separator + displayFilename), file, true);
//                    }
//                    else {
//                        FileUtils.copyFile(new File(dirString + File.separator + displayFilename), file, true);
//                    }
                }
                // Use FtpBean for FTP transfers
                else if (dataURL.getProtocol().equals("ftp")) {

                    logger.info("FTP BEAN: Saving "+dataURL.getFile() + " TO "+ file.toString());


                    final FtpBean ftp = new FtpBean();
                    final File localTmpFile = new File(file.toString()+".ftp");
                    final File localFile = file;


                    if (batch) {
                        final WCTTransferProgressBatch transferProgress = new WCTTransferProgressBatch(ftp, progressListeners);
                       
                        try {
                            ftp.ftpConnect(dataURL.getHost(), "anonymous", "wct.ncdc.at.noaa.gov");                 
                            logger.fine("ftp.getBinaryFile("+dataURL.getFile()+", "+localTmpFile.toString());     
                           
                            ftp.getBinaryFile(dataURL.getFile(), localTmpFile.toString(), transferProgress);                    
                            localTmpFile.renameTo(localFile);                       
                            try {
                                ftp.close();
                            } catch (Exception ee) {
                                ee.printStackTrace();
                                throw new java.net.ConnectException(ee.toString());
                            }                    

                        } catch (Exception e) {
                            // close the progress monitor
                            ftp.close();
                            if (! file.delete()) {
                                logger.warning("ERROR DELETING PARTIAL FILE: "+localTmpFile);
                            }
                            try {
                               ftp.close();
                            } catch (Exception ee) {
                               ee.printStackTrace();
                            }
                            e.printStackTrace();
                            throw new java.net.ConnectException(e.toString());
                        }


                    }                                   
                    else {                 
                       

                        WCTTransferProgressDialog transferProgress = null;
                        if (frameOwner != null) {
                            transferProgress = new WCTTransferProgressDialog(frameOwner, ftp);
                        }
                        else if (dialogOwner != null) {
                            transferProgress = new WCTTransferProgressDialog(dialogOwner, ftp);
                        }
//                        transferProgress.setProgressBarInfo(displayFilename);
                        transferProgress.setProgressBarInfo(filename);
                        transferProgress.setTitleLabelInfo("Transferring file to local cache");

                        try {

//                            foxtrot.Worker.post(new foxtrot.Task() {
//                                public Object run() throws Exception {                    

                                    ftp.ftpConnect(dataURL.getHost(), "anonymous", "wct.ncdc.at.noaa.gov");                 
                                    ftp.getBinaryFile(dataURL.getFile(), localTmpFile.toString(), transferProgress);                    
                                    localTmpFile.renameTo(localFile);                       
//                                    return "DONE";
//                                }
//                            });

                            // close the progress monitor
                            transferProgress.dispose();

                            try {
                                ftp.close();
                            } catch (Exception ee) {
                                throw new java.net.ConnectException(ee.toString());
                            }


                        } catch (Exception e) {
                            // close the progress monitor
                            transferProgress.dispose();

                            if (! file.delete()) {
                                logger.warning("ERROR DELETING PARTIAL FILE: "+localTmpFile);
                            }
                            try {
                               ftp.close();
                            } catch (Exception ee) {
                               ee.printStackTrace();
                            }

                            e.printStackTrace();
                            throw new java.net.ConnectException(e.toString());
                        }

                    }

                }
                // http, etc...
                else {


                    final DataOutputStream os = new DataOutputStream(new BufferedOutputStream(
                            new FileOutputStream(file), 1 * 1024));              


                    URLTransfer urlTransfer = new URLTransfer(dataURL, os);
                    WCTTransferProgressDialog transferProgress = null;
                    try {
                        if (! batch) {
                            if (frameOwner != null) {
                                transferProgress = new WCTTransferProgressDialog(frameOwner, urlTransfer,
                                        urlTransfer.getURLConnection().getContentLength());
                            }
                            else if (dialogOwner != null) {
                                transferProgress = new WCTTransferProgressDialog(dialogOwner, urlTransfer,
                                        urlTransfer.getURLConnection().getContentLength());
                            }

//                            transferProgress.setProgressBarInfo(displayFilename);
                            transferProgress.setProgressBarInfo(filename);
                            transferProgress.setTitleLabelInfo("Transferring file to local cache");
                            urlTransfer.addDataTransferListener(transferProgress);
                        }
                        else {
                            WCTTransferProgressBatch transferProgressBatch = new WCTTransferProgressBatch(urlTransfer,  
                                    urlTransfer.getURLConnection().getContentLength(), progressListeners);
                            urlTransfer.addDataTransferListener(transferProgressBatch);
                        }
                        urlTransfer.run();
                        urlTransfer.close();
                        
                        if (transferProgress != null && transferProgress.isDownloadError()) {
                        	System.out.println("download error - erasing partial file...");
                        	file.delete();
                        }
                        
                    } catch (IOException e) {
                        urlTransfer.close();
                        os.close();
                        throw e;
                    }

                    os.flush();
                    os.close();

                }

            }

            if (file.exists()) {
            	return new URL(URLDecoder.decode(file.toURI().toURL().toString(), "UTF-8"));
            }
            else {
            	return null;
            }
//            return new URL(file.toURI().toURL().toString());

        } catch (FtpException e) {
            throw new IOException(e);
        } catch (IOException e) {
            throw new IOException(e);
        }

    }


    /**
     * Deletes all files in the WCTConstants.TMP_LOCATION directory that are older than WCTConstants.TMP_LOCATION_LIFETIME
     */
    public static void clearTempDirectory() {

        logger.fine("CLEANING TEMP DIRECTORY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");     
        gov.noaa.ncdc.common.SwingWorker worker = new gov.noaa.ncdc.common.SwingWorker() {
            public Object construct() {

               
                StringBuilder sb = new StringBuilder();
               
//                DiskCache.showCache(System.out);
               
//                DiskCache.cleanCache(WCTConstants.getInstance().getDataCacheSizeLimit(), sb); // 1 Gig limit               
       
               
               
               
               
                Comparator<File> fileAgeComparator = new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return  (f2.lastModified() - f1.lastModified()) > 0 ? 1 : -1;
                      }
                };
               
//                DiskCache.cleanCache(WCTConstants.getInstance().getDataCacheSizeLimit(), fileAgeComparator, sb);
                
                // clean based on size
                cleanCache(
                		new File(WCTConstants.getInstance().getDataCacheLocation()), 
                		WCTConstants.getInstance().getDataCacheSizeLimit(), 
                		fileAgeComparator, sb);
                // clean any files older than 2 weeks
                cleanCache(
                		new File(WCTConstants.getInstance().getDataCacheLocation()), 
                		1000L*60*60*24*14, sb);
               
                System.out.println(sb);
               

                // clean cache from legacy cache remnants along with NWS real-time cache directories
                deleteAllFilesInDir(new File(WCTConstants.getInstance().getCacheLocation()), true,
                        new File(WCTConstants.getInstance().getCacheLocation()+File.separator+"unidata-bdb"),
                        new File(WCTConstants.getInstance().getCacheLocation()+File.separator+"data"),
                        new File(WCTConstants.getInstance().getCacheLocation()+File.separator+"objdata"),
                        new File(WCTConstants.getInstance().getCacheLocation()+File.separator+"resources"),
                        new File(WCTConstants.getInstance().getCacheLocation()+File.separator+"config"));

                return "DONE";
            }
        };
        worker.start();


    }     
   
   
    private static void deleteAllFilesInDir(File dir, boolean recursive, File... ignoreFiles) {
        File[] files = dir.listFiles();
        for (int i=0; i<files.length; i++) {
//            System.out.println(files[i]);
           
            boolean ignoreFile = false;
            for (int n=0; n<ignoreFiles.length; n++) {
                if (files[i].equals(ignoreFiles[n])) {
                    ignoreFile = true;
                    System.out.println("CACHE DELETE: IGNORING "+files[i]);
                }
            }

            if (files[i].isDirectory() && recursive && ! ignoreFile) {
                System.out.println("Deleting Temp Directory: "+files[i]);
                deleteAllFilesInDir(files[i], true);
                files[i].delete();
            }
//            else if (System.currentTimeMillis()-files[i].lastModified() > WCTConstants.TMP_LOCATION_LIFETIME) {
            else if (! files[i].isDirectory() && System.currentTimeMillis()-files[i].lastModified() > 1) {
                logger.info("-- DELETING TEMP FILE ::::::::::::: "+files[i].toString());
                System.out.println("Deleting Temp File: "+files[i]);
                files[i].delete();
            }
        }
    }

    
    private static void deleteEmptyDirs(File dir, boolean recursive, File... ignoreFiles) {
        File[] files = dir.listFiles();
        for (int i=0; i<files.length; i++) {
//            System.out.println(files[i]);
           
            boolean ignoreFile = false;
            for (int n=0; n<ignoreFiles.length; n++) {
                if (files[i].equals(ignoreFiles[n])) {
                    ignoreFile = true;
                    System.out.println("CACHE DELETE: IGNORING "+files[i]);
                }
            }

            if (files[i].isDirectory() && recursive && ! ignoreFile) {
                System.out.println("Crawling - looking for empty directories: "+files[i]);
                deleteEmptyDirs(files[i], true);
            }
            if (files[i].isDirectory() && ! ignoreFile) {
            	if (files[i].listFiles().length == 0) {
            		System.out.println("Deleting Empty Temp Directory: "+files[i]);
            		files[i].delete();
            	}
            }
        }
    }


   
    /**
     * Delete all cache files older than the specified max life.
     * @param dir
     * @param maxLifeInMillis
     */
    public static void cleanCache(File dir, long maxLifeInMillis, StringBuilder sb) {
        for (File file : dir.listFiles()) {
            if ( (System.currentTimeMillis()-file.lastModified()) > maxLifeInMillis) {
                sb.append("  -- Deleting expired file: "+file+"\n");
                if (file.isDirectory()) {
                    try {
						FileUtils.deleteDirectory(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
                else {
                	file.delete();
                }
            }
        }
    }

    /**
     * Remove files if needed to make cache have less than maxBytes bytes file sizes.
     * This will remove files in sort order defined by fileComparator.
     * The first files in the sort order are kept, until the max bytes is exceeded, then they are deleted.
     *
     * @param maxBytes       max number of bytes in cache.
     * @param fileComparator sort files first with this
     * @param sbuff          write results here, null is ok.
     */
    static public void cleanCache(File dir, long maxBytes, Comparator<File> fileComparator, StringBuilder sbuff) {
    	if (sbuff != null) sbuff.append("DiskCache clean maxBytes= " + maxBytes + " on dir " + dir + "\n");


    	File[] files = dir.listFiles();
    	//      List<File> fileList = Arrays.asList(files);
    	//    Collections.sort(fileList, fileComparator);
    	Collection<File> fileCollection = FileUtils.listFiles(dir, null, true);
    	ArrayList<File> fileList = new ArrayList<File>(fileCollection);
    	Collections.sort(fileList, fileComparator);

    	long total = 0, total_delete = 0;
    	for (File file : fileList) {
//    		sbuff.append("Total="+total+" bytes   "+new Date(file.lastModified())+ " ---- " + file+" \n");

    		if (file.length() + total > maxBytes) {
    			total_delete += file.length();
    			if (sbuff != null) sbuff.append(" delete " + file + " (" + file.length() + ")\n");
    			file.delete();
    		} else {
    			total += file.length();
    		}
    	}
    	if (sbuff != null) {
    		sbuff.append("Total bytes deleted= " + total_delete + "\n");
    		sbuff.append("Total bytes left in cache= " + total + "\n");
    	}
    	
    	deleteEmptyDirs(dir, true);
    }




   
    /**
     *
     * @param url data URL, local or remote
     * @return
     */
    public static boolean isInCache(URL url) {
        return getFileInCache(url).exists() && getFileInCache(url).length() > 0;
    }

    /**
     *
     * @param url data URL, local or remote
     * @throws Exception
     */
    public static void removeFromCache(URL url) throws Exception {
        if (! getFileInCache(url).delete()) {
            throw new Exception("Problem deleting file from cache: "+getFileInCache(url));
        }
    }

   
    /**
     * Get local cache File from data URL
     * @param url data URL, local or remote
     * @return
     */
    public static File getFileInCache(URL url) {
        String urlString = url.toString();
       
        int index = urlString.lastIndexOf('/') + 1;
        String filename = urlString.substring(index, urlString.length());
//        filename = filename.replaceAll(":", "-").replaceAll("/", "_");
        try {
            String urlPath = urlString.substring(0, urlString.lastIndexOf('/'));
            urlPath = encode(urlPath);

            File file = new File(WCTConstants.getInstance().getDataCacheLocation() +
                    File.separator + urlPath + File.separator + filename);
//            File file = new File(WCTConstants.getInstance().getDataCacheLocation() + File.separator + filename);
//            File file = new File(WCTConstants.getInstance().getDataCacheLocation() + File.separator + URLEncoder.encode(urlString, "UTF-8"));
            return file;
           
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
   
   

    
    
    private static String encode(String s) throws UnsupportedEncodingException {
        return s.replaceAll(":", "~c~").replaceAll("/", "~s~").
        	replaceAll("@", "~a~").replaceAll("\\?", "~q~").replaceAll("&", "~m~");
        
        
//        return URLEncoder.encode(s, "UTF-8");       
    }
//    private static String decode(String s) throws UnsupportedEncodingException {
//        return s.replaceAll("-", ":").replaceAll("~", "/");
//        return URLDecoder.decode(s, "UTF-8");       
//    }

}


