package org.anchor.game.editor.gizmo.types;

import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.gizmo.Plane;
import org.lwjgl.opengl.GL11;

public class ArrowPlane extends Plane {

    private static int CIRCLE_VERTICES = 30;
    private static float SIZE = 0.1f;
    private static float CENTRE = 0.75f;

    public ArrowPlane() {
        super(generateVertices(), getMode());
    }

    private static float[] generateVertices() {
        if (GameEditor.getInstance().isModo()) {
            float[] vertices = new float[CIRCLE_VERTICES * 3 + 6];

            vertices[0] = CENTRE;
            vertices[1] = CENTRE;
            vertices[2] = 0;

            for (int i = 0; i <= CIRCLE_VERTICES; i++) {
                double angle = ((double) i / (double) CIRCLE_VERTICES) * Math.PI * 2;
                int index = i * 3 + 3;

                vertices[index] = CENTRE + SIZE * (float) Math.cos(angle);
                vertices[index + 1] = CENTRE + SIZE * (float) Math.sin(angle);
                vertices[index + 2] = 0;
            }

            return vertices;
        } else {
            return new float[] { 0.5f, 0, 0, 0.5f, 0.5f, 0, 0, 0, 0, 0, 0, 0, 0.5f, 0.5f, 0, 0, 0.5f, 0 };
        }
    }

    private static int getMode() {
        if (GameEditor.getInstance().isModo())
            return GL11.GL_TRIANGLE_FAN;

        return GL11.GL_TRIANGLES;
    }

}
