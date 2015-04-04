package gov.noaa.ncdc.wct.export.vector;

import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;


/**
 *  Export features to shapefile one feature at a time for low memory consumption.
 *
 * @author    steve.ansari
 */
public class StreamingShapefileExport implements StreamingProcess {

    
    // The list of event listeners.
    private Vector<GeneralProgressListener> listeners = new Vector<GeneralProgressListener>();

    
    
    private File outfile;
    private FeatureWriter fw;
    private FeatureType featureType;
    private String prj = WCTProjections.NAD83_ESRI_PRJ;
    private boolean firstTime = true;




    /**
     * Constructor for the ExportShapefileLite object
     *
     * @param  outfile          The destination file. (will create .prj, .dbf, .shx and .shp files)
     * @param  prj              The ESRI projection string for the .prj file.
     * @exception  IOException  Error when writing file?
     */
    public StreamingShapefileExport(File outfile, String prj) throws IOException {
        this.outfile = outfile;
        this.prj = prj;
    }



    /**
     * Constructor for the ExportShapefileLite object using default WGS84 lat/lon projection info.
     *
     * @param  outfile          The destination file. (will create .prj, .dbf, .shx and .shp files)
     * @exception  IOException  Error when writing file?
     */
    public StreamingShapefileExport(File outfile) throws IOException {
        this.outfile = outfile;
        this.prj = WCTProjections.NAD83_ESRI_PRJ;
    }


    /**
     *  Writes the default WGS84 .prj file.
     *
     * @exception  IOException  Error writing?
     */
    private void writePrj() throws IOException {
        // Write .prj file  
        // Check for .shp ending and remove it if necessary        
        String fileString = outfile.toString();
        File prjFile;
        if (! fileString.endsWith(".shp")) {
            outfile = new File(fileString+".shp");
            fileString = outfile.toString();
        }
        prjFile = new File(fileString.substring(0, fileString.length() - 4)+".prj");
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(prjFile));
        bw.write(prj);
        bw.flush();
        bw.close();

    }
    
    private void shpInit() throws IOException {
        
//        System.out.println("shpInit(): "+outfile);
        
        // Writes .shp , .shx and .dbf files
        ShapefileDataStore store = new ShapefileDataStore(outfile.toURI().toURL());
        store.createSchema(featureType);
        fw = store.getFeatureWriter(store.getTypeNames()[0], Transaction.AUTO_COMMIT);
    }


    /**
     *  Implementation of StreamingProcess.  Writes out the Feature to the already open Shapefile.  If the
     *  feature's schema does not match the supplied FeatureType in the constructor it is <b>ignored</b>, 
     *  and <b> NO </b>exception is thrown.  This functionality allows for multiple types of Feature objects
     *  with different schemas to be decoded in a streaming process.
     *
     * @param  feature                   The feature to write.
     * @exception  StreamingProcessException  Error writing the feature?
     */
    public void addFeature(Feature feature) throws StreamingProcessException {
        try {
            
            if (firstTime) {
                // Extract featureType from first feature
                this.featureType = feature.getFeatureType();

                shpInit();
                firstTime = false;
            }

            // Ignore and return if this feature type does not match
            if (! feature.getFeatureType().equals(featureType)) {
                return;
            }
            
//            System.out.println("writing: "+feature.getID());

            Object[] att = null;
            Feature writeFeature = fw.next();
            att = feature.getAttributes(att);
            for (int n=0; n<att.length; n++) {
                writeFeature.setAttribute(n, att[n]);  
            }
            fw.write();
        } catch (IOException ioe) {
            throw new StreamingProcessException("IOException: "+ioe.toString()+"\nFeature=" + feature.toString());
        } catch (IllegalAttributeException iae) {
            throw new StreamingProcessException("IllegalAttributeException: "+iae.toString()+"\nFeature=" + feature.toString());
        }
    }



    /**
     * Close the Shapefile - THIS MUST BE DONE AFTER ALL FEATURES ARE PROCESSED!
     *
     * @exception  StreamingProcessException  Error closing the writer?
     */
    public void close() throws StreamingProcessException {
        try {
            if (fw != null) {
                fw.close();
                writePrj();
            }
        } catch (IOException ioe) {
            throw new StreamingProcessException("IOException: "+ioe.toString()+"\nERROR CLOSING FILE: " + outfile);
        }
    }


    /**
     * Just in case we forgot to close...
     */
    public void finalize() {
        try {
            close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    

    
    
    
    
    /**
     * Adds a GeneralProgressListener to the list.
     *
     * @param  listener  The feature to be added to the GeneralProgressListener attribute
     */
    public void addGeneralProgressListener(GeneralProgressListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }


    /**
     * Removes a GeneralProgressListener from the list.
     *
     * @param  listener   GeneralProgressListener to remove.
     */
    public void removeGeneralProgressListener(GeneralProgressListener listener) {
        listeners.remove(listener);
    }

}


