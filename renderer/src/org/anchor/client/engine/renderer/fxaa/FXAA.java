package org.anchor.client.engine.renderer.fxaa;

import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
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
        outputFBO.bindFrameBuffer();

        shader.start();
        QuadRenderer.bind();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, scene);

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        outputFBO.unbindFrameBuffer();
    }

    public Framebuffer getOutputFBO() {
        return outputFBO;
    }

    public void shutdown() {
        outputFBO.shutdown();
        shader.shutdown();
    }

}
