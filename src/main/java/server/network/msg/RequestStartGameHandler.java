package server.network.msg;

import common.game.GameState;
import common.network.MsgFactory;
import org.joml.Vector2f;
import server.Server;
import server.game.Game;

import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Set;

public class RequestStartGameHandler implements Runnable {

    private final ObjectOutputStream dos;

    public RequestStartGameHandler(ObjectOutputStream dos){
        this.dos = dos;
    }

    @Override
    public void run() {
        int gridSize = Server.getInstance().getClients().size() * 10;
        // TODO: LOCK
        Game.getInstance().init(gridSize, gridSize, Server.getInstance().getClients().keySet());

        Set<Vector2f> food = Game.getInstance().getFood().getFood();
        LinkedList<LinkedList<Vector2f>> players = new LinkedList<LinkedList<Vector2f>>();
        Game.getInstance().getPlayers().values().forEach(player -> players.add((LinkedList) player.getSnakeBody()));

        Server.getInstance().broadcastMsg(MsgFactory.getInstance().getInitGameMsg(Server.getInstance().getClients().size(),gridSize, GameState.GAME_ACTIVE, food, players));
    }
}
