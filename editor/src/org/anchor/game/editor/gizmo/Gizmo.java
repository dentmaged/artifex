package org.anchor.game.editor.gizmo;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.editor.utils.TransformationMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Gizmo {

    protected Mesh mesh;
    protected Plane plane;
    protected int type;

    public static int SELECT_MODE = 0;
    public static int TRANSLATE_MODE = 1;
    public static int ROTATE_MODE = 2;
    public static int SCALE_MODE = 3;

    public Gizmo(float[] vertices, Plane plane, int type) {
        this.mesh = Loader.getInstance().loadToVAO(vertices, 3);
        this.plane = plane;
        this.type = type;
    }

    public void bind() {
        GL30.glBindVertexArray(mesh.getVAO());
        GL20.glEnableVertexAttribArray(0);
    }

    public void render() {
        GL11.glDrawArrays(type, 0, mesh.getVertexCount());
    }

    public void unbind() {
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    public Vector3f intersection(Matrix4f matrix, Vector3f origin, Vector3f ray, Vector3f position, Vector3f rotation, Vector3f scale) {
        return null;
    }

    public Vector3f getXRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f();
    }

    public Vector3f getYRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f();
    }

    public Vector3f getZRotation(Vector3f rotation, TransformationMode mode) {
        return new Vector3f();
    }

    public Vector3f getVector(Entity entity) {
        return new Vector3f();
    }

    public Vector3f performMove(Vector3f axis, Vector3f original, Vector3f position, Vector3f rotation, Vector3f origin, Vector3f ray, int mouseDX, int mouseDY, Vector3f axisOffset) {
        int dist = mouseDX;
        if (Math.abs(mouseDY) > Math.abs(mouseDX))
            dist = mouseDY;

        return VectorUtils.mul(axis, dist * 0.065f);
    }

    public float getMin() {
        return -Float.MAX_VALUE; // Java bug
    }

    public float getMax() {
        return Float.MAX_VALUE;
    }

    public Plane getPlane() {
        return plane;
    }

    public void shutdown() {
        GL30.glDeleteVertexArrays(mesh.getVAO());
        if (plane != null)
            plane.shutdown();
    }

}
