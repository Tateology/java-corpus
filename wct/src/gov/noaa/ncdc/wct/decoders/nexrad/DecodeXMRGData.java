/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.projections.HRAPProjection;
import gov.noaa.ncdc.projections.Stereo2LatLon;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Decodes NWS XMRG Data into JTS Features and XMRGRaster object.
 *
 * @author     steve.ansari
 * @created    Feb 15, 2005
 */
public class DecodeXMRGData implements StreamingRadialDecoder {


    /**
     *  Description of the Field
     */
    public final static int MM = 0, INCH = 1;

    private FeatureCollection features = FeatureCollections.newCollection();
    private FeatureType schema = null;
    private GeometryFactory geoFactory = new GeometryFactory();
    private java.awt.geom.Rectangle2D.Double wsrBounds;

    private Map<String, Object> hintsMap;

    private DecodeXMRGHeader header;


    private Vector<Polygon>[] polyVector = new Vector[16];
    private Vector<Coordinate> coordinates = new Vector<Coordinate>();


    private XMRGRaster xmrgRaster = null;




    // The list of event listeners.
    private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();






    /**
     *Constructor for the DecodeXMRGData object
     *
     * @param  header  A DecodeXMRGHeader object
     */
    public DecodeXMRGData(DecodeXMRGHeader header) {
        this(header, null);
    }

    /**
     *Constructor for the DecodeXMRGData object with supplied FeatureCollection
     *
     * @param  header    A DecodeXMRGHeader object
     * @param  features  FeatureCollection to populate
     */
    public DecodeXMRGData(DecodeXMRGHeader header, FeatureCollection features) {
        this.header = header;
        this.features = features;
        if (features == null) {
            FeatureCollections.newCollection();
        }
        init();
    }


    private void init() {
        try {
            hintsMap = new HashMap<String, Object>();

            // TODO: instead of using the NexradFilter object, use the hints map
            // to define the attributes managed by the NexradFilter class 
            hintsMap.put("nexradFilter", new WCTFilter());

            // Use JTS Geometry.buffer(0.0) to combine adjacent polygons
            hintsMap.put("reducePolys", new Boolean(false));

            hintsMap.put("classify", new Boolean(false));


            AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Geometry.class);
            AttributeType value = AttributeTypeFactory.newAttributeType("value", Float.class, true, 5);
            AttributeType colorIndex = AttributeTypeFactory.newAttributeType("colorIndex", Integer.class, true, 4);
            AttributeType[] attTypes = {geom, value, colorIndex};
            schema = FeatureTypeFactory.newFeatureType(attTypes, "Nexrad Attributes");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // initialize the dpa raster object
        xmrgRaster = new XMRGRaster();

    }


    /**
     * Get the key-value pairs for the current decode hints.  
     * If no hints have been set, this will return the supported
     * hints with default values.
     * @return
     */
    public Map<String, Object> getDecodeHints() {
        return hintsMap;
    }

    /**
     * Set a decodeHint.  To get a list of supported hints and default values,
     * use 'getDecodeHints()'.  The currently supported hints are as follows: <br><br>
     * <ol>
     *  <li> <b>nexradFilter</b>: 
     *          NexradFilter object that defines filtering options on range, azimuth, 
     *          height and geographic bounds.
     *  <li> <b>reducePolys</b>: 
     *          Reduce polygons for Level-III classification groups using a JTS buffer(0.0) command.
     * @param hintsMap
     */
    public void setDecodeHint(String hintKey, Object hintValue) throws DecodeHintNotSupportedException {
        if (! hintsMap.keySet().contains(hintKey)) {
            throw new DecodeHintNotSupportedException(this.getClass().toString(), hintKey, hintsMap);
        }
        hintsMap.put(hintKey, hintValue);
    }




    /**
     * Returns the feature types used for these features
     * 
     * @return The featureType value
     */
    public FeatureType[] getFeatureTypes() {
        return new FeatureType[] {schema};
    }


    /**
     * Returns Rectangle.Double Bounds for the NEXRAD Site calculated during decode. (unique to product)
     * Could be 248, 124 or 32 nmi.
     *
     * @return    The bounds value
     */
    public java.awt.geom.Rectangle2D.Double getBounds() {
        return (MaxGeographicExtent.getNexradExtent(header));
        //Envelope env = features.getBounds();
        //return (new java.awt.geom.Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight()));
    }






    /**
     * Decodes data and stores with in-memory FeatureCollection
     * @return
     * @throws DecodeException
     * @throws IOException 
     */
    public void decodeData() throws DecodeException, IOException {
        if (features == null) {
            features = FeatureCollections.newCollection();
        }
        features.clear();

        StreamingProcess process = new StreamingProcess() {
            public void addFeature(Feature feature) throws StreamingProcessException {
                features.add(feature);
            }
            public void close() throws StreamingProcessException {
                System.out.println("STREAMING PROCESS close() ::: fc.size() = "+features.size());
            }           
        };

        decodeData(new StreamingProcess[] { process } );

    }


    public void decodeData(StreamingProcess[] processArray) throws DecodeException, IOException {
        decodeData(processArray, true);
    }


    public void decodeData(StreamingProcess[] processArray, boolean autoClose)
    throws DecodeException, IOException {


        WCTFilter nxfilter = (WCTFilter)hintsMap.get("nexradFilter");
        boolean reducePolys = (Boolean)hintsMap.get("reducePolys");
        boolean classify = (Boolean)hintsMap.get("classify");



        DataDecodeEvent event = new DataDecodeEvent(this);

        // Start decode
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            ((DataDecodeListener) listeners.get(i)).decodeStarted(event);
        }











//      System.out.println(nxfilter.toString());      

        features = FeatureCollections.newCollection();
        //features.clear();

        for (int i = 0; i < 16; i++) {
            polyVector[i] = new Vector<Polygon>();
        }


        // Reset index counter
        int geoIndex = 0;

        // Initiate binary buffered read
        ucar.unidata.io.RandomAccessFile f = header.getRandomAccessFile();         

        int pcode = header.getProductCode();
        try {

            // Check for type of data: xmrg
            if (header.getProductType() != NexradHeader.XMRG) {
                throw new DecodeException("This data is not in the XMRG format.", header.getDataURL());
            }

            ((DecodeXMRGHeader)header).setDataThresholdStringArray(
                    new String[] {"0", "< 0.10", "0.25", "0.50", "0.75", "1.00",
                            "1.50", "2.00", "2.50", "3.00", "4.00", "> 4.00", "","","","" } );


            // reinitialize xmrgRaster object
            xmrgRaster.init((DecodeXMRGHeader)header);


            // Set up units conversion
            //double unitsFactor = 1.0; // Default for MM 
            //if(units == INCH)
            //   unitsFactor = (1.0/25.4); 
            double unitsFactor = (1.0/25.4); 

            int llHrapX = header.getLlHrapX();
            int llHrapY = header.getLlHrapY();            
            int numHrapX = header.getNumHrapX();
            int numHrapY = header.getNumHrapY();



            // Convert from Spherical to WGS84 Datum (for DPA Grid Cells)
            //MathTransform sphericalToWGS84Transform = nexradProjection.getSphericalToWGS84Transform(6371007.0);

            // New HRAP Converter
            HRAPProjection hrapProj = new HRAPProjection();

            classify = false;


            try {
                int cnt = 0;

                if (nxfilter == null) {
                    nxfilter = new WCTFilter();
                }                  

                short[] rowData = new short[numHrapX];
                for (int j=0; j<numHrapY; j++) {

                    // Fortran unformatted record begin - 4-byte int with number of bytes (numHrapX * 2)
                    //int numWordsBeg = Swap.swapInt(f.readInt()); // LITTLE_ENDIAN IS NOW SET IN HEADER READ
                    int numWordsBeg;
                    try {
                        numWordsBeg = f.readInt();
                    } catch (Exception e) {
                        throw new DecodeException("RandomAccessFile is null: row="+j, header.getDataURL());
                    }



                    if (numWordsBeg != numHrapX*2) {
                        throw new DecodeException("UNFORMATTED BYTES != numHrapX*2  BYTES="+
                                numWordsBeg+"  numHrapX="+numHrapX, header.getDataURL());
                    }
                    // Read entire row into array -- makes run length encoding easier
                    for (int i=0; i<numHrapX; i++) {
                        //short readValue = Swap.swapShort(f.readShort());
                        rowData[i] = f.readShort();


                        //xmrgRaster.setCellValue(i, numHrapY-j-1, (float)(rowData[i]*unitsFactor/100.0));
                        double value = rowData[i]*unitsFactor/100.0;                     
                        //                          value = (j*numHrapX + i)%6.0;                     
                        //                          value = i%6.0 + 1 + j%6;         
                        //                          System.out.println(value);
                        if (rowData[i] >= 0 && nxfilter.accept(value)) { // we want zeros in raster                                 
                            xmrgRaster.setCellValue(i, numHrapY-j-1, (float)value);
                        }                                      
                    }

                    //for (int i=0; i<numHrapX; i++) {
                    int i = 0;
                    while (i < numHrapX) { 
                        double value = rowData[i]*unitsFactor/100.0;
                        //                          value = ((double)(j*numHrapX + i))/(numHrapX*numHrapY);                     
                        //                          value = i%6.0 + 1 + j%6;         
                        /*
                     // Do raster processing
                     if (rowData[i] >= 0 && nxfilter.accept(value)) { // we want zeros in raster                                 
                        xmrgRaster.setCellValue(i, numHrapY-j-1, (float)value);
                     }                                      
                         */    
                        // Do vector processing
                        //                          if (true) {                     
                        if (rowData[i] > 0 && nxfilter.accept(value)) {                     

                            //xmrgRaster.setCellValue(i, numHrapY-j-1, (float)(rowData[i]*unitsFactor/100.0));


                            int xrun = 1;
                            short nextReadValue;
                            double nextValue;
                            int colorIndex = NexradDPAThresholdFactory.getColorIndex((float)value);
                            int nextColorIndex = colorIndex;
                            //while (classify && colorIndex == nextColorIndex && i+1 < numHrapX) {
                            while (classify && colorIndex == nextColorIndex && i+xrun < numHrapX) {
                                //nextValue = rowData[i+1]*unitsFactor/100.0;
                                nextValue = rowData[i+xrun]*unitsFactor/100.0;
                                nextColorIndex = NexradDPAThresholdFactory.getColorIndex((float)nextValue);
                                if (nextColorIndex == colorIndex) {
                                    //i++;
                                    xrun++;
                                }
                            }

                            //    System.out.println("i = "+i+"   xrun = "+xrun);
                            //    i+=xrun;
                            //    if (i%10 == 0) {
                            //       System.out.println(i);
                            //    }
                            //                              System.out.println("XRUN: "+xrun+"   INDEX: "+colorIndex);



                            //xster=(i+1)*4762.5 - 401*4762.5
                            //yster=(j+1)*4762.5-1601*4762.5

                            //int xrun = 1;
                            //                              /*
                            double[] stereoX = new double[2+xrun*2];
                            double[] stereoY = new double[2+xrun*2];

                            //stereoX[0] = (llHrapX+i+1)*4762.5 - 401*4762.5 ; 
                            //stereoY[0] = (llHrapY+j+1)*4762.5 - 1601*4762.5 ; 
                            //stereoX[1] = (llHrapX+i+1)*4762.5 - 401*4762.5 ;
                            //stereoY[1] = (llHrapY+j+2)*4762.5 - 1601*4762.5 ;
                            //stereoX[2] = (llHrapX+i+2)*4762.5 - 401*4762.5 ;
                            //stereoY[2] = (llHrapY+j+2)*4762.5 - 1601*4762.5 ;
                            //stereoX[3] = (llHrapX+i+2)*4762.5 - 401*4762.5 ;
                            //stereoY[3] = (llHrapY+j+1)*4762.5 - 1601*4762.5 ;


                            // Add points in this order:    2  3  4  5  (if xrun==4)
                            //                              1  8  7  6  
                            stereoX[0] = (llHrapX+i)*4762.5 - 401*4762.5 ; 
                            stereoY[0] = (llHrapY+j)*4762.5 - 1601*4762.5 ; 
                            stereoX[1] = (llHrapX+i)*4762.5 - 401*4762.5 ;
                            stereoY[1] = (llHrapY+j+1)*4762.5 - 1601*4762.5 ;
                            for (int nr=0; nr<xrun; nr++) {
                                stereoX[2+nr] = (llHrapX+i+1+nr)*4762.5 - 401*4762.5 ;
                                stereoY[2+nr] = (llHrapY+j+1)*4762.5 - 1601*4762.5 ;
                            }
                            for (int nr=xrun-1; nr>=0; nr--) {
                                stereoX[2+xrun+(xrun-1-nr)] = (llHrapX+i+1+nr)*4762.5 - 401*4762.5 ;
                                stereoY[2+xrun+(xrun-1-nr)] = (llHrapY+j)*4762.5 - 1601*4762.5 ;
                            }
                            // advance i
                            i+=xrun;


                            Stereo2LatLon cvt2 = new Stereo2LatLon(stereoX, stereoY, 60.0, -105.0);

                            //Vector coordinates = new Vector();
                            coordinates.clear();


                            double[] dwork;
                            for(int n=0;n<2+xrun*2;n++) { 
                                //                                  ---------              //dwork = (sphericalToWGS84Transform.transform(new CoordinatePoint(cvt2.getLon(n), cvt2.getLat(n)), null)).getCoordinates();
                                //                                  ---------              //coordinates.addElement((Object)new Coordinate(dwork[0], dwork[1]));
                                coordinates.addElement(new Coordinate(cvt2.getLon(n), cvt2.getLat(n)));
                            }
                            // Add the first point again to close polygon
                            //                              ---------           //dwork = (sphericalToWGS84Transform.transform(new CoordinatePoint(cvt2.getLon(0), cvt2.getLat(0)), null)).getCoordinates();
                            //                              ---------           //coordinates.addElement((Object)new Coordinate(dwork[0], dwork[1]));
                            coordinates.addElement(new Coordinate(cvt2.getLon(0), cvt2.getLat(0)));

                            //                              */


                            /*
                     double[] hrapX = new double[2+xrun*2];
                     double[] hrapY = new double[2+xrun*2];

                     // Add points in this order:    2  3  4  5  (if xrun==4)
                     //                              1  8  7  6  
                     hrapX[0] = llHrapX+i; 
                     hrapY[0] = llHrapY+j; 
                     hrapX[1] = llHrapX+i;
                     hrapY[1] = llHrapY+j+1;
                     for (int nr=0; nr<xrun; nr++) {
                        hrapX[2+nr] = (llHrapX+i+1+nr);
                        hrapY[2+nr] = (llHrapY+j+1);
                     }
                     for (int nr=xrun-1; nr>=0; nr--) {
                        hrapX[2+xrun+(xrun-1-nr)] = (llHrapX+i+1+nr);
                        hrapY[2+xrun+(xrun-1-nr)] = (llHrapY+j);
                     }
                     // advance i
                     i+=xrun;

                     coordinates.clear();


                     double[] dwork;
                     for(int n=0;n<2+xrun*2;n++) {
                        dwork = hrapProj.reverse(hrapX[n], hrapY[n]);
                        coordinates.addElement((Object)new Coordinate(dwork[0], dwork[1]));
                     }
                     // Add the first point again to close polygon
                     dwork = hrapProj.reverse(hrapX[0], hrapY[0]);
                     coordinates.addElement((Object)new Coordinate(dwork[0], dwork[1]));
                             */



                            // Create polygon
                            try {                    
                                Coordinate[] cArray = new Coordinate[coordinates.size()];
                                LinearRing lr = geoFactory.createLinearRing((Coordinate[])(coordinates.toArray(cArray)));
                                Polygon poly = geoFactory.createPolygon(lr, null);

                                if (nxfilter == null || nxfilter.accept(poly)) {

                                    if (nxfilter != null ) {
                                        poly = (Polygon)(nxfilter.clipToExtentFilter(poly));
                                    }

                                    if (poly != null) {


                                        // create the feature and convert values to inch
                                        Feature feature = schema.create(
                                                new Object[] { poly, 
                                                        new Float(value),
                                                        // find colorIndex value based on DPA value thresholds
                                                        new Integer(NexradDPAThresholdFactory.getColorIndex((float)value))
                                                }, new Integer(geoIndex++).toString());

                                        // send to processess
                                        for (int n=0; n<processArray.length; n++) {
                                            processArray[n].addFeature(feature);
                                        }
                                    }



                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } 
                        }
                        else {
                            i++;
                        }













                        cnt++;

                    }
                    // Fortran unformatted record end - 4-byte int with number of bytes (numHrapX * 2)
                    int numWordsEnd = f.readInt();
                    if (numWordsEnd != numHrapX*2) {
                        throw new DecodeException("UNFORMATTED BYTES != numHrapX*2  BYTES="+
                                numWordsEnd+"  numHrapX="+numHrapX, header.getDataURL());
                    }




                    // Decode progress
                    // --------------
                    for (int n = 0; n < listeners.size(); n++) {
                        event.setProgress( (int)( ( ((double)j) / numHrapY ) * 100.0) );
                        ((DataDecodeListener) listeners.get(n)).decodeProgress(event);
                    }



                }
                // end numHrapY



                System.out.println(cnt+" XMRG PROCESSED CELLS");


            } catch (Exception e) {
                e.printStackTrace();
                throw new DecodeException("XMRG DECODE ERROR", header.getDataURL());
            } finally {
                for (int n=0; n<processArray.length; n++) {
                    try {
                        processArray[n].close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }





            f.close();               



            // End decode
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(100);
                ((DataDecodeListener) listeners.get(i)).decodeEnded(event);
            }





        }
        // END try
        catch (Exception e) {
            System.out.println("CAUGHT EXCEPTION:  " + e);
            e.printStackTrace();


            try {
                f.close();
            } catch (Exception eee) {
                e.printStackTrace();
            }

            throw new DecodeException("CAUGHT EXCEPTION:  "+ e, header.getDataURL());
        }


    }
    // END METHOD decodeData













    /**
     * Adds a DataDecodeListener to the list.
     *
     * @param  listener  The feature to be added to the DataDecodeListener attribute
     */
    public void addDataDecodeListener(DataDecodeListener listener) {

        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

    }


    /**
     * Removes a DataDecodeListener from the list.
     *
     * @param  listener   DataDecodeListener to remove.
     */
    public void removeDataDecodeListener(DataDecodeListener listener) {

        listeners.remove(listener);

    }















    /**
     *  Gets the features for the last decoded data (point or polygon)
     *
     * @return    The FeatureCollection object
     */
    public FeatureCollection getFeatures() {
        return features;
    }

    /**
     *  Sets the collection for which the features of the next decoded data (point or polygon) will be filled
     *
     * @return    The FeatureCollection object
     */
    public void setFeatures(FeatureCollection features) {

        this.features = features;
    }




    public XMRGRaster getXMRGRaster() {
        return xmrgRaster;
    }



    /**
     * Not implemented - returns null
     */
    public String[] getSupplementalDataArray() throws IOException {
        return null;
    }





}

