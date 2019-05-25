package org.anchor.client.engine.renderer.fog;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.types.light.Light;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Fog {

    protected FogShader shader;

    public Fog() {
        shader = FogShader.getInstance();
    }

    public void perform(int scene, int depthMap, Vector3f baseColour, Light sun, Matrix4f viewMatrix) {
        GL11.glDepthMask(false);

        shader.start();
        shader.loadInformation(baseColour, sun.getDirection(), sun.getColour(), viewMatrix);
        QuadRenderer.bind();

        Graphics.bind2DTexture(scene, 0);
        Graphics.bind2DTexture(depthMap, 1);

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        GL11.glDepthMask(true);
    }

}
