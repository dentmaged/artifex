package org.anchor.game.client.animation;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;

public class Joint {

    private int index;
    private String name;
    private List<Joint> children = new ArrayList<Joint>();

    private Matrix4f animatedTransformation = new Matrix4f();
    private Matrix4f localBindTransformation;
    private Matrix4f inverseBindTransformation = new Matrix4f();

    public Joint(int index, String name, Matrix4f localBindTransformation) {
        this.index = index;
        this.name = name;
        this.localBindTransformation = localBindTransformation;
    }

    public void addChild(Joint child) {
        this.children.add(child);
    }

    public Matrix4f getAnimatedTransform() {
        return animatedTransformation;
    }

    public void setAnimationTransform(Matrix4f animatedTransformation) {
        this.animatedTransformation = animatedTransformation;
    }

    public Matrix4f getInverseBindTransform() {
        return inverseBindTransformation;
    }

    public void calcInverseBindTransform(Matrix4f parentBindTransform) {
        Matrix4f bindTransform = Matrix4f.mul(parentBindTransform, localBindTransformation, null);
        Matrix4f.invert(bindTransform, inverseBindTransformation);

        for (Joint child : children)
            child.calcInverseBindTransform(bindTransform);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public List<Joint> getChildren() {
        return children;
    }

}
