public interface Move extends Entity {
    boolean move(WorldModel world, Entity target, EventScheduler scheduler);
    Point nextPosition(WorldModel world, Point destPos);
}
