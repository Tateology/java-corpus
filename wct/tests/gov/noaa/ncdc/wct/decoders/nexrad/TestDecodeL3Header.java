package gov.noaa.ncdc.wct.decoders.nexrad;

import java.net.URL;

import org.junit.Test;

public class TestDecodeL3Header {

    
    

    @Test
    public void testRead() {
        try {
           DecodeL3Header header = new DecodeL3Header();
           //URL url = new URL("http://www1.ncdc.noaa.gov/pub/has/HAS000155503/7000KDTX_SDUS53_N0RDTX_200105252102");
           //URL url = new java.io.File("C:\\ViewerData\\HAS999900001\\7000KCLE_SDUS81_DPACLE_200211102333").toURL();         
           //URL url = new java.io.File("D:\\Nexrad_Viewer_Test\\AntonKruger\\data\\ldm\\7000PGUM_SDUS50_N1PGUA_200411291858").toURL();
//           URL url = new java.io.File("H:\\Nexrad_Viewer_Test\\StatusProductLog\\KGSP_SDUS42_RSLGSP_200607050758").toURI().toURL();         
           //URL url = new java.io.File("H:\\ViewerData\\HAS000202646\\7000KILN_NXUS61_GSMILN_200211102123").toURL();         
           //URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS81_DPACLE_200211102356").toURL();         
           //URL url = new URL("http://www1.ncdc.noaa.gov/pub/has/HAS999900001/KGSP_SDUS52_N0RGSP_200501100007");
//           URL url = new URL("http://mesonet.tamu.edu/products/RADAR/nexrad/NIDS/20070427/GSP/N0R/N0R_20070427_0054");
           URL url = new java.io.File("E:\\devel\\wct\\testdata\\KBIS_SDUS53_DSPMBX_200806192355").toURI().toURL();         
//           URL url = new java.io.File("E:\\devel\\wct\\testdata\\KBIS_SDUS53_DHRMBX_200806191830").toURI().toURL();         

           System.out.println("PROCESSING: "+url);
           header.decodeHeader(url);
           
           System.out.println(" ICAO: "+header.getICAO());
           System.out.println("  LAT: "+header.getLat());
           System.out.println("  LON: "+header.getLon());
           System.out.println("  ALT: "+header.getAlt());
           System.out.println("PCODE: "+header.getProductCode());


           
           
           String[] categories = header.getDataThresholdStringArray();
           for (int n=0; n<categories.length; n++) {
              System.out.println("categories["+n+"] = "+categories[n]);  
           }
           
           int[] prodSpec = header.getProductSpecificValueArray();
           for (int n=0; n<prodSpec.length; n++) {
              System.out.println("prodSpec["+n+"] = "+prodSpec[n]);  
           }


           
        } catch (Exception e) {
           e.printStackTrace();
        }
     }
     

}
