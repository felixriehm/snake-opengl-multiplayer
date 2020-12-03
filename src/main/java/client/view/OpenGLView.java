package client.view;

import client.controller.game.Game;
import client.view.renderer.*;
import client.view.shader.Shader;
import client.controller.network.NetworkManager;
import client.view.manager.ResourceManager;
import common.Configuration;
import common.game.model.*;
import common.network.MsgFactory;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.UUID;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLView {
    private long window;
    private Game snake;
    private MsgFactory msgFactory;
    private NetworkManager networkManager;
    // The Width of the screen
    private final int SCREEN_WIDTH = Integer.parseInt(Configuration.getInstance().getProperty("client.window.width"));
    // The height of the screen
    private final int SCREEN_HEIGHT = Integer.parseInt(Configuration.getInstance().getProperty("client.window.height"));
    public static final int GRID_LINE_WIDTH = 2;
    private boolean[] keys = new boolean[1024];
    private boolean[] keysProcessed = new boolean[1024];
    public final Vector3f GRID_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.grid.color.b")));
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
    public final Vector3f WALL_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.wall.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.wall.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.wall.color.b")));
    public final Vector3f RUIN_COLOR = new Vector3f(
            Float.parseFloat(Configuration.getInstance().getProperty("game.ruin.color.r")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.ruin.color.g")),
            Float.parseFloat(Configuration.getInstance().getProperty("game.ruin.color.b")));
    private IPrimitiveRenderer squareRenderer;
    private IPrimitiveRenderer triangleRenderer;
    private IPrimitiveRenderer circleRenderer;
    private TextRenderer textRenderer;

    public void run(Game game, NetworkManager networkManager) {
        this.snake = game;
        this.networkManager = networkManager;
        this.msgFactory = networkManager.getMsgFactory();
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "YASC", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ){
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
            if (key >= 0 && key < 1024)
            {
                if (action == GLFW_PRESS)
                    this.keys[key] = true;
                else if (action == GLFW_RELEASE)
                {
                    this.keys[key] = false;
                    this.keysProcessed[key] = false;
                }
            }
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void initRenderer() {
        try{
            ResourceManager.getInstance().loadShader("src/main/java/client/view/shader/primitive-vs.glsl",
                    "src/main/java/client/view/shader/primitive-fs.glsl",
                    null,"primitive"
            );
        } catch(Exception e) {
            throw new AssertionError(e);
        }
        Matrix4f projection = new Matrix4f().ortho(0f, SCREEN_WIDTH, SCREEN_HEIGHT, 0f, -1, 1);
        ResourceManager.getInstance().getShader("primitive").use();
        ResourceManager.getInstance().getShader("primitive").setMatrix4("projection", projection, false);
        Shader s = ResourceManager.getInstance().getShader("primitive");
        squareRenderer = new SquareRenderer(s);
        triangleRenderer = new TriangleRenderer(s);
        circleRenderer = new CircleRenderer(s);
        textRenderer = new TextRenderer(projection);
        textRenderer.load("src/main/resources/fonts/OCRAEXT.TTF", 24);
        // TODO: Create TextRenderer and load font
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        initRenderer();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            this.processInput();
            this.render();

            glfwSwapBuffers(window); // swap the color buffers
        }
    }

    private void render() {
        if (snake.getGridX() > 0 )
        {
            int cellSize = SCREEN_WIDTH / snake.getGridX();
            if(snake.getGameData() != null) {
                for(PointGameData point : snake.getGameData()) {
                    if(point instanceof PointSnake) {
                        PointSnake pointSnake = (PointSnake) point;
                        squareRenderer.draw(
                                new Vector2f(point.getX() * cellSize, point.getY() * cellSize),
                                new Vector2f(cellSize, cellSize),
                                0f,
                                generatePlayerColor(pointSnake.getUuid())
                        );
                    }
                    if(point instanceof PointFood) {
                        circleRenderer.draw(
                                new Vector2f(point.getX() * cellSize  + cellSize/2, point.getY() * cellSize + cellSize / 2 ),
                                new Vector2f(cellSize, cellSize),
                                0f,
                                FOOD_COLOR
                        );
                    }

                    if(point instanceof PointWall) {
                        triangleRenderer.draw(
                                new Vector2f(point.getX() * cellSize + cellSize/2, point.getY() * cellSize + cellSize/2),
                                new Vector2f(cellSize, cellSize),
                                0f,
                                WALL_COLOR
                        );
                    }
                    if(point instanceof PointRuin) {
                        triangleRenderer.draw(
                                new Vector2f(point.getX() * cellSize + cellSize/2, point.getY() * cellSize + cellSize/2),
                                new Vector2f(cellSize, cellSize),
                                0f,
                                RUIN_COLOR
                        );
                    }
                }
            }

            // draw grid
            for (int x = 1; x < snake.getGridX(); x++)
            {
                squareRenderer.draw(new Vector2f((x * cellSize) - (GRID_LINE_WIDTH / 2), 0.f),
                        new Vector2f(GRID_LINE_WIDTH, SCREEN_HEIGHT), 0.0f, GRID_COLOR);
            }
            for (int y = 1; y < snake.getGridY(); y++)
            {
                squareRenderer.draw(new Vector2f(0.f, (y * cellSize) - (GRID_LINE_WIDTH / 2)),
                        new Vector2f(SCREEN_WIDTH, GRID_LINE_WIDTH), 0.0f, GRID_COLOR);
            }
        }

        if(snake.getState() == ClientGameState.GAME_MENU){
            squareRenderer.draw(new Vector2f(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f),
                    new Vector2f(70f, 70f), 0.0f, new Vector3f(1f,1f,1f));

            textRenderer.renderText("Press Enter to start or restart.",
                    SCREEN_WIDTH / 2f, SCREEN_WIDTH / 2f, 1f, new Vector3f(1f,1f,1f));
        }

        if(snake.getState() == ClientGameState.GAME_WIN){
            squareRenderer.draw(new Vector2f(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f),
                    new Vector2f(70f, 70f), 0.0f, new Vector3f(0f,1f,0f));
            textRenderer.renderText("Won!",
                    SCREEN_WIDTH / 2f, SCREEN_WIDTH / 2f, 1f, new Vector3f(1f,1f,1f));
        }

        if(snake.getState() == ClientGameState.GAME_LOSS){
            squareRenderer.draw(new Vector2f(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f),
                    new Vector2f(70f, 70f), 0.0f, new Vector3f(1f,0f,0f));
            textRenderer.renderText("Lost!",
                    SCREEN_WIDTH / 2f, SCREEN_WIDTH / 2f, 1f, new Vector3f(1f,1f,1f));
        }
    }

    private Vector3f generatePlayerColor(UUID id) {
        if(id.equals(networkManager.getId())) {
            return PLAYER_COLOR;
        }
        return ENEMY_COLOR;
    }

    private void processInput(){
        if (snake.getState() == ClientGameState.GAME_ACTIVE) {
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
            if (this.keys[GLFW_KEY_G] && !keysProcessed[GLFW_KEY_G]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.PLAYER_GROW));
                keysProcessed[GLFW_KEY_G] = true;
            }
            if (this.keys[GLFW_KEY_V] && !keysProcessed[GLFW_KEY_V]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.TOGGLE_MAX_VIEW));
                keysProcessed[GLFW_KEY_V] = true;
            }
            if (this.keys[GLFW_KEY_B] && !keysProcessed[GLFW_KEY_B]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.TOGGLE_DISCOVERED_VIEW));
                keysProcessed[GLFW_KEY_B] = true;
            }
            if (this.keys[GLFW_KEY_U] && !keysProcessed[GLFW_KEY_U]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.TOGGLE_UPDATE));
                keysProcessed[GLFW_KEY_U] = true;
            }
            if (this.keys[GLFW_KEY_E] && !keysProcessed[GLFW_KEY_E]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.TOGGLE_WORLD_EVENT));
                keysProcessed[GLFW_KEY_E] = true;
            }
            if (this.keys[GLFW_KEY_K] && !keysProcessed[GLFW_KEY_K]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.PLAYER_KILL));
                keysProcessed[GLFW_KEY_K] = true;
            }
            if (this.keys[GLFW_KEY_R] && !keysProcessed[GLFW_KEY_R]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.PLAYER_RESPAWN));
                keysProcessed[GLFW_KEY_R] = true;
            }
            if (this.keys[GLFW_KEY_I] && !keysProcessed[GLFW_KEY_I]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.PLAYER_IMMORTAL));
                keysProcessed[GLFW_KEY_I] = true;
            }
            if (this.keys[GLFW_KEY_KP_ADD] && !keysProcessed[GLFW_KEY_KP_ADD]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.GREAT_WALL_INCREASE));
                keysProcessed[GLFW_KEY_KP_ADD] = true;
            }
            if (this.keys[GLFW_KEY_KP_SUBTRACT] && !keysProcessed[GLFW_KEY_KP_SUBTRACT]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.GREAT_WALL_DECREASE));
                keysProcessed[GLFW_KEY_KP_SUBTRACT] = true;
            }
            if (this.keys[GLFW_KEY_F] && !keysProcessed[GLFW_KEY_F]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.FOOD_SPAWN));
                keysProcessed[GLFW_KEY_F] = true;
            }
            if (this.keys[GLFW_KEY_Q] && !keysProcessed[GLFW_KEY_Q]) {
                networkManager.sendMessage(msgFactory.getCheatGrowMsg(CheatCode.FOOD_SHUFFLE));
                keysProcessed[GLFW_KEY_Q] = true;
            }
        }

        if (this.keys[GLFW_KEY_ENTER] && !keysProcessed[GLFW_KEY_ENTER]) {
            networkManager.sendMessage(msgFactory.getRequestStartGameMsg());
            keysProcessed[GLFW_KEY_ENTER] = true;
        }
    }
}
