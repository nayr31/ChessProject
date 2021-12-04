import java.util.ArrayList;

public class Board {
    public static boolean gameWillContinue = true;
    public static String winner;
    static boolean lastPlayerDidNotAct = false;
    static Spot[] spots = new Spot[64];
    static boolean isWhiteTurn = true;
    static int halfMoves, fullMoves;
    static boolean CanCastleWhiteKing, CanCastleWhiteQueen;
    static boolean CanCastleBlackKing, CanCastleBlackQueen;
    // This is the last move record, a record of moves and pieces that were taken during that move
    // This is used recursively to make consecutive moves and to undo them as well
    static ArrayList<LastMoveRecord> lastMoveRecords = new ArrayList<>();
    static class LastMoveRecord {
        private final Move move;
        private final Piece takenPiece;
        boolean isRoot;
        boolean[] boolChanges;

        public LastMoveRecord(Move move, Piece takenPiece, boolean isRoot, boolean[] boolChanges) {
            this.move = move;
            this.takenPiece = takenPiece;
            this.isRoot = isRoot;
            this.boolChanges = boolChanges;
        }

        public String toString() {
            return move.toString();
        }
    }

    public static void changeTurns() {
        Board.isWhiteTurn = !Board.isWhiteTurn;
    }

    // Default stalemate of only two kings
    public static boolean isStaleMate(){
        boolean white = false;
        boolean black = false;
        for(int i=0;i< spots.length-1;i++){
            if(spots[i].spotPiece != null){
                if(spots[i].spotPiece.isWhite){
                    if(white)
                        return false;
                    else
                        white = true;
                } else{
                    if(black)
                        return false;
                    else
                        black = true;
                }
            }
        }
        return true;
    }

    public static boolean colorIsCheckMate(boolean isWhite){
        return colorIsCheckMate(isWhite, null);
    }

    public static boolean colorIsCheckMate(boolean isWhite, ArrayList<Move> moves){
        if(moves == null)
            moves = MoveCoordinator.generateLegalMoves(isWhite);
        return moves.size() == 0;
    }

    public static boolean playerInCheck(boolean isWhite){
        return MoveCoordinator.kingIsInCheck(isWhite);
    }

    public static boolean playerInCheck(){
        return MoveCoordinator.kingIsInCheck(isWhiteTurn);
    }
    //Populates the board with empty objects
    static void initiate() {
        for (int i = 0; i < spots.length; i++)
            spots[i] = new Spot();
    }

    //The normal board configuration
    static void popNormal() {
        popFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    static void popTest() {
        popFromFEN("4k2r/6r1/8/8/8/8/3R4/R3K3 w Qk - 0 1");
    }

    //TODO Add an "output to FEN" method
    // Include half/full counting, 50 full ends game

    //Populates the board from the FEN string
    //TODO Enable error proofing for the FEN input
    static void popFromFEN(String inputString) {
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
        for (int i = 0; i < inputArray.length; i++) {
            String rowString = inputArray[i];
            //Now have a row of FEN input (first iteration is top, 56-63)
            //Since the input string starts at 56 (standard FEN), we inverse the string so it works and I don't have to change anything
            rowString = (new StringBuilder(rowString)).reverse().toString();

            // Get every character input from the FEN row string (/xxx/)
            int j = 0;
            for (char pieceChar : rowString.toCharArray()) {
                // Determine the spot location in the array
                // - Start at 63
                // - Backtrack down the row from each character read (piece placed)
                // - Every row skips 8 immediately
                int spotLoc = 63 - j - 8 * i;

                // If the character is not a digit, we set that spot to have a piece
                if (!Character.isDigit(pieceChar)) {
                    //So set the spot on the board to that piece
                    placeNewPiece(pieceChar, spotLoc);
                    j++;
                } else { // If it is a digit, we skip that many spaces horizontally
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
        // Check which can (ie. havn't moved yet)
        for (char c : varInput[2].toCharArray()) {
            switch (c) {
                case 'K': // White king side
                    CanCastleWhiteKing = true;
                    break;
                case 'Q':
                    CanCastleWhiteQueen = true;
                    break;
                case 'k':
                    CanCastleBlackKing = true;
                    break;
                case 'q':
                    CanCastleBlackQueen = true;
                    break;
            }
        }
        // Then punish the ones that have moved
        // [King ... Queen]
        //  56   ...  63   Black
        //  0    ...  7    White
        // Check every spot
        for (int i = 0; i < spots.length; i++) {
            Piece token = spots[i].spotPiece;
            if (token != null) {
                if (token.pieceType == Piece.Type.Rook) {
                    if (token.isWhite) {
                        if (!CanCastleWhiteKing) {
                            // If you can castle the white queen side, and this is on that spot
                            if (CanCastleWhiteQueen && i == 7) {
                                // Then don't do anything, since this is the white queen rook and it already hasn't moved
                            } else { // Otherwise, either the king side moved or the queen isn't allowed (and this is the queen)
                                token.hasMoved = true;
                            }
                        }
                        if (!CanCastleWhiteQueen) {
                            if (CanCastleWhiteKing && i == 0) {
                            } else {
                                token.hasMoved = true;
                            }
                        }
                    } else {
                        if (!CanCastleBlackKing) {
                            if (CanCastleBlackQueen && i == 63) {
                            } else {
                                token.hasMoved = true;
                            }
                        }

                        if (!CanCastleBlackQueen) {
                            if (CanCastleBlackKing && i == 56) {
                            } else {
                                token.hasMoved = true;
                            }
                        }
                    }
                }
            }
        }

        // --------- Step 3 ---------
        // Which pawns can be en-passant-ed
        // Use ascii and j,i multiplication
        // First char is a letter (a-h), 97-(ascii value) for j (left-right)
        // Second char is a number (1-8), 63-8*(number value-1) for i (up-down)
        if (varInput[3].length() != 1) { // If the space is not empty, diagnose it
            // If there is a viable passant move, then check every two spaces for them
            for (int i = 0; i < varInput[3].length(); i += 2) {
                // Get the space of the passant target
                String passantString = varInput[3].substring(i, i + 2);
                // Get the int interpretation of the target square
                int passantSquare = 0;
                try {
                    passantSquare = convertInputToIndex(passantString);
                } catch (NotLocationException e) {
                    e.printStackTrace(); // This should never happen
                }
                // Determine the spaceDelta, which offsets where the piece and its startSpot is
                int spaceDelta = 8;
                if (passantSquare > 23) // Black piece
                    spaceDelta = -8;
                // Set the piece's last move with a moveDelta of 2
                spots[passantSquare + spaceDelta].spotPiece.changeLastMove(
                        new Move(passantSquare - spaceDelta, passantSquare + spaceDelta));

            }
        }

        // --------- Step 4 ---------
        // Half moves
        halfMoves = Integer.parseInt(varInput[4]);

        // --------- Step 5 ---------
        // Full moves
        fullMoves = Integer.parseInt(varInput[5]);
    }

    public static void placeNewPiece(char pieceChar, int spotLoc) {
        spots[spotLoc].spotPiece = new Piece(
                pieceChar,
                Character.isUpperCase(pieceChar)
        );
    }

    /*
    // Populates all moves that each player can take
    // Required per turn, since some may become invalid
    static void populateMoveLists() {
        // Populate the general piece moves (not king)
        whiteMoves = MoveCoordinator.getGeneralPieceMoves(true);
        blackMoves = MoveCoordinator.getGeneralPieceMoves(false);
        // Once we know where pieces can attack for check, generate the king moves that are valid
        whiteMoves.addAll(MoveCoordinator.getKingMoves(true));
        blackMoves.addAll(MoveCoordinator.getKingMoves(false));
    }

    // Enemy side calling for move generation
    // Sequence:
    //  - All moves are generated in populateMoveLists()
    //  - Player (white) makes move
    //  - If king, spotIsCoveredByPiece()
    //  - Afterwards in both cases populateColorSide(black)
    //  - Bad move (ie, puts king in check), unmake it and populateColorSide(black) again
    //  - Good move, return a good result
    void populateColorSide(boolean isWhite) {
        if (isWhite) {
            whiteMoves = MoveCoordinator.getGeneralPieceMoves(true);
            whiteMoves.addAll(MoveCoordinator.getKingMoves(true));
        } else {
            blackMoves = MoveCoordinator.getGeneralPieceMoves(false);
            blackMoves.addAll(MoveCoordinator.getKingMoves(false));
        }
    }*/

    // Prints debug information about number of squares to the edge of the board given a startSpot
    // [0] [4] [1]
    // [7] [X] [5]
    // [3] [6] [2]
    static void debugNumToEdge(int startSpot) {
        System.out.println("Up from " + startSpot + ": " + MoveCoordinator.numSquaresToEdge(startSpot, 4));
        System.out.println("Up-Right from " + startSpot + ": " + MoveCoordinator.numSquaresToEdge(startSpot, 1));
        System.out.println("Right from " + startSpot + ": " + MoveCoordinator.numSquaresToEdge(startSpot, 5));
        System.out.println("Down-Right from " + startSpot + ": " + MoveCoordinator.numSquaresToEdge(startSpot, 2));
        System.out.println("Down from " + startSpot + ": " + MoveCoordinator.numSquaresToEdge(startSpot, 6));
        System.out.println("Down-Left from " + startSpot + ": " + MoveCoordinator.numSquaresToEdge(startSpot, 3));
        System.out.println("Left from " + startSpot + ": " + MoveCoordinator.numSquaresToEdge(startSpot, 7));
        System.out.println("Up-Left from " + startSpot + ": " + MoveCoordinator.numSquaresToEdge(startSpot, 0));
    }

    // Converts board state input (such as "a5") to an index
    static int convertInputToIndex(String input) throws NotLocationException {
        char[] arr = input.toCharArray();
        if (arr.length != 2)
            throw new NotLocationException("Incorrect length of string. Needs to be 2 long.");
        // arr[0] is the letter, convert it down from ascii
        int letterVal = arr[0] - 96 - 1;
        int numberVal = (arr[1] - 49) * 8;
        int combinedVal = letterVal + numberVal;
        if (combinedVal < 0 || combinedVal > 63) {
            throw new NotLocationException("Something went wrong converting the value " + input);
        }
        return combinedVal;
    }
    //Standard java inherited method override
    static public String boardString() {
        StringBuilder out = new StringBuilder();
        String borderString = "------------------";
        String letterString = "+ a b c d e f g h\n";

        out.append(borderString).append("\n");

        //63 - j - i*8, making it start at the left of the board and go right
        for (int i = 0; i < 8; i++) {//row on board
            //Add the number at which the row is stationed at
            out.append(8 - i); // Since it starts at 0 and works down, we need to inverse it
            //Each addition is "| x ", making the last one empty
            for (int j = 7; j >= 0; j--){//spot on row
                Spot sp = spots[63 - j - i * 8];
                out.append("|").append(sp).append("");
            }

            //So that when the row is over, we can close it and make a new line
            out.append("|\n");
        }
        out.append(letterString);
        out.append(borderString);
        return out.toString();
    }

    public String toString(){
        return boardString();
    }

    static void makeMove(Move move){
        makeMove(move,true);
    }

    // Preforms a move on the board and stores the information about what happened
    static void makeMove(Move move, boolean isRoot) {
        // Record if there was a piece that was taken with the move data
        Piece takenPiece = spots[move.endSpot].spotPiece;
        boolean[] boolChanges = determineBoolChanges(move);
        lastMoveRecords.add(new LastMoveRecord(move, takenPiece, isRoot, boolChanges));
        actOnBoolChanges(boolChanges);
        // Move the attacking piece into the end spot of the move
        Piece attackingPiece = spots[move.startSpot].spotPiece;
        attackingPiece.lastMoves.add(move);
        spots[move.startSpot].spotPiece = null;
        spots[move.endSpot].spotPiece = attackingPiece;
        // Recall this for embedded moves
        if (move.embeddedMove != null) makeMove(move.embeddedMove.embeddedMove, false);
    }

    // Unmakes and reverses the results of the last preformed move
    static void unmakeMove() {
        // Remove the last move that occurred
        LastMoveRecord lastMoveRecord = lastMoveRecords.remove(lastMoveRecords.size() - 1);
        // Reverse the movement of the attacking piece (set the starting point piece to the current
        spots[lastMoveRecord.move.startSpot].spotPiece = spots[lastMoveRecord.move.endSpot].spotPiece;
        // Replace the attacked piece
        spots[lastMoveRecord.move.endSpot].spotPiece = lastMoveRecord.takenPiece;
        // Un-remember the last move
        spots[lastMoveRecord.move.startSpot].spotPiece.forget();
        // Make the necessary changes to the castling booleans
        actOnBoolChanges(lastMoveRecord.boolChanges);
        // Recurse until we get to a root move
        if (!lastMoveRecord.isRoot) unmakeMove();
    }

    static void showDebugValues() {
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

    // Determines which castling booleans should change with the suggested move
    static boolean[] determineBoolChanges(Move move){ // WK, WQ, BK, BQ
        Piece token = spots[move.startSpot].spotPiece;
        boolean[] boolChanges = new boolean[]{false,false,false,false};
        if(token.pieceType == Piece.Type.Rook){
            if(token.isWhite){
                if(move.startSpot == 0) // WK
                    boolChanges[0] = true;
                if(move.startSpot == 7) // WQ
                    boolChanges[1] = true;
            } else{
                if(move.startSpot == 56) // BK
                    boolChanges[2] = true;
                if(move.startSpot == 63) // BQ
                    boolChanges[3] = true;
            }
        }
        return boolChanges;
    }

    static void actOnBoolChanges(boolean[] boolChanges){
        if(boolChanges[0])
            CanCastleWhiteKing = !CanCastleWhiteKing;
        if(boolChanges[1])
            CanCastleWhiteQueen = !CanCastleWhiteQueen;
        if(boolChanges[2])
            CanCastleBlackKing = !CanCastleBlackKing;
        if(boolChanges[3])
            CanCastleBlackQueen = !CanCastleBlackQueen;
    }

    public static Spot[] getSpots() {
        return spots;
    }

}

/*
This is the idea of the 1 dimensional board:
0, 1, 2, 3, .. , 63
-----------------------
56 57 58 59 60 61 62 63 8
48 49 50 51 52 53 54 55 7
40 41 42 43 44 45 46 47 6
32 33 34 35 36 37 38 39 5
24 25 26 27 28 29 30 31 4
16 17 18 19 20 21 22 23 3
8  9  10 11 12 13 14 15 2
0  1  2  3  4  5  6  7  1
-----------------------
a  b  c  d  e  f  g  h

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