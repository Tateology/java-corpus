package gov.noaa.ncdc.nexradiv.legend;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class TestLegendImageProducer {

    public static void main(String[] args) {

        try {

            CategoryLegendImageProducer legend = new CategoryLegendImageProducer();
            legend.setDataType("NEXRAD Level-III");
            legend.setDataDescription(new String[] {
                    "RADIAL VELOCITY",
                    "KGSP - GREER, SC"
            });
            legend.setDateTimeInfo("11/10/2001 22:58:57 GMT");
            legend.setMainMetadata(new String[] {
                    "LAT: 41/24/46 N",
                    "LON: 81/51/35 W",
                    "ELEV: 860.0 FT",
                    "MODE/VCP: A / 21"
            });
            legend.setSpecialMetadata(new String[] {
                    "ELEV ANGLE: 0.50 DEG",
                    "MAX NEG: -105 KT",
                    "MAX POS: 92 KT"
            });
            
            legend.setLegendTitle(new String[] { "Legend (dBZ)" , "Legend Line 2"});
            
            legend.setCategoryLabels(new String[] {
                    "75", "70", "65", "60", "55", "50", "45", "40", "35", "30", "25", "20", "15", "10", "5"
            });
            
            legend.setCategoryColors(new Color[] {
                    new Color(235, 235, 235),     
                    new Color(153,  85, 201),
                    new Color(255,   0, 255),
                    new Color(192,   0,   0),
                    new Color(214,   0,   0),
                    new Color(255,   0,   0),
                    new Color(255, 144,   0),
                    new Color(231, 192,   0),
                    new Color(255, 255,   0),
                    new Color(  0, 144,   0),
                    new Color(  0, 200,   0),
                    new Color(  0, 255,   0),
                    new Color(  0,   0, 246),
                    new Color(  1, 160, 246),
                    new Color(  0, 236, 236)
            });
            
            
            {
                ImageIcon icon;
                icon = new ImageIcon(legend.createLargeLegendImage(new Dimension(180, 600)));

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new JLabel(icon));
                frame.pack();
                frame.setVisible(true);
            }
            {
                ImageIcon icon;
                icon = new ImageIcon(legend.createMediumLegendImage(new Dimension(450, 100)));

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new JLabel(icon));
                frame.pack();
                frame.setVisible(true);
            }
            
            legend.setInterpolateBetweenCategories(true);
            legend.setDrawBorder(true);
            
            {
                ImageIcon icon;
                icon = new ImageIcon(legend.createLargeLegendImage(new Dimension(180, 600)));

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new JLabel(icon));
                frame.pack();
                frame.setVisible(true);
            }
            {   
                ImageIcon icon;
                icon = new ImageIcon(legend.createMediumLegendImage(new Dimension(450, 100)));

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new JLabel(icon));
                frame.pack();
                frame.setVisible(true);
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
