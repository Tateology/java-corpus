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
import gov.noaa.ncdc.common.RiverLayout;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
/**
 *  Description of the Class
 *
 * @author    steve.ansari
 * created    January 10, 2005
 */
public class WCTTransferProgressDialog extends JDialog 
implements DataTransferListener, FtpObserver {

    private DataTransfer transfer;

    private DecimalFormat fmt2 = new DecimalFormat("0.00");
    private long size = -1;
    private JLabel label = new JLabel();
    private JProgressBar progress = new JProgressBar();
    private JButton cancel = new JButton("Cancel");

    private FtpBean ftp;
    private boolean isFTP;
    private int totalFtpBytes = 0;
    private boolean ftpAbort = false;
    private Frame frameOwner;
    private Dialog dialogOwner;

    private String progressBarInfo = "";
    private String titleLabelInfo = "Data Transfer";
    
    private boolean isDownloadError = false;


    /**
     * Constructor for the WCTTransferProgress object
     *
     * @param  owner     Parent frame for this dialog popup
     * @param  transfer  DataTransfer object that handles transfer
     */
    public WCTTransferProgressDialog(Frame owner, DataTransfer transfer) {
        this(owner, transfer, -1);
    }

    /**
     * Constructor for the WCTTransferProgress object
     *
     * @param  owner     Parent frame for this dialog popup
     * @param  transfer  DataTransfer object that handles transfer
     * @param  size      Transfer size, which must be found before streaming transfer has started
     */
    public WCTTransferProgressDialog(Frame owner, DataTransfer transfer, long size) {
        super(owner, "Download Progress", false);
        this.frameOwner = owner;
        this.transfer = transfer;
        this.size = size;

        isFTP = false;
        createGUI();
    }


    /**
     * Constructor for the WCTTransferProgress object
     *
     * @param  owner     Parent dialog for this dialog popup
     * @param  transfer  DataTransfer object that handles transfer
     * @param  size      Transfer size, which must be found before streaming transfer has started
     */
    public WCTTransferProgressDialog(Dialog owner, DataTransfer transfer, long size) {
        super(owner, "Download Progress", false);
        this.dialogOwner = owner;
        this.transfer = transfer;
        this.size = size;

        isFTP = false;
        createGUI();
    }


    /**
     * Constructor for the WCTTransferProgress object
     *
     * @param  owner     Parent frame for this dialog popup
     * @param  ftp       FtpBean object that handles FTP transfers
     */
    public WCTTransferProgressDialog(Frame owner, FtpBean ftp) {
        super(owner, "Download Progress", false);
        this.frameOwner = owner;
        this.ftp = ftp;

        isFTP = true;
        createGUI();

    }

    /**
     * Constructor for the WCTTransferProgress object
     *
     * @param  owner     Parent dialog for this dialog popup
     * @param  ftp       FtpBean object that handles FTP transfers
     */
    public WCTTransferProgressDialog(Dialog owner, FtpBean ftp) {
        super(owner, "Download Progress", false);
        this.dialogOwner = owner;
        this.ftp = ftp;

        isFTP = true;
        createGUI();

    }

    
    

    /**
     *  Creates GUI with ProgressBar and Cancel Button
     */
    private void createGUI() {
        this.setTitle("Transfer Progress");

        progress.setIndeterminate((size <= 0));
        progress.setMinimum(0);
        progress.setMaximum((int)size);
        progress.setStringPainted(true);
        progress.setString("");
        progress.setPreferredSize(new java.awt.Dimension(500, 20));
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelTransfer();
            }
        });
        JPanel panel = new JPanel();
        panel.setLayout(new RiverLayout());
        panel.add(label, "center");
        panel.add(progress, "br hfill");
        panel.add(cancel);
        this.getContentPane().add(panel);


        this.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent e) {
            }
            public void windowClosed(WindowEvent e) {
            }
            public void windowClosing(WindowEvent e) {
                cancelTransfer();
            }
            public void windowDeactivated(WindowEvent e) {
            }
            public void windowDeiconified(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
            }
            public void windowOpened(WindowEvent e) {
            }
        });
    }



    /**
     *  Make GUI Visible at this event
     *
     * @param  dte  The DataTransferEvent
     */
    public void transferStarted(DataTransferEvent dte) {
        // Make GUI Visible
        this.pack();
        if (frameOwner != null) {
            if (frameOwner.isVisible()) {
            	this.setLocation(frameOwner.getX()+10, frameOwner.getY()+10);
            	this.setVisible(true);
//            	this.setAlwaysOnTop(true);
            }
        }
        else if (dialogOwner != null) {
            if (dialogOwner.isVisible()) {
            	this.setLocation(dialogOwner.getX()+10, dialogOwner.getY()+10);
            	this.setVisible(true);
//            	this.setAlwaysOnTop(true);
            }
        }
    }


    /**
     *  Update ProgressBar information
     *
     * @param  dte  The DataTransferEvent
     */
    public void transferProgress(DataTransferEvent dte) {
        // Update progress and transfer rate
        if (size > 0) {
            progress.setString(progressBarInfo + " : " + 
            		transfer.getTransferred()/1024 + " of " + size/1024 + " KB at " + 
            		fmt2.format(transfer.getRate()) + " KB/sec");
        }
        else {
            progress.setString(progressBarInfo + " : " + 
            		transfer.getTransferred()/1024 + " KB at " + 
            		fmt2.format(transfer.getRate()) + " KB/sec");
        }
        progress.setValue(transfer.getTransferred());

//        try {
//            Thread.sleep(300);
//        } catch (Exception e) {}
    }


    /**
     *  Dispose of GUI when finished
     *
     * @param  dte  The DataTransferEvent
     */
    public void transferEnded(DataTransferEvent dte) {
        // Dispose of GUI
        //try {
        //    Thread.sleep(500);
        //} catch (Exception e) {}
        //JOptionPane.showMessageDialog(null, "TRANSFER ENDED",
        //      "NEXRAD TRANSFER ERROR", JOptionPane.ERROR_MESSAGE);
        this.dispose();
    }


    /**
     *  Display DialogBox if Error
     *
     * @param  dte  The DataTransferEvent
     */
    public void transferError(DataTransferEvent dte) {
    	isDownloadError = true;
    	if (frameOwner != null) {
    		JOptionPane.showMessageDialog(frameOwner, "Error Downloading Data",
                "DATA TRANSFER ERROR", JOptionPane.ERROR_MESSAGE);
    	}
    	else if (dialogOwner != null) {
    		JOptionPane.showMessageDialog(dialogOwner, "Error Downloading Data",
                    "DATA TRANSFER ERROR", JOptionPane.ERROR_MESSAGE);
    	}
        this.dispose();
    }




    //=========================================================================================
    // Below - implementation of FtpObserver interface
    //=========================================================================================   
    public void byteRead(int bytes) {
    	
        // Make GUI Visible
        if (frameOwner != null && ! this.isVisible()) {
            this.pack();
            this.setLocation(frameOwner.getX()+10, frameOwner.getY()+10);
            this.setVisible(true);
//            this.setAlwaysOnTop(true);
        }
        else if (dialogOwner != null && ! this.isVisible()) {
            this.pack();
            this.setLocation(dialogOwner.getX()+10, dialogOwner.getY()+10);
            this.setVisible(true);
//            this.setAlwaysOnTop(true);
        }

    	
        totalFtpBytes += bytes;

        //try {
        //    Thread.sleep(1);
        //} catch (Exception e) {}

        progress.setString(progressBarInfo + " : " + totalFtpBytes/1024 + " KB ");
        //progress.setValue(transfer.getTransferred());
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
        cancel.setEnabled(false);
        dispose();
    }

    public boolean isDownloadError() {
    	return isDownloadError;
    }
    
    
    public void setProgressBarInfo(String progressBarInfo) {
        this.progressBarInfo = progressBarInfo;
        this.setTitle(progressBarInfo + " Transfer Progress");

    }

    public String getProgressBarInfo() {
        return progressBarInfo;
    }

    public void setTitleLabelInfo(String titleLabelInfo) {
        this.titleLabelInfo = titleLabelInfo;
        label.setText(titleLabelInfo);
    }

    public String getTitleLabelInfo() {
        return titleLabelInfo;
    }




}

