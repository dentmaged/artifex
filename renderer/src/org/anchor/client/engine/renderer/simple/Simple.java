package org.anchor.client.engine.renderer.simple;

import org.anchor.client.engine.renderer.QuadRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Simple {

    protected SimpleShader shader;

    public Simple() {
        shader = SimpleShader.getInstance();
    }

    public void perform(int texture) {
        shader.start();
        QuadRenderer.bind();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        QuadRenderer.render();

        QuadRenderer.unbind();
        shader.stop();
    }

}
