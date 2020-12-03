package server.controller.network.msg;

import common.game.model.CheatCode;
import common.game.model.ServerGameState;
import common.network.CheatCodeMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;
import server.controller.game.Game;
import server.model.game.entity.Player;

public class CheatCodeMsgHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(CheatCodeMsgHandler.class.getName());

    private final CheatCodeMsg msg;
    private final Server server;

    public CheatCodeMsgHandler(CheatCodeMsg msg, Server server){
        this.msg = msg;
        this.server = server;
    }

    @Override
    public void run() {
        Game game = server.getGame();

        if(game != null && game.getState() == ServerGameState.GAME_STARTED) {
            Player player = game.getPlayers().get(game.getGameInitiator());
            CheatCode cheatCode = msg.getCheatCode();

            switch (cheatCode) {
                case PLAYER_GROW:
                    // if player has not lost and is not removed
                    if(player != null) {
                        logger.debug("CHEAT: grow");
                        player.grow();
                    }
                    break;
                case TOGGLE_UPDATE:
                case TOGGLE_WORLD_EVENT:
                case TOGGLE_MAX_VIEW:
                case TOGGLE_DISCOVERED_VIEW:
                case PLAYER_IMMORTAL:
                    game.toogleCheatCode(cheatCode);
                    break;
                case PLAYER_KILL:
                    game.killPlayer(game.getGameInitiator());
                    break;
                case PLAYER_RESPAWN:
                    game.respawnPlayer(game.getGameInitiator());
                    break;
                case GREAT_WALL_INCREASE:
                    game.increaseGreatWall();
                    break;
                case GREAT_WALL_DECREASE:
                    game.decreaseGreatWall();
                    break;
                case FOOD_SPAWN:
                    game.spawnFood();
                    break;
                case FOOD_SHUFFLE:
                    game.shuffleFood();
                    break;
                default:
                    break;
            }
            server.broadcastGameUpdateMsg();
        }
    }
}
