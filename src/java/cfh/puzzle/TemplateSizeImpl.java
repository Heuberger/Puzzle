package cfh.puzzle;


public class TemplateSizeImpl implements Size {

    private final int count;
    private final Template template;

    TemplateSizeImpl(int count, Template template) {
        this.count = count;
        this.template = template;
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
