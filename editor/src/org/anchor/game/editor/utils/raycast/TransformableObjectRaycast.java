package org.anchor.game.editor.utils.raycast;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.editor.TransformableObject;
import org.lwjgl.util.vector.Vector3f;

public class TransformableObjectRaycast {

    protected TransformableObject transformableObject;
    protected float distance;
    protected Vector3f point, direction;

    public TransformableObjectRaycast(TransformableObject transformableObject, float distance, Vector3f direction, Vector3f origin) {
        this.transformableObject = transformableObject;
        this.distance = distance;
        this.direction = direction;
        this.point = Vector3f.add(origin, VectorUtils.mul(direction, distance), null);
    }

    public TransformableObject getTransformableObject() {
        return transformableObject;
    }

    public float getDistance() {
        return distance;
    }

    public Vector3f getPoint() {
        return point;
    }

    public Vector3f getDirection() {
        return direction;
    }

}
