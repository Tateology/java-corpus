package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.geotools.map.MapLayer;

public class NhcTracksUI extends JDialog {

    private WCTViewer viewer;
    private NhcTracks nhc = new NhcTracks();

    public NhcTracksUI(WCTViewer parent) {      
        super(parent, "NOAA's National Hurricane Center - Current Hurricanes", false);
        this.viewer = parent;
        
        createGUI();
        pack();
    }



    private void createGUI() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new RiverLayout());

        JButton jbDisplayLatest = new JButton("Display Current Hurricanes");
        jbDisplayLatest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ArrayList<URL> urlList = nhc.getCachedShapefileLatestUrlList();
                    MapLayer trackLineLayer = nhc.getTrackLineLayer(urlList);
                    MapLayer trackPolyLayer = nhc.getTrackPolygonLayer(urlList);
                    MapLayer coastalWarnLayer = nhc.getCoastalWarningLayer(urlList);
                    MapLayer trackPointLayer = nhc.getTrackPointLayer(urlList);
                    
                    viewer.getMapContext().addLayer(trackPolyLayer);
                    viewer.getMapContext().addLayer(trackLineLayer);
                    viewer.getMapContext().addLayer(coastalWarnLayer);
                    viewer.getMapContext().addLayer(trackPointLayer);
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(viewer, "Error adding latest hurricane tracks from NOAA NHC", 
                            "NHC Error", javax.swing.JOptionPane.ERROR_MESSAGE);            
                    
                }
            }            
        });
        
        
        
        mainPanel.add(new JLabel("EXPERIMENTAL: DO NOT USE - FOR TESTING ONLY!"), "br");
        mainPanel.add(jbDisplayLatest);
        
        this.add(mainPanel);
        
        
        
        JRootPane rootPane = this.getRootPane();
        InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

        ActionMap aMap = rootPane.getActionMap();
        aMap.put("escape", new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    dispose();
                }
            });

    }
}
