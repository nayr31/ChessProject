import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

class BoardWindowDefault extends JFrame {

    static final int width = 200;
    static final int height = 270;
    static JTextArea boardArea = new JTextArea(11, 20);
    static JTextArea lastMoveArea = new JTextArea(1, 13);

    BoardWindowDefault() {
        FrameSetup.setup(this, "-Board Window-", width, height, true, EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        initField();
    }

    static void setLastMoveArea(String text){
        lastMoveArea.setText(text);
    }

    void setAfter(int parentWidth) {
        this.setLocation(this.getX() + parentWidth / 2, this.getY());
    }

    void initField(){
        boardArea.setEditable(false);
        boardArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lastMoveArea.setEditable(false);
        add(lastMoveArea);
        add(boardArea);
    }
}