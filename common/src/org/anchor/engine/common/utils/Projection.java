package org.anchor.engine.common.utils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Projection {

    public static Vector2f projectPoint(Vector3f pointX, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        Vector3f point = new Vector3f(pointX.x, pointX.y, pointX.z);
        Vector4f point4 = new Vector4f(point.x, point.y, point.z, 1);

        Matrix4f.transform(viewMatrix, point4, point4);
        Matrix4f.transform(projectionMatrix, point4, point4);

        point = new Vector3f(point4);
        point.x /= point4.w;
        point.y /= point4.w;

        point.x = (point.x + 1) / 2;
        point.y = (point.y + 1) / 2;

        return new Vector2f(point);
    }

    public static Vector2f projectDirection(Vector3f pointX, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        Vector3f point = new Vector3f(pointX.x, pointX.y, pointX.z);
        Vector4f point4 = new Vector4f(point.x, point.y, point.z, 0);

        Matrix4f.transform(viewMatrix, point4, point4);
        Matrix4f.transform(projectionMatrix, point4, point4);

        point = new Vector3f(point4);
        point.x /= point4.w;
        point.y /= point4.w;

        point.x = (point.x + 1) / 2;
        point.y = (point.y + 1) / 2;

        return new Vector2f(point);
    }

}
