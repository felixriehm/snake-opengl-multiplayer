package manager;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import shader.Shader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import static util.IOUtils.*;

import static org.lwjgl.opengl.GL32.glDeleteProgram;

public final class ResourceManager {
    private Map<String, Shader> shaders = new HashMap<String, Shader>();
    private static ResourceManager instance;

    private ResourceManager () {}

    public static ResourceManager getInstance () {
        if (ResourceManager.instance == null) {
            ResourceManager.instance = new ResourceManager ();
        }
        return ResourceManager.instance;
    }

    public Shader loadShader(String vShaderFile, String fShaderFile, String gShaderFile, String name) throws IOException {
        shaders.put(name, loadShaderFromFile(vShaderFile, fShaderFile, gShaderFile));
        return shaders.get(name);
    }

    public Shader getShader(String name){
        return shaders.get(name);
    }

    public void clear(){
        // (properly) delete all shaders
        shaders.forEach((name, shader) -> glDeleteProgram(shader.getId()));
    }

    private Shader loadShaderFromFile(String vShaderFile, String fShaderFile, String gShaderFile) throws IOException {
        // 1. retrieve the vertex/fragment source code from filePath
        ByteBuffer vertexCode = ioResourceToByteBuffer(vShaderFile, 8192);
        ByteBuffer fragmentCode = ioResourceToByteBuffer(fShaderFile, 8192);
        ByteBuffer geometryCode = null;
        if(gShaderFile != null) {
            geometryCode = ioResourceToByteBuffer(gShaderFile, 8192);

        }

        PointerBuffer vsStrings = BufferUtils.createPointerBuffer(1);
        IntBuffer vsLengths = BufferUtils.createIntBuffer(1);
        PointerBuffer fsStrings = BufferUtils.createPointerBuffer(1);
        IntBuffer fsLengths = BufferUtils.createIntBuffer(1);
        PointerBuffer gsStrings = null;
        IntBuffer gsLengths = null;
        if(gShaderFile != null) {
            gsStrings = BufferUtils.createPointerBuffer(1);
            gsLengths = BufferUtils.createIntBuffer(1);
        }

        vsStrings.put(0, vertexCode);
        vsLengths.put(0, vertexCode.remaining());
        fsStrings.put(0, fragmentCode);
        fsLengths.put(0, fragmentCode.remaining());
        if(gShaderFile != null) {
            gsStrings.put(0, geometryCode);
            gsLengths.put(0, geometryCode.remaining());
        }

        // 2. now create shader object from source code
        Shader shader = new Shader();
        shader.compile(vsStrings, vsLengths, fsStrings, fsLengths, gsStrings, gsLengths);
        return shader;
    }
}
