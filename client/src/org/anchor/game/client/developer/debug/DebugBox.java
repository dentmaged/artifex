package org.anchor.game.client.developer.debug;

import org.anchor.client.engine.renderer.debug.DebugRenderer;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class DebugBox implements DebugShape {

    private Vector3f position, rotation, scale, colour;

    public DebugBox(Vector3f position, Vector3f rotation, Vector3f scale, Vector3f colour) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.colour = colour;
    }

    @Override
    public void render(Matrix4f viewMatrix) {
        DebugRenderer.box(viewMatrix, position, rotation, scale, colour);
    }

}
