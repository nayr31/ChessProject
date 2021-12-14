import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileDecoder {
    static ArrayList<String> gmGames = new ArrayList<>();
    static ArrayList<ArrayList<Move>> gmGameMoves = new ArrayList<>();
    static final int readLimit = 500; // Set a read limit for the number of lines for performance reasons
    static final Move[] castleMoves = generateCastleMoves();

    static void populateGMGameMoves() {
        try {
            populateGMGames();
        } catch (FileNotFoundException e) {
            System.out.println("File not found for making gm moves.");
        }

        Board.aiIsActing = true;

        for (String game : gmGames) {
            String[] strMoves = game.split(" ");
            if (isValidGame(game)) {
                ArrayList<Move> moves = new ArrayList<>();
                Board.popNormal();
                Board.isWhiteTurn = true;
                boolean willAdd = true;
                for (String str : strMoves) {  // For each string move made in the game
                    Move move = determineMove(str);
                    if (move != null) {
                        moves.add(move);
                        Board.makeMove(move);
                    } else {
                        willAdd = false;
                        break;
                    }
                    Board.changeTurns(false);
                }
                if (willAdd)
                    gmGameMoves.add(moves);
            }
        }

        Board.aiIsActing = false;
        Board.clear();
    }

    // We only want games that black wins and that only queen promotions are present if any
    static boolean isValidGame(String game) {
        String[] strMoves = game.split(" ");
        return strMoves[strMoves.length - 1].equals("0-1")
                && !game.contains("=R") && !game.contains("=N") && !game.contains("=B");
    }

    // https://www.ichess.net/blog/chess-notation/
    static Move determineMove(String input) {
        // Check the easy moves first
        // Castling
        if (input.equals("O-O")) { // King side
            if (Board.isWhiteTurn)
                return getCastleMove("wk");
            else
                return getCastleMove("bk");
        } else if (input.equals("O-O-O")) { // Queen side
            if (Board.isWhiteTurn)
                return getCastleMove("wq");
            else
                return getCastleMove("bq");
        }

        // Otherwise, we need to generate the locations
        int startSpot = -1;
        int endSpot = -1;
        int substringOffset = 1;

        Piece.Type moveType = charToPieceType(Character.toLowerCase(input.charAt(0)));
        if (moveType == Piece.Type.Pawn)
            substringOffset = 0;

        // Pawn move without capture
        if (input.length() == 2) {
            try {
                int targetLocation = Board.convertInputToIndex(input);
                int pawnLocation = getPawnLocation(targetLocation);
                return new Move(pawnLocation, targetLocation);
            } catch (NotLocationException e) {
                e.printStackTrace();
            }
        }

        // Determine if the move involves a capture
        // Figure out wtf Nbd2 means

        // Find the piece end square

        return null;
    }

    private static int getPawnLocation(int location) {
        int lookOffset;
        if (Board.isWhiteTurn)
            lookOffset = -8;
        else
            lookOffset = 8;
        for (int j = 0; j < 2; j++) {
            int target = location + lookOffset * (j + 1);
            if (target >= 0 && target <= 63) {
                Piece token = Board.getTokenAtSpot(target);
                if (token != null) {
                    if (token.pieceType == Piece.Type.Pawn)
                        return target;
                }
            }
        }
        return -1;
    }

    /*
    if (token.isWhite) {
        if (move.startSpot == 0) // WK
            boolChanges[0] = true;
        if (move.startSpot == 7) // WQ
            boolChanges[1] = true;
    } else {
        if (move.startSpot == 56) // BK
            boolChanges[2] = true;
        if (move.startSpot == 63) // BQ
            boolChanges[3] = true;
    }*/
    private static Move[] generateCastleMoves() {
        Move[] moves = new Move[4];
        // White king side
        moves[0] = new Move(3, 1, new Move(0, 2));
        moves[1] = new Move(59, 57, new Move(56, 58));
        moves[2] = new Move(3, 5, new Move(7, 4));
        moves[3] = new Move(59, 61, new Move(63, 60));
        return moves;
    }

    static Move getCastleMove(String side) {
        return switch (side) {
            case "wk" -> castleMoves[0];
            case "bk" -> castleMoves[1];
            case "wq" -> castleMoves[2];
            case "bq" -> castleMoves[3];
            default -> null;
        };
    }

    static Piece.Type charToPieceType(char c) {
        return switch (c) {
            case 'k' -> Piece.Type.King;
            case 'q' -> Piece.Type.Queen;
            case 'b' -> Piece.Type.Bishop;
            case 'n' -> Piece.Type.Knight;
            case 'r' -> Piece.Type.Rook;
            default -> isPawn(c);
        };
    }

    static Piece.Type isPawn(char c) {
        if ((int) c >= 97 && (int) c <= 104)
            return Piece.Type.None;
        else
            return Piece.Type.Pawn;
    }

    static void populateGMGames() throws FileNotFoundException {
        File file = new File("Games.txt");
        Scanner scanner = new Scanner(file);

        for (int i = 0; i < readLimit && scanner.hasNextLine(); i++) {
            gmGames.add(scanner.nextLine());
        }
    }
}
