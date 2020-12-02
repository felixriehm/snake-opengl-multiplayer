package common.network;

import common.game.model.CheatCode;

import java.util.UUID;

public class CheatCodeMsg extends BaseMsg {
    private static final long serialVersionUID = 1L;
    private final CheatCode cheatCode;

    public CheatCodeMsg(UUID sender, CheatCode cheatCode) {
        super(sender);
        this.cheatCode = cheatCode;
    }

    public CheatCode getCheatCode() {
        return cheatCode;
    }
}
