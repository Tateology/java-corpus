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

package gov.noaa.ncdc.nexradexport;



//JNX decoders
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.Level2Transfer;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradValueFactory;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.event.DataExportEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.io.WCTTransfer;

import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Formatter;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.gc.GridCoverage;
import org.xml.sax.SAXException;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;


public class ExtractCAPPI {

    public static DecimalFormat fmt02 = new DecimalFormat("0.00");



    private int numHeightIntervals = 6;
    private double startHeight=0.0;
    private double endHeight = 12000.0;
    private double overlap = 500.0;

    private WCTFilter nxfilter;
    private WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();

    // The list of event listeners.
    private final Vector<GeneralProgressListener> listeners = new Vector<GeneralProgressListener>();
    private final GeneralProgressEvent event = new GeneralProgressEvent(this);

    private int currentZIndex = 0;
    private int currentCut = 0;


    public ExtractCAPPI() {      
    }

    public ExtractCAPPI(int numHeightIntervals, double startHeight, 
            double endHeight, double overlap) {

        this.numHeightIntervals = numHeightIntervals;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.overlap = overlap;
    }


    public void setNumHeightIntervals(int num) {
        this.numHeightIntervals = num;
    }

    public void setStartHeight(double heightInMeters) {
        this.startHeight = heightInMeters;
    }

    public void setEndHeight(double heightInMeters) {
        this.endHeight = heightInMeters;
    }

    public void setOverlap(double heightInMeters) {
        this.overlap = heightInMeters;  
    }

    public void setNexradFilter(WCTFilter nxfilter) {
        this.nxfilter = nxfilter;
    }


    public int getCurrentZIndex() {
        return currentZIndex;
    }
    public int getCurrentCut() {
        return currentCut;
    }


    /*
     
   TODO:
   Write volume out to temp. NetCDF file to reduce RAM needs.  If needed in another format, convert NetCDF to that
   format (ex. VTK).

     */





    /**
     * Defaults to 600x600 grid.
     * @throws DecodeHintNotSupportedException 
     * @throws ParseException 
     * @throws SQLException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public void convertToVtkStructuredGrid(File inputFile, File outputFile, java.awt.geom.Rectangle2D.Double bounds) 
        throws IOException, IllegalAttributeException, StreamingProcessException, DecodeException, 
            ParseException, DecodeHintNotSupportedException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {

        // remember the rasterizer is a StreamingProcess too!
        WCTRasterizer rasterizer = new WCTRasterizer(600, 600, -999.0f); // x, y, noData

        //--------------------------------------------------------------------
        // Set up rasterizer
        //--------------------------------------------------------------------

        // Sets the attribute from the Feature that will represent the pixel value 
        rasterizer.setAttName("value");
        // Sets the bounds for the raster

        // Use the 230 km bounds for this test - hard code pcode 19 for N0R
        //rasterizer.setBounds(CalcNexradExtent.getNexradExtent(header.getLat(), header.getLon(), 19));

        // KVWX rasterizer.setBounds(new java.awt.geom.Rectangle2D.Double(-87.80, 37.75, 0.5, 0.5));
        // KTWX rasterizer.setBounds(new java.awt.geom.Rectangle2D.Double(-94.97, 38.94, 0.5, 0.5));
        //rasterizer.setBounds(new java.awt.geom.Rectangle2D.Double(-82.6, 26.4, 1.2, 1.2));
        rasterizer.setBounds(bounds);
        // Usually you would want these bounds generated from the header object
        //rasterizer.setBounds(header.getNexradBounds());

        // Smoothing
        gcSupport.setSmoothFactor(10);



        convertToVtkStructuredGrid(rasterizer, "Reflectivity", inputFile.toURI().toURL(), outputFile);

    }





















    public void convertToVtkStructuredGrid(WCTRasterizer rasterizer, String variable, URL inputURL, File outputFile) 
        throws IOException, IllegalAttributeException, StreamingProcessException, DecodeException, 
            ParseException, DecodeHintNotSupportedException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {

        convertToVtkStructuredGrid(rasterizer, variable, inputURL, outputFile, null);
    }


    public void convertToVtkStructuredGrid(WCTRasterizer rasterizer, String variable, URL inputURL, 
            File outputFile, DataExportEvent progressEvent)   
    throws IOException, IllegalAttributeException, StreamingProcessException, DecodeException, 
        ParseException, DecodeHintNotSupportedException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {





//      GeneralProgressEvent event = new GeneralProgressEvent(this);

        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            ((GeneralProgressListener) listeners.get(i)).started(event);
        }




        String momentName = "Unknown";
        String momentUnits = "";

        if (variable.equals("Reflectivity")) {
            momentName = "Reflectivity";
            momentUnits = "dBZ";
        }
        else if (variable.equals("RadialVelocity")) {
            momentName = "Velocity";
            momentUnits = "kts";
        }
        else if (variable .equals("SpectrumWidth")) {
            momentName = "Spectrum Width";
            momentUnits = "kts";
        }


        System.out.println("PROCESSING convertToVtkStructuredGrid , MOMENT: "+momentName+" :: URL: "+inputURL.getFile());
        System.out.println("  MARK 0");

        FileScanner nexradFile = new FileScanner();
        nexradFile.scanURL(inputURL);
        // Check for file compression
        if (nexradFile.isZCompressed()) {
            System.out.println("DECOMPRESSING .Z : "+inputURL);               
            inputURL = Level2Transfer.getNCDCLevel2UNIXZ(inputURL);
        }
        else if (nexradFile.isGzipCompressed()) {
            System.out.println("DECOMPRESSING .gz : "+inputURL);               
            inputURL = Level2Transfer.getNCDCLevel2GZIP(inputURL);
        }
        else if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.RADIAL) {
            inputURL = WCTTransfer.getURL(inputURL);
            // HARD CODE - for right now, all Level2 files will be checked for "AR2V0001" partial BZIP format
            inputURL = Level2Transfer.decompressAR2V0001(inputURL);
            nexradFile.scanURL(inputURL);
        }
        else {
            // Transfer file to local tmp area -- force overwrite if NWS
            if (nexradFile.getLastScanResult().getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
                inputURL = WCTTransfer.getURL(inputURL, true);
            }               
            else {
                inputURL = WCTTransfer.getURL(inputURL);
            }
            nexradFile.scanURL(inputURL);
        }




        CancelTask emptyCancelTask = new CancelTask() {
			@Override
            public boolean isCancel() {
                return false;
            }
			@Override
            public void setError(String arg0) {
            }
			@Override
			public void setProgress(String arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
        };

        RadialDatasetSweep radialDataset = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
                FeatureType.RADIAL, 
                inputURL.toString(), emptyCancelTask, new Formatter());

        DecodeRadialDatasetSweepHeader radialDatasetHeader = new DecodeRadialDatasetSweepHeader();
        radialDatasetHeader.setRadialDatasetSweep(radialDataset);





        // Set up rasterizer         
        rasterizer.setLongName(NexradUtilities.getLongName(radialDatasetHeader));
        rasterizer.setUnits(NexradUtilities.getUnits(radialDatasetHeader));
        rasterizer.setDateInMilliseconds(radialDatasetHeader.getMilliseconds());

        // Sets the attribute from the Feature that will represent the pixel value 
        rasterizer.setAttName("value");
        // Sets the bounds for the raster





        double heightInterval = (endHeight - startHeight)/numHeightIntervals;
        double[] heightArray = new double[numHeightIntervals+1];
        for (int n=0; n<heightArray.length; n++) {
            heightArray[n] = startHeight + n*heightInterval;            
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

        bw.write("# vtk DataFile Version 2.0\n");
        bw.write("NEXRAD Level-II Data - "+radialDatasetHeader.getICAO()+" - GEN TIME:  " +
                radialDatasetHeader.getDate() + " " + radialDatasetHeader.getHourString() +
                ":" + radialDatasetHeader.getMinuteString() + ":" + radialDatasetHeader.getSecondString()+" - "+momentName+"\n");
        bw.write("ASCII\n");
        bw.write("\n");
        bw.write("DATASET STRUCTURED_POINTS\n");
        bw.write("DIMENSIONS  "+rasterizer.getWidth()+"  "+rasterizer.getHeight()+"  "+numHeightIntervals+"\n");
        bw.write("ORIGIN  "+rasterizer.getBounds().x+"  "+rasterizer.getBounds().y+"  "+heightArray[0]+"\n");
        bw.write("SPACING "+rasterizer.getCellWidth()+"  "+rasterizer.getCellHeight()+"  "+(heightArray[1]-heightArray[0])/10000+"\n");
        bw.write("\n");
        bw.write("POINT_DATA  "+rasterizer.getWidth()*rasterizer.getHeight()*numHeightIntervals+"\n");
        bw.write("SCALARS "+momentName+"("+momentUnits+") float\n");
        bw.write("LOOKUP_TABLE default\n");
        bw.write("\n");

        // echo to standard out
        System.out.print("# vtk DataFile Version 2.0\n");
        System.out.print("NEXRAD Level-II Data - "+radialDatasetHeader.getICAO()+" - GEN TIME:  " +
                radialDatasetHeader.getDate() + " " + radialDatasetHeader.getHourString() +
                ":" + radialDatasetHeader.getMinuteString() + ":" + radialDatasetHeader.getSecondString()+" - "+momentName+"\n");
        System.out.print("ASCII\n");
        System.out.print("\n");
        System.out.print("DATASET STRUCTURED_POINTS\n");
        System.out.print("DIMENSIONS  "+rasterizer.getWidth()+"  "+rasterizer.getHeight()+"  "+numHeightIntervals+"\n");
        System.out.print("ORIGIN  "+rasterizer.getBounds().x+"  "+rasterizer.getBounds().y+"  "+heightArray[0]+"\n");
        System.out.print("SPACING "+rasterizer.getCellWidth()+"  "+rasterizer.getCellHeight()+"  "+(heightArray[1]-heightArray[0])/10000+"\n");
        System.out.print("\n");
        System.out.print("POINT_DATA  "+rasterizer.getWidth()*rasterizer.getHeight()*numHeightIntervals+"\n");
        System.out.print("SCALARS "+momentName+"("+momentUnits+") float\n");
        System.out.print("LOOKUP_TABLE default\n");
        System.out.print("\n");
        
        
        DecodeRadialDatasetSweep radialDatasetDecoder = new DecodeRadialDatasetSweep(radialDatasetHeader);
        RadialDatasetSweep.RadialVariable radialVar = (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable(variable);
        radialDatasetDecoder.setRadialVariable(radialVar);
        final int numOfCuts = radialVar.getNumSweeps();

        
        final DecimalFormat fmt2 = new DecimalFormat("0.00");
        radialDatasetDecoder.addDataDecodeListener(new DataDecodeListener(){
            public void metadataUpdate(DataDecodeEvent decodeEvent) {
            }
            public void decodeStarted(DataDecodeEvent evt) {};
            public void decodeProgress(DataDecodeEvent evt) {
                int[] processProgress = new int[]{ getCurrentZIndex(), getCurrentCut(), evt.getProgress() };
                int[] processCompletion = new int[] { numHeightIntervals, numOfCuts, 100 };
                double percent = WCTUtils.progressCalculator(processProgress, processCompletion);
                
                for (int i = 0; i < listeners.size(); i++) {
                    event.setProgress(Double.parseDouble(fmt2.format(percent * 100.0)));
                    ((GeneralProgressListener) listeners.get(i)).progress(event);
                }

//                System.out.println("PROGRESS: "+((int)(percent * 100.0))+
//                        " "+getCurrentZIndex()+" / "+numHeightIntervals+"  :  "+getCurrentCut()+" / "+numOfCuts+
//                        "  decodeProgress: "+evt.getProgress());                

            }
            public void decodeEnded(DataDecodeEvent evt) {};
        });
        
        
        
        
        for (int z=0; z<numHeightIntervals; z++) {


            // initialize raster to NoData value
            rasterizer.clearRaster();

            // Set up Filter
            //NexradFilter nxfilter = new NexradFilter();
            if (nxfilter == null) {
                nxfilter = new WCTFilter();
            }

            // Override the already created filter
            nxfilter.setHeightRange(heightArray[z], heightArray[z+1]+overlap);


            this.currentZIndex = z;

            //--------------------------------------------------------------------
            // Decode the data
            //--------------------------------------------------------------------
                
            System.out.println("NUMBER OF CUTS: "+numOfCuts);
            for (int cut=0; cut<numOfCuts; cut++) {

                this.currentCut = cut;

                // Progress
                // --------------
//                for (int i = 0; i < listeners.size(); i++) {
//                    event.setProgress((int)(((z*numOfCuts)+cut)/((double)(numHeightIntervals*numOfCuts)) * 90.0)); //leave last 10% for bw.write
//                    ((GeneralProgressListener) listeners.get(i)).progress(event);
//                }





                radialDatasetDecoder.setDecodeHint("classify", new Boolean(false));
                radialDatasetDecoder.setDecodeHint("nexradFilter", nxfilter);
                radialDatasetDecoder.setDecodeHint("startSweep", new Integer(cut));
                radialDatasetDecoder.setDecodeHint("endSweep", new Integer(cut));
                
                
                System.out.println("PROCESSING CUT: "+cut);
                radialDatasetDecoder.decodeData(new StreamingProcess[] { rasterizer });


            }

            /*
         //--------------------------------------------------------------------
         // Save the raster
         //--------------------------------------------------------------------
         File outRaster = new File("H:\\Nexrad_Viewer_Test\\Dickson\\klix-strucgrid-"+z+".asc");
         try {
            NexradRasterExport.saveAsciiGrid(outRaster, rasterizer);
         } catch (Exception e) {
            e.printStackTrace();
         }
             */            



            java.awt.Color[] colors = NexradColorFactory.getColors(variable, false);
            double[] maxmin = NexradValueFactory.getProductMaxMinValues(variable, 12, false);
            GridCoverage gc = gcSupport.getGridCoverage(rasterizer, radialDatasetHeader, false, colors, maxmin);
            Raster raster = gc.getRenderedImage().getData();


            for (int j = rasterizer.getHeight()-1; j >= 0; j--) {

                for (int i = 0; i < listeners.size(); i++) {
                    event.setProgress( (int)((90.0*z/numHeightIntervals) + (j/rasterizer.getHeight()) * 10.0) ); //leave last 10% for bw.write
                    ((GeneralProgressListener) listeners.get(i)).progress(event);
                }


                for (int i = 0; i < rasterizer.getWidth(); i++) {
                    double value = raster.getSampleFloat(i, j, 0);

                    if (variable.equals("Reflectivity")) {
                        if (value == rasterizer.getNoDataValue()) {
                            value = -25.0;
                        }
                        else if (value < 20.0) {
                            value = 20.0;
                        }
                    }
                    else if (variable.equals("RadialVelocity")) {
                        if (value == rasterizer.getNoDataValue()) {
                            value = 0.0;
                        }
                        else if (value < -75.0) {
                            value = -75.0;
                        }
                    }
                    else if (variable.equals("SpectrumWidth")) {
                        if (value == rasterizer.getNoDataValue()) {
                            value = -5.0;
                        }
                        else if (value < 0.0) {
                            value = 0.0;
                        }
                    }

                    //               if (value != rasterizer.NO_DATA) System.out.println(value);
                    bw.write(fmt02.format(value)+" ");               
                }
                bw.newLine();
            }

        }

        bw.flush();
        bw.close();




        // End
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            ((GeneralProgressListener) listeners.get(i)).ended(event);
        }



        System.out.println("SAVED VTK: "+outputFile);


    }































    public void processDir(File dir) 
        throws IOException, IllegalAttributeException, StreamingProcessException, 
        DecodeException, ParseException, DecodeHintNotSupportedException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {

        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                if (f.toString().endsWith(".Z")) {
                    return true;
                }
                else {
                    return false;
                }
            }
        };

        File[] files = dir.listFiles(filter);

        for (int n=0; n<files.length; n++) {

            System.out.println("PROCESSING "+files[n]+" TO VTK");

            String filename = files[n].toString();
            String outfilename = filename.substring(0, filename.length()-2) + ".vtk";

            // KVWX Rectangle2D.Double bounds = Rectangle2D.Double(-87.80, 37.75, 0.5, 0.5);
            // KTWX Rectangle2D.Double bounds = Rectangle2D.Double(-94.97, 38.94, 0.5, 0.5);
            java.awt.geom.Rectangle2D.Double bounds = 
                new java.awt.geom.Rectangle2D.Double(-82.6, 26.4, 1.2, 1.2);

            convertToVtkStructuredGrid(files[n], new File(outfilename), bounds);

        }
    }



    public void process() 
        throws IOException, IllegalAttributeException, StreamingProcessException, 
            DecodeException, ParseException, DecodeHintNotSupportedException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {

        File outputFile = new File("H:\\Nexrad_Viewer_Test\\Dickson\\ktbw-strucgrid.vtk");

        File inputFile = new java.io.File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Charley\\6500KTBW20040813_210303.Z");
        //File inputFile = new java.io.File("H:\\Nexrad_Viewer_Test\\Tornado\\KansasCity-200305\\HAS000258134-L2\\HAS000258134-L2\\6500KTWX20030504_212016.Z");
        //java.io.File f = new java.io.File("H:\\Nexrad_Viewer_Test\\Tornado\\Evansville-200511\\data\\6500KVWX20051106_075714.Z");
        //java.io.File f = new java.io.File("H:\\Nexrad_Viewer_Test\\Hurricanes\\Katrina-Gulf\\data\\KLIX20050829_101013.Z");
        //java.io.File f = new java.io.File("H:\\Nexrad_Viewer_Test\\Toronto\\6500\\KBUF20050802_200349.Z");


        // KVWX Rectangle2D.Double bounds = Rectangle2D.Double(-87.80, 37.75, 0.5, 0.5);
        // KTWX Rectangle2D.Double bounds = Rectangle2D.Double(-94.97, 38.94, 0.5, 0.5);
        java.awt.geom.Rectangle2D.Double bounds = 
            new java.awt.geom.Rectangle2D.Double(-82.6, 26.4, 1.2, 1.2);

        convertToVtkStructuredGrid(inputFile, outputFile, bounds);
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
