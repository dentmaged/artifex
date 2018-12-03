package org.anchor.client.engine.renderer.blur;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.ImageFormat;
import org.lwjgl.opengl.Display;

public class Blur {

    private Framebuffer horizontal, vertical;
    protected BlurShader shader;
    protected float width, height;

    public Blur() {
        this(Display.getWidth() / 4, Display.getHeight() / 4);
    }

    public Blur(ImageFormat format) {
        this(Display.getWidth() / 4, Display.getHeight() / 4, format);
    }

    public Blur(int width, int height) {
        this(width, height, ImageFormat.RGBA);
    }

    public Blur(int width, int height, ImageFormat format) {
        horizontal = new Framebuffer(width, height, Framebuffer.NONE, format);
        vertical = new Framebuffer(width, height, Framebuffer.NONE, format);
        shader = BlurShader.getInstance();

        this.width = width;
        this.height = height;
    }

    public void perform(int texture) {
        shader.start();
        QuadRenderer.bind();

        Graphics.bind2DTexture(texture, 0);
        horizontal.bindFramebuffer();
        shader.loadInformation(true, width);
        QuadRenderer.render();
        horizontal.unbindFramebuffer();

        Graphics.bindColourTexture(horizontal, 0);

        vertical.bindFramebuffer();
        shader.loadInformation(false, height);
        QuadRenderer.render();
        vertical.unbindFramebuffer();

        QuadRenderer.unbind();
        shader.stop();
    }

    public Framebuffer getOutputFBO() {
        return vertical;
    }

    public void shutdown() {
        horizontal.shutdown();
        vertical.shutdown();
    }

    public void setShader(BlurShader shader) {
        this.shader = shader;
    }

}
