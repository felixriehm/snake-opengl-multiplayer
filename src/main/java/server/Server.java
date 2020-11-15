package server;
import java.io.*;
import java.util.*;
import java.net.*;

import common.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.game.Game;
import server.network.ClientManager;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class.getName());

    // Vector to store active clients
    public static Vector<ClientManager> ar = new Vector<>();

    public static final UUID uuid = UUID.randomUUID();

    // counter for clients
    static int i = 0;

    public static void main(String[] args) throws IOException {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(Integer.parseInt(Configuration.getInstance().getProperty("server.port")));
        Socket s;

        // running infinite loop for getting
        // client request
        while (true) {
            // Accept the incoming request
            s = ss.accept();

            logger.debug("New client request received : " + s);

            // obtain input and output streams
            InputStream test = s.getInputStream();
            ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());

            logger.debug("Creating a new handler for this client...");

            // Create a new handler object for handling this request.
            ClientManager mtch = new ClientManager(s,"client " + i, dis, dos);

            // Create a new Thread with this object.
            Thread t = new Thread(mtch);

            logger.debug("Adding this client to active client list");

            // add this client to active clients list
            ar.add(mtch);

            // start the thread.
            t.start();

            // increment i for new client.
            // i is used for naming only, and can be replaced
            // by any naming scheme
            i++;
        }
    }
}
