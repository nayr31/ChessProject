import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;

// This code was copied form my last assignment if it looks familiar
public class TerminalControl extends JFrame {
    static final int width = 500;
    static final int height = 240;
    static final int commonColumns = 40;
    static String lastCommandInput = "Last input: ";
    static JTextField commandInputArea = new JTextField(commonColumns);
    static JTextArea commandsReceivedArea = new JTextArea(5, commonColumns);
    static JTextArea statusArea = new JTextArea(3, commonColumns);
    static Semaphore semaphore = new Semaphore(0);
    //BoardWindow boardWindow = new BoardWindow();
    BoardWindowDefault boardWindowDefault;
    static HelpWindow helpWindow = new HelpWindow();

    TerminalControl(){
        FrameSetup.setup(this,"-Chess Program-", width, height, true, EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        initField();
        //boardWindow.setAfter((int) (this.getWidth()*1.80));
        helpWindow.setBefore((int) (this.getWidth()*1.60));
        TerminalControl.sendCommandText("Loading images from internet, please make sure you have a connection...");
        //try {
        //    ImageHandler.loadImages();
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
        //if(!ImageHandler.hasLoaded){
        //    TerminalControl.sendStatusMessage("Image loading has failed!\n" +
        //            "Defaulting to simple board :(\n" +
        //            "You can find images of the board in action in the submission.");
        // boardWindow.setVisible(false);
        boardWindowDefault = new BoardWindowDefault();
        boardWindowDefault.setAfter((int) (this.getWidth()*1.40));
        //}
    }

    // Creates and places the fields on the screen
    private void initField() {
        JTextArea commandArea = new JTextArea(1, commonColumns);

        commandArea.setText(lastCommandInput);
        commandArea.setEditable(false);
        // Set the enter key to release the semaphore for user input
        commandInputArea.addActionListener(event ->{
            String textFromField = commandInputArea.getText();
            commandInputArea.setText("");
            lastCommandInput = textFromField;
            commandArea.setText("Last input: " + lastCommandInput);
            semaphore.release();
        });
        commandsReceivedArea.setEditable(false);
        statusArea.setEditable(false);

        add(commandsReceivedArea);
        add(commandInputArea);
        add(commandArea);
        add(statusArea);

    }

    // Causes the program to wait until the enter key is pressed in the command field
    static String getInput() throws InterruptedException {
        semaphore.acquire();
        return lastCommandInput;
    }

    // Sends text to the command text area
    static void sendCommandText(String text){
        commandsReceivedArea.setText(text);
    }

    static void sendInputText(String text) { commandInputArea.setText(text);}

    static void clearCommandText(){commandsReceivedArea.setText("");}

    static void sendStatusMessage(String text){
        statusArea.setText(text);
    }

    void refreshBoard(){
        //if(ImageHandler.hasLoaded)
        //    boardWindow.draw();
        //else{
            BoardWindowDefault.boardArea.setText(Board.boardString());
        //}
    }

    static void toggleHelpWindow() { helpWindow.setVisible(!helpWindow.isVisible());}

    static void setBoardMessage(String message){
        //if(ImageHandler.hasLoaded)
        //    BoardWindow.setLastMoveArea(message);
        //else
            BoardWindowDefault.setLastMoveArea(message);
    }

    private static class HelpWindow extends JFrame {
        static final int width = 330;
        static final int height = 300;
        static JTextArea helpArea = new JTextArea(11, 26);
        static String debugOptions = "gm - Generate moves\n" +
                "im \"a1-a2\" - Input move, without \"\n" +
                "ap \"Ka1\" - Add a piece to the board (FEN style piece)\n" +
                "cl - Clear the board\n" +
                "um - Undo the last move made\n" +
                "tt - Take Turn\n" +
                "exit - Close";

        HelpWindow(){
            FrameSetup.setup(this, "Help Commands", width, height, false, HIDE_ON_CLOSE);
            setLayout(new FlowLayout());
            initField();
        }

        void setBefore(int parentWidth){
            this.setLocation(this.getX() - parentWidth/2, this.getY());
        }

        private void initField(){
            helpArea.setEditable(false);
            helpArea.setText(debugOptions);
            add(helpArea);
        }
    }
}
