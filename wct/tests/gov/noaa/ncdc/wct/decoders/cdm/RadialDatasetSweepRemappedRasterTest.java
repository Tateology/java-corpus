package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.decoders.sigmet.SigmetDataset;
import gov.noaa.ncdc.wct.decoders.sigmet.SigmetIOServiceProvider;

import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import org.junit.Test;

import ucar.nc2.NetcdfFile;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.RadialDatasetSweep.Sweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.DebugFlags;
import ucar.unidata.geoloc.ProjectionPointImpl;
import ucar.unidata.geoloc.projection.FlatEarth;

import com.vividsolutions.jts.index.strtree.SIRtree;

public class RadialDatasetSweepRemappedRasterTest {

    @Test
    public void testRange() {
        double lat1 = RadarHashtables.getSharedInstance().getLat("KGSP");
        double lon1 = RadarHashtables.getSharedInstance().getLon("KGSP");
        double lat2 = RadarHashtables.getSharedInstance().getLat("KGSP") - 0;
        double lon2 = RadarHashtables.getSharedInstance().getLon("KGSP") - 1;

        FlatEarth trans = new FlatEarth(lat1, lon1);
        ProjectionPointImpl projPoint = trans.latLonToProj(lat2, lon2);
        
        
        double distance = projPoint.distance(trans.getOriginLon(), trans.getOriginLat());
        //        double bearing = Math.atan2(
        //                Math.cos(lat1)*Math.sin(lat2)-Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1), 
        //                Math.sin(lon2-lon1)*Math.cos(lat2)
        //            );

        double azimuth = Math.toDegrees(Math.atan(projPoint.x/projPoint.y)); // reverse for meteorlogical coord system
        if (projPoint.y < 0) {
            azimuth += 180;
        }
        if (projPoint.y >= 0 && projPoint.x <= 0) {
            azimuth += 360;
        }

        System.out.println("distance: "+distance+"  azimuth: "+azimuth);
    }
    
    
    @Test
    public void testAzimuthIndex() throws IllegalAccessException, InstantiationException, IOException {
        Sweep sweep = getTestSweep();
        
        SIRtree azTree = RadialDatasetSweepRemappedRaster.getAzimuthLookupTree(sweep);
        
        
        System.out.println(azTree.query(130.7)); // should be 354
        System.out.println(azTree.query(138)); // should be 361

        System.out.println(new Date());
        
        System.out.println("Doing 800x800 lookup");
        for (int n=0; n<800*800; n++) {
            List<Integer> azLookupResults = azTree.query(Math.random()*360);
            if (azLookupResults.size() == 0) {
                continue;
            }
            int azIndex = azLookupResults.get(0);

        }
        System.out.println(new Date());
//        System.out.println("Doing 500x500 lookup");
//        for (int n=0; n<500*500; n++) {
//            List<Integer> azLookupResults = azTree.query(Math.random()*360);
//            if (azLookupResults.size() == 0) {
//                continue;
//            }
//            int azIndex = azLookupResults.get(0);
//
//        }
//        System.out.println("Doing 800x800 lookup");
//        for (int n=0; n<800*800; n++) {
//            List<Integer> azLookupResults = azTree.query(Math.random()*360);
//            if (azLookupResults.size() == 0) {
//                continue;
//            }
//            int azIndex = azLookupResults.get(0);
//
//        }
        
    }

    
    
    
    
    
    
    @Test
    public void testAzimuthLuts() throws IllegalAccessException, InstantiationException, IOException {
        Sweep sweep = getTestSweep();
        
        int[] azLuts = RadialDatasetSweepRemappedRaster.getAzimuthLookupArray(sweep);
        
        
//        System.out.println(azTree.query(130.7)); // should be 354
//        System.out.println(azTree.query(138)); // should be 361
        System.out.println(azLuts[(int)Math.round(130.7*100)]); // should be 354
        System.out.println(azLuts[(int)Math.round(138*100)]); // should be 361

        System.out.println(new Date());
        
        System.out.println("Doing 800x800 lookup");
        for (int n=0; n<800*800; n++) {
            int azIndex = azLuts[(int)Math.round(Math.random()*360*100)];
        }
        System.out.println(new Date());
//        System.out.println("Doing 500x500 lookup");
//        for (int n=0; n<500*500; n++) {
//            List<Integer> azLookupResults = azTree.query(Math.random()*360);
//            if (azLookupResults.size() == 0) {
//                continue;
//            }
//            int azIndex = azLookupResults.get(0);
//
//        }
//        System.out.println("Doing 800x800 lookup");
//        for (int n=0; n<800*800; n++) {
//            List<Integer> azLookupResults = azTree.query(Math.random()*360);
//            if (azLookupResults.size() == 0) {
//                continue;
//            }
//            int azIndex = azLookupResults.get(0);
//
//        }
        
    }


    
    
    
    
    
    @Test
    public void testDecode() throws Exception {
        
        for (int n=0; n<100; n++) {
            
            Thread.sleep(3000);
        
        System.out.println(new Date());
        String fileIn = "testdata\\KLIX20050829_135451.Z";
        RadialDatasetSweepRemappedRaster r = new RadialDatasetSweepRemappedRaster();
        r.setVariableName("Reflectivity");
        r.process(fileIn);
        System.out.println(new Date());
        
        
        }
    }
    
    
    
    
    
    private Sweep getTestSweep() throws IllegalAccessException, InstantiationException, IOException {

        // Switch use of lines below to switch between NEXRAD and SIGMET data
        String fileIn = "testdata\\KLIX20050829_135451.Z";

        NetcdfFile.setDebugFlags(new DebugFlags() {
            public boolean isSet(String flag) {
                if (flag.equals("NetcdfFile/debugSPI")) {
                    return true;
                }
                return false;
            }
            public void set(String arg0, boolean arg1) {
                // TODO Auto-generated method stub

            }
        });

        NetcdfFile.registerIOProvider(SigmetIOServiceProvider.class);
        FeatureDatasetFactoryManager.registerFactory(FeatureType.RADIAL, SigmetDataset.class);       
        
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



        RadarHashtables nxhash = RadarHashtables.getSharedInstance();
        System.out.println("nxhash location = "+nxhash.getLat(stationID)+" "+nxhash.getLon(stationID)+" "+nxhash.getElev(stationID));


        /* radial variable */
        // Switch use of lines below to switch between NEXRAD and SIGMET data
        RadialDatasetSweep.RadialVariable varRef = (RadialDatasetSweep.RadialVariable) rds.getDataVariable("Reflectivity");


        // 1. Read data

        int sweepNum = 2;
        RadialDatasetSweep.Sweep sweep = varRef.getSweep(sweepNum);

        return sweep;
    }
    
    
    
    
    public static void main(String[] args) {
        
        RadialDatasetSweepRemappedRasterTest t = new RadialDatasetSweepRemappedRasterTest();
        try {
            t.testDecode();
//            t.testAzimuthIndex();
//            t.testAzimuthLuts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}


