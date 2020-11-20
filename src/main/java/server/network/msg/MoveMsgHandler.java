package server.network.msg;

import common.network.MoveMsg;
import server.Server;
import server.game.Game;
import server.game.entity.Player;

import java.io.ObjectOutputStream;

public class MoveMsgHandler  implements Runnable {

    private final ObjectOutputStream dos;
    private final MoveMsg msg;
    private final Server server;

    public MoveMsgHandler(MoveMsg msg, ObjectOutputStream dos, Server server){
        this.dos = dos;
        this.msg = msg;
        this.server = server;
    }

    @Override
    public void run() {
        // TODO: Lock
        Player player = server.getGame().getPlayers().get(msg.getSender());

        // if player has not lost and is not removed
        if(player != null) {
            player.setNextDirection(msg.getDirection());
        }
    }
}
