/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

/**
 * @author Carlos F. Heuberger
 */
public class Template75 extends Template {

    @Override public int getSizeX() { return 75; }
    @Override public int getSizeY() { return getSizeX(); }

    @Override public int getOverlap() { return 30; }
    @Override public int getBaseVariation() { return 7; }

    @Override public int getBorderWidth() { return 7; }

    @Override public int getPegWidth() { return 6; }
    @Override public int getPegLength() { return 3; }
    @Override public int getPegRadius() { return 9; }
    
    @Override public int getPegPositionDelta() { return 5; }
    @Override public int getPegRadiusDelta() { return 3; }
    @Override public int getPegHeightDelta() { return 3; }
    
    @Override public int getEdgeColorChange() { return 32 * 0x010101; }
}
