package common.network;

import common.game.Direction;

import java.util.UUID;

public class StartGameMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    public StartGameMsg(UUID sender) {
        super(sender);
    }
}
