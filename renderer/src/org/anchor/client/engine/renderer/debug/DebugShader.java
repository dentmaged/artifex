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
        super.bindFragOutput(0, "out_colour");
        super.bindAttribute(0, "position");
    }

    public void loadInformation(Matrix4f viewMatrix, Matrix4f transformationMatrix, Vector3f colour) {
        loadMatrix("viewMatrix", viewMatrix);
        loadMatrix("transformationMatrix", transformationMatrix);
        loadVector("colour", colour);
    }

    public static DebugShader getInstance() {
        return instance;
    }

}
