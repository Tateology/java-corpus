package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wsdemo.CDOExtract;
import gov.noaa.ncdc.wsdemo.CDOExtract.Station;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CdoServicesUI extends JDialog {

    
    private JList siteList;
    
    public CdoServicesUI(Frame parent) {      
        super(parent, "Climate Data Online: Data Browser", false);
        createGUI();
        pack();
    }
    
    
    
    private void createGUI() {
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new RiverLayout());
        
        siteList = new JList();
        JScrollPane listScrollPane = new JScrollPane(siteList);
        
        JButton listFilesButton = new JButton("List Sites");
        listFilesButton.addActionListener(new ActionListener() {
//            @Override
            public void actionPerformed(ActionEvent e) {
                listSites();
            }
        });

        
        
        mainPanel.add(listScrollPane, "hfill vfill");
        mainPanel.add(listFilesButton, "br");
        
        this.add(mainPanel);
        
        this.setSize(700, 600);
        

    }
    
    
    private void listSites() {
        
        CDOExtract cdo = new CDOExtract();
        try {
            ArrayList<Station> sites = cdo.getSitesFromWFS();
            DefaultListModel listModel = new DefaultListModel();
            for (int n=0; n<sites.size(); n++) {
                listModel.add(n, sites.get(n));
            }
            siteList.setModel(listModel);
            
        } catch (Exception e) {
//            siteListCombo = 
        }
    }
    
}
