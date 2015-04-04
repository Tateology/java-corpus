package gov.noaa.ncdc.wct.morph;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.util.Vector;

/**
 * File belongs to javamorph (Merging of human-face-pictures).
 * Copyright (C) 2009 - 2010  Claus Wimmer
 * See file ".../help/COPYING" for details!
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA * 
 *
 * @version 1.5
 * <br/>
 * @author claus.erhard.wimmer@googlemail.com
 * <br/>
 * Program: JavaMorph.
 * <br/>
 * Class: CFrame.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: JComponent to show either the left picture of the right one.
 * <br/>
 * Hint: Paint operation also performed by the four decorators (a pattern).
 */
public class MorphInput {
    /** Java API. */
    private static final long serialVersionUID = 1L;
    /** Picture's mesh. */
    private Vector<Point> mesh;
    /** Picture's clip polygon points. */
    private Vector<Point> polygon;
    /** Picture itself. */
    private WritableRaster image;
    /** Polygon's smoothed clip matrix. */
//    protected double clip[][];
//    /** File to store a debug copy of the clip matrix. */
//    protected File f_clip;
    /** 
     * Constructor.
     * 
     * @param parent Main JFrame.
     * @param mesh Picture's mesh.
     * @param polygon Clip polygon points.
     * @param image Picture itself.
     * @param clip Smoothed clip matrix.
     * @param f_mesh File to store to mesh to.
     * @param f_polygon File to store the polygon to.
     * @param f_clip File to debug the smoothed clip matrix to.
     */
    public MorphInput(Vector<Point> mesh,
            Vector<Point> polygon,
            WritableRaster image
//            double clip[][],
//            File f_mesh,
//            File f_polygon,
//            File f_clip){
            ) {
        this.mesh = mesh;
        this.polygon = polygon;
        this.image = image;
//        this.clip = clip;
//        this.f_clip = f_clip;
    }
//    /**
//     * Return the position & the size of the drawn image within the component.   
//     * @return Bounds in screen pixel units relating to this component.
//     */
    public Rectangle getImageBounds(){
//        return this.dpicture.getBounds();
    	return new Rectangle(this.image.getWidth(), this.image.getHeight());
    }
//    public void genClip(){
//        this.dpolygon.genClip();
//    }
    
    
    /**
     * Scale a screen point to a picture point.
     * @param p Point in screen resolution units.
     */
    public void scalePoint(Point p){
        /* Picture area on JComponent. */
        Rectangle bounds = getImageBounds();
        /* Picture's own size. */
        int w = image.getWidth(), h = image.getHeight();
        /* Scale. */
        double x, y;
        x = (p.x - bounds.x) * w / bounds.width;
        y = (p.y - bounds.y) * h / bounds.height;
        x = Math.max(0, Math.min(w, x));
        y = Math.max(0, Math.min(h, y));
        p.x = (int)x;
        p.y = (int)y;
        /* Limit relating the picture's own size. */
        p.x = Math.max(0, Math.min(image.getWidth() - 1, p.x));
        p.y = Math.max(0, Math.min(image.getHeight() - 1, p.y));
    }
    /** 
     * Scale mark & cursor size simultan.
     * (Regression.)
     * 
     * @return Mark size to be draw by scaling decorator.
     */
//    public int scaleMarkSize(){
//        /* Bounds = screen pixel size.*/
//        /* Image = origin pixel size.*/
//        return 
//            CConfig.MARK_SIZE * 
//            image.getWidth() /
//            getImageBounds().width;
//    }
//    /** 
//     * Delete content of mesh.
//     * Initialize matrix controlled by configuration_s number of rows & columns.
//     */
//    public void initMesh(){
//        dmesh.init();
//    }
//    /** 
//     * Delete polygon.
//     * Initialize controlled by configuration's number of points.
//     */
//    public void initPolygon(){
//        dpolygon.init();
//    }
	public void setImage(WritableRaster image) {
		this.image = image;
	}
	public WritableRaster getImage() {
		return image;
	}
	public void setMesh(Vector<Point> mesh) {
		this.mesh = mesh;
	}
	public Vector<Point> getMesh() {
		return mesh;
	}
	public void setPolygon(Vector<Point> polygon) {
		this.polygon = polygon;
	}
	public Vector<Point> getPolygon() {
		return polygon;
	}
}
