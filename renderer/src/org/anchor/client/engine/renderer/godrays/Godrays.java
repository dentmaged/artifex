package org.anchor.client.engine.renderer.godrays;

import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.bloom.Bloom;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.Light;
import org.anchor.engine.common.utils.Projection;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;

public class Godrays {

    protected Framebuffer outputFBO;
    protected GodraysShader shader;
    protected Projection projection;

    public Godrays() {
        outputFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        shader = GodraysShader.getInstance();
        projection = new Projection(Renderer.getProjectionMatrix());
    }

    public void perform(int scene, int godraysTexture, Matrix4f viewMatrix, Light light) {
        outputFBO.bindFrameBuffer();
        shader.start();
        QuadRenderer.bind();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, scene);

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, godraysTexture);

        shader.loadInformation(100, projection.update(light.getPosition(), viewMatrix), Bloom.exposure);

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();
        outputFBO.unbindFrameBuffer();
    }

    public void shutdown() {
        shader.shutdown();
        outputFBO.shutdown();
    }

    public Framebuffer getOutputFBO() {
        return outputFBO;
    }

}
