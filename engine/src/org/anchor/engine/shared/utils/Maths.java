package org.anchor.engine.shared.utils;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Maths {

    public static final float pi = 3.141592653589793238462643383279502f;

    public static Quaternion toQuaternion(Vector3f rotation) {
        Quaternion result = new Quaternion();

        rotation.x %= 360;
        rotation.y %= 360;
        rotation.z %= 360;

        float x = rotation.x * pi / 360;
        float y = rotation.y * pi / 360;
        float z = rotation.z * pi / 360;

        float c1 = (float) Math.cos(x);
        float c2 = (float) Math.cos(y);
        float c3 = (float) Math.cos(z);

        float s1 = (float) Math.sin(x);
        float s2 = (float) Math.sin(y);
        float s3 = (float) Math.sin(z);

        result.x = s1 * c2 * c3 + c1 * s2 * s3;
        result.y = s1 * s2 * c3 + c1 * c2 * s3;
        result.z = c1 * s2 * c3 - s1 * c2 * s3;
        result.w = c1 * c2 * c3 - s1 * s2 * s3;

        result.x = Math.round(result.x * 100000) / 100000f;
        result.y = Math.round(result.y * 100000) / 100000f;
        result.z = Math.round(result.z * 100000) / 100000f;
        result.w = Math.round(result.w * 100000) / 100000f;

        return result;
    }

    public static Vector3f toEuler(Quaternion rotation) {
        Vector3f result = new Vector3f();

        double sinr = 2.0 * (rotation.w * rotation.x + rotation.y * rotation.z);
        double cosr = 1.0 - 2.0 * (rotation.x * rotation.x + rotation.y * rotation.y);
        result.x = (float) Math.atan2(sinr, cosr);

        double siny = 2.0 * (rotation.w * rotation.z + rotation.x * rotation.y);
        double cosy = 1.0 - 2.0 * (rotation.y * rotation.y + rotation.z * rotation.z);
        result.y = (float) Math.atan2(siny, cosy);

        float sinp = 2.0f * (rotation.w * rotation.y - rotation.z * rotation.x);
        if (sinp > 1)
            sinp = 1;
        else if (sinp < -1)
            sinp = -1;
        result.z = (float) Math.asin(sinp);

        result.x *= 180 / pi;
        result.y *= 180 / pi;
        result.z *= 180 / pi;

        result.x %= 360;
        result.y %= 360;
        result.z %= 360;

        if (result.y < 0)
            result.y += 360;

        result.x = Math.round(result.x * 100000) / 100000f;
        result.y = Math.round(result.y * 100000) / 100000f;
        result.z = Math.round(result.z * 100000) / 100000f;

        return result;
    }

    public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale, float rx, float ry) {
        Matrix4f matrix = new Matrix4f();

        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
        Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);

        return matrix;
    }

    public static Matrix4f createTransformationMatrix(Vector3f translation, Vector3f rotation, Vector3f scale) {
        Matrix4f matrix = new Matrix4f();

        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rotation.z), new Vector3f(0, 0, 1), matrix, matrix);
        Matrix4f.scale(scale, matrix, matrix);

        return matrix;
    }

    public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;

        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static Matrix4f createViewMatrix(Matrix4f target, Entity player, LivingComponent component) {
        target.setIdentity();

        Matrix4f.rotate((float) Math.toRadians(component.pitch), new Vector3f(1, 0, 0), target, target);
        Matrix4f.rotate((float) Math.toRadians(player.getRotation().y), new Vector3f(0, 1, 0), target, target);
        Matrix4f.rotate((float) Math.toRadians(player.getRotation().z), new Vector3f(0, 0, 1), target, target);
        Matrix4f.translate(VectorUtils.mul(component.getEyePosition(), -1), target, target);

        return target;
    }

    public static Vector3f ceil3f(Vector3f vector) {
        Vector3f dest = new Vector3f();

        dest.x = (float) Math.ceil((double) vector.x);
        dest.y = (float) Math.ceil((double) vector.y);
        dest.z = (float) Math.ceil((double) vector.z);

        return dest;
    }

    public static float getAngleDifference(Vector3f viewer, Vector3f target, float yaw) {
        double deltaX = target.x - viewer.x;
        double deltaZ = target.z - viewer.z;

        float yawToPlayer;
        if ((deltaZ < 0.0D) && (deltaX < 0.0D)) {
            yawToPlayer = (float) (90.0D + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        } else {
            if ((deltaZ < 0.0D) && (deltaX > 0.0D))
                yawToPlayer = (float) (-90.0D + Math.toDegrees(Math.atan(deltaZ / deltaX)));
            else {
                yawToPlayer = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ));
            }
        }
        float angle = -(yaw - yawToPlayer) % 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    public static Vector3f reflect(Vector3f direction, Vector3f normal) {
        float dotProduct = Vector3f.dot(direction, normal);

        return new Vector3f(direction.x - 2 * dotProduct * normal.x, direction.y - 2 * dotProduct * normal.y, direction.z - 2 * dotProduct * normal.z);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f center) {
        Vector3f f = Vector3f.sub(center, eye, null);
        Vector3f s = Vector3f.cross(f, new Vector3f(0, 1, 0), null);
        Vector3f u = Vector3f.cross(s, f, null);

        f.normalise();
        s.normalise();

        Matrix4f matrix = new Matrix4f();
        matrix.m00 = s.x;
        matrix.m10 = s.y;
        matrix.m20 = s.z;
        matrix.m01 = u.x;
        matrix.m11 = u.y;
        matrix.m21 = u.z;
        matrix.m02 = -f.x;
        matrix.m12 = -f.y;
        matrix.m22 = -f.z;
        matrix.m30 = -Vector3f.dot(s, eye);
        matrix.m31 = -Vector3f.dot(u, eye);
        matrix.m32 = Vector3f.dot(f, eye);

        return matrix;
    }

}
