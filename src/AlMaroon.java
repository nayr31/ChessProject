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
        SearchDTO minus(){// Switches per turn
            return new SearchDTO(this.move, -this.eval);
        }
    }

    Move think(){
        SearchDTO bestResult = searchAlphaBeta(minimaxDepth, -1, -1);
        //TODO Double check initial alpha beta values
        return bestResult.move;
    }

    SearchDTO searchMinimax(int depth){
        if(depth == 0)
            return new SearchDTO(null, Scorer.scoreBoard());

        ArrayList<Move> moves = MoveCoordinator.generateLegalMoves();
        if(moves.isEmpty()){ // No possible moves for this player
            if(Board.playerInCheck()) // Is in check, which you never want
                return new SearchDTO(null, -9999999);
            return new SearchDTO(null, 0); // Or it is a stalemate
        }

        int bestEval = -9999999;
        Move bestMove = null;

        for(Move move:moves){
            // Make the move then change the turn
            Board.makeMove(move);
            Board.changeTurns();
            // Repeat on the next turn
            SearchDTO eval = searchMinimax(depth-1).minus();
            // After it is done change the turn back and determine the best values
            Board.changeTurns();
            Board.unmakeMove();
            if(eval.eval < bestEval){ // Check if it is better
                bestEval = eval.eval;
                bestMove = eval.move;
            }
        }

        return new SearchDTO(bestMove, bestEval);
    }

    SearchDTO searchAlphaBeta(int depth, int alpha, int beta){
        if(depth == 0)
            return new SearchDTO(null, Scorer.scoreBoard());

        ArrayList<Move> moves = MoveCoordinator.generateLegalMoves();
        if(moves.isEmpty()){ // No possible moves for this player, either checkmate or stalemate
            return new SearchDTO(null, -9999999);
        }

        Move bestMove = null;

        for(Move move:moves){
            // Make the move then change the turn
            Board.makeMove(move);
            Board.changeTurns();
            // Repeat on the next turn
            SearchDTO eval = searchAlphaBeta(depth-1, -beta, - alpha).minus();
            // After it is done change the turn back and determine the best values
            Board.unmakeMove();
            Board.changeTurns();
            if(eval.eval >= beta){ // Opponent will avoid this
                return new SearchDTO(eval.move, beta);
            }
            //alpha = Math.max(alpha, eval.eval);
            if(eval.eval > alpha){
                bestMove = eval.move;
                alpha = eval.eval;
            }//TODO double check this to make sure all bases are covered
        }

        return new SearchDTO(bestMove, alpha);
    }
}
