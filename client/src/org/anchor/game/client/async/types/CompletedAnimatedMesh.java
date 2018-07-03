package org.anchor.game.client.async.types;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.mesh.MeshRequest;
import org.anchor.engine.common.utils.AABB;
import org.anchor.game.client.loaders.dae.types.AnimatedModelData;

public class CompletedAnimatedMesh extends CompletedRequest {

    private MeshRequest request;
    private AnimatedModelData data;

    public CompletedAnimatedMesh(MeshRequest request, AnimatedModelData data) {
        this.request = request;
        this.data = data;
    }

    @Override
    public void load() {
        request.setMesh(Loader.getInstance().loadToVAO(data.getMeshData().getVertices(), data.getMeshData().getTextureCoords(), data.getMeshData().getNormals(), data.getMeshData().getIndices(), data.getMeshData().getJointIds(), data.getMeshData().getVertexWeights()));
        request.getMesh().setAABB(AABB.generateAABB(data.getMeshData().getVertices()));
    }

}
