import processing.core.PApplet;
import processing.core.PImage;
import java.util.Optional;

public final class WorldView {
    private final PApplet screen;
    private final WorldModel world;
    private final int tileWidth;
    private final int tileHeight;
    private final Viewport viewport;
    public static boolean isGrayscale = false;
    private long grayscaleStartTime;
    private boolean hasPlayedResumeAudio = false;
    public static boolean isTimeStopPlaying = false;
    public static boolean isResumePlaying = false;

    public WorldView(int numRows, int numCols, PApplet screen, WorldModel world, int tileWidth, int tileHeight) {
        this.screen = screen;
        this.world = world;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.viewport = new Viewport(numRows, numCols);
    }
    public static void setGrayscale(boolean grayscale) {
        WorldView.isGrayscale = grayscale;
    }

    public static boolean isGrayscale() {
        return WorldView.isGrayscale;
    }

    public static void setTimeStopPlaying(boolean playing) {
        WorldView.isTimeStopPlaying = playing;
    }

    public static boolean isTimeStopPlaying() {
        return WorldView.isTimeStopPlaying;
    }

    public static void setResumePlaying(boolean playing) {
        WorldView.isResumePlaying = playing;
    }

    public static boolean isResumePlaying() {
        return WorldView.isResumePlaying;
    }

    public void drawBackground() {
        for (int row = 0; row < viewport.getNumRows(); row++) {
            for (int col = 0; col < viewport.getNumCols(); col++) {
                Point worldPoint = viewport.viewportToWorld(col, row);
                Optional<PImage> image = world.getBackgroundImage(worldPoint);
                if (image.isPresent()) {
                    PImage img = image.get();
                    if (isGrayscale) {
                        PImage grayImg = img.copy();
                        grayImg.filter(PApplet.GRAY);
                        screen.image(grayImg, col * tileWidth, row * tileHeight);
                    } else {
                        screen.image(img, col * tileWidth, row * tileHeight);
                    }
                }
            }
        }
    }
    
    public void drawEntities() {
        for (Entity entity : world.getEntities()) {
            Point pos = entity.getPosition();
            if (viewport.contains(pos)) {
                Point viewPoint = viewport.worldToViewport(pos.x, pos.y);
                PImage img = entity.getCurrentImage();
    
                // Check if the entity is an instance of the Dio class
                if (isGrayscale && (!(entity instanceof Dio) && !(entity instanceof DioAttack))) {
                    PImage grayImg = img.copy();
                    grayImg.filter(PApplet.GRAY);
                    screen.image(grayImg, viewPoint.x * tileWidth, viewPoint.y * tileHeight);
                } else {
                    screen.image(img, viewPoint.x * tileWidth, viewPoint.y * tileHeight);
                }
            }
        }
    }
    
    

    public void drawViewport() {
        updateGrayscale();
        drawBackground();
        drawEntities();
    }

    public void enableGrayscale() {
        setTimeStopPlaying(true);
        SoundPlayer.playSound("timestop.wav");
        new java.util.Timer().schedule( 
        new java.util.TimerTask() {
            @Override
            public void run() {
                setGrayscale(true);
                grayscaleStartTime = System.currentTimeMillis();
                setTimeStopPlaying(false);
            }
        }, 
        4000
    );
    }

    public void updateGrayscale() {
        if (isGrayscale && !hasPlayedResumeAudio) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - grayscaleStartTime > 10000) { // 10 seconds
                hasPlayedResumeAudio = true;
                setResumePlaying(true);
                SoundPlayer.playSound("resume.wav");
                new java.util.Timer().schedule( 
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            setGrayscale(false);
                        }
                    }, 
                    3000 // Delay for the length of the resume audio
                );
            }
        } else if (!isGrayscale) {
            hasPlayedResumeAudio = false; // Reset the flag when not in grayscale mode
            setResumePlaying(false);
            hasPlayedResumeAudio = false;
        }
    }
    
    public void shiftView(int colDelta, int rowDelta) {
        int newCol = clamp(viewport.getCol() + colDelta, 0, world.getNumCols() - viewport.getNumCols());
        int newRow = clamp(viewport.getRow() + rowDelta, 0, world.getNumRows() - viewport.getNumRows());

        viewport.shift(newCol, newRow);
    }
    private static int clamp(int value, int low, int high) {
        return Math.min(high, Math.max(value, low));
    }

    public Viewport getViewport() {
        return viewport;
    }
}
