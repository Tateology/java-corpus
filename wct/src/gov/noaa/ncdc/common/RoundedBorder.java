package gov.noaa.ncdc.common;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.border.Border;

public class RoundedBorder implements Border {
    
    private int radius;
    private int thickness;
    private Color color;
    
    public RoundedBorder(Color color, int radius, int thickness) {
        this.radius = radius;
        this.thickness = thickness;
        this.color = color;
    }
    
    
    
//    @Override
    public Insets getBorderInsets(Component c) {
//        return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        return new Insets(this.radius, this.radius, this.radius, this.radius);
    }

//    @Override
    public boolean isBorderOpaque() {
        return true;
    }

//    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(new BasicStroke(thickness));
        g2.setColor(color);
        
        g2.drawRoundRect(x+1,y+1,width-2,height-2,radius,radius);
        
//        for (int n=0; n<thickness; n++) {
//            g.drawRoundRect(x+n,y+n,width-1-(n*2),height-1-(n*2),radius,radius);
//        }
        
//        g2.dispose();
    }
}