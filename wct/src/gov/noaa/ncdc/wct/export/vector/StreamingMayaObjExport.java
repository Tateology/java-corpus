
package gov.noaa.ncdc.wct.export.vector;

import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 *  Export features to shapefile one feature at a time for low memory consumption.
 *
 * @author    steve.ansari
 */
public class StreamingMayaObjExport implements StreamingProcess {

    private File outfile;
    private BufferedWriter bw;
    private FeatureType featureType;
    private int runningTotal = 0;
    
    //The string that represents the type of shapes (point, line, face, curve, surf).
    //                          Can be "p", "l", "f", "curv", "curv2", "surf".
    private String geometryElement = "l";
                              
    /**
     * Constructor for the ExportMayaObjLite object
     *
     * @param  outfile          The destination file. (will create .obj file)
     * @exception  IOException  Error when writing file?
     */
    public StreamingMayaObjExport(File outfile, FeatureType featureType) throws IOException {
        this.outfile = outfile;
        this.featureType = featureType;
        init();
    }



    /**
     *  Sets up the writer and writes the .prj file.
     *
     * @exception  IOException  Error writing?
     */
    private void init() throws IOException {

        // Write .obj file  -- All exported NEXRAD Data is in WGS83 LatLon Projection
        // Check for .txt ending and remove it if necessary
        String fileString = outfile.toString();
        if (fileString.endsWith(".obj")) {
            outfile = new File(fileString.substring(0, fileString.length() - 4));
        }

        // Write .txt WKT file
        bw = new BufferedWriter(new FileWriter(outfile + ".obj"));


    }


    /**
     *  Implementation of LiteProcess.  Writes out only the Geometry of each Feature 
     *  to the already open Maya .obj file.  If the
     *  feature's schema does not match the supplied FeatureType in the constructor it is <b>ignored</b>, 
     *  and <b> NO </b>exception is thrown.  This functionality allows for multiple types of Feature objects
     *  with different schemas to be decoded in a streaming process.
     *
     * @param  feature                   The feature to write.
     * @exception  StreamingProcessException  Error writing the feature?
     */
    public void addFeature(Feature feature) throws StreamingProcessException {
        try {

            // Ignore and return if this feature type does not match
            if (! feature.getFeatureType().equals(featureType)) {
                return;
            }

            // Extract geometry information from feature
            Geometry geometry = feature.getDefaultGeometry();
            String geomType = geometry.getGeometryType();
            //System.out.println("TYPE: "+geomType); 
            if (geomType.startsWith("Multi")) {
                GeometryCollection gcoll = (GeometryCollection)geometry;
                for (int n=0; n<gcoll.getNumGeometries(); n++) {
                    Geometry innerGeometry = gcoll.getGeometryN(n);
                    //System.out.println("INNER TYPE: "+innerGeometry.getGeometryType()); 
                    writeData(innerGeometry);
                }
            }
            else {
                writeData(geometry);
            }


        } catch (IOException ioe) {
            throw new StreamingProcessException("IOException: Feature=" + feature.toString());
        }
    }




    private void writeData(Geometry geometry) throws IOException {

        // Extract coordinates (vertices) from geometry
        Coordinate[] coords = geometry.getCoordinates();

        int length = coords.length;
        // Limit to 200 records for JavaView
        //if (length > 200) {
        //   length = 200;
        //}

        // Loop through the coordinates
        for (int n=0; n<length; n++) {
//          for (int n=0; n<coords.length; n++) {
            bw.write("# "+(runningTotal+n+1));
            bw.newLine();
            bw.write("v "+coords[n].x+" "+coords[n].y+" 0.0");
            bw.newLine();
        }
        // Define output geometry
        //bw.write("f");
        bw.write(geometryElement);
        for (int n=0; n<length; n++) {
//          for (int n=0; n<coords.length; n++) {
            bw.write(" "+(runningTotal+n+1));
        }
        bw.newLine();

//      runningTotal+=coords.length;
        runningTotal+=length;

    }



    /**
     * Close the .ohj file - THIS MUST BE DONE AFTER ALL FEATURES ARE PROCESSED!
     *
     * @exception  StreamingProcessException  Error closing the writer?
     */
    public void close() throws StreamingProcessException {
        try {
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new StreamingProcessException("IOException: ERROR CLOSING FILE: " + outfile);
        }
    }


    /**
     * Just in case we forgot to close...
     */
    public void finalize() {
        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


