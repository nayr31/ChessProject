public class Spot {
    Piece spotPiece;

    Spot(){
        spotPiece = null;
    }

    public String toString(){
        return spotPiece != null ? spotPiece.toString() : " ";
    }
}
