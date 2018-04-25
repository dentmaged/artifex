package org.anchor.game.client.loaders.dae;

import java.util.HashMap;
import java.util.Map;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.game.client.animation.Animation;
import org.anchor.game.client.animation.JointTransform;
import org.anchor.game.client.animation.KeyFrame;
import org.anchor.game.client.animation.Quaternion;
import org.anchor.game.client.loaders.dae.target.ColladaLoader;
import org.anchor.game.client.loaders.dae.types.AnimationData;
import org.anchor.game.client.loaders.dae.types.JointTransformData;
import org.anchor.game.client.loaders.dae.types.KeyFrameData;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class AnimationLoader {

    private static final String RES_LOC = "res";

    public static Animation loadAnimation(String file) {
        AnimationData animationData = ColladaLoader.loadColladaAnimation(FileHelper.newGameFile(RES_LOC, file + ".dae"));
        KeyFrame[] frames = new KeyFrame[animationData.getKeyFrames().length];

        for (int i = 0; i < frames.length; i++)
            frames[i] = createKeyFrame(animationData.getKeyFrames()[i]);

        return new Animation(animationData.getLength(), frames);
    }

    private static KeyFrame createKeyFrame(KeyFrameData data) {
        Map<String, JointTransform> map = new HashMap<String, JointTransform>();

        for (JointTransformData jointData : data.getJointTransforms()) {
            JointTransform jointTransform = createTransform(jointData);
            map.put(jointData.getJointNameId(), jointTransform);
        }

        return new KeyFrame(data.getTime(), map);
    }

    private static JointTransform createTransform(JointTransformData data) {
        Matrix4f mat = data.getLocalJointTransform();
        Vector3f translation = new Vector3f(mat.m30, mat.m31, mat.m32);
        Quaternion rotation = Quaternion.fromMatrix(mat);

        return new JointTransform(translation, rotation);
    }

}
