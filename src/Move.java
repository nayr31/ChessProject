public class Move {
    // This class simply keeps track of which squares are used (designated by an integer in the board's spots array)

    int startSpot;
    int endSpot;

    Move(int startSpot, int endSpot){
        this.startSpot = startSpot;
        this.endSpot = endSpot;
    }
}
