package org.anchor.game.editor.gizmo.types;

import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.Raycast;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.utils.TransformationMode;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class TranslateGizmo extends ArrowGizmo {

    private static float[] vertices = new float[] { 0, 0, 0, 0, 0, -1, 0, 0.1f, -0.9f, 0, 0, -1, 0, -0.1f, -0.9f, 0, 0, -1, 0.1f, 0, -0.9f, 0, 0, -1, -0.1f, 0, -0.9f, 0, 0, -1 };

    public TranslateGizmo() {
        super(new ArrowPlane(), vertices);
    }

    @Override
    public Vector3f getVector(Entity entity) {
        return entity.getPosition();
    }

    @Override
    public Vector3f performMove(Vector3f axis, Vector3f original, Vector3f position, Vector3f rotation, Vector3f origin, Vector3f ray, int mouseDX, int mouseDY, Vector3f axisOffset) {
        float length = axis.lengthSquared();
        Vector3f planeNormal = Vector3f.sub(new Vector3f(1, 1, 1), axis, null);

        if (length == 1) {
            Vector3f planeTangent = Vector3f.cross(axis, Vector3f.sub(position, origin, null), null);
            planeNormal = Vector3f.cross(axis, planeTangent, null);
        }

        Vector3f p = Raycast.intersectionPlane(planeNormal, original, origin, ray);

        if (p != null)
            if (length == 1)
                return VectorUtils.mul(GameEditor.getInstance().getTransformationMode() == TransformationMode.LOCAL ? new Vector3f(Matrix4f.transform(CoreMaths.createTransformationMatrix(new Vector3f(), rotation, new Vector3f(1, 1, 1)), new Vector4f(axis.x, axis.y, axis.z, 0), null)) : axis, Vector3f.dot(Vector3f.sub(p, original, null), axis));
            else
                return VectorUtils.mul(Vector3f.sub(p, original, null), axis);
        return new Vector3f();
    }

}
