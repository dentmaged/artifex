package org.anchor.game.client.debug;

import org.anchor.game.client.shaders.ModelShader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class DebugShader extends ModelShader {

    private static DebugShader instance = new DebugShader();

    protected DebugShader() {
        super("debug");
    }

    public void loadInformation(Matrix4f matrix, Vector3f colour) {
        loadMatrix("transformationMatrix", matrix);
        loadVector("colour", colour);
    }

    public static DebugShader getInstance() {
        return instance;
    }

}
