package org.anchor.client.engine.renderer.debug;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class DebugShader extends Shader {

    private static DebugShader instance = new DebugShader();

    protected DebugShader() {
        super("debug");
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");
        bindAttribute(0, "position");
    }

    public void loadInformation(Matrix4f viewMatrix, Matrix4f transformationMatrix, Vector3f colour, float alpha) {
        loadMatrix("viewMatrix", viewMatrix);
        loadMatrix("transformationMatrix", transformationMatrix);
        loadVector("colour", colour);
        loadFloat("alpha", alpha);
    }

    public static DebugShader getInstance() {
        return instance;
    }

}
