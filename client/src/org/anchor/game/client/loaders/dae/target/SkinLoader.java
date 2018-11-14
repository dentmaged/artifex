package org.anchor.game.client.loaders.dae.target;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.common.xml.XMLNode;
import org.anchor.game.client.loaders.dae.types.SkinningData;
import org.anchor.game.client.loaders.dae.types.VertexSkinData;

public class SkinLoader {

    private final XMLNode skinningData;

    public SkinLoader(XMLNode controllersNode) {
        this.skinningData = controllersNode.getChild("controller").getChild("skin");
    }

    public SkinningData extractSkinData() {
        List<String> jointsList = loadJointsList();
        float[] weights = loadWeights();
        XMLNode weightsDataNode = skinningData.getChild("vertex_weights");

        int[] effectorJointCounts = getEffectiveJointsCounts(weightsDataNode);
        List<VertexSkinData> vertexWeights = getSkinData(weightsDataNode, effectorJointCounts, weights);

        return new SkinningData(jointsList, vertexWeights);
    }

    private List<String> loadJointsList() {
        XMLNode inputNode = skinningData.getChild("vertex_weights");
        String jointDataId = inputNode.getChildWithAttribute("input", "semantic", "JOINT").getAttribute("source").substring(1);
        XMLNode jointsNode = skinningData.getChildWithAttribute("source", "id", jointDataId).getChild("Name_array");

        String[] names = jointsNode.getData().split(" ");
        List<String> jointsList = new ArrayList<String>();

        for (String name : names)
            jointsList.add(name);

        return jointsList;
    }

    private float[] loadWeights() {
        XMLNode inputNode = skinningData.getChild("vertex_weights");
        String weightsDataId = inputNode.getChildWithAttribute("input", "semantic", "WEIGHT").getAttribute("source").substring(1);
        XMLNode weightsNode = skinningData.getChildWithAttribute("source", "id", weightsDataId).getChild("float_array");

        String[] rawData = weightsNode.getData().split(" ");
        float[] weights = new float[rawData.length];

        for (int i = 0; i < weights.length; i++)
            weights[i] = Float.parseFloat(rawData[i]);

        return weights;
    }

    private int[] getEffectiveJointsCounts(XMLNode weightsDataNode) {
        String[] rawData = weightsDataNode.getChild("vcount").getData().split(" ");
        int[] counts = new int[rawData.length];

        for (int i = 0; i < rawData.length; i++)
            counts[i] = Integer.parseInt(rawData[i]);

        return counts;
    }

    private List<VertexSkinData> getSkinData(XMLNode weightsDataNode, int[] counts, float[] weights) {
        String[] rawData = weightsDataNode.getChild("v").getData().split(" ");
        List<VertexSkinData> skinningData = new ArrayList<VertexSkinData>();
        int pointer = 0;

        for (int count : counts) {
            VertexSkinData skinData = new VertexSkinData();

            for (int i = 0; i < count; i++) {
                int jointId = Integer.parseInt(rawData[pointer++]);
                int weightId = Integer.parseInt(rawData[pointer++]);
                skinData.addJointEffect(jointId, weights[weightId]);
            }

            skinData.limitJointNumber(Settings.maxWeights);
            skinningData.add(skinData);
        }

        return skinningData;
    }

}
