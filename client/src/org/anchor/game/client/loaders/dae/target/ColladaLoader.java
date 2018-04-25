package org.anchor.game.client.loaders.dae.target;

import java.io.File;

import org.anchor.engine.common.xml.XMLNode;
import org.anchor.engine.common.xml.XMLParser;
import org.anchor.game.client.loaders.dae.types.AnimatedModelData;
import org.anchor.game.client.loaders.dae.types.AnimationData;
import org.anchor.game.client.loaders.dae.types.MeshData;
import org.anchor.game.client.loaders.dae.types.SkeletonData;
import org.anchor.game.client.loaders.dae.types.SkinningData;

public class ColladaLoader {

    public static AnimatedModelData loadColladaModel(File colladaFile, int maxWeights) {
        XMLNode node = XMLParser.loadXMLFile(colladaFile);

        SkinLoader skinLoader = new SkinLoader(node.getChild("library_controllers"), maxWeights);
        SkinningData skinningData = skinLoader.extractSkinData();

        SkeletonLoader jointsLoader = new SkeletonLoader(node.getChild("library_visual_scenes"), skinningData.getJointOrder());
        SkeletonData jointsData = jointsLoader.extractBoneData();

        GeometryLoader g = new GeometryLoader(node.getChild("library_geometries"), skinningData.getVerticesSkinData());
        MeshData meshData = g.extractModelData();

        return new AnimatedModelData(meshData, jointsData);
    }

    public static AnimationData loadColladaAnimation(File colladaFile) {
        XMLNode node = XMLParser.loadXMLFile(colladaFile);
        XMLNode animNode = node.getChild("library_animations");
        XMLNode jointsNode = node.getChild("library_visual_scenes");
        AnimationLoader loader = new AnimationLoader(animNode, jointsNode);
        AnimationData animData = loader.extractAnimation();

        return animData;
    }

}
