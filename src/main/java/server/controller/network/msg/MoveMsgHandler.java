package server.controller.network.msg;

import common.network.MoveMsg;
import server.Server;
import server.model.game.entity.Player;

import java.io.ObjectOutputStream;
import java.util.concurrent.locks.Lock;

public class MoveMsgHandler  implements Runnable {

    private final ObjectOutputStream dos;
    private final MoveMsg msg;
    private final Server server;
    private final Lock moveLock;

    public MoveMsgHandler(MoveMsg msg, ObjectOutputStream dos, Server server, Lock moveLock){
        this.dos = dos;
        this.msg = msg;
        this.server = server;
        this.moveLock = moveLock;
    }

    @Override
    public void run() {
        moveLock.lock();
        Player player = server.getGame().getPlayers().get(msg.getSender());

        // if player has not lost and is not removed
        if(player != null) {
            player.setNextDirection(msg.getDirection());
        }
        moveLock.unlock();
    }
}
