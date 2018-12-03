package org.anchor.client.engine.renderer.vignette;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;

public class Vignette {

    protected VignetteShader shader;

    public Vignette() {
        shader = VignetteShader.getInstance();
    }

    public void perform(int texture) {
        shader.start();
        QuadRenderer.bind();
        Graphics.bind2DTexture(texture, 0);

        QuadRenderer.render();

        QuadRenderer.unbind();
        shader.stop();
    }

}
