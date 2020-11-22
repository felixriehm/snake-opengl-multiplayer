package client.view.renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import client.view.shader.Shader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class CircleRenderer extends PrimitiveRenderer implements IPrimitiveRenderer {

    public CircleRenderer(Shader shader){
        super(shader);
        this.initRenderData();
    }

    public void deleteCircleRenderer(){
        glDeleteVertexArrays(this.VAO);
    }

    @Override
    public void draw(Vector2f position, Vector2f size, float rotate, Vector3f color) {
        super.draw(position, size, rotate, color);

        glBindVertexArray(this.VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 21);
        glBindVertexArray(0);
    }

    private void initRenderData(){
        int VBO;

        ByteBuffer bb = BufferUtils.createByteBuffer(4 * 2 * 42);
        FloatBuffer fv = bb.asFloatBuffer();

        double twicePi = 2.0 * 3.142;
        double radius = 0.30;
        int x = 0, y = 0;

        fv.put(x).put(y);
        for (int i = 2; i < 42; i = i + 2) {
            fv.put((float) (x + (radius * cos(i * twicePi / 20)))).put((float)(y + (radius * sin(i * twicePi / 20))));
        }

        this.VAO = glGenVertexArrays();
        VBO = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, bb, GL_STATIC_DRAW);

        glBindVertexArray(this.VAO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
}
