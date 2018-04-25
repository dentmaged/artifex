package org.anchor.game.client.animation;

import org.anchor.client.engine.renderer.types.MeshRequest;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.ModelTexture;
import org.anchor.client.engine.renderer.types.TextureRequest;
import org.lwjgl.util.vector.Matrix4f;

public class AnimatedModel extends Model {

    private Joint root;
    private int jointCount;
    private Animator animator;

    public AnimatedModel(MeshRequest mesh, TextureRequest texture, Joint root, int jointCount) {
        this(mesh, new ModelTexture(texture), root, jointCount);
    }

    public AnimatedModel(MeshRequest mesh, ModelTexture texture, Joint root, int jointCount) {
        super(mesh, texture);

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
