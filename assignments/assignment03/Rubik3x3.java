package CS371.assignments.assignment03;

public class Rubik3x3
{
    public static final int SIZE = 3;
    public static final int TOTAL_CUBIES = (SIZE == 2 ? 8 : SIZE * SIZE * SIZE - 1);
    public static final int CUBIES_PER_LAYER = SIZE * SIZE;
    public static final int NUM_ACTIONS = 12;
    public static final char actions[] = {'R', 'r', 'L', 'l', 'U', 'u', 'D', 'd', 'F', 'f', 'B', 'b'};
    public static java.util.Random rand_gen = new java.util.Random();

    // these are the colors of the faces when the cube is solved
    public static String colors[] = { /* 0 */ "hidden",   
                   /* 1 */ "red ",     // RIGHT face  (x+)
			       /* 2 */ "orange",   // LEFT face   (x-)
			       /* 3 */ "blue",     // UP face     (y+)
			       /* 4 */ "green",    // DOWN face   (y-)
			       /* 5 */ "white",    // FRONT face  (z+)
			       /* 6 */ "yellow"}; // BACK face   (z-)


    public static int cubie_at_position[] = new int[TOTAL_CUBIES];

    static
    {
        for (int i = 0; i < TOTAL_CUBIES; i++)
        {
            cubie_at_position[i] = i;
        }
    }

    public static String getColor (int index) { return colors[index]; }

    /* given a cubie id, returns the list/array of colors of its facelets
       (in the goal configuration); in the array, the 6 facelets of each 
       cubie appear in the order x+ x- y+ y- z+ z-; therefore, each array 
       contains the values { 1, 2, 3, 4, 5, 6 } except for hidden facelets
       that are represented by zeros
     */
    public static int[] getCubieColor (int cubieID)
    {
        switch (cubieID)
        {

            // = = = = = FRONT layer = = = = =

            case 0: // LEFT-TOP-FRONT cubie
                return new int[]{0, 2, 3, 0, 5, 0};

            case 1: // MID-TOP-FRONT cubie
                return new int[]{0, 0, 3, 0, 5, 0};

            case 2: // RIGHT-TOP-FRONT cubie
                return new int[]{1, 0, 3, 0, 5, 0};

            case 3: // LEFT-MID-FRONT cubie
                return new int[]{0, 2, 0, 0, 5, 0};

            case 4: // MID-MID-FRONT cubie
                return new int[]{0, 0, 0, 0, 5, 0};

            case 5: // RIGHT-MID-FRONT cubie
                return new int[]{1, 0, 0, 0, 5, 0};

            case 6: // LEFT-DOWN-FRONT cubie
                return new int[]{0, 2, 0, 4, 5, 0};

            case 7: // MID-DOWN-FRONT cubie
                return new int[]{0, 0, 0, 4, 5, 0};

            case 8: // RIGHT-DOWN-FRONT cubie
                return new int[]{1, 0, 0, 4, 5, 0};

            // = = = = = MID layer = = = = =

            case 9: // LEFT-TOP-MID cubie
                return new int[]{0, 2, 3, 0, 0, 0};

            case 10: // MID-TOP-MID cubie
                return new int[]{0, 0, 3, 0, 0, 0};

            case 11: // RIGHT-TOP-MID cubie
                return new int[]{1, 0, 3, 0, 0, 0};

            case 12: // LEFT-MID-MID cubie
                return new int[]{0, 2, 0, 0, 0, 0};

            case 13: // RIGHT-MID-MID cubie
                return new int[]{1, 0, 0, 0, 0, 0};

            case 14: // LEFT-DOWN-MID cubie
                return new int[]{0, 2, 0, 4, 0, 0};

            case 15: // MID-DOWN-MID cubie
                return new int[]{0, 0, 0, 4, 0, 0};

            case 16: // RIGHT-DOWN-MID cubie
                return new int[]{1, 0, 0, 4, 0, 0};

            // = = = = = BACK layer = = = = =

            case 17: // LEFT-TOP-BACK cubie
                return new int[]{0, 2, 3, 0, 0, 6};

            case 18: // MID-TOP-BACK cubie
                return new int[]{0, 0, 3, 0, 0, 6};

            case 19: // RIGHT-TOP-BACK cubie
                return new int[]{1, 0, 3, 0, 0, 6};

            case 20: // LEFT-MID-BACK cubie
                return new int[]{0, 2, 0, 0, 0, 6};

            case 21: // MID-MID-BACK cubie
                return new int[]{0, 0, 0, 0, 0, 6};

            case 22: // RIGHT-MID-BACK cubie
                return new int[]{1, 0, 0, 0, 0, 6};

            case 23: // LEFT-DOWN-BACK cubie
                return new int[]{0, 2, 0, 4, 0, 6};

            case 24: // MID-DOWN-BACK cubie
                return new int[]{0, 0, 0, 4, 0, 6};

            case 25: // RIGHT-DOWN-BACK cubie
                return new int[]{1, 0, 0, 4, 0, 6};

            default:
                throw new RuntimeException("Invalid cubie ID: " + cubieID);

        }// switch on cubieID

    }// getCubieColor method


    /* return the position at the center of the face that the action rotates
     */
    static int get_center (char action)
    {
        switch (action)
        {
            case 'R':
            case 'r':
                return 13;
            case 'L':
            case 'l':
                return 12;
            case 'U':
            case 'u':
                return 10;
            case 'D':
            case 'd':
                return 15;
            case 'F':
            case 'f':
                return 4;
            case 'B':
            case 'b':
                return 21;
            default:
                throw new RuntimeException("Invalid action: " + action);

        }
    }

    /**
     * Does two things upon receiving an action code:
     * <p/>
     * 1. Update the cubie_at_position array so, given posiiton i,
     * we know the ID of the cubie at that position.
     * <p/>
     * 2. Return an array of size 9 that contains the positions into
     * which cubies rotated as a result of this action.  For
     * instance, if the action code were 'R', then the array
     * returned would be 2,5,8,11,13,16,19,22,25.
     */
    public static int[] performAction (char action)
    {
        int which_pos_rotate[] = new int[CUBIES_PER_LAYER];
        int i;
        int perm[] = performActionAux(action);
        int temp[] = new int[TOTAL_CUBIES];
        for (i = 0; i < TOTAL_CUBIES; ++i)
        {
            temp[perm[i]] = cubie_at_position[i];
        }

        for (i = 0; i < TOTAL_CUBIES; ++i)
        {
            cubie_at_position[i] = temp[i];
        }

        int j = 0;
        for (i = 0; i < TOTAL_CUBIES; i++)
        {
            if (i != perm[i])
            {
                which_pos_rotate[j] = perm[i];
                j++;
            }
            // the center cubie rotates but does not permute
            which_pos_rotate[j] = get_center(action);
        }
        return which_pos_rotate;
    }

    /*
      description of how the cubies move during each action, where an action
      is just a face rotation 
          uppercase letter = 90 deg. clockwise rotation of the face
          lowercase letter = 90 deg. counter-clockwise rotation of the face
      
      if 'a' denotes the returned array and (a[i] == j) is true, 
      then the cubie that was in position i before the action ends up in 
          position j after the action; so, if i==j then the cubie in position i 
          is not moved by the action
     */
    public static int[] performActionAux (char action)
    {
        switch (action)
        {

            case 'R':
                return new int[]{
                        0, 1, 19,
                        3, 4, 11,
                        6, 7, 2,

                        9, 10, 22,
                        12, 13,
                        14, 15, 5,

                        17, 18, 25,
                        20, 21, 16,
                        23, 24, 8
                };

            case 'r':
                return new int[]{
                        0, 1, 8,
                        3, 4, 16,
                        6, 7, 25,

                        9, 10, 5,
                        12, 13,
                        14, 15, 22,

                        17, 18, 2,
                        20, 21, 11,
                        23, 24, 19
                };

            case 'L':
                return new int[]{
                        6, 1, 2,
                        14, 4, 5,
                        23, 7, 8,

                        3, 10, 11,
                        12, 13,
                        20, 15, 16,

                        0, 18, 19,
                        9, 21, 22,
                        17, 24, 25
                };

            case 'l':
                return new int[]{
                        17, 1, 2,
                        9, 4, 5,
                        0, 7, 8,

                        20, 10, 11,
                        12, 13,
                        3, 15, 16,

                        23, 18, 19,
                        14, 21, 22,
                        6, 24, 25
                };

            case 'U':
                return new int[]{
                        17, 9, 0,
                        3, 4, 5,
                        6, 7, 8,

                        18, 10, 1,
                        12, 13,
                        14, 15, 16,

                        19, 11, 2,
                        20, 21, 22,
                        23, 24, 25
                };

            case 'u':
                return new int[]{
                        2, 11, 19,
                        3, 4, 5,
                        6, 7, 8,

                        1, 10, 18,
                        12, 13,
                        14, 15, 16,

                        0, 9, 17,
                        20, 21, 22,
                        23, 24, 25
                };

            case 'D':
                return new int[]{
                        0, 1, 2,
                        3, 4, 5,
                        8, 16, 25,

                        9, 10, 11,
                        12, 13,
                        7, 15, 24,

                        17, 18, 19,
                        20, 21, 22,
                        6, 14, 23
                };

            case 'd':
                return new int[]{
                        0, 1, 2,
                        3, 4, 5,
                        23, 14, 6,

                        9, 10, 11,
                        12, 13,
                        24, 15, 7,

                        17, 18, 19,
                        20, 21, 22,
                        25, 16, 8
                };

            case 'F':
                return new int[]{
                        2, 5, 8,
                        1, 4, 7,
                        0, 3, 6,

                        9, 10, 11,
                        12, 13,
                        14, 15, 16,

                        17, 18, 19,
                        20, 21, 22,
                        23, 24, 25
                };

            case 'f':
                return new int[]{
                        6, 3, 0,
                        7, 4, 1,
                        8, 5, 2,

                        9, 10, 11,
                        12, 13,
                        14, 15, 16,

                        17, 18, 19,
                        20, 21, 22,
                        23, 24, 25
                };

            case 'B':
                return new int[]{
                        0, 1, 2,
                        3, 4, 5,
                        6, 7, 8,

                        9, 10, 11,
                        12, 13,
                        14, 15, 16,

                        23, 20, 17,
                        24, 21, 18,
                        25, 22, 19
                };

            case 'b':
                return new int[]{
                        0, 1, 2,
                        3, 4, 5,
                        6, 7, 8,

                        9, 10, 11,
                        12, 13,
                        14, 15, 16,

                        19, 22, 25,
                        18, 21, 24,
                        17, 20, 23
                };


            default:
                throw new RuntimeException("Invalid action: " + action);

        }// switch on action


    }// performAction method


    // not used
    public static void scramble ()
    {
        // perform 100 random moves
        for (int i = 0; i < 100; i++)
        {
            performAction(actions[rand_gen.nextInt(NUM_ACTIONS)]);
        }
    }


    public static char getRandomAction ()
    {

        return actions[rand_gen.nextInt(NUM_ACTIONS)];
    }


}// Rubik2x2 class
