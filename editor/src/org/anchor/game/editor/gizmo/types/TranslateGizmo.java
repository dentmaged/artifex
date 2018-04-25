package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.common.utils.Raycast;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.lwjgl.util.vector.Vector3f;

public class TranslateGizmo extends ArrowGizmo {

    private static float[] vertices = new float[] {
            0, 0, 0, 0, 0, -1, 0, 0.1f, -0.9f, 0, 0, -1, 0, -0.1f, -0.9f, 0, 0, -1, 0.1f, 0, -0.9f, 0, 0, -1, -0.1f, 0, -0.9f, 0, 0, -1
    };

    public TranslateGizmo() {
        super(vertices);
    }

    @Override
    public Vector3f getVector(Entity entity) {
        return entity.getPosition();
    }

    @Override
    public Vector3f performMove(Vector3f axis, Vector3f original, Vector3f position, Vector3f origin, Vector3f ray, int mouseDX, int mouseDY) {
        Vector3f planeTangent = Vector3f.cross(axis, Vector3f.sub(position, origin, null), null);
        Vector3f planeNormal = Vector3f.cross(axis, planeTangent, null);
        Vector3f p = Raycast.intersectionPlane(planeNormal, original, origin, ray);

        if (p != null)
            return VectorUtils.mul(axis, Vector3f.dot(Vector3f.sub(p, original, null), axis));
        return new Vector3f();
    }

}
