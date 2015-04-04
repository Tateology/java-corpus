package steve.test;

import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.goes.GoesColorFactory;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;


public class ColorLutStuff {

	public static void main(String[] args) {

		//		Color[] c = NexradColorFactory.getColors("Reflectivity", false);
		//		double[] vals = NexradValueFactory.getProductMaxMinValues("Reflectivity", 12, false);
		//
		//		for (int n=c.length-1; n>=0; n--) {
		//			System.out.println("Color:  "+(vals[0]+(n*5))+"   "+c[n].getRed()+"  "+c[n].getGreen()+"  "+c[n].getBlue());
		//		}


		try {
			ncwmsPaletteStuff();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void ncwmsPaletteStuff() throws Exception {


		String source = 
			new File("E:\\work\\goes\\goes13.2010.308.214519.BAND_04").toURI().toURL().toString();


		GoesRemappedRaster goes = new GoesRemappedRaster();
		goes.setHeight(500);
		goes.setWidth(500);

		Rectangle2D.Double bounds = 
			new Rectangle2D.Double(-102.0, 17.0, 24.0, 24.0);

		goes.process(source, bounds);

		GoesColorFactory gcf = GoesColorFactory.getInstance();
		ColorsAndValues cav = gcf.readCmapColorsAndValues(goes);

//		Color[] colors = gcf.getEqualColorsAndValues().getColors();
//		Double[] values = gcf.getEqualColorsAndValues().getValues();
		Color[] colors = cav.getColors();
		Double[] values = cav.getValues();
		System.out.println(values.length+"=size ...  range: "+values[0]+" "+values[values.length-1]);
		for (int n=0; n<colors.length; n++) {
			System.out.println(
					colors[n].getRed()+" "+colors[n].getGreen()+" "+colors[n].getBlue()+" "+
//					colors[n].getAlpha());
					values[n]);
		}

	}

}
