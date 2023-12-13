import processing.core.PImage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Dio implements Move, Executable {
    private final String id;
    private Point position;
    private final List<PImage> images;
    private int imageIndex;
    private final double actionPeriod;
    private final double animationPeriod;
    private long pauseEndTime = 0;
    public Point targetpos;

    public Dio(String id, Point position, List<PImage> images, double actionPeriod, double animationPeriod) {
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
        imageIndex = imageIndex + 1;
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
        
        Optional<Entity> target = findNearestTarget(world);
        if (target.isPresent()) {
            targetpos = target.get().getPosition();
            if (move(world, target.get(), scheduler)) {
                interactWithTarget(world, target.get(), scheduler, imageStore);
            }
        }
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), this.actionPeriod);
    }

    private Optional<Entity> findNearestTarget(WorldModel world) {
        Optional<Entity> nearestFairy = world.findNearest(this.position, Arrays.asList(Fairy.class));
        Optional<Entity> nearestPerson = world.findNearest(this.position, Arrays.asList(PersonSearching.class, PersonFull.class));
        Optional<Entity> nearestAmongUs = world.findNearest(this.position, Arrays.asList(AmongUs.class));
        
        // Start with no nearest target found
        Optional<Entity> nearestTarget = Optional.empty();
        int nearestDistance = Integer.MAX_VALUE;
    
        // Check each entity type for the nearest target
        if (nearestFairy.isPresent()) {
            int distanceToFairy = calculateSquaredDistance(this.position, nearestFairy.get().getPosition());
            if (distanceToFairy < nearestDistance) {
                nearestDistance = distanceToFairy;
                nearestTarget = nearestFairy;
            }
        }
        
        if (nearestPerson.isPresent()) {
            int distanceToPerson = calculateSquaredDistance(this.position, nearestPerson.get().getPosition());
            if (distanceToPerson < nearestDistance) {
                nearestDistance = distanceToPerson;
                nearestTarget = nearestPerson;
            }
        }
        
        if (nearestAmongUs.isPresent()) {
            int distanceToAmongUs = calculateSquaredDistance(this.position, nearestAmongUs.get().getPosition());
            if (distanceToAmongUs < nearestDistance) {
                nearestDistance = distanceToAmongUs;
                nearestTarget = nearestAmongUs;
            }
        }
        
        return nearestTarget;
    }

    private int calculateSquaredDistance(Point p1, Point p2) {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        return dx * dx + dy * dy;
    }

    private void interactWithTarget(WorldModel world, Entity target, EventScheduler scheduler, ImageStore imageStore) {
        if (target instanceof AmongUs) {
            transformAmongUs(world, (AmongUs) target, scheduler, imageStore);
            transformToAttack(world, imageStore, scheduler);
            SoundPlayer.playSound("amogdeath.wav"); // Play a different sound for AmongUs
        } else {
            transformPerson(world, target, scheduler, imageStore);
            transformToAttack(world, imageStore, scheduler);
            SoundPlayer.playSound("MUDA.wav"); // Original sound for other entities
        }
    }

    private void transformAmongUs(WorldModel world, AmongUs amongUs, EventScheduler scheduler, ImageStore imageStore) {
        List<PImage> deadImages = imageStore.getImageList("deadamongus");
        DeadAmongUs deadAmongUs = new DeadAmongUs(amongUs.getId(), targetpos, deadImages);
        
        world.removeEntity(scheduler, amongUs);
        world.tryAddEntity(deadAmongUs);
    }

    private void transformPerson(WorldModel world,Entity target, EventScheduler scheduler, ImageStore imageStore) {
        List<PImage> deadImages = imageStore.getImageList("deadperson");
        DeadPerson deadPerson = new DeadPerson(target.getId(), targetpos, deadImages);
        
        world.removeEntity(scheduler, target);
        world.tryAddEntity(deadPerson);
    }

    private void transformToAttack(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        List<PImage> attackImages = imageStore.getImageList("dioattack");
        DioAttack attackForm = new DioAttack(this, this.id, this.position, attackImages, this.actionPeriod, this.animationPeriod);
        
        world.removeEntity(scheduler, this);
        world.tryAddEntity(attackForm);
        attackForm.scheduleActions(scheduler, world, imageStore);
    }

    @Override
    public void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        scheduler.scheduleEvent(this, new Activity(this, world, imageStore), actionPeriod);
        scheduler.scheduleEvent(this, new Animation(this, world, imageStore, 0), getAnimationPeriod());
    }

    @Override
    public boolean move(WorldModel world, Entity target, EventScheduler scheduler) {
        
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
            return !occupant.isPresent();
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
