package cfh.puzzle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Size {

    private transient int width;
    private transient int height;
    
    public static Size read(ObjectInputStream input) throws IOException {
        String name = input.readUTF();
        switch (name) {
            case "TemplateSizeImpl": return TemplateSizeImpl.read0(input);
            default: throw new IOException("unhandled Size type: " + name);
        }
    }
    
    
    protected Size() {
    }

    public final void write(ObjectOutputStream output) throws IOException {
        output.writeUTF(getClass().getSimpleName());
        write0(output);
    }

    public abstract int getCount();
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
    
    protected abstract void write0(ObjectOutputStream out) throws IOException;
    
    void width(int width) {
        this.width= width;
    }
    
    public int width() {
        return width;
    }
    
    void height(int height) {
        this.height = height;
    }
    
    public int height() {
        return height;
    }
    
    
}