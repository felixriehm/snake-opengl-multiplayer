package common.network;

import org.joml.Vector2f;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameEntitiesMsg extends BaseMsg{
    private static final long serialVersionUID = 1L;

    private final Set<Vector2f> food;
    private final List<List<Vector2f>> snakes;

    public GameEntitiesMsg(Set<Vector2f> food, List<List<Vector2f>> snakes, UUID sender) {
        super(sender);
        this.food = food;
        this.snakes = snakes;
    }

    public Set<Vector2f> getFood() {
        return food;
    }

    public List<List<Vector2f>> getSnakes() {
        return snakes;
    }
}
