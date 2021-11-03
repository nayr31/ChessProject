class Chess {

    Board board;

    Chess(){
        board = new Board();
        board.showBoard();
        //board.showDebugVals();
        board.debugNumToEdge(0);
    }

    public static void main(String[] args) {
        Chess c = new Chess();
    }
}

