package common.network;

import common.game.GameState;

import java.util.UUID;

public class GameStateMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    public GameStateMsg(GameState gameState, UUID sender) {
        super(sender);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
