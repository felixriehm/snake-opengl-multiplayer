package client.controller.game;

import client.model.game.entity.Food;
import client.model.game.entity.Player;
import client.controller.network.NetworkManager;
import common.game.ClientGameState;
import common.game.Direction;
import common.game.ai.AIController;
import common.network.MsgFactory;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private static final Logger logger = LogManager.getLogger(Game.class.getName());

    private final NetworkManager networkManager;
    private final AIController aiController;
    private MsgFactory msgFactory;
    private ClientGameState state;
    private int woldEventCountdown;
    private int GridX, GridY;

    private Direction lastDirection;

    private HashMap<UUID, Player> players = new HashMap<>();
    private Food food;
    private int playerCount;


    public Game (NetworkManager nm, AIController ai) { this.networkManager = nm; this.aiController = ai; }

    public void init() {
        this.msgFactory = networkManager.getMsgFactory();
        this.state = ClientGameState.GAME_MENU;
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
                new Player((LinkedList) snake.getValue().getKey())));
    }

    private void setFood(Set<Vector2f> food) {
        this.food = new Food();
        this.food.setFood(food);
    }

    private void setGridSize(int gridX, int gridY){
        this.GridX = gridX;
        this.GridY = gridY;
    }

    public void setGameState(ClientGameState state){
        this.state = state;
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


    public ClientGameState getState() {
        return state;
    }

    public int getGridX() {
        return GridX;
    }

    public int getGridY() {
        return GridY;
    }

    public HashMap<UUID, Player> getPlayers() {
        return players;
    }

    public Food getFood() {
        return food;
    }
}
