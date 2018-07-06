package org.anchor.client.engine.renderer.fxaa;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Vector2f;

public class FXAAShader extends Shader {

    private static FXAAShader instance = new FXAAShader();

    public FXAAShader() {
        super("fxaa");
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
    }

    public void loadInformation(Vector2f step) {
        loadVector("step", step);
    }

    public static FXAAShader getInstance() {
        return instance;
    }

}
