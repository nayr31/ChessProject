public class Spot {
    Piece spotPiece = null;

    public String toString(){
        return spotPiece != null ? spotPiece.toString() : " ";
    }
}
