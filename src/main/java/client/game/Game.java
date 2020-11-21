package client.game;

import client.game.entity.Food;
import client.game.entity.Player;
import client.game.manager.ResourceManager;
import client.game.renderer.*;
import client.network.NetworkManager;
import common.Configuration;
import common.game.ClientGameState;
import common.game.Direction;
import common.game.ai.AIController;
import common.network.MoveMsg;
import common.network.MsgFactory;
import common.network.RequestStartGameMsg;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import client.game.shader.Shader;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class Game {
    private static final Logger logger = LogManager.getLogger(Game.class.getName());

    public static final int GRID_LINE_WIDTH = 2;
    private final NetworkManager networkManager;
    private final AIController aiController;
    private final boolean withOpenGL;
    private MsgFactory msgFactory;
    private ClientGameState state;
    private int woldEventCountdown;
    private int Width, Height, GridX, GridY;
    private IPrimitiveRenderer squareRenderer;
    private IPrimitiveRenderer triangleRenderer;
    private IPrimitiveRenderer circleRenderer;
    private Direction lastDirection;
    public final Vector3f PLAYER_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.player.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.player.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.player.color.b")));
    public final Vector3f ENEMY_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.enemy.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.enemy.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.enemy.color.b")));
    public final Vector3f FOOD_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.food.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.food.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.food.color.b")));
    public final Vector3f GRID_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.b")));
    private HashMap<UUID, Player> players = new HashMap<>();
    private Food food;
    private boolean[] keys = new boolean[1024];
    private boolean[] keysProcessed = new boolean[1024];
    private int cellSize = 64;
    private int playerCount;


    public Game (NetworkManager nm, AIController ai, boolean withOpenGL) { this.networkManager = nm; this.aiController = ai; this.withOpenGL = withOpenGL; }

    public void init(int width, int height) {
        this.msgFactory = networkManager.getMsgFactory();
        this.state = ClientGameState.GAME_MENU;
        this.Width = width;
        this.Height = height;

        if(withOpenGL){
            try{
                ResourceManager.getInstance().loadShader("src/main/java/client/game/shader/primitive-vs.glsl",
                        "src/main/java/client/game/shader/primitive-fs.glsl",
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
        } else {
            squareRenderer = new NoOpenGLRenderer();
            triangleRenderer = new NoOpenGLRenderer();
            circleRenderer = new NoOpenGLRenderer();
        }
    }

    public void start(int playerCount, int gridX, int gridY, ClientGameState gameState, Set<Vector2f> food,
                      HashMap<UUID, Pair<List<Vector2f>, Direction>> snakes, int woldEventCountdown){
        this.playerCount = playerCount;
        this.woldEventCountdown = woldEventCountdown;
        this.setGameState(gameState);
        this.setGridSize(gridX, gridY);
        // on server side remove players from list
        this.lastDirection = snakes.get(networkManager.getId()).getValue();
        setFood(food);
        setPlayers(snakes);
    }

    private void setPlayers(HashMap<UUID, Pair<List<Vector2f>, Direction>> snakes) {
        players = new HashMap<>();
        snakes.entrySet().forEach(snake -> this.players.put(snake.getKey(),
                new Player(new Vector2f(cellSize, cellSize),
                        generatePlayerColor(snake.getKey()) ,squareRenderer, this, (LinkedList) snake.getValue().getKey())));
    }

    private void setFood(Set<Vector2f> food) {
        this.food = new Food(new Vector2f(cellSize, cellSize), FOOD_COLOR, circleRenderer, this);
        this.food.setFood(food);
    }

    private Vector3f generatePlayerColor(UUID id) {
        if(id.equals(networkManager.getId())) {
            return PLAYER_COLOR;
        }
        return ENEMY_COLOR;
    }

    public void processInput(){
        if (this.state == ClientGameState.GAME_ACTIVE) {
            if (this.keys[GLFW_KEY_A] && !keysProcessed[GLFW_KEY_A])
            {
                networkManager.sendMessage(msgFactory.getMoveMsg(Direction.LEFT));
                keysProcessed[GLFW_KEY_A] = true;
            }
            if (this.keys[GLFW_KEY_D] && !keysProcessed[GLFW_KEY_D])
            {
                networkManager.sendMessage(msgFactory.getMoveMsg(Direction.RIGHT));
                keysProcessed[GLFW_KEY_D] = true;
            }
            if (this.keys[GLFW_KEY_W] && !keysProcessed[GLFW_KEY_W])
            {
                networkManager.sendMessage(msgFactory.getMoveMsg(Direction.UP));
                keysProcessed[GLFW_KEY_W] = true;
            }
            if (this.keys[GLFW_KEY_S] && !keysProcessed[GLFW_KEY_S]) {
                networkManager.sendMessage(msgFactory.getMoveMsg(Direction.DOWN));
                keysProcessed[GLFW_KEY_S] = true;
            }
        }

        if (this.keys[GLFW_KEY_ENTER] && !keysProcessed[GLFW_KEY_ENTER]) {
            networkManager.sendMessage(msgFactory.getRequestStartGameMsg());
            keysProcessed[GLFW_KEY_ENTER] = true;
        }
    }

    public void render(){
        if (this.state == ClientGameState.GAME_ACTIVE)
        {
            if(players != null) {
                players.values().forEach(player -> player.draw());
            }
            if(food != null) {
                food.draw();
            }

            // draw grid
            for (int x = 1; x < GridX; x++)
            {
                squareRenderer.draw(new Vector2f((x * this.cellSize) - (GRID_LINE_WIDTH / 2), 0.f), new Vector2f(GRID_LINE_WIDTH, Height), 0.0f, GRID_COLOR);
            }
            for (int y = 1; y < GridY; y++)
            {
                squareRenderer.draw(new Vector2f(0.f, (y * this.cellSize) - (GRID_LINE_WIDTH / 2)), new Vector2f(Width, GRID_LINE_WIDTH), 0.0f, GRID_COLOR);
            }
        }
    }

    private void setGridSize(int gridX, int gridY){
        this.GridX = gridX;
        this.GridY = gridY;
        this.cellSize = this.Width / gridX;
    }

    public void setGameState(ClientGameState state){
        this.state = state;
    }

    public void setKeys(int index, boolean value){
        this.keys[index] = value;
    }

    public void setKeysProcessed(int index, boolean value){
        this.keysProcessed[index] = value;
    }


    public void update(Set<Vector2f> food, HashMap<UUID, Pair<List<Vector2f>, Direction>> players, int gridX,
                       int gridY, int woldEventCountdown){
        if(players.containsKey(networkManager.getId())) {
            this.lastDirection = players.get(networkManager.getId()).getValue();
        }
        this.woldEventCountdown = woldEventCountdown;
        setGridSize(gridX, gridY);
        setFood(food);
        setPlayers(players);

        if(aiController != null && this.players.containsKey(networkManager.getId())) {
            Set<LinkedList<Vector2f>> enemies = this.players.entrySet().stream()
                    .filter(p -> p.getKey() != networkManager.getId())
                    .map(player -> player.getValue().getSnakeBody())
                    .collect(Collectors.toCollection(HashSet::new));
            LinkedList<Vector2f> player = this.players.get(networkManager.getId()).getSnakeBody();

            Direction nextDirection = aiController.getNextMove(this.food.getFood(),enemies,player,
                    this.lastDirection, this.GridX, this.GridY, this.woldEventCountdown);
            networkManager.sendMessage(msgFactory.getMoveMsg(nextDirection));
        }
    }

    public int getCellSize() {
        return this.cellSize;
    }
}
