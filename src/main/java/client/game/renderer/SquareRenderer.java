package client.game.renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import client.game.shader.Shader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL32.*;

public class SquareRenderer extends PrimitiveRenderer implements IPrimitiveRenderer {

    public SquareRenderer(Shader shader){
        super(shader);
        this.initRenderData();
    }

    public void deleteSquareRenderer(){
        glDeleteVertexArrays(this.VAO);
    }

    @Override
    public void draw(Vector2f position, Vector2f size, float rotate, Vector3f color) {
        super.draw(position, size, rotate, color);

        glBindVertexArray(this.VAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

    private void initRenderData(){
        int VBO;

        ByteBuffer bb = BufferUtils.createByteBuffer(4 * 2 * 6);
        FloatBuffer fv = bb.asFloatBuffer();
        fv.put(0.0f).put(1.0f);
        fv.put(1.0f).put(0.0f);
        fv.put(0.0f).put(0.0f);
        fv.put(0.0f).put(1.0f);
        fv.put(1.0f).put(1.0f);
        fv.put(1.0f).put(0.0f);

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
