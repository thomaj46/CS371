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

    boolean goingToDragon, goingToInverse, atDragon, atInverse, inAnimation;
    int base, pointsToDraw, transitions, currentDrawList, decrementFrames, incrementFrames, maxFrames;
    int maximumTransitions = 25;
    double tween_factor;
    float left, right, bottom, top, xOrigin, yOrigin;
    //String carpetIfs, chaosIfs, coralIfs, curlIfs, davisIfs, fourIfs, galaxyIfs, dragonIfs, leadyIfs, kochIfs, mouseIfs, leafIfs, plantIfs, sevenIfs, threeIfs, triIfs;
    String baseDir, ifsfile, caption, dragonCaption, inverseDragonCaption;
    double[] rotate_scale_xx, rotate_scale_xy, rotate_scale_yx, rotate_scale_yy, trans_x, trans_y, prob;
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
//        carpetIfs = "carpet.ifs";
//        chaosIfs = "chaos.ifs";
//        coralIfs = "coral.ifs";
//        curlIfs = "curl.ifs";
//        davisIfs = "davis.ifs";
//        fourIfs = "four.ifs";
//        galaxyIfs = "galaxy.ifs";
//        dragonIfs = "dragon.ifs";
//        leadyIfs = "leady.ifs";
//        kochIfs = "koch.ifs";
//        mouseIfs = "mouse.ifs";
//        leafIfs = "leaf.ifs";
//        plantIfs = "plant.ifs";
//        sevenIfs = "seven.ifs";
//        threeIfs = "three.ifs";
//        triIfs = "tri.ifs";

        ifsFiles = new ArrayList<String>();
        ifsFiles.add("carpet.ifs");
        ifsFiles.add("chaos.ifs");
        ifsFiles.add("coral.ifs");
        ifsFiles.add("curl.ifs");
        //ifsFiles.add("davis.ifs");
        ifsFiles.add("four.ifs");
        ifsFiles.add("galaxy.ifs");
        ifsFiles.add("dragon.ifs");
        ifsFiles.add("leady.ifs");
        ifsFiles.add("koch.ifs");
        ifsFiles.add("mouse.ifs");
        ifsFiles.add("leaf.ifs");
        //ifsFiles.add("plant.ifs");
        //ifsFiles.add("seven.ifs");
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

        currentX = new ArrayList<Double>();
        currentY = new ArrayList<Double>();
        nextX = new ArrayList<Double>();
        nextY = new ArrayList<Double>();

        random = new Random(321);
        caps = new GLCapabilities(GLProfile.getGL2GL3());
        caps.setDoubleBuffered(true); // request double buffer display mode
        caps.setHardwareAccelerated(true);
        canvas = new GLJPanel();
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

        gl.glClearColor(0.8f, 0.835f, 0.38f, 0.0f);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(left, right, bottom, top);
        gl.glViewport((int)bottom, (int)left, (int)top, (int)right);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glColor3f(.545f, .357f, .576f);

        maxFrames = 50 * ifsFiles.size();
        base = gl.glGenLists(maxFrames);
        currentDrawList = base;

        ifsfile = baseDir + ifsFiles.get(0);
        loadifs();
        paintifs(currentX, currentY);
        beginX = currentX;
        beginY = currentY;

        for(int i = 1; i < ifsFiles.size(); i += 1)
        {
            ifsfile = baseDir + ifsFiles.get(i);
            loadifs();
            paintifs(nextX, nextY);

            drawPointsInList(currentDrawList, currentX, currentY, nextX, nextY);
            currentDrawList += 50;

            currentX = nextX;
            currentY = nextY;
            nextX = new ArrayList<Double>();
            nextY = new ArrayList<Double>();
        }

        drawPointsInList(currentDrawList, currentX, currentY, beginX, beginY);

        currentDrawList = base;
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

            gl.glEndList();
        }
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable)
    {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
        gl.glCallList(currentDrawList);

        // Fonts draw selves at the current raster position
        gl.glRasterPos2f(right - (right + 1.5f), bottom + 1);
        caption = "Current drawlist = " + currentDrawList;
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, caption);

        gl.glFlush();

        if (inAnimation)
        {
            if (incrementFrames > 0)
            {
                currentDrawList += 1;
                incrementFrames -= 1;
                if (currentDrawList > maxFrames)
                {
                    currentDrawList = base;
                }
            }
            else if (decrementFrames > 0)
            {
                currentDrawList -= 1;
                decrementFrames -= 1;
                if (currentDrawList < base)
                {
                    currentDrawList = maxFrames;
                }
            }

            if (incrementFrames < 1 && decrementFrames < 1)
            {
                inAnimation = false;
            }
        }






//        if(goingToDragon)
//        {
//            caption = dragonCaption;
//            currentDrawList += 1;
//            if (currentDrawList >= 50)
//            {
//                goingToDragon = false;
//                atDragon = true;
//                atInverse = false;
//            }
//        }
//
//        if (goingToInverse)
//        {
//            caption = inverseDragonCaption;
//            currentDrawList -= 1;
//            if (currentDrawList <= base)
//            {
//                goingToInverse = false;
//                atInverse = true;
//                atDragon = false;
//            }
//        }
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
                //setColor(gl);
                //gl.glBegin(GL.GL_POINTS);
                xCoordinates.add(newx);
                yCoordinates.add(newy);
                //gl.glVertex2d(newx,newy);
                //gl.glEnd();
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

}
