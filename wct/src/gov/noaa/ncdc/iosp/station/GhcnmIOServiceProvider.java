package gov.noaa.ncdc.iosp.station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.unidata.io.RandomAccessFile;

/** IOService Provider for GHCNM v3 data */

public class GhcnmIOServiceProvider extends AbstractIOServiceProvider {      
    ucar.unidata.io.RandomAccessFile raf;
    static String invFile=" ";
    static String datFile=" ";
    static String countryFile=" ";
    static String stnID=" ";
   
    static public Map<String,String> countryMap=new HashMap<String, String> (); 
    static public Map<String,String> metadataMap=new HashMap<String, String> (); 
    static public Map<String, String> indexMap=new HashMap<String, String> (); 

    static public ArrayList<Variable> varList=new ArrayList<Variable>();
    static public Map<String, Array> resultMap=new HashMap<String, Array> (); 

   public static void main(String[] args) { 
       
        if (args.length >3) {
          invFile=args[0];
          datFile=args[1];
          countryFile=args[2];
          stnID=args[3];
        } else if (args.length == 3) {
           invFile=args[0];
           datFile=args[1];
           countryFile=args[2];
        } else { System.out.println("Usage: java GhcnmIOServiceProvider [invFile] [datFile] [countryFile] <Station ID (Optional)>");
        System.exit(0);  
        } 
        
        // 1. write Unidata version
//        try {
//        	NetcdfFile ncfile = Ghcnm.open(datFile);
//        	
//        	System.out.println(ncfile);
//        	
//        	
//        	String outfile = datFile+"-unidata.nc";
//        	NetcdfFile outFile = ucar.nc2.FileWriter.writeToFile(ncfile, outfile);   
//
//        	ncfile.close();
//        	outFile.close();
//        	
//        } catch (Exception e) {
//        	e.printStackTrace();
//        }
        


        try {   
        	NetcdfFile.registerIOProvider(GhcnmIOServiceProvider.class);
        	NetcdfFile ncfile=NetcdfFile.open(datFile);
        	//       System.out.println("ncfile = \n"+ncfile);
        	//       String outfile="/home/nstroume/ghcnm_v3/data_out/ghcnm00.nc";
        	String outfile = datFile+"_A924.nc";
        	NetcdfFile outFile = ucar.nc2.FileWriter.writeToFile(ncfile, outfile);   

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

          countryMap=getCountryMap();
 /*
          Set set=countryMap.entrySet();       
          Iterator it=set.iterator();
          while (it.hasNext()) {
           Map.Entry m=(Map.Entry)it.next();
           String key=(String)m.getKey();
           String val=(String)m.getValue();
 System.out.print(""+key+": ");
 System.out.print(val+" "); 
 System.out.println();
          }
          System.out.println("COUNTRY SIZE="+countryMap.size());    
 */
         metadataMap=getMetadataMap();
 /*
          Set set=metadataMap.entrySet();       
          Iterator it=set.iterator();
          while (it.hasNext()) {
           Map.Entry m=(Map.Entry)it.next();
           String key=(String)m.getKey();
           String val=(String)m.getValue();
 System.out.print(""+key+": ");
 System.out.print(val+" "); 
 System.out.println();
          }
         System.out.println("METADATA SIZE="+metadataMap.size());
 */

  // Define the number of lines of input file          
         int lines=linecount(raf);                     //System.out.println("LINES="+lines);
              
        int[] nobs=new int[7290];
        String[] did=new String[lines];
        String[] dy=new String[lines];
        float[] dval=new float[lines*12];
        String[] dmflag=new String[lines*12];
        String[] qcflag=new String[lines*12];
        String[] dsflag=new String[lines*12];

      float[] lat=new float[metadataMap.size()];
      float[] lon=new float[metadataMap.size()];
      float[] elev=new float[metadataMap.size()];
      String[] theStn=new String[metadataMap.size()];
      String[] stn_name=new String[metadataMap.size()];

         int KK=0;  int ii=0;
         raf.seek(0);  
         stnID=new String(raf.readBytes(11));    
         theStn[0]=stnID; 
        indexMap.put(stnID, ""+KK); 
         int num_stn=0; 
         raf.seek(0);        
         for (int i = 0; i < lines; i++) {
          String q = raf.readLine(); 

          did[i]=q.substring(0,11);   
          dy[i]=q.substring(11,15);   
          if (did[i].indexOf(stnID) < 0)  {    
             theStn[ii]=stnID;           
             KK=KK+nobs[num_stn];
 //if (num_stn<10) System.out.println("KK="+KK+" num_stn="+num_stn+" nobs="+nobs[num_stn]);
            indexMap.put(did[i], ""+KK);  ii++;
             String temp=(String)metadataMap.get(stnID); 
             lat[num_stn]=Float.parseFloat(temp.substring(0, 8).trim());        //System.out.println("lat=|"+lat+"|");
             lon[num_stn]=Float.parseFloat(temp.substring(9, 18).trim());    // System.out.println("lon=|"+lon+"|");
             elev[num_stn]=Float.parseFloat(temp.substring(19, 25).trim());  // System.out.println("elev=|"+elev+"|");
             stn_name[num_stn]=temp.substring(26, 56);                             //System.out.println("stn_name=|"+stn_name+"| ");
 // System.out.println(" code="+stnID+" :  "+" KK="+KK+" nobs="+nobs[num_stn]+" lat="+lat[num_stn]+" lon="+lon[num_stn]+
//                               " elev="+elev[num_stn]+"  "+"|"+stn_name[num_stn]+"|");            
             stnID=did[i];    num_stn++;     //  System.out.println("num_stn="+num_stn+"  stnID="+stnID);
          }
                                    
          int pos_val=19;  int pos_dm=24; int pos_qc=25; int pos_ds=26;
       
          for (int j=0; j<12; j++) {  
            dval[i*12+j]=Float.parseFloat(q.substring(pos_val+j*8, pos_val+j*8+5))*0.01f;   //System.out.print(dval[KK+j]+"*");
            dmflag[i*12+j]=q.substring(pos_dm+j*8, pos_dm+j*8+1);  
               if (dmflag[i*12+j].trim().length() < 1) dmflag[i*12+j]="x";        //System.out.print(dmflag[KK+j]+"*");
            qcflag[i*12+j]=q.substring(pos_qc+j*8, pos_qc+j*8+1);   
                if (qcflag[i*12+j].trim().length() < 1) qcflag[i*12+j]="x";         //System.out.print(qcflag[KK+j]+"*");
            dsflag[i*12+j]=q.substring(pos_ds+j*8, pos_ds+j*8+1);   
                 if (dsflag[i*12+j].trim().length() < 1) dsflag[i*12+j]="x";        //System.out.print(dsflag[KK+j]+"*");
           }
         
             nobs[num_stn]++; 
                 
         }  //-------------------------------------------------------- for i ----------------------
                
 //System.out.println("stnID="+stnID+" num_stn="+num_stn+" nobs[num_stn]="+nobs[num_stn]+" KK="+KK);
             String temp=(String)metadataMap.get(stnID); 
             lat[num_stn]=Float.parseFloat(temp.substring(0, 8).trim());        //System.out.println("lat=|"+lat+"|");
             lon[num_stn]=Float.parseFloat(temp.substring(9, 18).trim());    // System.out.println("lon=|"+lon+"|");
             elev[num_stn]=Float.parseFloat(temp.substring(19, 25).trim());  // System.out.println("elev=|"+elev+"|");
             stn_name[num_stn]=temp.substring(26, 56);                          

          theStn[metadataMap.size()-1]=stnID;
          indexMap.put(stnID, ""+KK);     

 // Define Dimensions, Variables, Attributes for NetCDF file
        List<Dimension> dims=new ArrayList<Dimension> ();  
        List<Dimension> dimst=new ArrayList<Dimension> ();
        List<Dimension> dm=new ArrayList<Dimension> ();
        List<Dimension> wdm=new ArrayList<Dimension> ();
        List<Dimension> fdims=new ArrayList<Dimension> ();

       Dimension obsDim=new Dimension("obs", lines*12, true);
       Dimension stnDim=new Dimension("station", metadataMap.size(), true);
       Dimension nameDim=new Dimension("name_strlen", 11, true);
       Dimension wDim=new Dimension("info_strlen", 30, true);
       Dimension fgDim=new Dimension("flag_strlen", 1, true);

       dims.add(obsDim);                                                                             // for monthly variables
       dimst.add(stnDim);                                                                             // for lon, lat, alt
       dm.add(stnDim); dm.add(nameDim);                                                   // for char stn id
       wdm.add(stnDim); wdm.add(wDim);                                                   // for char WMO id
       fdims.add(obsDim);   fdims.add(fgDim);                                             // for monthly char flag values

       ncfile.addDimension(null, obsDim);
       ncfile.addDimension(null, stnDim);
       ncfile.addDimension(null, nameDim);
       ncfile.addDimension(null, wDim);
       ncfile.addDimension(null, fgDim);

       int[] shape = new int[] {lines*12};
       int[] sh=new int[]{metadataMap.size()}; 
       int[] nsh=new int[]{metadataMap.size(),11};  
       int[] wsh=new int[]{metadataMap.size(), 30};
       int[] fsh=new int[]{lines*12, 1};
       
   ArrayFloat.D1 latArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
   Index latIx=latArray.getIndex();
   ArrayFloat.D1 lonArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
   Index lonIx=lonArray.getIndex();
   ArrayFloat.D1 altArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, sh);
    Index altIx=altArray.getIndex();
   ArrayInt.D1 raw_sizeArray=(ArrayInt.D1)Array.factory(DataType.INT, sh);
    Index szIx=raw_sizeArray.getIndex();
   
   ArrayInt.D1 timeArray=(ArrayInt.D1)Array.factory(DataType.INT, shape);
   Index tIx=timeArray.getIndex();
   ArrayFloat.D1 tavgArray=(ArrayFloat.D1)Array.factory(DataType.FLOAT, shape);
   Index tavgIx=tavgArray.getIndex();

    for (int i=0; i<metadataMap.size(); i++) {
         latArray.setFloat(latIx.set(i), lat[i]);
         lonArray.setFloat(lonIx.set(i), lon[i]);
         altArray.setFloat(altIx.set(i), elev[i]);
         raw_sizeArray.setFloat(szIx.set(i), nobs[i]*12);
       }

   int[] mints=getMonths(dy);
   for (int i=0; i<lines*12; i++) {
     timeArray.setInt(tIx.set(i), mints[i]);
     tavgArray.setFloat(tavgIx.set(i),  dval[i]);
  }

  Variable vv=new Variable(ncfile, null, null, "station_name");
     vv.setDimensions(dm);
     vv.setDataType(DataType.CHAR);
     vv.addAttribute( new Attribute("long_name", "The station ID"));
     vv.addAttribute( new Attribute("standard_name", "station_id"));
     ArrayChar nameArray=new ArrayChar(nsh);
     Index nameIx=nameArray.getIndex();
     for (int ik=0; ik<metadataMap.size(); ik++) {
        char[] ca=theStn[ik].toCharArray();
        for (int i=0; i<11; i++) { nameArray.setChar(nameIx.set(ik,i), ca[i]); }
     } 
     vv.setCachedData(nameArray, false);
     ncfile.addVariable( null, vv);
     varList.add(vv);
     resultMap.put("station_name", nameArray); 
 // NCdump.printArray(nameArray, "station_name", System.out, null);

   Variable vvv=new Variable(ncfile, null, null, "station_description");
     vvv.setDimensions(wdm);
     vvv.setDataType(DataType.CHAR);
     vvv.addAttribute( new Attribute("long_name", "Geographic station description"));
     vvv.addAttribute( new Attribute("standard_name", "region"));
     ArrayChar wArray=new ArrayChar(wsh);
     Index wwIx=wArray.getIndex();
     for (int ik=0; ik<metadataMap.size(); ik++) {
         char[] wb=(stn_name[ik].trim()).toCharArray();  
         for (int i=0; i<wb.length; i++) { 
            wArray.setChar(wwIx.set(ik,i), wb[i]); }
    }
     vvv.setCachedData(wArray, false);
     ncfile.addVariable( null, vvv);
     varList.add(vvv);
     resultMap.put("station_description", wArray); 
  //  NCdump.printArray(wArray, "station_description", System.out, null);

   Variable var = new Variable(ncfile, null, null, "lat");
     var.setDimensions(dimst);
     var.setDataType(DataType.FLOAT);
     var.addAttribute( new Attribute("long_name", "Latitude coordinate of station"));
     var.addAttribute( new Attribute("standard_name", "latitude"));
     var.addAttribute( new Attribute("valid_range", "-90., 90."));
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
     var.addAttribute( new Attribute("long_name", "Longitude coordinate of station"));
     var.addAttribute( new Attribute("standard_name", "longitude"));
     var.addAttribute( new Attribute("valid_range", "-180., 180."));
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
     var.addAttribute(new Attribute("long_name", "Elevation dimension of station."));
     var.addAttribute(new Attribute("units", "meters"));
     var.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Height.toString()));
     var.addAttribute(new Attribute("axis", "Z"));
     var.addAttribute(new Attribute("positive", "up"));
     var.setCachedData(altArray, false);
     ncfile.addVariable( null, var);
     varList.add(var);
     resultMap.put("alt", altArray); 
 //NCdump.printArray(altArray, "altitude", System.out, null);

     var = new Variable(ncfile, null, null, "row_size");
     var.setDimensions(dimst);
     var.setDataType(DataType.INT);
     var.addAttribute(new Attribute("CF:ragged_row_count", "obs"));
     var.addAttribute(new Attribute("long_name", "Number of observations for this station."));
     var.addAttribute(new Attribute("standard_name", "ragged_rowSize"));
     var.setCachedData(raw_sizeArray, false);
     ncfile.addVariable( null, var);
     varList.add(var);
     resultMap.put("row_size", raw_sizeArray); 
 //NCdump.printArray(raw_sizeArray, "row_size", System.out, null);

     var = new Variable(ncfile, null, null, "time");
     var.setDimensions(dims);
     var.setDataType(DataType.INT);
     var.addAttribute(new Attribute("standard_name", "time"));
     var.addAttribute(new Attribute("long_name", "Observation time."));
     var.addAttribute(new Attribute("units", "months since 1900-01-01 00:00:00Z"));
     var.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
     var.addAttribute(new Attribute("axis", "T"));
     var.setCachedData(timeArray, false);
     ncfile.addVariable( null, var);
     varList.add(var);
     resultMap.put("time", timeArray); 
 //NCdump.printArray(timeArray, "time", System.out, null);

     var = new Variable(ncfile, null, null, "Monthly_average_temperature");
     var.setDimensions(dims);
     var.setDataType(DataType.FLOAT);
     var.addAttribute(new Attribute("long_name", "Monthly average temperature"));
     var.addAttribute(new Attribute("standard_name", "air_temperature"));
     var.addAttribute(new Attribute("coordinates", "time lat lon alt"));
     var.addAttribute(new Attribute("units", "degrees Celsius"));
     var.addAttribute(new Attribute("scalingFactor", 1));
     var.addAttribute(new Attribute("_FillValue", -99.9));
     var.setCachedData(tavgArray, false);
     ncfile.addVariable( null, var);
     varList.add(var);
     resultMap.put("Monthly_average_temperature", tavgArray);
 //NCdump.printArray(tavgArray, "Monthly_average_temperature", System.out, null);

    Variable vq=new Variable(ncfile, null, null, "DMFLAG");
     vq.setDimensions(fdims);
     vq.setDataType(DataType.CHAR);   
     vq.addAttribute( new Attribute("long_name", "Data measurement flag"));
     vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
     vq.addAttribute(new Attribute("description", "The code that denotes a quality status of the reported measurement information."));  
     vq.addAttribute(new Attribute("_FillValue", "x"));
     ArrayChar dpoArray=new ArrayChar(fsh);
     Index idpo=dpoArray.getIndex();
     for (int iu=0; iu < lines*12; iu++) {  
           char[] d=dmflag[iu].toCharArray();
           dpoArray.setChar(idpo.set(iu, 0), d[0]);     
    }
     vq.setCachedData(dpoArray, false);   
     ncfile.addVariable( null, vq);
     varList.add(vq);
     resultMap.put("DMFLAG", dpoArray); 
 //NCdump.printArray(dpoArray, "DMFLAG", System.out, null);

    vq=new Variable(ncfile, null, null, "QCFLAG");
     vq.setDimensions(fdims);
     vq.setDataType(DataType.CHAR);   
     vq.addAttribute( new Attribute("long_name", "Quality control flag"));
     vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
     vq.addAttribute(new Attribute("description", "The code that denotes a quality status of the reported information."));  
     vq.addAttribute(new Attribute("_FillValue", "x"));
     ArrayChar qcArray=new ArrayChar(fsh);
     Index iqc=qcArray.getIndex();
     for (int iu=0; iu < lines*12; iu++) {
           char[] d=qcflag[iu].toCharArray();
           qcArray.setChar(iqc.set(iu, 0), d[0]); 
    }
     vq.setCachedData(qcArray, false);   
     ncfile.addVariable( null, vq);
     varList.add(vq);
     resultMap.put("QCFLAG", qcArray); 
 //NCdump.printArray(qcArray, "QCFLAG", System.out, null);

    vq=new Variable(ncfile, null, null, "DSFLAG");
     vq.setDimensions(fdims);
     vq.setDataType(DataType.CHAR);   
     vq.addAttribute( new Attribute("long_name", "Data source flag"));
     vq.addAttribute(new Attribute("coordinates", "time lat lon alt"));        
     vq.addAttribute(new Attribute("description", "The code that denotes a quality status of the data source information."));  
     vq.addAttribute(new Attribute("_FillValue", "x"));
     ArrayChar dsArray=new ArrayChar(fsh);
     Index ids=dsArray.getIndex();
     for (int iu=0; iu < lines*12; iu++) { 
           char[] d=dsflag[iu].toCharArray();
           dsArray.setChar(ids.set (iu, 0), d[0]); 
    }
     vq.setCachedData(dsArray, false);   
     ncfile.addVariable( null, vq);
     varList.add(vq);
     resultMap.put("DSFLAG", dsArray); 
 //NCdump.printArray(dsArray, "DSFLAG", System.out, null);


 // Global attributes
    ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.4"));
    ncfile.addAttribute(null, new Attribute("CF:featureType", "timeSeries"));
    ncfile.addAttribute(null, new Attribute("Metadata_Conventions", "Unidata Dataset Discovery v1.0"));
    ncfile.addAttribute(null, new Attribute("Metadata_Link", "Metadata_Link"));
    ncfile.addAttribute(null, new Attribute("title", "Global Historical Climatology Network Monthly Version 3"));
        String ff=raf.getLocation();
        String[] fp=ff.split("/");
         for (int ih=0; ih<fp.length; ih++) {
           if (fp[ih].startsWith("ghcnm")) {  ncfile.addAttribute(null, new Attribute("id", fp[ih])); }
        }
    ncfile.addAttribute(null, new Attribute("naming_authority", "gov.noaa.ncdc"));
    ncfile.addAttribute(null, new Attribute("summary", "Global HistoricalClimotology Network-Monthly dataset of land surface mean temperature"));
    ncfile.addAttribute(null, new Attribute("keywords", "land surface temperature"));
    ncfile.addAttribute(null, new Attribute("keywords_vocabulary", "GCMO Science Keywords"));
    ncfile.addAttribute(null, new Attribute("standard_name_vocabulary", "GCMO Science Keywords"));
    ncfile.addAttribute(null, new Attribute("history", "GHCNM v3.0.0 was released in 2010-09-01.\nUpdated in 2011-01-15."));
    ncfile.addAttribute(null, new Attribute("comment", "Daily reprocessing of the GHCNM dataset consists of construction process that assembles\nthe data in "+
            "a specific source priority order, quality controls the data, identifies inhomogeneities\nand performs adjustments where possible."));
    ncfile.addAttribute(null, new Attribute("geospatial_lat_min", "-90.0"));
    ncfile.addAttribute(null, new Attribute("geospatial_lat_max", "90.0"));
    ncfile.addAttribute(null, new Attribute("geospatial_lon_min", "0.0"));
    ncfile.addAttribute(null, new Attribute("geospatial_lon_max", "360.0"));
    Arrays.sort(mints);
    int len=mints.length;
    ncfile.addAttribute(null, new Attribute("time_coverage_start", mints[0]+" months since 1900-01-01 00:00:00Z"));
    ncfile.addAttribute(null, new Attribute("time_coverage_end", mints[len-1]+" months since 1900-01-01 00:00:00Z"));
    ncfile.addAttribute(null, new Attribute("time_coverage_duration", "P"+(mints[len-1]-mints[0])+"M"));
    ncfile.addAttribute(null, new Attribute("time_coverage_resolution", "monthly"));
    Arrays.sort(elev);
    len=elev.length;
    float elev_min=elev[0];
    for (int ih=0; ih<len; ih++) { 
      if (elev[ih] > -900.0f) { 
            elev_min=elev[ih];  break;
      }
    }
    ncfile.addAttribute(null, new Attribute("geospatial_vertical_min", elev_min));
    ncfile.addAttribute(null, new Attribute("geospatial_vertical_max", elev[len-1]));
    ncfile.addAttribute(null, new Attribute("geospatial_lon_units", "degrees_east"));
    ncfile.addAttribute(null, new Attribute("geospatial_lon_resolution", "geospatial_lon_resolution"));
    ncfile.addAttribute(null, new Attribute("geospatial_lat_units", "degrees_north"));
    ncfile.addAttribute(null, new Attribute("geospatial_lat_resolution", "geospatial_lat_resolution"));
    ncfile.addAttribute(null, new Attribute("geospatial_vertical_units", "meters"));
    ncfile.addAttribute(null, new Attribute("geospatial_vertical_resolution", "geospatial_vertical_resolution"));
    ncfile.addAttribute(null, new Attribute("geospatial_vertical_positive", "up"));
    ncfile.addAttribute(null, new Attribute("creator_name", "Climate Service and Monitoring Division"));
    ncfile.addAttribute(null, new Attribute("creator_url", "http://www.ncdc.noaa.gov/ghcnm/"));
    ncfile.addAttribute(null, new Attribute("creator_email", "ncdc.ghcnm@noaa.gov"));
    ncfile.addAttribute(null, new Attribute("institution", "NOAA/National Climatic Data Center"));
    ncfile.addAttribute(null, new Attribute("date_created", "2010-09-01"));
    ncfile.addAttribute(null, new Attribute("date_modified", "2011-01-15"));
    ncfile.addAttribute(null, new Attribute("date_issued", "2011-01-15"));
    ncfile.addAttribute(null, new Attribute("project", "GHCN-Monthly Version 3.0.0"));
    ncfile.addAttribute(null, new Attribute("acknowledgment", "Climate Service and Monitoring Division"));
    ncfile.addAttribute(null, new Attribute("contributor_name", "NOAA/National Climatic Data Center"));
    ncfile.addAttribute(null, new Attribute("contributor_role", "contributor_role"));
    ncfile.addAttribute(null, new Attribute("publisher_name", "Climate Service and Monitoring Division"));
    ncfile.addAttribute(null, new Attribute("publisher_url", "http://www.ncdc.noaa.gov/ghcnm/"));
    ncfile.addAttribute(null, new Attribute("publisher_email", "ncdc.ghcnm@noaa.gov"));
    ncfile.addAttribute(null, new Attribute("processing_level", "Quality assurance reviews include preprocessing checks on source data, time series checks\n"+
                   "that identify spurious changes in the mean and variance, spatial comparisons that verify\nthe accuracy of the climatological mean and the seasonal cycle, "+
                   "and neighbor checks that\nidentify outliers from both a serial and a spatial perspective."));
    ncfile.addAttribute(null, new Attribute("license", "The dataset is available at ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/v3/"));
    ncfile.addAttribute(null, new Attribute("cdm_data_type", "Station"));
   
 //System.out.println("VARS="+varList.size());
    for (int j=0; j< varList.size(); j++) {
  //  System.out.println("j="+(j+1)+" NAME: "+varList.get(j).getName()); 
    }
 //  System.out.println("RESULT="+resultMap.size());

    ncfile.finish(); 
   }
  //------------------------------------------------------------------------------------ 

  public Map<String, String> getCountryMap() {
     Map<String,String> map=new HashMap<String, String> ();
     try {
       FileReader fr=new FileReader(countryFile);
       BufferedReader buf=new BufferedReader(fr);
       String s=null;
       while((s=buf.readLine()) != null) {
         String code=s.substring(0,3);
         String name=s.substring(4).trim();
         map.put(code, name);
       }
       buf.close();
     } catch (IOException ioe) { ioe.printStackTrace(); }
      return map;
   }
 //------------------------------------------------------------------------------------

   public Map<String, String> getMetadataMap() {
     Map<String,String> map=new HashMap<String, String> ();
     try {
       FileReader fr=new FileReader(invFile);
       BufferedReader buf=new BufferedReader(fr);
       String s=null;
       while((s=buf.readLine()) != null) {
         String id=s.substring(0,11);
         String metadata=s.substring(12);
         map.put(id, metadata);
       }
       buf.close();
     } catch (IOException ioe) { ioe.printStackTrace(); }
      return map;
   }
  
  /** Read CRN daily data and populate ncfile with it. */
  
 //--------------------------------------------------------------------------------------

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
 //------------------------------------------------------------------------------------

 /** 
 * Calculate date/time as number of months from 01.01.1900 00:00:00.
   * @param yy  array of strings "yyyy" (year). 
   *                Length of array = number of lines in input data file. 
   * @return       time as number of months from 01.01.1900 00:00:00
   */
    public int[] getMonths(String[]yy) {
 // 1900-01-01 is a base year
       int[] year=new int[yy.length];
       int[] mm=new int[yy.length*12];
       for (int i=0; i< yy.length; i++) {
         year[i]=Integer.parseInt(yy[i].trim());
         for (int j=0; j<12; j++) {
           mm[i*12+j]=(year[i]-1900)*12+j;
        }
      }
      return mm;
   }
 //---------------------------------------------------------------------------------------------
 /** 
 * Calculate date/time as number of minutes from 01.01.1970 00:00:00.
   * @param yymm  array of strings "yyyymm" (year/month). 
   *                Length of array = number of observations. 
   * @return      date as number of minutes from 01.01.1970 00:00:00
   */

   public int[] getMinutes(String[]yy) {
    Calendar cal=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    int[] year=new int[yy.length];
    int[] month=new int[12];
    int[] day=new int[12];
    int[] val=new int[yy.length*12];
    for (int i=0; i< yy.length; i++) {
      year[i]=Integer.parseInt(yy[i].trim());
      for (int j=0; j<12; j++) {
        month[j]=j+1;
        day[j]=1;    
        cal.set(year[i], month[j]-1, day[j]); 
        long tt=cal.getTime().getTime();
       val[i*12+j]=(int) (tt/1000/60);
      }
    }
     return val;
   }
 //-----------------------------------------------------------------------------


     public void close() throws IOException { /*this.raf.close();*/ }

     public String getDetailInfo() { return "GHCNM IOServiceProvider."; }
     public String getFileTypeId(){ return "GHCNM"; }
     public String getFileTypeDescription(){ return " "; }

 /** A way to communicate arbitrary information to an iosp. */
     public Object sendIospMessage(Object message) {
       return super.sendIospMessage(message);
      }
       
 /**
   * Checks to see if an CRN file is indeed valid
   * Read a name of input file  and define if there is
   * the "ghcnm" sequence.
   *
   * @param raf  the file to determine ownership
   */
     public boolean isValidFile(final RandomAccessFile raf) throws IOException {
     	String filename=raf.getLocation();
         return (filename.indexOf("ghcnm") > -1);     
     }
 //-----------------------------------------------------------------------------------------------------

 /** Read data from a top level Variable and return a memory resident Array */
     public Array readData(ucar.nc2.Variable v2, Section wantSection) 
      throws IOException, InvalidRangeException {
        Array data = Array.factory(v2.getDataType().getPrimitiveClassType(), wantSection.getShape());
        IndexIterator ii = data.getIndexIterator();

        String varName=v2.getName(); 
        Array arr=(Array)resultMap.get(varName);
        Index index = arr.getIndex();
        int[] ix=wantSection.getOrigin();                      
        Range range=wantSection.getRange(0);
        if (varName.startsWith("time")) {
            for (int i=range.first(); i<=range.last(); i+= range.stride()) {
                    ii.setIntNext( arr.getInt(index.set(i))); 
            }
        }  else if (varName.indexOf("temperature") > -1) {
            for (int i=range.first(); i<=range.last(); i+= range.stride()) {
                    ii.setFloatNext( arr.getFloat(index.set(i)));
            } 
         } else {
             for (int i=range.first(); i<=range.last(); i+= range.stride()) {
                    ii.setCharNext( arr.getChar(index.set(i)));
            } 
         }
          return data;
      
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
