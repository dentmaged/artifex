package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.common.utils.AABB;
import org.anchor.game.editor.gizmo.Gizmo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class ArrowGizmo extends Gizmo {

    protected AABB aabb;

    public ArrowGizmo(float[] vertices) {
        super(vertices, GL11.GL_LINES);
        this.aabb = AABB.generateAABB(vertices);
    }

    @Override
    public Vector3f intersection(Matrix4f matrix, Vector3f origin, Vector3f ray, Vector3f position, Vector3f rotation, Vector3f scale) {
        return AABB.generateAABB(aabb, matrix).raycast(origin, ray);
    }

    @Override
    public Vector3f getXRotation() {
        return new Vector3f(0, 270, 0);
    }

    @Override
    public Vector3f getYRotation() {
        return new Vector3f(90, 0, 0);
    }

    @Override
    public Vector3f getZRotation() {
        return new Vector3f(0, 180, 0);
    }

}
