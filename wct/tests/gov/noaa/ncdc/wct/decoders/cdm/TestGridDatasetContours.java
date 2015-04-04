package gov.noaa.ncdc.wct.decoders.cdm;
//package gov.noaa.ncdc.ndit.decoders.cdm;
//
//import org.junit.Test;
//
//public class TestGridDatasetContours {
//
//
//    @Test
//    public void testContour() throws Exception {
//        GridDatasetContours ce = new GridDatasetContours();
//        String inFile = "H:\\NetCDF\\Stage4\\ST4.2006040112.24h";
//        String outDir = "H:\\NetCDF\\Stage4\\";
//        ce.process(inFile, outDir);
//    }
//    
//    
//    
//    
//    
//    
//    
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//        try {
//            GridDatasetContours ce = new GridDatasetContours();
//            
//            if (args.length == 0 || (args.length != 2 && ! args[0].equalsIgnoreCase("test"))) {
//                System.err.println("Input arguments of \n  1: Input Stage-IV GRIB file \n  2: Output dir");
//            }
//            else if (args[0].equalsIgnoreCase("test")) {
//                String inFile = "H:\\NetCDF\\Stage4\\ST4.2006040112.24h";
//                String outDir = "H:\\NetCDF\\Stage4\\";
//                ce.process(inFile, outDir);
//            }
//            else {
//                ce.process(args[0], args[1]);
//            }
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
