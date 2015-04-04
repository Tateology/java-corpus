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

package gov.noaa.ncdc.wct.event;

import java.util.EventListener;

/**
 * An export listener receives export events and performs
 * some appripriate action in response.  Export events are used
 * to signal the details of a export, such as starting,
 * progress, status and ending.
 */
public interface DataExportListener
  extends EventListener {

  ////////////////////////////////////////////////////////////

  /** Responds to an export starting. */
  public void exportStarted(DataExportEvent event);


  ////////////////////////////////////////////////////////////

  /** Responds to an export ending. */
  public void exportEnded(DataExportEvent event);

  ////////////////////////////////////////////////////////////

  /** Responds to an export progress event. */
  public void exportProgress(DataExportEvent event);

  ////////////////////////////////////////////////////////////

  
  /** Responds to an export status event. */
  public void exportStatus(DataExportEvent event);

  ////////////////////////////////////////////////////////////
  
  /** Responds to a data transfer error. */
  //public void exportError(NexradExportEvent event);

  ////////////////////////////////////////////////////////////

} // NexradExportListener class

////////////////////////////////////////////////////////////////////////
