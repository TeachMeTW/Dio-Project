public class Activity implements Action {
    private final Entity entity;
    private final WorldModel world;
    private final ImageStore imageStore;

    public Activity(Entity entity, WorldModel world, ImageStore imageStore) {
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
    }

    @Override
    public void executeAction(EventScheduler scheduler) {
        if (entity instanceof Sapling) {
            ((Sapling) entity).executeActivity(world, imageStore, scheduler);
        } else if (entity instanceof Tree) {
            ((Tree) entity).executeActivity(world, imageStore, scheduler);
        } else if (entity instanceof Fairy) {
            ((Fairy) entity).executeActivity(world, imageStore, scheduler);
        } else if (entity instanceof Dio) {
            ((Dio) entity).executeActivity(world, imageStore, scheduler);
        } else if (entity instanceof AmongUs) {
            ((AmongUs) entity).executeActivity(world, imageStore, scheduler);
        } else if (entity instanceof PersonSearching) {
            ((PersonSearching) entity).executeActivity(world, imageStore, scheduler);
        } else if (entity instanceof PersonFull) {
            ((PersonFull) entity).executeActivity(world, imageStore, scheduler);
        }
        else if (entity instanceof DioAttack) { // Add this block to handle DioAttack
        ((DioAttack) entity).executeActivity(world, imageStore, scheduler); 
        }
        else {
            throw new UnsupportedOperationException("Unsupported entity type: " + entity.getClass().getSimpleName());
        }
    }
}
