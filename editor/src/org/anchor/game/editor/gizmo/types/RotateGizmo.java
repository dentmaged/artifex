package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.common.utils.Raycast;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.game.editor.gizmo.Gizmo;
import org.anchor.game.editor.gizmo.TransformationMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class RotateGizmo extends Gizmo {

    private static float ROTATION_TO_NORMAL = 1f / 90f;
    private static int CIRCLE_VERTICES = 30;
    private static float[] vertices;

    static {
        vertices = new float[CIRCLE_VERTICES * 3];

        for (int i = 0; i < CIRCLE_VERTICES; i++) {
            double angle = ((double) i / (double) CIRCLE_VERTICES) * Math.PI * 2;

            vertices[i * 3] = (float) Math.cos(angle);
            vertices[i * 3 + 1] = 0;
            vertices[i * 3 + 2] = (float) Math.sin(angle);
        }
    }

    public RotateGizmo() {
        super(vertices, null, GL11.GL_LINE_LOOP);
    }

    @Override
    public Vector3f intersection(Matrix4f matrix, Vector3f origin, Vector3f ray, Vector3f position, Vector3f rotation, Vector3f scale) {
        Vector3f normal = VectorUtils.mul(rotation, ROTATION_TO_NORMAL);
        float f = normal.x;
        normal.x = normal.z;
        normal.z = f;

        if (Raycast.intersectionCircle(normal, position, origin, ray, scale.x))
            return new Vector3f();

        return null;
    }

    @Override
    public Vector3f getXRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f(0, 0, 90);
    }

    @Override
    public Vector3f getYRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f(0, 90, 0);
    }

    @Override
    public Vector3f getZRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f(90, 0, 0);
    }

    @Override
    public Vector3f getVector(TransformableObject object) {
        return object.getRotation();
    }

    @Override
    public Vector3f performMove(Vector3f axis, Vector3f original, Vector3f position, Vector3f rotation, Vector3f origin, Vector3f ray, int mouseDX, int mouseDY, Vector3f axisOffset) {
        int dist = mouseDX;
        if (Math.abs(mouseDY) > Math.abs(mouseDX))
            dist = mouseDY;

        return VectorUtils.mul(axis, dist * 0.2f);
    }

}
