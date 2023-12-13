import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AStarPathingStrategy implements PathingStrategy {

    private PriorityQueue<PointNode> openSet;
    private Set<Point> closedSet;
    private Map<Point, Integer> gCosts;
    private Map<Point, Point> cameFrom;

    public AStarPathingStrategy() {
        // Initialize the sets and maps used in the algorithm
        openSet = new PriorityQueue<>(Comparator.comparingInt(PointNode::getFcost));
        closedSet = new HashSet<>();
        gCosts = new HashMap<>();
        cameFrom = new HashMap<>();
    }

    @Override
    public List<Point> computePath(Point start, Point end,
                                   Predicate<Point> canPassThrough,
                                   BiPredicate<Point, Point> withinReach,
                                   Function<Point, Stream<Point>> potentialNeighbors) {
        // A* Algorithm: Step 1 and 2 - Initialize and add start node to open list
        initializePathFinding(start, end);
        return executeAStarSearch(start, end, canPassThrough, withinReach, potentialNeighbors);
    }

    private void initializePathFinding(Point start, Point end) {
        // Clear previous data and add start node to open set
        openSet.clear();
        closedSet.clear();
        gCosts.clear();
        cameFrom.clear();

        openSet.add(new PointNode(start, 0, estimateDistance(start, end)));
        gCosts.put(start, 0);
    }

    private List<Point> executeAStarSearch(Point start, Point end,
                                           Predicate<Point> canPassThrough,
                                           BiPredicate<Point, Point> withinReach,
                                           Function<Point, Stream<Point>> potentialNeighbors) {
        // A* Algorithm: Step 3 to 6 - Main search loop
        while (!openSet.isEmpty()) {
            // Step 5: Choose node from open list with smallest f value
            Point current = openSet.poll().getPoint();

            // Step 6: Check if the current node is within reach of the end point
            if (withinReach.test(current, end)) {
                return reconstructPath(current); // If within reach, construct the path
            }

            // Step 4: Move current node to the closed list
            closedSet.add(current);

            // Step 3: Analyze valid adjacent nodes
            processNeighbors(current, end, canPassThrough, potentialNeighbors);
        }

        return Collections.emptyList(); // Return an empty path if no path is found
    }

    private void processNeighbors(Point current, Point end,
                                  Predicate<Point> canPassThrough,
                                  Function<Point, Stream<Point>> potentialNeighbors) {
        // Step 3: Process each valid neighbor
        potentialNeighbors.apply(current)
                .filter(canPassThrough)
                .filter(p -> !closedSet.contains(p))
                .forEach(neighbor -> updateNeighborIfNeeded(current, neighbor, end));
    }

    private void updateNeighborIfNeeded(Point current, Point neighbor, Point end) {
        // Step 3a: Calculate tentative g value
        int tentativeGCost = gCosts.get(current) + 1;

        // Step 3b and 3c: Check and update the neighbor if needed
        if (tentativeGCost < gCosts.getOrDefault(neighbor, Integer.MAX_VALUE)) {
            updatePath(current, neighbor, tentativeGCost, end);
        }
    }

    private void updatePath(Point current, Point neighbor, int tentativeGCost, Point end) {
        // Steps 3d, 3e, and 3f: Update g, f values and add the neighbor to the open list
        cameFrom.put(neighbor, current);
        gCosts.put(neighbor, tentativeGCost);
        int fCost = tentativeGCost + estimateDistance(neighbor, end);
        openSet.add(new PointNode(neighbor, tentativeGCost, fCost));
    }

    private List<Point> reconstructPath(Point current) {
        // Reconstruct the path from the end point to the start point
        LinkedList<Point> path = new LinkedList<>();
        while (cameFrom.containsKey(current)) {
            path.addFirst(current);
            current = cameFrom.get(current);
        }
        return path;
    }

    private int estimateDistance(Point from, Point to) {
        // Heuristic function: Manhattan distance
        return Math.abs(to.x - from.x) + Math.abs(to.y - from.y);
    }

    private static class PointNode {
        private final Point point;
        private final int fCost;

        public PointNode(Point point, int gCost, int fCost) {
            this.point = point;
            this.fCost = fCost;
        }

        public Point getPoint() {
            return point;
        }

        public int getFcost() {
            return fCost;
        }
    }
}
