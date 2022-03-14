/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

/**
 * @author Carlos F. Heuberger
 */
public class Template65 extends Template {

    @Override public int getSizeX() { return 65; }
    @Override public int getSizeY() { return getSizeX(); }

    @Override public int getOverlap() { return 25; }
    @Override public int getBaseVariation() { return 4; }

    @Override public int getBorderWidth() { return 5; }

    @Override public int getPegWidth() { return 5; }
    @Override public int getPegLength() { return 2; }
    @Override public int getPegRadius() { return 8; }
    
    @Override public int getPegPositionDelta() { return 4; }
    @Override public int getPegRadiusDelta() { return 2; }
    @Override public int getPegHeightDelta() { return 2; }
    
    @Override public int getEdgeColorChange() { return 32 * 0x010101; }
}
