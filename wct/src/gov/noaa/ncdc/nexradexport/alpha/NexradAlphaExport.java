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

package gov.noaa.ncdc.nexradexport.alpha;

import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

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

/**
 * Exports Nexrad Alphanumeric features.
 * 
 * @author steve.ansari
 */
public class NexradAlphaExport {

    /**
     * Save Nexrad Alphanumeric data to ESRI Shapefile with additional .prj
     * projection file
     * 
     * @param outFile
     *            Destination file
     * @param decoder
     *            Alphanumeric Nexrad decoder
     */
    public static void saveShapefile(File outFile, DecodeL3Alpha decoder)
            throws WCTExportException, WCTExportNoDataException,
            IOException, IllegalAttributeException {

        // 1. Write point shapefile

        if (decoder.getFeatures().size() == 0) {
            throw new WCTExportNoDataException(
                    "No Data Present in NEXRAD File: " + outFile.toString());
        }

        // Write .shp , .shx and .dbf files
        ShapefileDataStore store = new ShapefileDataStore(outFile.toURL());
        store.createSchema(decoder.getFeatureTypes()[0]);
        FeatureWriter fw = store.getFeatureWriter(store.getTypeNames()[0],
                Transaction.AUTO_COMMIT);

        FeatureIterator fci = decoder.getFeatures().features();
        Feature feature;
        Object[] att = null;
        while (fci.hasNext()) {
            feature = fci.next();
            fw.next().setAttributes(feature.getAttributes(att));
            fw.write();
        }
        fw.close();

        // Write .prj file -- All exported NEXRAD Data is in NAD83 LatLon
        // Projection
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile + ".prj"));
        String prj = "GEOGCS[\"GCS_WGS_1984\"," + "DATUM[\"D_WGS_1984\","
                + "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],"
                + "PRIMEM[\"Greenwich\",0.0],"
                + "UNIT[\"Degree\",0.0174532925199433]]";
        bw.write(prj);
        bw.close();

    }

    /**
     * Save Nexrad Alphanumeric data to ESRI Line Shapefile with additional .prj
     * projection file. This is currently only applicable for the Storm Tracking
     * Level-III Product (NST)
     * 
     * @param outFile
     *            Destination file
     * @param decoder
     *            Alphanumeric Nexrad decoder
     */
    public static void saveLineShapefile(File outFile, DecodeL3Alpha decoder)
            throws WCTExportException, WCTExportNoDataException,
            IOException, IllegalAttributeException {

        // 2. Write line shapefile if applicable
        if (decoder.getLineFeatures() != null) {
            // Write .shp , .shx and .dbf files
            outFile = new File(outFile.toString() + "_line");
            ShapefileDataStore store = new ShapefileDataStore(outFile.toURL());
            store.createSchema(decoder.getLineFeatureType());
            FeatureWriter fw = store.getFeatureWriter(store.getTypeNames()[0],
                    Transaction.AUTO_COMMIT);

            FeatureIterator fci = decoder.getLineFeatures().features();
            Feature feature;
            Object[] att = null;
            while (fci.hasNext()) {
                feature = fci.next();
                fw.next().setAttributes(feature.getAttributes(att));
                fw.write();
            }
            fw.close();

            // Write .prj file -- All exported NEXRAD Data is in NAD83 LatLon
            // Projection
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile
                    + ".prj"));
            String prj = "GEOGCS[\"GCS_WGS_1984\"," + "DATUM[\"D_WGS_1984\","
                    + "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],"
                    + "PRIMEM[\"Greenwich\",0.0],"
                    + "UNIT[\"Degree\",0.0174532925199433]]";
            bw.write(prj);
            bw.close();
        }

    }

    /**
     * Save Nexrad Alphanumeric data to Well-Known Text with additional .prj
     * projection file. This is currently only applicable for the Storm Tracking
     * Level-III Product (NST)
     * 
     * @param outFile
     *            Destination file
     * @param decoder
     *            Alphanumeric Nexrad decoder
     */
    public static void saveLineWKT(File outFile, DecodeL3Alpha decoder)
            throws WCTExportException, WCTExportNoDataException,
            IOException, IllegalAttributeException {

        // 2. Write line shapefile if applicable
        if (decoder.getLineFeatures() != null) {

            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                    outFile.toString() + "_line.txt")));
            try {
                ShapefileDataStore store = new ShapefileDataStore(outFile
                        .toURL());
                store.createSchema(decoder.getLineFeatureType());
                FeatureIterator fi = decoder.getLineFeatures().features();
                int i = 0;
                while (fi.hasNext()) {
                    bw.write(fi.next().toString());
                    bw.newLine();
                    i++;
                }

            } finally {
                bw.flush();
                bw.close();
            }

        }

    }

    /**
     * Save Nexrad Alphanumeric data to Comma-delimited file
     * 
     * @param outFile
     *            Destination file
     * @param decoder
     *            Alphanumeric Nexrad decoder
     */
    public static void saveCSV(File outFile, DecodeL3Alpha decoder)
            throws WCTExportException, WCTExportNoDataException,
            IOException, IllegalAttributeException {

        DecimalFormat format = new DecimalFormat("0.000");
        saveCSV(outFile, decoder, format);
    }

    /**
     * Save Nexrad Alphanumeric data to Comma-delimited file
     * 
     * @param outFile
     *            Destination file
     * @param decoder
     *            Alphanumeric Nexrad decoder
     * @param format
     *            Output text format for numeric data
     */
    public static void saveCSV(File outFile, DecodeL3Alpha decoder,
            DecimalFormat format) throws WCTExportException,
            WCTExportNoDataException, IOException, IllegalAttributeException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFile
                .toString()
                + ".csv")));

        try {
            FeatureType schema = decoder.getFeatureTypes()[0];
            FeatureCollection features = decoder.getFeatures();
            bw.write(schema.getAttributeType(1).getName());
            for (int n = 1; n < schema.getAttributeCount() - 1; n++) {
                bw.write("," + schema.getAttributeType(n + 1).getName());
            }
            bw.newLine();
            FeatureIterator fi = features.features();
            Feature f = null;
            int i = 0;
            while (fi.hasNext()) {
                f = fi.next();
                for (int n = 0; n < schema.getAttributeCount()-1; n++) {
                    //System.out.println(schema.getAttributeType(n+1).getType());
                    if (schema.getAttributeType(n+1).getType().toString().equals("class java.lang.Double")) {                        
                        bw.write(format.format(Double.parseDouble(f.getAttribute(n+1)
                                .toString())));                        
                    }
                    else {
                        bw.write(f.getAttribute(n+1).toString());
                    }
                    if (n == schema.getAttributeCount()-2) {
                        bw.newLine();
                    }
                    else {
                        bw.write(",");
                    }                    
                }
                i++;
            }

        } finally {
            bw.flush();
            bw.close();
        }
    }

    public static void saveWKT(File outFile, DecodeL3Alpha decoder)
            throws WCTExportException, WCTExportNoDataException,
            IOException, IllegalAttributeException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFile
                .toString()
                + ".txt")));

        try {
            FeatureCollection features = decoder.getFeatures();
            FeatureIterator fi = features.features();
            int i = 0;
            while (fi.hasNext()) {
                bw.write(fi.next().toString());
                bw.newLine();
                i++;
            }

        } finally {
            bw.flush();
            bw.close();
        }

    }

    public static void saveGML(File outFile, DecodeL3Alpha decoder)
            throws WCTExportException, WCTExportNoDataException,
            IOException, IllegalAttributeException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFile
                .toString()
                + ".gml")));

        try {

            System.out.println("FEATURE TYPE NAMESPACE: "+decoder.getFeatureTypes()[0].getNamespace());
            
            FeatureTransformer ft = new FeatureTransformer();
            // set the indentation to 4 spaces
            ft.setIndentation(4);
            // this will allow Features with the FeatureType which has the
            // namespace
            // "http://somewhere.org" to be prefixed with xxx...
            ft.getFeatureNamespaces().declarePrefix("jnx",
                    "http://www.ncdc.noaa.gov/oa/radar/jnx");
            // transform
            ft.transform(decoder.getFeatures(), bw);

        } catch (TransformerException e) {
            throw new WCTExportException(e.getMessage());
        } finally {
            bw.flush();
            bw.close();
        }

    }

    public static void saveLineGML(File outFile, DecodeL3Alpha decoder)
        throws WCTExportException, WCTExportNoDataException,
            IOException, IllegalAttributeException {

        // 2. Write line shapefile if applicable
        if (decoder.getLineFeatures() != null) {


            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFile
                    .toString()
                    + "_line.gml")));

            try {

                FeatureTransformer ft = new FeatureTransformer();
                // set the indentation to 4 spaces
                ft.setIndentation(4);
                // this will allow Features with the FeatureType which has the
                // namespace
                // "http://somewhere.org" to be prefixed with xxx...
                ft.getFeatureNamespaces().declarePrefix("jnx",
                "http://www.ncdc.noaa.gov/oa/radar/jnx");
                // transform
                ft.transform(decoder.getLineFeatures(), bw);

            } catch (TransformerException e) {
                throw new WCTExportException(e.getMessage());
            } finally {
                bw.flush();
                bw.close();
            }
        }

    }

    
    public static void saveSupplementalData(File outFile, StreamingRadialDecoder decoder) 
        throws IOException {
        
        String[] supArray = decoder.getSupplementalDataArray();
        if (supArray == null) {
            return;
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFile.toString()+ "_sup.txt")));
        for (int n=0; n<supArray.length; n++) {
            bw.write(supArray[n]);
            bw.newLine();
        }
        bw.flush();
        bw.close();
//        System.out.println("Successfully closed: "+outFile.toString()+ "_sup.txt");
        
    }
    
}
