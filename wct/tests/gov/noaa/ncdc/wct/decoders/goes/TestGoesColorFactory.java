package gov.noaa.ncdc.wct.decoders.goes;

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.nexradiv.legend.LegendCategoryFactory;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.junit.Test;

public class TestGoesColorFactory {

    @Test
    public void testGetColors() throws IllegalAccessException, InstantiationException, Exception {

        GoesRemappedRaster goes = new GoesRemappedRaster();
        GoesColorFactory gcf = GoesColorFactory.getInstance();
        gcf.calculateEqualColorsAndValues(goes);
    }
    
    
    
    
    
    public static void main(String[] args) throws Exception {
        
        
        CategoryLegendImageProducer legend = new CategoryLegendImageProducer();
        legend.setDataType("GOES GVAR");
        legend.setDataDescription(new String[] {
                "BAND 3",
                "GOES EAST"
        });
        legend.setDateTimeInfo("11/10/2001 22:58:57 GMT");
        legend.setMainMetadata(new String[] {
                "--1--",
                "--2--",
                "--3--",
                "--4--"
        });
        legend.setSpecialMetadata(new String[] {
                "--5--",
                "--6--",
                "--7--"
        });
        
        legend.setLegendTitle(new String[] { "Legend (dBZ)" , "Legend Line 2"});
       
        GoesRemappedRaster raster = new GoesRemappedRaster();
        legend.setCategoryColors(LegendCategoryFactory.getCategoryColors(raster));
        legend.setCategoryLabels(LegendCategoryFactory.getCategoryLabels(raster));
        legend.setInterpolateBetweenCategories(true);
        legend.setLabelEveryOtherN(20);
        {   
            ImageIcon icon;
            icon = new ImageIcon(legend.createMediumLegendImage(new Dimension(240, 100)));

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new JLabel(icon));
            frame.pack();
            frame.setVisible(true);
        }
    }
}
