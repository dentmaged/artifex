package org.anchor.game.client.animation;

import org.lwjgl.util.vector.Matrix4f;

public class Quaternion {

    private float x, y, z, w;

    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        normalise();
    }

    public void normalise() {
        float mag = (float) Math.sqrt(w * w + x * x + y * y + z * z);
        w /= mag;
        x /= mag;
        y /= mag;
        z /= mag;
    }

    public Matrix4f toRotationMatrix() {
        Matrix4f matrix = new Matrix4f();

        final float xy = x * y;
        final float xz = x * z;
        final float xw = x * w;

        final float yz = y * z;
        final float yw = y * w;
        final float zw = z * w;

        final float xSquared = x * x;
        final float ySquared = y * y;
        final float zSquared = z * z;

        matrix.m00 = 1 - 2 * (ySquared + zSquared);
        matrix.m01 = 2 * (xy - zw);
        matrix.m02 = 2 * (xz + yw);
        matrix.m03 = 0;

        matrix.m10 = 2 * (xy + zw);
        matrix.m11 = 1 - 2 * (xSquared + zSquared);
        matrix.m12 = 2 * (yz - xw);
        matrix.m13 = 0;

        matrix.m20 = 2 * (xz - yw);
        matrix.m21 = 2 * (yz + xw);
        matrix.m22 = 1 - 2 * (xSquared + ySquared);
        matrix.m23 = 0;

        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        matrix.m33 = 1;

        return matrix;
    }

    public static Quaternion fromMatrix(Matrix4f matrix) {
        float w, x, y, z;
        float diagonal = matrix.m00 + matrix.m11 + matrix.m22;
        if (diagonal > 0) {
            float w4 = (float) (Math.sqrt(diagonal + 1f) * 2f);
            w = w4 / 4f;
            x = (matrix.m21 - matrix.m12) / w4;
            y = (matrix.m02 - matrix.m20) / w4;
            z = (matrix.m10 - matrix.m01) / w4;
        } else if ((matrix.m00 > matrix.m11) && (matrix.m00 > matrix.m22)) {
            float x4 = (float) (Math.sqrt(1f + matrix.m00 - matrix.m11 - matrix.m22) * 2f);
            w = (matrix.m21 - matrix.m12) / x4;
            x = x4 / 4f;
            y = (matrix.m01 + matrix.m10) / x4;
            z = (matrix.m02 + matrix.m20) / x4;
        } else if (matrix.m11 > matrix.m22) {
            float y4 = (float) (Math.sqrt(1f + matrix.m11 - matrix.m00 - matrix.m22) * 2f);
            w = (matrix.m02 - matrix.m20) / y4;
            x = (matrix.m01 + matrix.m10) / y4;
            y = y4 / 4f;
            z = (matrix.m12 + matrix.m21) / y4;
        } else {
            float z4 = (float) (Math.sqrt(1f + matrix.m22 - matrix.m00 - matrix.m11) * 2f);
            w = (matrix.m10 - matrix.m01) / z4;
            x = (matrix.m02 + matrix.m20) / z4;
            y = (matrix.m12 + matrix.m21) / z4;
            z = z4 / 4f;
        }

        return new Quaternion(x, y, z, w);
    }

    public static Quaternion interpolate(Quaternion a, Quaternion b, float blend) {
        Quaternion result = new Quaternion(0, 0, 0, 1);
        float dot = a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;
        float blendI = 1f - blend;
        if (dot < 0) {
            result.w = blendI * a.w + blend * -b.w;
            result.x = blendI * a.x + blend * -b.x;
            result.y = blendI * a.y + blend * -b.y;
            result.z = blendI * a.z + blend * -b.z;
        } else {
            result.w = blendI * a.w + blend * b.w;
            result.x = blendI * a.x + blend * b.x;
            result.y = blendI * a.y + blend * b.y;
            result.z = blendI * a.z + blend * b.z;
        }
        result.normalise();

        return result;
    }

}
