import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class ImageHandler {

    static boolean hasLoaded = false;
    static BufferedImage[] whitePieces = new BufferedImage[6];
    static BufferedImage[] blackPieces = new BufferedImage[6];

    static public void loadImages() throws Exception{
        try{
            urlImport();
            hasLoaded = true;
        } catch (Exception e){
            System.out.println("Image loading failed.");
            throw e;
        }
    }

    static private void urlImport() throws Exception{
        String[] whitePieceURLStrings = new String[]{
                "https://github.com/nayr31/ChessProject/blob/main/src/images/white_bishop.png?raw=true",
                "https://github.com/nayr31/ChessProject/blob/main/src/images/white_king.png?raw=true",
                "https://github.com/nayr31/ChessProject/blob/main/src/images/white_knight.png?raw=true",
                "https://raw.githubusercontent.com/nayr31/ChessProject/main/src/images/white_pawn.png",
                "https://github.com/nayr31/ChessProject/blob/main/src/images/white_queen.png?raw=true",
                "https://raw.githubusercontent.com/nayr31/ChessProject/main/src/images/white_rook.png"
        };
        String[] blackPieceURLStrings = new String[]{
                "https://github.com/nayr31/ChessProject/blob/main/src/images/black_bishop.png?raw=true",
                "https://github.com/nayr31/ChessProject/blob/main/src/images/black_king.png?raw=true",
                "https://github.com/nayr31/ChessProject/blob/main/src/images/black_knight.png?raw=true",
                "https://github.com/nayr31/ChessProject/blob/main/src/images/black_pawn.png?raw=true",
                "https://github.com/nayr31/ChessProject/blob/main/src/images/black_queen.png?raw=true",
                "https://github.com/nayr31/ChessProject/blob/main/src/images/black_rook.png?raw=true"
        };
        URL whiteURL;
        URL blackURL;
        for (int i = 0; i < 6; i++) {
            try {
                whiteURL = new URL(whitePieceURLStrings[i]);
            } catch (Exception e) {
                System.out.println("Error downloading white file.");
                throw e;
            }
            try {
                blackURL = new URL(blackPieceURLStrings[i]);
            } catch (Exception e) {
                System.out.println("Error downloading black file.");
                throw e;
            }
            try {
                ImageHandler.whitePieces[i] = ImageIO.read(whiteURL);
                ImageHandler.blackPieces[i] = ImageIO.read(blackURL);
            } catch (Exception e) {
                System.out.println("Error converting to image(s).");
                throw e;
            }
        }
    }

    private void fileImport() {
        File file = null;
        try {
            //Path path = FileSystems.getDefault().getPath("white_bishop.png").toAbsolutePath();
            Path path = FileSystems.getDefault().getPath("16_chess.jpg").toAbsolutePath();
            System.out.println("CWD: " + path.toString());
            file = new File(String.valueOf(path));
        } catch (Exception e) {
            System.out.println("Error reading image from file.");
        }
        try {
            assert file != null;
            BufferedImage image = new BufferedImage(288, 591, BufferedImage.TYPE_INT_ARGB);
            FileInputStream stream = new FileInputStream(file);
            image = ImageIO.read(stream);
            //image = ImageIO.read(file);
        } catch (Exception e) {
            System.out.println("Error converting image to file.");
        }

    }

}
