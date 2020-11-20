package server.game;

import client.network.NetworkManager;
import common.Configuration;
import common.game.ClientGameState;
import common.game.ServerGameState;
import common.network.MsgFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import server.Server;
import server.game.entity.Food;
import server.game.entity.Player;
import server.network.ClientManager;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private static final Logger logger = LogManager.getLogger(Game.class.getName());

    public static final Vector2f PLAYER_SIZE = new Vector2f(512.0f, 100.0f);
    public static final Vector2f PLAYER_VELOCITY = new Vector2f(500.0f, 500.0f);
    public static final int GAME_UPDATE_RATE = Integer.parseInt(Configuration.getInstance().getProperty("game.update-rate"));
    public static final int GAME_WORLD_EVENT_TRIGGER = Integer.parseInt(Configuration.getInstance().getProperty("game.world-event.trigger.moves"));

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
    private int worldEventCounter;
    private int playerCount = 0;

    public Game () {}

    public void init(int gridX, int gridY, Set<UUID> clients, Server server) {
        this.server = server;
        this.worldEventCounter = 0;
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
        }, GAME_UPDATE_RATE, GAME_UPDATE_RATE);
    }

    public Food getFood(){
        return this.food;
    }

    public HashMap<UUID, Player> getPlayers(){
        return this.players;
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
            this.doWorldEvent();

            // check for collisions
            this.doCollisions();

            // check loss condition
            this.checkLossCondition();

            // check win condition
            this.checkWinCondition();

            this.server.broadcastMsg(msgFactory.getGameEntitiesMsg(
                    food.getFood(),
                    players.entrySet().stream().map(player -> player.getValue().getSnakeBody()).collect(Collectors.toList()),
                    this.GridX,
                    this.GridY
            ));
        }
    }

    private void doWorldEvent() {
        if(worldEventCounter == GAME_WORLD_EVENT_TRIGGER) {
            this.GridX = this.GridX - 2;
            this.GridY = this.GridY - 2;

            // TODO: breaks refernce in Food
            availableGridCells = new HashSet();
            for (int x = 0; x < this.GridX; x++)
            {
                for (int y = 0; y < this.GridY; y++)
                {
                    availableGridCells.add(new Vector2f(x,y));
                }
            }
            // TODO: reset Food if it is outside of grid

            players.values().forEach(player ->
                    player.setSnakeBody(player.getSnakeBody().stream().map(cell -> new Vector2f(cell.x - 1, cell.y - 1)).collect(Collectors.toCollection(LinkedList::new)))
            );

            worldEventCounter = 0;
        }
        worldEventCounter++;
    }

    public void setKeys(int index, boolean value){
        this.keys[index] = value;
    }

    public void setKeysProcessed(int index, boolean value){
        this.keysProcessed[index] = value;
    }

    public void checkWinCondition() {
        int deadSnakes = 0;
        Map.Entry<UUID, Player> winner = null;
        for(Map.Entry<UUID, Player> p : players.entrySet()) {
            if(p.getValue().getSnakeBody().isEmpty()) {
                deadSnakes++;
            } else {
                winner = p;
            }
        }
       if(deadSnakes == players.size() - 1) {
            ClientManager client = (ClientManager) this.server.getClients().get(winner.getKey());
            client.sendMessage(msgFactory.getGameStateMsg(ClientGameState.GAME_WIN));
            this.state = ServerGameState.GAME_ENDED;
            stopUpdate();
        }
    }

    public void checkLossCondition()
    {

        List<Vector2f> allSnakeCells = players.values().stream().flatMap(player -> player.getSnakeBody().stream())
                .collect(Collectors.toList());

        ArrayList<UUID> removedPlayer = new ArrayList();

        for(Map.Entry<UUID, Player> player : players.entrySet() ) {
            ClientManager client = (ClientManager) this.server.getClients().get(player.getKey());

            Vector2f head = player.getValue().getSnakeHead();
            // client.game.snake is outside of grid
            if (head != null && (head.x < 0 || head.y < 0 || head.x >= this.GridX || head.y >= this.GridY)) {
                //this.stopUpdate();
                removedPlayer.add(player.getKey());
                client.sendMessage(msgFactory.getGameStateMsg(ClientGameState.GAME_LOSS));
                continue;
            }

            // client.game.snake hits its own or another body
            allSnakeCells.retainAll(Arrays.asList(head));
            if(allSnakeCells.size() > 1) {
                removedPlayer.add(player.getKey());
                client.sendMessage(msgFactory.getGameStateMsg(ClientGameState.GAME_LOSS));
                continue;
            }
        }

        removedPlayer.forEach(player -> players.get(player).getSnakeBody().clear());

        if(players.entrySet().stream().flatMap(player -> player.getValue().getSnakeBody().stream()).count() == 0) {
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
        updateTimer.cancel();
    }
}
