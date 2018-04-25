package org.anchor.client.engine.renderer.bloom;

import org.anchor.client.engine.renderer.Shader;

public class BloomShader extends Shader {

    private static BloomShader instance = new BloomShader();

    public BloomShader() {
        super("bloom");
    }

    @Override
    public void start() {
        super.start();

        loadInt("scene", 0);
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");
        super.bindAttribute(0, "position");
    }

    public static BloomShader getInstance() {
        return instance;
    }

}
