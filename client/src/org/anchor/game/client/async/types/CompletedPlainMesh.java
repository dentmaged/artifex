package org.anchor.game.client.async.types;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.mesh.MeshRequest;
import org.anchor.engine.common.utils.AABB;
import org.anchor.game.client.loaders.obj.ModelData;

public class CompletedPlainMesh extends CompletedRequest {

    private MeshRequest request;
    private ModelData data;

    public CompletedPlainMesh(MeshRequest request, ModelData data) {
        this.request = request;
        this.data = data;
    }

    @Override
    public void load() {
        request.setMesh(Loader.getInstance().loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices()));
        request.getMesh().setAABB(AABB.generateAABB(data.getVertices()));
        request.getMesh().setFurthestVertex(data.getFurthestVertex());
    }

}
