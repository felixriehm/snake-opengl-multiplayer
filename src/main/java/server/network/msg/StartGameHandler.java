package server.network.msg;

import common.game.GameState;
import common.network.GameSizeMsg;
import common.network.GameStateMsg;
import server.Server;
import server.game.Game;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class StartGameHandler implements Runnable {

    private final ObjectOutputStream dos;

    public StartGameHandler(ObjectOutputStream dos){
        this.dos = dos;
    }

    @Override
    public void run() {
        int gridSize = Server.ar.size() * 10;
        Game snake = new Game(gridSize, gridSize);
        snake.init();

        try {
            dos.writeUnshared(new GameSizeMsg(gridSize, Server.uuid));
            dos.writeUnshared(new GameStateMsg(GameState.GAME_ACTIVE, Server.uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
