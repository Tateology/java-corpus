package gov.noaa.ncdc.wct;

import java.util.ArrayList;
import java.util.logging.Logger;

import ucar.nc2.NetcdfFile;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordSysBuilder;
import ucar.nc2.dt.TypedDatasetFactory;

public class WCTIospManager {


    private static final Logger logger = Logger.getLogger(WCTIospManager.class.getName());
	
    private static WCTIospManager singleton = null;
    
    ArrayList<Class> iospList = new ArrayList<Class>();
    ArrayList<Class> typedDatasetFactoryList = new ArrayList<Class>();
    
    
    private WCTIospManager() {        
    }

    public static WCTIospManager getInstance() {
        if (singleton == null) {
            singleton = new WCTIospManager();
        }
        return singleton;
    }

    
    
    public void registerIosp(Class c) throws IllegalAccessException, InstantiationException {
        logger.info("IOSP registration: "+c.getName());
//        if (c.getName().equals("gov.noaa.ncdc.iosp.area.AreaIosp")) {
//            System.out.println("IOSP override - ignoring registration of "+ c.toString());
//            return;
//        }
        
        
        
        if (! iospList.contains(c)) {
            NetcdfFile.registerIOProvider(c);
            iospList.add(c);
        }
    }
    

    public void registerTypedDatasetFactory(FeatureType featureType, Class c) throws IllegalAccessException, InstantiationException {
        if (! typedDatasetFactoryList.contains(c)) {
            TypedDatasetFactory.registerFactory(featureType, c);
            typedDatasetFactoryList.add(c);
        }
    }

    public static void registerWctIOSPs() throws InstantiationException {

        String errors = "";
 
//        try {
//            CoordSysBuilder.registerConvention("NPP", steve.test.swath.NPPConvention.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            errors += "Error registering convention steve.test.swath.NPPConvention.class\n";
//        }
//        try {
//        	WCTIospManager.getInstance().registerIosp(steve.test.swath.NPPIosp.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            errors += "Error registering iosp steve.test.swath.NPPIosp.class\n";
//        }

        
        
        try {
            CoordSysBuilder.registerConvention("PATMOS", gov.noaa.ncdc.wct.decoders.cdm.conv.PATMOSConvention.class);
        } catch (Exception e) {
            e.printStackTrace();
            errors += "Error registering convention gov.noaa.ncdc.wct.decoders.cdm.conv.PATMOSConvention.class\n";
        }

        try {
            CoordSysBuilder.registerConvention("TAFBMarineForecast", gov.noaa.ncdc.wct.decoders.cdm.conv.TAFBMarineForecastConvention.class);
        } catch (Exception e) {
            e.printStackTrace();
            errors += "Error registering convention gov.noaa.ncdc.wct.decoders.cdm.conv.TAFBMarineForecastConvention.class\n";
        }

        try {
            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.area.AreaIosp.class);
        } catch (Exception e) {
            e.printStackTrace();
            errors += "Error registering gov.noaa.ncdc.iosp.area.AreaIosp\n";
        }


//        try {
//            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.avhrr.AvhrrGacKLMIosp.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            errors += "Error registering gov.noaa.ncdc.iosp.avhrr.AvhrrGacKLMIosp\n";
//        }
//        try {
//            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.avhrr.ver1.gac.AvhrrGacVer1Iosp.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            errors += "Error registering gov.noaa.ncdc.iosp.avhrr.ver1.gac.AvhrrGacVer1Iosp\n";
//        }
//        try {
//            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.avhrr.ver1.lac.AvhrrLacVer1Iosp.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            errors += "Error registering gov.noaa.ncdc.iosp.avhrr.ver1.lac.AvhrrLacVer1Iosp\n";
//        }


//        try {
//        	WCTIospManager.getInstance().registerIosp(ucar.nc2.iosp.sigmet.SigmetIOServiceProvider.class);
//        } catch (Exception e) {
//        	e.printStackTrace();
//        	errors += "Error registering gov.noaa.ncdc.wct.decoders.sigmet.SigmetIOServiceProvider.class\n";
//        }

        
//        try {
//            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.isccp.IsccpDxIosp.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            errors += "Error registering gov.noaa.ncdc.iosp.isccp.IsccpDxIosp.class\n";
//        }
//        try {
//            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.isccp.IsccpD1Iosp.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            errors += "Error registering gov.noaa.ncdc.iosp.isccp.IsccpD1Iosp.class\n";
//        }

        
        try {
            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.wct.decoders.sigmet.SigmetIOServiceProvider.class);
        } catch (Exception e) {
            e.printStackTrace();
            errors += "Error registering gov.noaa.ncdc.wct.decoders.sigmet.SigmetIOServiceProvider.class\n";
        }
        try {
            WCTIospManager.getInstance().registerTypedDatasetFactory(ucar.nc2.constants.FeatureType.RADIAL, gov.noaa.ncdc.wct.decoders.sigmet.SigmetDataset.class);
        } catch (Exception e) {
            e.printStackTrace();
            errors += "Error registering gov.noaa.ncdc.wct.decoders.sigmet.SigmetDataset.class\n";
        }


        try {
        	WCTIospManager.getInstance().registerIosp(ucar.nc2.iosp.gempak.GempakGridServiceProvider.class);
        } catch (Exception e) {
        	e.printStackTrace();
        	errors += "Error registering ucar.nc2.iosp.gempak.GempakGridServiceProvider.class\n";
        }

        
        try {
            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.station.CrnIOServiceProvider.class);
        } catch (Exception e) {
            e.printStackTrace();
            errors += "Error registering gov.noaa.ncdc.iosp.station.CrnIOServiceProvider.class\n";
        }

        
        try {
            WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.station.IsdIOServiceProvider.class);
        } catch (Exception e) {
            e.printStackTrace();
            errors += "Error registering gov.noaa.ncdc.iosp.station.IsdIOServiceProvider.class\n";
        }


        try {
            WCTIospManager.getInstance().registerIosp(ucar.nc2.iosp.nexrad2.WCTNexrad2IOServiceProvider.class);
        } catch (Exception e) {
            e.printStackTrace();
            errors += "Error registering convention ucar.nc2.iosp.nexrad2.WCTNexrad2IOServiceProvider.class\n";
        }

       
        
        if (errors.length() > 0) {
            throw new InstantiationException(errors);
        }
        
    }
    

}
