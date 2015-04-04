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


/**
 *  Provides access to protected StyledMapPane methods
 *
 * @author     steve.ansari
 * @created    August 31, 2004
 */
public class WCTMapPane extends org.geotools.gui.swing.WCTStyledMapPane {

   /**
    *Constructor for the NexradMapPane object
    */
   public WCTMapPane() {
      super();
      init();
   }

   
   private void init() {
       
//       final MapPane thisPane = this;
       
//       this.addMouseListener(new MouseListener() {
//
//        public void mouseClicked(MouseEvent evt) {
//            System.out.println("MOUSE CLICKED  "+evt.toString());
//        }
//
//        public void mouseEntered(MouseEvent arg0) {
//            System.out.println("MOUSE ENTERED");
////            thisPane.transform(AffineTransform.getScaleInstance(2, 2));
//            
//        }
//
//        public void mouseExited(MouseEvent arg0) {
//            System.out.println("MOUSE EXITED");
////            thisPane.transform(AffineTransform.getScaleInstance(.5, .5));
//            
//        }
//
//        public void mousePressed(MouseEvent arg0) {
//            System.out.println("MOUSE PRESSED");
//            
//        }
//
//        public void mouseReleased(MouseEvent arg0) {
//            System.out.println("MOUSE RELEASED");
//            
//        }
//           
//       });
   }
   
   
   
   
   

   /**
    *  
    *
    * @param  bounds  Description of the Parameter
    * @return     
    */
   public java.awt.Rectangle getWCTZoomableBounds(java.awt.Rectangle bounds) {
       
      return getZoomableBounds(bounds);
   }


   /**
    *  Gets the nexradZoom attribute of the NexradMapPane object
    *
    * @return    The nexradZoom value
    */
   public java.awt.geom.AffineTransform getZoom() {
      return zoom;
   }

}

