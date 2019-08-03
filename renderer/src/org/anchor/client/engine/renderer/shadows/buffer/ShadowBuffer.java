package org.anchor.client.engine.renderer.shadows.buffer;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.blur.Blur;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.ImageFormat;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

public class ShadowBuffer {

    protected Framebuffer outputFBO;
    protected Blur blur;
    protected ShadowBufferShader shader;

    public ShadowBuffer() {
        outputFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE, ImageFormat.RGBA);
        blur = new Blur(Display.getWidth() / 2, Display.getHeight() / 2);
        shader = ShadowBufferShader.getInstance();
    }

    public void perform(Shadows shadows, Matrix4f inverseViewMatrix, int depthMap) {
        outputFBO.bindFramebuffer();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        shader.start();
        shader.loadInformation(inverseViewMatrix, shadows);

        QuadRenderer.bind();
        Graphics.bind2DTexture(depthMap, 0);

        for (int i = 0; i < Settings.shadowSplits; i++)
            Graphics.bind2DTexture(shadows.getShadowMap(i), 13 + i);

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        outputFBO.unbindFramebuffer();
        blur.perform(outputFBO.getColourTexture());
    }

    public int getShadowBuffer() {
        if (Keyboard.isKeyDown(Keyboard.KEY_V))
            return outputFBO.getColourTexture();
        return blur.getOutputFBO().getColourTexture();
    }

    public void shutdown() {
        outputFBO.shutdown();
    }

}
