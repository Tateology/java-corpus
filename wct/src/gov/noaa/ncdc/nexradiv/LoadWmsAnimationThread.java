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

package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.wct.ui.WCTFrame;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.WmsResource;
import gov.noaa.ncdc.wct.ui.animation.AnimationFrame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.geotools.renderer.j2d.LegendPosition;
import org.geotools.renderer.j2d.RenderedLogo;

public class LoadWmsAnimationThread extends Thread implements ActionListener
{

    private WCTViewer viewer;
    private AbstractButton jButton;
    private JFrame progressFrame;
    private JProgressBar progressBar, individualBar, memBar;
    private boolean hideViewer;
    private Dimension dimension;
    private JButton cancelButton;
    
    
    private boolean endThread = false;

    private AnimationFrame animationFrame;
    private WmsResource[] wmsResourceArray;
    
    private int alpha;
    private Color emptyBackgroundColor;
    

    /**
     * Constructor
     * @param nexview            NexradIAViewer Object
     * @param animationFrame     The UI that holds the frames and manages animation
     * @param wmsResourceArray   The WmsResources to load
     * @param jButton            The button used to initiate the thread
     * @param hideViewer         Hides the viewer during processing
     */
    public LoadWmsAnimationThread(WCTViewer nexview, AnimationFrame animationFrame, 
            WmsResource[] wmsResourceArray, AbstractButton jButton, boolean hideViewer) {

        this(nexview, animationFrame, wmsResourceArray, jButton, hideViewer, null);  
    }
    /**
     * Constructor
     * @param nexview            NexradIAViewer Object
     * @param animationFrame     The UI that holds the frames and manages animation
     * @param wmsResourceArray   The WmsResources to load
     * @param jButton            The button used to initiate the thread
     * @param hideViewer         Hides the viewer during processing
     */
    public LoadWmsAnimationThread(WCTViewer nexview, AnimationFrame animationFrame, 
            WmsResource[] wmsResourceArray, AbstractButton jButton, 
            boolean hideViewer, Dimension dimension) {

        this.viewer = nexview;
        this.wmsResourceArray = wmsResourceArray;
        this.jButton = jButton;
        this.hideViewer = hideViewer;
        this.dimension = dimension;
        this.animationFrame = animationFrame;
    }


    /**
     * Method that does the work
     */
    public void run() {
        
        
        
        // Hold onto the viewer extent before resizing
        //java.awt.geom.Rectangle2D.Double gr = nexview.getCurrentExtent();

        System.out.println("Starting the LoadAnimationThread");

//        jButton.setEnabled(false);            


        if (hideViewer) {
            viewer.setVisible(false);
        }

        dimension = null;
        if (dimension != null) {
            viewer.setSize(dimension);
        }

        // Create progress frame
        progressFrame = new WCTFrame("Animation Progress");
        individualBar = new JProgressBar();
        individualBar.setIndeterminate(false);
        individualBar.setStringPainted(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setString("0 %");
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

        progressFrame.getContentPane().setLayout(new GridLayout(5,1));
        progressFrame.getContentPane().add(new JLabel("Animation Processing Progress", JLabel.CENTER));
        progressFrame.getContentPane().add(individualBar);
        progressFrame.getContentPane().add(progressBar);
        progressFrame.getContentPane().add(memBar);
        progressFrame.getContentPane().add(cancelPanel);

        //progressFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //progressFrame.setBackground(new Color(159, 182, 205));
        progressFrame.setBounds(5, 5, 250, 180);

        //progressFrame.pack();
        progressFrame.setVisible(true);

        Runtime r = Runtime.getRuntime();

        animationFrame.clearImages();
        System.gc();

        try {

            boolean firstTime = true;
            
            String fmtStr = "0";
            if (wmsResourceArray.length >= 10) { fmtStr = "00"; }
            if (wmsResourceArray.length >= 100) { fmtStr = "000"; }
            if (wmsResourceArray.length >= 1000) { fmtStr = "0000"; }
            
            DecimalFormat fmt = new DecimalFormat(fmtStr);
            
            for (int i=0; i<wmsResourceArray.length; i++) {
                memBar.setValue((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0));
                memBar.setString("Memory Usage: "+ ((int)(((double)(r.totalMemory() - r.freeMemory()) / (double)r.maxMemory())*100.0)) + " %");

                RenderedLogo legend = new RenderedLogo(wmsResourceArray[i].getLegendImage());
                legend.setZOrder(400.2f);
                legend.setPosition(LegendPosition.NORTH_EAST);
                legend.setInsets(new Insets(115, 0, 0, 220));

                RenderedLogo logo = new RenderedLogo(wmsResourceArray[i].getResourceLogo());
                logo.setZOrder(500.2f);
                logo.setPosition(LegendPosition.SOUTH_EAST);
                logo.setInsets(new Insets(0, 0, 18, 50));

                viewer.displayWMS(wmsResourceArray[i].getName(), 
                        wmsResourceArray[i].getWmsUrl(viewer.getCurrentExtent()), 
                        viewer.getCurrentExtent(), 
                        wmsResourceArray[i].getZIndex(),
                        alpha, emptyBackgroundColor,                        
                        legend, logo);
                

                int percent = (int)(100*(((double)i+1)/wmsResourceArray.length));
                progressBar.setValue(percent);
                progressBar.setString(percent+" %");
                progressFrame.setTitle("("+percent+" %) Animation Progress");
                String title = wmsResourceArray[i].getName();

                animationFrame.addImage(viewer.getViewerBufferedImage(), title+"_"+fmt.format(i));

                
                if (memBar.getValue() > 90) {
                    endThread = true;
                    String string = "CANCELING ANIMATION\n-- System Memory Maximum Reached --\n"+i+" Frames Created";
                    JOptionPane.showMessageDialog(progressFrame, string,
                            "MEMORY LIMIT", JOptionPane.ERROR_MESSAGE);
                }


                System.gc();
                // End loop if needed
                if (endThread) {
                    i = wmsResourceArray.length;
                    progressFrame.setTitle("Animation Progress");
                }
                
                // repeat first one
                if (firstTime) {
                    firstTime = false;
                    animationFrame.clearImages();
                    i--;
                }
            }
            progressFrame.setTitle("Animation Progress");

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(progressFrame, e.toString(),
                    "ANIMATION ERROR", JOptionPane.ERROR_MESSAGE);

            progressFrame.dispose();
            jButton.setEnabled(true);
            viewer.setVisible(true);

            progressFrame.setTitle("Animation Progress");
        }

        try {
            
//            viewer.removeWMS(wmsResourceArray[0].getName());

            viewer.displayWMS(wmsResourceArray[0].getName(), 
                    wmsResourceArray[0].getWmsUrl(viewer.getCurrentExtent()), 
                    viewer.getCurrentExtent(), 
                    wmsResourceArray[0].getZIndex());
          
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(progressFrame, e.toString(),
                    "ANIMATION ERROR", JOptionPane.ERROR_MESSAGE);
        }


        memBar.setValue((int)(((double)r.freeMemory() / (double)r.maxMemory())*100.0));
        memBar.setString("Memory Usage: "+ ((int)(((double)r.freeMemory() / (double)r.maxMemory())*100.0)) + " %");
        System.out.println("Memory Usage: "+ r.freeMemory() +" / "+ r.maxMemory() );



        progressFrame.dispose();
        viewer.updateMemoryLabel();

        viewer.setVisible(true);
        
        
        animationFrame.setAnimationIcon(0);
        animationFrame.validate();
        animationFrame.pack();
        animationFrame.setVisible(true);      

        animationFrame.setSize(viewer.getSize());
        animationFrame.pack();
        
        animationFrame.toFront();
        animationFrame.requestFocus();
        animationFrame.startAnimation();

        jButton.setEnabled(true);

        
        
        System.out.println("FINISHED LOADING THREAD");

        // Set stuff to null because we're done!
        System.gc();

    } // END METHOD run()

    public void endThread() {
        endThread = true;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cancelButton) {
            endThread = true;
            progressBar.setString("--- Canceling ---");
        }
    }
    public int getAlpha() {
        return alpha;
    }
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
    public Color getEmptyBackgroundColor() {
        return emptyBackgroundColor;
    }
    public void setEmptyBackgroundColor(Color emptyBackgroundColor) {
        this.emptyBackgroundColor = emptyBackgroundColor;
    }
    
    


} // END CLASS
