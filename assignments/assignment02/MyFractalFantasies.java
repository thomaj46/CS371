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
 * Created by John on 10/3/2014.
 */
public class MyFractalFantasies extends JFrame implements GLEventListener, KeyListener
{

    FPSAnimator animator;
    GL2 gl;
    GLU glu;
    GLUT glut;
    GLJPanel canvas;
    GLCapabilities caps;

    String baseDir, caption;
    int base, pointsToDraw, transitions, currentDrawList, decrementFrames, incrementFrames, maxFrames, framesDrawn;
    int minLevel, maxLevel, currentFractalIndex;
    float left, right, bottom, top, xOrigin, yOrigin;
    boolean framesReady, initialized, inAnimation, drawByLevel;
    ArrayList<Fractal> fractals;
    Fractal currentFractal;


    private class Transformation
    {
        public double xx;
        public double xy;
        public double yx;
        public double yy;
        public double tx;
        public double ty;
        public double prob;

        public double transformX(double x, double y)
        {
            return this.xx * x + this.xy * y + this.tx;
        }

        public double transformY(double x, double y)
        {
            return this.yx * x + this.yy * y + this.ty;
        }
    }

    private class Fractal
    {
        public String Name;
        public ArrayList<Transformation> Transformations;
        public int RecursiveLevels = 4;
        public Polygon OptimalPolygon;

        public Fractal(String name, int recursiveLevels, Polygon optimalPolygon)
        {
            this.Name = name;
            this.RecursiveLevels = recursiveLevels;
            this.OptimalPolygon = optimalPolygon;
            this.Transformations = new ArrayList<Transformation>();
        }
    }

    private class Point
    {
        public double x;
        public double y;

        public Point(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
    }

    private class Polygon
    {
        public ArrayList<Point> Verticies;

        public Polygon()
        {
            this.Verticies = new ArrayList<Point>();
        }
    }

    public MyFractalFantasies()
    {
        baseDir = "CS371/assignments/assignment02/";

        Polygon rectangle = this.getRectangle();
        Polygon rightTriangle = this.getRightTriangle();

        fractals = new ArrayList<Fractal>();
        fractals.add(new Fractal("carpet.ifs", 4, rectangle));
        fractals.add(new Fractal("chaos.ifs", 4, rectangle));
        fractals.add(new Fractal("coral.ifs", 4, rectangle));
        fractals.add(new Fractal("curl.ifs", 4, rectangle));
        fractals.add(new Fractal("four.ifs", 4, rectangle));
        fractals.add(new Fractal("galaxy.ifs", 4, rectangle));
        fractals.add(new Fractal("dragon.ifs", 7, rectangle));
        fractals.add(new Fractal("leady.ifs", 5, rectangle));
        fractals.add(new Fractal("koch.ifs", 4, rectangle));
        fractals.add(new Fractal("mouse.ifs", 4, rectangle));
        fractals.add(new Fractal("leaf.ifs", 4, rectangle));
        fractals.add(new Fractal("seven.ifs", 4, rectangle));
        fractals.add(new Fractal("three.ifs", 6, rectangle));
        fractals.add(new Fractal("tri.ifs", 6, rectangle));

        pointsToDraw = 80000;
        left = -7;
        right = 7;
        bottom = -7;
        top = 11;
        xOrigin = 0;
        yOrigin = 0;
        minLevel = 3;
        maxLevel = 10;

        caps = new GLCapabilities(GLProfile.getGL2GL3());
        caps.setDoubleBuffered(true); // request double buffer display mode
        caps.setHardwareAccelerated(true);
        canvas = new GLJPanel();
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        animator = new FPSAnimator(canvas, 60);
        getContentPane().add(canvas);

    }

    private Polygon getRectangle()
    {
        Polygon polygon = new Polygon();
        polygon.Verticies.add(new Point(2, .5));
        polygon.Verticies.add(new Point(-2, .5));
        polygon.Verticies.add(new Point(-2, -.5));
        polygon.Verticies.add(new Point(2, -.5));

        return polygon;
    }

    private Polygon getRightTriangle()
    {
        Polygon polygon = new Polygon();
        polygon.Verticies.add(new Point(0, 0));
        polygon.Verticies.add(new Point(0, 2));
        polygon.Verticies.add(new Point(.5, 0));

        return polygon;
    }

    public static void main(String[] args)
    {
        new MyFractalFantasies().run();
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

        maxFrames = 50 * fractals.size() + 1000;
        base = gl.glGenLists(maxFrames + 50);
        currentDrawList = base;

        for(Fractal fractal : fractals)
        {
            loadFractal(fractal);
        }

        currentFractalIndex = 0;
        currentFractal = fractals.get(currentFractalIndex);
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
        caption = currentFractal.Name + " Current frame = " + currentDrawList;
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, caption);

        gl.glFlush();

        if (inAnimation) {
            if (incrementFrames > 0)
            {
                currentDrawList += 1;
                incrementFrames -= 1;

                if (drawByLevel)
                {
                    if (currentDrawList > maxFrames)
                    {
                        currentDrawList = 1001;
                    }
                }
                else
                {
                    if (currentDrawList > fractals.size() * 50)
                    {
                        currentDrawList = base;
                    }
                }

            }
            else if (decrementFrames > 0)
            {
                currentDrawList -= 1;
                decrementFrames -= 1;

                if (drawByLevel)
                {
                    if (currentDrawList <= maxFrames - fractals.size() * 50)
                    {
                        currentDrawList = maxFrames;
                    }
                }
                else
                {
                    if (currentDrawList < base)
                    {
                        currentDrawList = fractals.size() * 50;
                    }
                }
            }

            currentFractal = fractals.get(currentFractalIndex);
            if (incrementFrames < 1 && decrementFrames < 1) {
                inAnimation = false;
            }
        }
    }

    private void myInit()
    {
        Fractal beginFractal, startFractal, endFractal;
        beginFractal = fractals.get(0);
        startFractal = endFractal = beginFractal;

        ArrayList<Point> beginPoints, startPoints, endPoints;
        beginPoints = paintFractal(startFractal);
        startPoints = endPoints = beginPoints;
        for(int i = 1; i < fractals.size(); i += 1)
        {
            endFractal = fractals.get(i);
            endPoints = paintFractal(endFractal);
            tweenPoints(currentDrawList, startPoints, endPoints);
            drawByLevel(currentDrawList + 1001, startFractal, endFractal);
            currentDrawList += 50;

            startFractal = endFractal;
            startPoints = endPoints;
        }

        tweenPoints(currentDrawList, endPoints, beginPoints);
        drawByLevel(currentDrawList + 1001, endFractal, beginFractal);
        currentDrawList = base;
        framesReady = true;
    }

    private void tweenPoints(int startList, ArrayList<Point> startPoints, ArrayList<Point> endPoints)
    {
        Point startPoint, endPoint;
        double x, y, tweenFactor;
        for (int i = 0; i <= 50; i += 1)
        {
            gl.glNewList(startList + i % 700, GL2.GL_COMPILE);
            tweenFactor = i / 50.0;

            gl.glBegin(GL2.GL_POINTS);
            for(int j = 0; j < pointsToDraw; j += 1)
            {
                startPoint = startPoints.get(j);
                endPoint = endPoints.get(j);
                x = (tweenFactor * endPoint.x) + (1.0 - tweenFactor) * startPoint.x;
                y = (tweenFactor * endPoint.y) + (1.0 - tweenFactor) * startPoint.y;
                gl.glVertex2d(x, y);
            }

            gl.glEnd();
            framesDrawn =+ 1;
            gl.glEndList();
        }
    }

    private void drawByLevel(int startList, Fractal start, Fractal end)
    {
        ArrayList<Transformation> tween;
        double tweenFactor;
        for(int i = 0; i <= 50; i += 1)
        {
            tweenFactor = i / 50.0;
            tween = this.tweenTransformations(start.Transformations, end.Transformations, tweenFactor);

            int list = startList + i > maxFrames ? 1001 : startList + i;
            gl.glNewList(list, GL2.GL_COMPILE);
            recursiveDraw(end.RecursiveLevels, end.RecursiveLevels - 1, tween, end.OptimalPolygon.Verticies);
            gl.glEndList();
        }
    }

    private void recursiveDraw(int level, int maxDrawLevel, ArrayList<Transformation> transformations, ArrayList<Point> points)
    {
        if (level < 1)
        {
            return;
        }

        if (level < maxDrawLevel)
        {
            gl.glBegin(GL2.GL_POLYGON);
            for(Point point : points)
            {
                gl.glVertex2d(point.x, point.y);
            }
            gl.glEnd();
        }

        for (Transformation transformation : transformations)
        {
            ArrayList<Point> newPoints = new ArrayList<Point>();
            for(Point point : points)
            {
                double newx = transformation.transformX(point.x, point.y);
                double newy = transformation.transformY(point.x, point.y);
                newPoints.add(new Point(newx, newy));
            }

            recursiveDraw(level - 1, maxDrawLevel, transformations, newPoints);
        }
    }

    private ArrayList<Transformation> tweenTransformations(ArrayList<Transformation> start, ArrayList<Transformation> end, double tweenFactor)
    {
        ArrayList<Transformation> transformations = new ArrayList<Transformation>();
        Transformation tween, startTransformation, endTransformation;
        int startSize = start.size();
        for (int i = 0; i < end.size(); i += 1)
        {
            tween = new Transformation();
            endTransformation = end.get(i);
            startTransformation = start.get(i % startSize);
            tween.xx = (tweenFactor * endTransformation.xx) + (1.0 - tweenFactor) * startTransformation.xx;
            tween.xy = (tweenFactor * endTransformation.xy) + (1.0 - tweenFactor) * startTransformation.xy;
            tween.yx = (tweenFactor * endTransformation.yx) + (1.0 - tweenFactor) * startTransformation.yx;
            tween.yy = (tweenFactor * endTransformation.yy) + (1.0 - tweenFactor) * startTransformation.yy;
            tween.tx = (tweenFactor * endTransformation.tx) + (1.0 - tweenFactor) * startTransformation.tx;
            tween.ty = (tweenFactor * endTransformation.ty) + (1.0 - tweenFactor) * startTransformation.ty;
            transformations.add(tween);
        }

        return transformations;
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
    public void keyPressed(KeyEvent key)
    {
        switch (key.getKeyCode())
        {
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
            case KeyEvent.VK_T:
                drawByLevel = !drawByLevel;
                currentDrawList = (currentDrawList + 1000) % 2000;
                break;
            case KeyEvent.VK_KP_RIGHT:
            case KeyEvent.VK_RIGHT:
                if (!inAnimation)
                {
                    incrementFrames = 50;
                    inAnimation = true;
                    currentFractalIndex = currentFractalIndex + 1 >= fractals.size() ? 0 : currentFractalIndex + 1;
                }
                break;
            case KeyEvent.VK_KP_LEFT:
            case KeyEvent.VK_LEFT:
                if (!inAnimation)
                {
                    decrementFrames = 50;
                    inAnimation = true;
                    currentFractalIndex = currentFractalIndex - 1 < 0 ? fractals.size() - 1 : currentFractalIndex - 1;
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

    private ArrayList<Point> paintFractal(Fractal fractal)
    {
        int iteration, t;
        double oldx, oldy, newx, newy, p;
        ArrayList<Point> points = new ArrayList<Point>();
        double cumulative_prob [] = new double [fractal.Transformations.size()];
        cumulative_prob[0] = fractal.Transformations.get(0).prob;
        for (int i = 1; i < fractal.Transformations.size(); i++)
        {
            cumulative_prob[i] = cumulative_prob[i-1] + fractal.Transformations.get(i).prob; // Make probability cumulative
        }


        iteration = 0;
        oldx = xOrigin;
        oldy = yOrigin;
        Transformation transformation;
        while (iteration <= pointsToDraw + 50)
        {
            p = Math.random();

            // Select transformation t
            t = 0;
            while ((p > cumulative_prob[t]) && (t < fractal.Transformations.size() - 1))
            {
                ++t;
            }

            // Transform point by transformation t
            transformation = fractal.Transformations.get(t);
            newx = transformation.xx * oldx + transformation.xy * oldy + transformation.tx;
            newy = transformation.yx * oldx + transformation.yy * oldy + transformation.ty;

            // Jump around for awhile without plotting to make
            //   sure the first point seen is attracted into the
            //   fractal
            if (iteration > 50)
            {
                points.add(new Point(newx, newy));
            }

            oldx = newx;
            oldy = newy;
            iteration++;
        }

        return points;
    }

    Fractal loadFractal(Fractal fractal)
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(baseDir + fractal.Name)));
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }

        transitions = 0;
        String line = null;

        try
        {
            line = in.readLine();
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }

        Transformation transformation;
        try
        {
            while ((line = in.readLine()) != null)
            {
                String[] tokens = line.trim().split("\\s+"); // Tokens separated by "whitespace"
                if (tokens[0].equals("}"))
                {
                    break;
                }

                transformation = new Transformation();

                try
                {
                    transformation.xx = Double.parseDouble(tokens[0]);
                    transformation.xy = Double.parseDouble(tokens[1]);
                    transformation.yx = Double.parseDouble(tokens[2]);
                    transformation.yy = Double.parseDouble(tokens[3]);
                    transformation.tx = Double.parseDouble(tokens[4]);
                    transformation.ty = Double.parseDouble(tokens[5]);
                    transformation.prob = Double.parseDouble(tokens[6]);
                }
                catch (NumberFormatException ex)
                {
                    System.out.println("Not a double ");
                    System.out.println(ex.toString());
                }

                transitions++;
                fractal.Transformations.add(transformation);
            }
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }

        return fractal;
    }
}
