package org.anchor.engine.shared.utils;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Maths {

    public static Matrix4f createViewMatrix(Matrix4f target, Entity player, LivingComponent component) {
        target.setIdentity();

        Matrix4f.rotate((float) Math.toRadians(component.roll), new Vector3f(0, 0, 1), target, target);
        Matrix4f.rotate((float) Math.toRadians(component.pitch), new Vector3f(1, 0, 0), target, target);
        Matrix4f.rotate((float) Math.toRadians(component.yaw), new Vector3f(0, 1, 0), target, target);
        Matrix4f.translate(VectorUtils.mul(component.getEyePosition(), -1), target, target);

        return target;
    }

}
