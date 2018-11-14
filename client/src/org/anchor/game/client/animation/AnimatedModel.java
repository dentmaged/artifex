package org.anchor.game.client.animation;

import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.mesh.MeshRequest;
import org.lwjgl.util.vector.Matrix4f;

public class AnimatedModel extends Model {

    private Joint root;
    private int jointCount;
    private Animator animator;

    public AnimatedModel(MeshRequest mesh, Joint root, int jointCount) {
        super(mesh);

        this.root = root;
        this.jointCount = jointCount;
        this.animator = new Animator(this);

        root.calcInverseBindTransform(new Matrix4f());
    }

    public Joint getRoot() {
        return root;
    }

    public int getJointCount() {
        return jointCount;
    }

    public Animator getAnimator() {
        return animator;
    }

    public void update() {
        animator.update();
    }

    public void animate(Animation animation) {
        animator.animate(animation);
    }

    public Matrix4f[] getJointTransforms() {
        Matrix4f[] jointMatrices = new Matrix4f[jointCount];
        addJointsToArray(root, jointMatrices);

        return jointMatrices;
    }

    private void addJointsToArray(Joint headJoint, Matrix4f[] jointMatrices) {
        jointMatrices[headJoint.getIndex()] = headJoint.getAnimatedTransform();
        for (Joint childJoint : headJoint.getChildren())
            addJointsToArray(childJoint, jointMatrices);
    }

}
