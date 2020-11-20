package server.network.msg;

import common.game.ClientGameState;
import org.joml.Vector2f;
import server.Server;
import server.game.Game;

import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Set;

public class RequestStartGameHandler implements Runnable {

    private final ObjectOutputStream dos;
    private final Server server;

    public RequestStartGameHandler(ObjectOutputStream dos, Server server){
        this.dos = dos;
        this.server = server;
    }

    @Override
    public void run() {
        int gridSize = server.getClients().size() * 10;
        // TODO: LOCK
        server.setGame(new Game());
        server.getGame().init(gridSize, gridSize, server.getClients().keySet(), server);

        Set<Vector2f> food = server.getGame().getFood().getFood();
        LinkedList<LinkedList<Vector2f>> players = new LinkedList<LinkedList<Vector2f>>();
        server.getGame().getPlayers().values().forEach(player -> players.add((LinkedList) player.getSnakeBody()));

        server.broadcastMsg(server.getMsgFactory().getInitGameMsg(server.getClients().size(), gridSize, gridSize, ClientGameState.GAME_ACTIVE, food, players));
    }
}
