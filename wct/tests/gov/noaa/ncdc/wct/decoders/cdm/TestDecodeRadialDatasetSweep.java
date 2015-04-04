package gov.noaa.ncdc.wct.decoders.cdm;


import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.decoders.sigmet.SigmetDataset;
import gov.noaa.ncdc.wct.decoders.sigmet.SigmetIOServiceProvider;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.Feature;
import org.junit.Test;

import ucar.nc2.NCdumpW;
import ucar.nc2.NetcdfFile;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.DebugFlags;

public class TestDecodeRadialDatasetSweep {

	@Test
	public void testLevel2NEXRAD() 
	throws InstantiationException, IOException, DecodeException, DecodeHintNotSupportedException, IllegalAccessException, ParseException {



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

		StringWriter sw = new StringWriter();
        NCdumpW.print(NetcdfFile.open(fileIn.toString()), "", sw, null);
        System.out.println(sw);
        
        
        CancelTask emptyCancelTask = new CancelTask() {
            public boolean isCancel() {
                return false;
            }
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
		
		System.out.println("volume scan start time: "+sweep.getStartingTime());
		
		float msecsDouble = sweep.getTime(0);
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putFloat(msecsDouble);
        buf.position(0);
		long msecsLong = buf.getLong();
		
		System.out.println("sweep start time (msecs since volume scan start time (float): "+msecsDouble);
        System.out.println("sweep start time (msecs since volume scan start time  (long): "+msecsLong);
		

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

//		for (int i = 0; i < nrays; i++) {
//		float azimuth = sweep.getAzimuth(i);
//		float elevation = sweep.getElevation(i);
//		float[] data = sweep.readData(i);

//		System.out.print("azim["+i+"] = "+azimuth);
//		System.out.print("   elev["+i+"] = "+elevation);
//		System.out.println("   sweep="+sweepNum+" data["+i+"].length = "+data.length + " :::  sweep.getGateNumber()="+sweep.getGateNumber());

//		}

		// uncomment below to continue
		//if (true) return;









		List  rvars = rds.getDataVariables();
		Iterator iter = rvars.iterator();

		/* information at sweep level */
//		for (int n=0; n<varRef.getNumSweeps(); n++) {






		// 2. Decode data as GeoTools Features and export to Shapefile
		DecodeRadialDatasetSweepHeader header = new DecodeRadialDatasetSweepHeader();
		header.setRadialDatasetSweep(rds);
		DecodeRadialDatasetSweep decoder = new DecodeRadialDatasetSweep(header);
		
		decoder.setRadialVariable(varRef);
		decoder.setDecodeHint("startSweep", new Integer(0));
		decoder.setDecodeHint("endSweep", new Integer(0));
		//decoder.setDecodeHint("startSweep", new Integer(100));
		//decoder.setDecodeHint("endSweep", new Integer(100));
		WCTFilter nxfilter = new WCTFilter();
		// ignore the -200 values (no data?)
		nxfilter.setMinValue(0.0);
		decoder.setDecodeHint("nexradFilter", nxfilter);

		// print available and default hints
		System.out.println(decoder.getDecodeHints());




		File outfile = new File("C:\\netcdf\\data\\shp\\testRadialCDM-nexrad.shp");
		//File outfile = new File("C:\\netcdf\\data\\shp\\testRadialCDM-sigmet.shp");
//		StreamingShapefileExport shpExport = new StreamingShapefileExport(outfile, NexradProjections.WGS84_WKT);


		StreamingProcess streamingProcess = new StreamingProcess() {
			public void addFeature(Feature feature)
			throws StreamingProcessException {

				if (Double.parseDouble(feature.getID()) % 10000 == 0) {
					System.out.println(feature);
				}
			}
			public void close() throws StreamingProcessException {
			}
		};



//		decoder.decodeData(new StreamingProcess[] { shpExport });
		decoder.decodeData(new StreamingProcess[] { streamingProcess });

//		}




	}













	@Test
	public void testSIGMET() 
	throws InstantiationException, IOException, DecodeException, DecodeHintNotSupportedException, IllegalAccessException, ParseException {



		// Switch use of lines below to switch between NEXRAD and SIGMET data
//		String fileIn = "C:\\work\\CanadianExample-200705162030\\sigmet.dat";
		String fileIn = "E:\\work\\Canadian\\data\\CONVOL.sig";
		//String fileIn = "C:\\netcdf\\data\\sig1.dat";

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

		for (VariableSimpleIF var : rds.getDataVariables()) {
		    System.out.println("Found variable: "+var.getName());
		}
		

		/* radial variable */
		// Switch use of lines below to switch between NEXRAD and SIGMET data
		RadialDatasetSweep.RadialVariable varRef = (RadialDatasetSweep.RadialVariable) rds.getDataVariable("TotalPower_sweep_2");



		// 1. Read data

		int sweepNum = 0;
		RadialDatasetSweep.Sweep sweep = varRef.getSweep(sweepNum);

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

		for (int i = 0; i < nrays; i++) {
		    float azimuth = sweep.getAzimuth(i);
		    float elevation = sweep.getElevation(i);
		    float[] data = sweep.readData(i);

		    System.out.print("azim["+i+"] = "+azimuth);
		    System.out.print("   elev["+i+"] = "+elevation);
		    System.out.println("   sweep="+sweepNum+" data["+i+"].length = "+data.length + " :::  sweep.getGateNumber()="+sweep.getGateNumber());

		}

		// uncomment below to continue
		//if (true) return;









		List  rvars = rds.getDataVariables();
		Iterator iter = rvars.iterator();

		/* information at sweep level */






		// 2. Decode data as GeoTools Features and export to Shapefile		
		DecodeRadialDatasetSweepHeader header = new DecodeRadialDatasetSweepHeader();
		header.setRadialDatasetSweep(rds);
		DecodeRadialDatasetSweep decoder = new DecodeRadialDatasetSweep(header);
		decoder.setRadialVariable(varRef);
		decoder.setDecodeHint("startSweep", new Integer(100));
		decoder.setDecodeHint("endSweep", new Integer(100));
		WCTFilter nxfilter = new WCTFilter();
		// ignore the -200 values (no data?)
		nxfilter.setMinValue(0.0);
		decoder.setDecodeHint("nexradFilter", nxfilter);

		// print available and default hints
		System.out.println(decoder.getDecodeHints());




		File outfile = new File("C:\\netcdf\\data\\shp\\testRadialCDM-sigmet.shp");
//		StreamingShapefileExport shpExport = new StreamingShapefileExport(outfile, NexradProjections.WGS84_WKT);


		StreamingProcess streamingProcess = new StreamingProcess() {
			public void addFeature(Feature feature)
			throws StreamingProcessException {

				if (Double.parseDouble(feature.getID()) % 10000 == 0) {
					System.out.println(feature);
				}
			}
			public void close() throws StreamingProcessException {
			}
		};



//		decoder.decodeData(new StreamingProcess[] { shpExport });
		decoder.decodeData(new StreamingProcess[] { streamingProcess });





	}


}
