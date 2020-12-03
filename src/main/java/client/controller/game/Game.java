package client.controller.game;

import client.controller.network.NetworkManager;
import common.game.ai.AIController;
import common.game.model.*;
import common.network.MsgFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;

import java.util.*;

public class Game {
    private static final Logger logger = LogManager.getLogger(Game.class.getName());

    private final NetworkManager networkManager;
    private final AIController aiController;
    private MsgFactory msgFactory;
    private ClientGameState state;
    private int woldEventCountdown;
    private int GridX, GridY;

    private Direction lastDirection;

    private Set<PointGameData> gameData = new HashSet<>();
    private int playerCount;


    public Game (NetworkManager nm, AIController ai) { this.networkManager = nm; this.aiController = ai; }

    public void init() {
        this.msgFactory = networkManager.getMsgFactory();
        this.state = ClientGameState.GAME_MENU;
    }

    public void start(int playerCount, int gridX, int gridY, ClientGameState gameState,
                      Set<PointGameData> gameData, Direction lastDirection, int woldEventCountdown){
        this.playerCount = playerCount;
        this.woldEventCountdown = woldEventCountdown;
        this.setGameState(gameState);
        this.setGridSize(gridX, gridY);
        this.lastDirection = lastDirection;
        setGameData(gameData);
    }

    private void setGameData(Set<PointGameData> gameData) {
        this.gameData = gameData;
    }

    private void setGridSize(int gridX, int gridY){
        this.GridX = gridX;
        this.GridY = gridY;
    }

    public void setGameState(ClientGameState state){
        this.state = state;
    }

    public void update(Set<PointGameData> gameData, Direction lastDirection, int gridX,
                       int gridY, int woldEventCountdown){
        this.lastDirection = lastDirection;
        this.woldEventCountdown = woldEventCountdown;
        setGridSize(gridX, gridY);
        setGameData(gameData);

        Set<Vector2f> enemies = new HashSet<>();
        Set<Vector2f> food = new HashSet<>();
        Set<Vector2f> walls = new HashSet<>();
        Set<Vector2f> ruins = new HashSet<>();
        Set<Vector2f> playerBody = new HashSet<>();
        Vector2f playerHead = null;
        for (PointGameData point : gameData) {
            if(point instanceof PointSnake) {
                PointSnake pointSnake = (PointSnake) point;
                if(pointSnake.getUuid().equals(networkManager.getId())) {
                    if(pointSnake.isHead()) {
                        playerHead = new Vector2f(point.getX(), point.getY());
                    } else {
                        playerBody.add(new Vector2f(point.getX(), point.getY()));
                    }
                } else {
                    enemies.add(new Vector2f(point.getX(), point.getY()));
                }
            }
            if(point instanceof PointFood) {
                food.add(new Vector2f(point.getX(), point.getY()));
            }
            if(point instanceof PointWall) {
                walls.add(new Vector2f(point.getX(), point.getY()));
            }
            if(point instanceof PointRuin) {
                ruins.add(new Vector2f(point.getX(), point.getY()));
            }
        }
        if(aiController != null && playerHead != null) {
            Direction nextDirection = aiController.getNextMove(food, walls, ruins, enemies, playerBody, playerHead,
                    this.lastDirection, this.GridX, this.GridY, this.woldEventCountdown);
            logger.debug(nextDirection);
            networkManager.sendMessage(msgFactory.getMoveMsg(nextDirection));
        }
    }


    public ClientGameState getState() {
        return state;
    }

    public int getGridX() {
        return GridX;
    }

    public int getGridY() {
        return GridY;
    }

    public Set<PointGameData> getGameData() {
        return gameData;
    }
}
