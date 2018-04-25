package org.anchor.game.client.animation;

public class Animation {

    private float length;
    private KeyFrame[] keyFrames;

    public Animation(float length, KeyFrame[] keyFrames) {
        super();
        this.length = length;
        this.keyFrames = keyFrames;
    }

    public float getLength() {
        return length;
    }

    public KeyFrame[] getKeyFrames() {
        return keyFrames;
    }

}
