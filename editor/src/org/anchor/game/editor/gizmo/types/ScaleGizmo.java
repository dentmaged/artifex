package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.shared.editor.TransformableObject;
import org.lwjgl.util.vector.Vector3f;

public class ScaleGizmo extends ArrowGizmo {

    private static float[] vertices = new float[] { 0, 0, 0, 0, 0, -1, 0, 0.1f, -1, 0, -0.1f, -1, 0.1f, 0, -1, -0.1f, 0, -1 };

    public ScaleGizmo() {
        super(new ArrowPlane(), vertices);
    }

    @Override
    public Vector3f getVector(TransformableObject object) {
        return object.getScale();
    }

    @Override
    public Vector3f performMove(Vector3f axis, Vector3f original, Vector3f position, Vector3f rotation, Vector3f origin, Vector3f ray, int mouseDX, int mouseDY, Vector3f axisOffset) {
        Vector3f move = super.performMove(axis, original, position, rotation, origin, ray, mouseDX, mouseDY, axisOffset);
        if (axis.lengthSquared() == 2) {
            float size = Math.max(Math.max(move.x, move.y), move.z);
            move.set(size, size, size);
        }

        return move;
    }

    @Override
    public float getMin() {
        return 0.01f;
    }

}
