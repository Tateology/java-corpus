package gov.noaa.ncdc.wct.decoders.goes;

import gov.noaa.ncdc.wct.decoders.AbstractIospTest;

import java.io.File;

public class AreaIospTest extends AbstractIospTest {

    public final String TEST_FILE = "E:\\work\\goes\\goes12.2005.265.234514.BAND_04";



    @Override
    public Class getIospClass() {
        return gov.noaa.ncdc.iosp.area.AreaIosp.class;
    }

    @Override
    public File getTestFile() {
        return new File(TEST_FILE);
    }
    
    



}
