package gov.noaa.ncdc.wct.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipException;

import org.geotools.map.MapLayer;
import org.junit.Test;

public class NhcTracksTest {

    @Test
    public void testGetLatestUrlList() throws MalformedURLException {
        NhcTracks nhc = new NhcTracks();
        ArrayList<URL> list = nhc.getLatestUrlList();
        System.out.println(list);
    }
    
    @Test
    public void testGetCachedShapefileLatestUrlList() throws ZipException, IOException {
        NhcTracks nhc = new NhcTracks();
        ArrayList<URL> list = nhc.getCachedShapefileLatestUrlList();
        System.out.println(list);
    }

    
    @Test
    public void testGetTrackLineLayer() throws IOException {
        NhcTracks nhc = new NhcTracks();
        ArrayList<URL> list = nhc.getCachedShapefileLatestUrlList();
        
        MapLayer mapLayer = nhc.getTrackLineLayer(list);
    }

}
