import processing.core.PImage;
import java.util.List;

public class Tree implements Transform, Executable {

    private String id;
    private Point position;
    private List<PImage> images;
    private int imageIndex;
    private final double actionPeriod;
    private final double animationPeriod;
    private int health;

    public Tree(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod, int health) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.health = health;
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
        if (!WorldView.isGrayscale()) {
        imageIndex = imageIndex + 1;
        }
    }

    @Override
    public double getAnimationPeriod() {
        return animationPeriod;
    }
    

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        scheduler.scheduleEvent(this, new Animation(this,world, imageStore, 0), getAnimationPeriod());
    }

    @Override
    public boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (!WorldView.isGrayscale()) {
        if (health <= 0) {
            Entity stump = Factory.createStump(WorldLoader.STUMP_KEY + "_" + id, position, imageStore.getImageList(WorldLoader.STUMP_KEY));
            world.removeEntity(scheduler, this);
            world.tryAddEntity(stump);
            return true;
        }
        return false;
    }
    return false;
    }

    @Override
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        if (!transform(world, scheduler, imageStore)) {

            scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        }
    }

    public void decreaseHealth(int amount) {
        this.health -= amount;
    }

    public int getHealth() {
        return this.health;
    }
    
}