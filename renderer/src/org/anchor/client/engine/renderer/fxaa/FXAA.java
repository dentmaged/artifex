package org.anchor.client.engine.renderer.fxaa;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

public class FXAA {

    protected Framebuffer outputFBO;
    protected FXAAShader shader;

    public FXAA() {
        outputFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        shader = FXAAShader.getInstance();

        shader.start();
        shader.loadInformation(new Vector2f(1f / (float) Display.getWidth(), 1f / (float) Display.getHeight()));
        shader.stop();
    }

    public void perform(int scene) {
        outputFBO.bindFramebuffer();

        shader.start();
        QuadRenderer.bind();
        Graphics.bind2DTexture(scene, 0);

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        outputFBO.unbindFramebuffer();
    }

    public Framebuffer getOutputFBO() {
        return outputFBO;
    }

    public void shutdown() {
        outputFBO.shutdown();
        shader.shutdown();
    }

}
