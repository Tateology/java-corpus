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

// Imports
// -------
import gov.noaa.ncdc.wct.io.SupportedDataType;

import java.util.EventObject;
import java.util.HashMap;

/**

 */
public class DataDecodeEvent extends EventObject {


    private int progress;
    private String status;
    private SupportedDataType dataType;
    private HashMap<String, String> decodeMetadataMap = new HashMap<String, String>();
    
    
    ////////////////////////////////////////////////////////////

    /** Create a new event. */
    public DataDecodeEvent (Object source) { 

        super (source);

    } // NexradExportEvent

    ////////////////////////////////////////////////////////////



    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDataType(SupportedDataType dataType) {
        this.dataType = dataType;
    }

    public SupportedDataType getDataType() {
        return dataType;
    }

    public void setDecodeMetadataMap(HashMap<String, String> decodeMetadataMap) {
        this.decodeMetadataMap = decodeMetadataMap;
    }

    public HashMap<String, String> getDecodeMetadataMap() {
        return decodeMetadataMap;
    }



} // NexradExportEvent class

////////////////////////////////////////////////////////////////////////
