package common.game.ai;

import common.game.Direction;
import org.joml.Vector2f;

import java.util.*;

public class AIController {

    public Direction getNextMove(Set<Vector2f> food, Set<LinkedList<Vector2f>> enemies,
                                 LinkedList<Vector2f> player, Direction lastPlayerDirection,
                                 int gridX, int gridY, int worldEventCountdown){
        Direction nextDirection = Direction.LEFT;

        // find nearest food and move towards it
        if(!food.isEmpty()){
            Vector2f playerHead = player.getFirst();
            Vector2f nearestFood = null;

            float shortestDistance = -1f;
            for(Vector2f foodCell : food){
                float manhattanDistance = Math.abs(playerHead.x - foodCell.x) + Math.abs(playerHead.y - foodCell.y);
                if(shortestDistance == -1f) {
                    shortestDistance = manhattanDistance;
                    nearestFood = foodCell;
                    continue;
                }

                if(manhattanDistance < shortestDistance) {
                    shortestDistance = manhattanDistance;
                    nearestFood = foodCell;
                }
            }

            if(playerHead.x - nearestFood.x == 0) {
                if(playerHead.y - nearestFood.y > 0){
                    nextDirection = Direction.UP;
                } else {
                    nextDirection = Direction.DOWN;
                }
            } else {
                if(playerHead.x - nearestFood.x > 0){
                    nextDirection = Direction.LEFT;
                } else {
                    nextDirection = Direction.RIGHT;
                }
            }
        }

        if(nextDirection == lastPlayerDirection.opposite()){
            nextDirection = (Direction) nextDirection.possibleMoves()
                    .toArray()[new Random().nextInt(nextDirection.possibleMoves().size())];
        }

        return nextDirection;
    }
}
