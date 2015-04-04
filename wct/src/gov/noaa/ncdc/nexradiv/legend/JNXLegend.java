package gov.noaa.ncdc.nexradiv.legend;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public abstract class JNXLegend extends JPanel {

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    
    public abstract Dimension getPreferredSize();
}
