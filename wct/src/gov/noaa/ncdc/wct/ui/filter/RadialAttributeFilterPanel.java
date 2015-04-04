package gov.noaa.ncdc.wct.ui.filter;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTFilter;

import javax.swing.JComboBox;
import javax.swing.JLabel;

public class RadialAttributeFilterPanel extends AbstractAttributeFilter {

    private JComboBox radialMinDistCombo, radialMaxDistCombo;
    private JComboBox radialMinValueCombo, radialMaxValueCombo;
    private JComboBox radialMinAzimuthCombo, radialMaxAzimuthCombo;
    private JComboBox radialMinHeightCombo, radialMaxHeightCombo;
    private JComboBox radialDistUnitsCombo, radialValueUnitsCombo, radialheightUnitsCombo;

    public RadialAttributeFilterPanel() {
        createUI();
    }
    
    private void createUI() {
        this.setLayout(new RiverLayout());
        this.add(new JLabel("Radial Filter"), "center p");
        radialMinDistCombo = new JComboBox(new Object[] { "0.0", "20.0", "50.0" });
        radialMinDistCombo.setEditable(true);
        radialMaxDistCombo = new JComboBox(new Object[] { "NONE", "230.0", "150.0" });
        radialMaxDistCombo.setEditable(true);
        radialMinValueCombo = new JComboBox(new Object[] { "NONE", "20.0", "40.0" });
        radialMinValueCombo.setEditable(true);
        radialMaxValueCombo = new JComboBox(new Object[] { "NONE", "40.0", "20.0" });
        radialMaxValueCombo.setEditable(true);
        radialMinAzimuthCombo = new JComboBox(new Object[] { "NONE", "0.0", "90.0" });
        radialMinAzimuthCombo.setEditable(true);
        radialMaxAzimuthCombo = new JComboBox(new Object[] { "NONE", "270.0", "360.0" });
        radialMaxAzimuthCombo.setEditable(true);
        radialMinHeightCombo = new JComboBox(new Object[] { "0.0", "4000", "8000" });
        radialMinHeightCombo.setEditable(true);
        radialMaxHeightCombo = new JComboBox(new Object[] { "NONE", "20000", "10000" });
        radialMaxHeightCombo.setEditable(true);
        radialDistUnitsCombo = new JComboBox(new Object[] { "Kilometers", "Miles", "Nautical Mi." });
        radialheightUnitsCombo = new JComboBox(new Object[] { "Meters", "Kilometers", "Miles", "Feet" });
        radialValueUnitsCombo = new JComboBox(new Object[] { "dBZ" });

        this.add(new JLabel("Minimum Distance"), "left p");
        this.add(radialMinDistCombo, "tab hfill");
        this.add(new JLabel("Maximum Distance"), "br");
        this.add(radialMaxDistCombo, "tab hfill");
        this.add(new JLabel("Distance Units"), "br");
        this.add(radialDistUnitsCombo, "tab hfill");
        this.add(new JLabel("Minimum Value"), "br");
        this.add(radialMinValueCombo, "tab hfill");
        this.add(new JLabel("Maximum Value"), "br");
        this.add(radialMaxValueCombo, "tab hfill");
//        this.add(new JLabel("Value Units"), "br");
//        this.add(radialValueUnitsCombo, "tab");      
        this.add(new JLabel("Minimum Azimuth"), "br");
        this.add(radialMinAzimuthCombo, "tab hfill");
        this.add(new JLabel("Maximum Azimuth"), "br");
        this.add(radialMaxAzimuthCombo, "tab hfill");
        this.add(new JLabel("Minimum Height"), "br");
        this.add(radialMinHeightCombo, "tab hfill");
        this.add(new JLabel("Maximum Height"), "br");
        this.add(radialMaxHeightCombo, "tab hfill");
        this.add(new JLabel("Height Units"), "br");
        this.add(radialheightUnitsCombo, "tab hfill");

    }
    
    
    public void setRadialMinValueChoices(Object[] choices) {
        radialMinValueCombo.removeAllItems();
        for (int i=0; i<choices.length; i++) {
            radialMinValueCombo.addItem(choices[i]);
        }      
    }

    public void setRadialMaxValueChoices(Object[] choices) {
        radialMaxValueCombo.removeAllItems();
        for (int i=0; i<choices.length; i++) {
            radialMaxValueCombo.addItem(choices[i]);
        }      
    }

    public void setRadialValueUnits(Object valueUnits) {
        radialValueUnitsCombo.removeAllItems();
        radialValueUnitsCombo.addItem(valueUnits);
    }

    public double getRadialMinDistance() {
        try {
            return convertDistUnits(Double.parseDouble(radialMinDistCombo.getEditor().getItem().toString()));
        } catch (Exception e) {
            return (WCTFilter.NO_MIN_DISTANCE);
        }

    }

    public double getRadialMaxDistance() {
        try {
            return convertDistUnits(Double.parseDouble(radialMaxDistCombo.getEditor().getItem().toString()));
        } catch (Exception e) {
            return (WCTFilter.NO_MAX_DISTANCE);
        }

    }

    
    public void setRadialDistanceRange(double minValue, double maxValue) {
        radialMinDistCombo.setSelectedItem(parseValuesToString(new double[] {minValue}, WCTFilter.NO_MIN_VALUE));
        radialMaxDistCombo.setSelectedItem(parseValuesToString(new double[] {maxValue}, WCTFilter.NO_MIN_VALUE));
    }
    
    
    
    public double[] getRadialMinValues() {
        return parseStringToValues(radialMinValueCombo.getEditor().getItem().toString(), WCTFilter.NO_MIN_VALUE);
    }

    public double[] getRadialMaxValues() {
        return parseStringToValues(radialMaxValueCombo.getEditor().getItem().toString(), WCTFilter.NO_MAX_VALUE);
    }

    public void setRadialMinValues(double[] values) {
        radialMinValueCombo.setSelectedItem(parseValuesToString(values, WCTFilter.NO_MIN_VALUE));
    }

    public void setRadialMaxValues(double[] values) {
        radialMaxValueCombo.setSelectedItem(parseValuesToString(values, WCTFilter.NO_MAX_VALUE));
    }



    public double getRadialMinAzimuth() {
        try {
            return (Double.parseDouble(radialMinAzimuthCombo.getEditor().getItem().toString()));
        } catch (Exception e) {
            return (WCTFilter.NO_MIN_AZIMUTH);
        }

    }

    public double getRadialMaxAzimuth() {
        try {
            return (Double.parseDouble(radialMaxAzimuthCombo.getEditor().getItem().toString()));
        } catch (Exception e) {
            return (WCTFilter.NO_MAX_AZIMUTH);
        }

    }
    
    public void setRadialAzimuthRange(double minValue, double maxValue) {
        radialMinAzimuthCombo.setSelectedItem(parseValuesToString(new double[] {minValue}, WCTFilter.NO_MIN_VALUE));
        radialMaxAzimuthCombo.setSelectedItem(parseValuesToString(new double[] {maxValue}, WCTFilter.NO_MIN_VALUE));
    }

    public double getRadialMinHeight() {
        try {
            return (convertHeightUnits(Double.parseDouble(radialMinHeightCombo.getEditor().getItem().toString())));
        } catch (Exception e) {
            return (WCTFilter.NO_MIN_HEIGHT);
        }

    }

    public double getRadialMaxHeight() {
        try {
            return (convertHeightUnits(Double.parseDouble(radialMaxHeightCombo.getEditor().getItem().toString())));
        } catch (Exception e) {
            return (WCTFilter.NO_MAX_HEIGHT);
        }

    }
    
    
    public void setRadialHeightRange(double minValue, double maxValue) {
        radialMinHeightCombo.setSelectedItem(parseValuesToString(new double[] {minValue}, WCTFilter.NO_MIN_VALUE));
        radialMaxHeightCombo.setSelectedItem(parseValuesToString(new double[] {maxValue}, WCTFilter.NO_MIN_VALUE));
    }

    // input of distance in km -- convert based on value in JComboBox
    private double convertDistUnits(double inputDistance) {
        if (((String)radialDistUnitsCombo.getSelectedItem()).equals("Kilometers")) {
            return inputDistance;
        }
        else if (((String)radialDistUnitsCombo.getSelectedItem()).equals("Miles")) {
            return inputDistance*1.60924;
        }
        else if (((String)radialDistUnitsCombo.getSelectedItem()).equals("Nautical Mi.")) {
            return inputDistance*1.85318;
        }
        else {
            return inputDistance;
        }
    }

    // input of distance in km -- convert based on value in JComboBox
    private double convertHeightUnits(double inputHeight) {
        if (((String)radialheightUnitsCombo.getSelectedItem()).equals("Meters")) {
            return inputHeight;
        }
        else if (((String)radialheightUnitsCombo.getSelectedItem()).equals("Kilometers")) {
            return inputHeight*1000.0;
        }
        else if (((String)radialheightUnitsCombo.getSelectedItem()).equals("Miles")) {
            return inputHeight*1609.344;
        }
        else if (((String)radialheightUnitsCombo.getSelectedItem()).equals("Feet")) {
            return inputHeight*0.3048;
        }
        else {
            return inputHeight;
        }
    }

    
    
}
