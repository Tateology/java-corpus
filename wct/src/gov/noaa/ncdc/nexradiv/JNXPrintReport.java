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

import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

import com.jetbee.report.JetException;
import com.jetbee.report.JetGraphics2DReport;








public class JNXPrintReport extends JetGraphics2DReport {



   private String title = "JNX Print Example";
   private WCTViewer jnx;
   
   public JNXPrintReport(WCTViewer jnx) {         
      super("Alphanumeric Report",  "JNX PRINT");
      this.jnx = jnx;         
   }
   
   public void jetPrint() throws JetException { 
   // this member function will be called to generate the report.  
        
      // initialization.         
      jetSetInternalMargins(50, 0, 50, 0);
               
      // actual printing begins.         
      Graphics2D g = jetPrintBegin();

      g.setFont(new Font("Monospaced", Font.PLAIN, 10));

      int pageCnt = 1;
      int y=0;
      /*
      for (int i=0; i<supplementalData.size(); i++) {
         //g.drawString((String)supplementalData.elementAt(i), 75, 105+y*10);
         g.drawString((String)supplementalData.elementAt(i), 0, y*10);
         if (i>0 && i%54 == 0) {
            //g.drawString("     Page "+pageCnt++, 75, 105+(y+2)*10);
            jetNewPage();
            g.setFont(new Font("Monospaced", Font.PLAIN, 10));
            y=0;
         }
         else {
            y++;
         }
      }
      */
      
      
      g.drawString("TEST", 120, 120);
      //g.drawImage(jnx.getViewerBufferedImage(), 50, 50, jnx);
      
      //jw.jetCloseReport();

      
   }
   
   public void jetPrintHeader(Graphics2D g, PageFormat pf) { 
      // called at the beginning of printing a new report page.
               
      g.drawString(title, 150, 20);         
      g.drawLine(18, 23, 450, 23);
   }
   
   public void jetPrintFooter(Graphics2D g, PageFormat pf) { 
      // called before a new report page is supplied.
      
      g.drawString("Page " + (jetGetPageNumber() + 1), 230, 25);
      
   }
   
      
    





}
