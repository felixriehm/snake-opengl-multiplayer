package client.game.entity;

import client.game.Game;
import org.joml.Vector2f;
import org.joml.Vector3f;
import client.game.renderer.IPrimitiveRenderer;

import java.util.HashSet;
import java.util.Set;

public class Food extends GameObject {
    private Set<Vector2f> food = new HashSet<>();
    private Vector2f  size;

    public Food(Vector2f size, Vector3f color, IPrimitiveRenderer renderer) {
        super(color,renderer);
        this.size = size;
    }

    @Override
    public void draw() {
        int cellSize = Game.getInstance().getCellSize();
        this.food.forEach(food -> super.renderer.draw(
                new Vector2f(food.x * cellSize  + cellSize/2, food.y * cellSize + cellSize / 2 ),
                this.size,
                0f,
                super.color
        ));
    }

    public void setFood(Set<Vector2f> food) {
        this.food = food;
    }
}
