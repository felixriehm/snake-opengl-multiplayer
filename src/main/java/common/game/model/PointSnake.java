package common.game.model;

import java.util.UUID;

public class PointSnake extends PointGameData{
    private static final long serialVersionUID = 1L;
    private final UUID uuid;
    private final boolean isHead;

    public PointSnake(float x, float y, UUID uuid, boolean isHead) {
        super(x,y);
        this.uuid = uuid;
        this.isHead = isHead;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isHead() {
        return isHead;
    }
}
