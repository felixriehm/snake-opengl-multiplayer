package common.network;

import common.game.model.Direction;

import java.util.UUID;

public class CheatGrowMsg extends BaseMsg {
    private static final long serialVersionUID = 1L;

    public CheatGrowMsg(UUID sender) {
        super(sender);
    }
}
