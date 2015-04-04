package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;

import java.io.IOException;

import org.junit.Test;

public class TestDecodeXMRGData {

    
    
    @Test
    public void testDecoder() throws DecodeException, IOException {
        
          DecodeXMRGHeader xmrgHeader = new DecodeXMRGHeader();
          xmrgHeader.decodeHeader(new java.io.File("H:\\Nexrad_Viewer_Test\\XMRG\\serfc\\xmrg_08142004_00z_SE").toURI().toURL());
          DecodeXMRGData xmrgData = new DecodeXMRGData(xmrgHeader);
          xmrgData.decodeData();
          
    }

}
