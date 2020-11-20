package client;

import client.game.Game;
import client.network.NetworkManager;
import common.Configuration;
import common.game.ai.AIController;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Client {
    private long window;
    private Game snake;
    // The Width of the screen
    private final int SCREEN_WIDTH = Integer.parseInt(Configuration.getInstance().getProperty("client.window.width"));
    // The height of the screen
    private final int SCREEN_HEIGHT = Integer.parseInt(Configuration.getInstance().getProperty("client.window.height"));

    public void run(List<String> args) {
        init();
        loop(args);

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void runWithoutOpenGL(List<String> args) {
        NetworkManager nm = new NetworkManager();
        if(args.contains("ai")) {
            snake = new Game(nm, new AIController(), false);
        } else {
            snake = new Game(nm, null, false);
        }
        try {
            nm.run(snake);
        } catch (IOException e) {
            e.printStackTrace();
        }

        snake.init(0, 0);
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
                    snake.setKeys(key,true);
                else if (action == GLFW_RELEASE)
                {
                    snake.setKeys(key,false);
                    snake.setKeysProcessed(key,false);
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

    private void loop(List<String> args) {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        NetworkManager nm = new NetworkManager();
        if(args.contains("ai")) {
            snake = new Game(nm, new AIController(), true);
        } else {
            snake = new Game(nm, null, true);
        }
        try {
            nm.run(snake);
        } catch (IOException e) {
            e.printStackTrace();
        }

        snake.init(SCREEN_WIDTH, SCREEN_HEIGHT);

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer


            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            snake.processInput();
            snake.render();

            glfwSwapBuffers(window); // swap the color buffers
        }
    }

    public static void main(String[] args) {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
        if(arguments.size() == 0) {
            arguments.add("openGL");
        }

        if(arguments.contains("openGL")){
            new Client().run(arguments);
        } else {
            new Client().runWithoutOpenGL(arguments);
        }
    }


}
