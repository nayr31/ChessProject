import com.sun.source.tree.WhileLoopTree;

import java.util.ArrayList;

public class Board {
    Spot[] spots;
    static int[] FENKey = {56, 48, 40, 32, 24, 26, 8, 0};
    boolean isWhiteTurn;
    boolean CanCastleWhiteKing, CanCastleWhiteQueen;
    boolean CanCastleBlackKing, CanCastleBlackQueen;
    int halfMoves, fullMoves;
    boolean[] passants;
    //List<Piece> whitePieces;
    //List<Piece> blackPieces;

    Board() {
        spots = new Spot[64];
        passants = new boolean[64];
        initiate();
        //popNormal();
        popTest();
    }

    //Populates the board with empty objects just in case
    void initiate(){
        for(int i=0; i<spots.length; i++) {
            spots[i] = new Spot();
            passants[i] = false;
        }
    }

    //The normal board configuration
    void popNormal(){
        popFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    void popTest(){
        popFromFEN("4k2r/6r1/8/8/8/8/3R4/R3K3 w Qk - 0 1");
    }

    //Populates the board from the FEN string
    void popFromFEN(String inputString){
        //Split the input into its separate functions:
        // [0] = Board spaces
        // [1] = Turn to move (b - black, w - white)
        // [2] = Castling rights (up to 4 characters, K/Q are white, king side, queen side, - is empty)
        // [3] = Space for en-passant
        // [4] = Half move, how many moves each player has made together
        // [5] = Full move, how many times black has moved
        String[] varInput = inputString.split(" ");

        // --------- Step 0 ---------
        //Separate the string into each row
        String[] inputArray = varInput[0].split("/");
        for(int i=0; i<inputArray.length; i++){
            String rowString = inputArray[i];
            //Now have a row of FEN input (first iteration is top, 56-63)
            //Since the input string starts at 56 (standard FEN), we inverse the string so it works and I don't have to change anything
            rowString = (new StringBuilder(rowString)).reverse().toString();

            // Get every character input from the FEN row string (/xxx/)
            int j = 0;
            for(char pieceChar : rowString.toCharArray()){
                // Determine the spot location in the array
                // - Start at 63
                // - Backtrack down the row from each character read (piece placed)
                // - Every row skips 8 immediately
                int spotLoc = 63-j-8*i;

                // If the character is not a digit, we set that spot to have a piece
                if(!Character.isDigit(pieceChar)){
                    //So set the spot on the board to that piece
                    spots[spotLoc].spotPiece = new Piece(
                            pieceChar,
                            Character.isUpperCase(pieceChar)
                    );
                    j++;
                } else{ // If it is a digit, we skip that many spaces horizontally
                    j += Integer.parseInt(String.valueOf(pieceChar));
                }
            }
        }

        // --------- Step 1 ---------
        // Whose turn it is
        isWhiteTurn = varInput[1].equals("w");

        // --------- Step 2 ---------
        // Castling rights for each side
        CanCastleWhiteKing = false;
        CanCastleWhiteQueen = false;
        CanCastleBlackKing = false;
        CanCastleBlackQueen = false;
        for(char c : varInput[2].toCharArray()){
            switch (c) {
                case 'K' -> CanCastleWhiteKing = true;
                case 'Q' -> CanCastleWhiteQueen = true;
                case 'k' -> CanCastleBlackKing = true;
                case 'q' -> CanCastleBlackQueen = true;
            }
        }

        // --------- Step 3 ---------
        // Which pawns can be en-passant-ed
        // Use ascii and j,i multiplication
        // First char is a letter (a-h), 97-(ascii value) for j (left-right)
        // Second char is a number (1-8), 63-8*(number value-1) for i (up-down)
        if(varInput[3].length() != 1){ // If the space is not empty, diagnose it
            // If there is a viable passant move, then check every two spaces for them
            for(int i=0; i<varInput[3].length(); i+=2){
                //
            }
        }

        // --------- Step 4 ---------
        // Half moves
        halfMoves = Integer.parseInt(varInput[4]);

        // --------- Step 5 ---------
        // Full moves
        fullMoves = Integer.parseInt(varInput[5]);
    }

    // Generate a list of all moves that each piece can preform, following certain conditions:
    // 1 - Is the correct color of whomever turn it is
    // 2 - Different pieces need different methods to determine which
    ArrayList<Move> GetAllMoves(){
        ArrayList<Move> moves = new ArrayList<>();

        // For each spot on the board
        for(int startSpot=0; startSpot<64; startSpot++){
            // Check to see if there is a piece there
            if(spots[startSpot].spotPiece != null){
                // Check to see if the piece corresponds to the color that can move
                //  Meaning we skip the moves that can't be preformed, since it is not their turn
                // The statement asks if the piece is white and its white's turn, or black and black's turn
                if((spots[startSpot].spotPiece.isWhite && this.isWhiteTurn)
                        || (!spots[startSpot].spotPiece.isWhite && !this.isWhiteTurn)){
                    ArrayList<Move> retrievedMoves = new ArrayList<>();
                    // Generate the moves that it can preform by the type of piece is is
                    if(spots[startSpot].spotPiece.isSlidingType()){
                        retrievedMoves = generateSlidingMoves(startSpot, spots[startSpot].spotPiece);
                    }

                    //TODO Create methods for the rest of the pieces and their moves

                    // After we determined the list of moves that a piece can take by the type of movement, add them
                    moves.addAll(retrievedMoves);
                }
            }
        }

        return moves;
    }

    // Long range sliding pieces (Bishop, Queen and Rook)
    ArrayList<Move> generateSlidingMoves(int startSpot, Piece token){
        ArrayList<Move> slidingMoves = new ArrayList<>();
        // Set the start index to start at 0 for our bishop (only diagonals), so it ends at 4
        // And the end index to 8 if for our Rook, since it starts at 4
        // More on the reason behind this in the comments for @directionCorrection
        int startIndex = token.pieceType == Piece.Type.Bishop ? 0 : 4;
        int endIndex = token.pieceType == Piece.Type.Rook ? 4 : 8;

        // For each direction
        for(int dir=startIndex; dir<endIndex; dir ++){
            // For each square in between the start and the edge of the board
            for(int square = 0; square<numSquaresToEdge(startSpot, dir); square++){
                // Set the target spot of the piece to be:
                // - The start
                // - Plus the direction correction * (square+1)
                // This means that the moves generated start in the direction that we want, then as many times as it
                //  takes to get to the edge of the board, having a "move" for each square along the way
                int targetSpot = startSpot + directionCorrection(dir) * (square+1);
                Piece target = spots[targetSpot].spotPiece;

                // The correct order for this:
                // 1. Check if target is friendly
                // 2. If it is, break as we can't go farther
                // 3. Add this spot as a possible move
                // 4. Check if target is hostile
                // 5. If it is, break as we can't go farther

                // Spot could be empty, if it is we always add it
                // This is really messy though lol, could be improved if we didn't need this != null check
                // Since technically the whole first argument should take care of an empty space
                if(target != null){
                    if(token.isFriendly(target)) break;

                    slidingMoves.add(new Move(startSpot, targetSpot));

                    if(!token.isFriendly(target)) break;
                } else{
                    slidingMoves.add(new Move(startSpot, targetSpot));
                }
            }
        }
        return slidingMoves;
    }

    // Just the knight, as it is special with the L pattern
    ArrayList<Move> generateKnightMoves(int startSpot, Piece token){
        ArrayList<Move> knightMoves = new ArrayList<>();

        return knightMoves;
    }

    // Pawn only goes forward, depending on color
    ArrayList<Move> generatePawnMoves(int startSpot, Piece token){
        ArrayList<Move> pawnMoves = new ArrayList<>();

        return pawnMoves;
    }

    // King can only go once in each direction, but also can't go where there are other colored moves present
    ArrayList<Move> generateKingMoves(int startSpot, Piece token){
        ArrayList<Move> kingMoves = new ArrayList<>();

        return kingMoves;
    }

    // Returns a value depending on the direction provided, which depends on the integer value of the array
    // We use this, and a multiple of the @numSquaresToEdge method to preform moves
    // The reason that the numbers are weird for the directions is so we can use 0-4 for diagonal (Bishop)
    //  And 5-8 for horizontal/vertical as numbers for the for loop when generating moves
    // [0] [4] [1]    [7]  [8]  [9]
    // [7] [X] [5] -> [-1] [X]  [1]
    // [3] [6] [2]    [-9] [-8] [-7]
    int directionCorrection(int dir){
        switch (dir){
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

    // Special conversion for the knight since he is a special boy
    //  [ ]  [1]  [ ]  [2]  [ ]
    //  [8]  [ ]  [ ]  [ ]  [3]
    //  [ ]  [ ]  [X]  [ ]  [ ]
    //  [7]  [ ]  [ ]  [ ]  [4]
    //  [ ]  [6]  [ ]  [5]  [ ]
    // Requires special checks, since you can always go -9 or whatever, but it wont be caught by the board
    int directionConversionKnight(int dir){
        return 0;//TODO Finish this
    }

    // Returns the amount of squares that are in between a piece's spot and the edge of the board
    // Assumes the same directions as the ones from the sliding matrix
    // [0] [4] [1]
    // [7] [X] [5]
    // [3] [6] [2]
    int numSquaresToEdge(int startSpot, int dir){ //TODO Finish this
        int directionalOffset = directionCorrection(dir);//No need to calculate this multiple times
        int rowKey = 0; // Which row in the board it is at
        int numSpaces = 0;

        // Get the row key
        // For each key in the known library
        for(int i=0; i<FENKey.length; i++){
            // Set this key as the highest ([0] = 0)
            rowKey = FENKey[i];
            // Whenever it happens to be larger, then we are at the highest row in the board
            if(startSpot >= FENKey[i]) break; // Also equal, since it can be on the left side of the board
        }

        while(true){
            int suggestedSpace = startSpot + directionalOffset * (numSpaces + 1);
            // Because of the directions being limited by a a single number, we need to check different ones
            // Up:
            // Always larger than the rowKey, can be 1 less in dir=0
            if(dir == 0 || dir == 4 || dir == 1){
                if(suggestedSpace > rowKey) break; //TODO This is wrong, needs a looping thing
                if(rowKey - 1 == suggestedSpace && dir == 0) break;
            }
            // Left is only a general check on below the rowKey
            else if(dir == 7){
                //if(rowKey - 1 == suggestedSpace) break;
            }

            numSpaces++;
            break;
        }



        return numSpaces;
    }

    //TODO Make a method that takes two characters "a1" and converts it to a number on our grid

    //Standard java inherited method override
    public String toString(){
        String out = "";
        String borderString = "----------------------------";
        String letterString = "+  a   b   c   d   e   f   g   h\n";

        out += borderString + "\n";
        out += letterString;

        //63 - j - i*8, making it start at the left of the board and go right
        for(int i=0; i<8; i++){//row on board
            //Add the number at which the row is stationed at
            out += 8 - i; // Since it starts at 0 and works down, we need to inverse it
            //Each addition is "| x ", making the last one empty
            for(int j=7; j>=0; j--)//spot on row
                out += "| " + spots[63 - j - i*8] + " ";
            //So that when the row is over, we can close it and make a new line
            out += "|\n";
        }
        out += borderString;
        return out;
    }

    void showBoard(){
        System.out.println(this);
    }

    void showDebugVals(){
        String out = "";

        out += "White's turn: " + isWhiteTurn + "\n";

        out += "Castling:\n";
        out += "\tWhite King:  " + CanCastleWhiteKing + "\n";
        out += "\tWhite Queen: " + CanCastleWhiteQueen + "\n";
        out += "\tBlack King:  " + CanCastleBlackKing + "\n";
        out += "\tWhite Queen: " + CanCastleBlackQueen + "\n";

        out += "Half moves: " + halfMoves + "\n";
        out += "Full moves: " + fullMoves + "\n";

        System.out.println(out);
    }
}

/*
This is the idea of the 1 dimensional board:
0, 1, 2, 3, .. , 63
-----------------------
56 57 58 59 60 61 62 63
48 49 50 51 52 53 54 55
40 41 42 43 44 45 46 47
32 33 34 35 36 37 38 39
24 25 26 27 28 29 30 31
16 17 18 19 20 21 22 23
8  9  10 11 12 13 14 15
0  1  2  3  4  5  6  7
-----------------------

This makes translations look like this:
+7 +8 +9
-1  0 +1
-9 -8 -7
Will be useful for valid moves

With FEN interpretation of '/' being a quick substitution of i to the next key value.
Key values:
56, 48, 40, 32, 24, 26, 8, 0

Thoughts on this:
- FEN string start at 56, and goes right across each row
- Will require an interpreter of some degree to instead separate each rows pieces by string
*/