package steve.test;

import gov.noaa.ncdc.wct.WCTIospManager;

import java.io.File;

import ucar.nc2.FileWriter;
import ucar.nc2.NetcdfFile;

public class TestSigmetToNC {

    public static void main(String[] args) {
        try {
            String source = "testdata"+File.separator+"200705162030~~DOPVOL1_A-URP-XNC-RADAR-IRIS--20070516203241";
//            String source = "testdata"+File.separator+"200705162030~~CONVOL-URP-XNC-RADAR-IRIS--20070516203210";        
            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.wct.decoders.sigmet.SigmetIOServiceProvider.class);
//            FileWriter.writeToFile(NetcdfFile.open(source), "E:\\work\\Canadian\\nc\\convol.nc");
            FileWriter.writeToFile(NetcdfFile.open(source), "E:\\work\\Canadian\\nc\\XCAN20090101_000000");
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
