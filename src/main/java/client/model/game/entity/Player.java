package client.model.game.entity;

import client.view.renderer.IPrimitiveRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.LinkedList;

public class Player implements GameObject{
    LinkedList<Vector2f> body;

    public Player(LinkedList<Vector2f> body) {
        this.body = body;
    }

    public void draw(IPrimitiveRenderer renderer, int cellSize, Vector3f color, Vector2f size) {
        this.body.forEach(segment -> renderer.draw(
                new Vector2f(segment.x * cellSize, segment.y * cellSize),
                size,
                0f,
                color
        ));
    }

    public LinkedList<Vector2f> getSnakeBody() {
        return body;
    }

    public Vector2f getSnakeHead() {
        return body.getFirst();
    }
}
