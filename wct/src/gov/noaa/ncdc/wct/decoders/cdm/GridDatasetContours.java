package gov.noaa.ncdc.wct.decoders.cdm;
//package gov.noaa.ncdc.ndit.decoders.cdm;
//
//import gov.noaa.ncdc.ndit.decoders.nexrad.NexradProjections;
//import gov.noaa.ncdc.ndit.export.vector.StreamingShapefileExport;
//import gov.noaa.ncdc.projections.HRAPProjection;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.Vector;
//
//import org.geotools.ct.CannotCreateTransformException;
//import org.geotools.ct.MathTransform;
//import org.geotools.feature.AttributeType;
//import org.geotools.feature.AttributeTypeFactory;
//import org.geotools.feature.Feature;
//import org.geotools.feature.FeatureCollection;
//import org.geotools.feature.FeatureCollections;
//import org.geotools.feature.FeatureType;
//import org.geotools.feature.FeatureTypeFactory;
//import org.geotools.feature.IllegalAttributeException;
//import org.geotools.feature.SchemaException;
//import org.opengis.referencing.FactoryException;
//import org.opengis.referencing.operation.NoninvertibleTransformException;
//import org.opengis.referencing.operation.TransformException;
//
//import ucar.ma2.Array;
//import ucar.ma2.DataType;
//import ucar.ma2.Index;
//import ucar.nc2.dataset.NetcdfDataset;
//import ucar.nc2.dt.TypedDatasetFactory;
//import ucar.nc2.dt.grid.GeoGrid;
//import ucar.nc2.dt.grid.GridDataset;
//import ucar.nc2.util.CancelTask;
//
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.GeometryFactory;
//import com.vividsolutions.jts.geom.LineString;
//import com.vividsolutions.jts.geom.LinearRing;
//import com.vividsolutions.jts.geom.MultiLineString;
//import com.vividsolutions.jts.geom.MultiPolygon;
//import com.vividsolutions.jts.geom.Polygon;
//
//public class GridDatasetContours {
//
//    private final static GeometryFactory geoFactory = new GeometryFactory();
//    
//    private FeatureType schema;
//    private int geoIndex = 0;
//    
////    private final Stereographic stereoProj = new Stereographic(60.0, -105.0, 4.763);
////    private LatLonPointImpl llPnt = new LatLonPointImpl();
//    
//    private final NexradProjections nxProj = new NexradProjections();
//    private MathTransform nexradTransform = null;
//    private HRAPProjection hrapProj = new HRAPProjection();
//    
//    private FeatureCollection fc = FeatureCollections.newCollection();
//    
//    public GridDatasetContours() throws SchemaException, FactoryException, NoninvertibleTransformException, CannotCreateTransformException {
//        init();
//    }
//    
//    
//    
//    private void init() throws SchemaException, CannotCreateTransformException, org.opengis.referencing.operation.NoninvertibleTransformException, org.opengis.referencing.FactoryException {
//        AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Geometry.class);
//        AttributeType level = AttributeTypeFactory.newAttributeType("value", Float.class, true, 5);
//        AttributeType[] attTypes = {geom, level};
//        schema = FeatureTypeFactory.newFeatureType(attTypes, "Contour Attributes");
//
//        //nexradTransform = nxProj.getSphericalToWGS84Transform(6372000.0);
//        nexradTransform = nxProj.getWGS84ToSphericalTransform(6372000.0);
//    }
//    
//    
//    
//    
//    public void process(String inFile, String outDir) throws Exception {
//        
//        CancelTask emptyCancelTask = new CancelTask() {
//            public boolean isCancel() {
//                return false;
//            }
//            public void setError(String arg0) {
//            }
//        };
//
//        
//        //String inFile = "H:\\NetCDF\\Stage4\\ST4.2006040112.24h";
//        //String outDir = "H:\\NetCDF\\Stage4\\";
//        
//        //String inFile = ContourExport.class.getResource("/testdata/ST4.2006040112.24h").toString();
//        
//        
//        String inFilename = inFile.substring(inFile.lastIndexOf(File.separator)+1, inFile.length());
//       
//        File outFile = new File(outDir+inFilename+"_con_poly_hrap.shp");
//        StreamingShapefileExport exportShapefile = new StreamingShapefileExport(outFile);
//        File outFileLine = new File(outDir+inFilename+"_con_line_hrap.shp");
//        StreamingShapefileExport exportShapefileLine = new StreamingShapefileExport(outFileLine);
//        
//        
//        //NetcdfFile ncfileIn = NetcdfFile.open(inFile);
//        //Variable bref = ncfileIn.findVariable("Total_precipitation");
//        //Array data = bref.read();
//
//        StringBuffer errlog = new StringBuffer();
//        GridDataset gds = (GridDataset)TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, inFile, emptyCancelTask, errlog);
//        if (null == gds) { 
//            throw new Exception("Can't open GRID at location= "+inFile+"; error message= "+errlog);
//        }
////        gds.findGridDatatype("Total_precipitation");
//        GeoGrid geoGrid = gds.findGridByName("Total_precipitation");
//        System.out.println(geoGrid);
//        System.out.println(geoGrid.getInfo());
//        
//        Array data = geoGrid.readDataSlice(0, 0, -1, -1);
//        
//        System.out.println("NUMBER OF DIMENSIONS: "+data.getShape().length);
//        System.out.println("X DIM SIZE: "+geoGrid.getXDimension()+ "  Y DIM SIZE: "+geoGrid.getYDimension());
//        
//        NetcdfDataset dataset = gds.getNetcdfDataset();
////        List coordAxes = dataset.getCoordinateAxes();
////        for (int n=0; n<coordAxes.size(); n++) {
////            System.out.println("COORD AXES: "+coordAxes.get(n).toString());
////        }
//        Array xCoordArray = dataset.findCoordinateAxis("x").read();
//        Array yCoordArray = dataset.findCoordinateAxis("y").read();
//        Index xCoordIndex = xCoordArray.getIndex();
//        Index yCoordIndex = yCoordArray.getIndex();
//        double[] xCenters = new double[xCoordIndex.getShape()[0]];
//        double[] yCenters = new double[yCoordIndex.getShape()[0]];
//        
//        System.out.println("CENTERS SIZE: X="+xCenters.length+" Y="+yCenters.length);
//        for (int n=0; n<xCenters.length; n++) {
//            //xCenters[n] = xCoordArray.getFloat(xCoordIndex.set(n));
//            xCenters[n] = n+0.5;
//        }
//        for (int n=0; n<yCenters.length; n++) {
//            //yCenters[n] = yCoordArray.getFloat(yCoordIndex.set(n));
//            yCenters[n] = n+0.5;
//        }
//        
////      Scrub data!            
////        IndexIterator iter = data.getIndexIteratorFast();
////        while (iter.hasNext()) {
////            double val = iter.getDoubleNext();
////            if (val < 0 || val > 1000) {
////                iter.setDoubleCurrent(0.0);
////            }
////            else if (val == Double.NaN) {
////                iter.setDoubleCurrent(0.0);
////            }
////        }
//        
//        ArrayList<Double> contourLevels = new ArrayList<Double>();
//        contourLevels.add(new Double(5.0));
//        contourLevels.add(new Double(10.0));
//        contourLevels.add(new Double(20.0));
//        contourLevels.add(new Double(30.0));
//        contourLevels.add(new Double(40.0));
//        contourLevels.add(new Double(50.0));
//        
//        ContourGrid conGrid = new ContourGrid(data, contourLevels, yCenters, xCenters, geoGrid);
//        ArrayList<ContourFeature> contourList = conGrid.getContourLines();
//        
//        
////        Feature[] featureArray = new Feature[contourList.size()];
//        
//        System.out.println("NUMBER OF CONTOURS: "+contourList.size());
//        // Loop through list, create feature and add to collection
//        // This SHOULD always be in order of low to high contour level
//        for (int n=0; n<contourList.size(); n++) {
//            ContourFeature contour = contourList.get(n);
//            System.out.println("CONTOUR num="+n+
//                    " level="+contour.getContourValue()+
//                    " numLines="+contour.getNumParts());
//            
//            Feature feature = convertContourFeatureToMultiPoly(contour);
//            exportShapefile.addFeature(feature);
//            Feature featureLine = convertContourFeatureToMultiLine(contour);
//            exportShapefileLine.addFeature(featureLine);
//            
////            featureArray[n] = feature;
//        }
//        
//        
//        
//        // Loop through collection of features and substract each 
//        // higher contour level from lower contour level.
////        for (int n=0; n<featureArray.length-1; n++) {
////            
////            int numGeoms = featureArray[n].getDefaultGeometry().getNumGeometries();
////            if (numGeoms > 0) {
////                //System.out.println("NUM GEOMS: "+numGeoms);
////                
////                
////                System.out.println("SYMMETRIC DIFF: num="+n+
////                        " level="+featureArray[n].getAttribute("value"));
////                
////                try {
////                
////                    
////                    Geometry geom1 = featureArray[n].getDefaultGeometry();
////                    
////                    System.out.println(geom1.getClass());
////                    MultiPolygon mp = (MultiPolygon)geom1;
////                    for (int i=0; i<mp.getNumGeometries(); i++) {
////                        Polygon poly = (Polygon)mp.getGeometryN(i); 
////                        
////                    }
////                    
////                    Geometry geom2 = featureArray[n+1].getDefaultGeometry();
////                    Geometry diffGeom = geom1.symDifference(geom2);
////                    //Geometry diffGeom = geom1.difference(geom2);
////                    featureArray[n].setDefaultGeometry(diffGeom);
////
////                    
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
////            }
////        }
//        
//        
//        
//        System.out.println("FINISHING: "+outFile);
//        exportShapefile.close();
//        System.out.println("FINISHING: "+outFileLine);
//        exportShapefileLine.close();
//        
//
//        
//
//        
//    }
//    
//    
//    
//    /**
//     * Convert each ContourFeature to a MultiLine geometry with attribute of contour level
//     * @param contourFeature
//     * @return
//     */
//    private Feature convertContourFeatureToMultiLine(ContourFeature contourFeature)
//        throws IllegalAttributeException, TransformException {
//        
//        // Convert each ContourLine to a LineString
//        Iterator<ContourLine> iter = contourFeature.getGisParts();
//        
//        LineString[] lines = new LineString[contourFeature.getNumParts()];
//        
//        int contourCnt = 0;
//        while (iter.hasNext()) {
//            ContourLine contourLine = iter.next();
//            double[] vertexX = contourLine.getX();
//            double[] vertexY = contourLine.getY();
//            Coordinate[] vertexCoords = new Coordinate[contourLine.getNumPoints()];
//            
//            for (int n=0; n<contourLine.getNumPoints(); n++) {
//                //System.out.println(vertexX[n]+" , "+vertexY[n]);
//                //reversed because this ncTools thinks this is lat/lon
////                vertexCoords[n] = new Coordinate(vertexY[n], vertexX[n]);
//                vertexCoords[n] = convertStereoToLatLon(vertexY[n], vertexX[n]);
//            }
//            //LinearRing lr = geoFactory.createLinearRing(vertexCoords);
//            lines[contourCnt++] = geoFactory.createLineString(vertexCoords);
//                        
//        }
//        MultiLineString multiLine = geoFactory.createMultiLineString(lines);
//        
//        Feature feature = schema.create(
//                new Object[]{multiLine,
//                new Float(contourFeature.getContourValue()),
//                }, new Integer(geoIndex++).toString());
//        
//        return feature;
//        
//    }
//    
//    /**
//     * Convert each ContourFeature to a MultiPolygon geometry with attribute of contour level
//     * @param contourFeature
//     * @return Feature that represents MultiPolygon of ContourFeature
//     */
//    private Feature convertContourFeatureToMultiPoly(ContourFeature contourFeature)
//        throws IllegalAttributeException, TransformException {
//        
//        // Convert each ContourLine to a LineString
//        Iterator<ContourLine> iter = contourFeature.getGisParts();
//        
//        
//        Vector<Polygon> polyVector = new Vector<Polygon>();
//        
//        while (iter.hasNext()) {
//            ContourLine contourLine = iter.next();
//            double[] vertexX = contourLine.getX();
//            double[] vertexY = contourLine.getY();
//            Coordinate[] vertexCoords = new Coordinate[contourLine.getNumPoints()+1];
//            
//            // can't create polygon out of non-closed ring
//            if (contourLine.getNumPoints() > 3) {
//
//                for (int n=0; n<contourLine.getNumPoints(); n++) {
//                    //System.out.println(vertexX[n]+" , "+vertexY[n]);
//                    //reversed because this ncTools thinks this is lat/lon
////                    vertexCoords[n] = new Coordinate(vertexY[n], vertexX[n]);
//                    vertexCoords[n] = convertStereoToLatLon(vertexY[n], vertexX[n]);
//                }
//                // make last coord the same as first to close ring
//                vertexCoords[vertexCoords.length-1] = vertexCoords[0];
//                
//                
//                LinearRing lr = geoFactory.createLinearRing(vertexCoords);
//                polyVector.add(geoFactory.createPolygon(lr, null));
//                
//            }
//        }
//        Polygon[] polys = new Polygon[polyVector.size()];
//        polys = (Polygon[])(polyVector.toArray(polys));
//
//        
//        MultiPolygon multiPoly = geoFactory.createMultiPolygon(polys);
//        
//        Feature feature = schema.create(
//                new Object[]{multiPoly,
//                new Float(contourFeature.getContourValue()),
//                }, new Integer(geoIndex++).toString());
//        
//        return feature;
//        
//    }
//    
//
//    private Coordinate convertStereoToLatLon(double x, double y) throws TransformException {
//        // reproject the coordinate
//        //llPnt = (LatLonPointImpl)(stereoProj.projToLatLon(new ProjectionPointImpl(x, y), llPnt));
//        //return new Coordinate(llPnt.getLongitude(), llPnt.getLatitude());
////        double[] geoXY = (nexradTransform.transform(new DirectPosition2D(x, y), null)).getCoordinates();
//
//        
//        
//        
////        double[] geoXY = hrapProj.reverse(x, y);
////        geoXY = (nexradTransform.transform(new DirectPosition2D(geoXY[0], geoXY[1]), null)).getCoordinates();
////        return new Coordinate(geoXY[0], geoXY[1]);
//
//        x+=1.0;
//        y+=1.0;
//        
//        double stereoX = 4762.5 * (x - 401);
//        double stereoY = 4762.5 * (y - 1601);
//        return new Coordinate(stereoX/1000.0, stereoY/1000.0);
//
//        
//    }
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//
//}
