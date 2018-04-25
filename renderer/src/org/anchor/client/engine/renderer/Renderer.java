package org.anchor.client.engine.renderer;

import org.anchor.client.engine.renderer.types.Mesh;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.ModelTexture;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

public class Renderer {

    public static final float FOV = 70;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 10000;

    private static Matrix4f projectionMatrix, inverseProjectionMatrix;

    static {
        createProjectionMatrix();
    }

    public static void bind(Model model) {
        Mesh mesh = model.getMesh();
        ModelTexture texture = model.getTexture();

        GL30.glBindVertexArray(mesh.getVAO());
        for (int i = 0; i < mesh.getDimensions(); i++)
            GL20.glEnableVertexAttribArray(i);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());

        if (texture.getNormalMap() != 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getNormalMap());
        }

        if (texture.getSpecularMap() != 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getSpecularMap());
        }

        if (texture.isCullingEnabled()) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    public static void render(Model model) {
        GL11.glDrawElements(GL11.GL_TRIANGLES, model.getMesh().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
    }

    public static void unbind(Model model) {
        for (int i = 0; i < model.getMesh().getDimensions(); i++)
            GL20.glDisableVertexAttribArray(i);
        GL30.glBindVertexArray(0);
    }

    private static void createProjectionMatrix() {
        projectionMatrix = new Matrix4f();
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;

        inverseProjectionMatrix = Matrix4f.invert(projectionMatrix, null);
    }

    public static Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public static Matrix4f getInverseProjectionMatrix() {
        return inverseProjectionMatrix;
    }

}
