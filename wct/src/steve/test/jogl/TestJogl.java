//package steve.test.jogl;
//
//import java.awt.Color;
//
//import javax.media.opengl.GL;
//import javax.media.opengl.GLAutoDrawable;
//import javax.media.opengl.GLCanvas;
//import javax.media.opengl.GLEventListener;
//import javax.swing.JFrame;
//
//public class TestJogl implements GLEventListener {
//
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//        
//
//        TestJogl test = new TestJogl();
//        test.run();
//
//    }
//    
//    
//    
//    
//    private GLCanvas canvas;
//    
//    public void run() {
////        GLCapabilities capabilities = new GLCapabilities();
//        this.canvas = new GLCanvas();
//        canvas.addGLEventListener(this);
//        
//
//        JFrame frame = new JFrame();
//        frame.getContentPane().add(canvas);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1000, 1000);
//        frame.setVisible(true);
//
//    }
//
//    
//    
////    @Override
//    public void init(GLAutoDrawable glAutoDrawable) {
//
//        canvas.setSize(1000, 1000);
//
//        GL gl = glAutoDrawable.getGL();
//        
//        // From http://jerome.jouvie.free.fr/OpenGl/Tutorials/Tutorial2.php
//        /*
//         * Enables the Smooth color shading mode to create graduate color
//         * The effect can be shown on the Pyramid of the Tutorial 04
//         */
//        gl.glShadeModel(GL.GL_SMOOTH);
//        /*
//         * Defines the clearing color in RGB mode (Red Green Blue)
//         * This color is the background color of the scene.
//         * Components must be a float number in the interval [0,1]
//         * Here: R=0, G=0, B=0 so the color is black
//         * (r=1, G=1, B=1 is the white color)
//         *
//         * Rem: the last value is the Alpha components (also called RGBA).
//         * We should show its effects in the Blending tutorial (09)
//         */
////        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        
//        gl.glClearColor( 1.0f, 1.0f, 1.0f, 1.0f );
//        gl.glColor3f( 0.0f, 0.0f, 0.0f ); 
//       
//        /*
//         * The Depth buffer
//         * This buffer keeps tracks the depth of each points of the scene.
//         * This buffer, when an object is drawn, tests if the object
//         * is behind or in front of the objects already drawn.
//         * GL_LEQUAL test if the current object to be drawn is closer
//         * or as the same distance than
//         */
//        gl.glClearDepth(1.0);             //Enable Clearing of the Depth buffer
//        gl.glDepthFunc(GL.GL_LEQUAL);        //Type of Depth test
//        gl.glEnable(GL.GL_DEPTH_TEST);       //Enable Depth Testing
//       
//        //Define the correction done to the perspective calculation (perspective looks a it better)
//        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
//
//    }
//    
////    @Override
//    public void display(GLAutoDrawable glAutoDrawable) {
//        
//        GL gl = glAutoDrawable.getGL();
//
//        /*
//         * We begin to clear the color and the depth buffer and reset the view.
//         * After, we translate the repere axis to the back. This is used to view
//         * the scene : Remember to call something like that !
//         */
//        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);    //Clear the buffers
//        gl.glLoadIdentity();                                      //Reset the view
////        gl.glTranslatef(0.0f, 0.0f, zOffset);                     //Translate the scene backwards        
//        
//        
//        GLTools.setColor(gl, Color.BLUE);
//        GLTools.fillRect(gl, 10, 10, 200, 200);
//    }
//
////    @Override
//    public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
//        // TODO Auto-generated method stub
//        
//    }
//
//
////    @Override
//    public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
//        // TODO Auto-generated method stub
//        
//    }
//
//}
