package client.view;

import client.controller.game.Game;
import client.view.renderer.*;
import client.view.shader.Shader;
import client.controller.network.NetworkManager;
import client.view.manager.ResourceManager;
import common.Configuration;
import common.game.ClientGameState;
import common.game.Direction;
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

        glEnable(GL_CULL_FACE);
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
        if (snake.getState() == ClientGameState.GAME_ACTIVE)
        {
            int cellSize = SCREEN_WIDTH / snake.getGridX();
            if(snake.getPlayers() != null) {
                snake.getPlayers().entrySet().forEach(player -> player.getValue()
                        .draw(squareRenderer, cellSize, generatePlayerColor(player.getKey()), new Vector2f(cellSize, cellSize)));
            }
            if(snake.getFood() != null) {
                snake.getFood().draw(circleRenderer, cellSize, FOOD_COLOR, new Vector2f(cellSize, cellSize));
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
            for (int y = 1; y < 10; y++)
            {
                squareRenderer.draw(new Vector2f(0.f, (y * 64) - (GRID_LINE_WIDTH / 2)),
                        new Vector2f(SCREEN_WIDTH, GRID_LINE_WIDTH), 0.0f, GRID_COLOR);
            }

            textRenderer.renderText("Press Enter to start or restart the game.",
                    this.SCREEN_WIDTH / 2.0f, this.SCREEN_HEIGHT / 2.0f, 1.0f, new Vector3f(0.0f, 1.0f, 1.0f));
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
        }

        if (this.keys[GLFW_KEY_ENTER] && !keysProcessed[GLFW_KEY_ENTER]) {
            networkManager.sendMessage(msgFactory.getRequestStartGameMsg());
            keysProcessed[GLFW_KEY_ENTER] = true;
        }
    }
}
