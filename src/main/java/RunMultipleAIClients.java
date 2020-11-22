import client.Client;

public class RunMultipleAIClients {
    public static void main(String[] args) {
        Runnable runnable =
                () -> { Client.main(new String[]{"ai"}); };
        for (int i = 0; i < 7; i++) {
            new Thread(runnable).start();
        }
    }
}
