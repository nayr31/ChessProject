class Chess {

    TerminalControl terminalControl = new TerminalControl();
    //BoardWindow boardWindow = new BoardWindow();
    AlMaroon al = new AlMaroon();

    Chess(){ //TODO Add optional choice for game or debug
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
                    "WARNING: NO error checks on this string!");
            Board.popFromFEN(inputString);
            TerminalControl.sendStatusMessage("Loaded FEN string from input.");
        }

        //TODO Add PvP

        // AI information
        AlMaroon.minimaxDepth = InputGetter.askForInt("How deep should the AI look?");
        TerminalControl.sendStatusMessage("Al Maroon will search " + AlMaroon.minimaxDepth + " layers deep.");

        // Looping gameplay
        while(Board.gameWillContinue){
            //TODO write in a "last move made" into the move area on the board window
            // To accomplish this, make sure both AI and player input return a move, then act on that move
            //Move superMove = null;
            if (Board.isWhiteTurn){ // Player turn
                TerminalControl.refreshBoard();
                //TODO Finish player input, including options for FEN output at any turn
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
        while(true){ //TODO Maybe make a class that handles console commands?
            TerminalControl.refreshBoard();
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
                default -> TerminalControl.sendStatusMessage("Unknown command, try again.");
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

