package gov.noaa.ncdc.wct.ui.filter;

import gov.noaa.ncdc.wct.WCTUtils;

import javax.swing.JPanel;

public abstract class AbstractAttributeFilter extends JPanel {

    protected double[] parseStringToValues(String str, double noDataValue) {
        try {
            String[] valArray = str.split(",");
            double[] values = new double[valArray.length];
            for (int n=0; n<valArray.length; n++) {
                values[n] = Double.parseDouble(valArray[n]);
            }
            return values;
        } catch (Exception e) {
            return new double[] { noDataValue };
        }
    }
    
    
    
    protected String parseValuesToString(double[] values, double noDataValue) {
        try {
            if (values.length == 1 && values[0] == noDataValue) {
                return "NONE";
            }
            
            String str = "";
            for (int n=0; n<values.length; n++) {
                if (Math.abs(values[n]) > 10000 || 
                        ( Math.abs(values[n]) > 0 && Math.abs(values[n]) < 0.01)) {
                    str += WCTUtils.DECFMT_SCI.format(values[n]);
                }
                else {
                    str += WCTUtils.DECFMT_0D00.format(values[n]);
                }
                if (n+1 < values.length) {
                    str += ", ";
                }
            }
            return str;
        } catch (Exception e) {
            return "NONE";
        }
    }
    

}
