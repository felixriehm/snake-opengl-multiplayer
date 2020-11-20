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
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import client.game.shader.Shader;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.glfw.GLFW.*;

public class Game {
    public static final int GRID_LINE_WIDTH = 2;
    private final NetworkManager networkManager;
    private final AIController aiController;
    private final boolean withOpenGL;
    private MsgFactory msgFactory;
    private ClientGameState state;
    private int Width, Height, GridX, GridY;
    private IPrimitiveRenderer squareRenderer;
    private IPrimitiveRenderer triangleRenderer;
    private IPrimitiveRenderer circleRenderer;
    public final Vector3f FOOD_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.food.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.food.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.food.color.b")));
    public final Vector3f GRID_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.b")));
    private LinkedList<Player> players = new LinkedList<Player>();
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

    public void start(int playerCount, int gridX, int gridY, ClientGameState gameState, Set<Vector2f> food, LinkedList<LinkedList<Vector2f>> snakes){
        this.playerCount = playerCount;
        this.setGameState(gameState);
        this.setGridSize(gridX, gridY);
        // TODO: inside update init Food and Player new with given data and new cellSize
        // on server side remove players from list
        this.food = new Food(new Vector2f(cellSize, cellSize), FOOD_COLOR, circleRenderer, this);
        setFood(food);
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player(new Vector2f(cellSize, cellSize), generatePlayerColor(i) ,squareRenderer, this));
        }
        setSnakes(snakes);
    }

    private Vector3f generatePlayerColor(int i) {
        return new Vector3f((float) i / ((float) playerCount), 1f,1f);
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
        if(this.state == ClientGameState.GAME_MENU) {
            if (this.keys[GLFW_KEY_ENTER] && !keysProcessed[GLFW_KEY_ENTER]) {
                networkManager.sendMessage(msgFactory.getRequestStartGameMsg());
                keysProcessed[GLFW_KEY_ENTER] = true;
            }
        }
    }

    public void render(){
        if (this.state == ClientGameState.GAME_ACTIVE)
        {
            if(players != null) {
                players.forEach(player -> player.draw());
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

    private void setFood(Set<Vector2f> food){
        this.food.setFood(food);
    }
    private void setSnakes(LinkedList<LinkedList<Vector2f>> players){
        for (int i = 0; i < players.size(); i++) {
            this.players.get(i).setBody(players.get(i));
        }
    }

    public void update(Set<Vector2f> food, LinkedList<LinkedList<Vector2f>> players, int gridX, int gridY){
        setGridSize(gridX, gridY);
        setFood(food);
        setSnakes(players);

        if(aiController != null) {
            Direction nextDirection = aiController.getNextMove();
            networkManager.sendMessage(msgFactory.getMoveMsg(nextDirection));
        }
    }

    public int getCellSize() {
        return this.cellSize;
    }
}
