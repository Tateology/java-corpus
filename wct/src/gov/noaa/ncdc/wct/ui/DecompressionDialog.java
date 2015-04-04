package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.io.Decompressor;
import gov.noaa.ncdc.wct.io.WCTDataSourceDB;
import gov.noaa.ncdc.wct.io.WCTTransfer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.io.FileUtils;
import org.geotools.feature.IllegalAttributeException;
import org.jdesktop.swingx.JXDialog;

import ucar.ma2.InvalidRangeException;


public class DecompressionDialog extends JXDialog {

	private DataSelector dataSelector;
	private Decompressor decompressor = new Decompressor();
	
	private JButton cancelButton;
	
	private final JProgressBar progressBar = new JProgressBar();
	
	private TextFieldWithBrowse outDirTextField = 
			new TextFieldWithBrowse("Select output directory:", "decompression-output-dir", true);
	
	
	/**
	 * Show the WCT Math Dialog
	 * @param viewer
	 */
	public DecompressionDialog(DataSelector dataSelector) {
		this(dataSelector, dataSelector);
	}
	
	public DecompressionDialog(DataSelector dataSelector, Component parent) {
		super(dataSelector, new JPanel());
		
		this.dataSelector = dataSelector;
		
        setModal(true);
        setTitle("Decompression Tool");
        
        createGUI();
        
        pack();
        setLocation(parent.getX()+45, parent.getY()+45);
        setVisible(true);
	}
	
	
	
	
	private void createGUI() {
		

		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		progressBar.setMaximum(100);
		progressBar.setString("Processing Progress");
   
        
        // add listeners
		decompressor.addProgressListener(new GeneralProgressListener() {
			
			@Override
			public void started(GeneralProgressEvent event) {
				progressBar.setString(event.getStatus());
				progressBar.setValue((int)event.getProgress());
			}
			
			@Override
			public void progress(GeneralProgressEvent event) {
				progressBar.setString(event.getStatus());
				progressBar.setValue((int)event.getProgress());				
				
			}
			
			@Override
			public void ended(GeneralProgressEvent event) {
				progressBar.setString(event.getStatus());
				progressBar.setValue((int)event.getProgress());				
				
			}
		});
        


		try {


			JPanel mainPanel = new JPanel(new RiverLayout());
			
			mainPanel.add("p center", new JLabel("This file is a compressed archive (bundle) " +
					"of files and needs to be unpacked before use."));

			mainPanel.add("p left", outDirTextField);


			JButton goButton = new JButton("Start");
			goButton.setActionCommand("SUBMIT");
			goButton.setPreferredSize(new Dimension(60, (int)goButton.getPreferredSize().getHeight()));
			goButton.addActionListener(new SubmitListener(this));

			cancelButton = new JButton("Close");
			cancelButton.setActionCommand("CLOSE");
			cancelButton.setPreferredSize(new Dimension(60, (int)cancelButton.getPreferredSize().getHeight()));
			cancelButton.addActionListener(new SubmitListener(this));




			JPanel buttonPanel = new JPanel(new RiverLayout());

			buttonPanel.add("p hfill", progressBar);
			buttonPanel.add("p center", goButton);
			buttonPanel.add(cancelButton);

			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add(mainPanel, BorderLayout.CENTER);
			this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);


		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e, "General Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	
	
	
	
	public static File decompressAndShowProgress(final Dialog parent, final File infile, final File outputDir) {
		

		final JDialog dialog = new JDialog(parent, "Decompression Status", true);
		final Decompressor decompressor = new Decompressor();
		
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		progressBar.setMaximum(100);
		progressBar.setString("Processing Progress");
   
        
        // add listeners
		decompressor.addProgressListener(new GeneralProgressListener() {
			
			@Override
			public void started(GeneralProgressEvent event) {
				progressBar.setString(event.getStatus());
				progressBar.setValue((int)event.getProgress());
			}
			
			@Override
			public void progress(GeneralProgressEvent event) {
				progressBar.setString(event.getStatus());
				progressBar.setValue((int)event.getProgress());				
				
			}
			
			@Override
			public void ended(GeneralProgressEvent event) {
				progressBar.setString(event.getStatus());
				progressBar.setValue((int)event.getProgress());				
				
			}
		});
        


		try {


			JPanel mainPanel = new JPanel(new RiverLayout());
			
			mainPanel.add("p center", new JLabel("This file is compressed and " +
					"needs to be decompressed before use."));

			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("CANCEL");
			cancelButton.setPreferredSize(new Dimension(60, (int)cancelButton.getPreferredSize().getHeight()));
			cancelButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent evt) {
					decompressor.setShouldCancel(true);
				}
			});




			JPanel buttonPanel = new JPanel(new RiverLayout());

			buttonPanel.add("p hfill", progressBar);
			buttonPanel.add(cancelButton);

			dialog.getContentPane().setLayout(new BorderLayout());
			dialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
			dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);


			
			File returnFile = new File(outputDir.toString() + File.separator + 
					infile.getName().substring(0, infile.getName().lastIndexOf("."))); 
			
	        

	        dialog.pack();
	        dialog.setSize(dialog.getPreferredSize().width+50, dialog.getPreferredSize().height);
	        dialog.setModal(false);
	        parent.setEnabled(false);
	        dialog.setLocationRelativeTo(parent);
	        dialog.setVisible(true);
	        
	        foxtrot.Worker.post(new foxtrot.Task() {
	        	public Object run() {

		
            		try {
//            			System.out.println("doing it...");
						decompressor.processFile(infile, outputDir);
				        parent.setEnabled(true);
						dialog.dispose();
					} catch (IOException e) {
				        parent.setEnabled(true);
						e.printStackTrace();
					}
	        		return "DONE";
	        	}			        
	        });
	        
	        System.out.println("returning as if complete: "+returnFile);
	        System.out.println("file exists: "+returnFile.exists());
	        
	        return returnFile;
			
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(dialog, e, "General Error", JOptionPane.ERROR_MESSAGE);
		}

		return null;
	}
	
	
	
	
	
	
	
	
	private void process() throws WCTExportNoDataException, WCTExportException, DecodeException, FeatureRasterizerException, 
		IllegalAttributeException, InvalidRangeException, DecodeHintNotSupportedException, 
		URISyntaxException, ParseException, Exception {
		
		
		
		URL[] selectedURLs = dataSelector.getSelectedURLs();
		
		for (int n=0; n<selectedURLs.length; n++) {
			
			URL url = WCTTransfer.getURL(selectedURLs[n], null, false, 
					dataSelector, new ArrayList<GeneralProgressListener>());
			File outdir = new File(outDirTextField.getSelectedLocation());
			if (url.getProtocol().startsWith("file")) {
				decompressor.processFile(FileUtils.toFile(url), outdir);
			}
			else {
				decompressor.processURL(url, outdir);
			}
		}
	}

	
	
	
	public String getSelectedOutputLocation() {
		return outDirTextField.getSelectedLocation();
	}
	
	
	
	
	
	
	private final class SubmitListener implements ActionListener {
        private Dialog parent;
        public SubmitListener(Dialog parent) {
            this.parent = parent;
        }
        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equalsIgnoreCase("SUBMIT")) {

            	
            	((JButton)e.getSource()).setEnabled(false);
                try {
                    foxtrot.Worker.post(new foxtrot.Task() {
                        public Object run() {

                            try {

                            	cancelButton.setText("Cancel");
                                cancelButton.setActionCommand("CANCEL");
                                process();
                                cancelButton.setEnabled(true);
                            	cancelButton.setText("Close");
                                cancelButton.setActionCommand("CLOSE");

                        		progressBar.setValue(0);
                        		progressBar.setString("Processing Progress");

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                reportException(ex);
                            }

                            return "DONE";
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            	((JButton)e.getSource()).setEnabled(true);

            }
            else if (e.getActionCommand().equalsIgnoreCase("CANCEL")) {
            	decompressor.setShouldCancel(true);
                cancelButton.setEnabled(false);
            	cancelButton.setText("Canceling...");
            }
            else {
            	try {
					dataSelector.getDataSourcePanel().setDataType(WCTDataSourceDB.LOCAL_DISK);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
            	parent.dispose();
            }
        }


    }

	
	private void reportException(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage());
	}
	
//	private int getProcessedCount() {
//		return processedCountInLoop;
//	}

	
	
	
	
	
	
	
	
}
