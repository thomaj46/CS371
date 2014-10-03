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
public class DrawByLevelProgress extends JFrame implements GLEventListener, KeyListener
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
    int minLevel, maxLevel;
    float left, right, bottom, top, xOrigin, yOrigin;
    double[] rotate_scale_xx, rotate_scale_xy, rotate_scale_yx, rotate_scale_yy, trans_x, trans_y, prob;
    boolean drawn;
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
    }

    private class Fractal
    {
    	public String Name;
    	public ArrayList<Transformation> Transformations;
    	
    	public Fractal()
    	{
    		Name = "";
    		Transformations = new ArrayList<Transformation>();
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

    public DrawByLevelProgress()
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
        minLevel = 3;
        maxLevel = 10;

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
        
        fractals = new ArrayList<Fractal>();
    }

    public static void main(String[] args)
    {
        new DrawByLevelProgress().run();
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

        ifsfile = baseDir + "seven.ifs";
        fractals.add(loadifs());
        currentFractal = fractals.get(0);

        gl.glNewList(base, GL2.GL_COMPILE);
        drawByLevel();
        gl.glEndList();
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

        gl.glCallList(base);

        gl.glFlush();
    }

    private void drawByLevel()
    {
        Polygon polygon = new Polygon();
        polygon.Verticies.add(new Point(2, .5));
        polygon.Verticies.add(new Point(-2, .5));
        polygon.Verticies.add(new Point(-2, -.5));
        polygon.Verticies.add(new Point(2, -.5));

        recursiveDraw(0, polygon.Verticies);
    }

    private void recursiveDraw(int level, ArrayList<Point> verticies)
    {
        if (level >= maxLevel)
        {
            return;
        }

        if (level > minLevel)
        {
            gl.glBegin(GL2.GL_POLYGON);
            for (Point vertex : verticies)
            {
                gl.glVertex2d(vertex.x, vertex.y);
            }
            gl.glEnd();
        }

        double newX, newY;
        for (Transformation transformation : currentFractal.Transformations)
        {
            ArrayList<Point> newPoints = new ArrayList<Point>();
            for (Point vertex : verticies)
            {
                newX = transformation.xx * vertex.x + transformation.xy * vertex.y + transformation.tx;
                newY = transformation.yx * vertex.x + transformation.yy * vertex.y + transformation.ty;
                newPoints.add(new Point(newX, newY));
            }

        	recursiveDraw(level + 1, newPoints);
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
    Fractal loadifs()
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(ifsfile)));
        }
        catch (Exception e)
        {
        	System.out.println(e.toString());
    	};

        transitions = 0;
        Fractal fractal = new Fractal();
        fractal.Name = ifsfile;

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
                    System.out.println(ex);
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













