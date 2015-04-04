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

package gov.noaa.ncdc.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *  Description of the Class
 *
 * @author    steve.ansari
 * created    January 10, 2005
 */
public class DataTransferProgressGUI extends JFrame implements DataTransferListener, ActionListener {

   private URL url;
   private DataTransfer transfer;

   private DecimalFormat fmt2 = new DecimalFormat("0.00");
   private int size;
   private String filename;
   private JProgressBar progress = new JProgressBar();
   private JButton cancel = new JButton("Cancel");


   /**
    *Constructor for the Level2TransferProgress object
    *
    * @param  url       URL of file to transfer
    * @param  transfer  DataTransfer object that handles transfer
    */
   public DataTransferProgressGUI(URL url, DataTransfer transfer) {
      super("");
      this.url = url;
      this.transfer = transfer;
      createGUI();
   }


   /**
    *  Creates GUI with ProgressBar and Cancel Button
    */
   private void createGUI() {
      String urlString = url.toString();
      filename = urlString.substring(urlString.lastIndexOf("/") + 1, urlString.length());
      this.setTitle(filename + " Transfer Progress");
      size = getFileSize();
      progress.setIndeterminate((size < 0));
      progress.setMinimum(0);
      progress.setMaximum(size);
      progress.setStringPainted(true);
      progress.setPreferredSize(new java.awt.Dimension(500, 20));
      cancel = new JButton("Cancel");
      cancel.addActionListener(this);
      JPanel panel = new JPanel();
      panel.add(progress);
      panel.add(cancel);
      this.getContentPane().add(panel);
   }


   /**
    *  Gets the fileSize for url value input with constructor
    *
    * @return    The fileSize value
    */
   private int getFileSize() {
      try {
         int size;
         URLConnection conn = url.openConnection();
         size = conn.getContentLength();
         conn.getInputStream().close();
         return size;
      } catch (Exception e) {
         e.printStackTrace();
         return -1;
      }
   }


   /**
    *  Make GUI Visible at this event
    *
    * @param  dte  The DataTransferEvent
    */
   public void transferStarted(DataTransferEvent dte) {
      // Make GUI Visible
      this.pack();
      this.setLocation(10, 10);
      this.setVisible(true);
   }


   /**
    *  Update ProgressBar information
    *
    * @param  dte  The DataTransferEvent
    */
   public void transferProgress(DataTransferEvent dte) {
      // Update progress and transfer rate
      progress.setString(filename + " : " + transfer.getTransferred() + " of " + size + " bytes at " + fmt2.format(transfer.getRate()) + " bps");
      progress.setValue(transfer.getTransferred());
      //try {
      //    Thread.sleep(100);
      //} catch (Exception e) {}
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

      this.dispose();
   }


   /**
    *  Display DialogBox if Error
    *
    * @param  dte  The DataTransferEvent
    */
   public void transferError(DataTransferEvent dte) {
      JOptionPane.showMessageDialog(null, "Error Downloading File: "+filename,
            "NEXRAD TRANSFER ERROR", JOptionPane.ERROR_MESSAGE);
      this.dispose();
   }

   
   /**
    *  Detect a "Cancel" action and report it to the DataTransfer object (calls abort method in DataTransfer)
    *
    * @param  dte  The ActionEvent
    */
   public void actionPerformed(ActionEvent evt) {
      transfer.abort();
      cancel.setEnabled(false);
   }
   

}

