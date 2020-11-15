package server.game.entity;

import org.joml.Random;
import org.joml.Vector2f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Food {
    private Set<Vector2f> food = new HashSet<>();
    private Set<Vector2f> availableGridCells;

    public Food(Set<Vector2f> availableGridCells) {
        this.availableGridCells = availableGridCells;
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