import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

class BoardWindow extends JFrame {

    //static final int width = 220;
    //static final int height = 270;
    static final int width = 415;
    static final int height = 450;
    static JTextArea boardArea = new JTextArea(11, 20);
    static JTextArea lastMoveArea = new JTextArea(1, 13);
    DrawPane panel;
    BufferedImage whitePawn;
    Image blackPieces;

    BoardWindow() {
        FrameSetup.setup(this, "-Board Window-", width, height, true, EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        initField();
        panel = setPanel();
        importImages();
    }

    private void importImages() {
        //TODO finish image importing
        Toolkit tool = Toolkit.getDefaultToolkit();
        //whitePawn = tool.getImage("white_pawn.png");
        try{
            whitePawn = ImageIO.read(new File("white_pawn.png"));
        } catch(Exception e){
            System.out.println("Error reading image files.");
        }
    }

    void setAfter(int parentWidth) {
        this.setLocation(this.getX() + parentWidth / 2, this.getY());
    }

    static void setLastMoveArea(String text) {
        lastMoveArea.setText(text);
    }

    void initField() {
        boardArea.setEditable(false);
        boardArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lastMoveArea.setEditable(false);
        add(lastMoveArea);
        //add(boardArea);
    }

    private DrawPane setPanel() {
        DrawPane panel = new DrawPane();
        Container container = getContentPane();
        container.add(panel);
        panel.setPreferredSize(new Dimension(width, height));
        return panel;
    }

    public void draw(){

        panel.repaint();
    }

    class DrawPane extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintBoard((Graphics2D) g);
            paintNotation((Graphics2D) g);
            paintPieces((Graphics2D)g);
        }

        static final int spotWidth = 50;
        static final int spotHeight = 50;
        static final int boardXOffset = 25;
        static final int boardYOffset = 0;

        // Paint the squares
        private void paintBoard(Graphics2D g) {
            int x, y;
            Color whiteColor = Color.WHITE;
            Color blackColor = Color.BLACK;

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ((row % 2) == (col % 2))
                        g.setColor(whiteColor);
                    else
                        g.setColor(blackColor);
                    x = col * 45 + boardXOffset;
                    y = row * 45 + boardYOffset;
                    g.fillRect(x, y, spotWidth, spotHeight);
                }
            }
        }

        // Paint the letters and numbers
        private void paintNotation(Graphics g){
            g.setColor(Color.black);

            // Horizontal
            //TODO finish lettered indexes
            g.drawString("a", 10 + spotWidth/2 + 10,boardYOffset + 7 * spotHeight + 25);

            // Vertical numbers
            for (int i = 8; i > 0; i--)
                g.drawString(String.valueOf(9-i), 12, (int) (i * spotHeight*0.93 - spotHeight/2));
        }

        // Paint the pieces on the board
        private void paintPieces(Graphics g){
            Spot[] spots = Board.getSpots();
            g.drawImage(whitePawn, 0, 0, 50, 50, this);
            //TODO finish piece spot painting
        }
    }
}