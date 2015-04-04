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

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class BufferedImageExport {

   public final static int JPEG = 1;
   public final static int PNG = 2;

   BufferedImage[] bufImages;

   /**
    * Constructor 
    */
   public BufferedImageExport(BufferedImage bufImage) {
      this.bufImages = new BufferedImage[1];
      bufImages[0] = bufImage;
   }

   /**
    * Constructor 
    */
   public BufferedImageExport(BufferedImage[] bufImages) {
      this.bufImages = bufImages;
   }

   /**
    * Save an Image using JFileChooser.
    */
   public void exportBufferedChooser() {

      // Loop through bufImages Array
      for (int i=0; i<bufImages.length; i++) {

         // Set up File Chooser
         JFileChooser fc = new JFileChooser();
         OpenFileFilter jpgFilter = new OpenFileFilter("jpg", true, "JPEG Images");
         OpenFileFilter pngFilter = new OpenFileFilter("png", true, "PNG Images");
         fc.addChoosableFileFilter(jpgFilter);
         fc.addChoosableFileFilter(pngFilter);
         fc.setAcceptAllFileFilterUsed(false);

         int returnVal = fc.showSaveDialog(null);
         File file = null;
         if (returnVal == JFileChooser.APPROVE_OPTION) {
            int choice = JOptionPane.YES_OPTION; // intialize to YES!
            file = fc.getSelectedFile();
            File extfile = null;
            if (fc.getFileFilter() == jpgFilter)
               extfile = new File(file + ".jpg");
            else
               extfile = new File(file + ".png");
            // Check for existing file
            if (file.exists()) {
               String message = "The Image file \n" +
                                "<html><font color=red>"+file+"</font></html>\n"+
                                "already exists.\n\n"+
                                "Do you want to proceed and OVERWRITE?";
               choice = JOptionPane.showConfirmDialog(null, (Object)message,
                              "OVERWRITE IMAGE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
               String fstr = file.toString();
               file = new File(fstr.substring(0,(int)fstr.length()-4));
            } // END f(file.exists())
            // Check for existing file without extension
            else if (extfile.exists()) {
               String message = "The Image file \n" +
                                "<html><font color=red>"+extfile+"</font></html>\n"+
                                "already exists.\n\n"+
                                "Do you want to proceed and OVERWRITE?";
               choice = JOptionPane.showConfirmDialog(null, (Object)message,
                              "OVERWRITE IMAGE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            } // END else if(extfile.exists())
            if (choice == JOptionPane.YES_OPTION) {
               // Save the image
               if (fc.getFileFilter() == jpgFilter)
                  exportImage(file, i, JPEG);
               else
                  exportImage(file, i, PNG);
            } // END if(choice == YES_OPTION)
         } // END if(returnVal)
      } // END FOR LOOP
   } // END METHOD saveViewAsJPEG

   
   /**
    * Save an Image given an output file and image type.
    */
   public boolean exportImage(File[] files, int outType) {

      boolean status;
      if (files.length != bufImages.length) {
         System.out.println("File Array Size != Image Array Size");
         return false;
      }

      for (int i=0; i<files.length; i++) {
         if (! saveImage(bufImages[i], files[i], outType))
            return false;
      } // END FOR LOOP
      return true;
   } // END METHOD saveViewAsJPEG

   /**
    * Save an Image given an output file, array index and image type.
    */
   public boolean exportImage(File file, int index, int outType) {

      return (saveImage(bufImages[index], file, outType));
   } // END METHOD saveViewAsJPEG

   /**
    * Save an BufferedImage to Image file given an output file and type (JPEG or PNG).
    */
   public static boolean saveImage(BufferedImage image, File outFile, int outType) {

        try {
           // Save as PNG
           if (outType == PNG) {
              File pngfile = new File(outFile.toString()+".png");
              ImageIO.write(image, "png", pngfile);
           }
           else {
              File pngfile = new File(outFile.toString()+".jpg");
              ImageIO.write(image, "jpg", pngfile);
           }
           return true;
        } 
        catch (Exception e) {
           System.out.println("CAUGHT IMAGE EXCEPTION "+e);
           return false;
        }



/*
      try {
         JPEGEncodeParam param =   JPEGCodec.getDefaultJPEGEncodeParam(image);
         param.setQuality(1.0f, true);
         FileOutputStream fout   =  new FileOutputStream (outFile);
         JPEGImageEncoder encoder =  JPEGCodec.createJPEGEncoder (fout);
         encoder.encode (image, param);
         fout.close ();
         return true;
      } catch (Exception err) {
         System.err.println("Problem saving JPEG" + err);
         err.printStackTrace ();
         return false;
      }
*/
   } // END METHOD saveJPEG

   /**
    * Save array of BufferedImages to MPEG, AVI or QUICKTIME Movie using JFileChooser.
    * Images are first converted to JPEG Images in the directory specified 
    * for the Movie file.
    */
   public void saveAsMovie(int frameRateInMillis) {

      // Set up File Chooser
      JFileChooser fc = new JFileChooser();
      OpenFileFilter aviFilter = new OpenFileFilter("avi", true, "AVI Movie");
//      OpenFileFilter movFilter = new OpenFileFilter("mov", true, "Quicktime");
      OpenFileFilter mpegFilter = new OpenFileFilter("mpeg", true, "MPEG Movie");
      //fc.addChoosableFileFilter(aviFilter);
//      fc.addChoosableFileFilter(movFilter);
      //fc.addChoosableFileFilter(mpegFilter);
      fc.setAcceptAllFileFilterUsed(false);
 
      int returnVal = fc.showSaveDialog(null);
      File file = null;
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         int choice = JOptionPane.YES_OPTION; // intialize to YES!
         file = fc.getSelectedFile();
         String fstr = file.toString();
         // Add extension if needed
         if (! fstr.substring((int)fstr.length()-4, (int)fstr.length()).equals(".mov"))
            file = new File(file + ".mov");
         // Check for existing file
         if (file.exists()) {
            String message = "The Movie image file \n" +
                             "<html><font color=red>"+file+"</font></html>\n"+
                             "already exists.\n\n"+
                             "Do you want to proceed and OVERWRITE?";
            choice = JOptionPane.showConfirmDialog(null, (Object)message,
                           "OVERWRITE MOVIE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            //String fstr = file.toString();
            //file = new File(fstr.substring(0,(int)fstr.length()-4));
         } // END if(file.exists())
         /*
         else if (extfile.exists()) {
            String message = "The Movie image file \n" +
                             "<html><font color=red>"+extfile+"</font></html>\n"+
                             "already exists.\n\n"+
                             "Do you want to proceed and OVERWRITE?";
            choice = JOptionPane.showConfirmDialog(null, (Object)message,
                           "OVERWRITE MOVIE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
         } // END else if(extfile.exists())
*/
         if (choice == JOptionPane.YES_OPTION) {
            // Save the image
            saveAsMovie(file, frameRateInMillis);
         } // END if(choice == YES_OPTION)
      } // END if(returnVal)
   } // END saveAsMovie

   /**
    * Save array of BufferedImages to MPEG, AVI or QUICKTIME Movie using JFileChooser.
    * Images are first converted to JPEG Images in the directory specified 
    * for the Movie file.
    */
   public void saveAsMovie(File outFile, int frameRateInMillis) {

//      // create a video recorder
//      VideoRecorder recorder = new QTVideoRecorder();
//
//      // use the recorder to create and save a video
//      try {
//         // define the video file and set the frame duration
//         recorder.createVideo(outFile.toString());
//         recorder.setFrameDuration(frameRateInMillis);  
//         for (int n=0; n<bufImages.length; n++) {
//            recorder.addFrame(bufImages[n]);
//         }
//         recorder.saveVideo();
//      }
//      catch (Exception e) {
//         System.out.println("CAUGHT EXCEPTION "+e);
//      }
      


/*
      long timeStamp = System.currentTimeMillis();
      Vector inFiles = new Vector();
      // Create JPEGs
      for (int i=0; i<bufImages.length; i++) {
         saveImage(bufImages[i], new File(outFile+".F"+i+"."+timeStamp), JPEG); 
         inFiles.addElement(new File(outFile+".F"+i+"."+timeStamp));
      }
      // Export to desired format
      JpegImagesToMovie.createMovie(outFile.toString(), bufImages[0].getWidth(null), 
                                    bufImages[0].getHeight(null), 50, inFiles);
*/
   } // END saveAsMovie



} // END CLASS
