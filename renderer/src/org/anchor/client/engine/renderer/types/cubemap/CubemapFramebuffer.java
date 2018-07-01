package org.anchor.client.engine.renderer.types.cubemap;

import java.nio.ByteBuffer;

import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class CubemapFramebuffer {

    private int framebuffer, renderbuffer, cubemap;
    private int size;

    public CubemapFramebuffer(int size) {
        this(size, 0);
    }

    public CubemapFramebuffer(int size, int mipmaps) {
        this.size = size;

        framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);

        renderbuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderbuffer);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, renderbuffer);

        cubemap = GL11.glGenTextures();
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, cubemap);
        for (int i = 0; i < 6; i++)
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL30.GL_RGB16F, size, size, 0, GL11.GL_RGB, GL11.GL_FLOAT, (ByteBuffer) null);

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        if (mipmaps > 1)
            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        else
            GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
        if (mipmaps > 1)
            GL30.glGenerateMipmap(GL13.GL_TEXTURE_CUBE_MAP);

        unbindFramebuffer();
    }

    public void startMipmapRender(int mip) {
        int mipSize = (int) (size * Mathf.pow(0.5f, mip));
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderbuffer);

        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, mipSize, mipSize);
        GL11.glViewport(0, 0, mipSize, mipSize);
    }

    public void startFaceRender(int face) {
        startFaceRender(face, 0);
    }

    public void startFaceRender(int face, int mip) {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, cubemap, mip);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void bindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
    }

    public void unbindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public int getTexture() {
        return cubemap;
    }

    public float getPitch(int i) {
        if (i < 2 || i > 3)
            return 0;
        else if (i == 2)
            return 90;

        return -90;
    }

    public float getYaw(int i) {
        if (i > 1 && i < 5)
            return 180;
        else if (i == 0)
            return -90;
        else if (i == 1)
            return 90;

        return 0;
    }

}
