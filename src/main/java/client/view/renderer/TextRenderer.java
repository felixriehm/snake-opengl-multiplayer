package client.view.renderer;

import client.view.manager.ResourceManager;
import client.view.shader.Shader;
import com.mlomb.freetypejni.*;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL32.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

public class TextRenderer {
    HashMap<java.lang.Character, Character> characters;
    private int VAO, VBO;
    private Shader textShader;

    private class Character {
        int textureId; // ID handle of the glyph texture
        Vector2i size; // Size of glyph
        Vector2i bearing; // Offset from baseline to left/top of glyph
        int advance; // Offset to advance to next glyph

        public Character(int textureId, Vector2i size, Vector2i bearing, int advance) {
            this.textureId = textureId;
            this.size = size;
            this.bearing = bearing;
            this.advance = advance;
        }
    }

    public TextRenderer(Matrix4f projection){
        // load and configure shader
        try {
            textShader = ResourceManager.getInstance().loadShader("src/main/java/client/view/shader/text-2d-vs.glsl",
                "src/main/java/client/view/shader/text-2d-fs.glsl", null, "text");
        } catch (IOException e) {
            e.printStackTrace();
        }
        textShader.use();
        textShader.setMatrix4("projection", projection, true);
        textShader.setInteger("text", 0, true);
        // configure VAO/VBO for texture quads
        this.VAO = glGenVertexArrays();
        this.VBO = glGenBuffers();
        glBindVertexArray(this.VAO);
        glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
        glBufferData(GL_ARRAY_BUFFER, 4 * 6 * 4, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * 4, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void load(String fontPath, int fontSize) {
        characters = new HashMap<>();

        Library library = FreeType.newLibrary();
        if (library == null) {
            throw new RuntimeException("Error initializing FreeType.");
        }

        Face face = library.newFace(fontPath, 0);
        if (face == null){
            throw new RuntimeException("Error creating face from file '" + fontPath + "'.");
        }

        face.setPixelSizes(0, 24);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1); // disable byte-alignment restriction

        for (char c = 0; c < 128; c++)
        {
            // load character glyph
            face.loadChar(c, FreeTypeConstants.FT_LOAD_RENDER);
            // generate texture
            int texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RED,
                    face.getGlyphSlot().getBitmap().getWidth(),
                    face.getGlyphSlot().getBitmap().getRows(),
                    0,
                    GL_RED,
                    GL_UNSIGNED_BYTE,
                    face.getGlyphSlot().getBitmap().getBuffer() // maybe faulty
            );
            // set texture options
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            // now store character for later use
            characters.put(c, new Character(texture,
                    new Vector2i(face.getGlyphSlot().getBitmap().getWidth(), face.getGlyphSlot().getBitmap().getRows()),
                    new Vector2i(face.getGlyphSlot().getBitmapLeft(), face.getGlyphSlot().getBitmapTop()),
                    face.getGlyphSlot().getAdvance().getX()
                    ));
        }
        glBindTexture(GL_TEXTURE_2D, 0);

        // Delete face
        if (face.delete()) {
            throw new RuntimeException("Error deleting face from file '" + fontPath + "'.");
        }

        // Destroy FreeType
        if (library.delete()) {
            throw new RuntimeException("Error deleting FreeType.");
        }
    }

    public void renderText(String text, float x, float y, float scale, Vector3f color)
    {
        // activate corresponding render state
        textShader.use();
        textShader.setVector3f("textColor", color, true);
        glActiveTexture(GL_TEXTURE0);
        glBindVertexArray(this.VAO);

        // iterate through all characters
        for (int i = 0; i < text.length(); i++){
            Character ch = this.characters.get(text.charAt(i));

            float xpos = x + ch.bearing.x * scale;
            float ypos = y + (this.characters.get('H').bearing.y - ch.bearing.y) * scale;

            float w = ch.size.x * scale;
            float h = ch.size.y * scale;
            // update VBO for each character
            FloatBuffer fb = BufferUtils.createFloatBuffer(6 * 4);
            fb.put(xpos).put(ypos + h).put(0.0f).put(1.0f);
            fb.put(xpos + w).put(ypos).put(1.0f).put(0.0f);
            fb.put(xpos).put(ypos).put(0.0f).put(0.0f);
            fb.put(xpos).put(ypos + h).put(0.0f).put(1.0f);
            fb.put(xpos + w).put(ypos + h).put(1.0f).put(1.0f);
            fb.put(xpos + w).put(ypos).put(1.0f).put(0.0f);

            // render glyph texture over quad
            glBindTexture(GL_TEXTURE_2D, ch.textureId);
            // update content of VBO memory
            glBindBuffer(GL_ARRAY_BUFFER, this.VBO);
            glBufferSubData(GL_ARRAY_BUFFER, 0, fb);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            // render quad
            glDrawArrays(GL_TRIANGLES, 0, 6);
            // now advance cursors for next glyph
            x += (ch.advance >> 6) * scale; // bitshift by 6 to get value in pixels (1/64th times 2^6 = 64)
        }
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}