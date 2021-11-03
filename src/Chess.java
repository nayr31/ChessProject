class Chess {

    Board board;

    Chess(){
        board = new Board();
        board.showBoard();

        while(true){
            System.out.println("Please input the square");
            String s = InputGetter.getInput();
            System.out.println("Decoded value: " + board.convertInputToIndex(s));
        }
    }

    public static void main(String[] args) {
        Chess c = new Chess();
    }
}

