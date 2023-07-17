/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

/**
 * @author Carlos F. Heuberger
 */
public class Template85 extends Template {

    @Override public int getSizeX() { return 85; }
    @Override public int getSizeY() { return getSizeX(); }

    @Override public int getOverlap() { return 35; }
    @Override public int getBaseVariation() { return 10; }

    @Override public int getBorderWidth() { return 8; }

    @Override public int getPegWidth() { return 7; }
    @Override public int getPegLength() { return 5; }
    @Override public int getPegRadius() { return 11; }
    
    @Override public int getPegPositionDelta() { return 6; }
    @Override public int getPegRadiusDelta() { return 4; }
    @Override public int getPegHeightDelta() { return 4; }
    
    @Override public int getEdgeColorChange() { return 64 * 0x010101; }
}
