package renderers;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import javax.imageio.ImageIO;
import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;

import static java.awt.event.KeyEvent.*;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_LINEAR_MIPMAP_NEAREST;
import static javax.media.opengl.GL.GL_NEAREST;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;
import static javax.media.opengl.GL2.GL_LIGHT0;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.GL2.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT1;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_POSITION;

/**
 * NeHe Lesson #7 (JOGL 2 Port): Texture Filters, Lighting & Keyboard Inputs
 * @author Hock-Chuan Chua
 * @version May 2012
 * 
 * 'l': toggle light on/off
 * 'f': switch to the next texture filters (nearest, linear, mipmap)
 * Page-up/Page-down: zoom in/out decrease/increase z
 * up-arrow/down-arrow: decrease/increase x rotational speed
 * left-arrow/right-arrow: decrease/increase y rotational speed
 */
@SuppressWarnings("serial")
public class JOGL2Nehe07TextureFilterLightKey extends GLCanvas 
        implements GLEventListener, KeyListener {
   // Define constants for the top-level container
   private static String TITLE = "Nehe #7: Texture Filter, Lighting, and key-controlled";
   private static final int CANVAS_WIDTH = 320;  // width of the drawable
   private static final int CANVAS_HEIGHT = 240; // height of the drawable
   private static final int FPS = 60; // animator's target frames per second
   
   /** The entry main() method to setup the top-level container and animator */
   public static void main(String[] args) {
      // Run the GUI codes in the event-dispatching thread for thread safety
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            // Create the OpenGL rendering canvas
            GLCanvas canvas = new JOGL2Nehe07TextureFilterLightKey();
            canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

            // Create a animator that drives canvas' display() at the specified FPS. 
            final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);
            
            // Create the top-level container
            final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
            frame.getContentPane().add(canvas);
            frame.addWindowListener(new WindowAdapter() {
               @Override 
               public void windowClosing(WindowEvent e) {
                  // Use a dedicate thread to run the stop() to ensure that the
                  // animator stops before program exits.
                  new Thread() {
                     @Override 
                     public void run() {
                        if (animator.isStarted()) animator.stop();
                        System.exit(0);
                     }
                  }.start();
               }
            });
            frame.setTitle(TITLE);
            frame.pack();
            frame.setVisible(true);
            animator.start(); // start the animation loop
         }
      });
   }
   
   // Setup OpenGL Graphics Renderer
   
   private GLU glu;  // for the GL Utility
   private static float angleX = 0.0f; // rotational angle for x-axis in degree
   private static float angleY = 0.0f; // rotational angle for y-axis in degree
   private static float z = -3.0f;     // z-location
   private static float rotateSpeedX = 0.0f; // rotational speed for x-axis
   private static float rotateSpeedY = 0.0f; // rotational speed for y-axis

   private static float zIncrement = 0.02f;  // for zoom in/out
   private static float rotateSpeedXIncrement = 0.01f; // adjusting x rotational speed
   private static float rotateSpeedYIncrement = 0.01f; // adjusting y rotational speed

   // Textures with three different filters - Nearest, Linear & MIPMAP
   private Texture[] textures = new Texture[3];
   private static int currTextureFilter = 0; // currently used filter
   private String textureFileName = "images/crate.png";

    float verticies[][] = new float[6][3];

   // Texture image flips vertically. Shall use TextureCoords class to retrieve the
   // top, bottom, left and right coordinates.
   private float textureTop, textureBottom, textureLeft, textureRight;

   // Lighting
   private static boolean isLightOn;
   
   /** Constructor to setup the GUI for this Component */
   public JOGL2Nehe07TextureFilterLightKey() {
      this.addGLEventListener(this);
      // For handling KeyEvents
      this.addKeyListener(this);
      this.setFocusable(true);
      this.requestFocus();
   }
   
   // ------ Implement methods declared in GLEventListener ------

   /**
    * Called back immediately after the OpenGL context is initialized. Can be used 
    * to perform one-time initialization. Run only once.
    */
   @Override
   public void init(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
      glu = new GLU();                         // get GL Utilities
      gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
      gl.glClearDepth(1.0f);      // set clear depth value to farthest
      gl.glEnable(GL_DEPTH_TEST); // enables depth testing
      gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
      gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
      gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting

      // Load textures from image
      try {
         // Use URL so that can read from JAR and disk file.
         // Filename relative to the project root.
         BufferedImage image = 
               ImageIO.read(getClass().getClassLoader().getResource(textureFileName));

         // Create a OpenGL Texture object
         textures[0] = AWTTextureIO.newTexture(GLProfile.getDefault(), image, false); 
         // Nearest filter is least compute-intensive
         // Use nearer filter if image is larger than the original texture
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
         // Use nearer filter if image is smaller than the original texture
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

         textures[1] = AWTTextureIO.newTexture(GLProfile.getDefault(), image, false);
         // Linear filter is more compute-intensive
         // Use linear filter if image is larger than the original texture
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
         // Use linear filter if image is smaller than the original texture
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

         textures[2] = AWTTextureIO.newTexture(GLProfile.getDefault(), image, true); // mipmap is true
         // Use mipmap filter is the image is smaller than the texture
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
         gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
               GL_LINEAR_MIPMAP_NEAREST);

         // Get the top and bottom coordinates of the textures. Image flips vertically.
         TextureCoords textureCoords;
         textureCoords = textures[0].getImageTexCoords();
         textureTop = textureCoords.top();
         textureBottom = textureCoords.bottom();
         textureLeft = textureCoords.left();
         textureRight = textureCoords.right();
      } catch (GLException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      // Set up the lighting for Light-1
      // Ambient light does not come from a particular direction. Need some ambient
      // light to light up the scene. Ambient's value in RGBA
      float[] lightAmbientValue = {0.5f, 0.5f, 0.5f, 1.0f};
      // Diffuse light comes from a particular location. Diffuse's value in RGBA
      float[] lightDiffuseValue = {1.0f, 1.0f, 1.0f, 1.0f};
      // Diffuse light location xyz (in front of the screen).
      float lightDiffusePosition[] = {0.0f, 0.0f, 2.0f, 1.0f};

      gl.glLightfv(GL_LIGHT1, GL_AMBIENT, lightAmbientValue, 0);
      gl.glLightfv(GL_LIGHT1, GL_DIFFUSE, lightDiffuseValue, 0);
      gl.glLightfv(GL_LIGHT1, GL_POSITION, lightDiffusePosition, 0);
      gl.glEnable(GL_LIGHT1);    // Enable Light-1
      gl.glDisable(GL_LIGHTING); // But disable lighting
      isLightOn = false;

       initializeVerticies();
   }

    private void initializeVerticies() {


        verticies[0] = new float[] {0.0f, 1.2f, 0.0f};
        verticies[1] = new float[] {-1.0f, 0.0f, 1.0f};
        verticies[2] = new float[] {1.0f, 0.0f, 1.0f};
        verticies[3] = new float[] {1.0f, 0.0f, -1.0f};
        verticies[4] = new float[] {-1.0f, 0.0f, -1.0f};
        verticies[5] = new float[] {0.0f, -1.2f, 0.0f};

/*
        verticies[0] = new float[] {-1f, 0f, 0f};
        verticies[1] = new float[] {-1f, 0f, 1f};
        verticies[2] = new float[] {0f, 0.2f, 0f};
        verticies[3] = new float[] {0f, 0.2f, 1f};
        verticies[4] = new float[] {1f, 0f, 0f};
        verticies[5] = new float[] {1f, 0f, 1f};
        */
    }

    /**
    * Call-back handler for window re-size event. Also called when the drawable is 
    * first set to visible.
    */
   @Override
   public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
      GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

      if (height == 0) height = 1;   // prevent divide by zero
      float aspect = (float)width / height;

      // Set the view port (display area) to cover the entire window
      gl.glViewport(0, 0, width, height);

      // Setup perspective projection, with aspect ratio matches viewport
      gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
      gl.glLoadIdentity();             // reset projection matrix
      glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar

      // Enable the model-view transform
      gl.glMatrixMode(GL_MODELVIEW);
      gl.glLoadIdentity(); // reset
   }

   /**
    * Called back by the animator to perform rendering.
    */
   @Override
   public void display(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
      gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers

      // ------ Render a Cube with texture ------
      gl.glLoadIdentity();                    // reset model-view matrix
      gl.glTranslatef(0.0f, -0.5f, z);         // translate into the screen
      gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f); // rotate about the x-axis
      gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f); // rotate about the y-axis

      // Enables this texture's target in the current GL context's state.
      textures[currTextureFilter].enable(gl);
      // Bind the texture with the currently chosen filter to the current OpenGL graphics context.
      textures[currTextureFilter].bind(gl);
      
      if (isLightOn) {
         gl.glEnable(GL_LIGHTING);
      } else {
         gl.glDisable(GL_LIGHTING);
      }
       float[] ambientColor = new float[] {0.2f, 0.2f, 0.2f, 1.0f};
       FloatBuffer color = FloatBuffer.allocate(ambientColor.length);
       color.wrap(ambientColor);
       color.position(0);
       gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, color);
       gl.glLightfv(GL_LIGHT0, GL_LIGHT_MODEL_AMBIENT, color);
/*
      gl.glBegin(GL_QUADS); // of the color cube

      // Front Face3D
      gl.glNormal3f(0.0f, 0.0f, 1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, 1.0f); // bottom-left of the texture and quad
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, 1.0f);  // bottom-right of the texture and quad
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(1.0f, 1.0f, 1.0f);   // top-right of the texture and quad
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, 1.0f);  // top-left of the texture and quad

      // Back Face3D
      gl.glNormal3f(0.0f, 0.0f, -1.0f);
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(1.0f, 1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, -1.0f);

      // Top Face3D
      gl.glNormal3f(0.0f, 1.0f, 0.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(-1.0f, 1.0f, 1.0f);
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(1.0f, 1.0f, 1.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(1.0f, 1.0f, -1.0f);

      // Bottom Face3D
      gl.glNormal3f(0.0f, -1.0f, 0.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(-1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, 1.0f);
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, 1.0f);

      // Right face
      gl.glNormal3f(1.0f, 0.0f, 0.0f);
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(1.0f, 1.0f, -1.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(1.0f, 1.0f, 1.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(1.0f, -1.0f, 1.0f);

      // Left Face3D
      gl.glNormal3f(-1.0f, 0.0f, 0.0f);
      gl.glTexCoord2f(textureLeft, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, -1.0f);
      gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(-1.0f, -1.0f, 1.0f);
      gl.glTexCoord2f(textureRight, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, 1.0f);
      gl.glTexCoord2f(textureLeft, textureTop);
      gl.glVertex3f(-1.0f, 1.0f, -1.0f);

      gl.glEnd();
     */

       gl.glBegin(GL_TRIANGLE_STRIP);

       float[ ] normal = countNormal(verticies[1], verticies[0], verticies[2]);
      // Font-face triangle
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glTexCoord2f(textureLeft, textureTop);
       gl.glVertex3f(verticies[1][0], verticies[1][1], verticies[1][2]);

       normal = countNormal(verticies[0], verticies[1], verticies[2]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glTexCoord2f(textureLeft, textureBottom);
       gl.glVertex3f(verticies[0][0], verticies[0][1], verticies[0][2]);

       normal = countNormal(verticies[2], verticies[0], verticies[1]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glTexCoord2f(textureRight, textureBottom);
      gl.glVertex3f(verticies[2][0], verticies[2][1], verticies[2][2]);

      // Right-face triangle

       //gl.glNormal3f(0,1,0);

       normal = countNormal(verticies[2], verticies[0], verticies[3]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glTexCoord2f(textureLeft, textureTop);
       gl.glVertex3f(verticies[2][0], verticies[2][1], verticies[2][2]);

       gl.glTexCoord2f(textureLeft, textureBottom);
       normal = countNormal(verticies[0], verticies[2], verticies[3]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glVertex3f(verticies[0][0], verticies[0][1], verticies[0][2]);

       gl.glTexCoord2f(textureRight, textureBottom);
       normal = countNormal(verticies[3], verticies[2], verticies[0]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glVertex3f(verticies[3][0], verticies[3][1], verticies[3][2]);

      // Back-face triangle

       //gl.glNormal3f(0,1,0);
       normal = countNormal(verticies[3], verticies[0], verticies[4]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glTexCoord2f(textureLeft, textureTop);
       gl.glVertex3f(verticies[3][0], verticies[3][1], verticies[3][2]);

       gl.glTexCoord2f(textureLeft, textureBottom);
       normal = countNormal(verticies[0], verticies[3], verticies[4]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glVertex3f(verticies[0][0], verticies[0][1], verticies[0][2]);

       gl.glTexCoord2f(textureRight, textureBottom);
       normal = countNormal(verticies[4], verticies[3], verticies[0]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glVertex3f(verticies[4][0], verticies[4][1], verticies[4][2]);

      // Left-face triangle

       //gl.glNormal3f(0,1,0);

       normal = countNormal(verticies[4], verticies[0], verticies[1]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glTexCoord2f(textureLeft, textureTop);
       gl.glVertex3f(verticies[4][0], verticies[4][1], verticies[4][2]);

       normal = countNormal(verticies[0], verticies[4], verticies[1]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glTexCoord2f(textureLeft, textureBottom);
       gl.glVertex3f(verticies[0][0], verticies[0][1], verticies[0][2]);

       normal = countNormal(verticies[1], verticies[4], verticies[0]);
       gl.glNormal3f(normal[0], normal[1], normal[2]);
       gl.glTexCoord2f(textureRight, textureBottom);
       gl.glVertex3f(verticies[1][0], verticies[1][1], verticies[1][2]);

      // Font-face triangle
       normal = countNormal(verticies[5], verticies[1], verticies[2]);
       gl.glNormal3f(-normal[0], -normal[1], -normal[2]);

       gl.glTexCoord2f(textureLeft, textureTop);
       gl.glVertex3f(verticies[5][0], verticies[5][1], verticies[5][2]);
       gl.glTexCoord2f(textureLeft, textureBottom);
       gl.glVertex3f(verticies[1][0], verticies[1][1], verticies[1][2]);
       gl.glTexCoord2f(textureRight, textureBottom);
       gl.glVertex3f(verticies[2][0], verticies[2][1], verticies[2][2]);

       // Right-face triangle
       normal = countNormal(verticies[5], verticies[2], verticies[3]);
       gl.glNormal3f(-normal[0], -normal[1], -normal[2]);

       gl.glTexCoord2f(textureLeft, textureTop);
       gl.glVertex3f(verticies[5][0], verticies[5][1], verticies[5][2]);
       gl.glTexCoord2f(textureLeft, textureBottom);
       gl.glVertex3f(verticies[2][0], verticies[2][1], verticies[2][2]);
       gl.glTexCoord2f(textureRight, textureBottom);
       gl.glVertex3f(verticies[3][0], verticies[3][1], verticies[3][2]);

       // Back-face triangle
       normal = countNormal(verticies[5], verticies[3], verticies[4]);
       gl.glNormal3f(-normal[0], -normal[1], -normal[2]);

       gl.glTexCoord2f(textureLeft, textureTop);
       gl.glVertex3f(verticies[5][0], verticies[5][1], verticies[5][2]);
       gl.glTexCoord2f(textureLeft, textureBottom);
       gl.glVertex3f(verticies[3][0], verticies[3][1], verticies[3][2]);
       gl.glTexCoord2f(textureRight, textureBottom);
       gl.glVertex3f(verticies[4][0], verticies[4][1], verticies[4][2]);

       // Left-face triangle
       normal = countNormal(verticies[5], verticies[4], verticies[1]);
       //gl.glNormal3f(-normal[0], -normal[1], -normal[2]);

       gl.glTexCoord2f(textureLeft, textureTop);
       gl.glVertex3f(verticies[5][0], verticies[5][1], verticies[5][2]);
       gl.glTexCoord2f(textureLeft, textureBottom);
       gl.glVertex3f(verticies[4][0], verticies[4][1], verticies[4][2]);
       gl.glTexCoord2f(textureRight, textureBottom);
       gl.glVertex3f(verticies[1][0], verticies[1][1], verticies[1][2]);

       gl.glEnd(); // of the pyramid
      
      // Update the rotational position after each refresh.
      angleX += rotateSpeedX;
      angleY += rotateSpeedY;
   }

    private float[] countNormal(float[] pointA, float[] pointB, float[] pointC) {
        float[] normal = new float[3];

        float[] nVector1 = new float[3];
        float[] nVector2 = new float[3];

        float vectorLength = (float)Math.sqrt(
                        (pointB[0] - pointA[0])*(pointB[0] - pointA[0]) +
                        (pointB[1] - pointA[1])*(pointB[1] - pointA[1]) +
                        (pointB[2] - pointA[2])*(pointB[2] - pointA[2])
                     );

        nVector1[0] = (pointB[0] - pointA[0])/ vectorLength;
        nVector1[1] = (pointB[1] - pointA[1])/ vectorLength;
        nVector1[2] = (pointB[2] - pointA[2])/ vectorLength;

        vectorLength = (float)Math.sqrt(
                        (pointC[0] - pointA[0])*(pointC[0] - pointA[0]) +
                        (pointC[1] - pointA[1])*(pointC[1] - pointA[1]) +
                        (pointC[2] - pointA[2])*(pointC[2] - pointA[2])
                    );

        nVector2[0] = (pointC[0] - pointA[0])/ vectorLength;
        nVector2[1] = (pointC[1] - pointA[1])/ vectorLength;
        nVector2[2] = (pointC[2] - pointA[2])/ vectorLength;

        normal[0] = nVector1[1]*nVector2[2] - nVector1[2]*nVector2[1];
        normal[1] = nVector1[2]*nVector2[0] - nVector1[0]*nVector2[2];
        normal[2] = nVector1[0]*nVector2[1] - nVector1[1]*nVector2[0];

        return normal;
    }

    /**
    * Called back before the OpenGL context is destroyed. Release resource such as buffers. 
    */
   @Override
   public void dispose(GLAutoDrawable drawable) { }

   // ------ Implement methods declared in KeyListener ------

   @Override
   public void keyPressed(KeyEvent e) {
      int keyCode = e.getKeyCode();
      switch (keyCode) {
         case VK_L: // toggle light on/off
            isLightOn = !isLightOn;
            break;
         case VK_F: // switch to the next filter (NEAREST, LINEAR, MIPMAP)
            currTextureFilter = (currTextureFilter + 1) % textures.length;
            break;
         case VK_PAGE_UP:   // zoom-out
            z -= zIncrement;
            break;
         case VK_PAGE_DOWN: // zoom-in
            z += zIncrement;
            break;
         case VK_UP:   // decrease rotational speed in x
            rotateSpeedX -= rotateSpeedXIncrement;
            break;
         case VK_DOWN: // increase rotational speed in x
            rotateSpeedX += rotateSpeedXIncrement;
            break;
         case VK_LEFT:  // decrease rotational speed in y
            rotateSpeedY -= rotateSpeedYIncrement;
            break;
         case VK_RIGHT: // increase rotational speed in y
            rotateSpeedY += rotateSpeedYIncrement;
            break;
      }
   }

   @Override
   public void keyReleased(KeyEvent e) {}

   @Override
   public void keyTyped(KeyEvent e) {}
}
