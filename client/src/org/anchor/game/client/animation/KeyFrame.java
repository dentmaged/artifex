package org.anchor.game.client.animation;

import java.util.Map;

public class KeyFrame {

    private float timeStamp;
    private Map<String, JointTransform> pose;

    public KeyFrame(float timeStamp, Map<String, JointTransform> pose) {
        super();
        this.timeStamp = timeStamp;
        this.pose = pose;
    }

    public float getTimeStamp() {
        return timeStamp;
    }

    public Map<String, JointTransform> getJointKeyFrames() {
        return pose;
    }

}
