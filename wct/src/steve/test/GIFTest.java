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

package steve.test;

import gov.noaa.ncdc.wct.ui.AWTImageExport;
import gov.noaa.ncdc.wct.ui.AWTImageExport.Type;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;

public class GIFTest {


   public static void changeGIFSpeed(int frameRate) {
      try {
         File dir = new File("D:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf\\jnx-img");
         File animatedGif = new File(dir+File.separator+"klix-landfall-anim.gif");
         File outputGif = new File(dir+File.separator+"klix-landfall-anim_newspeed.gif");
         
         // Set up destination file
         AnimatedGifEncoder encoder = new AnimatedGifEncoder();
         encoder.start(outputGif.toString());
         int delay = (int)(1000.0/frameRate);
         encoder.setDelay(delay);
         encoder.setRepeat(1000);
         
         // Initialize the existing animated GIF
         GifDecoder decoder = new GifDecoder();
         decoder.read(animatedGif.toString());
         
         for (int n=0; n<decoder.getFrameCount(); n++) {
            //Image frame = decoder.getFrame(n);  // frame n
            encoder.addFrame(decoder.getFrame(n));
         }
         encoder.finish();
         
         
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

//   public static void changeQTSpeed(int frameRate) {
//      try {
//         int skipFrames = 1;
//         int startFrame = 0;
//         int endFrame = -1;
//         
//         File dir = new File("D:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf");
//         File inQT = new File(dir+File.separator+"katrina-gulf-h10-2.mov");
//         File outQT = new File(dir+File.separator+"katrina-gulf-h10-2-newspeed.mov");
//         
//         // Set up destination file
//         QTVideo inMovie = new QTVideo(inQT);
//         
//         QTVideoRecorder outMovie = new QTVideoRecorder();
//         outMovie.createVideo(outQT);
//         int delay = (int)(1000.0/frameRate);
//         outMovie.setFrameDuration(delay);
//
//System.out.println("::: DELAY = "+delay);
//System.out.println("::: NUM FRAMES = "+inMovie.getEndFrameNumber());
//
//
//         
//         int n = inMovie.getEndFrameNumber();
//         for (int i=0; i<n; i++) {
//System.out.println("PROCESSING ::: "+i*skipFrames);            
//            inMovie.setFrameNumber(i*skipFrames);
//            outMovie.addFrame(inMovie.getImage());
//         }
//         
//         
//         inMovie.dispose();
//         outMovie.saveVideo();
//         outMovie.dispose();
//         
//         
//         
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
//   }

   
//   public static void extractQTFrames() {
//
//      try {
//         int skipFrames = 1;
//         int startFrame = 0;
//         int endFrame = -1;
//         
//         double speedRatio = .05;
//         
//         File dir = new File("H:\\Nexrad_Viewer_Test\\Dickson");
//         File qtMovie = new File(dir+File.separator+"kmob-n0z.mov");
//         
//         QTVideo qt = new QTVideo(qtMovie);
//         
//         
//         int n = qt.getEndFrameNumber();
//System.out.println("::: NUM FRAMES = "+n);
//         // set endframe
//         if (endFrame > 0 && endFrame < n) {
//            n = endFrame;
//         }
//         
//         for (int i = 0; i < n/skipFrames; i++) {
//            
//            File outputFile = new File("H:\\Nexrad_Viewer_Test\\Dickson\\katrina-kmob-frames\\katrina-kmob-"+i);
//            //AnimatedGifEncoder encoder = new AnimatedGifEncoder();
//            //encoder.start(outputFile.toString());
//            
//            qt.setFrameNumber(i*skipFrames);
//            //encoder.addFrame(qt.getImage());
//            //encoder.finish();
//            AWTImageExport.saveImage(qt.getImage(), outputFile, AWTImageExport.Type.JPEG);
//            
//            if (i%10 == 0) {
//               System.out.println("PROCESSING ::: "+i*skipFrames+" / "+n);
//            }
//            
//         }
//         qt.dispose();
//            
//
//         /*
//         File dir = new File("C:\\Documents and Settings\\steve.ansari\\My Documents\\Presentations\\NWS_VisitView\\gifsicle\\charley-3d\\save");
//         File[] files = dir.listFiles();
//
//         for (int n=0; n<files.length; n++) {
//            
//            Image img = new ImageIcon(files[n].toURL()).getImage();
//            String newfilename = "charley"+n;
//            File newfile = new File(dir.toString()+File.separator+newfilename);
//            AWTImageExport.saveImage(img, newfile, AWTImageExport.PNG);
//         }
//         */
//         
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
//   }
//   
//
//   
//   public static void convertQT2GIF() {
//
//      try {
//         int skipFrames = 3;
////         int skipFrames = 3;
//         int startFrame = 0;
//         int endFrame = -1;
//         
//         double speedRatio = .05;
//         
//         File dir = new File("D:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf");
//         File qtMovie = new File(dir+File.separator+"katrina-gulf-both.mov");
//         File outputGif = new File(dir+File.separator+"katrina-anim-both.gif");
//         
//         QTVideo qt = new QTVideo(qtMovie);
//         //QTVideo qt = new QTVideo();
//         //qt.play();
//         
//         AnimatedGifEncoder encoder = new AnimatedGifEncoder();
//         encoder.start(outputGif.toString());
//         int delay = (int)(qt.getFrameTime(2)-qt.getFrameTime(1))/skipFrames;
//         delay = (int)(delay/speedRatio);
//         encoder.setDelay(delay);
//         encoder.setRepeat(1000);
//         
//System.out.println("::: DELAY = "+delay);
//         
//         
//         int n = qt.getEndFrameNumber();
//System.out.println("::: NUM FRAMES = "+n);
//         // set endframe
//         if (endFrame > 0 && endFrame < n) {
//            n = endFrame;
//         }
//         
//         for (int i = 0; i < n/skipFrames; i++) {
//         //for (int i = 0; i < 50; i++) {
//            //BufferedImage frame = d.getFrame(i);  // frame i
//            
//            qt.setFrameNumber(i*skipFrames);
//            encoder.addFrame(qt.getImage());
//            
//            if (i%10 == 0) {
//               System.out.println("PROCESSING ::: "+i*skipFrames+" / "+n);
//            }
//            
//            //int t = d.getDelay(i*skipFrames);  // display duration of frame in milliseconds
//            // do something with frame
//            
//         }
//         encoder.finish();
//         qt.dispose();
//            
//
//         /*
//         File dir = new File("C:\\Documents and Settings\\steve.ansari\\My Documents\\Presentations\\NWS_VisitView\\gifsicle\\charley-3d\\save");
//         File[] files = dir.listFiles();
//
//         for (int n=0; n<files.length; n++) {
//            
//            Image img = new ImageIcon(files[n].toURL()).getImage();
//            String newfilename = "charley"+n;
//            File newfile = new File(dir.toString()+File.separator+newfilename);
//            AWTImageExport.saveImage(img, newfile, AWTImageExport.PNG);
//         }
//         */
//         
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
//   }
   
   
   public static void extractGIFFrames() {

      try {
         int skipFrames = 3;
         int startFrame = 0;
         int endFrame = -1;
         
         File dir = new File("C:\\Documents and Settings\\steve.ansari\\My Documents\\Presentations\\NWS_VisitView\\anim-extract\\charley-topo");
         File animatedGif = new File(dir+File.separator+"charley-topo-anim.gif");
         
         GifDecoder d = new GifDecoder();
         d.read(animatedGif.toString());
         int n = d.getFrameCount();
         // set endframe
         if (endFrame > 0 && endFrame < n) {
            n = endFrame;
         }
         
         for (int i = 0; i < n/skipFrames; i++) {
            //BufferedImage frame = d.getFrame(i);  // frame i
            Image frame = d.getFrame(i*skipFrames);  // frame i
            //int t = d.getDelay(i*skipFrames);  // display duration of frame in milliseconds
            // do something with frame
            
            String newfilename = "charleytopo"+(i*skipFrames);
            File newfile = new File(dir.toString()+File.separator+newfilename);
            AWTImageExport.saveImage(frame, newfile, AWTImageExport.Type.GIF);
            
            
         }

         /*
         File dir = new File("C:\\Documents and Settings\\steve.ansari\\My Documents\\Presentations\\NWS_VisitView\\gifsicle\\charley-3d\\save");
         File[] files = dir.listFiles();

         for (int n=0; n<files.length; n++) {
            
            Image img = new ImageIcon(files[n].toURL()).getImage();
            String newfilename = "charley"+n;
            File newfile = new File(dir.toString()+File.separator+newfilename);
            AWTImageExport.saveImage(img, newfile, AWTImageExport.PNG);
         }
         */
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }



   public static void createAnimatedGIF() {
      try {
    
         File dir = new File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Charley\\paraview\\frames");
         File outFile = new File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Charley\\paraview\\charley-paraview-anim.gif");
         File[] files = dir.listFiles();
         Image[] imgArray = new Image[files.length];
         // scan images into memory
         for (int n=0; n<files.length; n++) {
            imgArray[n] = new ImageIcon(files[n].toURL()).getImage();
         }
         AWTImageExport export = new AWTImageExport(imgArray);
         export.saveAsMovie(Type.AVI_JPEG, outFile, 100, new javax.swing.JProgressBar());
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void createQT() {
      try {
    
         File dir = new File("H:\\Nexrad_Viewer_Test\\wes\\frames");
         File outFile = new File("H:\\Nexrad_Viewer_Test\\wes\\klix-n0r-anim.mov");
         File[] files = dir.listFiles();
         Image[] imgArray = new Image[files.length];
         // scan images into memory
         for (int n=0; n<files.length; n++) {
            System.out.println("LOADING: "+files[n]);
            imgArray[n] = new ImageIcon(files[n].toURL()).getImage();
         }
         AWTImageExport export = new AWTImageExport(imgArray);
         export.saveAsMovie(Type.AVI_JPEG, outFile, 100, new javax.swing.JProgressBar());
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   
   public static void main(String[] args) {
      //changeQTSpeed(Integer.parseInt(args[0]));
      //changeGIFSpeed(Integer.parseInt(args[0]));
      //extractQTFrames();
      //createAnimatedGIF();
      createQT();

      System.exit(1);
   }
   
   
}
