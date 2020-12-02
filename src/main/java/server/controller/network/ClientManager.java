package server.controller.network;

import common.network.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;
import server.controller.network.msg.CheatCodeMsgHandler;
import server.controller.network.msg.MoveMsgHandler;
import server.controller.network.msg.RegisterMsgHandler;
import server.controller.network.msg.RequestStartGameHandler;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientManager <T extends BaseMsg> implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientManager.class.getName());

    final ObjectInputStream dis;
    final ObjectOutputStream dos;
    Socket s;
    boolean isloggedin;
    private UUID serverId;
    private UUID clientId;
    private Server server;
    private final Lock initGameLock;
    private final Lock moveLock = new ReentrantLock();

    // constructor
    public ClientManager(Socket s,
                         ObjectInputStream dis, ObjectOutputStream dos, UUID serverId, Server server, Lock initGameLock) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
        this.isloggedin=true;
        this.serverId = serverId;
        this.server = server;
        this.initGameLock = initGameLock;
    }

    @Override
    public void run() {

        Object received;
        while (true)
        {
            try
            {
                // receive the string
                received = dis.readUnshared();

                logger.debug(received);

                if (received instanceof RegisterMsg) {
                    RegisterMsg message = (RegisterMsg) received;
                    clientId = message.getSender();
                    RegisterMsgHandler mtch = new RegisterMsgHandler(this, message, dos, server);
                    Thread t = new Thread(mtch);
                    t.start();
                }

                if (received instanceof RequestStartGameMsg) {
                    RequestStartGameHandler mtch = new RequestStartGameHandler(dos, server, initGameLock, clientId);
                    Thread t = new Thread(mtch);
                    t.start();
                }

                if (received instanceof MoveMsg) {
                    MoveMsgHandler mtch = new MoveMsgHandler((MoveMsg) received, dos, server, moveLock);
                    Thread t = new Thread(mtch);
                    t.start();
                }

                if (received instanceof CheatCodeMsg) {
                    CheatCodeMsgHandler mtch = new CheatCodeMsgHandler((CheatCodeMsg) received, server);
                    Thread t = new Thread(mtch);
                    t.start();
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
            this.dis.close();
            this.dos.close();
            this.server.removeClient(clientId);

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(T msg) {
        logger.debug("Server " + serverId + ": sent message");
        logger.debug(msg);
        if (msg instanceof GameUpdateMsg) {
            logger.debug(((GameUpdateMsg) msg).getGameData());
        }
        try {
            dos.writeUnshared(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
