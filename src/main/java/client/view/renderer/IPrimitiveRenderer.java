package client.view.renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;

public interface IPrimitiveRenderer {
    void draw(Vector2f position, Vector2f size, float rotate, Vector3f color);
}
