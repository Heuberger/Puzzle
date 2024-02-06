/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Carlos F. Heuberger
 */
public abstract class Template {

    public static Template get(String name) {
		switch (name) {
		    case "40": return new Template40();
			case "50": return new Template50(); 
			case "55": return new Template55(); 
			case "60": return new Template60(); 
			case "65": return new Template65(); 
			case "75": return new Template75(); 
			case "85": return new Template85(); 
			default: throw new IllegalArgumentException("unknown template: " + name);
		}
	}
	
    public static Template read(ObjectInputStream input) throws IOException {
        String name = input.readUTF();
        return get(name);
    }
    
    protected Template() {
    }
    
    public void write(ObjectOutputStream output) throws IOException {
        output.writeUTF(getClass().getSimpleName().substring(8));
    }
	
    public abstract int getSizeX();
    public abstract int getSizeY();
    
    public abstract int getOverlap();
    public abstract int getBaseVariation();
    
    public abstract int getBorderWidth();
    
    public abstract int getPegWidth();
    public abstract int getPegLength();
    public abstract int getPegRadius();
    
    public abstract int getPegPositionDelta();
    public abstract int getPegRadiusDelta();
    public abstract int getPegHeightDelta();
    
    public abstract int getEdgeColorChange();
}
