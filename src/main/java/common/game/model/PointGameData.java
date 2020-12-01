package common.game.model;

import java.io.Serializable;

public class PointGameData implements Serializable {
    private static final long serialVersionUID = 1L;

    private float x;
    private float y;

    public PointGameData(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
