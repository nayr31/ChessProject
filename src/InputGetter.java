import java.util.Scanner;

public class InputGetter {
    static Scanner scanner = new Scanner(System.in);

    static Move askForMoveInput(String output){
        String input;
        String[] inputSplit;
        while(true){
            System.out.println(output);
            input = getInput();
            inputSplit = input.split("-");
            if(inputSplit.length != 2 || inputSplit[0].length() != 2 || inputSplit[1].length() != 2){
                System.out.println("Wrong input format. Please use \"a1-a2\"");
            } else{
                int startSpot = Board.convertInputToIndex(inputSplit[0]);
                int endSpot = Board.convertInputToIndex(inputSplit[1]);
                Move suggestedMove = new Move(startSpot, endSpot);
                boolean moveIsInList = Board.moveIsInList(suggestedMove, Board.isWhiteTurn);
            }
        }
    }

    static String getInput(){
        return scanner.nextLine();
    }
}
