package steve.test.swath;

import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetUtils;
import gov.noaa.ncdc.wct.ui.WCTNoGridsFoundException;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;

public class NPPReader {

	public void test() throws Exception {
		
		String source = "E:\\work\\NPP\\murzen\\gimgo-svi01.nc";
		
		
		
		
		
		
//		System.out.println("... opening grid dataset");
//        StringBuilder errlog = new StringBuilder();
//        GridDataset gds = GridDatasetUtils.openGridDataset(source, errlog);
//        if (gds.getGrids().size() == 0) {
//        	gds.close();
//            throw new WCTNoGridsFoundException("No Grids found in file.  Check CF-compliance for gridded data");
//        }
//
//        
//		System.out.println("... opening grid dataset complete");
//        System.out.println( gds.getGrids() );
//        
//        
//        GridDatatype grid = gds.findGridDatatype("radiance");
        
//        HorizontalGrid hGrid = CdmUtils.createHorizontalGrid(grid.getCoordinateSystem());
        
        //        KDTree kdTree = new KDTree()
        
        
        
//        System.out.println("... reading data");
//        Array data = grid.readVolumeData(0);
//        System.out.println("... reading data done");
//        System.out.println("... reading lat...");
//        Variable latGrid = gds.getNetcdfFile().findVariable("lat");
//        Array latData = latGrid.read();
//        System.out.println("... reading lon...");
//        Variable lonGrid = gds.getNetcdfFile().findVariable("lon");
//        Array lonData = lonGrid.read();
//        System.out.println("..done..");
//        
//        
//        System.out.println(Arrays.toString(data.getShape()));
//        Index idx = data.getIndex();
//        int[] shape = data.getShape();
//        for (int y=0; y<shape[0]; y++) {
//            for (int x=0; x<shape[1]; x++) {
//            	
//            	double val = data.getDouble(idx.set(y, x));
//            	
//                LatLonPoint llp = grid.getCoordinateSystem().getLatLon(x, y);
//            }        	
//        }
        
		
		
		
		
		System.out.println("opening file...");
		NetcdfFile ncfile = NetcdfFile.open(source);
		
		System.out.println("getting variable...");
		Variable var = ncfile.findVariable("radiance");
		
		System.out.println("reading anchor points...");
		
	}
	
	public static void main(String[] args) {
		NPPReader gr = new NPPReader();
		try {
			gr.test();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
