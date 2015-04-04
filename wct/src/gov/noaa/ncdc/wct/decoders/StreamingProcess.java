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

package gov.noaa.ncdc.wct.decoders;

import org.geotools.feature.Feature;

/**
 *  Interface that defines the necessary "addFeature" method that receives a single 
 *  Feature and does some process on it.  The 'close' method is only used when I/O 
 *  is present such as writing a Shapefile.
 *
 * @author    steve.ansari
 */
public interface StreamingProcess {

   /**
    *  Receives a feature and does a process on it.  It is up to the LiteProcess 
    *  implementor to decide what to do with this feature.  This could be to 
    *  export it to shapefile, rasterize it, etc...
    *
    * @param  feature                   The feature to be added to the Feature attribute
    * @exception  StreamingProcessException  Description of the Exception
    */
   public void addFeature(Feature feature) throws StreamingProcessException;


   /**
    * Only used when I/O is present such as writing a Shapefile (in ExportShapefileLite).
    *
    * @exception  StreamingProcessException  Description of the Exception
    */
   public void close() throws StreamingProcessException;

}

