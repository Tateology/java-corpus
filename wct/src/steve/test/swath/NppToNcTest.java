package steve.test.swath;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class NppToNcTest {

	public static void main(String[] args) {
		
//		String[] s = new String[] {
//				"C:\\work\\npp\\GMTCO-VSSTO_npp_d20120425_t0756391_e0802195_b02553_c20120426143244193439_noaa_ops.h5",
//				"C:\\work\\npp\\GMTCO-VSSTO_npp_d20120425_t0756391_e0802195_b02553_c20120426143244193439_noaa_ops.nc",
//				"C:\\work\\npp\\474-00001-04-03_JPSS_CDFCB-X-Vol-IV-Part-3_F_VIIRS-SST-EDR-PP.xml"
//		};
		
//		String[] s = new String[] {
//				"C:\\work\\npp\\GMTCO-VSSTO_npp_d20120426_t0556450_e0602254_b02566_c20120427193220029403_noaa_ops.h5",
//				"C:\\work\\npp\\GMTCO-VSSTO_npp_d20120426_t0556450_e0602254_b02566_c20120427193220029403_noaa_ops.nc",
//				"C:\\work\\npp\\474-00001-04-03_JPSS_CDFCB-X-Vol-IV-Part-3_F_VIIRS-SST-EDR-PP.xml"
//		};
		
		File[] xmlFiles = new File("E:\\work2\\npp\\xml").listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.toString().endsWith(".xml") && f.toString().contains("_JPSS_")) {
					return true;
				}
				return false;
			}
		});
		
		String[] convArgs = new String[xmlFiles.length + 2];

		
		File dataDir = new File("E:\\work2\\npp\\testdata");
		for (File f : dataDir.listFiles()) {
			if (f.getName().endsWith(".h5")) {
				
				if (! f.getName().contains("SST")) {
					continue;
				}
				
				String name = f.toString();
				String ncfile = name.substring(0, name.length()-3)+".nc";

				convArgs[0] = f.toString();
				convArgs[1] = ncfile;
				for (int n=2; n<convArgs.length; n++) {
					convArgs[n] = xmlFiles[n-2].toString();
				}
				
				System.out.println(Arrays.toString(convArgs));
				
				NppToNc.main(convArgs);

			}
		}
		
	}
}
