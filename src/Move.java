public class Move {
    // This class simply keeps track of which squares are used (designated by an integer in the board's spots array)

    int startSpot; // Start position of the piece
    int endSpot; // End position of the piece
    Move embeddedMove = null; // This embedded move allows us to recursively call moves when making one
    // This is used for castling and passant-ing

    Move(int startSpot, int endSpot){
        this(startSpot, endSpot, null);
    }

    Move(int startSpot, int endSpot, Move embeddedMove) {
        this.startSpot = startSpot;
        this.endSpot = endSpot;
        this.embeddedMove = embeddedMove;
    }

    int moveDelta(){
        return Math.abs(endSpot - startSpot);
    }
}
