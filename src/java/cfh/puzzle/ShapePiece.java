package cfh.puzzle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;

public class ShapePiece extends Piece {

    private final Shape shape;
    private final Image image;
    private final int imgX;
    private final int imgY;
    
    public ShapePiece(int x, int y, Shape shp, boolean border) {
        this(x, y, shp, null, 0, 0, border);
    }
    
    public ShapePiece(int x, int y, Shape shp, Image img, boolean border) {
        this(x, y, shp, img, 0, 0, border);
    }
    
    public ShapePiece(int x, int y, Shape shp, Image img, int ix, int iy, boolean border) {
        super(x, y, border);
        Rectangle bounds = shp.getBounds();
        if (bounds.x < 0 || bounds.y < 0) {
            throw new IllegalArgumentException("negative bounds " + bounds);
        }
        
        shape = shp;
        image = img;
        imgX = ix;
        imgY = iy;
        setSize((int)bounds.getMaxX()+1, (int)bounds.getMaxY()+1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gg = (Graphics2D) g;
        
        if (image == null) {
            Color tmp = gg.getColor();
            try {
                gg.setColor(getBackground());
                gg.fill(shape);
            } finally {
                gg.setColor(tmp);
            }
        } else {
            Shape tmp = gg.getClip();
            try {
                gg.setClip(shape);
                gg.drawImage(image, -imgX, -imgY, this);
            } finally {
                gg.setClip(tmp);
            }
        }
        
        gg.draw(shape);
    }
    
    @Override
    public boolean contains(int x, int y) {
        return (x >= 0) && (y >= 0) && (x < getWidth()) && (y < getHeight()) 
               && shape.contains(x, y);
    }
}
