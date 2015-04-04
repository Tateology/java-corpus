package steve.test.swath;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordSysBuilder;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.util.CancelTask;


public class NPPConvention extends CoordSysBuilder {


	private List<Attribute> globalAttList = null;
	private Vector<Dimension> vGlobalDims = new Vector<Dimension>();
	private Vector<String> vVarNames = new Vector<String>();
	
	private boolean bReadShortAsInt = true;
	
	
//	@Override
    public static boolean isMine(NetcdfFile ncfile) {
//    :Distributor = "arch";
//    :Mission_Name = "NPP";
//    :N_Dataset_Source = "noaa";
//    :N_HDF_Creation_Date = "20120503";
//    :N_HDF_Creation_Time = "170936.435343Z";
//    :Platform_Short_Name = "NPP";
    	
    	System.out.println("in isMine in NPPConvention");
    	
    	try {
    		if (ncfile.getRootGroup().findAttribute("Platform_Short_Name").getStringValue().equals("NPP")) {
    			return true;
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	} 
    	return false;
    }
	
	
	@Override
	public void augmentDataset(NetcdfDataset ncDataset, CancelTask cancelTask) throws IOException {
		
		try {
			List<Variable> varList = loadHd5(ncDataset);
			buildNetCdf(ncDataset, new Vector<NppProfile>(), varList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/*****************************************************************************************************************
	 * @throws Exception
	 * *************************************************************************************************************/
	private List<Variable> loadHd5(NetcdfDataset ncH5) throws Exception
	{
		int iGeoX = -9, iGeoY = -9;

//		ncH5 = NetcdfFile.open(args[0]);
//		System.out.println(args[0] + " Open for read");

		List<Variable> varList = ncH5.getVariables();
		globalAttList = ncH5.getGlobalAttributes();
		
		
		

		// Drop NPP Data_Products
		for(int v = 0; v < varList.size(); v++)
		{
			String groupName = varList.get(v).getParentGroup().getName();
			int iLoc = groupName.indexOf("/");
			if(iLoc > 0)
				if(groupName.substring(0, iLoc).equalsIgnoreCase("Data_Products"))
					varList.remove(v--);
		}

		// Work Lat & Lon vars
		for(int v = 0; v < varList.size(); v++)
			if(varList.get(v).getShortName().equalsIgnoreCase("Latitude"))
			{
				List<Dimension> varDimList = varList.get(v).getDimensions(); // Variable dimensions
				if(varDimList.size() != 2) // Check for expected dims
					throw new Exception("Latitude variable dimensions is expected to be 2. It is " + varDimList.size() + "\n");

				varList.get(v).setName("lat"); // Rename variable
				varList.get(v).getDimension(0).setName("GeoY"); // Set dim name
				iGeoY = varList.get(v).getDimension(0).getLength(); // Save for comaprisons
				varList.get(v).getDimension(1).setName("GeoX"); // Set dim name
				iGeoX = varList.get(v).getDimension(1).getLength(); // Save for comaprisons
				vGlobalDims.add(varList.get(v).getDimension(0)); // Add to global dim vector
				vGlobalDims.add(varList.get(v).getDimension(1)); // Add to global dim vector
			}
			else
				if(varList.get(v).getShortName().equalsIgnoreCase("Longitude"))
				{
					varList.get(v).setName("lon"); // Rename variable
					varList.get(v).getDimension(0).setName("GeoY"); // Set dim name
					varList.get(v).getDimension(1).setName("GeoX"); // Set dim name
				}

		// Work all other vars
		for(int v = 0; v < varList.size(); v++)
		{
			List<Dimension> varDimList = varList.get(v).getDimensions(); // Variable dimensions

			for(int x = 0; x < varDimList.size(); x++)
			{
				if(varDimList.get(x).getLength() == iGeoX) // Is this a Geo variable?
					varList.get(v).getDimension(x).setName("GeoX"); // Set dim name
				else
					if(varDimList.get(x).getLength() == iGeoY) // Is this a Geo variable?
						varList.get(v).getDimension(x).setName("GeoY"); // Set Dim name
					else
					{
						varList.get(v).getDimension(x).setName("dim_" + varDimList.get(x).getLength()); // Set generic dim name
						vGlobalDims.add(varList.get(v).getDimension(x)); // Add to global dim vector
					}
			}
		}

		// Can not get GROUPS to work - so check for var name dups and rename if found
		for(int x = 0; x < varList.size() - 1; x++)
			for(int y = x + 1; y < varList.size(); y++)
				if(varList.get(x).getShortName().trim().equalsIgnoreCase(varList.get(y).getShortName().trim()))
					varList.get(x).setName(varList.get(y).getShortName().trim() + "_2");

		// Remove dup dims from gloabal Vector
		for(int x = 0; x < vGlobalDims.size() - 1; x++)
			for(int y = x + 1; y < vGlobalDims.size(); y++)
				if(vGlobalDims.elementAt(x).getLength() == vGlobalDims.elementAt(y).getLength())
				{
					vGlobalDims.remove(y);
					y--;
				}
		
    // Find & read VarNameFactor variable - Check all are the same then Load scale_factor & add_offset
    for(int v = 0; v < varList.size(); v++)
    {
      String name = varList.get(v).getName();
      Variable varib = ncH5.findVariable(name + "Factors");

      if(varib != null)
      {
        Array A = varib.read();
       
        for(int a = 0; a < A.getSize() - 2; a++)
          if(A.getDouble(a) != A.getDouble(a + 2))
            throw new Exception("\n\nThe variable " + name + "Factors has different scaling values");
        
        Attribute Atrib = new Attribute("scale_factor", varib.read().getDouble(0));
        varList.get(v).addAttribute(Atrib);
        Atrib = new Attribute("add_offset", varib.read().getDouble(1));
        varList.get(v).addAttribute(Atrib);
      }
    }
    
    return varList;
	}
	
	
	
	/******************************************************************************************************************
	 * @throws InvalidRangeException
	 * ***************************************************************************************************************/
	private void buildNetCdf(NetcdfDataset ncFile,
			Vector<NppProfile> vProfiles,
			List<Variable> varList) throws IOException, InvalidRangeException
	{
//		NetcdfFileWriteable ncFile = NetcdfFileWriteable.createNew(args[1]); // NC file path
//		System.out.println(args[1] + " Open for Write");
		ncFile.empty();
		
		
//		for (Variable v : ncFile.getVariables()) {
//			ncFile.removeVariable(v.getParentGroup(), v.getName());
//		}
//		for (Dimension d : ncFile.getDimensions()) {
//			ncFile.removeDimension(d.getGroup(), d.getName());
//		}
//		for (Attribute a : ncFile.getGlobalAttributes()) {
//			
//		}
		
		
		
		
		

		// Hard coded global attributes
		ncFile.addAttribute(null, new Attribute("title", "NPP Converted from HDF5 to CF compliant NetCDF"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		ncFile.addAttribute(null, new Attribute("creation_date", sdf.format(new Date())));
		ncFile.addAttribute(null, new Attribute("Conventions", "CF-1.4"));
		ncFile.addAttribute(null, new Attribute("CF Note", 
				"units values are a direct translation from HDF5 file and may not be CF compliant"));

		// HDF5 file global attributes
		for(int i = 0; i < globalAttList.size(); i++)
		{
			DataType dt = globalAttList.get(i).getDataType();
			if(dt.isString())
				ncFile.addAttribute(null, 
						new Attribute(globalAttList.get(i).getName(), globalAttList.get(i).getStringValue()));
			else
				if(dt.isNumeric())
					ncFile.addAttribute(null, 
							new Attribute(globalAttList.get(i).getName(), globalAttList.get(i).getNumericValue()));
				else
					System.out.println("Invalid Global Attribute type: " + globalAttList.get(i).getName() + "   " + dt.name());
		}

		// Profile.xml global attributes
		for(int p = 0; p < vProfiles.size(); p++)
			for(int i = 0; i < vProfiles.elementAt(p).vGlobAtts.size(); i++)
				ncFile.addAttribute(null, new Attribute(
						vProfiles.elementAt(p).vGlobAtts.elementAt(i)[0][0],
						vProfiles.elementAt(p).vGlobAtts.elementAt(i)[0][1]));

		// Add dims
		for(int i = 0; i < vGlobalDims.size(); i++)
			ncFile.addDimension(null, 
					new Dimension(vGlobalDims.elementAt(i).getName(), vGlobalDims.elementAt(i).getLength()));

		// Add variables to NetCdf
		for(int v = 0; v < varList.size(); v++)
		{
			Variable var = varList.get(v); // Get single variable

			boolean bSpecDataSet = false; // Only process requested DataSets

			for(int x = 0; x < vVarNames.size(); x++)	// vVarNames is from args[] - User IP
				if(vVarNames.elementAt(x).equalsIgnoreCase(var.getShortName().toLowerCase().trim()))
					bSpecDataSet = true;

			if(vVarNames.isEmpty()) // Default is gettem all.
				bSpecDataSet = true;

			if(bSpecDataSet)
			{
				System.out.println("Loading Variable " + var.getShortName() + "  " + var.getDataType().toString());

				List<Dimension> varDimList = var.getDimensions(); // Variable Dimensions

				// Passing varDimList did not work ?? - Passing the Dim names works
				String dims = "";
				for(int i = 0; i < varDimList.size(); i++) {
					for(int x = 0; x < vGlobalDims.size(); x++)
						if(varDimList.get(i).getLength() == vGlobalDims.elementAt(x).getLength())
						{
							dims = dims + vGlobalDims.elementAt(x).getName().trim() + " ";
							break;
						}
				}
				
				// ANSARI ADDITION
				boolean repeatVariable = false;
				for(int vv = 0; vv < v; vv++) {
					// variable already added?
					if (var.getShortName().equals(varList.get(vv).getShortName())) {
						repeatVariable = true;
					}
				}				
				if (repeatVariable) {
					continue;
				}
				// END ANSARI ADDITION
				

				ucar.ma2.DataType datatype = var.getDataType();
				// Read short variables as int
				if(var.getDataType().toString().equalsIgnoreCase("short")	&& bReadShortAsInt) { 
//					ncFile.addVariable(null, var.getShortName().toLowerCase().trim(),	ucar.ma2.DataType.INT, dims.trim());
					datatype = ucar.ma2.DataType.INT;
				}
				else {
					// Need to change long variables to double
					if(var.getDataType().toString().equalsIgnoreCase("long")) { 
//						ncFile.addVariable(null, var.getShortName().toLowerCase().trim(),	ucar.ma2.DataType.DOUBLE, dims.trim());
						datatype = ucar.ma2.DataType.DOUBLE;
					}
					else {
//						ncFile.addVariable(null, var.getShortName().toLowerCase().trim(),	var.getDataType(), dims.trim());
					}
				}

				ncFile.addVariable(null, new VariableDS(null, 
						new Variable(ncFile, null, null, var.getShortName().toLowerCase().trim(),	datatype, dims.trim()), 
						true));
				
//				var.s
				
				addVarAttributes(ncFile, var);

				for(int p = 0; p < vProfiles.size(); p++) // Add variable attributes from profile.xml
					addProfileAtts(ncFile, var, vProfiles.elementAt(p));
			}
			else
				System.out.println("Skipping Variable " + var.getShortName() + "  "	+ var.getDataType().toString());
		}

		ncFile.finish(); // finish the augmentation

//		return(ncFile);
	}

	
	
	
	/*****************************************************************************************************************/
	private void addProfileAtts(NetcdfDataset ncFile, Variable var,
			NppProfile profile)
	{
		int iProfileFieldNumber = -9; // Is this variable in the profile.xml file?
		for(int p = 0; p < profile.vFields.size(); p++)
		{
			iProfileFieldNumber = -9;
			if(profile.vFields.elementAt(p)[0][1].equalsIgnoreCase(var.getShortName()))
			{
				iProfileFieldNumber = p;
				break;
			}
		}

		if(iProfileFieldNumber >= 0)
			cfAtts(ncFile, profile, iProfileFieldNumber, ncFile.getVariables(), var); // Create CF required atts
  }

	
	
	/*****************************************************************************************************************/
	  private void cfAtts(NetcdfDataset ncFile, NppProfile nppProf, int iProfileFieldNumber, 
			  List<Variable> varList, Variable var)
	  {
	    for(int a = 0; a < nppProf.vFields.elementAt(iProfileFieldNumber).length; a++ )
	    {
	      //System.out.println("\t" + nppProf.vFields.elementAt(iProfileFieldNumber)[a][0] + "  " + 
	      //                   nppProf.vFields.elementAt(iProfileFieldNumber)[a][1]);

	      if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("MeasurementUnits"))
	    
	        ncFile.addVariableAttribute(var, new Attribute("units", 
	                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim()));
	      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("Description"))
	      {
	        ncFile.addVariableAttribute(var, new Attribute("long_name", 
	                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim()));
	        ncFile.addVariableAttribute(var, new Attribute("standard_name", 
	                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim()));
	      }
	      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("DatumOffset"))
	        ncFile.addVariableAttribute(var, new Attribute("add_offset", 
	                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim()));
	      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("Scaled"))
	        ncFile.addVariableAttribute(var, new Attribute("scale_factor", 
	                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim()));
	      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("RangeMax"))
	        ncFile.addVariableAttribute(var, new Attribute("valid_max", 
	                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim()));
	      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("RangeMin"))
	        ncFile.addVariableAttribute(var, new Attribute("valid_min", 
	                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim()));

	      if( !nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("Field_Name"))
	        ncFile.addVariableAttribute(var, new Attribute(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0], 
	                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim()));
	    }
	  }
	  
	  
		/*****************************************************************************************************************/
		private void addVarAttributes(NetcdfDataset ncFile, Variable var)
		{
			for(int x = 0; x < var.getAttributes().size(); x++) // Add Variable Attributes
			{
				Attribute att = var.getAttributes().get(x);
				if(att.getDataType().toString().equalsIgnoreCase("LONG")) // Long is invalid type - Change to double
					ncFile.addVariableAttribute(var, new Attribute(att.getName(), att.getNumericValue().doubleValue()));
				else
					if(att.isString())
						ncFile.addVariableAttribute(var, new Attribute(att.getName(), att.getStringValue()));
					else
						ncFile.addVariableAttribute(var, new Attribute(att.getName(), att.getNumericValue()));
			}

			// Dataset specific atts
			if(var.getShortName().equalsIgnoreCase("lat"))
			{
				ncFile.addVariableAttribute(var, new Attribute("units", "degrees_north"));
				ncFile.addVariableAttribute(var, new Attribute("long_name", "latitude"));
				ncFile.addVariableAttribute(var, new Attribute("standard_name", "latitude"));
			}
			else
				if(var.getShortName().equalsIgnoreCase("lon"))
				{
					ncFile.addVariableAttribute(var, new Attribute("units", "degrees_east"));
					ncFile.addVariableAttribute(var, new Attribute("long_name", "longitude"));
					ncFile.addVariableAttribute(var, new Attribute("standard_name", "longitude"));
				}
				else
					if(var.getShortName().equalsIgnoreCase("midtime"))
					{
						Variable v = ncFile.findVariable("midtime");
						ncFile.addVariableAttribute(v, new Attribute("units", "microseconds since 1958-01-01 00:00:00Z"));
						ncFile.addVariableAttribute(v, new Attribute("long_name", "midtime"));
						ncFile.addVariableAttribute(v, new Attribute("standard_name","midtime"));
					}

			if(isSpecDs(var)) // If this is a spec DS add Coordinates Att
				ncFile.addVariableAttribute(var, new Attribute("coordinates", "lat lon"));
		}
		
		
		
		/*****************************************************************************************************************/
		private boolean isSpecDs(Variable var)
		{
			boolean bSpecDs = false;
			List<Dimension> varDimList = var.getDimensions(); // Variable Dimensions

			if(varDimList.size() >= 2)
			{
				if(varDimList.get(1).getName().equalsIgnoreCase("GeoX")	&& varDimList.get(0).getName().equalsIgnoreCase("GeoY"))
					bSpecDs = true;

				if(var.getShortName().toLowerCase().trim().equalsIgnoreCase("lat") || var.getShortName().toLowerCase().trim().equalsIgnoreCase("lon"))
					bSpecDs = false;
			}

			return(bSpecDs);
		}
		
		
}


