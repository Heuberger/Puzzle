package cfh.puzzle;

import java.io.Serializable;

public interface Size extends Serializable {

    public int getCount();
    
    public int getSizeX();
    public int getSizeY();

    public int getOverlap();
    public int getBaseVariation();

    public int getBorderWidth();

    public int getPegWidth();
    public int getPegLength();
    public int getPegRadius();

    public int getPegPositionDelta();
    public int getPegRadiusDelta();
    public int getPegHeightDelta();

    public int getEdgeColorChange();
}