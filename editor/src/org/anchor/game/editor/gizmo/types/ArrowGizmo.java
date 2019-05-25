package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.common.utils.AABB;
import org.anchor.game.editor.gizmo.Gizmo;
import org.anchor.game.editor.gizmo.Plane;
import org.anchor.game.editor.gizmo.TransformationMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class ArrowGizmo extends Gizmo {

    protected AABB aabb;

    public ArrowGizmo(Plane plane, float[] vertices) {
        super(vertices, plane, GL11.GL_LINES);
        this.aabb = AABB.generateAABB(vertices);
    }

    @Override
    public Vector3f intersection(Matrix4f matrix, Vector3f origin, Vector3f ray, Vector3f position, Vector3f rotation, Vector3f scale) {
        return AABB.generateAABB(aabb, matrix).raycast(origin, ray);
    }

    @Override
    public Vector3f getXRotation(Vector3f rotation, TransformationMode mode) {
        if (mode == TransformationMode.LOCAL)
            return new Vector3f(270, 270 + rotation.y, rotation.z);

        return new Vector3f(270, 270, 0);
    }

    @Override
    public Vector3f getYRotation(Vector3f rotation, TransformationMode mode) {
        if (mode == TransformationMode.LOCAL)
            return new Vector3f(90 + rotation.x, 0, 270 + rotation.z);

        return new Vector3f(90, 0, 270);
    }

    @Override
    public Vector3f getZRotation(Vector3f rotation, TransformationMode mode) {
        if (mode == TransformationMode.LOCAL)
            return new Vector3f(rotation.z, rotation.y, 0);

        return new Vector3f(0, 0, 0);
    }

}
