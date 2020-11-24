package client.controller.network;

import client.controller.game.Game;
import common.Configuration;
import common.network.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.UUID;

public class NetworkManager <T extends BaseMsg>
{
    private static final Logger logger = LogManager.getLogger(NetworkManager.class.getName());
    private final UUID uuid = UUID.randomUUID();

    final static int ServerPort = Integer.parseInt(Configuration.getInstance().getProperty("server.port"));
    private ObjectOutputStream dos;
    private ObjectInputStream dis;
    private Socket s = null;
    private Game game = null;
    private MsgFactory msgFactory;

    public NetworkManager () {}

    public void run(Game game) throws UnknownHostException, IOException
    {
        this.game = game;
        msgFactory = new MsgFactory();
        msgFactory.init(uuid);

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        try
        {
            s = new Socket(ip, ServerPort);

            // obtaining input and out streams
            dos = new ObjectOutputStream(s.getOutputStream());

            dis = new ObjectInputStream(s.getInputStream());

            // closing resources
        } catch(IOException e){
            e.printStackTrace();
            if(s == null) {
                return;
            }
            s.close();
            dos.close();
            dis.close();
        }

        this.sendMessage((T) msgFactory.getRegisterMsg());

        new Thread(new MsgReader(s, dis, dos,  uuid, game)).start();
    }

    public synchronized void sendMessage(T msg) {
        logger.debug("Client: sent message: " + msg);
        try {
            dos.writeUnshared(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MsgFactory getMsgFactory() {
        return this.msgFactory;
    }

    public UUID getId(){
        return uuid;
    }
}