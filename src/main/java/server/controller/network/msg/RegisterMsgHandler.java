package server.controller.network.msg;

import common.network.RegisterMsg;
import server.Server;
import server.controller.network.ClientManager;

import java.io.ObjectOutputStream;

public class RegisterMsgHandler implements Runnable {

    private final ObjectOutputStream dos;
    private final RegisterMsg msg;
    private final ClientManager client;
    private final Server server;

    public RegisterMsgHandler(ClientManager client, RegisterMsg msg, ObjectOutputStream dos, Server server){
        this.dos = dos;
        this.msg = msg;
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        server.addClient(msg.getSender(), client);
    }
}