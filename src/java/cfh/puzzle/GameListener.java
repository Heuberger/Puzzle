/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

/**
 * @author Carlos F. Heuberger
 */
public interface GameListener {

    void pieceSelected(Piece piece);
    
    void pieceMoved(Piece piece, int x, int y);
    
    void pieceDisconnect(Piece piece);
}
