package server.game;

import client.network.NetworkManager;
import common.Configuration;
import common.game.Direction;
import common.game.GameState;
import common.network.GameEntitiesMsg;
import common.network.MsgFactory;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import server.Server;
import server.game.entity.Food;
import server.game.entity.Player;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class Game {

    public static final Vector2f PLAYER_SIZE = new Vector2f(512.0f, 100.0f);
    public static final Vector2f PLAYER_VELOCITY = new Vector2f(500.0f, 500.0f);
    public static final int GAME_UPDATE_RATE = Integer.parseInt(Configuration.getInstance().getProperty("game.update-rate"));

    private GameState state;
    private int GridX, GridY;
    // will be modified inside Player and Food Object
    private Set availableGridCells = new HashSet();
    private HashMap<UUID, Player> players = new HashMap<UUID, Player>();
    private Food food;
    private boolean[] keys = new boolean[1024];
    private boolean[] keysProcessed = new boolean[1024];
    private Game gameReference;
    private Timer updateTimer;
    private boolean gameInitiated = false;
    private int playerCount = 0;

    private static Game instance;

    private Game () {}

    public static Game getInstance () {
        if (Game.instance == null) {
            Game.instance = new Game ();
        }
        return Game.instance;
    }

    public void init(int gridX, int gridY, Set<UUID> clients) {
        this.gameInitiated = true;
        this.state = GameState.GAME_ACTIVE;
        this.GridX = gridX;
        this.GridY = gridY;
        this.playerCount = clients.size();

        for (int x = 0; x < this.GridX; x++)
        {
            for (int y = 0; y < this.GridY; y++)
            {
                availableGridCells.add(new Vector2f(x,y));
            }
        }

        gameReference = this;

        clients.forEach(clientId -> this.players.put(clientId, new Player(this.availableGridCells, GridX, GridY)));

        // spawn food, needs to happen after Player init
        food = new Food(this.availableGridCells);
        food.spawnFood(this.players.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet()));

        this.updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameReference.update();
            }
        }, 0, GAME_UPDATE_RATE);
    }

    public Food getFood(){
        return this.food;
    }

    public HashMap<UUID, Player> getPlayers(){
        return this.players;
    }

    public boolean isInitiated(){
        return gameInitiated;
    }

    public void update()
    {
        if (this.state == GameState.GAME_ACTIVE)
        {
            // update objects
            players.values().forEach(player -> player.moveSnake());


            // check for collisions
            //this.doCollisions();

            // check loss condition
            //this.checkLossCondition();

            // check win condition
            //this.checkWinCondition();
        }

        Server.getInstance().broadcastMsg(MsgFactory.getInstance().getGameEntitiesMsg(food.getFood(), players.entrySet().stream().map(player -> player.getValue().getSnakeBody()).collect(Collectors.toList())));
    }

    public void setKeys(int index, boolean value){
        this.keys[index] = value;
    }

    public void setKeysProcessed(int index, boolean value){
        this.keysProcessed[index] = value;
    }

    /*public void checkWinCondition() {
        if (player.getSnakeBody().size() == this.GridX * this.GridY) {
            this.stopUpdate();
            this.state = GameState.GAME_WIN;
        }
    }

    public void checkLossCondition()
    {
        Vector2f head = player.getSnakeHead();
        // client.game.snake is outside of grid
        if (head.x < 0 || head.y < 0 || head.x >= this.GridX || head.y >= this.GridY)
        {
            this.stopUpdate();
            this.state = GameState.GAME_LOSS;
            return;
        }

        // client.game.snake hits its own body
        List<Vector2f> snakeBody = player.getSnakeBody();
        for (int i = 1; i < snakeBody.size(); i++) {
            Vector2f currentSegment = snakeBody.get(i);
            if(currentSegment.x == head.x && currentSegment.y == head.y) {
                this.stopUpdate();
                this.state = GameState.GAME_LOSS;
                return;
            }
        }
    }

    public void doCollisions()
    {
        Set<Vector2f> intersectedCells = new HashSet<>(player.getSnakeBody());
        intersectedCells.retainAll(food.getFood());

        if (!intersectedCells.isEmpty()) {
            player.grow();
            food.removeFood((Vector2f)intersectedCells.toArray()[0]);
            food.spawnFood(player.getSnakeBody());
        }
    }*/

    public void stopUpdate(){
        updateTimer.cancel();
    }
}
