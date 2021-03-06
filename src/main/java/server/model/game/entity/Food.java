package server.model.game.entity;

import org.joml.Random;
import org.joml.Vector2f;

import java.util.HashSet;
import java.util.Set;

public class Food {
    private Set<Vector2f> food = new HashSet<>();
    private Set<Vector2f> availableGridCells;

    public Food(Set<Vector2f> availableGridCells) {
        this.availableGridCells = availableGridCells;
    }

    public void spawnFood(Set<Player> players){
        Set<Vector2f> freeCells = new HashSet<Vector2f>(availableGridCells);
        players.forEach(player -> freeCells.removeAll(player.getSnakeBody()));
        freeCells.removeAll(food);

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

    public void removeFood(Set<Vector2f> foodToRemove){
        this.food.removeAll(foodToRemove);
    }

    public Set<Vector2f> getFood(){
        return this.food;
    }

    public void setAvailableGridCells(Set<Vector2f> availableGridCells) {
        this.availableGridCells = availableGridCells;
    }

    public void clearFood() {
        this.food.clear();
    }

    public void setFood(Set<Vector2f> food){
        this.food = food;
    }

    public void removeFood(Vector2f food) {
        this.food.remove(food);
    }
}
