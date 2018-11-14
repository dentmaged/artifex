package org.anchor.game.client.loaders.dae;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.Material;
import org.anchor.client.engine.renderer.types.texture.TextureRequest;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.game.client.animation.AnimatedModel;
import org.anchor.game.client.animation.Joint;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.loaders.dae.target.ColladaLoader;
import org.anchor.game.client.loaders.dae.types.AnimatedModelData;
import org.anchor.game.client.loaders.dae.types.JointData;

public class AnimatedModelLoader {

    public static AnimatedModel loadAnimatedModel(String name, TextureRequest texture) {
        return loadAnimatedModel(name, new Material(texture));
    }

    public static AnimatedModel loadAnimatedModel(String name, Material texture) {
        AnimatedModelData data = ColladaLoader.loadColladaModel(FileHelper.newGameFile(Loader.RES_LOC, name + ".dae"));
        Joint headJoint = createHeadJoint(data);

        return new AnimatedModel(Requester.alreadyLoadedMesh(Loader.getInstance().loadToVAO(data.getMeshData().getVertices(), data.getMeshData().getTextureCoords(), data.getMeshData().getNormals(), data.getMeshData().getIndices(), data.getMeshData().getJointIds(), data.getMeshData().getVertexWeights())), headJoint, data.getJointsData().getJointCount());
    }

    public static Joint createHeadJoint(AnimatedModelData data) {
        return createJoints(data.getJointsData().getHeadJoint());
    }

    private static Joint createJoints(JointData data) {
        Joint joint = new Joint(data.getIndex(), data.getNameId(), data.getLocalBindTransformation());
        for (JointData child : data.getChildren())
            joint.addChild(createJoints(child));

        return joint;
    }

}
