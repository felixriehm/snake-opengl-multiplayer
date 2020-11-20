import client.Client;
import server.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RunAIClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client.main(new String[]{"ai"});
    }
}
