package org.anchor.game.client.loaders.obj.normals;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.AABB;
import org.anchor.game.client.loaders.obj.ModelData;
import org.anchor.game.client.loaders.obj.OBJFileLoader;

public class NormalMappedOBJLoader {

    public static Mesh loadOBJ(String objFileName) {
        ModelData data = OBJFileLoader.loadOBJModel(objFileName);
        Mesh mesh = Loader.getInstance().loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getTangents(), data.getIndices());
        mesh.setAABB(AABB.generateAABB(data.getVertices()));
        mesh.setFurthestVertex(data.getFurthestVertex());

        return mesh;
    }

}
