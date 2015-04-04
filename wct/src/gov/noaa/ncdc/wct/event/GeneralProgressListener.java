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
 * A data transfer listener receives data transfer events and performs
 * some appripriate action in response.  Data transfer events are used
 * to signal the details of a data transfer, such as starting,
 * transfer progress, and ending.
 */
public interface GeneralProgressListener
  extends EventListener {

  ////////////////////////////////////////////////////////////

  /** Responds to a starting. */
  public void started(GeneralProgressEvent event);


  ////////////////////////////////////////////////////////////

  /** Responds to an ending. */
  public void ended(GeneralProgressEvent event);

  ////////////////////////////////////////////////////////////

  /** Responds to an progress. */
  public void progress(GeneralProgressEvent event);

  ////////////////////////////////////////////////////////////


} // NexradExportListener class

////////////////////////////////////////////////////////////////////////
