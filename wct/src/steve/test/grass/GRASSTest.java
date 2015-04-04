package steve.test.grass;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class GRASSTest {
	
//	public static final String GISBASE = "C:\\Programs\\GRASS_643svn";
//	public static final String GISBASE = "C:\\Program Files\\GRASS 6.4.2";
	// short name, using dir /x command
	// PROGRA~1\\GRASS6~1.2
	public static final String GISBASE = "C:\\PROGRA~1\\GRASS6~1.2";
	public static final String GISRC = "C:\\Documents and Settings\\ansari\\Application Data\\GRASS6\\grassrc6";
	public static final String LD_LIBRARY_PATH = GISBASE + "\\lib";
	
	
	public static void main(String[] args) {

		try {
			
//			String str = executeGRASS("g.version");
			String str = executeGRASS("g.region -p");
//			String str = executeGRASS("r.info elev_ned_1arcsec@PERMANENT");
			
			System.out.println(str);
			
			
			
			str = executeGRASS("d.rast elev_ned_1arcsec@PERMANENT");
			System.out.println(str);
			
			
			
		
//			String str = executePlexus("g.version");
//			System.out.println(str);
		
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	
	
	public static String executeGRASS(String command) {
		
		try {


			
			
			
			String path = System.getenv("PATH");
//			System.out.println(path);
 
			File tmpFile = generateRunScript();
			System.out.println(tmpFile);
			ProcessRunner pr = new ProcessRunner(new File("C:\\devel\\wct"), 
					GISBASE+"\\msys\\bin\\sh.exe", tmpFile.toString(), command);

			
			pr.addEnvironmentVariable("GISBASE", GISBASE);
			pr.addEnvironmentVariable("GISRC", GISRC);
			pr.addEnvironmentVariable("LD_LIBRARY_PATH", LD_LIBRARY_PATH);
			pr.addEnvironmentVariable("PATH", path+";"+GISBASE+"\\bin;"+GISBASE+"\\lib;"+GISBASE+"\\scripts;");
			
			
//			System.out.println(pr.getProcessBuilder().environment());
			
			StringBuilder sb = new StringBuilder();
			OutputReader or = pr.subscribeToOutput();
			pr.start();
//			while (! or.END_OF_OUTPUT) {
//				System.out.println(or.);
//			}
	        int ch;
	        while ((ch = or.read()) != -1) {
//	            System.out.print(Character.toChars(ch));
	            sb.append(Character.toChars(ch));
	        }
	        return sb.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		
		
	}
	
	
	
	private static File generateRunScript() throws IOException {
		
//		C:\\Programs\\GRASS_643svn
		char driveChar = GISBASE.charAt(0);
		String msysGrassPath = GISBASE.replace(driveChar+":\\", "/"+driveChar+"/");
		msysGrassPath = msysGrassPath.replaceAll("\\\\", "/");
		
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("\n export GISBASE="+msysGrassPath);
		sb.append("\n ");
		sb.append("\n export PATH=$GISBASE/bin:$GISBASE/lib:$GISBASE/scripts:$GISBASE/msys/lib:$GISBASE/msys/bin:$GISBASE/extrabin:$GISBASE/extralib:.  ");
		sb.append("\n export GISRC=grassrc          ");
		sb.append("\n export GIS_LOCK=$$            ");
		sb.append("\n                                                ");
		sb.append("\n  g.gisenv set=GISDBASE=/c/ncsu/gis582/data     ");
		sb.append("\n # g.gisenv set=LOCATION_NAME=nc_spm_08         ");
		sb.append("\n  g.gisenv set=LOCATION_NAME=nc_ll              ");
		sb.append("\n  g.gisenv set=MAPSET=coursework                ");
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
		
		sb.append("\n export GRASS_WIDTH=500                                           ");
		sb.append("\n export GRASS_HEIGHT=500                                          ");
		sb.append("\n export GRASS_PNGFILE=grass6output.png                            ");
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
		
		File tmpFile = File.createTempFile("gegrass-", ".sh");
		FileUtils.writeStringToFile(tmpFile, sb.toString());
		tmpFile.deleteOnExit();
		
		
		File shFile = new File("test10.sh");
		FileUtils.writeStringToFile(shFile, sb.toString());
		
		return tmpFile;
	}
	
	
	
	
	
	
	
	
	
	
	private static File generateBatScript() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("\r\n set GISBASE=C:\\Programs\\GRASS_643svn ");
		sb.append("\r\n ");
		sb.append("\r\n set PATH=%GISBASE%\\bin;%GISBASE%\\lib;%GISBASE%\\scripts;%GISBASE%\\msys\\lib;%GISBASE%\\msys\\bin;%GISBASE%\\extrabin;%GISBASE%\\extralib;.  ");
		sb.append("\r\n set GISRC=grassrc                                   ");
		sb.append("\r\n                                                ");
		sb.append("\r\n                                                ");
		sb.append("\r\n  g.gisenv set=GISDBASE=C:\\ncsu\\gis582\\data  ");
//		sb.append("\r\n # g.gisenv set=LOCATION_NAME=nc_spm_08         ");
		sb.append("\r\n  g.gisenv set=LOCATION_NAME=nc_ll              ");
		sb.append("\r\n  g.gisenv set=MAPSET=coursework                ");
		sb.append("\r\n                                                ");
//		sb.append("\r\n                                                ");
//		sb.append("\r\n                                                ");
//		sb.append("\r\n                                                ");
//		sb.append("\r\n                                                ");
//		sb.append("\r\n                                                ");
//		sb.append("\r\n                                                ");
//		sb.append("\r\n # Check that this is a geographic projection (required by Google Earth)   ");
//		sb.append("\r\n #g.proj -p                                                                ");
//		sb.append("\r\n #PROJECTION=`g.proj -p | grep \"name\" | sed \"s/\\ //g\" | cut -c6-`          ");
//		sb.append("\r\n #if [ \"$PROJECTION\" != \"Lat/Lon\" ] ; then                                 ");
//		sb.append("\r\n #    echo \"r.out.kml: error: export only works from a Latitude-Longitude location, found $PROJECTION\" ");
//		sb.append("\r\n #    exit 1                                                                 ");
//		sb.append("\r\n #fi                                                                         ");
//		sb.append("\r\n                                                                             ");
//		sb.append("\r\n # Check that this is a WGS84 datum                                          ");
//		sb.append("\r\n #DATUM=`g.proj -d | grep \"datum code: wgs84\" | cut -d: -f2`                 ");
//		sb.append("\r\n #if [ \"$DATUM\" != \" wgs84\" ] ; then                                       ");
//		sb.append("\r\n #    echo \"r.out.kml: error: export only works from a WGS84 datum location\" ");
//		sb.append("\r\n #    exit 1                                                      ");
//		sb.append("\r\n #fi                                                              ");
//		sb.append("\r\n                                                                  ");
//		sb.append("\r\n # Store current environment                                      ");
//		sb.append("\r\n #g.region rast=$GIS_OPT_INPUT                                    ");
//		sb.append("\r\n OLD_GRASS_WIDTH=$GRASS_WIDTH                                     ");
//		sb.append("\r\n OLD_GRASS_HEIGHT=$GRASS_HEIGHT                                   ");
//		sb.append("\r\n OLD_GRASS_PNGFILE=$GRASS_PNGFILE                                 ");
//		sb.append("\r\n OLD_GRASS_TRANSPARENT=$GRASS_TRANSPARENT                         ");
//		sb.append("\r\n OLD_GRASS_TRUECOLOR=$GRASS_TRUECOLOR                             ");
//		sb.append("\r\n                                                                  ");
//		sb.append("\r\n # define the driver settings                                     ");
//		sb.append("\r\n cols=$(g.region -g | grep \"cols\" | cut -d= -f2)   ");
//		sb.append("\r\n rows=`g.region -g | grep \"rows\" | cut -d= -f2`   ");
		
//		sb.append("\r\n echo $cols $rows ");
		
		sb.append("\r\n set GRASS_WIDTH=500                                           ");
		sb.append("\r\n set GRASS_HEIGHT=500                                          ");
		sb.append("\r\n set GRASS_PNGFILE=grass6output.png                            ");
		sb.append("\r\n set GRASS_TRANSPARENT=TRUE                                    ");
		sb.append("\r\n set GRASS_TRUECOLOR=TRUE                                      ");
//		sb.append("\r\n                                                                  ");
//		sb.append("\r\n                                                                  ");
//		sb.append("\r\n #d.mon start=PNG                                                 ");
//		sb.append("\r\n #d.mon select=PNG                                                ");
//		sb.append("\r\n                                                                  ");
//		sb.append("\r\n d.rast -o elev_ned_1arcsec@PERMANENT            ");
//		sb.append("\r\n                                                                  ");
//		sb.append("\r\n #d.mon stop=PNG                                                  ");
//		sb.append("\r\n                                                                  ");
		sb.append("\r\n %*                                                             ");
//		sb.append("\r\n d.out.file output=grass_out.png format=png size=500,500 -t       ");
//		sb.append("\r\ n exit                                                             ");
		
		File tmpFile = File.createTempFile("gegrass-", ".bat");  
		FileUtils.writeStringToFile(tmpFile, sb.toString());
		tmpFile.deleteOnExit();
		
		
		File shFile = new File("test10.bat");
		FileUtils.writeStringToFile(shFile, sb.toString());
		
		return tmpFile;
	}
	
}
