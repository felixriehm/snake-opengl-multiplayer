package common.network;

import common.game.Direction;
import javafx.util.Pair;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameUpdateMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    private final Set<Vector2f> food;
    private final HashMap<UUID, Pair<List<Vector2f>, Direction>> snakes;
    private final int gridX;
    private final int gridY;
    private final int worldEventCountdown;

    public GameUpdateMsg(Set<Vector2f> food, HashMap<UUID, Pair<List<Vector2f>, Direction>> snakes, UUID sender, int gridX, int gridY, int worldEventCountdown) {
        super(sender);
        this.food = food;
        this.snakes = snakes;
        this.gridX = gridX;
        this.gridY = gridY;
        this.worldEventCountdown = worldEventCountdown;
    }

    public Set<Vector2f> getFood() {
        return food;
    }

    public int getWorldEventCountdown() {
        return worldEventCountdown;
    }

    public HashMap<UUID, Pair<List<Vector2f>, Direction>> getSnakes() {
        return snakes;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }
}
