package server.controller.game;

import common.Configuration;
import common.game.model.*;
import common.network.MsgFactory;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import server.Server;
import server.model.game.entity.Food;
import server.model.game.entity.Player;
import server.controller.network.ClientManager;
import server.model.game.entity.Ruin;
import server.model.game.entity.Wall;
import server.util.DiamondSquare;
import server.util.QuadTree;

import java.net.ServerSocket;
import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private static final Logger logger = LogManager.getLogger(Game.class.getName());

    public static final int GAME_UPDATE_RATE = Integer.parseInt(Configuration.getInstance().getProperty("game.update-rate"));
    public static final int GAME_WORLD_EVENT_COUNTDOWN = Integer.parseInt(Configuration.getInstance().getProperty("game.world-event.countdown"));
    public static final int GAME_WORLD_EVENT_GRID_DECREASE = Integer.parseInt(Configuration.getInstance().getProperty("game.world-event.grid-decrease"));
    public static final boolean GAME_WORLD_EVENT_ENABLED = Boolean.parseBoolean(Configuration.getInstance().getProperty("game.world-event.enabled"));
    public static final int PLAYER_VIEW_DISTANCE = Integer.parseInt(Configuration.getInstance().getProperty("game.player.view-distance"));
    public static final boolean GAME_WIN_CONDITION_ENABLED = Boolean.parseBoolean(Configuration.getInstance().getProperty("game.win-condition.enabled"));
    private ServerGameState state;
    private int GridX, GridY;
    // will be modified inside Player and Food Object
    private Set availableGridCells = new HashSet();
    private HashMap<UUID, Player> players = new HashMap<UUID, Player>();
    private Food food;
    private Wall wall;
    private Ruin ruins;
    private MsgFactory msgFactory;
    private Server server;
    private Game gameReference;
    private UUID gameInitiator;
    private Timer updateTimer;
    private HashMap<CheatCode, Boolean> cheatCodes;
    private int worldEventCountdown = GAME_WORLD_EVENT_COUNTDOWN;
    private int playerCount = 0;

    public Game () {}

    public void init(int gridX, int gridY, Set<UUID> clients, Server server) {
        this.server = server;
        this.worldEventCountdown = GAME_WORLD_EVENT_COUNTDOWN;
        this.msgFactory = this.server.getMsgFactory();
        this.state = ServerGameState.GAME_STARTED;
        this.GridX = gridX;
        this.GridY = gridY;
        this.playerCount = clients.size();
        this.cheatCodes = new HashMap<>();

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

        wall = new Wall();
        wall.initGreatWall(0,0, gridX, gridY);
        ruins = new Ruin();
        ruins.initRuins(gridX);
        this.availableGridCells.removeAll(wall.getGreatWall());
        this.availableGridCells.removeAll(ruins.getGeneratedRuins());

        freeCells.retainAll(availableGridCells);
        clients.forEach(clientId -> this.players.put(clientId, new Player(freeCells)));

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

    public void update()
    {
        if (this.state == ServerGameState.GAME_STARTED && !isCheatCodeActivated(CheatCode.TOGGLE_UPDATE))
        {
            // update objects
            players.values().forEach(player -> player.moveSnake());

            // process world event
            if(GAME_WORLD_EVENT_ENABLED || isCheatCodeActivated(CheatCode.TOGGLE_WORLD_EVENT)) {
                this.doWorldEvent();
            }

            // check for collisions
            this.doCollisions();

            // check loss condition
            this.checkLossCondition();

            // check win condition
            if(GAME_WIN_CONDITION_ENABLED) {
                this.checkWinCondition();
            }

            this.server.broadcastGameUpdateMsg();
        }
    }

    public Set<PointGameData> getGameData(QuadTree.PointRegionQuadTree tree, Player player) {
        Set<PointGameData> worldSpace = getPlayerView(tree, player);
        Set<PointGameData> localSpace = transformWorldSpaceToLocalSpace(worldSpace, player);
        logger.debug(localSpace);
        return localSpace;
    }

    public Set<PointGameData> getGameData(QuadTree.PointRegionQuadTree tree) {
        return getPlayerView(tree);
    }

    private Set<PointGameData> transformWorldSpaceToLocalSpace(Set<PointGameData> points, Player player){
        Vector2f playerHead = player.getSnakeHead();
        float viewEdgeToGridEdgeDistanceX = getActualViewDistanceX(playerHead, player.getSnakeBody().size());
        float viewEdgeToGridEdgeDistanceY = getActualViewDistanceY(playerHead, player.getSnakeBody().size());
        HashSet<PointGameData> newPoints = new HashSet<>();
        for(PointGameData point : points) {
            if(point instanceof PointWall){
                newPoints.add(new PointWall(point.getX() - viewEdgeToGridEdgeDistanceX, point.getY() - viewEdgeToGridEdgeDistanceY));
            }
            if(point instanceof PointRuin){
                newPoints.add(new PointRuin(point.getX() - viewEdgeToGridEdgeDistanceX, point.getY() - viewEdgeToGridEdgeDistanceY));
            }
            if(point instanceof PointSnake){
                PointSnake pointSnake = (PointSnake) point;
                newPoints.add(new PointSnake(point.getX() - viewEdgeToGridEdgeDistanceX,
                        point.getY() - viewEdgeToGridEdgeDistanceY, pointSnake.getUuid(), pointSnake.isHead()));
            }
            if(point instanceof PointFood){
                newPoints.add(new PointFood(point.getX() - viewEdgeToGridEdgeDistanceX, point.getY() - viewEdgeToGridEdgeDistanceY));
            }
        }
        return newPoints;
    }

    public void saveDiscoveredRuins(QuadTree.PointRegionQuadTree<QuadTree.XYPointGameData> tree, Player player) {
        int playerGridX = calculatePlayerGrid(player);
        int playerGridY = playerGridX;
        Vector2f head = player.getSnakeHead();
        int snakeLength = player.getSnakeBody().size();
        for (QuadTree.XYPointGameData point : tree
                .queryRange(getActualViewDistanceX(head, snakeLength), getActualViewDistanceY(head, snakeLength),
                        playerGridX, playerGridY)) {
            Object pointGameData = point.getData();
            if (pointGameData instanceof PointRuin) {
                player.discoverRuin(new Vector2f((float) point.getX(), (float) point.getY()));
            }
        }
    }

    private Set<PointGameData> getPlayerView(QuadTree.PointRegionQuadTree<QuadTree.XYPointGameData> tree, Player player) {
        int playerGridX = calculatePlayerGrid(player);
        int playerGridY = playerGridX;
        Set<PointGameData> points = new HashSet<>();
        Vector2f head = player.getSnakeHead();
        int snakeLength = player.getSnakeBody().size();
        for(QuadTree.XYPointGameData point : tree
                .queryRange(getActualViewDistanceX(head, snakeLength), getActualViewDistanceY(head, snakeLength),
                        playerGridX, playerGridY)) {
            points.add((PointGameData) point.getData());
        }
        return points;
    }

    private Set<PointGameData> getPlayerView(QuadTree.PointRegionQuadTree<QuadTree.XYPointGameData> tree) {
        Set<PointGameData> points = new HashSet<>();
        Player playerGameInitiator = players.get(gameInitiator);

        for(QuadTree.XYPointGameData point : tree.queryRange(0, 0, GridX, GridY)) {
            Object pointGameData = point.getData();
            if(!isCheatCodeActivated(CheatCode.TOGGLE_DISCOVERED_VIEW)) {
                boolean isPointRuin = pointGameData instanceof PointRuin;
                if(!isPointRuin || (isPointRuin && playerGameInitiator != null &&
                        playerGameInitiator.getDiscoveredRuins()
                        .contains(new Vector2f((float)point.getX(), (float)point.getY())))) {
                    points.add((PointGameData) point.getData());
                }
            } else {
                points.add((PointGameData) point.getData());
            }
        }
        return points;
    }

    public QuadTree.PointRegionQuadTree generateGameDataQuadTree(){
        QuadTree.PointRegionQuadTree tree = new QuadTree.PointRegionQuadTree(0, 0, GridX , GridY);

        for(Map.Entry<UUID, Player> entry : this.players.entrySet()) {
            List<Vector2f> snake = entry.getValue().getSnakeBody();
            for (int i = 0; i < snake.size(); i++) {
                Vector2f point = snake.get(i);
                tree.insertGameData(point.x, point.y, new PointSnake(point.x,
                        point.y, entry.getKey(), i == 0));
            }
        }

        for(Vector2f food : food.getFood()) {
            tree.insertGameData(food.x, food.y, new PointFood(food.x, food.y));
        }

        for(Vector2f wall : wall.getGreatWall()) {
            tree.insertGameData(wall.x, wall.y, new PointWall(wall.x, wall.y));
        }

        for(Vector2f ruin : ruins.getGeneratedRuins()) {
            tree.insertGameData(ruin.x, ruin.y, new PointRuin(ruin.x, ruin.y));
        }

        return tree;
    }

    private float getActualViewDistanceX(Vector2f head, int snakeLength){
        return head.x - (snakeLength - 1) - PLAYER_VIEW_DISTANCE;
    }

    private float getActualViewDistanceY(Vector2f head, int snakeLength){
        return head.y - (snakeLength - 1) - PLAYER_VIEW_DISTANCE;
    }

    private void doWorldEvent() {
        if(worldEventCountdown == 0) {
            decreaseGreatWall();
            worldEventCountdown = GAME_WORLD_EVENT_COUNTDOWN;
        }
        worldEventCountdown--;
    }

    public void increaseGreatWall() {
        Vector2f currentStartPoint = this.wall.getGreatWallStartPoint();
        if(currentStartPoint.x - GAME_WORLD_EVENT_GRID_DECREASE < 0 ||
                currentStartPoint.y - GAME_WORLD_EVENT_GRID_DECREASE < 0 ) {
            return;
        }
        this.wall.initGreatWall(currentStartPoint.x - GAME_WORLD_EVENT_GRID_DECREASE,
                currentStartPoint.y - GAME_WORLD_EVENT_GRID_DECREASE,
                this.wall.getGreatWallWidth() + GAME_WORLD_EVENT_GRID_DECREASE,
                this.wall.getGreatWallHeight() + GAME_WORLD_EVENT_GRID_DECREASE
        );

        Vector2f newStartPoint = this.wall.getGreatWallStartPoint();
        availableGridCells = new HashSet();
        for (float x = newStartPoint.x + 1; x < this.wall.getGreatWallWidth() - 1; x++)
        {
            for (float y = newStartPoint.y + 1; y < this.wall.getGreatWallHeight() - 1; y++)
            {
                availableGridCells.add(new Vector2f(x,y));
            }
        }
        this.food.setAvailableGridCells(availableGridCells);
    }

    public void decreaseGreatWall() {
        Vector2f currentStartPoint = this.wall.getGreatWallStartPoint();
        this.wall.initGreatWall(currentStartPoint.x + GAME_WORLD_EVENT_GRID_DECREASE,
                currentStartPoint.y + GAME_WORLD_EVENT_GRID_DECREASE,
                this.wall.getGreatWallWidth() - GAME_WORLD_EVENT_GRID_DECREASE,
                this.wall.getGreatWallHeight() - GAME_WORLD_EVENT_GRID_DECREASE
        );

        logger.debug(wall.getGreatWall());
        logger.debug(food.getFood());

        Vector2f newStartPoint = this.wall.getGreatWallStartPoint();
        float startPointX = newStartPoint.x + 1;
        float startPointY = newStartPoint.y + 1;
        QuadTree.PointRegionQuadTree tree = new QuadTree.PointRegionQuadTree(
                startPointX,
                startPointY,
                Math.abs(this.wall.getGreatWallWidth() - 1 - startPointX),
                Math.abs(this.wall.getGreatWallHeight() - 1 - startPointY)
        );

        Set<Vector2f> foodSet = this.food.getFood();
        Set<Vector2f> removeFood = new HashSet<>();
        for(Vector2f food : foodSet){
            if(!tree.insert(food.x, food.y)){
                logger.debug("cant insert");
                removeFood.add(food);
            }
        }

        logger.debug(tree);

        this.food.removeFood(removeFood);

        availableGridCells = new HashSet();
        for (float x = newStartPoint.x + 1; x < this.wall.getGreatWallWidth() - 1; x++)
        {
            for (float y = newStartPoint.y + 1; y < this.wall.getGreatWallHeight() - 1; y++)
            {
                availableGridCells.add(new Vector2f(x,y));
            }
        }
        this.food.setAvailableGridCells(availableGridCells);

        removeFood.forEach(food -> this.food.spawnFood(this.players.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet())));
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

        // add all wall cells
        for (Vector2f point : wall.getGreatWall()){
            tree.insertGameData(point.x, point.y , new PointWall(point.x, point.y));
        }

        // add all ruins cells
        for (Vector2f point : ruins.getGeneratedRuins()){
            tree.insertGameData(point.x, point.y , new PointRuin(point.x, point.y));
        }

        // add all snake cells and do hit detection
        for(Map.Entry<UUID, Player> player : players.entrySet() ) {
            List<Vector2f> snake = player.getValue().getSnakeBody();
            // loop must be inverted so that self hit detection works
            for (int i = snake.size() - 1; i >= 0; i--) {
                Vector2f cell = snake.get(i);
                Pair<QuadTree.XYPointGameData, Boolean> tupel = tree.insertGameData(cell.x, cell.y, new PointSnake(cell.x, cell.y, player.getKey(), i == 0));
                boolean inserted = tupel.getValue();
                QuadTree.XYPointGameData point = tupel.getKey();
                if(point != null){
                    // cell is already taken another entity
                    if(!inserted) {
                        Object data = point.getData();
                        // head of another player is inside this player
                        if (data instanceof PointSnake) {
                            PointSnake pointSnake = ((PointSnake) data);
                            if(pointSnake.isHead()) {
                                removedPlayer.add(pointSnake.getUuid());
                            }
                        }
                        // head of this player is inside another player or itself or wall
                        if (i == 0) {
                            removedPlayer.add(player.getKey());
                        }
                    }
                } // else: this player is outside of the grid
            }
        }

        removedPlayer.forEach(player -> {
            if(isCheatCodeActivated(CheatCode.PLAYER_IMMORTAL) && gameInitiator.equals(player)){
                return;
            }
            killPlayer(player);
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

    public int calculatePlayerGrid(Player player){
        //TODO: only when ...
        return player.getSnakeBody().size()*2 + Game.PLAYER_VIEW_DISTANCE*2 - 1;
    }

    public int getGridX() {
        return GridX;
    }

    public int getGridY() {
        return GridY;
    }

    public void setGameInitiator(UUID client) {
        this.gameInitiator = client;
    }

    public UUID getGameInitiator() {
        return this.gameInitiator;
    }

    public ServerGameState getState() {
        return state;
    }

    public void toogleCheatCode(CheatCode cheatCode){
        if(this.cheatCodes != null) {
            Boolean result = this.cheatCodes.get(cheatCode);
            if(result == null) {
                this.cheatCodes.put(cheatCode, true);
            } else {
                this.cheatCodes.put(cheatCode, !result);
            }
        }
    }

    public boolean isCheatCodeActivated(CheatCode cheatCode) {
        if(this.cheatCodes != null) {
            Boolean result = this.cheatCodes.get(cheatCode);
            if(result == null) {
                return false;
            } else {
                return result;
            }
        }
        return false;
    }

    public void spawnFood(){
        this.food.spawnFood(this.players.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet()));
    }

    public void shuffleFood(){
        int size = food.getFood().size();
        food.clearFood();

        for (int i = 0; i < size; i++) {
            food.spawnFood(this.players.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet()));
        }
    }

    public void killPlayer(UUID player){
        players.remove(player);
        ClientManager client = (ClientManager) this.server.getClients().get(player);
        if(client != null) {
            client.sendMessage(msgFactory.getGameStateMsg(ClientGameState.GAME_LOSS));
        }
    }

    public void respawnPlayer(UUID player) {
        //TODO: implement
        /*
        Set<Vector2f> freeCells = new HashSet<Vector2f>(availableGridCells);

        freeCells.removeAll(this.players.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet()));
        this.players.put(player, new Player(freeCells));
        ((ClientManager) server.getClients().get(player)).sendMessage(msgFactory.getGameStateMsg(ClientGameState.GAME_ACTIVE));
        logger.debug(this.players.values());
        */
    }
}
