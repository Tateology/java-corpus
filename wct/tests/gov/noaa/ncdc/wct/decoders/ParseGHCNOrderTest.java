package gov.noaa.ncdc.wct.decoders;

import gov.noaa.ncdc.wct.decoders.ParseGHCNOrder.ParseGHCNOrderException;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.geotools.feature.Feature;
import org.junit.Assert;
import org.junit.Test;

public class ParseGHCNOrderTest {

	
	@Test
	public void testParseOrder() throws IOException, ParseGHCNOrderException, StreamingProcessException {
		URL url = new File("E:\\work\\station-data\\cdo\\5233.dat").toURI().toURL();

		ParseGHCNOrder order = new ParseGHCNOrder();
		StreamingProcess sp = new StreamingProcess() {
			@Override
			public void addFeature(Feature feature)
					throws StreamingProcessException {
				System.out.println(feature);
			}
			@Override
			public void close() throws StreamingProcessException {
			}
		};
		
		order.parseOrder(url, new StreamingProcess[] {  
				new StreamingShapefileExport(new File("E:\\work\\station-data\\cdo\\5233.dat.shp"), WCTProjections.NAD83_ESRI_PRJ) });
	}
	
	
	@Test
	public void testGetGeographicLocationColIndex() {
		ParseGHCNOrder order = new ParseGHCNOrder();
		String headerLine  = "STATION           STATION_NAME                                       GEOGRAPHIC_LOCATION             DATE     TMAX     TMIN     TOBS";
		String spacingLine = "----------------- -------------------------------------------------- ------------------------------- -------- -------- -------- --------";
		
		int index = order.getGeographicLocationColIndex(headerLine, spacingLine);
		
		Assert.assertEquals(2, index);
	}

	
	@Test
	public void testGetColumnIndexLocationFromLine() {
		ParseGHCNOrder order = new ParseGHCNOrder();
		String spacingLine = "----------------- -------------------------------------------------- ------------------------------- -------- -------- -------- --------";
		int[] colIndexLocations = order.getColumnIndexLocation(spacingLine);
		
		Assert.assertArrayEquals(new int[] { 0, 18, 69, 101, 110, 119, 128 }, colIndexLocations);
	}
	
	@Test
	public void testGetColumnIndexLocation() {
		
		// 012345678901234567890
		// --- ----- --
		
		ParseGHCNOrder order = new ParseGHCNOrder();
		String[] cols = new String[] { "---", "-----", "--" };
		int[] colIndexLocations = order.getColumnIndexLocation(cols);
		
		Assert.assertArrayEquals(new int[] { 0, 4, 10 }, colIndexLocations);
	}
	
	@Test
	public void testGetFixedWidthColumns() {
		ParseGHCNOrder order = new ParseGHCNOrder();
		String[] spacingCols = new String[] { "---", "-----", "--" };
		String line = "ABC ABCDE AB";
		
		String[] cols = order.getFixedWidthColumns(line, order.getColumnIndexLocation(spacingCols));
		
		Assert.assertArrayEquals(new String[] { "ABC", "ABCDE", "AB" }, cols);
	}
}
