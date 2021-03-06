package org.anchor.client.engine.renderer;

import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class QuadRenderer {

    private static Mesh mesh = Loader.getInstance().loadToVAO(new float[] { -1, 1, -1, -1, 1, 1, 1, -1 }, 2);

    public static void bind() {
        GL30.glBindVertexArray(mesh.getVAO());
        GL20.glEnableVertexAttribArray(0);
    }

    public static void render() {
        Renderer.triangleCount += 2;
        Renderer.drawCalls++;

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void unbind() {
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

}
