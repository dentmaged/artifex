package org.anchor.game.client.loaders.dae.target;

import java.io.File;

import org.anchor.engine.common.xml.XMLNode;
import org.anchor.engine.common.xml.XMLParser;
import org.anchor.game.client.loaders.dae.types.AnimatedModelData;
import org.anchor.game.client.loaders.dae.types.AnimationData;
import org.anchor.game.client.loaders.dae.types.SkinningData;

public class ColladaLoader {

    public static AnimatedModelData loadColladaModel(File colladaFile) {
        XMLNode root = XMLParser.loadXMLFile(colladaFile);
        SkinningData skinningData = new SkinLoader(root.getChild("library_controllers")).extractSkinData();

        return new AnimatedModelData(new GeometryLoader(root.getChild("library_geometries"), skinningData.getVerticesSkinData()).extractModelData(), new SkeletonLoader(root.getChild("library_visual_scenes"), skinningData.getJointOrder()).extractBoneData());
    }

    public static AnimationData loadColladaAnimation(File colladaFile) {
        XMLNode root = XMLParser.loadXMLFile(colladaFile);

        return new AnimationLoader(root.getChild("library_animations"), root.getChild("library_visual_scenes")).extractAnimation();
    }

}
