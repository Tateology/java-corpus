package steve.test.nhc;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import ucar.nc2.Attribute;
import ucar.nc2.NCdumpW;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.nc2.ncml.NcMLReader;
import ucar.nc2.util.CancelTask;

public class ConvertToCFNetCDF {

    public static void main(String[] args) {
        
//        String source = "E:\\work\\nhc-2dimgrids\\NH2_experimental_grids_latest.nc";
        
        if (args.length != 2) {
            System.err.println("ARG 1: Input NH2 grid NetCDF file with full path.");
            System.err.println("ARG 2: Title used in CF convention metadata (i.e. \"NOAA/NHC Model Output\")");
            System.err.println("NOTES: Use double quotes ( \" ) to enclose arguments with spaces.");
            System.exit(0);
        }
        
        try {
            
            process(args[0], args[1]);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static void process(String source, String title) throws Exception {
        
    
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
				// TODO Auto-generated method stub
				
			}
        };
        PrintWriter outWriter = new PrintWriter(System.out);
        
        
        NetcdfDataset ncd = NetcdfDataset.acquireDataset(source, cancelTask);
        outWriter.println("\n\n'Original' CDL: \n");
        NCdumpW.print(ncd, "-h", outWriter, cancelTask);
        outWriter.flush();
        
        
        List<String> validTimesVarNameList = new ArrayList<String>();
        List<Attribute> validTimesAttributeList = new ArrayList<Attribute>();
        
        List<Variable> varList = ncd.getVariables();
        for (Variable var : varList) {
            outWriter.println(var.getName());
            
            Attribute att = var.findAttribute("validTimes");
            if (att != null) {
                validTimesVarNameList.add(var.getName());
                validTimesAttributeList.add(att);
            }
        }

        if (validTimesAttributeList.size() == 0) {
            throw new Exception("validTimes attribute could not be found on any variable - unexpected and a problem!");
        }
        
//        for (int n=0; n<validTimesAttributeList.size(); n++) {
//            if (! validTimesAttributeList.get(0).toString().equals(validTimesAttributeList.get(n).toString())) {
//                outWriter.println(validTimesAttributeList.get(0));
//                outWriter.println(validTimesAttributeList.get(n));
//                System.err.println("validTimes attributes differ between variables - unexpected and a problem!");
//                System.err.println("Grid 0: "+validTimesVarNameList.get(0)+"  : "+validTimesAttributeList.get(0));
//                System.err.println("Grid n="+n+": "+validTimesVarNameList.get(n)+"  : "+validTimesAttributeList.get(n));
//                
//                throw new Exception("validTimes attributes differ between variables - unexpected and a problem!");
//            }
//        }
        
//        String[] validTimeStrings = validTimesAttributeList.get(0).getStringValue().split(",");

        
        // determine how many time dimensions we need and what their sizes are; also populate with values
//        ArrayList<String> timeDimList = new ArrayList<String>();
        for (int n=0; n<validTimesAttributeList.size(); n++) {
            validTimesAttributeList.get(0).getLength();
        }
        
        
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
        sb.append("<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\"  location=\""+source+"\">");
        
        
        // divide length by two because attribute values are 'startTime endTime startTime endTime ...' 
        // and we only need startTime.
        for (int n=0; n<validTimesAttributeList.size(); n++) {
            validTimesAttributeList.get(n).getLength();
            sb.append("<dimension name=\"time_"+validTimesVarNameList.get(n)+"\" length=\""+validTimesAttributeList.get(n).getLength()/2+"\" > \n");
            sb.append("</dimension> \n");
        }
//        sb.append("<dimension name=\"time\" orgName=\"DIM_25\"> \n");
//        sb.append("</dimension> \n");
        sb.append("<dimension name=\"y\" orgName=\"DIM_284\"> \n");
        sb.append("</dimension> \n");
        sb.append("<dimension name=\"x\" orgName=\"DIM_596\"> \n");
        sb.append("</dimension> \n");
        
        
        for (int n=0; n<validTimesVarNameList.size(); n++) {
            sb.append("<variable name=\"time_"+validTimesVarNameList.get(n)+"\" shape=\"time_"+validTimesVarNameList.get(n)+"\" type=\"int\"> \n");
            sb.append("     <attribute name=\"units\" type=\"String\" value=\"seconds since 1970-01-01T00:00:00Z\" /> \n");
            sb.append("     <attribute name=\"long_name\" type=\"String\" value=\"forecast time\" /> \n");
            sb.append("     <values>" );
            for (int i=0; i<validTimesAttributeList.get(n).getLength(); i++) {
                sb.append(validTimesAttributeList.get(n).getNumericValue(i)+" ");
                i++; // skip the end time
                // divide length by two because attribute values are 'startTime endTime startTime endTime ...' 
                // and we only need startTime.
            }
            sb.append("     </values> \n");
            sb.append("</variable> \n");
        }

        sb.append("<variable name=\"latitude\"> \n");
        sb.append("     <attribute name=\"standard_name\" type=\"String\" value=\"latitude\" /> \n");
        sb.append("</variable> \n");
        sb.append("<variable name=\"longitude\"> \n");
        sb.append("     <attribute name=\"standard_name\" type=\"String\" value=\"longitude\" /> \n");
        sb.append("</variable> \n");
        
        sb.append("<variable name=\"Topo\"> \n");
        sb.append("     <attribute name=\"coordinates\" type=\"String\" value=\"latitude longitude\" /> \n");
        sb.append("</variable> \n");
        for (int n=0; n<validTimesVarNameList.size(); n++) {
            sb.append("<variable name=\""+validTimesVarNameList.get(n)+"\" shape=\"time_"+validTimesVarNameList.get(n)+" y x\"> \n");
            sb.append("     <attribute name=\"coordinates\" type=\"String\" value=\"latitude longitude\" /> \n");
            sb.append("</variable> \n");
        }
        
        

        
        sb.append("<attribute name=\"title\" type=\"String\" value=\"NOAA/NHC Model Output\" /> \n");
        sb.append("<attribute name=\"Conventions\" value=\"CF-1.0\" /> \n");
        
        sb.append("</netcdf> \n");
        
        ncd.close();
        

        outWriter.println("\n\nNcML markup used to 'fix' the dataset:\n"+sb.toString());
        outWriter.flush();

        NetcdfDataset ncdFixed = NcMLReader.readNcML(new StringReader(sb.toString()), cancelTask);
        outWriter.println("\n\n'Fixed' CDL: \n");
        NCdumpW.print(ncdFixed, "-h", outWriter, cancelTask);
        outWriter.flush();
        

        
        StringBuilder errlog = new StringBuilder();
        ucar.nc2.dt.GridDataset gds = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, source, cancelTask, errlog);
        outWriter.println("\n\nCF Grids found in 'original' dataset': \n"+gds.getGrids());
        gds.close();

        ucar.nc2.dt.GridDataset gdsFixed = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, ncdFixed, cancelTask, errlog);
        outWriter.println("\nCF Grids found in 'fixed' dataset': \n"+gdsFixed.getGrids());
        gdsFixed.close();
        
        outWriter.flush();
        
//        FileWriter.writeToFile(ncdFixed, source+"_fixed.nc", true);
        NcMLReader.writeNcMLToFile(new ByteArrayInputStream(sb.toString().getBytes()), source+"_cf2.nc");
        

        outWriter.close();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
