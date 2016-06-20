package renderers;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import objects.*;

import javax.imageio.ImageIO;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static java.awt.event.KeyEvent.*;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_NEAREST;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_NICEST;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLE_STRIP;
import static com.jogamp.opengl.GL2.GL_LIGHT0;
import static com.jogamp.opengl.GL2.*;
import static com.jogamp.opengl.GL2.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHT1;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_POSITION;

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
public class ModelRenderer extends GLCanvas
        implements GLEventListener, KeyListener {

    private GLU glu;  // for the GL Utility
    private static float angleX = 0.0f; // rotational angle for x-axis in degree
    private static float angleY = 0.0f; // rotational angle for y-axis in degree
    private static float z = -2.0f;     // z-location
    private static float rotateSpeedX = 0.0f; // rotational speed for x-axis
    private static float rotateSpeedY = 0.0f; // rotational speed for y-axis

    private static float zIncrement = 0.02f;  // for zoom in/out
    private static float rotateSpeedXIncrement = 0.01f; // adjusting x rotational speed
    private static float rotateSpeedYIncrement = 0.01f; // adjusting y rotational speed

    // Textures with three different filters - Nearest, Linear & MIPMAP
    private Texture[] textures = new Texture[3];
    private Texture[] texturesR = new Texture[3];
    private Texture[] texturesG = new Texture[3];
    private Texture[] texturesB = new Texture[3];
    private static int currTextureFilter = 0; // currently used filter
    private String textureFileName = "images/crate.png";

    private String textureR = "images/r.jpg";
    private String textureG = "images/g.jpg";
    private String textureB = "images/b.jpg";

    // Texture image flips vertically. Shall use TextureCoords class to retrieve the
    // top, bottom, left and right coordinates.
    private float textureTop, textureBottom, textureLeft, textureRight;

    // Lighting
    private static boolean isLightOn;

    private boolean blendingEnabled;
    private boolean blendingChanged = false;

    //ObjModel model = new ObjModel();

    ObjModel redCores = new ObjModel(true, false, false);
    ObjModel blueCores = new ObjModel(false, true, false);
    ObjModel greenCores = new ObjModel(false, false, true);

    //TriangledModel model = new TriangledModel();

    /** Constructor to setup the GUI for this Component */
    public ModelRenderer() {
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

            BufferedImage imageR =
                    ImageIO.read(getClass().getClassLoader().getResource(textureR));

            BufferedImage imageG =
                    ImageIO.read(getClass().getClassLoader().getResource(textureG));

            BufferedImage imageB =
                    ImageIO.read(getClass().getClassLoader().getResource(textureB));

            // Create a OpenGL Texture object
            textures[0] = AWTTextureIO.newTexture(GLProfile.getDefault(), image, false);
            texturesR[0] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageR, false);
            texturesG[0] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageG, false);
            texturesB[0] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageB, false);

            // Nearest filter is least compute-intensive
            // Use nearer filter if image is larger than the original texture
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            // Use nearer filter if image is smaller than the original texture
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            textures[1] = AWTTextureIO.newTexture(GLProfile.getDefault(), image, false);
            texturesR[1] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageR, false);
            texturesG[1] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageG, false);
            texturesB[1] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageB, false);
            // Linear filter is more compute-intensive
            // Use linear filter if image is larger than the original texture
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            // Use linear filter if image is smaller than the original texture
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            textures[2] = AWTTextureIO.newTexture(GLProfile.getDefault(), image, true); // mipmap is true
            texturesR[2] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageR, true); // mipmap is true
            texturesG[2] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageG, true); // mipmap is true
            texturesB[2] = AWTTextureIO.newTexture(GLProfile.getDefault(), imageB, true); // mipmap is true
            // Use mipmap filter is the image is smaller than the texture
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR_MIPMAP_NEAREST);

            // Get the top and bottom coordinates of the textures. Image flips vertically.
            TextureCoords textureCoords;
            textureCoords = texturesR[0].getImageTexCoords();
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

        /*
        if (redCores.isTextureOn && blueCores.isTextureOn && greenCores.isTextureOn) {
            // Enables this texture's target in the current GL context's state.
            textures[currTextureFilter].enable(gl);
            // Bind the texture with the currently chosen filter to the current OpenGL graphics context.
            textures[currTextureFilter].bind(gl);
        }
        else {
            textures[currTextureFilter].disable(gl);

            //textures[currTextureFilter].destroy(gl);
        */

        if (isLightOn) {
            gl.glEnable(GL_LIGHTING);
        } else {
            gl.glDisable(GL_LIGHTING);
        }

        // toggle blending
        if (blendingChanged) {
            if (blendingEnabled) {
                gl.glEnable(GL_BLEND);
                gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                gl.glEnable(GL.GL_CULL_FACE);
                gl.glEnable(GL_NORMALIZE);
                gl.glDisable(GL.GL_DEPTH_TEST);  // Turn Depth Testing Off
                //redCores.invertNormals();
                //greenCores.invertNormals();
                //blueCores.invertNormals();
            } else {
                gl.glDisable(GL.GL_BLEND);    // Turn Blending Off
                gl.glDisable(GL_NORMALIZE);
                gl.glDisable(GL_CULL_FACE);
                gl.glEnable(GL.GL_DEPTH_TEST);  // Turn Depth Testing On
                //redCores.invertNormals();
                //greenCores.invertNormals();
                //blueCores.invertNormals();
            }
            blendingChanged = false;
        }

        if (redCores.isTextureOn && blueCores.isTextureOn && greenCores.isTextureOn) {
            // Enables this texture's target in the current GL context's state.
            texturesR[currTextureFilter].enable(gl);
            // Bind the texture with the currently chosen filter to the current OpenGL graphics context.
            texturesR[currTextureFilter].bind(gl);
            redCores.render(gl, textures[0].getImageTexCoords());
            texturesR[currTextureFilter].disable(gl);

            texturesG[currTextureFilter].enable(gl);
            texturesG[currTextureFilter].bind(gl);
            greenCores.render(gl, textures[0].getImageTexCoords());
            texturesG[currTextureFilter].disable(gl);

            texturesB[currTextureFilter].enable(gl);
            texturesB[currTextureFilter].bind(gl);
            blueCores.render(gl, textures[0].getImageTexCoords());
            texturesB[currTextureFilter].disable(gl);
        }
        else {
            textures[currTextureFilter].disable(gl);
            texturesR[currTextureFilter].disable(gl);
            texturesG[currTextureFilter].disable(gl);
            texturesB[currTextureFilter].disable(gl);
            redCores.render(gl, textures[0].getImageTexCoords());
            greenCores.render(gl, textures[0].getImageTexCoords());
            blueCores.render(gl, textures[0].getImageTexCoords());
        }

        angleX += rotateSpeedX;
        angleY += rotateSpeedY;
    }

    /**
     * Called back before the OpenGL context is destroyed. Release resource such as buffers.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) { }

    // ------ Implement methods declared in KeyListener ------

    public void toggleBlending() {
        blendingEnabled = !blendingEnabled;
        blendingChanged = true;
    }

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
            case VK_N: // toggle normals on/off
                redCores.isSelfNormals = !redCores.isSelfNormals;
                greenCores.isSelfNormals = !greenCores.isSelfNormals;
                blueCores.isSelfNormals = !blueCores.isSelfNormals;
                break;
            case VK_T: // toggle normals on/off
                redCores.isTextureOn = !redCores.isTextureOn;
                greenCores.isTextureOn = !greenCores.isTextureOn;
                blueCores.isTextureOn = !blueCores.isTextureOn;
                break;
            case VK_I: // toggle normals on/off
                redCores.isInterpolate = !redCores.isInterpolate;
                greenCores.isInterpolate = !greenCores.isInterpolate;
                blueCores.isInterpolate = !blueCores.isInterpolate;
                break;
            case VK_W:
                redCores.interpolateNumber++;
                redCores.interpolate();

                greenCores.interpolateNumber++;
                greenCores.interpolate();

                blueCores.interpolateNumber++;
                blueCores.interpolate();
                break;
            case VK_S:
                redCores.interpolateNumber--;
                if (redCores.interpolateNumber >0) {
                    redCores.interpolate();
                }
                else
                    redCores.interpolateNumber = 1;

                greenCores.interpolateNumber--;
                if (greenCores.interpolateNumber >0) {
                    greenCores.interpolate();
                }
                else
                    greenCores.interpolateNumber = 1;

                blueCores.interpolateNumber--;
                if (blueCores.interpolateNumber >0) {
                    blueCores.interpolate();
                }
                else
                    blueCores.interpolateNumber = 1;
                break;

            case VK_1:
                    redCores.isRenderable = !redCores.isRenderable;
                break;

            case VK_2:
                greenCores.isRenderable = !greenCores.isRenderable;
                break;

            case VK_3:
                blueCores.isRenderable = !blueCores.isRenderable;
                break;

            case VK_B:
                toggleBlending();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
