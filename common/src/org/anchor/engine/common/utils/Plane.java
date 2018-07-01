package org.anchor.engine.common.utils;

import org.lwjgl.util.vector.Vector3f;

public class Plane {

    float[] equation = new float[4];
    Vector3f normal, origin;

    public Plane(Vector3f point, Vector3f normal) {
        this.origin = point;
        this.normal = normal;
        this.normal.normalise();

        equation[0] = normal.x;
        equation[1] = normal.y;
        equation[2] = normal.z;
        equation[3] = -Vector3f.dot(normal, origin);
    }

    public Plane(Vector3f p1, Vector3f p2, Vector3f p3) {
        this.origin = p1;
        this.normal = Vector3f.cross(Vector3f.sub(p2, p1, null), Vector3f.sub(p3, p1, null), null);
        if (normal.lengthSquared() > 0)
            this.normal.normalise();

        equation[0] = normal.x;
        equation[1] = normal.y;
        equation[2] = normal.z;
        equation[3] = -Vector3f.dot(normal, origin);
    }

    public boolean isFrontFacingTo(Vector3f direction) {
        return Vector3f.dot(normal, direction) <= 0;
    }

    public float signedDistanceTo(Vector3f point) {
        return Vector3f.dot(point, normal) + equation[3];
    }

    public Vector3f getNormal() {
        return normal;
    }

    public boolean checkPointInTriangle(Vector3f point, Vector3f pa, Vector3f pb, Vector3f pc) {
        return sameSide(point, pa, pb, pc) && sameSide(point, pb, pa, pc) && sameSide(point, pc, pa, pb);
    }

    public boolean sameSide(Vector3f point, Vector3f pointPrime, Vector3f pa, Vector3f pb) {
        Plane p = new Plane(pa, Vector3f.cross((Vector3f) Vector3f.sub(pa, pb, null).normalise(), normal, null));

        return p.onSameSide(point, pointPrime);
    }

    public boolean onSameSide(Vector3f point1, Vector3f point2) {
        float p1 = equation[0] * point1.x + equation[1] * point1.y + equation[2] * point1.z + equation[3];
        float p2 = equation[0] * point2.x + equation[1] * point2.y + equation[2] * point2.z + equation[3];
        return p1 * p2 > 0;
    }

}
