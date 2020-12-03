package common.network;

import common.game.model.CheatCode;
import common.game.model.ClientGameState;
import common.game.model.Direction;
import common.game.model.PointGameData;

import java.util.*;

public class MsgFactory {
    private UUID networkId;

    public MsgFactory () {}

    public void init (UUID networkId) {
        this.networkId = networkId;
    }

    public GameUpdateMsg getGameEntitiesMsg(Set<PointGameData> gameData, Direction direction, int gridX, int gridY,
                                            int worldEventCountdown){

        return new GameUpdateMsg(gameData, direction, networkId, gridX, gridY, worldEventCountdown);
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

    public InitGameMsg getInitGameMsg(int playerCount, int gridX, int gridY, ClientGameState gameState,
                                      Set<PointGameData> gameData, Direction direction, int worldEventCountdown){

        return new InitGameMsg(playerCount, gridX, gridY, gameState, gameData, direction, networkId, worldEventCountdown);
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

    public CheatCodeMsg getCheatGrowMsg(CheatCode cheatCode){
        return new CheatCodeMsg(networkId, cheatCode);
    }
}

