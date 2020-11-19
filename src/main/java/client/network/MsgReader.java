package client.network;

import client.game.Game;
import common.network.GameEntitiesMsg;
import common.network.GameSizeMsg;
import common.network.GameStateMsg;
import common.network.InitGameMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.UUID;

public class MsgReader implements Runnable {
    private static final Logger logger = LogManager.getLogger(NetworkManager.class.getName());

    private final ObjectInputStream dis;
    private final ObjectOutputStream dos;
    private final Socket s;
    private final UUID networkId;

    public MsgReader(Socket s, ObjectInputStream dis, ObjectOutputStream dos, UUID networkId) {
        this.dis = dis;
        this.networkId = networkId;
        this.s = s;
        this.dos = dos;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // read the message sent to this client
                Object msg = dis.readUnshared();
                logger.debug("Client " + networkId + ": received message");
                logger.debug(msg);

                if(msg instanceof InitGameMsg){
                    InitGameMsg ism = (InitGameMsg) msg;
                    Game.getInstance().start(ism.getPlayerCount(), ism.getGridSize(), ism.getGameState(), ism.getFood(), ism.getSnakes());
                }

                if(msg instanceof GameSizeMsg){
                    GameSizeMsg gsm = (GameSizeMsg) msg;
                    logger.debug(gsm.getGridSize());
                    Game.getInstance().setGridSize(gsm.getGridSize());
                }

                if(msg instanceof GameStateMsg){
                    GameStateMsg gsm = (GameStateMsg) msg;
                    logger.debug(gsm.getGameState());
                    Game.getInstance().setGameState(gsm.getGameState());
                }

                if(msg instanceof GameEntitiesMsg){
                    GameEntitiesMsg gem = (GameEntitiesMsg) msg;
                    logger.debug(gem.getSnakes());
                    logger.debug(gem.getFood());
                    Game.getInstance().setSnakes((LinkedList) gem.getSnakes());
                    Game.getInstance().setFood(gem.getFood());
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try
        {
            // closing resources
            this.s.close();
            this.dos.close();
            this.dis.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
