package steve.test;

import gov.noaa.ncdc.nexrad.WCTLookAndFeel;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.io.File;
import java.net.URL;

import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

public class TestRadialDatasetOpenFiles {

	
	public static void main(String[] args) {
		for (int n=0; n<10; n++) {
			process(args);
		}
	}
	
	public static void process(String[] args) {
		
		try {
			
			RandomAccessFile.setDebugLeaks(true);
			
			// init cache and cache settings
			WCTConstants.getInstance();
			
			
			String source = new File("E:\\work\\l2bzip\\KMRX_20110513_2038").toURI().toURL().toString();
			
			CancelTask cancelTask = new CancelTask() {
				@Override
				public boolean isCancel() {
					return false;
				}
				@Override
				public void setError(String msg) {
				}
				@Override
				public void setProgress(String arg0, int arg1) {					
				}
			};
			StringBuilder errlog = new StringBuilder();
//			RadialDatasetSweep radialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(
//					ucar.nc2.constants.FeatureType.RADIAL, 
//					source, cancelTask, errlog);
//
//			radialDataset.getDataVariable("Reflectivity");
//			
//			RadialDatasetSweep radialDataset2 = (RadialDatasetSweep) TypedDatasetFactory.open(
//					ucar.nc2.constants.FeatureType.RADIAL, 
//					source, cancelTask, errlog);
//
//			((RadialVariable)radialDataset2.getDataVariable("Reflectivity")).readAllData();
//
//			
//			RadialDatasetSweep radialDataset3 = (RadialDatasetSweep) TypedDatasetFactory.open(
//					ucar.nc2.constants.FeatureType.RADIAL, 
//					source, cancelTask, errlog);


//
//			System.out.println("1: "+RandomAccessFile.getOpenFiles().size()+" :: "+RandomAccessFile.getOpenFiles());
//			
//
//			RadialDatasetSweepRemappedRaster raster = new RadialDatasetSweepRemappedRaster();
//			raster.setVariableName("Reflectivity");
//			raster.process(source);
			
			
			
//			System.out.println("2: "+RandomAccessFile.getOpenFiles().size()+" :: "+RandomAccessFile.getOpenFiles());
//
//			radialDataset3.close();
//			radialDataset.close();
//			radialDataset2.close();
			
			System.out.println("3: "+RandomAccessFile.getOpenFiles().size()+" :: "+RandomAccessFile.getOpenFiles());
			
			
			WCTLookAndFeel.configureUI();
			WCTViewer viewer = new WCTViewer();
			viewer.loadFile(new URL(source));
			

			System.out.println("4: "+RandomAccessFile.getOpenFiles().size()+" :: "+RandomAccessFile.getOpenFiles());

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
