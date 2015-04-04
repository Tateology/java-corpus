package gov.noaa.ncdc.wct.morph;

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
 * Class: CTransform.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: Affine transform matrix.
 * <br/>
 * Hint: Example: a_rc -> r = row_in_matrix, c = column in matrix. Row != Column
 * because inversion is not necessary.
 * <br/>
 * Usage: M * P' = P (M...Matrix, P'...Result Point, P...Origin Point).
 */
public class Transform {
    /** Matrix factor. Row = 1, Column = 1.*/
    double a_11;
    /** Matrix factor. Row = 1, Column = 2.*/
    double a_12;
    /** Matrix factor. Row = 1, Column = 3.*/
    double a_13;
    /** Matrix factor. Row = 2, Column = 1.*/
    double a_21;
    /** Matrix factor. Row = 2, Column = 2.*/
    double a_22;
    /** Matrix factor. Row = 2, Column = 3.*/
    double a_23;
}
