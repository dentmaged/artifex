package org.anchor.client.engine.renderer.clear;

import org.anchor.client.engine.renderer.QuadRenderer;
import org.lwjgl.opengl.GL11;

public class ClearColour {

    protected ClearColourShader shader;

    public ClearColour() {
        shader = ClearColourShader.getInstance();
    }

    public void perform() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        shader.start();
        QuadRenderer.bind();

        QuadRenderer.render();

        QuadRenderer.unbind();
        shader.stop();
        GL11.glDepthMask(true);
    }

}
