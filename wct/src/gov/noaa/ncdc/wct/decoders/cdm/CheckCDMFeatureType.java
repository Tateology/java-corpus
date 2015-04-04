package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.WCTUtils;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;

import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class CheckCDMFeatureType {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: Argument 1 = file/dir to check");
			System.exit(9);
		}

		process(args[0]);
	}

	
	
	private static void process(String location) {
		File f = new File(location);
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				try {
					processFile(file.toString());
				} catch (Exception e) {
//					e.printStackTrace();
					System.out.println(file.toString()+" -- "+e.getMessage());
				}
			}
		}
		else {
			try {
				processFile(f.toString());
			} catch (Exception e) {
//				e.printStackTrace();
				System.out.println(f.toString()+" -- "+e.getMessage());
			}
		}
	}
	
	private static void processFile(String infile) throws IOException {
		Formatter fmter = new Formatter();
		FeatureDataset fd = (FeatureDataset) FeatureDatasetFactoryManager.open(null, infile, WCTUtils.getSharedCancelTask(), fmter);
		if (fd == null) {
			System.out.println(infile + " -- no CDM Feature Type detected.  Check CF-convetions!  -- "+fmter.toString());
//			throw new IOException("Data does not appear to be of any CDM type - check CF conventions! - "+fmter.toString());
		}
		else {
			System.out.println(infile + " -- CDM Feature Type: " + fd.getFeatureType());
		}
	}
}
