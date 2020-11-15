package server.game.entity;

import common.game.Direction;
import org.joml.Vector2f;

import java.util.LinkedList;
import java.util.List;

public class Player {
    Vector2f sizeOfBodySegments, lastSegmentOfLastMove;
    LinkedList<Vector2f> body = new LinkedList();
    Direction nextDirection;
    Direction lastDirection;

    public Player(Vector2f startPosition, Direction startDirection) {
        this.sizeOfBodySegments = sizeOfBodySegments;
        this.body.addFirst(startPosition);
        this.nextDirection = startDirection;
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
