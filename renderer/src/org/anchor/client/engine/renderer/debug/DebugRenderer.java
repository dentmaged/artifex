package org.anchor.client.engine.renderer.debug;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.CoreMaths;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class DebugRenderer {

    private static Mesh circle;
    private static DebugShader shader = DebugShader.getInstance();

    private static int CIRCLE_VERTICES = 30;

    public static void circle(Matrix4f viewMatrix, Vector3f position, Vector3f rotation) {
        circle(viewMatrix, position, rotation, new Vector3f(1, 1, 1), new Vector3f(1, 0, 0));
    }

    public static void circle(Matrix4f viewMatrix, Vector3f position, Vector3f rotation, Vector3f scale, Vector3f colour) {
        if (circle == null)
            circle = Loader.getInstance().loadToVAO(generateCircle(), 3);

        shader.start();
        shader.loadInformation(viewMatrix, CoreMaths.createTransformationMatrix(position, rotation, scale), colour, 1);
        GL30.glBindVertexArray(circle.getVAO());
        GL20.glEnableVertexAttribArray(0);

        GL11.glDrawArrays(GL11.GL_LINE_LOOP, 0, circle.getVertexCount());

        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    public static void box(Matrix4f viewMatrix, Vector3f position, Vector3f rotation) {
        box(viewMatrix, position, rotation, new Vector3f(1, 1, 1), new Vector3f(1, 0, 0));
    }

    public static void box(Matrix4f viewMatrix, Vector3f position, Vector3f rotation, Vector3f scale, Vector3f colour) {
        if (Renderer.getCubeModel() == null)
            return;

        shader.start();
        shader.loadInformation(viewMatrix, CoreMaths.createTransformationMatrix(position, rotation, scale), colour, 0.8f);
        GL30.glBindVertexArray(Renderer.getCubeModel().getMesh().getVAO());
        GL20.glEnableVertexAttribArray(0);

        Renderer.triangleCount += Renderer.getCubeModel().getMesh().getVertexCount() / 3;
        Renderer.drawCalls++;

        GL11.glDrawElements(GL11.GL_TRIANGLES, Renderer.getCubeModel().getMesh().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    private static float[] generateCircle() {
        float[] vertices = new float[CIRCLE_VERTICES * 3];

        for (int i = 0; i < CIRCLE_VERTICES; i++) {
            double angle = ((double) i / (double) CIRCLE_VERTICES) * Math.PI * 2;

            vertices[i * 3] = (float) Math.cos(angle);
            vertices[i * 3 + 1] = 0;
            vertices[i * 3 + 2] = (float) Math.sin(angle);
        }

        return vertices;
    }

}
