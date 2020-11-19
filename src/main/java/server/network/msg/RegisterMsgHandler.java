package server.network.msg;

import common.network.MoveMsg;
import common.network.RegisterMsg;
import server.Server;
import server.game.Game;
import server.network.ClientManager;

import java.io.ObjectOutputStream;

public class RegisterMsgHandler implements Runnable {

    private final ObjectOutputStream dos;
    private final RegisterMsg msg;
    private final ClientManager client;

    public RegisterMsgHandler(ClientManager client, RegisterMsg msg, ObjectOutputStream dos){
        this.dos = dos;
        this.msg = msg;
        this.client = client;
    }

    @Override
    public void run() {
        // TODO: Lock
        Server.getInstance().addClient(msg.getSender(), client);
    }
}