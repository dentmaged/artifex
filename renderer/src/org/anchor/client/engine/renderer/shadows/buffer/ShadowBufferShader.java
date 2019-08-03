package org.anchor.client.engine.renderer.shadows.buffer;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.lwjgl.util.vector.Matrix4f;

public class ShadowBufferShader extends Shader {

    private static ShadowBufferShader instance = new ShadowBufferShader();

    public ShadowBufferShader() {
        super("shadowBuffer");
    }

    @Override
    public void start() {
        super.start();

        loadInt("depthMap", 0);
        for (int i = 0; i < Settings.shadowSplits; i++)
            loadInt("shadowMaps[" + i + "]", 13 + i);
    }

    public void loadInformation(Matrix4f inverseViewMatrix, Shadows shadows) {
        loadMatrix("inverseViewMatrix", inverseViewMatrix);

        for (int i = 0; i < Settings.shadowSplits; i++) {
            loadMatrix("toShadowMapSpaces[" + i + "]", shadows.getToShadowMapSpaceMatrix(i));
            loadFloat("shadowDistances[" + i + "]", shadows.getExtents(i));
        }
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");

        bindAttribute(0, "position");
    }

    public static ShadowBufferShader getInstance() {
        return instance;
    }

}
