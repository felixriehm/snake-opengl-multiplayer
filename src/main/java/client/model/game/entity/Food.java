package client.model.game.entity;

import client.view.renderer.IPrimitiveRenderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class Food implements GameObject{
    private Set<Vector2f> food = new HashSet<>();

    public Food() {
    }

    public void draw(IPrimitiveRenderer renderer, int cellSize, Vector3f color, Vector2f size) {
        this.food.forEach(food -> renderer.draw(
                new Vector2f(food.x * cellSize  + cellSize/2, food.y * cellSize + cellSize / 2 ),
                size,
                0f,
                color
        ));
    }

    public void setFood(Set<Vector2f> food) {
        this.food = food;
    }

    public Set<Vector2f> getFood() {
        return this.food;
    }
}
