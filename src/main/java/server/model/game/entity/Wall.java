package server.model.game.entity;

import org.joml.Vector2f;

import java.util.HashSet;
import java.util.Set;

public class Wall {
    private Set<Vector2f> greatWall = new HashSet<>();

    public void initGreatWall (int gridX, int gridY) {
        greatWall = new HashSet<>();
        for (int x = 0; x < gridX; x++) {
            greatWall.add(new Vector2f(x, 0));
            greatWall.add(new Vector2f(x, gridY-1));
        }

        for (int y = 0; y < gridY; y++) {
            greatWall.add(new Vector2f(0, y));
            greatWall.add(new Vector2f(gridX-1, y));
        }
    }

    public Set<Vector2f> getGreatWall() {
        return greatWall;
    }
}
