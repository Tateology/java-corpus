//package steve.test.jogl;
////package org.opensourcephysics.display3d.jogl.utils;
//
//import java.awt.Color;
//
//import javax.media.opengl.GL;
//import javax.media.opengl.glu.GLU;
//
//public class GLTools {
//    
//    public static GLU GLU;
//    
//    /**
//     * Draws a rectangle width the specified dimensions.
//     */
//    public static void drawRect(GL gl, float x, float y, float width, float height) {
//        gl.glBegin(GL.GL_LINES);
//        gl.glVertex2f(x,       y);          gl.glVertex2f(x+width, y);
//        gl.glVertex2f(x+width, y);          gl.glVertex2f(x+width, y+height);
//        gl.glVertex2f(x+width, y+height);   gl.glVertex2f(x,     y+height);
//        gl.glVertex2f(x,       y+height+1); gl.glVertex2f(x,     y);
//        gl.glEnd();
//    }
//
//
//    /**
//     * Fills the specified rectangle.
//     */
//    public static void fillRect(GL gl, float x, float y, float width, float height) {
//        float x1 = Math.min(x, x+width);
//        float x2 = Math.max(x, x+width);
//        float y1 = Math.min(y, y+height);
//        float y2 = Math.max(y, y+height);
//        gl.glBegin(GL.GL_TRIANGLES);
//        gl.glVertex2f(x1, y1);
//        gl.glVertex2f(x2, y1);
//        gl.glVertex2f(x2, y2);
//
//        gl.glVertex2f(x2, y2);
//        gl.glVertex2f(x1, y2);
//        gl.glVertex2f(x1, y1);
//        gl.glEnd();
//    }
//    /**
//     *
//     */
//    public static void setColor(GL gl, int c) {
//        gl.glColor4ub((byte)(0xff&(c>>16)), (byte)(0xff&(c>>8)), (byte)(0xff&(c)), (byte)(0xff&(c>>>24)));
//    }
//    
//    public static void setColor(GL gl, Color c) {
//        setColor(gl, c.getRGB());
//        
//        if (c.getAlpha() < 255) {
//            gl.glDisable(GL.GL_DEPTH_TEST);
//            gl.glDisable(GL.GL_CULL_FACE);
//        }
//        else {
//            gl.glEnable(GL.GL_DEPTH_TEST);
//            gl.glEnable(GL.GL_CULL_FACE);
//        }
//    }
//    
//    public static void setStroke(GL gl, float lineWidth) {
//        //enable antialiasing
//        gl.glEnable(GL.GL_LINE_SMOOTH);
//        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
//        
//        gl.glLineWidth(lineWidth);
//    }
//    /*
//    public static void drawPolygon(GL gl, int[] xs, int[] ys, int[] zs, int numPoints) {
//        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
//        
//        gl.glBegin(GL.GL_POLYGON);
//        for (int i = 0; i < numPoints; i++) {
//            gl.glVertex3f(xs[i], ys[i], zs[i]);
//        }
//        gl.glEnd();
//    }
//    
//    public static void fillPolygon(GL gl, int[] xs, int[] ys, int numPoints) {
//        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
//    }
//*/
//    
//    
//    
//    
//    
//    
//}
