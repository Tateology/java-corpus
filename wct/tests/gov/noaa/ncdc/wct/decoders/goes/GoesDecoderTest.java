package gov.noaa.ncdc.wct.decoders.goes;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.SchemaException;
import org.junit.Test;

public class GoesDecoderTest {

	@Test
	public void testGoesRemappedRaster() 
		throws IllegalAccessException, InstantiationException, 
		FactoryConfigurationError, SchemaException, IOException, DecodeException {

		GoesDecoder goes = new GoesDecoder();
		
		goes.addDataDecodeListener(new DataDecodeListener() {
		    public void metadataUpdate(DataDecodeEvent decodeEvent) {
		    }
		    
//			@Override
			public void decodeEnded(DataDecodeEvent event) {
				System.out.println("DECODE END");
			}
//			@Override
			public void decodeProgress(DataDecodeEvent event) {
				System.out.println("DECODE PROGRESS: "+event.getProgress());
			}
//			@Override
			public void decodeStarted(DataDecodeEvent event) {
				System.out.println("DECODE START");				
			}			
		});
		



//		String source = "E:\\work\\goes\\katrina\\goes12.2005.241.144513.BAND_04";
		String source = "C:\\work\\goes\\goes12.2004.247.212513.BAND_04";            
		Rectangle2D.Double bounds = 
			new Rectangle2D.Double(-102.0, 17.0, 24.0, 24.0);

		goes.setSource(source);
		goes.setBounds(bounds);

		File outFile = new File("C:\\work\\shptest\\goestest.shp");
		StreamingShapefileExport shpExport = new StreamingShapefileExport(outFile);
		
		goes.decodeData(new StreamingProcess[] { shpExport });

	}

}
