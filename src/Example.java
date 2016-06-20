import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_B;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_NORMALIZE;


@SuppressWarnings("serial")
public class Example extends GLCanvas
		implements GLEventListener, KeyListener {
	
	private static int width;
	private static int height;
	private FPSAnimator animator;
	
	private GLModel chairModel = null;

	private static float angleX = 0.0f; // rotational angle for x-axis in degree
	private static float angleY = 0.0f; // rotational angle for y-axis in degree
	private static float z = -4.0f;     // z-location
	private static float rotateSpeedX = 0.0f; // rotational speed for x-axis
	private static float rotateSpeedY = 0.0f; // rotational speed for y-axis

	private static float zIncrement = 0.02f;  // for zoom in/out
	private static float rotateSpeedXIncrement = 0.01f; // adjusting x rotational speed
	private static float rotateSpeedYIncrement = 0.01f; // adjusting y rotational speed

	private static int currTextureFilter = 0;
	private Texture[] textures = new Texture[3];
	private String textureFileName = "images/crate.png";

	private static boolean isLightOn;

	private boolean blendingEnabled;
	private boolean blendingChanged = false;

	public Example() {
		setFocusable(true);
		addGLEventListener(this);
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		animator = new FPSAnimator(this, 60, false);
		animator.start();
		width = height = 800;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		gl.glLoadIdentity();
		//gl.glTranslatef(0,0,-1);
		gl.glTranslatef(0.0f, 0f, z);         // translate into the screen
		gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f); // rotate about the x-axis
		gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f); // rotate about the y-axis
		gl.glScalef(0.08f, 0.08f, 0.08f);

		if (isLightOn) {
			gl.glEnable(GL_LIGHTING);
		} else {
			gl.glDisable(GL_LIGHTING);
		}

		if (blendingChanged) {
			if (blendingEnabled) {
				gl.glEnable(GL_BLEND);
				gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				gl.glEnable(GL_CULL_FACE);
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

		angleX += rotateSpeedX;
		angleY += rotateSpeedY;

		chairModel.opengldraw(gl);

		//gl.glFlush();
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glEnable(GL_NORMALIZE);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU glu = new GLU();
		
		if (false == loadModels(gl)) {
			System.exit(1);
		}
		
		setLight(gl);

		glu.gluPerspective(1, (double) getWidth() / getHeight(), 0.3, 50);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}
	
	private void setLight(GL2 gl) {
		
		gl.glEnable(GL_LIGHTING);
		
		float SHINE_ALL_DIRECTIONS = 1;
		float[] lightPos = { -30, 30, 30, SHINE_ALL_DIRECTIONS };
		float[] lightColorAmbient = { 0.02f, 0.02f, 0.02f, 1f };
		float[] lightColorSpecular = { 0.9f, 0.9f, 0.9f, 1f };

		// Set light parameters.
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightColorSpecular, 0);
		gl.glEnable(GL2.GL_LIGHT1);
		
	}

	private Boolean loadModels(GL2 gl) {
		chairModel = ModelLoaderOBJ.LoadModel("./models/cells.obj", null, gl);
		if (chairModel == null) {
			return false;
		}
		return true;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU glu = new GLU();

		glu.gluPerspective(100, (double) getWidth() / getHeight(), 0.1, 100);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

	}

	public static void main(String[] args) {
		JFrame window = new JFrame();
		window.getContentPane().add(new Example());
		window.setSize(width, height);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
				//model.isSelfNormals = !model.isSelfNormals;
				break;
			case VK_T: // toggle normals on/off
				//model.isTextureOn = !model.isTextureOn;
				break;
			case VK_I: // toggle normals on/off
				//model.isInterpolate = !model.isInterpolate;
				break;
			case VK_W:
				//model.interpolateNumber++;
				//model.interpolate();
				break;
			case VK_S:
				//model.interpolateNumber--;
				/*
				if (model.interpolateNumber >0) {
					model.interpolate();
				}
				else
					model.interpolateNumber = 1;
					*/
				break;
			case VK_B:
				toggleBlending();
				break;
		}
	}

	public void toggleBlending() {
		blendingEnabled = !blendingEnabled;
		blendingChanged = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

}
