package entity;

import org.joml.Vector3f;
import renderer.IPrimitiveRenderer;

public abstract class GameObject {
    protected Vector3f color;
    protected IPrimitiveRenderer renderer;

    protected GameObject(Vector3f color, IPrimitiveRenderer renderer) {
        this.color = color;
        this.renderer = renderer;
    }

    abstract public void draw();
}
