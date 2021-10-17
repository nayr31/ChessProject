# Chess Project

This project aims to simulate chess, with an AI partner that uses minimax and alpha-beta pruning to make it's moves.

## File details

### Chess.java

The main class that is meant to be run. This class takes care of the overlooking aspects of the game, such as looping through turns, checking for a complete board state (such as a stalemate or winner), to ensure a clear understanding of which steps are being taken at which point in the game.

### Board.java

This class stores the data that pertains to the board itself. This could mean anything from which piece is on which square, what the output of a turn would look like, or any logic that is required to simulate the AI's turn.

### Move.java

A data object that stores the start position, and the ending position of a move. This allows us to store every possible move of a piece, be it a Rook sliding forward 1, 2, 3, etc. squares. This is mainly used to determine possible moves, and also used for minimax when storing which moves the AI has taken during the recursion rabbit-hole.

### Piece.java

A data object that contains all data that represents a board piece. To differentiate them, I used an enum, and some getting functions for which "kind" of piece they are based on movement type.

### Spot.java

A fluff object, simply stores a piece. This is used for convenience for the board object, making checking for an empty space just a "null" reference instead of having another piece that takes up space in memory.

## Resources

Below are some articles and websites that I used in the creation of this project:

- [Simple overall board state evaluation](https://www.chessprogramming.org/Evaluation#Where_to_Start)
- [Chess piece weights per square](https://www.chessprogramming.org/Simplified_Evaluation_Function)
- [Grandmaster games](https://raw.githubusercontent.com/SebLague/Chess-AI/main/Assets/Book/Games.txt)
- [Chess programming](https://web.archive.org/web/20071026090003/http://www.brucemo.com/compchess/programming/index.htm)
- [Lazy Evaluation](https://www.chessprogramming.org/Lazy_Evaluation)
