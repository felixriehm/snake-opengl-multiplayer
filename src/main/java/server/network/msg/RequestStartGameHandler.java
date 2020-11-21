package server.network.msg;

import common.game.ClientGameState;
import common.game.Direction;
import javafx.util.Pair;
import org.joml.Vector2f;
import server.Server;
import server.game.Game;
import server.game.entity.Player;

import java.io.ObjectOutputStream;
import java.util.*;

public class RequestStartGameHandler implements Runnable {

    private final ObjectOutputStream dos;
    private final Server server;

    public RequestStartGameHandler(ObjectOutputStream dos, Server server){
        this.dos = dos;
        this.server = server;
    }

    @Override
    public void run() {
        if(server.getGame() != null) {
            server.getGame().stopUpdate();
        }

        int gridSize = (server.getClients().size() * 10) / 2;
        // TODO: LOCK
        server.setGame(new Game());
        server.getGame().init(gridSize, gridSize, server.getClients().keySet(), server);

        Set<Vector2f> food = server.getGame().getFood().getFood();

        server.broadcastMsg(server.getMsgFactory().getInitGameMsg(server.getClients().size(),
                gridSize, gridSize, ClientGameState.GAME_ACTIVE, food, server.getGame().getPlayersInfo(),
                server.getGame().getWorldEventCountdown()));
    }
}
