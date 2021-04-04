package cfh.puzzle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TemplateSizeImpl extends Size {

    private final int count;
    private final Template template;

    protected static TemplateSizeImpl read0(ObjectInputStream input) throws IOException {
        int count = input.readInt();
        Template template = Template.read(input);
        return new TemplateSizeImpl(count, template);
    }
    
    
    TemplateSizeImpl(int count, Template template) {
        this.count = count;
        this.template = template;
    }

    @Override
    protected void write0(ObjectOutputStream output) throws IOException {
        output.writeInt(count);
        template.write(output);
    }
    
    @Override
    public int getCount() {
        return count;
    }
    
    @Override
    public int getSizeX() {
        return template.getSizeX();
    }

    @Override
    public int getSizeY() {
        return template.getSizeY();
    }

    @Override
    public int getOverlap() {
        return template.getOverlap();
    }

    @Override
    public int getBaseVariation() {
        return template.getBaseVariation();
    }

    @Override
    public int getBorderWidth() {
        return template.getBorderWidth();
    }

    @Override
    public int getPegWidth() {
        return template.getPegWidth();
    }

    @Override
    public int getPegLength() {
        return template.getPegLength();
    }

    @Override
    public int getPegRadius() {
        return template.getPegRadius();
    }

    @Override
    public int getPegPositionDelta() {
        return template.getPegPositionDelta();
    }

    @Override
    public int getPegRadiusDelta() {
        return template.getPegRadiusDelta();
    }

    @Override
    public int getPegHeightDelta() {
        return template.getPegHeightDelta();
    }

    @Override
    public int getEdgeColorChange() {
        return template.getEdgeColorChange();
    }
    
    @Override
    public String toString() {
        return count + "x" + template.getClass().getSimpleName();
    }
}
