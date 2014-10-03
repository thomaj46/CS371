package CS371.assignments.assignment02;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by John on 9/27/2014.
 */
public class MyFractalFantasies extends JFrame implements GLEventListener, KeyListener
{
    GLJPanel canvas;
    GL2 gl;
    GLU glu;
    GLUT glut;
    GLCapabilities caps;
    FPSAnimator animator;

    Random random;

    boolean inAnimation, framesReady, initialized;
    int base, pointsToDraw, transitions, currentDrawList, decrementFrames, incrementFrames, maxFrames, framesDrawn, currentTransitions, minStartLevel;
    int maximumTransitions = 25;
    float left, right, bottom, top, xOrigin, yOrigin;
    String baseDir, ifsfile, caption, dragonCaption, inverseDragonCaption;
    double[] rotate_scale_xx, rotate_scale_xy, rotate_scale_yx, rotate_scale_yy, trans_x, trans_y, prob;
    double[] current_xx, current_xy, current_yx, current_yy, current_tx, current_ty, current_prob;
    double[] next_xx, next_xy, next_yx, next_yy, next_tx, next_ty, next_prob;
    double[] tween_xx, tween_xy, tween_yx, tween_yy, tween_tx, tween_ty, tween_prob;
    ArrayList<Double> currentX, currentY, nextX, nextY, beginX, beginY;
    ArrayList<String> ifsFiles;

    public static void main(String[] args)
    {
        new MyFractalFantasies().run();
    }

    public MyFractalFantasies()
    {
        dragonCaption = "Dragon to inverse";
        inverseDragonCaption = "Inverse to dragon";
        caption = dragonCaption;
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
        minStartLevel = 3;

        rotate_scale_xx = new double[maximumTransitions];
        rotate_scale_xy = new double[maximumTransitions];
        rotate_scale_yx = new double[maximumTransitions];
        rotate_scale_yy = new double[maximumTransitions];
        trans_x = new double[maximumTransitions];
        trans_y = new double[maximumTransitions];
        prob = new double[maximumTransitions];

        current_xx = new double[maximumTransitions];
        current_xy = new double[maximumTransitions];
        current_yx = new double[maximumTransitions];
        current_yy = new double[maximumTransitions];
        current_tx = new double[maximumTransitions];
        current_ty = new double[maximumTransitions];
        current_prob = new double[maximumTransitions];

        next_xx = new double[maximumTransitions];
        next_xy = new double[maximumTransitions];
        next_yx = new double[maximumTransitions];
        next_yy = new double[maximumTransitions];
        next_tx = new double[maximumTransitions];
        next_ty = new double[maximumTransitions];
        next_prob = new double[maximumTransitions];

        tween_xx = new double[maximumTransitions];
        tween_xy = new double[maximumTransitions];
        tween_yx = new double[maximumTransitions];
        tween_yy = new double[maximumTransitions];
        tween_tx = new double[maximumTransitions];
        tween_ty = new double[maximumTransitions];
        tween_prob = new double[maximumTransitions];

        currentX = new ArrayList<Double>();
        currentY = new ArrayList<Double>();
        nextX = new ArrayList<Double>();
        nextY = new ArrayList<Double>();

        random = new Random(321);
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

    public void run()
    {
        setSize(800,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        animator.start();
    }

    /**
     * Start of GLEventListener methods
     */
    @Override
    public void init(GLAutoDrawable glAutoDrawable)
    {
        gl = glAutoDrawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();


//        gl.glClearColor(0.549f, 0.675f, 0.227f, 0.0f);
//        gl.glColor3f(.357f, .184f, .478f);




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
    }

    private void myInit()
    {
        ifsfile = baseDir + ifsFiles.get(0);
        loadifs();
        paintifs(currentX, currentY);
        beginX = currentX;
        beginY = currentY;
        current_xx = rotate_scale_xx;
        current_xy = rotate_scale_xy;
        current_yx = rotate_scale_yx;
        current_yy = rotate_scale_yy;
        current_tx = trans_x;
        current_ty = trans_y;
        current_prob = prob;
        rotate_scale_xx = new double[maximumTransitions];
        rotate_scale_xy = new double[maximumTransitions];
        rotate_scale_yx = new double[maximumTransitions];
        rotate_scale_yy = new double[maximumTransitions];
        trans_x = new double[maximumTransitions];
        trans_y = new double[maximumTransitions];
        prob = new double[maximumTransitions];

        for(int i = 1; i < ifsFiles.size(); i += 1)
        {
            ifsfile = baseDir + ifsFiles.get(i);
            loadifs();
            paintifs(nextX, nextY);
            next_xx = rotate_scale_xx;
            next_xy = rotate_scale_xy;
            next_yx = rotate_scale_yx;
            next_yy = rotate_scale_yy;
            next_tx = trans_x;
            next_ty = trans_y;
            next_prob = prob;



            drawPointsInList(currentDrawList, currentX, currentY, nextX, nextY);
            drawByLevel(currentDrawList + 1001);
            currentDrawList += 50;

            currentX = nextX;
            currentY = nextY;
            current_xx = next_xx;
            current_xy = next_xy;
            current_yx = next_yx;
            current_yy = next_yy;
            current_tx = next_tx;
            current_ty = next_ty;
            current_prob = next_prob;

            nextX = new ArrayList<Double>();
            nextY = new ArrayList<Double>();
            rotate_scale_xx = new double[maximumTransitions];
            rotate_scale_xy = new double[maximumTransitions];
            rotate_scale_yx = new double[maximumTransitions];
            rotate_scale_yy = new double[maximumTransitions];
            trans_x = new double[maximumTransitions];
            trans_y = new double[maximumTransitions];
            prob = new double[maximumTransitions];
        }

        drawPointsInList(currentDrawList, currentX, currentY, beginX, beginY);
        ifsfile = baseDir + ifsFiles.get(0);
        loadifs();
        drawByLevel(currentDrawList + 1001);
        currentDrawList = base;
        framesReady = true;
    }

    private void drawPointsInList(int startList, ArrayList<Double> startX, ArrayList<Double> startY, ArrayList<Double> endX, ArrayList<Double> endY)
    {
        double x, y, tweenFactor;
        for (int i = 0; i <= 50; i += 1)
        {
            gl.glNewList(startList + i, GL2.GL_COMPILE);
            tweenFactor = i / 50.0;

            gl.glBegin(GL2.GL_POINTS);
            for(int j = 0; j < pointsToDraw; j += 1)
            {
                x = (tweenFactor * endX.get(j)) + (1.0 - tweenFactor) * startX.get(j);
                y = (tweenFactor * endY.get(j)) + (1.0 - tweenFactor) * startY.get(j);
                gl.glVertex2d(x, y);
            }

            gl.glEnd();
            framesDrawn =+ 1;
            gl.glEndList();
        }
    }

    private void drawByLevel(int startList)
    {
        double tweenFactor;
        for (int i = 0; i <= 50; i += 1)
        {
            gl.glNewList(startList + i, GL2.GL_COMPILE);
            tweenFactor = i / 50.0;
            for (int j = 0; j < tween_xx.length; j += 1)
            {
                tween_xx[j] = (tweenFactor * next_xx[j]) + (1.0 - tweenFactor) * current_xx[j];
                tween_xy[j] = (tweenFactor * next_xy[j]) + (1.0 - tweenFactor) * current_xy[j];
                tween_yx[j] = (tweenFactor * next_yx[j]) + (1.0 - tweenFactor) * current_yx[j];
                tween_yy[j] = (tweenFactor * next_yy[j]) + (1.0 - tweenFactor) * current_yy[j];
                tween_tx[j] = (tweenFactor * next_tx[j]) + (1.0 - tweenFactor) * current_tx[j];
                tween_ty[j] = (tweenFactor * next_ty[j]) + (1.0 - tweenFactor) * current_ty[j];
            }


            recursiveDraw(4, .5, .5, -.5, .5, -.5, -.5, .5, -.5);
            gl.glEndList();
        }

    }

    private void recursiveDraw(int level, double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        if (level < 1)
        {
            return;
        }

        if (level < 3)
        {
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex2d(x1, y1);
            gl.glVertex2d(x2, y2);
            gl.glVertex2d(x3, y3);
            gl.glVertex2d(x4, y4);
            gl.glEnd();
        }

        for (int i = 0; i < transitions; i += 1)
        {
            double nx1 = tween_xx[i] * x1 + tween_xy[i] * y1 + tween_tx[i];
            double ny1 = tween_yx[i] * x1 + tween_yy[i] * y1 + tween_ty[i];
            double nx2 = tween_xx[i] * x2 + tween_xy[i] * y2 + tween_tx[i];
            double ny2 = tween_yx[i] * x2 + tween_yy[i] * y2 + tween_ty[i];
            double nx3 = tween_xx[i] * x3 + tween_xy[i] * y3 + tween_tx[i];
            double ny3 = tween_yx[i] * x3 + tween_yy[i] * y3 + tween_ty[i];
            double nx4 = tween_xx[i] * x4 + tween_xy[i] * y4 + tween_tx[i];
            double ny4 = tween_yx[i] * x4 + tween_yy[i] * y4 + tween_ty[i];

            recursiveDraw(level - 1, nx1, ny1, nx2, ny2, nx3, ny3, nx4, ny4);
        }

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable)
    {
        if (!framesReady)
        {
            if (!initialized)
            {
                initialized = true;
                GL2 gl = glAutoDrawable.getGL().getGL2();
                gl.glClear(GL.GL_COLOR_BUFFER_BIT);

                // Fonts draw selves at the current raster position
                gl.glRasterPos2f(right - (right + 2.0f), bottom + 1);
                caption = "Please wait... drawing frames";
                glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, caption);

                gl.glFlush();
            }
            else
            {
                myInit();
            }
        }
        else
        {
            myDisplay(glAutoDrawable);
        }
    }

    private void myDisplay(GLAutoDrawable glAutoDrawable)
    {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glCallList(currentDrawList);

        // Fonts draw selves at the current raster position
        gl.glRasterPos2f(right - (right + 1.5f), bottom + 1);
        caption = "Current frame = " + currentDrawList;
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, caption);

        gl.glFlush();

        if (inAnimation) {
            if (incrementFrames > 0) {
                currentDrawList += 1;
                incrementFrames -= 1;
                if (currentDrawList > maxFrames) {
                    currentDrawList = base;
                }
            } else if (decrementFrames > 0) {
                currentDrawList -= 1;
                decrementFrames -= 1;
                if (currentDrawList < base) {
                    currentDrawList = maxFrames;
                }
            }

            if (incrementFrames < 1 && decrementFrames < 1) {
                inAnimation = false;
            }
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

    /**
     * Start of KeyListener methods
     */
    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent key)
    {
        switch (key.getKeyCode())
        {
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
            case KeyEvent.VK_R:
//                if (!goingToDragon && !goingToInverse)
//                {
//                    goingToDragon = !atDragon;
//                    goingToInverse = !atInverse;
//                }
                break;
            case KeyEvent.VK_KP_RIGHT:
            case KeyEvent.VK_RIGHT:
                if (!inAnimation)
                {
                    incrementFrames = 50;
                    inAnimation = true;
                }
                break;
            case KeyEvent.VK_KP_LEFT:
            case KeyEvent.VK_LEFT:
                if (!inAnimation)
                {
                    decrementFrames = 50;
                    inAnimation = true;
                }
                break;
            default:
                break;
        }
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

    // The spray paint algorithm for the IFS representation of the
    // fractal is encapsulated here
    void paintifs(ArrayList<Double> xCoordinates, ArrayList<Double> yCoordinates) {

        int iter, t;
        double oldx, oldy, newx, newy, p;
        double cumulative_prob [] = new double [transitions];
        cumulative_prob[0] = prob[0];
        for (int i = 1; i < transitions; i++)
        {
            cumulative_prob[i] = cumulative_prob[i-1] + prob[i]; // Make probability cumulative
        }


        iter = 0;
        oldx = xOrigin;
        oldy = yOrigin;
        while (iter <= pointsToDraw + 50)
        {
            p = Math.random();

            // Select transformation t
            t = 0;
            while ((p > cumulative_prob[t]) && (t < transitions - 1))
            {
                ++t;
            }

            // Transform point by transformation t
            newx = rotate_scale_xx[t]*oldx + rotate_scale_xy[t]*oldy + trans_x[t];
            newy = rotate_scale_yx[t]*oldx + rotate_scale_yy[t]*oldy + trans_y[t];

            // Jump around for awhile without plotting to make
            //   sure the first point seen is attracted into the
            //   fractal
            if (iter > 50) {
                xCoordinates.add(newx);
                yCoordinates.add(newy);
            }
            oldx = newx;
            oldy = newy;
            iter++;
        }
    }
}
