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

package gov.noaa.ncdc.nexradiv.rt;

import gov.noaa.ncdc.common.FTPList;
import gov.noaa.ncdc.common.SwingWorker;
import gov.noaa.ncdc.common.URLTransfer;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.io.FileInfo;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * @created    September 15, 2004
 */
public class NWSRTData extends javax.swing.JFrame implements ActionListener, ListSelectionListener, KeyListener, ItemListener {
   /**
    *  Description of the Field
    */
   public final static String NWS_ADDRESS = "tgftp.nws.noaa.gov";
   /**
    *  Description of the Field
    */
   public final static String NWS_FTP_DIR = "SL.us008001/DF.of/DC.radar";

   private JMenuBar menuBar = new JMenuBar();
   private JMenu fileMenu, optionMenu;
   private JMenuItem jmiClose, jmiOption, jmiAddThread, jmiShowThreads;
   private JButton jbCancel, jbDownloadLatest, jbDownloadAll, jbBrowse;
   private JTextField jOpText;
   private JLabel jlInstructions;
   private final JComboBox jcomboSites = new JComboBox(new String[]{"--Loading--"});
   private final JComboBox jcomboProducts = new JComboBox(new String[]{"--Loading--"});
   private File lastFolder = null;
   private FileInfo[] files;
   private Vector downloadThreads = new Vector();

   private DecodeL3Header header;
   private FTPList ftpList;

   private final JProgressBar progressBar = new JProgressBar();
   private boolean exitOnClose;

   private int loopIndexValue, loopMaxValue;

   private boolean isDownloadInProgress = false;


   /**
    *Constructor for the NWSRTData object
    */
   public NWSRTData() {
      this(true);
   }


   /**
    *Constructor for the NWSRTData object
    *
    * @param  exitOnClose  Description of the Parameter
    */
   public NWSRTData(boolean exitOnClose) {
      super("NWS Real-Time NEXRAD Level-III Data Download");
      if (exitOnClose) {
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      }
      this.exitOnClose = exitOnClose;
      createGUI();
      pack();
      setVisible(true);

      // Populate in thread
      SwingWorker worker =
         new SwingWorker() {
            public Object construct() {

               jcomboProducts.setModel(new DefaultComboBoxModel(listDirectory(NWS_FTP_DIR)));
               jcomboSites.setModel(new DefaultComboBoxModel(listDirectory(NWS_FTP_DIR + "/DS.p19r0")));

               jbDownloadLatest.setEnabled(true);
               jbDownloadAll.setEnabled(true);

               System.out.println("THREAD RUN IS DONE");
               return "Done";
            }
         };
      worker.start();

      System.out.println("THREAD CREATION DONE");

   }


   /**
    *  Description of the Method
    */
   private void createGUI() {

      jmiClose = new JMenuItem("Close", KeyEvent.VK_C);
      jmiClose.addActionListener(this);
      jmiOption = new JMenuItem("Options", KeyEvent.VK_O);
      jmiOption.addActionListener(this);
      jmiAddThread = new JMenuItem("Add Thread", KeyEvent.VK_A);
      jmiAddThread.addActionListener(this);
      jmiShowThreads = new JMenuItem("Show Threads", KeyEvent.VK_T);
      jmiShowThreads.addActionListener(this);
      fileMenu = new JMenu("File");
      fileMenu.setMnemonic(KeyEvent.VK_F);
      optionMenu = new JMenu("Options");
      optionMenu.setMnemonic(KeyEvent.VK_O);
      fileMenu.add(jmiClose);
      optionMenu.add(jmiOption);
      optionMenu.add(jmiAddThread);
      optionMenu.add(jmiShowThreads);
      menuBar.add(fileMenu);
      menuBar.add(optionMenu);
      this.setJMenuBar(menuBar);

      jcomboProducts.addItemListener(this);

      JPanel topPanel = new JPanel();
      topPanel.setLayout(new GridLayout(1, 2));
      topPanel.add(jcomboProducts);
      topPanel.add(jcomboSites);

      jOpText = new JTextField(22);
      jOpText.addKeyListener(this);
      jlInstructions = new JLabel("Choose Local Save Directory", JLabel.CENTER);
      jlInstructions.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      jbBrowse = new JButton("Browse Local");
      //jbBrowse.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
      jbBrowse.addActionListener(this);
      jbBrowse.setMnemonic(KeyEvent.VK_B);

      // Initial HAS JOB Number input panel
      JPanel buttonPanel = new JPanel();
      //buttonPanel.setLayout(new GridLayout(1,2));
      buttonPanel.add(jlInstructions);
      JPanel inputPanel = new JPanel();
      inputPanel.setLayout(new GridLayout(4, 1));
      inputPanel.add(topPanel);
      inputPanel.add(buttonPanel);
      JPanel textPanel = new JPanel();
      textPanel.add(jOpText);
      textPanel.add(jbBrowse);
      inputPanel.add(textPanel);

      progressBar.setString("Download Status");
      progressBar.setStringPainted(true);
      progressBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

      inputPanel.add(progressBar);

//      topPanel = new JPanel();
//      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
//     topPanel.add(inputPanel);
//     topPanel.add(buttonPanel);

      // Initial File List panel
      /*
       *  listModel = new DefaultListModel();
       *  listModel.addElement("------ Waiting for HAS Job Number ------");
       *  fileList = new JList(listModel);
       *  fileList.addKeyListener(this);
       *  fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       *  /fileList.addListSelectionListener(this);
       *  JScrollPane listScrollPane = new JScrollPane(fileList);
       */
      // Create Action Buttons
      JPanel choicePanel = new JPanel();
      choicePanel.setLayout(new GridLayout(1, 3));
      jbDownloadLatest = new JButton("Download Latest");
      jbDownloadLatest.setEnabled(false);
      jbDownloadLatest.addActionListener(this);
      jbDownloadLatest.addKeyListener(this);
      jbDownloadLatest.setMnemonic(KeyEvent.VK_D);
      jbDownloadAll = new JButton("Download All");
      jbDownloadAll.setEnabled(false);
      jbDownloadAll.addActionListener(this);
      jbDownloadAll.setMnemonic(KeyEvent.VK_A);
      jbDownloadAll.addKeyListener(this);
      jbCancel = new JButton("Cancel");
      jbCancel.addActionListener(this);
      jbCancel.addKeyListener(this);
      jbCancel.setEnabled(false);
      choicePanel.add(jbDownloadAll);
      choicePanel.add(jbDownloadLatest);
      choicePanel.add(jbCancel);

      jcomboProducts.addKeyListener(this);
      jcomboSites.addKeyListener(this);

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(inputPanel, "North");
//      getContentPane().add(listScrollPane, "Center");
      getContentPane().add(choicePanel, "South");

   }


   // Implementation of KeyListener interface.
   /**
    *  Description of the Method
    *
    * @param  e  Description of the Parameter
    */
   public void keyPressed(KeyEvent e) {
      //System.out.println(e.getKeyCode()+"  " + KeyEvent.VK_ESCAPE);
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
         if (! exitOnClose) {
            this.dispose();
         }
      }
      else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
         System.out.println("KEY ENTER PRESSED");
         if (!jbDownloadLatest.isEnabled()) {
         }
         else {
            actionPerformed(new ActionEvent(jbDownloadLatest, 0, "DownloadLatest"));
            //downloadLatestNexrad();
         }
      }
   }


   /**
    *  Description of the Method
    *
    * @param  e  Description of the Parameter
    */
   public void keyReleased(KeyEvent e) {
      //System.out.println(e.getKeyCode()+"  " + KeyEvent.VK_ESCAPE);
   }


   /**
    *  Description of the Method
    *
    * @param  e  Description of the Parameter
    */
   public void keyTyped(KeyEvent e) {
      //System.out.println(e.getKeyCode()+"  " + KeyEvent.VK_ESCAPE);
   }


   // Implementation of ItemListener interface.
   /**
    *  Description of the Method
    *
    * @param  e  Description of the Parameter
    */
   public void itemStateChanged(final ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
         System.out.println("ITEM CHANGED: " + e.getItem().toString());

         /*
          *  / Populate in thread
          *  SwingWorker worker =
          *  new SwingWorker() {
          *  public Object construct() {
          *  jcomboSites.setModel(new DefaultComboBoxModel(listDirectory(NWS_FTP_DIR+"/"+e.getItem().toString())));
          *  return "Done";
          *  }
          *  };
          *  worker.start();
          */
      }
   }


   // Implementation of ActionListener interface.
   /**
    *  Description of the Method
    *
    * @param  event  Description of the Parameter
    */
   public void actionPerformed(ActionEvent event) {

      Object source = event.getSource();
      if (source == jbCancel) {
         try {
            progressBar.setString("Aborting Export...");
            setLoopIndexValue(getLoopMaxValue());
         } catch (NullPointerException npe) {}
      }
      else if (source == jmiClose) {
         if (exitOnClose) {
            System.exit(1);
         }
         else {
            dispose();
         }
      }
      else if (source == jmiAddThread) {

         // Check destination file and url
         String saveDirectory = jOpText.getText();
         if (saveDirectory.trim().length() == 0) {
            actionPerformed(new ActionEvent(jbBrowse, 0, "Browse"));
            saveDirectory = jOpText.getText();
         }
         try {
            File test = new File(saveDirectory);
            if (!test.exists()) {
               throw new Exception();
            }
         } catch (Exception e) {
            String message = "Invalid Directory: \n" +
                  "<html><font color=red>" + saveDirectory + "</font></html>";
            JOptionPane.showMessageDialog(null, (Object) message,
                  "DIRECTORY SELECTION ERROR", JOptionPane.ERROR_MESSAGE);
            return;
         }

         URL nexrad_url;

         try {
            nexrad_url = new URL("ftp://" + NWS_ADDRESS + "/" + NWS_FTP_DIR + "/" +
                  jcomboProducts.getSelectedItem().toString().trim() + "/" +
                  jcomboSites.getSelectedItem().toString().trim() + "/sn.last");

         } catch (Exception e) {
            String message = "Invalid Nexrad URL!";
            JOptionPane.showMessageDialog(null, (Object) message,
                  "NEXRAD URL ERROR", JOptionPane.ERROR_MESSAGE);
            return;
         }

         String name = jcomboSites.getSelectedItem().toString().trim() + "_" +
               jcomboProducts.getSelectedItem().toString().trim();
         
         boolean alreadyExists = false;
         for (int n=0; n<downloadThreads.size(); n++) {
            if (name.equals(((NWSRTDataThread)downloadThreads.elementAt(n)).getThreadName())) {
               alreadyExists = true;
            }
         }
               
         if (alreadyExists) {
            String message = "Thread already exists!\n"+name;
            JOptionPane.showMessageDialog(null, (Object) message,
                  "THREAD ERROR", JOptionPane.ERROR_MESSAGE);            
         }
         else {
            NWSRTDataThread thread =
               new NWSRTDataThread(name, saveDirectory, nexrad_url, this);

            thread.start();
            downloadThreads.addElement(thread);
            
         }
         
         
         
         
      }
      else if (source == jbBrowse) {
         // Set up File Chooser
         JFileChooser fc = new JFileChooser(lastFolder);
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         fc.setDialogTitle("Choose Directory of Nexrad Files");
         int returnVal = fc.showOpenDialog(this);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
            lastFolder = fc.getSelectedFile().getParentFile();
            jOpText.setText(fc.getSelectedFile().toString());
            //listLocalFiles(new File(jOpText.getText()));
         }
      }
      else if (source == jbDownloadLatest) {

         // Populate in thread
         SwingWorker worker =
            new SwingWorker() {
               public Object construct() {

                  try {
                     progressBar.setString("Downloading Latest File");
                     progressBar.setIndeterminate(true);
                     jbDownloadAll.setEnabled(false);
                     jbDownloadLatest.setEnabled(false);

                     String saveDirectory = jOpText.getText();
                     if (saveDirectory.trim().length() == 0) {
                        actionPerformed(new ActionEvent(jbBrowse, 0, "Browse"));
                        saveDirectory = jOpText.getText();
                     }
                     try {
                        File test = new File(saveDirectory);
                        if (!test.exists()) {
                           throw new Exception();
                        }
                     } catch (Exception e) {
                        String message = "Invalid Directory: \n" +
                              "<html><font color=red>" + saveDirectory + "</font></html>";
                        JOptionPane.showMessageDialog(null, (Object) message,
                              "DIRECTORY SELECTION ERROR", JOptionPane.ERROR_MESSAGE);
                        return "ERROR";
                     }

                     URL nexrad_url;

                     try {
                        nexrad_url = new URL("ftp://" + NWS_ADDRESS + "/" + NWS_FTP_DIR + "/" +
                              jcomboProducts.getSelectedItem().toString().trim() + "/" +
                              jcomboSites.getSelectedItem().toString().trim() + "/sn.last");

                     } catch (Exception e) {
                        String message = "Invalid Nexrad URL!";
                        JOptionPane.showMessageDialog(null, (Object) message,
                              "NEXRAD URL ERROR", JOptionPane.ERROR_MESSAGE);
                        return "ERROR";
                     }

                     downloadNexrad(saveDirectory, nexrad_url);

                  } catch (Exception e) {
                     e.printStackTrace();
                  } finally {
                     progressBar.setString("Download Status");
                     progressBar.setIndeterminate(false);
                     jbDownloadAll.setEnabled(true);
                     jbDownloadLatest.setEnabled(true);
                  }

                  return "Done";
               }
            };
         worker.start();

      }
      else if (source == jbDownloadAll) {
         // Populate in thread
         SwingWorker worker =
            new SwingWorker() {
               public Object construct() {
                  downloadAllNexrad();
                  return "Done";
               }
            };
         worker.start();

      }
   }


   // actionPerformed

   /**
    * @param  directory  Description of the Parameter
    * @return            Description of the Return Value
    */
   //private synchronized String[] listDirectory(String directory) {
   private FileInfo[] listDirectory(String directory) {

      // Send into thread
      if (ftpList == null) {
         ftpList = new FTPList();
      }

      System.out.println("LISTING: " + NWS_ADDRESS + "  /  " + directory);

      try {
         files = ftpList.getDirectoryList(NWS_ADDRESS,
               "ftp",
               "nwsrt@noaa.gov",
               "bin",
               directory
               );
      } catch (Exception e) {
         e.printStackTrace();
         files = null;
      }

      return files;
   }


   /**
    * Download Latest Nexrad File
    *
    * @param  saveDirectory  Description of the Parameter
    * @param  nexrad_url     Description of the Parameter
    */
   public void downloadNexrad(String saveDirectory, URL nexrad_url) {

         String nwsSite = jcomboSites.getSelectedItem().toString().trim();
         String nwsProduct = jcomboProducts.getSelectedItem().toString().trim();
         
         downloadNexrad(saveDirectory, nexrad_url, nwsSite, nwsProduct);
   }
   
   public void downloadNexrad(String saveDirectory, URL nexrad_url, String nwsSite, String nwsProduct) {
   
      // Wait for all other downloads to finish (thread safe)
      while (isDownloadInProgress) {
         try {
            Thread.sleep(500);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      setDownloadInProgress(true);

      // Get user tmp directory
      String tmp_dir = System.getProperty("java.io.tmpdir");
      
      
//*
      try {

         File tmp_nexrad_file = new File(tmp_dir + File.separator + "sn.last");

         try {
            DataOutputStream os = new DataOutputStream(new BufferedOutputStream(
            //new FileOutputStream (new File(saveDirectory+"/NWS_NEXRAD_TEST.raw")), 1*1024));
               new FileOutputStream(tmp_nexrad_file), 1 * 1024));

            URLTransfer urlTransfer = new URLTransfer(nexrad_url, os);
            urlTransfer.run();

            os.flush();
            os.close();

            urlTransfer.close();
            urlTransfer = null;
//*/
         /*
          *  String remote = NWS_FTP_DIR+"/"+
          *  jcomboProducts.getSelectedItem()+"/"+jcomboSites.getSelectedItem()+"/"+listFiles[n];
          *  String local = tmp_dir+"/"+listFiles[n];
          *  ftp.ftpIt(NWS_ADDRESS,
          *  "ftp",
          *  "jnx@noaa.gov",
          *  "bin",
          *  "get",
          *  remote,
          *  local);
          *  ftp = null;
          */
          
         } catch (Exception e) {
            e.printStackTrace();
            tmp_nexrad_file.delete();
            return;
         }
         
         
         if (header == null) {
            header = new DecodeL3Header();
         }
         URL local_nexrad_url = tmp_nexrad_file.toURL();
         header.decodeHeader(local_nexrad_url);

         String filename = "7000" + nwsSite.substring(3, 4).toUpperCase() +
               "NWS_NWSFTP_" + getAWIPSCode(nwsProduct) +
               nwsSite.substring(4, 7).toUpperCase() +
               "_" + header.getDate() + header.getHourString() + header.getMinuteString();
         // release file from DecodeL3Header
         header.close();
         // rename file to NCDC Standard
         File final_nexrad_file = new File(saveDirectory + File.separator + filename);
         if (final_nexrad_file.exists()) {
            final_nexrad_file.delete();
         }
         if (!tmp_nexrad_file.renameTo(final_nexrad_file)) {
            System.out.println("DID NOT RENAME FILE");
         }

         System.out.println(filename);
         setDownloadInProgress(false);

      } catch (Exception e) {
         e.printStackTrace();
      }

   }



   /**
    * Download All Nexrad Files for chosen product and site
    */
   private void downloadAllNexrad() {
      try {

         jbCancel.setEnabled(true);
         jbDownloadAll.setEnabled(false);
         jbDownloadLatest.setEnabled(false);

         // Get user tmp directory
         String tmp_dir = System.getProperty("java.io.tmpdir");

         System.out.println(tmp_dir);

         String saveDirectory = jOpText.getText();
         if (saveDirectory.trim().length() == 0) {
            actionPerformed(new ActionEvent(jbBrowse, 0, "Browse"));
            saveDirectory = jOpText.getText();
         }
         try {
            File test = new File(saveDirectory);
            if (!test.exists()) {
               throw new Exception();
            }
         } catch (Exception e) {
            String message = "Invalid Directory: \n" +
                  "<html><font color=red>" + saveDirectory + "</font></html>";
            JOptionPane.showMessageDialog(null, (Object) message,
                  "DIRECTORY SELECTION ERROR", JOptionPane.ERROR_MESSAGE);
            return;
         }

         // List files
         FileInfo[] listFiles = listDirectory(NWS_FTP_DIR + "/" +
               jcomboProducts.getSelectedItem().toString().trim() + "/" + jcomboSites.getSelectedItem().toString().trim());

         URL nexrad_url;


         String site = jcomboSites.getSelectedItem().toString().trim();
         String awipsCode = getAWIPSCode(jcomboProducts.getSelectedItem().toString().trim());

         progressBar.setMaximum(listFiles.length);

         setLoopIndexValue(0);
         setLoopMaxValue(listFiles.length);
         int n;
         while ((n = getLoopIndexValue()) < listFiles.length) {
            //for (int n = 0; n < listFiles.length; n++) {

            if (listFiles[n].getName().trim().charAt(0) != '.') {

               progressBar.setString("Downloading:  " + n + "/" + listFiles.length + " Complete");
               progressBar.setValue(n);

               try {
                  nexrad_url = new URL("ftp://" + NWS_ADDRESS + "/" + NWS_FTP_DIR + "/" +
                        jcomboProducts.getSelectedItem().toString().trim() + "/" + site + "/" + listFiles[n]);

               } catch (Exception e) {
                  String message = "Invalid Nexrad URL!";
                  JOptionPane.showMessageDialog(null, (Object) message,
                        "NEXRAD URL ERROR", JOptionPane.ERROR_MESSAGE);
                  return;
               }

               System.out.println("DOWNLOADING " + listFiles[n]);

//*
               try {

                  File tmp_nexrad_file = new File(tmp_dir + File.separator + listFiles[n]);

                  DataOutputStream os = new DataOutputStream(new BufferedOutputStream(
                  //new FileOutputStream (new File(saveDirectory+"/NWS_NEXRAD_TEST.raw")), 1*1024));
                        new FileOutputStream(tmp_nexrad_file), 1 * 1024));

                  URLTransfer urlTransfer = new URLTransfer(nexrad_url, os);
                  urlTransfer.run();

                  os.flush();
                  os.close();

                  urlTransfer.close();
                  urlTransfer = null;
//*/
                  /*
                   *  String remote = NWS_FTP_DIR+"/"+
                   *  jcomboProducts.getSelectedItem()+"/"+jcomboSites.getSelectedItem()+"/"+listFiles[n];
                   *  String local = tmp_dir+"/"+listFiles[n];
                   *  ftp.ftpIt(NWS_ADDRESS,
                   *  "ftp",
                   *  "jnx@noaa.gov",
                   *  "bin",
                   *  "get",
                   *  remote,
                   *  local);
                   *  ftp = null;
                   */
                  if (header == null) {
                     header = new DecodeL3Header();
                  }
                  URL local_nexrad_url = tmp_nexrad_file.toURL();
                  header.decodeHeader(local_nexrad_url);

                  String filename = "7000" + site.substring(3, 4).toUpperCase() +
                        "NWS_NWSFTP_" + awipsCode + site.substring(4, 7).toUpperCase() +
                        "_" + header.getDate() + header.getHourString() + header.getMinuteString();

                  // release file from DecodeL3Header
                  header.close();
                  // rename file to NCDC Standard
                  File final_nexrad_file = new File(saveDirectory + File.separator + filename);
                  if (final_nexrad_file.exists()) {
                     final_nexrad_file.delete();
                  }
                  if (!tmp_nexrad_file.renameTo(final_nexrad_file)) {
                     System.out.println("DID NOT RENAME FILE");
                  }

                  System.out.println(saveDirectory + File.separator + filename);

               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
            incrementLoopIndexValue();
         }

         progressBar.setString("Completed Download of " + listFiles.length + " Files");
         jbCancel.setEnabled(false);
         try {
            Thread.sleep(1500);
         } catch (Exception e) {}
         progressBar.setValue(0);
         progressBar.setString("Download Status");

      } catch (Exception e) {
         e.printStackTrace();
         //listModel.clear();
         //listModel.add(0, "------ NO DATA FOUND FOR HAS JOB NUMBER HAS" + hasString + " ------");
      } finally {
         jbDownloadAll.setEnabled(true);
         jbDownloadLatest.setEnabled(true);
         jbCancel.setEnabled(false);
      }
   }


   public String getSelectedSite() {
      return jcomboSites.getSelectedItem().toString().trim();
   }
   
   public String getSelectedProduct() {
      return jcomboProducts.getSelectedItem().toString().trim();
   }
   
   
   /**
    *  Gets the downloadInProgress attribute of the NWSRTData object
    *
    * @return    The downloadInProgress value
    */
   private boolean isDownloadInProgress() {
      return isDownloadInProgress;
   }


   /**
    *  Sets the downloadInProgress attribute of the NWSRTData object
    *
    * @param  b  The new downloadInProgress value
    */
   private void setDownloadInProgress(boolean b) {
      isDownloadInProgress = b;
   }



   /**
    *  Gets the loopIndexValue attribute of the NWSRTData object
    *
    * @return    The loopIndexValue value
    */
   private int getLoopIndexValue() {
      return loopIndexValue;
   }


   /**
    *  Sets the loopIndexValue attribute of the NWSRTData object
    *
    * @param  n  The new loopIndexValue value
    */
   private void setLoopIndexValue(int n) {
      loopIndexValue = n;
   }


   /**
    *  Description of the Method
    */
   private void incrementLoopIndexValue() {
      loopIndexValue++;
   }


   /**
    *  Sets the loopMaxValue attribute of the NWSRTData object
    *
    * @param  max  The new loopMaxValue value
    */
   private void setLoopMaxValue(int max) {
      loopMaxValue = max;
   }


   /**
    *  Gets the loopMaxValue attribute of the NWSRTData object
    *
    * @return    The loopMaxValue value
    */
   private int getLoopMaxValue() {
      return loopMaxValue;
   }


   /**
    *  Gets the aWIPSCode attribute of the NWSRTData object
    *
    * @param  nwsCode  Description of the Parameter
    * @return          The aWIPSCode value
    */
   public String getAWIPSCode(String nwsCode) {
      
System.out.println("NWS CODE: "+nwsCode);      
      
      if (nwsCode.equals("DS.p19r0")) {
         return "N0R";
      }
      else if (nwsCode.equals("DS.p19r1")) {
         return "N1R";
      }
      else if (nwsCode.equals("DS.p19r2")) {
         return "N2R";
      }
      else if (nwsCode.equals("DS.p19r3")) {
         return "N3R";
      }
      else if (nwsCode.equals("DS.p20-r")) {
         return "N0Z";
      }
      else if (nwsCode.equals("DS.p25-v")) {
         return "N0W";
      }
      else if (nwsCode.equals("DS.p27v0")) {
         return "N0V";
      }
      else if (nwsCode.equals("DS.p27v1")) {
         return "N1V";
      }
      else if (nwsCode.equals("DS.p27v2")) {
         return "N2V";
      }
      else if (nwsCode.equals("DS.p27v3")) {
         return "N3V";
      }
      else if (nwsCode.equals("DS.p28sw")) {
         return "NSP";
      }
      else if (nwsCode.equals("DS.p2gsm")) {
         return "GSM";
      }
      else if (nwsCode.equals("DS.p30sw")) {
         return "NSW";
      }
      else if (nwsCode.equals("DS.p36cr")) {
         return "NCO";
      }
      else if (nwsCode.equals("DS.p37cr")) {
         return "NCR";
      }
      else if (nwsCode.equals("DS.p38cr")) {
         return "NCZ";
      }
      else if (nwsCode.equals("DS.p41et")) {
         return "NET";
      }
      else if (nwsCode.equals("DS.p59hi")) {
         return "NHI";
      }
      else if (nwsCode.equals("DS.p60-m")) {
         return "NME";
      }
      else if (nwsCode.equals("DS.p62ss")) {
         return "NSS";
      }
      else if (nwsCode.equals("DS.31usp")) {
         return "NUP";
      }
      else if (nwsCode.equals("DS.34cfc")) {
         return "NCF";
      }
      else if (nwsCode.equals("DS.47swp")) {
         return "NWP";
      }
      else if (nwsCode.equals("DS.48vwp")) {
         return "NVW";
      }
      else if (nwsCode.equals("DS.56rm0")) {
         return "N0S";
      }
      else if (nwsCode.equals("DS.56rm1")) {
         return "N1S";
      }
      else if (nwsCode.equals("DS.56rm2")) {
         return "N2S";
      }
      else if (nwsCode.equals("DS.56rm3")) {
         return "N3S";
      }
      else if (nwsCode.equals("DS.57vil")) {
         return "NVL";
      }
      else if (nwsCode.equals("DS.58sti")) {
         return "NST";
      }
      else if (nwsCode.equals("DS.61tvs")) {
         return "NTV";
      }
      else if (nwsCode.equals("DS.65lrm")) {
         return "NLL";
      }
      else if (nwsCode.equals("DS.66lrm")) {
         return "NML";
      }
      else if (nwsCode.equals("DS.67apr")) {
         return "NLA";
      }
      else if (nwsCode.equals("DS.74rcm")) {
         return "RCM";
      }
      else if (nwsCode.equals("DS.75ftm")) {
         return "FTM";
      }
      else if (nwsCode.equals("DS.78ohp")) {
         return "N1P";
      }
      else if (nwsCode.equals("DS.79thp")) {
         return "N3P";
      }
      else if (nwsCode.equals("DS.80stp")) {
         return "NTP";
      }
      else if (nwsCode.equals("DS.81dpr")) {
         return "DPA";
      }
      else if (nwsCode.equals("DS.82spd")) {
         return "SUP";
      }
      else if (nwsCode.equals("DS.83irm")) {
         return "IRM";
      }
      else if (nwsCode.equals("DS.90lrm")) {
         return "NHL";
      }
      else {
         return "ZZZ";
      }

   }


   /**
    *  Description of the Method
    *
    * @param  e  Description of the Parameter
    */
   public void valueChanged(ListSelectionEvent e) {
// Break up menus here:   WSR   PRODUCT   DATE

   }



   /**
    *  The main program for the NWSRTData class
    *
    * @param  args  The command line arguments
    */
   public static void main(String[] args) {
      NWSRTData nwsData = new NWSRTData();
      System.out.println("NWSRTData Object Created");
      //nwsData.pack();
      //nwsData.show();
   }

}

