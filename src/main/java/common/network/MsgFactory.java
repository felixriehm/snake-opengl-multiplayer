package common.network;

import common.game.ClientGameState;
import common.game.Direction;
import org.joml.Vector2f;

import java.util.*;

public class MsgFactory {
    private UUID networkId;

    public MsgFactory () {}

    public void init (UUID networkId) {
        this.networkId = networkId;
    }

    public GameUpdateMsg getGameEntitiesMsg(Set<Vector2f> food, List<List<Vector2f>> snakes, int gridX, int gridY){
        Set<Vector2f> foodCopy = new HashSet<>(food);
        List<List<Vector2f>> snakesCopy = new LinkedList();
        for (int i = 0; i < snakes.size(); i++) {
            snakesCopy.add(new LinkedList<>(snakes.get(i)));
        }

        return new GameUpdateMsg(foodCopy, snakesCopy, networkId, gridX, gridY);
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

    public InitGameMsg getInitGameMsg(int playerCount, int gridX, int gridY, ClientGameState gameState, Set<Vector2f> food, LinkedList<LinkedList<Vector2f>> snakes){
        Set<Vector2f> foodCopy = new HashSet<>(food);
        List<List<Vector2f>> snakesCopy = new LinkedList();
        for (int i = 0; i < snakes.size(); i++) {
            snakesCopy.add(new LinkedList<>(snakes.get(i)));
        }

        return new InitGameMsg(playerCount, gridX, gridY, gameState,foodCopy,snakesCopy, networkId);
    }

    public RegisterMsg getRegisterMsg(){
        return new RegisterMsg(networkId);
    }

    public LogoutMsg getLogoutMsg(){
        return new LogoutMsg(networkId);
    }

}

