public class Animation implements Action {
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    public Animation(Entity entity, WorldModel world, ImageStore imageStore, int repeatCount)
    {
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = repeatCount;
    }

    public static Animation createAnimationAction(Entity entity, int repeatCount)
    {
        return new Animation( entity, null, null, repeatCount);
    }

    @Override
    public void executeAction(EventScheduler scheduler) {
        entity.nextImage();

        if (repeatCount != 1) {
            scheduler.scheduleEvent(this.entity, createAnimationAction(this.entity, Math.max(repeatCount - 1, 0)), this.entity.getAnimationPeriod());
        }
    }
}