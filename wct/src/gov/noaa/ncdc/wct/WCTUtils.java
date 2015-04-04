package gov.noaa.ncdc.wct;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import ucar.nc2.util.CancelTask;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class WCTUtils {

	//  public final static DecimalFormat sciFmt = new DecimalFormat("##0.#####E0");
	public final static DecimalFormat DECFMT_SCI = new DecimalFormat("##0.##E0");
	public final static DecimalFormat DECFMT_0D0000 = new DecimalFormat("0.0000");
	public final static DecimalFormat DECFMT_0D000 = new DecimalFormat("0.000");
	public final static DecimalFormat DECFMT_0D00 = new DecimalFormat("0.00");
	public final static DecimalFormat DECFMT_0D0 = new DecimalFormat("0.0");
	public final static DecimalFormat DECFMT_00 = new DecimalFormat("00");
	public final static DecimalFormat DECFMT_0 = new DecimalFormat("0");
	public final static DecimalFormat DECFMT_pDpppp = new DecimalFormat("#.####"); 
	public final static SimpleDateFormat ISO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    static {
    	ISO_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
    }




	static GeometryFactory geoFactory = new GeometryFactory();

	private static WCTCancelTask cancelTaskSingleton = null; 


	public static int max(int... values) {
		int returnValue = Integer.MIN_VALUE;
		for (int val : values) {
			returnValue = Math.max(returnValue, val);
		}
		return returnValue;
	}


	public static String getPaddedBitString(int value, int numBits) {   
		String binaryString = Integer.toBinaryString(value);
		// Add enough zeros so binaryString represents all 8 bits
		int binaryLength = binaryString.length();
		for (int n = 0; n < numBits - binaryLength; n++) {
			binaryString = "0" + binaryString;
		}
		return binaryString;
	}


	public static <T> T[] flipArray(T[] array) {
		//        System.out.println(Arrays.deepToString(array));
		ArrayList<T> list = new ArrayList<T>();
		for (int n=0; n<array.length; n++) {
			list.add(array[array.length-1-n]);
		}
		for (int n=0; n<array.length; n++) {
			array[n] = list.get(n);
		}
		//        System.out.println(Arrays.deepToString(array));
		return array;
	}

	/** List order maintained **/

	public static <T> void removeDuplicateWithOrder(ArrayList<T> arlList) {
		Set<T> set = new HashSet<T>();
		List<T> newList = new ArrayList<T>();
		for (Iterator<T> iter = arlList.iterator(); iter.hasNext(); ) {
			T element = iter.next();
			if (set.add(element)) {
				newList.add(element);
			}
		}
		arlList.clear();
		arlList.addAll(newList);
	}


	public static Rectangle2D.Double toRectangle(Envelope env) {
		return new Rectangle2D.Double(env.getMinX(), env.getMaxY(), env.getWidth(), env.getHeight());
	}

	public static Rectangle2D.Double toRectangle(org.geotools.resources.geometry.XRectangle2D env) {
		return new Rectangle2D.Double(env.getMinX(), env.getMaxY(), env.getWidth(), env.getHeight());
	}

	public static Rectangle2D.Double toRectangle(org.geotools.pt.Envelope env) {
		return new Rectangle2D.Double(env.getMinimum(0), env.getMinimum(1), env.getLength(0), env.getLength(1));
	}

	public static Rectangle2D.Double extentIntersection(Rectangle2D.Double extent1, Rectangle2D.Double extent2) {

		Envelope env1 = new Envelope(extent1.getMinX(), extent1.getMaxX(), extent1.getMinY(), extent1.getMaxY());
		Envelope env2 = new Envelope(extent2.getMinX(), extent2.getMaxX(), extent2.getMinY(), extent2.getMaxY());
		Geometry geom1 = geoFactory.toGeometry(env1);
		Geometry geom2 = geoFactory.toGeometry(env2);
		Geometry clippedGeom = geom1.intersection(geom2);
		Coordinate[] coords = clippedGeom.getCoordinates();
		Envelope env = new Envelope();
		for (Coordinate c : coords) {
			env.expandToInclude(c);
		}
		System.out.println("ENV 1: "+env1);
		System.out.println("ENV 2: "+env2);
		System.out.println("ENV F: "+env);
		return toRectangle(env);
	}



	/**
	 * Calculate a single progress from several levels of processes.  The processes can be thought of as loops within
	 * loops within loops, etc... , where index 0 is the outer most loop and index n-1 is the inner most loop.  
	 * @param processProgress  Can be thought of as the index in the loop
	 * @param processCompletion  Can be thought of as the loop size
	 * @return progress from 0.0 to 1.0 ( multiply by 100 for percent)
	 */
	public static double progressCalculator(int[] processProgress, int[] processCompletion) {
		double progress = 0.0;
		double progressPart = 0.0;
		for (int n=0; n<processProgress.length; n++) {
			progressPart = processProgress[n];
			for (int m=n; m>=0; m--) {
				progressPart *= (1.0/processCompletion[m]);
			}
			progress += progressPart;
		}
		return progress;
	}



	public static Color[] setAlphaValues(Color[] c, int alphaChannelValue) {

		for (int i=1; i<c.length; i++) {
			if (c[i].getAlpha() > alphaChannelValue) {
				c[i] = new Color(c[i].getRed(), c[i].getGreen(), c[i].getBlue(), alphaChannelValue);
			}
		}      
		return c;
	}

	public static Color[] applyAlphaFactor(Color[] c, int alphaChannelValue) {

		double factor = alphaChannelValue/255.0;

		for (int i=0; i<c.length; i++) {
			if (c[i].getAlpha() > alphaChannelValue) {
				c[i] = new Color(c[i].getRed(), c[i].getGreen(), c[i].getBlue(), (int)(c[i].getAlpha()*factor));
			}
		}      
		return c;
	}



	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {

		byte[] buffer = new byte[1024];
		int len;

		while((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}



	public static int[] getJavaVersionMajorMinorBugBuild() {
		String javaVersion = System.getProperty("java.version");
		System.out.println("Java Version: "+javaVersion);
		// 1.6.0_03
		// 1.6.0_10-rc
		String[] verArray = javaVersion.split("\\.");
		//        System.out.println(Arrays.toString(verArray));
		String[] bugBuildArray = verArray[2].split("_");

		int majorVersion = -1;
		int minorVersion = -1;
		int bugVersion = -1;
		int buildVersion = -1;

		try {
			majorVersion = Integer.parseInt(verArray[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			minorVersion = Integer.parseInt(verArray[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}    
		try {
			bugVersion = Integer.parseInt(bugBuildArray[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			buildVersion = Integer.parseInt(bugBuildArray[1].split("-")[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int[] majorMinorBugBuildArray = new int[] { 
				majorVersion, minorVersion,
				bugVersion, buildVersion
		};
		return majorMinorBugBuildArray;
	}


	public static WCTCancelTask getSharedCancelTask() {
		if (cancelTaskSingleton == null) {
			cancelTaskSingleton = new WCTCancelTask();
		}
		return cancelTaskSingleton;
		//		return new CancelTask() {
		//			public boolean isCancel() {
		//				return false;
		//			}
		//			public void setError(String arg0) {
		//			}
		//		};
	}



	public static java.awt.geom.Rectangle2D.Double adjustGeographicBounds(Dimension imageDimension, java.awt.geom.Rectangle2D.Double bounds) {

		double imgRatio = (double)imageDimension.getWidth() / (double)imageDimension.getHeight();
		double geoRatio = bounds.getWidth() / bounds.getHeight();

		double dlon = bounds.getWidth();
		double dlat = bounds.getHeight();
		double geoCenterX = bounds.getMinX() + (dlon / 2.0);
		double geoCenterY = bounds.getMinY() + (dlat / 2.0);

		double x = imgRatio / geoRatio;

		if (x > 1.0) {
			dlon = dlon * x;
		}
		else {
			dlat = dlat / x;
		}

		return new java.awt.geom.Rectangle2D.Double(geoCenterX - dlon / 2.0, 
				geoCenterY - dlat / 2.0, 
				dlon, dlat);

		//        return new ReferencedEnvelope(geoCenterX + dlon / 2.0, 
		//                geoCenterX - dlon / 2.0, 
		//                geoCenterY + dlat / 2.0, 
		//                geoCenterY - dlat / 2.0,
		//                bounds.getCoordinateReferenceSystem());

	}  //  end adjustGeographicBounds()



	/**
	 * Adjust dimension size so aspect ratio at provided extent (bounds) is equal.  Similar to 'adjustGeographicBounds',
	 * except that the bounds are preserved and dimension changed.
	 * @param bounds
	 * @param width
	 * @param height
	 * @return
	 */
	public static Dimension getEqualDimensions(java.awt.geom.Rectangle2D.Double bounds, int width, int height) {
		// All the data should start in the lower left corner.  Don't export what we don't need.
		double ratio = bounds.height / bounds.width;
		int ncols;
		int nrows;
		if (ratio < 1) {
			// wider than tall
			nrows = (int) (width * ratio);
			ncols = width;
		}
		else {
			nrows = height;
			ncols = (int) (height / ratio);
		}

		//        logger.info("             Original --- WIDTH: " + width + "   HEIGHT: " + height);
		//        logger.info("Bounds Ratio Adjusted --- WIDTH: " + ncols + "   HEIGHT: " + nrows);
		//        logger.info("Cellsize -- X: "+bounds.width/ncols+" Y: "+bounds.height/nrows);

		return new Dimension(ncols, nrows);
	}



//	public static String getXPathCData(Document doc, XPath xpath, String xpathString) throws XPathExpressionException {
//		
//		XPathExpression expr = xpath.compile(xpathString);
//		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
//
//		if (nodes.getLength() > 0) {
//			return nodes.item(0).getNodeValue().trim();
//		}
//		else {
//			return null;
//		}
//	}
	
	public static String getXPathValue(Document doc, XPath xpath, String xpathString) throws XPathExpressionException {
		XPathExpression expr = xpath.compile(xpathString);
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (nodes.getLength() > 0) {
			return nodes.item(0).getNodeValue().trim();
		}
		else {
			return null;
		}
	}

	public static ArrayList<String> getXPathValues(Document doc, XPath xpath, String xpathString) throws XPathExpressionException {
		XPathExpression expr = xpath.compile(xpathString);         
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		ArrayList<String> values = new ArrayList<String>(nodes.getLength());

		for (int i=0; i<nodes.getLength(); i++) {           
			if (nodes.item(i).getNodeValue() != null) {
				values.add(nodes.item(i).getNodeValue().trim());
			}
		}
		return values;
	}

	public static String getXPathInnerXML(Document doc, XPath xpath, String xpathString) throws XPathExpressionException {
		XPathExpression expr = xpath.compile(xpathString);
		Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
		return innerXml(node);
		
//		if (nodes.getLength() > 0) {
//			return innerXml(nodes.item(0));
//		}
//		else {
//			return null;
//		}
	}

	public static String innerXml(Node node) {
	    DOMImplementationLS lsImpl = (DOMImplementationLS)node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
	    LSSerializer lsSerializer = lsImpl.createLSSerializer();
	    lsSerializer.getDomConfig().setParameter("xml-declaration", false);
	    NodeList childNodes = node.getChildNodes();
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < childNodes.getLength(); i++) {
	       sb.append(lsSerializer.writeToString(childNodes.item(i)));
	    }
	    return sb.toString(); 
	}






	public static class WCTCancelTask implements CancelTask {

		private boolean cancel = false;
		int cnt = 0;

		@Override
		public boolean isCancel() {
			//			if (cancel) {
			//				System.out.println("is cancel is being called "+(cnt++)+"  cancel="+cancel);
			//				StackTraceElement[] ste = new Exception().getStackTrace();
			//				for (int i=0; i<ste.length; i++) {
			//					if (ste[i].toString().startsWith("gov.")) {
			//						System.out.println(ste[i]);
			//					}
			//				}
			//			}


			//			try {
			//				Thread.sleep(100);
			//			} catch (InterruptedException e) {
			//				e.printStackTrace();
			//			}
			if (cancel) {
				cancel = false;
				return true;
			}
			else {
				return false;
			}
		}

		public void setCancel(boolean cancel) {
			System.out.println("setCancel="+cancel+" is called");
			this.cancel = cancel;
		}


		@Override
		public void setError(String msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setProgress(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

	}
}
