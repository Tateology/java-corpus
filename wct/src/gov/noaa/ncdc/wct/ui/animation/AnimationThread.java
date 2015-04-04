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


public class AnimationThread extends Thread 
{
   int numImages, start;
   Animator animate;
   int time;
   boolean theEnd = false;
   boolean rock;
 
   /**
    * Constructor
    * @param animate The Animator frame object
    * @param numImages The number of images in the icon array 
    * 
    */
   AnimationThread(Animator animate, int numImages) {
      this(animate, numImages, 0, false);
   }
   /**
    * Constructor
    * @param animate The Animator frame object
    * @param numImages The number of images in the icon array
    * @param start The image number to start on 
    *
    */
   AnimationThread(Animator animate, int numImages, int start) {
      this(animate, numImages, start, false);
   }
   /**
    * Constructor
    * @param animate The Animator frame object
    * @param numImages The number of images in the icon array
    * @param start The image number to start on
    * @param rock Rock the animation
    *
    */
   AnimationThread(Animator animate, int numImages, int start, boolean rock) {
      this(animate, numImages, start, false, 50);
   }
   /**
    * Constructor
    * @param animate The Animator frame object
    * @param numImages The number of images in the icon array
    * @param start The image number to start on
    * @param rock Rock the animation
    * @param time The frame rate in millisec/10
    *
    */
   AnimationThread(Animator animate, int numImages, int start, boolean rock, int time) {
      this.animate = animate;
      this.numImages = numImages;
      this.start = start;
      this.rock = rock;
      this.time = time;
   }

  
   /**
    * Method that starts the thread
    */
   public void run() {
      theEnd = false;
      boolean firstTime = true; 
      boolean reverse = false;
      for (int i=0; i<numImages; i++) {
         if (firstTime) {
            i=start; // Set starting image
            firstTime = false;
         }
         try {
           // set the icon
           if (reverse)
              animate.setAnimationIcon(numImages-i-1);
           else
              animate.setAnimationIcon(i);
           // wait 
           try {
              if (time > 0)
                 Thread.sleep(time); 
              else
                 Thread.sleep(5);
           }
           catch (Exception e) {}
           // repeat
           if (i == numImages - 1) {
              i=-1;
              if (rock) {
                 reverse = reverse ? false : true ; // switch boolean value
              }
              else 
                 reverse = false;
           }
           // let this thread naturally die 
           if (theEnd) i=numImages;
         }
         catch (Exception ae) {}
      } // end for
   } // END METHOD run()


   /**
    * Method that gracefully ends the tread 
    */
   public void endThread() {
      theEnd = true;
   } // END METHOD endThread()

   /**
    * Method that restarts the tread 
    */
   public void restartThread() {
      theEnd = false;
      start();
   } // END METHOD restartThread()

   /**
    * Method that sets the time wait in millis between each frame of the animation  
    */
   public void setWaitTime(int time) {
      this.time = time;
   } // END METHOD setWaitTime()

   /**
    * Method that sets the rock status 
    */
   public void setRock(boolean rock) {
      this.rock = rock;
   } // END METHOD setRock()

} // END CLASS
