package common.network;

import common.game.GameState;
import org.joml.Vector2f;

import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class InitGameMsg extends BaseMsg {

    private static final long serialVersionUID = 1L;

    private final int playerCount;
    private final int gridSize;
    private final GameState gameState;
    private final Set<Vector2f> food;
    private final LinkedList<LinkedList<Vector2f>> snakes;

    public InitGameMsg(int playerCount, int gridSize, GameState gameState, Set<Vector2f> food, LinkedList<LinkedList<Vector2f>> snakes, UUID sender){
        super(sender);
        this.playerCount = playerCount;
        this.gridSize = gridSize;
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

    public int getGridSize() {
        return gridSize;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Set<Vector2f> getFood() {
        return food;
    }

    public LinkedList<LinkedList<Vector2f>> getSnakes() {
        return snakes;
    }
}
