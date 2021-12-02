import java.util.ArrayList;
import java.util.InputMismatchException;

public class InputGetter {

    static Move askForMoveInput(String output){
        while(true){
            ArrayList<Move> legalMoves = MoveCoordinator.generateLegalMoves();
            String input;
            String[] inputSplit;
            input = getInputFromTerminalControl();
            inputSplit = input.split("-");
            if(inputSplit.length != 2 || inputSplit[0].length() != 2 || inputSplit[1].length() != 2){
                TerminalControl.sendStatusMessage("Wrong input format. Please use \"a1-a2\".\n" +
                        "These represent board spaces, with the start space being the first index.");
            } else{
                try{
                    int startSpot = Board.convertInputToIndex(inputSplit[0]);
                    int endSpot = Board.convertInputToIndex(inputSplit[1]);
                    Move suggestedMove = new Move(startSpot, endSpot);
                    boolean moveIsInList = legalMoves.contains(suggestedMove);//MoveCoordinator.moveIsInList(suggestedMove, Board.isWhiteTurn, MoveCoordinator.get, Board.blackMoves);
                    if(!moveIsInList){
                        TerminalControl.sendStatusMessage("Move is not possible.");
                    } else{
                        return suggestedMove;
                    }
                } catch(NotLocationException e){
                    TerminalControl.sendStatusMessage("Something terrible happened trying to decipher the move!");
                }
            }
        }
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
