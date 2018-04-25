package org.anchor.client.engine.renderer.bloom;

import org.anchor.client.engine.renderer.Shader;

public class CombineShader extends Shader {

    private static CombineShader instance = new CombineShader();

    public CombineShader() {
        super("combine");
    }

    @Override
    public void start() {
        super.start();

        loadInt("a", 0);
        loadInt("b", 1);
    }

    public void loadInformation(float exposure) {
        loadFloat("exposure", exposure);
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");
        super.bindAttribute(0, "position");
    }

    public static CombineShader getInstance() {
        return instance;
    }

}
