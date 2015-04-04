package gov.noaa.ncdc.wct.ui.filter;

import gov.noaa.ncdc.wct.WCTUtils;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SpatialFilterPanel extends JPanel {

    
    private JTextField extentNorth = new JTextField();
    private JTextField extentSouth = new JTextField();
    private JTextField extentEast = new JTextField();
    private JTextField extentWest = new JTextField();
    private JCheckBox jcbEngageSpatialFilter;
    private JCheckBox jcbLockSpatialFilterToViewer;
    private java.awt.geom.Rectangle2D.Double spatialFilterExtent;
    private java.awt.geom.Rectangle2D.Double manualSpatialFilterExtent;

    private double minLatLimit = -90;
    private double maxLatLimit = 90;
    private double minLonLimit = -180;
    private double maxLonLimit = 180;
    
    public SpatialFilterPanel() {
        createUI();
    }
    
    private void createUI() {
        
        JPanel extentHintPanel = new JPanel();
        extentHintPanel.setLayout(new GridLayout(3, 3));
        extentHintPanel.add(new JLabel("", JLabel.CENTER));
        extentHintPanel.add(new JLabel("North", JLabel.CENTER));
        extentHintPanel.add(new JLabel("", JLabel.CENTER));
        extentHintPanel.add(new JLabel("West", JLabel.CENTER));
        extentHintPanel.add(new JLabel("------ | ------", JLabel.CENTER));
        extentHintPanel.add(new JLabel("East", JLabel.CENTER));
        extentHintPanel.add(new JLabel("", JLabel.CENTER));
        extentHintPanel.add(new JLabel("South", JLabel.CENTER));
        extentHintPanel.add(new JLabel("", JLabel.CENTER));

        JPanel extentHintPanel2 = new JPanel();
        extentHintPanel2.add(extentHintPanel);

        JPanel extentPanel = new JPanel();
        extentPanel.setLayout(new GridLayout(3, 3));
        extentPanel.add(new JLabel("                    "));
        extentPanel.add(extentNorth);
        extentPanel.add(new JLabel("                    "));
        extentPanel.add(extentWest);
        extentPanel.add(new JLabel("                    "));
        extentPanel.add(extentEast);
        extentPanel.add(new JLabel("                    "));
        extentPanel.add(extentSouth);
        extentPanel.add(new JLabel("                    "));
        extentNorth.setEnabled(false);
        extentSouth.setEnabled(false);
        extentEast.setEnabled(false);
        extentWest.setEnabled(false);

        JPanel extentPanel2 = new JPanel();
        extentPanel2.add(extentPanel);

        jcbEngageSpatialFilter = new JCheckBox("Engage Spatial Filter");
        jcbEngageSpatialFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Object source = evt.getSource();
                if (source == jcbEngageSpatialFilter) {
                	jcbLockSpatialFilterToViewer.setEnabled(jcbEngageSpatialFilter.isSelected());
                	
                    if (jcbEngageSpatialFilter.isSelected() && ! jcbLockSpatialFilterToViewer.isSelected()) {
                        extentNorth.setEnabled(true);
                        extentSouth.setEnabled(true);
                        extentEast.setEnabled(true);
                        extentWest.setEnabled(true);
                    }
                    else {
                        extentNorth.setEnabled(false);
                        extentSouth.setEnabled(false);
                        extentEast.setEnabled(false);
                        extentWest.setEnabled(false);
                    }
                    
                }
            }
        });
        
        
        
        jcbLockSpatialFilterToViewer = new JCheckBox("Lock Spatial Filter to Viewer", true);
        jcbLockSpatialFilterToViewer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	setSpatialFilterLockedToViewer(jcbLockSpatialFilterToViewer.isSelected());
            }
        });
        JPanel spatialCheckBoxPanel = new JPanel();
        spatialCheckBoxPanel.setLayout(new BoxLayout(spatialCheckBoxPanel, BoxLayout.Y_AXIS));
        spatialCheckBoxPanel.add(jcbLockSpatialFilterToViewer);
        spatialCheckBoxPanel.add(jcbEngageSpatialFilter);


        this.setLayout(new BorderLayout());
        this.add(extentHintPanel2, "North");
        this.add(extentPanel2, "Center");
        this.add(spatialCheckBoxPanel, "South");
    }
    
    
    

    public boolean isSpatialFilterEngaged() {
        return jcbEngageSpatialFilter.isSelected();       
    }

    public void setSpatialFilterEngaged(boolean isEngaged) {
        jcbEngageSpatialFilter.setSelected(isEngaged);
    }

    public boolean isSpatialFilterLockedToViewer() {
        return jcbLockSpatialFilterToViewer.isSelected();       
    }

    public void setSpatialFilterLockedToViewer(boolean isLocked) {
        jcbLockSpatialFilterToViewer.setSelected(isLocked);
        
        extentNorth.setEnabled(! isLocked);
        extentSouth.setEnabled(! isLocked);
        extentEast.setEnabled(! isLocked);
        extentWest.setEnabled(! isLocked);
        
        if (isLocked && spatialFilterExtent != null) {
        	parseExtent(spatialFilterExtent);                	
        }
        else if (! isLocked && manualSpatialFilterExtent != null) {
        	parseExtent(manualSpatialFilterExtent);
        }
    }


    /**
     * Used to set the spatial extent from the map pane controller, 
     * when NOT manually entered.  
     * Use 'setManualSpatialFilterExtent' for the manual entry case.  
     * If 'isSpatialFilterLockedToViewer()' is true, then the min/max 
     * text boxes will be populated with the extent.
     * @param extent
     */
    public void setSpatialFilterExtent(java.awt.geom.Rectangle2D.Double extent) {
        this.spatialFilterExtent = extent;
        if (isSpatialFilterLockedToViewer()) {
        	parseExtent(extent);
        }
    }
    
    
    private java.awt.geom.Rectangle2D.Double parseManualExtent() {
    	
    	return new java.awt.geom.Rectangle2D.Double(
    			Double.parseDouble(extentWest.getText()),
    			Double.parseDouble(extentSouth.getText()),
    			Math.abs(Double.parseDouble(extentWest.getText())-
    					Double.parseDouble(extentEast.getText())),
    	    	Math.abs(Double.parseDouble(extentNorth.getText())-
    	    			Double.parseDouble(extentSouth.getText()))
    	    );
    }
    
    
    
    private void parseExtent(java.awt.geom.Rectangle2D.Double extent) {
        extentNorth.setText(WCTUtils.DECFMT_0D0000.format((extent.getMaxY() > maxLatLimit) ? maxLatLimit : extent.getMaxY()));
        extentSouth.setText(WCTUtils.DECFMT_0D0000.format((extent.getMinY() < minLatLimit) ? minLatLimit : extent.getMinY()));
        extentEast.setText(WCTUtils.DECFMT_0D0000.format((extent.getMaxX() > maxLonLimit) ? maxLonLimit : extent.getMaxX()));
        extentWest.setText(WCTUtils.DECFMT_0D0000.format((extent.getMinX() < minLonLimit) ? minLonLimit : extent.getMinX()));
    }

    public Rectangle2D.Double getSpatialExtent() {
    	if (! jcbLockSpatialFilterToViewer.isSelected()) {
    		return parseManualExtent();
    	}
    	else {
    		return this.spatialFilterExtent;
    	}
    }

    public void setMinLatLimit(double minLatLimit) {
        this.minLatLimit = minLatLimit;
    }

    public double getMinLatLimit() {
        return minLatLimit;
    }

    public void setMaxLatLimit(double maxLatLimit) {
        this.maxLatLimit = maxLatLimit;
    }

    public double getMaxLatLimit() {
        return maxLatLimit;
    }

    public void setMinLonLimit(double minLonLimit) {
        this.minLonLimit = minLonLimit;
    }

    public double getMinLonLimit() {
        return minLonLimit;
    }

    public void setMaxLonLimit(double maxLonLimit) {
        this.maxLonLimit = maxLonLimit;
    }

    public double getMaxLonLimit() {
        return maxLonLimit;
    }

    /**
     * Used to set the spatial extent manually entered.  
     * Use 'setSpatialFilterExtent' for the non-manual entry case.  
     * If 'isSpatialFilterLockedToViewer()' is false, then the min/max 
     * text boxes will be populated with the extent.
     * @param extent
     */

	public void setManualSpatialFilterExtent(java.awt.geom.Rectangle2D.Double manualSpatialFilterExtent) {
		this.manualSpatialFilterExtent = manualSpatialFilterExtent;
        if (! isSpatialFilterLockedToViewer()) {
        	parseExtent(manualSpatialFilterExtent);
        }
	}

	public java.awt.geom.Rectangle2D.Double getManualSpatialFilterExtent() {
		return manualSpatialFilterExtent;
	}

//    /**
//     * This represents the default extent shown when the 'lock to viewer' checkbox is de-selected. 
//     * @param maxSpatialFilterExtent
//     */
//	public void setMaxSpatialFilterExtent(java.awt.geom.Rectangle2D.Double maxSpatialFilterExtent) {
//		this.maxSpatialFilterExtent = maxSpatialFilterExtent;
//	}
//
//	public java.awt.geom.Rectangle2D.Double getMaxSpatialFilterExtent() {
//		return maxSpatialFilterExtent;
//	}
    
    
}
