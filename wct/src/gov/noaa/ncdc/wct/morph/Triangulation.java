package gov.noaa.ncdc.wct.morph;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
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
 * Class: CTriangulation.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: Groups a set of points within one picture to a
 * DELAUNAY triangulation.
 * <br/> 
 * Hint: Not more than 500 points expected.
 */
public class Triangulation{
    /** Width of the picture. */
    private int width;
    /** Height of the picture. */
    private int height;
    /** Collection of all input points, order not modified. */
    private final Vector<Point> order = new Vector<Point>();
    /** 
     * Collection of all unique input points. No point occurs twice. 
     * Order not original.
     */
    private final HashSet<Point> points = new HashSet<Point>();
    /** Collection of lines which have already been processed. */
    private final HashSet<Line> used = new HashSet<Line>();
    /** Queue of explored points which have to be processed. */
    private final Queue<Point[]> queue= new ArrayDeque<Point[]>();
    /** Result of the process is this triangulation. */
    private final HashSet<Triangle> triangles = 
        new HashSet<Triangle>();
    /** Due to DELAUNAY. Circumcircle of the triangle to test. */
    private double circle_radius;
    /** Center x of the circumcircle of the triangle to test. */
    private double center_x;
    /** Center y of the circumcircle of the triangle to test. */
    private double center_y;
    /** First point of the line to explore. */
    private Point p1;
    /** Second point of the line to explore. */
    private Point p2;
    /** Point to test relating to the line: No other point shall lay nearer. */
    private Point pnew;
    /** 
     * Count of found new point relating to the line. Max. two points 
     * can be found.
     */
    private int count;
    
    
    
    
    
    private Config config = null;
    
    
    
    
    public Triangulation(Config config) {
    	this.config = config;
    }
    
    
    
    
    /**
     * Perform complete operation.
     */
    public void triangulate(){
long time = System.currentTimeMillis();
        System.out.println("Begin triangulation.");
        Point p, l, r;
        clear();
        /* Bug fix "Array index out of bounds" in CTriangulation.add().*/
        order.clear();
        points.clear();
        if (config.getRight_image() != null) {
        	width = config.getLeft_image().getWidth() + config.getRight_image().getWidth();
        	height = config.getLeft_image().getHeight() + config.getRight_image().getHeight();        
        	width /= 2;
        	height /= 2;
        }
        else {
        	width = config.getLeft_image().getWidth();
        	height = config.getLeft_image().getHeight();
        }
        for(int i = 0; i < config.getLeft_mesh().size(); ++i)
        {
            l = config.getLeft_mesh().get(i);
            r = config.getRight_mesh().get(i);
            p = new Point((l.x + r.x) / 2, (l.y + r.y) / 2);
            points.add(p);
            order.add(p);
        }
        work();
//        debug();
System.out.println("End. Duration of triangulation = " + (System.currentTimeMillis() - time) + '.');
    }
    /**
     * Clear all permanent date of the collections.
     */
    private void clear(){
        used.clear();
        triangles.clear();
        queue.clear();
        this.config.getLeft_triangles().clear();
        this.config.getRight_triangles().clear();
        this.config.getResult_triangles().clear();
    }
    /**
     * Calculate the triangles.
     */
    private void work(){
        clear();
        if(3 > points.size())return;
        Iterator<Point> it= points.iterator();
        p1 = it.next();
        p2 = findNearest(p1);
        used.add(new Line(p1, p2));
        queue.add(new Point[]{p1, p2});
        while(0 < queue.size())findPoint();
    }
    /**
     * Explore the third points for one line.
     */
    private void findPoint(){
        Point a[] = queue.poll();
        p1 = a[0];
        p2 = a[1];
        count = 0;
        for(Point p: points){
            pnew = p;
            if(circle()){
                if(delaunayCond()){
                    add(new Triangle(p1, p2, pnew));
                    if(used.add(new Line(p1, pnew))){
                        queue.add(new Point[]{p1, pnew});
                    }
                    if(used.add(new Line(p2, pnew))){
                        queue.add(new Point[]{p2, pnew});
                    }
                    if(1 == count++){
                        return;
                    }
                }
            }
        }
    }
    /**
     * Add one left & one right triangle.
     * Points are fetched ordered from left & right mesh them self.
     *
     * @param temp Input triangle.
     */
    private void add(Triangle temp){
        if(triangles.add(temp)){
        	Point l0 = config.getLeft_mesh().get(indexOf(temp.getPoints()[0]));
        	Point l1 = config.getLeft_mesh().get(indexOf(temp.getPoints()[1]));
        	Point l2 = config.getLeft_mesh().get(indexOf(temp.getPoints()[2]));
        	Point r0 = config.getRight_mesh().get(indexOf(temp.getPoints()[0]));
        	Point r1 = config.getRight_mesh().get(indexOf(temp.getPoints()[1]));
        	Point r2 = config.getRight_mesh().get(indexOf(temp.getPoints()[2]));
            config.getLeft_triangles().add(new Triangle(l0, l1, l2));
            config.getRight_triangles().add(new Triangle(r0, r1, r2));
        }
    }
    /**
     * Seek one point within the ordered input list.
     * @param p Point to seek.
     * @return Index of the point in the ordered input list.
     */
    private int indexOf(Point p){
        return order.indexOf(p);
    }
    /**
     * Check the DELAUNAY condition of P1 P2 and PNEW.
     * No other point shall be within the circumcircle of the three points. 
     * 
     * @return <code>true<code> if DELAUNAY condition is satisfied.
     */
    private boolean delaunayCond(){
    	Line l1 = new Line(p1, pnew); 
    	Line l2 = new Line(p2, pnew);
        for(Point p: points){
            double d = distance(p);
            if(!p.equals(pnew) && !p.equals(p1) && !p.equals(p2)){
                if(d < circle_radius){
                    return false;
                }
            }
        }
        for(Line l: used) {
            if(l.cross(l1) || l.cross(l2)){
                return false;
            }
        }
        return true;
    }
    /** 
     * Find the nearest neighbor of one mesh point.
     * 
     * @param p1 First point.
     * @return Nearest point to p1.
     */
    private Point findNearest(Point p1){
        double dist = Double.MAX_VALUE, d;
        Point result = null;
        for(Point p: points){
            d = p.distance(p1);
            if(d < dist && d > 0.0){
                dist = d;
                result = p;
            }
        }
        return result;
    }
    /**
     * Calculates the circumcircle of the current test triangle's points.
     * 
     * @return <code>true</code> if such a circle can be found.
     */
    private boolean circle(){
        double x1, y1, x2, y2, x3, y3, q, n;
        x1 = (p1.x + pnew.x) / 2.0;
        y1 = (p1.y + pnew.y) / 2.0;
        x3 = (p2.x + pnew.x) / 2.0;
        y3 = (p2.y + pnew.y) / 2.0;
        x2 = pnew.x;
        y2 = pnew.y;
        q = (y2 - y1) * (y3 - y1) - (-x2 + x1) * (x3 - x1);
        n = (y2 - y3) * (-x2 + x1) - (-x2 + x3) * (y2 - y1);
        if(0.0 == n){
            return false;
        }
        q /= n;
        center_x = x3 + q * (y2 - y3);
        center_y = y3 + q * (-x2 + x3);
        circle_radius = distance(p1);
        return true;
    }
    /**
     * Distance of one point to the center of the circumcircle.
     * 
     * @param p Point to be explored.
     * @return Distance.
     */
    private double distance(Point p){
        double dx = p.x - center_x, dy = p.y -center_y;
        return Math.sqrt(dx * dx + dy * dy);
    }
//    /**
//     * Write left right and 50% triangulation into the debug directory.
//     */
//    private void debug(){
//        BufferedImage image;
//        try{
//            image = new BufferedImage(CConfig.left_image.getWidth(), 
//                    CConfig.left_image.getHeight(),
//                    BufferedImage.TYPE_INT_RGB);
//            for(CTriangle t: CConfig.left_triangles){
//                t.debug(image);
//            }
//            ImageIO.write(image, "png", new File(CStrings.LEFT_TRI));
//            image = new BufferedImage(
//                    CConfig.right_image.getWidth(),
//                    CConfig.right_image.getHeight(),
//                    BufferedImage.TYPE_INT_RGB
//                    );
//            for(CTriangle t: CConfig.right_triangles){
//                t.debug(image);
//            }
//            ImageIO.write(image, "png", new File(CStrings.RIGHT_TRI));
//
//            image = new BufferedImage(
//                    width,
//                    height,
//                    BufferedImage.TYPE_INT_RGB
//                    );
//            for(CTriangle t: triangles){
//                t.debug(image);
//            }
//            ImageIO.write(image, "png", new File(CStrings.MIDDLE_TRI));
//        }catch(Exception e){
//            System.out.println("Can't debug triangles.");
//            e.printStackTrace();
//        }
//    }
 }
