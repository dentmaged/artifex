package org.anchor.client.engine.renderer.bloom;

import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.blur.Blur;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Bloom {

    private Framebuffer outputFBO;
    protected BloomShader bloomShader;
    protected CombineShader combineShader;
    protected Blur blur;

    public static float exposure = 1;

    public Bloom() {
        outputFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.DEPTH_TEXTURE);
        bloomShader = BloomShader.getInstance();
        combineShader = CombineShader.getInstance();
        blur = new Blur();
    }

    public void perform(int scene, int bloomTexture) {
        bloomShader.start();
        QuadRenderer.bind();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, bloomTexture);

        outputFBO.bindFrameBuffer();
        QuadRenderer.render();
        outputFBO.unbindFrameBuffer();

        QuadRenderer.unbind();
        bloomShader.stop();

        blur.perform(outputFBO.getColourTexture());

        combineShader.start();
        QuadRenderer.bind();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, scene);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, blur.getOutputFBO().getColourTexture());

        outputFBO.bindFrameBuffer();
        combineShader.loadInformation(exposure);
        QuadRenderer.render();
        outputFBO.unbindFrameBuffer();

        QuadRenderer.unbind();
        combineShader.stop();
    }

    public void shutdown() {
        outputFBO.shutdown();
        blur.shutdown();
        bloomShader.shutdown();
        combineShader.shutdown();
    }

    public Framebuffer getOutputFBO() {
        return outputFBO;
    }

}
