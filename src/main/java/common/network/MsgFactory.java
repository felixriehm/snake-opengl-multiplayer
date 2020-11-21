package common.network;

import common.game.ClientGameState;
import common.game.Direction;
import javafx.util.Pair;
import org.joml.Vector2f;

import java.util.*;

public class MsgFactory {
    private UUID networkId;

    public MsgFactory () {}

    public void init (UUID networkId) {
        this.networkId = networkId;
    }

    public GameUpdateMsg getGameEntitiesMsg(Set<Vector2f> food, HashMap<UUID, Pair<List<Vector2f>, Direction>> snakes, int gridX, int gridY){
        Set<Vector2f> foodCopy = new HashSet<>(food);

        return new GameUpdateMsg(foodCopy, snakes, networkId, gridX, gridY);
    }

    public GameStateMsg getGameStateMsg(ClientGameState gameState){
        return new GameStateMsg(gameState, networkId);
    }

    public RequestStartGameMsg getRequestStartGameMsg(){
        return new RequestStartGameMsg(networkId);
    }

    public MoveMsg getMoveMsg(Direction direction){
        return new MoveMsg(direction, networkId);
    }

    public InitGameMsg getInitGameMsg(int playerCount, int gridX, int gridY, ClientGameState gameState, Set<Vector2f> food, HashMap<UUID, Pair<List<Vector2f>, Direction>> snakes){
        Set<Vector2f> foodCopy = new HashSet<>(food);

        return new InitGameMsg(playerCount, gridX, gridY, gameState,foodCopy, snakes, networkId);
    }

    public RegisterMsg getRegisterMsg(){
        return new RegisterMsg(networkId);
    }

    public ExitGameMsg getExitGameMsg(){
        return new ExitGameMsg(networkId);
    }

    public EnterGameMsg getEnterGameMsg(){
        return new EnterGameMsg(networkId);
    }

}

