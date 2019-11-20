package org.anchor.client.engine.renderer.fog;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.types.light.Light;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

public class Fog {

    protected FogShader shader;

    public Fog() {
        shader = FogShader.getInstance();
    }

    public void perform(int scene, int depthMap, IFogManager fogManager, Light sun, Matrix4f viewMatrix) {
        if (fogManager == null || !fogManager.isFogEnabled())
            return;

        GL11.glDepthMask(false);

        shader.start();
        shader.loadInformation(fogManager, sun.getDirection(), sun.getColour(), viewMatrix);
        QuadRenderer.bind();

        Graphics.bind2DTexture(scene, 0);
        Graphics.bind2DTexture(depthMap, 1);

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        GL11.glDepthMask(true);
    }

}
