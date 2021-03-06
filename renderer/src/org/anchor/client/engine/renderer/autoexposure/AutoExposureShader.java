package org.anchor.client.engine.renderer.autoexposure;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.anchor.client.engine.renderer.Window;

public class AutoExposureShader extends Shader {

    private static AutoExposureShader instance = new AutoExposureShader();

    public AutoExposureShader() {
        super("exposure");
    }

    @Override
    public void start() {
        super.start();

        loadInt("exposure", 0);
        loadInt("previousExposure", 1);
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");

        bindAttribute(0, "position");
    }

    public void loadInformation(float mip) {
        loadFloat("exposureSpeed", Settings.exposureSpeed * Window.getFrameTimeSeconds());
        loadFloat("mip", mip);
    }

    public static AutoExposureShader getInstance() {
        return instance;
    }

}
