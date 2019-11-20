package org.anchor.client.engine.renderer.clear;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.anchor.engine.common.Log;

public class ClearColourShader extends Shader {

    private static ClearColourShader instance = new ClearColourShader();

    public ClearColourShader() {
        super("clear");
    }

    @Override
    protected void bindAttributes() {
        Log.debug("binding");
        bindFragOutput(0, "out_diffuse");
        bindFragOutput(1, "out_other");
        bindFragOutput(2, "out_normal");
        bindFragOutput(3, "out_albedo");

        bindAttribute(0, "position");
    }

    @Override
    public void start() {
        super.start();

        loadVector("background", Settings.clearR, Settings.clearG, Settings.clearB);
    }

    public static ClearColourShader getInstance() {
        return instance;
    }

}
