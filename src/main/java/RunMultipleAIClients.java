import client.Client;
import server.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RunMultipleAIClients {
    public static void main(String[] args) throws IOException, InterruptedException {
        Runnable runnable =
                () -> { Client.main(new String[]{"ai"}); };
        for (int i = 0; i < 7; i++) {
            new Thread(runnable).start();
        }
    }
}
