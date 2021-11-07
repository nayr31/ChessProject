import javax.swing.*;
import java.awt.*;

// Some of my frames used the same setup sequence
public class FrameSetup {

    static void setup(JFrame frame, String title, int width, int height, boolean visible, int close){
        frame.setResizable(false);
        // Set it to appear in the middle of the screen
        int sWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2;
        int sHeight = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2;
        int x = sWidth - (width / 2);
        int y = sHeight - (height / 2);
        frame.setLocation(x, y);
        frame.pack();
        // Normal arguments
        frame.setTitle(title);
        frame.setDefaultCloseOperation(close);
        frame.setSize(width, height);
        frame.setVisible(visible);
    }
}
