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

import java.awt.Image;
import java.io.File;

import com.madgag.gif.fmsware.GifDecoder;

public class RenameGIFs {


   public static void main(String[] args) {

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

}
