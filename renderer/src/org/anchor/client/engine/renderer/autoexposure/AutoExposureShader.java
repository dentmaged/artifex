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
        super.bindFragOutput(0, "out_colour");

        super.bindAttribute(0, "position");
    }

    public void loadInformation() {
        loadFloat("exposureSpeed", Settings.exposureSpeed * Window.getFrameTimeSeconds());
    }

    public static AutoExposureShader getInstance() {
        return instance;
    }

}
