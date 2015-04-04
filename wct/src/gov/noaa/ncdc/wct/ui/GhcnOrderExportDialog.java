package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.decoders.ParseGHCNOrder;
import gov.noaa.ncdc.wct.decoders.ParseGHCNOrder.ParseGHCNOrderException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.jdesktop.swingx.JXDialog;

public class GhcnOrderExportDialog extends JXDialog {

	private WCTViewer viewer;
	
	private static GhcnOrderExportDialog singleton = null;

    private TextFieldWithBrowse orderLocation = 
    	new TextFieldWithBrowse("Enter order number, delivery FTP URL or file location:",
    			"ghcnOrderLocation");
    private TextFieldWithBrowse exportLocation = 
    	new TextFieldWithBrowse("Enter export location:", "ghcnExportLocation");

    private JProgressBar progressBar = new JProgressBar();
    private JButton goButton = new JButton("Start");
    private JButton doneButton = new JButton("Done");
    
    private ParseGHCNOrder parseOrder = new ParseGHCNOrder();

    
	private GhcnOrderExportDialog(WCTViewer viewer) {
		this(viewer, viewer);
	}
	
	private GhcnOrderExportDialog(WCTViewer viewer, Component parent) {
		super(viewer, new JPanel());
		
		this.viewer = viewer;
		
        setModal(true);
        setTitle("GHCN Data Export Tool (BETA)");
        
        createGUI();
        
//        pack();
//        setLocation(viewer.getX()+45, viewer.getY()+45);
//        setLocationRelativeTo(parent);
//        setVisible(true);
	}
	
	public static GhcnOrderExportDialog getInstance(WCTViewer viewer) {
		return getInstance(viewer, viewer);
	}
	
	public static GhcnOrderExportDialog getInstance(WCTViewer viewer, Component parent) {
		if (singleton == null) {
			singleton = new GhcnOrderExportDialog(viewer, parent);
		}
        singleton.pack();
        singleton.setLocationRelativeTo(parent);
		return singleton;
	}
	
	
	
	
	private void createGUI() {
		

		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		progressBar.setString("Processing Progress");
		

		
		DataDecodeListener listener = new DataDecodeListener() {
			@Override
			public void decodeStarted(DataDecodeEvent event) {
				progressBar.setString(event.getStatus());
			}
			@Override
			public void decodeEnded(DataDecodeEvent event) {
				progressBar.setString(event.getStatus());
			}
			@Override
			public void decodeProgress(DataDecodeEvent event) {
				progressBar.setString(event.getStatus());
			}
			@Override
			public void metadataUpdate(DataDecodeEvent event) {
			}
		};
		parseOrder.addDataDecodeListener(listener);
		
		
		
		
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
    

        
        mainPanel.add("br center", new JLabel("This tool exports GHCN data orders from NCDC into Shapefiles"));
//        mainPanel.add("p left", new JLabel("Enter order number, delivery FTP URL or file location:"));
//        mainPanel.add("br", orderLocation);
//        mainPanel.add("br", new JLabel("Enter export location:"));
//        mainPanel.add("br", exportLocation);
        mainPanel.add("p left", orderLocation);
        mainPanel.add("br", exportLocation);
        mainPanel.add("p hfill", progressBar);
        
        
        goButton.setActionCommand("SUBMIT");
        goButton.setPreferredSize(new Dimension(60, (int)goButton.getPreferredSize().getHeight()));
        goButton.addActionListener(new SubmitListener(this));
        doneButton.setActionCommand("DONE");
        doneButton.setPreferredSize(new Dimension(60, (int)doneButton.getPreferredSize().getHeight()));
        doneButton.addActionListener(new SubmitListener(this));

        
        JPanel buttonPanel = new JPanel(new RiverLayout());

        buttonPanel.add("p center", goButton);
        buttonPanel.add(doneButton);

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
        			"The GHCN Data Export tool is not available for this file."));
        	this.getContentPane().add("p", cancelButton);
        }
		
	}
	

	
	
	
	
	private final class SubmitListener implements ActionListener {
        private Dialog parent;
        public SubmitListener(Dialog parent) {
            this.parent = parent;
        }
        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equalsIgnoreCase("SUBMIT")) {
            	
//            	((JButton)e.getSource()).setEnabled(false);
                try {
                    foxtrot.Worker.post(new foxtrot.Task() {
                        public Object run() {


                        	progressBar.setIndeterminate(true);
                        	goButton.setText("Cancel");
                        	goButton.setActionCommand("CANCEL");
                        	doneButton.setEnabled(false);
                            try {       
                                
                                process();
                                
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            goButton.setText("Start");
                            goButton.setActionCommand("SUBMIT");
                        	doneButton.setEnabled(true);
                            

                        	progressBar.setIndeterminate(false);
                    		progressBar.setValue(0);
                    		progressBar.setString("Processing Progress");

                            return "DONE";
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

//            	((JButton)e.getSource()).setEnabled(true);

            }
            else if (e.getActionCommand().equalsIgnoreCase("CANCEL")) {
            	parseOrder.setCancel(true);
            }
            else {
            	parent.dispose();
            }
        }


    }
	
	
	private void process() throws IOException, ParseGHCNOrderException, StreamingProcessException {
		
		
		if (orderLocation.getSelectedLocation() == null || 
				orderLocation.getSelectedLocation().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a valid order location.", 
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		String source = orderLocation.getSelectedLocation();
		URL url = null;
		if (source.startsWith("http://") || source.startsWith("ftp://") || 
				source.startsWith("file://")) {
			url = new URL(source);
		}
		else if (source.length() < 12 && source.indexOf("/") == -1) {
//			ftp://ftp.ncdc.noaa.gov/pub/orders/5233.dat
			if (! source.endsWith(".dat")) {
				source = source + ".dat";
			}
			
			url = new URL("ftp://ftp.ncdc.noaa.gov/pub/orders/"+source);
		}
		else {
			url = new File(source).toURI().toURL();
		}

		
		if (exportLocation.getSelectedLocation() == null || 
				exportLocation.getSelectedLocation().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a valid output file.", 
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		File outfile = new File(exportLocation.getSelectedLocation());
		StreamingShapefileExport shpExport = new StreamingShapefileExport(outfile);
		try {
			parseOrder.parseOrder(url, new StreamingProcess[] { shpExport });
		} catch (SocketException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error connecting to order.\n" +
					"The order may have expired, or an internet connection is not available.", 
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "General processing error.\n" +
					"Please report this bug to Steve.Ansari@noaa.gov", 
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		String errLog = parseOrder.getLastParsedErrorLog().toString();
		if (errLog.length() > 0) {
			WCTTextDialog errorLog = new WCTTextDialog(this, errLog, "Error Log", false);
			errorLog.setSize(500, 350);
			errorLog.setLocationRelativeTo(this);
			errorLog.setVisible(true);
		}
	}

}
