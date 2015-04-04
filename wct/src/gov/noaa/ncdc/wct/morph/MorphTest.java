package gov.noaa.ncdc.wct.morph;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.Vector;

import javax.media.jai.RasterFactory;

public class MorphTest {

	
	public static void main(String[] args) {
		
		try {
			
//			BufferedImage startImage = ImageIO.read(new File("E:\\work\\morph\\KBMX_V03_20110427_212443_REF_0053-59875.png"));
//			BufferedImage endImage = ImageIO.read(new File("E:\\work\\morph\\KBMX_V03_20110427_212918_REF_0053-63281.png"));
			
			WritableRaster startImage = RasterFactory.createBandedRaster(
                    DataBuffer.TYPE_FLOAT, 100, 100, 1, null);
			
			WritableRaster endImage = RasterFactory.createBandedRaster(
                    DataBuffer.TYPE_FLOAT, 100, 100, 1, null);
			
			
			int numberOfMorphSteps = 20;
			
			
			
			Vector<Point> lMeshList = new Vector<Point>();
			Vector<Point> lPolyList = new Vector<Point>();
			lMeshList.add(new Point(0, 0));
			lMeshList.add(new Point(0, startImage.getHeight()));
			lMeshList.add(new Point(startImage.getWidth(), startImage.getHeight()));
			lMeshList.add(new Point(startImage.getWidth(), 0));
			lMeshList.add(new Point(startImage.getWidth()/2, startImage.getHeight()/2));

			lPolyList.add(new Point(0, 0));
			lPolyList.add(new Point(0, startImage.getHeight()));
			lPolyList.add(new Point(startImage.getWidth(), startImage.getHeight()));
			lPolyList.add(new Point(startImage.getWidth(), 0));

			Vector<Point> rMeshList = new Vector<Point>();
			Vector<Point> rPolyList = new Vector<Point>();

			rMeshList.add(new Point(0, 0));
			rMeshList.add(new Point(0, endImage.getHeight()));
			rMeshList.add(new Point(endImage.getWidth(), endImage.getHeight()));
			rMeshList.add(new Point(endImage.getWidth(), 0));
			rMeshList.add(new Point(endImage.getWidth()/2+20, endImage.getHeight()/2+20));

			rPolyList.add(new Point(0, 0));
			rPolyList.add(new Point(0, endImage.getHeight()));
			rPolyList.add(new Point(endImage.getWidth(), endImage.getHeight()));
			rPolyList.add(new Point(endImage.getWidth(), 0));

			
			
			MorphInput startInput = new MorphInput(lMeshList, lPolyList, startImage);
			MorphInput endInput = new MorphInput(rMeshList, rPolyList, endImage);
			
			Config config = new Config(startInput, endInput, numberOfMorphSteps);
			
			Triangulation triangulation = new Triangulation(config);
			triangulation.triangulate();
			
			MorphOperator op = new MorphOperator(config);
			op.run();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
