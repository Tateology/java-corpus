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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.madgag.gif.fmsware.AnimatedGifEncoder;

//import com.kitfox.movieTools.video.BufferedImageProducer;
//import com.kitfox.movieTools.video.ImagesDataSource;
//import com.kitfox.movieTools.video.VideoRecorder;

/**
 * Description of the Class
 * 
 * @author steve.ansari
 * @created August 23, 2004
 */
public class ExportAnimationThread extends Thread implements ActionListener {

    public final static int ANIMATED_GIF = 1;

    public final static int AVI = 2;

    private WCTViewer viewer;

    private URL[] urlsToLoad;

    private ImageIcon[] icons;

    private JFrame progressFrame;

    private JProgressBar individualBar, progressBar, memBar;

    private boolean hideViewer;

    private Dimension dimension;

    private String[] iconTitles;

    private JButton cancelButton;

    private Component[] framesToHide;

    private File movieFile;

    private boolean endThread = false;

    private int exportType;

    private int frameRateInMillis;

    private FrameHandler frameHandler = null;

    private MorphSettings morphSettings;

    
    
    
    /**
     * Constructor
     * 
     * @param viewer
     *            WCTViewer Object
     * @param urlsToLoad
     *            The Nexrad Filenames to load
     * @param hideViewer
     *            Hides the viewer during processing
     * @param movieFile
     *            Description of the Parameter
     * @param exportType
     *            Description of the Parameter
     * @param framesToHide
     *            Description of the Parameter
     */
    ExportAnimationThread(WCTViewer viewer, File movieFile,
            int exportType, int frameRateInMillis, Component[] framesToHide,
            URL[] urlsToLoad, boolean hideViewer) {

        this(viewer, movieFile, exportType, frameRateInMillis, framesToHide,
                urlsToLoad, hideViewer, null);
    }

    /**
     * Constructor
     * 
     * @param viewer
     *            WCTViewer Object
     * @param urlsToLoad
     *            The Nexrad Filenames to load
     * @param hideViewer
     *            Hides the viewer during processing
     * @param movieFile
     *            Description of the Parameter
     * @param exportType
     *            Description of the Parameter
     * @param framesToHide
     *            Description of the Parameter
     * @param dimension
     *            Description of the Parameter
     */
    ExportAnimationThread(WCTViewer viewer, File movieFile,
            int exportType, int frameRateInMillis, Component[] framesToHide,
            URL[] urlsToLoad, boolean hideViewer,
            Dimension dimension) {

        this.viewer = viewer;
        this.framesToHide = framesToHide;
        this.urlsToLoad = urlsToLoad;
        this.hideViewer = hideViewer;
        this.dimension = dimension;
        this.movieFile = movieFile;
        this.exportType = exportType;
        this.frameRateInMillis = frameRateInMillis;
    }

    /**
     * Method that does the work
     */
    public void run() {
        RenderCompleteListener renderCompleteListener = new RenderCompleteListener() {             
            @Override
            public void renderProgress(int progress) {
                individualBar.setValue(progress);                 
            }
            @Override
            public void renderComplete() {
            }
        };
        viewer.addRenderCompleteListener(renderCompleteListener); 

        if (hideViewer) {
            viewer.setVisible(false);
            if (framesToHide != null) {
                for (int i = 0; i < framesToHide.length; i++) {
                    if (framesToHide[i] != null) {
                        framesToHide[i].setVisible(false);
                    }
                }
            }
        }

        dimension = null;
        Dimension holdDim = null;
        if (dimension != null) {
            holdDim = viewer.getSize();
            viewer.setSize(dimension);
            // viewer.setCurrentExtent(gr);
        }

        // Create progress frame
        progressFrame = new WCTFrame("Export Animation Progress");
        individualBar = new JProgressBar();
        individualBar.setIndeterminate(false);
        individualBar.setStringPainted(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Processed 0/" + urlsToLoad.length
                + " Frames (0 %)");
        progressBar.setValue(0);
        memBar = new JProgressBar(0, 100);
        memBar.setIndeterminate(false);
        memBar.setStringPainted(true);
        memBar.setString("0 %");
        memBar.setValue(0);
        cancelButton = new JButton("Cancel Animation");
        cancelButton.addActionListener(this);

        progressFrame.getContentPane().setLayout(new GridLayout(5, 1));
        progressFrame.getContentPane().add(
                new JLabel("Animation Processing Progress", JLabel.CENTER));
        progressFrame.getContentPane().add(individualBar);
        progressFrame.getContentPane().add(progressBar);
        progressFrame.getContentPane().add(memBar);
        progressFrame.getContentPane().add(cancelButton);

        progressFrame.pack();
        progressFrame.setSize(progressFrame.getPreferredSize().width + 100, progressFrame.getPreferredSize().height);
        progressFrame.setVisible(true);


        icons = new ImageIcon[urlsToLoad.length];
        iconTitles = new String[urlsToLoad.length];

        long processTime = System.currentTimeMillis();
        long timeStamp = processTime % 100000000;

        boolean autoMinMax = viewer.getMapSelector().isGridAutoMinMaxSelected();
        viewer.getMapSelector().setGridAutoMinMax(false);


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
                int percent = (int)event.getProgress();
                progressBar.setValue(percent);
                progressBar.setString(percent+" % -- Animation Progress");
                progressFrame.setTitle("("+percent+" %) Animation Progress");
            }
            @Override
            public void ended(GeneralProgressEvent event) {
                progressFrame.setTitle("Animation Progress");
            }
        };
        
        
    	boolean isValidMinMax = ! (
        		Double.isNaN(viewer.getMapSelector().getGridColorTableMinValue()) ||
        		Double.isNaN(viewer.getMapSelector().getGridColorTableMaxValue()) ||
        		Double.isInfinite(viewer.getMapSelector().getGridColorTableMinValue()) ||
        		Double.isInfinite(viewer.getMapSelector().getGridColorTableMaxValue())
        	);
        	if (isValidMinMax) {
        		viewer.getMapSelector().setGridAutoMinMax(false);
        	}


        if (exportType == ANIMATED_GIF) {
            try {
                final AnimatedGifEncoder e = new AnimatedGifEncoder();
                e.setQuality(1);
                e.start(movieFile.toString() + "_" + timeStamp); 
                e.setDelay(frameRateInMillis);
                e.setRepeat(0);
                
                frameHandler = new FrameHandler() {
                    private boolean canceled = false;
                    private int cnt=0;
                    
                    @Override
                    public void cancel() {
                        canceled = true;
                    }
                    @Override
                    public boolean isCanceled() {
                        return canceled;
                    }

                    @Override
                    public void processFrame(BufferedImage frameImage, String displayName, String saveName) {
                        e.addFrame(frameImage);
                        
                		int numImages = urlsToLoad.length;
                		if (morphSettings != null) {
                			numImages = (urlsToLoad.length-1)*morphSettings.getNumMorphSteps();

                			// update progress in listener
                    		double progress = ((double)++cnt)/numImages*100;
                            individualBar.setValue((int)progress);
                            individualBar.setString("Adding Image Frame "+cnt+"/"+numImages);
                		}

                    }

                };
                AnimationUtils.frameProcess(new FrameHandler[] { frameHandler }, individualProgressListener, progressListener, 
                		viewer, urlsToLoad, morphSettings);
                
                
                progressBar.setString("Finalizing...");
                progressBar.setValue(100);
                e.finish();
                progressFrame.setTitle("Export Animation Progress");
            } catch (Exception e) {
                System.out.println("CAUGHT EXCEPTION " + e);
            }
        }
        else if (exportType == AVI) {

            try {

                // create a video recorder
                final AVIWriter aviWriter = new AVIWriter();

                // get first frame
                BufferedImage firstFrame = viewer.getViewerBufferedImage();

                double frameRateInFPS = 1000.0/frameRateInMillis;
                int width = firstFrame.getWidth(null);
                int height = firstFrame.getHeight(null);
                int frameCount = AnimationUtils.frameCount(new FrameHandler[] { frameHandler }, progressListener, viewer, urlsToLoad);
                aviWriter.init(new File(movieFile.toString()+"_"+timeStamp), width, height, frameCount, frameRateInFPS);

                frameHandler = new FrameHandler() {
                    private boolean canceled = false;
                    private int cnt=0;

                    @Override
                    public void cancel() {
                        canceled = true;
                    }
                    @Override
                    public boolean isCanceled() {
                        return canceled;
                    }

                    @Override
                    public void processFrame(BufferedImage frameImage, String displayName, String saveName) {
                        try {
                            aviWriter.addFrame(frameImage);

                    		int numImages = urlsToLoad.length;
                    		if (morphSettings != null) {
                    			numImages = (urlsToLoad.length-1)*morphSettings.getNumMorphSteps();

                    			// update progress in listener
                        		double progress = ((double)++cnt)/numImages*100;
                                individualBar.setValue((int)progress);
                                individualBar.setString("Adding Image Frame "+cnt+"/"+numImages);
                    		}

                        } catch (IOException e) {
                            e.printStackTrace();
                        }                        
                    }

                };
                AnimationUtils.frameProcess(new FrameHandler[] { frameHandler }, individualProgressListener, progressListener, 
                		viewer, urlsToLoad, morphSettings);

                progressBar.setString("Finalizing...");
                progressBar.setValue(urlsToLoad.length);
                aviWriter.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("CAUGHT EXCEPTION " + e);
                progressFrame.setTitle("Animation Progress");
                return;
            }

        }

        // Delete old movie if present
        try {
            if (movieFile.exists()) {
                movieFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Rename file
        try {
            File procFile = new File(movieFile.toString() + "_" + timeStamp);
            procFile.renameTo(movieFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int movieLength = (int) (urlsToLoad.length * (frameRateInMillis * .001)) + 1;
        int movieLengthMin = movieLength / 60;
        int movieLengthSec = movieLength % 60;
        processTime = (long) ((System.currentTimeMillis() - processTime) * .001) + 1;
        long processTimeMin = processTime / 60;
        long processTimeSec = processTime % 60;
        String message = "Completed Animation Export of: \n"
            + movieFile.toString() + "\n" + urlsToLoad.length
            + " Frames Created at " + frameRateInMillis + "ms Frame Rate ("
            + (1000 / frameRateInMillis)
            + " frames/sec) \nTotal Movie Length: " + movieLengthMin
            + " min " + movieLengthSec + " sec (" + movieLength
            + "s)\nExport Processing Time: " + processTimeMin + " min "
            + processTimeSec + " sec (" + processTime + "s)\n";

        JOptionPane.showMessageDialog(progressFrame, message,
                "ANIMATION EXPORT COMPLETE", JOptionPane.INFORMATION_MESSAGE);

        updateMemoryProgressBar();
        
        progressFrame.dispose();

        if (dimension != null) {
            viewer.setSize(holdDim);
        }
        viewer.getMapSelector().setGridAutoMinMax(autoMinMax);
        viewer.getDataSelector().checkCacheStatus();

        viewer.updateMemoryLabel();

        if (hideViewer) {
            viewer.setVisible(true);
            if (framesToHide != null) {
                for (int i = 0; i < framesToHide.length; i++) {
                    if (framesToHide[i] != null) {
                        framesToHide[i].setVisible(true);
                    }
                }
            }
        }

        System.out.println("FINISHED LOADING THREAD");

        // Set stuff to null because we're done!
        icons = null;
        System.gc();

        viewer.setIsLoading(false);

        viewer.removeRenderCompleteListener(renderCompleteListener); 
    }

    // END METHOD run()

    /**
     * Description of the Method
     */
    public void endThread() {
        endThread = true;
    }

    private void updateMemoryProgressBar() {
        Runtime r = Runtime.getRuntime();
        memBar.setValue((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0));
        memBar.setString("Memory Usage: "+ ((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0)) + " %");
    }

    /**
     * Description of the Method
     * 
     * @param e
     *            Description of the Parameter
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cancelButton) {
            frameHandler.cancel();
            endThread = true;
            progressBar.setString("--- Canceling ---");
        }
    }

	public void setMorphSettings(MorphSettings morphSettings) {
		this.morphSettings = morphSettings;
	}

	public MorphSettings getMorphSettings() {
		return morphSettings;
	}

}
