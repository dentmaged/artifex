package org.anchor.client.engine.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.client.engine.renderer.types.texture.Texture;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.FileHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLContext;

public class Loader {

    private List<Integer> vaos = new ArrayList<Integer>();
    private List<Integer> vbos = new ArrayList<Integer>();
    private List<Integer> textures = new ArrayList<Integer>();

    private static final Loader instance = new Loader();
    public static final String RES_LOC = "res";

    private Loader() {

    }

    public int loadToVAO(float[] positions, float[] textureCoords) {
        int vaoID = createVAO();

        storeDataInAttributeList(0, 2, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        unbindVAO();

        return vaoID;
    }

    public Mesh loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices, int[] jointIds, float[] weights) {
        int vaoID = createVAO();

        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        storeDataInAttributeList(3, 3, jointIds);
        storeDataInAttributeList(4, 3, weights);
        unbindVAO();

        return new Mesh(vaoID, vbos.size() - 4, indices.length, 3);
    }

    public Mesh loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
        int vaoID = createVAO();

        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        unbindVAO();

        return new Mesh(vaoID, vbos.size() - 2, indices.length, 3);
    }

    public Mesh loadToVAO(float[] positions, float[] textureCoords, float[] normals, float[] tangents, int[] indices) {
        int vaoID = createVAO();

        bindIndicesBuffer(indices);
        storeDataInAttributeList(0, 3, positions);
        storeDataInAttributeList(1, 2, textureCoords);
        storeDataInAttributeList(2, 3, normals);
        storeDataInAttributeList(3, 3, tangents);
        unbindVAO();

        return new Mesh(vaoID, vbos.size() - 3, indices.length, 3);
    }

    public Mesh loadToVAO(float[] positions, int dimensions) {
        int vaoID = createVAO();

        storeDataInAttributeList(0, dimensions, positions);
        unbindVAO();

        return new Mesh(vaoID, vbos.size(), positions.length / dimensions, dimensions);
    }

    public void updateVbo(int vbo, int[] data, IntBuffer buffer) {
        buffer.clear();
        buffer.put(data);
        buffer.flip();

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer.capacity() * 4, GL15.GL_STREAM_DRAW);
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, buffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void updateVBO(int vbo, float[] data, FloatBuffer buffer) {
        buffer.clear();
        buffer.put(data);
        buffer.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data.length * 4, GL15.GL_DYNAMIC_DRAW);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public int createEmptyVBO(int floatCount) {
        int vbo = GL15.glGenBuffers();

        vbos.add(vbo);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatCount * 4, GL15.GL_STREAM_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        return vbo;
    }

    public void addInstancedAttribute(int vao, int vbo, int attribute, int dataSize, int instanceDataLength, int offset) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL30.glBindVertexArray(vao);
        GL20.glVertexAttribPointer(attribute, dataSize, GL11.GL_FLOAT, false, instanceDataLength * 4, offset * 4);
        GL33.glVertexAttribDivisor(attribute, 1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public int loadTexture(TextureType type, String fileName) {
        return getTexture(type.withFile(fileName)).getTextureId();
    }

    public Texture getTexture(TextureType type, String fileName) {
        return getTexture(type.withFile(fileName));
    }

    public int loadTexture(String fileName) {
        return getTexture(fileName).getTextureId();
    }

    public Texture getTexture(String fileName) {
        Texture texture = null;
        File file = FileHelper.newGameFile(RES_LOC, fileName.replace(RES_LOC + "/", "").replace(RES_LOC, "") + ".png");
        if (!file.exists()) {
            System.err.println(file.getName() + " does not exist! Falling back on default texture.");
            file = FileHelper.newGameFile(RES_LOC, "missing_texture.png");
        }

        try {
            texture = new Texture(file);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error whilst loading texture " + fileName + ".png!");
            System.exit(-1);
        }
        textures.add(texture.getTextureId());

        return texture;
    }

    public int loadColour(float red, float green, float blue) {
        return loadColour(red, green, blue, 1);
    }

    public int loadColour(int red, int green, int blue) {
        return loadColour(red, green, blue, 1);
    }

    public int loadColour(float red, float green, float blue, float alpha) {
        if (red < 0 || red > 1 || green < 0 || green > 1 || blue < 0 || blue > 1 || alpha < 0 || alpha > 1) {
            System.err.println("Colour is out of range! " + red + " " + green + " " + blue);
            return -1;
        }

        return loadColour((int) (255 * red), (int) (255 * green), (int) (255 * blue), (int) (255 * alpha));
    }

    public int loadColour(int red, int green, int blue, int alpha) {
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255 || alpha < 0 || alpha > 255) {
            System.err.println("Colour is out of range! " + red + " " + green + " " + blue);
            return -1;
        }

        Texture texture = null;
        try {
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(new Color(red, green, blue, alpha));
            graphics.fillRect(0, 0, 1, 1);

            texture = new Texture(image);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4f);

            if (GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                float amount = Math.min(4, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
            } else {
                System.err.println("Anisotropic texture filtering is not supported! Make sure you update your driver.\nIf you don't, you may recieve lower FPS and/or textures at steep angles\nwill look low quality!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Tried to create texture, didn't work");
            System.exit(-1);
        }
        textures.add(texture.getTextureId());

        return texture.getTextureId();
    }

    public void shutdown() {
        for (int vao : vaos)
            GL30.glDeleteVertexArrays(vao);

        for (int vbo : vbos)
            GL15.glDeleteBuffers(vbo);

        for (int texture : textures)
            GL11.glDeleteTextures(texture);
    }

    public int loadCubemap(String[] textureFiles) {
        int id = GL11.glGenTextures();
        textures.add(id);

        Engine.bindCubemap(id, 0);
        for (int i = 0; i < textureFiles.length; i++) {
            Texture texture = new Texture(FileHelper.newGameFile(RES_LOC, textureFiles[i] + ".png"));

            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texture.getBuffer());
        }

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
        GL30.glGenerateMipmap(GL13.GL_TEXTURE_CUBE_MAP);

        return id;
    }

    public int loadCubemap(Texture[] cubemapTextures) {
        int id = GL11.glGenTextures();
        textures.add(id);

        Engine.bindCubemap(id, 0);
        for (int i = 0; i < cubemapTextures.length; i++) {
            Texture texture = cubemapTextures[i];

            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texture.getBuffer());
        }

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
        GL30.glGenerateMipmap(GL13.GL_TEXTURE_CUBE_MAP);

        return id;
    }

    private int createVAO() {
        int vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);
        vaos.add(vaoID);

        return vaoID;
    }

    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);

        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, coordinateSize * 4, 0); // 4 bytes per float
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, int[] data) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDataInIntBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);

        GL30.glVertexAttribIPointer(attributeNumber, coordinateSize, GL11.GL_INT, coordinateSize * 4, 0); // 4 bytes per int
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void unbindVAO() {
        GL30.glBindVertexArray(0);
    }

    private void bindIndicesBuffer(int[] indices) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
    }

    public static IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();

        return buffer;
    }

    public static FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();

        return buffer;
    }

    public static ByteBuffer storeDataInByteBuffer(float[] data) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length * 12);
        for (float value : data)
            buffer.putFloat(value);
        buffer.flip();

        return buffer;
    }

    public static ByteBuffer storeDataInByteBuffer(int[] data) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length * 12);
        for (int value : data)
            buffer.putInt(value);
        buffer.flip();

        return buffer;
    }

    public List<Integer> getVBOs() {
        return vbos;
    }

    public static Loader getInstance() {
        return instance;
    }

}
