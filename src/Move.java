public class Move {
    // This class simply keeps track of which squares are used (designated by an integer in the board's spots array)

    int startSpot; // Start position of the piece
    int endSpot; // End position of the piece
    int scoreOfMove;
    EmbeddedMove embeddedMove = null;
    class EmbeddedMove{
        Move embeddedMove; // This embedded move allows us to recursively call moves when making one
        public boolean isSamePiece = true;

        public EmbeddedMove(Move embeddedMove) {
            this.embeddedMove = embeddedMove;
        }
    }

    Move(int startSpot, int endSpot){
        this(startSpot, endSpot, null);
    }

    Move(int startSpot, int endSpot, Move embeddedMove) {
        this.startSpot = startSpot;
        this.endSpot = endSpot;
        if(embeddedMove != null)
            this.embeddedMove = new EmbeddedMove(embeddedMove);
    }

    Move(int startSpot, int endSpot, Move embeddedMove, boolean isSamePiece){
        this(startSpot, endSpot, embeddedMove);
        this.embeddedMove.isSamePiece = isSamePiece;
    }

    int moveDelta(){
        if(embeddedMove != null)
            return Math.abs(embeddedMove.embeddedMove.endSpot - startSpot);
        return Math.abs(endSpot - startSpot);
    }

    public String toString(){
        return "[" + startSpot + "-" + endSpot + "]";
    }
}
