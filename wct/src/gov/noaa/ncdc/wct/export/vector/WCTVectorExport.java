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

package gov.noaa.ncdc.wct.export.vector;

import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.gml.producer.FeatureTransformer;

public class WCTVectorExport {



    // The list of event listeners.
    private Vector<GeneralProgressListener> listeners = new Vector<GeneralProgressListener>();






    public void saveWKT(File file, StreamingRadialDecoder decoder) 
    throws WCTExportException, WCTExportNoDataException, MalformedURLException, 
    IOException, IllegalAttributeException {

        saveWKT(file, decoder.getFeatures(), decoder.getFeatureTypes()[0]);
    }


    public void saveWKT(File file, FeatureCollection featureCollection, 
            FeatureType featureType) 
    throws WCTExportException, WCTExportNoDataException, MalformedURLException, 
    IOException, IllegalAttributeException {


        if (featureCollection.size() == 0) {
            throw new WCTExportNoDataException("No Data for: "+file.toURI().toURL());
        }



        GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).started(event);
        }




        // Write .txt WKT file
        BufferedWriter bwWKT = new BufferedWriter(new FileWriter(file + ".txt"));
        FeatureIterator fci = featureCollection.features();
        Feature feature;

        int size = featureCollection.size();
        int cnt = 0;
        while (fci.hasNext()) {
            bwWKT.write(fci.next().toString());
            bwWKT.newLine();

            // Progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)++cnt) / size ) * 100.0) );
                listeners.get(n).progress(event);
            }


        }


        bwWKT.close();

        // Write .prj file  -- All exported NEXRAD Data is in WGS84 LatLon Projection         
        // Check for .shp ending and remove it if necessary
        String fileString = file.toString();
        if (fileString.endsWith(".txt")) {
            file = new File(fileString.substring(0, fileString.length()-4));
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file + ".prj"));
        bw.write(WCTProjections.NAD83_ESRI_PRJ);
        bw.flush();
        bw.close();



        // End
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).ended(event);
        }



    }






    public void saveGML(File file, StreamingRadialDecoder decoder) 
    throws WCTExportException, WCTExportNoDataException, MalformedURLException, 
    IOException, IllegalAttributeException {

        saveGML(file, decoder.getFeatures(), decoder.getFeatureTypes()[0]);
    }


    public void saveGML(File file, FeatureCollection featureCollection, 
            FeatureType featureType) 
    throws WCTExportException, WCTExportNoDataException, MalformedURLException, 
    IOException, IllegalAttributeException {


        try {
            if (featureCollection.size() == 0) {
                throw new WCTExportNoDataException("No Data for: "+file.toURI().toURL());
            }



            GeneralProgressEvent event = new GeneralProgressEvent(this);

            // Start
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(0);
                listeners.get(i).started(event);
            }

            // Write .gml GML file
            BufferedWriter bw = new BufferedWriter(new FileWriter(file + ".gml"));

            FeatureTransformer ft = new FeatureTransformer();
            // set the indentation to 4 spaces
            ft.setIndentation(4);
            // this will allow Features with the FeatureType which has the namespace
            // "http://somewhere.org" to be prefixed with xxx...
            ft.getFeatureNamespaces().declarePrefix("jnx", featureType.getNamespace());
            // transform
            ft.transform(featureCollection, bw);
            
            bw.flush();
            bw.close();



            // End
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(0);
                listeners.get(i).ended(event);
            }
            
            
            
        } catch (NoSuchElementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new WCTExportException(e.getMessage());
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new WCTExportException(e.getMessage());
        }



    }









    public void saveShapefile(File file, StreamingRadialDecoder decoder) 
    throws WCTExportException, WCTExportNoDataException, MalformedURLException, 
    IOException, IllegalAttributeException {

        saveShapefile(file, decoder.getFeatures(), decoder.getFeatureTypes()[0]);
    }


    public void saveShapefile(File file, FeatureCollection featureCollection, 
            FeatureType featureType) 
    throws WCTExportException, WCTExportNoDataException, MalformedURLException, 
    IOException, IllegalAttributeException {


        if (featureCollection.size() == 0) {
            throw new WCTExportNoDataException("No Data for: "+file.toURI().toURL());
        }



        GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).started(event);
        }





        // Write .shp , .shx and .dbf files
        ShapefileDataStore store = new ShapefileDataStore(file.toURI().toURL());
        store.createSchema(featureType);
        FeatureWriter fw = store.getFeatureWriter(store.getTypeNames()[0], Transaction.AUTO_COMMIT);

        FeatureIterator fci = featureCollection.features();
        Feature feature;
        Object[] att = null;


        int size = featureCollection.size();
        int cnt = 0;
        while (fci.hasNext()) {
            feature = fci.next();
            fw.next().setAttributes(feature.getAttributes(att));
            fw.write();

            // Progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)++cnt) / size ) * 100.0) );
                listeners.get(n).progress(event);
            }


        }


        fw.close();

        // Write .prj file  -- All exported NEXRAD Data is in NAD83 LatLon Projection         
        // Check for .shp ending and remove it if necessary
        String fileString = file.toString();
        if (fileString.endsWith(".shp")) {
            file = new File(fileString.substring(0, fileString.length()-4));
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file + ".prj"));
        bw.write(WCTProjections.NAD83_ESRI_PRJ);
        bw.flush();
        bw.close();



        // End
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).ended(event);
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
