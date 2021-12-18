# Chess Project

This project aims to simulate chess, with an AI partner that uses minimax and alpha-beta pruning to make it's moves.

## Compilation

Included in project submission is an executable jar file, but if you need to build the project yourself simply open the source files in IntelliJ and build artifacts, or build all `.java` files with `Chess.java` as the main class.

## File details

### Al Maroon.java

The AI class. It "thinks" to return a move to the chess handler which is dependant on a variety of board states.

### Board.java

This class stores the data that pertains to the board itself.

### BoardWindow.java

These are windows that display the board state. The default variant is just a simple text window, but the regular version has images but proved to be too buggy for consistent game-play session to session.

### Chess.java

The main class that is meant to be run. This class takes care of the overlooking aspects of the game, such as looping through turns, checking for a complete board state (such as a stalemate or winner), to ensure a clear understanding of which steps are being taken at which point in the game.

### Director.java

Helper class for our one dimensional array style board. This would contain conversions for the 8 directions 0-7 into array index directions like +1, +7, +8, +9, -1, -7, -8, -9.

### FileDecoder.java

Decodes the `Games.txt` file into grandmaster game moves for use in move prediction.

### FrameSetup.java

Helper class for use in creating windows.

### ImageHandler.java

Loads images for use in the regular version of BoardWindow.

### Move.java

A data object that stores the start position, and the ending position of a move. This allows us to store every possible move of a piece, be it a Rook sliding forward 1, 2, 3, etc. squares. This is mainly used to determine possible moves, and also used for minimax when storing which moves the AI has taken during the recursion rabbit-hole.

### MoveCoordinator.java

Generates moves for pieces. Although this sounds simple, this is the largest class in the project.

### Piece.java

A data object that contains all data that represents a board piece. To differentiate them an enum is used, and some getting functions for which "kind" of piece they are based on movement type.

### Scorer.java

When the board state needs to be evaluated, the scorer class is called on to decide.

### Spot.java

A fluff object, simply stores a piece. This is used for convenience for the board object, making checking for an empty space just a "null" reference instead of having another piece that takes up space in memory.

### TerminalControl.java

The main window for input and control over the program.

### Writer.java

The writer is called on whenever we needed a file to be written to the disk. In this case, its just the FEN string.
