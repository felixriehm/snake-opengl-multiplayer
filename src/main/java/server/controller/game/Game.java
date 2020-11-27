package server.controller.game;

import common.Configuration;
import common.game.ClientGameState;
import common.game.Direction;
import common.game.ServerGameState;
import common.network.MsgFactory;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import server.Server;
import server.model.game.entity.Food;
import server.model.game.entity.Player;
import server.controller.network.ClientManager;
import server.util.QuadTree;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private static final Logger logger = LogManager.getLogger(Game.class.getName());

    public static final int GAME_UPDATE_RATE = Integer.parseInt(Configuration.getInstance().getProperty("game.update-rate"));
    public static final int GAME_WORLD_EVENT_COUNTDOWN = Integer.parseInt(Configuration.getInstance().getProperty("game.world-event.countdown"));
    public static final int GAME_WORLD_EVENT_GRID_DECREASE = Integer.parseInt(Configuration.getInstance().getProperty("game.world-event.grid-decrease"));
    public static final boolean GAME_WORLD_EVENT_ENABLED = Boolean.parseBoolean(Configuration.getInstance().getProperty("game.world-event.enabled"));
    private ServerGameState state;
    private int GridX, GridY;
    // will be modified inside Player and Food Object
    private Set availableGridCells = new HashSet();
    private HashMap<UUID, Player> players = new HashMap<UUID, Player>();
    private Food food;
    private MsgFactory msgFactory;
    private Server server;
    private boolean[] keys = new boolean[1024];
    private boolean[] keysProcessed = new boolean[1024];
    private Game gameReference;
    private Timer updateTimer;
    private boolean gameInitiated = false;
    private int worldEventCountdown = GAME_WORLD_EVENT_COUNTDOWN;
    private int playerCount = 0;

    public Game () {}

    public void init(int gridX, int gridY, Set<UUID> clients, Server server) {
        this.server = server;
        this.worldEventCountdown = GAME_WORLD_EVENT_COUNTDOWN;
        this.msgFactory = this.server.getMsgFactory();
        this.gameInitiated = true;
        this.state = ServerGameState.GAME_STARTED;
        this.GridX = gridX;
        this.GridY = gridY;
        this.playerCount = clients.size();

        for (int x = 0; x < this.GridX; x++)
        {
            for (int y = 0; y < this.GridY; y++)
            {
                availableGridCells.add(new Vector2f(x,y));
            }
        }

        gameReference = this;

        Set<Vector2f> freeCells = new HashSet<Vector2f>(availableGridCells);
        // spawn edge safe zone of 2 cells
        // left edge
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < gridY; y++) {
                freeCells.remove(new Vector2f(x,y));
            }
        }
        // upper edge
        for (int x = 0; x < gridX; x++) {
            for (int y = 0; y < 2; y++) {
                freeCells.remove(new Vector2f(x,y));
            }
        }
        // right edge
        for (int x = gridX - 1; x >= gridX - 2; x--) {
            for (int y = 0; y < gridY; y++) {
                freeCells.remove(new Vector2f(x,y));
            }
        }
        // lower edge
        for (int x = 0; x < gridX; x++) {
            for (int y = gridY - 1; y >= gridY - 2; y--) {
                freeCells.remove(new Vector2f(x,y));
            }
        }
        clients.forEach(clientId -> this.players.put(clientId, new Player(freeCells, GridX, GridY)));

        // spawn food, needs to happen after Player init
        food = new Food(this.availableGridCells);
        food.spawnFood(this.players.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet()));

        this.updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gameReference.update();
            }
        }, 0, GAME_UPDATE_RATE);
    }

    public Food getFood(){
        return this.food;
    }

    public HashMap<UUID, Player> getPlayers(){
        return this.players;
    }

    public int getWorldEventCountdown() {
        return worldEventCountdown;
    }

    public boolean isInitiated(){
        return gameInitiated;
    }

    public void update()
    {
        if (this.state == ServerGameState.GAME_STARTED)
        {
            // update objects
            players.values().forEach(player -> player.moveSnake());

            // process world event
            if(GAME_WORLD_EVENT_ENABLED) {
                this.doWorldEvent();
            }

            // check for collisions
            this.doCollisions();

            // check loss condition
            this.checkLossCondition();

            // check win condition
            this.checkWinCondition();


            this.server.broadcastMsg(msgFactory.getGameEntitiesMsg(
                    food.getFood(),
                    getPlayersInfo(),
                    this.GridX,
                    this.GridY,
                    this.getWorldEventCountdown()
            ));
        }
    }

    public HashMap<UUID, Pair<List<Vector2f>, Direction>> getPlayersInfo() {
        HashMap<UUID, Pair<List<Vector2f>, Direction>> playersDTO = new HashMap<>();
        for(Map.Entry<UUID, Player> player : this.players.entrySet()) {
            playersDTO.put(player.getKey(), new Pair<>(new LinkedList<Vector2f>(player.getValue().getSnakeBody()),
                    player.getValue().getLastDirection()));
        }
        return playersDTO;
    }

    private void doWorldEvent() {
        if(worldEventCountdown == 0) {
            this.GridX = this.GridX - (GAME_WORLD_EVENT_GRID_DECREASE*2);
            this.GridY = this.GridY - (GAME_WORLD_EVENT_GRID_DECREASE*2);

            availableGridCells = new HashSet();
            for (int x = 0; x < this.GridX; x++)
            {
                for (int y = 0; y < this.GridY; y++)
                {
                    availableGridCells.add(new Vector2f(x,y));
                }
            }

            this.food.setFood(this.food.getFood().stream()
                    .map(foodCell -> new Vector2f(foodCell.x - GAME_WORLD_EVENT_GRID_DECREASE,
                            foodCell.y - GAME_WORLD_EVENT_GRID_DECREASE))
                    .collect(Collectors.toCollection(HashSet::new)));
            this.food.setAvailableGridCells(availableGridCells);

            ArrayList<Vector2f> deletedFood = new ArrayList();
            for(Vector2f foodCell : this.food.getFood()) {
                if(foodCell.x < 0 || foodCell.y < 0 || foodCell.x >= this.GridX || foodCell.y >= this.GridY) {
                    deletedFood.add(foodCell);
                }
            }

            deletedFood.forEach(foodCell -> {
                this.food.removeFood(foodCell);
                this.food.spawnFood(this.players.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet()));
            });

            this.players.values().forEach(player -> {
                    player.setSnakeBody(player.getSnakeBody().stream()
                            .map(cell -> new Vector2f(cell.x - GAME_WORLD_EVENT_GRID_DECREASE,
                                    cell.y - GAME_WORLD_EVENT_GRID_DECREASE))
                            .collect(Collectors.toCollection(LinkedList::new)));
                    Vector2f lastSegmentOfLastMove = player.getLastSegmentOfLastMove();
                    player.setLastSegmentOfLastMove(new Vector2f(lastSegmentOfLastMove.x - GAME_WORLD_EVENT_GRID_DECREASE,
                            lastSegmentOfLastMove.y - GAME_WORLD_EVENT_GRID_DECREASE));
            });

            worldEventCountdown = GAME_WORLD_EVENT_COUNTDOWN;
        }
        worldEventCountdown--;
    }

    public void setKeys(int index, boolean value){
        this.keys[index] = value;
    }

    public void setKeysProcessed(int index, boolean value){
        this.keysProcessed[index] = value;
    }

    public void checkWinCondition() {
       if(players.size() == 1) {
            ClientManager client = (ClientManager) this.server.getClients().get(players.keySet().toArray()[0]);
            client.sendMessage(msgFactory.getGameStateMsg(ClientGameState.GAME_WIN));
            this.state = ServerGameState.GAME_ENDED;
            stopUpdate();
        }
    }

    public void checkLossCondition()
    {
        // store players which should be removed after collision check
        HashSet<UUID> removedPlayer = new HashSet<>();

        // init QuadTree
        QuadTree.PointRegionQuadTree tree = new QuadTree.PointRegionQuadTree(0, 0, GridX , GridY);

        // add all snake cells and do hit detection
        for(Map.Entry<UUID, Player> player : players.entrySet() ) {
            List<Vector2f> snake = player.getValue().getSnakeBody();
            // loop must be inverted so that self hit detection works
            for (int i = snake.size() - 1; i >= 0; i--) {
                Vector2f cell = snake.get(i);
                Pair<QuadTree.XYPointAltered, Boolean> tupel = tree.insertAltered(cell.x, cell.y, player.getKey(), i == 0);
                boolean inserted = tupel.getValue();
                QuadTree.XYPointAltered point = tupel.getKey();
                if(point != null){
                    // cell is already taken another player
                    if(!inserted) {
                        // head of another player is inside this player
                        if (point.isHead()) {
                            removedPlayer.add(point.getUUID());
                        }
                        // head of this player is inside another player or itself
                        if (i == 0) {
                            removedPlayer.add(player.getKey());
                        }
                    }
                } else {
                    // this player is outside of the grid
                    removedPlayer.add(player.getKey());
                }
            }
        }

        removedPlayer.forEach(player -> {
            players.remove(player);
            ClientManager client = (ClientManager) this.server.getClients().get(player);
            if(client != null) {
                client.sendMessage(msgFactory.getGameStateMsg(ClientGameState.GAME_LOSS));
            }
        });


        if(players.size() == 0) {
            this.state = ServerGameState.GAME_ENDED;
            stopUpdate();
        }
    }

    public void doCollisions()
    {
        for(Player player : players.values() ) {
            Set<Vector2f> intersectedCells = new HashSet<Vector2f>(player.getSnakeBody());
            intersectedCells.retainAll(food.getFood());

            if (!intersectedCells.isEmpty()) {
                player.grow();
                food.removeFood(intersectedCells);
                food.spawnFood(this.players.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet()));
            }
        }
    }

    public void stopUpdate(){
        if(updateTimer != null) {
            updateTimer.cancel();
        }
    }
}
