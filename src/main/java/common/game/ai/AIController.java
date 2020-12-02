package common.game.ai;

import common.game.model.Direction;
import org.joml.Vector2f;

import java.util.*;

public class AIController {

    public Direction getNextMove(Set<Vector2f> food, Set<Vector2f> walls, Set<Vector2f> enemies,
                                 Set<Vector2f> playerBody, Vector2f playerHead, Direction lastPlayerDirection,
                                 int gridX, int gridY, int worldEventCountdown){
        Direction nextDirection = Direction.LEFT;

        // find nearest food and move towards it
        if(!food.isEmpty()){
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

        Set<Direction> notValid = new HashSet<>();
        Set<Direction> possibleMoves = new HashSet<>();
        possibleMoves.add(Direction.LEFT);
        possibleMoves.add(Direction.RIGHT);
        possibleMoves.add(Direction.DOWN);
        possibleMoves.add(Direction.LEFT);
        while(walls.contains(nextDirection.nextPosition(playerHead)) || nextDirection == lastPlayerDirection.opposite() ||
                enemies.contains(nextDirection.nextPosition(playerHead)) || playerBody.contains(nextDirection.nextPosition(playerHead)) ){
            notValid.add(nextDirection);
            possibleMoves.removeAll(notValid);
            if(possibleMoves.isEmpty()) {
                // just walk somewhere and die
                nextDirection = (Direction) nextDirection.possibleMoves()
                        .toArray()[new Random().nextInt(nextDirection.possibleMoves().size())];
                break;
            }
            nextDirection = (Direction) possibleMoves
                    .toArray()[new Random().nextInt(possibleMoves.size())];
        }

        return nextDirection;
    }
}
