package org.anchor.game.client.loaders.dae.types;

import java.util.ArrayList;
import java.util.List;

public class KeyFrameData {

    private float time;
    private List<JointTransformData> jointTransforms = new ArrayList<JointTransformData>();

    public KeyFrameData(float time) {
        this.time = time;
    }

    public void addJointTransform(JointTransformData transform) {
        jointTransforms.add(transform);
    }

    public float getTime() {
        return time;
    }

    public List<JointTransformData> getJointTransforms() {
        return jointTransforms;
    }

}
