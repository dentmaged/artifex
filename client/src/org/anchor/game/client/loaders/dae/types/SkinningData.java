package org.anchor.game.client.loaders.dae.types;

import java.util.List;

public class SkinningData {

    private List<String> jointOrder;
    private List<VertexSkinData> verticesSkinData;

    public SkinningData(List<String> jointOrder, List<VertexSkinData> verticesSkinData) {
        this.jointOrder = jointOrder;
        this.verticesSkinData = verticesSkinData;
    }

    public List<String> getJointOrder() {
        return jointOrder;
    }

    public List<VertexSkinData> getVerticesSkinData() {
        return verticesSkinData;
    }

}
