package common.game.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT,
    NONE;

    public Direction opposite() {
        switch(this) {
            case UP: return Direction.DOWN;
            case RIGHT: return Direction.LEFT;
            case DOWN: return Direction.UP;
            case LEFT: return Direction.RIGHT;
            default: return Direction.NONE;
        }
    }

    public Set<Direction> possibleMoves() {
        switch(this) {
            case UP: return new HashSet<>(Arrays.asList(DOWN, RIGHT, LEFT));
            case RIGHT: return new HashSet<>(Arrays.asList(DOWN, UP, LEFT));
            case DOWN: return new HashSet<>(Arrays.asList(UP, RIGHT, LEFT));
            case LEFT: return new HashSet<>(Arrays.asList(DOWN, RIGHT, UP));
            default: return new HashSet<>(Arrays.asList(DOWN, RIGHT, LEFT, UP));
        }
    }
}
