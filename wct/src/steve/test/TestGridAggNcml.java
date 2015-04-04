package steve.test;

import java.io.StringReader;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.util.CancelTask;

public class TestGridAggNcml {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        try {
            
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
            
            
            // 1.  GridDataset from an NCML file (works)
            
            
            
            
            
            // 2.  GridDataset form a NetcdfDataset created from an NCML String (doesn't work? - no works!)
            
            String ncml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"+
                          "<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\">  \n"+
                          "  <aggregation dimName=\"time\" type=\"joinExisting\" timeUnitsChange=\"true\" > \n"+
                          "    <variableAgg name=\"Total_precipitation\" /> \n"+
                          "    <scan location=\"E:/work/stage-iv/testdata\" suffix=\".24h\" /> \n"+
                          "  </aggregation> \n"+
                          "</netcdf> \n";
            
            NetcdfDataset ncd = NcMLReader.readNcML(new StringReader(ncml), cancelTask);
            
            ucar.nc2.dt.GridDataset gds = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, ncd, cancelTask, new StringBuilder());
            System.out.println("\nCF Grids found in 'fixed' dataset': \n"+gds.getGrids());
            System.out.println("\nCDL: \n"+gds.getGrids().get(0).getVariable());
            System.out.println("\nStart Time: "+gds.getGrids().get(0).getCoordinateSystem().getDateRange().getStart().toDateTimeStringISO());
            System.out.println("\n  End Time: "+gds.getGrids().get(0).getCoordinateSystem().getDateRange().getEnd().toDateTimeStringISO());

            
//            NcMLReader.writeNcMLToFile(new ByteArrayInputStream(ncml.getBytes()), "E:\\work\\stage-iv\\testdata\\agg-out1.nc");
//            FileWriter.writeToFile(ncd, "E:\\work\\stage-iv\\testdata\\agg-out2.nc").close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
