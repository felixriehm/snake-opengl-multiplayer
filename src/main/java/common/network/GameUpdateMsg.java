package common.network;

import org.joml.Vector2f;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameUpdateMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    private final Set<Vector2f> food;
    private final List<List<Vector2f>> snakes;
    private final int gridX;
    private final int gridY;

    public GameUpdateMsg(Set<Vector2f> food, List<List<Vector2f>> snakes, UUID sender, int gridX, int gridY) {
        super(sender);
        this.food = food;
        this.snakes = snakes;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public Set<Vector2f> getFood() {
        return food;
    }

    public List<List<Vector2f>> getSnakes() {
        return snakes;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }
}
