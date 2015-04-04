package steve.test;

import java.io.StringWriter;
import java.util.Formatter;

import ucar.nc2.NCdumpW;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

public class ReadNexradExample {

   public static void main(String[] args) {

      try {        

        String fileIn = "KPAH20080330_133313_V03";

        // print an 'ncdump' of the file
        StringWriter sw = new StringWriter();
        NCdumpW.print(fileIn, sw);
        System.out.println(sw);
      
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
       
        
        
        // open the file and represent as a 
        // RadialDatasetSweep object        
        RadialDatasetSweep rds = (RadialDatasetSweep)
        	FeatureDatasetFactoryManager.open(
               FeatureType.RADIAL, 
               fileIn, 
               emptyCancelTask,
               new Formatter()
            );


        // radar information
        String stationID      = rds.getRadarID();
        String stationName    = rds.getRadarName();
        boolean isVolume       = rds.isVolume();

        System.out.println("stationID = "+stationID);
        System.out.println("stationName = "+stationName);
        System.out.println("isVolume = "+isVolume);
        System.out.println("station location = "+
            rds.getCommonOrigin());



        // Read a radial variable
        RadialDatasetSweep.RadialVariable varRef =
            (RadialDatasetSweep.RadialVariable) 
               rds.getDataVariable("Reflectivity");


        // Read a single sweep
        int sweepNum = 0;
        RadialDatasetSweep.Sweep sweep = 
            varRef.getSweep(sweepNum);

        float meanElev = sweep.getMeanElevation();
        int nrays = sweep.getRadialNumber();
        float beamWidth = sweep.getBeamWidth();
        int ngates = sweep.getGateNumber();
        float gateSize = sweep.getGateSize();

        System.out.println("meanElev = "+meanElev);
        System.out.println("nrays = "+nrays);
        System.out.println("beamWidth = "+beamWidth);
        System.out.println("ngates = "+ngates);
        System.out.println("gateSize = "+gateSize);


         // Read data variable at radial level - 
         // this is where actual data is read 
         // into memory
         for (int i = 0; i < nrays; i++) {
             float azimuth = sweep.getAzimuth(i);
             float elevation = sweep.getElevation(i);
             float[] data = sweep.readData(i);
             // data.length should equal ngates

         }
         
      } catch (Exception e) {
          e.printStackTrace();
      }
   }

}
