class Chess {

    TerminalControl terminalControl = new TerminalControl();
    AlMaroon al = new AlMaroon();
    static boolean isPVP = false;

    Chess() { //TODO Add optional choice for game or debug
        game();
        //debug();
    }

    private void game() {
        // Get the board configuration
        int inputNum = InputGetter.askForType("Please select an option:\n" +
                "[0] - Normal configuration\n" +
                "[1] - Input FEN string", 1);
        if (inputNum == 0)
            Board.popNormal();
        else if (inputNum == 1) {
            String inputString = InputGetter.askForString("Input the FEN string now.\n" +
                    "WARNING: NO error checks on this string!");
            Board.popFromFEN(inputString);
            TerminalControl.sendStatusMessage("Loaded FEN string from input.");
        }

        if(InputGetter.askForYN("Do you want to play PvP [y/n]?"))
            isPVP = true;
        else{
            // AI information
            AlMaroon.minimaxDepth = InputGetter.askForInt("How deep should the AI look?");
            TerminalControl.sendStatusMessage("Al Maroon will search " + AlMaroon.minimaxDepth + " layers deep.");
        }

        // Looping gameplay
        while (Board.gameWillContinue) {
            Move superMove;
            if (Board.isWhiteTurn) { // Player turn
                superMove = takePlayerTurn();
            } else {
                if (isPVP)
                    superMove = takePlayerTurn();
                else
                    superMove = takeAITurn();
            }
            // After getting the respective player's move, we act on it
            // If it is null, that means that an end-game state was reached
            if (superMove != null) {
                Board.makeMove(superMove);
                TerminalControl.refreshBoard();
                Board.lastPlayerDidNotAct = false;
            } else {
                Board.lastPlayerDidNotAct = true;
            }
            // Always change turns at the end of moving
            Board.changeTurns(true);
            // Then check if the game end
            checkForGameEnd();
        }
    }

    // These rules were derived from this website detailing stalemates
    // "If the are no legal moves for a player, but they are not in check, it is a stalemate."
    // https://www.chessvariants.com/d.chess/matefaq.html
    private void checkForGameEnd() {
        if(Board.halfMoves == 100){
            TerminalControl.sendCommandText("Game has ended from turn limit.");
            Board.gameWillContinue = false;
            TerminalControl.sendStatusMessage("Printing FEN of board...");
            Board.outputToFile();
            return;
        }
        // If the last player did not act
        if (Board.lastPlayerDidNotAct) {
            // That means that they had no legal moves
            boolean whiteInCheck = Board.playerInCheck(true);
            boolean blackInCheck = Board.playerInCheck(false);
            // If a player was in check, then there is a winner
            if (whiteInCheck || blackInCheck) {
                if (blackInCheck)
                    Board.winner = "Black";
                else Board.winner = "White";
                TerminalControl.sendCommandText(Board.winner + " has won.");
            } else{
                if (Board.isStaleMate()) // Otherwise, check for a flat stalemate
                    TerminalControl.sendCommandText("Game ended in flat Stalemate.\n" +
                            "Only kings remain.");
                else{
                    if(MoveCoordinator.generateLegalMoves().size() != 0){
                        System.out.println("Something went wrong parsing the last move.");
                        TerminalControl.sendCommandText("Something went wrong parsing the last move.");
                    } else{
                        TerminalControl.sendCommandText("Game ended in normal stalemate.\n" +
                                "King is not in check, but no moves are possible");
                    }
                }
            }

            Board.gameWillContinue = false;
            TerminalControl.sendStatusMessage("Printing FEN of board...");
            Board.outputToFile();
        }
    }

    private Move takePlayerTurn() {
        TerminalControl.refreshBoard();
        return InputGetter.playerTurnInput();
    }

    private Move takeAITurn() {
        TerminalControl.sendStatusMessage("Al Maroon is thinking...");
        return al.think();
    }

    private void debug() {
        String input = "";
        while (true) {
            TerminalControl.refreshBoard();
            input = InputGetter.askForString("Please input your debug command.\n" +
                    "\"help\" for options.");
            String[] split = input.split(" ");
            switch (split[0]) {
                case "gm" -> {
                    MoveCoordinator.getGeneralPieceMoves(Board.isWhiteTurn);
                    MoveCoordinator.getKingMoves(Board.isWhiteTurn);
                }
                case "help" -> {
                    TerminalControl.toggleHelpWindow();
                }
                case "im" -> InputGetter.debugMoveInput(split[1]);
                case "ap" -> {
                    if (split.length != 2) {
                        TerminalControl.sendStatusMessage("Incorrect length of args, make sure you only have 2.");
                    } else {
                        try {
                            char fenChar = split[1].toCharArray()[0];
                            String strLoc = split[1].substring(1);
                            try {
                                int location = Board.convertInputToIndex(strLoc);
                                Board.placeNewPiece(fenChar, location);
                            } catch (NotLocationException e) {
                                TerminalControl.sendStatusMessage(e.getMessage());
                                System.out.println(e.getMessage());
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
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
        if(!isPVP)
            Scorer.initiate();
        Chess c = new Chess();
    }
}

