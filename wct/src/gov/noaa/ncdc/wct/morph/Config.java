package gov.noaa.ncdc.wct.morph;

import java.awt.Point;
import java.awt.image.WritableRaster;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;

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
 * Class: CConfig.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: Data list of the application's global data (shown as JDialog).
 * <br/>
 * Hint: Loading and saving to the propertie's file.
 */
public class Config {
    /** Java API. */
    private final long serialVersionUID = 1L;
    /** User is adding mesh points with the GUI. */
    private final int EDIT_MESH_ADD = 1;
    /** User is removing mesh points with the GUI. */
    private final int EDIT_MESH_SUB = 2;
    /** User is moving mesh points with the GUI. */
    private final int EDIT_MESH_OFF = 3;
    /** User is adding polygon points with the GUI. */
    private final int EDIT_POLYGON_ADD = 4;
    /** User is removing polygon points with the GUI. */
    private final int EDIT_POLYGON_SUB = 5;
    /** User is moving polygon points with the GUI. */
    private final int EDIT_POLYGON_OFF = 6;
    /** Rows of the mesh. Count of both windows is identical. */
    private int ROWS_OF_MESH = 0;
    /** Columns of the mesh. Count of both windows is identical. */
    private int COLUMNS_OF_MESH = 5;
    /** Points of the left polygon. Appears within the left window. */
    private int POINTS_OF_POLYGON = 5;
    /** Number of morph steps. Steps between the pictures counted. */
    private int numberOfMorphSteps = 25;
    /** Smooth radius of the polygon. Fuzzy polygon clipping done. */
    private int SMOOTH_RADIUS = 19;
    /** Size of the marker points for mesh & polygon. */
    private int MARK_SIZE = 7;
    /** Collection of points which represent the left picture's mesh. */
    private Vector<Point> left_mesh = new Vector<Point>();
    /** Collection of points which represent the right picture's mesh. */
    private  Vector<Point> right_mesh = new Vector<Point>();
    /** Collection of points which represent the left picture's polygon.*/
    private Vector<Point> left_polygon = new Vector<Point>();
    /** Collection of points which represent the right picture's polygon. */
    private Vector<Point> right_polygon = new Vector<Point>();
    /** Left input image. */
    private WritableRaster left_image;
    /** Right input image. */
    private WritableRaster right_image;
    /** Current result image to be saved to disk. */
    private WritableRaster result_image;
    /** Smoothed clip polygon matrix of the left picture. */
    private double left_clip[][];
    /** Smoothed clip polygon matrix of the right picture. */
    private double right_clip[][];
    /** Equal edit mode of both picture display viewers. */
    private int edit_state = EDIT_MESH_OFF;
    /** Triangulation of the left picture. */
    private final Vector<Triangle> left_triangles 
        = new Vector<Triangle>();
    /** Triangulation of the right picture. */
    private final Vector<Triangle> right_triangles
        = new Vector<Triangle>();
    /** Current result triangulation. */
    private final Vector<Triangle> result_triangles
        = new Vector<Triangle>();
    /** Property object. Can load and store the numerical data from file. */
    private Properties props = new Properties();
    /** Parent JFrame to enable modal behavior. */
    private JFrame parent;
    /** Left morph picture componenent. */
    private MorphInput left;
    /** Right morph picture component. */
    private MorphInput right;

    /**
     * Constructor.
     * @param parent The parent JFrame.
     * @param left Left picture displaying component.
     * @param right Right picture displaying component.
     */
    public Config(MorphInput left, MorphInput right, int numberOfMorphSteps){
        this.left = left;
        this.right = right;
        this.numberOfMorphSteps = numberOfMorphSteps;
        
        left_image = left.getImage();
        right_image = right.getImage();
        
        left_mesh = left.getMesh();
        right_mesh = right.getMesh();
        
//        if(msh){
//            left.initMesh();
//            right.initMesh();
//        }
//        if(pg){
//            left.initPolygon();
//            right.initPolygon();
//        }

    }
//	public void setLeft_image(BufferedImage left_image) {
//		this.left_image = left_image;
//	}
	public WritableRaster getLeft_image() {
		return left_image;
	}
//	public void setRight_image(BufferedImage right_image) {
//		this.right_image = right_image;
//	}
	public WritableRaster getRight_image() {
		return right_image;
	}
	
	
	public WritableRaster getResultImage() {
        /* Calculate size of result picture. */
//        int w = Math.max(left_image.getWidth(), right_image.getWidth());
//        int h = Math.max(left_image.getHeight(), right_image.getHeight());
        
        /* Create empty result picture. */
//        result_image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result_image = left_image.createCompatibleWritableRaster();
        // clear raster?
        
//        /* Create left clip matrix. */
//        left_clip = new double[left_image.getWidth()][left_image.getHeight()];
//        for (int x=0; x<left_image.getWidth(); x++) {
//        	for (int y=0; y<left_image.getHeight(); y++) {
//        		left_clip[x][y] = 1.0;
//        	}
//        }
//        /* Create right clip matrix. */
//        if (right_image == null) {
//
//        	right_clip = new double[left_image.getWidth()][left_image.getHeight()];
//        	for (int x=0; x<left_image.getWidth(); x++) {
//        		for (int y=0; y<left_image.getHeight(); y++) {
//        			right_clip[x][y] = 1.0;
//        		}
//        	}
//        }
//        else {
//       
//        	right_clip = new double[right_image.getWidth()][right_image.getHeight()];
//        	for (int x=0; x<right_image.getWidth(); x++) {
//        		for (int y=0; y<right_image.getHeight(); y++) {
//        			right_clip[x][y] = 1.0;
//        		}
//        	}
//        }
        

        return result_image;
	}
	public Vector<Point> getLeft_mesh() {
		return left_mesh;
	}
	public Vector<Point> getRight_mesh() {
		return right_mesh;
	}
	public Vector<Triangle> getLeft_triangles() {
		return left_triangles;
	}
	public Vector<Triangle> getRight_triangles() {
		return right_triangles;
	}
	public Vector<Triangle> getResult_triangles() {
		return result_triangles;
	}
	public void setNumberOfMorphSteps(int numberOfMorphSteps) {
		this.numberOfMorphSteps = numberOfMorphSteps;
	}
	public int getNumberOfMorphSteps() {
		return numberOfMorphSteps;
	}
	
	
	
}


