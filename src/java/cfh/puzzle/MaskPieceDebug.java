package cfh.puzzle;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class MaskPieceDebug extends MaskPiece {

    private Rectangle base;

    public MaskPieceDebug(int x, int y, Direction dir, 
                     BufferedImage msk, 
                     BufferedImage img, 
                     int ix, int iy, 
                     int r, 
                     Rectangle b, 
                     Polygon shp, boolean border) {
        super(x, y, dir, msk, img, ix, iy, r, border);
        base = b;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D gg = (Graphics2D) g;
        
        gg.setComposite(AlphaComposite.Src);
        gg.setColor(Color.RED);
        gg.translate(base.x, base.y);
        for (Piece piece : getConnected()) {
            int dx = piece.getX() - getX();
            int dy = piece.getY() - getY();
            System.out.printf("%s (%d,%d)%n", piece.getName(), dx, dy);  // XXX
            if (dx > 0) {
                if (dy > 0) {
                    gg.fillOval(base.width-19, base.height-19, 18, 18);
                } else if (dy < 0) {
                    gg.fillOval(base.width-19, 1, 18, 18);                    
                } else {
                    gg.fillRect(base.width-6, 0, 5, base.height);
                }
            } else if (dx < 0) {
                if (dy > 0) {
                    gg.fillOval(1, base.height-19, 18, 18);
                } else if (dy < 0) {
                    gg.fillOval(1, 1, 18, 18);                    
                } else {
                    gg.fillRect(1, 0, 5, base.height);
                }
            } else {
                if (dy > 0) {
                    gg.fillRect(0, base.height-6, base.width, 5);
                } else if (dy < 0) {
                    gg.fillRect(0, 1, base.width, 5);
                } else {
                    gg.fillRect(base.width/2-4, base.height/2-4, 9, 9);
                }
            }
        }
    }
}
