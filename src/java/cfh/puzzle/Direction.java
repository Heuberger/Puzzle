/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

/**
 * @author Carlos F. Heuberger
 */
public enum Direction {

    NORTH(0),
    EAST(90),
    SOUTH(180),
    WEST(270);
    
    public final double angle;
    
    private Direction(int deg) {
        angle = Math.toRadians(deg);
    }
    
    public Direction getNext() {
        int next = (this.ordinal()>0 ? this.ordinal() : values().length) - 1; 
        return values()[next];
    }

    public Direction getPrev() {
        int next = (this.ordinal() + 1) % values().length; 
        return values()[next];
    }
}
