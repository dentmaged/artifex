package org.anchor.game.client.loaders.dae.target;

import java.nio.FloatBuffer;
import java.util.List;

import org.anchor.engine.common.xml.XMLNode;
import org.anchor.game.client.loaders.dae.types.AnimationData;
import org.anchor.game.client.loaders.dae.types.JointTransformData;
import org.anchor.game.client.loaders.dae.types.KeyFrameData;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class AnimationLoader {

    private static final Matrix4f CORRECTION = new Matrix4f().rotate((float) Math.toRadians(-90), new Vector3f(1, 0, 0));

    private XMLNode animationData, jointHierarchy;

    public AnimationLoader(XMLNode animationData, XMLNode jointHierarchy) {
        this.animationData = animationData;
        this.jointHierarchy = jointHierarchy;
    }

    public AnimationData extractAnimation() {
        String rootNode = findRootJointName();
        float[] times = getKeyTimes();
        float duration = times[times.length - 1];

        KeyFrameData[] keyFrames = initKeyFrames(times);
        List<XMLNode> animationNodes = animationData.getChildren("animation");

        for (XMLNode jointNode : animationNodes)
            loadJointTransforms(keyFrames, jointNode, rootNode);

        return new AnimationData(duration, keyFrames);
    }

    private float[] getKeyTimes() {
        XMLNode timeData = animationData.getChild("animation").getChild("source").getChild("float_array");
        String[] rawTimes = timeData.getData().split(" ");
        float[] times = new float[rawTimes.length];

        for (int i = 0; i < times.length; i++)
            times[i] = Float.parseFloat(rawTimes[i]);

        return times;
    }

    private KeyFrameData[] initKeyFrames(float[] times) {
        KeyFrameData[] frames = new KeyFrameData[times.length];
        for (int i = 0; i < frames.length; i++)
            frames[i] = new KeyFrameData(times[i]);

        return frames;
    }

    private void loadJointTransforms(KeyFrameData[] frames, XMLNode jointData, String rootNodeId) {
        String jointNameId = getJointName(jointData);
        String dataId = getDataId(jointData);

        XMLNode transformData = jointData.getChildWithAttribute("source", "id", dataId);
        String[] rawData = transformData.getChild("float_array").getData().split(" ");

        processTransforms(jointNameId, rawData, frames, jointNameId.equals(rootNodeId));
    }

    private String getDataId(XMLNode jointData) {
        return jointData.getChild("sampler").getChildWithAttribute("input", "semantic", "OUTPUT").getAttribute("source").substring(1);
    }

    private String getJointName(XMLNode jointData) {
        XMLNode channelNode = jointData.getChild("channel");
        String data = channelNode.getAttribute("target");

        return data.split("/")[0];
    }

    private void processTransforms(String jointName, String[] rawData, KeyFrameData[] keyFrames, boolean root) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        float[] matrixData = new float[16];
        for (int i = 0; i < keyFrames.length; i++) {
            for (int j = 0; j < 16; j++)
                matrixData[j] = Float.parseFloat(rawData[i * 16 + j]);

            buffer.clear();
            buffer.put(matrixData);
            buffer.flip();
            Matrix4f transform = new Matrix4f();
            transform.load(buffer);
            transform.transpose();
            if (root)
                Matrix4f.mul(CORRECTION, transform, transform);

            keyFrames[i].addJointTransform(new JointTransformData(jointName, transform));
        }
    }

    private String findRootJointName() {
        XMLNode skeleton = jointHierarchy.getChild("visual_scene").getChildWithAttribute("node", "id", "Armature");

        return skeleton.getChild("node").getAttribute("id");
    }

}
