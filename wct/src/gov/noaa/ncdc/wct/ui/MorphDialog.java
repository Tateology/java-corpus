package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.morph.WCTMorphOperation.MorphVectorSource;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.jdesktop.swingx.JXDialog;

public class MorphDialog extends JXDialog {

	private WCTViewer viewer;
	
	public static final String SOURCE_ITEM_SWDI = "SWDI (NEXRAD tracked storm cells)";
	public static final String SOURCE_ITEM_SWDI_TVS = "SWDI (NEXRAD tracked tvs)";
	public static final String SOURCE_ITEM_RUC = "RUC (Storm motion u/v)";
	public static final String SOURCE_ITEM_MARKERS = "Markers (manual input, see docs...)";
	public static final String SOURCE_ITEM_NONE = "None (Simple fade in/out)";
	
	private JCheckBox jcbEngageMorphing = new JCheckBox("");
	private JSpinner jspinnerNumSteps = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
	private JComboBox<String> jcomboVectorSource = new JComboBox<String>(new String[] { 
			SOURCE_ITEM_SWDI, SOURCE_ITEM_SWDI_TVS, SOURCE_ITEM_RUC, SOURCE_ITEM_MARKERS, SOURCE_ITEM_NONE });
	private JSpinner jspinnerDateBuffer = new JSpinner(new SpinnerNumberModel(100, 1, 500, 1));
	private JSpinner jspinnerNumGridCells = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
	private JCheckBox jcbShowMotionVector = new JCheckBox("");

	private static MorphDialog singleton = null;

	private MorphDialog(WCTViewer viewer) {
		this(viewer, viewer);
	}
	
	private MorphDialog(WCTViewer viewer, Component parent) {
		super(viewer, new JPanel());
		
		this.viewer = viewer;
		
        setModal(true);
        setTitle("Time Morphing Tool (BETA)");
        
        createGUI();
        
//        pack();
//        setLocation(viewer.getX()+45, viewer.getY()+45);
//        setLocationRelativeTo(parent);
//        setVisible(true);
	}
	
	public static MorphDialog getInstance(WCTViewer viewer) {
		return getInstance(viewer, viewer);
	}
	
	public static MorphDialog getInstance(WCTViewer viewer, Component parent) {
		if (singleton == null) {
			singleton = new MorphDialog(viewer, parent);
		}
        singleton.pack();
        singleton.setLocationRelativeTo(parent);
		return singleton;
	}
	
	
	
	
	private void createGUI() {
		

//		progressBar.setStringPainted(true);
//		progressBar.setValue(0);
//		progressBar.setString("Processing Progress");
		
		

		
		
		
		
        String dir = WCTProperties.getWCTProperty("kmzsavedir");
        if (dir == null) {
            dir = "";
        }
        else {
            dir = dir + File.separator + "export.kmz"; 
        }

        
        
        
        
        
        
        try {
        
        
        JPanel mainPanel = new JPanel(new RiverLayout());
//        mainPanel.setBorder(WCTUiUtils.myTitledTopBorder("Isosurface Tool (for Grid and Radial data)", 5, 5, 5, 5, TitledBorder.CENTER, TitledBorder.TOP));
    

//        JButton goButton = new JButton("Start");
//        goButton.setActionCommand("SUBMIT");
//        goButton.setPreferredSize(new Dimension(60, (int)goButton.getPreferredSize().getHeight()));
//        goButton.addActionListener(new SubmitListener(this));
        JButton doneButton = new JButton("Done");
        doneButton.setActionCommand("DONE");
        doneButton.setPreferredSize(new Dimension(60, (int)doneButton.getPreferredSize().getHeight()));
        doneButton.addActionListener(new SubmitListener(this));

//        if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GRIDDED) {        	
//        	mainPanel.add("p hfill", gridOptionsPanel);        
//        }
//        else if (viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
//        	mainPanel.add("p hfill", radialOptionsPanel);   
//        }
//        else {
//        	mainPanel.add("p hfill", new JLabel("This data type is not supported.  "));
//        	cancelButton.setText("Close");
//        	goButton.setEnabled(false);
//        }
        
        jcbEngageMorphing.setSelected(false);
		jspinnerNumSteps.setEnabled(false);
		jcomboVectorSource.setEnabled(false);
		jspinnerDateBuffer.setEnabled(false);
		jspinnerNumGridCells.setEnabled(false);
        jcbShowMotionVector.setSelected(false);

        jcbEngageMorphing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				jspinnerNumSteps.setEnabled(jcbEngageMorphing.isSelected());
				jcomboVectorSource.setEnabled(jcbEngageMorphing.isSelected());
				jspinnerDateBuffer.setEnabled(jcbEngageMorphing.isSelected());
				jspinnerNumGridCells.setEnabled(jcbEngageMorphing.isSelected());
		        jcbShowMotionVector.setSelected(jcbEngageMorphing.isSelected());
			}
        });


        String disclaimer = "<html><b>EXPERIMENTAL TOOL: Please use with caution! </b><br>" +
        		"This tool interpolates data between two timesteps.  The interpolation can " +
        		"be either a simple weighted average (fade in/out effect) or motion vectors " +
        		"can be used to add movement to the interpolation.  The motion vectors can " +
        		"be derived from the NEXRAD Level-III Storm Structure product available in " +
        		"the NOAA/NCDC Severe Weather Data Inventory, or the Storm Motion variables " +
        		"in the RUC Model available from the NOAA/NCDC NOMADS THREDDS Server. <br><br>" +
        		"Instructions:  Enable time morphing below and then go through the normal " +
        		"process of animating data (select multiple files and press 'Animate').  A " +
        		"new checkbox will now be visible allowing you to enable time morphing for " +
        		"the animation.  Interpolated time steps will have an asterisk ('*') " +
        		"next to the time in the legend. <br><br>" +
        		"Disclaimer:  This tool is currently designed for Radar and file-based Gridded " +
        		"or Satellite data.  While the tool may work with file-based (not dimension-based) " +
        		"animations of non-Radar data, it is up to the user to know when it is applicable " +
        		"to use the morphing effect.  Again - the time morphing is an interpolation for " +
        		"visualization purposes and requires user interaction and discretion to know when " +
        		"and how to use.  Adjustment of parameters may be needed for each animation to " +
        		"produce the best results. <br><br>" +
        		"Tips:  <br> 1) Filter out the low or noisy values (such as less than 15 dBZ with Radar " +
        		"Reflectivity) to get better results. <br>" +
        		"2) The tool is currently designed for data such as Reflectivity, Satellite IR, " +
        		"Echo Tops and Hail Size.  The interpolation is not suited for Radial Velocity.  " +
        		"</html>";
        	
        JEditorPane l = new JEditorPane("text/html", disclaimer);
        l.setCaretPosition(0);
        l.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(l);
        scrollPane.setPreferredSize(new Dimension(150, 150));
        scrollPane.getVerticalScrollBar().setValue(10);
        mainPanel.add("p hfill", scrollPane);
        
        
        
        mainPanel.add("p", new JLabel("Engage Time Morphing: "));
        mainPanel.add(jcbEngageMorphing, "tab");
        mainPanel.add("br", new JLabel("Number of Time Morph steps: "));
        mainPanel.add(jspinnerNumSteps, "tab");
        mainPanel.add("br", new JLabel("Source of Morph vectors: "));
        mainPanel.add(jcomboVectorSource, "tab");
        mainPanel.add("br", new JLabel("Date buffer percent for SWDI query: "));
        mainPanel.add(jspinnerDateBuffer, "tab");
        mainPanel.add("br", new JLabel("Number of morph vector grid cells in SWDI query: "));
        mainPanel.add(jspinnerNumGridCells, "tab");
        mainPanel.add("br", new JLabel("Show motion vectors used in morphing: "));
        mainPanel.add(jcbShowMotionVector, "tab");
        
        

        
        JPanel buttonPanel = new JPanel(new RiverLayout());

//        buttonPanel.add("p hfill", progressBar);
        buttonPanel.add("p center", doneButton);
//        buttonPanel.add(doneButton);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        
        } catch (Exception e) {
        	
        	e.printStackTrace();
        	
            JButton cancelButton = new JButton("Close");
            cancelButton.setActionCommand("CANCEL");
            cancelButton.setPreferredSize(new Dimension(60, (int)cancelButton.getPreferredSize().getHeight()));
            cancelButton.addActionListener(new SubmitListener(this));
            
            this.getContentPane().removeAll();
        	this.getContentPane().setLayout(new RiverLayout());
        	this.getContentPane().add("p", new JLabel(
        			"Time Morphing is not available for this file."));
        	this.getContentPane().add("p", cancelButton);
        }
		
	}
	

	
	
	
	public int getNumMorphSteps() {
		return Integer.parseInt(jspinnerNumSteps.getValue().toString());
	}
	public MorphVectorSource getMorphVectorSource() {
		String item = jcomboVectorSource.getSelectedItem().toString();
		if (item.equals(SOURCE_ITEM_SWDI)) {
			return MorphVectorSource.NCDC_SWDI;
		}
		else if (item.equals(SOURCE_ITEM_SWDI_TVS)) {
			return MorphVectorSource.NCDC_SWDI_TVS;
		}
		else if (item.equals(SOURCE_ITEM_RUC)) {
			return MorphVectorSource.NCDC_RUC;
		}
		else if (item.equals(SOURCE_ITEM_MARKERS)) {
			return MorphVectorSource.WCT_MARKERS;
		}
		else {
			return MorphVectorSource.NONE;
		}
	}
	public void setMorphingEngaged(boolean isEngaged) {
		jspinnerNumSteps.setEnabled(isEngaged);
		jcomboVectorSource.setEnabled(isEngaged);
		jspinnerDateBuffer.setEnabled(isEngaged);
		jspinnerNumGridCells.setEnabled(isEngaged);
		jcbShowMotionVector.setEnabled(isEngaged);
	}
	
	
	public boolean isMorphingEngaged() {
		return jcbEngageMorphing.isSelected();
	}
	public int getSWDIDateBuffer() {
		return Integer.parseInt(jspinnerDateBuffer.getValue().toString());
	}
	public int getSWDINumGridCells() {
		return Integer.parseInt(jspinnerNumGridCells.getValue().toString());
	}
	public boolean isDrawMotionVector() {
		return jcbShowMotionVector.isSelected();
	}
	
	
	
	
	
	private final class SubmitListener implements ActionListener {
        private Dialog parent;
        public SubmitListener(Dialog parent) {
            this.parent = parent;
        }
        public void actionPerformed(ActionEvent e) {

//            if (e.getActionCommand().equalsIgnoreCase("SUBMIT")) {
////            	saveProperties();
////                formSubmitted = true;
//            	
//            	((JButton)e.getSource()).setEnabled(false);
//                try {
//                    foxtrot.Worker.post(new foxtrot.Task() {
//                        public Object run() {
//
//                            try {
//
//                            	
////                                process();
////                        		progressBar.setValue(0);
////                        		progressBar.setString("Processing Progress");
//
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                            }
//
//                            return "DONE";
//                        }
//                    });
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//            	((JButton)e.getSource()).setEnabled(true);
//
//            }
//            else {
            	parent.dispose();
//            }
        }


    }

}
