package org.anchor.game.client.loaders.dae.types;

public class AnimationData {

    private float length;
    private KeyFrameData[] keyFrames;

    public AnimationData(float length, KeyFrameData[] keyFrames) {
        this.length = length;
        this.keyFrames = keyFrames;
    }

    public float getLength() {
        return length;
    }

    public KeyFrameData[] getKeyFrames() {
        return keyFrames;
    }

}
