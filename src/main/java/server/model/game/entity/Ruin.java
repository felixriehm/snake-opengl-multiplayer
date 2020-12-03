package server.model.game.entity;

import common.Configuration;
import org.joml.Vector2f;
import server.util.DiamondSquare;

import java.util.HashSet;
import java.util.Set;

public class Ruin {
    public static final long WORLD_GENERATION_SEED = Long.parseLong(Configuration.getInstance().getProperty("game.world-generation.seed"));
    private Set<Vector2f> generatedRuins = new HashSet<>();

    public void initRuins(int gridSize){
        generatedRuins = new HashSet<>();

        DiamondSquare diamondSquare = new DiamondSquare(WORLD_GENERATION_SEED,
                (int)(Math.log(gridSize - 1) / Math.log(2)));

        float[][] generatedWorld = diamondSquare.getData();

        for(int i=0; i<diamondSquare.getWidth(); i++) {
            for(int j=0; j<diamondSquare.getHeight(); j++) {
                if(generatedWorld[i][j] < 0.2){
                    generatedRuins.add(new Vector2f(i,j));
                }
            }
        }
    }

    public Set<Vector2f> getGeneratedRuins() {
        return generatedRuins;
    }
}
