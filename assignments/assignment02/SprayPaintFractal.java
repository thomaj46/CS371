package CS371.assignments.assignment02;

/**
 * Copied from class demonstration program
 *
 *
 * SprayPaintFractal.java - Use two-dimensional affine transformations
 * to paint a fractal loaded from an IFS file.  Each line of an IFS
 * file specifies a composite transformation of the 7-term form:
 *
 * rotatescale_xx rotate_scale_xy rotate_scale_yx rotate_scale_yy
 * trans_x trans_y probability-of-trans
 *
 * The command line for running the program has arguments:
 *
 * ifsfile-name numpts left-wc right-wc bottom-wc top-wc original-x original-y
 * e.g., dragon.ifs 40000 -16.0 16.0 0.0 16.0 0.0 0.0
 * e.g., tri.ifs 40000 -5.0 5.0 -5.0 5.0 0.0 0.0
 *
 * Interaction ESC to quit; r,g,b,y,w to toggle colors
 *
 * CONVERSELY -- If you already have the seven "magic arrays" that
 * comprise an iterated function system and are drawing the fractal by
 * a technique other than the spray point algorithm here, then you can
 * merely use the flavor of the constructor that takes these arrays
 * and an output filename to write the iterated function system to an
 * IFS-file format.
 */

import java.awt.*;
import java.awt.event.*;
import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import com.jogamp.opengl.util.gl2.GLUT;

import java.io.*;
import java.util.*;

public class SprayPaintFractal extends JFrame implements GLEventListener, KeyListener
{
    Random random;

    GLU glu;
    GLUT glut;

    GLCapabilities caps;
    GLJPanel canvas;

    final static int NTRANS = 25; // maximum possible number of transformations

    // Transformation information
    int ntrans;  // actual number of transformations
    double rotate_scale_xx[  ];
    double rotate_scale_xy[  ];
    double rotate_scale_yx[  ];
    double rotate_scale_yy[  ];
    double trans_x[  ];
    double trans_y[  ];
    double prob[  ];

    // World coordinate window
    static double left, right, bottom, top;

    // Count for number of points
    static long numpts;

    // IFS file name
    static String ifsfile;

    // Starting point for spray
    static double orig_x, orig_y;

    // Color for spray
    float red, green, blue;

    // This flavor of the constructor assume you have an existing
    // IFS-format file and will construct a GL drawing canvas for the
    // fractal as read in from that file
    //
    public SprayPaintFractal(String ifsfile) {
        super("SprayPaintFractal");

        this.ifsfile = ifsfile;
        rotate_scale_xx = new double[NTRANS];
        rotate_scale_xy = new double[NTRANS];
        rotate_scale_yx = new double[NTRANS];
        rotate_scale_yy = new double[NTRANS];
        trans_x = new double[NTRANS];
        trans_y = new double[NTRANS];
        prob = new double[NTRANS];

        random = new Random(321);

        loadifs();
        //	new SprayPaintFractal("foo.ifs", ntrans, rotate_scale_xx, rotate_scale_xy, rotate_scale_yx, rotate_scale_yy, trans_x, trans_y, prob).writeifs();
        red = green = blue = 1.0f;
        canvas = new GLJPanel();
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);

        getContentPane().add(canvas);

    }

    // This flavor of the constructor assumes that you've created the
    // seven "magic number" arrays by other means (as you will do in
    // the second assignment) and want to write that iterated function
    // system to an IFS-format file, which you you can do by later
    // calling writeifs().
    //
    public SprayPaintFractal(String ifsfile,
                             int ntrans,
                             double rotate_scale_xx [],
                             double rotate_scale_xy [],
                             double rotate_scale_yx [],
                             double rotate_scale_yy [],
                             double trans_x [],
                             double trans_y [],
                             double prob []) {
        super("SprayPaintFractal");

        this.ifsfile = ifsfile;
        this.ntrans = ntrans;
        this.rotate_scale_xx = rotate_scale_xx;
        this.rotate_scale_xy = rotate_scale_xy;
        this.rotate_scale_yx = rotate_scale_yx;
        this.rotate_scale_yy = rotate_scale_yy;
        this.trans_x = trans_x;
        this.trans_y = trans_y;
        this.prob = prob;
    }

    public static void main(String[] args) {

        if (args.length == 8) {
            ifsfile = "CS371/assignments/assignment02/ifs/john.ifs"; //args[0];
            try {
                numpts = 80000; //Long.parseLong(args[1]);
                left = -10; //Double.parseDouble(args[2]);
                right = 10; //Double.parseDouble(args[3]);
                bottom = -8; //Double.parseDouble(args[4]);
                top = 14; //Double.parseDouble(args[5]);
                orig_x = 0; //Double.parseDouble(args[6]);
                orig_y = 0; //Double.parseDouble(args[7]);
            } catch (NumberFormatException e) {
                System.err.println("After filename should see: numpts left-wc right-wc bottom-wc top-wc original-x original-y");
                System.exit(1);
            }
        }
        else {
            System.err.println("Usage: java SprayPaintFractal ifsfile-name numpts left-wc right-wc bottom-wc top-wc original-x original-y");
            System.exit(1);
        }
        new SprayPaintFractal(ifsfile).run();
    }

    public void run()
    {
        setSize(600,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }


    /* The following routine loads Iterated Function System codes.  Note
     * that loadifs() only reads the very first set of IFS codes in the
     * file and ignores the name label in the file entirely.
     */
    void loadifs()
    {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(ifsfile)));
        } catch (Exception e) { System.out.println(e.toString()); };

        ntrans = 0;

        String line = null;

        // throw away first line
        try {
            line = in.readLine();
        } catch (IOException e) { System.out.println(e.toString()); }

        try {
            while ((line = in.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+"); // Tokens separated by "whitespace"
                if (tokens[0].equals("}")) return;
                try {
                    rotate_scale_xx[ntrans] = Double.parseDouble(tokens[0]);
                    rotate_scale_xy[ntrans] = Double.parseDouble(tokens[1]);
                    rotate_scale_yx[ntrans] = Double.parseDouble(tokens[2]);
                    rotate_scale_yy[ntrans] = Double.parseDouble(tokens[3]);
                    trans_x[ntrans] = Double.parseDouble(tokens[4]);
                    trans_y[ntrans] = Double.parseDouble(tokens[5]);
                    prob[ntrans] = Double.parseDouble(tokens[6]);
                } catch (NumberFormatException ex) {
                    System.out.println("Not a double ");
                    System.out.println(ex);
                }
                ntrans++;
            }
        }
        catch (IOException e) { System.out.println(e.toString());}
    }

    // Write the iterated function system data out to an IFS-format file
    void writeifs() {
        PrintWriter out = null;

        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(ifsfile)));
            out.println(ifsfile + " {");
            for (int i = 0; i < ntrans; i++) {
                out.println(String.format("%f %f %f %f %f %f %f",
                        rotate_scale_xx[i],
                        rotate_scale_xy[i],
                        rotate_scale_yx[i],
                        rotate_scale_yy[i],
                        trans_x[i], trans_y[i],
                        prob[i]));
            }
            out.println("}");
            out.close();
        }
        catch (IOException ex) {System.err.println(ex);}

    }

    // The spray paint algorithm for the IFS representation of the
    // fractal is encapsulated here
    void paintifs(GL2 gl) {

        int iter, t;
        double oldx, oldy, newx, newy, p;
        double cumulative_prob [] = new double [ntrans];
        cumulative_prob[0] = prob[0];
        for (int i = 1; i < ntrans; i++)
            cumulative_prob[i] = cumulative_prob[i-1] + prob[i]; // Make probability cumulative

        iter = 0;
        oldx = orig_x;
        oldy = orig_y;
        while (iter < numpts)
        {
            p = Math.random();

            // Select transformation t
            t = 0;
            while ((p > cumulative_prob[t]) && (t < ntrans - 1)) ++t;

            // Transform point by transformation t
            newx = rotate_scale_xx[t]*oldx + rotate_scale_xy[t]*oldy + trans_x[t];
            newy = rotate_scale_yx[t]*oldx + rotate_scale_yy[t]*oldy + trans_y[t];

            // Jump around for awhile without plotting to make
            //   sure the first point seen is attracted into the
            //   fractal
            if (iter > 50) {
                setColor(gl);
                gl.glBegin(GL.GL_POINTS);
                gl.glVertex2d(newx,newy);
                gl.glEnd();
            }
            oldx = newx;
            oldy = newy;
            iter++;
        }
    }

    public void setColor(GL2 gl)
    {
        double rand = random.nextDouble();

        if (rand < .75)
        {
            // Blue 336699
            gl.glColor3f(.2f, .4f, .6f);
        }
        else if (rand < .9)
        {
            // 19538A
            gl.glColor3f(.31f, .49f, .67f);
        }
        else
        {
            // 7BA3CA
            gl.glColor3f(.48f, .64f, .8f);
        }
    }


    //////////////////////////////////////////////////////////
    // init, display, reshape, and displayChanged comprise the
    // GLEventListener interface

    public void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();

        gl.glClearColor(0.11f, 0.231f, 0.0f, 0.0f);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(left, right, bottom, top);
        gl.glViewport((int)bottom, (int)left, (int)top, (int)right);
        gl.glMatrixMode(GL2.GL_MODELVIEW);


    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
        //gl.glColor3f (red, green, blue);
        paintifs(gl);
        gl.glFlush ();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) { // Use the default reshape
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
                               boolean deviceChanged) { // Nothing for us to do here
    }

    public void dispose(GLAutoDrawable arg0) { // GLEventListeners must implement
    }

    /////////////////////////////////////////////////////////////////
    // Methods in the KeyListener interface are keyTyped, keyPressed,
    // keyReleased.  Listeners should affect the animation by changing
    // state variables, NOT by directory making calls to GL graphic
    // methods -- that should be left for the display method.

    public void keyTyped(KeyEvent key)
    {
    }

    public void keyPressed(KeyEvent key)
    {
        int keyCode = key.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE)
        {
            System.exit(0);
        }

        switch (key.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                // The call to display below is only used because there is no animation occurring
            case KeyEvent.VK_R : red = 1.0f; green = 0.0f; blue = 0.0f; canvas.display(); break;
            case KeyEvent.VK_G : red = 0.0f; green = 1.0f; blue = 0.0f; canvas.display(); break;
            case KeyEvent.VK_B : red = 0.0f; green = 0.0f; blue = 1.0f; canvas.display(); break;
            case KeyEvent.VK_Y : red = 1.0f; green = 1.0f; blue = 0.0f; canvas.display(); break;
            case KeyEvent.VK_W : red = 1.0f; green = 1.0f; blue = 1.0f; canvas.display(); break;
            default:
                break;
        }
    }

    public void keyReleased(KeyEvent key)
    {
    }

}