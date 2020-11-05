package entity;

import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.IPrimitiveRenderer;

import java.util.LinkedList;
import java.util.List;

import static snake.Game.CELL_SIZE;
import static snake.Game.Direction;

public class Player extends GameObject {
    Vector2f sizeOfBodySegments, lastSegmentOfLastMove;
    LinkedList<Vector2f> body = new LinkedList();
    Direction nextDirection;
    Direction lastDirection;

    public Player(Vector2f startPosition, Vector2f sizeOfBodySegments, Vector3f color, Direction startDirection, IPrimitiveRenderer renderer) {
        super(color,renderer);
        this.sizeOfBodySegments = sizeOfBodySegments;
        this.body.addFirst(startPosition);
        this.nextDirection = startDirection;
    }

    @Override
    public void draw() {
        this.body.forEach(segment -> super.renderer.draw(
                new Vector2f(segment.x * CELL_SIZE, segment.y * CELL_SIZE),
                this.sizeOfBodySegments,
                0f,
                super.color
        ));
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
