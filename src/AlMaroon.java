import java.util.ArrayList;

public class AlMaroon {
    // This is the AI, named Al Maroon
    // He always uses the black pieces, and is doesn't know every rule of chess
    static int minimaxDepth = 1;

    class SearchDTO {
        Move move;
        int eval;

        public SearchDTO(Move move, int eval) {
            this.move = move;
            this.eval = eval;
        }

        SearchDTO minus() {// Switches per turn
            return new SearchDTO(this.move, -this.eval);
        }
    }

    Move think() {
        Board.aiIsActing = true;
        SearchDTO bestResult = searchAlphaBeta(minimaxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        Board.aiIsActing = false;
        return bestResult.move;
    }


    SearchDTO searchAlphaBeta(int depth, int alpha, int beta) {
        if (depth == 0)
            return new SearchDTO(null, Scorer.scoreBoard());

        ArrayList<Move> moves = orderedMoves(MoveCoordinator.generateLegalMoves());
        if (moves.isEmpty()) { // No possible moves for this player, either checkmate or stalemate
            return new SearchDTO(null, Integer.MIN_VALUE);
        }

        Move bestMove = null;

        for (Move move : moves) {
            // Make the move then change the turn
            Board.makeMove(move);
            Board.changeTurns(false);
            // Repeat on the next turn
            SearchDTO eval = searchAlphaBeta(depth - 1, -beta, -alpha).minus();
            // After it is done change the turn back and determine the best values
            Board.unmakeMove();
            Board.changeTurns(false);
            if (eval.eval >= beta) { // Opponent will avoid this, move was too good
                return new SearchDTO(eval.move, beta); // Snip the branch
            }
            if (eval.eval >= alpha) {
                bestMove = eval.move;
                alpha = eval.eval;
            }
        }

        return new SearchDTO(bestMove, alpha);
    }

    // Orders the list so initial pruning is more effective and efficient
    ArrayList<Move> orderedMoves(ArrayList<Move> legalMoves) {
        ArrayList<Move> finalMoves = new ArrayList<>();
        for (Move move : legalMoves) {
            Piece startPiece = Board.getTokenAtSpot(move.startSpot);
            Piece endPiece = Board.getTokenAtSpot(move.endSpot);

            // If teh move captures a piece
            if (endPiece != null) {
                // Set the score to be 10 times the captured piece - the piece that captured it
                // This should make pawns capturing queens a better score
                move.scoreOfMove = 10 * endPiece.getPieceValue() - startPiece.getPieceValue();
            }

            // Promoting a pawn is good
            //TODO Finish pawn promotion ordering

            // Don't want moves where the move ends in a spot that is covered by a pawn
            if (MoveCoordinator.spotIsNotCoveredByEnemyPiece(move.endSpot, false, MoveCoordinator.generateAttackingPawnMoveSpots(false))) {
                move.scoreOfMove -= startPiece.getPieceValue();
            }

            //arrange it in the list
        }

        return finalMoves;
    }
}
