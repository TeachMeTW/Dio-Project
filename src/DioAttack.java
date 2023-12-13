import processing.core.PImage;

import java.util.List;

public class DioAttack implements Executable {
    private final String id;
    private Point position;
    private final List<PImage> attackImages;
    private int imageIndex;
    private final double actionPeriod;
    private final double animationPeriod;
    private final long transformationTime;
    private final Dio originalDio;
    private boolean hasTransformedBack;

    public DioAttack(Dio originalDio, String id, Point position, List<PImage> attackImages, double actionPeriod, double animationPeriod) {
        this.originalDio = originalDio;
        this.id = id;
        this.position = position;
        this.attackImages = attackImages;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.transformationTime = System.currentTimeMillis();
        this.hasTransformedBack = false;
    }

    @Override
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        long currentTime = System.currentTimeMillis();
        if (!hasTransformedBack && currentTime - transformationTime >= 1000) {
            revertToOriginalDio(world, imageStore, scheduler);
            hasTransformedBack = true;
        } else if (!hasTransformedBack) {
            // Continue to animate DioAttack
            nextImage();
            // Reschedule this Activity event to keep checking if it's time to revert back to Dio
            scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        }
    }

    private void revertToOriginalDio(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        // Remove DioAttack from the world
        
        originalDio.setPosition(this.getPosition());
        world.removeEntity(scheduler, this);

        // Set the position of original Dio to the current position of DioAttack before adding it back
        
        
        try {
            world.tryAddEntity(originalDio);
            originalDio.scheduleActions(scheduler, world, imageStore); // Schedule Dio's actions
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to add Original Dio back to the world at position: " + this.position);
            System.out.println(e.getMessage());
        }
    }
    

    @Override
    public String log() {
        return this.id.isEmpty() ? null :
        String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
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
        imageIndex = (imageIndex + 1) % attackImages.size();
    }

    @Override
    public PImage getCurrentImage() {
        return attackImages.get(imageIndex);
    }


    @Override
    public double getAnimationPeriod() {
        return animationPeriod;
    }

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this,
            new Activity(this, world, imageStore), actionPeriod);
        scheduler.scheduleEvent(this,
            new Animation(this, world, imageStore, 0), getAnimationPeriod());
    }

}
