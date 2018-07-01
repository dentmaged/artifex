package org.anchor.client.engine.renderer.vignette;

import org.anchor.client.engine.renderer.Shader;

public class VignetteShader extends Shader {

    private static VignetteShader instance = new VignetteShader();

    public VignetteShader() {
        super("vignette");
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");
        super.bindAttribute(0, "position");
    }

    @Override
    public void start() {
        super.start();

        loadInt("a", 0);
    }

    public static VignetteShader getInstance() {
        return instance;
    }

}
