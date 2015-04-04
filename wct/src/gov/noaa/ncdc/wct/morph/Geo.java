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
 * Class: CGeo.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: Helper class for geometric calculations.
 * <br/>
 * Handle interpolation - transformation.
 * Triangulation now done by CTriangulation.
 */
public class Geo {
    /**
     * Provide reverse transformation matrix from one result triangle to one
     * input picture triangle. All three points of the two triangles
     * correspond.
     * @param origin_ One triangle of the source picture. 
     * @param result One triangle of the result picture.
     * @return Floating point transformation matrix. Indexes are row / column.
     */
    public static Transform getTrafo(Triangle origin_, Triangle result){
        /* Initialize helper variables. */
        int
            x1_ = origin_.getPoints()[0].x,
            x2_ = origin_.getPoints()[1].x,
            x3_ = origin_.getPoints()[2].x,
            y1_ = origin_.getPoints()[0].y,
            y2_ = origin_.getPoints()[1].y,
            y3_ = origin_.getPoints()[2].y,
            x1 = result.getPoints()[0].x,
            x2 = result.getPoints()[1].x,
            x3 = result.getPoints()[2].x,
            y1 = result.getPoints()[0].y,
            y2 = result.getPoints()[1].y,
            y3 = result.getPoints()[2].y;
        Transform trafo = new Transform();
        double t, u, d;
        /* Try variants depending on which points are orthogonal. */
        if(x1 != x3){
            d = (double)x1 - x3;
            t = ((double)x1 - x2) / d;
            u = ((double)y1 - y2) - ((double)y1 - y3) * t;
            trafo.a_12 = (((double)x1_ - x2_) - ((double)x1_ - x3_) * t) / u; 
            trafo.a_22 = (((double)y1_ - y2_) - ((double)y1_ - y3_) * t) / u;
            trafo.a_11 = (((double)x1_ - x3_) - trafo.a_12 * ((double)y1 - y3)) 
                / d;
            trafo.a_21 = (((double)y1_ - y3_) - trafo.a_22 * ((double)y1 - y3)) 
                / d;
        }else{
            d = (double)y1 - y3;
            t = ((double)y1 - y2) / d;
            u = ((double)x1 - x2) - ((double)x1 - x3) * t;
            trafo.a_11 = (((double)x1_ - x2_) - ((double)x1_ - x3_) * t) / u; 
            trafo.a_21 = (((double)y1_ - y2_) - ((double)y1_ - y3_) * t) / u;
            trafo.a_12 = (((double)x1_ - x3_) - trafo.a_11 * ((double)x1 - x3)) 
                / d;
            trafo.a_22 = (((double)y1_ - y3_) - trafo.a_21 * ((double)x1 - x3)) 
                / d;
        }
        trafo.a_13 = (double)x1_ - trafo.a_11 * x1 - trafo.a_12 * y1;
        trafo.a_23 = (double)y1_ - trafo.a_21 * x1 - trafo.a_22 * y1;
        return trafo;
    }
    /**
     * Transform one point from the result matrix to the one point of the input
     * matrix.
     * @param result Point of the result picture.
     * @param trafo Transformation matrix.
     * @return Corresponding point of the input picture.
     */
    public static Point getOrigin_(Point result, Transform trafo){
        Point origin_ = new Point();
        /* Transform x. */
        origin_.x = (int)
            (result.x * trafo.a_11 + result.y * trafo.a_12 + trafo.a_13);
        /* Transform y. */
        origin_.y = (int)
            (result.x * trafo.a_21 + result.y * trafo.a_22 + trafo.a_23);
        return origin_;
    }
}
