package server.controller.network.msg;

import common.game.ClientGameState;
import org.joml.Vector2f;
import server.Server;
import server.controller.game.Game;

import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;

public class RequestStartGameHandler implements Runnable {

    private final ObjectOutputStream dos;
    private final Server server;
    private final Lock initGameLock;

    public RequestStartGameHandler(ObjectOutputStream dos, Server server, Lock initGameLock){
        this.dos = dos;
        this.server = server;
        this.initGameLock = initGameLock;
    }

    @Override
    public void run() {
        initGameLock.lock();
        if(server.getGame() != null) {
            server.getGame().stopUpdate();
        }

        int gridSize = (int) (Math.log(server.getClients().size()) * 10);
        if(gridSize < 10) {
            gridSize = 10;
        }
        server.setGame(new Game());
        server.getGame().init(gridSize, gridSize, server.getClients().keySet(), server);

        Set<Vector2f> food = server.getGame().getFood().getFood();

        server.broadcastMsg(server.getMsgFactory().getInitGameMsg(server.getClients().size(),
                gridSize, gridSize, ClientGameState.GAME_ACTIVE, food, server.getGame().getPlayersInfo(),
                server.getGame().getWorldEventCountdown()));
        initGameLock.unlock();
    }
}
