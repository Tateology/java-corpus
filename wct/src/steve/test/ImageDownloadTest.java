package steve.test;

import java.net.URL;
import java.net.URLConnection;

public class ImageDownloadTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            URL url = new URL("http://gisdata.usgs.net/servlet/com.esri.wms.Esrimap?ServiceName=USGS_WMS_NED&version=1.1.1&request=GetMap&layers=US_NED_Shaded_Relief&styles=&srs=EPSG:4326&format=jpeg&transparent=false&WIDTH=775&HEIGHT=455&BBOX=-77.44429779052734,41.86633960123985,-74.62901020050049,43.519185863771746");
            URLConnection conn = url.openConnection();
            double contentLength = (double)conn.getContentLength();
            System.out.println("DOWNLOAD SIZE="+contentLength);
            
        
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    

}
