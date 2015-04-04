package gov.noaa.ncdc.iosp.station;

import gov.noaa.ncdc.wct.WCTUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Formatter;
import java.util.List;

import org.junit.Test;

import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;
import ucar.nc2.NCdumpW;
import ucar.nc2.NetcdfFile;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.unidata.geoloc.Station;


public class TestStationIosp {

	@Test
	public void testMetarNC() throws IllegalAccessException, InstantiationException, IOException {
		
//		NetcdfFile.registerIOProvider(IsdIOServiceProvider.class);
//		String infile = "testdata\\911650-22536-2001";
		String infile = "E:\\work\\station-data\\Surface_METAR_20101102_0000.nc";

		NCdumpW.print(infile+" -h", new OutputStreamWriter(System.out));
		if (true) return;
		
		Formatter fmter = new Formatter();
		FeatureDatasetPoint fdp = (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(null, infile, WCTUtils.getSharedCancelTask(), fmter);
		if (fdp == null) {
			System.out.println(fmter.toString());
			throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint' - "+fmter.toString());
		}
		System.out.println("STATIONS: "+fdp.getPointFeatureCollectionList().size());
		
		List<FeatureCollection> pfcList = fdp.getPointFeatureCollectionList();
		StationTimeSeriesFeatureCollection stsfc = (StationTimeSeriesFeatureCollection)(pfcList.get(0));
		List<Station> stationList = stsfc.getStations();
		
		for (Station station : stationList) {
			StationTimeSeriesFeature sf = stsfc.getStationFeature(station);
			
			System.out.println("Station: "+station.toString());
			System.out.println("Location: "+sf.getLatLon());

			PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
			// iterate through data for each station
			while (pfIter.hasNext()) {
				PointFeature pf = pfIter.next();

				System.out.println( pf.getObservationTimeAsDate() + " -- " + pf.getLocation().toString());
				StructureData sdata = pf.getData();
				StructureMembers smembers = sdata.getStructureMembers();
				System.out.println( smembers.getMemberNames().toString() );
				
				float data = sdata.getScalarFloat("air_temperature");
				System.out.println("air_temperature value: "+data);
				
			}
		}

		


//		PointFeatureCollectionIterator pfcIter = stsfc.getPointFeatureCollectionIterator(1*1024*1024);
//		
//		// loop stations
//		while (pfcIter.hasNext()) {
//			PointFeatureCollection pfc = pfcIter.next();
//
//			PointFeatureIterator pfIter = pfc.getPointFeatureIterator(1*1024*1024);
//			// iterate through data for each station
//			while (pfIter.hasNext()) {
//				PointFeature pf = pfIter.next();
//
//				System.out.println( pf.getObservationTimeAsDate() + " -- " + pf.getLocation().toString());
//				StructureData sdata = pf.getData();
//				StructureMembers smembers = sdata.getStructureMembers();
//				System.out.println( smembers.getMemberNames().toString() );
//				
//				float data = sdata.getScalarFloat("air_temperature");
//				System.out.println("air_temperature value: "+data);
//				
//			}
//		}


	}
	
	
//	@Test
	public void testISD() throws IllegalAccessException, InstantiationException, IOException {
		
		NetcdfFile.registerIOProvider(IsdIOServiceProvider.class);
		String infile = "testdata\\911650-22536-2001";

		Formatter fmter = new Formatter();
		FeatureDatasetPoint fdp = (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(null, infile, WCTUtils.getSharedCancelTask(), fmter);
		if (fdp == null) {
			System.out.println(fmter.toString());
			throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint' - "+fmter.toString());
		}
		System.out.println("STATIONS: "+fdp.getPointFeatureCollectionList().size());
		
		List<FeatureCollection> pfcList = fdp.getPointFeatureCollectionList();
		StationTimeSeriesFeatureCollection stsfc = (StationTimeSeriesFeatureCollection)(pfcList.get(0));
		List<Station> stationList = stsfc.getStations();
		
		for (Station station : stationList) {
			StationTimeSeriesFeature sf = stsfc.getStationFeature(station);
			
			System.out.println("Station: "+station.toString());
			System.out.println("Location: "+sf.getLatLon());

			PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
			// iterate through data for each station
			while (pfIter.hasNext()) {
				PointFeature pf = pfIter.next();

				System.out.println( pf.getObservationTimeAsDate() + " -- " + pf.getLocation().toString());
				StructureData sdata = pf.getData();
				StructureMembers smembers = sdata.getStructureMembers();
				System.out.println( smembers.getMemberNames().toString() );
				
				float data = sdata.getScalarFloat("air_temperature");
				System.out.println("air_temperature value: "+data);
				
			}
		}

	}

	
	
	
	
	
	
	
	
//	@Test
	public void testCRN() throws IllegalAccessException, InstantiationException, IOException {
		
		NetcdfFile.registerIOProvider(CrnIOServiceProvider.class);
		String infile = "testdata\\CRNDAILY01-2010-NC_Asheville_8_SSW.txt";

		Formatter fmter = new Formatter();
		FeatureDatasetPoint fdp = (FeatureDatasetPoint) FeatureDatasetFactoryManager.open(null, infile, WCTUtils.getSharedCancelTask(), fmter);
		if (fdp == null) {
			System.out.println(fmter.toString());
			throw new IOException("Data does not appear to be of CDM type 'FeatureDatasetPoint' - "+fmter.toString());
		}
		System.out.println("STATIONS: "+fdp.getPointFeatureCollectionList().size());
		
		List<FeatureCollection> pfcList = fdp.getPointFeatureCollectionList();
		StationTimeSeriesFeatureCollection stsfc = (StationTimeSeriesFeatureCollection)(pfcList.get(0));
		List<Station> stationList = stsfc.getStations();
		
		for (Station station : stationList) {
			StationTimeSeriesFeature sf = stsfc.getStationFeature(station);
			
			System.out.println("Station: "+station.toString());
			System.out.println("Location: "+sf.getLatLon());

			PointFeatureIterator pfIter = sf.getPointFeatureIterator(1*1024*1024);
			// iterate through data for each station
			while (pfIter.hasNext()) {
				PointFeature pf = pfIter.next();

				System.out.println( pf.getObservationTimeAsDate() + " -- " + pf.getLocation().toString());
				StructureData sdata = pf.getData();
				StructureMembers smembers = sdata.getStructureMembers();
				System.out.println( smembers.getMemberNames().toString() );
				
				for (String col : smembers.getMemberNames()) {
					String data = sdata.getScalarObject(col).toString();
					System.out.print(col+"="+data+" ");
				}
				System.out.println();
				
				
			}
		}

		
		
		NCdumpW.print(infile+" -h", new OutputStreamWriter(System.out));
	}

}
