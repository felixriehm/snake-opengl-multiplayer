package client;

import client.controller.game.Game;
import client.controller.network.NetworkManager;
import client.view.OpenGLView;
import com.mlomb.freetypejni.Face;
import com.mlomb.freetypejni.FreeType;
import com.mlomb.freetypejni.FreeTypeConstants;
import com.mlomb.freetypejni.Library;
import common.game.ai.AIController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
    public void run(List<String> args) {
        NetworkManager nm = new NetworkManager();
        Game snake;
        if(args.contains("ai")) {
            snake = new Game(nm, new AIController());
        } else {
            snake = new Game(nm, null);
        }
        try {
            nm.run(snake);
        } catch (IOException e) {
            e.printStackTrace();
        }

        snake.init();

        if(args.contains("openGL")) {
            new OpenGLView().run(snake, nm);
        }
    }

    public static void main(String[] args) {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
        if(arguments.size() == 0) {
            arguments.add("openGL");
        }

        new Client().run(arguments);
    }


}
