public class Scorer {
    
    // Below are the score per spot Multiplieripliers from the website https://www.chessprogramming.org/Simplified_Evaluation_Function
    // Because these are copy pasted, they don't work with the linear interpretation of the system, and they need to be "reversed"
    static int[] pawnMultiplier = new int[]
    {
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    };

    static int[] knightMultiplier = new int[]
    {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    static int[] bishopMultiplier = new int[]
    {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20,    
    };

    static int[] rookMultiplier = new int[]
    {
        0,  0,  0,  0,  0,  0,  0,  0,
        5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        0,  0,  0,  5,  5,  0,  0,  0
    };

    static int[] queenMultiplier = new int[]
    {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
        -5,  0,  5,  5,  5,  5,  0, -5,
        0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    static int[] kingEarlyMultiplier = new int[]{
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        20, 20,  0,  0,  0,  0, 20, 20,
        20, 30, 10,  0,  0, 10, 30, 20
    };

    static int[] kingLateMultiplier = new int[]
    {
        -50,-40,-30,-20,-20,-30,-40,-50,
        -30,-20,-10,  0,  0,-10,-20,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 30, 40, 40, 30,-10,-30,
        -30,-10, 20, 30, 30, 20,-10,-30,
        -30,-30,  0,  0,  0,  0,-30,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };

    static int[] pawnMultiplierBlack;
    static int[] rookMultiplierBlack;
    static int[] bishopMultiplierBlack;
    static int[] queenMultiplierBlack;
    static int[] knightMultiplierBlack;
    static int[] kingEarlyMultiplierBlack;
    static int[] kingLateMultiplierBlack;

    // Required to run exactly once before using the score boards
    static void initiate(){
        // We need to convert the arrays from their current implementation into one that represents our board
        pawnMultiplier = convertArray(pawnMultiplier);
        knightMultiplier = convertArray(knightMultiplier);
        rookMultiplier = convertArray(rookMultiplier);
        queenMultiplier = convertArray(queenMultiplier);
        bishopMultiplier = convertArray(bishopMultiplier);
        kingLateMultiplier = convertArray(kingLateMultiplier);
        kingEarlyMultiplier = convertArray(kingEarlyMultiplier);
        // Then we need to make black colored versions
        pawnMultiplierBlack = switchColorOfArray(pawnMultiplier);
        knightMultiplierBlack = switchColorOfArray(knightMultiplier);
        rookMultiplierBlack = switchColorOfArray(rookMultiplier);
        queenMultiplierBlack = switchColorOfArray(queenMultiplier);
        bishopMultiplierBlack = switchColorOfArray(bishopMultiplier);
        kingLateMultiplierBlack = switchColorOfArray(kingLateMultiplier);
        kingEarlyMultiplierBlack = switchColorOfArray(kingEarlyMultiplier);
    }

    static String printArray(int[] arr) {
        StringBuilder out = new StringBuilder();
        String borderString = "----------------------------";
        String letterString = "+  a   b   c   d   e   f   g   h\n";

        out.append(borderString).append("\n");

        //63 - j - i*8, making it start at the left of the board and go right
        for (int i = 0; i < 8; i++) {//row on board
            //Add the number at which the row is stationed at
            out.append(8 - i); // Since it starts at 0 and works down, we need to inverse it
            //Each addition is "| x ", making the last one empty
            for (int j = 7; j >= 0; j--)//spot on row
                out.append("| ").append(arr[63 - j - i * 8]).append(" ");
            //So that when the row is over, we can close it and make a new line
            out.append("|\n");
        }
        out.append(letterString);
        out.append(borderString);
        return out.toString();
    }

    // Converts the copy-pasted array into our linear scale interpretation of it
    static int[] convertArray(int[] arr){
        int[] tempArr = new int[arr.length];
        for(int i=0; i<8; i++){
            for(int j=0;j<8;j++){
                int arrIndex = i*8 + j;
                int value = arr[arrIndex];
                int newIndex = 63 - j - i*8;
                tempArr[newIndex] = value;
            }
        }
        return tempArr;
    }
    
    // Switches the color interpretation of the score board array
    static int[] switchColorOfArray(int[] arr){
        int[] tempArray = new int[arr.length];
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                int newArrIndex = j + i*8;
                tempArray[newArrIndex] = (7-i)*8 + j;
            }
        }
        return tempArray;
    }

    // A check to determine if the game state is in lategame or not
    static boolean isLateGame(Spot[] spots){
        boolean whiteHasQueen = false;
        boolean blackHasQueen = false;
        int numWhite = 0;
        int numBlack = 0;

        // Count some values of the piece information
        for (Spot spot : spots) {
            Piece spotPiece = spot.spotPiece;
            if (spotPiece != null) {
                if (spotPiece.pieceType == Piece.Type.Queen) {
                    if (spotPiece.isWhite)
                        whiteHasQueen = true;
                    else
                        blackHasQueen = true;
                } else {
                    if (spotPiece.isWhite)
                        numWhite++;
                    else
                        numBlack++;
                }
            }
        }

        // If both sides have lost their queens, or one has and is losing piece number wise
        return (!whiteHasQueen && !blackHasQueen) || ((!whiteHasQueen && numWhite < numBlack) || (!blackHasQueen && numBlack < numWhite));
    }

    // Scores the board
    // The main evaluation function of the system
    static int scoreBoard(){
        Spot[] spots = Board.getSpots();

        int whiteCount = countColor(true);
        int blackCount = countColor(false);

        int evalVal = whiteCount - blackCount;
        int perspective = Board.isWhiteTurn ? 1 : -1;

        return evalVal * perspective;
    }

    static int countColor(boolean isWhite){
        Spot[] spots = Board.getSpots();
        int count = 0;
        for (Spot spot : spots) {
            Piece token = spot.spotPiece;
            if (token != null) {
                if (token.isWhite == isWhite)
                    count += token.getPieceValue();
            }
        }
        return count;
    }
}
