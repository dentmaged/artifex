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
        this.normal.normalise();

        equation[0] = normal.x;
        equation[1] = normal.y;
        equation[2] = normal.z;
        equation[3] = -Vector3f.dot(normal, origin);
    }

    public boolean onSameSide(Vector3f point1, Vector3f point2) {
        float p1 = Vector3f.dot(normal, point1) + equation[3];
        float p2 = Vector3f.dot(normal, point2) + equation[3];

        return p1 * p2 > 0;
    }

    public boolean isFrontFacingTo(Vector3f direction) {
        return Vector3f.dot(normal, direction) <= 0;
    }

    public float signedDistanceTo(Vector3f point) {
        return Vector3f.dot(point, normal) + equation[3];
    }

    public boolean inTriangle(Vector3f point, Vector3f pa, Vector3f pb, Vector3f pc) {
        return sameSide(point, pa, pb, pc) && sameSide(point, pb, pa, pc) && sameSide(point, pc, pa, pb);
    }

    public boolean sameSide(Vector3f point, Vector3f pointPrime, Vector3f pa, Vector3f pb) {
        Vector3f pab = Vector3f.sub(pa, pb, null);
        pab.normalise();
        Plane p = new Plane(pa, Vector3f.cross(pab, normal, null));

        return p.onSameSide(point, pointPrime);
    }

    public Vector3f getNormal() {
        return normal;
    }

}
