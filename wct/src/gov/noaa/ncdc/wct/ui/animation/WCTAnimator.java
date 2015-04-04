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

package gov.noaa.ncdc.wct.ui.animation;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.ui.AWTImageExport;
import gov.noaa.ncdc.wct.ui.AWTImageExport.FileAndFormat;
import gov.noaa.ncdc.wct.ui.KmzExportDialog;
import gov.noaa.ncdc.wct.ui.MorphDialog;
import gov.noaa.ncdc.wct.ui.MorphSettings;
import gov.noaa.ncdc.wct.ui.TimeMorphKmzExport;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXLabel;

public class WCTAnimator extends JXDialog {  

    private static final long serialVersionUID = -4674821697599186493L;

    private WCTViewer viewer;
    private URL[] urlsToLoad;
    
    private JRadioButton jrbLoad = new JRadioButton("Load", true);
    private JRadioButton jrbFrames = new JRadioButton("Export Frames");
    private JRadioButton jrbExport = new JRadioButton("Export Movie");
    private JRadioButton jrbKMZ = new JRadioButton("Export KMZ");
    
    private JCheckBox jcbEngageTimeMorphing = new JCheckBox("Engage Time Morphing?", false);
    
    
    private JXLabel jlDescription = new JXLabel();
    private String loadDescription = 
        "<html>Load animation frames into internal animation player.  The player allows " +
              "customization of speed and loops along with export options for frames or " +
    	      "movies in AVI or Animated GIF formats. </html>";
    private String framesDescription = "<html>Export each frame directly to disk.</html>";
    private String exportDescription = 
        "<html>Export directly to a movie (AVI or Animated GIF) without loading into " +
              "the internal player.  You will be prompted to provide the animation " +
              "speed (frame rate).</html>";
    private String kmzDescription = 
        "<html>Export frames to a 'time-aware' KMZ file for Google Earth.  You will be " +
              "prompted to provide the output file location, 3-D height and shading options.</html>";
    
    private final static AnimationFrame animationFrame = new AnimationFrame();
    
    private Frame animationProgressFrame = null;
    private MorphSettings morphSettings = null;
    
    public WCTAnimator(WCTViewer viewer) {
        this(viewer, null);
    }
    public WCTAnimator(WCTViewer viewer, Component parent) {
        super(viewer.getDataSelector(), new JPanel());
        setTitle("Animation Wizard");
        setModal(true);
        this.viewer=viewer;

//System.out.println("mark 1");        
        
        createGUI();

//System.out.println("mark 2");        
        
        pack();
        setSize(500, 340);
        if (parent != null) {
            setLocationRelativeTo(parent);
            setVisible(true);
        }
    } 

    private void createGUI() {
        
    	final Component finalThis = this;
    	
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        ActionListener radioButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRadioButtonEvent();
            }
        };
        
        jrbLoad.addActionListener(radioButtonListener);
        jrbFrames.addActionListener(radioButtonListener);
        jrbExport.addActionListener(radioButtonListener);
        jrbKMZ.addActionListener(radioButtonListener);        
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(jrbLoad);
        buttonGroup.add(jrbFrames);
        buttonGroup.add(jrbExport);
        buttonGroup.add(jrbKMZ);
        
        JPanel radioButtonPanel = new JPanel(new RiverLayout());
        radioButtonPanel.setBorder(WCTUiUtils.myTitledBorder("Animation Type", 10));
        radioButtonPanel.add(jrbLoad, "left p");
        radioButtonPanel.add(jrbFrames, "br");
        radioButtonPanel.add(jrbExport, "br");
        radioButtonPanel.add(jrbKMZ, "br");

        if (MorphDialog.getInstance(viewer).isMorphingEngaged()) {
        	radioButtonPanel.add(new JLabel(" "), "p");
        	radioButtonPanel.add(jcbEngageTimeMorphing, "p");
        }
        
        jcbEngageTimeMorphing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jcbEngageTimeMorphing.isSelected()) {
					MorphDialog morphDialog = MorphDialog.getInstance(viewer, finalThis);
					morphDialog.setMorphingEngaged(true);
			        morphDialog.setVisible(true);
			        
			        
			        MorphSettings morphSettings = new MorphSettings();
			        morphSettings.setNumMorphSteps(morphDialog.getNumMorphSteps());
			        morphSettings.setMorphVectorSource(morphDialog.getMorphVectorSource());
			        morphSettings.setSwdiDateBuffer(morphDialog.getSWDIDateBuffer());
			        morphSettings.setSwdiNumGridCells(morphDialog.getSWDINumGridCells());
			        morphSettings.setDrawMotionVectors(morphDialog.isDrawMotionVector());
			        morphSettings.setSampleDimension(viewer.getSampleDimension());
			        
			        
			        setMorphSettings(morphSettings);
				}
				else {
			        setMorphSettings(null);
				}
			}
        });
        

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, (int)cancelButton.getPreferredSize().getHeight()));
        cancelButton.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JButton startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(100, (int)startButton.getPreferredSize().getHeight()));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAnimationProcess();
            }
        });
        
        JPanel buttonPanel = new JPanel(new RiverLayout());
        buttonPanel.add(startButton, "center");
        buttonPanel.add(cancelButton);
        
        JPanel descriptionPanel = new JPanel(new RiverLayout());
        descriptionPanel.setBorder(WCTUiUtils.myTitledBorder("Description", 10));
        descriptionPanel.add(jlDescription, "left hfill vtop");

        jlDescription.setLineWrap(true);
        jlDescription.setText(loadDescription);
        

        this.getContentPane().add(radioButtonPanel, BorderLayout.WEST);
        this.getContentPane().add(descriptionPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    } // END METHOD createGUI()

    
    private void setMorphSettings(MorphSettings morphSettings) {
    	this.morphSettings = morphSettings;
    }
    
    
    
    
    private void handleRadioButtonEvent() {
        if (jrbLoad.isSelected()) {
            jlDescription.setText(loadDescription);
        }
        else if (jrbFrames.isSelected()) {
            jlDescription.setText(framesDescription);
        }
        else if (jrbExport.isSelected()) {
            jlDescription.setText(exportDescription);
        }
        else if (jrbKMZ.isSelected()) {
            jlDescription.setText(kmzDescription);
        }
    }

    private void startAnimationProcess() {
        this.setVisible(false);
        
        try {

            urlsToLoad = viewer.getDataSelector().getSelectedURLs();
            animationFrame.setVisible(false);

            if (jrbLoad.isSelected()) {
                LoadAnimationThread loadThread = new LoadAnimationThread(viewer, this, animationFrame, 
                        new Component[] {this}, urlsToLoad, true, null);
                loadThread.setMorphSettings(morphSettings);
                animationProgressFrame = loadThread.getProgressFrame();
                loadThread.start();
            }
            else if (jrbFrames.isSelected()) {
                ExportFramesThread framesThread = new ExportFramesThread(viewer, 
                        new Component[] {this}, urlsToLoad, true, null); 
                framesThread.start();
            }
            else if (jrbExport.isSelected()) {
                FileAndFormat faf = AWTImageExport.saveAsMovieDialog(this);

                if (faf != null) {
                	File movieFile = faf.getFile();
                    String extension = movieFile.toString().substring(movieFile.toString().length()-4, movieFile.toString().length());
                    int exportType = 0;
                    System.out.println(extension+" ---------------------------------");               
                    if (extension.equals(".avi")) {
                        exportType = ExportAnimationThread.AVI;
                    }
                    else if (extension.equals(".gif")) {
                        exportType = ExportAnimationThread.ANIMATED_GIF;
                    }

                    // Get framerate
                    int frameRateInMillis = 50;
                    boolean valid = false;
                    while (! valid) {
                        try {
                            String message = "Enter Time Per Frame in Milliseconds:\n\n"+
                            "VERY FAST    = 10 Frames/Sec  = 100\n"+
                            "FAST             = 5 Frames/Sec    = 200\n"+
                            "MODERATE   = 3 Frames/Sec    = 350\n"+
                            "SLOW           = 2 Frames/Sec    = 500\n"+
                            "VERY SLOW  = 1 Frames/Sec    = 1000";
                            frameRateInMillis = Integer.parseInt(JOptionPane.showInputDialog(this, message, "350"));
                            valid = true;
                        }
                        catch (Exception e) {   
                            valid = false;
                            return;
                        }
                    }

                   	ExportAnimationThread exportThread = new ExportAnimationThread(viewer, movieFile, exportType, 
                            frameRateInMillis, new Component[] {this}, urlsToLoad, true, null); 
                   	exportThread.start();
                }

            }
            else if (jrbKMZ.isSelected()) {
                if (morphSettings == null) {
                	ExportKMZThread kmzThread = new ExportKMZThread(viewer, 
                        new Component[] {this}, urlsToLoad, true, null); 
                	kmzThread.start();
                }
                else {
//                	KmzExportDialog kmzExportDialog = new KmzExportDialog(viewer, isColladaCapable, dataType, showFileChoiceSection);
                	KmzExportDialog kmzExportDialog = new KmzExportDialog(viewer, false, SupportedDataType.RADIAL, true);
            		kmzExportDialog.pack();
            		kmzExportDialog.setLocationRelativeTo(viewer);
            		kmzExportDialog.setVisible(true);
            		
            		String outFileString = kmzExportDialog.getOutputFile();
                    if (! outFileString.endsWith(".kmz")) {
                    	outFileString = outFileString+".kmz";
                    }
            		final File outFile = new File(outFileString);
                	
                    if (! outFile.toString().startsWith(WCTConstants.getInstance().getDataCacheLocation())) {
                    	WCTProperties.setWCTProperty("kmzsavedir", outFile.getParent());
                    }
                    // Check for existing file
                    if (outFile.exists()) {
                        String message = "The KMZ file \n" +
                        "<html><font color=red>" + outFile + "</font></html>\n" +
                        "already exists.\n\n" +
                        "Do you want to proceed and OVERWRITE?";

                        Object[] options = {"YES", "NO"};
                        int choice = JOptionPane.showOptionDialog(kmzExportDialog, message, "OVERWRITE IMAGE FILE",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, options, options[0]);
                        if (choice == 1) { // NO
                            return;
                        }
                    }
                    System.out.println("SAVING: " + outFile);


                    long processTime = System.currentTimeMillis();
                    
                   
                    HashMap<String, String> extraInfoMap = new HashMap<String, String>();
                    extraInfoMap.put(MorphSettings.INFO_RADIAL_SWEEP, String.valueOf(viewer.getRadialRemappedRaster().getLastDecodedSweepIndex()));
                    morphSettings.setExtraInfoMap(extraInfoMap);
                    
                    final JDialog progressDialog = new JDialog(this, "Morphing Progress");
                    progressDialog.setLayout(new RiverLayout());
                    final JProgressBar progressBar = new JProgressBar(0, 100);
                    progressDialog.add(progressBar);
                    final TimeMorphKmzExport tmke = new TimeMorphKmzExport();
                    tmke.addProgressListener(new GeneralProgressListener() {						
						@Override
						public void started(GeneralProgressEvent event) {
						}						
						@Override
						public void progress(GeneralProgressEvent event) {
							progressBar.setValue((int)event.getProgress()*100);
							progressBar.setString(event.getStatus());
						}						
						@Override
						public void ended(GeneralProgressEvent event) {							
						}
					});
                    progressDialog.setModal(true);
                    progressDialog.pack();
                    
                    
                    try {
                        foxtrot.Worker.post(new foxtrot.Task() {
                            public Object run() {

                                try {
                                    tmke.process(urlsToLoad, outFile, viewer.getCurrentExtent(), morphSettings);
                                    progressDialog.setVisible(true);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                return "DONE";
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }       	

                    
                    
                	processTime = (long)((System.currentTimeMillis() - processTime)*.001) + 1;

                	long processTimeMin = processTime / 60;
                	long processTimeSec = processTime % 60;
                	String message = "Export Processing Time: "+processTimeMin + " min "+
                	processTimeSec+" sec ("+processTime+"s)\n\n"+
                	"Open '"+outFile+"'?";

                	int openChoice = JOptionPane.showOptionDialog(this, message,
                			"ANIMATION EXPORT COMPLETE", JOptionPane.YES_NO_OPTION, 
                			JOptionPane.INFORMATION_MESSAGE, 
                			new ImageIcon(WCTViewer.class.getResource("/icons/ge-icon.png")), null, null);
                	if (openChoice == JOptionPane.YES_OPTION) {
                		try {
                			Desktop.getDesktop().open(outFile);
                		} catch (IOException e) {
                			e.printStackTrace();
                			JOptionPane.showMessageDialog(this, "Open Error: "+e.getMessage(),
                					"WCT EXCEPTION", JOptionPane.ERROR_MESSAGE);
                		}
                	}
                	
                }
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public Frame getAnimationProgressFrame() {
    	return animationProgressFrame;
    }
    
} // END CLASS
