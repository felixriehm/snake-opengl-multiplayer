package common.network;

import common.game.ClientGameState;
import common.game.Direction;
import javafx.util.Pair;
import org.joml.Vector2f;

import java.util.*;

public class InitGameMsg extends BaseMsg {

    private static final long serialVersionUID = 1L;

    private final int playerCount;
    private final int gridX;
    private final int gridY;
    private final ClientGameState gameState;
    private final Set<Vector2f> food;
    private final HashMap<UUID, Pair<List<Vector2f>, Direction>> snakes;
    private final int worldEventCountdown;

    public InitGameMsg(int playerCount, int gridX, int gridY, ClientGameState gameState, Set<Vector2f> food, HashMap<UUID, Pair<List<Vector2f>, Direction>> snakes, UUID sender, int worldEventCountdown){
        super(sender);
        this.playerCount = playerCount;
        this.gridX = gridX;
        this.gridY = gridY;
        this.gameState = gameState;
        this.food = food;
        this.snakes = snakes;
        this.worldEventCountdown = worldEventCountdown;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getGridX() {
        return gridX;
    }

    public int getWorldEventCountdown() {
        return worldEventCountdown;
    }

    public int getGridY() {
        return gridY;
    }

    public ClientGameState getGameState() {
        return gameState;
    }

    public Set<Vector2f> getFood() {
        return food;
    }

    public HashMap<UUID, Pair<List<Vector2f>, Direction>> getSnakes() {
        return snakes;
    }
}
