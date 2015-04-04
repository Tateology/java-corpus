package gov.noaa.ncdc.wct.decoders.cdm.conv;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.dataset.CoordSysBuilder;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.util.CancelTask;

public class TAFBMarineForecastConvention extends CoordSysBuilder {

    /**
     * Is this my file?
     * http://www.nhc.noaa.gov/tafb/gridded_marine/index.php
     *
     * @param ncfile test this NetcdfFile
     * @return true if we think this is a TAFB Marine Forecast file.
     */
    public static boolean isMine(NetcdfFile ncfile) {
        
        System.out.println("in 'isMine' for "+ncfile.getLocation());
        
        String cs = ncfile.findAttValueIgnoreCase(null, "Conventions", null);
        if (cs != null) return false;

        List<Variable> varList = ncfile.getVariables();
        ArrayList<String> varNameList = new ArrayList<String>(varList.size());
        for (Variable var : varList) {
//            System.out.println(var.getName());
            varNameList.add(var.getName());
        }
        
//        System.out.println(ncfile.findVariable("pmsl_SFC"));

        // findVariable is returning null and I don't know why.
        // use name list instead
//        if ( ncfile.findVariable("psml_SFC") == null ||
//                ncfile.findVariable("Wind_Mag_SFC") == null ||  
//                ncfile.findVariable("Wind_Dir_SFC") == null ||
//                ncfile.findVariable("WaveHeight_SFC") == null ||
//                ncfile.findVariable("Topo") == null ) {
//            return false;
//        }
        
//        System.out.println(varNameList);
//        System.out.println(varNameList.contains("pmsl_SFC"));

        boolean hasTimeDimensions = false;
        for (Dimension dim : ncfile.getDimensions()) {
            if (dim.getName().startsWith("time")) {
                hasTimeDimensions = true;
            }
        }

        
        if (varNameList.contains("pmsl_SFC") && 
            varNameList.contains("Wind_Mag_SFC") &&
            varNameList.contains("Wind_Dir_SFC") &&
            varNameList.contains("WaveHeight_SFC") &&
            varNameList.contains("Topo") && ! hasTimeDimensions) {
        
            return true;
        }

//        System.out.println("isMine? returning false...");
        return false;
    }

    @Override
    public void augmentDataset(NetcdfDataset ncDataset, CancelTask cancelTask) throws IOException {

        System.out.println("Using CoordSysBuilder: "+this.getClass());
        
        List<String> validTimesVarNameList = new ArrayList<String>();
        List<Attribute> validTimesAttributeList = new ArrayList<Attribute>();
        
        List<Variable> varList = ncDataset.getVariables();
        for (Variable var : varList) {
//            System.out.println(var.getName());
            
            Attribute att = var.findAttribute("validTimes");
            if (att != null) {
                validTimesVarNameList.add(var.getName());
                validTimesAttributeList.add(att);
            }
        }

        if (validTimesAttributeList.size() == 0) {
            throw new IOException("validTimes attribute could not be found on any variable - unexpected and a problem!");
        }

        
        // 1. rename x/y dimensions
        for (Dimension dim : ncDataset.getDimensions()) {
            if (dim.getName().equals("DIM_284")) {
                dim.setName("y");
            }
            else if (dim.getName().equals("DIM_596")) {
                dim.setName("x");
            }
        }

        // 2. add time dimension
        for (int n=0; n<validTimesAttributeList.size(); n++) {
            validTimesAttributeList.get(n).getLength();
//            sb.append("<dimension name=\"time_"+validTimesVarNameList.get(n)+"\" length=\""+validTimesAttributeList.get(n).getLength()/2+"\" > \n");
//            sb.append("</dimension> \n");
            ncDataset.addDimension(null, new Dimension("time_"+validTimesVarNameList.get(n), validTimesAttributeList.get(n).getLength()/2));
        }

        // 3. add time coordinate variable
        for (int n=0; n<validTimesVarNameList.size(); n++) {

            ArrayList<String> valueList = new ArrayList<String>();
            for (int i=0; i<validTimesAttributeList.get(n).getLength(); i++) {
                valueList.add(validTimesAttributeList.get(n).getNumericValue(i)+"");
                i++; // skip the end time
                // divide length by two because attribute values are 'startTime endTime startTime endTime ...' 
                // and we only need startTime.
            }

            
            CoordinateAxis v = new CoordinateAxis1D(ncDataset, null, "time_"+validTimesVarNameList.get(n), DataType.INT, 
                    "time_"+validTimesVarNameList.get(n), "seconds since 1970-01-01T00:00:00Z", "time coordinate");
            ncDataset.setValues(v, valueList);
            v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
            v.addAttribute(new Attribute("units", "seconds since 1970-01-01T00:00:00Z"));
            v.addAttribute(new Attribute("long_name", "forecast time"));
            v.addAttribute(new Attribute("standard_name", "time"));
            
            ncDataset.addCoordinateAxis(v);
        }   

        
        
        // 4. add standard name to lat/lon coord variables
        {
            Variable v = ncDataset.findVariable("latitude");
            v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lat.name()));
            v.addAttribute(new Attribute("standard_name", "latitude"));
        }
        {
            Variable v = ncDataset.findVariable("longitude");
            v.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lon.name()));
            v.addAttribute(new Attribute("standard_name", "longitude"));
        }
        {
            Variable v = ncDataset.findVariable("Topo");
            v.addAttribute(new Attribute("coordinates", "latitude longitude"));
        }
        {
            for (int n=0; n<validTimesVarNameList.size(); n++) {
                Variable v = ncDataset.findVariable(validTimesVarNameList.get(n));
                v.setDimensions("time_"+validTimesVarNameList.get(n)+" y x");
                v.addAttribute(new Attribute("coordinates", "latitude longitude"));
                v.addAttribute(new Attribute(_Coordinate.Axes, "Time Lat Lon"));
            }
        }

//        :title = "NOAA/NHC Model Output";
//        :Conventions = "CF-1.0";
        
        ncDataset.addAttribute(null, new Attribute("title", "NOAA/NHC Marine Forecast"));
        ncDataset.addAttribute(null, new Attribute("Conventions", "CF-1.0"));
        
        ncDataset.finish();
        
        
//      PrintWriter outWriter = new PrintWriter(System.out);
//      NCdumpW.print(ncDataset, "-h", outWriter, cancelTask);
//      outWriter.close();
        
    }
    
    
    
    
    
    
    
//    @Override
    public void augmentDatasetOLD(NetcdfDataset ncDataset, CancelTask cancelTask) throws IOException {
//        super.augmentDataset(ncDataset, cancelTask);
        
        List<String> validTimesVarNameList = new ArrayList<String>();
        List<Attribute> validTimesAttributeList = new ArrayList<Attribute>();
        
        List<Variable> varList = ncDataset.getVariables();
        for (Variable var : varList) {
//            System.out.println(var.getName());
            
            Attribute att = var.findAttribute("validTimes");
            if (att != null) {
                validTimesVarNameList.add(var.getName());
                validTimesAttributeList.add(att);
            }
        }

        if (validTimesAttributeList.size() == 0) {
            throw new IOException("validTimes attribute could not be found on any variable - unexpected and a problem!");
        }
        
//        for (int n=0; n<validTimesAttributeList.size(); n++) {
//            if (! validTimesAttributeList.get(0).toString().equals(validTimesAttributeList.get(n).toString())) {
//                System.out.println(validTimesAttributeList.get(0));
//                System.out.println(validTimesAttributeList.get(n));
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
//        for (int n=0; n<validTimesAttributeList.size(); n++) {
//            validTimesAttributeList.get(0).getLength();
//        }
        
        
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
        sb.append("<netcdf xmlns=\"http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2\"  location=\""+ncDataset.getLocation()+" \">");
        
        
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
            sb.append("     <attribute name=\"standard_name\" type=\"String\" value=\"time\" /> \n");
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
        

//        System.out.println("\n\nNcML markup used to 'fix' the dataset:\n"+sb.toString());

        WCTNcmlReader.wrapNcML(ncDataset, new StringReader(sb.toString()), cancelTask);
        

        
//        System.out.println("\n\n'Fixed' CDL: \n");
//        PrintWriter outWriter = new PrintWriter(System.out);
//        NCdumpW.print(ncdFixed, "-h", outWriter, cancelTask);
//        outWriter.close();
        
//        ncDataset.close();
        
    }
}
