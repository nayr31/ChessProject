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
    }

    void think(){
        SearchDTO searchDTO = search(minimaxDepth);
    }

    SearchDTO search(int depth){
        if(depth == 0)
            return new SearchDTO(null, Scorer.scoreBoard());

        ArrayList<Move> moves = MoveCoordinator.generateLegalMoves();
        if(moves.isEmpty()){ // No possible moves for this player, either checkmate or stalemate
            return new SearchDTO(null, -9999999);
        }

        int bestEval = -9999999;
        Move bestMove = null;

        for(Move move:moves){
            // Make the move then change the turn
            Board.makeMove(move);
            Board.changeTurns();
            // Repeat on the next turn
            SearchDTO eval = search(depth-1);
            eval.eval *= -1; // Switches per turn
            // After it is done change the turn back and determine the best values
            Board.changeTurns();
            if(eval.eval < bestEval){
                bestEval = eval.eval;
                bestMove = move;
            }
            Board.unmakeMove();
        }

        return new SearchDTO(bestMove, bestEval);
    }
}
