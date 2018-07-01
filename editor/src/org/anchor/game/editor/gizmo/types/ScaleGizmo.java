package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.editor.utils.TransformationMode;
import org.lwjgl.util.vector.Vector3f;

public class ScaleGizmo extends ArrowGizmo {

    private static float[] vertices = new float[] {
            0, 0, 0, 0, 0, -1, 0, 0.1f, -1, 0, -0.1f, -1, 0.1f, 0, -1, -0.1f, 0, -1
    };

    public ScaleGizmo() {
        super(vertices);
    }

    @Override
    public Vector3f getXRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f(0, 270 + rotation.y, rotation.z);
    }

    @Override
    public Vector3f getYRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f(90 + rotation.x, 0, rotation.z);
    }

    @Override
    public Vector3f getZRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f(rotation.z, 180 + rotation.y, 0);
    }

    @Override
    public Vector3f getVector(Entity entity) {
        return entity.getScale();
    }

}
