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
import gov.noaa.ncdc.common.SwingWorker;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.ui.AWTImageExport;
import gov.noaa.ncdc.wct.ui.AWTImageExport.FileAndFormat;
import gov.noaa.ncdc.wct.ui.WCTFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class AnimationFrame extends WCTFrame  
implements Animator, ActionListener, ChangeListener//, WindowListener
{
    private static final long serialVersionUID = 6869298774299142228L;
    
    public static final int MAX_FRAMES_IN_MEMORY = 840;
    
    private JPanel controlPanel, animatePanel;
    private JButton jbExport, jbStartStop, jbPrevious, jbNext, jbSaveAll;
    private JSlider jsSpeed;
    private JCheckBox jcbRock, jcbReverse;
    private JLabel jlIcon, jlIconTitle;
    private AnimationThread animateThread;
    private int counter=0;  
    String sp = "                              ";

    Color bcolor = new Color(180, 180, 180);

    private Vector<String> iconTitles = new Vector<String>();

    private final static CacheManager singletonManager = CacheManager.create();
    private Cache animationFrameCache = null;
    
    private KeyListener frameKeyListener = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) {
//            System.out.println("key pressed code="+e.getKeyCode());
            if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_UP) {
                nextFrame();
            }
            else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_DOWN) {
                previousFrame();
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
//            System.out.println("key released code="+e.getKeyCode());
        }
        @Override
        public void keyTyped(KeyEvent e) {
//            System.out.println("key typed code="+e.getKeyCode());
        }
        
    };
    
    
    public AnimationFrame() {
        super("Animation Window");
        createGUI();

        if (! singletonManager.cacheExists("animationFrameCache")) {
            animationFrameCache = 
                new Cache("animationFrameCache",
                          MAX_FRAMES_IN_MEMORY, 
                          MemoryStoreEvictionPolicy.LRU,
                          true,
                          WCTConstants.DEFAULT_DATA_CACHE_LOCATION,
                          false,
                          60*60*24,
                          60*60*24,
                          false,
                          60*60*24,
                          null
                          );
            singletonManager.addCache(animationFrameCache);
        }
        else {
        	animationFrameCache = singletonManager.getCache("animationFrameCache");
        }

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (animateThread != null) {
                    animateThread.endThread();
                }
                clearImages();
                System.gc();
            }
        });

    }

    public AnimationFrame(final Vector<BufferedImage> iconList, 
    		final Vector<String> iconTitles) throws IOException {
    	
    	
        super("Animation Window");
        createGUI();

        if (! singletonManager.cacheExists("animationFrameCache")) {
            animationFrameCache = 
                new Cache("animationFrameCache",
                		  MAX_FRAMES_IN_MEMORY,
                          MemoryStoreEvictionPolicy.LRU,
                          true,
                          WCTConstants.DEFAULT_DATA_CACHE_LOCATION,
                          false,
                          60*60*24,
                          60*60*24,
                          false,
                          60*60*24,
                          null
                          );
            singletonManager.addCache(animationFrameCache);
        }
        else {
        	animationFrameCache = singletonManager.getCache("animationFrameCache");
        }

        
        setData(iconList, iconTitles);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (animateThread != null) {
                    animateThread.endThread();
                }
                clearImages();
                System.gc();
            }
        });

    } 


    
    
    public void setData(Vector<BufferedImage> imageList, Vector<String> iconTitles) 
    	throws IOException {
    	
    	clearImages();
    	
        for (int n=0; n<imageList.size(); n++) {
        	addImage(imageList.elementAt(n), iconTitles.elementAt(n));
        }
        
        
        jlIcon.setIcon(new ImageIcon(imageList.elementAt(0)));
        startAnimation();
    }

    
    
    

    
    
    public void clearImages() {
        System.out.println("CLEARING IMAGES");
        
        animationFrameCache.removeAll();
        iconTitles.clear();
    }

    
    
    
    
    public void addImage(BufferedImage image, String title) throws IOException {
        
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ImageIO.write(image, "png", baos);
    	
    	
        animationFrameCache.put(new Element(title, baos.toByteArray()));
        
        iconTitles.add(title);
        
    }






    private void createGUI() {

        controlPanel = new JPanel();
        jbSaveAll =     new JButton("Save All");
        jbSaveAll.setForeground(Color.blue);
        jbSaveAll.addActionListener(this);
        jbSaveAll.addKeyListener(frameKeyListener);
        jbExport =     new JButton("Export Movie");
        jbExport.setForeground(Color.blue);
        jbExport.addActionListener(this);
        jbExport.addKeyListener(frameKeyListener);
        jbStartStop =     new JButton("  Stop  ");
        jbStartStop.setForeground(Color.red);
        jbStartStop.addActionListener(this);
        jbStartStop.addKeyListener(frameKeyListener);
        jbPrevious = new JButton("Previous");
        jbPrevious.setForeground(Color.blue);
        jbPrevious.addActionListener(this);
        jbPrevious.addKeyListener(frameKeyListener);
        jbNext =     new JButton("  Next  ");
        jbNext.setForeground(Color.blue);
        jbNext.addActionListener(this);
        jbNext.addKeyListener(frameKeyListener);
        jsSpeed = new JSlider(50, 1100);
        jsSpeed.addChangeListener(this);
//        jsSpeed.addKeyListener(frameKeyListener);
        TitledBorder speedTitle = BorderFactory.createTitledBorder("Slower  <   Speed   >  Faster");
        speedTitle.setTitleJustification(TitledBorder.CENTER);
        jsSpeed.setBorder(speedTitle);
        jcbRock = new JCheckBox("Rock");
        jcbRock.addActionListener(this);
        jcbRock.addKeyListener(frameKeyListener);
        jcbReverse = new JCheckBox("Reverse");
        jcbReverse.addActionListener(this);
        jcbReverse.addKeyListener(frameKeyListener);
        controlPanel.add(jbSaveAll);
        controlPanel.add(jbExport);
        controlPanel.add(jbPrevious);
        controlPanel.add(jbStartStop);
        controlPanel.add(jbNext);
        controlPanel.add(jsSpeed);
        controlPanel.add(jcbRock);
        controlPanel.add(jcbReverse);

        jlIcon = new JLabel("",JLabel.CENTER);
        jlIconTitle = new JLabel("Animation Frame 0", JLabel.CENTER);
        jlIcon.addKeyListener(frameKeyListener);
        jlIconTitle.addKeyListener(frameKeyListener);


        animatePanel = new JPanel();
        animatePanel.setLayout(new BorderLayout());
        animatePanel.add(new JScrollPane(controlPanel), "South");
        JScrollPane iconScrollPane = new JScrollPane(jlIcon);
        iconScrollPane.getViewport().setBackground(bcolor);
        animatePanel.add(iconScrollPane, "Center");
        animatePanel.add(jlIconTitle, "North");

        animatePanel.setBackground(bcolor);
        controlPanel.setBackground(bcolor);
        jsSpeed.setBackground(bcolor);
        jcbRock.setBackground(bcolor);
        jcbReverse.setBackground(bcolor);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(animatePanel, "Center");

        controlPanel.addKeyListener(frameKeyListener);
        iconScrollPane.addKeyListener(frameKeyListener);
        animatePanel.addKeyListener(frameKeyListener);
        getContentPane().addKeyListener(frameKeyListener);
        
    } // END METHOD createGUI()



    public void startAnimation() {
        counter=0;
        if (animateThread != null) animateThread.endThread(); // end a thread if one is going


        if (iconTitles.size() > 1) {
            try {
                animateThread = new AnimationThread(this, iconTitles.size(), counter, 
                		jcbRock.isSelected(), (jsSpeed.getMaximum()-jsSpeed.getValue()+5));
                animateThread.start();

                jbStartStop.setEnabled(true);
                jbPrevious.setEnabled(true);
                jbNext.setEnabled(true);
                jbSaveAll.setEnabled(true);
                jsSpeed.setEnabled(true);
                jcbRock.setEnabled(true);

                jbStartStop.setText("  Stop  ");
                jbStartStop.setForeground(Color.red);
                jbExport.setText("Export Movie");
                jbExport.setForeground(Color.blue);

                jbStartStop.setEnabled(true);
            }
            catch (Exception e) {}
        }
        else {
            try {

                setAnimationIcon(0);               

                jbStartStop.setEnabled(false);
                jbPrevious.setEnabled(false);
                jbNext.setEnabled(false);
                jbSaveAll.setEnabled(false);
                jsSpeed.setEnabled(false);
                jcbRock.setEnabled(false);
                jcbReverse.setEnabled(false);
                jbExport.setText("Save Image");
                jbExport.setForeground(Color.blue);

            }
            catch (Exception e) {}
        }

    } 


    private ImageIcon createImageIcon(int index) {
    	return new ImageIcon(
    			(byte[]) (animationFrameCache.get(
    					iconTitles.elementAt(index)).getObjectValue())
    	);
    }

    
    

    // Sets the image to display
    public void setAnimationIcon(final int index) {
    	jlIcon.setIcon(createImageIcon(index));
        
        jlIconTitle.setText("Animation Frame "+index);
        counter = index;

    }

    
    
    
    
    
    
    public void previousFrame() {
        counter--;
        if (counter < 0) counter = iconTitles.size()-1;
        jlIcon.setIcon(createImageIcon(counter));
        jlIconTitle.setText("Animation Frame "+counter);
    }
    
    public void nextFrame() {
        counter++;
        if (counter == iconTitles.size()) {
            counter = 0;
        }
        jlIcon.setIcon(createImageIcon(counter));
        jlIconTitle.setText("Animation Frame "+counter);
    }
    
    
    // Implementation of ActionListener interface.
    public void actionPerformed(ActionEvent event) {

        Object source = event.getSource();
        // Stop or Start the tread
        if (source == jbStartStop) {
            if (jbStartStop.getText().equals("  Stop  ")) { // Stop
                if (animateThread != null) { 
                    animateThread.endThread(); // end a thread if one is going
                }
                jbStartStop.setForeground(new Color(0, 139, 0));
                jbStartStop.setText("  Play  ");
                jbExport.setText("Save Image");
                jbExport.setForeground(Color.blue);
            }
            else { // Start
                animateThread.endThread(); // end a thread if one is going
                animateThread = new AnimationThread(this, iconTitles.size(), counter, jcbRock.isSelected(), calculateDelayBetweenFrames());
                animateThread.start();
                jbStartStop.setForeground(Color.red);
                jbStartStop.setText("  Stop  ");
                jbExport.setText("Export Movie");
                jbExport.setForeground(Color.blue);
            }
        }
        // Previous Image
        else if (source == jbPrevious) {
            previousFrame();
        }
        // Next Image
        else if (source == jbNext) {
            nextFrame();
        }
        // Rock images?
        else if (source == jcbRock) {
            animateThread.setRock(jcbRock.isSelected());
        }
        // Reverse image order
        else if (source == jcbReverse) {
//            animateThread.setRock(jcbRock.isSelected());
        	Collections.reverse(iconTitles);
        }
        // Save a single image or all images using a file name template
        else if (source == jbSaveAll) {
            animateThread.endThread(); // end a thread if one is going
            jbStartStop.setForeground(new Color(0, 139, 0));
            jbStartStop.setText("  Play  ");
            jbExport.setText("Save Image");
            jbExport.setForeground(Color.blue);

            saveAllImages();

        }
        // Save the animation as a movie file
        else if (source == jbExport) {
            if (jbExport.getText().equals("Export Movie")) {

            	exportMovie();
            }
            else {
                Image[] imageArray = new Image[1];
                imageArray[0] = createImageIcon(counter).getImage();

                AWTImageExport imgExport = new AWTImageExport(imageArray);
                imgExport.exportAWTChooser(this);
            }
        }

    } // actionPerformed
    
    
    
    
    
    
    
    
    private void exportMovie() {
    	
        final Component finalThis = this;


        animateThread.endThread(); // end a thread if one is going
        jbStartStop.setForeground(new Color(0, 139, 0));
        jbStartStop.setText("  Play  ");
        jbExport.setText("Save Image");
        jbExport.setForeground(Color.blue);

        
        Iterator<Image> imageIterator = null;
        if (jcbRock.isSelected()) {
            imageIterator = new Iterator<Image>() {
                int n=0;
                @Override
                public boolean hasNext() {
                    return n<iconTitles.size()*2;
                }
                @Override
                public Image next() {
                	int index;
                    if (n<iconTitles.size()) {
                        index = n++;
                    }
                    else {
                        index = 2*iconTitles.size()-(n++)-1;
                    }
                    return createImageIcon(index).getImage();

                }
                @Override
                public void remove() {
//                    throw new Exception("remove() operation is not supported.");
                }
            };

        }
        else {
            imageIterator = new Iterator<Image>() {
                int n=0;
                @Override
                public boolean hasNext() {
                    return n<iconTitles.size();
                }
                @Override
                public Image next() {
                    return createImageIcon(n++).getImage();
                }
                @Override
                public void remove() {
//                    throw new Exception("remove() operation is not supported.");
                }
            };
        }


        int fCount = iconTitles.size();
        if (jcbRock.isSelected()) {
            fCount = iconTitles.size()*2;
        }
        final int frameCount = fCount;
        final AWTImageExport imgExport = new AWTImageExport(imageIterator, frameCount);

        final JFrame finalAnimationFrame = this;
        SwingWorker worker = new SwingWorker() {
        	public Object construct() {
        		JDialog progressDialog = new JDialog(finalAnimationFrame, "Export Progress", false);
        		JProgressBar progressBar = new JProgressBar();
        		progressBar.setMaximum(frameCount);
        		progressDialog.getContentPane().add(progressBar);
        		progressDialog.setBounds(10, 10, 300, 50);
        		progressDialog.setVisible(true);
        		FileAndFormat faf = imgExport.saveAsMovie(finalThis, calculateDelayBetweenFrames(), progressBar);
        		
        		if (faf == null) {
            		progressDialog.dispose();
        			return "CANCEL";
        		}
        		
        		
            	int openChoice = JOptionPane.showOptionDialog(progressDialog, "Open: '"+faf.getFile()+"'?",
                        "Movie Export Complete", JOptionPane.YES_NO_OPTION, 
                        JOptionPane.INFORMATION_MESSAGE, null, null, null);

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

               	if (openChoice == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().browse(faf.getFile().toURI());
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(progressDialog, "Open Error: "+e.getMessage()+"\nThe parent folder will now open...",
                                "WCT EXCEPTION", JOptionPane.ERROR_MESSAGE);
                        try {
                            Desktop.getDesktop().open(faf.getFile().getParentFile());
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            JOptionPane.showMessageDialog(progressDialog, "Open Error: "+e2.getMessage(),
                                    "WCT EXCEPTION", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

        		
        		try {
        			Thread.sleep(1000);
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        		progressDialog.dispose();
        		return "Done";
        	}
        };
        worker.start();

    }
    
    
    
    
    
    
    public void stateChanged(ChangeEvent event) {
        try {
            animateThread.setWaitTime(calculateDelayBetweenFrames()); 
        }
        catch (Exception e) {}
    } // stateChanged

    
    
    private int calculateDelayBetweenFrames() {
    	return (jsSpeed.getMaximum() - jsSpeed.getValue() + 5);
    }
    
    
    private void saveAllImages() {
        
        final JFrame parent = this;

        SwingWorker worker = new SwingWorker() {
            public Object construct() {

                // Set up File Chooser
                String startPath = WCTProperties.getWCTProperty("imgsavedir");
                JFileChooser fc = new JFileChooser(startPath);
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

                int returnVal = fc.showSaveDialog(parent);
                File dir = null;
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    int choice = JOptionPane.YES_OPTION;
                    // intialize to YES!
                    dir = fc.getSelectedFile();               
                    WCTProperties.setWCTProperty("imgsavedir", dir.toString());

                    
                    String ext;
                    AWTImageExport.Type imageType;
                    if (fc.getFileFilter() == jpgFilter) {
                        ext = ".jpg";
                        imageType = AWTImageExport.Type.JPEG;
                    }
                    else if (fc.getFileFilter() == gifFilter) {
                        ext = ""; // Program adds extension
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

                    // Set up progress bar 
                    JFrame frame = new WCTFrame("Export Progress");
                    JProgressBar progressBar = new JProgressBar();
                    // set up progressbar
                    progressBar.setMinimum(0);
                    progressBar.setMaximum(iconTitles.size());
                    progressBar.setStringPainted(true);
                    progressBar.setString("Saving Image 0 of "+iconTitles.size());
                    progressBar.setValue(0);
                    frame.getContentPane().add(progressBar);
                    frame.setBounds(10, 10, 300, 50);
                    frame.setAlwaysOnTop(true);
                    frame.setVisible(true);

                    for (int n=0; n<iconTitles.size(); n++) {
                        // 01234567890123456789012
                        // KMLB NHL 20030603 15:04
                        String title = iconTitles.get(n);

                        String saveName = title.replaceAll(" ", "_").replaceAll(":", "");

                        outFile = new File(dir+"/"+saveName);
                        // Check for existing file
                        if (new File(outFile.toString()+ext).exists() && (! yesToAll)) {
                            if (yesToAll)
                                choice = 0;
                            else {
                                String message = "The Image file \n" +
                                "<html><font color=red>" + outFile + "</font></html>\n" +
                                "already exists.\n\n" +
                                "Do you want to proceed and OVERWRITE?";

                                Object[] options = { "YES", "NO", "YES TO ALL", "CANCEL" };
                                choice = JOptionPane.showOptionDialog(null, message, "OVERWRITE IMAGE FILE", 
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                        null, options, options[0]);
                            }
                        }

                        if (choice == 2) {
                            yesToAll = true;
                        }


                        // YES or YES TO ALL
                        if (choice == 0 || choice == 2) { 
                            progressBar.setString("Adding Frame " + (n+1) + " of " + iconTitles.size());
                            progressBar.setValue(n);                            
                            AWTImageExport.saveImage(
                            		createImageIcon(n).getImage(), 
                                    outFile, imageType
                                );
                        }
                        else if (choice == 3) // CANCEL
                            n = iconTitles.size();
                        // otherwise do nothing (for NO)   
                    }
                    progressBar.setString("Finalizing...");
                    progressBar.setValue(iconTitles.size());
                    
                	int openChoice = JOptionPane.showOptionDialog(frame, "Show Folder: '"+dir+"'?",
                            "Image Export Complete", JOptionPane.YES_NO_OPTION, 
                            JOptionPane.INFORMATION_MESSAGE, null, null, null);

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                   	if (openChoice == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(dir);
                        } catch (IOException e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(frame, "Open Error: "+e.getMessage(),
                                    "WCT EXCEPTION", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    frame.dispose();

                    

                    
                }

                return "Done";
            }
        };
        worker.start();
    }

    
} // END CLASS
