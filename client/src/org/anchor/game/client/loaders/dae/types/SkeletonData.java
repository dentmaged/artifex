package org.anchor.game.client.loaders.dae.types;

public class SkeletonData {

    private int jointCount;
    private JointData headJoint;

    public SkeletonData(int jointCount, JointData headJoint) {
        this.jointCount = jointCount;
        this.headJoint = headJoint;
    }

    public int getJointCount() {
        return jointCount;
    }

    public JointData getHeadJoint() {
        return headJoint;
    }

}
