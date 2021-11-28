class Chess {

    TerminalControl terminalControl = new TerminalControl();
    BoardWindow boardWindow = new BoardWindow();
    AlMaroon al = new AlMaroon();

    Chess(){
        //game();
        debug();
    }

    private void game(){
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

        //TODO Add PvP

        // AI information
        AlMaroon.minimaxDepth = InputGetter.askForInt("How deep should the AI look?");
        TerminalControl.sendStatusMessage("Al Maroon will search " + AlMaroon.minimaxDepth + " layers deep.");

        // Looping gameplay
        while(Board.gameWillContinue){
            boardWindow.updateTurnDisplay();
            if (Board.isWhiteTurn){ // Player turn
                boardWindow.showBoard();
                //TODO Finish player input
            }
            else{
                TerminalControl.sendStatusMessage("Al Maroon is thinking...");
                al.think();
            }
        }
    }

    private void debug(){
        String debugOptions = "help - This menu\n" +
                "rb - Refresh board\n" +
                "im \"a1-a2\" - Input move, without \"\n" +
                "ap \"Ka1\" - Add a piece to the board (FEN style piece)\n" +
                "exit - Close";

        String input = "";
        while(true){
            input = InputGetter.askForString("Please input your debug command.\n" +
                    "\"help\" for options.");
            String[] split = input.split(" ");
            switch (split[0]) {
                case "rb" -> TerminalControl.refreshBoard();
                case "help" -> {
                    TerminalControl.sendCommandText(debugOptions);
                    TerminalControl.sendStatusMessage("Press enter to continue...");
                    InputGetter.ask();
                    TerminalControl.sendStatusMessage("");
                }
                case "im" -> InputGetter.debugMoveInput(split[1]);
                case "ap" -> {
                    if(split.length != 2){
                        TerminalControl.sendStatusMessage("Incorrect length of args, make sure you only have 2.");
                    } else{
                        try{
                            char fenChar = split[1].toCharArray()[0];
                            String strLoc = split[1].substring(1);
                            try{
                                int location = Board.convertInputToIndex(strLoc);
                                Board.placeNewPiece(fenChar, location);

                            } catch(NotLocationException e){
                                TerminalControl.sendStatusMessage(e.getMessage());
                                System.out.println(e.getMessage());
                            }
                        } catch (ArrayIndexOutOfBoundsException e){
                            TerminalControl.sendStatusMessage(e.getMessage());
                            System.out.println(e.getMessage());
                        }
                    }
                }
                case "exit" -> System.exit(1);
            }
        }
    }

    public static void main(String[] args) {
        // Initialize program
        Board.initiate();
        Scorer.initiate();
        Chess c = new Chess();
    }
}

