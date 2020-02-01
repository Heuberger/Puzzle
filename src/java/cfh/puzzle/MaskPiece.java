package cfh.puzzle;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class MaskPiece extends Piece {

    private static final ColorModel DEF_COL_MOD = ColorModel.getRGBdefault();
    
    private final BufferedImage mask;
    private final BufferedImage image;
    private final int imgX;
    private final int imgY;
    private final int rule;
    
    private Image lastImage = null;
    private Direction lastDir = null;
    
    public MaskPiece(int x, int y, Direction dir, 
                     BufferedImage msk, 
                     BufferedImage img, 
                     int ix, int iy, 
                     int r) {
        super(x, y, dir);
        mask = msk;
        image = img;
        imgX = ix;
        imgY = iy;
        rule = r;
        
        setSize(msk.getWidth(), msk.getHeight());
    }

    private Image createImage() {
        if (getDir() == lastDir)
            return lastImage;
        int width = mask.getWidth();
        int height = mask.getHeight();
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] mcomp = DEF_COL_MOD.getComponents(0, null, 0);
        int[] icomp = DEF_COL_MOD.getComponents(0, null, 0);
        int[] bcomp = DEF_COL_MOD.getComponents(getBackground().getRGB(), null, 0);
        int ialpha = mcomp.length - 1;
        Direction dir = getDir();
        int tx;
        int ty;
        for (int x = 0, ix = imgX; x < width; x++, ix++) {
            for (int y = 0, iy = imgY; y < height; y++, iy++) {
                switch (dir) {
                    case NORTH: tx = x; ty = y; break;
                    case EAST: tx = y; ty = width-x-1; break;
                    case SOUTH: tx = width-x-1; ty = height-y-1; break;
                    case WEST: tx = height-y-1; ty = x; break;
                    default: throw new RuntimeException("dir: " + dir);
                }
                DEF_COL_MOD.getComponents(mask.getRGB(x, y), mcomp, 0);
                if ( image != null &&
                        0 <= ix && ix < image.getWidth() &&
                        0 <= iy && iy < image.getHeight() ) {
                    DEF_COL_MOD.getComponents(image.getRGB(ix, iy), icomp, 0);
                    icomp[ialpha] = mcomp[ialpha];
                } else {
                    boolean useColor = false;
                    for (int i = 0; i < ialpha; i++) {
                        if (mcomp[i] != 128) {
                            useColor = true;
                            break;
                        }
                    }
                    if (useColor) {
                        for (int i = 0; i < ialpha; i++) {
                            icomp[i] = bcomp[i];
                        }
                        icomp[ialpha] = mcomp[ialpha];
                    } else {
                        icomp[ialpha] = 0;
                    }
                }
                switch (rule) {
                    default:
                        System.err.println("unknow rule " + rule);
                    break;
                    case 0:
                        // icomp = icomp
                        break;
                    case 1:
                        for (int i = 0; i < ialpha; i++) {
                            int v = icomp[i] + mcomp[i] - 128;
                            icomp[i] = Math.max(0, Math.min(v, 255));
                        }
                        break;
                    case 2:
                        for (int i = 0; i < ialpha; i++) {
                            int v = (int)(icomp[i] * (mcomp[i]/128.0));
                            icomp[i] = Math.max(0, Math.min(v, 255));
                        }
                        break;
                    case 3:
                        for (int i = 0; i < ialpha; i++) {
                            int v = (int)(icomp[i] * (mcomp[i]/128.0) * (mcomp[i]/128.0));
                            icomp[i] = Math.max(0, Math.min(v, 255));
                        }
                        break;
                }
                img.setRGB(tx, ty, DEF_COL_MOD.getDataElement(icomp, 0));
            }
        }
        lastImage = img;
        lastDir = dir;
        return img;
    }
    
    @Override
    public int getWidth() {
        switch (getDir()) {
            case NORTH:
            case SOUTH:
                return mask.getWidth();
            case EAST:
            case WEST:
                return mask.getHeight();
            default:
                throw new RuntimeException("dir: " + getDir());
        }
    }
    
    @Override
    public int getHeight() {
        switch (getDir()) {
            case NORTH:
            case SOUTH:
                return mask.getHeight();
            case EAST:
            case WEST:
                return mask.getWidth();
            default:
                throw new RuntimeException("dir: " + getDir());
        }
    }
    
    @Override
    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponents(g);
        
        Graphics2D gg = (Graphics2D) g;

        Image img = isSelected() ? mask : createImage();
        if (img == null) {
            gg.clearRect(0, 0, getWidth(), getHeight());
        } else {
            gg.drawImage(img, 0, 0, this);
        }
    }
    
    @Override
    public boolean contains(int x, int y) {
        int tx;
        int ty;
        switch (getDir()) {
            case NORTH: tx = x; ty = y; break;
            case EAST: tx = getHeight()-y; ty = x; break;
            case SOUTH: tx = getWidth()-x; ty = getHeight()-y; break;
            case WEST: tx = y; ty = getWidth()-x; break;
            default: throw new IllegalArgumentException("dir: " + getDir());
        }
        return (tx >= 0) && (tx < mask.getWidth()) && (ty >= 0) && (ty < mask.getHeight()) &&
            (DEF_COL_MOD.getAlpha(mask.getRGB(tx, ty)) > 0);
    }
}
