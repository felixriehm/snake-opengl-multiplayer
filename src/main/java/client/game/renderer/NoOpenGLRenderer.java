package client.game.renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class NoOpenGLRenderer implements IPrimitiveRenderer{
    @Override
    public void draw(Vector2f position, Vector2f size, float rotate, Vector3f color) {
        // do nothing
    }
}
