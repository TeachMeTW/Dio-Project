import processing.core.PImage;

import java.util.*;

/**
 * Represents the 2D World in which this simulation is running.
 * Keeps track of the size of the world, the background image for each
 * location in the world, and the entities that populate the world.
 */
public final class WorldModel {
    private int numRows;
    private int numCols;
    private Background[][] background;
    private Entity[][] occupancy;
    private Set<Entity> entities;

    public Optional<PImage> getBackgroundImage(Point pos) {
        if (withinBounds(pos)) {
            return Optional.of(getBackgroundCell(pos).getCurrentImage());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Set a background cell at a specific location.
     * Not currently being used, but may want to use on Project 5.
     *
     * @param pos - location in the grid we are placing the background at
     * @param background - the Background object (with the associated image)
     */
    public void setBackgroundCell(Point pos, Background background) {
        this.background[pos.y][pos.x] = background;
    }

    private Background getBackgroundCell(Point pos) {
        return background[pos.y][pos.x];
    }


    /**
     * Removes and un-schedules a given entity at the specified location.
     * @param scheduler - the EventScheduler which handles all upcoming events
     * @param entity - the entity we wish to remove and un-schedule
     */
    public void removeEntity(EventScheduler scheduler, Entity entity) {
        // System.out.println("REMOVED" + entity);
        scheduler.unscheduleAllEvents(entity);
        this.removeEntityAt(entity.getPosition());
    }

    private void removeEntityAt(Point pos) {
        if (withinBounds(pos) && this.getOccupancyCell(pos) != null) {
            Entity entity = this.getOccupancyCell(pos);

            /* This moves the entity just outside of the grid for
              debugging purposes. */
            entity.setPosition(new Point(-1, -1));
            entities.remove(entity);
            this.setOccupancyCell(pos, null);
        }
    }

    /**
     * Moves an entity from its current location to the specified location, pos.
     * If there is an occupant in the location we plan to move we remove and un-schedule it.
     * @param scheduler - the EventScheduler which handles all upcoming events
     * @param entity - the entity we wish to move
     * @param pos - the location the entity is to move to
     */
    public void moveEntity(EventScheduler scheduler, Entity entity, Point pos) {
        Point oldPos = entity.getPosition();
        if (withinBounds(pos) && !pos.equals(oldPos)) {
            this.setOccupancyCell(oldPos, null);
            Optional<Entity> occupant = this.getOccupant(pos);
            occupant.ifPresent(target -> this.removeEntity(scheduler, target));
            this.setOccupancyCell(pos, entity);
            entity.setPosition(pos);
        }
    }

    public void tryAddEntity(Entity entity) {
        if (this.isInBoundsAndOccupied(entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        this.addEntity(entity);
    }

    /**
     * Adds an entity to the WorldModel at its specified location.
     * Assumes that there is no entity currently occupying the
     * intended destination cell.
     * @param entity - the entity we are adding
     */
    private void addEntity(Entity entity) {
        if (withinBounds(entity.getPosition())) {
            this.setOccupancyCell(entity.getPosition(), entity);
            entities.add(entity);
        }
    }

    private void setOccupancyCell(Point pos, Entity entity) {
        occupancy[pos.y][pos.x] = entity;
    }

    public Optional<Entity> getOccupant(Point pos) {
        if (isInBoundsAndOccupied(pos)) {
            return Optional.of(this.getOccupancyCell(pos));
        } else {
            return Optional.empty();
        }
    }

    private Entity getOccupancyCell(Point pos) {
        return occupancy[pos.y][pos.x];
    }

    /**
     * Returns true if a location is both in bounds and occupied.
     * If a point is out of bounds, we do not want to check if it is occupied
     * as indexing into an out-of-bounds spot would crash.
     *
     * @param pos - the location we are checking
     * @return true if location is both in bounds and occupied.
     */
    private boolean isInBoundsAndOccupied(Point pos) {
        return withinBounds(pos) && this.getOccupancyCell(pos) != null;
    }

    private boolean withinBounds(Point pos) {
        return pos.y >= 0 && pos.y < numRows && pos.x >= 0 && pos.x < numCols;
    }


    public Optional<Entity> findNearest(Point pos, List<Class> types) {
    
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : entities) {
            for (Class type : types) {
                if (type.isInstance(entity)) {
                    ofType.add(entity);
                    break; // Avoid adding the entity multiple times
                }
            }
        }
      
        return nearestEntity(ofType, pos);
    }
  
    private static Optional<Entity> nearestEntity(List<Entity> entities, Point pos) {
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            Entity nearest = entities.get(0);
            int nearestDistance = distanceSquared(nearest.getPosition(), pos);

            for (Entity other : entities) {
                int otherDistance = distanceSquared(other.getPosition(), pos);

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    private static int distanceSquared(Point p1, Point p2) {
        int deltaX = p1.x - p2.x;
        int deltaY = p1.y - p2.y;

        return deltaX * deltaX + deltaY * deltaY;
    }

    /**
     * Helper method for testing. Don't move or modify this method.
     */
    public List<String> log(){
        List<String> list = new ArrayList<>();
        for (Entity entity : entities) {
            String log = entity.log();
            if(log != null) list.add(log);
        }
        return list;
    }


    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public void setNumCols(int numCols) {
        this.numCols = numCols;
    }

    public Background[][] getBackground() {
        return background;
    }

    public void setBackground(Background[][] background) {
        this.background = background;
    }

    public Entity[][] getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(Entity[][] occupancy) {
        this.occupancy = occupancy;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public void setEntities(Set<Entity> entities) {
        this.entities = entities;
    }


    private boolean stopEntities = false;

    public void setStopEntities(boolean stop) {
        this.stopEntities = stop;
    }

    public boolean shouldStopEntities() {
        return this.stopEntities;
    }
}
