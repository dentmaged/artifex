package org.anchor.engine.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class AABB {

    protected float x1, x2, y1, y2, z1, z2;

    protected float centerX, centerY, centerZ;
    protected float width, height, length, furthest;

    public AABB(Vector3f one, Vector3f two) {
        this(one.x, two.x, one.y, two.y, one.z, two.z);
    }

    public AABB(float x1, float x2, float y1, float y2, float z1, float z2) {
        this.x1 = Math.min(x1, x2);
        this.x2 = Math.max(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.y2 = Math.max(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.z2 = Math.max(z1, z2);

        centerX = (x1 + x2) / 2;
        centerY = (y1 + y2) / 2;
        centerZ = (z1 + z2) / 2;

        width = 2 * (x2 - centerX);
        height = 2 * (y2 - centerY);
        length = 2 * (z2 - centerZ);

        furthest = Mathf.sqrt(width * width + height * height + length * length);
    }

    public float getX1() {
        return x1;
    }

    public float getX2() {
        return x2;
    }

    public float getY1() {
        return y1;
    }

    public float getY2() {
        return y2;
    }

    public float getZ1() {
        return z1;
    }

    public float getZ2() {
        return z2;
    }

    public boolean inside(Vector3f point) {
        return point.x >= x1 && point.x <= x2 && point.y >= y1 && point.y <= y2 && point.z >= z1 && point.z <= z2;
    }

    public boolean inside(float x, float y, float z) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getCenterZ() {
        return centerZ;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getLength() {
        return length;
    }

    public float getFurthest() {
        return furthest;
    }

    public boolean collides(AABB other) {
        if (other == null)
            return false;

        return (x1 <= other.x2 && x2 >= other.x1) && (y1 <= other.y2 && y2 >= other.y1) && (z1 <= other.z2 && z2 >= other.z1);
    }

    public List<Vector3f> getCorners() {
        List<Vector3f> corners = new ArrayList<Vector3f>();
        corners.add(new Vector3f(x2, y1, z1));
        corners.add(new Vector3f(x2, y1, z2));
        corners.add(new Vector3f(x1, y1, z2));
        corners.add(new Vector3f(x1, y1, z1));

        corners.add(new Vector3f(x2, y2, z1));
        corners.add(new Vector3f(x2, y2, z2));
        corners.add(new Vector3f(x1, y2, z2));
        corners.add(new Vector3f(x1, y2, z1));

        return corners;
    }

    public float[] getCornersArray() {
        float[] corners = new float[24];

        corners[0] = x2;
        corners[1] = y1;
        corners[2] = z1;

        corners[3] = x2;
        corners[4] = y1;
        corners[5] = z2;

        corners[6] = x1;
        corners[7] = y1;
        corners[8] = z2;

        corners[9] = x1;
        corners[10] = y1;
        corners[11] = z1;

        corners[12] = x2;
        corners[13] = y2;
        corners[14] = z1;

        corners[15] = x2;
        corners[16] = y2;
        corners[17] = z2;

        corners[18] = x1;
        corners[19] = y2;
        corners[20] = z2;

        corners[21] = x1;
        corners[22] = y2;
        corners[23] = z1;

        return corners;
    }

    public static AABB generateAABB(float[] vertices) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;

        // JAVA BUG: Float.MIN_VALUE is sometimes wrong when comparing
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < vertices.length; i += 3) {
            minX = Math.min(minX, vertices[i + 0]);
            minY = Math.min(minY, vertices[i + 1]);
            minZ = Math.min(minZ, vertices[i + 2]);

            maxX = Math.max(maxX, vertices[i + 0]);
            maxY = Math.max(maxY, vertices[i + 1]);
            maxZ = Math.max(maxZ, vertices[i + 2]);
        }

        return new AABB(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public static AABB generateAABB(float[] vertices, float margin) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;

        // JAVA BUG: Float.MIN_VALUE is sometimes wrong when comparing
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < vertices.length; i += 3) {
            minX = Math.min(minX, vertices[i + 0]);
            minY = Math.min(minY, vertices[i + 1]);
            minZ = Math.min(minZ, vertices[i + 2]);

            maxX = Math.max(maxX, vertices[i + 0]);
            maxY = Math.max(maxY, vertices[i + 1]);
            maxZ = Math.max(maxZ, vertices[i + 2]);
        }

        if (minX == maxX) {
            minX -= margin;
            maxX += margin;
        }

        if (minY == maxY) {
            minY -= margin;
            maxY += margin;
        }

        if (minZ == maxZ) {
            minZ -= margin;
            maxZ += margin;
        }

        return new AABB(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public static AABB generateAABB(List<Vector3f> vertices) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;

        // JAVA BUG: Float.MIN_VALUE is sometimes wrong when comparing
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (Vector3f vertex : vertices) {
            minX = Math.min(minX, vertex.x);
            minY = Math.min(minY, vertex.y);
            minZ = Math.min(minZ, vertex.z);

            maxX = Math.max(maxX, vertex.x);
            maxY = Math.max(maxY, vertex.y);
            maxZ = Math.max(maxZ, vertex.z);
        }

        return new AABB(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public static AABB generateAABB(AABB aabb, Matrix4f transformationMatrix) {
        List<Vector3f> corners = aabb.getCorners();
        for (Vector3f corner : corners)
            corner.set(Matrix4f.transform(transformationMatrix, new Vector4f(corner.x, corner.y, corner.z, 1), null));

        return generateAABB(corners);
    }

    public Vector3f raycast(Vector3f origin, Vector3f ray) {
        float distance = raycastDistance(origin, ray);
        if (distance == -1)
            return null;

        return Vector3f.add(origin, VectorUtils.mul(ray, distance), null);
    }

    public float raycastDistance(Vector3f origin, Vector3f ray) {
        ray.normalise();
        Vector3f dirfrac = new Vector3f(1f / ray.x, 1f / ray.y, 1f / ray.z);

        float t1 = (x1 - origin.x) * dirfrac.x;
        float t2 = (x2 - origin.x) * dirfrac.x;

        float t3 = (y1 - origin.y) * dirfrac.y;
        float t4 = (y2 - origin.y) * dirfrac.y;

        float t5 = (z1 - origin.z) * dirfrac.z;
        float t6 = (z2 - origin.z) * dirfrac.z;

        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0 || tmin > tmax)
            return -1;

        return tmin;
    }

}
