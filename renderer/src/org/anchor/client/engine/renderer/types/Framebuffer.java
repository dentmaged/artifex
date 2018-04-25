package org.anchor.client.engine.renderer.types;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Framebuffer {

    public static final int NONE = 0;
    public static final int DEPTH_TEXTURE = 1;
    public static final int DEPTH_RENDER_BUFFER = 2;

    public static final int COLOUR_BUFFERS = 5;

    private final int width;
    private final int height;

    private int id;
    private boolean multiTarget = false;

    private int colourTexture;
    private int depthTexture;

    private int depthBuffer;
    private int[] colourBuffers = new int[COLOUR_BUFFERS];

    public Framebuffer(int width, int height, int depthBufferType) {
        this.width = width;
        this.height = height;
        initialiseFrameBuffer(depthBufferType);
    }

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.multiTarget = true;
        initialiseFrameBuffer(DEPTH_RENDER_BUFFER);
    }

    public void shutdown() {
        GL30.glDeleteFramebuffers(id);

        GL11.glDeleteTextures(colourTexture);
        GL11.glDeleteTextures(depthTexture);
        GL30.glDeleteRenderbuffers(depthBuffer);

        for (int i = 0; i < colourBuffers.length; i++)
            GL30.glDeleteRenderbuffers(colourBuffers[i]);
    }

    public void bindFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, id);
        GL11.glViewport(0, 0, width, height);
    }

    public void unbindFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void bindToRead() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, id);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
    }

    public int getColourTexture() {
        return colourTexture;
    }

    public int getDepthTexture() {
        return depthTexture;
    }

    public void resolveToFBO(int targetBuffer, Framebuffer outputFBO) {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, outputFBO.id);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, id);
        GL11.glReadBuffer(targetBuffer);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, outputFBO.width, outputFBO.height, GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        unbindFrameBuffer();
    }

    public void resolveToBuffer(int targetBuffer, Framebuffer outputFBO) {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, outputFBO.id);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, id);
        GL11.glDrawBuffer(targetBuffer);
        GL11.glReadBuffer(targetBuffer);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, outputFBO.width, outputFBO.height, GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        unbindFrameBuffer();
    }

    public void copyDepthToFBO(int targetBuffer, Framebuffer outputFBO) {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, outputFBO.id);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, id);
        GL11.glReadBuffer(targetBuffer);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, outputFBO.width, outputFBO.height, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        unbindFrameBuffer();
    }

    public void copyDepthToDefaultFBO(int targetBuffer) {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, id);
        GL11.glReadBuffer(targetBuffer);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, Display.getWidth(), Display.getHeight(), GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        unbindFrameBuffer();
    }

    public void resolveToScreen() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, id);
        GL11.glDrawBuffer(GL11.GL_BACK);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, Display.getWidth(), Display.getHeight(), GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        unbindFrameBuffer();
    }

    private void initialiseFrameBuffer(int type) {
        createFrameBuffer();
        if (multiTarget)
            for (int i = 0; i < colourBuffers.length; i++)
                colourBuffers[i] = createColourAttachment(GL30.GL_COLOR_ATTACHMENT0 + i);
        else
            createTextureAttachment();

        if (type == DEPTH_RENDER_BUFFER)
            createDepthBufferAttachment();
        else if (type == DEPTH_TEXTURE)
            createDepthTextureAttachment();

        unbindFrameBuffer();
    }

    private void createFrameBuffer() {
        id = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);

        determineDrawBuffers();
    }

    private void determineDrawBuffers() {
        IntBuffer buffers = BufferUtils.createIntBuffer(colourBuffers.length);
        buffers.put(GL30.GL_COLOR_ATTACHMENT0);

        if (multiTarget)
            for (int i = 1; i < colourBuffers.length; i++)
                buffers.put(GL30.GL_COLOR_ATTACHMENT0 + i);

        buffers.flip();
        GL20.glDrawBuffers(buffers);
    }

    private void createTextureAttachment() {
        colourTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colourTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colourTexture, 0);
    }

    private void createDepthTextureAttachment() {
        depthTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture, 0);
    }

    private int createColourAttachment(int attachment) {
        int colourBuffer = GL30.glGenRenderbuffers();

        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, colourBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_RGBA16F, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, attachment, GL30.GL_RENDERBUFFER, colourBuffer);

        return colourBuffer;
    }

    private void createDepthBufferAttachment() {
        depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        if (!multiTarget)
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
        else
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
    }

}
