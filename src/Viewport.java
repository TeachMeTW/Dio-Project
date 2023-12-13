/**
 * The window with which we are viewing the world.  The WorldModel
 * is larger than the window we are using to look into the world.
 * The Viewport has the row and column for where we are to start viewing
 * and then the number of rows and columns we wish to currently view from
 * that starting point.
 */
public final class Viewport {
    private int row;
    private int col;
    private final int numRows;
    private final int numCols;

    public Viewport(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
    }

    /**
     * Checks if a point is in the current view of the world
     * we have.
     * @param p - the location we are checking
     * @return - true if the location is in the viewport, and false otherwise
     */
    public boolean contains(Point p) {
        return p.y >= row && p.y < row + numRows && p.x >= col && p.x < col + numCols;
    }

    /**
     * Shift our camera / viewport so we see a different part of the world
     * @param col - the column we want to move our camera / viewport to
     * @param row - the row we want to move our camera / viewport to
     */
    public void shift(int col, int row) {
        this.col = col;
        this.row = row;
    }

    /**
     * The viewport is a smaller grid withing our larger WorldModel grid.
     * This function does the math to take a location in our WorldModel and
     * transform it to be the proper location within our current view.
     * @param col - column from the world model
     * @param row - row from the world model
     * @return - location in the context of our viewport instead of world model
     */
    public Point worldToViewport(int col, int row) {
        return new Point(col - this.col, row - this.row);
    }

    /**
     * The viewport is a smaller grid withing our larger WorldModel grid.
     * This function does the math to take a location in our Viewport
     * (e.g. current view) and transforms it to be the proper location
     * within our WorldModel.
     *
     * @param col - column from the viewport
     * @param row - row from the viewport
     * @return - location in the context of our world model, not viewport
     */
    public Point viewportToWorld(int col, int row) {
        return new Point(col + this.col, row + this.row);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }
}
