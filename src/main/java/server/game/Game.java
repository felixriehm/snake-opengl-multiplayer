package server.game;

import common.Configuration;
import common.game.Direction;
import common.game.GameState;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import server.game.entity.Food;
import server.game.entity.Player;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Game {

    public static final Vector2f PLAYER_SIZE = new Vector2f(512.0f, 100.0f);
    public static final Vector2f PLAYER_VELOCITY = new Vector2f(500.0f, 500.0f);
    public static final int GAME_UPDATE_RATE = Integer.parseInt(Configuration.getInstance().getProperty("game.update-rate"));

    private GameState state;
    private int GridX, GridY;
    private Set availableGridCells = new HashSet();
    private Player player;
    private Food food;
    private boolean[] keys = new boolean[1024];
    private boolean[] keysProcessed = new boolean[1024];
    private Game gameReference;
    private Timer updateTimer;

    public Game(int gridX, int gridY) {
        this.state = GameState.GAME_ACTIVE;
        this.GridX = gridX;
        this.GridY = gridY;

        for (int x = 0; x < this.GridX; x++)
        {
            for (int y = 0; y < this.GridY; y++)
            {
                availableGridCells.add(new Vector2f(x,y));
            }
        }
    }

    public void init() {
        gameReference = this;

        this.resetGame();
    }

    public void processInput(){
        if (this.state == GameState.GAME_ACTIVE)
        {
            if (this.keys[GLFW_KEY_A] && (player.getSnakeBody().size() == 1 || (player.getSnakeBody().size() > 1 && player.getLastDirection() != Direction.RIGHT)))
            {
                player.setNextDirection(Direction.LEFT);
            }
            if (this.keys[GLFW_KEY_D] && (player.getSnakeBody().size() == 1 || (player.getSnakeBody().size() > 1 && player.getLastDirection() != Direction.LEFT)))
            {
                player.setNextDirection(Direction.RIGHT);
            }
            if (this.keys[GLFW_KEY_W] && (player.getSnakeBody().size() == 1 || (player.getSnakeBody().size() > 1 && player.getLastDirection() != Direction.DOWN)))
            {
                player.setNextDirection(Direction.UP);
            }
            if (this.keys[GLFW_KEY_S] && (player.getSnakeBody().size() == 1 || (player.getSnakeBody().size() > 1 && player.getLastDirection() != Direction.UP)))
            {
                player.setNextDirection(Direction.DOWN);
            }
        }
    }

    public void update()
    {
        if (this.state == GameState.GAME_ACTIVE)
        {
            // update objects
            player.moveSnake();

            // check for collisions
            this.doCollisions();

            // check loss condition
            this.checkLossCondition();

            // check win condition
            this.checkWinCondition();
        }
    }

    public void resetGame(){
        Vector2f playerPos = new Vector2f(2, 2);
        player = new Player(playerPos, Direction.RIGHT);
        // spawn food, needs to happen after Player init
        food = new Food(this.availableGridCells);
        food.spawnFood(player.getSnakeBody());

        this.updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameReference.update();
            }
        }, 0, GAME_UPDATE_RATE);
    }

    public void setKeys(int index, boolean value){
        this.keys[index] = value;
    }

    public void setKeysProcessed(int index, boolean value){
        this.keysProcessed[index] = value;
    }

    public void checkWinCondition() {
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
    }

    public void stopUpdate(){
        updateTimer.cancel();
    }
}
