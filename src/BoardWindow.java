import javax.swing.*;
import java.awt.*;

class BoardWindow extends JFrame {

    static final int width = 220;
    static final int height = 270;
    static JTextArea boardArea = new JTextArea(11, 20);
    static JTextArea lastMoveArea = new JTextArea(1, 13);

    BoardWindow(){
        FrameSetup.setup(this,"-Board Window-", width, height, true, EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        initField();
    }

    void setAfter(int parentWidth){
        this.setLocation(this.getX() + parentWidth/2, this.getY());
    }

    void updateTurnDisplay(){
        lastMoveArea.setText(turnToString() + " to move.");
    }

    String turnToString(){
        return Board.isWhiteTurn ? "White" : "Black";
    }

    void initField(){
        boardArea.setEditable(false);
        boardArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(boardArea);
        lastMoveArea.setEditable(false);
        add(lastMoveArea);
    }
}