package org.anchor.client.engine.renderer.blur;

import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Blur {

    private Framebuffer horizontal, vertical;
    protected BlurShader shader;
    protected float width, height;

    public Blur() {
        this(Display.getWidth() / 4, Display.getHeight() / 4);
    }

    public Blur(int width, int height) {
        horizontal = new Framebuffer(width, height, Framebuffer.DEPTH_TEXTURE);
        vertical = new Framebuffer(width, height, Framebuffer.DEPTH_TEXTURE);
        shader = BlurShader.getInstance();

        this.width = width;
        this.height = height;
    }

    public void perform(int texture) {
        shader.start();
        QuadRenderer.bind();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        horizontal.bindFrameBuffer();
        shader.loadInformation(true, width);
        QuadRenderer.render();
        horizontal.unbindFrameBuffer();

        vertical.bindFrameBuffer();
        shader.loadInformation(false, height);
        QuadRenderer.render();
        vertical.unbindFrameBuffer();

        QuadRenderer.unbind();
        shader.stop();
    }

    public Framebuffer getOutputFBO() {
        return vertical;
    }

    public void shutdown() {
        horizontal.shutdown();
        vertical.shutdown();
        shader.shutdown();
    }

    public void setShader(BlurShader shader) {
        this.shader = shader;
    }

}
