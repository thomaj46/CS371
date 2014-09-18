package CS371.assignments.assignment01;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.util.Random;

/**
 * Created by John Thomas on 2014-09-07.
 * CS371 Computer Graphics - Assignment 1
 *
 * There are 3 main routines.
 * 1 - Quarter Circles; 2 - Truchet Arcs; 3 - Rose Curve.
 * To run any of these three simply specify 1, 2, or 3 at startup.
 *
 * Option 1 will draw the quarter circles motif from the assignment.
 * I accomplished this by splitting up the for loop into a concept of
 * 0 - 360 degree circle.  Then I can simply specify from x to y in
 * degrees for the portion of the circle I want to draw.
 *
 * Option 2 will draw the arcs from the second motif in the assignment.
 * This will randomly draw one of 4 'tiles' by either rotating around
 * the z axis by 90 degrees or the y axis by 180 degrees.  The arcs
 * are built using the same concept from the first motif.
 *
 * Option 3 was my first approach to this assignment.  I was trying to
 * figure out different ways to draw and test my changes.  I thought
 * of using a Rose curve since that is easy to parameterize and produces
 * a wide array of shapes and designs.  The fun part to this piece is that
 * you can specify 2 additional parameters to draw different shapes.
 * Some of my favorites include: (12 8), (2 3), (5 3), (2 8) and (7 2).
 * Arguments should be specified as follows "...MyTiles 3 12 8".
 *
 */
public class MyTiles implements GLEventListener
{
    private static int programOption;
    private static int base = 0;
    private static double roseCurveRatio;
    private GLU glu;

    public static void main(String[] args)
    {
        GLJPanel canvas = new GLJPanel();
        canvas.addGLEventListener(new MyTiles());
        JFrame frame = new JFrame();
        MyTiles.programOption = Integer.parseInt(args[0]);
        if (1 == MyTiles.programOption)
        {
            frame.setTitle("Quarter Circles");
        }
        else if (2 == MyTiles.programOption)
        {
            frame.setTitle("Truchet Arcs");
        }
        else if (3 == MyTiles.programOption)
        {
            if (args.length > 2)
            {
                int n = Integer.parseInt(args[1]);
                int d = Integer.parseInt(args[2]);
                MyTiles.roseCurveRatio = 1.0 * n / d;
                frame.setTitle("Rose Curve " + n + " x " + d);
            }
            else
            {
                MyTiles.roseCurveRatio = 7.0 / 2.0;
                frame.setTitle("Rose Curve 7x2");
            }
        }

        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(canvas);
        frame.setVisible(true);
        canvas.requestFocusInWindow();

    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable)
    {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        this.glu = new GLU();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        this.glu.gluOrtho2D(-1.0, 1.0, -1.0, 1.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        MyTiles.base = gl.glGenLists(1);
        gl.glNewList(base, GL2.GL_COMPILE);

        if (MyTiles.programOption == 1)
        {
            MyTiles.makeQuarterCircles(gl);
        }
        else if (MyTiles.programOption == 2)
        {
            MyTiles.makeArcs(gl);
        }
        else if (MyTiles.programOption == 3)
        {
            MyTiles.makeRose(gl);
        }

        gl.glEndList();
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable)
    {
        if (MyTiles.programOption == 1)
        {
            MyTiles.displayQuarterCircles(glAutoDrawable);
        }
        else if (MyTiles.programOption == 2)
        {
            MyTiles.displayArcs(glAutoDrawable);
        }
        else if (MyTiles.programOption == 3)
        {
            MyTiles.displayRose(glAutoDrawable);
        }
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i2, int i3, int i4) {

    }

    /**
     * This method draws the quarter circle tile in small areas of 100x100 pixels.
     * This is accomplished by moving the viewport around the display area in a nested loop.
     */
    private static void displayQuarterCircles(GLAutoDrawable glAutoDrawable)
    {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        // clear all pixels
        gl.glClear (GL2.GL_COLOR_BUFFER_BIT);

        gl.glColor3f (0.10f, 1.0f, .10f);
        gl.glLineWidth(.01f);

        int x1, x2, y1, y2;
        for (int i = -1; i < 30; i += 1)
        {
            for (int j = -1; j < 60; j += 1)
            {
                x1 = 50 * j;
                x2 = 100;
                y1 = 50 * i;
                y2 = 100;
                gl.glViewport(x1, y1, x2, y2);
                gl.glCallList(MyTiles.base);
            }
        }

        gl.glFlush ();
    }

    /**
     * This method draws the arcs tile.  This is accomplished by rotating the tile around the y-axis
     * or the z-axis or both.  Then the viewport is moved around the display area to draw the image.
     *
     */
    private static void displayArcs(GLAutoDrawable glAutoDrawable)
    {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClear (GL2.GL_COLOR_BUFFER_BIT);
        gl.glColor3f (0.10f, 1.0f, .10f);
        gl.glLineWidth(.01f);

        Random random = new Random();
        int x1, x2, y1, y2;
        for (int i = -1; i < 30; i += 1)
        {
            for (int j = -1; j < 60; j += 1)
            {
                x1 = 25 * j;
                x2 = 25;
                y1 = 25 * i;
                y2 = 25;
                gl.glViewport(x1, y1, x2, y2);

                if (random.nextBoolean())
                {
                    gl.glRotatef(90, 0, 0, 1);
                }
                if (random.nextBoolean())
                {
                    gl.glRotatef(180, 0, 1, 0);
                }

                gl.glCallList(MyTiles.base);
            }
        }

        gl.glFlush ();
    }

    /**
     * This method draws the rose curves.  This is accomplished by moving the viewport around
     * to fill the display.  While moving the viewport around I am actually moving by just a little
     * bit smaller than the drawn curve to make sure they overlap just a little bit.
     *
     */
    private static void displayRose(GLAutoDrawable glAutoDrawable)
    {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        // clear all pixels
        gl.glClear (GL2.GL_COLOR_BUFFER_BIT);

        gl.glColor3f (0.10f, 1.0f, .10f);
        gl.glLineWidth(2.0f);

        int x1, x2, y1, y2;
        for (int i = -1; i < 13; i += 1)
        {
            for (int j = -1; j < 13; j += 1)
            {
                x1 = 40 * j;
                x2 = 50;
                y1 = 40 * i;
                y2 = 50;
                gl.glViewport(x1, y1, x2, y2);
                gl.glCallList(MyTiles.base);
            }
        }

        gl.glFlush ();
    }

    /**
     * This method calculates and draws the lines for the quarter circles.
     * I used a ratio here to help calculate what the angle for theta should be.
     * The ratio was calculated just right to allow me to specify in degrees the start
     * and end point for the curve.  For example I could specify in the loop that I want
     * to start at 90 degrees and draw up to 270 degrees thus giving me the left half of
     * a circle.
     *
     */
    private static void makeQuarterCircles(GL2 gl)
    {
        int i;
        double t;
        double ratio = Math.PI / 180;
        double xCoordinate;
        double yCoordinate;

        // Lower Left
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (i = 180; i <= 270; i += 1)
        {
            t = ratio * i;
            xCoordinate = .5 + Math.cos(t);
            yCoordinate = .5 + Math.sin(t);
            gl.glVertex2d(xCoordinate, yCoordinate);
        }
        gl.glEnd();

        // Upper Right
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (i = 0; i <= 90; i += 1)
        {
            t = ratio * i;
            xCoordinate = -.5 + Math.cos(t);
            yCoordinate = -.5 + Math.sin(t);
            gl.glVertex2d(xCoordinate, yCoordinate);
        }
        gl.glEnd();

        // Upper Left
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (i = 90; i <= 180; i += 1)
        {
            t = ratio * i;
            xCoordinate = .5 + Math.cos(t);
            yCoordinate = -.5 + Math.sin(t);
            gl.glVertex2d(xCoordinate, yCoordinate);
        }
        gl.glEnd();

        // Lower Right
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (i = 270; i <= 360; i += 1)
        {
            t = ratio * i;
            xCoordinate = -.5 + Math.cos(t);
            yCoordinate = .5 + Math.sin(t);
            gl.glVertex2d(xCoordinate, yCoordinate);
        }
        gl.glEnd();

        // Box
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2d(.5, .5);
        gl.glVertex2d(.5, -.5);
        gl.glVertex2d(-.5, -.5);
        gl.glVertex2d(-.5, .5);
        gl.glEnd();
    }

    /**
     * This method uses the same mathematics as the quarter circle method.  The
     * main difference was the radius of the circle to be drawn.
     *
     */
    private static void makeArcs(GL2 gl)
    {
        int i;
        double t;
        double ratio = Math.PI / 180;
        double xCoordinate;
        double yCoordinate;

        // Lower Left
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (i = 180; i <= 270; i += 1)
        {
            t = ratio * i;
            xCoordinate = 1 + Math.cos(t);
            yCoordinate = 1 + Math.sin(t);
            gl.glVertex2d(xCoordinate, yCoordinate);
        }
        gl.glEnd();

        // Upper Right
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (i = 0; i <= 90; i += 1)
        {
            t = ratio * i;
            xCoordinate = -1 + Math.cos(t);
            yCoordinate = -1 + Math.sin(t);
            gl.glVertex2d(xCoordinate, yCoordinate);
        }
        gl.glEnd();
    }

    /**
     * This method draws the rose curve.  It uses the ratio provided in the optional
     * 2nd and 3rd parameters.  There is nearly an unlimited number of 'tiles' that
     * could be made using this method.  This is why I chose this to start learning
     * about jogl and how to draw graphics.  I could quickly draw different shapes to
     * see how they would end up on the screen.
     *
     */
    private static void makeRose(GL2 gl)
    {
        int i;
        double petalRatio = 1.0 * MyTiles.roseCurveRatio;
        double xCoordinate;
        double yCoordinate;

        gl.glBegin(GL2.GL_POINTS);
        for (i = 0; i < 3600; i += 1)
        {
            xCoordinate = Math.cos(petalRatio * i) * Math.sin(i);
            yCoordinate = Math.cos(petalRatio * i) * Math.cos(i);

            gl.glVertex2d(xCoordinate, yCoordinate);
        }
        gl.glEnd();
    }
}