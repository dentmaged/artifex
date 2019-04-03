package org.anchor.client.engine.renderer.shadows;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class ShadowFrustum {

    private static final Vector4f UP = new Vector4f(0, 1, 0, 0);
    private static final Vector4f FORWARD = new Vector4f(0, 0, -1, 0);

    private float worldUnitsPerTexel;

    private float minX, maxX;
    private float minY, maxY;
    private float minZ, maxZ;
    private float near, far;

    private Matrix4f lightViewMatrix;

    private float farHeight, farWidth, nearHeight, nearWidth;

    public ShadowFrustum(Matrix4f lightViewMatrix, float extents) {
        this.lightViewMatrix = lightViewMatrix;

        this.near = -extents;
        this.far = extents;
        this.worldUnitsPerTexel = extents * 2f / (float) Settings.shadowResolution;

        float tanFOV = Mathf.tan(Mathf.toRadians(Settings.fov / 2));
        float aspectRatio = (float) Settings.width / (float) Settings.height;

        farHeight = tanFOV * far;
        nearHeight = tanFOV * near;

        farWidth = farHeight * aspectRatio;
        nearWidth = nearHeight * aspectRatio;
    }

    public void update(Vector3f position, float pitch, float yaw) {
        Matrix4f rotation = new Matrix4f();

        Matrix4f.rotate((float) Math.toRadians(-pitch), new Vector3f(1, 0, 0), rotation, rotation);
        Matrix4f.rotate((float) Math.toRadians(-yaw), new Vector3f(0, 1, 0), rotation, rotation);
        Vector3f forwardVector = new Vector3f(Matrix4f.transform(rotation, FORWARD, null));

        Vector3f toFar = new Vector3f(forwardVector);
        Vector3f toNear = new Vector3f(forwardVector);

        toFar.scale(far);
        toNear.scale(near);

        Vector3f centerNear = Vector3f.add(toNear, position, null);
        Vector3f centerFar = Vector3f.add(toFar, position, null);

        Vector4f[] points = calculateFrustumVertices(rotation, forwardVector, centerNear, centerFar);

        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        minZ = Float.MAX_VALUE;

        // JAVA BUG: Float.MIN_VALUE is sometimes wrong when comparing
        maxX = -Float.MAX_VALUE;
        maxY = -Float.MAX_VALUE;
        maxZ = -Float.MAX_VALUE;

        for (Vector4f point : points) {
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            minZ = Math.min(minZ, point.z);

            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
            maxZ = Math.max(maxZ, point.z);
        }

        minX = Mathf.roundTo(minX, worldUnitsPerTexel);
        minY = Mathf.roundTo(minY, worldUnitsPerTexel);
        minZ = Mathf.roundTo(minZ, worldUnitsPerTexel);

        maxX = Mathf.roundTo(maxX, worldUnitsPerTexel);
        maxY = Mathf.roundTo(maxY, worldUnitsPerTexel);
        maxZ = Mathf.roundTo(maxZ, worldUnitsPerTexel) + 18;

        // System.out.println(minX + " " + minY + " " + minZ + " " + maxX + " " + maxY + " " + maxZ);
    }

    public Vector3f getCenter() {
        float x = (minX + maxX) / 2f;
        float y = (minY + maxY) / 2f;
        float z = (minZ + maxZ) / 2f;

        Matrix4f invertedLight = new Matrix4f();
        Matrix4f.invert(lightViewMatrix, invertedLight);

        return new Vector3f(Matrix4f.transform(invertedLight, new Vector4f(x, y, z, 1), null));
    }

    public float getWidth() {
        return maxX - minX;
    }

    public float getHeight() {
        return maxY - minY;
    }

    public float getLength() {
        return maxZ - minZ;
    }

    public float getFarPlane() {
        return far;
    }

    private Vector4f[] calculateFrustumVertices(Matrix4f rotation, Vector3f forwardVector, Vector3f centerNear, Vector3f centerFar) {
        Vector3f upVector = new Vector3f(Matrix4f.transform(rotation, UP, null));
        Vector3f downVector = new Vector3f(-upVector.x, -upVector.y, -upVector.z);

        Vector3f rightVector = Vector3f.cross(forwardVector, upVector, null);
        Vector3f leftVector = new Vector3f(-rightVector.x, -rightVector.y, -rightVector.z);

        Vector3f farTop = Vector3f.add(centerFar, new Vector3f(upVector.x * farHeight, upVector.y * farHeight, upVector.z * farHeight), null);
        Vector3f farBottom = Vector3f.add(centerFar, new Vector3f(downVector.x * farHeight, downVector.y * farHeight, downVector.z * farHeight), null);

        Vector3f nearTop = Vector3f.add(centerNear, new Vector3f(upVector.x * nearHeight, upVector.y * nearHeight, upVector.z * nearHeight), null);
        Vector3f nearBottom = Vector3f.add(centerNear, new Vector3f(downVector.x * nearHeight, downVector.y * nearHeight, downVector.z * nearHeight), null);
        Vector4f[] points = new Vector4f[8];

        points[0] = calculateLightSpaceFrustumCorner(farTop, rightVector, farWidth);
        points[1] = calculateLightSpaceFrustumCorner(farTop, leftVector, farWidth);

        points[2] = calculateLightSpaceFrustumCorner(farBottom, rightVector, farWidth);
        points[3] = calculateLightSpaceFrustumCorner(farBottom, leftVector, farWidth);

        points[4] = calculateLightSpaceFrustumCorner(nearTop, rightVector, nearWidth);
        points[5] = calculateLightSpaceFrustumCorner(nearTop, leftVector, nearWidth);

        points[6] = calculateLightSpaceFrustumCorner(nearBottom, rightVector, nearWidth);
        points[7] = calculateLightSpaceFrustumCorner(nearBottom, leftVector, nearWidth);

        return points;
    }

    private Vector4f calculateLightSpaceFrustumCorner(Vector3f startPoint, Vector3f direction, float width) {
        Vector3f point = Vector3f.add(startPoint, new Vector3f(direction.x * width, direction.y * width, direction.z * width), null);

        return Matrix4f.transform(lightViewMatrix, new Vector4f(point.x, point.y, point.z, 1), null);
    }

}
