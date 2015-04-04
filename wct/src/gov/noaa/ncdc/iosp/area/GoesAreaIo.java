package gov.noaa.ncdc.iosp.area;

import edu.wisc.ssec.mcidas.AREAnav;
import edu.wisc.ssec.mcidas.AreaDirectory;
import edu.wisc.ssec.mcidas.AreaFileException;
import edu.wisc.ssec.mcidas.GOESnav;
import gov.noaa.ncdc.iosp.VariableInfo;
import gov.noaa.ncdc.iosp.VariableInfoManager;
import gov.noaa.ncdc.swath.NcdcSwathTransform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants._Coordinate;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.util.Parameter;

//TODO
//- HardCode the ancillary data file
//- output netCDF file
//- get netCDF to be COARDS compliant
//- get netCDF output to use udunits in most variables

/**
 * Describe class <code>Areaio</code> here.
 *
 * @author <a href="mailto:ken.knapp@noaa.gov">Ken Knapp</a>
 * @version 1.0
 */
public class GoesAreaIo {
    public ucar.unidata.io.RandomAccessFile raFile = null;

    private VariableInfoManager varInfoManager = new VariableInfoManager();

    public GoesNavBlock goesBlock;
    public NCDCAreaFile af=null;
    public AreaDir areaDir;
    public int [] dir;
    public AREAnav ng = null;
    public int [] nav;

    public List<Dimension> dimPixelScanBand = new ArrayList<Dimension>();
    public List<Dimension> dimPixelScan = new ArrayList<Dimension>();
    public List<Dimension> dimScan = new ArrayList<Dimension>();
    public List<Dimension> dimPixel = new ArrayList<Dimension>();
    public List<Dimension> dimTime = new ArrayList<Dimension>();
    public List<Dimension> dimText = new ArrayList<Dimension>();
    public List<Dimension> dimTextScan = new ArrayList<Dimension>();
    public List<Dimension> dimDeltaLon = new ArrayList<Dimension>();
    public List<Dimension> dimDeltaRad = new ArrayList<Dimension>();
    public List<Dimension> dimDeltaLat = new ArrayList<Dimension>();
    public List<Dimension> dimSinusoid = new ArrayList<Dimension>();
    public List<Dimension> dimNominal = new ArrayList<Dimension>();
    public List<Dimension> dimPrefixScan = new ArrayList<Dimension> ();
    
    public boolean dimensionsAreSet = false;
    public boolean latLonCalculated = false;
    public boolean imageHasBeenRead = false;

    public double[][][] latLonValues = null;
    public int [][][] image = null;
    public int numPixels = 0;
    public int numScans =  0;
    public int numBands =  0;
    public int prefixLength = 0;
    
    protected VariableInfo Time, SCAN, PIXEL, IMAGE,CALIBRATED_DATA; 

    //area directory variables
    protected VariableInfo  AREA_STATUS, VERSION_NUM, SENSOR_ID_NUM, IMG_DATE, IMG_TIME, NORTH_BOUND, WEST_VIS_ELEM,
    ZCOORD, NUM_LINES, NUM_ELEM, BYTES_PIXEL, LINE_RES, ELEM_RES, NUM_CHAN, PREFIX_BYTES, PROJ_NUM, CREATION_DATE, CREATION_TIME, SNDR_FILTER_MAP, 
    IMAGE_ID, COMMENT, PRI_KEY_CAL, PRI_KEY_NAV, SEC_KEY_NAV, VAL_CODE, PDL, BAND8, ACT_IMG_DATE, ACT_IMG_TIME,
    ACT_START_SCAN, LEN_PREFIX_DOC, LEN_PREFIX_CAL, LEN_PREFIX_LEV, SRC_TYPE, CALIB_TYPE, /*AVGSAMPLE, POES_SIGNAL, POES_UPDOWN,
    ORIG_SRC_TYPE,  */AUX_BLOCK_OFFSET, AUX_BLOCK_LENGTH, CAL_BLOCK_OFFSET,NUM_COMMENT_REC;
    
    
    // goes nav vars - only available for GOESNav mcidas files
    protected VariableInfo TYPE, IDDATE, START_TIME, ORBIT, ETIMY, ETIMH, SEMIMA, ECCEN, ORBINC, MEANA,
    PERIGEE, ASNODE, DECLIN, RASCEN, PICLIN, SPINP,DEGLIN, LINTOT, DEGELE, ELETOT,PITCH, YAW, ROLL,
    IAJUST, IAJTIME, ISEANG, SKEW, BETA1_SCAN, BETA1_TIME, BETA1_TIME2, BETA1_COUNT, BETA2_SCAN, BETA2_TIME, BETA2_TIME2, BETA2_COUNT, GAMMA, GAMMADOT, MEMO;   
   

    //prefix doc for goesNav
    private VariableInfo GOES_PREFIX,DETECTOR;
    
    public static boolean isValidFile ( ucar.unidata.io.RandomAccessFile raf ) {
        String fileName = raf.getLocation();
        try { 
            NCDCAreaFile af = new NCDCAreaFile(fileName);
            String navtype = AreaFileUtil.getNavType(raf);
            if("GOES".equals(navtype)){
            	return true;
            }
        } catch (AreaFileException e) {
            return false;
        }
        return false;
    }

    public void setAttributesAndVariables(ucar.unidata.io.RandomAccessFile raf, NetcdfFile ncfile) throws IOException,InvalidRangeException, AreaFileException {
        varInfoManager.clearAll();
        this.raFile = raf;
        af = new NCDCAreaFile(raf.getLocation());
        //read headers
        dir = af.getDir();
//        areaDir = af.getAreaDirectory();
        areaDir = new AreaDir();
        areaDir.readScanLine(raf);

        AreaDirectory ad;
        ad = new AreaDirectory(dir);
        numPixels = ad.getElements();
        numScans = ad.getLines();
        numBands= ad.getNumberOfBands();
        prefixLength = dir[14] - 4;
        if(prefixLength <0) prefixLength = 0;
        System.out.println("prefix length: " + prefixLength);

        nav = af.getNav();

       	ng = (GOESnav)af.getNavigation();
       	goesBlock = new GoesNavBlock();
       	goesBlock.readNav(raf);
       
        //define dimensions
        ucar.nc2.Dimension GeoX = new ucar.nc2.Dimension("GeoX",numPixels,true);
        ucar.nc2.Dimension GeoY = new ucar.nc2.Dimension("GeoY",numScans,true);
        ucar.nc2.Dimension band = new ucar.nc2.Dimension("band",numBands,true);
        ucar.nc2.Dimension time = new ucar.nc2.Dimension("time",1,true);
        ucar.nc2.Dimension text = new ucar.nc2.Dimension("char",32,true);
        ucar.nc2.Dimension deltaLon = new ucar.nc2.Dimension("deltaLon",13,true);
        ucar.nc2.Dimension deltaRad = new ucar.nc2.Dimension("delatRad",11,true);
        ucar.nc2.Dimension deltaLat = new ucar.nc2.Dimension("deltaLat",9,true);
        ucar.nc2.Dimension sinusoid = new ucar.nc2.Dimension("sinusoid",15,true);
        ucar.nc2.Dimension nominal = new ucar.nc2.Dimension("nominal",4,true);
        ucar.nc2.Dimension prefix = new ucar.nc2.Dimension("prefix",prefixLength,true);
        if ( ! dimensionsAreSet ) {
            //dimPixelScanBand.add(band);
            dimPixelScanBand.add(GeoY);
            dimPixelScanBand.add(GeoX);
            dimPixelScan.add(GeoY);
            dimPixelScan.add(GeoX);
            dimScan.add(GeoY);
            dimPixel.add(GeoX);
            dimTime.add(time);
            dimText.add(text);
            dimTextScan.add(text);
            dimTextScan.add(GeoY);
            
            dimDeltaLon.add(time);
            dimDeltaLon.add(deltaLon);
            
            dimDeltaRad .add(time);
            dimDeltaRad .add(deltaRad);
            dimDeltaLat.add(time);
            dimDeltaLat.add(deltaLat);
            dimSinusoid.add(time);
            dimSinusoid.add(sinusoid);
            dimNominal.add(time);
            dimNominal.add(nominal);
            dimPrefixScan.add(prefix);
            dimPrefixScan.add(GeoY);
            dimensionsAreSet=true;
        }

        //Global dimensions
//        ncfile.addAttribute(null,new Attribute("IOSP_Author","Kenjimoto"));
//		ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.0"));
		
        //add dimensions
        ncfile.addDimension(null,GeoX);
        ncfile.addDimension(null,GeoY);
        ncfile.addDimension(null,time);
        ncfile.addDimension(null,text);
        ncfile.addDimension(null,deltaLon);
        ncfile.addDimension(null,deltaRad);
        ncfile.addDimension(null,deltaLat);
        ncfile.addDimension(null,sinusoid);
        ncfile.addDimension(null,nominal);
        if(prefixLength != 0){
        	ncfile.addDimension(null,prefix);
        }        
        //ncfile.addDimension(null,band);

        //set variables & attributes using VariableInfo
        //varInfoManager.clearAll();
        //Initialize VariableInfo
        Time = new VariableInfo("time","Time",null,DataType.INT,dimTime);
        SCAN = new VariableInfo("GeoY","GeoY line","km",DataType.INT,dimScan);
        PIXEL =new VariableInfo("GeoX","GeoX number","km",DataType.INT,dimPixel);
        IMAGE = new VariableInfo("image","Image counts","count",DataType.SHORT,dimPixelScanBand,"GeoSatSwath");
        CALIBRATED_DATA = new VariableInfo("calibratedData","Brightness Temp","k",DataType.FLOAT,dimPixelScanBand,"GeoSatSwath");
        
        ////Area Block Variables (first 256 bytes of file)
        AREA_STATUS = new VariableInfo("areaStatus","Area Status - 0 if record is valid",null,DataType.INT, dimTime);
        VERSION_NUM = new VariableInfo("versionNum","McIDAS area file format version number",null,DataType.INT,dimTime);
        SENSOR_ID_NUM = new VariableInfo("sensorId","McIDAS sensor identifier",null,DataType.CHAR,dimText);
        IMG_DATE = new VariableInfo("imageDate","nominal year and day of the image"," YYYYDDD",DataType.INT,dimTime);
        IMG_TIME = new VariableInfo("imageTime","nominal time of the image","HHMMSS UTC",DataType.INT,dimTime);
        NORTH_BOUND = new VariableInfo("startLine","upper left image line coordinate",null,DataType.INT,dimTime);
        WEST_VIS_ELEM = new VariableInfo("startElement","upper left image element coordinate",null,DataType.INT,dimTime);
        ZCOORD = new VariableInfo("zCoord","Upper Z-coordinate",null,DataType.INT,dimTime);
        NUM_LINES = new VariableInfo("numLines","number of lines in the image",null,DataType.INT, dimTime);
        NUM_ELEM = new VariableInfo("numElements","number of data points per line",null,DataType.INT,dimTime); 
        BYTES_PIXEL = new VariableInfo("bytesPixel","number of bytes per data point ","byte",DataType.INT,dimTime);
        LINE_RES = new VariableInfo("lineRes","data resolution in line direction ",null,DataType.INT, dimTime);
        ELEM_RES = new VariableInfo("elementRes","data resolution in element direction",null,DataType.INT,dimTime);
        NUM_CHAN = new VariableInfo("numChan","number of channels in image",null,DataType.INT,dimTime);
        PREFIX_BYTES = new VariableInfo("prefixBytes","Number of Bytes in line prefix","byte",DataType.INT,dimTime);   //word 15 = word49 + word 50 + word51 (+ 4 if word 36 validity code is present)
        PROJ_NUM = new VariableInfo("projectionNum","SSEC project number used in creating image",null,DataType.INT,dimTime);	//word 16
        CREATION_DATE = new VariableInfo("creationDate","year and day image was created"," YYDDD",DataType.INT,dimTime);	 
        CREATION_TIME = new VariableInfo("creationTime","Time image was created", "HHMMSS",DataType.INT,dimTime);			//word 18
        SNDR_FILTER_MAP = new VariableInfo("bandMap","spectral band map, bit set for each of 32 bands present ",null,DataType.BYTE, dimTime);  //word 19
        IMAGE_ID = new VariableInfo("imageId","Image ID Number",null,DataType.INT,dimTime);  
        COMMENT = new VariableInfo("comment","Comments",null,DataType.CHAR,dimText);
        PRI_KEY_CAL = new VariableInfo("priKeyCalib","Calibration colicil (area number)",null,DataType.INT,dimTime); 
        PRI_KEY_NAV = new VariableInfo("priKeyNav","Primary navigation codicil (date)",null,DataType.INT,dimTime); 
        SEC_KEY_NAV = new VariableInfo("secKeyNav","Secondary navigation codicil(nav)",null,DataType.INT,dimTime); 
        VAL_CODE = new VariableInfo("validityCode","Validity Code",null,DataType.INT,dimTime);
        BAND8 = new VariableInfo("band8","Where band 8 came from",null,DataType.INT,dimTime);
        ACT_IMG_DATE = new VariableInfo("actualImgDate","Actual Image Date","YYYYDD",DataType.INT,dimTime);
        ACT_IMG_TIME = new VariableInfo("actualImgTime","Actual Image TIme","HHMMSS",DataType.INT,dimTime);
        ACT_START_SCAN = new VariableInfo("actualStartScan","Actual start scan",null,DataType.INT,dimTime); 
        LEN_PREFIX_DOC = new VariableInfo("prefixDocLength","Length of prefix DOC","bytes",DataType.INT,dimTime);
        LEN_PREFIX_CAL = new VariableInfo("prefixCalLength","Length of prefix CAL","bytes",DataType.INT,dimTime);
        LEN_PREFIX_LEV = new VariableInfo("prefixLevLength","Length of prefix LEV","bytes",DataType.INT,dimTime);
        SRC_TYPE = new VariableInfo("sourceType","Source Type",null,DataType.CHAR,dimText);
        CALIB_TYPE = new VariableInfo("calibrationType","Calibration Type",null,DataType.CHAR, dimText);
//        AVGSAMPLE = new VariableInfo("averageSample","Data was averaged(0), or sampled(1)",null,DataType.BYTE,dimTime);
//        POES_SIGNAL = new VariableInfo("poesSignal","LAC, GAC, HRPT",null,DataType.INT, dimTime);
 //       POES_UPDOWN = new VariableInfo("poesUpDown","POES ascending/descending",null,DataType.INT,dimTime);
//        ORIG_SRC_TYPE = new VariableInfo("srcType","original source type",null,DataType.CHAR,dimTime);
        AUX_BLOCK_OFFSET = new VariableInfo("auxBlockOffset","byte offset to the beginning of the area file's AUX block","byte",DataType.INT,dimTime);
        AUX_BLOCK_LENGTH = new VariableInfo("auxBlockLength","length of area file's AUX block","byte",DataType.INT,dimTime);
        CAL_BLOCK_OFFSET = new VariableInfo("calBlockOffset","byte offset to the beginning of the area file's CAL block",null,DataType.INT,dimTime); 
        NUM_COMMENT_REC = new VariableInfo("commentRecCount","number of comment records in AUDIT block",null,DataType.INT,dimTime);
        //end of Area Directory variables
        
        //GOES nav variables

        //goes variables   String name, String long_name, String units, DataType dataType, List dimList, Float scale_factor, Float add_offset, Integer valid_max, Integer recordNumber)
        TYPE = new VariableInfo("navType","Type",null,DataType.CHAR,dimText);
        IDDATE = new VariableInfo("iddate","ID and Date","SSSYYJJJ",DataType.INT,dimTime);
        START_TIME = new VariableInfo("startTime","nomial start time of image","HHMMSS",DataType.INT,dimTime);
        ORBIT = new VariableInfo("orbitType","orbit type 1=goes",null,DataType.BYTE,dimTime);
        ETIMY = new VariableInfo("epochDate","epoch date","YYMMDD",DataType.INT,dimTime);
        ETIMH = new VariableInfo("epochTime","epoch time","HHMMSS",DataType.INT,dimTime);
        SEMIMA = new VariableInfo("semiMajorAxis","semi-major axis","km",DataType.INT,dimTime,1E-2f,null,null,null);
        ECCEN = new VariableInfo("eccentricity","Eccentricity","km",DataType.INT,dimTime,1E-6f,null,null,null);
        ORBINC = new VariableInfo("inclination","orbital inclination","degrees",DataType.INT,dimTime,1E-3f,null,null,null);
        MEANA = new VariableInfo("meanAnomoly","Mean Anomoly","degrees",DataType.INT,dimTime,1E-3f,null,null,null);
        PERIGEE = new VariableInfo("perigee","argument of perigee","degrees",DataType.INT,dimTime,1E-3f,null,null,null);
        ASNODE = new VariableInfo("rightAscension","right ascension of asc. node","degrees",DataType.INT,dimTime,1E-3f,null,null,null);
        DECLIN = new VariableInfo("declin", "eclination of the satellite axis (+ = NORTH)","DDDMMSS",DataType.INT,dimTime);
        RASCEN = new VariableInfo("rightAsc","Right ascension of the satellite axis","DDDMMSS",DataType.INT,dimTime);
        PICLIN = new VariableInfo("picCenterLine","picture center line number",null,DataType.INT,dimTime);
        SPINP = new VariableInfo("spinPeriod","Spin period","rpm",DataType.INT,dimTime);
        DEGLIN = new VariableInfo("sweepAngle","Total sweep angle, line direction","DDDMMSS",DataType.INT,dimTime);
        LINTOT = new VariableInfo("lineTotal","number of scan lines",null,DataType.INT,dimTime);
        DEGELE = new VariableInfo("sweepAngleDir","Sweep angle element direction","DDDMMSS",DataType.INT,dimTime);
        ELETOT = new VariableInfo("eletot","Number of elements in scan line",null,DataType.INT,dimTime);
        PITCH = new VariableInfo("pitch","Forward-leaning (PITCH)","DDDMMSS",DataType.INT,dimTime);
        YAW = new VariableInfo("yaw","Sideways-leaning (YAW)","DDDMMSS",DataType.INT,dimTime);
        ROLL = new VariableInfo("roll","Rotation (ROLL)","DDDMMSS",DataType.INT,dimTime);
        IAJUST = new VariableInfo("iajust","East-West adjustment value (IAJUST), in visible elements(+ OR -)",null,DataType.INT,dimTime);
        IAJTIME = new VariableInfo("iajtime","Time that IAJUST computed from the first valid landmark of the day (IAJTIM)","HHMMSS",DataType.INT,dimTime);
        ISEANG = new VariableInfo("iseang","Angle between the VISSR and sun sensor","DDDMMSS",DataType.INT, dimTime);
        SKEW = new VariableInfo("skew","Skew",null,DataType.INT,dimTime);
        BETA1_SCAN = new VariableInfo("beta1scan","Scan line of the first beta",null,DataType.INT,dimTime);
        BETA1_TIME = new VariableInfo("beta1time","Time of the above scan line (BEGINNING)","HHMMSS",DataType.INT,dimTime);
        BETA1_TIME2 = new VariableInfo("betatime2","Time of the above scan line (CONTINUED)","ms",DataType.INT,dimTime,1E-1f,null,null,null);
        BETA1_COUNT = new VariableInfo("beta1count","Beta count 1",null,DataType.INT,dimTime);
        BETA2_SCAN = new VariableInfo("beta2scan","Scan line of second beta",null,DataType.INT,dimTime);       
        BETA2_TIME = new VariableInfo("beta2time","Time of the above scan line (BEGINNING)","HHMMSS",DataType.INT,dimTime);
        BETA2_TIME2 = new VariableInfo("beta2time2","Time of the above scan line (CONTINUED)","ms",DataType.INT,dimTime,1E-1f,null,null,null);
        BETA2_COUNT = new VariableInfo("beta2scan","Scan line of the first beta",null,DataType.INT,dimTime);
        GAMMA = new VariableInfo("gamma","GAMMA, element offset * 100",null,DataType.INT,dimTime,1E-2f,null,null,null);
        GAMMADOT = new VariableInfo("gamdo","GAMMA-DOT, element drift per hour * 100. ",null,DataType.INT,dimTime,1E-2f,null,null,null);
        MEMO = new VariableInfo("memo","EBCDIC comment",null,DataType.CHAR,dimTime);   

        VariableInfo LATITUDE = new VariableInfo("lat", "Latitude",
				"degrees_north", DataType.FLOAT, dimPixelScan, -9999.f,
				-9999.f, -9999, -1);
		VariableInfo LONGITUDE = new VariableInfo("lon", "Longitude",
				"degrees_east", DataType.FLOAT, dimPixelScan, -9999.f, -9999.f,
				-9999, -1);
        if(prefixLength != 0){
        	GOES_PREFIX = new VariableInfo("goesPrefix","Values for Geos Scan line prefix",null,DataType.INT,dimPrefixScan);	
//        	DETECTOR = new VariableInfo("detector","Detector Number",null,DataType.BYTE,dimScan);
        }
        Variable var3 = new Variable (ncfile,null,null,"GeoSatSwath");
        var3.setDataType(DataType.CHAR);
		var3.setDimensions("");
        var3.addAttribute( new Attribute(_Coordinate.Axes, "GeoX GeoY"));
//      ncfile.addVariableAttribute(var3,new Attribute("_CoordinateTransforms","NcdcSwath"));
        ncfile.addVariableAttribute(var3,new Attribute("_CoordinateTransforms","NcdcSwath"));
//      ncfile.addVariableAttribute(var3,new Attribute("_CoordinateTest","Test"));
        
        ncfile.addVariable(null, var3);
/**
      
    Variable var2 = new Variable (ncfile,null,null,"McIDASProjection");
    var2.setDataType(DataType.CHAR);
    ncfile.addVariableAttribute(var2,new Attribute("_CoordinateTransformType","projection"));
    ncfile.addVariableAttribute(var2,new Attribute("transform_name","mcidas_area"));
    ncfile.addVariableAttribute(var2,new Attribute("_CoordinateTest","Test"));    
         
*/
        Array arrayDir = new ArrayInt.D1(64);
        Index dirIndex = arrayDir.getIndex();
        for (int i=0; i< 64; i++){
            arrayDir.setInt(dirIndex.set(i), dir[i]);
        }

        Array arrayNav = null; 

        	arrayNav = new ArrayInt.D1(128);
        System.out.println("nav.length--->"+nav.length);
        Index navIndex = arrayNav.getIndex();
        for (int i=0; i< arrayNav.getSize(); i++){
            arrayNav.setInt(navIndex.set(i), nav[i]);
        }

        ProjectionImpl projection = null;
        projection = new NcdcSwathTransform(arrayDir,arrayNav,"GOES");
//        projection = new ucar.unidata.geoloc.projection.McIDASProjection(dir,nav);

        // coordinate transform variable
        Variable ct = new Variable( ncfile, null, null, "NcdcSwath");
        ct.setDataType( DataType.CHAR);
        ct.setDimensions("");
        List params = projection.getProjectionParameters();
        for (int i = 0; i < params.size(); i++) {
            Parameter p = (Parameter) params.get(i);
            ct.addAttribute( new Attribute(p));
        }
        ct.addAttribute( new Attribute(_Coordinate.TransformType, "Projection"));
 
        ct.addAttribute( new Attribute("AreaHeader",arrayDir));
        ct.addAttribute( new Attribute("NavHeader", arrayNav));
        // fake data
        Array dataA = Array.factory(DataType.CHAR.getPrimitiveClassType(), new int[] {});
        dataA.setChar(dataA.getIndex(), ' ');
        ct.setCachedData(dataA, false);

        ncfile.addVariable(null, ct);
        ncfile.addAttribute( null, new Attribute("Conventions","Kenjimoto"));

        //ncfile.addAttribute(null,new Attribute("Conventions","CF-1.0"));
        varInfoManager.setVariables( ncfile );

        //Lastly, finish the file	
        ncfile.finish();
    }

    public ucar.ma2.Array getVariableValues(ucar.nc2.Variable v2, List section) throws IOException, InvalidRangeException {
		Range geoXRange = null;
		Range geoYRange = null;
		if(section != null & section.size() > 0){
			geoYRange = (Range) section.get(0);
			if(section.size() > 1){
				geoXRange = (Range)section.get(1);
			}
		}
		String varname = v2.getName();
		//        System.out.println("X RANGE: "+geoXRange+" Y RANGE: "+geoYRange);

		//ucar.ma2.Array dataArray = Array.factory(v2.getDataType(), v2.getShape());
		ucar.ma2.Array dataArray = Array.factory(v2.getDataType().getPrimitiveClassType(), Range.getShape(section));

		if (v2.getName().equals("lat") || v2.getName().equals("lon")) {
			double[][] pixel = new double[2][1];
			double[][] latLon;
			latLonValues = new double[geoXRange.length()][geoYRange.length()][2];
			//Initialize navigation variables
			ng.setImageStart(dir[5], dir[6]);
			ng.setRes(dir[11], dir[12]);
			ng.setStart(1, 1);

			Index dataIndex = dataArray.getIndex();

			// Use Range object, which calculates requested i, j values and incorporates stride
			for (int i = 0; i < geoXRange.length(); i++) {
				for (int j = 0; j < geoYRange.length(); j++) {
					pixel[0][0] = (double) geoXRange.element(i);
					pixel[1][0] = (double) geoYRange.element(j);
					latLon = ng.toLatLon(pixel);

					if (v2.getName().equals("lat")) {
						dataArray.setFloat(dataIndex.set(j, i), (float) (latLon[0][0]));
					} else {
//						System.out.println("i:j-->" + i + ":" + j);
						dataArray.setFloat(dataIndex.set(j, i), (float) (latLon[1][0]));
					}
				}
			}
		}

		if (v2.getName().equals(PIXEL.getName()) || v2.getName().equals(SCAN.getName())) {
			Index dataIndex = dataArray.getIndex();
			int[] shape = v2.getShape();
			for (int i = 0; i < shape[0]; i++) {
				dataArray.setInt(dataIndex.set(i), i);
			}
		}

		if (v2.getName().equals(IMAGE.getName())) {
			Index dataIndex = dataArray.getIndex();
			try {
				// Use Range object, which calculates requested i, j values and incorporates stride
				int[][] pixelData = new int[1][1];
				for (int j = 0; j < geoYRange.length(); j++) {
					for (int i = 0; i < geoXRange.length(); i++) {
						pixelData = af.getData(geoYRange.element(j), geoXRange.element(i), 1, 1);
						dataArray.setInt(dataIndex.set(j, i), (pixelData[0][0]));
					}
				}

			} catch (AreaFileException afe) {
				afe.printStackTrace();
				throw new IOException(afe.toString());
			}

		}

		if (v2.getName().equals(CALIBRATED_DATA.getName())) {
			Index dataIndex = dataArray.getIndex();
			try {
				// Use Range object, which calculates requested i, j values and incorporates stride
				int[][] pixelData = new int[1][1];
				double[] calVals = AreaFileUtil.getCalibratedValues();
				for (int j = 0; j < geoYRange.length(); j++) {
					for (int i = 0; i < geoXRange.length(); i++) {
						pixelData = af.getData(geoYRange.element(j), geoXRange.element(i), 1, 1);
						dataArray.setFloat(dataIndex.set(j, i), (float)calVals[pixelData[0][0]]);
					}
				}

			} catch (AreaFileException afe) {
				afe.printStackTrace();
				throw new IOException(afe.toString());
			}

		}
	
		if (v2.getName().equals(Time.getName())) {
			Index dataIndex = dataArray.getIndex();
			int[] shape = v2.getShape();
			for (int i = 0; i < shape[0]; i++) {
				dataArray.setInt(dataIndex.set(i), (int) (19991231235959l));
			}

		}
		Index dataIndex = dataArray.getIndex();
		
		
//###########		area block vars         ######################//
		if(varname.equals(AREA_STATUS.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getStatus());
		}else if(varname.equals(VERSION_NUM.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getVersion());
		}else if(varname.equals(SENSOR_ID_NUM.getName())){
			int instNum = areaDir.getSatId();
			String s = " " + instNum + " - " + AreaFileUtil.findSensorSource(instNum);
			for(int i=0;i<s.length();i++){
				dataArray.setChar(dataIndex.set(i),s.charAt(i));
			}
			//need to get sat/instrument name and append to number
			dataArray.setInt(dataIndex.set(0),instNum);
		}else if(varname.equals(IMG_DATE.getName())){
			dataArray.setInt(dataIndex.set(0), areaDir.getImageDate());
		}else if(varname.equals(IMG_TIME.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getImageTime());
		}else if(varname.equals(NORTH_BOUND.getName())){
			dataArray.setInt(dataIndex.set(0), areaDir.getYCoord());
		}else if(varname.equals(WEST_VIS_ELEM.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getXCoord());
		}else if(varname.equals(ZCOORD.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getZCoord());
		}else if(varname.equals(NUM_LINES.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getNumLine());
		}else if(varname.equals(NUM_ELEM.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getNumEle());
		}else if(varname.equals(BYTES_PIXEL.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getBytesPixel());
		}else if(varname.equals(LINE_RES.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getLinesRes());
		}else if(varname.equals(ELEM_RES.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getElemRes());
		}else if(varname.equals(NUM_CHAN.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getNumChan());
		}else if(varname.equals(PREFIX_BYTES.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getPrefixBytes());
		}else if(varname.equals(PROJ_NUM.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getProjNum());
		}else if(varname.equals(CREATION_DATE.getName())){
			dataArray.setInt(dataIndex.set(0), areaDir.getCreationDate());
		}else if(varname.equals(CREATION_TIME.getName())){
			dataArray.setInt(dataIndex.set(0), areaDir.getCreationTime());
		}else if(varname.equals(SNDR_FILTER_MAP.getName())){
			int i = areaDir.getFilterMap();
			String s = Integer.toBinaryString(i);
        	dataArray.setByte(dataIndex.set(0),(byte)s.length());
		}else if(varname.equals(IMAGE_ID.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getImageIdNum());
		}else if(varname.equals(COMMENT.getName())){
			String comment = areaDir.getCommnets();;
			for(int i=0;i<comment.length();i++){
				dataArray.setChar(dataIndex.set(i),comment.charAt(i));
			}
		}else if(varname.equals(PRI_KEY_CAL.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getPriKeyCalib());
		}else if(varname.equals(PRI_KEY_NAV.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getPriKeyNav());
		}else if(varname.equals(SEC_KEY_NAV.getName())){
			dataArray.setInt(dataIndex.set(0),areaDir.getSecKeyNav());
		}else if(varname.equals(VAL_CODE.getName())){
        	dataArray.setInt(dataIndex.set(0),areaDir.getValidityCode());
        }else if(varname.equals(BAND8.getName())){
        	dataArray.setInt(dataIndex.set(0),areaDir.getBand8());
        }else if(varname.equals(ACT_IMG_DATE.getName())){
        	dataArray.setInt(dataIndex.set(0),areaDir.getActImgDate());
        }else if(varname.equals(ACT_IMG_TIME.getName())){
        	dataArray.setInt(dataIndex.set(0),areaDir.getActImgTime());
        }else if(varname.equals(ACT_START_SCAN.getName())){
        	dataArray.setInt(dataIndex.set(0),areaDir.getActStartScan());
        }else if(varname.equals(LEN_PREFIX_DOC.getName())){
        	dataArray.setInt(dataIndex.set(0),dir[48]);
        }else if(varname.equals(LEN_PREFIX_CAL.getName())){
        	dataArray.setInt(dataIndex.set(0),dir[49]);
        }else if(varname.equals(LEN_PREFIX_LEV.getName())){
        	dataArray.setInt(dataIndex.set(0),dir[50]);
        }else if(varname.equals(SRC_TYPE.getName())){
        	String st = areaDir.getSrcType();
        	for(int i=0;i<st.length();i++){
        		dataArray.setObject(dataIndex.set(i),st.charAt(i));
        	}
        }else if(varname.equals(CALIB_TYPE.getName())){
        	String ct = areaDir.getCalType();
        	for(int i=0;i<ct.length();i++){
        		dataArray.setObject(dataIndex.set(i),ct.charAt(i));
        	}
        }
        else if(varname.equals(AUX_BLOCK_OFFSET.getName())){
        	dataArray.setInt(dataIndex.set(0),dir[59]);
        }else if(varname.equals(AUX_BLOCK_LENGTH.getName())){
        	dataArray.setInt(dataIndex.set(0), dir[60]);
        }
        
        /**
        else if(varname.equals(AVGSAMPLE.getName())){
        	dataArray.setByte(dataIndex.set(0),(byte)areaDir.getAvgSample());
        }else if(varname.equals(POES_SIGNAL.getName())){
        	dataArray.setByte(dataIndex.set(0),(byte)areaDir.getAvgSample());
        }else if(varname.equals(POES_UPDOWN.getName())){
        	dataArray.setByte(dataIndex.set(0),(byte)areaDir.getAvgSample());
        }else if(varname.equals(ORIG_SRC_TYPE.getName())){
        	String ost = areaDir.getOrigSrcType();
        	for(int i=0;i<ost.length();i++){
        		dataArray.setChar(dataIndex.set(0),ost.charAt(i));
        	}
        }  
        */
        else if(varname.equals(CAL_BLOCK_OFFSET.getName())){
        	dataArray.setInt(dataIndex.set(0),dir[62]);
        }
        else if(varname.equals(NUM_COMMENT_REC.getName())){
        	dataArray.setInt(dataIndex.set(0),dir[63]);
        }
//  ###############  end of Area Directory Variable ###############################################################    


		
		
		
//  #################  Beginning of GOES Nav Variables  ##############################		

        else if(varname.equals(TYPE.getName())){
        	String type = goesBlock.getType();
			for(int i=0;i<4;i++){
				dataArray.setInt(dataIndex.set(i),type.charAt(i));
			}
		}else if(varname.equals(IDDATE.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getIdDate());
		}else if(varname.equals(START_TIME.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getTime());
		}else if(varname.equals(ORBIT.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getOrbit());
		}else if(varname.equals(ETIMY.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getEpochDate());
		}else if(varname.equals(ETIMH.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getEpochTime());
		}else if(varname.equals(SEMIMA.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getSemiMajorAxis());
		}else if(varname.equals(ECCEN.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getEccentricity());
		}else if(varname.equals(ORBINC.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getInclination());
		}else if(varname.equals(MEANA.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getMeanAnomaly());
		}else if(varname.equals(PERIGEE.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getPerigee());
		}else if(varname.equals(ASNODE.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getAscendingNode());
		}else if(varname.equals(DECLIN.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getDeclination());
		}else if(varname.equals(RASCEN.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getRightAscension());
		}else if(varname.equals(PICLIN.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getPicCL());
		}else if(varname.equals(SPINP.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getSpinPeriod());
		}else if(varname.equals(DEGLIN.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getSweepAngle());
		}else if(varname.equals(LINTOT.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getLineTotal());
		}else if(varname.equals(DEGELE.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getElementDir());
		}else if(varname.equals(ELETOT.getName())){
				dataArray.setInt(dataIndex.set(0),goesBlock.getElementTotal());
		}else if(varname.equals(PITCH.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getPitch());
		}else if(varname.equals(YAW.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getYaw());
		}else if(varname.equals(ROLL.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getRoll());
		}else if(varname.equals(IAJUST.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getIajust());
		}else if(varname.equals(IAJTIME.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getIajtim());
		}else if(varname.equals(ISEANG.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getIseang());
		}else if(varname.equals(SKEW.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getSkew());
		}else if(varname.equals(BETA1_SCAN.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getBeta1Scan());
		}else if(varname.equals(BETA1_TIME.getName())){
			dataArray.setInt(dataIndex.set(0),goesBlock.getBeta1Time());
		}else if(varname.equals( BETA1_TIME2.getName())){
        	dataArray.setInt(dataIndex.set(0),goesBlock.getBeta1Time2());
        }else if(varname.equals(BETA1_COUNT.getName())){
        	dataArray.setInt(dataIndex.set(0),goesBlock.getBeta1Count());
        }else if(varname.equals(BETA2_SCAN.getName())){
        	dataArray.setInt(dataIndex.set(0),goesBlock.getBeta2Scan());
        }else if(varname.equals(BETA2_TIME.getName())){
        	dataArray.setInt(dataIndex.set(0),goesBlock.getBeta2Time());
        }else if(varname.equals(BETA2_TIME2.getName())){
        	dataArray.setInt(dataIndex.set(0),goesBlock.getBeta2Time2());
        }else if(varname.equals(BETA2_COUNT.getName())){
        	dataArray.setInt(dataIndex.set(0),goesBlock.getBeta2Count());
        }else if(varname.equals(GAMMA.getName())){
        	dataArray.setInt(dataIndex.set(0),goesBlock.getGamma());
        }else if(varname.equals(GAMMADOT.getName())){
        	dataArray.setInt(dataIndex.set(0),goesBlock.getGammaDot());
        }else if(varname.equals(MEMO.getName())){
        	String memo = goesBlock.getMemo();
        	for(int i=0;i<32;i++){
        		dataArray.setChar(dataIndex.set(0),memo.charAt(i));
        	}
        }

// ################## End of Goes Nav Variables ################################################################

        if( prefixLength != 0){
        	if(varname.equals(GOES_PREFIX.getName())){
        		int[] xy = dataArray.getShape();
        		Range yrange = (Range)section.get(1);
        		int first = yrange.first();
        		for(int i=0;i<xy[1];i++){
        			short[] data = af.readGoesLinePrefix(prefixLength,first + i);
        			for(int j=0;j<xy[0];j++){
        				dataArray.setInt(dataIndex.set(j,i),j*j);
        			}
        		}
        	}
        	/**
        	else{
        		if(varname.equals(DETECTOR.getName())){
        			int[] xy = dataArray.getShape();
        			Range xRange = (Range)section.get(0);
        			for(int i=0;i<xy[0];i++){
        				GoesPrefix gp = af.readGoesLinePrefix(xRange.first() + i);
        				dataArray.setByte(dataIndex.set(i),(byte)gp.getSensor());
        			}
        		}
        	}
        	*/
        }
		return dataArray;
	}

}
