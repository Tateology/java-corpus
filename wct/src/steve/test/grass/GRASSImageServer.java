package steve.test.grass;


import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class GRASSImageServer {

	public static final int DEFAULT_PORT = 8421; 

	private HttpServer server = null;
	private WCTViewer viewer = null;
	private int port;

	private GRASSUtils grassUtils = null;
	

	public GRASSImageServer(int port, GRASSUtils grassUtils) throws IOException {
		this.port = port;
		this.grassUtils = grassUtils;
		init();        
	}



	private void init() throws IOException {
		//        System.out.println("Localhost: "+InetAddress.getByName("localhost"));

		InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), port);
		server = HttpServer.create(addr, 0);

		server.createContext("/kml"  , new GRASSKmlHandler());
		server.createContext("/image", new GRASSImageHandler(grassUtils));
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("GRASS Image Server is listening on port "+port);
	}

	public void shutdown() {
		server.stop(0);
	}

	public int getPort() {
		return port;
	}







	class GRASSKmlHandler implements HttpHandler {

		private boolean firstTime = true;

		public GRASSKmlHandler() {
		}

		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			if (requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "application/vnd.google-earth.kml+xml");
				exchange.sendResponseHeaders(200, 0);


				OutputStream responseBody = exchange.getResponseBody();
				System.out.println( exchange.getRequestURI() );
				String kml = getKML(exchange.getRequestURI().toString());
				responseBody.write(kml.getBytes());
				responseBody.close();
			}
		}

		public String getKML(String requestURI) {
			String params = requestURI.substring(requestURI.indexOf("?")+1);
			System.out.println("params = "+params);
			String[] paramsArray = params.split(";");
			String bbox = paramsArray[0].substring(paramsArray[0].indexOf("BBOX=")+5);
			String width = paramsArray[1].substring(paramsArray[1].indexOf("WIDTH=")+6);
			String height = paramsArray[2].substring(paramsArray[2].indexOf("HEIGHT=")+7);
			String cameraLon = paramsArray[3].substring(paramsArray[3].indexOf("CAMERA_LON=")+11);
			String cameraLat = paramsArray[4].substring(paramsArray[4].indexOf("CAMERA_LAT=")+11);
			String cameraAlt = paramsArray[5].substring(paramsArray[5].indexOf("CAMERA_ALT=")+11);
			String[] cornerArray = bbox.split(",");
			//        System.out.println(bbox);
			//        /kml?BBOX=-180,-56.92616410826921,180,90
			//        double aspectRatio = Double.parseDouble(width) / Double.parseDouble(height);
			double extentLLX = Double.parseDouble(cornerArray[0]); 
			double extentLLY = Double.parseDouble(cornerArray[1]);
			double extentWidth = Double.parseDouble(cornerArray[2])-Double.parseDouble(cornerArray[0]);
			double extentHeight = Double.parseDouble(cornerArray[3])-Double.parseDouble(cornerArray[1]);


			if (extentWidth > 90 || Math.abs(Double.parseDouble(cameraAlt)) > 10000000) {
				extentLLX = Double.parseDouble(cameraLon) - 45;
				extentLLY = Double.parseDouble(cameraLat) - 45;
				extentWidth = 90;
				extentHeight = 90;
			}


			Rectangle2D.Double extent = new Rectangle2D.Double(
					extentLLX,
					extentLLY,
					extentWidth,
					extentHeight);

			try {
				//            System.out.println("extent 1: "+extent);
				// extent extent so that geotools map pane shows entire extent requested,
				// instead of clipping the extent to fit the map pane.
				extent = WCTUtils.adjustGeographicBounds(new Dimension(Integer.parseInt(width), Integer.parseInt(height)), extent);
				//            System.out.println("extent 2: "+extent);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (firstTime) {
				firstTime = false;
			}
			else {
				viewer.setCurrentExtent(extent);
			}


			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>               ");
			sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\"            ");
			sb.append("     xmlns:gx=\"http://www.google.com/kml/ext/2.2\"      ");
			sb.append("     xmlns:kml=\"http://www.opengis.net/kml/2.2\"        ");
			sb.append("     xmlns:atom=\"http://www.w3.org/2005/Atom\">         ");
			//        sb.append("<GroundOverlay>                                          ");
			//        sb.append(" <name>Untitled Image Overlay</name>             ");
			//        sb.append(" <Icon>                                          ");
			//        // random extra parameter eliminates caching!
			//        sb.append("     <href>http://localhost:8421/image?"+(int)(Math.random()*100000)+" </href>    ");
			////        sb.append("     <href>http://localhost:8421/image?"+params+"</href>    ");
			//        sb.append("     <viewBoundScale>0.75</viewBoundScale>       ");
			//        sb.append(" </Icon>                                         ");
			//        sb.append(" <LatLonBox>                                     ");
			//        sb.append("     <north>"+cornerArray[3]+"</north>           ");
			//        sb.append("     <south>"+cornerArray[1]+"</south>           ");
			//        sb.append("     <east>"+cornerArray[2]+"</east>             ");
			//        sb.append("     <west>"+cornerArray[0]+"</west>             ");
			//        sb.append(" </LatLonBox>                                    ");
			//        sb.append("</GroundOverlay>                                 ");
			sb.append("</kml>                                           ");

			return sb.toString();
		}
	}



	class GRASSImageHandler implements HttpHandler {

		private GRASSUtils grassUtils = null;

		public GRASSImageHandler(GRASSUtils grassUtils) {
			this.grassUtils = grassUtils;			
		}

		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			if (requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				//      responseHeaders.set("Content-Type", "text/plain");
				responseHeaders.set("Content-Type", "image/png");
				exchange.sendResponseHeaders(200, 0);

				OutputStream responseBody = exchange.getResponseBody();
				//            Headers requestHeaders = exchange.getRequestHeaders();

				String requestURI = exchange.getRequestURI().toString();
//				if (requestURI.equals("/image/") || requestURI.equals("/image") ||
//						requestURI.contains("?cacheBuster=") || requestURI.contains(".png")) {
					
					
					String params = requestURI.substring(requestURI.indexOf("?")+1);
					System.out.println("params = "+params);
					String[] paramsArray = params.split(";");
					
					String layer = null;
					String type = null;
					
						try {                	
							File file = new File(GRASSUtils.GEGRASS_CACHE_DIR + File.separator + 
									"grass6output-"+GRASSUtils.TMP_ID+".png");
							
							if (paramsArray.length == 3) {

								layer = paramsArray[1].substring(paramsArray[0].indexOf("LAYER=")+7);
								type = paramsArray[2].substring(paramsArray[1].indexOf("TYPE=")+6);

//								GRASSUtils grass = new GRASSUtils(grassINSTALL_DIR, grassGISDBASE, grassLOCATION_NAME, grassMAPSET);
								System.out.println("grass.generateImage("+layer+", "+type+");");

								file = grassUtils.generateImageFile(layer, type);
								
							}
							System.out.println(file.length() + " bytes");

							responseBody.write(FileUtils.readFileToByteArray(file));
							responseBody.close();
							//						file.delete();

							System.out.println("image finished!");
						} catch (Exception e) {
							System.out.println(" ERROR INFO: "+grassUtils.getGrassINSTALL_DIR()+" : "+
									grassUtils.getGrassGISDBASE()+" : "+grassUtils.getGrassLOCATION_NAME()+
									" : "+grassUtils.getGrassMAPSET());
							
							System.out.println("           : layer="+layer+" : type="+type);
							e.printStackTrace();
							throw new IOException(e);
						}
					
					
			
					
					return;
//				}



			}
		}
	}



}
