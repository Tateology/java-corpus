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

package gov.noaa.ncdc.wct.ui;



import gov.noaa.ncdc.wct.ui.event.LoadDataListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import ucar.nc2.dt.RadialDatasetSweep;

public class RadialProperties extends JDialog implements ActionListener {

    private static final long serialVersionUID = -7292474559033533121L;


    private RadialDatasetSweep radialDataset;

    private RadialPropertiesPanel radialPropsPanel;

    private JButton loadButton;
//    private JComboBox jcomboVariables;
//    private JRadioButton[] cutButtons;
    private JCheckBox jcbUseRF, jcbClassify;
//    private JPanel choicePanel = new JPanel();
//    private JPanel cutSubPanel = new JPanel();
//    private JPanel cutPanel = new JPanel();
    
//    private String currentVariableName;
//    private int currentCutIndex;


    private Vector<LoadDataListener> listeners = new Vector<LoadDataListener>();




    public RadialProperties(Frame parent, RadialDatasetSweep radialDataset) {      
        super(parent, "Radial Properties", false);
        this.radialDataset = radialDataset;
        createGUI();
        pack();
    }


    private void createGUI() {

        radialPropsPanel = new RadialPropertiesPanel(radialDataset);
        
//        JPanel momentPanel = new JPanel();
//        JPanel momentSubPanel = new JPanel();
//        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
//        momentPanel.setLayout(new BorderLayout());
//        cutPanel.setLayout(new BorderLayout());
//        momentSubPanel.setLayout(new BoxLayout(momentSubPanel, BoxLayout.Y_AXIS));
//        momentPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
//        JPanel rfPanel = new JPanel();
//        rfPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
//        rfPanel.setLayout(new BoxLayout(rfPanel, BoxLayout.Y_AXIS));
//
//
//        jcbUseRF = new JCheckBox("Show RF Values", false);
//        jcbUseRF.setEnabled(false);
        jcbClassify = new JCheckBox("Classify Data", false);
//
//        // Create unique cut buttons
//        cutSubPanel = this.makeCutButtons();
//
//        momentPanel.add(new JLabel("Moment:", JLabel.CENTER), BorderLayout.NORTH);
//        momentPanel.add(makeVariablesPanel(), BorderLayout.CENTER);
//
//        cutPanel.add(new JLabel("Cut (Elev. Angle):", JLabel.CENTER), BorderLayout.NORTH);
//        cutPanel.add(cutSubPanel, BorderLayout.CENTER);
//        cutPanel.add(rfPanel, BorderLayout.SOUTH);
//
//        choicePanel.add(momentPanel);
//        choicePanel.add(cutPanel);

        loadButton = new JButton("Load");
        loadButton.addActionListener(this);
        loadButton.setPreferredSize(new Dimension(100, (int)loadButton.getPreferredSize().getHeight()));
        this.getRootPane().setDefaultButton(loadButton);
        JPanel loadPanel = new JPanel();
        loadPanel.add(loadButton);

        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel("Radial Properties", JLabel.CENTER), BorderLayout.NORTH);
//        getContentPane().add(new JScrollPane(choicePanel), BorderLayout.CENTER);
        getContentPane().add(new JScrollPane(radialPropsPanel), BorderLayout.CENTER);
        getContentPane().add(loadPanel, BorderLayout.SOUTH);

        
        
        
        
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


    public boolean getUseRFvalues() {
//        return jcbUseRF.isSelected();
        return true;
    }

    public boolean getClassify() {
//        return jcbClassify.isSelected();
        return false;
    }


    public void setLoadButtonEnabled(boolean enabled) {
        loadButton.setEnabled(enabled);
    }


    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (source == loadButton) {
            
//            this.currentVariableName = jcomboVariables.getSelectedItem().toString();
//            for (int i=0; i<cutButtons.length; i++) {
//                if (cutButtons[i] != null) {
//                    if (cutButtons[i].isSelected()) {
//                        this.currentCutIndex = i;
//                    }
//                }         
//            }
//            
//            
            
            for (int n=0; n<listeners.size(); n++) {
                listeners.get(n).loadData();
            }
        }
    }





    public void addLoadDataListener(LoadDataListener listener) {
        listeners.add(listener);
    }
    public void removeLoadDataListener(LoadDataListener listener) {
        listeners.remove(listener);
    }


    

    public RadialPropertiesPanel getRadialPropsPanel() {
        return radialPropsPanel;
    }


}
