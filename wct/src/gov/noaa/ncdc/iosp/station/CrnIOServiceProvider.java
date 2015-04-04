package gov.noaa.ncdc.iosp.station;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.unidata.io.RandomAccessFile;

/** IOService Provider for CRN data */

public class CrnIOServiceProvider extends AbstractIOServiceProvider {      
	ucar.unidata.io.RandomAccessFile raf;
	ArrayList<Variable> varList=new ArrayList<Variable>();
	Map<String, Array> resultMap=new HashMap<String, Array> (); 

	public static void main(String[] args) { 
		String infile=" ";
		if (args.length == 1) {
			infile=args[0];
		} else { System.out.println("Usage: java CrnIOServiceProvider inputFile");
		System.exit(0);  
		}   	   
		try {   
			NetcdfFile.registerIOProvider(CrnIOServiceProvider.class);
			NetcdfFile ncfile=NetcdfFile.open(infile);
			System.out.println("ncfile = \n"+ncfile);
			String outfile="C:\\Documents and Settings\\Nina.Stroumentova\\crn_netcdf\\data_out\\crnout.nc";
			NetcdfFile outFile = ucar.nc2.FileWriter.writeToFile(ncfile, outfile, false, 0);                
			ncfile.close();
			outFile.close();

		} catch (Exception e) {
			System.out.println("MAIN!!!   "+e.toString());
			e.printStackTrace(); }
	}
	//-------------------------------------------------------------------------------


	/**  Open existing file. */

	public void open(ucar.unidata.io.RandomAccessFile raf, ucar.nc2.NetcdfFile ncfile,
			ucar.nc2.util. CancelTask cancelTask) throws IOException {
		String filename=raf.getLocation();  //System.out.println("NAME="+filename);
		if (filename.indexOf("CRNDAILY") > -1) { 
			doCrnDaily(raf, ncfile);
		} else { 
			doCrnHourly(raf, ncfile);	 
		}
	}
	//------------------------------------------------------------------------------------ 

	/** Read CRN daily data and populate ncfile with it. */
	void doCrnDaily(ucar.unidata.io.RandomAccessFile raf, ucar.nc2.NetcdfFile ncfile)throws IOException {

		// Define the number of lines of input file          
		int lines=linecount(raf); System.out.println("LINES="+lines);

		String[] wbanno=new String[lines];
		String[] coopno=new String[lines];
		String[] date=new String[lines];
		float[] crx_vn=new float[lines];
		float[] lon=new float[lines];
		float[] lat=new float[lines];
		float[] tmax=new float[lines];
		float[] tmin=new float[lines];
		float[] tmean=new float[lines];
		float[] tavg=new float[lines];
		float[] pcalc=new float[lines];
		float[] solrad=new float[lines];
		float[] surtmax=new float[lines];
		float[] surtmin=new float[lines];
		float[] surtavg=new float[lines];
		float[] rhmax=new float[lines];
		float[] rhmin=new float[lines];
		float[] rhavg=new float[lines];
		float[] sm5=new float[lines];
		float[] sm10=new float[lines];
		float[] sm20=new float[lines];
		float[] sm50=new float[lines];
		float[] sm100=new float[lines];
		float[] st5=new float[lines];
		float[] st10=new float[lines];
		float[] st20=new float[lines];
		float[] st50=new float[lines];
		float[] st100=new float[lines];

		raf.seek(0);  
		for (int i = 0; i < lines; i++) {
			int len=0;  int pos0=0;
			String q = raf.readLine(); //System.out.println("i="+i+" "+q);  

			wbanno[i]=q.substring(0,5); //System.out.print("  wbanno="+wbanno[i]);  
			coopno[i]=q.substring(6,13);  //System.out.print("  coopno="+coopno[i]); 
			date[i]  =q.substring(13, 22);  //System.out.print("  date="+date[i]); 
			crx_vn[i]=Float.parseFloat(q.substring(22,29).trim());  //System.out.print("  crx_vn="+crx_vn[i]); 
			lon[i]=Float.parseFloat(q.substring(29,37).trim());  //System.out.print("  lon="+lon[i]); 
			lat[i]=Float.parseFloat(q.substring(37,45).trim());  //System.out.print("  lat="+lat[i]); 
			tmax[i]=Float.parseFloat(q.substring(45,53).trim()); //System.out.print("  tmax="+tmax[i]); 
			tmin[i]=Float.parseFloat(q.substring(53,61).trim());  //System.out.print("  tmin="+tmin[i]); 
			tmean[i]=Float.parseFloat(q.substring(61,69).trim()); //System.out.print("  tmean="+tmean[i]); 
			tavg[i]=Float.parseFloat(q.substring(69,77).trim());  //System.out.print("  tavg="+tavg[i]); 
			pcalc[i]=Float.parseFloat(q.substring(77,85).trim()); //System.out.print("  pcalc="+pcalc[i]); 
			solrad[i]=Float.parseFloat(q.substring(85,94).trim()); //System.out.print("  solrad="+solrad[i]); 
			surtmax[i]=Float.parseFloat(q.substring(94,102).trim()); //System.out.print("  surtmax="+surtmax[i]); 
			surtmin[i]=Float.parseFloat(q.substring(102,110).trim()); //System.out.print("  surtmin="+surtmin[i]); 
			surtavg[i]=Float.parseFloat(q.substring(110,118).trim()); //System.out.print("  surtavg="+surtavg[i]); 
			rhmax[i]=Float.parseFloat(q.substring(118,126).trim());   //System.out.print("  rhmax="+rhmax[i]); 
			rhmin[i]=Float.parseFloat(q.substring(126,134).trim());   //System.out.print("  rhmin="+rhmin[i]); 
			rhavg[i]=Float.parseFloat(q.substring(134,142).trim());  //System.out.print("  rhavg="+rhavg[i]); 
			sm5[i]=Float.parseFloat(q.substring(142,150).trim());    //System.out.print("  sm5="+sm5[i]); 
			sm10[i]=Float.parseFloat(q.substring(150,158).trim());    //System.out.print("  sm10="+sm10[i]);
			sm20[i]=Float.parseFloat(q.substring(158,166).trim());   // System.out.print("  sm20="+sm20[i]);
			sm50[i]=Float.parseFloat(q.substring(166,174).trim());   //  System.out.print("  sm50="+sm50[i]);
			sm100[i]=Float.parseFloat(q.substring(174,182).trim());  //System.out.print("  sm100="+sm100[i]);
			st5[i]=Float.parseFloat(q.substring(182,190).trim());   // System.out.print("  st5="+st5[i]);
			st10[i]=Float.parseFloat(q.substring(190,198).trim());  // System.out.print("  st10="+st10[i]);
			st20[i]=Float.parseFloat(q.substring(198,206).trim());  // System.out.print("  st20="+st20[i]);
			st50[i]=Float.parseFloat(q.substring(206,214).trim());  // System.out.print("  st50="+st50[i]);
			st100[i]=Float.parseFloat(q.substring(214).trim());  //System.out.print("  st100="+st100[i]);
		}

		// Define Dimensions, Variables, Attributes for NetCDF file
		List<Dimension> dims=new ArrayList<Dimension> ();  
		List<Dimension> dm=new ArrayList<Dimension> ();
		List<Dimension> dimst=new ArrayList<Dimension> ();
		List<Dimension> wdm=new ArrayList<Dimension> ();
		List<Dimension> od=new ArrayList<Dimension> ();
		Dimension obsDim=new Dimension("obs", lines, true);
		Dimension stnDim=new Dimension("station", 1, true);
		Dimension nameDim=new Dimension("name_strlen", 6, true);
		Dimension wDim=new Dimension("info_strlen", 5, true);
		dims.add(stnDim);  dims.add(obsDim);   // for numeric variables
		dimst.add(stnDim);                     // for lon, lat
		dm.add(stnDim); dm.add(nameDim);       // for char coopno
		wdm.add(stnDim); wdm.add(wDim);        // for char wbanno

		ncfile.addDimension(null, obsDim);
		ncfile.addDimension(null, stnDim);
		ncfile.addDimension(null, nameDim);
		ncfile.addDimension(null, wDim);

		int[] shape = new int[] {1, lines};
		int[] sh=new int[]{1}; 
		int[] nsh=new int[]{1,6};  
		int[] wsh=new int[]{1,5};

		Variable vv=new Variable(ncfile, null, null, "station_name");
		vv.setDimensions(dm);
		vv.setDataType(DataType.CHAR);
		vv.addAttribute( new Attribute("long_name", "The station COOP number"));
		vv.addAttribute( new Attribute("standard_name", "station_id"));
		ArrayChar nameArray=new ArrayChar(nsh);
		Index nameIx=nameArray.getIndex();
		char[] ca=coopno[0].toCharArray();
		for (int i=0; i<6; i++) { nameArray.setChar(nameIx.set(0,i), ca[i]); }
		vv.setCachedData(nameArray, false);
		ncfile.addVariable( null, vv);
		varList.add(vv);
		//NCdump.printArray(nameArray, "station_name", System.out, null);
		resultMap.put("station_name", nameArray); 

		Variable vvv=new Variable(ncfile, null, null, "station_info");
		vvv.setDimensions(wdm);
		vvv.setDataType(DataType.CHAR);
		vvv.addAttribute( new Attribute("long_name", "The station WBAN number."));
		ArrayChar wArray=new ArrayChar(wsh);
		Index wwIx=wArray.getIndex();
		char[] wb=wbanno[0].toCharArray();
		for (int i=0; i<5; i++) { wArray.setChar(wwIx.set(0,i), wb[i]); }
		vvv.setCachedData(wArray, false);
		ncfile.addVariable( null, vvv);
		varList.add(vvv);
		resultMap.put("station_info", wArray); 
		// NCdump.printArray(wArray, "station_info", System.out, null);

		ArrayFloat.D1 latArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
		ArrayFloat.D1 lonArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
		latArray.set(0, lat[0]);
		lonArray.set(0, lon[0]);
		Variable var = new Variable(ncfile, null, null, "lat");
		var.setDimensions(dimst);
		var.setDataType(DataType.FLOAT);
		var.addAttribute( new Attribute("long_name", "Station latitude coordinate"));
		var.addAttribute( new Attribute("units", "degrees_north"));
		var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
		var.setCachedData(latArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("lat", latArray); 
		// NCdump.printArray(latArray, "latitude", System.out, null);

		var = new Variable(ncfile, null, null, "lon");
		var.setDimensions(dimst);
		var.setDataType(DataType.FLOAT);
		var.addAttribute( new Attribute("long_name", "Station longitude coordinate"));
		var.addAttribute( new Attribute("units", "degrees_east"));
		var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
		var.setCachedData(lonArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("lon", lonArray); 
		// NCdump.printArray(lonArray, "longitude", System.out, null); 

		ArrayInt.D2 timeArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index tIx=timeArray.getIndex();  
		ArrayFloat.D2 crxArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index crxIx=crxArray.getIndex();  
		ArrayFloat.D2 tmaxArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index tmaxIx=tmaxArray.getIndex(); 
		ArrayFloat.D2 tminArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index tminIx=tminArray.getIndex(); 
		ArrayFloat.D2 tmeanArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index tmeanIx=tmeanArray.getIndex(); 
		ArrayFloat.D2 tavgArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index tavgIx=tavgArray.getIndex(); 
		ArrayFloat.D2 pArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index pIx=pArray.getIndex(); 
		ArrayFloat.D2 solArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index solIx=solArray.getIndex(); 
		ArrayFloat.D2 surtmaxArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index surtmaxIx=surtmaxArray.getIndex(); 
		ArrayFloat.D2 surtminArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index surtminIx=surtminArray.getIndex(); 
		ArrayFloat.D2 surtavgArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index surtavgIx=surtavgArray.getIndex(); 
		ArrayFloat.D2 rhmaxArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index rhmaxIx=rhmaxArray.getIndex(); 
		ArrayFloat.D2 rhminArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index rhminIx=rhminArray.getIndex(); 
		ArrayFloat.D2 rhavgArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index rhavgIx=rhavgArray.getIndex(); 
		ArrayFloat.D2 sm5Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm5Ix=sm5Array.getIndex(); 
		ArrayFloat.D2 sm10Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm10Ix=sm10Array.getIndex(); 
		ArrayFloat.D2 sm20Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm20Ix=sm20Array.getIndex(); 
		ArrayFloat.D2 sm50Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm50Ix=sm50Array.getIndex(); 
		ArrayFloat.D2 sm100Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm100Ix=sm100Array.getIndex(); 
		ArrayFloat.D2 st5Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st5Ix=st5Array.getIndex(); 
		ArrayFloat.D2 st10Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st10Ix=st10Array.getIndex(); 
		ArrayFloat.D2 st20Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st20Ix=st20Array.getIndex(); 
		ArrayFloat.D2 st50Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st50Ix=st50Array.getIndex(); 
		ArrayFloat.D2 st100Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st100Ix=st100Array.getIndex(); 

		int[] mints=getMinutes(date);
		for (int i=0; i<lines; i++) {
			timeArray.setInt(tIx.set(0,i), mints[i]);
			crxArray.setFloat(crxIx.set(0,i), crx_vn[i]);
			tmaxArray.setFloat(tmaxIx.set(0,i), tmax[i]);
			tminArray.setFloat(tminIx.set(0,i), tmin[i]);
			tmeanArray.setFloat(tmeanIx.set(0,i), tmean[i]);
			tavgArray.setFloat(tavgIx.set(0,i), tavg[i]);
			pArray.setFloat(pIx.set(0,i), pcalc[i]);
			solArray.setFloat(solIx.set(0,i), solrad[i]);
			surtmaxArray.setFloat(surtmaxIx.set(0,i), surtmax[i]);
			surtminArray.setFloat(surtminIx.set(0,i), surtmin[i]);
			surtavgArray.setFloat(surtavgIx.set(0,i), surtavg[i]);
			rhmaxArray.setFloat(rhmaxIx.set(0,i), rhmax[i]);
			rhminArray.setFloat(rhminIx.set(0,i), rhmin[i]);
			rhavgArray.setFloat(rhavgIx.set(0,i), rhavg[i]);
			sm5Array.setFloat(sm5Ix.set(0,i), sm5[i]);
			sm10Array.setFloat(sm10Ix.set(0,i), sm10[i]);
			sm20Array.setFloat(sm20Ix.set(0,i), sm20[i]);
			sm50Array.setFloat(sm50Ix.set(0,i), sm50[i]);
			sm100Array.setFloat(sm100Ix.set(0,i), sm100[i]);
			st5Array.setFloat(st5Ix.set(0,i), st5[i]);
			st10Array.setFloat(st10Ix.set(0,i), st10[i]);
			st20Array.setFloat(st20Ix.set(0,i), st20[i]);
			st50Array.setFloat(st50Ix.set(0,i), st50[i]);
			st100Array.setFloat(st100Ix.set(0,i), st100[i]);
		}

		Variable ivar = new Variable(ncfile, null, null, "time");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("standard_name", "time"));
		ivar.addAttribute(new Attribute("long_name", " The local calendar date of the observations."));
		ivar.addAttribute(new Attribute("units", "minutes since 1970-01-01 00:00:00"));
		ivar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
		ivar.addAttribute(new Attribute("axis", "T"));
		ivar.setCachedData(timeArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("time", timeArray); 
		//NCdump.printArray(timeArray, "time", System.out, null);

		var = new Variable(ncfile, null, null, "CRX_VN");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "The version number of the station datalogger program."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("_FillValue", "9.999"));
		var.setCachedData(crxArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("CRX_VN", crxArray); 
		// NCdump.printArray(crxArray, "CRX_VN", System.out, null); 

		var = new Variable(ncfile, null, null, "T_DAILY_MAX");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Maximum temperature during the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(tmaxArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("T_DAILY_MAX", tmaxArray); 
		// NCdump.printArray(tmaxArray, "T_DAILY_MAX", System.out, null); 

		var = new Variable(ncfile, null, null, "T_DAILY_MIN");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Minimum temperature during the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(tminArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("T_DAILY_MIN", tminArray); 

		var = new Variable(ncfile, null, null, "T_DAILY_MEAN");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Mean temperature."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(tmeanArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("T_DAILY_MEAN", tmeanArray); 

		var = new Variable(ncfile, null, null, "T_DAILY_AVG");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average temperature during the 24 hours of the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(tavgArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("T_DAILY_AVG", tavgArray); 

		var = new Variable(ncfile, null, null, "P_DAILY_CALC");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Total amount of precipitation recorded during the 24 hours of the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "milimeters"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(pArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("P_DAILY_CALC", pArray); 

		var = new Variable(ncfile, null, null, "SOLARAD_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Total solar energy received recorded during the 24 hours of the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "MJ/meter^2"));
		var.addAttribute(new Attribute("_FillValue", "-9999.00"));
		var.setCachedData(solArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOLARAD_DAILY", solArray); 

		var = new Variable(ncfile, null, null, "SUR_TEMP_DAILY_MAX");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Maximum 5-minute temperature during the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(surtmaxArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SUR_TEMP_DAILY_MAX", surtmaxArray); 

		var = new Variable(ncfile, null, null, "SUR_TEMP_DAILY_MIN");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Minimum 5-minute temperature during the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(surtminArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SUR_TEMP_DAILY_MIN", surtminArray); 

		var = new Variable(ncfile, null, null, "SUR_TEMP_DAILY_AVG");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "The temperature that is calculated by averaging 24 full-hour averages."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(surtavgArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SUR_TEMP_DAILY_AVG", surtavgArray); 

		var = new Variable(ncfile, null, null, "RH_DAILY_MAX");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Maximum clock hour relative humidity for the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(rhmaxArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("RH_DAILY_MAX", rhmaxArray); 

		var = new Variable(ncfile, null, null, "RH_DAILY_MIN");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Minimum clock hour relative humidity for the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(rhminArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("RH_DAILY_MIN", rhminArray); 

		var = new Variable(ncfile, null, null, "RH_DAILY_AVG");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average hourly relative humidity for the day."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(rhavgArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("RH_DAILY_AVG", rhavgArray); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_5_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire day at 5 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm5Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_5_DAILY", sm5Array); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_10_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire day at 10 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm10Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_10_DAILY", sm10Array); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_20_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire day at 20 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm20Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_20_DAILY", sm20Array); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_50_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire day at 50 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm50Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_50_DAILY", sm50Array); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_100_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire day at 100 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm100Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_100_DAILY", sm100Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_5_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire day at 5 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st5Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_5_DAILY", st5Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_10_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire day at 10 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st10Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_10_DAILY", st10Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_20_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire day at 20 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st20Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_20_DAILY", st20Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_50_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire day at 50 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st50Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_50_DAILY", st50Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_100_DAILY");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire day at 100 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st100Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_100_DAILY", st100Array); 

		ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.4"));
		ncfile.addAttribute(null, new Attribute("CF:featureType", "timeSeries"));
		ncfile.addAttribute(null, new Attribute("title", "Climate Reference Network (CRN)"));
		ncfile.addAttribute(null, new Attribute("description", "Climate Reference Network daily data"));
		String s0=date[0].substring(0,4)+"-"+date[0].substring(4,6)+"-"+date[0].substring(6);
		ncfile.addAttribute(null, new Attribute("time_coverage_start", s0));
		String s1=date[lines-1].substring(0,4)+"-"+date[lines-1].substring(4,6)+"-"+
		date[lines-1].substring(6);
		ncfile.addAttribute(null, new Attribute("time_coverage_end", ""+s1));



		//System.out.println("VARS="+varList.size());
		//for (int j=0; j< varList.size(); j++) {
		//System.out.println("j="+(j+1)+" NAME: "+varList.get(j).getName()); 
		//}

		System.out.println("RESULT="+resultMap.size());

		ncfile.finish();

	}
	//-----------------------------------------------------------------------------

	/** Read CRN hourly data and populate ncfile with it. */
	void doCrnHourly(ucar.unidata.io.RandomAccessFile raf, ucar.nc2.NetcdfFile ncfile)throws IOException { 	  
		// Define the number of lines of input file          
		int lines=linecount(raf);  //System.out.println("LINES="+lines);

		String[] wbanno=new String[lines];
		String[] coopno=new String[lines];
		String[] utc_date=new String[lines];
		String[] utc_time=new String[lines];
		String[] lst_date=new String[lines];
		String[] lst_time=new String[lines];
		float[] crx_vn=new float[lines];
		float[] lon=new float[lines];
		float[] lat=new float[lines];
		float[] tmax=new float[lines];
		float[] tmin=new float[lines];
		float[] tavg=new float[lines];
		float[] tcalc=new float[lines];
		float[] pcalc=new float[lines];
		int[] sol=new int[lines];
		int[] sol_qc=new int[lines];
		int[] sol_max=new int[lines];
		int[] sol_max_qc=new int[lines];
		int[] sol_min=new int[lines];
		int[] sol_min_qc=new int[lines];
		float[] surt=new float[lines];
		int[] surt_qc=new int[lines];       
		float[] surtmax=new float[lines];
		int[] surtmax_qc=new int[lines];
		float[] surtmin=new float[lines];
		int[] surtmin_qc=new int[lines];
		int[] rhavg=new int[lines];
		int[] rhavg_qc=new int[lines];
		float[] sm5=new float[lines];
		float[] sm10=new float[lines];
		float[] sm20=new float[lines];
		float[] sm50=new float[lines];
		float[] sm100=new float[lines];
		float[] st5=new float[lines];
		float[] st10=new float[lines];
		float[] st20=new float[lines];
		float[] st50=new float[lines];
		float[] st100=new float[lines];

		raf.seek(0);  
		for (int i = 0; i < lines; i++) {
			int len=0;  int pos0=0;
			String q = raf.readLine(); //System.out.print("i="+i);  

			wbanno[i]=q.substring(0,5); //System.out.print("  wbanno="+wbanno[i]);  
			coopno[i]=q.substring(6,12);  //System.out.print("  coopno="+coopno[i]); 
			utc_date[i]  =q.substring(13, 22); //System.out.print("  utc_date="+utc_date[i]); 
			utc_time[i]  =q.substring(22, 26); //System.out.print("  utc_time="+utc_time[i]); 
			lst_date[i]  =q.substring(27, 35);  //System.out.print("  lst_date="+lst_date[i]); 
			lst_time[i]  =q.substring(36, 40);  //System.out.print("  lst_time="+lst_time[i]); 
			crx_vn[i]=Float.parseFloat(q.substring(41,47).trim());  //System.out.print("  crx_vn="+crx_vn[i]); 
			lon[i]=Float.parseFloat(q.substring(48,55).trim());  //System.out.print("  lon="+lon[i]); 
			lat[i]=Float.parseFloat(q.substring(56,63).trim());  //System.out.print("  lat="+lat[i]); 
			tcalc[i]=Float.parseFloat(q.substring(64,71).trim()); //System.out.print("  tcalc="+tcalc[i]); 
			tavg[i]=Float.parseFloat(q.substring(72,79).trim());  //System.out.print("  tavg="+tavg[i]); 
			tmax[i]=Float.parseFloat(q.substring(80,87).trim()); //System.out.print("  tmax="+tmax[i]); 
			tmin[i]=Float.parseFloat(q.substring(88,95).trim());  //System.out.print("  tmin="+tmin[i]);        	  
			pcalc[i]=Float.parseFloat(q.substring(96,103).trim()); //System.out.print("  pcalc="+pcalc[i]); 
			sol[i]=Integer.parseInt(q.substring(104,110).trim()); //System.out.print("  sol="+sol[i]); 
			sol_qc[i]=Integer.parseInt(q.substring(111,112).trim());  //System.out.print("  sol_qc="+sol_qc[i]); 
			sol_max[i]=Integer.parseInt(q.substring(113,119).trim()); //System.out.print("  sol_max="+sol_max[i]); 
			sol_max_qc[i]=Integer.parseInt(q.substring(120,121).trim()); //System.out.print("  sol_max_qc="+sol_max_qc[i]); 
			sol_min[i]=Integer.parseInt(q.substring(122,128).trim());    //System.out.print("  sol_min="+sol_min[i]); 
			sol_min_qc[i]=Integer.parseInt(q.substring(129,130).trim()); //System.out.print("  sol_min_qc="+sol_min_qc[i]);       	  
			surt[i]=Float.parseFloat(q.substring(131,138).trim()); //System.out.print("  surt="+surt[i]);
			surt_qc[i]=Integer.parseInt(q.substring(139,140).trim()); //System.out.print("  surt_qc="+surt_qc[i]);
			surtmax[i]=Float.parseFloat(q.substring(141,148).trim()); //System.out.print("  surtmax="+surtmax[i]); 
			surtmax_qc[i]=Integer.parseInt(q.substring(149,150).trim()); //System.out.print("  surtmax_qc="+surtmax_qc[i]); 
			surtmin[i]=Float.parseFloat(q.substring(151,158).trim()); //System.out.print("  surtmin="+surtmin[i]); 
			surtmin_qc[i]=Integer.parseInt(q.substring(159,160).trim()); //System.out.print("  surtmin_qc="+surtmin_qc[i]); 
			rhavg[i]=Integer.parseInt(q.substring(161,166).trim());  //System.out.print("  rhavg="+rhavg[i]); 
			rhavg_qc[i]=Integer.parseInt(q.substring(167,168).trim());  //System.out.print("  rhavg_qc="+rhavg_qc[i]);        	  
			sm5[i]=Float.parseFloat(q.substring(169,176).trim());    //System.out.print("  sm5="+sm5[i]); 
			sm10[i]=Float.parseFloat(q.substring(177,184).trim());    //System.out.print("  sm10="+sm10[i]);
			sm20[i]=Float.parseFloat(q.substring(185,192).trim());    //System.out.print("  sm20="+sm20[i]);
			sm50[i]=Float.parseFloat(q.substring(193,200).trim());     //System.out.print("  sm50="+sm50[i]);
			sm100[i]=Float.parseFloat(q.substring(201,208).trim());  //System.out.print("  sm100="+sm100[i]);
			st5[i]=Float.parseFloat(q.substring(209,216).trim());    //System.out.print("  st5="+st5[i]);
			st10[i]=Float.parseFloat(q.substring(217,224).trim());   //System.out.print("  st10="+st10[i]);
			st20[i]=Float.parseFloat(q.substring(225,232).trim());   //System.out.print("  st20="+st20[i]);
			st50[i]=Float.parseFloat(q.substring(233,240).trim());   //System.out.print("  st50="+st50[i]);
			st100[i]=Float.parseFloat(q.substring(241).trim());      //System.out.print("  st100="+st100[i]);
			//System.out.println();
		}

		// Define Dimensions, Variables, Attributes for NetCDF file
		List<Dimension> dims=new ArrayList<Dimension> ();  
		List<Dimension> dm=new ArrayList<Dimension> ();
		List<Dimension> dimst=new ArrayList<Dimension> ();
		List<Dimension> wdm=new ArrayList<Dimension> ();
		List<Dimension> od=new ArrayList<Dimension> ();
		Dimension obsDim=new Dimension("obs", lines, true);
		Dimension stnDim=new Dimension("station", 1, true);
		Dimension nameDim=new Dimension("name_strlen", 6, true);
		Dimension wDim=new Dimension("info_strlen", 5, true);
		dims.add(stnDim);  dims.add(obsDim);   // for numeric variables
		dimst.add(stnDim);                     // for lon, lat
		dm.add(stnDim); dm.add(nameDim);       // for char coopno
		wdm.add(stnDim); wdm.add(wDim);        // for char wbanno

		ncfile.addDimension(null, obsDim);
		ncfile.addDimension(null, stnDim);
		ncfile.addDimension(null, nameDim);
		ncfile.addDimension(null, wDim);

		int[] shape = new int[] {1, lines};
		int[] sh=new int[]{1}; 
		int[] nsh=new int[]{1,6};  
		int[] wsh=new int[]{1,5};

		Variable vv=new Variable(ncfile, null, null, "station_name");
		vv.setDimensions(dm);
		vv.setDataType(DataType.CHAR);
		vv.addAttribute( new Attribute("long_name", "The station COOP number"));
		vv.addAttribute( new Attribute("standard_name", "station_id"));
		ArrayChar nameArray=new ArrayChar(nsh);
		Index nameIx=nameArray.getIndex();
		char[] ca=coopno[0].toCharArray();
		for (int i=0; i<6; i++) { nameArray.setChar(nameIx.set(0,i), ca[i]); }
		vv.setCachedData(nameArray, false);
		ncfile.addVariable( null, vv);
		varList.add(vv);
		// NCdump.printArray(nameArray, "station_name", System.out, null);
		resultMap.put("station_name", nameArray); 

		Variable vvv=new Variable(ncfile, null, null, "station_info");
		vvv.setDimensions(wdm);
		vvv.setDataType(DataType.CHAR);
		vvv.addAttribute( new Attribute("long_name", "The station WBAN number."));
		ArrayChar wArray=new ArrayChar(wsh);
		Index wwIx=wArray.getIndex();
		char[] wb=wbanno[0].toCharArray();
		for (int i=0; i<5; i++) { wArray.setChar(wwIx.set(0,i), wb[i]); }
		vvv.setCachedData(wArray, false);
		ncfile.addVariable( null, vvv);
		varList.add(vvv);
		resultMap.put("station_info", wArray); 
		//  NCdump.printArray(wArray, "station_info", System.out, null);

		ArrayFloat.D1 latArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
		ArrayFloat.D1 lonArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
		latArray.set(0, lat[0]);
		lonArray.set(0, lon[0]);
		Variable var = new Variable(ncfile, null, null, "lat");
		var.setDimensions(dimst);
		var.setDataType(DataType.FLOAT);
		var.addAttribute( new Attribute("long_name", "Station latitude coordinate"));
		var.addAttribute( new Attribute("units", "degrees_north"));
		var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
		var.setCachedData(latArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("lat", latArray); 
		// NCdump.printArray(latArray, "latitude", System.out, null);

		var = new Variable(ncfile, null, null, "lon");
		var.setDimensions(dimst);
		var.setDataType(DataType.FLOAT);
		var.addAttribute( new Attribute("long_name", "Station longitude coordinate"));
		var.addAttribute( new Attribute("units", "degrees_east"));
		var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
		var.setCachedData(lonArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("lon", lonArray); 
		//NCdump.printArray(lonArray, "longitude", System.out, null);    

		ArrayInt.D2 timeArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index tIx=timeArray.getIndex();  
		ArrayInt.D2 loc_timeArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index ltIx=loc_timeArray.getIndex();  
		ArrayFloat.D2 crxArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index crxIx=crxArray.getIndex(); 
		ArrayFloat.D2 tcalcArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index tcalcIx=tcalcArray.getIndex(); 
		ArrayFloat.D2 tavgArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index tavgIx=tavgArray.getIndex(); 
		ArrayFloat.D2 tmaxArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index tmaxIx=tmaxArray.getIndex(); 
		ArrayFloat.D2 tminArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index tminIx=tminArray.getIndex();   
		ArrayFloat.D2 pArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index pIx=pArray.getIndex();   
		ArrayInt.D2 solArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index solIx=solArray.getIndex(); 
		ArrayInt.D2 sol_qcArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index sol_qcIx=sol_qcArray.getIndex(); 
		ArrayInt.D2 sol_maxArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index sol_maxIx=sol_maxArray.getIndex(); 
		ArrayInt.D2 sol_max_qcArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index sol_max_qcIx=sol_max_qcArray.getIndex(); 
		ArrayInt.D2 sol_minArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index sol_minIx=sol_minArray.getIndex(); 
		ArrayInt.D2 sol_min_qcArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index sol_min_qcIx=sol_min_qcArray.getIndex(); 
		ArrayFloat.D2 surtArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index surtIx=surtArray.getIndex(); 
		ArrayInt.D2 surt_qcArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index surt_qcIx=surt_qcArray.getIndex();  
		ArrayFloat.D2 surtmaxArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index surtmaxIx=surtmaxArray.getIndex(); 
		ArrayInt.D2 surtmax_qcArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index surtmax_qcIx=surtmax_qcArray.getIndex();
		ArrayFloat.D2 surtminArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index surtminIx=surtminArray.getIndex(); 
		ArrayInt.D2 surtmin_qcArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index surtmin_qcIx=surtmin_qcArray.getIndex(); 
		ArrayInt.D2 rhavgArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index rhavgIx=rhavgArray.getIndex(); 
		ArrayInt.D2 rhavg_qcArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index rhavg_qcIx=rhavg_qcArray.getIndex();
		ArrayFloat.D2 sm5Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm5Ix=sm5Array.getIndex(); 
		ArrayFloat.D2 sm10Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm10Ix=sm10Array.getIndex(); 
		ArrayFloat.D2 sm20Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm20Ix=sm20Array.getIndex(); 
		ArrayFloat.D2 sm50Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm50Ix=sm50Array.getIndex(); 
		ArrayFloat.D2 sm100Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index sm100Ix=sm100Array.getIndex(); 
		ArrayFloat.D2 st5Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st5Ix=st5Array.getIndex(); 
		ArrayFloat.D2 st10Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st10Ix=st10Array.getIndex(); 
		ArrayFloat.D2 st20Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st20Ix=st20Array.getIndex(); 
		ArrayFloat.D2 st50Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st50Ix=st50Array.getIndex(); 
		ArrayFloat.D2 st100Array=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index st100Ix=st100Array.getIndex(); 

		int[] mints=getMinutes(utc_date, utc_time);
		int[] loc_mints=getMinutes(lst_date, lst_time);
		for (int i=0; i<lines; i++) {
			timeArray.setInt(tIx.set(0,i), mints[i]);
			loc_timeArray.setInt(ltIx.set(0,i), loc_mints[i]);
			crxArray.setFloat(crxIx.set(0,i), crx_vn[i]);
			tcalcArray.setFloat(tcalcIx.set(0,i), tcalc[i]);
			tavgArray.setFloat(tavgIx.set(0,i), tavg[i]);
			tmaxArray.setFloat(tmaxIx.set(0,i), tmax[i]);
			tminArray.setFloat(tminIx.set(0,i), tmin[i]);    
			pArray.setFloat(pIx.set(0,i), pcalc[i]);    
			solArray.setInt(solIx.set(0,i), sol[i]);
			sol_qcArray.setInt(sol_qcIx.set(0,i), sol_qc[i]);
			sol_maxArray.setInt(sol_maxIx.set(0,i), sol_max[i]);
			sol_max_qcArray.setInt(sol_max_qcIx.set(0,i), sol_max_qc[i]);
			sol_minArray.setInt(sol_minIx.set(0,i), sol_min[i]);
			sol_min_qcArray.setInt(sol_min_qcIx.set(0,i), sol_min_qc[i]);
			surtArray.setFloat(surtIx.set(0,i), surt[i]);
			surt_qcArray.setInt(surt_qcIx.set(0,i), surt_qc[i]);    
			surtmaxArray.setFloat(surtmaxIx.set(0,i), surtmax[i]);
			surtmax_qcArray.setInt(surtmax_qcIx.set(0,i), surtmax_qc[i]);
			surtminArray.setFloat(surtminIx.set(0,i), surtmin[i]);
			surtmin_qcArray.setInt(surtmin_qcIx.set(0,i), surtmin_qc[i]);
			rhavgArray.setInt(rhavgIx.set(0,i), rhavg[i]);
			rhavg_qcArray.setInt(rhavg_qcIx.set(0,i), rhavg_qc[i]);    
			sm5Array.setFloat(sm5Ix.set(0,i), sm5[i]);
			sm10Array.setFloat(sm10Ix.set(0,i), sm10[i]);
			sm20Array.setFloat(sm20Ix.set(0,i), sm20[i]);
			sm50Array.setFloat(sm50Ix.set(0,i), sm50[i]);
			sm100Array.setFloat(sm100Ix.set(0,i), sm100[i]);
			st5Array.setFloat(st5Ix.set(0,i), st5[i]);
			st10Array.setFloat(st10Ix.set(0,i), st10[i]);
			st20Array.setFloat(st20Ix.set(0,i), st20[i]);
			st50Array.setFloat(st50Ix.set(0,i), st50[i]);
			st100Array.setFloat(st100Ix.set(0,i), st100[i]);
		}

		Variable ivar = new Variable(ncfile, null, null, "time");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("standard_name", "time"));
		ivar.addAttribute(new Attribute("long_name", " The UTC of the observation."));
		ivar.addAttribute(new Attribute("units", "minutes since 1970-01-01 00:00:00"));
		ivar.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
		ivar.addAttribute(new Attribute("axis", "T"));
		ivar.setCachedData(timeArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("time", timeArray); 
		// NCdump.printArray(timeArray, "time", System.out, null);

		ivar = new Variable(ncfile, null, null, "LST_time");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("long_name", " The LST of the observation."));
		ivar.addAttribute(new Attribute("units", "minutes since 1970-01-01 00:00:00"));
		ivar.setCachedData(loc_timeArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("LST_time", loc_timeArray); 
		//  NCdump.printArray(loc_timeArray, "LST_time", System.out, null);

		var = new Variable(ncfile, null, null, "CRX_VN");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "The version number of the station datalogger program."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("_FillValue", "9.999"));
		var.setCachedData(crxArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("CRX_VN", crxArray); 
		// NCdump.printArray(crxArray, "CRX_VN", System.out, null); 

		var = new Variable(ncfile, null, null, "T_CALC");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average temperature during the last 5 minutes of the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(tcalcArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("T_CALC", tcalcArray); 

		var = new Variable(ncfile, null, null, "T_HR_AVG");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average temperature during the entire hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(tavgArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("T_HR_AVG", tavgArray); 

		var = new Variable(ncfile, null, null, "T_MAX");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Maximum temperature during the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(tmaxArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("T_MAX", tmaxArray); 
		// NCdump.printArray(tmaxArray, "T_DAILY_MAX", System.out, null); 

		var = new Variable(ncfile, null, null, "T_MIN");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Minimum temperature during the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(tminArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("T_MIN", tminArray); 

		var = new Variable(ncfile, null, null, "P_CALC");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Total amount of precipitation recorded during the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "milimeters"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(pArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("P_CALC", pArray); 

		var = new Variable(ncfile, null, null, "SOLARAD");
		var.setDimensions(dims);
		var.setDataType(DataType.INT);
		var.addAttribute(new Attribute("long_name", "Average solar radiation for the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "watts/meter^2"));
		var.addAttribute(new Attribute("_FillValue", "-9999"));
		var.setCachedData(solArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOLARAD", solArray); 

		ivar = new Variable(ncfile, null, null, "SOLARAD_FLAG");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("long_name", "QC flag for solar radiation."));
		ivar.addAttribute(new Attribute("coordinates", "time lat lon"));
		ivar.addAttribute(new Attribute("_FillValue", "9"));
		ivar.setCachedData(sol_qcArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("SOLARAD_FLAG", sol_qcArray); 
		// NCdump.printArray(sol_qcArray, "SOLARAD_FLAG", System.out, null);

		var = new Variable(ncfile, null, null, "SOLARAD_MAX");
		var.setDimensions(dims);
		var.setDataType(DataType.INT);
		var.addAttribute(new Attribute("long_name", "Maximum solar radiation for the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "watts/meter^2"));
		var.addAttribute(new Attribute("_FillValue", "-9999"));
		var.setCachedData(sol_maxArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOLARAD_MAX", sol_maxArray); 

		ivar = new Variable(ncfile, null, null, "SOLARAD_MAX_FLAG");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("long_name", "QC flag for solar radiation maximum."));
		ivar.addAttribute(new Attribute("coordinates", "time lat lon"));
		ivar.addAttribute(new Attribute("_FillValue", "9"));
		ivar.setCachedData(sol_max_qcArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("SOLARAD_MAX_FLAG", sol_max_qcArray); 
		// NCdump.printArray(sol_max_qcArray, "SOLARAD_MAX_FLAG", System.out, null);

		var = new Variable(ncfile, null, null, "SOLARAD_MIN");
		var.setDimensions(dims);
		var.setDataType(DataType.INT);
		var.addAttribute(new Attribute("long_name", "Minimum solar radiation for the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "watts/meter^2"));
		var.addAttribute(new Attribute("_FillValue", "-9999"));
		var.setCachedData(sol_minArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOLARAD_MIN", sol_minArray); 

		ivar = new Variable(ncfile, null, null, "SOLARAD_MIN_FLAG");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("long_name", "QC flag for solar radiation minimum."));
		ivar.addAttribute(new Attribute("coordinates", "time lat lon"));
		ivar.addAttribute(new Attribute("_FillValue", "9"));
		ivar.setCachedData(sol_min_qcArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("SOLARAD_MIN_FLAG", sol_min_qcArray); 
		// NCdump.printArray(sol_max_qcArray, "SOLARAD_MAX_FLAG", System.out, null);

		var = new Variable(ncfile, null, null, "SUR_TEMP");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average surface temperature for the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(surtArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SUR_TEMP", surtArray); 
		// NCdump.printArray(surtArray, "SUR_TEMP", System.out, null); 

		ivar = new Variable(ncfile, null, null, "SUR_TEMP_FLAG");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("long_name", "QC flag for surface temperature."));
		ivar.addAttribute(new Attribute("coordinates", "time lat lon"));
		ivar.addAttribute(new Attribute("_FillValue", "9"));
		ivar.setCachedData(surt_qcArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("SUR_TEMP_FLAG", surt_qcArray); 
		// NCdump.printArray(surt_qcArray, "SUR_TEMP_FLAG", System.out, null);

		var = new Variable(ncfile, null, null, "SUR_TEMP_MAX");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Maximum surface temperature for the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(surtmaxArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SUR_TEMP_MAX", surtmaxArray); 
		// NCdump.printArray(surtmaxArray, "SUR_TEMP_MAX", System.out, null); 

		ivar = new Variable(ncfile, null, null, "SUR_TEMP_MAX_FLAG");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("long_name", "QC flag for surface temperature maximum."));
		ivar.addAttribute(new Attribute("coordinates", "time lat lon"));
		ivar.addAttribute(new Attribute("_FillValue", "9"));
		ivar.setCachedData(surtmax_qcArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("SUR_TEMP_MAX_FLAG", surtmax_qcArray); 
		// NCdump.printArray(surtmax_qcArray, "SUR_TEMP_MAX_FLAG", System.out, null);

		var = new Variable(ncfile, null, null, "SUR_TEMP_MIN");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Minimum surface temperature for the hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(surtminArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SUR_TEMP_MIN", surtminArray); 
		// NCdump.printArray(surtminArray, "SUR_TEMP_MIN", System.out, null); 

		ivar = new Variable(ncfile, null, null, "SUR_TEMP_MIN_FLAG");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("long_name", "QC flag for surface temperature minimum."));
		ivar.addAttribute(new Attribute("coordinates", "time lat lon"));
		ivar.addAttribute(new Attribute("_FillValue", "9"));
		ivar.setCachedData(surtmin_qcArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("SUR_TEMP_MIN_FLAG", surtmin_qcArray); 
		// NCdump.printArray(surtmin_qcArray, "SUR_TEMP_MIN_FLAG", System.out, null);

		var = new Variable(ncfile, null, null, "RH_HR_AVG");
		var.setDimensions(dims);
		var.setDataType(DataType.INT);
		var.addAttribute(new Attribute("long_name", "Average relative humidity for hour."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999"));
		var.setCachedData(rhavgArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("RH_HR_AVG", rhavgArray); 

		ivar = new Variable(ncfile, null, null, "RH_HR_AVG_FLAG");
		ivar.setDimensions(dims);
		ivar.setDataType(DataType.INT);
		ivar.addAttribute(new Attribute("long_name", "QC flag for average relative humidity."));
		ivar.addAttribute(new Attribute("coordinates", "time lat lon"));
		ivar.addAttribute(new Attribute("_FillValue", "9"));
		ivar.setCachedData(rhavg_qcArray, false);
		ncfile.addVariable( null, ivar);
		varList.add(ivar);
		resultMap.put("RH_HR_AVG_FLAG", rhavg_qcArray); 
		// NCdump.printArray(rhavg_qcArray, "RH_HR_AVG_FLAG", System.out, null);

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_5");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire hour at 5 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm5Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_5", sm5Array); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_10");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire hour at 10 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm10Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_10", sm10Array); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_20");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire hour at 20 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm20Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_20", sm20Array); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_50");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire hour at 50 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm50Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_50", sm50Array); 

		var = new Variable(ncfile, null, null, "SOIL_MOISTURE_100");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil moisture during the entire hour at 100 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "percents"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(sm100Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_MOISTURE_100", sm100Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_5");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire hour at 5 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st5Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_5", st5Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_10");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire hour at 10 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st10Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_10", st10Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_20");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire hour at 20 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st20Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_20", st20Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_50");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire hour at 50 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st50Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_50", st50Array); 

		var = new Variable(ncfile, null, null, "SOIL_TEMP_100");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "Average soil temperature during the entire hour at 100 cm below the surface."));
		var.addAttribute(new Attribute("coordinates", "time lat lon"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("_FillValue", "-9999.0"));
		var.setCachedData(st100Array, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SOIL_TEMP_100", st100Array); 

		ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.4"));
		ncfile.addAttribute(null, new Attribute("CF:featureType", "timeSeries"));
		ncfile.addAttribute(null, new Attribute("title", "Climate Reference Network (CRN)"));
		ncfile.addAttribute(null, new Attribute("description", "Climate Reference Network hourly data"));
		String s0=utc_date[0].substring(0,4)+"-"+utc_date[0].substring(4,6)+"-"+utc_date[0].substring(6)+
		" "+utc_time[0].substring(0,2)+":"+utc_time[0].substring(2);
		ncfile.addAttribute(null, new Attribute("time_coverage_start", s0));
		String s1=utc_date[lines-1].substring(0,4)+"-"+utc_date[lines-1].substring(4,6)+"-"+
		utc_date[lines-1].substring(6)+" "+utc_time[lines-1].substring(0,2)+":"+utc_time[lines-1].substring(2);
		ncfile.addAttribute(null, new Attribute("time_coverage_end", ""+s1));



		//System.out.println("VARS="+varList.size());
		// for (int j=0; j< varList.size(); j++) {
		//System.out.println("j="+(j+1)+" NAME: "+varList.get(j).getName()); 
		// }
		System.out.println("RESULT="+resultMap.size());

		ncfile.finish();

	}
	//------------------------------------------------------------------------------------------------------------


	/** Calculate the number of lines in input ISD data file  */
	int linecount(ucar.unidata.io.RandomAccessFile raf) throws IOException {
		long pos = raf.getFilePointer();
		raf.seek(0);
		int sz=(int)raf.length();	
		int lines = 0;

		while (pos < sz) {  
			try { 				
				if (raf.readByte() == '\n') {  
					lines++; pos = raf.getFilePointer();}
			} catch (java.io.EOFException e) { e.printStackTrace();
			break;
			}
		}	raf.seek(raf.getFilePointer());			
		return lines;
	}
	//-------------------------------------------------------------------------

	/** 
	 * Calculate date/time as number of minutes from 01.01.1970 00:00:00.
	 * @param yymmdd  array of strings "yyyymmdd". 
	 *                Length of array = number of lines of input datafile. 
	 * @return      date as number of minutes from 01.01.1970 00:00:00
	 */

	public int[] getMinutes(String[] yymmdd) {
		Calendar cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int[] year=new int[yymmdd.length];
		int[] month=new int[yymmdd.length];
		int[] day=new int[yymmdd.length];
		int[] val=new int[yymmdd.length];
		for (int j=0; j < yymmdd.length; j++) {
			year[j]=Integer.parseInt(yymmdd[j].substring(0,4).trim());
			month[j]=Integer.parseInt(yymmdd[j].substring(4,6).trim());
			day[j]=Integer.parseInt(yymmdd[j].substring(6).trim());
			cal.set(year[j], month[j]-1, day[j]); 
			long tt=cal.getTime().getTime();
			val[j]=(int) (tt/1000/60);
		}
		return val;
	}
	//-----------------------------------------------------------------------------

	/** 
	 * Calculate date/time as number of minutes from 01.01.1970 00:00:00.
	 * @param yymmdd  array of strings "yyyymmdd". 
	 *                Length of array = number of lines of input datafile. 
	 * @param hhmm    array of strings "hhmm". 
	 *                Length of array = number of lines of input datafile. 
	 * @return      date as number of minutes from 01.01.1970 00:00:00
	 */

	public int[] getMinutes(String[] yymmdd, String[] hhmm) {
		Calendar cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int[] year=new int[yymmdd.length];
		int[] month=new int[yymmdd.length];
		int[] day=new int[yymmdd.length];
		int[] hour=new int[yymmdd.length];
		int[] minute=new int[yymmdd.length];
		int[] val=new int[yymmdd.length];
		for (int j=0; j < yymmdd.length; j++) {
			year[j]=Integer.parseInt(yymmdd[j].substring(0,4).trim());
			month[j]=Integer.parseInt(yymmdd[j].substring(4,6).trim());
			day[j]=Integer.parseInt(yymmdd[j].substring(6).trim());
			hour[j]=Integer.parseInt(hhmm[j].substring(0,2));
			minute[j]=Integer.parseInt(hhmm[j].substring(2));
			cal.set(year[j], month[j]-1, day[j], hour[j]-1, minute[j]-1, 0);  
			long tt=cal.getTime().getTime();
			val[j]=(int) (tt/1000/60);
		}
		return val;
	}
	//-----------------------------------------------------------------------------

	public void close() throws IOException { /*this.raf.close();*/ }

	public String getDetailInfo() { return "CRN IOServiceProvider."; }
	public String getFileTypeId(){ return "CRN"; }
	public String getFileTypeDescription(){ return " "; }

	/** A way to communicate arbitrary information to an iosp. */
	public Object sendIospMessage(Object message) {
		return super.sendIospMessage(message);
	}

	/**
	 * Checks to see if an CRN file is indeed valid
	 * Read a name of input file  and define if there is
	 * the "CRN" sequence.
	 *
	 * @param raf  the file to determine ownership
	 */
	public boolean isValidFile(final RandomAccessFile raf) throws IOException {
		String filename=raf.getLocation();
		return (filename.indexOf("CRN") > -1 && filename.endsWith(".txt"));     
	}
	//-----------------------------------------------------------------------------------------------------

	/** Read data from a top level Variable and return a memory resident Array */
	public Array readData(ucar.nc2.Variable v2, Section wantSection) 
	throws IOException, InvalidRangeException {
		String varName=v2.getName();
		Array arr=resultMap.get(varName);       
		return arr.section(wantSection.getRanges());
	}

	/** Read data from a Variable that is nested in one or more Structures */
	public Array readNestedData(Variable v2, List section) throws IOException, InvalidRangeException {
		return null;
	}

	/** Extend the file if needed in a way that is compatible with the current metadata */
	public boolean syncExtend() throws IOException {
		return false;
	}

	/** Check if file has changed, and reread metadata if needed */
	public boolean sync() throws IOException {
		return false;
	}

	/** A way to communicate arbitrary information to an iosp */
	public void setSpecial(Object special) {  }
	public String toStringDebug(Object o) {
		return null;
	}

}
