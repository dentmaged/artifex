package org.anchor.client.engine.renderer.simple;

import org.anchor.client.engine.renderer.Shader;

public class SimpleShader extends Shader {

    private static SimpleShader instance = new SimpleShader();

    public SimpleShader() {
        super("simple");
    }

    @Override
    public void start() {
        super.start();

        loadInt("a", 0);
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");

        super.bindAttribute(0, "position");
    }

    public static SimpleShader getInstance() {
        return instance;
    }

}
