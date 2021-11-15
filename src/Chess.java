class Chess {

    TerminalControl terminalControl = new TerminalControl();
    BoardWindow boardWindow = new BoardWindow();
    AlMaroon al = new AlMaroon();

    Chess(){
        // Initialize program
        Board.initiate();
        Scorer.initiate();

        // Get the board configuration
        int inputNum = InputGetter.askForType("Please select an option:\n" +
                "[0] - Normal configuration\n" +
                "[1] - Input FEN string", 1);
        if(inputNum == 0)
            Board.popNormal();
        else if(inputNum == 1){
            String inputString = InputGetter.askForString("Input the FEN string now.\n" +
                    "WARNING: MO error checks on this string!");
            Board.popFromFEN(inputString);
            TerminalControl.sendStatusMessage("Loaded FEN string from input.");
        }

        // AI information
        AlMaroon.minimaxDepth = InputGetter.askForInt("How deep should the AI look?");
        TerminalControl.sendStatusMessage("Al Maroon will search " + AlMaroon.minimaxDepth + " layers deep.");

        // Looping gameplay
        while(Board.gameWillContinue){
            boardWindow.updateTurnDisplay();
            if (Board.isWhiteTurn){
                boardWindow.showBoard();
                //TODO Finish player input
            }
            else{
                TerminalControl.sendStatusMessage("Al Maroon is thinking...");
                al.think();
            }
        }
    }

    public static void main(String[] args) {
        Chess c = new Chess();
    }
}

