package org.anchor.client.engine.renderer.pbr;

import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.types.Framebuffer;

public class BRDF {

    protected Framebuffer outputFBO;
    protected static BRDFShader shader;

    public BRDF() {
        outputFBO = new Framebuffer(512, 512, Framebuffer.NONE, true);
        shader = BRDFShader.getInstance();
    }

    public void perform() {
        outputFBO.bindFramebuffer();
        shader.start();

        QuadRenderer.bind();
        QuadRenderer.render();
        QuadRenderer.unbind();

        shader.stop();
        outputFBO.unbindFramebuffer();
    }

    public Framebuffer getOutputFBO() {
        return outputFBO;
    }

}
