package common.network;

import common.game.model.Direction;
import common.game.model.PointGameData;

import java.util.Set;
import java.util.UUID;

public class GameUpdateMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    private final Set<PointGameData> gameData;
    private final Direction direction;
    private final int gridX;
    private final int gridY;
    private final int worldEventCountdown;

    public GameUpdateMsg(Set<PointGameData> gameData, Direction direction, UUID sender, int gridX, int gridY, int worldEventCountdown) {
        super(sender);
        this.gameData = gameData;
        this.gridX = gridX;
        this.gridY = gridY;
        this.worldEventCountdown = worldEventCountdown;
        this.direction = direction;
    }

    public int getWorldEventCountdown() {
        return worldEventCountdown;
    }

    public Set<PointGameData> getGameData() {
        return gameData;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }
}
