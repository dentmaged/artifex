package org.anchor.client.engine.renderer;

import org.anchor.client.engine.renderer.types.Mesh;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.ModelTexture;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

public class Renderer {

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

        Engine.bind2DTexture(texture.getId(), 0);
        Engine.bind2DTexture(texture.getNormalMap(), 1);
        Engine.bind2DTexture(texture.getSpecularMap(), 2);
        Engine.bind2DTexture(texture.getMetallicMap(), 3);
        Engine.bind2DTexture(texture.getRoughnessMap(), 4);
        Engine.bind2DTexture(texture.getAmbientOcclusionMap(), 5);

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

    public static Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public static Matrix4f getInverseProjectionMatrix() {
        return inverseProjectionMatrix;
    }

}
