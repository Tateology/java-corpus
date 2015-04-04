package gov.noaa.ncdc.wct.ui.ge;


import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.ui.ViewerKmzUtilities;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WCTImageServer {

	public static final int DEFAULT_PORT = 8421; 

	private HttpServer server = null;
	private WCTViewer viewer = null;
	private int port;


	public WCTImageServer(WCTViewer viewer) throws IOException {
		this(viewer, DEFAULT_PORT);
	}   

	public WCTImageServer(WCTViewer viewer, int port) throws IOException {
		this.viewer = viewer;
		this.port = port;
		init();        
	}



	private void init() throws IOException {
		//        System.out.println("Localhost: "+InetAddress.getByName("localhost"));

		InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("localhost"), port);
		server = HttpServer.create(addr, 0);

		server.createContext("/bbox" , new WCTBBoxHandler(viewer));
		server.createContext("/kml"  , new WCTKmlHandler(viewer));
		server.createContext("/image", new WCTImageHandler(viewer));
		server.createContext("/legend", new WCTLegendHandler(viewer));
		server.createContext("/wms"  , new WCTWmsHandler(viewer));
		server.createContext("/arclayer"  , new WCTArcLayerHandler(viewer));
		server.createContext("/dae", new WCTDaeHandler(viewer, port));
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("WCT Image Server is listening on port 8421" );
	}

	public void shutdown() {
		server.stop(0);
	}

	public int getPort() {
		return port;
	}





	class WCTBBoxHandler implements HttpHandler {
		private WCTViewer viewer = null;
		private ViewerKmzUtilities kmzUtils = null;

		public WCTBBoxHandler(WCTViewer viewer) {
			this.viewer = viewer;
			this.kmzUtils = new ViewerKmzUtilities(viewer);
		}

		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			if (requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "text/plain");
				exchange.sendResponseHeaders(200, 0);

				OutputStream responseBody = exchange.getResponseBody();
				System.out.println( exchange.getRequestURI() );
				Rectangle2D.Double extent = viewer.getCurrentExtent();
				String bboxString = extent.getMinX() + "," + extent.getMinY() + "," +
				extent.getMaxX() + "," + extent.getMaxY();
				//              /kml?BBOX=-180,-56.92616410826921,180,90
				responseBody.write(bboxString.getBytes());
				responseBody.close();
			}
		}
	}



	class WCTDaeHandler implements HttpHandler {
		private WCTViewer viewer = null;
		private ViewerKmzUtilities kmzUtils = null;
		private int port = -1;

		public WCTDaeHandler(WCTViewer viewer, int port) {
			this.viewer = viewer;
			this.port = port;
			this.kmzUtils = new ViewerKmzUtilities(viewer);
		}

		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			if (requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "text/plain");
				exchange.sendResponseHeaders(200, 0);

				OutputStream responseBody = exchange.getResponseBody();
				//            System.out.println( exchange.getRequestURI() );


				//            URL url = new URL("http://kml-samples.googlecode.com/svn/trunk/resources/bldg.dae");
				//            String str;
				//            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				//            while ((str = in.readLine()) != null) {
				//            	responseBody.write((str+"\n").getBytes());
				//            }
				//            responseBody.close();
				//            
				//            if (true)
				//            return;




				String imageName = "http://localhost:"+port+"/image/"+(int)(Math.random()*10000000)+".png?includeSnapshotLayers=false";
				//            String imageName = "image/"+(int)(Math.random()*10000000)+".png";
				try {
					responseBody.write(WCTColladaUtils.getColladaDAE(viewer, imageName, 1).getBytes());
				} catch (Exception e) {
					e.printStackTrace();
					responseBody.write(e.getMessage().getBytes());
				}
				responseBody.close();
			}
		}
	}



	class WCTKmlHandler implements HttpHandler {
		private WCTViewer viewer = null;
		private ViewerKmzUtilities kmzUtils = null;

		private boolean firstTime = true;

		public WCTKmlHandler(WCTViewer viewer) {
			this.viewer = viewer;
			this.kmzUtils = new ViewerKmzUtilities(viewer);
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



	class WCTImageHandler implements HttpHandler {
		private WCTViewer viewer = null;
		private ViewerKmzUtilities kmzUtils = null;

		public WCTImageHandler(WCTViewer viewer) {
			this.viewer = viewer;
			this.kmzUtils = new ViewerKmzUtilities(viewer);
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
				if (requestURI.equals("/image/") || requestURI.equals("/image") ||
						requestURI.contains("?cacheBuster=") || requestURI.contains(".png")) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					BufferedImage bimage = null;
					try {                	
						boolean ignoreSnapshotLayers = requestURI.contains("includeSnapshotLayers=false");                	
						bimage = kmzUtils.getCustomMapPaneBufferedImage(! ignoreSnapshotLayers);
					} catch (Exception e) {
						e.printStackTrace();
						throw new IOException(e);
					}
					ImageIO.write(
							bimage,
							"png", baos);
					responseBody.write(baos.toByteArray());
					responseBody.close();
					return;
				}




				// likely unused stuff below

				String params = requestURI.substring(requestURI.indexOf("?")+1);
				System.out.println("params = "+params);
				String bboxParam = params.split(";")[0];
				String bbox = bboxParam.substring(bboxParam.indexOf("BBOX=")+5);
				String widthParam = params.split(";")[1];
				String width = widthParam.substring(widthParam.indexOf("WIDTH=")+6);
				String heightParam = params.split(";")[2];
				String height = heightParam.substring(heightParam.indexOf("HEIGHT=")+7);




				System.out.println(bbox);
				String[] cornerArray = bbox.split(",");
				//          /kml?BBOX=-180,-56.92616410826921,180,90  (West, South, East, North)
				Rectangle2D.Double extent = new Rectangle2D.Double(
						Double.parseDouble(cornerArray[0]),
						Double.parseDouble(cornerArray[1]),
						Double.parseDouble(cornerArray[2])-Double.parseDouble(cornerArray[0]),
						Double.parseDouble(cornerArray[3])-Double.parseDouble(cornerArray[1]));

				System.out.println(extent);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BufferedImage bimage = null;
				try {
					bimage = kmzUtils.getFreshMapPaneBufferedImage(extent, Integer.parseInt(width), Integer.parseInt(height));
				} catch (Exception e) {
					e.printStackTrace();
					throw new IOException(e);
				}
				ImageIO.write(
						//                    ImageIO.read(new URL("http://l.yimg.com/a/i/us/sp/fn/default/full/add.gif")),
						bimage,
						"png", baos);
				responseBody.write(baos.toByteArray());
				//      Set<String> keySet = requestHeaders.keySet();
				//      Iterator<String> iter = keySet.iterator();
				//      while (iter.hasNext()) {
				//        String key = iter.next();
				//        List values = requestHeaders.get(key);
				//        String s = key + " = " + values.toString() + "\n";
				//        responseBody.write(s.getBytes());
				//      }
				responseBody.close();
			}
		}
	}




	class WCTLegendHandler implements HttpHandler {
		private WCTViewer viewer = null;
		private ViewerKmzUtilities kmzUtils = null;

		public WCTLegendHandler(WCTViewer viewer) {
			this.viewer = viewer;
			this.kmzUtils = new ViewerKmzUtilities(viewer);
		}

		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			if (requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "image/png");
				exchange.sendResponseHeaders(200, 0);

				OutputStream responseBody = exchange.getResponseBody();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BufferedImage bimage = null;
				try {
					bimage = kmzUtils.getMetaBufferedImage();
					if (bimage == null) {
						responseBody.close();
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					responseBody.close();
					return;
				}
				ImageIO.write(
						bimage,
						"png", baos);
				responseBody.write(baos.toByteArray());
				responseBody.close();
			}
		}
	}







	class WCTArcLayerHandler implements HttpHandler {
		private WCTViewer viewer = null;
		private ViewerKmzUtilities kmzUtils = null;

		public WCTArcLayerHandler(WCTViewer viewer) {
			this.viewer = viewer;
			this.kmzUtils = new ViewerKmzUtilities(viewer);
		}

		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			if (requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "image/png");
				exchange.sendResponseHeaders(200, 0);


				String requestURI = exchange.getRequestURI().toString();
				String params = requestURI.substring(requestURI.indexOf("?")+1);
				System.out.println("params = "+params);




				OutputStream responseBody = exchange.getResponseBody();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BufferedImage bimage = null;
				try {
					bimage = kmzUtils.getMetaBufferedImage();
				} catch (Exception e) {
					e.printStackTrace();
					throw new IOException(e);
				}
				ImageIO.write(
						bimage,
						"png", baos);
				responseBody.write(baos.toByteArray());
				responseBody.close();
			}
		}
	}







	class WCTWmsHandler implements HttpHandler {
		private WCTViewer viewer = null;
		private ViewerKmzUtilities kmzUtils = null;

		private boolean waiting = true;

		private Timer timer = new Timer();
		private TimerTask task;
		private static final int WAIT_TIME_IN_MILLIS = 1000;


		public WCTWmsHandler(WCTViewer viewer) {
			this.viewer = viewer;
			this.kmzUtils = new ViewerKmzUtilities(viewer);
		}

		public void handle(HttpExchange exchange) throws IOException {

			//        if (task != null) {
			//            try {
			////                exchange.sendResponseHeaders(404, 0);
			//                task.cancel();
			//            } catch (Exception e) {
			//            }
			//        }
			//
			//        startWmsTimer(exchange);

			processWmsRequest(exchange);
		}


		private synchronized void startWmsTimer(final HttpExchange exchange) {

			task = new TimerTask() {
				public void run() {
					//                System.out.println(" EXECUTING ZOOM CHANGE EVENT ");
					try {
						processWmsRequest(exchange);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};

			timer.purge();
			timer.schedule(task , WAIT_TIME_IN_MILLIS);        
		}


		private void processWmsRequest(HttpExchange exchange) throws IOException {


			// this will most likely be called twice for each zoom change
			// 1st - pull in current view before data refresh
			// 2nd - after data refresh, renderComplete call and refresh from the javascript client


			String requestMethod = exchange.getRequestMethod();
			if (requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "image/png");
				//            responseHeaders.set("Content-Type", "image/gif");
				exchange.sendResponseHeaders(200, 0);

				final OutputStream responseBody = exchange.getResponseBody();
				//            Headers requestHeaders = exchange.getRequestHeaders();

				String requestURI = exchange.getRequestURI().toString();

				System.out.println(requestURI);

				try {

					String params = requestURI.substring(requestURI.indexOf("?")+1);
					System.out.println("params = "+params);
					String[] paramArray = params.split("&");
					HashMap<String, String> keyValueMap = new HashMap<String, String>(paramArray.length);
					for (String keyValueParam : paramArray) {
						try {
							keyValueMap.put(keyValueParam.split("=")[0].toUpperCase(), keyValueParam.split("=")[1]);
						} catch (Exception e) {

						}
					}

					//            String bboxParam = paramArray;
					//            String bbox = bboxParam.substring(bboxParam.indexOf("BBOX=")+5);
					//            String widthParam = params.split(";")[1];
					//            String width = widthParam.substring(widthParam.indexOf("WIDTH=")+6);
					//            String heightParam = params.split(";")[2];
					//            String height = heightParam.substring(heightParam.indexOf("HEIGHT=")+7);


					String bbox = keyValueMap.get("BBOX").replaceAll("%2C", ",");
					String width = keyValueMap.get("WIDTH");
					String height = keyValueMap.get("HEIGHT");

					viewer.getMapPane().setSize(Integer.parseInt(width), Integer.parseInt(height));


					System.out.println(bbox);
					System.out.println(viewer.getMapPane().getSize().toString());
					String[] cornerArray = bbox.split(",");
					//          /kml?BBOX=-180,-56.92616410826921,180,90  (West, South, East, North)
					Rectangle2D.Double extent = new Rectangle2D.Double(
							Double.parseDouble(cornerArray[0]),
							Double.parseDouble(cornerArray[1]),
							Double.parseDouble(cornerArray[2])-Double.parseDouble(cornerArray[0]),
							Double.parseDouble(cornerArray[3])-Double.parseDouble(cornerArray[1]));

					System.out.println(extent);

					viewer.setCurrentExtent(extent);

					BufferedImage bimage = null;
					try {
						//                bimage = kmzUtils.getFreshMapPaneBufferedImage(extent, Integer.parseInt(width), Integer.parseInt(height));
						bimage = kmzUtils.getCustomMapPaneBufferedImage();
					} catch (Exception e) {
						e.printStackTrace();
						throw new IOException(e);
					}
					ImageIO.write(bimage, "png", responseBody);
					responseBody.close();

					//            
					//            
					//            final RenderCompleteListener renderListener = new RenderCompleteListener() {
					//                @Override
					//                public void renderComplete() {
					//                    setWaiting(false);
					//                }
					//                @Override
					//                public void renderProgress(int progressPercent) {
					//                }
					//            };
					//            
					//            setWaiting(true);
					//            viewer.addRenderCompleteListener(renderListener);
					//
					//            
					////          viewer.getNexradMapPaneZoomChange().setActive(false);
					//            viewer.setCurrentExtent(extent);
					////            viewer.getNexradMapPaneZoomChange().setActive(true);
					////            viewer.refreshDataWithWait(true);
					//            
					//            synchronized (this) {
					//                int timeoutCount = 0;
					//                while (isWaiting()) {
					//                    try {
					//                        System.out.println("waiting for wms response...");
					////                        this.wait();
					//                        Thread.sleep(100);
					//                        timeoutCount++;
					//                        if (timeoutCount == 300) {
					//                            setWaiting(false);
					//                        }
					//                    } catch (InterruptedException e) {
					//                        e.printStackTrace();
					//                    }
					//                }
					//                handleResponse(responseBody);
					//                setWaiting(true);
					//                viewer.removeRenderCompleteListener(renderListener);
					//            }            
					//
					//                
					////                viewer.refreshCurrentView();
					//                
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		private void handleResponse(final OutputStream responseBody) throws IOException {


			BufferedImage bimage = null;
			try {
				//          bimage = kmzUtils.getFreshMapPaneBufferedImage(extent, Integer.parseInt(width), Integer.parseInt(height));
				bimage = kmzUtils.getCustomMapPaneBufferedImage();
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException(e);
			}
			ImageIO.write(
					bimage,
					"png", responseBody);
			responseBody.close();

		}


		private boolean isWaiting() {
			return waiting;
		}

		private void setWaiting(boolean waiting) {
			this.waiting = waiting;
		}

	}


}
