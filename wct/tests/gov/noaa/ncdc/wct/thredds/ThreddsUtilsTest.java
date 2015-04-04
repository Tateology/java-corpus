package gov.noaa.ncdc.wct.thredds;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Test;

public class ThreddsUtilsTest {

    
    @Test
    public void test1() throws IOException {
        ThreddsUtils utils = new ThreddsUtils();
//        utils.process("http://motherlode.ucar.edu:8080/thredds/catalog.xml");
        utils.process("http://motherlode.ucar.edu:8080/thredds/idd/radars.xml", false);
        
        System.out.println("Directory List: "+utils.getDirectoryDatasetArray());
        System.out.println("OPENDAP List: "+utils.getOpendapAccessArray());
        System.out.println("HTTPSerer List: "+utils.getHttpAccessArray());
    }
}
