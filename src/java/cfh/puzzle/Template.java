package cfh.puzzle;

import java.io.Serializable;

public abstract class Template implements Serializable {

    public static Template get(String name) {
		switch (name) {
			case "50": return new Template50(); 
			case "55": return new Template55(); 
			case "60": return new Template60(); 
			case "65": return new Template65(); 
			case "85": return new Template85(); 
			default: throw new RuntimeException("unknown template: " + name);
		}
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
