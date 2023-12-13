import java.util.List;
import processing.core.PImage;

public class Sapling implements Transform, Executable {
    private final String id;
    private Point position;
    private final List<PImage> images;
    private int imageIndex;
    private final double actionPeriod;
    private final double animationPeriod;
    private int health;
    private final int healthLimit;

    public Sapling(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod, int healthLimit) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.health = 0; // Initial health can be set as needed
        this.healthLimit = healthLimit;
    }

    // Will refactor to default? Since we use these for most things
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public PImage getCurrentImage() {
        return this.images.get(this.imageIndex % this.images.size());
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
    public String log() {
        return this.id.isEmpty() ? null :
        String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
    }

    public void decreaseHealth(int amount) {
        this.health -= amount;
    }

    public int getHealth() {
        return this.health;
    }

    
    
    @Override
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        // System.out.println("Health Incr");
        if (!WorldView.isGrayscale()) {
        this.health++;
        if (!transform(world, scheduler, imageStore)) {
            scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        }
    }
    }

    @Override
    public boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (!WorldView.isGrayscale()) {
        if (health <= 0) {
            Entity stump = Factory.createStump(WorldLoader.STUMP_KEY + "_" + id, position, imageStore.getImageList(WorldLoader.STUMP_KEY));
            // System.out.println("Stump created at: " + position);
            world.removeEntity(scheduler, this);
            world.tryAddEntity(stump);
            return true;
        } else if (health >= healthLimit) {
            Executable tree = Factory.createTreeWithDefaults(WorldLoader.TREE_KEY + "_" + id, position, imageStore.getImageList(WorldLoader.TREE_KEY));
            // System.out.println("Tree created at: " + position);
            world.removeEntity(scheduler, this);
            world.tryAddEntity(tree);
            tree.scheduleActions(scheduler, world, imageStore);
            return true;
        }
        return false;
    }
    return false;
    }


    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        scheduler.scheduleEvent(this, new Animation(this,world,imageStore, 0), getAnimationPeriod());
    }
}
