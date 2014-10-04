package CS371.assignments.assignment02;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by John on 10/1/2014.
 */
public class DrawByLevelFractal extends JFrame implements GLEventListener, KeyListener
{

    FPSAnimator animator;
    GL2 gl;
    GLU glu;
    GLUT glut;

    GLJPanel canvas;
    GLCapabilities caps;
    ArrayList<String> ifsFiles;
    String baseDir, caption, ifsfile;
    int maximumTransitions = 25;
    int base, pointsToDraw, transitions, currentDrawList, decrementFrames, incrementFrames, maxFrames, framesDrawn, currentTransitions;
    float left, right, bottom, top, xOrigin, yOrigin;
    double[] rotate_scale_xx, rotate_scale_xy, rotate_scale_yx, rotate_scale_yy, trans_x, trans_y, prob;
    private boolean drawn;

    public DrawByLevelFractal()
    {
        baseDir = "CS371/assignments/assignment02/ifs/";

        ifsFiles = new ArrayList<String>();
        ifsFiles.add("carpet.ifs");
        ifsFiles.add("chaos.ifs");
        ifsFiles.add("coral.ifs");
        ifsFiles.add("curl.ifs");
        ifsFiles.add("four.ifs");
        ifsFiles.add("galaxy.ifs");
        ifsFiles.add("dragon.ifs");
        ifsFiles.add("leady.ifs");
        ifsFiles.add("koch.ifs");
        ifsFiles.add("mouse.ifs");
        ifsFiles.add("leaf.ifs");
        ifsFiles.add("seven.ifs");
        ifsFiles.add("three.ifs");
        ifsFiles.add("tri.ifs");

        pointsToDraw = 80000;
        left = -7;
        right = 7;
        bottom = -7;
        top = 11;
        xOrigin = 0;
        yOrigin = 0;

        rotate_scale_xx = new double[maximumTransitions];
        rotate_scale_xy = new double[maximumTransitions];
        rotate_scale_yx = new double[maximumTransitions];
        rotate_scale_yy = new double[maximumTransitions];
        trans_x = new double[maximumTransitions];
        trans_y = new double[maximumTransitions];
        prob = new double[maximumTransitions];

        caps = new GLCapabilities(GLProfile.getGL2GL3());
        caps.setDoubleBuffered(true); // request double buffer display mode
        caps.setHardwareAccelerated(true);
        canvas = new GLJPanel();
        //canvas.setOpaque(true);
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        animator = new FPSAnimator(canvas, 60);

        getContentPane().add(canvas);
    }

    public static void main(String[] args)
    {
        new DrawByLevelFractal().run();
    }

    private void run()
    {
        setSize(800,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        animator.start();
    }


    @Override
    public void init(GLAutoDrawable glAutoDrawable)
    {
        gl = glAutoDrawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();

        gl.glClearColor(0.549f, 0.675f, 0.227f, 0.0f);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(left, right, bottom, top);
        gl.glViewport((int)bottom, (int)left, (int)top, (int)right);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glColor3f(.357f, .184f, .478f);

        maxFrames = 50 * ifsFiles.size() + 1000;
        base = gl.glGenLists(maxFrames);
        currentDrawList = base;

        ifsfile = "CS371/assignments/assignment02/tri.ifs";
        loadifs();
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable)
    {
        gl = glAutoDrawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // Fonts draw selves at the current raster position
        gl.glRasterPos2f(right - (right + 2.0f), bottom + 1);
        caption = "Please wait... drawing frames";
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, caption);

        drawByLevel();

        gl.glFlush();
    }

    private void drawByLevel()
    {
        recursiveDraw(4, -2, 1, 2, 1, 0, -2);
    }

    private void recursiveDraw(int level, double x1, double y1, double x2, double y2, double x3, double y3)
    {
        if (level < 1)
        {
            return;
        }

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2d(x1, y1);
        gl.glVertex2d(x2, y2);
        gl.glVertex2d(x3, y3);
        gl.glEnd();

        for (int i = 0; i < transitions; i += 1)
        {
            x1 = rotate_scale_xx[i] * x1 + rotate_scale_xy[i] * y1 + trans_x[i];
            y1 = rotate_scale_yx[i] * x1 + rotate_scale_yy[i] * y1 + trans_y[i];
            x2 = rotate_scale_xx[i] * x2 + rotate_scale_xy[i] * y2 + trans_x[i];
            y2 = rotate_scale_yx[i] * x2 + rotate_scale_yy[i] * y2 + trans_y[i];
            x3 = rotate_scale_xx[i] * x3 + rotate_scale_xy[i] * y3 + trans_x[i];
            y3 = rotate_scale_yx[i] * x3 + rotate_scale_yy[i] * y3 + trans_y[i];

            recursiveDraw(level - 1, x1, y1, x2, y2, x3, y3);
        }

    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable)
    {

    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i2, int i3, int i4)
    {

    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {

    }

    @Override
    public void keyReleased(KeyEvent e)
    {

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

        transitions = 0;

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
                    rotate_scale_xx[transitions] = Double.parseDouble(tokens[0]);
                    rotate_scale_xy[transitions] = Double.parseDouble(tokens[1]);
                    rotate_scale_yx[transitions] = Double.parseDouble(tokens[2]);
                    rotate_scale_yy[transitions] = Double.parseDouble(tokens[3]);
                    trans_x[transitions] = Double.parseDouble(tokens[4]);
                    trans_y[transitions] = Double.parseDouble(tokens[5]);
                    prob[transitions] = Double.parseDouble(tokens[6]);
                } catch (NumberFormatException ex) {
                    System.out.println("Not a double ");
                    System.out.println(ex);
                }
                transitions++;
            }
        }
        catch (IOException e) { System.out.println(e.toString());}
    }

}
