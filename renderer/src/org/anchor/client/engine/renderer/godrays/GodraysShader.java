package org.anchor.client.engine.renderer.godrays;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Vector2f;

public class GodraysShader extends Shader {

    private static GodraysShader instance = new GodraysShader();

    public GodraysShader() {
        super("godrays");
    }

    @Override
    public void start() {
        super.start();

        loadInt("scene", 0);
        loadInt("godrays", 1);
        loadInt("exposure", 1);
    }

    public void loadInformation(int samples, Vector2f projectedSunPosition) {
        loadInt("numSamples", samples);
        loadVector("lightPositionOnScreen", projectedSunPosition);
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");

        super.bindAttribute(0, "position");
    }

    public static GodraysShader getInstance() {
        return instance;
    }

}
