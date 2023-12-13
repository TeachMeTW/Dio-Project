import processing.core.PImage;
import java.util.List;

public class Stump implements Entity {
    private final String id;
    private Point position;
    private final List<PImage> images;
    private int imageIndex;

    public Stump(String id, Point position, List<PImage> images) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
    }

    // Will refactor to default? Since we use these for most things
    @Override
    public PImage getCurrentImage() {
        return this.images.get(this.imageIndex % this.images.size());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public void setPosition(Point pos) {
        this.position = pos;
    }

    @Override
    public void nextImage() {
        imageIndex = imageIndex + 1;
    }

    @Override
    public double getAnimationPeriod() {
        throw new UnsupportedOperationException("getAnimationPeriod not supported");
    }

    @Override
    public String log() {
        return this.id.isEmpty() ? null :
        String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
    }

    
}
