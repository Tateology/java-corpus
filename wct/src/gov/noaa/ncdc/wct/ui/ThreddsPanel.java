package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.thredds.ThreddsUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;

import thredds.catalog.InvAccess;
import thredds.catalog.InvDataset;

public class ThreddsPanel extends JPanel {

    private String currentRootCatalog;
    private String currentCatalogURL;
    private URL[] selectedDataAccessURLArray;
    private int[] selectedIndices;
    
    private JXLabel historyTrailLabel = new JXLabel();
    private JLabel statusLabel = new JLabel();
    
    private JXHyperlink browseLink = new JXHyperlink();
    private JXHyperlink copyLink = new JXHyperlink();
    private JXHyperlink saveLink = new JXHyperlink();
    private JXList itemList = new JXList();
    private JButton jbBack = new JButton("Back");
    private DefaultListModel listModel = new DefaultListModel();

    final private ThreddsUtils utils = new ThreddsUtils();
    final private ArrayList<InvDataset> historyList = new ArrayList<InvDataset>();

    final private JPanel linkPanel = new JPanel(new RiverLayout());
    final private ArrayList<JXHyperlink> addedLinkList = new ArrayList<JXHyperlink>();
    
    final private static Icon LOADING_ICON = new ImageIcon(WCTToolBar.class.getResource("/icons/ajax-loader.gif"));

    public ThreddsPanel() {
        createGUI();
    }
    
    private void createGUI() {
        this.setLayout(new RiverLayout());

        
//        itemList.setPreferredSize(new Dimension(100, (int)(itemList.getPreferredSize().height)));
//        itemList.setFilterEnabled(true);
        itemList.setCellRenderer(new ThreddsCellRenderer());
        itemList.setModel(listModel);
        itemList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
//                    processDataCommand();
                    processDataCommandInBackground();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        
        
        
        
        
        
        jbBack.setEnabled(false);
        jbBack.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                processBackCommand();
                statusLabel.setText("");
                statusLabel.setIcon(null);
            }
        });
        
        historyTrailLabel.setLineWrap(true);

        
        copyLink.setText("Copy Link");
        copyLink.setToolTipText("Copy the data access URL link to the clipboard");
        copyLink.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
//                System.out.println("linked clicked!");
                try {
                    ClipboardOwner owner = new ClipboardOwner() {
                        @Override
                        public void lostOwnership(Clipboard clipboard, Transferable contents) {
                        }
                    };
                    
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection( getSelectedDataAccessURLArray()[0].toString()), owner);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        browseLink.setText("Browse");
        browseLink.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
//                System.out.println("linked clicked!");
                try {
                    Desktop.getDesktop().browse(new URL(currentCatalogURL).toURI());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        this.add(jbBack, "left");
        this.add(historyTrailLabel, "hfill");
        
        linkPanel.add(statusLabel, "right");
        linkPanel.add(copyLink, "right");
        linkPanel.add(browseLink, "right");
        
        this.add(linkPanel, "right");
        
        JPanel legendPanel = new JPanel(new RiverLayout());
        legendPanel.setBorder(WCTUiUtils.myTitledBorder("Icon Legend", 3));
        legendPanel.add(new JLabel(new ImageIcon(ThreddsPanel.class.getResource("/icons/gnome-fs-directory.png"))), "left p");
        legendPanel.add(new JLabel("=  Directory"), "tab");
        legendPanel.add(new JLabel(new ImageIcon(ThreddsPanel.class.getResource("/icons/gnome-mime-text-O.png"))), "left p");
        legendPanel.add(new JLabel("=  OPeNDAP Access (subset)"), "tab");
        JLabel opendapInfoLabel = new JLabel("<html>Faster for files with multiple<br>variables and dimensions.</html>");
//        opendapInfoLabel.setLineWrap(true);
        legendPanel.add(opendapInfoLabel, "br left tab");
        legendPanel.add(new JLabel(new ImageIcon(ThreddsPanel.class.getResource("/icons/gnome-mime-text-H.png"))), "left p");
        legendPanel.add(new JLabel("=  HTTP Access (full download)"), "tab");
        JLabel httpInfoLabel = new JLabel("<html>Faster for files with single<br>variables and only x/y or<br>lat/lon dimensions.</html>");
//        httpInfoLabel.setLineWrap(true);
        legendPanel.add(httpInfoLabel, "br left tab");
        
        JPanel listLegendPanel = new JPanel(new BorderLayout());
        listLegendPanel.add(new JScrollPane(itemList), BorderLayout.CENTER);
        listLegendPanel.add(legendPanel, BorderLayout.EAST);
        
        this.add(listLegendPanel, "br hfill vfill");
    }
    
    
    public void addLink(JXHyperlink link) {
        addedLinkList.add(link);

        linkPanel.removeAll();
        linkPanel.add(statusLabel, "right");
        for (JXHyperlink l : addedLinkList) {
            linkPanel.add(l, "right");
        }
        linkPanel.add(copyLink, "right");
        linkPanel.add(browseLink, "right");
    }
    
    
    private void processDataCommandInBackground() throws Exception {
        

        foxtrot.AsyncWorker.post(new foxtrot.AsyncTask() {
            @Override
            public Object run() throws Exception {
                statusLabel.setText("Loading...   ");
                statusLabel.setIcon(LOADING_ICON);
                processDataCommand();
                return "DONE";
            }
			@Override
			public void failure(Throwable t) {
                statusLabel.setText("");
                statusLabel.setIcon(null);
                t.printStackTrace();
			}
			@Override
			public void success(Object o) {
                statusLabel.setText("");
                statusLabel.setIcon(null);
			}
        });
        
    }
    
    private void processDataCommand() {
        
        
        try {
            
            if (itemList.getSelectedIndices().length == 0) {
                return;
            }
            
            selectedIndices = itemList.getSelectedIndices();
            selectedDataAccessURLArray = new URL[selectedIndices.length];
            ArrayList<URL> urlList = new ArrayList<URL>();
            
            for (int index : selectedIndices) {
                ThreddsEntryType type = getEntryTypeFromListIndex(index);
                if (type == ThreddsEntryType.DIRECTORY) {
                    process(getInvDatasetFromListIndex(index));
                    selectedDataAccessURLArray = new URL[0];                                                         
                    break;
                }
                else if (type == ThreddsEntryType.OPENDAP) {
                    process(getOpendapInvAccessFromListIndex(index));
                    urlList.add(new URL(getOpendapInvAccessFromListIndex(index).getStandardUrlName()));
                }
                else if (type == ThreddsEntryType.HTTPSERVER) {
                    process(getHttpInvAccessFromListIndex(index));
                    urlList.add(new URL(getHttpInvAccessFromListIndex(index).getStandardUrlName()));
                }
                else {
                    throw new IndexOutOfBoundsException();
                }
            }
            
            urlList.toArray(selectedDataAccessURLArray);
            
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
    }
    
    private void processBackCommand() {
    	try {
            InvDataset prevDataset = null;
            if (historyList.size() > 1) {
                prevDataset = historyList.get(historyList.size()-2);
            }
            // currently, if null then use the base catalog
            if (prevDataset == null) {
                jbBack.setEnabled(false);
                jbBack.setToolTipText("");
                historyList.clear();
                listModel.clear();
                process(currentRootCatalog);
            }
            else {

                historyList.remove(historyList.size()-1);
                if (historyList.size() <= 2) {
                    jbBack.setEnabled(false);
                    jbBack.setToolTipText("To parent catalog entries");
                }
                historyList.remove(historyList.size()-1);

                String historyTrail = historyTrailLabel.getText();
                // do twice because target dataset will add it back
                historyTrail = historyTrail.substring(0, historyTrail.lastIndexOf(" /"));
                historyTrail = historyTrail.substring(0, historyTrail.lastIndexOf(" /"));
                historyTrailLabel.setText(historyTrail);

                listModel.clear();
                process(prevDataset);

            }
            
        } catch (IOException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(this, e1.getMessage(), "THREDDS Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public ThreddsEntryType getEntryTypeFromListIndex(int index) {
        // order is always: directories, opendap, httpserver
        if (index < utils.getDirectoryDatasetArray().size()) {
            return ThreddsEntryType.DIRECTORY;
        }
        else if (index >= utils.getDirectoryDatasetArray().size() && 
                 index < ( utils.getOpendapAccessArray().size() + utils.getDirectoryDatasetArray().size()) ) {
            return ThreddsEntryType.OPENDAP;
        }
        else if (index >= utils.getOpendapAccessArray().size() && 
                 index < ( utils.getHttpAccessArray().size() + 
                           utils.getOpendapAccessArray().size() + 
                           utils.getDirectoryDatasetArray().size()) ) {
            return ThreddsEntryType.HTTPSERVER;
        }
        else {
            throw new IndexOutOfBoundsException("Index of "+index+" not valid: " +
            		" Dir Size: "+utils.getDirectoryDatasetArray().size() +
            		", Opendap Size: "+utils.getOpendapAccessArray().size() +
            		", Http Size: "+utils.getHttpAccessArray().size());
        }
    }
    
    /**
     * Get InvDataset object from the single combined list index.  Assumed list order is:
     * Directory, Opendap, HTTP
     * @param index
     * @return
     */
    public InvDataset getInvDatasetFromListIndex(int index) {
        return utils.getDirectoryDatasetArray().get(index);
    }

    /**
     * Get InvAccess object from the single combined list index.  Assumed list order is:
     * Directory, Opendap, HTTP
     * @param index
     * @return
     */
    public InvAccess getOpendapInvAccessFromListIndex(int index) {
        return utils.getOpendapAccessArray().get(index-utils.getDirectoryDatasetArray().size());
    }
    /**
     * Get InvAccess object from the single combined list index.  Assumed list order is:
     * Directory, Opendap, HTTP
     * @param index
     * @return
     */
    public InvAccess getHttpInvAccessFromListIndex(int index) {
        return utils.getHttpAccessArray().get(index-utils.getDirectoryDatasetArray().size()-utils.getOpendapAccessArray().size());
    }
    
    
    
    
    
    
    

    public void process(String rootCatalogURI) throws IOException {

        this.currentRootCatalog = rootCatalogURI;
        this.currentCatalogURL = currentRootCatalog.replaceAll(".xml", ".html");
        
//        listModel.clear();
        utils.process(rootCatalogURI);

        historyList.add(null);
        if (historyList.size() > 1) {
            jbBack.setEnabled(true);
        }
        if (historyList.size() > 2) {
            if (historyList.get(historyList.size()-2) == null) {
                jbBack.setToolTipText("Back to catalog root");
            }
            else {
                jbBack.setToolTipText("Back to: "+historyList.get(historyList.size()-2));
            }
        }
        else {
            jbBack.setToolTipText("Back to catalog root");
        }
        
        historyTrailLabel.setText("Root");
        handleProcess();
    }

    public void process(InvDataset dataset) throws MalformedURLException {

        this.currentCatalogURL = dataset.getParentCatalog().getUriString().replaceAll(".xml", ".html");
        try {
            this.currentCatalogURL = dataset.getDatasets().get(0).getParentCatalog().getUriString().replaceAll(".xml", ".html");
        } catch (Exception e) {
            e.printStackTrace();
        }
        browseLink.setToolTipText(currentCatalogURL);
        
//        listModel.clear();
        utils.processDataset(dataset, false);

        historyList.add(dataset);
        if (historyList.size() > 1) {
            jbBack.setEnabled(true);
        }
        if (historyList.size() > 2) {
            if (historyList.get(historyList.size()-2) == null) {
                jbBack.setToolTipText("Back to catalog root");
            }
            else {
                jbBack.setToolTipText("Back to: "+historyList.get(historyList.size()-2));
            }
        }
        else {
            jbBack.setToolTipText("Back to catalog root");
        }
        
//        System.out.println("history list: "+historyList);

        historyTrailLabel.setText(historyTrailLabel.getText()+" / "+dataset.getName());
        handleProcess();
    }

    private synchronized void handleProcess() {
//        System.out.println("dir: "+utils.getDirectoryDatasetArray());
//        System.out.println("opendap: "+utils.getOpendapAccessArray());
//        System.out.println("http: "+utils.getHttpAccessArray());
        
        
        listModel.clear();

//        listModel = new DefaultListModel();
//        itemList.setModel(listModel);
        
        List<InvDataset> directoryInvDatasetList = utils.getDirectoryDatasetArray();
        List<InvAccess> opendapInvAccessOpendapList = utils.getOpendapAccessArray();
        List<InvAccess> httpInvAccessList = utils.getHttpAccessArray();

        listModel.setSize(directoryInvDatasetList.size()+opendapInvAccessOpendapList.size()+httpInvAccessList.size());
        
//        System.out.println("listModel size: "+listModel.size());
        
        
        statusLabel.setIcon(LOADING_ICON);
        for (int n=0; n<directoryInvDatasetList.size(); n++) {
            int index = n;
            listModel.set(index, new ThreddsEntry( directoryInvDatasetList.get(n).getName(), ThreddsEntryType.DIRECTORY ));
            statusLabel.setText("Loading... "+WCTUtils.DECFMT_00.format((index+1)*100.0/listModel.size())+"%");
        }
        for (int n=0; n<opendapInvAccessOpendapList.size(); n++) {
            int index = directoryInvDatasetList.size() + n;
//            String path = opendapInvAccessOpendapList.get(n).getUrlPath();
//            String name = path.substring(path.lastIndexOf("/")+1);
            String name = opendapInvAccessOpendapList.get(n).getDataset().getName();
            listModel.set(index, new ThreddsEntry(name, ThreddsEntryType.OPENDAP));
            statusLabel.setText("Loading... "+WCTUtils.DECFMT_00.format((index+1)*100.0/listModel.size())+"%");
        }
        for (int n=0; n<httpInvAccessList.size(); n++) {
            int index = directoryInvDatasetList.size() + opendapInvAccessOpendapList.size() + n;
//            System.out.println(n+" of "+httpInvAccessList.size());
//            String path = httpInvAccessList.get(n).getUrlPath();
//            String name = path.substring(path.lastIndexOf("/")+1);
            String name = httpInvAccessList.get(n).getDataset().getName();
//            double sizeInBytes = httpInvAccessList.get(n).getDataSize();
//            System.out.println(name+": "+sizeInBytes);
//            if (httpInvAccessList.get(n).hasDataSize()) {
//                name += " ( size unknown )";
//            }
//            else {
//                name += " ( "+WCTUtils.DECFMT_0D00.format( sizeInBytes/1024.0/1024.0 )+" MB )";
//            }
            listModel.set(index, new ThreddsEntry(name, ThreddsEntryType.HTTPSERVER));
            statusLabel.setText("Loading... "+WCTUtils.DECFMT_00.format((index+1)*100.0/listModel.size())+"%");
        }

        statusLabel.setText("");
        statusLabel.setIcon(null);
    }
    
    public void process(InvAccess access) {
//        System.out.println("ACCESS REQUESTED: "+access.getStandardUrlName());
//        this.selectedDataAccessURL = access.getStandardUrlName();
    }
    
    public URL[] getSelectedDataAccessURLArray() {
        return selectedDataAccessURLArray;
    }
    public int[] getSelectedIndices() {
        return selectedIndices;
    }

//    public void setFilters(FilterPipeline pipeline) {
//        itemList.setFilters(pipeline);
//    }
    
    public JList getItemList() {
        return this.itemList;
    }
    
    
    
    
    
    
    
    
    
    
    static enum ThreddsEntryType { DIRECTORY, OPENDAP, HTTPSERVER }
    class ThreddsEntry {
        private String name;
        private ThreddsEntryType type;
        
        public ThreddsEntry(String name, ThreddsEntryType type) {
            this.name = name;
            this.type = type;
        }
        public String getName() {
            return name;
        }
        public ThreddsEntryType getType() {
            return type;        
        }
        @Override
        public String toString() {
            return name;
        }
    }
    
    
    class ThreddsCellRenderer extends JLabel implements ListCellRenderer {

        private final Icon DIRECTORY_ICON = new ImageIcon(ThreddsPanel.class.getResource("/icons/gnome-fs-directory.png"));
        private final Icon OPENDAP_ICON = new ImageIcon(ThreddsPanel.class.getResource("/icons/gnome-mime-text-O.png"));
        private final Icon HTTPSERVER_ICON = new ImageIcon(ThreddsPanel.class.getResource("/icons/gnome-mime-text-H.png"));
        
        private final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);

        public ThreddsCellRenderer() {
          setOpaque(true);
          setIconTextGap(12);
        }

        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
    
          ThreddsEntry entry = (ThreddsEntry) value;
          if (entry == null) {
              return this;
          }
          
          setText(entry.getName());
          if (entry.getType() == ThreddsEntryType.DIRECTORY) {
              setIcon(DIRECTORY_ICON);
          }
          else if (entry.getType() == ThreddsEntryType.OPENDAP) {
              setIcon(OPENDAP_ICON);
          }
          else if (entry.getType() == ThreddsEntryType.HTTPSERVER) {
              setIcon(HTTPSERVER_ICON);
          }
          else {
              
          }
          
          
          if (isSelected) {
            setBackground(HIGHLIGHT_COLOR);
            setForeground(Color.white);
          } else {
            setBackground(Color.white);
            setForeground(Color.black);
          }
          return this;
        }
      }
    

    public static void main(String[] args) {
        try {

            ThreddsPanel panel = new ThreddsPanel();
              String catalogURI = "http://www.ncdc.noaa.gov/thredds/catalog.xml";
//            String catalogURI = "http://motherlode.ucar.edu:8080/thredds/catalog.xml";
//            String catalogURI = "http://nomads.ncdc.noaa.gov/thredds/catalog.xml";
            
                
                
            panel.process(catalogURI);

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCurrentCatalogURL() {
        return currentCatalogURL;
    }

    public String getCurrentRootCatalog() {
        return currentRootCatalog;
    }



}

