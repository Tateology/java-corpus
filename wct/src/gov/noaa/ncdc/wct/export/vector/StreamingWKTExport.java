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

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;


/**
 *  Export features to Well-Known Text (WKT) one feature at a time for low memory consumption.
 *
 * @author    steve.ansari
 */
public class StreamingWKTExport implements StreamingProcess {

    // The list of event listeners.
    private Vector<GeneralProgressListener> listeners = new Vector<GeneralProgressListener>();
    
    
    private File outfile;
    private BufferedWriter bwWKT;
    private FeatureType featureType;
    private String prj = WCTProjections.NAD83_ESRI_PRJ;
    private boolean firstTime = true;


    /**
     *Constructor 
     *
     * @param  outfile          The destination file.
     * @exception  IOException  Error when writing file?
     */
    public StreamingWKTExport(File outfile) throws IOException {
        this.outfile = outfile;
        init();
    }

    
    /**
     * Constructor 
     *
     * @param  outfile          The destination file. 
     * @param  prj              The ESRI projection string for the .prj file.
     * @exception  IOException  Error when writing file?
     */
    public StreamingWKTExport(File outfile, String prj) throws IOException {
        this.outfile = outfile;
        this.prj = prj;
    }
    
    /**
     *Constructor 
     *
     * @param  outfile          The destination file.
     * @param  featureType      FeatureType schema (attribute and geometry definitions) from the decoder.
     *                          This is generally provided via the NexradDecoderLite.getFeatureType() method.
     *                          This FeatureType is compared to each Feature that is 'streamed' and only
     *                          Feature objects with the same FeatureType are processed by this class.
     * @exception  IOException  Error when writing file?
     */
    public StreamingWKTExport(File outfile, FeatureType featureType) throws IOException {
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
        String fileString = outfile.toString();
        if (fileString.endsWith(".txt")) {
            outfile = new File(fileString.substring(0, fileString.length() - 4));
        }

        // Write .txt WKT file
        bwWKT = new BufferedWriter(new FileWriter(outfile + ".txt"));

        // Write .prj file  -- All exported NEXRAD Data is in NAD83 LatLon Projection
        // Check for .shp ending and remove it if necessary
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile + ".prj"));

        bw.write(this.prj);
        bw.flush();
        bw.close();

    }
    



    /**
     *  Implementation of StreamingProcess.  Writes out the Feature to the already open WKT file.
     *
     * @param  feature                   The feature to write.
     * @exception  StreamingProcessException  Error writing the feature?
     */
    public void addFeature(Feature feature) throws StreamingProcessException {
        
        if (firstTime) {
            // Extract featureType from first feature
            this.featureType = feature.getFeatureType();
            firstTime = false;
        }

        // Ignore and return if this feature type does not match
        if (! feature.getFeatureType().equals(featureType)) {
            return;
        }

        try {
            bwWKT.write(feature.toString());
            bwWKT.newLine();
        } catch (IOException ioe) {
            throw new StreamingProcessException("IOException: Feature=" + feature.toString());
        }
    }



    /**
     * Close the WKT file - THIS MUST BE DONE AFTER ALL FEATURES ARE PROCESSED!
     *
     * @exception  StreamingProcessException  Error closing the writer?
     */
    public void close() throws StreamingProcessException {
        try {
            bwWKT.flush();
            bwWKT.close();
        } catch (IOException ioe) {
            throw new StreamingProcessException("IOException: ERROR CLOSING FILE: " + outfile);
        }
    }


//    /**
//     * Just in case we forgot to close...
//     *
//     * @exception  StreamingProcessException  Description of the Exception
//     */
//    public void finalize() throws StreamingProcessException {
//        try {
//            close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    
    

    
    
    
    
    
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


