package client.model.game.entity;

import client.view.renderer.IPrimitiveRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface GameObject {
    void draw(IPrimitiveRenderer renderer, int cellSize, Vector3f color, Vector2f size);
}
