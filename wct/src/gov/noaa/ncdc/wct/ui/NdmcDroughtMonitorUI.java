package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.BareBonesBrowserLaunch;
import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.nexradiv.LoadWmsAnimationThread;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.ui.animation.AnimationFrame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geotools.renderer.j2d.LegendPosition;
import org.geotools.renderer.j2d.RenderedLogo;

public class NdmcDroughtMonitorUI extends JDialog {

    private static final long serialVersionUID = -2193671445928942293L;
    
    public final static float Z_INDEX = 2+((float)1)/100 + 0.201f;
    public final static String LAYER_NAME = "U.S. Drought Monitor";
    public final static Color EMPTY_BACKGROUND_COLOR = new Color(255, 255, 255);
    
    private JList dateList;
    private WCTViewer viewer;
    private NdmcDroughtMonitor ndm = new NdmcDroughtMonitor();
    private HashMap<String, String> dateMap = new HashMap<String, String>();
    private AnimationFrame animationFrame = new AnimationFrame();
    
    private JButton displayButton, animateButton, clearButton;
    private final JComboBox transparency = new JComboBox(new Object[] {
            "  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
            " 60 %", " 70 %", " 80 %", " 90 %", "100 %"
    }); 
    private JCheckBox clearOnClose;
    
    private final static String OLD_TO_NEW = "Old to New";
    private final static String NEW_TO_OLD = "New to Old";    
    private JComboBox animationOrder = new JComboBox(new Object[] {
            OLD_TO_NEW, NEW_TO_OLD
    });
    private JLabel animationOrderLabel = new JLabel("     Order:");
    
    public NdmcDroughtMonitorUI(WCTViewer parent) {      
        super(parent, "U.S. Drought Monitor Browser", false);
        this.viewer = parent;
        
        
        ndm.setName(LAYER_NAME);
        ndm.setZIndex(Z_INDEX);
        
        createGUI();
        pack();
    }



    private void createGUI() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new RiverLayout());

        dateList = new JList();
        listDates();
        dateList.setSelectedIndex(0);
        dateList.addMouseListener(new ClickHandler());
        dateList.addListSelectionListener(new ListSelectionListener() {
//            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (dateList.getSelectedIndices().length > 1) {
                    displayButton.setEnabled(false);
                    clearButton.setEnabled(false);
                    clearOnClose.setEnabled(false);
                    animateButton.setEnabled(true);
                    animationOrderLabel.setEnabled(true);
                    animationOrder.setEnabled(true);
                }
                else {
                    displayButton.setEnabled(true);
                    clearButton.setEnabled(true);
                    clearOnClose.setEnabled(true);
                    animateButton.setEnabled(false);
                    animationOrderLabel.setEnabled(false);
                    animationOrder.setEnabled(false);
                }
            }
        });

        
        
        JScrollPane listScrollPane = new JScrollPane(dateList);
        listScrollPane.setBorder(WCTUiUtils.myTitledBorder("Select Date", 10));

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new RiverLayout());
        datePanel.add(listScrollPane, "hfill vfill");
        datePanel.add(new JLabel("Hold the 'Select' or 'Control' keys"), "br center");
        datePanel.add(new JLabel("to make multiple selections"), "br center");
        
        

        displayButton = new JButton("Display");
        displayButton.setPreferredSize(new Dimension(60, displayButton.getPreferredSize().height));
        displayButton.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    foxtrot.Worker.post(new foxtrot.Task() {
                        public Object run() throws Exception {
                            displayWmsBackground();
                            return "DONE";
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        animateButton = new JButton("Animate");
        animateButton.setPreferredSize(new Dimension(60, animateButton.getPreferredSize().height));
        animateButton.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    animateWms();
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        
        
        JButton aboutButton = new JButton("About");
        aboutButton.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                showInfo();
            }
        });
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                clearWmsBackground();
            }
        });
        clearOnClose = new JCheckBox("Clear on Close", true);
        
        JButton dataButton = new JButton("Raw Data (KML/SHP/Excel)");
        dataButton.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    BareBonesBrowserLaunch.openURL("http://www.drought.unl.edu/dm/dmshps_archive.htm");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(viewer, "No Default Browser found.\n"+
                            "Please direct you browser to \" http://www.drought.unl.edu/dm/dmshps_archive.htm \"", 
                            "BROWSER CONTROL ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
                }
            }
        });

        transparency.setEditable(true);
        transparency.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("itemStateChanged event");
                
                String value = transparency.getSelectedItem().toString().replaceAll("%", "").trim();
                int alpha = 255 - ((int)((Integer.parseInt(value)/100.0)*255));
                try {
                    viewer.setWMSTransparency(LAYER_NAME, alpha, EMPTY_BACKGROUND_COLOR);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        JButton backgroundMapButton = new JButton("Background Maps");
        backgroundMapButton.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                viewer.getMapSelector().setVisible(true);
                viewer.getMapSelector().setSelectedTab(2);
            }
        });
        

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new RiverLayout());
        
        buttonPanel.add(new JLabel("Transparency: "));
        buttonPanel.add(transparency);
        buttonPanel.add(backgroundMapButton);
        
        buttonPanel.add(displayButton, "br center");
        buttonPanel.add(clearButton);
        buttonPanel.add(clearOnClose);
        buttonPanel.add(animateButton, "br center");
        buttonPanel.add(animationOrderLabel);
        buttonPanel.add(animationOrder);
        buttonPanel.add(dataButton, "p center");
        buttonPanel.add(aboutButton);

        
        
        mainPanel.add(datePanel, "hfill vfill");
        mainPanel.add(buttonPanel, "br center");

        
        animateButton.setEnabled(false);
        animationOrderLabel.setEnabled(false);
        animationOrder.setEnabled(false);


        this.add(mainPanel);

        this.setSize(700, 600);

        

        
        
        
        
        
        this.addWindowListener(new WindowListener() {
//            @Override
            public void windowActivated(WindowEvent arg0) {
            }
//            @Override
            public void windowClosed(WindowEvent arg0) {
            }
//            @Override
            public void windowClosing(WindowEvent e) {
                if (clearOnClose.isSelected()) {
                    clearWmsBackground();
                }
            }
//            @Override
            public void windowDeactivated(WindowEvent arg0) {
            }
//            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }
//            @Override
            public void windowIconified(WindowEvent arg0) {
            }
//            @Override
            public void windowOpened(WindowEvent arg0) {
            }
        });
        
        
        JRootPane rootPane = this.getRootPane();
        InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

        ActionMap aMap = rootPane.getActionMap();
        aMap.put("escape", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
                if (clearOnClose.isSelected()) {
                    clearWmsBackground();
                }
            }
        });

    }
    
    public void displayWmsBackground() throws Exception {
                
        ndm.setYYMMDD(dateMap.get(dateList.getSelectedValue().toString()));
        ndm.setName(LAYER_NAME);
        ndm.setZIndex(Z_INDEX);
        ndm.setImageSize(viewer.getMapPane().getWCTZoomableBounds(new java.awt.Rectangle()));
        
        Rectangle2D.Double curExtent = viewer.getCurrentExtent();
        // set max extent to -180,180,-90,90
//        Rectangle2D.Double maxExtent = new Rectangle2D.Double(-180.0, -90.0, 360.0, 180.0);
//        Rectangle2D.Double extent = NDITUtils.extentIntersection(curExtent, maxExtent);
        
        URL wmsImageURL = ndm.getWmsUrl(curExtent);
        Image legendImage = ndm.getLegendImage();
        RenderedLogo legend = new RenderedLogo(legendImage);
        legend.setZOrder(400.2f);
        legend.setPosition(LegendPosition.NORTH_EAST);
        legend.setInsets(new Insets(legendImage.getHeight(this)-5, 0, 0, legendImage.getWidth(this)-5));

        RenderedLogo logo = new RenderedLogo(ndm.getResourceLogo());
        logo.setZOrder(500.2f);
        logo.setPosition(LegendPosition.SOUTH_EAST);
        logo.setInsets(new Insets(0, 0, 18, 50));

        
        String value = transparency.getSelectedItem().toString().replaceAll("%", "").trim();
        int alpha = 255 - ((int)((Integer.parseInt(value)/100.0)*255));
        viewer.displayWMS(ndm.getName(), wmsImageURL, curExtent, Z_INDEX, alpha, EMPTY_BACKGROUND_COLOR, legend, logo);
        if (! viewer.getMapPaneZoomChange().getWmsResources().contains(ndm)) {
            viewer.getMapPaneZoomChange().addWmsResource(ndm);
        }
    }
    
    public void animateWms() {
        
        if (viewer.getMapPaneZoomChange().getWmsResources().contains(ndm)) {
            viewer.getMapPaneZoomChange().removeWmsResource(ndm);
        }

        
        int[] selectedIndices = dateList.getSelectedIndices();
        NdmcDroughtMonitor[] wmsResources = new NdmcDroughtMonitor[selectedIndices.length];
        for (int n=0; n<selectedIndices.length; n++) {
            wmsResources[n] = new NdmcDroughtMonitor();
            wmsResources[n].setYYMMDD(dateMap.get(dateList.getModel().getElementAt(selectedIndices[n])));            
            wmsResources[n].setName(LAYER_NAME);
            wmsResources[n].setZIndex(Z_INDEX);
            wmsResources[n].setImageSize(viewer.getMapPane().getWCTZoomableBounds(new java.awt.Rectangle()));
        }
        if (animationOrder.getSelectedItem().equals(OLD_TO_NEW)) {
            WCTUtils.flipArray(wmsResources);
        }
        
        LoadWmsAnimationThread animationThread = new LoadWmsAnimationThread(
                viewer, 
                animationFrame,
                wmsResources, animateButton, true);
        
        String value = transparency.getSelectedItem().toString().replaceAll("%", "").trim();
        int alpha = 255 - ((int)((Integer.parseInt(value)/100.0)*255));
        animationThread.setAlpha(alpha);
        animationThread.setEmptyBackgroundColor(null);
        
        animationThread.start();
                
    }
    
    public void clearWmsBackground() {
        viewer.removeWMS(LAYER_NAME);
        viewer.getMapPaneZoomChange().removeWmsResource(ndm);
    }
    

    
    public void showInfo() {
//        JEditorPane editPane = new JEditorPane();
//        editPane.setContentType("text/plain");
//        editPane.setText(ndm.getInfo());
//        
//        JDialog info = new JDialog(this, true);
//        info.add(editPane);
//        info.setVisible(true);
        
        JOptionPane.showMessageDialog(viewer,
                ndm.getInfo(), 
                "About the Drought Monitor", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    

    private void listDates() {

        try {
            
            SimpleDateFormat sdfIn = new SimpleDateFormat("yyMMdd");
            SimpleDateFormat sdfOut = new SimpleDateFormat("EEE, MMM dd, yyyy");
            dateMap.clear();


            ArrayList<String> dates = ndm.getDates();
            DefaultListModel listModel = new DefaultListModel();
            for (int n=0; n<dates.size(); n++) {
                String yyMMdd = dates.get(dates.size()-n-1);
                String dateString = sdfOut.format(sdfIn.parse(yyMMdd));
                dateMap.put(dateString, yyMMdd);
                listModel.add(n, dateString);
            }
            dateList.setModel(listModel);

        } catch (Exception e) {
            e.printStackTrace();
            dateMap.clear();
            DefaultListModel listModel = new DefaultListModel();
            listModel.add(0, "Error Getting Dates");
            dateList.setModel(listModel);
        }
    }

    
    
    
    class ClickHandler implements MouseListener {

        public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2) {
                try {
                    displayWmsBackground();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    String message = e.getMessage();
                    javax.swing.JOptionPane.showMessageDialog(null, message, "WMS ERROR", JOptionPane.INFORMATION_MESSAGE);
                    e.printStackTrace();
                }

            }
        }

        public void mouseEntered(MouseEvent arg0) {
        }

        public void mouseExited(MouseEvent arg0) {
        }

        public void mousePressed(MouseEvent arg0) {
        }

        public void mouseReleased(MouseEvent arg0) {
        }

    }

}
