package gov.noaa.ncdc.wct.morph;

import java.awt.Point;
import java.awt.image.WritableRaster;

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
 * Class: CMorphOperator.
 * <br/>
 * License: GPLv2.
 * <br/>
 * Description: Morph the result from left input to right input depending on
 * the ratio parameter.
 * <br/>
 * Hint: Writes the result into the working directory.
 */
public class MorphOperator {
    /** Application's main class. */
//    private static CMain parent;
    /** 
     * If <code>0.0</code then output is the left image, if <code>1.0</code>
     * then output is the right image. Every value between them leads to a
     * merged image.
     */
    private double ratio;
    /** Current point coordinates of the left image. */
    private Point left_point = new Point();
    /** Current point coordinates of the right image. */
    private Point right_point = new Point();
    /** Current point coordinates of the result image. */
    private Point result_point = new Point();
    /** RGB value of the current left pixel. */
    private int left_pixel;
    /** RGB value of the current right pixel. */
    private int right_pixel;
    /** RGB value of the current result pixel. */
    private int result_pixel;
    /** Transformation matrix from result to left point. */
    private Transform left_trafo;
    /** Transformation matrix from result to right point. */
    private Transform right_trafo;
    /** Index of the current triangle within all three lists. */
    private int t_idx;
    /** List of result points situated within the current result triangle. */
    private Point withins[];
    /** Polygon clip ratio of the current left pixel. */
    private double left_ratio;
    /** Polygon clip ratio of the current right pixel. */
    private double right_ratio;
    /** If <code>true</code> the user forces the morph process to abort. */
    private boolean f_break;
//    /** Instance of the progress bar. */
//    private static CProgress progress;
    /**
     * Initialize static class components.
     * 
     * @param parent Main JFrame.
     * @param progress Progress bar.
     */
//    public static void morph(CMain parent, CProgress progress){
//        /* Assign parameters. */
//        CMorphOperator.parent = parent;
//        CMorphOperator.progress = progress;
//    }
//    /**
//     * Enable abort of the morph process forced by user.
//     */
//    public static void doBreak(){
//        f_break = true;
//    }
    
    
    
    
    private Config config;
    private WritableRaster resultImage;
    
    public MorphOperator(Config config) {
    	this.config = config;
    }
    
    
    
    
    /**
     * Thread API. Starts morph batch for a number of intermediate pictures
     * with increasing ratio value.
     */
    public void run(){
    	run(null);
    }
    
    
    /**
     * Thread API. Starts morph batch for a number of intermediate pictures
     * with increasing ratio value.
     */
    public void run(MorphStepHandler handler){
        f_break = false;
        try{

//            ImageIO.write(config.getLeft_image(), "png", new File("leftimage.png"));
//            ImageIO.write(config.getRight_image(), "png", new File("rightimage.png"));

        	
        	
        	resultImage = config.getResultImage();
        	
            for(int i = 0; i < config.getNumberOfMorphSteps()+1 && (!f_break); i++){
            	
                /* Clear result picture.*/
                for(int x = 0; x < resultImage.getWidth(); x++){
                    for(int y = 0; y < resultImage.getHeight(); y++){
//                        resultImage.setRGB(x, y, 0x0);
                        resultImage.setSample(x, y, 0, 0.0);
                    }
                }
                /* Calculate ratio. */
                ratio = ((double)i / config.getNumberOfMorphSteps());
                /* Depends on current ratio. */
                genResultTriangles();
                
            	System.out.println("processing time step "+i);
            	System.out.println("number of triangles:   left: "+config.getLeft_triangles().size());
            	System.out.println("number of triangles:  right: "+config.getRight_triangles().size());
            	System.out.println("number of triangles: result: "+config.getResult_triangles().size());
            	

                
                /* Iterate through the triangles. */
                for(t_idx = 0; t_idx < config.getResult_triangles().size(); t_idx++){
//                	System.out.println("t_idx="+t_idx+" of: "+config.getResult_triangles().size());
                    triangle();
                }
//                File f = new File(CStrings.getOutput(i));
                /* Save image into workdir. */
//                ImageIO.write(resultImage, "png", new File("morph/morph"+i+".png"));
                if (handler != null) {
                	handler.processRaster(resultImage, i);
                }
//                /* Show progress. */
//                progress.setProgress(i, 0, CConfig.NUM_OF_MORPH_STEPS);
//                Thread.sleep(1);
            }
//            progress.close();
        }catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
//            JOptionPane.showMessageDialog(parent, 
//                    "Can't save result. Please see console output!");
//            
        }
    }
    
    
    
    
    
    

    /**
     * Thread API. Starts morph batch for a number of intermediate pictures
     * with increasing ratio value.
     */
    public void run(MorphStepHandler handler, double[] ratios){
        f_break = false;
        try{

//            ImageIO.write(config.getLeft_image(), "png", new File("leftimage.png"));
//            ImageIO.write(config.getRight_image(), "png", new File("rightimage.png"));

        	
        	
        	resultImage = config.getResultImage();
        	
            for(int i = 0; i < ratios.length && (!f_break); i++){
            	
                /* Clear result picture.*/
                for(int x = 0; x < resultImage.getWidth(); x++){
                    for(int y = 0; y < resultImage.getHeight(); y++){
//                        resultImage.setRGB(x, y, 0x0);
                        resultImage.setSample(x, y, 0, 0.0);
                    }
                }
                /* Calculate ratio. */
//                ratio = ((double)i / config.getNumberOfMorphSteps());
                ratio = ratios[i];
                /* Depends on current ratio. */
                genResultTriangles();
                
            	System.out.println("processing time step "+i);
            	System.out.println("number of triangles:   left: "+config.getLeft_triangles().size());
            	System.out.println("number of triangles:  right: "+config.getRight_triangles().size());
            	System.out.println("number of triangles: result: "+config.getResult_triangles().size());
            	

                
                /* Iterate through the triangles. */
                for(t_idx = 0; t_idx < config.getResult_triangles().size(); t_idx++){
//                	System.out.println("t_idx="+t_idx+" of: "+config.getResult_triangles().size());
                    triangle();
                }
//                File f = new File(CStrings.getOutput(i));
                /* Save image into workdir. */
//                ImageIO.write(resultImage, "png", new File("morph/morph"+i+".png"));
                if (handler != null) {
                	handler.processRaster(resultImage, i);
                }
//                /* Show progress. */
//                progress.setProgress(i, 0, CConfig.NUM_OF_MORPH_STEPS);
//                Thread.sleep(1);
            }
//            progress.close();
        }catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
//            JOptionPane.showMessageDialog(parent, 
//                    "Can't save result. Please see console output!");
//            
        }
    }
    
    
    
    
    
    
    
    
    /**
     * Make a weighted average mesh depending on the current ratio.
     */
    private void genResultTriangles(){
        config.getResult_triangles().clear();
        /* For all triangles belonging to both pictures. */
        /* First with first, second with second and so on. */ 
        for(int i = 0; i < config.getLeft_triangles().size(); ++i){
            Triangle r = config.getLeft_triangles().get(i);
            Triangle s = config.getRight_triangles().get(i);
            Triangle t = new Triangle(
                    merge(r.getPoints()[0], s.getPoints()[0]),
                    merge(r.getPoints()[1], s.getPoints()[1]),
                    merge(r.getPoints()[2], s.getPoints()[2])
                );
            /* Add merged triangle relating to ratio. */
            config.getResult_triangles().add(t);
        }
    }
    /** Merge two points weighted by ratio.
     * 
     * @param p1 First point.
     * @param p2 Second point.
     * @return Point on a line between them.
     */
    private Point merge(Point p1, Point p2){
        return new Point(
            (int)(p1.x * (1.0 - ratio) + p2.x * ratio), 
            (int)(p1.y * (1.0 - ratio) + p2.y * ratio));
    }
    /**
     * Merge all points of a triangle.
     */
    private void triangle(){
        Triangle result = config.getResult_triangles().get(t_idx);
        /* Left transformation matrix. */
        left_trafo = Geo.getTrafo(config.getLeft_triangles().get(t_idx), result);
        /* Right transformation matrix. */
        right_trafo = Geo.getTrafo(config.getRight_triangles().get(t_idx), result);
        /* For all target points. */
        withins = result.getWithins();
        for(Point p: withins){
            result_point = p;
            /* Transform left. */
            left_point = Geo.getOrigin_(result_point, left_trafo);
            /* Transform right. */
            right_point = Geo.getOrigin_(result_point, right_trafo);
            /* Merge both pixels. */
//            System.out.println("before merge");
            merge();
//            System.out.println("after merge");
        }   
    }
    /**
     * Merge (left.pixel, right.pixel)->(result.pixel). Result depends on
     * ratio value & both polygon matrixes.
     */
    private void merge(){
        try{
//            left_pixel = config.getLeft_image().getRGB(left_point.x, left_point.y);
//            right_pixel = config.getRight_image().getRGB(right_point.x, right_point.y);
        	
        	double left_pixel = config.getLeft_image().getSampleDouble(left_point.x, left_point.y, 0);
        	double right_pixel;
        	if (config.getRight_image() != null) {
        		right_pixel = config.getRight_image().getSampleDouble(right_point.x, right_point.y, 0);
        	}
        	else {
        		right_pixel = left_pixel;
        	}
        	
            
//            left_ratio = config.left_clip[left_point.x][left_point.y];
//            right_ratio = config.right_clip[right_point.x][right_point.y];
            left_ratio = 1;
            right_ratio = 1;
            
            
            /* Unify all 3 ratios. */
            double
                t1 = left_ratio,
                t2 = 1.0 - left_ratio,
                t3 = 1.0 - right_ratio,
                t4 = right_ratio,
                fl = t3 + (1.0 - ratio) * (t1 - t3),
                fr = t2 + ratio * (t4 - t2);
//            /* For each color in 32 bit color value. */
//            int 
//                l_r = ( left_pixel & 0xffff0000) >> 16,
//                r_r = (right_pixel & 0xffff0000) >> 16,
//                l_g = ( left_pixel & 0xff00ff00) >> 8,
//                r_g = (right_pixel & 0xff00ff00) >> 8,
//                l_b =   left_pixel & 0xff0000ff,
//                r_b =  right_pixel & 0xff0000ff,
//                r = (int)(l_r * fl + r_r * fr),
//                g = (int)(l_g * fl + r_g * fr),
//                b = (int)(l_b * fl + r_b * fr);
//            /* Set pixel. */
//            result_pixel = (0x00000000) | (r << 16) | (g << 8) | b;

            double result_pixel = left_pixel*fl + right_pixel*fr;
            
            if (left_point.x == 200 && left_point.y == 200) {
            	System.out.println(left_point+" --- "+right_point+" ::: "+result_point+"    "+t1 + "," + t2 +","+t3+","+t4+","+fl+","+fr);
//            	System.out.println(config.getLeft_image().getType() + " - " +config.getRight_image().getType());
            }
            
            
            resultImage.setSample(result_point.x, result_point.y, 0, result_pixel);
//            resultImage.setRGB(result_point.x, result_point.y, result_pixel);            
//            resultImage.setRGB(result_point.x, result_point.y, left_pixel);
            
        }catch(Exception e){}
    }
    
    
    
    
    
    
    
    
    
    interface MorphStepHandler {
    	public void processRaster(WritableRaster raster, int step);
    }
    
}
