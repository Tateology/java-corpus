package gov.noaa.ncdc.wct.decoders.cdm;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;

import ucar.nc2.NetcdfFile;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.DebugFlags;

public class RadialDatasetSweepTest {

    public static void main(String[] args) {
        try {
            runTest1();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void runTest1() throws IOException {
        // Switch use of lines below to switch between NEXRAD and SIGMET data
        String fileIn = "E:\\work\\dual-pole\\DP-2010-04-05-0406Z-0428Z\\KOUN20100405040607V06.raw";

        NetcdfFile.setDebugFlags(new DebugFlags() {
			@Override
            public boolean isSet(String flag) {
                if (flag.equals("NetcdfFile/debugSPI")) {
                    return true;
                }
                return false;
            }
			@Override
            public void set(String arg0, boolean arg1) {
            }
        });
    
        
        CancelTask emptyCancelTask = new CancelTask() {
			@Override
            public boolean isCancel() {
                return false;
            }
			@Override
            public void setError(String arg0) {
            }
			@Override
			public void setProgress(String arg0, int arg1) {				
			}
        };
        
        RadialDatasetSweep rds = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
        		FeatureType.RADIAL, fileIn, emptyCancelTask, new Formatter());


        /* radar information */
        String stationID      = rds.getRadarID();
        String stationName    = rds.getRadarName();
        boolean isVolume       = rds.isVolume();

        System.out.println("stationID = "+stationID);
        System.out.println("stationName = "+stationName);
        System.out.println("isVolume = "+isVolume);
        System.out.println("station location = "+rds.getCommonOrigin());
        
        List<VariableSimpleIF> varList = rds.getDataVariables();
        for (VariableSimpleIF var : varList) {
            RadialDatasetSweep.RadialVariable radialVar = (RadialDatasetSweep.RadialVariable) var;
            System.out.println("processing sweep variable: "+radialVar.getName());
            
            System.out.println("  - num sweeps: "+radialVar.getNumSweeps());
            for (int n=0; n<radialVar.getNumSweeps(); n++) {

                RadialDatasetSweep.Sweep sweep = radialVar.getSweep(n);
                System.out.println("  - sweep "+n+" mean elev: "+sweep.getMeanElevation()+
                        " num gates: "+sweep.getGateNumber()+" start time: "+sweep.getStartingTime());
                
                System.out.print(" reading data from sweep...  ");
                float[] data = sweep.readData();
                float minValue = Float.MAX_VALUE;
                float maxValue = Float.MIN_VALUE;
                for (float val : data) {
                    if (val > maxValue) maxValue = val;
                    if (val < minValue) minValue = val;
                }
                System.out.println("done.  min/max="+minValue+"/"+maxValue);
            }
            
        }

    }
}
