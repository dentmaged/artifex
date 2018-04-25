package org.anchor.game.editor.gizmo;

import org.anchor.game.client.shaders.ModelShader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class GizmoShader extends ModelShader {

    private static GizmoShader instance = new GizmoShader();

    protected GizmoShader() {
        super("gizmo");
    }

    public void loadInformation(Matrix4f matrix, Vector3f colour) {
        loadMatrix("transformationMatrix", matrix);
        loadVector("colour", colour);
    }

    public static GizmoShader getInstance() {
        return instance;
    }

}
