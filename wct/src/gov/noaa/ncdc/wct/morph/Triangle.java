package gov.noaa.ncdc.wct.morph;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
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
 * Class: CTriangle.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: Data structure for triangulation of the mesh.
 * <br/> 
 * Hint: Relating to picture pixel units.
 */
public class Triangle implements Comparator<Point>{
    /** All three corners of the triangle. (not sorted) */
    private Point p[] = new Point[]{new Point(), new Point(), new Point()};
    /** All three corners of the triangle. (sorted by comparator) */
    private Point c[] = new Point[]{new Point(), new Point(), new Point()};
    /** All pixel points within the triangle. */
    private Point withins[];
    /** Rectangular border of the triangle. */
    private int y_min = Integer.MAX_VALUE;
    /** Rectangular border of the triangle. */
    private int y_max = Integer.MIN_VALUE;
    /** Rectangular border of the triangle. */
    private int x_min;
    /** Rectangular border of the triangle. */
    private int x_max;
    /**
     * Copy constructor.
     * @param _p0 First of the three points.
     * @param _p1 Second of the three points.
     * @param _p2 Third of the three points
     */
    public Triangle(Point _p0, Point _p1, Point _p2){
       Vector<Point> v = new Vector<Point>();
        p[0].x = _p0.x;
        p[0].y = _p0.y;
        p[1].x = _p1.x;
        p[1].y = _p1.y;
        p[2].x = _p2.x;
        p[2].y = _p2.y;
        c[0].x = _p0.x;
        c[0].y = _p0.y;
        c[1].x = _p1.x;
        c[1].y = _p1.y;
        c[2].x = _p2.x;
        c[2].y = _p2.y;
        Arrays.sort(c, this);
        /* Calculate the y range. */
        for(int i = 0;i < 3; ++i){
            this.y_min = Math.min(this.y_min, p[i].y);
            this.y_max = Math.max(this.y_max, p[i].y);
        }
        /* For all horizontal lines. */
        for(int y = this.y_min; y <= this.y_max; ++y){
            /* Calculate the x range. */
            this.calculateXBounds(y);
            /* For all points of the horizontal line. */
            for(int x = this.x_min; x <= this.x_max; ++x){
                /* Add within - point. */
                Point pt = new Point();
                pt.x = x;
                pt.y = y;
                v.add(pt);
            }
        }
        this.withins = new Point[v.size()];
        /* Make vector to array. */
        v.toArray(this.withins);
    }
    /**
     * Get function.
     * @return All points within the triangle.
     */
    public Point[] getWithins(){
        return this.withins;
    }    
    /** 
     * Get function.
     * @return All three corner points.
     */
    public Point[] getPoints(){
        return this.p;
    }
    /**
     * Draw the triangle to an image.
     * 
     * @param image Image to draw the lines to.
     */
    public void debug(BufferedImage image){
        drawLine(image, p[0], p[1]);
        drawLine(image, p[1], p[2]);
        drawLine(image, p[2], p[0]);
    }
    /**
     * Hash code calculated by sorted points.
     * See hash contract.
     */
    public int hashCode(){
        return (c[0].hashCode() + c[1].hashCode() + c[2].hashCode()) % 
            Integer.MAX_VALUE;
    }
    /**
     * Equal if the sorted points are equal.
     * See has contract.
     */
    public boolean equals(Object obj){
        Triangle other = (Triangle)obj;
        return c[0].equals(other.c[0]) && 
            c[1].equals(other.c[1]) && 
            c[2].equals(other.c[2]);
    }
    /**
     * Due to comparator API.
     * Used as sort order for the points.
     */
    public int compare(Point o1, Point o2) {
        if(o1.x == o2.x){
            return o1.y - o2.y;
        }else{
            return o1.x - o2.x;
        }
    }
    /**
     * Debug one line to an image.
     * @param image Image to draw to.
     * @param p1 First point.
     * @param p2 Second point.
     */
    private void drawLine(BufferedImage image, Point p1, Point p2){
        if(p1.equals(p2))return;
        int 
            x1 = p1.x,
            x2 = p2.x,
            y1 = p1.y,
            y2 = p2.y,
            dx = x2 - x1,
            dy = y2 - y1,
            param = Math.max(Math.abs(dx), Math.abs(dy)),
            x,
            y;
        for(int p = 0; p <= param; ++p){
            x = (int)(x1 + (double)p * dx / (double)param);
            y = (int)(y1 + (double)p * dy / (double)param);
            image.setRGB(x, y, 0xffff0000);
        }
    }
    /**
     * Calculate left & right edge point of the scan line. The scan line must 
     * cross two of the three edges. Seldom the scan line crosses one edge
     * + two end points of the other both edges.
     * @param y Vertical position of the scan line.
     */
    private void calculateXBounds(int y){
        int 
            x1 = getXIntersection(y, p[0], p[1]),
            x2 = getXIntersection(y, p[0], p[2]),
            x3 = getXIntersection(y, p[1], p[2]);
        if(Integer.MIN_VALUE == x1)
        {
            this.x_min = Math.min(x2, x3);
            this.x_max = Math.max(x2, x3);
        }else if(Integer.MIN_VALUE == x2){
            this.x_min = Math.min(x1, x3);
            this.x_max = Math.max(x1, x3);
        }else if(Integer.MIN_VALUE == x3){
            this.x_min = Math.min(x2, x1);
            this.x_max = Math.max(x2, x1);
        }else if(x1 != x2){
            this.x_min = Math.min(x2, x1);
            this.x_max = Math.max(x2, x1);
        }else if(x1 != x3){
            this.x_min = Math.min(x3, x1);
            this.x_max = Math.max(x3, x1);
        }else if(x3 != x2){
            this.x_min = Math.min(x2, x3);
            this.x_max = Math.max(x2, x3);
        }
    }
    /**
     * Test 
     * @param y Vertical position of the scan line.
     * @param p1 First point of the edge line.
     * @param p2 Second point of the edge line.
     * @return X coordinate of the intersection point between edge & scan line.
     */
    private int getXIntersection(int y, Point p1, Point p2){
        int quotient = p2.y - p1.y;
        double param = ((double)y - p1.y) / quotient;
        if(0.0 > param || 1.0 < param || 0 == quotient)return Integer.MIN_VALUE;
        return (int)(p1.x +  param  * ((double)p2.x - p1.x));
    }
}
