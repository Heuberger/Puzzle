/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * @author Carlos F. Heuberger
 */
public class ImagePiece extends Piece {

    private static final ColorModel DEF_COL_MOD = ColorModel.getRGBdefault();
    
    private final BufferedImage image;
    
    public ImagePiece(int x, int y, BufferedImage img) {
        super(x, y);
        image = img;
        setSize(img.getWidth(), img.getHeight());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }
    
    @Override
    public boolean contains(int x, int y) {
        return (0 <= x) && (x < getWidth()) && 
        (0 <= y) && (y < getHeight()) &&
        (DEF_COL_MOD.getAlpha(image.getRGB(x, y)) > 0);
    }
}
