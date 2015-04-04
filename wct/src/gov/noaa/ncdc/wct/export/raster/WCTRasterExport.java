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

package gov.noaa.ncdc.wct.export.raster;

//STANDARD IMPORTS
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.ui.WCTUiUtils;

import java.awt.Dimension;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.geotiff.GeoTiffWriter2;

/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * @created    July 20, 2004
 */
public class WCTRasterExport {

    public static enum GeoTiffType { TYPE_8_BIT, TYPE_32_BIT };
    
    private static final Logger logger = Logger.getLogger(WCTRasterExport.class.getName());

    // The list of event listeners.
    private Vector<GeneralProgressListener> listeners = new Vector<GeneralProgressListener>();

    public static final int DEFAULT_CHUNK_SIZE = 1;

    
    

    
    
    /**
     *  Saves data to specified file in ESRI ASCII Grid format.
     *
     * @param  f           Output file
     * @param  wctRaster   WCTRaster object
     */
    public void saveAsciiGrid(File f, WCTRaster wctRaster) 
    	throws WCTExportNoDataException, WCTExportException, IOException {
    	
    	saveAsciiGrid(f, wctRaster, WCTUtils.DECFMT_pDpppp);
    }
    
    
    /**
     *  Saves data to specified file in ESRI ASCII Grid format.
     *
     * @param  f           Output file
     * @param  wctRaster   WCTRaster object
     * @param  fmt         DecimalFormat object which determines the text formatting style for the data.
     */
    public void saveAsciiGrid(File f, WCTRaster wctRaster, DecimalFormat fmt) 
    	throws WCTExportNoDataException, WCTExportException, IOException {


        if (wctRaster.isEmptyGrid()) {
            throw new WCTExportNoDataException("No Data Present in File: "+f.toString());
        }

        logger.info(wctRaster.getLongName()+"\n"+wctRaster.getBounds()+"\n"+
                wctRaster.getCellWidth()+" x "+wctRaster.getCellHeight()+"\n"+
                wctRaster.getWritableRaster().getWidth()+" x "+wctRaster.getWritableRaster().getHeight());
        

        

        
        
        GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).started(event);
        }




        BufferedWriter bw = null;


        // Get decimal format to reduce string size of double output
//        java.text.DecimalFormat fmt7 = new java.text.DecimalFormat("0.0000000");
//        java.text.DecimalFormat fmt7 = new java.text.DecimalFormat("#.#######");

        // Condense raster from square to rectangle fitting rasterizer.getBounds()         
        WritableRaster raster = wctRaster.getWritableRaster();
        java.awt.geom.Rectangle2D.Double bounds = wctRaster.getBounds();
        double cellWidth = wctRaster.getCellWidth();
        double cellHeight = wctRaster.getCellHeight();
        
        if (Math.abs(cellWidth-cellHeight) > 0.0001) {
            throw new WCTExportException("The cell width and cell height must be identical for ESRI ASCII GRID export.\n"+
                    "cellWidth="+cellWidth+"  cellHeight="+cellHeight);
        }
        
        double noData = wctRaster.getNoDataValue();
        if (Double.isNaN(noData)) {
        	noData = -999;
        }
        
        
        int height = raster.getHeight();
        int width = raster.getWidth(); 
        // All the data should start in the lower left corner.  Don't export what we don't need.

        
        int ncols = width;
        int nrows = height;
        if (! wctRaster.isNative()) {
            Dimension dim = WCTUtils.getEqualDimensions(bounds, width, height);
            ncols = (int)dim.getWidth();
            nrows = (int)dim.getHeight();
        }

        
        // Begin export
        // Strip any provided extension
        File ascFile;
        if (f.toString().endsWith(".asc")) {
            f = new File(f.toString().substring(0, f.toString().length()-4));
        }
        
        if (wctRaster.isNative()) {
            ascFile = new File(f.toString() + "_native.asc");
        }
        else {
            ascFile = new File(f.toString() + ".asc");
        }
        
        bw = new BufferedWriter(new FileWriter(ascFile));
        bw.write("NCOLS " + ncols);
        bw.newLine();
        bw.write("NROWS " + nrows);
        bw.newLine();
        bw.write("XLLCORNER " + fmt.format(bounds.x));
        bw.newLine();
        bw.write("YLLCORNER " + fmt.format(bounds.y));
        bw.newLine();
        bw.write("CELLSIZE " + fmt.format(cellWidth));
        bw.newLine();
        bw.write("NODATA_VALUE " + fmt.format(noData));
        bw.newLine();

        for (int j = 0; j < nrows; j++) {
            for (int i = 0; i < ncols; i++) {
            	double value = raster.getSampleFloat(i+(width-ncols),  j+(height-nrows), 0);
            	if (Double.isNaN(value)) {
            		value = noData;
            	}
                bw.write(fmt.format(value));
                bw.write(" ");
            }
            bw.newLine();

            // Progress
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)j) / nrows ) * 100.0) );
                listeners.get(n).progress(event);
            }
        }
        bw.flush();
        bw.close();

        // Export projection file
//      if (rasterizer.isNative()) {
//      File prjFile = new File(f.getParent() + "/dpa_prj.info");
//      if (! prjFile.exists()) {
//      bw = new BufferedWriter(new FileWriter(prjFile));
//      bw.write(((NexradNativeRaster)rasterizer).getProjectionInfo());
//      bw.newLine();
//      bw.flush();
//      bw.close();
//      }
//      }
//      else {
//      // Write .prj file  -- All exported NEXRAD Data is in NAD83 LatLon Projection
//      bw = new BufferedWriter(new FileWriter(f.getParent() + "/jnx-ascii-export.prj"));
//      String prj = "GEOGCS[\"GCS_WGS_1984\","+
//      "DATUM[\"D_WGS_1984\","+
//      "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],"+
//      "PRIMEM[\"Greenwich\",0.0],"+
//      "UNIT[\"Degree\",0.0174532925199433]]";
//      bw.write(prj);
//      bw.close();
//      }


        String fileString = ascFile.toString();
        File prjFile = new File(fileString.substring(0, fileString.length()-4) + ".prj");
        bw = new BufferedWriter(new FileWriter(prjFile));
        if (wctRaster.isNative() && ! (wctRaster instanceof gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster)) {
            bw.write(((WCTNativeRaster)wctRaster).getESRIprj());
        }
        else {
            // Write .prj file  -- All exported NEXRAD Data is in NAD83 LatLon Projection
            bw.write(WCTProjections.NAD83_ESRI_PRJ);
        }
        bw.flush();
        bw.close();


        
        
        for (int n = 0; n < listeners.size(); n++) {
            event.setProgress(100);
            event.setStatus("");
            listeners.get(n).ended(event);
        }
    }


    /**
     *  Saves data to specified file in ESRI Binary Grid format. (.flt and .hdr files)
     *
     * @param  f           Output file
     * @param  wctRaster  NexradRaster object
     */
    public void saveBinaryGrid(File f, WCTRaster wctRaster) 
    throws WCTExportNoDataException, WCTExportException, IOException {

        saveBinaryGrid(f, wctRaster, true);
    }

    /**
     *  Saves data to specified file in ESRI Binary Grid format. (.flt and optional .hdr files)
     *
     * @param  f           Output file
     * @param  wctRaster  NexradRaster object
     * @param  makeHdrFile Create .hdr header file?
     */
    public void saveBinaryGrid(File f, WCTRaster wctRaster, boolean makeHdrFile) 
    throws WCTExportNoDataException, WCTExportException, IOException {


        if (wctRaster.isEmptyGrid()) {
            throw new WCTExportNoDataException("No Data Present in File: "+f.toString());
        }

        logger.info(wctRaster.getLongName()+"\n"+wctRaster.getBounds()+"\n"+
                wctRaster.getCellWidth()+" x "+wctRaster.getCellHeight()+"\n"+
                wctRaster.getWritableRaster().getWidth()+" x "+wctRaster.getWritableRaster().getHeight());
        





        GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).started(event);
        }




        // Get decimal format to reduce string size of double output
        java.text.DecimalFormat fmt7 = new java.text.DecimalFormat("0.0000000");
        
        BufferedWriter bw = null;

        // Condense raster from square to rectangle fitting rasterizer.getBounds()         
        WritableRaster raster = wctRaster.getWritableRaster();
        java.awt.geom.Rectangle2D.Double bounds = wctRaster.getBounds();
        double cellWidth = wctRaster.getCellWidth();
        double cellHeight = wctRaster.getCellHeight();
        
        if (Math.abs(cellWidth-cellHeight) > 0.0001) {
            throw new WCTExportException("The cell width and cell height must be identical for ESRI Binary GRID export.\n"+
                    "cellWidth="+cellWidth+"  cellHeight="+cellHeight);
        }
        double noData = wctRaster.getNoDataValue();

        int height = raster.getHeight();
        int width = raster.getWidth(); // should always = height!
        
        int ncols = width;
        int nrows = height;
        if (! wctRaster.isNative()) {
            Dimension dim = WCTUtils.getEqualDimensions(bounds, width, height);
            ncols = (int)dim.getWidth();
            nrows = (int)dim.getHeight();
        }

        // Begin data export
        
        // Strip any provided extension
        File fltFile;
        if (f.toString().endsWith(".flt")) {
            f = new File(f.toString().substring(0, f.toString().length()-4));
        }

        
        
        if (wctRaster.isNative()) {
            fltFile = new File(f.toString() + "_native.flt");
        }
        else {
            fltFile = new File(f.toString() + ".flt");
        }
        
        if (fltFile.exists()) {
            fltFile.delete();
        }

        // Create output file
        DataOutputStream os = new DataOutputStream (new BufferedOutputStream (
                new FileOutputStream (fltFile), DEFAULT_CHUNK_SIZE*1024));

        for (int j = 0; j < nrows; j++) {
            for (int i = 0; i < ncols; i++) {
                os.writeFloat(raster.getSampleFloat(i+(width-ncols),  j+(height-nrows), 0));
            }

            // Progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)j) / nrows ) * 100.0) );
                listeners.get(n).progress(event);
            }

        }
        os.flush();
        os.close();

        if (makeHdrFile) {
            // Begin header export
            File hdrFile;
            if (wctRaster.isNative()) {
                hdrFile = new File(f.toString() + "_native.hdr");
            }
            else {
                hdrFile = new File(f.toString() + ".hdr");
            }
            

            bw = new BufferedWriter(new FileWriter(hdrFile));
            bw.write("NCOLS " + ncols);
            bw.newLine();
            bw.write("NROWS " + nrows);
            bw.newLine();
            bw.write("XLLCORNER " + fmt7.format(bounds.x));
            bw.newLine();
            bw.write("YLLCORNER " + fmt7.format(bounds.y));
            bw.newLine();
            bw.write("CELLSIZE " + fmt7.format(cellWidth));
            bw.newLine();
            bw.write("NODATA_VALUE " + fmt7.format(noData));
            bw.newLine();
            bw.write("BYTEORDER MSBFIRST");
            bw.newLine();
            bw.flush();
            bw.close();
        }


        // Export projection file
//        if (rasterizer.isNative()) {
//            File prjFile = new File(f.getParent() + "/dpa_prj.info");
//            if (! prjFile.exists()) {
//                bw = new BufferedWriter(new FileWriter(prjFile));
//                bw.write(((NexradNativeRaster)rasterizer).getProjectionInfo());
//                bw.newLine();
//                bw.flush();
//                bw.close();
//            }
//        }
//        else {
//            // Write .prj file  -- All exported rasterized NEXRAD Data is in NAD83 LatLon Projection
//            bw = new BufferedWriter(new FileWriter(f.getParent() + "/jnx-ascii-export.prj"));
//            String prj = "GEOGCS[\"GCS_WGS_1984\","+
//            "DATUM[\"D_WGS_1984\","+
//            "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],"+
//            "PRIMEM[\"Greenwich\",0.0],"+
//            "UNIT[\"Degree\",0.0174532925199433]]";
//            bw.write(prj);
//            bw.close();
//        }

        String fileString = fltFile.toString();
        File prjFile = new File(fileString.substring(0, fileString.length()-4) + ".prj");
        bw = new BufferedWriter(new FileWriter(prjFile));
        if (wctRaster.isNative()) {
            bw.write(((WCTNativeRaster)wctRaster).getESRIprj());
        }
        else {
            // Write .prj file  -- All exported NEXRAD Data is in NAD83 LatLon Projection
            bw.write(WCTProjections.NAD83_ESRI_PRJ);
            
        }
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
     *  Saves data to specified file in GrADS Binary Grid format (.grd and .ctl files)
     *
     * @param  f           Output file
     * @param  wctRaster  NexradRaster object
     */
    public void saveGrADSBinary(File f, WCTRaster wctRaster) 
    throws WCTExportNoDataException, IOException {


        if (wctRaster.isEmptyGrid()) {
            throw new WCTExportNoDataException("No Data Present in NEXRAD File: "+f.toString());
        }




        GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).started(event);
        }





        BufferedWriter bw = null;

        // Condense raster from square to rectangle fitting rasterizer.getBounds()         
        WritableRaster raster = wctRaster.getWritableRaster();
        java.awt.geom.Rectangle2D.Double bounds = wctRaster.getBounds();
        double cellWidth = wctRaster.getCellWidth();
        double cellHeight = wctRaster.getCellHeight();
        double noData = wctRaster.getNoDataValue();

        int height = raster.getHeight();
        int width = raster.getWidth(); // should always = height!
        int ncols = width;
        int nrows = height;
        if (! wctRaster.isNative()) {
            Dimension dim = WCTUtils.getEqualDimensions(bounds, width, height);
            ncols = (int)dim.getWidth();
            nrows = (int)dim.getHeight();
        }

        // Begin data export
        File grdFile;
        if (f.toString().endsWith(".grd")) {
            grdFile = f;
        }
        else {
            grdFile = new File(f.toString() + ".grd");
        }
        if (grdFile.exists()) {
            grdFile.delete();
        }

        // Create output file
        DataOutputStream os = new DataOutputStream (new BufferedOutputStream (
                new FileOutputStream (grdFile), DEFAULT_CHUNK_SIZE*1024));

        for (int j = 0; j < nrows; j++) {
            for (int i = 0; i < ncols; i++) {
                os.writeFloat(raster.getSampleFloat(i+(width-ncols),  j+(height-nrows), 0));
            }

            // Progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)j) / nrows ) * 100.0) );
                listeners.get(n).progress(event);
            }

        }
        os.flush();
        os.close();

        // Begin header export
        File ctlFile;
        if (f.toString().endsWith(".ctl")) {
            ctlFile = f;
        }
        else {
            ctlFile = new File(f.toString() + ".ctl");
        }

        bw = new BufferedWriter(new FileWriter(ctlFile));
        bw.write("DSET ^" + grdFile.getName());
        bw.newLine();
        bw.write("TITLE NEXRAD Data File: "+f.getName());
        bw.newLine();
        bw.write("UNDEF " + noData);
        bw.newLine();
        bw.write("OPTIONS big_endian"); // Java writes everything in BIG_ENDIAN unless using java.nio.* libraries
        bw.newLine();
        bw.write("OPTIONS yrev"); // Tell GrADS to flip the y-axis when loading the data
        bw.newLine();
        bw.write("XDEF " + ncols + " LINEAR " + bounds.x + " " + cellWidth);
        bw.newLine();
        bw.write("YDEF " + nrows + " LINEAR " + bounds.y + " " + cellHeight);
        bw.newLine();
        bw.write("ZDEF 1 LINEAR 1 1");
        bw.newLine();
        bw.write("TDEF 1 LINEAR  1jan1950 1yr");
        bw.newLine();
        bw.write("VARS 1");
        bw.newLine();
        bw.write("nexrad 0 1 "+f.getName());
        bw.newLine();
        bw.write("ENDVARS");
        //bw.newLine(); Don't put new line here!  Will cause GrADS error!

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
     *  Saves data to specified file in NetCDF format
     *
     * @param  f           Output file
     * @param  wctRaster  NexradRaster object
     * @param  rangeModel  BoundedRangeModel from a progress bar object
     */
    public void saveNetCDF(File f, WCTRaster wctRaster) 
    throws WCTExportNoDataException, IOException, InvalidRangeException {



//        logger.info(wctRaster.getLongName()+"\n"+wctRaster.getBounds()+"\n"+
//                wctRaster.getCellWidth()+" x "+wctRaster.getCellHeight()+"\n"+
//                wctRaster.getWritableRaster().getWidth()+" x "+wctRaster.getWritableRaster().getHeight());

        System.out.println(wctRaster.getLongName()+"\n"+wctRaster.getBounds()+"\n"+
                wctRaster.getCellWidth()+" x "+wctRaster.getCellHeight()+"\n"+
                wctRaster.getWritableRaster().getWidth()+" x "+wctRaster.getWritableRaster().getHeight());
        
        
        




        GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).started(event);
        }





        //NetcdfFile ncfileIn = ucar.nc2.dataset.NetcdfDataset.openFile(datasetIn, null);
        //NetcdfFile ncfileOut = ucar.nc2.FileWriter.writeToFile( ncfileIn, filenameOut);
        //ncfileOut.close();


        // create NetCDF data structure
        // Condense raster from square to rectangle fitting rasterizer.getBounds()         
        WritableRaster raster = wctRaster.getWritableRaster();
        java.awt.geom.Rectangle2D.Double bounds = wctRaster.getBounds();
        double cellWidth = wctRaster.getCellWidth();
        double cellHeight = wctRaster.getCellHeight();

        int height = raster.getHeight();
        int width = raster.getWidth(); // should always = height!

        double[][][] data = new double[1][raster.getHeight()][raster.getWidth()];
        for (int j = 0; j < raster.getHeight(); j++) {
            for (int i = 0; i < raster.getWidth(); i++) {
                data[0][raster.getHeight()-j-1][i] = raster.getSampleFloat(i+(width-raster.getWidth()),  j+(height-raster.getHeight()), 0);
            }
        }



        if (! f.toString().endsWith(".nc")) {
            f = new File(f.toString()+".nc");
        }
        NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(f.toString(), true);
        
        // is a time dimension needed?
        boolean isTimeDimensionNeeded = ( wctRaster.getDateInMilliseconds() != Long.MIN_VALUE );
        
        
        // define dimensions
        ucar.nc2.Dimension latDim = ncfile.addDimension("lat", raster.getHeight());
        ucar.nc2.Dimension lonDim = ncfile.addDimension("lon", raster.getWidth());
        ucar.nc2.Dimension timeDim = null;
        ucar.nc2.Dimension[] dimArray;
        if (isTimeDimensionNeeded) {
            timeDim = ncfile.addDimension("time", 1);
            dimArray = new ucar.nc2.Dimension[] { timeDim, latDim, lonDim };
        }
        else {
            dimArray = new ucar.nc2.Dimension[] { latDim, lonDim };
        }
//        System.out.println("exporting variable '"+wctRaster.getVariableName()+"' to CF-Gridded NetCDF");
        ncfile.addVariable(wctRaster.getVariableName(), ucar.ma2.DataType.DOUBLE, dimArray);
        ncfile.addVariableAttribute(wctRaster.getVariableName(), "long_name", wctRaster.getLongName());
        ncfile.addVariableAttribute(wctRaster.getVariableName(), "missing_value", wctRaster.getNoDataValue());
        ncfile.addVariableAttribute(wctRaster.getVariableName(), "units", wctRaster.getUnits());

        String dateString = "";
        
        if (isTimeDimensionNeeded) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            calendar.setTimeInMillis(wctRaster.getDateInMilliseconds());
            DecimalFormat fmt02 = new DecimalFormat("00");
            dateString = fmt02.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
            fmt02.format(calendar.get(Calendar.MINUTE)) + ":" +
            fmt02.format(calendar.get(Calendar.SECOND)) + " UTC  " +
            fmt02.format(calendar.get(Calendar.MONTH)+1) + "/" +
            fmt02.format(calendar.get(Calendar.DATE)) + "/" +
            fmt02.format(calendar.get(Calendar.YEAR));            
        }

        String title = wctRaster.getLongName() + "  " + dateString;




        ncfile.addGlobalAttribute("title", title);
        ncfile.addGlobalAttribute("Conventions", "CF-1.0");
        ncfile.addGlobalAttribute("History", "Exported to NetCDF-3 CF-1.0 conventions by the NOAA Weather and " +
                "Climate Toolkit (version "+WCTUiUtils.getVersion()+")  \n" +
                        "Export Date: "+new Date());



        // float lat(lat) ;
        // lat:units = "degrees_north" ;
        ncfile.addVariable("lat", ucar.ma2.DataType.DOUBLE, new ucar.nc2.Dimension[] {latDim});
        ncfile.addVariableAttribute("lat", "units", "degrees_north");
        ncfile.addVariableAttribute("lat", "spacing", String.valueOf(cellHeight));
        ncfile.addVariableAttribute("lat", "datum", "NAD83 - NOAA Standard");
        // float lon(lon) ;
        // lon:units = "degrees_east" ;
        ncfile.addVariable("lon", ucar.ma2.DataType.DOUBLE, new ucar.nc2.Dimension[] {lonDim});
        ncfile.addVariableAttribute("lon", "units", "degrees_east");         
        ncfile.addVariableAttribute("lon", "spacing", String.valueOf(cellWidth));
        ncfile.addVariableAttribute("lon", "datum", "NAD83 - NOAA Standard");

        if (isTimeDimensionNeeded) {
            // int time(time) ;
            // time:units = "hours" ;
            ncfile.addVariable("time", ucar.ma2.DataType.INT, new ucar.nc2.Dimension[] {timeDim});
            ncfile.addVariableAttribute("time", "units", "seconds since 1970-1-1");     
        }
        
        ncfile.addGlobalAttribute("geographic_datum_ESRI_PRJ", WCTProjections.NAD83_ESRI_PRJ);
        ncfile.addGlobalAttribute("geographic_datum_OGC_WKT", WCTProjections.NAD83_WKT);
        

        // create the file
        ncfile.create();
        logger.fine( "ncfile = "+ ncfile);


        double maxVal = -99999;
        double minVal = 99999;

        ucar.ma2.ArrayDouble valueArray = null;
        if (isTimeDimensionNeeded) {
            
            valueArray = new ucar.ma2.ArrayDouble.D3(timeDim.getLength(), latDim.getLength(), lonDim.getLength());
            ucar.ma2.Index ima = valueArray.getIndex();
            // write
            for (int i=0; i<timeDim.getLength(); i++) {
                for (int j=0; j<latDim.getLength(); j++) {
                    for (int k=0; k<lonDim.getLength(); k++) {

                        if (data[i][j][k] != wctRaster.getNoDataValue()) {
                            valueArray.setDouble(ima.set(i,j,k), data[i][j][k]);

                            maxVal = (data[i][j][k] > maxVal) ? data[i][j][k] : maxVal;
                            minVal = (data[i][j][k] < minVal) ? data[i][j][k] : minVal;

                        }
                        else {
                            valueArray.setDouble(ima.set(i,j,k), Double.NaN);
                            //valueArray.setDouble(ima.set(i,j,k), 0.0);
                        }
                        //valueArray.setDouble(ima.set(i,j,k), data[i][j][k]);
                    }

                    // Progress
                    // --------------
                    for (int n = 0; n < listeners.size(); n++) {
                        event.setProgress( (int)( ( ((double)j) / latDim.getLength() ) * 100.0) );
                        listeners.get(n).progress(event);
                    }
                }
            }

        }
        else {
            valueArray = new ucar.ma2.ArrayDouble.D2(latDim.getLength(), lonDim.getLength());
            ucar.ma2.Index ima = valueArray.getIndex();
            // write
            for (int j=0; j<latDim.getLength(); j++) {
                for (int k=0; k<lonDim.getLength(); k++) {

                    if (data[0][j][k] != wctRaster.getNoDataValue()) {
                        valueArray.setDouble(ima.set(j,k), data[0][j][k]);

                        maxVal = (data[0][j][k] > maxVal) ? data[0][j][k] : maxVal;
                        minVal = (data[0][j][k] < minVal) ? data[0][j][k] : minVal;

                    }
                    else {
                        valueArray.setDouble(ima.set(j,k), Double.NaN);
                        //valueArray.setDouble(ima.set(i,j,k), 0.0);
                    }
                    //valueArray.setDouble(ima.set(i,j,k), data[i][j][k]);
                }

                // Progress
                // --------------
                for (int n = 0; n < listeners.size(); n++) {
                    event.setProgress( (int)( ( ((double)j) / latDim.getLength() ) * 100.0) );
                    listeners.get(n).progress(event);
                }
            }
        }


        logger.fine("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
        logger.fine("MAX VAL: "+maxVal);
        logger.fine("MIN VAL: "+minVal);
        logger.fine("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");

        ncfile.write(wctRaster.getVariableName(), valueArray);

        /* Store the rest of variable values */
        ucar.ma2.ArrayDouble latArray = new ucar.ma2.ArrayDouble.D1(latDim.getLength());
        ucar.ma2.Index latIndex = latArray.getIndex();
        ucar.ma2.ArrayDouble lonArray = new ucar.ma2.ArrayDouble.D1(lonDim.getLength());
        ucar.ma2.Index lonIndex = lonArray.getIndex();
        for (int n=0; n<raster.getHeight(); n++) {
//        	System.out.println((bounds.y + n*cellHeight) + " " +cellWidth+" , "+bounds);
            latArray.setDouble(latIndex.set(n), bounds.y + n*cellHeight);
        }
        for (int n=0; n<raster.getWidth(); n++) {
//        	System.out.println((bounds.x + n*cellWidth) + " " +cellWidth+" , "+bounds);
            lonArray.setDouble(lonIndex.set(n), bounds.x + n*cellWidth);
        }
        ncfile.write("lat", latArray);
        ncfile.write("lon", lonArray);

        
        
        if (isTimeDimensionNeeded) {
        	ucar.ma2.ArrayInt timeArray = new ucar.ma2.ArrayInt.D1(timeDim.getLength());
        	ucar.ma2.Index timeIndex = timeArray.getIndex();
            timeArray.setLong(timeIndex.set(0), (long)(wctRaster.getDateInMilliseconds()/1000.0));
            ncfile.write("time", timeArray);
        }


        ncfile.close();

        // End
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).ended(event);
        }

    }




    /**
     *  Saves data to specified file in GeoTIFF format
     *
     * @param  f           Output file
     * @param  wctRaster  NexradRaster object
     * @param  rangeModel  BoundedRangeModel from a progress bar object
     */
    public void saveGeoTIFF(File f, WCTRaster wctRaster, GeoTiffType type) 
    throws WCTExportNoDataException, IOException, InvalidRangeException {



        // USE saveNetCDF GeneralProgressEvents

        logger.info(wctRaster.getLongName()+"\n"+wctRaster.getBounds()+"\n"+
                wctRaster.getCellWidth()+" x "+wctRaster.getCellHeight()+"\n"+
                wctRaster.getWritableRaster().getWidth()+" x "+wctRaster.getWritableRaster().getHeight());
        

        


        if (! f.toString().endsWith(".tif")) {
            f = new File(f.toString()+".tif");
        }



        java.awt.geom.Rectangle2D.Double bounds = wctRaster.getBounds();
        double x = bounds.getMinX();
        double y = bounds.getMinY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();


        int timeStamp = (int)System.currentTimeMillis()%1000000;
        File tmpFile = new File(WCTConstants.getInstance().getDataCacheLocation() + File.separator + f.getName()+timeStamp+".nc");
        tmpFile.deleteOnExit();

        saveNetCDF(tmpFile, wctRaster);


        ucar.unidata.geoloc.LatLonPointImpl p1 = new ucar.unidata.geoloc.LatLonPointImpl(y, x);
        ucar.unidata.geoloc.LatLonPointImpl p2 = new ucar.unidata.geoloc.LatLonPointImpl(y+height, x+width);
        ucar.unidata.geoloc.LatLonRect llr = new ucar.unidata.geoloc.LatLonRect(p1, p2);
//        ucar.nc2.geotiff.JNXGeotiffWriter writer = new ucar.nc2.geotiff.JNXGeotiffWriter(f.toString());
        GeoTiffWriter2 writer = new GeoTiffWriter2(f.toString());
//        writer.setMissingValue(wctRaster.getNoDataValue());
//        writer.writeGrid(tmpFile.toURI().toURL().toString(), "value", 0, 0, true, llr);
        if (type == GeoTiffType.TYPE_8_BIT) {
            writer.writeGrid(tmpFile.toString(), wctRaster.getVariableName(), 0, 0, true, llr);
        }
        else if (type == GeoTiffType.TYPE_32_BIT) {
            writer.writeGrid(tmpFile.toString(), wctRaster.getVariableName(), 0, 0, false, llr);            
        }
        else {
            throw new IOException("GeoTIFF output type invalid - currently only 8 bit and 32 bit supported"); 
        }
        writer.close();

        

//        GridDataset dataset = ucar.nc2.dt.grid.GridDataset.open(tmpFile.toURI().toURL().toString());
//        GridDatatype grid = dataset.findGridDatatype("value");
//        GeotiffWriter writer = new GeotiffWriter(f.toString());
//        writer.writeGrid(dataset, grid, grid.readDataSlice(-1, -1, -1, -1), true);
//        writer.close();
//        dataset.close();




    }   







    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


    /**
     *  Saves data to specified file in NetCDF format
     *
     * @param  f           Output file
     * @param  wctRaster  WCTRaster with multi-band underlying WritableRaster
     * used to store each height level.
     */
    public void saveNetCDF(File f, WCTRaster wctRaster, double[] heightsInMeters) 
    throws WCTExportNoDataException, IOException, InvalidRangeException {



//        logger.info(wctRaster.getLongName()+"\n"+wctRaster.getBounds()+"\n"+
//                wctRaster.getCellWidth()+" x "+wctRaster.getCellHeight()+"\n"+
//                wctRaster.getWritableRaster().getWidth()+" x "+wctRaster.getWritableRaster().getHeight());
        
        




        GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).started(event);
        }






        // create NetCDF data structure
        // Condense raster from square to rectangle fitting rasterizer.getBounds()         


        WritableRaster raster = wctRaster.getWritableRaster();
        java.awt.geom.Rectangle2D.Double bounds = wctRaster.getBounds();
        double cellWidth = wctRaster.getCellWidth();
        double cellHeight = wctRaster.getCellHeight();
        
        int zBands = wctRaster.getWritableRaster().getNumBands();
        
        double[][][][] data = new double[1][zBands][raster.getHeight()][raster.getWidth()];
        
        for (int z=0; z<zBands; z++) {
        	
            raster = wctRaster.getWritableRaster();
        	
            int height = raster.getHeight();
            int width = raster.getWidth(); // should always = height!
            
        	for (int j=0; j<raster.getHeight(); j++) {
        		for (int i=0; i<raster.getWidth(); i++) {
        			data[0][z][raster.getHeight()-j-1][i] = raster.getSampleFloat(i+(width-raster.getWidth()),  j+(height-raster.getHeight()), z);
        		}
        	}
        }



        if (! f.toString().endsWith(".nc")) {
            f = new File(f.toString()+".nc");
        }
        NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(f.toString(), true);
        
        // is a time dimension needed?
        boolean isTimeDimensionNeeded = ( wctRaster.getDateInMilliseconds() != Long.MIN_VALUE );
        
        
        // define dimensions
        ucar.nc2.Dimension latDim = ncfile.addDimension("lat", raster.getHeight());
        ucar.nc2.Dimension lonDim = ncfile.addDimension("lon", raster.getWidth());
        ucar.nc2.Dimension heightDim = ncfile.addDimension("height", zBands);
        ucar.nc2.Dimension timeDim = null;
        ucar.nc2.Dimension[] dimArray;
        if (isTimeDimensionNeeded) {
            timeDim = ncfile.addDimension("time", 1);
            dimArray = new ucar.nc2.Dimension[] { timeDim, heightDim, latDim, lonDim };
        }
        else {
            dimArray = new ucar.nc2.Dimension[] { heightDim, latDim, lonDim };
        }
//        System.out.println("exporting variable '"+wctRaster.getVariableName()+"' to CF-Gridded NetCDF");
        ncfile.addVariable(wctRaster.getVariableName(), ucar.ma2.DataType.DOUBLE, dimArray);
        ncfile.addVariableAttribute(wctRaster.getVariableName(), "long_name", wctRaster.getLongName());
        ncfile.addVariableAttribute(wctRaster.getVariableName(), "missing_value", wctRaster.getNoDataValue());
        ncfile.addVariableAttribute(wctRaster.getVariableName(), "units", wctRaster.getUnits());

        String dateString = "";
        
        if (isTimeDimensionNeeded) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            calendar.setTimeInMillis(wctRaster.getDateInMilliseconds());
            DecimalFormat fmt02 = new DecimalFormat("00");
            dateString = fmt02.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
            fmt02.format(calendar.get(Calendar.MINUTE)) + ":" +
            fmt02.format(calendar.get(Calendar.SECOND)) + " UTC  " +
            fmt02.format(calendar.get(Calendar.MONTH)+1) + "/" +
            fmt02.format(calendar.get(Calendar.DATE)) + "/" +
            fmt02.format(calendar.get(Calendar.YEAR));            
        }

        String title = wctRaster.getLongName() + "  " + dateString;




        ncfile.addGlobalAttribute("title", title);
        ncfile.addGlobalAttribute("Conventions", "CF-1.0");
        ncfile.addGlobalAttribute("History", "Exported to NetCDF-3 CF-1.0 conventions by the NOAA Weather and " +
                "Climate Toolkit (version "+WCTUiUtils.getVersion()+")  \n" +
                        "Export Date: "+new Date());



        // float lat(lat) ;
        // lat:units = "degrees_north" ;
        ncfile.addVariable("lat", ucar.ma2.DataType.DOUBLE, new ucar.nc2.Dimension[] {latDim});
        ncfile.addVariableAttribute("lat", "units", "degrees_north");
        ncfile.addVariableAttribute("lat", "spacing", String.valueOf(cellHeight));
        ncfile.addVariableAttribute("lat", "datum", "NAD83 - NOAA Standard");
        // float lon(lon) ;
        // lon:units = "degrees_east" ;
        ncfile.addVariable("lon", ucar.ma2.DataType.DOUBLE, new ucar.nc2.Dimension[] {lonDim});
        ncfile.addVariableAttribute("lon", "units", "degrees_east");         
        ncfile.addVariableAttribute("lon", "spacing", String.valueOf(cellWidth));
        ncfile.addVariableAttribute("lon", "datum", "NAD83 - NOAA Standard");
        // float height(height) ;
        // lon:units = "meters" ;
        ncfile.addVariable("height", ucar.ma2.DataType.DOUBLE, new ucar.nc2.Dimension[] {heightDim});
        ncfile.addVariableAttribute("height", "units", "meters");         
        ncfile.addVariableAttribute("height", "positive", "up");         

        if (isTimeDimensionNeeded) {
            // int time(time) ;
            // time:units = "hours" ;
            ncfile.addVariable("time", ucar.ma2.DataType.INT, new ucar.nc2.Dimension[] {timeDim});
            ncfile.addVariableAttribute("time", "units", "seconds since 1970-1-1");     
        }
        
        ncfile.addGlobalAttribute("geographic_datum_ESRI_PRJ", WCTProjections.NAD83_ESRI_PRJ);
        ncfile.addGlobalAttribute("geographic_datum_OGC_WKT", WCTProjections.NAD83_WKT);
        

        // create the file
        ncfile.create();
        logger.fine( "ncfile = "+ ncfile);


        double maxVal = -99999;
        double minVal = 99999;

        ucar.ma2.ArrayDouble valueArray = null;
        if (isTimeDimensionNeeded) {
            
            valueArray = new ucar.ma2.ArrayDouble.D4(timeDim.getLength(), heightDim.getLength(), latDim.getLength(), lonDim.getLength());
            ucar.ma2.Index ima = valueArray.getIndex();
            // write
            for (int i=0; i<timeDim.getLength(); i++) {
                for (int z=0; z<heightDim.getLength(); z++) {
                	for (int j=0; j<latDim.getLength(); j++) {
                		for (int k=0; k<lonDim.getLength(); k++) {

                			if (data[i][z][j][k] != wctRaster.getNoDataValue()) {
                				valueArray.setDouble(ima.set(i,z, j,k), data[i][z][j][k]);

                				maxVal = (data[i][z][j][k] > maxVal) ? data[i][z][j][k] : maxVal;
                				minVal = (data[i][z][j][k] < minVal) ? data[i][z][j][k] : minVal;

                			}
                			else {
                				valueArray.setDouble(ima.set(i,z,j,k), Double.NaN);
                				//valueArray.setDouble(ima.set(i,j,k), 0.0);
                			}
                        //	valueArray.setDouble(ima.set(i,j,k), data[i][j][k]);
                		}
                	

                		// 	Progress
                		// 	--------------
                		for (int n = 0; n < listeners.size(); n++) {
                			event.setProgress( (int)( ( ((double)j) / latDim.getLength() ) * 100.0) );
                			listeners.get(n).progress(event);
                		}
                	}
                }
            }

        }
        else {
            valueArray = new ucar.ma2.ArrayDouble.D3(heightDim.getLength(), latDim.getLength(), lonDim.getLength());
            ucar.ma2.Index ima = valueArray.getIndex();
            // write
            for (int z=0; z<heightDim.getLength(); z++) {
            for (int j=0; j<latDim.getLength(); j++) {
                for (int k=0; k<lonDim.getLength(); k++) {

                    if (data[0][z][j][k] != wctRaster.getNoDataValue()) {
                        valueArray.setDouble(ima.set(z,j,k), data[0][z][j][k]);

                        maxVal = (data[0][z][j][k] > maxVal) ? data[0][z][j][k] : maxVal;
                        minVal = (data[0][z][j][k] < minVal) ? data[0][z][j][k] : minVal;

                    }
                    else {
                        valueArray.setDouble(ima.set(j,k), Double.NaN);
                        //valueArray.setDouble(ima.set(i,j,k), 0.0);
                    }
                    //valueArray.setDouble(ima.set(i,j,k), data[i][j][k]);
                }

                // Progress
                // --------------
                for (int n = 0; n < listeners.size(); n++) {
                    event.setProgress( (int)( ( ((double)j) / latDim.getLength() ) * 100.0) );
                    listeners.get(n).progress(event);
                }
            }
            }
        }


        logger.fine("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
        logger.fine("MAX VAL: "+maxVal);
        logger.fine("MIN VAL: "+minVal);
        logger.fine("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");

        ncfile.write(wctRaster.getVariableName(), valueArray);

        /* Store the rest of variable values */
        ucar.ma2.ArrayDouble latArray = new ucar.ma2.ArrayDouble.D1(latDim.getLength());
        ucar.ma2.Index latIndex = latArray.getIndex();
        ucar.ma2.ArrayDouble lonArray = new ucar.ma2.ArrayDouble.D1(lonDim.getLength());
        ucar.ma2.Index lonIndex = lonArray.getIndex();
        ucar.ma2.ArrayDouble heightArray = new ucar.ma2.ArrayDouble.D1(heightDim.getLength());
        ucar.ma2.Index heightIndex = heightArray.getIndex();
        ucar.ma2.ArrayInt timeArray = new ucar.ma2.ArrayInt.D1(timeDim.getLength());
        ucar.ma2.Index timeIndex = timeArray.getIndex();
        for (int n=0; n<raster.getHeight(); n++) {
            latArray.setDouble(latIndex.set(n), bounds.y + n*cellHeight);
        }
        for (int n=0; n<raster.getWidth(); n++) {
            lonArray.setDouble(lonIndex.set(n), bounds.x + n*cellWidth);
        }
        for (int n=0; n<heightsInMeters.length; n++) {
            heightArray.setDouble(heightIndex.set(n), heightsInMeters[n]);
        }
        timeArray.setLong(timeIndex.set(0), (long)(wctRaster.getDateInMilliseconds()/1000.0));

        ncfile.write("lat", latArray);
        ncfile.write("lon", lonArray);
        ncfile.write("height", heightArray);
        ncfile.write("time", timeArray);

        ncfile.close();

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

