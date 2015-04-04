package gov.noaa.ncdc.wct.ui;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WctSessionManagerTest {

	private final static File OUT_SESSION_FILE = new File("C:\\work\\wct\\outexample.wctproj");
	private final static File IN_SESSION_FILE = new File("C:\\work\\wct\\inexample.wctproj");
	private static WCTViewer nexview;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		nexview = new WCTViewer();
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		nexview.dispose();
		System.exit(0);
	}

	@Test
	public void testSaveWctSession() throws IOException {
		WctSessionManager.saveWctSession(nexview, OUT_SESSION_FILE);
	}

	@Test
	public void testLoadWctSession() throws IOException, ClassNotFoundException {
		WctSessionManager.loadWctSession(nexview, IN_SESSION_FILE);
	}

//	@Test
//	public void testLoadObjectData() throws IOException, ClassNotFoundException {
//		WctSession wctSession = WctSessionManager.loadObjectData(IN_SESSION_FILE);
//		Assert.assertNotNull(wctSession);
//	}
//
//	@Test
//	public void testSaveObjectData() throws IOException {
//		WctSessionManager.saveObjectData(new WctSession(), OUT_SESSION_FILE);
//		Assert.assertTrue(OUT_SESSION_FILE.exists());
//	}

	
	
}
