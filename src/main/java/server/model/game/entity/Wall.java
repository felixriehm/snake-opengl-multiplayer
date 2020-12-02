package server.model.game.entity;

import org.joml.Vector2f;

import java.util.HashSet;
import java.util.Set;

public class Wall {
    private Set<Vector2f> greatWall = new HashSet<>();
    private Vector2f greatWallStartPoint;
    private float greatWallWidth;
    private float greatWallHeight;


    public void initGreatWall (float startX, float startY, float width, float height) {
        greatWall = new HashSet<>();
        for (float x = startX; x < width; x++) {
            greatWall.add(new Vector2f(x, startY));
            greatWall.add(new Vector2f(x, height-1));
        }

        for (float y = startY; y < height; y++) {
            greatWall.add(new Vector2f(startX, y));
            greatWall.add(new Vector2f(width-1, y));
        }

        greatWallStartPoint = new Vector2f(startX, startY);
        greatWallWidth = width;
        greatWallHeight = height;
    }

    public Set<Vector2f> getGreatWall() {
        return greatWall;
    }

    public void setGreatWall(Set<Vector2f> greatWall) {
        this.greatWall = greatWall;
    }

    public Vector2f getGreatWallStartPoint() {
        return greatWallStartPoint;
    }

    public float getGreatWallWidth() {
        return greatWallWidth;
    }

    public float getGreatWallHeight() {
        return greatWallHeight;
    }
}
