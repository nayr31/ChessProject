import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;

// This code was copied form my last assignment if it looks familiar
public class TerminalControl extends JFrame {
    static final int width = 500;
    static final int height = 240;
    static final int commonColumns = 40;
    static String lastCommandInput = "Last input: ";
    static JTextArea commandsReceivedArea = new JTextArea(5, commonColumns);
    static JTextArea statusArea = new JTextArea(3, commonColumns);
    static Semaphore semaphore = new Semaphore(0);
    static BoardWindow boardWindow = new BoardWindow();

    TerminalControl(){
        FrameSetup.setup(this,"-Chess Program-", width, height, true, EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        initField();
        boardWindow.setAfter((int) (this.getWidth()*1.40));
    }

    // Creates and places the fields on the screen
    private void initField() {
        JTextField field = new JTextField(commonColumns);
        JTextArea commandArea = new JTextArea(1, commonColumns);

        commandArea.setText(lastCommandInput);
        commandArea.setEditable(false);
        // Set the enter key to release the semaphore for user input
        field.addActionListener(event ->{
            String textFromField = field.getText();
            field.setText("");
            lastCommandInput = textFromField;
            commandArea.setText("Last input: " + lastCommandInput);
            semaphore.release();
        });
        commandsReceivedArea.setEditable(false);
        statusArea.setEditable(false);

        add(commandsReceivedArea);
        add(field);
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

    static void clearCommandText(){commandsReceivedArea.setText("");}

    static void sendStatusMessage(String text){
        statusArea.setText(text);
    }

    static void refreshBoard(){
        BoardWindow.boardArea.setText(Board.boardString());
    }

    void toggleBoard(){
        boardWindow.setVisible(!boardWindow.isVisible());
    }
}
