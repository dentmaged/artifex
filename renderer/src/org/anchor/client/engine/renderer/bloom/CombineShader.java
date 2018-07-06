package org.anchor.client.engine.renderer.bloom;

import org.anchor.client.engine.renderer.Shader;

public class CombineShader extends Shader {

    private static CombineShader instance = new CombineShader();

    public CombineShader() {
        super("combine");
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");
        bindAttribute(0, "position");
    }

    @Override
    public void start() {
        super.start();

        loadInt("ldr", 0);
        loadInt("bloomOne", 1);
        loadInt("bloomTwo", 2);
        loadInt("bloomThree", 3);
        loadInt("exposure", 4);
    }

    public static CombineShader getInstance() {
        return instance;
    }

}
