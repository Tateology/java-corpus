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

package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingDecoder;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;

import java.io.IOException;
import java.util.Map;

import org.geotools.feature.FeatureCollection;

public interface StreamingRadialDecoder extends StreamingDecoder {

    public String[] getSupplementalDataArray() throws IOException;

//    public FeatureType getFeatureType();
    // now use getFeatureTypes() in StreamingDecoder
    
    /**
     * Decodes data into Features and stores in an in-memory FeatureCollection
     * @throws DecodeException
     * @throws IOException
     */
    public void decodeData() throws DecodeException, IOException;
    
    /**
     * Gets the Features stored after the use of 'decodeData()'
     * @return
     */
    public FeatureCollection getFeatures();
    
    /**
     * Decodes the data and processes each Feature with a StreamingProcess - no data is stored in-memory.
     * Does call the .close() method for the StreamingProcess array.
     * @param processArray
     * @throws DecodeException
     * @throws IOException
     */
    // inherit from StreamingDecoder
//    public void decodeData(StreamingProcess[] processArray) throws DecodeException, IOException;
    
    /**
     * Decodes the data and processes each Feature with a StreamingProcess - no data is stored in-memory.
     * @param processArray
     * @param autoClose Do we call the .close() method for the StreamingProcess array when we are finished?
     * @throws DecodeException
     * @throws IOException
     */
    public void decodeData(StreamingProcess[] processArray, boolean autoClose) throws DecodeException, IOException;
    
    
    public Map<String, Object> getDecodeHints();
    public void setDecodeHint(String hintKey, Object hintValue) throws DecodeHintNotSupportedException;

}
