package client.network;

import client.game.Game;
import client.game.manager.ResourceManager;
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

    private static NetworkManager instance;

    private NetworkManager () {}

    public static NetworkManager getInstance () {
        if (NetworkManager.instance == null) {
            NetworkManager.instance = new NetworkManager ();
        }
        return NetworkManager.instance;
    }

    public void run() throws UnknownHostException, IOException
    {
        MsgFactory.getInstance().init(uuid);

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

        this.sendMessage((T) MsgFactory.getInstance().getRegisterMsg());

        new Thread(new MsgReader(s, dis, dos,  uuid)).start();
    }

    public void sendMessage(T msg) {
        logger.debug("Client: sent message: " + msg);
        new Thread(new MsgWriter<T>(msg, dos)).start();
    }

    public UUID getId(){
        return uuid;
    }
}