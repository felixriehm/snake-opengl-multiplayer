package client.network;

import client.game.Game;
import common.network.GameEntitiesMsg;
import common.network.GameSizeMsg;
import common.network.GameStateMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;

public class MsgReader implements Runnable {
    private static final Logger logger = LogManager.getLogger(NetworkManager.class.getName());

    private final ObjectInputStream dis;

    public MsgReader(ObjectInputStream dis) {
        this.dis = dis;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // read the message sent to this client
                Object msg = dis.readUnshared();
                logger.debug(msg);

                if(msg instanceof GameSizeMsg){
                    GameSizeMsg gsm = (GameSizeMsg) msg;
                    Game.getInstance().setGridSize(gsm.getGridSize());
                }

                if(msg instanceof GameStateMsg){
                    GameStateMsg gsm = (GameStateMsg) msg;
                    Game.getInstance().setGameState(gsm.getGameState());
                }

                if(msg instanceof GameEntitiesMsg){
                    GameEntitiesMsg gem = (GameEntitiesMsg) msg;
                    Game.getInstance().setSnakes(gem.getSnakes());
                    Game.getInstance().setFood(gem.getFood());
                }

            } catch (IOException | ClassNotFoundException e) {

                e.printStackTrace();
            }
        }
    }
}
