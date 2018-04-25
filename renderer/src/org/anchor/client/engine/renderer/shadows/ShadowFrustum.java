package org.anchor.client.engine.renderer.shadows;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.types.Light;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class ShadowFrustum {

    private static final Vector4f UP = new Vector4f(0, 1, 0, 0);
    private static final Vector4f FORWARD = new Vector4f(0, 0, -1, 0);
    public static final float SHADOW_DISTANCE = 50;

    public static final float WORLD_UNITS_PER_TEXEL = SHADOW_DISTANCE * 2f / (float) Shadows.SHADOW_MAP_SIZE;

    private float minX, maxX;
    private float minY, maxY;
    private float minZ, maxZ;

    private Light sun;
    private Matrix4f lightViewMatrix;

    private float farHeight, farWidth, nearHeight, nearWidth;

    public ShadowFrustum(Light sun, Matrix4f lightViewMatrix) {
        this.sun = sun;
        this.lightViewMatrix = lightViewMatrix;

        float tanFOV = (float) Math.tan(Math.toRadians(Renderer.FOV / 2));
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();

        farHeight = tanFOV * SHADOW_DISTANCE;
        nearHeight = tanFOV * Renderer.NEAR_PLANE;

        farWidth = farHeight * aspectRatio;
        nearWidth = nearHeight * aspectRatio;
    }

    public void update(Vector3f position, float pitch, float yaw) {
        Matrix4f rotation = new Matrix4f();
        Vector3f direction = new Vector3f(sun.getPosition());
        direction.negate();
        if (sun.getPosition().lengthSquared() > 0)
            direction.normalise();

        Matrix4f.rotate((float) Math.toRadians(-pitch), new Vector3f(1, 0, 0), rotation, rotation);
        Matrix4f.rotate((float) Math.toRadians(-yaw), new Vector3f(0, 1, 0), rotation, rotation);
        Vector3f forwardVector = new Vector3f(Matrix4f.transform(rotation, FORWARD, null));

        Vector3f toFar = new Vector3f(forwardVector);
        Vector3f toNear = new Vector3f(forwardVector);

        toFar.scale(SHADOW_DISTANCE);
        toNear.scale(Renderer.NEAR_PLANE);

        Vector3f centerNear = Vector3f.add(toNear, position, null);
        Vector3f centerFar = Vector3f.add(toFar, position, null);

        Vector4f[] points = calculateFrustumVertices(rotation, forwardVector, centerNear, centerFar);

        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        minZ = Float.MAX_VALUE;

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

        minX = Mathf.roundTo(minX, WORLD_UNITS_PER_TEXEL);
        minY = Mathf.roundTo(minY, WORLD_UNITS_PER_TEXEL);

        maxX = Mathf.roundTo(maxX, WORLD_UNITS_PER_TEXEL);
        maxY = Mathf.roundTo(maxY, WORLD_UNITS_PER_TEXEL);
        maxZ += 18;
    }

    public Vector3f getCenter() {
        float x = (minX + maxX) / 2f;
        float y = (minY + maxY) / 2f;
        float z = (minZ + maxZ) / 2f;

        Matrix4f invertedLight = new Matrix4f();
        Matrix4f.invert(lightViewMatrix, invertedLight);

        return new Vector3f(Matrix4f.transform(invertedLight, new Vector4f(x, y, z, 1), null));
    }

    public void setSun(Light sun) {
        this.sun = sun;
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
