package gov.noaa.ncdc.wct.ui.filter;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTFilter;

import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class Level3AttributeFilterPanel extends AbstractAttributeFilter {

    private JComboBox l3MinDistCombo, l3MaxDistCombo;
    private JComboBox l3MinValueCombo, l3MaxValueCombo;
    private JComboBox l3distUnitsCombo;
    private JComboBox l3CategoryOverridesCombo;

    public Level3AttributeFilterPanel() {
        createUI();
    }

    private void createUI() {
    
        this.setLayout(new RiverLayout());
        l3MinDistCombo = new JComboBox(new Object[] { "0.0", "20.0", "50.0" });
        l3MinDistCombo.setEditable(true);
        l3MaxDistCombo = new JComboBox(new Object[] { "NONE", "230.0", "150.0" });
        l3MaxDistCombo.setEditable(true);
        l3MinValueCombo = new JComboBox(new Object[] { "NONE", "20.0", "40.0" });
        l3MinValueCombo.setEditable(true);
        l3MaxValueCombo = new JComboBox(new Object[] { "NONE", "40.0", "20.0" });
        l3MaxValueCombo.setEditable(true);
        l3CategoryOverridesCombo = new JComboBox(new Object[] { "RF" });
        l3CategoryOverridesCombo.setEditable(true);


        l3distUnitsCombo = new JComboBox(new Object[] { "Kilometers", "Miles", "Nautical Mi." });
//        JPanel l3choicePanel = new JPanel();
//        l3choicePanel.setLayout(new GridLayout(4,2));
//
//        l3choicePanel.add(new JLabel("Minimum Value", JLabel.CENTER));
//        l3choicePanel.add(l3MinValueCombo);
//        l3choicePanel.add(new JLabel("Maximum Value", JLabel.CENTER));
//        l3choicePanel.add(l3MaxValueCombo);
//        l3choicePanel.add(new JLabel("Category Overrides", JLabel.CENTER));
//        l3choicePanel.add(l3CategoryOverridesCombo);
//
//        l3choicePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 95, 0));

        this.add(new JLabel("Level-III Filter", JLabel.CENTER), "center p");
//        this.add(l3choicePanel, "br");
        this.add(new JLabel("Minimum Value"), "left p");
        this.add(l3MinValueCombo, "tab hfill");
        this.add(new JLabel("Maximum Value"), "br");
        this.add(l3MaxValueCombo, "tab hfill");
        this.add(new JLabel("Category Overrides"), "br");
        this.add(l3CategoryOverridesCombo, "tab hfill");

        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 95, 0));

        

    }
    
    
    
    
    
    
    public void setLevel3MinValueChoices(Object[] choices) {
        l3MinValueCombo.removeAllItems();
        for (int i=0; i<choices.length; i++) {
            l3MinValueCombo.addItem(choices[i]);
        }      
    }

    public void setLevel3MaxValueChoices(Object[] choices) {
        l3MaxValueCombo.removeAllItems();
        for (int i=0; i<choices.length; i++) {
            l3MaxValueCombo.addItem(choices[i]);
        }      
    }




    public double getLevel3MinDistance() {
        try {
            return convertDistUnits(Double.parseDouble(l3MinDistCombo.getEditor().getItem().toString()));
        } catch (Exception e) {
            return (WCTFilter.NO_MIN_DISTANCE);
        }

    }

    public double getLevel3MaxDistance() {
        try {
            return convertDistUnits(Double.parseDouble(l3MaxDistCombo.getEditor().getItem().toString()));
        } catch (Exception e) {
            return (WCTFilter.NO_MAX_DISTANCE);
        }

    }

    public double[] getLevel3MinValues() {
        return parseStringToValues(l3MinValueCombo.getEditor().getItem().toString(), WCTFilter.NO_MIN_VALUE);
    }

    public double[] getLevel3MaxValues() {
        return parseStringToValues(l3MaxValueCombo.getEditor().getItem().toString(), WCTFilter.NO_MAX_VALUE);
    }
    
    public void setLevel3MinValues(double[] values) {
        l3MinValueCombo.setSelectedItem(parseValuesToString(values, WCTFilter.NO_MIN_VALUE));
    }

    public void setLevel3MaxValues(double[] values) {
        l3MaxValueCombo.setSelectedItem(parseValuesToString(values, WCTFilter.NO_MAX_VALUE));
    }


    public void setCategoryOverrides(String[] catOverrides) {
    	String s = Arrays.toString(catOverrides).replaceAll("\\[","").replaceAll("\\]","");
    	l3CategoryOverridesCombo.getEditor().setItem(s);
    }

    public String[] getCategoryOverrides() {
        String str = l3CategoryOverridesCombo.getEditor().getItem().toString();
        return str.split(",");
    }


    public void setLevel3DistanceFilterEnabled(boolean enabled) {
        l3MinDistCombo.setEnabled(enabled);
        l3MaxDistCombo.setEnabled(enabled);
        l3distUnitsCombo.setEnabled(enabled);
    }

    
    // input of distance in km -- convert based on value in JComboBox
    private double convertDistUnits(double inputDistance) {
        if (((String)l3distUnitsCombo.getSelectedItem()).equals("Kilometers")) {
            return inputDistance;
        }
        else if (((String)l3distUnitsCombo.getSelectedItem()).equals("Miles")) {
            return inputDistance*1.60924;
        }
        else if (((String)l3distUnitsCombo.getSelectedItem()).equals("Nautical Mi.")) {
            return inputDistance*1.85318;
        }
        else {
            return inputDistance;
        }
    }



}
