package common.network;

import common.game.model.ClientGameState;
import common.game.model.Direction;
import common.game.model.PointGameData;
import javafx.util.Pair;
import org.joml.Vector2f;

import java.util.*;

public class InitGameMsg extends BaseMsg {

    private static final long serialVersionUID = 1L;

    private final int playerCount;
    private final int gridX;
    private final int gridY;
    private final ClientGameState gameState;
    private final Set<PointGameData> gameData;
    private final Direction lastDirection;
    private final int worldEventCountdown;

    public InitGameMsg(int playerCount, int gridX, int gridY, ClientGameState gameState, Set<PointGameData> gameData, Direction direction, UUID sender, int worldEventCountdown){
        super(sender);
        this.playerCount = playerCount;
        this.gridX = gridX;
        this.gridY = gridY;
        this.gameState = gameState;
        this.gameData = gameData;
        this.lastDirection = direction;
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

    public Set<PointGameData> getGameData() {
        return gameData;
    }

    public Direction getLastDirection() {
        return lastDirection;
    }
}
