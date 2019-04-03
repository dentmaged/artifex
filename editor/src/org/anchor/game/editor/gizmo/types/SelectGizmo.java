package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.shared.editor.TransformableObject;
import org.lwjgl.util.vector.Vector3f;

public class SelectGizmo extends ArrowGizmo {

    private static float[] vertices = new float[] { 0, 0, 0, 0, 0, -1 };

    public SelectGizmo() {
        super(null, vertices);
    }

    @Override
    public Vector3f getVector(TransformableObject object) {
        return object.getPosition(); // default transformation
    }

}
