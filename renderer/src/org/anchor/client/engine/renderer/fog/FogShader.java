package org.anchor.client.engine.renderer.fog;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Vector3f;

public class FogShader extends Shader {

    private static FogShader instance = new FogShader();

    public FogShader() {
        super("fog");
    }

    @Override
    public void start() {
        super.start();

        loadInt("scene", 0);
        loadInt("depthMap", 1);
    }

    public void loadInformation(Vector3f baseColour) {
        loadFloat("density", Settings.density);
        loadFloat("gradient", Settings.gradient);
        loadVector("skyColour", baseColour);
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");

        bindAttribute(0, "position");
    }

    public static FogShader getInstance() {
        return instance;
    }

}
