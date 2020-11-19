package server.game.entity;

import common.game.Direction;
import common.game.GameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Random;
import org.joml.Vector2f;
import server.Server;

import java.util.*;
import java.util.stream.Collectors;

public class Player {
    private static final Logger logger = LogManager.getLogger(Server.class.getName());

    Vector2f lastSegmentOfLastMove;
    LinkedList<Vector2f> body = new LinkedList();
    Direction nextDirection;
    Direction lastDirection;

    public Player(Set<Vector2f> availableGridCells, int gridX, int gridY) {
        this.nextDirection = Direction.values()[new Random().nextInt(Direction.values().length)];

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

        int size = freeCells.size();
        int item = new Random().nextInt(size);
        int i = 0;
        for(Vector2f cell : freeCells)
        {
            if (i == item){
                this.body.addFirst(cell);
                return;
            }
            i++;
        }
    }

    public void setNextDirection(Direction d){
        this.nextDirection = d;
    }

    public void moveSnake(){
        this.lastDirection = this.nextDirection;
        Vector2f lastSegment = this.body.getLast();
        this.lastSegmentOfLastMove = new Vector2f(lastSegment.x, lastSegment.y);

        Vector2f firstSegment = this.body.getFirst();
        Vector2f newHead = new Vector2f(firstSegment.x, firstSegment.y);
        switch(this.nextDirection) {
            case UP:
                newHead.y = --newHead.y;
                break;
            case DOWN:
                newHead.y = ++newHead.y;
                break;
            case RIGHT:
                newHead.x = ++newHead.x;
                break;
            case LEFT:
                newHead.x = --newHead.x;
                break;
            default:
                break;
        }
        body.addFirst(newHead);
        body.removeLast();
    }

    public List<Vector2f> getSnakeBody(){
        return this.body;
    }

    public Direction getLastDirection(){
        return this.lastDirection;
    }

    public Vector2f getSnakeHead(){
        return this.body.getFirst();
    }

    public void grow(){
        this.body.add(this.lastSegmentOfLastMove);
    }
}
