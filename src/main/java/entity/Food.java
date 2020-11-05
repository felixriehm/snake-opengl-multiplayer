package entity;

import snake.Game;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.IPrimitiveRenderer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Food extends GameObject {
    private Set<Vector2f> food = new HashSet<>();
    private Set<Vector2f> availableGridCells;
    private Vector2f  size;

    public Food(Vector2f size, Vector3f color, IPrimitiveRenderer renderer, Set<Vector2f> availableGridCells) {
        super(color,renderer);
        this.size = size;
        this.availableGridCells = availableGridCells;
    }

    @Override
    public void draw() {
        this.food.forEach(food -> super.renderer.draw(
                new Vector2f(food.x * Game.CELL_SIZE  + Game.CELL_SIZE/2, food.y * Game.CELL_SIZE + Game.CELL_SIZE / 2 ),
                this.size,
                0f,
                super.color
        ));
    }

    public void spawnFood(List<Vector2f> snakeBodyList){
        Set<Vector2f> freeCells = new HashSet<Vector2f>(availableGridCells);
        Set<Vector2f> snakeBodySet = snakeBodyList.stream().collect(Collectors.toSet());
        freeCells.removeAll(snakeBodySet);

        int size = freeCells.size();
        int item = new Random().nextInt(size);
        int i = 0;
        for(Vector2f cell : freeCells)
        {
            if (i == item){
                this.food.add(cell);
                return;
            }
            i++;
        }
    }

    public void removeFood(Vector2f foodToRemove){
        this.food.remove(foodToRemove);
    }

    public Set<Vector2f> getFood(){
        return this.food;
    }
}
