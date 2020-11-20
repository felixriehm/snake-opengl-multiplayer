package server;
import java.io.*;
import java.util.*;
import java.net.*;

import client.network.NetworkManager;
import common.Configuration;
import common.network.BaseMsg;
import common.network.MsgFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.game.Game;
import server.network.ClientManager;

public class Server <T extends BaseMsg>  {
    private static final Logger logger = LogManager.getLogger(Server.class.getName());

    // Vector to store active clients
    private HashMap<UUID, ClientManager> clients = new HashMap<>();
    private MsgFactory msgFactory;
    private Game game;

    private final UUID uuid = UUID.randomUUID();

    public Server () {}

    public void run() throws IOException {
        msgFactory = new MsgFactory();
        msgFactory.init(uuid);

        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(Integer.parseInt(Configuration.getInstance().getProperty("server.port")));
        Socket s = null;
        ObjectOutputStream dos = null;
        ObjectInputStream dis = null;

        // running infinite loop for getting
        // client request
        while (true) {
            try {
                // Accept the incoming request
                s = ss.accept();

                logger.debug("New client request received : " + s);

                // obtain input and output streams
                dos = new ObjectOutputStream(s.getOutputStream());
                dis = new ObjectInputStream(s.getInputStream());

                logger.debug("Creating a new handler for this client...");

                // Create a new handler object for handling this request.
                ClientManager mtch = new ClientManager(s, dis, dos, uuid, this);

                // Create a new Thread with this object.
                Thread t = new Thread(mtch);

                // start the thread.
                t.start();
            } catch (Exception e){
                s.close();
                dis.close();
                dos.close();
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    public void broadcastMsg(T msg) {
        this.clients.entrySet().forEach(client -> client.getValue().sendMessage(msg));
    }

    public void sendMsgToClient() {

    }

    public MsgFactory getMsgFactory(){
        return this.msgFactory;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public HashMap<UUID, ClientManager> getClients() {
        return this.clients;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void addClient(UUID clientId, ClientManager client){
        this.clients.put(clientId, client);
    }

    public void removeClient(UUID clientId) {
        logger.debug("Removing Client " + clientId);
        this.clients.remove(clientId);
    }
}
