package shader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL32.*;

public class Shader {
    private int Id;

    public Shader use(){
        glUseProgram(Id);
        return this;
    }

    public int getId(){
        return this.Id;
    }

    public  void compile(PointerBuffer vertexSource, IntBuffer vertexSourceRemaining, PointerBuffer fragmentSource, IntBuffer fragmentSourceRemaining, PointerBuffer geometrySource, IntBuffer geometrySourceRemaining) {
        int sVertex, sFragment, gShader = -1;
        // vertex Shader
        sVertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(sVertex, vertexSource, vertexSourceRemaining);
        glCompileShader(sVertex);
        checkCompileErrors(sVertex, "VERTEX");
        // fragment Shader
        sFragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(sFragment, fragmentSource, fragmentSourceRemaining);
        glCompileShader(sFragment);
        checkCompileErrors(sFragment, "FRAGMENT");
        // if geometry shader source code is given, also compile geometry shader
        if (geometrySource != null)
        {
            gShader = glCreateShader(GL_GEOMETRY_SHADER);
            glShaderSource(gShader, geometrySource, geometrySourceRemaining);
            glCompileShader(gShader);
            checkCompileErrors(gShader, "GEOMETRY");
        }
        // shader program
        this.Id = glCreateProgram();
        glAttachShader(this.Id, sVertex);
        glAttachShader(this.Id, sFragment);
        if (geometrySource != null){
            glAttachShader(this.Id, gShader);
        }
        glLinkProgram(this.Id);
        checkCompileErrors(this.Id, "PROGRAM");
        // delete the shaders as they're linked into our program now and no longer necessary
        glDeleteShader(sVertex);
        glDeleteShader(sFragment);
        if (geometrySource != null)
            glDeleteShader(gShader);
    }

    public void checkCompileErrors(int object, String type){
        if(type != "PROGRAM") {
            int compiled = glGetShaderi(object, GL_COMPILE_STATUS);
            String shaderLog = glGetShaderInfoLog(object);
            if (shaderLog.trim().length() > 0) {
                System.err.println(shaderLog);
            }
            if (compiled == 0) {
                throw new AssertionError("Could not compile shader");
            }
        } else {
            int linked  = glGetProgrami(object, GL_LINK_STATUS);
            String programLog  = glGetProgramInfoLog(object);
            if (programLog.trim().length() > 0) {
                System.err.println(programLog);
            }
            if (linked == 0) {
                throw new AssertionError("Could not link program");
            }
        }

    }

    public void setFloat(String name, float value, boolean useShader)
    {
        if (useShader)
            this.use();
        glUniform1f(glGetUniformLocation(this.Id, name), value);
    }
    public void setInteger(String name, int value, boolean useShader)
    {
        if (useShader)
            this.use();
        glUniform1i(glGetUniformLocation(this.Id, name), value);
    }
    public void setVector2f(String name, float x, float y, boolean useShader)
    {
        if (useShader)
            this.use();
        glUniform2f(glGetUniformLocation(this.Id, name), x, y);
    }
    public void setVector2f(String name, Vector2f value, boolean useShader)
    {
        if (useShader)
            this.use();
        glUniform2f(glGetUniformLocation(this.Id, name), value.x, value.y);
    }
    public void setVector3f(String name, float x, float y, float z, boolean useShader)
    {
        if (useShader)
            this.use();
        glUniform3f(glGetUniformLocation(this.Id, name), x, y, z);
    }
    public void setVector3f(String name, Vector3f value, boolean useShader)
    {
        if (useShader)
            this.use();
        glUniform3f(glGetUniformLocation(this.Id, name), value.x, value.y, value.z);
    }
    public void setVector4f(String name, float x, float y, float z, float w, boolean useShader)
    {
        if (useShader)
            this.use();
        glUniform4f(glGetUniformLocation(this.Id, name), x, y, z, w);
    }
    public void setVector4f(String name, Vector4f value, boolean useShader)
    {
        if (useShader)
            this.use();
        glUniform4f(glGetUniformLocation(this.Id, name), value.x, value.y, value.z, value.w);
    }
    public void setMatrix4(String name, Matrix4f matrix, boolean useShader)
    {
        if (useShader)
            this.use();
        FloatBuffer fb = BufferUtils.createFloatBuffer(4 * 4);
        glUniformMatrix4fv(glGetUniformLocation(this.Id, name), false, matrix.get(fb));
    }
}
