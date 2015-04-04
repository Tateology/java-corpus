package gov.noaa.ncdc.wct.morph;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

public class WCTMorphVectorsTest {

	
	@Test
	public void testRUC() throws ParseException, MalformedURLException, IOException {
		Date begDate = WCTMorphVectors.ISO_DATE_FORMAT.parse("2011-11-03T23:00:00Z");
		Date endDate = WCTMorphVectors.ISO_DATE_FORMAT.parse("2011-11-03T23:00:00Z");
		Rectangle2D.Double extent = new Rectangle2D.Double(-82.26243915240009, 34.86909948387037, 0.001, 0.001);		
		StringBuilder log = new StringBuilder();
		
		ArrayList<MorphVector> mvList = WCTMorphVectors.queryRUCStormMotion(begDate, endDate, extent, log);
		
		System.out.println(mvList);
	}
	
}
