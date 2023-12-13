import processing.core.PImage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.stream.Stream;

public class Fairy implements Move, Executable{
    private final String id;
    private Point position;
    private final List<PImage> images;
    private int imageIndex;
    private final double actionPeriod;
    private final double animationPeriod;

    public Fairy(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    // Will refactor to default? Since we use these for most things
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
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        if (!WorldView.isGrayscale()) {
        Optional<Entity> fairyTarget = world.findNearest(position,  Arrays.asList(Stump.class));
        // System.out.println("Fairy target: " + fairyTarget.get().getPosition());
        // System.out.println("Currently" + position);
        if (fairyTarget.isPresent()) {
            Point tgtPos = fairyTarget.get().getPosition();
    
            if (move(world, fairyTarget.get(), scheduler)) {
                Executable sapling = Factory.createSapling(WorldLoader.SAPLING_KEY + "_" + fairyTarget.get().getId(), tgtPos, imageStore.getImageList(WorldLoader.SAPLING_KEY));
                world.tryAddEntity(sapling);
                
                sapling.scheduleActions(scheduler, world, imageStore);
            }
        }
    
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
    }
    }

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        scheduler.scheduleEvent(this, new Animation(this, world, imageStore, 0), getAnimationPeriod());
    }

    @Override
    public boolean move(WorldModel world, Entity target, EventScheduler scheduler) {
        // System.out.println("Target" + target.getPosition());
        if (!WorldView.isGrayscale()) {
        if (position.adjacent(target.getPosition())) {
            world.removeEntity(scheduler, target);
            return true;
        } else {
            Point nextPos = nextPosition(world, target.getPosition());

            if (!position.equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }
    return false;
    }

    @Override
    public Point nextPosition(WorldModel world, Point destPos) {
        PathingStrategy strategy = new AStarPathingStrategy();
        Predicate<Point> canPassThrough = p -> {
            // Check if the point is within bounds of the world
            if (!isWithinBounds(p, world.getNumRows(), world.getNumCols())) {
                return false;
            }

            Optional<Entity> occupant = world.getOccupant(p);
            // Check if the space is empty or occupied by a House
            return !occupant.isPresent() || occupant.get() instanceof House;
        };

        BiPredicate<Point, Point> withinReach = (p1, p2) -> {
            // Check if the point is reachable horizontally or vertically in one step
            return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) == 1;
        };

        List<Point> path = strategy.computePath(position, destPos, canPassThrough, withinReach, PathingStrategy.CARDINAL_NEIGHBORS);

        return path.isEmpty() ? position : path.get(0);
    }

    private boolean isWithinBounds(Point p, int numRows, int numCols) {
        return p.y >= 0 && p.y < numRows && p.x >= 0 && p.x < numCols;
    }

    
}
