package client.view.renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import client.view.shader.Shader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class TriangleRenderer extends PrimitiveRenderer implements IPrimitiveRenderer {

    public TriangleRenderer(Shader shader){
        super(shader);
        this.initRenderData();
    }

    public void deleteTriangleRenderer(){
        glDeleteVertexArrays(this.VAO);
    }

    @Override
    public void draw(Vector2f position, Vector2f size, float rotate, Vector3f color) {
        super.draw(position, size, rotate, color);

        glBindVertexArray(this.VAO);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);
    }

    private void initRenderData(){
        int VBO;

        ByteBuffer bb = BufferUtils.createByteBuffer(4 * 2 * 3);
        FloatBuffer fv = bb.asFloatBuffer();
        fv.put(0.0f).put(1.0f);
        fv.put(1.0f).put(0.0f);
        fv.put(0.0f).put(0.0f);

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
