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

package gov.noaa.ncdc.wct.ui;

import fi.faidon.jis.BMPImageSaver;
import fi.faidon.jis.ImageSaverObject;
import fi.faidon.jis.PICTImageSaver;
import fi.faidon.jis.TIFFImageSaver;
import fi.faidon.jis.TargaImageSaver;
import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.ui.animation.AVIWriter;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import ch.randelshofer.media.avi.AVIOutputStream;
import ch.randelshofer.media.avi.AVIOutputStream.VideoFormat;
import ch.randelshofer.media.quicktime.QuickTimeOutputStream;

import com.madgag.gif.fmsware.AnimatedGifEncoder;


/**
 *  Provides dialogs and methods for saving Java AWT Images or Buffered Images in
 *  various image formats using external libraries.
 *
 *  Packages needed:
 *  <table border="1"><tr><td>QUICKTIME, GIF</td><td>org.opensourcephysics.*</td></tr>
 *  <!--<tr><td>GIF</td><td>com.fmsware.*</td></tr>-->
 *  <tr><td>BMP,TIFF,PICT,TARGA,JPEG</td><td>fi.faiodn.jis.*</td></tr>
 *  </table>
 *
 * @author     Steve.Ansari
 * @created    April 14, 2004
 */
public class AWTImageExport {


    public enum Type { JPEG, PNG, GIF, PICT, TIFF, BMP, TARGA, AVI, ANIMATED_GIF, 
    	AVI_JPEG, AVI_PNG, AVI_RAW, AVI_RLE8, QUICKTIME };

    private Image[] awtImages = null;
    private Iterator<Image> imageIterator = null;
    private int frameCount = -1;
    
    
    public AWTImageExport(Iterator<java.awt.Image> imageIterator, int frameCount) {
        this.imageIterator = imageIterator;
        this.frameCount = frameCount;
    }
    

    /**
     * Constructor
     *
     * @param  awtImage  Description of the Parameter
     */
    public AWTImageExport(java.awt.Image awtImage) {
        this( new Image[] { awtImage } );
    }


    /**
     * Constructor
     *
     * @param  awtImages  Description of the Parameter
     */
    public AWTImageExport(java.awt.Image[] awtImages) {
        this.awtImages = awtImages;
        this.frameCount = awtImages.length;
    }

    /**
     * Save an Image using JFileChooser.
     */
    public File exportAWTChooser(Component parent) {
        String startPath = WCTProperties.getWCTProperty("imgsavedir");
        return exportAWTChooser(parent, startPath);
    }

    /**
     * Save an Image using JFileChooser given a initial directory path.
     */
    public File exportAWTChooser(Component parent, String startPath) {

        File file = null;
        // Loop through awtImages Array
        for (int i = 0; i < frameCount; i++) {

            // Set up File Chooser
            JFileChooser fc = new JFileChooser(startPath);
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

            int returnVal = fc.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                int choice = JOptionPane.YES_OPTION;
                // intialize to YES!
                file = fc.getSelectedFile();
                File extfile = null;
                String fstr = file.toString();
                if (fc.getFileFilter() == jpgFilter) {
                    // Add extension if needed
                    if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".jpg")) {
                        extfile = new File(file + ".jpg");
                    }
                    else {
                        extfile = file;
                        file = new File(fstr.substring(0, (int) fstr.length() - 4));
                    }
                }
                else if (fc.getFileFilter() == gifFilter) {
                    // Add extension if needed
                    if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".gif")) {
                        extfile = new File(file + ".gif");
                    }
                    else {
                        extfile = file;
                        file = new File(fstr.substring(0, (int) fstr.length() - 4));
                    }
                }
                else if (fc.getFileFilter() == pictFilter) {
                    // Add extension if needed
                    if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".pict")) {
                        extfile = new File(file + ".pict");
                    }
                    else {
                        extfile = file;
                        file = new File(fstr.substring(0, (int) fstr.length() - 4));
                    }
                }
                else if (fc.getFileFilter() == tiffFilter) {
                    // Add extension if needed
                    if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".tiff")) {
                        extfile = new File(file + ".tiff");
                    }
                    else {
                        extfile = file;
                        file = new File(fstr.substring(0, (int) fstr.length() - 4));
                    }
                }
                else if (fc.getFileFilter() == bmpFilter) {
                    // Add extension if needed
                    if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".bmp")) {
                        extfile = new File(file + ".bmp");
                    }
                    else {
                        extfile = file;
                        file = new File(fstr.substring(0, (int) fstr.length() - 4));
                    }
                }
                else if (fc.getFileFilter() == targaFilter) {
                    // Add extension if needed
                    if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".tga")) {
                        extfile = new File(file + ".tga");
                    }
                    else {
                        extfile = file;
                        file = new File(fstr.substring(0, (int) fstr.length() - 4));
                    }
                }
                else {
                    // Add extension if needed
                    if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".png")) {
                        extfile = new File(file + ".png");
                    }
                    else {
                        extfile = file;
                        file = new File(fstr.substring(0, (int) fstr.length() - 4));
                    }
                }


                // Check for existing file
                if (file.exists()) {
                    String message = "The Image file \n" +
                    "<html><font color=red>" + file + "</font></html>\n" +
                    "already exists.\n\n" +
                    "Do you want to proceed and OVERWRITE?";
                    choice = JOptionPane.showConfirmDialog(null, (Object) message,
                            "OVERWRITE IMAGE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    fstr = file.toString();
                    file = new File(fstr.substring(0, (int) fstr.length() - 4));
                }
                // END f(file.exists())
                // Check for existing file without extension
                else if (extfile.exists()) {
                    String message = "The Image file \n" +
                    "<html><font color=red>" + extfile + "</font></html>\n" +
                    "already exists.\n\n" +
                    "Do you want to proceed and OVERWRITE?";
                    choice = JOptionPane.showConfirmDialog(null, (Object) message,
                            "OVERWRITE IMAGE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                }
                // END else if(extfile.exists())
                if (choice == JOptionPane.YES_OPTION) {
                    // Save the image
                    if (fc.getFileFilter() == jpgFilter) {
                        exportImage(file, i, Type.JPEG);
                    }
                    else if (fc.getFileFilter() == gifFilter) {
                        exportImage(file, i, Type.GIF);
                    }
                    else if (fc.getFileFilter() == pictFilter) {
                        exportImage(file, i, Type.PICT);
                    }
                    else if (fc.getFileFilter() == tiffFilter) {
                        exportImage(file, i, Type.TIFF);
                    }
                    else if (fc.getFileFilter() == bmpFilter) {
                        exportImage(file, i, Type.BMP);
                    }
                    else if (fc.getFileFilter() == targaFilter) {
                        exportImage(file, i, Type.TARGA);
                    }
                    else {
                        exportImage(file, i, Type.PNG);
                    }
                }
                // END if(choice == YES_OPTION)
            }
            // END if(returnVal)
        }
        // END FOR LOOP
        if (file == null) {
            return new File(startPath);
        }
        else {
            return new File(file.getParent());
        }
    }

    // END METHOD saveViewAsJPEG


    /**
     * Save an Image given an output file and image type.
     *
     * @param  files    Description of the Parameter
     * @param  outType  Description of the Parameter
     * @return          Description of the Return Value
     */
    public boolean exportImage(File[] files, Type outType) {

        boolean status;
        if (files.length != frameCount) {
            System.out.println("File Array Size != Image Array Size");
            return false;
        }

        for (int i = 0; i < files.length; i++) {
            if (!saveImage(awtImages[i], files[i], outType)) {
                return false;
            }
        }
        // END FOR LOOP
        return true;
    }

    // END METHOD saveViewAsJPEG


    /**
     * Save an Image given an output file, array index and image type.
     *
     * @param  file     Description of the Parameter
     * @param  index    Description of the Parameter
     * @param  outType  Description of the Parameter
     * @return          Description of the Return Value
     */
    public boolean exportImage(File file, int index, Type outType) {

        return (saveImage(awtImages[index], file, outType));
    }

    // END METHOD saveViewAsJPEG

    
    private static String checkExtension(String filename, String extToTry) {
    	if (extToTry.startsWith(".")) {
    		extToTry = extToTry.substring(1);
    	}
    	return (filename.endsWith("."+extToTry) ? filename : filename+"."+extToTry);
    }

    /**
     * Save an AWT Image to Image file given an output file and type (JPEG or PNG).
     *
     * @param  outFile  Description of the Parameter
     * @param  outType  Description of the Parameter
     * @param  image    Description of the Parameter
     * @return          Description of the Return Value
     */

    public static boolean saveImage(java.awt.Image image, File outFile, Type outType) {
        try {
            if (! outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            
            // Save as PNG
            if (outType == Type.PNG) {
                ImageIO.write(AWTImageExport.toBufferedImage(image), "png", 
                		new File(checkExtension(outFile.toString(),".png")));
            }
            else if (outType == Type.PICT) {
                ImageSaverObject iso = new ImageSaverObject();
                iso.saveToFile(image, checkExtension(outFile.toString(),".pict"), PICTImageSaver.FORMAT_CODE);
            }
            else if (outType == Type.TIFF) {
                ImageSaverObject iso = new ImageSaverObject();
                iso.saveToFile(image, checkExtension(outFile.toString(),".tif"), TIFFImageSaver.FORMAT_CODE);
            }
            else if (outType == Type.BMP) {
                ImageSaverObject iso = new ImageSaverObject();
                iso.saveToFile(image, checkExtension(outFile.toString(),".bmp"), BMPImageSaver.FORMAT_CODE);
            }
            else if (outType == Type.TARGA) {
                ImageSaverObject iso = new ImageSaverObject();
                iso.saveToFile(image, checkExtension(outFile.toString(),".tga"), TargaImageSaver.FORMAT_CODE);
            }
            else if (outType == Type.GIF) {
                AnimatedGifEncoder e = new AnimatedGifEncoder();
                e.setQuality(1);
                e.start(checkExtension(outFile.toString(),".gif"));
                e.addFrame(AWTImageExport.toBufferedImage(image));
                e.finish();
            }
            else if (outType == Type.JPEG){
                ImageIO.write(AWTImageExport.toBufferedImage(image), "jpg", new File(outFile.toString() + ".jpg"));

//                try {
//                    BufferedImage bufimage = toBufferedImage(image);
//                    JPEGEncodeParam param =   JPEGCodec.getDefaultJPEGEncodeParam(bufimage);
//                    param.setQuality(.95f, false);
//                    FileOutputStream fout   =  new FileOutputStream (new File(outFile.toString() + ".jpg"));
//                    JPEGImageEncoder encoder =  JPEGCodec.createJPEGEncoder (fout);
//                    encoder.encode (bufimage, param);
//                    fout.close ();
//                } catch (Exception err) {
//                    System.err.println("Problem saving JPEG" + err);
//                    err.printStackTrace ();
//                }

            }
            else {
                throw new Exception("Invalid export type specified: "+outType);
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("CAUGHT IMAGE EXCEPTION " + e);
            e.printStackTrace();
            return false;
        }

    }

    // END METHOD saveJPEG


    public FileAndFormat saveAsMovie(Component parent, int frameRateInMillis) {
        return saveAsMovie(parent, frameRateInMillis, new JProgressBar());   
    }

    /**
     * Save array of AWT Images to AVI, QUICKTIME Movie or Animated GIF using JFileChooser.
     *
     * @param  frameRateInMillis  Frame rate of movie in milliseconds
     */
    public FileAndFormat saveAsMovie(Component parent, int frameRateInMillis, JProgressBar progressBar) {

        // Save the image
        FileAndFormat fileAndFormat = saveAsMovieDialog(parent);
        if (fileAndFormat != null) {
            saveAsMovie(fileAndFormat.getFormat(), fileAndFormat.getFile(), frameRateInMillis, progressBar);
        }
        return fileAndFormat;
    }

    // END saveAsMovie



    /**
     * Save array or iterator of AWT Images to AVI or Animated GIF from given filename.
     *
     * @param  outFile            The output filename
     * @param  delayBetweenFramesInMillis  Frame rate of movie in milliseconds
     * @param  progressBar          JProgressBar that tracks export progress
     */
    public void saveAsMovie(Type outFormat, File outFile, int delayBetweenFramesInMillis, JProgressBar progressBar) {

        // set up progressbar
        progressBar.setStringPainted(true);
        progressBar.setString("Adding Frame 0 of "+progressBar.getMaximum());
        progressBar.setValue(0);

        String outString = outFile.toString();
        String extension = outString.substring(outString.length()-4, outString.length());



        if (outFormat == Type.AVI) {

            try {

                // create a video recorder
                AVIWriter aviWriter = new AVIWriter();

                double frameRateInFPS = 1000.0/delayBetweenFramesInMillis;
                // 1. Get first frame so we know the image dimensions and init
                BufferedImage firstFrame = null;
                if (awtImages == null) {
                    firstFrame = toBufferedImage(imageIterator.next());
                }
                else {
                    firstFrame = toBufferedImage(awtImages[0]);
                }
                int width = firstFrame.getWidth(null);
                int height = firstFrame.getHeight(null);
                aviWriter.init(outFile, width, height, frameCount, frameRateInFPS);

                // 2. Write the first image frame
                aviWriter.addFrame(firstFrame);
                
                // 3. Write from 1...n frames
                for (int n=1; n<frameCount; n++) {
                    progressBar.setString("Adding Frame " + n + " of " + frameCount);
                    progressBar.setValue(n);
                    if (awtImages == null) {
                        aviWriter.addFrame(toBufferedImage(imageIterator.next()));
                    }
                    else {
                        aviWriter.addFrame(toBufferedImage(awtImages[n]));
                    }
                }
                progressBar.setString("Finalizing...");
                progressBar.setValue(frameCount);
                aviWriter.close();

            } catch (Exception e) {
                e.printStackTrace();
                String message = "Error Writing "+outFile+" : "+e;
                JOptionPane.showMessageDialog(null, message, "AVI EXPORT ERROR", JOptionPane.ERROR_MESSAGE);         
                return;
            }

        }
        else if (outFormat == Type.AVI_JPEG || outFormat == Type.AVI_PNG || 
      		  outFormat == Type.AVI_RAW || outFormat == Type.AVI_RLE8) {

            try {
         
          	  AVIOutputStream.VideoFormat format = null;
          	  int depth = 24;
          	  if (outFormat == Type.AVI_JPEG) {
          		  format = VideoFormat.JPG;
          		  depth = 24;
          	  }
          	  else if (outFormat == Type.AVI_PNG) {
          		  format = VideoFormat.PNG;
          		  depth = 24;
          	  }
          	  else if (outFormat == Type.AVI_RLE8) {
          		  format = VideoFormat.RLE;
          		  depth = 8;
          	  }
          	  else {
          		  format = VideoFormat.RAW;
          		  depth = 24;
          	  }
          	  
          	  
               // create a video recorder
                AVIOutputStream out = new AVIOutputStream(outFile, format, depth);
                out.setTimeScale(1);
                double frameRateInFPS = 1000.0/delayBetweenFramesInMillis;
                out.setFrameRate((int)Math.round(frameRateInFPS));
                for (int n = 0; n < frameCount; n++) {
                    progressBar.setString("Adding Frame " + n + " of " + frameCount);
                    progressBar.setValue(n);
                    if (awtImages == null) {
                        out.writeFrame(toBufferedImage(imageIterator.next()));
                    }
                    else {
                  	  out.writeFrame(toBufferedImage(awtImages[n]));
                    }
                }
                progressBar.setString("Finalizing...");
                progressBar.setValue(frameCount);
                out.close();
               
            } catch (Exception e) {
               e.printStackTrace();
               String message = "Error Writing "+outFile+" : "+e;
               JOptionPane.showMessageDialog(null, message, "AVI EXPORT ERROR", JOptionPane.ERROR_MESSAGE);         
               return;
            }

         }
        else if (outFormat == Type.QUICKTIME) {
        	
//        	ch.randelshofer.media.quicktime.QuickTimeWriter.VideoFormat videoFormat = 
//        		ch.randelshofer.media.quicktime.QuickTimeWriter.VideoFormat.RAW;
//        	
//        	
//        	try {
//				QuickTimeWriter qtOut = new QuickTimeWriter(outFile);
//
////				delayBetweenFramesInMillis = 1;
//				long animationLengthInMillis = delayBetweenFramesInMillis*frameCount;
//				
//				
//				int fpsScale = 1000;
//                double frameRateInFPS = 1000.0/delayBetweenFramesInMillis;
//                // 1. Get first frame so we know the image dimensions and init
//                BufferedImage firstFrame = null;
//                if (awtImages == null) {
//                    firstFrame = toBufferedImage(imageIterator.next());
//                }
//                else {
//                    firstFrame = toBufferedImage(awtImages[0]);
//                }
//                int width = firstFrame.getWidth(null);
//                int height = firstFrame.getHeight(null);
//                
//                qtOut.setSyncInterval(0, 30);
////	            qtOut.addVideoTrack(videoFormat, (int)(frameRateInFPS*fpsScale), width, height);
//	            qtOut.addVideoTrack(videoFormat, 10, width, height);
//
//	            
//                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//                Graphics2D g = img.createGraphics();
//                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//
//                
//                
//                // 2. Write the first image frame
//                g.drawImage(firstFrame, 0, 0, width, height, null);
//                qtOut.writeFrame(0, img, 1);
////                qtOut.writeFrame(0, firstFrame, delayBetweenFramesInMillis*1000);
//				
//                // 3. Write from 1...n frames
//                for (int n=1; n<frameCount; n++) {
//                    progressBar.setString("Adding Frame " + n + " of " + frameCount);
//                    progressBar.setValue(n);
//                    if (awtImages == null) {
//                        g.drawImage(imageIterator.next(), 0, 0, width, height, null);
//                        qtOut.writeFrame(0, img, 1);
////                        qtOut.writeFrame(0, img, delayBetweenFramesInMillis*1000);
////                        qtOut.writeFrame(0, toBufferedImage(imageIterator.next()), delayBetweenFramesInMillis*1000);
//                    }
//                    else {
//                        g.drawImage(awtImages[n], 0, 0, width, height, null);
//                        qtOut.writeFrame(0, img, 1);                        
////                        qtOut.writeFrame(0, img, delayBetweenFramesInMillis*1000);                        
////                        qtOut.writeFrame(0, toBufferedImage(awtImages[n]), delayBetweenFramesInMillis*1000);
//                    }
//                }
//                
//                img.flush();
//                
//                progressBar.setString("Finalizing...");
//                progressBar.setValue(frameCount);
//                
//                qtOut.close();
//
//				
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//	               String message = "Error Writing "+outFile+" : "+e;
//	               JOptionPane.showMessageDialog(null, message, "AVI EXPORT ERROR", JOptionPane.ERROR_MESSAGE);         
//	               return;
//			}
        	ch.randelshofer.media.quicktime.QuickTimeOutputStream.VideoFormat videoFormat = 
        		ch.randelshofer.media.quicktime.QuickTimeOutputStream.VideoFormat.JPG;
        	
        	
        	try {
				QuickTimeOutputStream qtOut = new QuickTimeOutputStream(outFile, videoFormat);
				qtOut.setTimeScale(1000);
				
////				delayBetweenFramesInMillis = 1;
//				long animationLengthInMillis = delayBetweenFramesInMillis*frameCount;
//				
//				
//				int fpsScale = 1000;
//                double frameRateInFPS = 1000.0/delayBetweenFramesInMillis;
//                // 1. Get first frame so we know the image dimensions and init
                BufferedImage firstFrame = null;
                if (awtImages == null) {
                    firstFrame = toBufferedImage(imageIterator.next());
                }
                else {
                    firstFrame = toBufferedImage(awtImages[0]);
                }
//                int width = firstFrame.getWidth(null);
//                int height = firstFrame.getHeight(null);
//                
////                qtOut.setSyncInterval(0, 30);
////	            qtOut.addVideoTrack(videoFormat, (int)(frameRateInFPS*fpsScale), width, height);
////	            qtOut.addVideoTrack(videoFormat, 10, width, height);
//
//	            
//                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//                Graphics2D g = img.createGraphics();
//                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                
                
                // 2. Write the first image frame
//                g.drawImage(firstFrame, 0, 0, width, height, null);
//                qtOut.writeFrame(img, 1);
                qtOut.writeFrame(firstFrame, delayBetweenFramesInMillis);
				
                // 3. Write from 1...n frames
                for (int n=1; n<frameCount; n++) {
                    progressBar.setString("Adding Frame " + n + " of " + frameCount);
                    progressBar.setValue(n);
                    if (awtImages == null) {
//                        g.drawImage(imageIterator.next(), 0, 0, width, height, null);
//                        qtOut.writeFrame(0, img, 1);
//                        qtOut.writeFrame(0, img, delayBetweenFramesInMillis*1000);
                        qtOut.writeFrame(toBufferedImage(imageIterator.next()), delayBetweenFramesInMillis);
                    }
                    else {
//                        g.drawImage(awtImages[n], 0, 0, width, height, null);
//                        qtOut.writeFrame(0, img, 1);                        
//                        qtOut.writeFrame(0, img, delayBetweenFramesInMillis*1000);                        
                        qtOut.writeFrame(toBufferedImage(awtImages[n]), delayBetweenFramesInMillis);
                    }
                }
                
//                img.flush();
                
                progressBar.setString("Finalizing...");
                progressBar.setValue(frameCount);
                
                qtOut.close();

				
				
			} catch (IOException e) {
				e.printStackTrace();
	               String message = "Error Writing "+outFile+" : "+e;
	               JOptionPane.showMessageDialog(null, message, "AVI EXPORT ERROR", JOptionPane.ERROR_MESSAGE);         
	               return;
			}

        }
        else {
            try {
                AnimatedGifEncoder e = new AnimatedGifEncoder();
                e.setQuality(1);
                e.start(outFile.toString());
                e.setDelay(delayBetweenFramesInMillis);
                e.setRepeat(0);
                for (int n = 0; n < frameCount; n++) {
                    progressBar.setString("Adding Frame " + n + " of " + frameCount);
                    progressBar.setValue(n);
                    if (awtImages == null) {
                        e.addFrame(toBufferedImage(imageIterator.next()));
                    }
                    else {
                        e.addFrame(toBufferedImage(awtImages[n]));
                    }
                }
                progressBar.setString("Finalizing...");
                progressBar.setValue(frameCount);
                e.finish();
            } catch (Exception e) {
                e.printStackTrace();
                String message = "Error writing "+outFile+".\n";
                JOptionPane.showMessageDialog(null, message, "GIF EXPORT ERROR", JOptionPane.ERROR_MESSAGE);         
            }
        }


    }

    // END saveAsMovie

    /**
     *  Bring up save dialog
     *
     * @return    Description of the Return Value
     */
    public static FileAndFormat saveAsMovieDialog(Component parent) {       
        String startPath = WCTProperties.getWCTProperty("imgsavedir");
        return saveAsMovieDialog(parent, startPath);
    }


    public static FileAndFormat saveAsMovieDialog(Component parent, String startPath) {       

        // Set up File Chooser
        JFileChooser fc = new JFileChooser(startPath);
        OpenFileFilter qtFilterJPEG = new OpenFileFilter("mov", true, "Quicktime Movie (Encoding: JPEG)");
        OpenFileFilter aviFilterRLE = new OpenFileFilter("avi", true, "AVI Movie (Encoding: RLE8)");
        OpenFileFilter aviFilterRAW2 = new OpenFileFilter("avi", true, "AVI Movie (Encoding: Raw - No Compression, Large File Size)");
        OpenFileFilter aviFilterPNG = new OpenFileFilter("avi", true, "AVI Movie (Encoding: PNG)");
        OpenFileFilter aviFilterJPEG = new OpenFileFilter("avi", true, "AVI Movie (Encoding: JPEG, Small File Size)");
        OpenFileFilter aviFilterRAW1 = new OpenFileFilter("avi", true, "AVI Movie (Encoding: Raw 1)");
        //OpenFileFilter movFilter = new OpenFileFilter("mov", true, "Quicktime");
        //OpenFileFilter mpegFilter = new OpenFileFilter("mpeg", true, "MPEG Movie");
        OpenFileFilter gifFilter = new OpenFileFilter("gif", true, "Animated GIF");

        fc.setAcceptAllFileFilterUsed(false);

        fc.addChoosableFileFilter(aviFilterJPEG);
        fc.addChoosableFileFilter(aviFilterRAW2);
        fc.addChoosableFileFilter(gifFilter);
//        fc.addChoosableFileFilter(qtFilterJPEG);
//        fc.addChoosableFileFilter(aviFilterRAW1);
//        fc.addChoosableFileFilter(aviFilterPNG);
//        fc.addChoosableFileFilter(aviFilterRLE);
        //fc.addChoosableFileFilter(movFilter);
        //fc.addChoosableFileFilter(mpegFilter);
        
        fc.setFileFilter(aviFilterJPEG);

        int returnVal = fc.showSaveDialog(parent);
        File file = null;
        File extfile = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            int choice = JOptionPane.YES_OPTION;
            // intialize to YES!
            file = fc.getSelectedFile();
            String fstr = file.toString();
            // Add extension if needed
            if (fc.getFileFilter() == aviFilterRAW1 || fc.getFileFilter() == aviFilterRLE ||
           		 fc.getFileFilter() == aviFilterJPEG || fc.getFileFilter() == aviFilterPNG ||
           		 fc.getFileFilter() == aviFilterRAW2) {
                // Add extension if needed
                if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".avi")) {
                   extfile = new File(file + ".avi");
                }
                else {
                   extfile = file;
                   file = new File(fstr.substring(0, (int) fstr.length() - 4));
                }
            }
            else if (fc.getFileFilter() == qtFilterJPEG) {
                   // Add extension if needed
                   if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".mov")) {
                      extfile = new File(file + ".mov");
                   }
                   else {
                      extfile = file;
                      file = new File(fstr.substring(0, (int) fstr.length() - 4));
                   }
            }
            else {
                // Add extension if needed
                if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".gif")) {
                    extfile = new File(file + ".gif");
                }
                else {
                    extfile = file;
                    file = new File(fstr.substring(0, (int) fstr.length() - 4));
                }
            }
            // Check for existing file
            if (extfile.exists()) {
                String message = "The Movie image file \n" +
                "<html><font color=red>" + extfile + "</font></html>\n" +
                "already exists.\n\n" +
                "Do you want to proceed and OVERWRITE?";
                choice = JOptionPane.showConfirmDialog(null, (Object) message,
                        "OVERWRITE MOVIE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                //String fstr = file.toString();
                //file = new File(fstr.substring(0,(int)fstr.length()-4));
            }
            // END if(file.exists())


            WCTProperties.setWCTProperty("imgsavedir", file.getParent());


            
            
            Type format = null;
            if (fc.getFileFilter() == aviFilterRAW1) {
           	 format = Type.AVI;
            }
            else if (fc.getFileFilter() == aviFilterRLE) {
           	 format = Type.AVI_RLE8;
            }
            else if (fc.getFileFilter() == aviFilterJPEG) {
           	 format = Type.AVI_JPEG;
            }
            else if (fc.getFileFilter() == aviFilterPNG) {
           	 format = Type.AVI_PNG;
            }
            else if (fc.getFileFilter() == aviFilterRAW2) {
           	 format = Type.AVI_RAW;
            }
            else {
           	 format = Type.ANIMATED_GIF;
            }
           	 

            
            

            if (choice == JOptionPane.YES_OPTION) {
                return new FileAndFormat(extfile, format);
        }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }


    /**
     * This method returns a buffered image with the contents of an image.<BR>
     *
     * THIS CODE WAS TAKEN FROM THE JavaAlmanac SITE.<BR>
     * http://javaalmanac.com/egs/java.awt.image/Image2Buf.html?l=rel<BR>
     * e671. Creating a Buffered Image from an Image<BR>
     * An Image object cannot be converted to a BufferedImage object.
     * The closest equivalent is to create a buffered image and then draw the image on the
     * buffered image. This example defines a method that does this.
     *
     * @param  image  Description of the Parameter
     * @return        Description of the Return Value
     */
    public static BufferedImage toBufferedImage(Image image) {
        return toBufferedImage(image, false);
    }

    public static BufferedImage toBufferedImage(Image image, boolean hasAlpha) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e665 Determining If an Image Has Transparent Pixels
        //boolean hasAlpha = hasAlpha(image);
        //boolean hasAlpha = false;
        // In this case we no there is no transparency

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }
    // END METHOD toBufferedImage


    
    
    public static class FileAndFormat {
 	   private File file;
 	   private Type format;

 	   public FileAndFormat(File file, Type format) {
 		   this.file = file;
 		   this.format = format;
 	   }

 	   public File getFile() {
 		   return file;
 	   }
 	   public Type getFormat() {
 		   return format;
 	   }
    }


}

