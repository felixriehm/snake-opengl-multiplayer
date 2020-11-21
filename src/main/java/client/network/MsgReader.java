package client.network;

import client.game.Game;
import common.network.GameUpdateMsg;
import common.network.GameStateMsg;
import common.network.InitGameMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.UUID;

public class MsgReader implements Runnable {
    private static final Logger logger = LogManager.getLogger(MsgReader.class.getName());

    private final ObjectInputStream dis;
    private final ObjectOutputStream dos;
    private final Socket s;
    private final UUID networkId;
    private final Game game;

    public MsgReader(Socket s, ObjectInputStream dis, ObjectOutputStream dos, UUID networkId, Game game) {
        this.dis = dis;
        this.networkId = networkId;
        this.s = s;
        this.dos = dos;
        this.game = game;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // read the message sent to this client
                //TODO LOCK
                Object msg = dis.readUnshared();

                logger.debug("Client " + networkId + ": received message");
                logger.debug(msg);

                if(msg instanceof InitGameMsg){
                    InitGameMsg ism = (InitGameMsg) msg;
                    game.start(ism.getPlayerCount(), ism.getGridX(), ism.getGridX(), ism.getGameState(), ism.getFood(), ism.getSnakes());
                }

                if(msg instanceof GameStateMsg){
                    GameStateMsg gsm = (GameStateMsg) msg;
                    logger.debug(gsm.getClientGameState());
                    game.setGameState(gsm.getClientGameState());
                }

                if(msg instanceof GameUpdateMsg){
                    GameUpdateMsg gem = (GameUpdateMsg) msg;
                    logger.debug(gem.getSnakes());
                    logger.debug(gem.getFood());
                    game.update(gem.getFood(), gem.getSnakes(), gem.getGridX(), gem.getGridY());
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
