import java.util.ArrayList;

// Contains all methods for generating Moves for pieces
public class MoveCoordinator {
    static int[] FENKey = {56, 48, 40, 32, 24, 16, 8, 0};

    // Generate a list of all moves that each piece can preform, following certain conditions:
    // 1 - Is the correct color of whomever turn it is
    // 2 - Different pieces need different methods to determine which moves are possible
    static ArrayList<Move> getGeneralPieceMoves(boolean isWhite) {
        ArrayList<Move> moves = new ArrayList<>();
        Spot[] spots = Board.getSpots();

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
        Spot[] spots = Board.getSpots();
        int kingSpot = getKingSpot(isWhite);
        if (kingSpot == -1) return new ArrayList<Move>();
        else return generateKingMoves(kingSpot, spots[kingSpot].spotPiece);
    }

    static boolean kingIsInCheck(boolean isWhite) {
        int kingSpot = getKingSpot(isWhite);
        return !spotIsNotCoveredByEnemyPiece(kingSpot, isWhite);
    }

    static boolean kingIsNotInCheck(boolean isWhite){
        return !kingIsInCheck(isWhite);
    }

    static int getKingSpot(boolean isWhite) {
        Spot[] spots = Board.getSpots();
        for (int startSpot = 0; startSpot < 64; startSpot++) {
            Piece token = spots[startSpot].spotPiece;
            if (token != null)
                if (token.pieceType == Piece.Type.King && token.isWhite == isWhite)
                    return startSpot;
        }
        return -1;
    }

    // Long range sliding pieces (Bishop, Queen and Rook)
    static ArrayList<Move> generateSlidingMoves(int startSpot, Piece token) {
        Spot[] spots = Board.getSpots();
        ArrayList<Move> slidingMoves = new ArrayList<>();
        // Set the start index to start at 0 for our bishop (only diagonals), so it ends at 4
        // And the end index to 8 if for our Rook, since it starts at 4
        // More on the reason behind this in the comments for @directionCorrection
        int startIndex, endIndex;
        if (token.pieceType == Piece.Type.Queen) {
            startIndex = 0;
            endIndex = 8;
        } else { // Still could be prettier
            startIndex = token.pieceType == Piece.Type.Bishop ? 0 : 4;
            endIndex = token.pieceType == Piece.Type.Rook ? 8 : 4;
        }

        // For each direction
        for (int dir = startIndex; dir < endIndex; dir++) {
            // For each square in between the start and the edge of the board
            for (int square = 0; square < numSquaresToEdge(startSpot, dir); square++) {
                // Set the target spot of the piece to be:
                // - The start
                // - Plus the direction correction * (square+1)
                // This means that the moves generated start in the direction that we want, then as many times as it
                //  takes to get to the edge of the board, having a "move" for each square along the way
                int targetSpot = startSpot + Director.directionCorrection(dir) * (square + 1);
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
        for (int dir = 0; dir < 8; dir++) {
            int suggestedEndSpot = knightDirectionIsValid(startSpot, dir);
            if (suggestedEndSpot != -1 && Board.getTokenAtSpot(suggestedEndSpot).isWhite != token.isWhite){
                knightMoves.add(new Move(startSpot, suggestedEndSpot));
            }
        }
        return knightMoves;
    }

    // Pawn only goes forward, direction depending on color
    static ArrayList<Move> generatePawnMoves(int startSpot, Piece token) {
        Spot[] spots = Board.getSpots();
        ArrayList<Move> pawnMoves = new ArrayList<>();
        // Determine how far the pawn can move depending on how far it has moved
        int spacesToMove = 1;
        if (!token.hasMoved())
            spacesToMove++;

        // For each of the possible move distances
        // This is just for moving forward, not taking pieces
        for (int i = 0; i < spacesToMove; i++) {
            int targetSpot = startSpot + Director.pawnDirectionCorrection(token.isWhite) * (i+1);
            // Check to see if the target spot is within bounds
            if (targetSpot < 63 && targetSpot > 0) {
                // Target spot is within bounds
                Piece target = spots[targetSpot].spotPiece;

                // Space is occupied, cannot move forward
                if (target != null) break;
                // Otherwise, it is a possible move
                pawnMoves.add(new Move(startSpot, targetSpot));
            } else break; // Break in both instances
        }

        pawnMoves.addAll(pawnAttackingMoves(startSpot, token));
        return pawnMoves;
    }

    static ArrayList<Move> pawnAttackingMoves(int startSpot, Piece token){
        ArrayList<Move> moves = new ArrayList<>();
        // Target taking
        // Get the target directions of the piece depending on the color
        int[] targetDir = Director.pawnAttackDirectionCorrection(token.isWhite);
        // PassantDir relates to the left-right relation of the targetDir
        int[] passantDir = {7, 5};
        // For each targeting direction
        for (int i = 0; i < targetDir.length; i++) {
            if (numSquaresToEdge(startSpot, targetDir[i]) >= 1) {
                int targetSpot = startSpot + Director.directionCorrection(targetDir[i]); // Board wise value of the target direction

                // Diagonal target in the pawns relative attack vectors
                Piece target = Board.spots[targetSpot].spotPiece;

                // Add the move if it is an enemy piece
                if (target != null) {
                    if (!token.isFriendly(target))
                        moves.add(new Move(startSpot, targetSpot));
                } else { // If the space is empty, we may be able to passant
                    // We would normally check to see if there are enough spaces to the right or left, but diagonal ensures both
                    // Make a new passantTargetSpot where the pawn would be that it needs to take
                    int passantTargetSpot = startSpot + Director.directionCorrection(passantDir[i]);
                    target = Board.spots[passantTargetSpot].spotPiece;
                    if (target != null) { // There is a piece at the new targetSpot
                        // Check if it is an enemy pawn that move delta is 16 (moved 2 spaces)
                        Move targetLastMove = target.getLastMove();
                        if(targetLastMove != null){ // If the target hasn't moved yet, then they can't be passant-d
                            if (target.pieceType == Piece.Type.Pawn
                                    && !token.isFriendly(target)
                                    && targetLastMove.moveDelta() == 16) {
                                // Add the move, but make the spot that dies the enemy pawn
                                // So it would technically move into the pawn, then towards its final destination
                                moves.add(new Move(startSpot, passantTargetSpot, new Move(passantTargetSpot, targetSpot)));
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    // Generates a list of moves for pawns on all of their attacking spots
    static ArrayList<Move> generateAttackingPawnMoveSpots(boolean isWhite) { // The passed isWhite value is the enemy's
        ArrayList<Move> pawnMoves = new ArrayList<>();
        Spot[] spots = Board.getSpots();
        int[] targetDir = Director.pawnAttackDirectionCorrection(!isWhite);

        for (int startSpot = 0; startSpot < spots.length; startSpot++) {
            Piece token = spots[startSpot].spotPiece;
            // If the piece selected is a pawn
            if (token != null) {
                if (token.pieceType == Piece.Type.Pawn) {
                    // Check it's attacking directions for a valid move
                    for (int i = 0; i < targetDir.length; i++) {
                        if (numSquaresToEdge(startSpot, targetDir[i]) >= 1) {
                            // Add the attacking move spot
                            int targetSpot = startSpot + Director.directionCorrection(targetDir[i]);
                            pawnMoves.add(new Move(startSpot, targetSpot));
                        }
                    }
                }
            }
        }
        return pawnMoves;
    }

    // King can only go once in each direction, but also can't go where there are other colored moves present
    static ArrayList<Move> generateKingMoves(int startSpot, Piece token) {
        ArrayList<Move> kingMoves = new ArrayList<>(kingMoveMoves(startSpot, token));
        kingMoves.addAll(kingCastleMoves(startSpot, token));
        return kingMoves;
    }

    static ArrayList<Move> kingMoveMoves(int startSpot, Piece token){
        ArrayList<Move> kingMoves = new ArrayList<>();
        // For each move direction
        // Just moves, castling is done afterwards
        for (int i = 0; i < 8; i++) {
            // If there is a spot in that direction
            if (numSquaresToEdge(startSpot, i) >= 1) {
                int targetSpot = startSpot + Director.directionCorrection(i);

                Piece target = Board.spots[targetSpot].spotPiece;
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
        return kingMoves;
    }

    static ArrayList<Move> kingCastleMoves(int startSpot, Piece token){
        ArrayList<Move> kingMoves = new ArrayList<>();
        // Castling has rules:
        //  1 - King moves two spaces towards king/queen side
        //  2 - Rook goes on other side of king
        //  3 - Can't be made when king is in check
        //  4 - Can't move through squares that are covered by enemy moves
        //  5 - Can't castle if king has already moved
        //  6 - Can't castle if rook has already moved
        if (!token.hasMoved()) { // (5)
            if (spotIsNotCoveredByEnemyPiece(startSpot, token.isWhite)) { // (3)
                // (1,2,4,6)
                if (token.isWhite ? Board.CanCastleWhiteQueen : Board.CanCastleBlackQueen) {
                    Move move = makeCastle(token.isWhite, startSpot, false);
                    if (move != null)
                        kingMoves.add(move);
                }
                if (token.isWhite ? Board.CanCastleWhiteKing : Board.CanCastleBlackKing) {
                    Move move = makeCastle(token.isWhite, startSpot, true);
                    if (move != null)
                        kingMoves.add(move);
                }
            }
        }
        return kingMoves;
    }

    // Returns the general vicinity of "legal" king moves
    static ArrayList<Move> getGeneralKingMoves(int kingSpot, boolean isWhite) {
        Spot[] spots = Board.getSpots();
        ArrayList<Move> kingMoves = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            int dirVal = Director.directionCorrection(i);
            if (numSquaresToEdge(kingSpot, i) >= 1) {
                int targetSpot = kingSpot + Director.directionCorrection(i);
                if (spots[targetSpot].spotPiece == null)
                    kingMoves.add(new Move(kingSpot, targetSpot));
            }
        }
        return kingMoves;
    }

    // Checks for blocking attack spaces while returning a valid move if one exists
    static Move makeCastle(boolean isWhite, int startSpot, boolean isKingSide) {
        // Get the direction needed for each side - left is 7 right is 5
        int dir = kingCastleDirectionCorrection(isKingSide);
        // Check if there are two spaces free to castle on
        boolean spacesTowardsRookAreFree = castleSpacesAreFree(startSpot, dir, isWhite);
        // And if there is actually a piece there (should always be, but just to be safe)
        int castleSpot = getCastleSpot(isWhite, isKingSide);
        boolean castleIsPresent = Board.getTokenAtSpot(castleSpot) != null;
        if (spacesTowardsRookAreFree && castleIsPresent) {
            // Get the end spot where the king will sit afterwards
            int targetSpot = startSpot + Director.directionCorrection(dir) * 2;
            Move rookMove = new Move(castleSpot, targetSpot - Director.directionCorrection(dir));
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
        Spot[] spots = Board.getSpots();
        int spaceDirOffset = Director.directionCorrection(dir);
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

    static ArrayList<Move> getAttackingPiecesOnSpot(int spot, boolean isWhite, ArrayList<Move> enemyMoveList) {
        ArrayList<Move> attackers = new ArrayList<>();
        // Gather all attacking moves on that spot
        if (enemyMoveList == null) {
            enemyMoveList = cullPawnMoves(getGeneralPieceMoves(!isWhite));
            enemyMoveList.addAll(generateAttackingPawnMoveSpots(isWhite));
        }

        for (Move move : enemyMoveList) {
            if (move.endSpot == spot)
                attackers.add(move);
        }
        return attackers;
    }

    static boolean spotIsNotCoveredByEnemyPiece(int spot, boolean isWhite, ArrayList<Move> enemyMoveList) {
        Spot[] spots = Board.getSpots();
        class Attacker {
            public Attacker(Move move, Piece piece) {
                this.move = move;
                this.piece = piece;
            }

            final Move move;
            final Piece piece;
        }
        ArrayList<Attacker> attackers = new ArrayList<>();
        // Determine which enemy list to search through
        if (enemyMoveList == null)
            enemyMoveList = getGeneralPieceMoves(!isWhite);

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

    // Checks to see if a known enemy move is covers a spot with a move
    static boolean spotIsNotCoveredByEnemyPiece(int spot, boolean isWhite) {
        return spotIsNotCoveredByEnemyPiece(spot, isWhite, null);
    }

    // Returns the amount of squares that are in between a piece's spot and the edge of the board
    // Assumes the same directions as the ones from the sliding matrix
    // [0] [4] [1]
    // [7] [X] [5]
    // [3] [6] [2]
    static int numSquaresToEdge(int startSpot, int dir) {
        int directionalOffset = Director.directionCorrection(dir);//No need to calculate this multiple times
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
                if (suggestedSpace == rowKey + 7) break;
            }

            // No conditions found to stop, increase the amount of moves possible and set the new rowkey
            numSpaces++;
            rowKey = generateLocalRowKey(suggestedSpace);
        }

        return numSpaces;
    }

    //  [ ]  [0]  [ ]  [1]  [ ]
    //  [7]  [ ]  [ ]  [ ]  [2]
    //  [ ]  [ ]  [X]  [ ]  [ ]
    //  [6]  [ ]  [ ]  [ ]  [3]
    //  [ ]  [5]  [ ]  [4]  [ ]
    static int knightDirectionIsValid(int startSpot, int dir) {
        int directionalOffset = Director.directionConversionKnight(dir);
        int suggestedSpot = startSpot + directionalOffset;

        // Going up above the board
        if (dir == 0 || dir == 1 || dir == 2 || dir == 7) {
            if (suggestedSpot > 63)
                return -1;
        }
        // Going below the board
        else if (dir == 3 || dir == 4 || dir == 5 || dir == 6) {
            if (suggestedSpot < 0)
                return -1;
        }

        // We are in bounds vertically, now we check horizontal positioning
        // This would assume that each rowKey call is in bounds

        // This value is the left-most spot in the current start square
        //int rowKey = generateLocalRowKey(startSpot);
        int keyRowKey;

        // Depending on the direction, we check the suggested space compared to the rowKeys
        if (dir == 7 || dir == 2 || dir == 0 || dir == 1) {

            if (dir == 7 || dir == 2) {
                keyRowKey = generateLocalRowKey(startSpot + 8);
            } else {
                keyRowKey = generateLocalRowKey(startSpot + 16);
            }
            // Going left, it would decrease. We want it to be equal to or higher than the next rowKey
            if (dir == 7 || dir == 0) {
                if (suggestedSpot < keyRowKey)
                    return -1;
            }
            // Otherwise going right, we would get it to be more than 8 on the next next row
            else {
                if (suggestedSpot >= keyRowKey + 8)
                    return -1;
            }
        } else if (dir == 3 || dir == 6 || dir == 4 || dir == 5) {
            if (dir == 3 || dir  == 6)
                keyRowKey = generateLocalRowKey(startSpot - 8);
            else
                keyRowKey = generateLocalRowKey(startSpot - 16);
            // Going left,
            if(dir == 5 || dir == 6){
                if(suggestedSpot < keyRowKey)
                    return -1;
            }
            // Going right
            else { // dir == 4 || dir == 3
                if(suggestedSpot == keyRowKey + 8)
                    return -1;
                if(dir == 3){
                    if(suggestedSpot == keyRowKey + 9)
                        return -1;
                    if(suggestedSpot == 0)
                        return -1;
                }
            }
        }

        // Otherwise, the point is valid
        return suggestedSpot;
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

    // Checks to see if an over-arching move is in one of the lists
    static boolean moveIsInList(Move move, boolean isWhite, ArrayList<Move> whiteMoves, ArrayList<Move> blackMoves) {
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

    static ArrayList<Move> cullPawnMoves(ArrayList<Move> moves) {
        ArrayList<Move> hoopla = new ArrayList<>();
        Spot[] spots = Board.getSpots();
        for (Move move : moves) {
            Piece token = spots[move.startSpot].spotPiece;
            if (token.pieceType != Piece.Type.Pawn)
                hoopla.add(move);
        }
        return hoopla;
    }

    // Another method in case we want a specific color move list
    public static ArrayList<Move> generateLegalMoves() {
        return generateLegalMoves(Board.isWhiteTurn);
    }


    public static ArrayList<Move> generateLegalMoves(boolean isWhite) {
        TerminalControl.sendStatusMessage("Generating legal moves...");
        Board.aiIsActing = true;

        ArrayList<Move> moves = new ArrayList<>(getGeneralPieceMoves(isWhite));
        moves.addAll(getKingMoves(isWhite));

        ArrayList<Move> legalMoves = new ArrayList<>();
        for (Move move:moves) {
            Board.makeMove(move);
            if(!Board.playerInCheck(isWhite))
                legalMoves.add(move);
            Board.unmakeMove();
        }

        TerminalControl.sendStatusMessage("Finished generating legal moves.");
        Board.aiIsActing = false;
        return legalMoves;
    }
}
