package org.anchor.engine.common.utils;

import org.lwjgl.util.vector.Vector3f;

public class Raycast {

    public static float intersectionPlaneDistance(Vector3f normal, Vector3f position, Vector3f origin, Vector3f direction) {
        direction = new Vector3f(direction);
        direction.normalise();

        float denom = Vector3f.dot(direction, normal);
        float d = -Vector3f.dot(normal, position);

        if (Math.abs(denom) == 0)
            return -1;

        return -(Vector3f.dot(origin, normal) + d) / denom;
    }

    public static Vector3f intersectionPlane(Vector3f normal, Vector3f position, Vector3f origin, Vector3f direction) {
        float t = intersectionPlaneDistance(normal, position, origin, direction);
        if (t >= 0)
            return Vector3f.add(origin, VectorUtils.mul(direction, t), null);

        return null;
    }

    public static boolean intersectionCircle(Vector3f normal, Vector3f position, Vector3f origin, Vector3f direction, float radius) {
        float t = intersectionPlaneDistance(normal, position, origin, direction);

        if (t >= 0) {
            Vector3f p = Vector3f.add(origin, VectorUtils.mul(direction, t), null);
            Vector3f v = Vector3f.sub(p, position, null);

            return Math.abs(v.lengthSquared() - radius * radius) < 0.1f * radius;
        }

        return false;
    }

    public static boolean intersectionDisc(Vector3f normal, Vector3f position, Vector3f origin, Vector3f direction, float radius) {
        float t = intersectionPlaneDistance(normal, position, origin, direction);

        if (t >= 0) {
            Vector3f p = Vector3f.add(origin, VectorUtils.mul(direction, t), null);
            Vector3f v = Vector3f.sub(p, position, null);

            return v.lengthSquared() <= radius * radius;
        }

        return false;
    }

}
