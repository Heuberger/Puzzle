package cfh.puzzle;

public class Template50 extends Template {

    @Override public int getSizeX() { return 50; }
    @Override public int getSizeY() { return getSizeX(); }

    @Override public int getOverlap() { return 18; }
    @Override public int getBaseVariation() { return 3; }

    @Override public int getBorderWidth() { return 5; }

    @Override public int getPegWidth() { return 4; }
    @Override public int getPegLength() { return 2; }
    @Override public int getPegRadius() { return 6; }
    
    @Override public int getPegPositionDelta() { return 4; }
    @Override public int getPegRadiusDelta() { return -1; }
    @Override public int getPegHeightDelta() { return -1; }
    
    @Override public int getEdgeColorChange() { return 16 * 0x010101; }
}
