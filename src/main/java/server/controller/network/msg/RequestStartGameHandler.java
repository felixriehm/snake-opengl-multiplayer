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

        // ensure that the grid is 2^n + 1 for diamond algorithm
        List<Integer> values = new ArrayList();
        for (int i = 4; i < 7; i++) {
            values.add((int)Math.pow(2,i));
        }

        int scaleHelper = (int) (Math.log(server.getClients().size()) * 10);
        int gridSize = 0;
        for (int i = values.size() - 1; i >= 0; i--) {
            if(scaleHelper > values.get(i)){
                gridSize = values.get(i);
                break;
            }
        }

        if(gridSize < 16) {
            gridSize = 8;
        }

        gridSize = gridSize + 1;

        server.setGame(new Game());
        server.getGame().init(gridSize, gridSize, server.getClients().keySet(), server);
        server.getGame().setGameInitiator(client);

        server.broadcastInitGameMsg(server.getClients().size(), ClientGameState.GAME_ACTIVE);
        initGameLock.unlock();
    }
}
