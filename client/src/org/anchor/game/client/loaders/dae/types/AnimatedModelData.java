package org.anchor.game.client.loaders.dae.types;

public class AnimatedModelData {

    private SkeletonData joints;
    private MeshData mesh;

    public AnimatedModelData(MeshData mesh, SkeletonData joints) {
        this.joints = joints;
        this.mesh = mesh;
    }

    public SkeletonData getJointsData() {
        return joints;
    }

    public MeshData getMeshData() {
        return mesh;
    }

}
