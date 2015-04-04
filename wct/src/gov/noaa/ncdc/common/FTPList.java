package gov.noaa.ncdc.common;

import ftp.FtpBean;
import ftp.FtpException;
import ftp.FtpListResult;
import gov.noaa.ncdc.wct.io.FileInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *  Helper class that interacts with FTP client, in this case
 *
 * @author     steve.ansari
 * @created    August 9, 2004
 */
public class FTPList {

    /**
     *  Gets the Files only (not directories or links) of a FTP directory
     *
     * @param  server           Description of the Parameter
     * @param  username         Description of the Parameter
     * @param  pw               Description of the Parameter
     * @param  mode             Description of the Parameter
     * @param  remoteDirectory  Description of the Parameter
     * @return                  The directoryList value
     */
    public FileInfo[] getDirectoryList(String server,
            String username,
            String pw,
            String mode,
            String remoteDirectory
    ) throws FtpException, IOException  {

    	return getDirectoryList(server, username, pw, mode, new String[] { remoteDirectory });
    }

    /**
     *  Gets the Files only (not directories or links) of a FTP directory
     *
     * @param  server           Description of the Parameter
     * @param  username         Description of the Parameter
     * @param  pw               Description of the Parameter
     * @param  mode             Description of the Parameter
     * @param  remoteDirectory  Description of the Parameter
     * @return                  The directoryList value
     */
    public FileInfo[] getDirectoryList(String server,
            String username,
            String pw,
            String mode,
            String[] remoteDirectoryArray
    ) throws FtpException, IOException  {


    	FtpBean ftp = new FtpBean();
    	ArrayList<FileInfo> list = new ArrayList<FileInfo>();


    	ftp.ftpConnect(server, username, pw);

		boolean anyDirFound = false;
    	for (String remoteDirectory : remoteDirectoryArray) {

    		try {

    			ftp.setDirectory(remoteDirectory);
    			FtpListResult listResults = ftp.getDirectoryContent();

    			while (listResults.next()) {
    				if (listResults.getType() == FtpListResult.FILE || 
    						listResults.getType() == FtpListResult.LINK) {

    					list.add(new FileInfo(listResults.getName(), remoteDirectory, listResults.getDate(), listResults.getSize()));
    				}
    			}

    			anyDirFound = true;
    			
    		} catch (FtpException e) {
    		} catch (IOException e) {
    			ftp.close();
    			throw e;
    		}

    		
    		if (! anyDirFound) {
    			ftp.close();
    			throw new FtpException("The directory(s) "+Arrays.toString(remoteDirectoryArray)+" do not exist.");
    		}

    	}

    	ftp.close();

        return list.toArray(new FileInfo[list.size()]);
    }

    
    
    
  
}

