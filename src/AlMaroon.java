import java.util.ArrayList;
import java.util.Collections;

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
        // get gmgame move
        // if the move is not null
        //      return the move
        SearchDTO bestResult = searchAlphaBeta(minimaxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
        Board.aiIsActing = false;
        return bestResult.move;
    }

    //get gm game move
    // if the current history of game moves (lastmove record) matches a record of a previous game
    // return that next move
    // otherwise return null

    SearchDTO searchAlphaBeta(int depth, int alpha, int beta) {
        if (depth == 0)
            return new SearchDTO(null, Scorer.scoreBoard());

        ArrayList<Move> moves = orderedMoves(MoveCoordinator.generateLegalMoves());
        if (moves.isEmpty()) { // No possible moves for this player, either checkmate or stalemate
            if(Board.playerInCheck()) // Checkmate
                return new SearchDTO(null, Integer.MIN_VALUE);
            return new SearchDTO(null, 0); // Stalemate
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

            if(bestMove == null){
                bestMove = move;
            }

            if (eval.eval >= beta) { // Opponent will avoid this, move was too good
                return new SearchDTO(move, beta); // Snip the branch
            }

            if (eval.eval >= alpha) {
                bestMove = move;
                alpha = eval.eval;
            }
        }

        return new SearchDTO(bestMove, alpha);
    }

    // Orders the list so initial pruning is more effective and efficient
    // This is based on predicting the future score of a move based on certain aspects such as promotion
    ArrayList<Move> orderedMoves(ArrayList<Move> legalMoves) {
        // Give all moves a move value
        for (Move move : legalMoves) {
            Piece startPiece = Board.getTokenAtSpot(move.startSpot);
            Piece endPiece = Board.getTokenAtSpot(move.endSpot);
            move.scoreOfMove = 0; // Initialize score for this move, just in case

            // If the move captures a piece
            if (endPiece != null) {
                // Set the score to be 10 times the captured piece - the piece that captured it
                // This should make pawns capturing queens a better score
                move.scoreOfMove = 10 * endPiece.getPieceValue() - startPiece.getPieceValue();
            }

            // Promoting a pawn is good
            if(Board.doesPromote(move, startPiece)){
                // Value increase would be the score of the queen minus the pawn, which is usually the obvious choice
                // Technically, this value is always 800
                move.scoreOfMove += Piece.getPieceValue(Piece.Type.Queen) - startPiece.getPieceValue();
            }

            // Don't want moves where the move ends in a spot that is covered by a pawn (obviously the pawn would take it)
            if (MoveCoordinator.spotIsNotCoveredByEnemyPiece(move.endSpot, false,
                    MoveCoordinator.generateAttackingPawnMoveSpots(false))) {
                move.scoreOfMove -= startPiece.getPieceValue();
            }
        }

        legalMoves.sort(Collections.reverseOrder());

        return legalMoves;
    }

    //SearchDTO searchMinimax(int depth){
    //    if(depth == 0)
    //        return new SearchDTO(null, Scorer.scoreBoard());
//
    //    ArrayList<Move> moves = MoveCoordinator.generateLegalMoves();
    //    if(moves.isEmpty()){ // No possible moves for this player
    //        if(Board.playerInCheck()) // Is in check, which you never want
    //            return new SearchDTO(null, -9999999);
    //        return new SearchDTO(null, 0); // Or it is a stalemate
    //    }
//
    //    int bestEval = -9999999;
    //    Move bestMove = null;
//
    //    for(Move move:moves){
    //        // Make the move then change the turn
    //        Board.makeMove(move);
    //        Board.changeTurns();
    //        // Repeat on the next turn
    //        SearchDTO eval = searchMinimax(depth-1).minus();
    //        // After it is done change the turn back and determine the best values
    //        Board.changeTurns();
    //        Board.unmakeMove();
    //        if(eval.eval < bestEval){ // Check if it is better
    //            bestEval = eval.eval;
    //            bestMove = eval.move;
    //        }
    //    }
//
    //    return new SearchDTO(bestMove, bestEval);
    //}
}
