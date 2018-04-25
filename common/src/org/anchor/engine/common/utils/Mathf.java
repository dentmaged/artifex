package org.anchor.engine.common.utils;

public class Mathf {

    public static float floor(float f) {
        return (float) Math.floor(f);
    }

    public static float ceil(float f) {
        return (float) Math.ceil(f);
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static float toRadians(float f) {
        return (float) Math.toRadians(f);
    }

    public static float toDegrees(float f) {
        return (float) Math.toDegrees(f);
    }

    public static float sqrt(float f) {
        return (float) Math.sqrt(f);
    }

    public static float abs(float f) {
        return Math.abs(f);
    }

    public static float roundTo(float f, float precision) {
        return floor(f / precision) * precision;
    }

}
