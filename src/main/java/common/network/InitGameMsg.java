package common.network;

import common.game.ClientGameState;
import org.joml.Vector2f;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InitGameMsg extends BaseMsg {

    private static final long serialVersionUID = 1L;

    private final int playerCount;
    private final int gridX;
    private final int gridY;
    private final ClientGameState gameState;
    private final Set<Vector2f> food;
    private final List<List<Vector2f>> snakes;

    public InitGameMsg(int playerCount, int gridX, int gridY, ClientGameState gameState, Set<Vector2f> food, List<List<Vector2f>> snakes, UUID sender){
        super(sender);
        this.playerCount = playerCount;
        this.gridX = gridX;
        this.gridY = gridY;
        this.gameState = gameState;
        this.food = food;
        this.snakes = snakes;
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

    public int getGridY() {
        return gridY;
    }

    public ClientGameState getGameState() {
        return gameState;
    }

    public Set<Vector2f> getFood() {
        return food;
    }

    public List<List<Vector2f>> getSnakes() {
        return snakes;
    }
}
