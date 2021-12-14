import java.util.ArrayList;
public class Piece {
    enum Type {
        Pawn,
        Rook,
        Knight,
        Queen,
        King,
        Bishop,
        None
    }

    Type pieceType;
    boolean isWhite;
    ArrayList<Move> lastMoves = new ArrayList<>();
    String name;

    Piece(char c, boolean isWhite){
        name = isWhite ? "White " : "Black ";
        pieceType = charToType(c);
        this.isWhite = isWhite;
    }

    static Type charToType(char c){
        return switch (Character.toLowerCase(c)) {
            case 'r' -> Type.Rook;
            case 'n' -> Type.Knight;
            case 'p' -> Type.Pawn;
            case 'b' -> Type.Bishop;
            case 'q' -> Type.Queen;
            case 'k' -> Type.King;
            default -> Type.None;
        };
    }
    
    /// Constructor for piece
    // Inits with provided types
    Piece(Type pieceType, boolean isWhite){
        this.pieceType = pieceType;
        this.isWhite = isWhite;
    }

    static boolean isSlidingType(Piece.Type type){
        return switch (type) {
            case Bishop, Queen, Rook -> true;
            default -> false;
        };
    }

    // Long range sliding pieces (Bishop, Queen and Rook)
    boolean isSlidingType(){
        return isSlidingType(pieceType);
    }

    // Returns if another piece is friendly to this token
    boolean isFriendly(Piece otherToken){
        // The compare function returns 0 is the booleans are the same
        return Boolean.compare(this.isWhite, otherToken.isWhite) == 0;
    }

    boolean hasMoved(){
        return lastMoves.size() > 0;
    }

    void addDummyMove(int spot){
        lastMoves.add(new Move(spot, spot));
    }

    // Returns the literal interpretation of a piece value without multipliers
    // P = 100
    // N = 320
    // B = 330
    // R = 500
    // Q = 900
    // K = 20000
    int getPieceValue(){
        return getPieceValue(pieceType);
    }

    static int getPieceValue(Piece.Type type){
        return switch(type){
            case Bishop -> 330;
            case King -> 20000;
            case Knight -> 320;
            case Pawn -> 100;
            case Queen -> 900;
            case Rook -> 500;
            case None -> -1;
        };
    }

    //Maybe less efficient than setting the spot to just store the value?
    public String toString(){
        switch(pieceType){
            case Bishop:
                return isWhite ? "B" : "b";
            case King:
                return isWhite ? "K" : "k";
            case Knight:
                return isWhite ? "N" : "n";
            case Pawn:
                return isWhite ? "P" : "p";
            case Queen:
                return isWhite ? "Q" : "q";
            case Rook:
                return isWhite ? "R" : "r";
            case None:
                return " ";
            default:
                break;
        }
        return "Error";
    }

    Move getLastMove(){
        if(lastMoves.size() != 0)
            return lastMoves.get(lastMoves.size()-1);
        return null;
    }

    void changeLastMove(Move move){
        forget();
        lastMoves.add(move);
    }

    void forget(){
        lastMoves.remove(lastMoves.size()-1);
    }
}
