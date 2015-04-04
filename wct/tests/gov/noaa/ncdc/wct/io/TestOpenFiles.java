package gov.noaa.ncdc.wct.io;

import gov.noaa.ncdc.wct.WCTIospManager;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.WCTDataUtils;
import gov.noaa.ncdc.wct.export.WCTExport;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.geotools.feature.IllegalAttributeException;
import org.junit.Test;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.FileWriter2.FileWriterProgressListener;
import ucar.nc2.NetcdfFile;


public class TestOpenFiles {

//	@Test
	public void testWctExportNative() throws WCTExportNoDataException, WCTExportException, DecodeException, 
		FeatureRasterizerException, IllegalAttributeException, InvalidRangeException, 
		DecodeHintNotSupportedException, URISyntaxException, ParseException, Exception {
		
		
		WCTExport exporter = new WCTExport();
		exporter.setOutputFormat(ExportFormat.NATIVE);
		File outFile = new File("E:\\work\\export\\goes12.2004.247.212513.BAND_01");
		URL dataURL = new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/testdata/goes/goes12.2004.247.212513.BAND_01");
		exporter.exportData(dataURL, outFile);
		
		if (! outFile.delete()) {
			throw new Exception("Could not delete exported file!  exists? "+outFile.exists());
		}
		
		if (WCTTransfer.isInCache(dataURL)) {
			System.out.println(dataURL+" :::: IS IN CACHE, deleting file...");
		
			// delete cache file
			File cacheFile = WCTTransfer.getFileInCache(dataURL);
			if (! cacheFile.delete()) {
				throw new Exception("Could not delete cache file!");
			}
			
		}
	}
	
	
//	@Test
	public void testDataScan() throws Exception {
		
		URL dataURL = new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/testdata/goes/goes12.2004.247.212513.BAND_01");
        URL cacheURL = WCTDataUtils.scan(dataURL, new FileScanner(), true, false, null, null, null);
        System.out.println("URL after scan: "+cacheURL);

		if (WCTTransfer.isInCache(dataURL)) {
			System.out.println(dataURL+" :::: IS IN CACHE, deleting file...");
		
			// delete cache file
			File cacheFile = WCTTransfer.getFileInCache(dataURL);
			if (! cacheFile.delete()) {
				throw new Exception("Could not delete cache file!");
			}
			
		}

	}
	

//	@Test
	public void testWctExportNativeNetcdf() throws WCTExportNoDataException, WCTExportException, DecodeException, 
		FeatureRasterizerException, IllegalAttributeException, InvalidRangeException, 
		DecodeHintNotSupportedException, URISyntaxException, ParseException, Exception {
		
		
		WCTExport exporter = new WCTExport();
		exporter.setOutputFormat(ExportFormat.RAW_NETCDF);
		File outFile = new File("E:\\work\\export\\goes12.2004.247.212513.BAND_01.nc");
		URL dataURL = new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/testdata/goes/goes12.2004.247.212513.BAND_01");
		exporter.exportData(dataURL, outFile);
		
		
		
		System.out.println("Deleting exported file: "+outFile);
		if (! outFile.delete()) {
			throw new Exception("Could not delete exported file!  exists? "+outFile.exists());
		}
		
		if (WCTTransfer.isInCache(dataURL)) {
			System.out.println(dataURL+" :::: IS IN CACHE, deleting file...");
		
			// delete cache file
			File cacheFile = WCTTransfer.getFileInCache(dataURL);
			System.out.println("Deleting cache file: "+cacheFile);
			if (! cacheFile.delete()) {
				System.out.println("Could not delete cache file!");
				throw new Exception("Could not delete cache file!");
			}
			
		}
	}

	
//	@Test
	public void testFileWriter2() throws Exception {
        ArrayList<FileWriterProgressListener> listeners = new ArrayList<FileWriterProgressListener>();
        
		URL remoteURL = new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/testdata/goes/goes12.2004.247.212513.BAND_01");
        URL dataURL = WCTTransfer.getURL(remoteURL, true); 
		File outFile = new File("E:\\work\\export\\goes12.2004.247.212513.BAND_01.nc");
        NetcdfFile ncIn = NetcdfFile.open(dataURL.toString());
        NetcdfFile ncOut = ucar.nc2.FileWriter2.writeToFile(ncIn, outFile.toString(), listeners);
        ncIn.close();
        ncOut.close();
        
		System.out.println("Deleting exported file: "+outFile);
		if (! outFile.delete()) {
			throw new Exception("Could not delete exported file!  exists? "+outFile.exists());
		}
		
		if (WCTTransfer.isInCache(remoteURL)) {
			System.out.println(remoteURL+" :::: IS IN CACHE, deleting file...");
		
			// delete cache file
			System.out.println("Deleting cache file: "+outFile);
			File cacheFile = WCTTransfer.getFileInCache(remoteURL);
			if (! cacheFile.delete()) {
				throw new Exception("Could not delete cache file!");
			}
			
		}

	}
	
	
	
	@Test
	public void testFileWriter2NoCache() throws Exception {
        ArrayList<FileWriterProgressListener> listeners = new ArrayList<FileWriterProgressListener>();
        
//        File inFile = new File("E:\\work\\export\\KGSP_V03_20101026_205047.nc");
//		File outFile = new File("E:\\work\\export\\KGSP_V03_20101026_205047-OUTPUT.nc");
		
        File dir = new File("E:\\work\\file-type-tests");
        
        WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.area.AreaIosp.class);
        
        FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return ! f.toString().endsWith("gbx8");
			}
        };
        
		for (File inFile : dir.listFiles(fileFilter)) {
			
			try {
				
				// in case last test was stopped midway
				if (! inFile.exists()) {
					continue;
				}
				
			
				System.out.println("File lock test - processing "+inFile);
				File outFile = new File(inFile.toString()+"-OUTPUT.nc");


				URL dataURL = inFile.toURI().toURL();
				NetcdfFile ncIn = NetcdfFile.open(dataURL.toString());
				NetcdfFile ncOut = ucar.nc2.FileWriter2.writeToFile(ncIn, outFile.toString(), listeners);
				//        NetcdfFile ncOut = ucar.nc2.FileWriter.writeToFile(ncIn, outFile.toString());
				ncIn.close();
				ncOut.close();

				System.out.println("Deleting exported file: "+outFile);
				if (! outFile.delete()) {
					throw new Exception("Could not delete exported file!  exists? "+outFile.exists());
				}


				System.out.println("Testing input file lock: "+inFile);
				File inFile2 = new File(inFile.toString()+"---test");
				System.out.println("Moving "+inFile+" to "+inFile2);
				FileUtils.moveFile(inFile, inFile2);
				System.out.println("Moving "+inFile2+" to "+inFile);
				FileUtils.moveFile(inFile2, inFile);


			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

		}

	}

}
