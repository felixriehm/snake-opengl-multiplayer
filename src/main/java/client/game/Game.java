package client.game;

import client.game.entity.Food;
import client.game.entity.Player;
import client.game.manager.ResourceManager;
import client.network.NetworkManager;
import common.game.Direction;
import common.game.GameState;
import common.network.MoveMsg;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import client.game.renderer.CircleRenderer;
import client.game.renderer.SquareRenderer;
import client.game.renderer.TriangleRenderer;
import client.game.shader.Shader;

import java.util.LinkedList;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class Game {
    public static final int GRID_LINE_WIDTH = 2;

    private GameState state;
    private int Width, Height, GridX, GridY;
    private SquareRenderer squareRenderer;
    private TriangleRenderer triangleRenderer;
    private CircleRenderer circleRenderer;
    private LinkedList<Player> players;
    private Food food;
    private boolean[] keys = new boolean[1024];
    private boolean[] keysProcessed = new boolean[1024];
    private int cellSize = 64;
    private static Game instance;


    private Game () { }

    public static Game getInstance () {
        if (Game.instance == null) {
            Game.instance = new Game();
        }
        return Game.instance;
    }

    public void init(int width, int height) {
        this.state = GameState.GAME_MENU;
        this.Width = width;
        this.Height = height;
        this.GridX = 10;
        this.GridY = 10;

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
    }

    public void processInput(){
        if (this.state == GameState.GAME_ACTIVE)
        {
            if (this.keys[GLFW_KEY_A])
            {
                NetworkManager.getInstance().sendMessage(new MoveMsg(Direction.LEFT, NetworkManager.getInstance().getId()));
            }
            if (this.keys[GLFW_KEY_D])
            {
                NetworkManager.getInstance().sendMessage(new MoveMsg(Direction.RIGHT, NetworkManager.getInstance().getId()));
            }
            if (this.keys[GLFW_KEY_W])
            {
                NetworkManager.getInstance().sendMessage(new MoveMsg(Direction.UP, NetworkManager.getInstance().getId()));
            }
            if (this.keys[GLFW_KEY_S]) {
                NetworkManager.getInstance().sendMessage(new MoveMsg(Direction.DOWN, NetworkManager.getInstance().getId()));
            }
        }
    }

    public void render(){
        if (this.state == GameState.GAME_ACTIVE)
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
                squareRenderer.draw(new Vector2f((x * this.cellSize) - (GRID_LINE_WIDTH / 2), 0.f), new Vector2f(GRID_LINE_WIDTH, Height), 0.0f, new Vector3f(0.f, 0.f, 0.89f));
            }
            for (int y = 1; y < GridY; y++)
            {
                squareRenderer.draw(new Vector2f(0.f, (y * this.cellSize) - (GRID_LINE_WIDTH / 2)), new Vector2f(Width, GRID_LINE_WIDTH), 0.0f, new Vector3f(0.f, 0.f, 0.89f));
            }
        }
    }

    public void setGridSize(int gridSize){
        this.GridX = gridSize;
        this.GridY = gridSize;
        this.cellSize = this.Width / gridSize;
    }

    public void setGameState(GameState state){
        this.state = state;
    }

    public void setKeys(int index, boolean value){
        this.keys[index] = value;
    }

    public void setKeysProcessed(int index, boolean value){
        this.keysProcessed[index] = value;
    }

    public void setFood(Set<Vector2f> food){
        this.food.setFood(food);
    }
    public void setSnakes(LinkedList<LinkedList<Vector2f>> players){
        for (int i = 0; i < players.size(); i++) {
            this.players.get(i).setBody(players.get(i));
        }
    }

    public int getCellSize() {
        return this.cellSize;
    }
}
