package CS371.assignments.assignment03;

import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Random;

/**
 * Created by John on 10/18/2014.
 */
public class MyRubikCube extends JFrame
{
    GLU glu;
    GLCapabilities capabilities;
    GLJPanel canvas;
    FPSAnimator animator;
    int nearPerspective = 1;
    int farPerspective = 55;
    double eyeX = -10;
    double eyeY = 6;
    double eyeZ = 10;
    float thetaX = 0;
    float thetaY = 0;
    float thetaZ = 0;
    boolean idle = true;
    int[] cubesToRotate;
    Cube[] cubes;


    public static void main (String[] args)
    {
        new MyRubikCube().run();
    }

    public MyRubikCube ()
    {
        capabilities = new GLCapabilities(GLProfile.getGL2GL3());
        capabilities.setDoubleBuffered(true);
        capabilities.setHardwareAccelerated(true);
        canvas = new GLJPanel(capabilities);
        canvas.addGLEventListener(new RubikEventListener());
        canvas.addKeyListener(new RubikKeyListener());
        canvas.addMouseWheelListener(new RubikMouseWheelListener());
        animator = new FPSAnimator(canvas, 15);
        this.getContentPane().add(canvas);
    }

    public void run ()
    {
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        animator.start();
    }

    private class RubikEventListener implements GLEventListener
    {
        float cubeSpacing = 2.1f;
        float vertices[][] =
                {
                        {-1, -1, 1}, // lower front left
                        {-1, 1, 1}, // upper front left
                        {1, 1, 1}, // upper front right
                        {1, -1, 1}, // lower front right
                        {-1, -1, -1}, // lower back left
                        {-1, 1, -1}, // upper back left
                        {1, 1, -1}, // upper back right
                        {1, -1, -1}, // lower back right
                };

        float colors[][] =
                {
                        {0, 0, 0},          // Hidden       => Black  = 0
                        {1, .5f, 0},        // Right Face   => Orange = 1
                        {1, 0, 0},          // Left Face    => Red    = 2
                        {1, 1, 1},          // Top Face     => White  = 3
                        {1, 1, 0},          // Bottom Face  => Yellow = 4
                        {0, 0, 1},          // Front Face   => Blue   = 5
                        {.13f, .54f, .13f}, // Back Face    => Green  = 6
                };

        @Override
        public void init (GLAutoDrawable glAutoDrawable)
        {
            GL2 gl = glAutoDrawable.getGL().getGL2();
            glu = new GLU();
            gl.glEnable(GL2.GL_DEPTH_TEST);
            gl.glClearColor(0, 0, 0, 0);
            glu.gluPerspective(45, 1, nearPerspective, farPerspective);
            cubes = new Cube[26];
            for (int i = 0; i < 26; i += 1)
            {
                cubes[i] = new Cube();
            }
        }

        @Override
        public void display (GLAutoDrawable glAutoDrawable)
        {
            GL2 gl = glAutoDrawable.getGL().getGL2();
            gl.glShadeModel(GL2.GL_FLAT);
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            glu.gluLookAt(eyeX, eyeY, eyeZ, 0, 0, 0, 0, 1, 0);

            gl.glRotatef(thetaX, 1, 0, 0);
            gl.glRotatef(thetaY, 0, 1, 0);
            gl.glRotatef(thetaZ, 0, 0, 1);
            this.drawCubes(gl);

            if (!idle)
            {
                int rotationsRemaining = 0;
                for(int cubePosition : cubesToRotate)
                {
                    rotationsRemaining += cubes[Rubik3x3.cubie_at_position[cubePosition]].rotate();
                }

                idle = rotationsRemaining == 0;
            }

            gl.glFlush();
        }

        private void drawCubes (GL2 gl)
        {
            // Front
            gl.glPushMatrix();
            gl.glTranslatef(0, 0, cubeSpacing);
            this.drawFrontLayer(gl);
            gl.glPopMatrix();

            // Mid
            gl.glPushMatrix();
            gl.glTranslatef(0, 0, 0);
            this.drawMidLayer(gl);
            gl.glPopMatrix();

            // Back
            gl.glPushMatrix();
            gl.glTranslatef(0, 0, -cubeSpacing);
            this.drawBackLayer(gl);
            gl.glPopMatrix();
        }

        private void drawFrontLayer (GL2 gl)
        {
            // Top Row
            gl.glPushMatrix();
            gl.glTranslatef(0, cubeSpacing, 0);
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[0]);
            this.drawMidCube(gl, Rubik3x3.cubie_at_position[1]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[2]);
            gl.glPopMatrix();

            // Mid Row
            gl.glPushMatrix();
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[3]);
            this.drawMidCube(gl, Rubik3x3.cubie_at_position[4]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[5]);
            gl.glPopMatrix();

            // Bottom Row
            gl.glPushMatrix();
            gl.glTranslatef(0, -cubeSpacing, 0);
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[6]);
            this.drawMidCube(gl, Rubik3x3.cubie_at_position[7]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[8]);
            gl.glPopMatrix();
        }

        private void drawMidLayer (GL2 gl)
        {
            // Top Row
            gl.glPushMatrix();
            gl.glTranslatef(0, cubeSpacing, 0);
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[9]);
            this.drawMidCube(gl, Rubik3x3.cubie_at_position[10]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[11]);
            gl.glPopMatrix();

            // Mid Row
            gl.glPushMatrix();
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[12]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[13]);
            gl.glPopMatrix();

            // Bottom Row
            gl.glPushMatrix();
            gl.glTranslatef(0, -cubeSpacing, 0);
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[14]);
            this.drawMidCube(gl, Rubik3x3.cubie_at_position[15]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[16]);
            gl.glPopMatrix();
        }

        private void drawBackLayer (GL2 gl)
        {
            // Top Row
            gl.glPushMatrix();
            gl.glTranslatef(0, cubeSpacing, 0);
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[17]);
            this.drawMidCube(gl, Rubik3x3.cubie_at_position[18]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[19]);
            gl.glPopMatrix();

            // Mid Row
            gl.glPushMatrix();
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[20]);
            this.drawMidCube(gl, Rubik3x3.cubie_at_position[21]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[22]);
            gl.glPopMatrix();

            // Bottom Row
            gl.glPushMatrix();
            gl.glTranslatef(0, -cubeSpacing, 0);
            this.drawLeftCube(gl, Rubik3x3.cubie_at_position[23]);
            this.drawMidCube(gl, Rubik3x3.cubie_at_position[24]);
            this.drawRightCube(gl, Rubik3x3.cubie_at_position[25]);
            gl.glPopMatrix();
        }

        private void drawLeftCube (GL2 gl, int cubeId)
        {
            gl.glPushMatrix();
            gl.glTranslatef(-cubeSpacing, 0, 0);
            this.drawCube(gl, cubeId, Rubik3x3.getCubieColor(cubeId));
            gl.glPopMatrix();
        }

        private void drawMidCube (GL2 gl, int cubeId)
        {
            gl.glPushMatrix();
            this.drawCube(gl, cubeId, Rubik3x3.getCubieColor(cubeId));
            gl.glPopMatrix();
        }

        private void drawRightCube (GL2 gl, int cubeId)
        {
            gl.glPushMatrix();
            gl.glTranslatef(cubeSpacing, 0, 0);
            this.drawCube(gl, cubeId, Rubik3x3.getCubieColor(cubeId));
            gl.glPopMatrix();
        }

        private void drawCube (GL2 gl, int cubeId, int[] colors)
        {
            gl.glMultMatrixf(cubes[cubeId].matrix, 0);
            this.drawPolygon(gl, colors[0], 2, 3, 7, 6); // Right Face  0
            this.drawPolygon(gl, colors[1], 0, 1, 5, 4); // Left Face   1
            this.drawPolygon(gl, colors[2], 1, 2, 6, 5); // Top Face    2
            this.drawPolygon(gl, colors[3], 0, 4, 7, 3); // Bottom Face 3
            this.drawPolygon(gl, colors[4], 0, 3, 2, 1); // Front Face  4
            this.drawPolygon(gl, colors[5], 4, 5, 6, 7); // Back Face   5
        }

        private void drawPolygon (GL2 gl, int color, int vertex1, int vertex2, int vertex3, int vertex4)
        {
            gl.glColor3fv(colors[color], 0);
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex3fv(vertices[vertex1], 0);
            gl.glVertex3fv(vertices[vertex2], 0);
            gl.glVertex3fv(vertices[vertex3], 0);
            gl.glVertex3fv(vertices[vertex4], 0);
            gl.glEnd();
        }

        @Override
        public void reshape (GLAutoDrawable glAutoDrawable, int x, int y, int width, int height)
        {
            GL2 gl = glAutoDrawable.getGL().getGL2();
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            glu.gluPerspective(45, (double) width / (double) height, nearPerspective, farPerspective);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
        }

        @Override
        public void dispose (GLAutoDrawable glAutoDrawable) { }
    }

    private class RubikKeyListener implements KeyListener
    {
        double rotation = Math.toRadians(10);

        @Override
        public void keyPressed (KeyEvent key)
        {
            double x = eyeX;
            double y = eyeY;
            double z = eyeZ;

            if (idle)
            {
                if (key.getKeyChar() == 's')
                {
                    Random random = new Random();
                    for (int i = 0; i < random.nextInt(20) + 10; i +=1)
                    {
                        this.performActionImmediate(Rubik3x3.getRandomAction());
                    }
                }
                else if (key.getKeyChar() == 'a')
                {
                    this.performActionAnimated(Rubik3x3.getRandomAction());
                }
                else
                {
                    this.performActionAnimated(key.getKeyChar());
                }
            }

            switch (key.getKeyCode())
            {
                case KeyEvent.VK_ESCAPE:
                    new Thread()
                    {
                        public void run () { animator.stop(); }
                    }.start();
                    System.exit(0);
                    break;
                case KeyEvent.VK_LEFT:
                    eyeX = (x * Math.cos(-rotation)) - (z * Math.sin(-rotation));
                    eyeZ = (x * Math.sin(-rotation)) + (z * Math.cos(-rotation));
                    break;
                case KeyEvent.VK_RIGHT:
                    eyeX = (x * Math.cos(rotation)) - (z * Math.sin(rotation));
                    eyeZ = (x * Math.sin(rotation)) + (z * Math.cos(rotation));
                    break;
                case KeyEvent.VK_UP:
                    eyeY = (y * Math.cos(-rotation)) - (z * Math.sin(-rotation));
                    eyeZ = (y * Math.sin(-rotation)) + (z * Math.cos(-rotation));
                    break;
                case KeyEvent.VK_DOWN:
                    eyeY = (y * Math.cos(rotation)) - (z * Math.sin(rotation));
                    eyeZ = (y * Math.sin(rotation)) + (z * Math.cos(rotation));
                    break;
                case KeyEvent.VK_X:
                    thetaX = (thetaX + 5) % 360;
                    break;
                case KeyEvent.VK_Y:
                    thetaY = (thetaY + 5) % 360;
                    break;
                case KeyEvent.VK_Z:
                    thetaZ = (thetaZ + 5) % 360;
                    break;
                case KeyEvent.VK_0:
                case KeyEvent.VK_NUMPAD0:
                    thetaX = 0;
                    thetaY = 0;
                    thetaZ = 0;
                    break;
            }
        }

        private void performActionAnimated(char key)
        {
            Rotation rotation = this.performAction(key);
            for (int cubePosition : cubesToRotate)
            {
                cubes[Rubik3x3.cubie_at_position[cubePosition]].startRotation(rotation);
                idle = false;
            }
        }

        private void performActionImmediate(char key)
        {
            Rotation rotation = performAction(key);
            for (int cubePosition : cubesToRotate)
            {
                cubes[Rubik3x3.cubie_at_position[cubePosition]].rotateCube(rotation, 90);
            }
        }

        private Rotation performAction(char key)
        {
            Rotation rotation = Rotation.None;
            switch (key)
            {
                case 'F':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.ClockwiseZ;
                    break;
                case 'f':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.CounterClockwiseZ;
                    break;
                case 'B':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.CounterClockwiseZ;
                    break;
                case 'b':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.ClockwiseZ;
                    break;
                case 'L':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.CounterClockwiseX;
                    break;
                case 'l':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.ClockwiseX;
                    break;
                case 'R':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.ClockwiseX;
                    break;
                case 'r':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.CounterClockwiseX;
                    break;
                case 'U':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.ClockwiseY;
                    break;
                case 'u':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.CounterClockwiseY;
                    break;
                case 'D':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.CounterClockwiseY;
                    break;
                case 'd':
                    cubesToRotate = Rubik3x3.performAction(key);
                    rotation = Rotation.ClockwiseY;
                    break;
            }

            return rotation;
        }

        @Override
        public void keyTyped (KeyEvent e) { }

        @Override
        public void keyReleased (KeyEvent e) { }
    }

    private class RubikMouseWheelListener implements MouseWheelListener
    {

        @Override
        public void mouseWheelMoved (MouseWheelEvent wheel)
        {
            double newZ = eyeZ + wheel.getPreciseWheelRotation();
            if (newZ > 50)
            {
                eyeZ = 50;
            }
            else if (newZ < 0)
            {
                eyeZ = 0;
            }
            else
            {
                eyeZ = newZ;
            }
        }
    }

    private enum Rotation
    {
        None,
        ClockwiseX,
        ClockwiseY,
        ClockwiseZ,
        CounterClockwiseX,
        CounterClockwiseY,
        CounterClockwiseZ,
    }

    private class Cube
    {
        public float[] matrix;
        private int rotationIncrement = 9;
        private int amountToRotate;
        private Rotation rotation;

        private final float[] identity = new float[]
                {
                        1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1,
                };

        public Cube()
        {
            this.matrix = this.identity.clone();
        }

        public void startRotation(Rotation rotation)
        {
            this.amountToRotate = 90;
            this.rotation = rotation;
        }

        public int rotate()
        {
            if (this.amountToRotate < 1)
            {
                return 0;
            }

            this.rotateCube(rotation, rotationIncrement);
            this.amountToRotate -= rotationIncrement;
            return this.amountToRotate;
        }

        public void rotateCube(Rotation rotation, int theta)
        {
            switch (rotation)
            {
                case ClockwiseX:
                    this.rotateClockwiseX(theta);
                    break;
                case CounterClockwiseX:
                    this.rotateCounterClockwiseX(theta);
                    break;
                case ClockwiseY:
                    this.rotateClockwiseY(theta);
                    break;
                case CounterClockwiseY:
                    this.rotateCounterClockwiseY(theta);
                    break;
                case ClockwiseZ:
                    this.rotateClockwiseZ(theta);
                    break;
                case CounterClockwiseZ:
                    this.rotateCounterClockwiseZ(theta);
                    break;
            }
        }

        private float[] multiplyMatrix (float[] matrixB)
        {
            int a = 0;
            int b = 0;
            float[] result = new float[matrixB.length];
            for (int i = 0; i < matrixB.length; i++){
                a = (i/4)*4;
                b = (i%4);
                result[i] = (this.matrix[a] * matrixB[b]) + (this.matrix[a+1] * matrixB[b+4]) + (this.matrix[a+2] * matrixB[b+8]) + (this.matrix[a+3] * matrixB[b+12]);
            }
            return result;
        }

        public void rotateClockwiseX (int theta)
        {
            double thetaRadians = Math.toRadians(theta);
            float cosTheta = (float)Math.cos(thetaRadians);
            float sinTheta = (float)Math.sin(thetaRadians);
            float[] rotationMatrix = new float[]
                    {
                            1,        0,         0, 0,
                            0, cosTheta, -sinTheta, 0,
                            0, sinTheta,  cosTheta, 0,
                            0,        0,         0, 1
                    };

            this.matrix = this.multiplyMatrix(rotationMatrix);
        }

        public void rotateCounterClockwiseX (int theta)
        {
            double thetaRadians = Math.toRadians(-theta);
            float cosTheta = (float)Math.cos(thetaRadians);
            float sinTheta = (float)Math.sin(thetaRadians);
            float[] rotationMatrix = new float[]
                    {
                            1,           0,         0, 0,
                            0,    cosTheta, -sinTheta, 0,
                            0,    sinTheta,  cosTheta, 0,
                            0,           0,         0, 1
                    };

            this.matrix = this.multiplyMatrix(rotationMatrix);
        }

        public void rotateClockwiseY (int theta)
        {
            double thetaRadians = Math.toRadians(theta);
            float cosTheta = (float)Math.cos(thetaRadians);
            float sinTheta = (float)Math.sin(thetaRadians);
            float[] rotationMatrix = new float[]
                    {
                             cosTheta, 0, sinTheta, 0,
                                    0, 1,        0, 0,
                            -sinTheta, 0, cosTheta, 0,
                                    0, 0,        0, 1
                    };

            this.matrix = this.multiplyMatrix(rotationMatrix);
        }

        public void rotateCounterClockwiseY (int theta)
        {
            double thetaRadians = Math.toRadians(-theta);
            float cosTheta = (float)Math.cos(thetaRadians);
            float sinTheta = (float)Math.sin(thetaRadians);
            float[] rotationMatrix = new float[]
                    {
                             cosTheta, 0, sinTheta, 0,
                                    0, 1,        0, 0,
                            -sinTheta, 0, cosTheta, 0,
                                    0, 0,        0, 1
                    };

            this.matrix = this.multiplyMatrix(rotationMatrix);

        }

        public void rotateClockwiseZ (int theta)
        {
            double thetaRadians = Math.toRadians(theta);
            float cosTheta = (float)Math.cos(thetaRadians);
            float sinTheta = (float)Math.sin(thetaRadians);
            float[] rotationMatrix = new float[]
                    {
                            cosTheta, -sinTheta, 0, 0,
                            sinTheta,  cosTheta, 0, 0,
                                   0,         0, 1, 0,
                                   0,         0, 0, 1
                    };

            this.matrix = this.multiplyMatrix(rotationMatrix);
        }

        public void rotateCounterClockwiseZ (int theta)
        {
            double thetaRadians = Math.toRadians(-theta);
            float cosTheta = (float)Math.cos(thetaRadians);
            float sinTheta = (float)Math.sin(thetaRadians);
            float[] rotationMatrix = new float[]
                    {
                            cosTheta, -sinTheta, 0, 0,
                            sinTheta,  cosTheta, 0, 0,
                                   0,         0, 1, 0,
                                   0,         0, 0, 1
                    };

            this.matrix = this.multiplyMatrix(rotationMatrix);

        }
    }
}
