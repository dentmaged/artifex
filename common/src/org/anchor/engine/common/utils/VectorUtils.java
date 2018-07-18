package org.anchor.engine.common.utils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class VectorUtils {

    public static Vector3f stringToVector(String input) {
        String[] split = input.split(",");

        return new Vector3f(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()), Float.parseFloat(split[2].trim()));
    }

    public static Vector4f stringToFourVector(String input) {
        String[] split = input.split(",");

        return new Vector4f(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()), Float.parseFloat(split[2].trim()), Float.parseFloat(split[3].trim()));
    }

    public static float horizontalLength(Vector3f vector) {
        return (float) Math.sqrt(vector.x * vector.x + vector.z * vector.z);
    }

    public static Vector3f mul(Vector3f v, Vector3f b) {
        return new Vector3f(v.x * b.x, v.y * b.y, v.z * b.z);
    }

    public static Vector3f mul(Vector3f v, float f) {
        return new Vector3f(v.x * f, v.y * f, v.z * f);
    }

    public static Vector2f mul(Vector2f v, float f) {
        return new Vector2f(v.x * f, v.y * f);
    }

    public static Vector3f div(Vector3f v, Vector3f b) {
        return new Vector3f(v.x / b.x, v.y / b.y, v.z / b.z);
    }

    public static Vector3f div(Vector3f v, float f) {
        return new Vector3f(v.x / f, v.y / f, v.z / f);
    }

    public static Vector3f floor(Vector3f v) {
        return new Vector3f(Mathf.floor(v.x), Mathf.floor(v.y), Mathf.floor(v.z));
    }

    public static boolean anyZero(Vector3f v) {
        return v.x == 0 || v.y == 0 || v.z == 0;
    }

    public static void set(Matrix4f a, Matrix4f b) {
        a.m00 = b.m00;
        a.m01 = b.m01;
        a.m02 = b.m02;
        a.m03 = b.m03;

        a.m10 = b.m10;
        a.m11 = b.m11;
        a.m12 = b.m12;
        a.m13 = b.m13;

        a.m20 = b.m20;
        a.m21 = b.m21;
        a.m22 = b.m22;
        a.m23 = b.m23;

        a.m30 = b.m30;
        a.m31 = b.m31;
        a.m32 = b.m32;
        a.m33 = b.m33;
    }

}
