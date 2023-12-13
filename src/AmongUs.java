import processing.core.PImage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AmongUs implements Move, Executable {
    private final String id;
    private Point position;
    private final List<PImage> images;
    private int imageIndex;
    private final double actionPeriod;
    private final double animationPeriod;
    private final Random random = new Random();

    public AmongUs(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod) {
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
        WorldView.isGrayscale(); // Update the grayscale state
        if (!WorldView.isGrayscale) {
            move(world, null, scheduler); // Move if not in grayscale mode
        }

        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
    }

    @Override
    public boolean move(WorldModel world, Entity target, EventScheduler scheduler) {
        if (!WorldView.isGrayscale) { // Only move if not in grayscale mode
            Point nextPos = nextPosition(world, this.position);

            if (!position.equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
                return true;
            }
        }
        return false;
    }

    @Override
    public Point nextPosition(WorldModel world, Point destPos) {
        if (!WorldView.isGrayscale()) {
        Stream<Point> potentialMoves = PathingStrategy.CARDINAL_NEIGHBORS.apply(position);
        List<Point> shuffledMoves = potentialMoves.collect(Collectors.toList());
        Collections.shuffle(shuffledMoves, random);

        for (Point newPos : shuffledMoves) {
            if (isWithinBounds(newPos, world.getNumRows(), world.getNumCols())) {
                Optional<Entity> occupant = world.getOccupant(newPos);
                if (!occupant.isPresent()) { // Check if the position is not occupied
                    return newPos;
                }
            }
        }
        return position; // Remain in the same position if none are valid
    }
    return position;
    }

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        scheduler.scheduleEvent(this, new Animation(this, world, imageStore, 0), getAnimationPeriod());
    }

    private boolean isWithinBounds(Point p, int numRows, int numCols) {
        return p.y >= 0 && p.y < numRows && p.x >= 0 && p.x < numCols;
    }
}

