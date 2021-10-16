class Chess {

    Board board;

    Chess(){
        board = new Board();
        board.showBoard();
        board.showDebugVals();
    }

    public static void main(String[] args) {
        Chess c = new Chess();
    }
}

