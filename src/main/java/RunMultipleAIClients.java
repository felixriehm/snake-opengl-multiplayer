import client.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Server;

import java.util.concurrent.TimeUnit;

public class RunMultipleAIClients {
    private static final Logger logger = LogManager.getLogger(RunMultipleAIClients.class.getName());

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable =
                () -> { Client.main(new String[]{"ai"}); };
        for (int i = 0; i < 99; i++) {
            new Thread(runnable).start();
            TimeUnit.MILLISECONDS.sleep(100);
        }
        logger.debug("All threads started!");
    }
}
