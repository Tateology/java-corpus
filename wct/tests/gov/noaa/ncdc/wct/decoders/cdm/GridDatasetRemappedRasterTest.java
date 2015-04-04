package gov.noaa.ncdc.wct.decoders.cdm;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import junit.framework.TestCase;

import org.geotools.gc.GridCoverage;
import org.geotools.gui.swing.WCTMapPane;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.junit.Test;

import ucar.nc2.Attribute;
import ucar.nc2.NCdumpW;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.util.CancelTask;


public class GridDatasetRemappedRasterTest extends TestCase {

//    @Test
//    public void testCdmOpen() throws Exception {
//        
//        DebugFlagsImpl debug = new DebugFlagsImpl("NetcdfFile/debugSPI");
//        NetcdfFile.setDebugFlags(debug);
//        NetcdfFile.registerIOProvider(ucar.nc2.iosp.grib.Grib1ServiceProvider.class);
//        NetcdfFile.registerIOProvider(ucar.nc2.iosp.grib.Grib2ServiceProvider.class);
//        
//        String source = "E:\\work\\model\\narr-a_221_20091010_0000_000.grb";
//        
//        CancelTask cancelTask = new CancelTask() {
//            public boolean isCancel() {
//                return false;
//            }
//            public void setError(String msg) {
//            }
//        };
//
//        StringBuilder errlog = new StringBuilder();
//        ucar.nc2.dt.GridDataset gds = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, source, cancelTask, errlog);
//
//    }
//    
//    
//    @Test
//    public void testStage4() throws Exception {
//        
//        GridDatasetRemappedRaster g = new GridDatasetRemappedRaster();
//        String source = "E:\\work\\stage-iv-gis\\ST4_Agg_2006-TEST.nc";
//        g.process(source);
//        
//        GridCoverage gc = g.getGridCoverage();
//        showMap(gc);
//    }
    
    
    
    
//    @Test
//    public void testStage4Histo() throws Exception {
//
//        GridDatasetRemappedRaster g = new GridDatasetRemappedRaster();
//        String source = "E:\\work\\stage-iv-gis\\ST4_Agg_2006-TEST.nc";
//        g.calculateStatistics(source);
//    }

    
//    @Test
//    public void testCurvilinearGrid() throws Exception {
//        
//        String source = "E:\\work\\nhc-2dimgrids\\NH2_experimental_grids_latest.nc";
//
//        CancelTask cancelTask = new CancelTask() {
//            public boolean isCancel() {
//                return false;
//            }
//            public void setError(String msg) {
//            }
//        };
//
//        StringBuilder errlog = new StringBuilder();
//        ucar.nc2.dt.GridDataset gds = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, source, cancelTask, errlog);
//
//        System.out.println(errlog.toString());
//        System.out.println(gds.getGrids());
//        
//        GridDatatype gd = gds.getGrids().get(0);
//        System.out.println("generating 2-d coord system and cached lookup table");
//        TwoDCoordSys coordSys = TwoDCoordSys.generate(gd.getCoordinateSystem());
//        
////        LonLatPositionImpl llPoint = new LonLatPositionImpl(0, 0);
//        for (int i=0; i<100; i++) {
//            for (int j=0; j<100; j++) {
//
////                llPoint.setOrdinate(0, -90+(i/10.0));
////                llPoint.setOrdinate(1, 20+(j/10.0));
//                LonLatPositionImpl llPoint = new LonLatPositionImpl(-90+(i/10.0), 20+(j/10.0));
//                int[] indices = coordSys.lonLatToGrid(llPoint);
//
//                System.out.println(llPoint+" = "+indices[0]+","+indices[1]);
//            }
//        }
//        
//        
//        gds.close();
//    }
    
    
    
    @Test
    public void testReadNonCFGrids() throws Exception {
        
        String source = "E:\\work\\nhc-2dimgrids\\NH2_experimental_grids_latest.nc";

        CancelTask cancelTask = new CancelTask() {
			@Override
            public boolean isCancel() {
                return false;
            }
			@Override
            public void setError(String msg) {
            }
			@Override
			public void setProgress(String arg0, int arg1) {				
			}
        };
        
        NetcdfDataset ncd = NetcdfDataset.acquireDataset(source, cancelTask);
        NCdumpW.print(ncd, "-h", new PrintWriter(System.out), cancelTask);
        
        
        List<Attribute> validTimesAttributeList = new ArrayList<Attribute>();
        
        List<Variable> varList = ncd.getVariables();
        for (Variable var : varList) {
            System.out.println(var.getShortName());
            
            Attribute att = var.findAttribute("validTimes");
            if (att != null) {
                validTimesAttributeList.add(att);
            }
        }

        if (validTimesAttributeList.size() == 0) {
            throw new Exception("validTimes attribute could not be found on any variable - unexpected and a problem!");
        }
        
        for (int n=0; n<validTimesAttributeList.size(); n++) {
            if (! validTimesAttributeList.get(0).toString().equals(validTimesAttributeList.get(n).toString())) {
                System.out.println(validTimesAttributeList.get(0));
                System.out.println(validTimesAttributeList.get(n));
                throw new Exception("validTimes attributes differ between variables - unexpected and a problem!");
            }
        }
        
//        String[] validTimeStrings = validTimesAttributeList.get(0).getStringValue().split(",");

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
        sb.append("<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\"  location=\""+source+"\">");
        
        
        sb.append("<dimension name=\"time\" orgName=\"DIM_25\"> \n");
        sb.append("</dimension> \n");
        sb.append("<dimension name=\"y\" orgName=\"DIM_284\"> \n");
        sb.append("</dimension> \n");
        sb.append("<dimension name=\"x\" orgName=\"DIM_596\"> \n");
        sb.append("</dimension> \n");
        
        
        sb.append("<variable name=\"time\" shape=\"time\" type=\"int\"> \n");
        sb.append("     <attribute name=\"units\" type=\"String\" value=\"seconds since 1970-01-01T00:00:00Z\" /> \n");
        sb.append("     <attribute name=\"long_name\" type=\"String\" value=\"forecast time\" /> \n");
        sb.append("     <values>" );
        for (int n=0; n<validTimesAttributeList.get(0).getLength(); n++) {
            sb.append(validTimesAttributeList.get(0).getNumericValue(n)+" ");
            n++;
        }
        sb.append("     </values> \n");
        sb.append("</variable> \n");

        sb.append("<variable name=\"latitude\"> \n");
        sb.append("     <attribute name=\"standard_name\" type=\"String\" value=\"latitude\" /> \n");
        sb.append("</variable> \n");
        sb.append("<variable name=\"longitude\"> \n");
        sb.append("     <attribute name=\"standard_name\" type=\"String\" value=\"longitude\" /> \n");
        sb.append("</variable> \n");
        
        sb.append("<variable name=\"Topo\"> \n");
        sb.append("     <attribute name=\"coordinates\" type=\"String\" value=\"latitude longitude\" /> \n");
        sb.append("</variable> \n");
        sb.append("<variable name=\"WaveHeight_SFC\"> \n");
        sb.append("     <attribute name=\"coordinates\" type=\"String\" value=\"time latitude longitude\" /> \n");
        sb.append("</variable> \n");
        
        

        
        sb.append("<attribute name=\"title\" type=\"String\" value=\"NOAA/NHC Model Output\" /> \n");
        sb.append("<attribute name=\"Conventions\" value=\"CF-1.0\" /> \n");
        
        sb.append("</netcdf> \n");
        
        ncd.close();
        
        
        NetcdfDataset ncdFixed = NcMLReader.readNcML(new StringReader(sb.toString()), cancelTask);
        NCdumpW.print(ncdFixed, "-h", new PrintWriter(System.out), cancelTask);
        
        System.out.println("\n\n\n"+sb.toString()+"\n\n");
        
        StringBuilder errlog = new StringBuilder();
        ucar.nc2.dt.GridDataset gds = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, ncdFixed, cancelTask, errlog);
        System.out.println(gds.getGrids());
        gds.close();
        
        
//        FileWriter.writeToFile(ncdFixed, source+"_fixed.nc", true);
        NcMLReader.writeNcMLToFile(new ByteArrayInputStream(sb.toString().getBytes()), source+"_cf2.nc");
        
        
    }
    
    private static void showMap(GridCoverage gc) {
        WCTMapPane mapPane = new WCTMapPane();
        RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
        rgc.setZOrder(1);
        rgc.setVisible(true);
        mapPane.getRenderer().addLayer(rgc);
        JFrame frame = new JFrame("Grid Coverage Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mapPane);
        frame.pack();
        frame.setVisible(true);
        try {
            Thread.sleep(1000*50);
        } catch (Exception e) {            
        }
    }
}
