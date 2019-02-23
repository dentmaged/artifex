package org.anchor.game.client.shaders;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.animation.AnimatedModel;
import org.anchor.game.client.components.MeshComponent;
import org.lwjgl.util.vector.Matrix4f;

public class AnimationShader extends ModelShader {

    private static AnimationShader instance = new AnimationShader();

    private static Matrix4f identity = new Matrix4f();

    protected AnimationShader() {
        super("animation");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("specular", 2);
        loadInt("metallic", 3);
        loadInt("roughness", 4);
        loadInt("ao", 5);
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        MeshComponent component = entity.getComponent(MeshComponent.class);
        AnimatedModel model = (AnimatedModel) component.model;
        loadBoolean("useAOMap", component.material.hasAmbientOcclusionMap());

        Matrix4f[] transforms = model.getJointTransforms();
        for (int i = 0; i < Settings.maxJoints; i++) {
            if (i < transforms.length)
                loadMatrix("jointTransforms[" + i + "]", transforms[i]);
            else
                loadMatrix("joinTransforms[" + i + "]", identity);
        }
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_diffuse");
        bindFragOutput(1, "out_other");
        bindFragOutput(2, "out_normal");
        bindFragOutput(3, "out_albedo");

        bindAttribute(0, "position");
        bindAttribute(1, "textureCoordinates");
        bindAttribute(2, "normal");
        bindAttribute(3, "jointIndices");
        bindAttribute(4, "weights");
    }

    public static AnimationShader getInstance() {
        return instance;
    }

}
