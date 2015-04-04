package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.common.SwingManipulator;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.io.ScanResultsComparator;
import gov.noaa.ncdc.wct.io.WCTDataSourceDB;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXLabel;

import com.jcraft.jsch.Session;
import com.jidesoft.swing.FolderChooser;

public class DataSourcePanel extends JPanel {

	public final static HashMap<String, JPanel> TAB_PANEL_LOOKUP = new HashMap<String, JPanel>();
	public final static LinkedHashMap<Integer, String> CURRENT_TAB_ORDER_LOOKUP = new LinkedHashMap<Integer, String>();
	
	
	
    public final static int HISTORY_SIZE = 40;
    

    //private JTextField jText;
    private JButton jbSubmitHAS, jbSubmitCLASS, jbSubmitLocal, jbSubmitCustom, jbSubmitThredds, jbSubmitSSH;
    private JComboBox jcomSortByHAS, jcomSortByCLASS, jcomSortByLocal, jcomSortByCustom, jcomSortByThredds, jcomSortBySSH, jcomSortByFavorites;
    private DefaultComboBoxModel sortByModelHAS = new DefaultComboBoxModel(new String[] { " Filename ", " Timestamp Asc ", " Timestamp Desc "," Product " });
    private DefaultComboBoxModel sortByModelCLASS = new DefaultComboBoxModel(new String[] { " Filename ", " Timestamp Asc ", " Timestamp Desc ", " Product " });
    private DefaultComboBoxModel sortByModelLocal = new DefaultComboBoxModel(new String[] { " Filename ", " Timestamp Asc ", " Timestamp Desc ", " Product " });
    private DefaultComboBoxModel sortByModelCustom = new DefaultComboBoxModel(new String[] { " Filename ", " Timestamp Asc ", " Timestamp Desc ", " Product " });
    private DefaultComboBoxModel sortByModelFavorites = new DefaultComboBoxModel(new String[] { " Filename ", " Timestamp Asc ", " Timestamp Desc ", " Product " });

    private JCheckBox jcbListAllFilesLocal = new JCheckBox("Show All Files ?");
    private JCheckBox jcbListAllFilesCustom = new JCheckBox("Show All Files ?");

    
    private String hasString = "";
    private String localString = "";
    private String customString = "";
    private String threddsString = "";
    private String sshServerString = "";
    private String sshUserString = "";
    private String sshDirString = "";
    private String classOrderString;
    private String singleFileSourceString = "";
    private File lastFolder = null;


    private JComboBox jcomboDataDescHAS, jcomboDataDescLocal, jcomboDataDescCustom, jcomboDataDescThredds, jcomboDataDescSSH, jcomboDataDescCLASS;
    private JLabel statusHAS, statusLocal, statusCustom, statusTHREDDS, statusSSH, statusCLASS, statusFavorite;

    final JTextField jtfHASListFilter = new JTextField(10);
    final JTextField jtfCLASSListFilter = new JTextField(10);
    final JTextField jtfLocalListFilter = new JTextField(10);
    final JTextField jtfCustomListFilter = new JTextField(10);
    final JTextField jtfTHREDDSListFilter = new JTextField(10);
    final JTextField jtfFavoriteListFilter = new JTextField(10);

    
    
    private JProgressBar listProgress = new JProgressBar();

    private ArrayList<String> prevHASList, prevLocalList, prevCustomList, prevThreddsList, prevSshDirList, prevSshUserList, prevSshServerList, prevCLASSList, prevSingleFileSourceList;
    private AutoCompleteComboBox jcomboHAS, jcomboLocal, jcomboCustom, jcomboThredds, jcomboSshDir, jcomboSshUser, jcomboSshServer, jcomboCLASS, jcomboSingleFileSource;
    private DefaultComboBoxModel dcbmHAS, dcbmLocal, dcbmCustom, dcbmThredds, dcbmSshDir, dcbmSshUser, dcbmSshServer, dcbmCLASS; 
    private JPasswordField jpassSshPassword;

    private JTabbedPane tabPane = new JTabbedPane();

    private String sshUser, sshPassword, sshServer, sshDirectory;
    private Session sshSession;

    
    private boolean showDescriptionEntries = true;


    private ActionListener submitListener, sortByListener, favoritesListener;
    private KeyListener filterKeyListener;

    /**
     *Constructor for the DataSourcePanel object
     *
     * @param  submitListener - handles events when Submit Button is pressed - 
     * 	this is usually listing a directory or reading a catalog
     * @param  sortByListener - handles events when 'Sort By' combo box is used
     * @param  filterKeyListener - handles key events when text is typed in filter box
     * @param  favoritesListener - handles events from buttons on 'favorites' tab
     */
    public DataSourcePanel(ActionListener submitListener, 
    			ActionListener sortByListener, KeyListener filterKeyListener, 
    			ActionListener favoritesListener) {
        this(submitListener, sortByListener, filterKeyListener, favoritesListener, true);
    }

    /**
     *Constructor for the DataSourcePanel object
     *     
     * @param  submitListener - handles events when Submit Button is pressed - 
     * 	this is usually listing a directory or reading a catalog
     * @param  sortByListener - handles events when 'Sort By' combo box is used
     * @param  filterKeyListener - handles key events when text is typed in filter box
     * @param  favoritesListener - handles events from buttons on 'favorites' tab
     */
    public DataSourcePanel(ActionListener submitListener, ActionListener sortByListener, 
    		KeyListener filterKeyListener, ActionListener favoritesListener, 
    		boolean showDescriptionEntries) {
        
        this.submitListener = submitListener;
        this.sortByListener = sortByListener;
        this.filterKeyListener = filterKeyListener;
        this.favoritesListener = favoritesListener;
        this.showDescriptionEntries = showDescriptionEntries;
        createGUI();
    }


    /**
     *  Description of the Method
     */
    private void createGUI() {

    	

        //jText = new JTextField(10);
        //jText.addKeyListener(this);
        String wctpropHas = WCTProperties.getWCTProperty("ncdc_hasnum");
        if (wctpropHas != null) {
            //jText.setText(wctpropHas);
            hasString = wctpropHas;
        }
        String wctpropLocal = WCTProperties.getWCTProperty("customLocal");
        if (wctpropLocal != null) {
            localString = wctpropLocal;
        }
        String wctpropCustom = WCTProperties.getWCTProperty("customURL");
        if (wctpropCustom != null) {
            customString = wctpropCustom;
        }
        String wctpropThredds = WCTProperties.getWCTProperty("threddsURL");
        if (wctpropThredds != null) {
            threddsString = wctpropThredds;
        }
        String wctpropSshServer = WCTProperties.getWCTProperty("sshServer");
        if (wctpropSshServer != null) {
            sshServerString = wctpropSshServer;
        }
        String wctpropSshUser = WCTProperties.getWCTProperty("sshUser");
        if (wctpropSshUser != null) {
            sshUserString = wctpropSshUser;
        }
        String wctpropSshDir = WCTProperties.getWCTProperty("sshDir");
        if (wctpropSshDir != null) {
            sshDirString = wctpropSshDir;
        }
        String wctpropCLASS = WCTProperties.getWCTProperty("classOrder");
        if (wctpropCLASS != null) {
            classOrderString = wctpropCLASS;
        }
        String wctpropSingleFileSource = WCTProperties.getWCTProperty("singleFileSource");
        if (wctpropSingleFileSource != null) {
            singleFileSourceString = wctpropSingleFileSource;
        }





        prevHASList = new ArrayList<String>();      
        String wctpropPrevHAS;
        int cnt = 0;
        while ((wctpropPrevHAS = WCTProperties.getWCTProperty("prevHAS"+cnt++)) != null) {
            prevHASList.add(wctpropPrevHAS);
        }
        if (prevHASList.size() == 0 && wctpropHas != null) {
            prevHASList.add(wctpropHas);
        }
        jcomboHAS = new AutoCompleteComboBox(prevHASList.toArray());
        jcomboHAS.setEditable(true);
//      jcomboHAS.addItemListener(this);


        jcomboDataDescHAS = new JComboBox();
        jcomboDataDescHAS.setEditable(true);







        prevCLASSList = new ArrayList<String>();      
        String wctpropPrevCLASS;
        cnt = 0;
        while ((wctpropPrevCLASS = WCTProperties.getWCTProperty("prevCLASS"+cnt++)) != null) {
            prevCLASSList.add(wctpropPrevCLASS);
        }
        if (prevCLASSList.size() == 0 && wctpropCLASS != null) {
            prevCLASSList.add(wctpropCLASS);
        }
        jcomboCLASS = new AutoCompleteComboBox(prevCLASSList.toArray());
        jcomboCLASS.setEditable(true);
        jcomboDataDescCLASS = new JComboBox();
        jcomboDataDescCLASS.setEditable(true);








        prevLocalList = new ArrayList<String>();      
        String wctpropPrevLocal;
        cnt = 0;
        while ((wctpropPrevLocal = WCTProperties.getWCTProperty("prevLocal"+cnt++)) != null) {
            prevLocalList.add(wctpropPrevLocal);
        }
        if (prevLocalList.size() == 0 && wctpropLocal != null) {
            prevLocalList.add(wctpropLocal);
        }
        jcomboLocal = new AutoCompleteComboBox(prevLocalList.toArray());
        jcomboLocal.setEditable(true);
//      jcomboLocal.addItemListener(this);

        localString = (jcomboLocal.getSelectedItem() == null) ? null : jcomboLocal.getSelectedItem().toString();

        jcomboDataDescLocal = new JComboBox();
        jcomboDataDescLocal.setEditable(true);

        
        
        
        
        
        
        
        
        
        
        
        
        

        prevCustomList = new ArrayList<String>();      
        String wctpropPrevCustom;
        cnt = 0;
        while ((wctpropPrevCustom = WCTProperties.getWCTProperty("prevCustom"+cnt++)) != null) {
            prevCustomList.add(wctpropPrevCustom);
        }
        if (prevCustomList.size() == 0 && wctpropCustom != null) {
            prevCustomList.add(wctpropCustom);
        }
        jcomboCustom = new AutoCompleteComboBox(prevCustomList.toArray());
        jcomboCustom.setEditable(true);
//      jcomboCustom.addItemListener(this);


        jcomboDataDescCustom = new JComboBox();
        jcomboDataDescCustom.setEditable(true);


        prevThreddsList = new ArrayList<String>();      
        String wctpropPrevThredds;
        cnt = 0;
        while ((wctpropPrevThredds = WCTProperties.getWCTProperty("prevThredds"+cnt++)) != null) {
            prevThreddsList.add(wctpropPrevThredds);
        }
        if (prevThreddsList.size() == 0 && wctpropThredds != null) {
            prevThreddsList.add(wctpropThredds);
        }
        jcomboThredds = new AutoCompleteComboBox(prevThreddsList.toArray());
        jcomboThredds.setEditable(true);
//      jcomboThredds.addItemListener(this);
//        jcomboThredds.setDropTarget(new DropTarget(this, DnDConstants.ACTION_NONE, new WCTDropTargetHandler()));


        jcomboDataDescThredds = new JComboBox();
        jcomboDataDescThredds.setEditable(true);



        prevSshDirList = new ArrayList<String>();      
        String wctpropPrevSshDir;
        cnt = 0;
        while ((wctpropPrevSshDir = WCTProperties.getWCTProperty("prevSshDir"+cnt++)) != null) {
            prevSshDirList.add(wctpropPrevSshDir);
        }
        if (prevSshDirList.size() == 0 && wctpropSshDir != null) {
            prevSshDirList.add(wctpropSshDir);
        }
        jcomboSshDir = new AutoCompleteComboBox(prevSshDirList.toArray());
        jcomboSshDir.setEditable(true);
//      jcomboSshDir.addItemListener(this);

        if (sshUserString == null) {
            sshUserString = " -- Username --";
        }
        if (sshServerString == null) {
            sshServerString = " --  Server  --";
        }
        jcomboSshUser = new AutoCompleteComboBox(new String[] {sshUserString});
        jcomboSshUser.setEditable(true);
//      jcomboSshUser.addItemListener(this);

        jcomboSshServer = new AutoCompleteComboBox(new String[] {sshServerString});
        jcomboSshServer.setEditable(true);
//      jcomboSshServer.addItemListener(this);


        jcomboDataDescSSH = new JComboBox();
        jcomboDataDescSSH.setEditable(true);



        
        
        
        prevSingleFileSourceList = new ArrayList<String>();      
        String wctpropPrevSingleFileSource;
        cnt = 0;
        while ((wctpropPrevSingleFileSource = WCTProperties.getWCTProperty("prevSingleFileSource"+cnt++)) != null) {
            prevSingleFileSourceList.add(wctpropPrevSingleFileSource);
        }
        if (prevSingleFileSourceList.size() == 0 && wctpropSingleFileSource != null) {
            prevSingleFileSourceList.add(wctpropSingleFileSource);
        }
        jcomboSingleFileSource = new AutoCompleteComboBox(prevSingleFileSourceList.toArray());
        jcomboSingleFileSource.setEditable(true);
        
        
        SwingManipulator.addStandardEditingPopupMenu(new JTextComponent[] {
        	(JTextComponent)jcomboHAS.getEditor().getEditorComponent(),
        	(JTextComponent)jcomboCLASS.getEditor().getEditorComponent(),
        	(JTextComponent)jcomboLocal.getEditor().getEditorComponent(),
        	(JTextComponent)jcomboCustom.getEditor().getEditorComponent(),
        	(JTextComponent)jcomboThredds.getEditor().getEditorComponent(),
        	(JTextComponent)jcomboSingleFileSource.getEditor().getEditorComponent()
        });
        
        

        SwingManipulator.addStandardEditingPopupMenu(new JTextComponent[] {
        		jtfHASListFilter, jtfCLASSListFilter, jtfLocalListFilter,
        		jtfTHREDDSListFilter, jtfCustomListFilter
        });



        jbSubmitHAS = new JButton("  List Files  ");
        jbSubmitHAS.setActionCommand("Submit HAS");
        jbSubmitHAS.addActionListener(submitListener);
        jbSubmitHAS.setDefaultCapable(true);
        jbSubmitHAS.setMnemonic(KeyEvent.VK_L);
        //jbSubmitHAS.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        statusHAS = new JLabel();


        jtfHASListFilter.addKeyListener(filterKeyListener);
        
        jcomSortByHAS = new JComboBox(sortByModelHAS);
        jcomSortByHAS.setActionCommand("HAS");
        jcomSortByHAS.addActionListener(sortByListener);

        // HAS JOB Number input panel
        JPanel hasPanel = new JPanel();
        hasPanel.setLayout(new RiverLayout());
        hasPanel.add("center", new JLabel("Access to NCDC HAS Order Results on NCDC FTP Site"));
        JXHyperlink hasTutorialLink = new JXHyperlink();
        hasTutorialLink.setText("[ ? ]");
        hasTutorialLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					Desktop.getDesktop().browse(new URI("http://www.ncdc.noaa.gov/wct/tutorials/?file=order-nexrad"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
        hasPanel.add("center", hasTutorialLink);
        
        hasPanel.add("p left", new JLabel("Enter HAS Job Number"));
        hasPanel.add("tab hfill", jcomboHAS);
        if (showDescriptionEntries) {
            hasPanel.add("br", new JLabel("Description (optional)"));
            hasPanel.add("tab hfill", jcomboDataDescHAS);
        }
        hasPanel.add("p left hfill", jbSubmitHAS);
        hasPanel.add("tab hfill", statusHAS);
        hasPanel.add("right", new JLabel("Filter:"));
        hasPanel.add("right", jtfHASListFilter);
        hasPanel.add("right", new JLabel("  "));
        hasPanel.add("right", new JLabel("Sort By:"));
        hasPanel.add("right", jcomSortByHAS);








        jbSubmitCLASS = new JButton("  List Files  ");
        jbSubmitCLASS.setActionCommand("Submit CLASS");
        jbSubmitCLASS.addActionListener(submitListener);
        jbSubmitCLASS.setDefaultCapable(true);
        jbSubmitCLASS.setMnemonic(KeyEvent.VK_L);
        //jbSubmitCLASS.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        statusCLASS = new JLabel();

        jtfCLASSListFilter.addKeyListener(filterKeyListener);
        
        jcomSortByCLASS = new JComboBox(sortByModelCLASS);
        jcomSortByCLASS.setActionCommand("CLASS");
        jcomSortByCLASS.addActionListener(sortByListener);

        // CLASS JOB Number input panel
        JPanel classPanel = new JPanel();
        classPanel.setLayout(new RiverLayout());
        classPanel.add("center", new JLabel("Access to CLASS Order Results on CLASS FTP Site"));
        JXHyperlink classTutorialLink = new JXHyperlink();
        classTutorialLink.setText("[ ? ]");
        classTutorialLink.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					Desktop.getDesktop().browse(new URI("http://www.ncdc.noaa.gov/wct/tutorials/?file=order-satellite"));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
        classPanel.add("center", classTutorialLink);
        
        
        classPanel.add("p left", new JLabel("Enter CLASS Job Code"));
        classPanel.add("tab hfill", jcomboCLASS);
        if (showDescriptionEntries) {
            classPanel.add("br", new JLabel("Description (optional)"));
            classPanel.add("tab hfill", jcomboDataDescCLASS);
        }
        classPanel.add("p left hfill", jbSubmitCLASS);
        classPanel.add("tab hfill", statusCLASS);        
        classPanel.add("right", new JLabel("Filter:"));
        classPanel.add("right", jtfCLASSListFilter);
        classPanel.add("right", new JLabel("  "));
        classPanel.add("right", new JLabel("Sort By:"));
        classPanel.add("right", jcomSortByCLASS);




        // Find data input panel
        JButton jbBrowseToGuide = new JButton("   Open Data Guide   ");
        jbBrowseToGuide.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					Desktop.getDesktop().browse(new URI("http://www.ncdc.noaa.gov/wct/data.php"));
				} catch (IOException e) {
					JOptionPane.showMessageDialog(tabPane, e, "Error Message", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
        });
        JPanel findDataPanel = new JPanel();
        findDataPanel.setLayout(new BorderLayout());
//        findDataPanel.add("center", new JLabel("A guide to some popular data access sites"));
        String msg = "\nThe Weather and Climate Toolkit supports Gridded and Radial data types " +
        		       "in a variety of binary formats including NetCDF, GRIB, HDF, GINI, AREA, " +
        		       "NEXRAD, and more.  \n\n" +
		"Please refer to this online guide to help find data from a variety of sources. \n";
        JXLabel msgLabel = new JXLabel(msg);
        msgLabel.setLineWrap(true);
        findDataPanel.add(msgLabel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 20));
        buttonPanel.add(jbBrowseToGuide);
        findDataPanel.add(buttonPanel, BorderLayout.EAST);
        findDataPanel.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 5));
//        findDataPanel.add("p left", jbLaunchGuide);

        
        

        final JPanel finalThis = this;
        final JComboBox localSourceCombo = jcomboLocal;

        final JButton jbBrowseLocal = new JButton("  Browse...  ");
        jbBrowseLocal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
//            	try {
//            		lastFolder = new File(localSourceCombo.getSelectedItem().toString());
//            	} catch (Exception ex) {
//            	}
//                // Set up File Chooser
//                if (lastFolder == null) {
//                    String wctprop = WCTProperties.getWCTProperty("customLocal");
//                    if (wctprop != null) {
//                        lastFolder = new File(wctprop);
//                    }
//                    else {
//                        lastFolder = new File(System.getProperty("user.home"));
//                    }
//                }
//                JFileChooser fc = new JFileChooser(lastFolder);
//                fc.setDialogTitle("Choose Directory of Data Files");
//                int returnVal = fc.showOpenDialog(finalThis);
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    if (fc.getSelectedFile().isDirectory()) {
//                        lastFolder = fc.getSelectedFile();
//                        localString = fc.getSelectedFile().toString();
//                        jcomboLocal.setSelectedItem(localString);
//                    }
//                    else if (fc.getSelectedFile().isFile()) {
//                        lastFolder = fc.getSelectedFile().getParentFile();
//                        localString = fc.getSelectedFile().getParent();
//                        jcomboLocal.setSelectedItem(localString);
//                    }
//                    submitListener.actionPerformed(new ActionEvent(jbSubmitLocal, 0, "Submit Local"));
//                }
                

                
                
                
                
                FolderChooser folderChooser = new FolderChooser() {
                    protected JDialog createDialog(Component parent)
                    	throws HeadlessException {
                    	JDialog dlg = super.createDialog(parent);
                    	dlg.setTitle("Select Directory");
                    	dlg.setLocation((int)jbBrowseLocal.getLocationOnScreen().getX()+jbBrowseLocal.getWidth()+4, 
                    			(int)jbBrowseLocal.getLocationOnScreen().getY());
                    	return dlg;
                    }
                };
                
                folderChooser.setRecentListVisible(false);
                folderChooser.setAvailableButtons(FolderChooser.BUTTON_REFRESH | FolderChooser.BUTTON_DESKTOP 
                		| FolderChooser.BUTTON_MY_DOCUMENTS);
            	try {
            		lastFolder = new File(localSourceCombo.getSelectedItem().toString());
                    if (lastFolder != null && lastFolder.length() > 0) {
                        lastFolder = folderChooser.getFileSystemView().createFileObject(lastFolder.toString());
                    }
            	} catch (Exception ex) {
            	}
                folderChooser.setCurrentDirectory(lastFolder);
                folderChooser.setFileHidingEnabled(true);
                folderChooser.setPreferredSize(new Dimension(320, 500));
                int result = folderChooser.showOpenDialog(finalThis.getParent());
                if (result == FolderChooser.APPROVE_OPTION) {
                    lastFolder = folderChooser.getSelectedFile();
                    localString = lastFolder.toString();
                    File selectedFile = folderChooser.getSelectedFile();
                    if (selectedFile != null) {
                    	jcomboLocal.setSelectedItem(selectedFile.toString());
                    }
                    else {
                    	jcomboLocal.setSelectedItem("");
                    }
                    submitListener.actionPerformed(new ActionEvent(jbSubmitLocal, 0, "Submit Local"));
                }
                
            }
        });


        jbBrowseLocal.setMnemonic(KeyEvent.VK_B);
        jbSubmitLocal = new JButton("  List Files  ");
        jbSubmitLocal.setActionCommand("Submit Local");
        jbSubmitLocal.addActionListener(submitListener);
        jbSubmitLocal.setDefaultCapable(true);
        jbSubmitLocal.setMnemonic(KeyEvent.VK_L);
        statusLocal = new JLabel();

        jcomSortByLocal = new JComboBox(sortByModelLocal);
        jcomSortByLocal.setActionCommand("Local");
        jcomSortByLocal.addActionListener(sortByListener);

        // Local Directory input panel
        JPanel localPanel2 = new JPanel();
        localPanel2.setLayout(new BorderLayout());
        localPanel2.add(jbBrowseLocal, BorderLayout.WEST);
        localPanel2.add(jcomboLocal, BorderLayout.CENTER);
        localPanel2.add(jbSubmitLocal, BorderLayout.EAST);

        jtfLocalListFilter.addKeyListener(filterKeyListener);
        
        JPanel localPanel = new JPanel();
        localPanel.setLayout(new RiverLayout());
        localPanel.add("center", new JLabel("Access to Data Stored on Local Disk"));
        localPanel.add("p left", jbBrowseLocal);
        localPanel.add("tab hfill", jcomboLocal);
        if (showDescriptionEntries) {
            localPanel.add("br", new JLabel("Description (optional)"));
            localPanel.add("tab hfill", jcomboDataDescLocal);
        }
        localPanel.add("p left hfill", jbSubmitLocal);
        localPanel.add("left", jcbListAllFilesLocal);
        localPanel.add("hfill", statusLocal);
        localPanel.add("right", new JLabel("Filter:"));
        localPanel.add("right", jtfLocalListFilter);
        localPanel.add("right", new JLabel("  "));
        localPanel.add("right", new JLabel("Sort By:"));
        localPanel.add("right", jcomSortByLocal);
//        localPanel.add("right", localFilterPanel);









        jbSubmitCustom = new JButton("  List Files  ");
        jbSubmitCustom.setActionCommand("Submit Custom");
        jbSubmitCustom.addActionListener(submitListener);
        jbSubmitCustom.setDefaultCapable(true);
        jbSubmitCustom.setMnemonic(KeyEvent.VK_L);
        statusCustom = new JLabel();

        jtfCustomListFilter.addKeyListener(filterKeyListener);
        
        jcomSortByCustom = new JComboBox(sortByModelCustom);
        jcomSortByCustom.setActionCommand("Custom");
        jcomSortByCustom.addActionListener(sortByListener);

        JPanel customPanel = new JPanel();
        customPanel.setLayout(new RiverLayout());
        customPanel.add("center", new JLabel("Access to Data Stored in a Remote HTTP or FTP Location"));
        customPanel.add("p left", new JLabel("Enter Custom URL"));
        customPanel.add("tab hfill", jcomboCustom);
        if (showDescriptionEntries) {
            customPanel.add("br", new JLabel("Description (optional)"));
            customPanel.add("tab hfill", jcomboDataDescCustom);
        }
        customPanel.add("p left hfill", jbSubmitCustom);
        customPanel.add("left", jcbListAllFilesCustom);
        customPanel.add("hfill", statusCustom);
        customPanel.add("right", new JLabel("Filter:"));
        customPanel.add("right", jtfCustomListFilter);
        customPanel.add("right", new JLabel("  "));
        customPanel.add("right", new JLabel("Sort By:"));
        customPanel.add("right", jcomSortByCustom);




        jtfTHREDDSListFilter.addKeyListener(filterKeyListener);
        
        jbSubmitThredds = new JButton("  List Files  ");
        jbSubmitThredds.setActionCommand("Submit THREDDS");
        jbSubmitThredds.addActionListener(submitListener);
        jbSubmitThredds.setDefaultCapable(true);
        jbSubmitThredds.setMnemonic(KeyEvent.VK_L);
        statusTHREDDS = new JLabel();

        JPanel threddsPanel = new JPanel();
        threddsPanel.setLayout(new RiverLayout());
        threddsPanel.add("center", new JLabel("Access to Data Stored in a THREDDS Data Server"));
        threddsPanel.add("p left", new JLabel("Enter THREDDS Catalog URL"));
        threddsPanel.add("tab hfill", jcomboThredds);
        if (showDescriptionEntries) {
            threddsPanel.add("br", new JLabel("Description (optional)"));
            threddsPanel.add("tab hfill", jcomboDataDescThredds);
        }
        threddsPanel.add("p left", jbSubmitThredds);
        threddsPanel.add("tab hfill", statusTHREDDS);
//        threddsPanel.add("right", new JLabel("Filter:"));
//        threddsPanel.add("right", jtfTHREDDSListFilter);
//        threddsPanel.add("right", new JLabel("  "));
        


        jbSubmitSSH = new JButton("  List Files  ");
        jbSubmitSSH.setActionCommand("Submit SSH");
        jbSubmitSSH.addActionListener(submitListener);
        jbSubmitSSH.setDefaultCapable(true);
        jbSubmitSSH.setMnemonic(KeyEvent.VK_L);
        statusSSH = new JLabel();

        JPanel sshPanel = new JPanel();       
        sshPanel.setLayout(new RiverLayout());
        sshPanel.add("center", new JLabel("Access to Data Stored in a Remote SSH Location"));
        sshPanel.add("p center", new JLabel("User"));
        sshPanel.add("center", jcomboSshUser);
        sshPanel.add("center", new JLabel("Host"));
        sshPanel.add("center", jcomboSshServer);
        sshPanel.add("br", new JLabel("Remote Directory"));
        sshPanel.add("tab hfill", jcomboSshDir);
        if (showDescriptionEntries) {
            sshPanel.add("br", new JLabel("Description (optional)"));
            sshPanel.add("tab hfill", jcomboDataDescSSH);
        }
        sshPanel.add("p left", jbSubmitSSH);
        sshPanel.add("tab hfill", statusSSH);


        
        
        
        
        
        
        
        
        
//        JButton jbSubmitSingleFile = new JButton("  Load Data  ");
//        jbSubmitSingleFile.setActionCommand("Submit Single File");
//        jbSubmitSingleFile.addActionListener(submitListener);
//        jbSubmitSingleFile.setDefaultCapable(true);
//        jbSubmitSingleFile.setMnemonic(KeyEvent.VK_L);

        JButton jbSingleFileBrowseLocal = new JButton("  Browse...  ");
        jbSingleFileBrowseLocal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // Set up File Chooser
                if (lastFolder == null) {
                    //lastFolder = new File(System.getProperty("user.home"));
                    String wctprop = WCTProperties.getWCTProperty("customLocal");
                    if (wctprop != null) {
                        lastFolder = new File(wctprop);
                    }
                    else {
                        lastFolder = new File(System.getProperty("user.home"));
                    }
                }
                JFileChooser fc = new JFileChooser(lastFolder);
                fc.setDialogTitle("Choose Single File To Load");
                int returnVal = fc.showOpenDialog(finalThis);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    lastFolder = fc.getSelectedFile().getParentFile();
                    singleFileSourceString = fc.getSelectedFile().toString();
                    jcomboSingleFileSource.setSelectedItem(singleFileSourceString);
                    submitListener.actionPerformed(new ActionEvent(jbSubmitLocal, 0, "Submit Single File"));
                }
            }
        });


        JPanel sfPanel = new JPanel();
        sfPanel.setLayout(new RiverLayout());
        sfPanel.add("center", new JLabel("Access to a single file (local, remote (HTTP) or OPeNDAP)"));
        sfPanel.add("p left", jbSingleFileBrowseLocal);
        sfPanel.add("tab hfill", jcomboSingleFileSource);
        JButton favoritesButton = new JButton("Add to Favorites");
        favoritesButton.setActionCommand("Add to Favorites");
        favoritesButton.addActionListener(favoritesListener);
        sfPanel.add("br left", favoritesButton);
        
        
//        if (showDescriptionEntries) {
//            localPanel.add("br", new JLabel("Description (optional)"));
//            localPanel.add("tab hfill", jcomboDataDescLocal);
//        }
//        sfPanel.add("p left ", jbSubmitSingleFile);
//        localPanel.add("tab hfill", statusLocal);
//        localPanel.add("right", new JLabel("Sort By:"));
//        localPanel.add("right", jcomSortByLocal);

        
        
        JPanel favoritesPanel = new JPanel();
        favoritesPanel.setLayout(new RiverLayout());
        favoritesPanel.add("center", new JLabel("Favorite Data Files"));
//        starredPanel.add("p left", new JLabel("Enter Custom URL"));
//        starredPanel.add("tab hfill", jcomboCustom);
//        if (showDescriptionEntries) {
//        	starredPanel.add("br", new JLabel("Description (optional)"));
//        	starredPanel.add("tab hfill", jcomboDataDescCustom);
//        }
//        starredPanel.add("p left hfill", jbSubmitCustom);
//        starredPanel.add("left", jcbListAllFilesCustom);
//        favoritesPanel.add("left", new JLabel(" "));
        
        
        JButton saveListButton = new JButton("Save List");
        saveListButton.setActionCommand("Save List");
        saveListButton.addActionListener(favoritesListener);
        JButton loadListButton = new JButton("Load List");
        loadListButton.setActionCommand("Load List");
        loadListButton.addActionListener(favoritesListener);
//        JButton bundleButton = new JButton("Bundle");
//        bundleButton.setActionCommand("Bundle");
//        bundleButton.addActionListener(favoritesListener);
        JButton removeButton = new JButton("Remove");
        removeButton.setActionCommand("Remove");
        removeButton.addActionListener(favoritesListener);
//        JButton removeAllButton = new JButton("Remove All");
//        removeAllButton.setActionCommand("Remove All");
//        removeAllButton.addActionListener(favoritesListener);

        saveListButton.setPreferredSize(new Dimension(70, (int)saveListButton.getPreferredSize().getHeight()));
        loadListButton.setPreferredSize(new Dimension(70, (int)loadListButton.getPreferredSize().getHeight()));
//        bundleButton.setPreferredSize(new Dimension(70, (int)bundleButton.getPreferredSize().getHeight()));
        removeButton.setPreferredSize(new Dimension(70, (int)removeButton.getPreferredSize().getHeight()));
//        removeAllButton.setPreferredSize(new Dimension(90, (int)removeAllButton.getPreferredSize().getHeight()));

        
        jtfFavoriteListFilter.addKeyListener(filterKeyListener);

        
        favoritesPanel.add("p left", loadListButton);
        favoritesPanel.add("left", saveListButton);
        favoritesPanel.add("left", removeButton);
//        favoritesPanel.add("left", bundleButton);
        favoritesPanel.add("hfill", new JLabel(" "));
        
        statusFavorite = new JLabel();
//        favoritesPanel.add("p hfill", statusFavorite);
        favoritesPanel.add("right", new JLabel("Filter:"));
        favoritesPanel.add("right", jtfFavoriteListFilter);
//        favoritesPanel.add("right", new JLabel("  "));
//        favoritesPanel.add("right", new JLabel("Sort By:"));
//        jcomSortByFavorites = new JComboBox(sortByModelFavorites);
//        favoritesPanel.add("right", jcomSortByFavorites);

//        favoritesPanel.add("br left", removeButton);
//        favoritesPanel.add("left", removeAllButton);
        
        
        
        
        
        
        
//          OLD ORDER        
//        tabPane.add(hasPanel, "NCDC");
//        tabPane.add(classPanel, "CLASS");
//        tabPane.add(localPanel, "Local");
//        tabPane.add(customPanel, "Custom");
//        tabPane.add(threddsPanel, "THREDDS");
//        tabPane.add(sfPanel, "Single File/URL");
//
////        tabPane.add(favoritesPanel, "Favorites");
//        
//        oldTabOrderLookup.put(0, "NCDC");
//        oldTabOrderLookup.put(1, "CLASS");
//        oldTabOrderLookup.put(2, "Local");
//        oldTabOrderLookup.put(3, "Custom");
//        oldTabOrderLookup.put(4, "THREDDS");
//        oldTabOrderLookup.put(5, "Single File/URL");

        
        
        CURRENT_TAB_ORDER_LOOKUP.put(7, "Find Data");
        CURRENT_TAB_ORDER_LOOKUP.put(2, "Local Files");
        CURRENT_TAB_ORDER_LOOKUP.put(3, "Remote Files");
        CURRENT_TAB_ORDER_LOOKUP.put(5, "Single File/URL");
        CURRENT_TAB_ORDER_LOOKUP.put(4, "THREDDS");
        CURRENT_TAB_ORDER_LOOKUP.put(0, "NCDC HAS Order");
        CURRENT_TAB_ORDER_LOOKUP.put(1, "CLASS Order");
        CURRENT_TAB_ORDER_LOOKUP.put(6, "Favorites");
        
        
        TAB_PANEL_LOOKUP.put("Find Data", findDataPanel);
        TAB_PANEL_LOOKUP.put("Local Files", localPanel);
        TAB_PANEL_LOOKUP.put("Remote Files", customPanel);
        TAB_PANEL_LOOKUP.put("Single File/URL", sfPanel);
        TAB_PANEL_LOOKUP.put("THREDDS", threddsPanel);
        TAB_PANEL_LOOKUP.put("NCDC HAS Order", hasPanel);
        TAB_PANEL_LOOKUP.put("CLASS Order", classPanel);
        TAB_PANEL_LOOKUP.put("Favorites", favoritesPanel);

        for (String tabName : CURRENT_TAB_ORDER_LOOKUP.values()) {
        	tabPane.add(TAB_PANEL_LOOKUP.get(tabName), tabName);
        }
        
        
//	MOVED TO DataSelector, so we can better handle the tab event change this should fire        
//        int selectedTypeID = Integer.parseInt(WCTProperties.getWCTProperty("dataSourceType"));
//        int selectedTabIndex = new ArrayList<Integer>(CURRENT_TAB_ORDER_LOOKUP.keySet()).indexOf(selectedTypeID);
//        try {
//            tabPane.setSelectedIndex(selectedTabIndex);
//        } catch (Exception e) {
//        }




        this.setLayout(new BorderLayout());
        this.add(tabPane, BorderLayout.CENTER);

        
        
    }


    
    
    public JTabbedPane getDataTypeTabPane() {
        return tabPane;
    }
    
    public void clearListFilters() {
        jtfHASListFilter.setText("");
        jtfHASListFilter.dispatchEvent(new KeyEvent(jtfHASListFilter, KeyEvent.KEY_RELEASED, 0, 0, KeyStroke.getKeyStroke(' ').getKeyCode(), ' '));
        jtfCLASSListFilter.setText("");
        jtfCLASSListFilter.dispatchEvent(new KeyEvent(jtfCLASSListFilter, KeyEvent.KEY_RELEASED, 0, 0, KeyStroke.getKeyStroke(' ').getKeyCode(), ' '));
        jtfLocalListFilter.setText("");
        jtfLocalListFilter.dispatchEvent(new KeyEvent(jtfLocalListFilter, KeyEvent.KEY_RELEASED, 0, 0, KeyStroke.getKeyStroke(' ').getKeyCode(), ' '));
        jtfCustomListFilter.setText("");
        jtfCustomListFilter.dispatchEvent(new KeyEvent(jtfCustomListFilter, KeyEvent.KEY_RELEASED, 0, 0, KeyStroke.getKeyStroke(' ').getKeyCode(), ' '));
        jtfTHREDDSListFilter.setText("");
        jtfTHREDDSListFilter.dispatchEvent(new KeyEvent(jtfTHREDDSListFilter, KeyEvent.KEY_RELEASED, 0, 0, KeyStroke.getKeyStroke(' ').getKeyCode(), ' '));
        
    }
    

    /**
     * Get currently selected data type (i.e. selected tab).
     * @return
     */
    public String getDataType() {
    	//        System.out.println("SELECTED TAB INDEX: "+tabPane.getSelectedIndex());



    	//        if (tabPane.getSelectedIndex() == 0) {
    	//            return WCTDataSourceDB.NCDC_HAS_FTP;
    	//        }
    	//        else if (tabPane.getSelectedIndex() == 1) {
    	//            return WCTDataSourceDB.CLASS_ORDER;
    	//        }
    	//        else if (tabPane.getSelectedIndex() == 2) {
    	//            return WCTDataSourceDB.LOCAL_DISK;
    	//        }
    	//        else if (tabPane.getSelectedIndex() == 3) {
    	//            return WCTDataSourceDB.URL_DIRECTORY;
    	//        }
    	//        else if (tabPane.getSelectedIndex() == 4) {
    	//            return WCTDataSourceDB.THREDDS;
    	//        }
    	////        else if (tabPane.getSelectedIndex() == 5) {
    	////            return WCTDataSourceDB.SSH_DIRECTORY;
    	////        }
    	//        else if (tabPane.getSelectedIndex() == 5) {
    	//            return WCTDataSourceDB.SINGLE_FILE;
    	//        }
    	//        else if (tabPane.getSelectedIndex() == 6) {
    	//            return WCTDataSourceDB.FAVORITES;
    	//        }
    	//        else {
    	//            return "UNKNOWN";
    	//        }








    	//      currentTabOrderLookup.put(2, "Local Files");
    	//      currentTabOrderLookup.put(3, "Remote Files");
    	//      currentTabOrderLookup.put(5, "Single File/URL");
    	//      currentTabOrderLookup.put(4, "THREDDS");
    	//      currentTabOrderLookup.put(0, "NCDC HAS Order");
    	//      currentTabOrderLookup.put(1, "CLASS Order");



    	int selectedTabPaneIndex = tabPane.getSelectedIndex();
    	String selectedTabPaneName = tabPane.getTitleAt(selectedTabPaneIndex);
    	//  	int selectedTypeID = new ArrayList<String>(currentTabOrderLookup.values()).indexOf(selectedTabPaneName);
    	//      int selectedTabIndex = new ArrayList<Integer>(currentTabOrderLookup.keySet()).indexOf(selectedTypeID);



    	if (selectedTabPaneName.equals("NCDC HAS Order")) {
    		return WCTDataSourceDB.NCDC_HAS_FTP;
    	}
    	else if (selectedTabPaneName.equals("CLASS Order")) {
    		return WCTDataSourceDB.CLASS_ORDER;
    	}
    	else if (selectedTabPaneName.equals("Local Files")) {
    		return WCTDataSourceDB.LOCAL_DISK;
    	}
    	else if (selectedTabPaneName.equals("Remote Files")) {
    		return WCTDataSourceDB.URL_DIRECTORY;
    	}
    	else if (selectedTabPaneName.equals("THREDDS")) {
    		return WCTDataSourceDB.THREDDS;
    	}
    	//        else if (tabPane.getSelectedIndex() == 5) {
    	//            return WCTDataSourceDB.SSH_DIRECTORY;
    	//        }
    	else if (selectedTabPaneName.equals("Single File/URL")) {
    		return WCTDataSourceDB.SINGLE_FILE;
    	}
    	else if (selectedTabPaneName.equals("Favorites")) {
    		return WCTDataSourceDB.FAVORITES;
    	}
    	else {
    		return "UNKNOWN";
    	}
    }

    
    /**
     * Set the currently selected data type (i.e. selected tab)
     * from static variables defined in WCTDataSourceDB, such as 
     * WCTDataSourceDB.LOCAL_DISK
     * @param dataType
     * @throws Exception
     */
    public void setDataType(String dataType) throws Exception {
    	
//    	if (dataType.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
//            tabPane.setSelectedIndex(0);
//        }
//        else if (dataType.equals(WCTDataSourceDB.CLASS_ORDER)) {
//            tabPane.setSelectedIndex(1);
//        }
//        else if (dataType.equals(WCTDataSourceDB.LOCAL_DISK)) {
//            tabPane.setSelectedIndex(2);
//        }
//        else if (dataType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
//            tabPane.setSelectedIndex(3);
//        }
//        else if (dataType.equals(WCTDataSourceDB.THREDDS)) {
//            tabPane.setSelectedIndex(4);
//        }
////        else if (dataType.equals(WCTDataSourceDB.SSH_DIRECTORY)) {
////            tabPane.setSelectedIndex(5);
////        }
//        else if (dataType.equals(WCTDataSourceDB.SINGLE_FILE)) {
//            tabPane.setSelectedIndex(5);
//        }
//        else if (dataType.equals(WCTDataSourceDB.FAVORITES)) {
//            tabPane.setSelectedIndex(6);
//        }
//        else {
//            throw new Exception("dataType: '"+dataType+"' is not supported!!!");
//        }
    	
    	
    	
    	int selectedTabPaneIndex = tabPane.getSelectedIndex();
    	String selectedTabPaneName = tabPane.getTitleAt(selectedTabPaneIndex);
    	//  	int selectedTypeID = new ArrayList<String>(currentTabOrderLookup.values()).indexOf(selectedTabPaneName);
    	//      int selectedTabIndex = new ArrayList<Integer>(currentTabOrderLookup.keySet()).indexOf(selectedTypeID);

    	//      currentTabOrderLookup.put(2, "Local Files");
    	//      currentTabOrderLookup.put(3, "Remote Files");
    	//      currentTabOrderLookup.put(5, "Single File/URL");
    	//      currentTabOrderLookup.put(4, "THREDDS");
    	//      currentTabOrderLookup.put(0, "NCDC HAS Order");
    	//      currentTabOrderLookup.put(1, "CLASS Order");

//        tabPanelLookup.put("Local Files", localPanel);
//        tabPanelLookup.put("Remote Files", customPanel);
//        tabPanelLookup.put("Single File/URL", sfPanel);
//        tabPanelLookup.put("THREDDS", threddsPanel);
//        tabPanelLookup.put("NCDC HAS Order", hasPanel);
//        tabPanelLookup.put("CLASS Order", classPanel);


    	if (dataType.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            tabPane.setSelectedComponent(TAB_PANEL_LOOKUP.get("NCDC HAS Order"));
        }
        else if (dataType.equals(WCTDataSourceDB.CLASS_ORDER)) {
            tabPane.setSelectedComponent(TAB_PANEL_LOOKUP.get("CLASS Order"));
        }
        else if (dataType.equals(WCTDataSourceDB.LOCAL_DISK)) {
            tabPane.setSelectedComponent(TAB_PANEL_LOOKUP.get("Local Files"));
        }
        else if (dataType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
            tabPane.setSelectedComponent(TAB_PANEL_LOOKUP.get("Remote Files"));
        }
        else if (dataType.equals(WCTDataSourceDB.THREDDS)) {
            tabPane.setSelectedComponent(TAB_PANEL_LOOKUP.get("THREDDS"));
        }
//        else if (dataType.equals(WCTDataSourceDB.SSH_DIRECTORY)) {
//            tabPane.setSelectedIndex(5);
//        }
        else if (dataType.equals(WCTDataSourceDB.SINGLE_FILE)) {
            tabPane.setSelectedComponent(TAB_PANEL_LOOKUP.get("Single File/URL"));
        }
        else if (dataType.equals(WCTDataSourceDB.FAVORITES)) {
            tabPane.setSelectedComponent(TAB_PANEL_LOOKUP.get("Favorites"));
        }
        else {
            throw new Exception("dataType: '"+dataType+"' is not supported!!!");
        }

    	
    	
    }

    /**
     * Gets the HAS Job Number, CLASS Order Number, Local File, URL or THREDDS Catalog 
     * depending on the current data type selected.
     * @return
     */
    public String getDataLocation() {
        if (getDataType().equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            return jcomboHAS.getSelectedItem().toString().trim();
        }
        else if (getDataType().equals(WCTDataSourceDB.CLASS_ORDER)) {
            return jcomboCLASS.getSelectedItem().toString().trim();
        }
        else if (getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
            return jcomboLocal.getSelectedItem().toString().trim();
        }
        else if (getDataType().equals(WCTDataSourceDB.URL_DIRECTORY)) {
            return jcomboCustom.getSelectedItem().toString().trim();
        }
        else if (getDataType().equals(WCTDataSourceDB.THREDDS)) {
            return jcomboThredds.getSelectedItem().toString().trim();
        }
        else if (getDataType().equals(WCTDataSourceDB.SSH_DIRECTORY)) {
            return jcomboSshUser.getSelectedItem().toString().trim() + "@" +
            jcomboSshServer.getSelectedItem().toString().trim() + ":/" +
            jcomboSshDir.getSelectedItem().toString().trim();
        }
        else if (getDataType().equals(WCTDataSourceDB.SINGLE_FILE)) {
            return jcomboSingleFileSource.getSelectedItem().toString().trim();
        }
//        else if (getDataType().equals(WCTDataSourceDB.FAVORITES)) {
//            return jcomboSingleFileSource.getSelectedItem().toString().trim();
//        }
        else {
            return "UNKNOWN";
        }
    }

    /**
     * Sets the HAS Job Number, CLASS Order Number, Local File, URL or THREDDS Catalog 
     * depending on the current data type provided.

     * @param dataType
     * @param location
     * @throws Exception
     */
    public void setDataLocation(String dataType, String location) throws Exception {
        if (dataType.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            jcomboHAS.setSelectedItem(location);
        }
        else if (dataType.equals(WCTDataSourceDB.CLASS_ORDER)) {
            jcomboCLASS.setSelectedItem(location);
        }
        else if (dataType.equals(WCTDataSourceDB.LOCAL_DISK)) {
            jcomboLocal.setSelectedItem(location);
        }
        else if (dataType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
            jcomboCustom.setSelectedItem(location);
        }
        else if (dataType.equals(WCTDataSourceDB.THREDDS)) {
            jcomboThredds.setSelectedItem(location);
        }
        else if (dataType.equals(WCTDataSourceDB.SSH_DIRECTORY)) {
            String user = location.substring(0, location.indexOf("@"));
            String server = location.substring(location.indexOf("@")+1, location.indexOf(":/"));
            String dir = location.substring(location.indexOf(":/")+1, location.length());
            jcomboSshUser.setSelectedItem(user);
            jcomboSshServer.setSelectedItem(server);
            jcomboSshDir.setSelectedItem(dir);
        }        
        else if (dataType.equals(WCTDataSourceDB.SINGLE_FILE)) {
            jcomboSingleFileSource.setSelectedItem(location);
        }
        else if (dataType.equals(WCTDataSourceDB.FAVORITES)) {
            jcomboSingleFileSource.setSelectedItem(location);
        }
        else {
            throw new Exception("dataType: '"+dataType+"' is not supported!!!");
        }
    }

    
    public String getDataDescription() {

        if (getDataType().equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            if (jcomboDataDescHAS.getSelectedIndex() < 0) {
                return "";
            }
            else {
                return jcomboDataDescHAS.getSelectedItem().toString();
            }
        }
        else if (getDataType().equals(WCTDataSourceDB.CLASS_ORDER)) {
            if (jcomboDataDescCLASS.getSelectedIndex() < 0) {
                return "";
            }
            else {
                return jcomboDataDescCLASS.getSelectedItem().toString();
            }
        }
        else if (getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
            if (jcomboDataDescLocal.getSelectedIndex() < 0) {
                return "";
            }
            else {
                return jcomboDataDescLocal.getSelectedItem().toString();
            }
        }
        else if (getDataType().equals(WCTDataSourceDB.URL_DIRECTORY)) {
            if (jcomboDataDescCustom.getSelectedIndex() < 0) {
                return "";
            }
            else {
                return jcomboDataDescCustom.getSelectedItem().toString();
            }
        }
        else if (getDataType().equals(WCTDataSourceDB.THREDDS)) {
            if (jcomboDataDescThredds.getSelectedIndex() < 0) {
                return "";
            }
            else {
                return jcomboDataDescThredds.getSelectedItem().toString();
            }
        }
        else if (getDataType().equals(WCTDataSourceDB.SSH_DIRECTORY)) {
            if (jcomboDataDescSSH.getSelectedIndex() < 0) {
                return "";
            }
            else {
                return jcomboDataDescSSH.getSelectedItem().toString();
            }
        }
        else {
            return "UNKNOWN";
        }
    }



    public boolean getListAllFiles() {
        if (getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
            return jcbListAllFilesLocal.isSelected();
        }
        else if (getDataType().equals(WCTDataSourceDB.URL_DIRECTORY)) {
            return jcbListAllFilesCustom.isSelected();
        }
        else {
            return false;
        }
    }

    public void setListAllFiles(String dataType, boolean isSelected) throws WCTException {
        if (dataType.equals(WCTDataSourceDB.LOCAL_DISK)) {
            jcbListAllFilesLocal.setSelected(isSelected);
        }
        else if (dataType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
            jcbListAllFilesCustom.setSelected(isSelected);
        }
        else {
            throw new WCTException("'listAllFiles' option is not available for datatype: "+dataType);
        }
    }


    public ScanResultsComparator getSortByComparator() {
        if (getDataType().equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            return getComparator(jcomSortByHAS.getSelectedItem().toString());
        }
        else if (getDataType().equals(WCTDataSourceDB.CLASS_ORDER)) {
            return getComparator(jcomSortByCLASS.getSelectedItem().toString());
        }
        else if (getDataType().equals(WCTDataSourceDB.LOCAL_DISK)) {
            return getComparator(jcomSortByLocal.getSelectedItem().toString());
        }
        else if (getDataType().equals(WCTDataSourceDB.URL_DIRECTORY)) {
            return getComparator(jcomSortByCustom.getSelectedItem().toString());
        }
//        else if (getDataType().equals(NDITDataSourceDB.THREDDS)) {
//            return getComparator(jcomSortByThredds.getSelectedItem().toString());
//        }
//        else if (getDataType().equals(NDITDataSourceDB.SSH_DIRECTORY)) {
//            return getComparator(jcomSortBySSH.getSelectedItem().toString());
//        }
        else {
            return new ScanResultsComparator(ScanResultsComparator.CompareBy.FILE_NAME);
        }
    }

    public void setSortByComparator(String dataType, ScanResultsComparator comparator) throws Exception {
        if (dataType.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            jcomSortByHAS.setSelectedItem(" "+getComparatorString(comparator.getCompareBy())+" ");
        }
        else if (dataType.equals(WCTDataSourceDB.CLASS_ORDER)) {
            jcomSortByCLASS.setSelectedItem(" "+getComparatorString(comparator.getCompareBy())+" ");
        }
        else if (dataType.equals(WCTDataSourceDB.LOCAL_DISK)) {
            jcomSortByLocal.setSelectedItem(" "+getComparatorString(comparator.getCompareBy())+" ");
        }
        else if (dataType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
            jcomSortByCustom.setSelectedItem(" "+getComparatorString(comparator.getCompareBy())+" ");
        }
        else if (dataType.equals(WCTDataSourceDB.THREDDS)) {
        }
        else if (dataType.equals(WCTDataSourceDB.SSH_DIRECTORY)) {
        }
        else if (dataType.equals(WCTDataSourceDB.SINGLE_FILE)) {
        }
        else {
            throw new Exception("dataType: '"+dataType+"' is not supported!!!");
        }

    }
    
    
    
    private ScanResultsComparator getComparator(String sortBy) {
        if (sortBy.trim().equalsIgnoreCase("Filename")) {
            return new ScanResultsComparator(ScanResultsComparator.CompareBy.FILE_NAME);
        }
        else if (sortBy.trim().equalsIgnoreCase("Timestamp Asc")) {
            return new ScanResultsComparator(ScanResultsComparator.CompareBy.TIMESTAMP_ASC);
        }
        else if (sortBy.trim().equalsIgnoreCase("Timestamp Desc")) {
            return new ScanResultsComparator(ScanResultsComparator.CompareBy.TIMESTAMP_DESC);
        }
        else if (sortBy.trim().equalsIgnoreCase("Product")) {
            return new ScanResultsComparator(ScanResultsComparator.CompareBy.PRODUCT_ID);
        }
        else {
            return new ScanResultsComparator(ScanResultsComparator.CompareBy.FILE_NAME);
        }
    }
    private String getComparatorString(ScanResultsComparator.CompareBy compareBy) {
        if (compareBy == ScanResultsComparator.CompareBy.FILE_NAME) {
            return "Filename";
        }
        else if (compareBy == ScanResultsComparator.CompareBy.TIMESTAMP_ASC) {
            return "Timestamp Asc";
        }
        else if (compareBy == ScanResultsComparator.CompareBy.TIMESTAMP_DESC) {
            return "Timestamp Desc";
        }
        else if (compareBy == ScanResultsComparator.CompareBy.PRODUCT_ID) {
            return "Product";
        }
        else {
            return "Filename";
        }
    }






    
    public void setStatus(String status, String dataType) {
        
//        System.out.println("status="+status+" dataType="+dataType);
        
        if (dataType.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            statusHAS.setText(status);
        }
        else if (dataType.equals(WCTDataSourceDB.CLASS_ORDER)) {
            statusCLASS.setText(status);
        }
        else if (dataType.equals(WCTDataSourceDB.LOCAL_DISK)) {
            statusLocal.setText(status);
        }
        else if (dataType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
            statusCustom.setText(status);
        }
        else if (dataType.equals(WCTDataSourceDB.THREDDS)) {
            statusTHREDDS.setText(status);
        }
    }

    public void setShowLoadingIcon(boolean showIcon, String dataType) {
        if (showIcon) {
            if (dataType.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
                statusHAS.setIcon(new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif")));            
            }
            else if (dataType.equals(WCTDataSourceDB.CLASS_ORDER)) {
                statusCLASS.setIcon(new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif")));            
            }
            else if (dataType.equals(WCTDataSourceDB.LOCAL_DISK)) {
                statusLocal.setIcon(new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif")));            
            }
            else if (dataType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
                statusCustom.setIcon(new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif")));            
            }
            else if (dataType.equals(WCTDataSourceDB.THREDDS)) {
                statusTHREDDS.setIcon(new ImageIcon(WCTViewer.class.getResource("/icons/ajax-loader.gif")));            
            }
        }
        else {
            if (dataType.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
                statusHAS.setIcon(null);            
            }
            else if (dataType.equals(WCTDataSourceDB.CLASS_ORDER)) {
                statusCLASS.setIcon(null);            
            }
            else if (dataType.equals(WCTDataSourceDB.LOCAL_DISK)) {
                statusLocal.setIcon(null);            
            }
            else if (dataType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
                statusCustom.setIcon(null);            
            }
            else if (dataType.equals(WCTDataSourceDB.THREDDS)) {
                statusTHREDDS.setIcon(null);            
            }
        }
    }


    
    
    

    public void updateHistory() {

        String type = this.getDataType();

        if (type.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            processHistory(jcomboHAS, prevHASList, "ncdc_hasnum", "prevHAS");            
        }
        else if (type.equals(WCTDataSourceDB.CLASS_ORDER)) {
            processHistory(jcomboCLASS, prevCLASSList, "classOrder", "prevCLASS");            
        }
        else if (type.equals(WCTDataSourceDB.LOCAL_DISK)) {
            localString = jcomboLocal.getSelectedItem().toString();
            processHistory(jcomboLocal, prevLocalList, "customLocal", "prevLocal");            
        }
        else if (type.equals(WCTDataSourceDB.URL_DIRECTORY)) {            
            processHistory(jcomboCustom, prevCustomList, "customURL", "prevCustom");
        }
        else if (type.equals(WCTDataSourceDB.THREDDS)) {
            processHistory(jcomboThredds, prevThreddsList, "threddsURL", "prevThredds");            
        }
        else if (type.equals(WCTDataSourceDB.SINGLE_FILE)) {
            processHistory(jcomboSingleFileSource, prevSingleFileSourceList, "singleFileSource", "prevSingleFileSource");            
        }
        
        
        // dataSourceType is an id that is different from the tab index, although it used to be the same
        // follwing is example code to convert between
//        int selectedTypeID = Integer.parseInt(WCTProperties.getWCTProperty("dataSourceType"));
//        int selectedTabIndex = new ArrayList<Integer>(currentTabOrderLookup.keySet()).indexOf(selectedTypeID);
//        
//        try {
//            tabPane.setSelectedIndex(selectedTabIndex);
//        } catch (Exception e) {
//        }

    	//      currentTabOrderLookup.put(2, "Local Files");
    	//      currentTabOrderLookup.put(3, "Remote Files");
    	//      currentTabOrderLookup.put(5, "Single File/URL");
    	//      currentTabOrderLookup.put(4, "THREDDS");
    	//      currentTabOrderLookup.put(0, "NCDC HAS Order");
    	//      currentTabOrderLookup.put(1, "CLASS Order");

//        tabPanelLookup.put("Local Files", localPanel);
//        tabPanelLookup.put("Remote Files", customPanel);
//        tabPanelLookup.put("Single File/URL", sfPanel);
//        tabPanelLookup.put("THREDDS", threddsPanel);
//        tabPanelLookup.put("NCDC HAS Order", hasPanel);
//        tabPanelLookup.put("CLASS Order", classPanel);

        
        
        int selectedTabIndex = tabPane.getSelectedIndex();
        int selectedTypeID = new ArrayList<Integer>(CURRENT_TAB_ORDER_LOOKUP.keySet()).get(selectedTabIndex);
        
        
//        JOptionPane.showMessageDialog(this, "tab="+selectedTabIndex + " , typeID="+selectedTypeID);
        

//        WCTProperties.setWCTProperty("dataSourceType", String.valueOf(tabPane.getSelectedIndex()));
        WCTProperties.setWCTProperty("dataSourceType", String.valueOf(selectedTypeID));

        
        
        
    }

    
    private void processHistory(JComboBox jcombo, ArrayList<String> prevList, String propsDataLocation, String propsHistoryID) {
        String selected = jcombo.getSelectedItem().toString();

        WCTProperties.setWCTProperty(propsDataLocation, this.getDataLocation());
        jcombo.insertItemAt(selected, 0);
        
        prevList.remove(selected);
        prevList.add(0, selected);
        WCTUtils.removeDuplicateWithOrder(prevList);
        
        if (prevList.size() > HISTORY_SIZE) {
            prevList.ensureCapacity(HISTORY_SIZE);
            prevList.trimToSize();
        }
        // limit to HISTORY_SIZE
        int numPrev = (prevList.size()<HISTORY_SIZE) ? prevList.size() : HISTORY_SIZE;
        for (int n=0; n<numPrev; n++) {
//            System.out.println(propsHistoryID+n+"="+ prevList.get(n));
            WCTProperties.setWCTProperty(propsHistoryID+n, prevList.get(n));
        }
        jcombo.setModel(new DefaultComboBoxModel(prevList.toArray()));

    }

    
    
    public JComboBox getSingleFileSourceCombo() {
        return jcomboSingleFileSource;
    }
    

    
    
}
