package org.anchor.engine.common.utils;

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

    public static Vector3f mul(Vector3f v, float f) {
        return new Vector3f(v.x * f, v.y * f, v.z * f);
    }

    public static Vector3f floor(Vector3f v) {
        return new Vector3f(Mathf.floor(v.x), Mathf.floor(v.y), Mathf.floor(v.z));
    }

    public static boolean hasZero(Vector3f v) {
        return v.x == 0 || v.y == 0 || v.z == 0;
    }

}
