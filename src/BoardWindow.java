import javax.swing.*;
import java.awt.*;

class BoardWindow extends JFrame {

    static final int width = 200;
    static final int height = 230;
    static JTextArea boardArea = new JTextArea(15, 15);
    static JTextArea turnArea = new JTextArea(1, 15);

    BoardWindow(){
        FrameSetup.setup(this,"-Board Window-", width, height, true, EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        initField();
    }

    void showBoard(){
        boardArea.setText(Board.boardString());
    }

    void updateTurnDisplay(){
        turnArea.setText(turnToString() + " to move.");
    }

    String turnToString(){
        return Board.isWhiteTurn ? "White" : "Black";
    }

    void initField(){
        boardArea.setEditable(false);
        add(boardArea);
        turnArea.setEditable(false);
        add(turnArea);
    }
}