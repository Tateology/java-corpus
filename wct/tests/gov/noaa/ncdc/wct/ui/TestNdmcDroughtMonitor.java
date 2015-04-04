package gov.noaa.ncdc.wct.ui;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.junit.Test;

public class TestNdmcDroughtMonitor {

    @Test
    public void testGetDates() throws Exception {
        NdmcDroughtMonitor ndm = new NdmcDroughtMonitor();
        ArrayList<String> dates = ndm.getDates();
        for (int n=0; n<dates.size(); n++) {
            System.out.println(dates.get(n));
        }
    }
    
    @Test
    public void testGetWmsUrls() throws Exception {
        NdmcDroughtMonitor ndm = new NdmcDroughtMonitor();
        ArrayList<String> dates = ndm.getDates();
        for (int n=0; n<dates.size(); n++) {
            ndm.setYYMMDD(dates.get(n));
            System.out.println(ndm.getWmsUrl(new Rectangle2D.Double(-165.0, 15.0, 105.0, 65.0)));
        }
    }
    
    
    @Test
    public void testGetWmsImages() throws Exception {
        NdmcDroughtMonitor ndm = new NdmcDroughtMonitor();
        ArrayList<String> dates = ndm.getDates();
//        for (int n=0; n<dates.size(); n++) {
        for (int n=0; n<dates.size(); n=n+1000) {
            ndm.setYYMMDD(dates.get(n));
            System.out.println("GETTING WMS IMAGE FOR: "+dates.get(n));
            ImageIO.read(ndm.getWmsUrl(new Rectangle2D.Double(-165.0, 15.0, 105.0, 65.0)));
        }
    }

    @Test
    public void testLegend() throws Exception {
        NdmcDroughtMonitor ndm = new NdmcDroughtMonitor();
        Image image = ndm.getLegendImage();
        
        ImageIcon icon = new ImageIcon(image);
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.add(new JLabel(icon));
        dialog.pack();
        dialog.setVisible(true);
    }
}
