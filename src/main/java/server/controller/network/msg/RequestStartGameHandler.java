package server.controller.network.msg;

import common.game.model.ClientGameState;
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
    private final UUID client;

    public RequestStartGameHandler(ObjectOutputStream dos, Server server, Lock initGameLock, UUID client){
        this.dos = dos;
        this.server = server;
        this.initGameLock = initGameLock;
        this.client = client;
    }

    @Override
    public void run() {
        initGameLock.lock();
        if(server.getGame() != null) {
            server.getGame().stopUpdate();
        }

        int gridSize = (int) (Math.log(server.getClients().size()) * 10);
        if(gridSize < 10) {
            gridSize = 11;
        }
        // ensure that the grid is uneven for diamond algorithm
        if(gridSize % 2 == 0) {
            gridSize = gridSize + 1;
        }
        server.setGame(new Game());
        server.getGame().init(gridSize, gridSize, server.getClients().keySet(), server);
        server.getGame().setGameInitiator(client);

        server.broadcastInitGameMsg(server.getClients().size(), ClientGameState.GAME_ACTIVE);
        initGameLock.unlock();
    }
}
