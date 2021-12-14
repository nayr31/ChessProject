import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileDecoder {
    static ArrayList<String> gmGames = new ArrayList<>();
    static ArrayList<ArrayList<Move>> gmGameMoves = new ArrayList<>();
    static final int readLimit = 500; // Set a read limit for the number of lines for performance reasons
    static final Move[] castleMoves = generateCastleMoves();

    //TODO Preform major testing on this, moves like "Nc6" are failing from a NotLocationException
    // The method "removePlusFromEndLocation" is causing the problem
    // Either make another dedicated method for it, or figure out why it isn't working in the first place

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
        System.out.println("Loaded gm game moves.");
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

        // Pawn move without capture
        if (input.length() == 2) {
            try {
                int targetLocation = Board.convertInputToIndex(input);
                int pawnLocation = getPawnStandardMoveLocation(targetLocation);
                return new Move(pawnLocation, targetLocation);
            } catch (NotLocationException e) {
                return null;
            }
        }
        Piece.Type moveType = charToPieceType(Character.toLowerCase(input.charAt(0)));

        // Pawn captures only involve the column as an identifier
        try {
            int targetLocation = Board.convertInputToIndex(removePlusFromEndLocation(input));
            int startLocation = -1;
            if (moveType == Piece.Type.Pawn) {
                // The string will have the letter in the column as the first character
                startLocation = getPawnLocationBeforeAttacking(targetLocation,
                        Board.convertLetterToIndex(input.charAt(0)));
            } else { // Piece is a normal piece
                ArrayList<Integer> possiblePieceLocations =
                        findLocationsOfPieceType(moveType, Board.isWhiteTurn);
                if (possiblePieceLocations.size() == 1) {
                    return new Move(possiblePieceLocations.get(0), targetLocation);
                } else {
                    // This means that there are more than one piece of that type
                    // Generate the moves of those types
                    ArrayList<Move> movesOfThatType =
                            generateMovesByPieceTypeAndLocations(moveType, possiblePieceLocations);
                    // Now check to see if more than one piece has that target location
                    ArrayList<Move> possibleMoves = new ArrayList<>();
                    for (Move possibleMove : movesOfThatType) {
                        if (possibleMove.endSpot == targetLocation)
                            possibleMoves.add(possibleMove);
                    }
                    if (possibleMoves.size() == 0)
                        return null;
                    if (possibleMoves.size() > 2)
                        return null;
                    if (possibleMoves.size() == 1) {
                        return possibleMoves.get(0);
                    }
                    // Last possible outcome is that the size is two
                    // This means that we need to get the col in the string and reference the start loc
                    int col = Board.convertLetterToIndex(input.charAt(1));
                    for (int i = 0; i < 8; i++) { // For each row
                        int suggestedStartSpot = col + i*8;
                        Move move = moveListHasStartSpot(possibleMoves, suggestedStartSpot);
                        if(move != null)
                            return move;
                    }
                }
            }
            if (startLocation != -1 && targetLocation != -1)
                return new Move(startLocation, targetLocation);
        } catch (NotLocationException e) {
            return null;
        }

        return null;
    }

    private static Move moveListHasStartSpot(ArrayList<Move> moves, int spot){
        for(Move move: moves){
            if(move.startSpot == spot)
                return move;
        }
        return null;
    }

    private static ArrayList<Move> generateMovesByPieceTypeAndLocations(Piece.Type moveType, ArrayList<Integer> possiblePieceLocations) {
        ArrayList<Move> moves = new ArrayList<>();
        for (Integer i : possiblePieceLocations) {
            if (Piece.isSlidingType(moveType)) {
                moves.addAll(MoveCoordinator.generateSlidingMoves(i, Board.spots[i].spotPiece));
            } else if (moveType == Piece.Type.Knight) {
                moves.addAll(MoveCoordinator.generateKnightMoves(i, Board.spots[i].spotPiece));
            } else if (moveType == Piece.Type.King) {
                moves.addAll(MoveCoordinator.generateKingMoves(i, Board.spots[i].spotPiece));
            } // Will never have pawns here
        }
        return moves;
    }

    private static ArrayList<Integer> findLocationsOfPieceType(Piece.Type targetType, boolean isWhite) {
        ArrayList<Integer> locations = new ArrayList<>();
        for (int i = 0; i < 63; i++) {
            if (Board.spots[i].spotPiece != null) {
                Piece piece = Board.spots[i].spotPiece;
                if (piece.pieceType == targetType && piece.isWhite == isWhite) {
                    locations.add(i);
                }
            }
        }
        return locations;
    }

    private static int getPawnLocationBeforeAttacking(int target, int col) {
        //TODO Finish pawn location before attacking
        int[] pawnAttackDirections = Director.pawnAttackDirectionCorrection(Board.isWhiteTurn);
        return -1;
    }

    private static String removePlusFromEndLocation(String line) {
        int offset = 0;
        if (stringHasPlus(line))
            offset = 1;
        return line.substring(line.length() - 1 - offset - 2, line.length() - 1 - offset);
    }

    private static boolean stringHasPlus(String line) {
        return line.charAt(line.length() - 1) == '+';
    }

    private static int getPawnStandardMoveLocation(int location) {
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

    private static void populateGMGames() throws FileNotFoundException {
        File file = new File("Games.txt");
        Scanner scanner = new Scanner(file);

        for (int i = 0; i < readLimit && scanner.hasNextLine(); i++) {
            gmGames.add(scanner.nextLine());
        }
    }
}
