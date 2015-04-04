package gov.noaa.ncdc.wct.morph;

import java.awt.Point;

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
 * Class: CLine.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: Line on a pixel picture.
 * <br/>
 * Hint: Consisting of two points. Evaluation helper functions provided.
 */

public class Line {
    /** x of first point. */
    public int x1;
    /** x of second point. */
    public int x2;
    /** y of first point. */
    public int y1;
    /** y of second point. */
    public int y2;
    /**
     * Constructor with numbers.
     * 
     * @param x1 X of first point.
     * @param y1 Y of first point.
     * @param x2 X of second point.
     * @param y2 Y of second point.
     */
    public Line(int x1, int y1, int x2, int y2){
        /* Assign arguments. */
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }
    /**
     * Constructor with two points.
     * 
     * @param p1 Complete first point.
     * @param p2 Complete second point.
     */
    public Line(Point p1, Point p2){
        /* Assign arguments. */
        this.x1 = p1.x;
        this.y1 = p1.y;
        this.x2 = p2.x;
        this.y2 = p2.y;
    }
    /**
     * Does one line cross the other?
     * Hint: Except endpoints.
     * 
     * @param other The other line.
     * @return <code>true</code> if crossing.
     */
    public boolean cross(Line other){
        /* Initialize helper variables. */
        double
            x1 = this.x1,
            y1 = this.y1,
            x1_ = this.x2,
            y1_ = this.y2,
            x2 = other.x1,
            y2 = other.y1,
            x2_ = other.x2,
            y2_ = other.y2,
            z,
            n,
            q,
            p;
        /* Divident. */
        z = (x1_ - x1) * (y2 - y1) - (y1_ - y1) * (x2 - x1);
        /* Divisor. */
        n = (y1_ - y1) * (x2_ - x2) - (x1_ - x1) * (y2_ - y2);
        /* Is parallel? => Can't cross! */
        if(0.0 == n)return false;
        /* Quotient q. */
        q = z / n;
        /* Is len zero? */
        if(x1 == x1_ && y1 == y1_)return false;
        /* Variants depending on whether points are orthogonal. */
        if(0.0 != x1_ -x1){
            p = (x2 - x1 + q * (x2_ - x2)) / (x1_ -x1);
        }else{
            p = (y2 - y1 + q * (y2_ - y2)) / (y1_ -y1);
        }
        /* Crossing after the end of the line. */
        if(q < 0.0 || q > 1.0)return false;
        /* Crossing after the end of the line. */
        if(p < 0.0 || p > 1.0)return false;
        /* Determine whether crossing on end point. */
        Point
            p1 = new Point(this.x1, this.y1),
            p2 = new Point(this.x2, this.y2),
            p3 = new Point(other.x1, other.y1),
            p4 = new Point(other.x2, other.y2);
        /* Determine whether crossing on end point. */
        if(p1.equals(p3) || p1.equals(p4))return false;
        /* Determine whether crossing on end point. */
        if(p2.equals(p3) || p2.equals(p4))return false;
        /* No false condition fired. */
        return true;
    }
    /**
     * Both end points are identical.
     */
    public boolean equals(Object o){
        Line other = (Line)o;
        return 
            x1 == other.x1 && 
            x2 == other.x2 && 
            y1 == other.y1 && 
            y2 == other.y2;
    }
    /**
     * Hash code relating to <code>equals</code>.
     */
    public int hashCode(){
        return (x1 + x2 + y1 + y2) % Integer.MAX_VALUE;
    }
}
