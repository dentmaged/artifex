package org.anchor.game.client.loaders.dae.types;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;

public class JointData {

    private int index;
    private String nameId;
    private Matrix4f localBindTransformation;

    private List<JointData> children = new ArrayList<JointData>();

    public JointData(int index, String nameId, Matrix4f bindLocalTransform) {
        this.index = index;
        this.nameId = nameId;
        this.localBindTransformation = bindLocalTransform;
    }

    public void addChild(JointData child) {
        children.add(child);
    }

    public int getIndex() {
        return index;
    }

    public String getNameId() {
        return nameId;
    }

    public Matrix4f getLocalBindTransformation() {
        return localBindTransformation;
    }

    public List<JointData> getChildren() {
        return children;
    }

}
