package common.game.model;

import java.io.Serializable;

public class RGBColor implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double r;
    private final double g;
    private final double b;

    public RGBColor(double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public double getR() {
        return r;
    }

    public double getG() {
        return g;
    }

    public double getB() {
        return b;
    }
}
