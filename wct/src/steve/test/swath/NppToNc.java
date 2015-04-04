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
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/*****************************************************************************
 * Create a NetCdf file from an NPP HDF5 file.
 * 
 * @author Michael.Urzen
 ****************************************************************************/

public class NppToNc
{
	private static List<Variable> varList = null;
	private static Vector<Dimension> vGlobalDims = new Vector<Dimension>();
	private static Vector<String> vVarNames = new Vector<String>();
	private static List<Attribute> globalAttList = null;
	private static boolean bReadShortAsInt = true;

	/************************************************************************
	 * Read an NPP file in HDF5 format and output a more CF compliant file in
	 * NetCdf format. <br>
	 * 
	 * @param args
	 * <br>
	 *          <ul>
	 *          <li>-s (optional) Disables "Read Short As Int" function. (See
	 *          Notes below)</li>
	 *          <li>HDF5 Path</li>
	 *          <li>NetCdf Path</li>
	 *          <li>Profile.xml Path(s) (As many as needed)</li>
	 *          <li>Variable name(s) to extract (Default is ALL)</li>
	 *          </ul>
	 * 
	 *          <pre>
	 * Example:
	 *  java -classpath $NppJar gov.noaa.ncdc.npp.NppToNc Profile1.xml Profile2.xml..... Variable_Name1 Variable_Name2.....
	 * </pre>
	 * 
	 *          <strong>Notes:</strong>
	 * 
	 *          <pre>
	 *  Profile.xml files support variables in the HDF5 file.
	 *  Without the proper Profile.xml file, key CF elements 
	 *  are left blank or set to a default value.
	 * 
	 *  Use Variable_Names to subset NPP files.
	 *  
	 *  HDF variables of type "long" are changed to type "double" in the NetCdf output.
	 *  
	 *  Testing discovered HDF5 variables of type "short" need to be read as type "int" 
	 *  to retrieve the correct values. The "-s" flag disables this function. 
	 *  
	 *  Example:
	 *    java -classpath $NppJar gov.noaa.ncdc.npp.NppToNc -s Profile1.xml Profile2.xml..... Variable_Name1 Variable_Name2.....
	 * </pre>
	 * 
	 *          </pre>
	 * 
	 *          <strong>NPP files containing "Latitude" & "Longitude"
	 *          variables:</strong>
	 * 
	 *          <pre>
	 * Coordinate Attributes are added to any variable with the same dimensions
	 * as Latitude & Longitude, making them viewable by any viewer capable of
	 * reading NetCdf gidded data.
	 * </pre>
	 * **********************************************************************/
	public static void main(String[] args)
	{
		NetcdfFileWriteable ncFile = null;
		NetcdfFile ncH5 = null;
		Vector<NppProfile> vProfiles = new Vector<NppProfile>();

		if(args.length > 1 && args[0].trim().equalsIgnoreCase("-s")) // Disable "Read Short as int"
		{
			bReadShortAsInt = false;
			String[] S = new String[args.length - 1]; // Remove the flag from args[]
			for(int x = 1; x < args.length; x++)
				S[x - 1] = args[x];
			args = null;
			args = S;
		}

		if(args.length < 2)
		{
			System.out.println("Required Arguments:\n\n\tHDF5 Path\n\tNC Path\n");
			System.out.println("Optional Arguments:\n\n\tAs many Profile.xml files as needed, and / or, ");
			System.out.println("\tVariable names to extract from the file. (Default is all Variables)\n");

			System.out.println("NOTE:  Profile names must end in \".xml\"");
			System.exit(0);
		}

		try
		{
			for(int p = 2; p < args.length; p++) // Get optional args
			{
				if(args[p].indexOf(".xml") > 0)
					vProfiles.add(new NppProfile(args[p])); // Load profile.xml files
				else
					vVarNames.add(args[p]); // Load var names to extract - Default is all
			}

			loadHd5(args, ncH5); // Open HDF5 file and returns non-"Data_Products" variables

			ncFile = buildNetCdf(args, vProfiles, varList); // Create NetCdf file and build data structures

			for(int iVar = 0; iVar < varList.size(); iVar++)  // Write data into NetCdf structure
				if(ncFile.findVariable(varList.get(iVar).getShortName().toLowerCase()) != null)
				{
					System.out.println("Processing Variable "	+ varList.get(iVar).getShortName().toLowerCase().trim());
					ncFile.write(varList.get(iVar).getShortName().toLowerCase().trim(),	varList.get(iVar).read());
				}
				else
					System.out.println("Skipping Variable "	+ varList.get(iVar).getShortName() + " not processed.");
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}
		catch(InvalidRangeException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			closeUp(ncFile, ncH5);
			System.exit(1);
		}

		closeUp(ncFile, ncH5);

		System.out.println("Complete\nOutput = " + args[1]);

	}

	/*****************************************************************************************************************
	 * @throws Exception
	 * *************************************************************************************************************/
	private static void loadHd5(String[] args, NetcdfFile ncH5) throws Exception
	{
		int iGeoX = -9, iGeoY = -9;

		ncH5 = NetcdfFile.open(args[0]);
		System.out.println(args[0] + " Open for read");

		varList = ncH5.getVariables();
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
	}

	/******************************************************************************************************************
	 * @throws InvalidRangeException
	 * ***************************************************************************************************************/
	private static NetcdfFileWriteable buildNetCdf(String[] args,
			Vector<NppProfile> vProfiles,
			List<Variable> varList) throws IOException, InvalidRangeException
	{
		NetcdfFileWriteable ncFile = NetcdfFileWriteable.createNew(args[1]); // NC file path
		System.out.println(args[1] + " Open for Write");

		// Hard coded global attributes
		ncFile.addGlobalAttribute("title", "NPP Converted from HDF5 to CF compliant NetCdf");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		ncFile.addGlobalAttribute("creation_date", sdf.format(new Date()));
		ncFile.addGlobalAttribute("Conventions", "CF-1.4");
		ncFile.addGlobalAttribute("CF Note", "units values are a direct translation from HDF5 file and may not be CF compliant");

		// HDF5 file global attributes
		for(int i = 0; i < globalAttList.size(); i++)
		{
			DataType dt = globalAttList.get(i).getDataType();
			if(dt.isString())
				ncFile.addGlobalAttribute(globalAttList.get(i).getName(), globalAttList.get(i).getStringValue());
			else
				if(dt.isNumeric())
					ncFile.addGlobalAttribute(globalAttList.get(i).getName(),	globalAttList.get(i).getNumericValue());
				else
					System.out.println("Invalid Global Attribute type: " + globalAttList.get(i).getName() + "   " + dt.name());
		}

		// Profile.xml global attributes
		for(int p = 0; p < vProfiles.size(); p++)
			for(int i = 0; i < vProfiles.elementAt(p).vGlobAtts.size(); i++)
				ncFile.addGlobalAttribute(
						vProfiles.elementAt(p).vGlobAtts.elementAt(i)[0][0],
						vProfiles.elementAt(p).vGlobAtts.elementAt(i)[0][1]);

		// Add dims
		for(int i = 0; i < vGlobalDims.size(); i++)
			ncFile.addDimension(vGlobalDims.elementAt(i).getName(), vGlobalDims.elementAt(i).getLength());

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
				for(int i = 0; i < varDimList.size(); i++)
					for(int x = 0; x < vGlobalDims.size(); x++)
						if(varDimList.get(i).getLength() == vGlobalDims.elementAt(x).getLength())
						{
							dims = dims + vGlobalDims.elementAt(x).getName().trim() + " ";
							break;
						}

				if(var.getDataType().toString().equalsIgnoreCase("short")	&& bReadShortAsInt) // Read short variables as int
					ncFile.addVariable(var.getShortName().toLowerCase().trim(),	ucar.ma2.DataType.INT, dims.trim());
				else
					if(var.getDataType().toString().equalsIgnoreCase("long")) // Need to change long variables to double
						ncFile.addVariable(var.getShortName().toLowerCase().trim(),	ucar.ma2.DataType.DOUBLE, dims.trim());
					else
						ncFile.addVariable(var.getShortName().toLowerCase().trim(),	var.getDataType(), dims.trim());

				addVarAttributes(ncFile, var);

				for(int p = 0; p < vProfiles.size(); p++) // Add variable attributes from profile.xml
					addProfileAtts(ncFile, var, vProfiles.elementAt(p));
			}
			else
				System.out.println("Skipping Variable " + var.getShortName() + "  "	+ var.getDataType().toString());
		}

		ncFile.create(); // Create on disk

		return(ncFile);
	}

	/*****************************************************************************************************************/
	private static void addVarAttributes(NetcdfFileWriteable ncFile, Variable var)
	{
		for(int x = 0; x < var.getAttributes().size(); x++) // Add Variable Attributes
		{
			Attribute Att = var.getAttributes().get(x);
			if(Att.getDataType().toString().equalsIgnoreCase("LONG")) // Long is invalid type - Change to double
				ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), Att.getName(), Att.getNumericValue().doubleValue());
			else
				if(Att.isString())
					ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), Att.getName(), Att.getStringValue());
				else
					ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), Att.getName(), Att.getNumericValue());
		}

		// Dataset specific atts
		if(var.getShortName().equalsIgnoreCase("lat"))
		{
			ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "units", "degrees_north");
			ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "long_name", "latitude");
			ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "standard_name", "latitude");
		}
		else
			if(var.getShortName().equalsIgnoreCase("lon"))
			{
				ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "units", "degrees_east");
				ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "long_name", "longitude");
				ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "standard_name", "longitude");
			}
			else
				if(var.getShortName().equalsIgnoreCase("midtime"))
				{
					ncFile.addVariableAttribute("midtime", "units",	"microseconds since 1958-01-01 00:00:00Z");
					ncFile.addVariableAttribute("midtime", "long_name", "midtime");
					ncFile.addVariableAttribute("midtime", "standard_name", "midtime");
				}

		if(isSpecDs(var)) // If this is a spec DS add Coordinates Att
			ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "coordinates", "lat lon");
	}

	/*****************************************************************************************************************/
  private static void cfAtts(NetcdfFileWriteable ncFile, NppProfile nppProf, int iProfileFieldNumber, List<Variable> varList, Variable var)
  {
    for(int a = 0; a < nppProf.vFields.elementAt(iProfileFieldNumber).length; a++ )
    {
      //System.out.println("\t" + nppProf.vFields.elementAt(iProfileFieldNumber)[a][0] + "  " + 
      //                   nppProf.vFields.elementAt(iProfileFieldNumber)[a][1]);

      if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("MeasurementUnits"))
        ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "units", 
                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim());
      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("Description"))
      {
        ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "long_name", 
                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim());
        ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "standard_name", 
                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim());
      }
      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("DatumOffset"))
        ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "add_offset", 
                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim());
      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("Scaled"))
        ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "scale_factor", 
                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim());
      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("RangeMax"))
        ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "valid_max", 
                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim());
      else if(nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("RangeMin"))
        ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), "valid_min", 
                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim());

      if( !nppProf.vFields.elementAt(iProfileFieldNumber)[a][0].equalsIgnoreCase("Field_Name"))
        ncFile.addVariableAttribute(var.getShortName().toLowerCase().trim(), nppProf.vFields.elementAt(iProfileFieldNumber)[a][0], 
                                    nppProf.vFields.elementAt(iProfileFieldNumber)[a][1].trim());
    }
  }

	/*****************************************************************************************************************/
	private static void addProfileAtts(NetcdfFileWriteable ncFile, Variable var,
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
			cfAtts(ncFile, profile, iProfileFieldNumber, varList, var); // Create CF required atts
  }

	/*****************************************************************************************************************/
	private static boolean isSpecDs(Variable var)
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

	/*****************************************************************************************************************/
	private static void closeUp(NetcdfFileWriteable ncFile, NetcdfFile ncH5)
	{
		try
		{
			if(ncH5 != null)
				ncH5.close();
			if(ncFile != null)
				ncFile.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}