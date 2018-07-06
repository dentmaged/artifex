package org.anchor.client.engine.renderer.pbr;

import org.anchor.client.engine.renderer.Shader;

public class BRDFShader extends Shader {

    private static BRDFShader instance = new BRDFShader();

    public BRDFShader() {
        super("brdf");
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");

        bindAttribute(0, "position");
    }

    public static BRDFShader getInstance() {
        return instance;
    }

}
