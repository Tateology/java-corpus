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

package gov.noaa.ncdc.wct.decoders.nexrad;

import ftp.FtpBean;
import ftp.FtpObserver;
import gov.noaa.ncdc.common.DataTransfer;
import gov.noaa.ncdc.common.DataTransferEvent;
import gov.noaa.ncdc.common.DataTransferListener;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
/**
 *  Description of the Class
 *
 * @author    steve.ansari
 * created    January 10, 2005
 */
public class WCTTransferProgressBatch 
implements DataTransferListener, FtpObserver {

    private DataTransfer transfer;

    private DecimalFormat fmt2 = new DecimalFormat("0.00");
    private long size = -1;

    private FtpBean ftp;
    private boolean isFTP;
    private int totalFtpBytes = 0;
    private boolean ftpAbort = false;

    private String progressInfo = "";
    private String titleLabelInfo = "Data Transfer";
    
    private GeneralProgressEvent progressEvent = new GeneralProgressEvent(this);
    private ArrayList<GeneralProgressListener> progressListeners = new ArrayList<GeneralProgressListener>();

    /**
     * Constructor for the WCTTransferProgress object
     *
     * @param  transfer  DataTransfer object that handles transfer
     */
    public WCTTransferProgressBatch(DataTransfer transfer, ArrayList<GeneralProgressListener> progressListeners) {
        this(transfer, -1, progressListeners);
    }

    /**
     * Constructor for the WCTTransferProgress object
     *
     * @param  transfer  DataTransfer object that handles transfer
     * @param  size      Transfer size, which must be found before streaming transfer has started
     */
    public WCTTransferProgressBatch(DataTransfer transfer, long size, ArrayList<GeneralProgressListener> progressListeners) {
        this.transfer = transfer;
        this.size = size;
        this.progressListeners = progressListeners;

        isFTP = false;
    }
 
    /**
     * Constructor for the WCTTransferProgress object
     *
     * @param  ftp       FtpBean object that handles FTP transfers
     */
    public WCTTransferProgressBatch(FtpBean ftp, ArrayList<GeneralProgressListener> progressListeners) {
        this.ftp = ftp;
        this.progressListeners = progressListeners;

        isFTP = true;

    }




    /**
     *
     * @param  dte  The DataTransferEvent
     */
    public void transferStarted(DataTransferEvent dte) {

    }


    /**
     *  Update ProgressBar information
     *
     * @param  dte  The DataTransferEvent
     */
    public void transferProgress(DataTransferEvent dte) {
    	
    	if (progressListeners == null) {
    		return;
    	}
    	
        // Update progress and transfer rate
        if (size > 0) {
            progressEvent.setStatus(progressInfo + " : " + transfer.getTransferred()/1024 + " of " + size/1024 + " KB at " + fmt2.format(transfer.getRate()) + " KB/sec");
        }
        else {
        	progressEvent.setStatus(progressInfo + " : " + transfer.getTransferred()/1024 + " KB at " + fmt2.format(transfer.getRate()) + " KB/sec");
        }
        progressEvent.setProgress(transfer.getTransferred());
        for (GeneralProgressListener l : progressListeners) {
        	l.progress(progressEvent);
        }
    }


    /**
     *
     * @param  dte  The DataTransferEvent
     */
    public void transferEnded(DataTransferEvent dte) {
    }


    /**
     *  Display DialogBox if Error
     *
     * @param  dte  The DataTransferEvent
     */
    public void transferError(DataTransferEvent dte) {
        System.err.println("ERROR DOWNLOADING FILE");
    }




    //=========================================================================================
    // Below - implementation of FtpObserver interface
    //=========================================================================================   
    public void byteRead(int bytes) {

        totalFtpBytes += bytes;

        //try {
        //    Thread.sleep(1);
        //} catch (Exception e) {}

//        progress.setString(progressBarInfo + " : " + totalFtpBytes/1024 + " KB ");
        //progress.setValue(transfer.getTransferred());

        progressEvent.setStatus(progressInfo + " : " + totalFtpBytes/1024 + " KB ");
        for (GeneralProgressListener l : progressListeners) {
        	l.progress(progressEvent);
        }
    }

    public void byteWrite(int bytes) {
        // not implemented here
    }

    public boolean abortTransfer() {
        return ftpAbort;
    }


    /**
     *  Detect a "Cancel" action and report it to the DataTransfer object (calls abort method in DataTransfer)
     *
     * @param  dte  The ActionEvent
     */
    public void cancelTransfer() {
        if (isFTP) {
            ftpAbort = true;
        }
        else {
            transfer.abort();
        }
    }






}

