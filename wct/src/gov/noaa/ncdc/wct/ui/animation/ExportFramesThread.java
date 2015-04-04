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

import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.animation.AnimationUtils;
import gov.noaa.ncdc.wct.animation.AnimationUtils.FrameHandler;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.ui.AWTImageExport;
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
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * @created    August 23, 2004
 */
public class ExportFramesThread extends Thread implements ActionListener {

    /**
     *  Description of the Field
     */
    public final static int ANIMATED_GIF = 1;

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
    private boolean endThread = false;


    private FrameHandler frameHandler;

    /**
     * Constructor
     *
     * @param  viewer            NexradIAViewer Object
     * @param  urlsToLoad         The Nexrad Filenames to load
     * @param  hideViewer         Hides the viewer during processing
     * @param  movieFile          Description of the Parameter
     * @param  exportType         Description of the Parameter
     * @param  framesToHide       Description of the Parameter
     * @param  frameRateInMillis  Description of the Parameter
     */
    ExportFramesThread(WCTViewer viewer, Component[] framesToHide, 
            URL[] urlsToLoad, boolean hideViewer) {

        this(viewer, framesToHide, urlsToLoad, hideViewer, null);
    }


    /**
     * Constructor
     *
     * @param  viewer            NexradIAViewer Object
     * @param  urlsToLoad         The Nexrad Filenames to load
     * @param  hideViewer         Hides the viewer during processing
     * @param  movieFile          Description of the Parameter
     * @param  exportType         Description of the Parameter
     * @param  framesToHide       Description of the Parameter
     * @param  dimension          Description of the Parameter
     * @param  frameRateInMillis  Description of the Parameter
     */
    ExportFramesThread(WCTViewer viewer, Component[] framesToHide, URL[] urlsToLoad,
            boolean hideViewer, Dimension dimension) {

        this.viewer = viewer;
        this.framesToHide = framesToHide;
        this.urlsToLoad = urlsToLoad;
        this.hideViewer = hideViewer;
        this.dimension = dimension;
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

        try {

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
                //viewer.setCurrentExtent(gr);
            }

            // Create progress frame
            progressFrame = new WCTFrame("Export Frames Progress");
            individualBar = new JProgressBar(0, 100);
            individualBar.setIndeterminate(false);
            individualBar.setStringPainted(false);
            progressBar = new JProgressBar(0, 100);
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(true);
            progressBar.setString("Processed 0/" + urlsToLoad.length + " Frames (0 %)");
            progressBar.setValue(0);
            memBar = new JProgressBar(0, 100);
            memBar.setIndeterminate(false);
            memBar.setStringPainted(true);
            memBar.setString("0 %");
            memBar.setValue(0);
            cancelButton = new JButton("Cancel Animation");
            cancelButton.addActionListener(this);

            progressFrame.getContentPane().setLayout(new GridLayout(5, 1));
            progressFrame.getContentPane().add(new JLabel("Animation Processing Progress", JLabel.CENTER));
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

            
            // Set up File Chooser
            JFileChooser fc = new JFileChooser(WCTProperties.getWCTProperty("framessavedir"));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle("Choose Directory for Saved Images");
            OpenFileFilter jpgFilter = new OpenFileFilter("jpg", true, "JPEG Images");
            OpenFileFilter gifFilter = new OpenFileFilter("gif", true, "GIF Images");
            OpenFileFilter pngFilter = new OpenFileFilter("png", true, "PNG Images");
            OpenFileFilter pictFilter = new OpenFileFilter("pict", true, "PICT Images");
            OpenFileFilter tiffFilter = new OpenFileFilter("tif", true, "TIFF Images");
            OpenFileFilter bmpFilter = new OpenFileFilter("bmp", true, "BMP Images");
            OpenFileFilter targaFilter = new OpenFileFilter("tga", true, "TARGA Images");
            fc.addChoosableFileFilter(jpgFilter);
            fc.addChoosableFileFilter(gifFilter);
            fc.addChoosableFileFilter(pictFilter);
            fc.addChoosableFileFilter(tiffFilter);
            fc.addChoosableFileFilter(bmpFilter);
            fc.addChoosableFileFilter(targaFilter);
            fc.addChoosableFileFilter(pngFilter);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(pngFilter);
            
            int returnVal = fc.showSaveDialog(viewer);
            File dir = null;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                int choice = JOptionPane.YES_OPTION;
                // intialize to YES!
                dir = fc.getSelectedFile();
                WCTProperties.setWCTProperty("framessavedir", dir.toString());

                String ext;
                AWTImageExport.Type imageType;
                if (fc.getFileFilter() == jpgFilter) {
                    ext = ".jpg";
                    imageType = AWTImageExport.Type.JPEG;
                }
                else if (fc.getFileFilter() == gifFilter) {
                    ext = "";
                    // Program adds extension
                    imageType = AWTImageExport.Type.GIF;
                }
                else if (fc.getFileFilter() == pictFilter) {
                    ext = ".pict";
                    imageType = AWTImageExport.Type.PICT;
                }
                else if (fc.getFileFilter() == tiffFilter) {
                    ext = ".tif";
                    imageType = AWTImageExport.Type.TIFF;
                }
                else if (fc.getFileFilter() == targaFilter) {
                    ext = ".tga";
                    imageType = AWTImageExport.Type.TARGA;
                }
                else if (fc.getFileFilter() == bmpFilter) {
                    ext = ".bmp";
                    imageType = AWTImageExport.Type.BMP;
                }
                else {
                    ext = ".png";
                    imageType = AWTImageExport.Type.PNG;
                }

                File outFile;
                boolean yesToAll = false;









                final File finalDir = dir;
                final String finalExt = ext;
                final AWTImageExport.Type finalImageType = imageType;

                frameHandler = new FrameHandler() {
                    private boolean isProcessCanceled = false;
                    private int choice;
                    private boolean yesToAll = false;

                    @Override
                    public void processFrame(BufferedImage frameImage, String displayName, String saveName) {
                        System.out.println("SAVING: " + saveName);
                        File outFile = new File(finalDir + "/" + saveName);
                        // Check for existing file
                        if (new File(outFile.toString() + finalExt).exists() && (!yesToAll)) {
                            if (yesToAll) {
                                choice = 0;
                            }
                            else {
                                String message = "The Image file \n" +
                                "<html><font color=red>" + outFile + "</font></html>\n" +
                                "already exists.\n\n" +
                                "Do you want to proceed and OVERWRITE?";

                                Object[] options = {"YES", "NO", "YES TO ALL", "CANCEL"};
                                choice = JOptionPane.showOptionDialog(progressFrame, message, "OVERWRITE IMAGE FILE",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                        null, options, options[0]);
                            }
                        }

                        if (choice == 2) {
                            yesToAll = true;
                        }

                        // YES or YES TO ALL
                        if (choice == 0 || choice == 2) {
                            AWTImageExport.saveImage(frameImage, outFile, finalImageType);
                            
                            // do auto min/max the first time, if selected, then don't for every other image
                            viewer.getMapSelector().setGridAutoMinMax(false);
                        }
                        else if (choice == 3) {
                            // CANCEL
                            cancel();
                            endThread();
                        }


                        updateMemoryProgressBar();
                        if (memBar.getValue() > 90) {
                            endThread = true;
                            String string = "CANCELING ANIMATION\n-- System Memory Maximum Reached --\n";
                            JOptionPane.showMessageDialog(progressFrame, string,
                                    "MEMORY LIMIT", JOptionPane.ERROR_MESSAGE);
                        }

                        System.gc();

                    }

                    @Override
                    public void cancel() {
//                        System.out.println("cancel() called");
                        isProcessCanceled = true;
                    }
                    public boolean isCanceled() {
//                        System.out.println("isCanceled(): "+isProcessCanceled);
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


                progressBar.setString("0 % (0/"+urlsToLoad.length+") Animation Progress");            
                progressFrame.setTitle("Animation Progress");


                AnimationUtils.frameProcess(new FrameHandler[] { frameHandler }, individualProgressListener, progressListener, viewer, urlsToLoad);


                progressBar.setString("Finalizing...");
                progressBar.setValue(100);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


            processTime = (long)((System.currentTimeMillis() - processTime)*.001) + 1;
            long processTimeMin = processTime / 60;
            long processTimeSec = processTime % 60;
            String message = "Export Processing Time: "+processTimeMin + " min "+
            processTimeSec+" sec ("+processTime+"s)\n";

            JOptionPane.showMessageDialog(progressFrame, message,
                    "ANIMATION EXPORT COMPLETE", JOptionPane.INFORMATION_MESSAGE);

            System.gc();
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


        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(progressFrame, e.toString(),
                    "GENERAL EXCEPTION", JOptionPane.ERROR_MESSAGE);
        }

        viewer.removeRenderCompleteListener(renderCompleteListener); 

        viewer.setIsLoading(false);

    }

    // END METHOD run()


    /**
     *  Description of the Method
     */
    public void endThread() {
        endThread = true;
        frameHandler.cancel();
    }

    private void updateMemoryProgressBar() {
        Runtime r = Runtime.getRuntime();
        memBar.setValue((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0));
        memBar.setString("Memory Usage: "+ ((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0)) + " %");
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cancelButton) {
            frameHandler.cancel();
            endThread = true;
            progressBar.setString("--- Canceling ---");
        }
    }




}

