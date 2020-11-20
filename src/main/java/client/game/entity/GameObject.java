package client.game.entity;

import client.game.Game;
import org.joml.Vector3f;
import client.game.renderer.IPrimitiveRenderer;

public abstract class GameObject {
    protected Vector3f color;
    protected IPrimitiveRenderer renderer;
    protected Game game;

    protected GameObject(Vector3f color, IPrimitiveRenderer renderer, Game game) {
        this.color = color;
        this.renderer = renderer;
        this.game = game;
    }

    abstract public void draw();
}
