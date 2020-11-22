package client.view.renderer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import client.view.shader.Shader;

public class PrimitiveRenderer {
    protected Shader shader;
    protected int VAO;

    protected PrimitiveRenderer(Shader shader){
        this.shader = shader;
    }

    protected void draw(Vector2f position, Vector2f size, float rotate, Vector3f color) {
        this.shader.use();

        Matrix4f model = new Matrix4f()
                .translate(new Vector3f(position, 0.0f))
                .translate(new Vector3f(0.5f * size.x, 0.5f * size.y, 0.0f))
                .rotate((float) Math.toRadians(rotate), new Vector3f(0.0f, 0.0f, 1.0f))
                .translate(new Vector3f(-0.5f * size.x, -0.5f * size.y, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        this.shader.setMatrix4("model", model, false);
        this.shader.setVector3f("primitiveColor", color, false);
    }
}
