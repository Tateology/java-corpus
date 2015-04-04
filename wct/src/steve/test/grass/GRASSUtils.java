package steve.test.grass;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

public class GRASSUtils {
	
	public static final int TMP_ID = (int)(System.currentTimeMillis()%1000);
	
	public static final File GEGRASS_CACHE_DIR = new File(System.getProperty("user.home")+File.separator+".gegrass");
	
//	public static final String GISBASE = "C:\\Programs\\GRASS_643svn";
//	public static final String GISRC = "C:\\Documents and Settings\\ansari\\Application Data\\GRASS6\\grassrc6";
//	public static final String LD_LIBRARY_PATH = GISBASE + "\\lib";
	
	private String grassINSTALL_DIR;
	private String grassGISDBASE;
	private String grassLOCATION_NAME;
	private String grassMAPSET;
	
	private int imageWidth = 500;
	private int imageHeight = 500;
	
	public static void main(String[] args) {

		try {
//			sb.append("\n  g.gisenv set=GISDBASE=/c/ncsu/gis582/data     ");
//			sb.append("\n # g.gisenv set=LOCATION_NAME=nc_spm_08         ");
//			sb.append("\n  g.gisenv set=LOCATION_NAME=nc_ll              ");
//			sb.append("\n  g.gisenv set=MAPSET=coursework                ");
			
			GRASSUtils grass = new GRASSUtils("C:\\Program Files\\GRASS 6.4.2", "/c/ncsu/gis582/data", "nc_ll", "coursework");
			
			System.out.println(grass.getRegionInfoExtent());
			
//			String str = executeGRASS("g.version");
			String str = grass.executeGRASS("g.region -p");
//			String str = executeGRASS("r.info elev_ned_1arcsec@PERMANENT");
			
			System.out.println(str);
			
			
			
			HashMap<String, String> regionInfo = grass.getRegionInfo();
			System.out.println(regionInfo);
//			BufferedImage bimage = grass.generateImage("elev_ned_1arcsec@PERMANENT", "rast");
			BufferedImage bimage = grass.generateImage("rast", "developed_lake@coursework");
			
			
//			str = grass.executeGRASS("d.rast elev_ned_1arcsec@PERMANENT");
//			System.out.println(str);
			
			GRASSImageServer grassImageServer = new GRASSImageServer(8765, grass);
			
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public GRASSUtils() {		
	}
	
	public GRASSUtils(String grassInstallDir, String grassGISDBASE, String grassLOCATION_NAME, String grassMAPSET) {
		this.grassINSTALL_DIR = grassInstallDir;
		this.grassGISDBASE = grassGISDBASE;
		this.grassLOCATION_NAME = grassLOCATION_NAME;
		this.grassMAPSET = grassMAPSET;
	}
	
	public Rectangle2D.Double getRegionInfoExtent() {
		HashMap<String, String> regionInfo = getRegionInfo();
		
		System.out.println(regionInfo);
		
		Rectangle2D.Double extent = new Rectangle2D.Double(
				Double.parseDouble(regionInfo.get("w")),
				Double.parseDouble(regionInfo.get("s")),
				Double.parseDouble(regionInfo.get("e"))-Double.parseDouble(regionInfo.get("w")),
				Double.parseDouble(regionInfo.get("n"))-Double.parseDouble(regionInfo.get("s"))
			);
		
		return extent;
	}
	
	public HashMap<String, String> getRegionInfo() {
		HashMap<String, String> regionMap = new HashMap<String, String>();
		String regionInfo = executeGRASS("g.region -g");
//		g.region -g                                                                     
//		n=35.9113890408333
//		s=35.5880558605556
//		w=-78.8216666630556
//		e=-78.4522223933333
//		nsres=0.000277777646286751
//		ewres=0.000277777646407686
//		rows=1164
//		cols=1330
//		cells=1548120
		String[] lines = regionInfo.split("\n");
		for (int n=0; n<lines.length; n++) {
			if (lines[n].contains("=")) {
				String[] kvp = lines[n].split("=");
				regionMap.put(kvp[0], kvp[1]);
			}
		}
		return regionMap;		
	}
	
	
	public BufferedImage generateImage(String layerName, String type) throws IOException {
		
		return ImageIO.read(generateImageFile(layerName, type));
	}
		
	public File generateImageFile(String layerName, String type) throws IOException {
		HashMap<String, String> regionInfoMap = getRegionInfo();
		
		System.out.println(regionInfoMap);
		
		if (regionInfoMap.get("rows") == null) {
			throw new IOException("grass region could not be read");
		}
		
		imageHeight = Integer.parseInt(regionInfoMap.get("rows"));
		imageWidth = Integer.parseInt(regionInfoMap.get("cols"));
		
		if (type.equals("rast")) {
			executeGRASS("d.rast -o "+layerName);
		}
		else {
			executeGRASS("d."+type+" "+layerName);
		}
		
		System.out.println(new File("grass6output-"+TMP_ID+".png").getAbsolutePath());
		
		return new File("grass6output-"+TMP_ID+".png");
	}
	
	
	
	public String executeGRASS(String command) {
		final StringBuilder sb = new StringBuilder();
		OutputListener outputListener = new OutputListener() {
			@Override
			public void reportOutput(String c) {
				sb.append(c);
			}			
		};
		executeGRASS(command, outputListener);
		return sb.toString();
	}
	
	
	public void executeGRASS(String command, OutputListener outputListener) {
		
		
		
		try {


			
			
			
			String path = System.getenv("PATH");
			File tmpFile = generateRunScript();
			System.out.println(tmpFile);
			
			File gegrassCacheDir = new File(System.getProperty("user.home")+File.separator+".gegrass");
			if (! gegrassCacheDir.exists()) {
				gegrassCacheDir.mkdirs();
			}
				
			
//			ProcessRunner pr = new ProcessRunner(new File("C:\\devel\\wct"), 
//					grassINSTALL_DIR+"\\msys\\bin\\sh.exe", tmpFile.toString(), command);
			ProcessRunner pr = new ProcessRunner(gegrassCacheDir, 
					grassINSTALL_DIR+File.separator+"msys"+File.separator+"bin"+File.separator+"sh.exe", 
					tmpFile.toString(), command);

			
			pr.addEnvironmentVariable("GISBASE", grassINSTALL_DIR);
//			pr.addEnvironmentVariable("GISRC", gegrassCacheDirgrassrc6");
//			pr.addEnvironmentVariable("LD_LIBRARY_PATH", LD_LIBRARY_PATH);
			pr.addEnvironmentVariable("PATH", path+";"+grassINSTALL_DIR+"\\bin;"+grassINSTALL_DIR+"\\lib;"+grassINSTALL_DIR+"\\scripts;");
			
			
//			System.out.println(pr.getProcessBuilder().environment());
			
//			StringBuilder sb = new StringBuilder();
			OutputReader or = pr.subscribeToOutput();
			pr.start();
//			while (! or.END_OF_OUTPUT) {
//				System.out.println(or.);
//			}
	        int ch;
	        while ((ch = or.read()) != -1) {
	            System.out.print(Character.toChars(ch));
	            outputListener.reportOutput(new String(Character.toChars(ch)));
	        }
//	        return sb.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
//			return e.getMessage();
			outputListener.reportOutput(e.getMessage());
		}
		
		
	}
	
	
	public String convertWindowsPathToMSys(String wpath) {
		String mpath = wpath;
		mpath = mpath.replace(":\\", "/");
		mpath = mpath.replace("\\", "/");
		mpath = mpath.replace(" ", "\\ ");
		return "/"+mpath;
	}
	
	
	private File generateRunScript() throws IOException {

		
		if (! GEGRASS_CACHE_DIR.exists()) {
			GEGRASS_CACHE_DIR.mkdirs();
		}

		String msysGISBASE = convertWindowsPathToMSys(grassINSTALL_DIR);
		String msysGISDB = convertWindowsPathToMSys(grassGISDBASE);
		String msysGEGrassTmpDir = convertWindowsPathToMSys(GEGRASS_CACHE_DIR.toString());
		
		System.out.println("msysGISBASE   "+msysGISBASE);
		System.out.println("msysGISDB     "+msysGISDB);
		
		
		StringBuilder sb = new StringBuilder();
//		sb.append("\n export GISBASE=/c/Programs/GRASS_643svn ");
//		sb.append("\n export GISBASE=/c/Program\\ Files/GRASS\\ 6.4.2");
//                                   /C/Program\ Files\GRASS\ 6.4.2
		sb.append("\n export GISBASE="+msysGISBASE);
		sb.append("\n ");
		sb.append("\n export PATH=$GISBASE/bin:$GISBASE/lib:$GISBASE/scripts:$GISBASE/msys/lib:$GISBASE/msys/bin:$GISBASE/extrabin:$GISBASE/extralib:.  ");
//		sb.append("\n export GISRC=grassrc          ");
		sb.append("\n export GISRC="+msysGEGrassTmpDir+"/grassrc6");
		sb.append("\n export GIS_LOCK=$$            ");
		sb.append("\n                                                ");
//		sb.append("\n  g.gisenv set=GISDBASE=/c/ncsu/gis582/data     ");
//		sb.append("\n # g.gisenv set=LOCATION_NAME=nc_spm_08         ");
//		sb.append("\n  g.gisenv set=LOCATION_NAME=nc_ll              ");
//		sb.append("\n  g.gisenv set=MAPSET=coursework                ");
		sb.append("\n  g.gisenv set=GISDBASE="+msysGISDB);
		sb.append("\n  g.gisenv set=LOCATION_NAME="+grassLOCATION_NAME);
		sb.append("\n  g.gisenv set=MAPSET="+grassMAPSET);
		sb.append("\n                                                ");
		sb.append("\n                                                ");
		sb.append("\n                                                ");
		sb.append("\n                                                ");
		sb.append("\n                                                ");
		sb.append("\n                                                ");
		sb.append("\n                                                ");
		sb.append("\n # Check that this is a geographic projection (required by Google Earth)   ");
		sb.append("\n #g.proj -p                                                                ");
		sb.append("\n #PROJECTION=`g.proj -p | grep \"name\" | sed \"s/\\ //g\" | cut -c6-`          ");
		sb.append("\n #if [ \"$PROJECTION\" != \"Lat/Lon\" ] ; then                                 ");
		sb.append("\n #    echo \"r.out.kml: error: export only works from a Latitude-Longitude location, found $PROJECTION\" ");
		sb.append("\n #    exit 1                                                                 ");
		sb.append("\n #fi                                                                         ");
		sb.append("\n                                                                             ");
		sb.append("\n # Check that this is a WGS84 datum                                          ");
		sb.append("\n #DATUM=`g.proj -d | grep \"datum code: wgs84\" | cut -d: -f2`                 ");
		sb.append("\n #if [ \"$DATUM\" != \" wgs84\" ] ; then                                       ");
		sb.append("\n #    echo \"r.out.kml: error: export only works from a WGS84 datum location\" ");
		sb.append("\n #    exit 1                                                      ");
		sb.append("\n #fi                                                              ");
		sb.append("\n                                                                  ");
		sb.append("\n # Store current environment                                      ");
		sb.append("\n #g.region rast=$GIS_OPT_INPUT                                    ");
		sb.append("\n OLD_GRASS_WIDTH=$GRASS_WIDTH                                     ");
		sb.append("\n OLD_GRASS_HEIGHT=$GRASS_HEIGHT                                   ");
		sb.append("\n OLD_GRASS_PNGFILE=$GRASS_PNGFILE                                 ");
		sb.append("\n OLD_GRASS_TRANSPARENT=$GRASS_TRANSPARENT                         ");
		sb.append("\n OLD_GRASS_TRUECOLOR=$GRASS_TRUECOLOR                             ");
		sb.append("\n                                                                  ");
		sb.append("\n # define the driver settings                                     ");
//		sb.append("\n cols=$(g.region -g | grep \"cols\" | cut -d= -f2)   ");
//		sb.append("\n rows=`g.region -g | grep \"rows\" | cut -d= -f2`   ");
		
//		sb.append("\n echo $cols $rows ");
		
		sb.append("\n export GRASS_WIDTH="+imageWidth);
		sb.append("\n export GRASS_HEIGHT="+imageHeight);
//		sb.append("\n export GRASS_PNGFILE=grass6output-"+TMP_ID+".png ");
		sb.append("\n export GRASS_PNGFILE="+msysGEGrassTmpDir+"/grass6output-"+TMP_ID+".png ");
		sb.append("\n export GRASS_TRANSPARENT=TRUE                                    ");
		sb.append("\n export GRASS_TRUECOLOR=TRUE                                      ");
//		sb.append("\n                                                                  ");
//		sb.append("\n                                                                  ");
//		sb.append("\n #d.mon start=PNG                                                 ");
//		sb.append("\n #d.mon select=PNG                                                ");
//		sb.append("\n                                                                  ");
//		sb.append("\n d.rast -o elev_ned_1arcsec@PERMANENT            ");
//		sb.append("\n                                                                  ");
//		sb.append("\n #d.mon stop=PNG                                                  ");
//		sb.append("\n                                                                  ");
		sb.append("\n $@                                                             ");
//		sb.append("\n d.out.file output=grass_out.png format=png size=500,500 -t       ");
		sb.append("\n exit                                                             ");
		
//		File tmpFile = File.createTempFile("gegrass-", ".sh");
		File tmpFile = new File(GEGRASS_CACHE_DIR.toString()+File.separator+"gegrass-"+TMP_ID+".sh");
		System.out.println(tmpFile.getAbsolutePath());
		FileUtils.writeStringToFile(tmpFile, sb.toString());
		tmpFile.deleteOnExit();
		
		File imgFile = new File(GEGRASS_CACHE_DIR.toString()+File.separator+"grass6output-"+TMP_ID+".png");
		imgFile.deleteOnExit();
		
//		File shFile = new File("test10.sh");
//		FileUtils.writeStringToFile(shFile, sb.toString());
		
		return tmpFile;
	}
	
	
	
	
	
	
	

	public String getGrassINSTALL_DIR() {
		return grassINSTALL_DIR;
	}

	public void setGrassINSTALL_DIR(String grassINSTALL_DIR) {
		this.grassINSTALL_DIR = grassINSTALL_DIR;
	}

	public void setGrassGISDBASE(String grassGISDBASE) {
		this.grassGISDBASE = grassGISDBASE;
	}


	public String getGrassGISDBASE() {
		return grassGISDBASE;
	}


	public void setGrassLOCATION_NAME(String grassLOCATION_NAME) {
		this.grassLOCATION_NAME = grassLOCATION_NAME;
	}


	public String getGrassLOCATION_NAME() {
		return grassLOCATION_NAME;
	}


	public void setGrassMAPSET(String grassMAPSET) {
		this.grassMAPSET = grassMAPSET;
	}


	public String getGrassMAPSET() {
		return grassMAPSET;
	}
	
	public interface OutputListener {
		public void reportOutput(String c);
	}
}
