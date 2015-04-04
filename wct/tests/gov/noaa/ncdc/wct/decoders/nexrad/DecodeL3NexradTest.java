package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class DecodeL3NexradTest {

    
    @Test
    public void testDecoder() throws IOException, DecodeException {
        
          DecodeL3Header header = new DecodeL3Header();
          
          //URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\NCF\\7000KLWX_SDUS61_NCFLWX_200304291156").toURL();
//          URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\NCF\\7000KOHX_SDUS64_NCFOHX_200604060537").toURL();
          //URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\1.1.0\\RSL\\KGSP_SDUS42_RSLGSP_200607050758").toURL();
          //URL url = new URL("http://www1.ncdc.noaa.gov/pub/has/HAS000394190/7000KOKX_SDUS51_NTPOKX_199607311605");
//          URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS31_N1PCLE_200211102200").toURL();
//          URL url = new java.io.File("E:\\devel\\wct\\testdata\\KBIS_SDUS53_DHRMBX_200806191830").toURI().toURL();         
//          URL url = new java.io.File("E:\\devel\\wct\\testdata\\KBIS_SDUS53_DSPMBX_200806192355").toURI().toURL();         

//          URL url = new java.io.File("E:\\work\\herz\\baddata\\N0R_20001201_0426").toURI().toURL();
          URL url = new java.io.File("E:\\work\\level3-dualpole\\koax-dualpol-wmo\\koax-dualpol-wmo\\KOAX.176pr.20100428_2329").toURI().toURL();
          
          header.decodeHeader(url);

          System.out.println("ICAO: " + header.getICAO());
          System.out.println(" LAT: " + header.getLat());
          System.out.println(" LON: " + header.getLon());

          for (int n=0; n<16; n++) {
             System.out.println("PROD. SPEC. ["+n+"]: "+((DecodeL3Header)header).getProductSpecificValue(n));
          }
          
          DecodeL3Nexrad decoder = new DecodeL3Nexrad(header);
          decoder.decodeData();

          
          
          System.out.println(decoder.getFeatures().size() + "  FEATURES CREATED");
    }

}
