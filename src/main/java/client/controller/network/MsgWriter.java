package client.controller.network;

import common.network.BaseMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MsgWriter <T extends BaseMsg> implements Runnable {
    private static final Logger logger = LogManager.getLogger(MsgWriter.class.getName());

    private final ObjectOutputStream dos;
    private final T msg;

    public MsgWriter(T msg, ObjectOutputStream dos) {
        this.dos = dos;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            //TODO LOCK
            dos.writeUnshared(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
