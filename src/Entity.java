import processing.core.PImage;

public interface Entity {
    String getId();
    Point getPosition();
    void setPosition(Point pos);
    PImage getCurrentImage();
    String log();
    void nextImage();
    double getAnimationPeriod();
    
}
