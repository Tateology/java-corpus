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
import gov.noaa.ncdc.wct.animation.AnimationUtils;
import gov.noaa.ncdc.wct.animation.AnimationUtils.FrameHandler;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.ui.MorphSettings;
import gov.noaa.ncdc.wct.ui.WCTFrame;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.WCTViewer.RenderCompleteListener;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class LoadAnimationThread extends Thread implements ActionListener
{

    private WCTViewer viewer;
    private WCTAnimator wctAnimator;
    private URL[] urlsToLoad;
    private JFrame progressFrame = new WCTFrame("Animation Progress");
    private JProgressBar progressBar, individualBar, memBar;
    private boolean hideViewer;
    private Dimension dimension;
    private JButton cancelButton;
    
    private Component[] framesToHide;
    private boolean[] framesToHideVisibility;
    
    private boolean endThread = false;

    private AnimationFrame animationFrame;
    private FrameHandler frameHandler;
    
    private MorphSettings morphSettings;
    

    /**
     * Constructor
     * @param viewer            WCTViewer Object
     * @param urlsToLoad        The data Filenames to load
     * @param jButton            The button used to initiate the thread
     * @param hideViewer         Hides the viewer during processing
     */
    LoadAnimationThread(WCTViewer viewer, WCTAnimator wctAnimator, AnimationFrame animationFrame, 
            Component[] framesToHide, URL[] urlsToLoad, boolean hideViewer) {

        this(viewer, wctAnimator, animationFrame, framesToHide, urlsToLoad, hideViewer, null);  
    }
    /**
     * Constructor
     * @param viewer            WCTViewer Object
     * @param urlsToLoad        The data Filenames to load
     * @param jButton            The button used to initiate the thread
     * @param hideViewer         Hides the viewer during processing
     */
    LoadAnimationThread(WCTViewer viewer, WCTAnimator wctAnimator, AnimationFrame animationFrame, 
            Component[] framesToHide, URL[] urlsToLoad, 
            boolean hideViewer, Dimension dimension) {

        this.viewer = viewer;
        this.wctAnimator = wctAnimator;
        this.framesToHide = framesToHide;
        this.urlsToLoad = urlsToLoad;
        this.hideViewer = hideViewer;
        this.dimension = dimension;
        this.animationFrame = animationFrame;
    }


    /**
     * Method that does the work
     */
    public void run() {
        
        RenderCompleteListener renderCompleteListener = new RenderCompleteListener() {             
            @Override
            public void renderProgress(int progress) {
                individualBar.setValue(progress);
                individualBar.setString("Processing Data: "+progress+" % Complete");
            }
            @Override
            public void renderComplete() {
            }
        };
        viewer.addRenderCompleteListener(renderCompleteListener); 

        
        
        // Hold onto the viewer extent before resizing
        //java.awt.geom.Rectangle2D.Double gr = viewer.getCurrentExtent();

        System.out.println("Starting the LoadAnimationThread");

//        jButton.setEnabled(false);            

        framesToHideVisibility = new boolean[framesToHide.length];
        
        if (hideViewer) {
            viewer.setVisible(false);
            if (framesToHide != null) {
                for (int i=0; i<framesToHide.length; i++) {                    
                    if (framesToHide[i] != null) {
                        framesToHideVisibility[i] = framesToHide[i].isVisible();
                        framesToHide[i].setVisible(false);
                    }
                }
            }
        }

        dimension = null;
        if (dimension != null) {
            viewer.setSize(dimension);
        }

        // Create progress frame        
        individualBar = new JProgressBar();
        individualBar.setIndeterminate(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        memBar = new JProgressBar(0, 100);
        memBar.setIndeterminate(false);
        memBar.setStringPainted(true);
        memBar.setString("0 %");
        memBar.setValue(0);
        cancelButton = new JButton("Cancel Animation");
        cancelButton.addActionListener(this);
        JPanel cancelPanel = new JPanel();
        cancelPanel.add(cancelButton);

        progressFrame.getContentPane().setLayout(new RiverLayout());
        progressFrame.getContentPane().add(new JLabel("Animation Processing Progress", JLabel.CENTER), "center hfill");
        progressFrame.getContentPane().add(individualBar, "hfill br");
        progressFrame.getContentPane().add(progressBar, "hfill br");
        progressFrame.getContentPane().add(memBar, "hfill br");
        progressFrame.getContentPane().add(cancelPanel, "hfill br");

        progressFrame.pack();
        progressFrame.setSize(progressFrame.getPreferredSize().width + 100, progressFrame.getPreferredSize().height);
        progressFrame.setVisible(true);
        
        viewer.setAnimationProgressFrame(progressFrame);

        
        progressFrame.setDefaultCloseOperation(JFrame. DO_NOTHING_ON_CLOSE);
        // what happens when user closes the JFrame.
        WindowListener windowListener = new WindowAdapter() {
            //  anonymous WindowAdapter class
            public void windowClosing (WindowEvent w) {
                endThread();
                progressBar.setString("--- Canceling ---");
            } // end windowClosing
        };// end anonymous class
        progressFrame.addWindowListener(windowListener);
        
        
        boolean autoMinMax = viewer.getMapSelector().isGridAutoMinMaxSelected();

        
        animationFrame.clearImages();
        System.gc();

        try {
            
        	// create the frame handler to add each processed image to the animation frame list
            frameHandler = new FrameHandler() {
                private boolean isProcessCanceled = false;
                private int cnt=0;
                
                @Override
                public void processFrame(BufferedImage frameImage, String displayName, String saveName) {
                	try {
                		animationFrame.addImage(frameImage, displayName);
                		
                		int numImages = urlsToLoad.length;
                		if (morphSettings != null) {
                			numImages = (urlsToLoad.length-1)*morphSettings.getNumMorphSteps();

                			// update progress in listener
                    		double progress = ((double)++cnt)/numImages*100;
                            individualBar.setValue((int)progress);
                            individualBar.setString("Adding Image Frame "+cnt+"/"+numImages);
                		}
                		
                        
                	} catch (IOException e) {
                        String string = "-- General Animation Error --\n" +
                        		"Please report as a bug to Steve.Ansari@noaa.gov";
                        JOptionPane.showMessageDialog(progressFrame, string,
                                "ANIMATION ERROR", JOptionPane.ERROR_MESSAGE);

                	}
                	
                	
                    // do auto min/max the first time, if selected, then use that min/max for every other image
//                	System.out.println(viewer.getMapSelector().getGridColorTableMinValue()+" ::: "+viewer.getMapSelector().getGridColorTableMaxValue());
                	
                	boolean isValidMinMax = ! (
                		Double.isNaN(viewer.getMapSelector().getGridColorTableMinValue()) ||
                		Double.isNaN(viewer.getMapSelector().getGridColorTableMaxValue()) ||
                		Double.isInfinite(viewer.getMapSelector().getGridColorTableMinValue()) ||
                		Double.isInfinite(viewer.getMapSelector().getGridColorTableMaxValue())
                	);
                	if (isValidMinMax) {
                		viewer.getMapSelector().setGridAutoMinMax(false);
                	}
                    
                    updateMemoryProgressBar();
                    if (memBar.getValue() > 90) {
                        endThread = true;
                        cancel();
                        String string = "CANCELING ANIMATION\n-- System Memory Maximum Reached --\n";
                        JOptionPane.showMessageDialog(progressFrame, string,
                                "MEMORY LIMIT", JOptionPane.ERROR_MESSAGE);
                    }

                    System.gc();

                }

                @Override
                public void cancel() {
//                    System.out.println("cancel() called");
                    isProcessCanceled = true;
                }
                public boolean isCanceled() {
//                    System.out.println("isCanceled(): "+isProcessCanceled);
                    return isProcessCanceled;
                }
            };
            
            
            GeneralProgressListener individualProgressListener = new GeneralProgressListener() {
                @Override
                public void started(GeneralProgressEvent event) {
                }
                @Override
                public void progress(GeneralProgressEvent event) {
                    updateMemoryProgressBar();
                    int percent = (int)event.getProgress();
                    individualBar.setValue(percent);
                    individualBar.setString(event.getStatus());
                }
                @Override
                public void ended(GeneralProgressEvent event) {
                }
            };
            
            GeneralProgressListener progressListener = new GeneralProgressListener() {
                @Override
                public void started(GeneralProgressEvent event) {
                }
                @Override
                public void progress(GeneralProgressEvent event) {
                    updateMemoryProgressBar();
                    int percent = (int)event.getProgress();
                    progressBar.setValue(percent);
                    progressBar.setString("Animation Progress: "+percent+" %");
                    progressFrame.setTitle("("+percent+" %) Animation Progress");
                }
                @Override
                public void ended(GeneralProgressEvent event) {
                    progressFrame.setTitle("Animation Progress");
                }
            };
            
            
            progressBar.setString("Animation Progress: 0 % (0/"+urlsToLoad.length+")");            
            progressFrame.setTitle("Animation Progress");
            updateMemoryProgressBar();
            
            
            AnimationUtils.frameProcess(new FrameHandler[] { frameHandler }, individualProgressListener,
            		progressListener, viewer, urlsToLoad, morphSettings);
            
            
            System.out.println("done with .frameProcess");
            

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(progressFrame, e.toString(),
                    "ANIMATION ERROR", JOptionPane.ERROR_MESSAGE);

            progressFrame.dispose();
            viewer.setVisible(true);
            progressFrame.setTitle("Animation Progress");
        } finally {
            if (hideViewer) {
                viewer.setVisible(true);
                if (framesToHide != null) {
                    for (int i=0; i<framesToHide.length; i++) {
                        if (framesToHide[i] != null) {
                            framesToHide[i].setVisible(framesToHideVisibility[i]);
                        }
                    }
                }
            }
        }

        viewer.getMapSelector().setGridAutoMinMax(autoMinMax);
        
        viewer.getDataSelector().checkCacheStatus();

        updateMemoryProgressBar();

        progressFrame.dispose();
        viewer.updateMemoryLabel();

        viewer.removeRenderCompleteListener(renderCompleteListener);        
        
        
        animationFrame.setAnimationIcon(0);
        animationFrame.validate();
        
        animationFrame.pack();
        animationFrame.setSize(new Dimension(viewer.getSize().width-58, viewer.getSize().height+8));
//        animationFrame.setLocation(25, 25);
        

        animationFrame.startAnimation();

        wctAnimator.setVisible(false);
        
        
        System.out.println("FINISHED LOADING THREAD");

        // Set stuff to null because we're done!
        System.gc();

        try {
            Thread.sleep(500);
        } catch (Exception e) {}
        
        animationFrame.setLocationRelativeTo(viewer);
        animationFrame.setVisible(true);
        animationFrame.setAlwaysOnTop(true);
        animationFrame.toFront();
        
        animationFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                animationFrame.setAlwaysOnTop(false);
            }
        });
        
        viewer.setIsLoading(false);

    } // END METHOD run()

    private void updateMemoryProgressBar() {
        Runtime r = Runtime.getRuntime();
        memBar.setValue((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0));
        memBar.setString("Memory Usage: "+ ((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0)) + " %");
    }
    
    public void endThread() {
        endThread = true;
        if (frameHandler != null) {
            frameHandler.cancel();
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cancelButton) {
            endThread = true;
            if (frameHandler != null) {
                frameHandler.cancel();
            }
            progressBar.setString("--- Canceling ---");
        }
    }

    
    
    public Frame getProgressFrame() {
    	return progressFrame;
    }
	public void setMorphSettings(MorphSettings morphSettings) {
		this.morphSettings = morphSettings;
	}
	public MorphSettings getMorphSettings() {
		return morphSettings;
	}
    
    
    
    
    
    
    
    
    
    
    
} // END CLASS
