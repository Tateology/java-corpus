package gov.noaa.ncdc.iosp.station;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

/** IOService Provider for  Integrated Surface Database (ISD) data */

public class IsdIOServiceProvider extends AbstractIOServiceProvider {
	static final Logger logger=Logger.getLogger("gov.noaa.ncdc.iosp.station.IsdIOServiceProvider.class");
	static final String LOG_PROPERTIES_FILE="log4j.properties";
	static Properties prop=null;
	static Properties logProperties=null;
	ucar.unidata.io.RandomAccessFile raf;
	Map<String, Array> resultMap=new HashMap<String, Array> (); 

	//	private static String config="isd-iosp-config.xml"; 
	private static URL configURL = IsdIOServiceProvider.class.getResource("/gov/noaa/ncdc/iosp/station/isd-iosp-config.xml"); 
	//	static String infile=null;

	public static void main(String[] args) { 
		try {
			// get properties from "app.properties"
			//			prop=new Properties();
			//			prop.load(new FileInputStream(new File("app.properties")));
			//			logProperties=new Properties();
			//			logProperties.load(new FileInputStream(LOG_PROPERTIES_FILE));
			//			PropertyConfigurator.configure(logProperties);
			//			logger.info("---IsdIOServiceProvider---");
			//			String infile=prop.getProperty("src_dir");
			String infile = "E:\\work\\station-data\\911650-22536-2001";
			logger.info("Input file: "+infile);
			//			String outfile=prop.getProperty("target_dir"); 
			String outfile = "E:\\work\\station-data\\911650-22536-2001.nc";
			logger.info("Output file: "+outfile); 
			//			config=prop.getProperty("xml_dir");

			NetcdfFile.registerIOProvider(IsdIOServiceProvider.class);
			NetcdfFile ncfile=NetcdfFile.open(infile);
			System.out.println("ncfile = \n"+ncfile);

			NetcdfFile outFile = ucar.nc2.FileWriter.writeToFile(ncfile, outfile, false, 0);                
			ncfile.close();
			outFile.close();

		} catch (Exception e) { logger.info("!!! "+e.toString()+" !!!\n");
		System.out.println("MAIN!!!   "+e.toString());
		e.printStackTrace(); }
	}
	//-------------------------------------------------------------------------------


	/**  Open existing file, and populate ncfile with it. */

	public void open(ucar.unidata.io.RandomAccessFile raf, ucar.nc2.NetcdfFile ncfile,
			ucar.nc2.util. CancelTask cancelTask) throws IOException {

		//----------------------------------------------------------------------
		/* This part only for PerformanceTest taken from main()
    // get properties from "app.properties"
    prop=new Properties();
    prop.load(new FileInputStream(new File("app.properties")));
    logProperties=new Properties();
    logProperties.load(new FileInputStream(LOG_PROPERTIES_FILE));
    PropertyConfigurator.configure(logProperties);
    logger.info("---IsdIOServiceProvider---");
    infile=prop.getProperty("src_dir");  
    logger.info("Input file: "+infile);
    String outfile=prop.getProperty("target_dir"); 
    logger.info("Output file: "+outfile); 
    config=prop.getProperty("xml_dir");
		 */   
		//-------------------------------------------------------------------  

		// Create map: "keys"=identifiers, "values"=array of lengths of fields of the identifier     
		Map<String,String[]> formatMap=getFormatMap();
		/*         Set s=formatMap.entrySet();       
         Iterator it=s.iterator();
         while (it.hasNext()) {
          Map.Entry m=(Map.Entry)it.next();
          String key=(String)m.getKey();
          String val[]=(String[])m.getValue();
System.out.print(""+key+": ");
for (int j=0; j<val.length; j++) { System.out.print(val[j]+","); }
System.out.println();
         }
		 */

		// Create map: "keys"=yyyymmdd+hhmm+src_flag+identifier, "values"=FieldValue object for this identifier
		Map<String,FieldValue> rawMap=new HashMap<String,FieldValue> ();
		// Create set which contains all of the identifiers in input file
		Set<String> idfSet=new HashSet<String> ();
		// Create set which contains 'yyyymmdd+hhmm+src_flag'
		Set<String> dh=new HashSet<String> ();
		Map<String, String> dir_angleMap=new HashMap<String, String> ();
		Map<String, String> dir_angle_qcMap=new HashMap<String, String> ();
		Map<String, String> dir_angle_typeMap=new HashMap<String, String> ();
		Map<String, String> speed_rateMap=new HashMap<String, String> ();
		Map<String, String> speed_rate_qcMap=new HashMap<String, String> ();
		Map<String, String> ceiling_heightMap=new HashMap<String, String> ();
		Map<String, String> ceiling_height_qcMap=new HashMap<String, String> ();
		Map<String, String> ceiling_dtMap=new HashMap<String, String> ();
		Map<String, String> ceiling_cavokMap=new HashMap<String, String> ();
		Map<String, String> horizontal_distMap=new HashMap<String, String> ();
		Map<String, String> horizontal_dist_qcMap=new HashMap<String, String> ();
		Map<String, String> horizontal_dist_varMap=new HashMap<String, String> ();
		Map<String, String> horizontal_dist_var_qcMap=new HashMap<String, String> ();
		Map<String, String> air_tempMap=new HashMap<String, String> ();
		Map<String, String> air_temp_qcMap=new HashMap<String, String> ();
		Map<String, String> dew_tempMap=new HashMap<String, String> ();
		Map<String, String> dew_temp_qcMap=new HashMap<String, String> ();
		Map<String, String> sea_level_prsMap=new HashMap<String, String> ();
		Map<String, String> sea_level_qcMap=new HashMap<String, String> ();

		// Define the number of lines of input file          
		int lines=linecount(raf); //System.out.println("LINES="+lines);
		logger.info("Input file lines="+lines+"\n");

		String[] date=new String[lines];
		String[] hm=new String[lines];
		String[] src_flag=new String[lines];
		String[] dir_angle=new String[lines];
		String[] dir_angle_qc=new String[lines];
		String[] dir_angle_type=new String[lines];
		String[] speed_rate=new String[lines];
		String[] speed_rate_qc=new String[lines];
		String[] ceiling_height=new String[lines];
		String[] ceiling_height_qc=new String[lines];
		String[] ceiling_dt=new String[lines];
		String[] ceiling_cavok=new String[lines];
		String[] horizontal_dist=new String[lines];
		String[] horizontal_dist_qc=new String[lines];
		String[] horizontal_dist_var=new String[lines];
		String[] horizontal_dist_var_qc=new String[lines];
		String[] air_temp=new String[lines];
		String[] air_temp_qc=new String[lines];
		String[] dew_temp=new String[lines];
		String[] dew_temp_qc=new String[lines];
		String[] sea_level_prs=new String[lines];
		String[] sea_level_qc=new String[lines];
		String catalog_id="";
		String wban_id="";
		float lat=0.0f;
		float lon=0.0f;
		float elev=0.0f;
		raf.seek(0); 

		for (int i = 0; i < lines; i++) {
			int len=0;  int pos0=0;
			String q = raf.readLine(); //System.out.println("i="+i+" "+q);
			// read CONTROL SECTION
			int add_len=Integer.parseInt(q.substring(0,4));
			catalog_id=q.substring(4,10); 
			wban_id=q.substring(10,15);   
			date[i]=q.substring(15,23);  //yyyymmdd
			hm[i]=q.substring(23,27);    //hhmm
			src_flag[i]=q.substring(27,28);
			String ff=date[i]+hm[i]+src_flag[i];
			dh.add(ff);
			lat=Float.parseFloat(q.substring(28,34))*0.001f;       
			lon=Float.parseFloat(q.substring(34,41))*0.001f;
			elev=Float.parseFloat(q.substring(46,51)); 

			// read <section type='mandatory'>
			dir_angle[i]=q.substring(60,63);
			dir_angleMap.put(ff, dir_angle[i]);
			dir_angle_qc[i]=q.substring(63,64);
			dir_angle_qcMap.put(ff, dir_angle_qc[i]);
			dir_angle_type[i]=q.substring(64,65);
			dir_angle_typeMap.put(ff, dir_angle_type[i]);
			speed_rate[i]=q.substring(65,69);
			speed_rateMap.put(ff, speed_rate[i]);
			speed_rate_qc[i]=q.substring(69,70);
			speed_rate_qcMap.put(ff, speed_rate_qc[i]);
			ceiling_height[i]=q.substring(70,75);
			ceiling_heightMap.put(ff, ceiling_height[i]);
			ceiling_height_qc[i]=q.substring(75,76);
			ceiling_height_qcMap.put(ff, ceiling_height_qc[i]);
			ceiling_dt[i]=q.substring(76,77);
			ceiling_dtMap.put(ff, ceiling_dt[i]);
			ceiling_cavok[i]=q.substring(77,78);
			ceiling_cavokMap.put(ff, ceiling_cavok[i]);
			horizontal_dist[i]=q.substring(78,84);
			horizontal_distMap.put(ff, horizontal_dist[i]);
			horizontal_dist_qc[i]=q.substring(84,85);
			horizontal_dist_qcMap.put(ff, horizontal_dist_qc[i]);
			horizontal_dist_var[i]=q.substring(85,86);
			horizontal_dist_varMap.put(ff, horizontal_dist_var[i]);
			horizontal_dist_var_qc[i]=q.substring(86,87);
			horizontal_dist_var_qcMap.put(ff, horizontal_dist_var_qc[i]);
			if (q.charAt(87) == '+') { air_temp[i]=q.substring(88,92);
			} else { air_temp[i]=q.substring(87,92); }
			air_tempMap.put(ff, air_temp[i]);
			air_temp_qc[i]=q.substring(92,93); 
			air_temp_qcMap.put(ff, air_temp_qc[i]);
			if (q.charAt(93) == '+') { dew_temp[i]=q.substring(94,98); 
			} else { dew_temp[i]=q.substring(93,98); }
			dew_tempMap.put(ff, dew_temp[i]);
			dew_temp_qc[i]=q.substring(98,99); 
			dew_temp_qcMap.put(ff, dew_temp_qc[i]);
			sea_level_prs[i]=q.substring(99,104);
			sea_level_prsMap.put(ff, sea_level_prs[i]);
			sea_level_qc[i]=q.substring(104,105);
			sea_level_qcMap.put(ff, sea_level_qc[i]);

			// Separate ADDITIONAL SECTION
			String qd=q; 
			pos0=0;
			String[] qq=new String[2];
			String[] qqq=new String[2];
			if (q.indexOf("REM") > -1) { 
				qq=q.split("REM"); 
			} else { 
				// logger.info("Line "+i+" time:"+date[i]+hm[i]+" - NO REMARKS SECTION");
				if (q.indexOf("EQD") > -1) { 
					qq=q.split("EQD");
				} else { 
					if (q.indexOf("QNN") > -1) { 
						qq=q.split("QNN");
					} else { 
						qq[0]=q; 
						//    logger.info("Line "+i+" time:"+date[i]+hm[i]+" - NO ELEMENT QUALITY DATA SECTION");
					} 
				}          
			}
			if (qq[0].indexOf("ADD") > -1) { 
				qqq=qq[0].split("ADD");
				qd=qqq[1]; 
			} else { 
				//  logger.info("Line "+i+" time:"+date[i]+hm[i]+" - NO ADDITIONAL DATA SECTION");
				qd=qq[0]; 
				// There is no ADDITIONAL SECTION 
				if (qd.length() == q.length()) continue; }   
			len=qd.length();
			//System.out.println("LINE="+(i+1)+" LEN="+len+"  "+qd);
			if (len == 105 | len == q.length()) {
				// logger.info("Line "+i+" time:"+date[i]+hm[i]+" - total length="+len);
				continue;
			}

			while (pos0 <len) {  //System.out.println("OUT: pos0="+pos0);
				String idf=qd.substring(pos0,pos0+3); pos0=pos0+3; 
				idfSet.add(idf);
				// Read identifier and define its lengths of fields
				String[] ip=(String[])formatMap.get(idf); 
				if (ip == null) { 
					//  logger.info("Line "+i+" time:"+date[i]+hm[i]+" : idf="+idf+" - wrong identifier");
					idfSet.remove(idf); 
					break;
				}

				String[] temp=new String[ip.length]; 
				// Read data corresponding to the identier
				for (int j=0; j<ip.length; j++) { 
					String tt=qd.substring(pos0,pos0+Integer.parseInt(ip[j]));
					pos0=pos0+Integer.parseInt(ip[j]); 
					temp[j]=tt;               
				}  
				FieldValue fv=new FieldValue(idf, temp, ip.length);
				rawMap.put(date[i]+hm[i]+src_flag[i]+idf, fv);  
			}//---------- while(pos0<len)---------------------
			/*
         Set s=rawMap.entrySet();
         Iterator it=s.iterator();
         while (it.hasNext()) {
          Map.Entry m=(Map.Entry)it.next();
          String key=(String)m.getKey();
          FieldValue fv=(FieldValue)m.getValue();
System.out.println(""+key+": "+fv.toString());
         } 
			 */

		}//------------ for ( readLine ----------------------------

		// System.out.println("RAW_SIZE="+rawMap.size()+" IDF_SET="+idfSet.size());
		//logger.info("idfSet.size="+idfSet.size());

		//-----------------------------------------------------------------------------

		// Define Dimensions, Variables, Attributes for NetCDF file
		List<Dimension> dims=new ArrayList<Dimension> ();
		List<Dimension> dimst=new ArrayList<Dimension> ();
		List<Dimension> dm=new ArrayList<Dimension> ();
		List<Dimension> wdm=new ArrayList<Dimension> ();
		List<Dimension> od=new ArrayList<Dimension> ();
		Dimension obsDim=new Dimension("obs", dh.size(), true);
		Dimension stnDim=new Dimension("station", 1, true);
		Dimension nameDim=new Dimension("name_strlen", 6, true);
		Dimension wDim=new Dimension("info_strlen", 5, true);
		dims.add(stnDim);  dims.add(obsDim);
		dimst.add(stnDim);
		dm.add(stnDim); dm.add(nameDim);
		wdm.add(stnDim); wdm.add(wDim);

		ncfile.addDimension(null, obsDim);
		ncfile.addDimension(null, stnDim);
		ncfile.addDimension(null, nameDim);
		ncfile.addDimension(null, wDim);
		int[] shape = new int[] {1, dh.size()};
		ArrayInt.D2 intArr=null; 
		ArrayChar charArr=null;
		Index iix=null;

		int idfsize=idfSet.size();   System.out.println("IDF_SET="+idfSet.size());

		Variable var=null;   
		Variable[] tmvar=null;
		Variable[] chvar=null;
		ArrayList<Variable> varList=new ArrayList<Variable>();
		String begDate="", endDate="";
		String nm="";
		int var_count=0;
		// Work with data of ADDITIONAL SECTION
		// Iterate over identifier set    
		Iterator its=idfSet.iterator();
		while (its.hasNext()) {
			String theIdf=(String)its.next(); //System.out.println("idf: "+theIdf); 
			String[] pp=(String[])formatMap.get(theIdf);
			// Define temporary map for "theIdf" identifier to held: "key"=yyyymmddhhmm,
			// "value"=data values of theIdf corresponding to this date/time 
			Map<String,String[]> tmp=new HashMap<String,String[]> ();
			Set entry=rawMap.entrySet();
			Iterator iter=entry.iterator();
			// Iterate over rawMap entries and select the entries with "theIdf" identifier
			while (iter.hasNext()) {
				Map.Entry m=(Map.Entry)iter.next();
				String key=(String)m.getKey();
				//if (key == null) System.out.println("!!! HERE key=NULL  idf="+theIdf+"  pp.length="+pp.length);
				FieldValue fv=(FieldValue)m.getValue();
				//if (fv == null) System.out.println("!!! HERE fv=NULL  idf="+theIdf+"  pp.length="+pp.length);
				if (key.substring(13).compareTo(theIdf) == 0) {
					tmp.put(key.substring(0,13), (String[])fv.getValues());
				}       
			}  
			// Define map which contains properties (attributes) of a field of theIdf identifier  
			Map<String,String> prop=new HashMap<String,String> ();
			// Each field corresponds to NetCDF variable
			//   Variable has numeric type (integer)
			tmvar=new Variable[pp.length];
			//   Variable has character type 
			chvar=new Variable[pp.length];

			String domain="";
			String[] ms=new String[pp.length];
			String[] sc=new String[pp.length];
			for (int j=0; j<pp.length; j++) { 
				prop=getPropertyMap(theIdf, j); 
				domain=prop.get("domain");
				// Describe Variable of character type
				if (domain.compareTo("specific-ascii") == 0) { 
					chvar[j]=new Variable(ncfile, null, null, prop.get("name"));
					chvar[j].setDataType(DataType.CHAR);
					Dimension lenDim=new Dimension(theIdf+"_strlen_"+j, Integer.parseInt(pp[j]), true);
					ncfile.addDimension(null, lenDim);
					od.add(stnDim); od.add(obsDim); od.add(lenDim);
					chvar[j].setDimensions(od);                 
					chvar[j].addAttribute(new Attribute("coordinates", "time lat lon alt"));        
					chvar[j].addAttribute(new Attribute("description", prop.get("description")));  
					ms[j]=prop.get("missing");       //System.out.println("j="+j+" ms="+ms[j]);         
					if (ms[j] == null) { StringBuffer sb=new StringBuffer();
					for (int ku=0; ku < Integer.parseInt(pp[j]); ku++) sb.append("W");
					ms[j]=sb.toString();
					} 
					chvar[j].addAttribute(new Attribute("_FillValue", ms[j]));              
					ncfile.addVariable(null, chvar[j]);
					varList.add(chvar[j]);   
					var_count++;  
					od.clear(); 
				} else {
					// Describe Variable of numerical type
					tmvar[j]=new Variable(ncfile, null, null, prop.get("name"));
					tmvar[j].setDataType(DataType.INT); 
					tmvar[j].setDimensions(dims);
					if (prop.get("units") != null) {
						tmvar[j].addAttribute(new Attribute("units", prop.get("units"))); 
					}
					tmvar[j].addAttribute(new Attribute("coordinates", "time lat lon alt"));        
					tmvar[j].addAttribute(new Attribute("description", prop.get("description")));       
					ms[j]=prop.get("missing");       //System.out.println("j="+j+" ms="+ms[j]);
					sc[j]=prop.get("scalingFactor");//System.out.println("j="+j+" sc="+sc[j]);        
					if (sc[j] == null) sc[j]="1";
					tmvar[j].addAttribute(new Attribute("scalingFactor", sc[j]));
					if (ms[j] == null) { StringBuffer sb=new StringBuffer();
					for (int ku=0; ku < Integer.parseInt(pp[j]); ku++) sb.append("9");
					ms[j]=sb.toString();
					} 
					if (ms[j].charAt(0) == '+') ms[j]=ms[j].substring(1);
					tmvar[j].addAttribute(new Attribute("_FillValue", ms[j]));      
					ncfile.addVariable(null, tmvar[j]);
					varList.add(tmvar[j]);             
					var_count++;  //System.out.println("VAR_COUNT="+var_count);
					od.clear(); 
				}                    
				//--------end for (j)    -------------------     

				// Define temporary map where "key"=yyyymmddhhmm+src_flag+idf ,
				// "value'=missing ("_FillValue") value for  the Variable										
				Map<String,String> missing_tmp=new HashMap<String,String> ();
				String[] date_time=new String[lines];

				for (int ik=0; ik<lines; ik++) { 
					date_time[ik]=date[ik]+hm[ik];
					missing_tmp.put(date_time[ik]+src_flag[ik], ms[j]); 
				}
				Set sett=tmp.entrySet();
				Iterator itt=sett.iterator();
				// Iterate over entries of missing_tmp map and put there data values
				// if they exist for theIdf identifier
				while (itt.hasNext()) {
					Map.Entry me=(Map.Entry)itt.next();
					String key=(String)me.getKey();
					String[] vv=(String[])me.getValue();
					missing_tmp.put(key, vv[j]);
				}
				// Sort missing_tmp by data_time
				SortedMap<String,String> sortTmp=new TreeMap(missing_tmp);

				// Prepare resultMap where "key"=name of Variable, "value"=Array of data of the Variable
				if (domain.compareTo("specific-ascii") == 0 & chvar[j] !=null) {
					int[] stu=new int[]{1, dh.size(), Integer.parseInt(pp[j])}; 
					charArr=new ArrayChar(stu);
					Index chIx=charArr.getIndex();
					Set st=sortTmp.entrySet();
					Iterator itr=st.iterator();
					int n=0;
					while (itr.hasNext()) {
						Map.Entry m=(Map.Entry)itr.next();
						String key=(String)m.getKey();
						String vl=(String)m.getValue();  
						if (vl.charAt(0) == '+') vl=vl.replace('+', ' ');
						char[] ca=vl.toCharArray();  
						for (int ku=0; ku<Integer.parseInt(pp[j]); ku++) { 
							charArr.setChar(chIx.set(0,n,ku), ca[ku]);
						}
						n++;
					} 
					chvar[j].setCachedData(charArr, false);
					resultMap.put(chvar[j].getName(), charArr);

				} else {
					intArr=(ArrayInt.D2)Array.factory(DataType.INT, shape); 
					iix=intArr.getIndex();
					Set st=sortTmp.entrySet();
					Iterator itr=st.iterator();
					int n=0;
					while (itr.hasNext()) {
						Map.Entry m=(Map.Entry)itr.next();
						String key=(String)m.getKey();
						String ff=(String)m.getValue();
						if (ff.charAt(0) == '+') ff=ff.replace('+','0');
						intArr.setInt(iix.set(0,n), Integer.parseInt(ff)); 
						n++;
					} 
					tmvar[j].setCachedData(intArr, false);   
					resultMap.put(tmvar[j].getName(), intArr);  
				}
			}//------- j ---------------------------------------------
			//if (theIdf.compareTo("AW1") == 0) { 
			// NCdump.printArray(charArr, "KA2_EXTREME-AIR-TEMPERATURE_guality_code", System.out, null); 
			//}          
		}    //-------- while (its  idfSet)----------------


		// Work with data of CONTROL SECTION
		int[] sh=new int[]{1}; int[] shh=new int[] {dh.size()};
		int[] nsh=new int[]{1,6};  int[] wsh=new int[]{1,5};

		ArrayFloat.D1 latArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
		ArrayFloat.D1 lonArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
		ArrayFloat.D1 altArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);

		ArrayInt.D2 timeArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index tIx=timeArray.getIndex();
		ArrayInt.D2 windArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index wIx=windArray.getIndex();
		ArrayFloat.D2 speedArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index spIx=speedArray.getIndex();
		ArrayInt.D2 ceilingArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index ceIx=ceilingArray.getIndex();
		ArrayInt.D2 horizontArray=(ArrayInt.D2)Array.factory(DataType.INT, shape);
		Index hIx=horizontArray.getIndex();
		ArrayFloat.D2 airArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index airIx=airArray.getIndex();
		ArrayFloat.D2 dewArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index dewIx=dewArray.getIndex();
		ArrayFloat.D2 seaArray=(ArrayFloat.D2)Array.factory(DataType.FLOAT, shape);
		Index seaIx=seaArray.getIndex();

		latArray.set(0, lat);
		lonArray.set(0, lon);
		altArray.set(0, elev);   

		String[] ymd=new String[dh.size()];
		String[] hrm=new String[dh.size()];
		String[] ds_flag=new String[dh.size()];  
		String[] wnd_dr=new String[dh.size()];                       //wind_direction_angle
		String[] wnd_dr_qc=new String[dh.size()];                  //wind_direction_angle_QC
		String[] wnd_dr_type=new String[dh.size()];               //wind_direction_angle_type_code
		String[] wnd_sp=new String[dh.size()];                       //wind_speed_rate
		String[] wnd_sp_qc=new String[dh.size()];                  //wind_speed_rate_QC
		String[] sky_h=new String[dh.size()];                          //sky_ceiling_height
		String[] sky_qc=new String[dh.size()];                        //sky_ceiling_height_QC
		String[] sky_dt=new String[dh.size()];                         //sky_ceiling_determination_code
		String[] sky_cavok=new String[dh.size()];                   //sky_ceiling_CAVOK_code
		String[] visb_d=new String[dh.size()];                         //visibility_distance
		String[] visb_d_qc=new String[dh.size()];                    //visibility_distance_QC
		String[] var_c=new String[dh.size()];                           //visibility_variability_code
		String[] var_c_qc=new String[dh.size()];                      //visibility_variability_code_QC
		String[] air_t=new String[dh.size()];                             //air_temperature
		String[] air_t_qc=new String[dh.size()];                        //air_temperature_QC
		String[] dewp=new String[dh.size()];                           //dew_point_temperature
		String[] dewp_qc=new String[dh.size()];                      //dew_point_temperature_QC
		String[] sealv=new String[dh.size()];                            //sea_level_pressure
		String[] sealv_qc=new String[dh.size()];                       //sea_level_pressure_QC
		int io=0;
		for (Iterator iter=dh.iterator(); iter.hasNext(); ) {
			String tmp_date=(String)iter.next();
			ymd[io]=tmp_date.substring(0,8);
			hrm[io]=tmp_date.substring(8, 12);
			ds_flag[io]=tmp_date.substring(12);
			wnd_dr[io]=(String)dir_angleMap.get(tmp_date);
			wnd_dr_qc[io]=(String)dir_angle_qcMap.get(tmp_date);
			wnd_dr_type[io]=(String)dir_angle_typeMap.get(tmp_date);
			wnd_sp[io]=(String)speed_rateMap.get(tmp_date);
			wnd_sp_qc[io]=(String)speed_rate_qcMap.get(tmp_date);
			sky_h[io]=(String)ceiling_heightMap.get(tmp_date);
			sky_qc[io]=(String)ceiling_height_qcMap.get(tmp_date);
			sky_dt[io]=(String)ceiling_dtMap.get(tmp_date);
			sky_cavok[io]=(String)ceiling_cavokMap.get(tmp_date);
			visb_d[io]=(String)horizontal_distMap.get(tmp_date);
			visb_d_qc[io]=(String)horizontal_dist_qcMap.get(tmp_date);
			var_c[io]=(String)horizontal_dist_varMap.get(tmp_date);
			var_c_qc[io]=(String)horizontal_dist_var_qcMap.get(tmp_date);
			air_t[io]=(String)air_tempMap.get(tmp_date);
			air_t_qc[io]=(String)air_temp_qcMap.get(tmp_date);
			dewp[io]=(String)dew_tempMap.get(tmp_date);
			dewp_qc[io]=(String)dew_temp_qcMap.get(tmp_date);
			sealv[io]=(String)sea_level_prsMap.get(tmp_date);
			sealv_qc[io]=(String)sea_level_qcMap.get(tmp_date);
			io++;
		}

		int[] mints=getMinutes(ymd, hrm);
		for (int i=0; i<io; i++) {
			timeArray.setInt(tIx.set(0,i), mints[i]);
			windArray.setInt(wIx.set(0,i), Integer.parseInt(wnd_dr[i]));
			speedArray.setFloat(spIx.set(0,i), Float.parseFloat(wnd_sp[i])*0.1f);
			ceilingArray.setInt(ceIx.set(0,i), Integer.parseInt(sky_h[i]));
			horizontArray.setInt(hIx.set(0,i), Integer.parseInt(visb_d[i]));
			airArray.setFloat(airIx.set(0,i), Float.parseFloat(air_t[i])*0.1f);
			dewArray.setFloat(dewIx.set(0,i), Float.parseFloat(dewp[i])*0.1f);
			seaArray.setFloat(seaIx.set(0,i), Float.parseFloat(sealv[i])*0.1f);
		}

		Variable vv=new Variable(ncfile, null, null, "station_name");
		vv.setDimensions(dm);
		vv.setDataType(DataType.CHAR);
		vv.addAttribute( new Attribute("long_name", "FIXED-WEATHER-STATION USAF MASTER STATION CATALOG identifier"));
		vv.addAttribute( new Attribute("standard_name", "station_id"));
		ArrayChar nameArray=new ArrayChar(nsh);
		Index nameIx=nameArray.getIndex();
		char[] ca=catalog_id.toCharArray();
		for (int i=0; i<6; i++) { nameArray.setChar(nameIx.set(0,i), ca[i]); }
		vv.setCachedData(nameArray, false);
		ncfile.addVariable( null, vv);
		varList.add(vv);
		//NCdump.printArray(nameArray, "station_name", System.out, null);
		resultMap.put("station_name", nameArray); 

		Variable vvv=new Variable(ncfile, null, null, "station_info");
		vvv.setDimensions(wdm);
		vvv.setDataType(DataType.CHAR);
		vvv.addAttribute( new Attribute("long_name", "FIXED-WEATHER-STATION NCDC WBAN identifier"));
		ArrayChar wArray=new ArrayChar(wsh);
		Index wwIx=wArray.getIndex();
		char[] wb=wban_id.toCharArray();
		for (int i=0; i<5; i++) { wArray.setChar(wwIx.set(0,i), wb[i]); }
		vvv.setCachedData(wArray, false);
		ncfile.addVariable( null, vvv);
		varList.add(vvv);
		resultMap.put("station_info", wArray); 
		//NCdump.printArray(wArray, "station_info", System.out, null);


		var = new Variable(ncfile, null, null, "lat");
		var.setDimensions(dimst);
		var.setDataType(DataType.FLOAT);
		var.addAttribute( new Attribute("long_name", "GEOPHYSICAL-POINT-OBSERVATION latitude coordinate"));
		var.addAttribute( new Attribute("units", "degrees_north"));
		var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
		var.setCachedData(latArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("lat", latArray); 
		//NCdump.printArray(latArray, "latitude", System.out, null);

		var = new Variable(ncfile, null, null, "lon");
		var.setDimensions(dimst);
		var.setDataType(DataType.FLOAT);
		var.addAttribute( new Attribute("long_name", "GEOPHYSICAL-POINT-OBSERVATION longitude coordinate"));
		var.addAttribute( new Attribute("units", "degrees_east"));
		var.addAttribute( new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
		var.setCachedData(lonArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("lon", lonArray); 
		//NCdump.printArray(lonArray, "longitude", System.out, null);

		var = new Variable(ncfile, null, null, "alt");
		var.setDimensions(dimst);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("standard_name", "height"));
		var.addAttribute(new Attribute("long_name", "GEOPHYSICAL-POINT-OBSERVATION elevation dimension. The elevation of a GEOPHYSICAL-POINT-OBSERVATION relative to Mean Sea Level (MSL)."));
		var.addAttribute(new Attribute("units", "meters"));
		var.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Height.toString()));
		var.addAttribute(new Attribute("axis", "Z"));
		var.addAttribute(new Attribute("positive", "up"));
		var.setCachedData(altArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("alt", altArray); 
		//NCdump.printArray(altArray, "altitude", System.out, null);

		var = new Variable(ncfile, null, null, "time");
		var.setDimensions(dims);
		var.setDataType(DataType.INT);
		var.addAttribute(new Attribute("standard_name", "time"));
		var.addAttribute(new Attribute("long_name", "GEOPHYSICAL-POINT-OBSERVATION time. The time of a GEOPHYSICAL-POINT-OBSERVATION based on Coordinated Universal Time Code (UTC)."));
		var.addAttribute(new Attribute("units", "minutes since 1970-01-01 00:00:00Z"));
		var.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
		var.addAttribute(new Attribute("axis", "T"));
		var.setCachedData(timeArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("time", timeArray); 
		//NCdump.printArray(timeArray, "time", System.out, null);

		Variable vq=new Variable(ncfile, null, null, "data_source_flag");
		List<Dimension> ad=new ArrayList<Dimension> ();
		Dimension naDim=new Dimension("name_strlen_src", 1, true);
		ncfile.addDimension(null, naDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(naDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "GEOPHYSICAL-POINT-OBSERVATION data source flag"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The flag of a GEOPHYSICAL-POINT-OBSERVATION showing the source or combination of sources used in creating the observation."));  
		vq.addAttribute(new Attribute("_FillValue", "0"));
		int[] cs=new int[]{1, dh.size(), 1}; 
		ArrayChar srcArray=new ArrayChar(cs);
		Index sx=srcArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] dsrc=ds_flag[iu].toCharArray();
			srcArray.setChar(sx.set(0, iu, 0), dsrc[0]); 
		}
		vq.setCachedData(srcArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("data_source_flag", srcArray); 
		ad.clear(); 

		var = new Variable(ncfile, null, null, "WIND-OBSERVATION_direction_angle");
		var.setDimensions(dims);
		var.setDataType(DataType.INT);
		var.addAttribute(new Attribute("long_name", "The angle, measured in a clockwise direction, between true north and the direction from which the wind is blowing"));
		var.addAttribute(new Attribute("coordinates", "time lat lon alt"));
		var.addAttribute(new Attribute("units", "angular degrees"));
		var.addAttribute(new Attribute("scalingFactor", "1"));
		var.addAttribute(new Attribute("_FillValue", "999"));
		var.setCachedData(windArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("WIND-OBSERVATION_direction_angle", windArray); 

		vq=new Variable(ncfile, null, null, "WIND-OBSERVATION_direction_angle_qc");
		Dimension daqcDim=new Dimension("name_strlen_wda_qc", 1, true);
		ncfile.addDimension(null, daqcDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(daqcDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "WIND-OBSERVATION direction quality code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes a quality status of a reported WIND-OBSERVATION direction angle"));  
		vq.addAttribute(new Attribute("_FillValue", "W"));
		int[] swdqc=new int[]{1, dh.size(), 1}; 
		ArrayChar daqcArray=new ArrayChar(swdqc);
		Index iwqc=daqcArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=wnd_dr_qc[iu].toCharArray();
			daqcArray.setChar(iwqc.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(daqcArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("WIND-OBSERVATION_direction_angle_qc", daqcArray); 
		ad.clear(); 

		vq=new Variable(ncfile, null, null, "WIND-OBSERVATION_type");
		Dimension datDim=new Dimension("name_strlen_wda_t", 1, true);
		ncfile.addDimension(null, datDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(datDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "WIND-OBSERVATION type code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes the character of the WIND-OBSERVATION."));  
		vq.addAttribute(new Attribute("_FillValue", "9"));
		int[] st=new int[]{1, dh.size(), 1}; 
		ArrayChar datArray=new ArrayChar(st);
		Index iwt=datArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=wnd_dr_type[iu].toCharArray();
			datArray.setChar(iwt.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(datArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("WIND-OBSERVATION_type", daqcArray); 
		ad.clear(); 

		var = new Variable(ncfile, null, null, "WIND-OBSERVATION_speed_rate");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "The rate of horizontal travel of air past a fixed point."));
		var.addAttribute(new Attribute("coordinates", "time lat lon alt"));
		var.addAttribute(new Attribute("units", "meters per second"));
		var.addAttribute(new Attribute("scalingFactor", "1"));
		var.addAttribute(new Attribute("_FillValue", "999.9"));
		var.setCachedData(speedArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("WIND-OBSERVATION_speed_rate", speedArray);
		//NCdump.printArray(speedArray, "WIND-OBSERVATION_speed_rate", System.out, null);

		vq=new Variable(ncfile, null, null, "WIND-OBSERVATION_speed_qc");
		Dimension spqcDim=new Dimension("name_strlen_speed_qc", 1, true);
		ncfile.addDimension(null, spqcDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(spqcDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "WIND-OBSERVATION speed quality code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes a quality status of a reported WIND-OBSERVATION speed rate"));  
		vq.addAttribute(new Attribute("_FillValue", "W"));
		int[] spqc=new int[]{1, dh.size(), 1}; 
		ArrayChar spqcArray=new ArrayChar(spqc);
		Index ispqc=spqcArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=wnd_sp_qc[iu].toCharArray();
			spqcArray.setChar(ispqc.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(spqcArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("WIND-OBSERVATION_speed_qc", spqcArray); 
		ad.clear(); 

		var = new Variable(ncfile, null, null, "SKY-CONDITION-OBSERVATION_ceiling_height");
		var.setDimensions(dims);
		var.setDataType(DataType.INT);
		var.addAttribute(new Attribute("long_name", "The height above ground level (AGL) of the lowest cloud or obscuring phenomena layer aloft with 5/8 or more summation total sky cover, which may be predominantly opaque, or the verticalvisibility into a surface-based obstruction"));
		var.addAttribute(new Attribute("coordinates", "time lat lon alt"));
		var.addAttribute(new Attribute("units", "meters"));
		var.addAttribute(new Attribute("scalingFactor", "1"));
		var.addAttribute(new Attribute("_FillValue", "99999"));
		var.setCachedData(ceilingArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("SKY-CONDITION-OBSERVATION_ceiling_height", ceilingArray);

		vq=new Variable(ncfile, null, null, "SKY-CONDITION-OBSERVATION_ceiling_height_qc");
		Dimension skqcDim=new Dimension("name_strlen_ceiling_qc", 1, true);
		ncfile.addDimension(null, skqcDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(skqcDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "SKY CONDITION OBSERVATION ceiling height quality code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes a quality status of a reported ceiling height dimension."));  
		vq.addAttribute(new Attribute("_FillValue", "W"));
		int[] skqc=new int[]{1, dh.size(), 1}; 
		ArrayChar skqcArray=new ArrayChar(skqc);
		Index iskqc=skqcArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=sky_qc[iu].toCharArray();
			skqcArray.setChar(iskqc.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(skqcArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("SKY-CONDITION-OBSERVATION_ceiling_height_qc", skqcArray); 
		ad.clear(); 

		vq=new Variable(ncfile, null, null, "SKY-CONDITION-OBSERVATION_ceiling_determination_code");
		Dimension skdtDim=new Dimension("name_strlen_ceiling_dt", 1, true);
		ncfile.addDimension(null, skdtDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(skdtDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "SKY CONDITION OBSERVATION ceiling determination code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes the method used to determine the ceiling."));  
		vq.addAttribute(new Attribute("_FillValue", "9"));
		int[] skdt=new int[]{1, dh.size(), 1}; 
		ArrayChar skdtArray=new ArrayChar(skdt);
		Index iskdt=skdtArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=sky_dt[iu].toCharArray();
			skdtArray.setChar(iskdt.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(skdtArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("SKY-CONDITION-OBSERVATION_ceiling_determination_code", skdtArray); 
		ad.clear(); 

		vq=new Variable(ncfile, null, null, "SKY-CONDITION-OBSERVATION_ceiling_cavok");
		Dimension skcaDim=new Dimension("name_strlen_ceiling_cavok", 1, true);
		ncfile.addDimension(null, skcaDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(skcaDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "SKY CONDITION OBSERVATION ceiling CAVOK code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that represents whether the 'Ceiling And Visibility Okay' (CAVOK) condition has been reported."));  
		vq.addAttribute(new Attribute("_FillValue", "9"));
		int[] skca=new int[]{1, dh.size(), 1}; 
		ArrayChar skcaArray=new ArrayChar(skca);
		Index iskca=skcaArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=sky_cavok[iu].toCharArray();
			skcaArray.setChar(iskca.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(skcaArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("SKY-CONDITION-OBSERVATION_ceiling_cavok", skdtArray); 
		ad.clear(); 

		var = new Variable(ncfile, null, null, "VISIBILITY-OBSERVATION_distance");
		var.setDimensions(dims);
		var.setDataType(DataType.INT);
		var.addAttribute(new Attribute("long_name", "The horizontal distance at which an object can be seen and identified"));
		var.addAttribute(new Attribute("coordinates", "time lat lon alt"));
		var.addAttribute(new Attribute("units", "meters"));
		var.addAttribute(new Attribute("scalingFactor", "1"));
		var.addAttribute(new Attribute("_FillValue", "999999"));
		var.setCachedData(horizontArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("VISIBILITY-OBSERVATION_distance", horizontArray);

		vq=new Variable(ncfile, null, null, "VISIBILITY-OBSERVATION_distance_qc");
		Dimension distqcDim=new Dimension("name_strlen_vis_qc", 1, true);
		ncfile.addDimension(null, distqcDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(distqcDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "VISIBILITY-OBSERVATION distance quality code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes a quality status of a reported distance of a visibility observation"));  
		vq.addAttribute(new Attribute("_FillValue", "W"));
		int[] distqc=new int[]{1, dh.size(), 1}; 
		ArrayChar distqcArray=new ArrayChar(distqc);
		Index idistqc=distqcArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=visb_d_qc[iu].toCharArray();
			distqcArray.setChar(idistqc.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(distqcArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("VISIBILITY-OBSERVATION_distance_qc", distqcArray); 
		ad.clear(); 

		vq=new Variable(ncfile, null, null, "VISIBILITY-OBSERVATION_variability_code");
		Dimension distvarDim=new Dimension("name_strlen_vis_var", 1, true);
		ncfile.addDimension(null, distvarDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(distvarDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "VISIBILITY-OBSERVATION variability code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes whether or not the reported visibility is variable."));  
		vq.addAttribute(new Attribute("_FillValue", "9"));
		int[] distvar=new int[]{1, dh.size(), 1}; 
		ArrayChar distvarArray=new ArrayChar(distvar);
		Index idistvar=distvarArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=var_c[iu].toCharArray();
			distvarArray.setChar(idistvar.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(distvarArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("VISIBILITY-OBSERVATION_variability_code", distvarArray); 
		ad.clear(); 

		vq=new Variable(ncfile, null, null, "VISIBILITY-OBSERVATION_quality_variability_code");
		Dimension distvarqcDim=new Dimension("name_strlen_vis_var_qc", 1, true);
		ncfile.addDimension(null, distvarqcDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(distvarqcDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "VISIBILITY-OBSERVATION quality variability code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes a quality status of a reported VISIBILITY-OBSERVATION variability code."));  
		vq.addAttribute(new Attribute("_FillValue", "W"));
		int[] distvarqc=new int[]{1, dh.size(), 1}; 
		ArrayChar distvarqcArray=new ArrayChar(distvarqc);
		Index idistvarqc=distvarqcArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=var_c_qc[iu].toCharArray();
			distvarqcArray.setChar(idistvarqc.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(distvarqcArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("VISIBILITY-OBSERVATION_quality_variability_code", distvarqcArray); 
		ad.clear(); 

		var = new Variable(ncfile, null, null, "AIR-TEMPERATURE-OBSERVATION");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "The temperature of the air"));
		var.addAttribute(new Attribute("coordinates", "time lat lon alt"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("scalingFactor", "1"));
		var.addAttribute(new Attribute("_FillValue", "999.9"));
		var.setCachedData(airArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("AIR-TEMPERATURE-OBSERVATION", airArray);
		//NCdump.printArray(airArray, "AIR-TEMPERATURE-OBSERVATION", System.out, null);

		vq=new Variable(ncfile, null, null, "AIR-TEMPERATURE-OBSERVATION_quality_code");
		Dimension arDim=new Dimension("name_strlen_air_qc", 1, true);
		ncfile.addDimension(null, arDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(arDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "AIR-TEMPERATURE-OBSERVATION air temperature quality code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes a quality status of an AIR-TEMPERATURE-OBSERVATION."));  
		vq.addAttribute(new Attribute("_FillValue", "W"));
		int[] ar=new int[]{1, dh.size(), 1}; 
		ArrayChar arArray=new ArrayChar(ar);
		Index iar=arArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=air_t_qc[iu].toCharArray();
			arArray.setChar(iar.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(arArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("AIR-TEMPERATURE-OBSERVATION_quality_code", arArray); 
		ad.clear(); 

		var = new Variable(ncfile, null, null, "AIR-TEMPERATURE-OBSERVATION_dew_point");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "The temperature to which a given parcel of air must be cooled at constant pressure and water vapor content in order for saturation to occur"));
		var.addAttribute(new Attribute("coordinates", "time lat lon alt"));
		var.addAttribute(new Attribute("units", "degrees Celsius"));
		var.addAttribute(new Attribute("scalingFactor", "1"));
		var.addAttribute(new Attribute("_FillValue", "999.9"));
		var.setCachedData(dewArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("AIR-TEMPERATURE-OBSERVATION_dew_point", dewArray);
		//NCdump.printArray(dewArray, "AIR-TEMPERATURE-OBSERVATION_dew_point", System.out, null);

		vq=new Variable(ncfile, null, null, "AIR-TEMPERATURE-OBSERVATION_dew_point_quality_code");
		Dimension dpoDim=new Dimension("name_strlen_dpo_qc", 1, true);
		ncfile.addDimension(null, dpoDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(dpoDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "AIR-TEMPERATURE-OBSERVATION dew point quality code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes a quality status of the reported dew point temperature."));  
		vq.addAttribute(new Attribute("_FillValue", "W"));
		int[] dpos=new int[]{1, dh.size(), 1}; 
		ArrayChar dpoArray=new ArrayChar(dpos);
		Index idpo=dpoArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=dewp_qc[iu].toCharArray();
			dpoArray.setChar(idpo.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(dpoArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("AIR-TEMPERATURE-OBSERVATION_dew_point_quality_code", arArray); 
		ad.clear(); 

		var = new Variable(ncfile, null, null, "ATMOSPHERIC-PRESSURE-OBSERVATION_sea_level");
		var.setDimensions(dims);
		var.setDataType(DataType.FLOAT);
		var.addAttribute(new Attribute("long_name", "The air pressure relative to Mean Sea Level (MSL)"));
		var.addAttribute(new Attribute("coordinates", "time lat lon alt"));
		var.addAttribute(new Attribute("units", "hectopascals"));
		var.addAttribute(new Attribute("scalingFactor", "1"));
		var.addAttribute(new Attribute("_FillValue", "9999.9"));
		var.setCachedData(seaArray, false);
		ncfile.addVariable( null, var);
		varList.add(var);
		resultMap.put("ATMOSPHERIC-PRESSURE-OBSERVATION_sea_level", seaArray);

		vq=new Variable(ncfile, null, null, "ATMOSPHERIC-PRESSURE-OBSERVATION_sea_level_quality_code");
		Dimension slqcDim=new Dimension("name_strlen_sl_qc", 1, true);
		ncfile.addDimension(null, slqcDim);
		ad.add(stnDim); ad.add(obsDim);  ad.add(slqcDim);
		vq.setDimensions(ad);
		vq.setDataType(DataType.CHAR);   
		vq.addAttribute( new Attribute("long_name", "ATMOSPHERIC-PRESSURE-OBSERVATION sea level pressure quality code"));
		vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
		vq.addAttribute(new Attribute("description", "The code that denotes a quality status of the sea level pressure of an ATMOSPHERIC-PRESSURE-OBSERVATION."));  
		vq.addAttribute(new Attribute("_FillValue", "W"));
		int[] sls=new int[]{1, dh.size(), 1}; 
		ArrayChar slqcArray=new ArrayChar(sls);
		Index isl=slqcArray.getIndex();
		for (int iu=0; iu < dh.size(); iu++) {
			char[] d=sealv_qc[iu].toCharArray();
			slqcArray.setChar(isl.set(0, iu, 0), d[0]); 
		}
		vq.setCachedData(slqcArray, false);   
		ncfile.addVariable( null, vq);
		varList.add(vq);
		resultMap.put("ATMOSPHERIC-PRESSURE-OBSERVATION_sea_level_quality_code", arArray); 
		ad.clear(); 


		ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.4"));
		ncfile.addAttribute(null, new Attribute("CF:featureType", "timeSeries"));
		ncfile.addAttribute(null, new Attribute("title", "Integrated Surface Data (ISD)"));
		ncfile.addAttribute(null, new Attribute("description", "Surface observational data"));
		String s0=date[0].substring(0,4)+"-"+date[0].substring(4,6)+"-"+date[0].substring(6,8)+
		" "+hm[0].substring(0,2)+":"+hm[0].substring(2,4);
		ncfile.addAttribute(null, new Attribute("time_coverage_start", s0));
		String s1=date[lines-1].substring(0,4)+"-"+date[lines-1].substring(4,6)+"-"+
		date[lines-1].substring(6,8)+" "+hm[lines-1].substring(0,2)+":"+hm[lines-1].substring(2,4);
		ncfile.addAttribute(null, new Attribute("time_coverage_end", s1));

		System.out.println("VARS="+varList.size());
		for (int j=0; j< varList.size(); j++) {
			// logger.info("j="+(j+1)+" var name: "+varList.get(j).getName());
			//System.out.println("j="+(j+1)+" NAME: "+varList.get(j).getName()); 
		}
		//  logger.info("Size of resultMap="+resultMap.size());
		System.out.println("RESULT="+resultMap.size());

		ncfile.finish();

	}
	//-----------------------------------------------------------------------------

	/** Calculate the number of lines in input ISD data file  */

	public int linecount(ucar.unidata.io.RandomAccessFile raf) throws IOException {
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
	 * Calculate date/time as number of minutes from 01.01.1970.
	 * @param ddtt  array of strings "yyyymmdd". 
	 *              Length of array = number of lines of input datafile. 
	 * @param hhmm  array of strings "hhmm". 
	 *              Length of array = number of lines of input datafile. 
	 * @return      date as number of minutes from 01.01.1970
	 */

	public int[] getMinutes(String[] ddtt, String[] hhmm) {
		Calendar cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int[] year=new int[ddtt.length];
		int[] month=new int[ddtt.length];
		int[] day=new int[ddtt.length];
		int[] hour=new int[ddtt.length];
		int[] minute=new int[ddtt.length];
		int[] val=new int[ddtt.length];
		for (int j=0; j < ddtt.length; j++) {
			year[j]=Integer.parseInt(ddtt[j].substring(0,4));
			month[j]=Integer.parseInt(ddtt[j].substring(4,6));
			day[j]=Integer.parseInt(ddtt[j].substring(6,8));
			hour[j]=Integer.parseInt(hhmm[j].substring(0,2));
			minute[j]=Integer.parseInt(hhmm[j].substring(2,4));
			cal.set(year[j], month[j]-1, day[j], hour[j]-1, minute[j]-1, 0); 
			long tt=cal.getTime().getTime();
			val[j]=(int) (tt/1000/60);
		}
		return val;
	}
	//-----------------------------------------------------------------------------

	/** Read "addconfig.xml" and prepare formatMap: "key"=identifier, "value"= {length_field_1, length_field_2,....}
	 *  for the identifier
	 */ 
	public Map<String,String[]> getFormatMap() {
		//String infile="/home/nstroume/isd_point_iosp/lib/addconfig.xml";
		Map<String,String[]> formatMap=new HashMap<String,String[]> ();

		try { 
			javax.xml.parsers.DocumentBuilder builder =
				javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
			String config = configURL.toString(); 
			Document document = builder.parse(config);   
			XPath xpath = XPathFactory.newInstance().newXPath();
			String exp="//identifier/@value";
			NodeList nlist= (NodeList)xpath.evaluate(exp, document, XPathConstants.NODESET);
			String[] s=new String[nlist.getLength()];   
			for (int i= 0; i < nlist.getLength(); i++) { 
				s[i]=nlist.item(i).getNodeValue(); 
				String exp1="//identifier[@value='"+s[i]+"']/field/@length";
				NodeList nlist1= (NodeList)xpath.evaluate(exp1, document, XPathConstants.NODESET);
				String[] ss=new String[nlist1.getLength()];   
				for (int ii= 0; ii < nlist1.getLength(); ii++) { 
					ss[ii]=nlist1.item(ii).getNodeValue(); //System.out.println(t+s3[ii]);
				}
				formatMap.put(s[i], ss);  
			}     
			return formatMap;
		} catch (XPathExpressionException e) {   e.printStackTrace(); return null;
		} catch (ParserConfigurationException e) {   e.printStackTrace(); return null;
		} catch (SAXException e) {   e.printStackTrace(); return null;
		} catch (IOException e) {   e.printStackTrace(); return null;   }
	}
	//------------------------------------------------------------------------------

	/** Read "addconfig.xml" and prepare propertyMap: "key"=name of attribute, 
	 * "value"=value of the attribute for a field of identifier
	 *  @param idf  identifier
	 *  @param pf   position of the field 
	 *  @return       propertyMap
	 */ 
	public Map<String,String> getPropertyMap(String idf, int pf) {
		//String infile="/home/nstroume/isd_point_iosp/lib/addconfig.xml";
		Map<String,String> propMap=new HashMap<String,String> ();

		try { 
			javax.xml.parsers.DocumentBuilder builder =
				javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			Document document = builder.parse(new File(config));   
			Document document = builder.parse(configURL.toString());   
			XPath xpath = XPathFactory.newInstance().newXPath();
			String exp5="//identifier[@value='"+idf+"']/field["+(pf+1)+"]/@*";
			NodeList nlist5= (NodeList)xpath.evaluate(exp5, document, XPathConstants.NODESET); 
			for (int ii= 0; ii< nlist5.getLength(); ii++) {
				String att_name=nlist5.item(ii).getNodeName(); 
				String att_val=nlist5.item(ii).getNodeValue();
				if (att_name.matches("name")) { 
					String f=idf+"_"+att_val.replace('-','_');
					String ff=f.replace(' ','_');
					att_val=ff;
				} 
				propMap.put(att_name, att_val); 
			}             
			return propMap;
		} catch (XPathExpressionException e) {   e.printStackTrace(); return null;
		} catch (ParserConfigurationException e) {   e.printStackTrace(); return null;
		} catch (SAXException e) {   e.printStackTrace(); return null;
		} catch (IOException e) {   e.printStackTrace(); return null;   }
	}
	//------------------------------------------------------------------------------

	public void close() throws IOException { /*this.raf.close();*/ }

	public String getDetailInfo() { return "ISD IOServiceProvider."; }
	public String getFileTypeId(){ return "ISD"; }
	public String getFileTypeDescription(){ return " "; }

	/** A way to communicate arbitrary information to an iosp. */
	public Object sendIospMessage(Object message) {
		return super.sendIospMessage(message);
	}

	/**
	 * Checks to see if an ISD file is indeed valid
	 * Read "station_id", "wban_id", "year" from a name of input file  and compare them with
	 * the same values from the 1st line of input file.
	 *
	 * @param raf  the file to determine ownership
	 */
	public boolean isValidFile(final RandomAccessFile raf) throws IOException {
		raf.seek(0);
		String line = raf.readLine();
		if (line == null) return false;
		String infile = raf.getLocation().substring(raf.getLocation().lastIndexOf("/")+1);
		try {
			if (infile.length() < 17) {
				return false;
			}
			String idyr=infile.substring((infile.length()-17), (infile.length()-11))+
			infile.substring((infile.length()-10), (infile.length()-5))+
			infile.substring(infile.length()-4);
			return line.regionMatches(4, idyr, 0, 15);		
		} catch (Exception e) {
			return false;
		}
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
	//----------------------------------------------------------------------------------------------------

	/** 
	 *  Local class represents part of input line with identifier
	 *  and its data values
	 */
	class FieldValue {
		private String identifier;
		private String[] values;
		private int quon;

		/**  @param  identifier  identifier 
		 *  @param values      array of data values for the identifier
		 *  @param quon        length of values[] array
		 */
		public FieldValue(String identifier, String[] values, int quon) {
			this.values=new String[quon];            
			setIdentifier(identifier);
			setQuon(quon);
			setValues(values);
		}
		public void setIdentifier(String identifier) { 
			this.identifier=identifier; 
		}
		public String getIdentifier() { return identifier; }
		public void setQuon(int quon) { this.quon=quon; }
		public int getQuon() { return quon; }
		public void setValues(String[] values) { 
			System.arraycopy(values, 0, this.values, 0, quon);
		}
		public String[] getValues() { return values; }

		public boolean equals(Object o) {
			if (this == o) { 
				return true; 
			} else if(o instanceof FieldValue) {
				FieldValue oo=(FieldValue)o;
				boolean v=true;
				for (int j=0; j<quon; j++) {
					v &= (values[j].compareTo(oo.values[j]) == 0);
				}
				return (identifier == oo.identifier & v & quon == oo.quon);
			} else { return false;  }
		}
		public int hashCode() {
			return new String(identifier).hashCode()+values.hashCode()+
			new Integer(quon).hashCode();
		}
		public String toString() {
			StringBuffer sb=new StringBuffer();
			sb.append("Identifier="+identifier);
			sb.append("  Quantity of fields with values="+quon);
			sb.append("  Values:");
			for (int j=0; j<quon; j++) { sb.append(" "+values[j]); }
			return sb.toString();
		}

	} //--------- end class --------------------

}
