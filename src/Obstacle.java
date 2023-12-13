import processing.core.PImage;
import java.util.List;

public class Obstacle implements Executable {

    private final String id;
    private Point position;
    private final List<PImage> images;
    private int imageIndex;
    private final double animationPeriod;

    public Obstacle(String id, Point position, List<PImage> images, double animationPeriod) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.animationPeriod = animationPeriod;
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
    public PImage getCurrentImage() {
        return this.images.get(this.imageIndex % this.images.size());
    }

    @Override
    public String log() {
        return this.id.isEmpty() ? null :
        String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
    }

    @Override
    public void nextImage() {
        imageIndex = imageIndex + 1;
    }

    @Override
    public double getAnimationPeriod() {
        return animationPeriod;
    }

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, new Animation(this, world, imageStore, 0), getAnimationPeriod());
    }

    @Override
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {

    }

    
}
