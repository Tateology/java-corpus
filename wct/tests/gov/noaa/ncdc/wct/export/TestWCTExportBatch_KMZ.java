package gov.noaa.ncdc.wct.export;

import java.io.File;

import org.junit.Test;

public class TestWCTExportBatch_KMZ {

	@Test
	public void testRadarKmzExport_1() {
		
		String dataURL = 
			"E:\\work\\tornado\\joplin\\data\\KSGF20110522_224348_V03.gz";
		
		WCTExportBatch.main( new String[] {
				dataURL,
				"E:\\work\\export\\wct-batch\\joplin-kmz-test.kmz",
				"KMZ",
				"E:\\work\\export\\wct-batch\\wctBatchConfig.xml" }
			);


	}
	
	@Test
	public void testRadarKmzExport_2() {
		
		String dataURL = 
			"ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.p94r1/SI.kgsp/sn.last";
		
		WCTExportBatch.main( new String[] {
				dataURL,
				"E:\\work\\export\\wct-batch\\kgsp-p94r1.kmz",
				"KMZ",
				"E:\\work\\export\\wct-batch\\wctBatchConfig.xml" }
			);


	}
	
	
	@Test
	public void testGridKmzExport_1() {
		String opendapURL = 
			"http://motherlode.ucar.edu/thredds/dodsC/fmrc/NCEP/RAP/CONUS_13km/files/RR_CONUS_13km_20130305_2200.grib2";
		
		WCTExportBatch.main( new String[] {
				opendapURL,
				"E:\\work\\export\\wct-batch\\grid-kmz-test.kmz",
				"KMZ",
				"E:\\work\\export\\wct-batch\\wctBatchConfig-RR_CONUS.xml" }
			);

	}
	
	@Test
	public void testGridKmzExport_2() {
		String url = 
			"ftp://ftp.ncep.noaa.gov/pub/data/nccf/com/cfs/prod/cfs/cfs.20130314/00/monthly_grib_01/flxf.01.2013031400.201307.avrg.grib.grb2";
		
		WCTExportBatch.main( new String[] {
				url,
				"E:\\work\\export\\wct-batch\\jack-cfs-kmz-test.kmz",
				"KMZ",
				"E:\\work\\export\\wct-batch\\wctBatchConfig-CFS.xml:::VARIABLE=Temperature" }
			);

	}

	
}
