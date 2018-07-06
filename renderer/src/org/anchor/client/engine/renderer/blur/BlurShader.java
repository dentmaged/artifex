package org.anchor.client.engine.renderer.blur;

import org.anchor.client.engine.renderer.Shader;

public class BlurShader extends Shader {

    private static BlurShader instance = new BlurShader();

    public BlurShader() {
        super("blur");
    }

    public BlurShader(String shader) {
        super(shader);
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");
        bindAttribute(0, "position");
    }

    @Override
    public void start() {
        super.start();

        loadInt("originalTexture", 0);
    }

    public void loadInformation(boolean horizontal, float dimension) {
        loadVector("unit", horizontal ? 1f / dimension : 0, horizontal ? 0 : 1f / dimension);
    }

    public static BlurShader getInstance() {
        return instance;
    }

}
