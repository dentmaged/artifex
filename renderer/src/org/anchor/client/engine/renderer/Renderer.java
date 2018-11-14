package org.anchor.client.engine.renderer;

import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

public class Renderer {

    private static Matrix4f projectionMatrix, inverseProjectionMatrix;
    private static Model cube;

    static {
        createProjectionMatrix();
    }

    public static void bind(Model model) {
        Mesh mesh = model.getMesh();

        GL30.glBindVertexArray(mesh.getVAO());
        for (int i = 0; i < mesh.getDimensions(); i++)
            GL20.glEnableVertexAttribArray(i);
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
        float aspectRatio = (float) Settings.width / (float) Settings.height;
        float yScale = 1f / Mathf.tan(Mathf.toRadians(Settings.fov * 0.5f));
        float xScale = yScale / aspectRatio;
        float frustumLength = Settings.farPlane - Settings.nearPlane;

        projectionMatrix.m00 = xScale;
        projectionMatrix.m11 = yScale;

        projectionMatrix.m22 = -((Settings.farPlane + Settings.nearPlane) / frustumLength);
        projectionMatrix.m23 = -1;

        projectionMatrix.m32 = -((2 * Settings.farPlane * Settings.nearPlane) / frustumLength);
        projectionMatrix.m33 = 0;

        inverseProjectionMatrix = Matrix4f.invert(projectionMatrix, null);
    }

    public static void refreshProjectionMatrix() {
        createProjectionMatrix();

        for (Shader shader : Shader.getShaders()) {
            shader.start();
            shader.loadMatrix("projectionMatrix", Renderer.getProjectionMatrix());
            shader.loadMatrix("inverseProjectionMatrix", Renderer.getInverseProjectionMatrix());
            shader.stop();
        }
    }

    public static void reloadShaders() {
        for (Shader shader : Shader.getShaders())
            shader.reload();
    }

    public static void setCubeModel(Model cube) {
        Renderer.cube = cube;
    }

    public static Model getCubeModel() {
        return cube;
    }

    public static Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public static Matrix4f getInverseProjectionMatrix() {
        return inverseProjectionMatrix;
    }

}
