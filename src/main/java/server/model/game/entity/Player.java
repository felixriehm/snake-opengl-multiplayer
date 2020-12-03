package server.model.game.entity;

import common.game.model.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Random;
import org.joml.Vector2f;

import java.util.*;

public class Player {
    private static final Logger logger = LogManager.getLogger(Player.class.getName());

    Vector2f lastSegmentOfLastMove;
    LinkedList<Vector2f> body = new LinkedList();
    Set<Vector2f> discoveredRuins = new HashSet<>();
    Direction nextDirection;
    Direction lastDirection;

    public Player(Set<Vector2f> freeCells) {
        this.nextDirection = Direction.NONE;

        int size = freeCells.size();
        if(size == 0){
            throw new RuntimeException("freeCells cant be empty when spawning new players");
        }
        int item = new Random().nextInt(size);
        int i = 0;
        for(Vector2f cell : freeCells)
        {
            if (i == item){
                this.body.addFirst(cell);
                // spawn safe zone around the player
                for (float x = cell.x - 1; x <= cell.x + 1; x++) {
                    for (float y = cell.y - 1; y <= cell.y + 1; y++) {
                        freeCells.remove(new Vector2f(x,y));
                    }
                }
                return;
            }
            i++;
        }
    }

    public void setNextDirection(Direction d){
        this.nextDirection = d;
    }

    public void moveSnake(){
        synchronized (this) {
            if (!this.body.isEmpty()) {
                if (body.size() > 1) {
                    if (this.nextDirection == this.lastDirection.opposite()) {
                        this.nextDirection = this.nextDirection.opposite();
                    }
                }
                this.lastDirection = this.nextDirection;
                Vector2f lastSegment = this.body.getLast();
                this.lastSegmentOfLastMove = new Vector2f(lastSegment.x, lastSegment.y);

                Vector2f firstSegment = this.body.getFirst();
                Vector2f newHead = new Vector2f(firstSegment.x, firstSegment.y);
                switch (this.nextDirection) {
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
        }
    }

    public List<Vector2f> getSnakeBody(){
        return this.body;
    }

    public void setSnakeBody(LinkedList<Vector2f> body){
        this.body = body;
    }

    public Direction getLastDirection(){
        return this.lastDirection;
    }

    public Vector2f getSnakeHead(){
        if(body.isEmpty()) {
            return null;
        }
        return this.body.getFirst();
    }

    public void setLastSegmentOfLastMove(Vector2f cell){
        this.lastSegmentOfLastMove = cell;
    }

    public Vector2f getLastSegmentOfLastMove(){
        return this.lastSegmentOfLastMove;
    }

    public void grow(){
        synchronized (this) {
            // ensure grow cheat works
            if(this.lastSegmentOfLastMove == null || this.lastSegmentOfLastMove.equals(body.getLast())){
                Vector2f newCell = new Vector2f(body.getLast().x - 1, body.getLast().y);
                this.body.add(newCell);
                this.lastSegmentOfLastMove = newCell;
                return;
            }

            // normal behaviour
            this.body.add(this.lastSegmentOfLastMove);
        }
    }

    public void discoverRuin(Vector2f ruin) {
        this.discoveredRuins.add(ruin);
    }

    public Set<Vector2f> getDiscoveredRuins() {
        return discoveredRuins;
    }

    @Override
    public String toString(){
        return this.getSnakeBody().toString();
    }
}
