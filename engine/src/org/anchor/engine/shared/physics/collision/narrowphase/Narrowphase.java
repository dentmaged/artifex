package org.anchor.engine.shared.physics.collision.narrowphase;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.physics.collision.broadphase.BroadphaseCollisionResult;
import org.lwjgl.util.vector.Vector3f;

public class Narrowphase {

    public static NarrowphaseCollisionResult collision(BroadphaseCollisionResult result) {
        PhysicsComponent primary = result.getOne().getComponent(PhysicsComponent.class);
        PhysicsComponent secondary = result.getTwo().getComponent(PhysicsComponent.class);

        Vector3f[] shapePrimary = convert(primary.getVertices());
        Vector3f[] shapeSecondary = convert(secondary.getVertices());
        Vector3f[] normals = cull(combine(convert(primary.getNormals()), convert(secondary.getNormals())));

        float overlap = Float.MAX_VALUE;
        Vector3f mtv = new Vector3f();

        for (int i = 0; i < normals.length; i++) {
            if (!projectionOverlap(normals[i], shapePrimary, shapeSecondary)) {
                return new NarrowphaseCollisionResult(false, result.getOne(), result.getTwo(), null, 0);
            } else {
                Vector3f normal = normals[i];

                float o = overlap(projectOnPlane(normal, shapePrimary), projectOnPlane(normal, shapeSecondary));
                if (o < overlap) {
                    overlap = o;
                    mtv.set(normal);
                }
            }
        }

        return new NarrowphaseCollisionResult(true, result.getOne(), result.getTwo(), mtv, overlap);
    }

    private static float[] projectOnPlane(Vector3f plane, Vector3f[] verts) {
        float max = Vector3f.dot(plane, verts[0]);
        float min = Vector3f.dot(plane, verts[0]);

        for (int i = 1; i < verts.length; i++) {
            float val = Vector3f.dot(plane, verts[i]);
            if (val > max)
                max = val;

            if (val < min)
                min = val;
        }

        return new float[] {
                min, max
        };
    }

    private static boolean lineOverlap(float[] line1, float[] line2) {
        return !(line1[0] > line2[1] || line1[1] < line2[0]);
    }

    private static boolean projectionOverlap(Vector3f normal, Vector3f[] primary, Vector3f[] secondary) {
        return lineOverlap(projectOnPlane(normal, primary), projectOnPlane(normal, secondary));
    }

    private static float overlap(float[] line1, float[] line2) {
        if (line1[1] > line2[0])
            return line1[1] - line2[0];
        else if (line1[0] > line2[1])
            return line1[0] - line2[1];

        return 0;
    }

    public static Vector3f[] convert(List<Vector3f> list) {
        Vector3f[] array = new Vector3f[list.size()];
        for (int i = 0; i < list.size(); i++)
            array[i] = list.get(i);

        return array;
    }

    public static Vector3f[] combine(Vector3f[] a, Vector3f[] b) {
        Vector3f[] total = new Vector3f[a.length + b.length];
        for (int i = 0; i < a.length; i++)
            total[i] = a[i];

        for (int i = 0; i < b.length; i++)
            total[i + a.length] = b[i];

        return total;
    }

    public static Vector3f[] cull(Vector3f[] list) {
        List<Vector3f> repeatless = new ArrayList<Vector3f>();
        for (Vector3f value : list)
            if (!repeatless.contains(value))
                repeatless.add(value);

        return convert(repeatless);
    }

}
