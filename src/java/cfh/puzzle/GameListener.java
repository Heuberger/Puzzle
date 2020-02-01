package cfh.puzzle;

public interface GameListener {

    void pieceSelected(Piece piece);
    
    void pieceMoved(Piece piece, int x, int y);
    
    void pieceDisconnect(Piece piece);
}
