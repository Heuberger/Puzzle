package cfh.puzzle;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ConstSizeImpl extends Size {

    private final int count;
    private final int sizeX;
    private final int sizeY;
    private final int overlap;
    private final int baseVariation;
    private final int borderWidth;
    private final int pegWidth;
    private final int pegLength;
    private final int pegRadius;
    private final int pegPositionDelta;
    private final int pegRadiusDelta;
    private final int pegHeightDelta;
    private final int edgeColorChange;

    ConstSizeImpl(
        int count,
        int sizeX,
        int sizeY,
        int overlap,
        int baseVariation,
        int borderWidth,
        int pegWidth,
        int pegLength,
        int pegRadius,
        int pegPositionDelta,
        int pegRadiusDelta,
        int pegHeightDelta,
        int edgeColorChange)
    {
        this.count = count;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.overlap = overlap;
        this.baseVariation = baseVariation;
        this.borderWidth = borderWidth;
        this.pegWidth = pegWidth;
        this.pegLength = pegLength;
        this.pegRadius = pegRadius;
        this.pegPositionDelta = pegPositionDelta;
        this.pegRadiusDelta = pegRadiusDelta;
        this.pegHeightDelta = pegHeightDelta;
        this.edgeColorChange = edgeColorChange;
    }

    @Override
    protected void write0(ObjectOutputStream out) throws IOException {
        throw new IOException("not implemented");
    }

    @Override
    public int getCount() {
        return count;
    }
    
    @Override
    public int getSizeX() {
        return sizeX;
    }
    
    @Override
    public int getSizeY() {
        return sizeY;
    }

    @Override
    public int getOverlap() {
        return overlap;
    }
    
    @Override
    public int getBaseVariation() {
        return baseVariation;
    }

    @Override
    public int getBorderWidth() {
        return borderWidth;
    }

    @Override
    public int getPegWidth() {
        return pegWidth;
    }
    
    @Override
    public int getPegLength() {
        return pegLength;
    }
    
    @Override
    public int getPegRadius() {
        return pegRadius;
    }

    @Override
    public int getPegPositionDelta() {
        return pegPositionDelta;
    }
    
    @Override
    public int getPegRadiusDelta() {
        return pegRadiusDelta;
    }
    
    @Override
    public int getPegHeightDelta() {
        return pegHeightDelta;
    }

    @Override
    public int getEdgeColorChange() {
        return edgeColorChange;
    }
}
