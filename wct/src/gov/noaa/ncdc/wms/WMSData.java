/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wms;

import gov.noaa.ncdc.common.DataTransfer;
import gov.noaa.ncdc.common.DataTransferListener;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.jai.RasterFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *  Loads Images Data from OGC WMS (Open-GIS Compliant Web Map Servers) into GridCoverages. 
 *  <br><br> Examples are aerial photography or topo map data from TerraServer 
 *  website and shaded relief from USGS.
 *  <br><br>
 *  Example URLs: <br>
 *
 *  TerraServer: http://terraserver-usa.com/ogcmap.ashx?&version=1.1.1
 *    &request=GetMap&layers=DOQ&styles=&srs=EPSG:4326&format=jpeg
 *    &transparent=true&width=600&height=600&bbox=-90.1,35.0,-90.0,35.1
 *
 *  USGS: http://gisdata.usgs.net/servlet/com.esri.wms.Esrimap?
 *    ServiceName=USGS_WMS_NED&VERSION=1.1.1&REQUEST=map
 *    &Layers=US_NED_Shaded_Relief&SRS=EPSG:4326&REASPECT=false
 *    &format=jpeg&transparent=true&width=600&height=600&bbox=-90.1,35.0,-90.0,35.1
 *  <br><br>
 *  TerraServer address: <a href="http://terraserver-usa.com/">http://terraserver-usa.com/</a>
 *
 * @author    steve.ansari
 */
public class WMSData {



    private Vector<DataTransferListener> dataTransferListeners = new Vector<DataTransferListener>();
    private DataTransfer dataTransfer;
    private int transferSize = 0;
    public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

    private URL lastURL = null;
    private java.awt.geom.Rectangle2D.Double lastMapExtent = null;
    private boolean lastIsBW = true;
    private int lastTransparency = 0;
    private Color lastEmptyBackgroundColor = null;
    private GridCoverage lastGC = null;
    
    

    /**
     *  Gets a GridCoverage of the specified type from OGC WMS. <br><br>
     *
     * @param  name                           Image type, specified by name attribute (AERIALPHOTO, TOPOMAP, NONE)
     * @param  layers                         URL parameter string of layers (ex: &roads,labels,rivers )
     * @param  mapExtent                      Lat/Lon extent of desired image
     * @param  imageSize                      Size of desired image
     * @return                                The GridCoverage object
     * @exception  WMSException               Thrown if no connection to WMS
     * @exception  Exception                  General Exception
     */
    public GridCoverage getGridCoverage(String name, String layers, 
            java.awt.geom.Rectangle2D.Double mapExtent,
            java.awt.Rectangle imageSize)
    throws WMSException, Exception {

        return getGridCoverage(name, layers, false, mapExtent, imageSize);
    }




    /**
     *  Gets a GridCoverage of the specified type from OGC WMS. <br><br>
     *
     * @param  name                           Image type, specified by name attribute (AERIALPHOTO, TOPOMAP, NONE)
     * @param  layers                         URL parameter string of layers (ex: &roads,labels,rivers )
     * @param  isBW                           Should image be converted to grayscale?   Default is color.
     * @param  mapExtent                      Lat/Lon extent of desired image
     * @param  imageSize                      Size of desired image
     * @return                                The GridCoverage object
     * @exception  WMSException               Thrown if no connection to WMS
     * @exception  Exception                  General Exception
     */
    public GridCoverage getGridCoverage(String name, String layers, boolean isBW, 
            java.awt.geom.Rectangle2D.Double mapExtent,
            java.awt.Rectangle imageSize)
    throws WMSException, Exception {

        String urlString = null;
        String server = null;
        String wmsLayers = null;
        String transparent = null;
        String imageFormat = null;
        String version = null;

        try {

            if (name == null || name.equalsIgnoreCase("none")) {
                return new GridCoverage("OGCWMS",
                        RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, 2, 2, 1, null),
                        new Envelope(new java.awt.geom.Rectangle2D.Double(179.0, 30.0, .001, .001))
                );
            }
            
            WMSRequestInfo wms = WMSConfigManager.getInstance().getWmsRequestInfo(name);
            
            server = wms.getCapabilitiesURL();
            imageFormat = wms.getImageType();
            wmsLayers = wms.getDefaultLayers();
            transparent = wms.getTransparent();
            version = wms.getVersion();

            if (wmsLayers == null || wmsLayers.length() == 0) {
                throw new WMSException("No layers selected!");
            }

            String queryDelim = "?";
            if (server.endsWith("?")) {
                queryDelim = "";
            }
            else if (server.contains("?")) {
                queryDelim = "&";
            }
                
            
            
            urlString = server + queryDelim +
            "VERSION=" + version +
            "&REQUEST=GetMap&LAYERS=" + wmsLayers +
            "&STYLES=&SRS=EPSG:4326" +
            "&FORMAT=" + imageFormat;

            if (transparent != null) {
                urlString = urlString + "&TRANSPARENT=" + transparent;
            }

            urlString = urlString +
            "&WIDTH=" + (int) (imageSize.getWidth()) +
            "&HEIGHT=" + (int) (imageSize.getHeight()) +
            "&BBOX=" + mapExtent.getX() + "," + mapExtent.getY() + "," +
            (mapExtent.getX() + mapExtent.getWidth()) + "," +
            (mapExtent.getY() + mapExtent.getHeight());

        } catch (Exception e) {
            throw e;
        }

        // example URL:
        // http://terraserver-usa.com/ogcmap.ashx?&version=1.1.1&request=GetMap&layers=DRG&styles=&srs=EPSG:4326&format=png&transparent=false&WIDTH=775&HEIGHT=455&BBOX=-84.52710723876953,38.98346094931325,-77.09584331512451,43.34633202706613
        return getGridCoverage(new URL(urlString), mapExtent, isBW);
    }





    public GridCoverage getGridCoverage(URL url, Rectangle2D.Double mapExtent, boolean isBW) throws WMSException {
        return getGridCoverage(url, mapExtent, isBW, 255, null);
    }

    /**
     * Get a grid coverage from an URL
     * @param url              URL for the image
     * @param mapExtent        Geographic Bounds of the image
     * @param isBW             Convert image to black and white (Grayscale)
     * @param transparency     Between 0 and 255 where 0 is fully transparent
     * @param emptyBackgroundColor  The background color that should be converted to fully transparent
     * @return
     * @throws WMSException
     */
    public GridCoverage getGridCoverage(URL url, Rectangle2D.Double mapExtent, boolean isBW, 
            int transparency, Color emptyBackgroundColor) 
    throws WMSException {

        
        
        if (this.lastURL != null && url.equals(this.lastURL) && 
                this.lastMapExtent != null && mapExtent.equals(this.lastMapExtent) && 
                isBW == this.lastIsBW &&
                transparency == this.lastTransparency && 
                (this.lastEmptyBackgroundColor == null || emptyBackgroundColor.equals(this.lastEmptyBackgroundColor))) {
            
            return this.lastGC;
        }


        BufferedImage bimage = null;
        try {
            System.out.println("WMS URL: " + url.toString());

            if (dataTransferListeners.size() > 0) {
                bimage = ImageIO.read(getDataTransferInputStream(url));
            }
            else {
                bimage = ImageIO.read(url);
            }
        } catch (java.io.IOException ioe) {
            throw new WMSException(url.getProtocol()+"://"+url.getHost()+" is currently unavailable.");
        }

        if (isBW) {
            bimage = convertToGrayscale(bimage);
        }
        if (transparency < 255) {
            bimage = getTransparentImage(bimage, transparency, emptyBackgroundColor);
        }

        // Check for empty image
        if (bimage == null) {
            throw new WMSException("No image available at this zoom extent.");
        }

        Envelope envelope = new Envelope(mapExtent);

        
        this.lastURL = url;
        this.lastMapExtent = mapExtent;
        this.lastIsBW = isBW;
        this.lastTransparency = transparency;
        this.lastEmptyBackgroundColor = emptyBackgroundColor;
        this.lastGC = new GridCoverage("WMSGC", bimage, GeographicCoordinateSystem.WGS84, envelope);
        
        return this.lastGC;

    }


    public static BufferedImage convertToGrayscale(BufferedImage bimage) {

        BufferedImage grayImage = new BufferedImage(bimage.getWidth(), bimage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = (Graphics2D)grayImage.getGraphics();
        g2.drawRenderedImage(bimage, null);
        g2.dispose();
        return grayImage;
    }

    public static BufferedImage getTransparentImage(BufferedImage bimage, int alphaValue, Color backgroundColor) {

        int w = bimage.getWidth(null);
        int h = bimage.getHeight(null);
        int[] rgbs = new int[w*h];

        int index = 0;
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                Color color = new Color(bimage.getRGB(i, j));

                if (color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0){
                    color = TRANSPARENT_COLOR;                   
                }
                else if (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255){
                    color = TRANSPARENT_COLOR;
                }
                else {
                    color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaValue);
                }

                rgbs[index++] = color.getRGB();
            }
        }

        bimage.setRGB(0, 0, w, h, rgbs, 0, w);
        return bimage;
    }

    public static String[] getLayerList(String name)
    throws ParserConfigurationException, SAXException, IOException, Exception {

        String serverURL = WMSConfigManager.getInstance().getWmsRequestInfo(name).getCapabilitiesURL();


        try {
            serverURL += "&REQUEST=GETCAPABILITIES";

            System.out.println("WMS URL: " + serverURL);

            // From http://www.developerfusion.com/show/2064/
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (serverURL);

            // normalize text representation
            doc.getDocumentElement ().normalize ();
            System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());


            NodeList listOfLayers = doc.getElementsByTagName("Layer");
            int totalLayers = listOfLayers.getLength();
            System.out.println("Total no of layers : " + totalLayers);

            // Prepare return array
            String[] layers = new String[totalLayers];

            for(int s=0; s<listOfLayers.getLength() ; s++) {

                Node layerNode = listOfLayers.item(s);
                if(layerNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element layerElement = (Element)layerNode;
                    //-------
                    Element nameElement = (Element)(layerElement.getElementsByTagName("Name").item(0));

                    NodeList nameList = nameElement.getChildNodes();
                    String layerName = ((Node)nameList.item(0)).getNodeValue().trim();
                    System.out.println("Layer "+s+" : " + layerName);

                    layers[s] = layerName;
                }

            }


            return layers;


        } catch (Exception e) {
            throw new WMSException(serverURL+" is currently unavailable.");
        }      

    }




    public void addDataTransferListeners(DataTransferListener dataTransferListener) {
        dataTransferListeners.add(dataTransferListener);
    }
    public Vector<DataTransferListener> getDataTransferListeners() {
        return dataTransferListeners;
    }
    public void clearDataTransferListeners() {
        dataTransferListeners.clear();
    }



    public InputStream getDataTransferInputStream(URL url) throws IOException {

        URLConnection conn = url.openConnection();
        conn.getContentType();
        transferSize = conn.getContentLength();

        InputStream in = conn.getInputStream();       
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        dataTransfer = new DataTransfer(in, out, 2048);
        for (int n=0; n<dataTransferListeners.size(); n++) {
            dataTransfer.addDataTransferListener((DataTransferListener)dataTransferListeners.get(n));
        }
        dataTransfer.run();
        return new ByteArrayInputStream(out.toByteArray());
    }

    public DataTransfer getDataTransfer() {
        return dataTransfer;
    }

    public int getDataTransferProgress() {
        if (dataTransfer != null) {
            //System.out.println("TRANSFERRED: "+dataTransfer.getTransferred()+" OF "+transferSize);
            return (int)((dataTransfer.getTransferred()*100.0)/transferSize);
        }
        else {
            return 0;
        }
    }




}

