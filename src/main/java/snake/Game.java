package snake;

import entity.Food;
import entity.Player;
import manager.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.CircleRenderer;
import renderer.SquareRenderer;
import renderer.TriangleRenderer;
import shader.Shader;

import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static snake.Game.Direction.*;
import static snake.Game.GameState.*;

public class Game {
    public enum GameState{
        GAME_ACTIVE,
        GAME_MENU,
        GAME_WIN,
        GAME_LOSS
    }

    public enum Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    };

    public static final Vector2f PLAYER_SIZE = new Vector2f(512.0f, 100.0f);
    public static final Vector2f PLAYER_VELOCITY = new Vector2f(500.0f, 500.0f);
    public static final Vector3f[] PLAYER_COLORS = { new Vector3f(0.96f, 0.63f, 0.f), new Vector3f(0.26f, 0.52f, 0.96f), new Vector3f(0.86f, 0.27f, 0.22f), new Vector3f(0.06f, 0.62f, 0.35f) };
    public static final int GAME_UPDATE_RATE = 500;
    public static final Vector3f FOOD_COLOR = new Vector3f(0.737f, 0.075f, 0.996f);
    public static final int CELL_SIZE = 64;
    public static final int GRID_LINE_WIDTH = 2;

    private GameState state;
    private int Width, Height, GridX, GridY;
    private Set availableGridCells = new HashSet();
    private SquareRenderer squareRenderer;
    private TriangleRenderer triangleRenderer;
    private CircleRenderer circleRenderer;
    private Player player;
    private Food food;
    private boolean[] keys = new boolean[1024];
    private boolean[] keysProcessed = new boolean[1024];
    private Game gameReference;
    private Timer updateTimer;

    public Game (int width, int height) {
        this.state = GAME_ACTIVE;
        this.Width = width;
        this.Height = height;
        this.GridX = width / CELL_SIZE;
        this.GridY = height / CELL_SIZE;

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

        try{
            ResourceManager.getInstance().loadShader("src/main/java/shader/primitive-vs.glsl",
                    "src/main/java/shader/primitive-fs.glsl",
                    null,"primitive"
            );
        } catch(Exception e) {
            throw new AssertionError(e);
        }
        Matrix4f projection = new Matrix4f().ortho(0f, this.Width, this.Height, 0f, -1, 1);
        ResourceManager.getInstance().getShader("primitive").use();
        ResourceManager.getInstance().getShader("primitive").setMatrix4("projection", projection, false);
        Shader s = ResourceManager.getInstance().getShader("primitive");
        squareRenderer = new SquareRenderer(s);
        triangleRenderer = new TriangleRenderer(s);
        circleRenderer = new CircleRenderer(s);

        // TODO: Create TextRenderer and load font

        this.resetGame();
    }

    public void processInput(){
        if (this.state == GAME_ACTIVE)
        {
            if (this.keys[GLFW_KEY_A] && (player.getSnakeBody().size() == 1 || (player.getSnakeBody().size() > 1 && player.getLastDirection() != RIGHT)))
            {
                player.setNextDirection(LEFT);
            }
            if (this.keys[GLFW_KEY_D] && (player.getSnakeBody().size() == 1 || (player.getSnakeBody().size() > 1 && player.getLastDirection() != LEFT)))
            {
                player.setNextDirection(RIGHT);
            }
            if (this.keys[GLFW_KEY_W] && (player.getSnakeBody().size() == 1 || (player.getSnakeBody().size() > 1 && player.getLastDirection() != DOWN)))
            {
                player.setNextDirection(UP);
            }
            if (this.keys[GLFW_KEY_S] && (player.getSnakeBody().size() == 1 || (player.getSnakeBody().size() > 1 && player.getLastDirection() != UP)))
            {
                player.setNextDirection(DOWN);
            }
        }
    }

    public void update()
    {
        if (this.state == GAME_ACTIVE)
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

    public void render(){
        if (this.state == GAME_ACTIVE)
        {
            player.draw();
            food.draw();

            // draw grid
            for (int x = 1; x < GridX; x++)
            {
                squareRenderer.draw(new Vector2f((x * CELL_SIZE) - (GRID_LINE_WIDTH / 2), 0.f), new Vector2f(GRID_LINE_WIDTH, Height), 0.0f, new Vector3f(0.f, 0.f, 0.89f));
            }
            for (int y = 1; y < GridY; y++)
            {
                squareRenderer.draw(new Vector2f(0.f, (y * CELL_SIZE) - (GRID_LINE_WIDTH / 2)), new Vector2f(Width, GRID_LINE_WIDTH), 0.0f, new Vector3f(0.f, 0.f, 0.89f));
            }
        }
    }

    public void resetGame(){
        Vector2f playerPos = new Vector2f(2, 2);
        player = new Player(playerPos, new Vector2f(CELL_SIZE, CELL_SIZE), PLAYER_COLORS[0], RIGHT, squareRenderer);
        // spawn food, needs to happen after Player init
        food = new Food(new Vector2f(CELL_SIZE, CELL_SIZE), FOOD_COLOR, circleRenderer, this.availableGridCells);
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
            this.state = GAME_WIN;
        }
    }

    public void checkLossCondition()
    {
        Vector2f head = player.getSnakeHead();
        // snake is outside of grid
        if (head.x < 0 || head.y < 0 || head.x >= this.GridX || head.y >= this.GridY)
        {
            this.stopUpdate();
            this.state = GAME_LOSS;
            return;
        }

        // snake hits its own body
        List<Vector2f> snakeBody = player.getSnakeBody();
        for (int i = 1; i < snakeBody.size(); i++) {
            Vector2f currentSegment = snakeBody.get(i);
            if(currentSegment.x == head.x && currentSegment.y == head.y) {
                this.stopUpdate();
                this.state = GAME_LOSS;
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
