import java.util.ArrayList;

public class Board {
    static Spot[] spots = new Spot[64];
    static int[] FENKey = {56, 48, 40, 32, 24, 16, 8, 0};
    static boolean isWhiteTurn = true;
    static int halfMoves, fullMoves;
    static boolean CanCastleWhiteKing, CanCastleWhiteQueen;
    static boolean CanCastleBlackKing, CanCastleBlackQueen;
    // Keep track of which moves each side can take
    static ArrayList<Move> whiteMoves = new ArrayList<>();
    static ArrayList<Move> blackMoves = new ArrayList<>();
    // This is the last move record, a record of moves and pieces that were taken during that move
    // This is used recursively to make consecutive moves and to undo them as well
    //
    static ArrayList<LastMoveRecord> lastMoveRecords = new ArrayList<>();

    static class LastMoveRecord {
        private final Move move;
        private final Piece takenPiece;
        boolean isRoot;

        public LastMoveRecord(Move move, Piece takenPiece, boolean isRoot) {
            this.move = move;
            this.takenPiece = takenPiece;
            this.isRoot = isRoot;
        }

        public String toString() {
            return move.toString();
        }
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

    //Populates the board from the FEN string
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
                    spots[spotLoc].spotPiece = new Piece(
                            pieceChar,
                            Character.isUpperCase(pieceChar)
                    );
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
                int passantSquare = convertInputToIndex(passantString);
                // Determine the spaceDelta, which offsets where the piece and its startSpot is
                int spaceDelta = 8;
                if (passantSquare > 23) // Black piece
                    spaceDelta = -8;
                // Set the piece's last move with a moveDelta of 2
                spots[passantSquare + spaceDelta].spotPiece.lastMove =
                        new Move(passantSquare - spaceDelta, passantSquare + spaceDelta);
            }
        }

        // --------- Step 4 ---------
        // Half moves
        halfMoves = Integer.parseInt(varInput[4]);

        // --------- Step 5 ---------
        // Full moves
        fullMoves = Integer.parseInt(varInput[5]);
    }

    // Populates all moves that each player can take
    // Required per turn, since some may become invalid
    static void populateMoveLists() {
        // Populate the general piece moves (not king)
        whiteMoves = getGeneralPieceMoves(true);
        blackMoves = getGeneralPieceMoves(false);
        // Once we know where pieces can attack for check, generate the king moves that are valid
        whiteMoves.addAll(getKingMoves(true));
        blackMoves.addAll(getKingMoves(false));
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
            whiteMoves = getGeneralPieceMoves(true);
            whiteMoves.addAll(getKingMoves(true));
        } else {
            blackMoves = getGeneralPieceMoves(false);
            blackMoves.addAll(getKingMoves(false));
        }
    }

    // Generate a list of all moves that each piece can preform, following certain conditions:
    // 1 - Is the correct color of whomever turn it is
    // 2 - Different pieces need different methods to determine which moves are possible
    static ArrayList<Move> getGeneralPieceMoves(boolean isWhite) {
        ArrayList<Move> moves = new ArrayList<>();

        // For each spot on the board
        for (int startSpot = 0; startSpot < 64; startSpot++) {
            // Check to see if there is a piece there
            if (spots[startSpot].spotPiece != null) {
                // Check to see if the piece corresponds to the color that can move
                //  Meaning we skip the moves that can't be preformed, since it is not their turn
                // The statement asks if the piece is white and its white's turn, or black and black's turn
                if (spots[startSpot].spotPiece.isWhite == isWhite) {
                    ArrayList<Move> retrievedMoves = new ArrayList<>();
                    // Generate the moves that it can preform by the type of piece is is
                    if (spots[startSpot].spotPiece.isSlidingType()) {
                        retrievedMoves = generateSlidingMoves(startSpot, spots[startSpot].spotPiece);
                    } else if (spots[startSpot].spotPiece.pieceType == Piece.Type.Pawn) {
                        retrievedMoves = generatePawnMoves(startSpot, spots[startSpot].spotPiece);
                    } else if (spots[startSpot].spotPiece.pieceType == Piece.Type.Knight) {
                        retrievedMoves = generateKnightMoves(startSpot, spots[startSpot].spotPiece);
                    }

                    // After we determined the list of moves that a piece can take by the type of movement, add them
                    moves.addAll(retrievedMoves);
                }
            }
        }
        return moves;
    }

    // King moves need to be calculated afterwards because of check restrictions
    static ArrayList<Move> getKingMoves(boolean isWhite) {
        ArrayList<Move> retrievedMoves = new ArrayList<>();
        for (int startSpot = 0; startSpot < 64; startSpot++) {
            Piece token = spots[startSpot].spotPiece;
            if (token.pieceType == Piece.Type.King && token.isWhite == isWhite) {
                retrievedMoves = generateKingMoves(startSpot, token);
                break;
            }
        }
        return retrievedMoves;
    }

    // Long range sliding pieces (Bishop, Queen and Rook)
    static ArrayList<Move> generateSlidingMoves(int startSpot, Piece token) {
        ArrayList<Move> slidingMoves = new ArrayList<>();
        // Set the start index to start at 0 for our bishop (only diagonals), so it ends at 4
        // And the end index to 8 if for our Rook, since it starts at 4
        // More on the reason behind this in the comments for @directionCorrection
        int startIndex = token.pieceType == Piece.Type.Bishop ? 0 : 4;
        int endIndex = token.pieceType == Piece.Type.Rook ? 4 : 8;

        // For each direction
        for (int dir = startIndex; dir < endIndex; dir++) {
            // For each square in between the start and the edge of the board
            for (int square = 0; square < numSquaresToEdge(startSpot, dir); square++) {
                // Set the target spot of the piece to be:
                // - The start
                // - Plus the direction correction * (square+1)
                // This means that the moves generated start in the direction that we want, then as many times as it
                //  takes to get to the edge of the board, having a "move" for each square along the way
                int targetSpot = startSpot + directionCorrection(dir) * (square + 1);
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
                if (target != null) {
                    if (token.isFriendly(target)) break; // Can't go into a friendly space, blocks path

                    slidingMoves.add(new Move(startSpot, targetSpot)); // Can go into an enemy space, also blocks path

                    if (!token.isFriendly(target)) break;
                } else {
                    slidingMoves.add(new Move(startSpot, targetSpot));
                }
            }
        }
        return slidingMoves;
    }

    // Just the knight, as it is special with the L pattern
    static ArrayList<Move> generateKnightMoves(int startSpot, Piece token) {
        ArrayList<Move> knightMoves = new ArrayList<>();
        //TODO Finish knight moves
        return knightMoves;
    }

    // Pawn only goes forward, direction depending on color
    static ArrayList<Move> generatePawnMoves(int startSpot, Piece token) {
        ArrayList<Move> pawnMoves = new ArrayList<>();
        // Determine how far the pawn can move depending on how far it has moved
        int spacesToMove = 1;
        if (token.hasMoved)
            spacesToMove++;

        // For each of the possible move distances
        // This is just for moving forward, not taking pieces
        for (int i = 0; i < spacesToMove; i++) {
            int targetSpot = startSpot + pawnDirectionCorrection(token.isWhite) * i;
            // Check to see if the target spot is within bounds
            if (targetSpot < 63 & targetSpot > 0) {
                // Target spot is within bounds
                Piece target = spots[targetSpot].spotPiece;

                // Space is occupied, cannot move forward
                if (target != null) break;
                // Otherwise, it is a possible move
                pawnMoves.add(new Move(startSpot, targetSpot));
            } else break; // Break in both instances
        }

        // Target taking
        // Get the target directions of the piece depending on the color
        int[] targetDir = pawnAttackDirectionCorrection(token.isWhite);
        // PassantDir relates to the left-right relation of the targetDir
        int[] passantDir = {7, 5};
        // For each targeting direction
        for (int i = 0; i < targetDir.length; i++) {
            if (numSquaresToEdge(startSpot, targetDir[i]) >= 1) {
                int targetSpot = startSpot + directionCorrection(targetDir[i]); // Board wise value of the target direction

                Piece target = spots[targetSpot].spotPiece;

                // Add the move if it is an enemy piece
                if (target != null) {
                    if (!token.isFriendly(target))
                        pawnMoves.add(new Move(startSpot, targetSpot));
                } else { // If the space is empty, we may be able to passant
                    // We would normally check to see if there are enough spaces to the right or left, but diagonal ensures both
                    // Make a new passantTargetSpot where the pawn would be
                    int passantTargetSpot = startSpot + directionCorrection(passantDir[i]);
                    target = spots[passantTargetSpot].spotPiece;
                    if (target != null) { // There is a piece at the new targetSpot
                        // Check if it is an enemy pawn that move delta is 16 (moved 2 spaces)
                        if (target.pieceType == Piece.Type.Pawn
                                && !token.isFriendly(target)
                                && target.lastMove.moveDelta() == 16) {
                            // Add the move, but make the spot that dies the enemy pawn
                            pawnMoves.add(new Move(startSpot, targetSpot, new Move(targetSpot, passantTargetSpot)));
                        }
                    }
                }
            }
        }

        return pawnMoves;
    }


    // Generates a list of moves for pawns on all of their attacking spots
    static ArrayList<Move> generateAttackingPawnMoveSpots(boolean isWhite) { // The passed isWhite value is the enemy's
        ArrayList<Move> pawnMoves = new ArrayList<>();
        int[] targetDir = pawnAttackDirectionCorrection(!isWhite);

        for (int startSpot = 0; startSpot < spots.length; startSpot++) {
            Piece token = spots[startSpot].spotPiece;
            // If the piece selected is a pawn
            if (token != null) {
                if (token.pieceType == Piece.Type.Pawn) {
                    // Check it's attacking directions for a valid move
                    for (int i = 0; i < targetDir.length; i++) {
                        if (numSquaresToEdge(startSpot, targetDir[i]) >= 1) {
                            // Add the attacking move spot
                            int targetSpot = startSpot + directionCorrection(targetDir[i]);
                            pawnMoves.add(new Move(startSpot, targetSpot));
                        }
                    }
                }
            }
        }
        return pawnMoves;
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

    // King can only go once in each direction, but also can't go where there are other colored moves present
    static ArrayList<Move> generateKingMoves(int startSpot, Piece token) {
        ArrayList<Move> kingMoves = new ArrayList<>();

        // For each move direction
        // Just moves, castling is done afterwards
        for (int i = 0; i < 8; i++) {
            // If there is a spot in that direction
            if (numSquaresToEdge(startSpot, i) >= 1) {
                int targetSpot = startSpot + directionCorrection(i);

                Piece target = spots[targetSpot].spotPiece;
                if (target != null) {
                    if (!token.isFriendly(target)) { // If enemy piece
                        kingMoves.add(new Move(startSpot, targetSpot));
                    } // Otherwise we can't move there
                } else { // Empty space, check for check in spot
                    if (spotIsNotCoveredByEnemyPiece(targetSpot, token.isWhite))
                        kingMoves.add(new Move(startSpot, targetSpot)); // If the spot is not covered by another p
                }
            }
        }

        // Castling has rules:
        //  1 - King moves two spaces towards king/queen side
        //  2 - Rook goes on other side of king
        //  3 - Can't be made when king is in check
        //  4 - Can't move through squares that are covered by enemy moves
        //  5 - Can't castle if king has already moved
        //  6 - Can't castle if rook has already moved
        if (!token.hasMoved) { // (5)
            if (spotIsNotCoveredByEnemyPiece(startSpot, token.isWhite)) { // (3)
                // (1,2,4,6)
                if (token.isWhite ? CanCastleWhiteQueen : CanCastleBlackQueen){
                    Move move = makeCastle(token, startSpot, false);
                    if (move != null)
                        kingMoves.add(move);
                }
                if (token.isWhite ? CanCastleWhiteKing : CanCastleBlackKing) {
                    Move move = makeCastle(token, startSpot, true);
                    if (move != null)
                        kingMoves.add(move);
                }
            }
        }

        return kingMoves;
    }

    // Checks for blocking attack spaces while returning a valid move if one ecists
    static Move makeCastle(Piece token, int startSpot, boolean isKingSide) {
        int dir = kingCastleDirectionCorrection(token.isWhite);
        if (castleSpacesAreFree(startSpot, dir, token.isWhite)) {
            int targetSpot = startSpot + directionCorrection(dir) * 2;
            Move rookMove = new Move(getCastleSpot(token.isWhite, isKingSide), targetSpot - directionCorrection(dir));
            return new Move(startSpot, targetSpot, rookMove, false);
        }
        return null;
    }

    // Returns the rook position when that side is valid (it will have never had moved from this spot
    static int getCastleSpot(boolean isWhite, boolean isKingSide) {
        if (isWhite) {
            if (isKingSide) {
                return 0;
            } else {
                return 7;
            }
        } else {
            if (isKingSide) {
                return 56;
            } else {
                return 63;
            }
        }
    }

    // Check to see if the two spaces towards the rook are free and aren't covered by a move
    static boolean castleSpacesAreFree(int startSpot, int dir, boolean isWhite) {
        int spaceDirOffset = directionCorrection(dir);
        int targetSpot = startSpot + spaceDirOffset;
        boolean space1 = spots[targetSpot].spotPiece == null && spotIsNotCoveredByEnemyPiece(targetSpot, isWhite);
        targetSpot = startSpot + spaceDirOffset * 2;
        boolean space2 = spots[targetSpot].spotPiece == null && spotIsNotCoveredByEnemyPiece(targetSpot, isWhite);
        return space1 && space2;
    }

    // Returns the direction that the king would need to move to obtain a castle on that side
    // [0] [4] [1]
    // [7] [X] [5]
    // [3] [6] [2]
    // King side is left (7), Queen side is right (5)
    static int kingCastleDirectionCorrection(boolean isKingSide) {
        return isKingSide ? 7 : 5;
    }

    // Checks to see if a known enemy move is covers a spot with a move
    static boolean spotIsNotCoveredByEnemyPiece(int spot, boolean isWhite) {
        class Attacker {
            public Attacker(Move move, Piece piece) {
                this.move = move;
                this.piece = piece;
            }

            final Move move;
            final Piece piece;
        }
        ArrayList<Move> enemyMoveList;
        ArrayList<Attacker> attackers = new ArrayList<>();
        // Determine which enemy list to search through
        if (isWhite) {
            enemyMoveList = blackMoves;
        } else {
            enemyMoveList = whiteMoves;
        }

        // Iterate through the moves list and keep track at all pieces attacking that spot
        for (Move move : enemyMoveList) {
            if (move.endSpot == spot)
                attackers.add(new Attacker(move, spots[move.startSpot].spotPiece));
        }

        // This resulting list will be all attacking moves on that spot
        // If there are any that aren't pawns, then the check fails
        for (Attacker attacker : attackers) {
            if (attacker.piece.pieceType != Piece.Type.Pawn)
                return false; // Otherwise, the list has another piece that ends on this square
        }

        // Pawns have moves that aren't attacks, so we need to check those separately
        // If the spot is located in that list, then it is covered
        ArrayList<Move> pawnAttackingSpots = generateAttackingPawnMoveSpots(isWhite);
        for (Move m : pawnAttackingSpots) {
            if (m.endSpot == spot)
                return false;
        }

        return true;
    }

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

    String directionCorrectionString(int dir) {
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

    // Returns the amount of squares that are in between a piece's spot and the edge of the board
    // Assumes the same directions as the ones from the sliding matrix
    // [0] [4] [1]
    // [7] [X] [5]
    // [3] [6] [2]
    static int numSquaresToEdge(int startSpot, int dir) {
        int directionalOffset = directionCorrection(dir);//No need to calculate this multiple times
        int numSpaces = 0;
        int rowKey = generateLocalRowKey(startSpot); // Which row in the board it is at
        //System.out.println("rowKey of " + directionCorrectionString(dir) + ":" + rowKey);

        while (true) {
            int suggestedSpace = startSpot + directionalOffset * (numSpaces + 1); // The square it is trying to move to
            // The order of generating the rowKey and the suggested space is very important
            // It needs to:
            //  - Start at the beginning (startSpot)
            //  - Relocate after a valid space is found

            // Always happens
            if (suggestedSpace > 63 || suggestedSpace < 0) break;
            // Duplicates removed (up, down)

            // Right
            if (dir == 5) { // Right means that it loops to the next rowKey, so just do +8
                if (suggestedSpace >= rowKey + 8) break;
            }
            // Left
            if (dir == 7) { // Left means that it goes lower than the rowKey
                if (suggestedSpace < rowKey) break;
            }
            // Up-Right
            if (dir == 1) { // Would go up, then loop around, +9 of start, meaning +16 of rowKey (2 rows up)
                if (suggestedSpace == rowKey + 16) break;
            }
            // Down-Right
            if (dir == 2) { // Down (-8) then right (+1), -7 of start, meaning (1 row down, then +1, which would be rowkey)
                if (suggestedSpace == rowKey) break;
            }
            // Down-Left
            if (dir == 3) { // -7 of the rowKey would be
                if (suggestedSpace < rowKey - 8) break;
            }
            // Up-Left
            if (dir == 0) { // Go up a row
                if (suggestedSpace >= rowKey + 7) break;
            }

            // No conditions found to stop, increase the amount of moves possible and set the new rowkey
            numSpaces++;
            rowKey = generateLocalRowKey(suggestedSpace);
        }

        return numSpaces;
    }

    // Finds the rowKey value of the spot that is given
    // A rowKey is the key values that start rows in the array
    static int generateLocalRowKey(int suggestedSpace) {
        int rowKey = 0;
        // Get the row key
        // For each key in the known library
        for (int j : FENKey) {
            // Set this key as the highest ([0] = 0)
            rowKey = j;
            // Whenever it happens to be larger, then we are at the highest row in the board
            if (suggestedSpace >= j) break; // Also equal, since it can be on the left side of the board
        }
        return rowKey;
    }

    // Prints debug information about number of squares to the edge of the board given a startSpot
    // [0] [4] [1]
    // [7] [X] [5]
    // [3] [6] [2]
    static void debugNumToEdge(int startSpot) {
        System.out.println("Up from " + startSpot + ": " + numSquaresToEdge(startSpot, 4));
        System.out.println("Up-Right from " + startSpot + ": " + numSquaresToEdge(startSpot, 1));
        System.out.println("Right from " + startSpot + ": " + numSquaresToEdge(startSpot, 5));
        System.out.println("Down-Right from " + startSpot + ": " + numSquaresToEdge(startSpot, 2));
        System.out.println("Down from " + startSpot + ": " + numSquaresToEdge(startSpot, 6));
        System.out.println("Down-Left from " + startSpot + ": " + numSquaresToEdge(startSpot, 3));
        System.out.println("Left from " + startSpot + ": " + numSquaresToEdge(startSpot, 7));
        System.out.println("Up-Left from " + startSpot + ": " + numSquaresToEdge(startSpot, 0));
    }

    // Converts board state input (such as "a5") to an index
    static int convertInputToIndex(String input) {
        char[] arr = input.toCharArray();
        if (arr.length != 2)
            return -1;
        // arr[0] is the letter, convert it down from ascii
        int letterVal = arr[0] - 96 - 1;
        int numberVal = (arr[1] - 49) * 8;
        int combinedVal = letterVal + numberVal;
        if (combinedVal < 0 || combinedVal > 63) {
            System.out.println("Something went wrong converting the value " + input);
            return -1;
        }
        return combinedVal;
    }

    // Checks to see if an over-arching move is in one of the lists
    static boolean moveIsInList(Move move, boolean isWhite) {
        // The move in this context searches through the deepness of the move, checking its overall final position
        ArrayList<Move> possibleMoves = new ArrayList<>();
        for (Move listMove : isWhite ? whiteMoves : blackMoves) {
            if (listMove.startSpot == move.startSpot)
                possibleMoves.add(listMove);
        }
        // After searching through all of the start spots, check to see if the end spots of those embedded moves works
        if (possibleMoves.size() != 0) {
            for (Move possibleMove : possibleMoves) {
                int endSpotInMove = endSpotInMove(possibleMove);
                Move finalMove = new Move(possibleMove.startSpot, endSpotInMove);
                boolean moveIsInList = finalMove.startSpot == move.startSpot && finalMove.endSpot == move.endSpot;
                if (moveIsInList) return true;
            }
        }
        return false;
    }

    // Recursively searches through a move and returns the final
    static int endSpotInMove(Move move) {
        // No more moves left in the series
        if (move.embeddedMove == null) return move.endSpot;
        // If it is not castling (ie, move series involves different pieces)
        if (move.embeddedMove.isSamePiece)
            return endSpotInMove(move.embeddedMove.embeddedMove);
        return move.endSpot;
    }

    //Standard java inherited method override
    static public String boardString() {
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
                out.append("| ").append(spots[63 - j - i * 8]).append(" ");
            //So that when the row is over, we can close it and make a new line
            out.append("|\n");
        }
        out.append(letterString);
        out.append(borderString);
        return out.toString();
    }

    static void showBoard() {
        System.out.println(boardString());
    }

    // Preforms a move on the board and stores the information about what happened
    static void makeMove(Move move, boolean isRoot) {
        // Record if there was a piece that was taken with the move data
        Piece takenPiece = spots[move.endSpot].spotPiece;
        lastMoveRecords.add(new LastMoveRecord(move, takenPiece, isRoot));
        // Move the attacking piece into the end spot of the move
        Piece attackingPiece = spots[move.startSpot].spotPiece;
        spots[move.startSpot].spotPiece = null;
        spots[move.endSpot].spotPiece = attackingPiece;
        // Recall this for embedded moves
        if (move.embeddedMove != null) makeMove(move.embeddedMove.embeddedMove, false);
    }

    // Unmakes and reverses the results of the last preformed move
    static void unmakeMove() {
        // Remove the last move that occurred
        LastMoveRecord lastMoveRecord = lastMoveRecords.remove(lastMoveRecords.size() - 1);
        // Reverse the movement of the attacking piece
        spots[lastMoveRecord.move.startSpot].spotPiece = spots[lastMoveRecord.move.endSpot].spotPiece;
        // Replace the attacked piece
        spots[lastMoveRecord.move.endSpot].spotPiece = lastMoveRecord.takenPiece;
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