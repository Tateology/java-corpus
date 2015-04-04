package gov.noaa.ncdc.nexradexport;

import java.io.File;

import org.junit.Test;

public class TestExtractCAPPI {



    @Test
    public void test1() {


        ExtractCAPPI cappi = new ExtractCAPPI();


        try {

            cappi.processDir(new File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Charley\\mayavi"));
            //process();
            //processDir(new File("H:\\Nexrad_Viewer_Test\\Tornado\\KansasCity-200305\\HAS000258134-L2\\tornado-1"));


        } catch (Exception e) {
            e.printStackTrace();
        }   

    }
}
