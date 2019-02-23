package org.anchor.client.engine.renderer.bloom;

import org.anchor.client.engine.renderer.Shader;

public class BloomShader extends Shader {

    private static BloomShader instance = new BloomShader();

    public BloomShader() {
        super("bloom");
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");
        bindAttribute(0, "position");
    }

    @Override
    public void start() {
        super.start();

        loadInt("scene", 0);
        loadInt("bloom", 1);
        loadInt("exposure", 2);
    }

    public static BloomShader getInstance() {
        return instance;
    }

}
