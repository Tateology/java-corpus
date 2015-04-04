package gov.noaa.ncdc.wct.export;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class WCTExportBatchTest {
	
	public final static String TEST_OUTPUT_DIR = "test-export-output";
	public final static String TEST_CONFIG_LOCATION = "build"+File.separator+"helphtml"+File.separator+"wctBatchConfig.xml";
	
	public String[] formats = new String[] { "shp", "wkt", "asc", "nc", "rnc" };
//	public String[] formats = new String[] { "rnc" };
	public File[] testFiles = new File[]{};
	
	@Before
	public void init() {
		File testdir = new File(TEST_OUTPUT_DIR);
		testdir.mkdir();
		
		testFiles = new File("testdata").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return ! (name.startsWith(".svn") || name.startsWith("2007"));
			}
			
		});
	}
	
	@After
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(new File(TEST_OUTPUT_DIR));
	}

	
	@Test
	public void testExport() {
	
		for (File file : testFiles) {				
		
//			WCTExportBatch.main( new String[] {
//				"http://mesonet.agron.iastate.edu/data/nexrd2/raw/KBYX/KBYX_20110113_1924",
//				TEST_OUTPUT_DIR+File.separator+"KBYX_20110113_1924",
//				format,
//				TEST_CONFIG_LOCATION	
//			} );

			for (String format : formats) {

				System.out.println(file);
				
			
				WCTExportBatch.main( new String[] {
					file.toString(),
					TEST_OUTPUT_DIR+File.separator+file.getName(),
					format,
					TEST_CONFIG_LOCATION
				} );

			}
		}
		
		
		
		
		
	}
	
	
}
