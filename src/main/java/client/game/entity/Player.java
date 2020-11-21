package client.game.entity;

import client.game.Game;
import org.joml.Vector2f;
import org.joml.Vector3f;
import client.game.renderer.IPrimitiveRenderer;

import java.util.LinkedList;
import java.util.Set;

public class Player extends GameObject {
    Vector2f sizeOfBodySegments;
    LinkedList<Vector2f> body;

    public Player(Vector2f sizeOfBodySegments, Vector3f color, IPrimitiveRenderer renderer, Game game, LinkedList<Vector2f> body) {
        super(color,renderer, game);
        this.sizeOfBodySegments = sizeOfBodySegments;
        this.body = body;
    }

    @Override
    public void draw() {
        int cellSize = super.game.getCellSize();
        this.body.forEach(segment -> super.renderer.draw(
                new Vector2f(segment.x * cellSize, segment.y * cellSize),
                this.sizeOfBodySegments,
                0f,
                super.color
        ));
    }

    public LinkedList<Vector2f> getSnakeBody() {
        return body;
    }

    public Vector2f getSnakeHead() {
        return body.getFirst();
    }
}
