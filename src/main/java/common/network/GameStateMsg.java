package common.network;

import common.game.ClientGameState;

import java.util.UUID;

public class GameStateMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    private final ClientGameState clientGameState;

    public GameStateMsg(ClientGameState clientGameState, UUID sender) {
        super(sender);
        this.clientGameState = clientGameState;
    }

    public ClientGameState getClientGameState() {
        return clientGameState;
    }
}
