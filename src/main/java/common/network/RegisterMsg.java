package common.network;

import common.game.Direction;

import java.util.UUID;

public class RegisterMsg extends BaseMsg {
    private static final long serialVersionUID = 1L;

    public RegisterMsg(UUID sender) {
        super(sender);
    }
}
