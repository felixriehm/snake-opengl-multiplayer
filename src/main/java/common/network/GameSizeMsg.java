package common.network;

import java.util.UUID;

public class GameSizeMsg extends BaseMsg {
    private static final long serialVersionUID = 1L;

    private final int gridSize;

    public GameSizeMsg(int gridSize, UUID sender) {
        super(sender);
        this.gridSize = gridSize;
    }

    public int getGridSize() {
        return gridSize;
    }
}
