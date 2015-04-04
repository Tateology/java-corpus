package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.nexradiv.MapSelector;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class RasterExportOptionsPanel extends JPanel {

    private JComboBox gridCellSizeCombo = new JComboBox(new Object[]{"Auto", "0.0075", "0.0100", "0.0125", "0.0150", "0.0175", "0.0200", "0.0250"});
    private JComboBox gridSizeCombo = new JComboBox(new Object[]{"Auto", "400", "600", "800", "1000", "1200", "1400", "1600"});
    private JTextField noDataTextField = new JTextField("-999");
    private JComboBox noDataFormatCombo = new JComboBox(new Object[]{"0.0000000", "0.00000", "0.000", "#.#######", "#.#####", "#.###"});
    private JSpinner smoothFactorSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));

    private boolean showSmoothingOptions = true;
    private boolean showNoDataOptions = true;
    private boolean showNoDataFormatOptions = true;
    
    public static final double AUTO_GRID_CELL_SIZE_VALUE = Double.MIN_VALUE;
    public static final int AUTO_GRID_DIMENSION_VALUE = Integer.MIN_VALUE;
    
    public RasterExportOptionsPanel() {
        init();
    }
    
    private void refresh() {
        this.removeAll();
        this.init();
    }
    
    private void init() {
        this.setLayout(new RiverLayout());
        
        gridCellSizeCombo.setEditable(true);
        gridSizeCombo.setEditable(true);
        noDataFormatCombo.setEditable(true);
        
        gridSizeCombo.setSelectedItem("800");

        
        
        
        
        
        
        final String formatText = "View format documentation in browser";
        
//        final Component finalThis = this;
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                JOptionPane.showMessageDialog(finalThis, formatText, "Format Info", JOptionPane.INFORMATION_MESSAGE);
            	try {
					Desktop.getDesktop().browse(new URL("http://download.oracle.com/javase/6/docs/api/java/text/DecimalFormat.html").toURI());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
            }
        };
        JLabel infoLabel = new JLabel(new ImageIcon(MapSelector.class.getResource("/icons/question-mark.png")));
        infoLabel.setToolTipText(formatText);
        infoLabel.addMouseListener(mouseListener);

        
        
        
        
        
        
        this.add(new JLabel("Grid Cell Size: "), "p");
        this.add(gridCellSizeCombo, "tab");
        this.add(new JLabel("Grid Dimension Size: "), "br");
        this.add(gridSizeCombo, "tab");
        if (isShowNoDataOptions()) {
        	this.add(new JLabel("No Data Value: "), "br");
        	this.add(noDataTextField, "tab hfill");
        	if (isShowNoDataFormatOptions()) {
        		this.add(new JLabel("No Data Format:"), "br");
        		this.add(noDataFormatCombo, "tab hfill");
        		this.add(infoLabel);
        	}
        }
        if (isShowSmoothingOptions()) {
            this.add(new JLabel("Smoothing Factor: "), "br");
            this.add(smoothFactorSpinner, "tab");
        }
        
        
        
        
        
        
    }

    public void setShowSmoothingOptions(boolean showSmoothingOptions) {
        this.showSmoothingOptions = showSmoothingOptions;
        refresh();
    }

    public boolean isShowSmoothingOptions() {
        return showSmoothingOptions;
    }
    
    
    public void setShowNoDataOptions(boolean showNoDataOptions) {
		this.showNoDataOptions = showNoDataOptions;
        refresh();
	}

	public boolean isShowNoDataOptions() {
		return showNoDataOptions;
	}

	public void setShowNoDataFormatOptions(boolean showNoDataFormatOptions) {
		this.showNoDataFormatOptions = showNoDataFormatOptions;
        refresh();
	}

	public boolean isShowNoDataFormatOptions() {
		return showNoDataFormatOptions;
	}

	public double getGridCellSizeValue() {
        String s = gridCellSizeCombo.getSelectedItem().toString();
        if (s.equalsIgnoreCase("AUTO")) {
            return AUTO_GRID_CELL_SIZE_VALUE;
        }
        else {
            try {
                return Double.parseDouble(s);
            } catch (Exception e) {
                gridCellSizeCombo.setSelectedItem("Auto");
                return AUTO_GRID_CELL_SIZE_VALUE;
            }
            
        }
    }
    
    
    public int getGridDimension() {
        String s = gridSizeCombo.getSelectedItem().toString();
        if (s.equalsIgnoreCase("AUTO")) {
            return AUTO_GRID_DIMENSION_VALUE;
        }
        else {
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                gridSizeCombo.setSelectedItem("Auto");
                return AUTO_GRID_DIMENSION_VALUE;
            }
            
        }
    }
    
    
    public float getNoDataValue() {
        try {
            return Float.parseFloat(noDataTextField.getText());
        } catch (Exception e) {
            noDataTextField.setText("-999");
            return -999;
        }
    }
    
	public DecimalFormat getNoDataFormat() {
		return new DecimalFormat(noDataFormatCombo.getSelectedItem().toString());
	}

	public int getSmoothingFactor() {
        return Integer.parseInt(smoothFactorSpinner.getValue().toString());
    }

    public void updateFromSpatialExtent(Rectangle2D.Double spatialExtent) {

        
    }
}
