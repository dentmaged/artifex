package org.anchor.game.editor.gizmo.types;

public class SelectGizmo extends ArrowGizmo {

    private static float[] vertices = new float[] {
            0, 0, 0, 0, 0, -1
    };

    public SelectGizmo() {
        super(vertices);
    }

}
