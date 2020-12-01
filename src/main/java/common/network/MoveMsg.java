package common.network;

import common.game.model.Direction;

import java.util.UUID;

public class MoveMsg extends BaseMsg {
    private static final long serialVersionUID = 1L;

    private final Direction direction;

    public MoveMsg(Direction direction, UUID sender) {
        super(sender);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
