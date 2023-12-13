import processing.core.PImage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class PersonFull implements Move, Transform, Executable {

    private final String id;
    private Point position;
    private final List<PImage> images;
    private int imageIndex;
    private final double actionPeriod;
    private final double animationPeriod;
    private final int resourceLimit;
    private int resourceCount;
    

    public PersonFull(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod, int resourceCount, int resourceLimit) {
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
        this.resourceCount = resourceCount;
        this.resourceLimit = resourceLimit;
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
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        if (!WorldView.isGrayscale()) {
        Optional<Entity> fullTarget = world.findNearest(position,  Arrays.asList(House.class));
        if (fullTarget.isPresent() && move(world, fullTarget.get(), scheduler)) {
            transform(world, scheduler, imageStore);
        } else {
            scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        }
    }
    }


    @Override
    public boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (!WorldView.isGrayscale()) {
        PersonSearching dude = Factory.createPersonSearching(id, position, actionPeriod, animationPeriod, resourceLimit, images);
        
        world.removeEntity(scheduler, this);
        world.tryAddEntity(dude);
        dude.scheduleActions(scheduler, world, imageStore);
        
        return true;
        }
        return false;
    }

    @Override
    public boolean move(WorldModel world, Entity target, EventScheduler scheduler) {
        if (!WorldView.isGrayscale()) {
        if (position.adjacent(target.getPosition())) {
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
            return !occupant.isPresent() || occupant.get() instanceof Stump;
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
    

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this,
                new Activity(this, world, imageStore),
                actionPeriod);
        scheduler.scheduleEvent(this,
                new Animation(this,world, imageStore, 0),
                getAnimationPeriod());
    }

}