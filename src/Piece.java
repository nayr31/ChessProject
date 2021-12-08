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
    };

    Type pieceType;
    boolean isWhite;
    boolean hasMoved = false;
    ArrayList<Move> lastMoves = new ArrayList<>();
    String name;

    Piece(char c, boolean isWhite){
        name = isWhite ? "White " : "Black ";
        pieceType = charToType(c);
        this.isWhite = isWhite;
    }

    static Type charToType(char c){
        switch (Character.toLowerCase(c)) {
            case 'r':
                return Type.Rook;
            case 'n':
                return Type.Knight;
            case 'p':
                return Type.Pawn;
            case 'b':
                return Type.Bishop;
            case 'q':
                return Type.Queen;
            case 'k':
                return Type.King;
            default:
                return Type.None;
        }
    }
    
    /// Constructor for piece
    // Inits with provided types
    Piece(Type pieceType, boolean isWhite){
        this.pieceType = pieceType;
        this.isWhite = isWhite;
    }

    // Long range sliding pieces (Bishop, Queen and Rook)
    boolean isSlidingType(){
        return switch (pieceType) {
            case Bishop, Queen, Rook -> true;
            default -> false;
        };
    }

    // Returns if another piece is friendly to this token
    boolean isFriendly(Piece otherToken){
        // The compare function returns 0 is the booleans are the same
        return Boolean.compare(this.isWhite, otherToken.isWhite) == 0;
    }

    // Returns the literal interpretation of a piece value without multipliers
    // P = 100
    // N = 320
    // B = 330
    // R = 500
    // Q = 900
    // K = 20000
    int getPieceValue(){
        if(pieceType == Piece.Type.Pawn)
            return 100;
        else if(pieceType == Piece.Type.Knight)
            return 320;
        else if(pieceType == Piece.Type.Bishop)
            return 330;
        else if(pieceType == Piece.Type.Rook)
            return 500;
        else if(pieceType == Piece.Type.Queen)
            return 900;
        else if(pieceType == Piece.Type.King)
            return 20000;
        return -1;
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
