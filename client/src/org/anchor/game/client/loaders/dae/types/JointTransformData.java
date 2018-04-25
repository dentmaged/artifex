package org.anchor.game.client.loaders.dae.types;

import org.lwjgl.util.vector.Matrix4f;

public class JointTransformData {

    private String jointNameId;
    private Matrix4f localJointTransform;

    public JointTransformData(String jointNameId, Matrix4f localJointTransform) {
        this.jointNameId = jointNameId;
        this.localJointTransform = localJointTransform;
    }

    public String getJointNameId() {
        return jointNameId;
    }

    public Matrix4f getLocalJointTransform() {
        return localJointTransform;
    }

}
