package common.network;

import common.game.Direction;
import common.game.GameState;
import org.joml.Vector2f;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MsgFactory {

    private static MsgFactory instance;
    private UUID networkId;

    private MsgFactory () {}

    public static MsgFactory getInstance () {
        if (MsgFactory.instance == null) {
            MsgFactory.instance = new MsgFactory ();
        }
        return MsgFactory.instance;
    }

    public void init (UUID networkId) {
        this.networkId = networkId;
    }

    public GameEntitiesMsg getGameEntitiesMsg(Set<Vector2f> food, List<List<Vector2f>> snakes){
        return new GameEntitiesMsg(food, snakes, networkId);
    }

    public GameStateMsg getGameStateMsg(GameState gameState){
        return new GameStateMsg(gameState, networkId);
    }

    public GameSizeMsg getGameSizeMsg(int gridSize){
        return new GameSizeMsg(gridSize, networkId);
    }

    public JoinGameMsg getJoinGameMsg(){
        return new JoinGameMsg(networkId);
    }

    public RequestStartGameMsg getRequestStartGameMsg(){
        return new RequestStartGameMsg(networkId);
    }

    public MoveMsg getMoveMsg(Direction direction){
        return new MoveMsg(direction, networkId);
    }

    public InitGameMsg getInitGameMsg(int playerCount, int gridSize, GameState gameState, Set<Vector2f> food, LinkedList<LinkedList<Vector2f>> snakes){
        return new InitGameMsg(playerCount, gridSize,gameState,food,snakes, networkId);
    }

    public RegisterMsg getRegisterMsg(){
        return new RegisterMsg(networkId);
    }

    public LogoutMsg getLogoutMsg(){
        return new LogoutMsg(networkId);
    }

}

