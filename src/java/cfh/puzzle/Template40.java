package cfh.puzzle;

public class Template40 extends Template {

    @Override public int getSizeX() { return 40; }
    @Override public int getSizeY() { return getSizeX(); }

    @Override public int getOverlap() { return 15; }
    @Override public int getBaseVariation() { return 3; }

    @Override public int getBorderWidth() { return 5; }

    @Override public int getPegWidth() { return 3; }
    @Override public int getPegLength() { return 1; }
    @Override public int getPegRadius() { return 4; }
    
    @Override public int getPegPositionDelta() { return 4; }
    @Override public int getPegRadiusDelta() { return -1; }
    @Override public int getPegHeightDelta() { return -1; }
    
    @Override public int getEdgeColorChange() { return 16 * 0x010101; }
}
