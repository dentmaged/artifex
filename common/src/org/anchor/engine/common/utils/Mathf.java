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

    public static float asin(float f) {
        return (float) Math.asin(f);
    }

    public static float sin(float f) {
        return (float) Math.sin(f);
    }

    public static float sinD(float f) {
        return (float) Math.sin(toRadians(f));
    }

    public static float acos(float f) {
        return (float) Math.acos(f);
    }

    public static float cos(float f) {
        return (float) Math.cos(f);
    }

    public static float cosD(float f) {
        return (float) Math.cos(toRadians(f));
    }

    public static float atan(float f) {
        return (float) Math.atan(f);
    }

    public static float tan(float f) {
        return (float) Math.tan(f);
    }

    public static float tanD(float f) {
        return (float) Math.tan((toRadians(f)));
    }

    public static float arcsin(float f) {
        return (float) Math.asin(f);
    }

    public static float arccos(float f) {
        return (float) Math.acos(f);
    }

    public static float arctan(float f) {
        return (float) Math.atan(f);
    }

    public static float pow(float a, float b) {
        return (float) Math.pow(a, b);
    }

    public static float clamp(float a, float b, float c) {
        if (b > a)
            return b;
        if (c < a)
            return c;

        return a;
    }

}
