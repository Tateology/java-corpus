package gov.noaa.ncdc.wct.export.vector;

import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;


/**
 *  Export features to shapefile one feature at a time for low memory consumption.
 *
 * @author    steve.ansari
 */
public class StreamingAttributeExport implements StreamingProcess {

    private File outfile;
    private BufferedWriter bw;
    private FeatureType featureType;
    private String defaultGeometryName;
    private boolean firstTime = true;



    /**
     * Constructor for the StreamingAttributeExport object
     *
     * @param  outfile          The destination CSV file.
     * @exception  IOException  Error when writing file?
     */
    public StreamingAttributeExport(File outfile) throws IOException {
        this.outfile = outfile;
    }
    
    private void init() throws IOException {

        bw = new BufferedWriter(new FileWriter(outfile));
        
        defaultGeometryName = featureType.getDefaultGeometry().getName();
        
        // write header line
        for (int n=0; n<featureType.getAttributeCount()-1; n++) {
            if (! featureType.getAttributeType(n).getName().equals(defaultGeometryName)) {
                bw.write(featureType.getAttributeType(n).getName()+",");
            }
        }
        if (! featureType.getAttributeType(featureType.getAttributeCount()-1).getName().equals(defaultGeometryName)) {
            bw.write(featureType.getAttributeType(featureType.getAttributeCount()-1).getName());
        }
        bw.newLine();
    }

    /**
     *  Implementation of StreamingProcess.  Writes out the Feature to the already open CSV file.  
     *  Only the attributes are written - the default geometry is not.  If the
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

                init();
                firstTime = false;
            }

            
            // Ignore and return if this feature type does not match
            if (! feature.getFeatureType().equals(featureType)) {
                return;
            }

            Object[] att = null; 
            att = feature.getAttributes(att);
            for (int n=0; n<att.length-1; n++) {
                if (! featureType.getAttributeType(n).getName().equals(defaultGeometryName)) {
                    bw.write(att[n].toString()+",");
                }
            }
            if (! featureType.getAttributeType(att.length-1).getName().equals(defaultGeometryName)) {
                bw.write(att[att.length-1].toString());
            }
            bw.newLine();
            
        } catch (IOException ioe) {
            throw new StreamingProcessException("IOException: "+ioe.toString()+"\nFeature=" + feature.toString());
        }
    }



    /**
     * Close the Shapefile - THIS MUST BE DONE AFTER ALL FEATURES ARE PROCESSED!
     *
     * @exception  StreamingProcessException  Error closing the writer?
     */
    public void close() throws StreamingProcessException {
        try {
            bw.flush();
            bw.close();
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

}


