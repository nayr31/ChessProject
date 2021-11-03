class Chess {

    Board board;

    Chess(){
        board = new Board();
        board.showBoard();
    }

    public static void main(String[] args) {
        Chess c = new Chess();
    }
}

