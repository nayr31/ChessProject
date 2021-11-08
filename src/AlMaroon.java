import java.util.ArrayList;

public class AlMaroon {
    // This is the AI, named Al Maroon
    // He always uses the black pieces, and is doesn't know every rule of chess
    static int minimaxDepth = 1;

    int search(int depth){
        if(depth == 0)
            return Scorer.scoreBoard();

        ArrayList<Move> moves = MoveCoordinator.generateLegalMoves();
        if(moves.isEmpty()){ // No possible moves for this player, either checkmate or stalemate
            return -9999999;
        }

        int bestEval = -9999999;

        for(Move move:moves){
            Board.makeMove(move);
            Board.changeTurns();
            int eval = -search(depth-1);
            Board.changeTurns();
            bestEval = Math.max(bestEval, eval);
            Board.unmakeMove();
        }

        return bestEval;
    }
}
