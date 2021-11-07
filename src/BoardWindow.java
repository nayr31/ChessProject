import javax.swing.*;
import java.awt.*;

class BoardWindow extends JFrame {

    static final int width = 200;
    static final int height = 200;
    static JTextArea boardArea = new JTextArea(15, 15);

    BoardWindow(){
        FrameSetup.setup(this,"-Chess 2-", width, height, true, EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        initField();
    }

    void initField(){
        add(boardArea);
    }
}