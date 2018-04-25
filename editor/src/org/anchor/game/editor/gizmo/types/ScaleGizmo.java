package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.shared.entity.Entity;
import org.lwjgl.util.vector.Vector3f;

public class ScaleGizmo extends ArrowGizmo {

    private static float[] vertices = new float[] {
            0, 0, 0, 0, 0, -1, 0, 0.1f, -1, 0, -0.1f, -1, 0.1f, 0, -1, -0.1f, 0, -1
    };

    public ScaleGizmo() {
        super(vertices);
    }

    @Override
    public Vector3f getVector(Entity entity) {
        return entity.getScale();
    }

}
