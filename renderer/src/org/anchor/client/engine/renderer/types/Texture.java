package org.anchor.client.engine.renderer.types;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

public class Texture {

    private int width, height;
    private IntBuffer buffer;
    private int texture;

    public Texture(String path) {
        texture = loadTexture(loadImage(path));
    }

    public Texture(InputStream in) {
        texture = loadTexture(loadImage(in));
    }

    public Texture(BufferedImage image) {
        texture = loadTexture(loadImage(image));
    }

    public Texture(File file) {
        texture = loadTexture(loadImage(file));
    }

    private int[] loadImage(String path) {
        try {
            return loadImage(ImageIO.read(new FileInputStream(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int[] loadImage(File file) {
        try {
            return loadImage(ImageIO.read(new FileInputStream(file)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int[] loadImage(InputStream in) {
        try {
            return loadImage(ImageIO.read(in));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int[] loadImage(BufferedImage image) {
        int[] pixels = null;
        width = image.getWidth();
        height = image.getHeight();

        pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        return pixels;
    }

    private int loadTexture(int[] pixels) {
        int[] data = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int a = (pixels[i] & 0xff000000) >> 24;
            int r = (pixels[i] & 0xff0000) >> 16;
            int g = (pixels[i] & 0xff00) >> 8;
            int b = (pixels[i] & 0xff);

            data[i] = a << 24 | b << 16 | g << 8 | r;
        }

        int result = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, result);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        buffer = ByteBuffer.allocateDirect(data.length << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.put(data).flip();

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return result;
    }

    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public IntBuffer getBuffer() {
        return buffer;
    }

    public int getTextureId() {
        return texture;
    }

}
