import java.util.ArrayList;
import java.util.InputMismatchException;

public class InputGetter {
    static final String fenString = "Type \"FEN\" to export the current board to a file.";

    static Move playerTurnInput(){
        // See if there are valid legal moves to take
        ArrayList<Move> legalMoves = MoveCoordinator.generateLegalMoves();
        if(legalMoves.size()==0){ // No legal moves, this means either stalemate or checkmate
            return null;
        } else{
            return getMoveInput(legalMoves);
        }
    }

    static Move getMoveInput(ArrayList<Move> legalMoves){
        TerminalControl.sendCommandText("Enter your stat and end squares for your move:\n" +
                "\"a1-a2\" would move the piece in a1 to a2. No not use quotes.");
        TerminalControl.sendStatusMessage(fenString);
        String input;
        String[] inputSplit;
        while(true){
            input = getInputFromTerminalControl();
            inputSplit = input.split("-");
            if(input.equals("FEN")){
                Board.outputToFile();
            }else if(inputSplit.length != 2 || inputSplit[0].length() != 2 || inputSplit[1].length() != 2){
                TerminalControl.sendStatusMessage("Wrong input format. Please use \"a1-a2\".\n" +
                        "These represent board spaces, with the start space being the first index.");
            } else{
                try{
                    int startSpot = Board.convertInputToIndex(inputSplit[0]);
                    int endSpot = Board.convertInputToIndex(inputSplit[1]);
                    Move moveIsInList = moveIsInList(new Move(startSpot, endSpot), legalMoves);
                    if(moveIsInList == null){
                        TerminalControl.sendStatusMessage("Move is not possible.");
                    } else{
                        TerminalControl.sendStatusMessage(fenString);
                        TerminalControl.setBoardMessage(Board.getTokenAtSpot(moveIsInList.startSpot).toString() +
                                " to " + inputSplit[1]);
                        return moveIsInList;
                    }
                } catch(NotLocationException e){
                    TerminalControl.sendStatusMessage("Something terrible happened trying to decipher the move!");
                }
            }
        }
    }

    static Move moveIsInList(Move move, ArrayList<Move> moves){
        for(Move refMove : moves){
            if(move.startSpot == refMove.startSpot && move.endSpot == MoveCoordinator.endSpotInMove(refMove))
                return refMove;
        }
        return null;
    }

    static void debugMoveInput(String input){
        String[] inputSplit = input.split("-");
        try {
            int startSpot = Board.convertInputToIndex(inputSplit[0]);
            int endSpot = Board.convertInputToIndex(inputSplit[1]);
            Move suggestedMove = new Move(startSpot, endSpot);
            Spot[] spots = Board.getSpots();
            if(spots[startSpot].spotPiece != null)
                Board.makeMove(suggestedMove);
            else
                TerminalControl.sendStatusMessage("Failed to move, no piece on start space.");
        } catch (NotLocationException e){
            TerminalControl.sendStatusMessage("Not a valid location.");
        }
    }

    static String getInputFromTerminalControl(){
        try{
            return TerminalControl.getInput();
        } catch (InterruptedException e){
            TerminalControl.sendStatusMessage("Got interrupted reading the terminal control.");
            return "";
        }
    }

    static void ask(){
        getInputFromTerminalControl();
    }

    static int askForInt(String message){
        while(true){
            TerminalControl.sendCommandText(message);
            try{
                return Integer.parseInt(getInputFromTerminalControl());
            } catch (InputMismatchException e){
                TerminalControl.sendStatusMessage("Incorrect format, requires an Integer.");
            } catch (Exception e){
                TerminalControl.sendStatusMessage("Unknown error occurred:\n" + e);
            }
        }
    }

    static boolean askForYN(String message){
        TerminalControl.sendCommandText(message);
        String input;
        while(true){
            input = getInputFromTerminalControl();
            if(input.equalsIgnoreCase("y"))
                return true;
            else if (input.equalsIgnoreCase("n"))
                return false;
            else
                TerminalControl.sendStatusMessage("Input must be 'y' or 'n'.");
        }
    }

    static String askForString(String message){
        TerminalControl.sendCommandText(message);
        return getInputFromTerminalControl();
    }

    static int askForType(String message, int selectionBound){
        while(true){
            TerminalControl.sendCommandText(message);
            try {
                int selection = Integer.parseInt(getInputFromTerminalControl());
                if(selection >= 0 && selection <= selectionBound)
                    return selection;
                TerminalControl.sendStatusMessage("Number not valid, input only the numbers listed.");
            } catch (InputMismatchException e){
                TerminalControl.sendStatusMessage("Not an int, try again.");
            }
        }
    }
}
