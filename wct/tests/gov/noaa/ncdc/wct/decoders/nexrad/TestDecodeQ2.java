package gov.noaa.ncdc.wct.decoders.nexrad;

import java.io.File;

import org.junit.Test;

public class TestDecodeQ2 {

    @Test
    public static void testDecoder() {

        try {

            String inFile = "E:\\work\\qpe\\QPE_20060427-0000_t7_3d.netcdf";
            DecodeQ2 q2Decoder = new DecodeQ2();
            q2Decoder.decodeData(new File(inFile).toURI().toURL());
            // q2Decoder.getGridCoverage(0);
            DecodeQ2.describeQPEFile(inFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
