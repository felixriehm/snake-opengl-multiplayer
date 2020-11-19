package server.network.msg;

import common.game.GameState;
import common.network.MoveMsg;
import common.network.MsgFactory;
import org.joml.Vector2f;
import server.Server;
import server.game.Game;

import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Set;

public class MoveMsgHandler  implements Runnable {

    private final ObjectOutputStream dos;
    private final MoveMsg msg;

    public MoveMsgHandler(MoveMsg msg, ObjectOutputStream dos){
        this.dos = dos;
        this.msg = msg;
    }

    @Override
    public void run() {
        // TODO: Lock
        Game.getInstance().getPlayers().get(msg.getSender()).setNextDirection(msg.getDirection());
    }
}
