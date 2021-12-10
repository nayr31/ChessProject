import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

class BoardWindow extends JFrame {

    static int width = 415;
    static int height = 450;
    static JTextArea boardArea = new JTextArea(11, 20);
    static JTextArea lastMoveArea = new JTextArea(1, 13);
    DrawPane panel;

    BoardWindow() {
        FrameSetup.setup(this, "-Board Window-", width, height, true, EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        initField();
        panel = setPanel();
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
    }

    private DrawPane setPanel() {
        DrawPane panel = new DrawPane();
        Container container = getContentPane();
        container.add(panel);
        panel.setPreferredSize(new Dimension(width, height));
        return panel;
    }

    public void draw() {
        panel.repaint();
    }

    class DrawPane extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintBoard((Graphics2D) g);
            paintNotation((Graphics2D) g);
            paintPieces((Graphics2D) g);
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
        private void paintNotation(Graphics g) {
            g.setColor(Color.black);

            // Horizontal
            //TODO finish lettered indexes (location multipliers)
            for (int i = 0; i < 8; i++) {
                char c = (char)(i + 97);
                g.drawString(String.valueOf(c), 10 + spotWidth / 2 + 10, boardYOffset + 7 * spotHeight + 25);
            }


            // Vertical numbers
            for (int i = 8; i > 0; i--)
                g.drawString(String.valueOf(9 - i), 12, (int) (i * spotHeight * 0.93 - spotHeight / 2));
        }

        // Paint the pieces on the board
        private void paintPieces(Graphics g) {
            for (int i = 0; i < Board.spots.length; i++) {
                Piece token = Board.spots[i].spotPiece;
                if (token != null) {
                    int[] xyCoordinates = Board.convertIndexToDoubleIndex(i);
                    BufferedImage image = getImageByTypeAndColor(token.pieceType, token.isWhite);
                    int[] xyNormalSizes = getNormalizedImageSize(image);
                    g.drawImage(image,
                            xyCoordinates[0] * 50 + 25, xyCoordinates[1] * 50,
                            xyNormalSizes[0], xyNormalSizes[1], this);
                }
            }
            //g.drawImage(whitePieces, 0, 0, 50, 50, this);
            //TODO finish piece spot painting (location multipliers)
        }

        private int[] getNormalizedImageSize(BufferedImage image){
            int w = image.getWidth() >= image.getHeight() ? image.getWidth() : image.getHeight() / 40;
            return new int[] {
                    image.getWidth() / w,
                    image.getHeight() / w
            };
        }

        public BufferedImage getImageByTypeAndColor(Piece.Type type, boolean isWhite) {
            return switch (type) {
                case Bishop -> isWhite ? ImageHandler.whitePieces[0] : ImageHandler.blackPieces[0];
                case King -> isWhite ? ImageHandler.whitePieces[1] : ImageHandler.blackPieces[1];
                case Knight -> isWhite ? ImageHandler.whitePieces[2] : ImageHandler.blackPieces[2];
                case Pawn -> isWhite ? ImageHandler.whitePieces[3] : ImageHandler.blackPieces[3];
                case Queen -> isWhite ? ImageHandler.whitePieces[4] : ImageHandler.blackPieces[4];
                case Rook -> isWhite ? ImageHandler.whitePieces[5] : ImageHandler.blackPieces[5];
                case None -> null;
            };
        }
    }
}