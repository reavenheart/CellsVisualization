import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Denis on 07.06.2016.
 */
public class JOGLWindow {
    static Example renderer;

    public static void main( String [] args ) {
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        //GLJPanel gljpanel = new GLJPanel( glcapabilities );
        renderer = new Example();

        //gljpanel.addGLEventListener(renderer);

        FPSAnimator animator = new FPSAnimator(renderer, 60, true);
        //animator.add(renderer);
        animator.start();

        final JFrame jframe = new JFrame( "JOGL visualization window." );
        jframe.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) {
                jframe.dispose();
                System.exit( 0 );
            }
        });

        jframe.getContentPane().add( renderer, BorderLayout.CENTER );
        jframe.setSize( 640, 480 );
        jframe.setVisible( true );
    }
}
