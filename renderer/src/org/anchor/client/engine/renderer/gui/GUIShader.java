package org.anchor.client.engine.renderer.gui;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Matrix4f;

public class GUIShader extends Shader {

    private static GUIShader instance = new GUIShader();

    public GUIShader() {
        super("gui");
    }

    @Override
    public void start() {
        super.start();

        loadInt("a", 0);
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");

        bindAttribute(0, "position");
    }

    public void loadInformation(Matrix4f transformationMatrix, float mip) {
        loadMatrix("transformationMatrix", transformationMatrix);
        loadFloat("mip", mip);
    }

    public static GUIShader getInstance() {
        return instance;
    }

}
