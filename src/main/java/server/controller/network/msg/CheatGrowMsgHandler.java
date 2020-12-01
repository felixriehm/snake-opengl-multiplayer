package server.controller.network.msg;

import common.network.CheatGrowMsg;
import common.network.MoveMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;
import server.controller.game.Game;
import server.model.game.entity.Player;

import java.io.ObjectOutputStream;

public class CheatGrowMsgHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(CheatGrowMsgHandler.class.getName());

    private final CheatGrowMsg msg;
    private final Server server;

    public CheatGrowMsgHandler(CheatGrowMsg msg, Server server){
        this.msg = msg;
        this.server = server;
    }

    @Override
    public void run() {
        Player player = server.getGame().getPlayers().get(msg.getSender());

        // if player has not lost and is not removed
        if(player != null) {
            logger.debug("start: grow cheat");
            player.grow();
            server.broadcastGameUpdateMsg();
            logger.debug("end: grow cheat");
        }
    }
}
