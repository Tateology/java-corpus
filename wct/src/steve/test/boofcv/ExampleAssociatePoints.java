//package steve.test.boofcv;
//
//import georegression.struct.point.Point2D_F64;
//
//import java.awt.image.BufferedImage;
//import java.util.ArrayList;
//import java.util.List;
//
//import boofcv.abst.feature.associate.GeneralAssociation;
//import boofcv.abst.feature.associate.ScoreAssociation;
//import boofcv.abst.feature.describe.DescribeRegionPoint;
//import boofcv.abst.feature.detect.interest.InterestPointDetector;
//import boofcv.core.image.ConvertBufferedImage;
//import boofcv.factory.feature.associate.FactoryAssociation;
//import boofcv.factory.feature.describe.FactoryDescribeRegionPoint;
//import boofcv.factory.feature.detect.interest.FactoryInterestPoint;
//import boofcv.gui.feature.AssociationPanel;
//import boofcv.gui.image.ShowImages;
//import boofcv.io.image.UtilImageIO;
//import boofcv.struct.FastQueue;
//import boofcv.struct.feature.TupleDesc;
//import boofcv.struct.feature.TupleDescQueue;
//import boofcv.struct.image.ImageFloat32;
//import boofcv.struct.image.ImageSingleBand;
//
//public class ExampleAssociatePoints<T extends ImageSingleBand, FD extends TupleDesc> {
//	 
//	// algorithm used to detect interest points
//	InterestPointDetector<T> detector;
//	// algorithm used to describe each interest point based on local pixels
//	DescribeRegionPoint<T, FD> describe;
//	// Associated descriptions together by minimizing an error metric
//	GeneralAssociation<FD> associate;
// 
//	// location of interest points
//	List<Point2D_F64> pointsA;
//	List<Point2D_F64> pointsB;
// 
//	Class<T> imageType;
// 
//	public ExampleAssociatePoints(InterestPointDetector<T> detector,
//								  DescribeRegionPoint<T, FD> describe,
//								  GeneralAssociation<FD> associate,
//								  Class<T> imageType) {
//		this.detector = detector;
//		this.describe = describe;
//		this.associate = associate;
//		this.imageType = imageType;
//	}
// 
//	/**
//	 * Detect and associate point features in the two images.  Display the results.
//	 */
//	public void associate( BufferedImage imageA , BufferedImage imageB )
//	{
//		T inputA = ConvertBufferedImage.convertFromSingle(imageA, null, imageType);
//		T inputB = ConvertBufferedImage.convertFromSingle(imageB, null, imageType);
// 
//		// stores the location of detected interest points
//		pointsA = new ArrayList<Point2D_F64>();
//		pointsB = new ArrayList<Point2D_F64>();
// 
//		// stores the description of detected interest points
//		FastQueue<FD> descA = new TupleDescQueue<FD>(describe,true);
//		FastQueue<FD> descB = new TupleDescQueue<FD>(describe,true);
// 
//		// describe each image using interest points
//		describeImage(inputA,pointsA,descA);
//		describeImage(inputB,pointsB,descB);
// 
//		// Associate features between the two images
//		associate.associate(descA,descB);
// 
//		// display the results
//		AssociationPanel panel = new AssociationPanel(20);
//		panel.setAssociation(pointsA,pointsB,associate.getMatches());
//		panel.setImages(imageA,imageB);
// 
//		ShowImages.showWindow(panel,"Associated Features");
//	}
// 
//	/**
//	 * Detects features inside the two images and computes descriptions at those points.
//	 */
//	private void describeImage(T input, List<Point2D_F64> points, FastQueue<FD> descs )
//	{
//		detector.detect(input);
//		describe.setImage(input);
// 
//		descs.reset();
// 
//		for( int i = 0; i < detector.getNumberOfFeatures(); i++ ) {
//			// get the feature location info
//			Point2D_F64 p = detector.getLocation(i);
//			double yaw = detector.getOrientation(i);
//			double scale = detector.getScale(i);
// 
//			// extract the description and save the results into the provided description
//			if( describe.isInBounds(p.x,p.y,yaw,scale)) {
//			   describe.process(p.x, p.y, yaw, scale, descs.pop());
//				points.add(p.copy());
//			}
//		}
//	}
// 
//	public static void main( String args[] ) {
// 
//		Class imageType = ImageFloat32.class;
// 
//		// select which algorithms to use
//		InterestPointDetector detector = FactoryInterestPoint.fastHessian(1, 2, 10, 1, 9, 4, 4);
//		DescribeRegionPoint describe = FactoryDescribeRegionPoint.surf(true, imageType);
////		DescribeRegionPoint describe = FactoryDescribeRegionPoint.brief(16,512,-1,4,true, imageType);
// 
//		ScoreAssociation scorer = FactoryAssociation.defaultScore(describe.getDescriptorType());
//		GeneralAssociation associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, -1, true);
// 
//		// load and match images
//		ExampleAssociatePoints app = new ExampleAssociatePoints(detector,describe,associate,imageType);
// 
////		BufferedImage imageA = UtilImageIO.loadImage("../data/evaluation/stitch/kayak_01.jpg");
////		BufferedImage imageB = UtilImageIO.loadImage("../data/evaluation/stitch/kayak_03.jpg");
////		BufferedImage imageA = UtilImageIO.loadImage("C:\\work\\boofcv\\ex-images\\KAPX_V06_20120827_024024_REF_0048-83230.png");
////		BufferedImage imageB = UtilImageIO.loadImage("C:\\work\\boofcv\\ex-images\\KAPX_V06_20120827_025206_REF_0048-85257.png");
//		
////		BufferedImage imageA = UtilImageIO.loadImage("C:\\work\\boofcv\\ex-images\\KAPX_V06_20120827_020519_REF_0049-54017.png");
////		BufferedImage imageB = UtilImageIO.loadImage("C:\\work\\boofcv\\ex-images\\KAPX_V06_20120827_021700_REF_0049-59031.png");
//		
//		BufferedImage imageA = UtilImageIO.loadImage("C:\\work\\boofcv\\ex-images\\KAPX_V06_20120827_020519_REF_0049-79627.png");
//		BufferedImage imageB = UtilImageIO.loadImage("C:\\work\\boofcv\\ex-images\\KAPX_V06_20120827_021700_REF_0049-81732.png");
//		
//		
//		
//		
//		
// 
//		app.associate(imageA,imageB);
//	}
//}