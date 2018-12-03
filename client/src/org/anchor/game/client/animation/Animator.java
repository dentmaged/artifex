package org.anchor.game.client.animation;

import java.util.HashMap;
import java.util.Map;

import org.anchor.game.client.app.AppManager;
import org.lwjgl.util.vector.Matrix4f;

public class Animator {

    private AnimatedModel model;

    private Animation currentAnimation;
    private float animationTime;

    public Animator(AnimatedModel model) {
        this.model = model;
    }

    public void update() {
        if (currentAnimation == null)
            return;

        animationTime += AppManager.getFrameTimeSeconds();
        if (animationTime > currentAnimation.getLength())
            animationTime %= currentAnimation.getLength();

        applyPoseToJoints(calculateCurrentAnimationPose(), model.getRoot(), new Matrix4f());
    }

    public void animate(Animation animation) {
        animationTime = 0;
        currentAnimation = animation;
    }

    private Map<String, Matrix4f> calculateCurrentAnimationPose() {
        KeyFrame[] frames = getPreviousAndNextFrames();
        float progression = calculateProgression(frames[0], frames[1]);
        return interpolatePoses(frames[0], frames[1], progression);
    }

    private void applyPoseToJoints(Map<String, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform) {
        Matrix4f currentLocalTransform = currentPose.get(joint.getName());
        Matrix4f currentTransform = Matrix4f.mul(parentTransform, currentLocalTransform, null);
        for (Joint childJoint : joint.getChildren())
            applyPoseToJoints(currentPose, childJoint, currentTransform);

        Matrix4f.mul(currentTransform, joint.getInverseBindTransform(), currentTransform);
        joint.setAnimationTransform(currentTransform);
    }

    private KeyFrame[] getPreviousAndNextFrames() {
        KeyFrame[] allFrames = currentAnimation.getKeyFrames();
        KeyFrame previousFrame = allFrames[0];
        KeyFrame nextFrame = allFrames[0];
        for (int i = 1; i < allFrames.length; i++) {
            nextFrame = allFrames[i];
            if (nextFrame.getTimeStamp() > animationTime) {
                break;
            }
            previousFrame = allFrames[i];
        }
        return new KeyFrame[] { previousFrame, nextFrame };
    }

    private float calculateProgression(KeyFrame previousFrame, KeyFrame nextFrame) {
        float totalTime = nextFrame.getTimeStamp() - previousFrame.getTimeStamp();
        float currentTime = animationTime - previousFrame.getTimeStamp();
        return currentTime / totalTime;
    }

    private Map<String, Matrix4f> interpolatePoses(KeyFrame previousFrame, KeyFrame nextFrame, float progression) {
        Map<String, Matrix4f> currentPose = new HashMap<String, Matrix4f>();
        for (String jointName : previousFrame.getJointKeyFrames().keySet()) {
            JointTransform previousTransform = previousFrame.getJointKeyFrames().get(jointName);
            JointTransform nextTransform = nextFrame.getJointKeyFrames().get(jointName);
            JointTransform currentTransform = JointTransform.interpolate(previousTransform, nextTransform, progression);
            currentPose.put(jointName, currentTransform.getLocalTransform());
        }
        return currentPose;
    }

}
