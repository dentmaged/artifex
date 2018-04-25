package org.anchor.game.client.debug;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.Mesh;
import org.anchor.engine.shared.utils.Maths;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;

public class DebugRenderer {

    private static Mesh circle;
    private static DebugShader shader = DebugShader.getInstance();

    private static int CIRCLE_VERTICES = 30;

    public static void circle(Vector3f position, Vector3f rotation) {
        circle(position, rotation, new Vector3f(1, 1, 1), new Vector3f(1, 0, 0));
    }

    public static void circle(Vector3f position, Vector3f rotation, Vector3f scale, Vector3f colour) {
        if (circle == null)
            circle = Loader.getInstance().loadToVAO(generateCircle(), 3);

        shader.start();
        shader.loadInformation(Maths.createTransformationMatrix(position, rotation, scale), colour);
        GL30.glBindVertexArray(circle.getVAO());
        GL20.glEnableVertexAttribArray(0);

        GL11.glDrawArrays(GL11.GL_LINE_LOOP, 0, circle.getVertexCount());

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
