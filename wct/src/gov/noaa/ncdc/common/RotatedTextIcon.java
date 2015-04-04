package gov.noaa.ncdc.common;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;

public class RotatedTextIcon implements Icon
{
    public static final int NONE = 0;
    public static final int CW = 1;
    public static final int CCW = 2;

    //{{{ RotatedTextIcon constructor
    public RotatedTextIcon(int rotate, Font font, String text)
    {
        this.rotate = rotate;
        this.font = font;

        FontRenderContext fontRenderContext
        = new FontRenderContext(null,true,true);
        this.text = text;
        glyphs = font.createGlyphVector(fontRenderContext,text);
        width = (int)glyphs.getLogicalBounds().getWidth() + 4;
        //height = (int)glyphs.getLogicalBounds().getHeight();

        LineMetrics lineMetrics = font.getLineMetrics(text,fontRenderContext);
        ascent = lineMetrics.getAscent();
        height = (int)lineMetrics.getHeight();

        renderHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        renderHints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
    } //}}}

    //{{{ getIconWidth() method
    public int getIconWidth()
    {
        return (int)(rotate == RotatedTextIcon.CW
                || rotate == RotatedTextIcon.CCW
                ? height : width);
    } //}}}

    //{{{ getIconHeight() method
    public int getIconHeight()
    {
        return (int)(rotate == RotatedTextIcon.CW
                || rotate == RotatedTextIcon.CCW
                ? width : height);
    } //}}}

    //{{{ paintIcon() method
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setFont(font);
        AffineTransform oldTransform = g2d.getTransform();
        RenderingHints oldHints = g2d.getRenderingHints();

        g2d.setRenderingHints(renderHints);
        g2d.setColor(c.getForeground());

        //{{{ No rotation
        if(rotate == RotatedTextIcon.NONE)
        {
            g2d.drawGlyphVector(glyphs,x + 2,y + ascent);
        } //}}}
        //{{{ Clockwise rotation
        else if(rotate == RotatedTextIcon.CW)
        {
            AffineTransform trans = new AffineTransform();
            trans.concatenate(oldTransform);
            trans.translate(x,y + 2);
            trans.rotate(Math.PI / 2,
                    height / 2, width / 2);
            g2d.setTransform(trans);
            g2d.drawGlyphVector(glyphs,(height - width) / 2,
                    (width - height) / 2
                    + ascent);
        } //}}}
        //{{{ Counterclockwise rotation
        else if(rotate == RotatedTextIcon.CCW)
        {
            AffineTransform trans = new AffineTransform();
            trans.concatenate(oldTransform);
            trans.translate(x,y - 2);
            trans.rotate(Math.PI * 3 / 2,
                    height / 2, width / 2);
            g2d.setTransform(trans);
            g2d.drawGlyphVector(glyphs,(height - width) / 2,
                    (width - height) / 2
                    + ascent);
        } //}}}

        g2d.setTransform(oldTransform);
        g2d.setRenderingHints(oldHints);
    } //}}}

    //{{{ Private members
    private int rotate;
    private Font font;
    private String text;
    private GlyphVector glyphs;
    private float width;
    private float height;
    private float ascent;
    private RenderingHints renderHints;
    //}}}
} //}}}

