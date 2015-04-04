package gov.noaa.ncdc.wct.ui.filter;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXHyperlink;

public class GridAttributeFilterPanel extends AbstractAttributeFilter {

    private JComboBox gridMinValueCombo, gridMaxValueCombo;
    private JXHyperlink gridMinValueEditLink, gridMaxValueEditLink;

    public GridAttributeFilterPanel() {
        createUI();
    }
    
    
    private void createUI() {
        this.setLayout(new RiverLayout());
        this.add(new JLabel("Grid Filter"), "center p");

        ActionListener editListener = new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == gridMinValueEditLink) {
                    gridMinValueCombo.setSelectedItem("");
                    gridMinValueCombo.grabFocus();
                }
                else if (e.getSource() == gridMaxValueEditLink) {
                    gridMaxValueCombo.setSelectedItem("");
                    gridMaxValueCombo.grabFocus();
                }
            }
        };
        
        gridMinValueCombo = new JComboBox(new Object[] { "NONE", "1.0", "2.0" });
        gridMinValueCombo.setEditable(true);        
        gridMinValueEditLink = new JXHyperlink();
        gridMinValueEditLink.setText("Edit");
        gridMinValueEditLink.addActionListener(editListener);
        
        gridMaxValueCombo = new JComboBox(new Object[] { "NONE", "4.0", "8.0" });
        gridMaxValueCombo.setEditable(true);
        gridMaxValueEditLink = new JXHyperlink();
        gridMaxValueEditLink.setText("Edit");
        gridMaxValueEditLink.addActionListener(editListener);
        
        this.add(new JLabel("Minimum Value"), "left p");
        this.add(gridMinValueCombo, "tab hfill");
        this.add(gridMinValueEditLink, "right");
        this.add(new JLabel("Maximum Value"), "left br");
        this.add(gridMaxValueCombo, "tab hfill");
        this.add(gridMaxValueEditLink, "right");

        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 200, 0));
    }
    

    public double[] getGridMinValues() {
        return parseStringToValues(gridMinValueCombo.getEditor().getItem().toString(), WCTFilter.NO_MIN_VALUE);
    }

    public double[] getGridMaxValues() {
        return parseStringToValues(gridMaxValueCombo.getEditor().getItem().toString(), WCTFilter.NO_MAX_VALUE);
    }    
    
    public void setGridMinValues(double[] values) {
        gridMinValueCombo.setSelectedItem(parseValuesToString(values, WCTFilter.NO_MIN_VALUE));
    }

    public void setGridMaxValues(double[] values) {
        gridMaxValueCombo.setSelectedItem(parseValuesToString(values, WCTFilter.NO_MAX_VALUE));
    }
    
    
}
