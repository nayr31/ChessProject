public class Director {

    // Returns a value depending on the direction provided, which depends on the integer value of the array
    // We use this, and a multiple of the @numSquaresToEdge method to preform moves
    // The reason that the numbers are weird for the directions is so we can use 0-4 for diagonal (Bishop)
    //  And 5-8 for horizontal/vertical as numbers for the for loop when generating moves
    // [0] [4] [1]    [7]  [8]  [9]
    // [7] [X] [5] -> [-1] [X]  [1]
    // [3] [6] [2]    [-9] [-8] [-7]
    static int directionCorrection(int dir) {
        switch (dir) {
            case 0:
                return 7;
            case 1:
                return 9;
            case 2:
                return -7;
            case 3:
                return -9;
            case 4:
                return 8;
            case 5:
                return 1;
            case 6:
                return -8;
            case 7:
                return -1;
            default:
                System.out.println("Something bad happened in DirectionCorrection: " + dir);
                return -99;
        }
    }

    static String directionCorrectionString(int dir) {
        switch (dir) {
            case 0:
                return "Up-Left";
            case 1:
                return "Up-Right";
            case 2:
                return "Down-Right";
            case 3:
                return "Down-Left";
            case 4:
                return "Up";
            case 5:
                return "Right";
            case 6:
                return "Down";
            case 7:
                return "Left";
            default:
                System.out.println("Something bad happened in DirectionCorrectionString: " + dir);
                return "";
        }
    }

    // Returns an array of target directions for the pawn
    // Sorted [left, right]
    static int[] pawnAttackDirectionCorrection(boolean isWhite) {
        if (isWhite) return new int[]{0, 1};
        return new int[]{3, 2};
    }

    static int pawnDirectionCorrection(boolean isWhite) {
        if (isWhite)
            return 8;
        return -8;
    }

    // Special conversion for the knight since he is a special boy
    //  [ ]  [1]  [ ]  [2]  [ ]
    //  [8]  [ ]  [ ]  [ ]  [3]
    //  [ ]  [ ]  [X]  [ ]  [ ]
    //  [7]  [ ]  [ ]  [ ]  [4]
    //  [ ]  [6]  [ ]  [5]  [ ]
    // Requires special checks, since you can always go -9 or whatever, but it wont be caught by the board
    static int directionConversionKnight(int dir) { //TODO Finish directional conversion/check for knight
        return 0;
    }
}
