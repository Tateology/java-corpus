package gov.noaa.ncdc.wct.unidata.contour;

import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;

import java.util.Iterator;
import java.util.Vector;

import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.TransformException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.NoninvertibleTransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class WCTContourFeatures {

	private final static GeometryFactory geoFactory = new GeometryFactory();

	private FeatureType schema;
	private int geoIndex = 0;

	private WCTContourGrid lastProcessedContourGrid;
	
	
	public WCTContourFeatures() throws CannotCreateTransformException, NoninvertibleTransformException, 
		SchemaException, FactoryException {
		
		init();
	}
	
	
	private void init() throws SchemaException, CannotCreateTransformException, 
		org.opengis.referencing.operation.NoninvertibleTransformException, org.opengis.referencing.FactoryException {
		
		AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Geometry.class);
		AttributeType level = AttributeTypeFactory.newAttributeType("value", Float.class, true, 5);
		AttributeType[] attTypes = {geom, level};
		schema = FeatureTypeFactory.newFeatureType(attTypes, "Contour Attributes");
	}


	public void process(final WCTContourGrid contourGrid, final StreamingProcess[] processArray) {
		
		this.lastProcessedContourGrid = contourGrid;
		
		StreamingContourProcess contourProcess = new StreamingContourProcess() {
			@Override
			public void processContourFeature(ContourFeature cf) {
				for (StreamingProcess sp : processArray) {
					try {
//						System.out.println(convertContourFeatureToMultiLine(cf));
						sp.addFeature(convertContourFeatureToMultiLine(cf));
					} catch (TransformException e) {
						e.printStackTrace();
					} catch (StreamingProcessException e) {
						e.printStackTrace();
					} catch (IllegalAttributeException e) {
						e.printStackTrace();
					}
				}
			}
			@Override
			public void finish() {
				for (StreamingProcess sp : processArray) {
					try {
						sp.close();
					} catch (StreamingProcessException e) {
						e.printStackTrace();
					}
				}		
			}
		};

		contourGrid.processContourLines(new StreamingContourProcess[] { contourProcess });
	}
	










	/**
	 * Convert each ContourFeature to a MultiLine geometry with attribute of contour level
	 * @param contourFeature
	 * @return
	 */
	public Feature convertContourFeatureToMultiLine(ContourFeature contourFeature)
		throws IllegalAttributeException, TransformException {

		double llXCorner = lastProcessedContourGrid.getInputRaster().getBounds().getMinX();
		double llYCorner = lastProcessedContourGrid.getInputRaster().getBounds().getMinY();
		double cellWidth = lastProcessedContourGrid.getInputRaster().getCellWidth();
		double cellHeight = lastProcessedContourGrid.getInputRaster().getCellHeight();
		
		// Convert each ContourLine to a LineString
		Iterator<ContourLine> iter = contourFeature.getGisParts();

		LineString[] lines = new LineString[contourFeature.getNumParts()];

		int contourCnt = 0;
		while (iter.hasNext()) {
			ContourLine contourLine = iter.next();
			double[] vertexX = contourLine.getX();
			double[] vertexY = contourLine.getY();
			Coordinate[] vertexCoords = new Coordinate[contourLine.getNumPoints()];

			for (int n=0; n<contourLine.getNumPoints(); n++) {
//				System.out.println(vertexX[n]+" , "+vertexY[n]);
//				reversed because this ncTools thinks this is lat/lon
				double x = vertexX[n]*cellWidth + llXCorner;
				double y = vertexY[n]*cellHeight + llYCorner;
				vertexCoords[n] = new Coordinate(x, y);
				System.out.println("geo contour coord: "+vertexCoords[n]);
			}
			//LinearRing lr = geoFactory.createLinearRing(vertexCoords);
			lines[contourCnt++] = geoFactory.createLineString(vertexCoords);

		}
		MultiLineString multiLine = geoFactory.createMultiLineString(lines);

		Feature feature = schema.create(
				new Object[]{multiLine,
						new Float(contourFeature.getContourValue()),
				}, new Integer(geoIndex++).toString());

		return feature;

	}

	/**
	 * Convert each ContourFeature to a MultiPolygon geometry with attribute of contour level
	 * @param contourFeature
	 * @return Feature that represents MultiPolygon of ContourFeature
	 */
	public Feature convertContourFeatureToMultiPoly(ContourFeature contourFeature)
	throws IllegalAttributeException, TransformException {

		// Convert each ContourLine to a LineString
		Iterator<ContourLine> iter = contourFeature.getGisParts();


		Vector<Polygon> polyVector = new Vector<Polygon>();

		while (iter.hasNext()) {
			ContourLine contourLine = iter.next();
			double[] vertexX = contourLine.getX();
			double[] vertexY = contourLine.getY();
			Coordinate[] vertexCoords = new Coordinate[contourLine.getNumPoints()+1];

			// can't create polygon out of non-closed ring
			if (contourLine.getNumPoints() > 3) {

				for (int n=0; n<contourLine.getNumPoints(); n++) {
					//System.out.println(vertexX[n]+" , "+vertexY[n]);
					//reversed because this ncTools thinks this is lat/lon
					//                  vertexCoords[n] = new Coordinate(vertexY[n], vertexX[n]);
//					vertexCoords[n] = convertStereoToLatLon(vertexY[n], vertexX[n]);
				}
				// make last coord the same as first to close ring
				vertexCoords[vertexCoords.length-1] = vertexCoords[0];


				LinearRing lr = geoFactory.createLinearRing(vertexCoords);
				polyVector.add(geoFactory.createPolygon(lr, null));

			}
		}
		Polygon[] polys = new Polygon[polyVector.size()];
		polys = (Polygon[])(polyVector.toArray(polys));


		MultiPolygon multiPoly = geoFactory.createMultiPolygon(polys);

		Feature feature = schema.create(
				new Object[]{multiPoly,
						new Float(contourFeature.getContourValue()),
				}, new Integer(geoIndex++).toString());

		return feature;

	}



}
