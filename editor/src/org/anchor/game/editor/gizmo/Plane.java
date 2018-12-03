package org.anchor.game.editor.gizmo;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Plane {

    protected Mesh mesh;
    protected int type;

    protected AABB aabb;

    public Plane(float[] vertices, int type) {
        this.mesh = Loader.getInstance().loadToVAO(vertices, 3);
        this.type = type;
        this.aabb = AABB.generateAABB(vertices);
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
        return AABB.generateAABB(aabb, matrix).raycast(origin, ray);
    }

    public Vector3f performMove(Vector3f axis, Vector3f original, Vector3f position, Vector3f rotation, Vector3f origin, Vector3f ray, int mouseDX, int mouseDY) {
        int dist = mouseDX;
        if (Math.abs(mouseDY) > Math.abs(mouseDX))
            dist = mouseDY;

        return VectorUtils.mul(axis, dist * 0.065f);
    }

    public void shutdown() {
        GL30.glDeleteVertexArrays(mesh.getVAO());
    }

}
