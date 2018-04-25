package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.common.utils.Raycast;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.editor.gizmo.Gizmo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class RotateGizmo extends Gizmo {

    private static float ROTATION_TO_NORMAL = 1f / 90f;
    private static int CIRCLE_VERTICES = 30;
    private static float[] vertices;

    static {
        generateCircle();
    }

    public RotateGizmo() {
        super(vertices, GL11.GL_LINE_LOOP);
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

    private static void generateCircle() {
        vertices = new float[CIRCLE_VERTICES * 3];

        for (int i = 0; i < CIRCLE_VERTICES; i++) {
            double angle = ((double) i / (double) CIRCLE_VERTICES) * Math.PI * 2;

            vertices[i * 3] = (float) Math.cos(angle);
            vertices[i * 3 + 1] = 0;
            vertices[i * 3 + 2] = (float) Math.sin(angle);
        }
    }

    @Override
    public Vector3f getXRotation() {
        return new Vector3f(0, 0, 90);
    }

    @Override
    public Vector3f getYRotation() {
        return new Vector3f(0, 90, 0);
    }

    @Override
    public Vector3f getZRotation() {
        return new Vector3f(90, 0, 0);
    }

    @Override
    public Vector3f getVector(Entity entity) {
        return entity.getRotation();
    }

}
