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

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.media.jai.RasterFactory;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Read non-CF NetCDF Q2 grids using the NetCDF-Java library.
 * 
 * @author steve.ansari
 */
public class DecodeQ2 {

    public final static float NO_DATA_VALUE = -9990.0f;

    private ArrayFloat valueArray;

    private Index valueIndex;

    private Dimension latDim;

    private Dimension lonDim;

    private Dimension heightDim;

    private ArrayFloat latArray;

    private ArrayFloat lonArray;

    private ArrayInt heightArray;

    private Index latIndex;

    private Index lonIndex;

    private Index heightIndex;

    private NetcdfFile q2File;

    private WritableRaster raster;

    private String[] variableNames;

    private float[] heightData;

    private float[] latData;

    private float[] lonData;

    private double lonSpacing;

    private double latSpacing;

    private double ulCornerLat;

    private double ulCornerLon;

    private URL url;

    private String currentVariableName = "";
    private int heightLevel = 0;
    private String heightUnits = "";
    private Date creationDate;
    private double scale = 1.0;
    private String units = "";
    private double maxVal = -9999999.0;
    private double minVal = 9999999.0;

    /**
     * Reads file intially
     * 
     * @param url
     *            Description of the Parameter
     */
    public void decodeData(URL url) throws IOException {

        this.url = url;

        // Open a file readable by the NetCDF-Java library
        q2File = NetcdfFile.open(url.toString());

        creationDate = new Date(q2File.findGlobalAttribute("Time")
                .getNumericValue().longValue() * 1000L);

        System.out.println("STARTING !!! ");
        // reset min/max vals
        maxVal = -9999999.0;
        minVal = 9999999.0;

        heightData = readHeightData();
        variableNames = readVariableNames();
        latData = readLatData();
        lonData = readLonData();

        System.out.println("OBTAINED GRID INFO");

    }

    public GridCoverage getGridCoverage(String currentVariableName,
            int heightLevel) throws IOException, InvalidRangeException {
        return getGridCoverage(currentVariableName, heightLevel, 255);
    }

    public GridCoverage getGridCoverage(String currentVariableName,
            int heightLevel, int alphaChannelValue) throws IOException,
            InvalidRangeException {

        this.currentVariableName = currentVariableName;
        this.heightLevel = heightLevel;

        // get data slice
        raster = null;
        raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, lonDim
                .getLength(), latDim.getLength(), 1, null);
        System.out.println("GETTING DATA SLICE " + heightLevel);

        getData();

        // calculate extent
        java.awt.geom.Rectangle2D.Double rect = new java.awt.geom.Rectangle2D.Double(
                ulCornerLon-lonSpacing/2.0, ulCornerLat - (latDim.getLength() * latSpacing) - latSpacing/2.0,
                lonDim.getLength() * lonSpacing, latDim.getLength()
                        * latSpacing);

        Envelope envelope = new Envelope(rect);

        System.out.println(envelope);

        // set up color shading

        Color[] colors;
        double[] vals;

        if (currentVariableName.equals("hsrh")) {
            maxVal = 10.0;
            minVal = 0.0;
        }

        if (units.equals("dBZ")) {
            colors = NexradColorFactory.getTransparentColors(
                    NexradHeader.Q2_NATL_MOSAIC_3DREFL, false,
                    alphaChannelValue);
            vals = NexradValueFactory.getProductMaxMinValues(
                    NexradHeader.Q2_NATL_MOSAIC_3DREFL, -1, false); // trick it
            // with vcp
            // 12
        }
        else {
            colors = NexradColorFactory.getTransparentColors(
                    NexradHeader.UNKNOWN, false, alphaChannelValue);
            vals = new double[] { minVal, maxVal };
        }

        System.out.println("colors.length=" + colors.length + "  vals.length="
                + vals.length);
        System.out.println("minVal=" + minVal + "  maxVal=" + maxVal);

        double[] minValues = new double[] { vals[0] };
        double[] maxValues = new double[] { vals[1] };

        // set up grid coverage
        GridCoverage gc = new GridCoverage("Q2 3D Reflectivity Mosaic", raster,
                GeographicCoordinateSystem.WGS84, envelope, minValues,
                maxValues, null, new Color[][] { colors }, null);

        return gc;

    }

    private String[] readVariableNames() throws IOException {

        List varList = q2File.getVariables();
        String[] vars = new String[varList.size()];

        for (int n = 0; n < vars.length; n++) {
            // if (! ((Variable)varList.get(n)).getName().equals("Height") ) {
            vars[n] = ((Variable) varList.get(n)).getName();
            // }
            System.out.println("VARIABLES[" + n + "]: " + vars[n]);
        }

        return vars;
    }

    private float[] readHeightData() throws IOException {
        String variableName = "Height";
        Variable var = q2File.findVariable(variableName);
        // Height variable is only applicable for the 3D products
        if (var == null) {
            return null;
        }
        
        try {
            heightUnits = var.findAttribute("Units").getStringValue();
        } catch (Exception e) {
            heightUnits = "";
        }
        
        Array data = var.read();
        Index index = data.getIndex();
        int[] shape = data.getShape();
        float[] heights = new float[shape[0]];

        for (int i = 0; i < shape[0]; i++) {
            // System.out.println("height=" + i + " value=" +
            // data.getDouble(index.set(i)));
            heights[i] = data.getFloat(index.set(i));
        }
        return heights;
    }

    private float[] readLatData() {
        // float[] lat = new float[] {34.0f, 35.0f, 36.0f};
        Attribute latAtt = q2File.findGlobalAttribute("Latitude");
        Attribute latSpacingAtt = q2File.findGlobalAttribute("LatGridSpacing");
        System.out.println("\n\nLATITUDE ATTRIBUTE = "
                + latAtt.getNumericValue());

        ulCornerLat = latAtt.getNumericValue().doubleValue();
        latSpacing = latSpacingAtt.getNumericValue().doubleValue();

        latDim = q2File.findDimension("Lat");
        float[] lat = new float[latDim.getLength()];

        for (int n = 0; n < latDim.getLength(); n++) {
            lat[n] = (float) (ulCornerLat - n * latSpacing);
        }

        return lat;
    }

    private float[] readLonData() {
        // float[] lon = new float[] {-80.0f, -79.0f, -78.0f};
        Attribute lonAtt = q2File.findGlobalAttribute("Longitude");
        Attribute lonSpacingAtt = q2File.findGlobalAttribute("LonGridSpacing");
        System.out.println("\n\nLONGITUDE ATTRIBUTE = "
                + lonAtt.getNumericValue());

        ulCornerLon = lonAtt.getNumericValue().doubleValue();
        lonSpacing = lonSpacingAtt.getNumericValue().doubleValue();

        lonDim = q2File.findDimension("Lon");
        float[] lon = new float[lonDim.getLength()];

        for (int n = 0; n < lonDim.getLength(); n++) {
            lon[n] = (float) (ulCornerLon + n * lonSpacing);
        }

        return lon;
    }

    private void getData() throws IOException, InvalidRangeException {

        Dimension latDim = q2File.findDimension("Lat");
        Dimension lonDim = q2File.findDimension("Lon");

        // String variableName = "mrefl_mosaic";
        // Variable var = q2File.findVariable(variableName);
        System.out.println("GET DATA: " + currentVariableName);
        Variable var = q2File.findVariable(currentVariableName);

        Attribute scaleAtt = var.findAttribute("Scale");
        if (scaleAtt == null) {
            this.scale = 1.0;
        }
        else {
            this.scale = scaleAtt.getNumericValue().doubleValue();
        }

        Attribute unitsAtt = var.findAttribute("Units");
        if (unitsAtt == null) {
            this.units = "N/A";
        }
        else {
            this.units = unitsAtt.getStringValue();
        }

        System.out.println("SCALE ATTRIBUTE: " + scale);
        System.out.println("UNITS ATTRIBUTE: " + units);

        // String sectionString =
        // heightLevel+":"+(heightLevel+1)+",0:"+(latDim.getLength()-1)+",0:"+(lonDim.getLength()-1);
        String sectionString;

        // extract height slice only from 3D data
        if (heightData != null) {
            sectionString = heightLevel + ":" + heightLevel + ",0:"
                    + (latDim.getLength() - 1) + ",0:"
                    + (lonDim.getLength() - 1);
        }
        else {
            sectionString = "0:" + (latDim.getLength() - 1) + ",0:"
                    + (lonDim.getLength() - 1);
        }
        System.out.println("EXTRACTING: " + sectionString);

        Array dataSlice = var.read(sectionString);
        IndexIterator iter = dataSlice.getIndexIteratorFast();

        for (int i = 0; i < latDim.getLength(); i++) {
            for (int j = 0; j < lonDim.getLength(); j++) {
                float val = iter.getFloatNext();
                if (val == -9990.0) {
                    raster.setSample(j, i, 0, val);
                }
                // else if (val == -990.0 || val == -999.0) {
                else if (val == -990.0) {
                    raster.setSample(j, i, 0, 0.0f);
                }
                else if (val == -999.0) {
                    raster.setSample(j, i, 0, -1.0f);
                    minVal = 0;
                }
                else {
                    raster.setSample(j, i, 0, val / scale);
                    if (val / scale < minVal) {
                        minVal = val / scale;
                    }
                    if (val / scale > maxVal) {
                        maxVal = val / scale;
                    }
                }
            }
        }

    }

    /**
     * Returns the heightLevel of the last decoded grid coverage.
     */
    public int getLastDecodedHeightLevel() {
        return heightLevel;
    }

    /**
     * Returns the height value of the last decoded grid coverage.
     */
    public double getLastDecodedHeightValue() {
        if (heightData == null) {
            return -1;
        }
        else {
            return heightData[heightLevel];
        }
    }

    /**
     * Returns the height value of the last decoded grid coverage.
     */
    public String getLastDecodedHeightUnits() {
        if (heightData == null) {
            return "";
        }
        else {
            return heightUnits;
        }
    }


    /**
     * Returns the value of url.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the value of url.
     * 
     * @param url
     *            The value to assign url.
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Returns the value of q2File.
     */
    public NetcdfFile getQ2File() {
        return q2File;
    }

    /**
     * Sets the value of q2File.
     * 
     * @param q2File
     *            The value to assign q2File.
     */
    public void setQ2File(NetcdfFile q2File) {
        this.q2File = q2File;
    }

    /**
     * Returns the value of creationDate.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of creationDate.
     * 
     * @param creationDate
     *            The value to assign creationDate.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the Variable name of the last decoded Grid Coverage.
     */
    public String getLastDecodedVariableName() {
        return currentVariableName;
    }

    /**
     * Returns the value of variableNames. Only valid after a call to
     * 'decodeData'
     */
    public String[] getVariableNames() {
        return variableNames;
    }

    /**
     * Returns the value of heightData.
     */
    public float[] getHeightData() {
        return heightData;
    }

    /**
     * Sets the value of heightData.
     * 
     * @param heightData
     *            The value to assign heightData.
     */
    public void setHeightData(float[] heightData) {
        this.heightData = heightData;
    }

    /**
     * Returns the value of latData.
     */
    public float[] getLatData() {
        return latData;
    }

    /**
     * Sets the value of latData.
     * 
     * @param latData
     *            The value to assign latData.
     */
    public void setLatData(float[] latData) {
        this.latData = latData;
    }

    /**
     * Returns the value of lonData.
     */
    public float[] getLonData() {
        return lonData;
    }

    /**
     * Sets the value of lonData.
     * 
     * @param lonData
     *            The value to assign lonData.
     */
    public void setLonData(float[] lonData) {
        this.lonData = lonData;
    }

    /**
     * Returns the units of the last decoded grid coverage.
     */
    public String getLastDecodedUnits() {
        return units;
    }

    /**
     * Returns the value of maxVal.
     */
    public double getMaxVal() {
        return maxVal;
    }

    /**
     * Sets the value of maxVal.
     * 
     * @param maxVal
     *            The value to assign maxVal.
     */
    public void setMaxVal(double maxVal) {
        this.maxVal = maxVal;
    }

    /**
     * Returns the value of minVal.
     */
    public double getMinVal() {
        return minVal;
    }

    /**
     * Sets the value of minVal.
     * 
     * @param minVal
     *            The value to assign minVal.
     */
    public void setMinVal(double minVal) {
        this.minVal = minVal;
    }

    public static void describeQPEFile(String inFile) {

        try {

            // Open a file readable by the NetCDF-Java library
            NetcdfFile ncFile = NetcdfFile.open(inFile);

            // List the attributes of the file (general information about data)
            System.out.println("\n\nNetCDF ATTRIBUTES: \n");
            List globalAtts = ncFile.getGlobalAttributes();
            for (int n = 0; n < globalAtts.size(); n++) {
                System.out.println(globalAtts.get(n).toString());
            }

            // Print specific attribute
            Attribute latAtt = ncFile.findGlobalAttribute("Latitude");
            System.out.println("\n\nLATITUDE ATTRIBUTE = "
                    + latAtt.getNumericValue());

            // List the variables present in the file (gate, azimuth, value)
            System.out.println("\n\nNetCDF VARIABLES: \n");
            List vars = ncFile.getVariables();
            for (int n = 0; n < vars.size(); n++) {
                System.out.println(((Variable) (vars.get(n))).toString());
            }

            // List the dimensions of the "value" array of data
            // String variableName = "pcp_flag";
            String variableName = "mrefl_mosaic";
            System.out.println("\n\n" + variableName + " DIMENSIONS: \n");
            Variable var = ncFile.findVariable(variableName);
            List dims = var.getDimensions();
            for (int n = 0; n < dims.size(); n++) {
                System.out.println(dims.get(n).toString());
            }
            /*
             * // Print the primitive datatype
             * System.out.println("\n\n"+variableName+" DATATYPE: \n");
             * System.out.println(var.getDataType().toString()); // Read the
             * data from the "value" array, create an index and print some
             * values System.out.println("\n\n"+variableName+" DATA: \n"); Array
             * data = var.read(); Index index = data.getIndex(); int[] shape =
             * data.getShape(); System.out.println("Data Array Dimensions: " +
             * shape[0] + " , " + shape[1] + " , " + shape[2]); // for (int i=0;
             * i<shape[0]; i++) { // for (int j=0; j<shape[1]; j++) { for (int
             * i = 0; i < 3; i++) { for (int j = 0; j < 3; j++) { for (int k =
             * 0; k < 3; k++) { System.out.println("i=" + i + " j=" + j + " k=" +
             * k +" value=" + data.getDouble(index.set(i, j, k))); } } }
             * 
             */

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
