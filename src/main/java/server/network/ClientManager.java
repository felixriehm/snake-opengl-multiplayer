package server.network;

import common.network.GameSizeMsg;
import common.network.MoveMsg;
import common.network.StartGameMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;
import server.network.msg.StartGameHandler;

import java.io.*;
import java.util.*;
import java.net.*;

public class ClientManager implements Runnable {
    private static final Logger logger = LogManager.getLogger(Server.class.getName());

    Scanner scn = new Scanner(System.in);
    private String name;
    final ObjectInputStream dis;
    final ObjectOutputStream dos;
    Socket s;
    boolean isloggedin;

    // constructor
    public ClientManager(Socket s, String name,
                         ObjectInputStream dis, ObjectOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin=true;
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

                if (received instanceof StartGameMsg) {
                    StartGameHandler mtch = new StartGameHandler(dos);
                    Thread t = new Thread(mtch);
                    t.start();
                }


                if(received.equals("logout")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
