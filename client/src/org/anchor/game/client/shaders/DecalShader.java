package org.anchor.game.client.shaders;

import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.DecalComponent;
import org.lwjgl.util.vector.Matrix4f;

public class DecalShader extends ModelShader {

    private static DecalShader instance = new DecalShader();

    protected DecalShader() {
        super("decal");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("specular", 2);
        loadInt("metallic", 3);
        loadInt("roughness", 4);
        loadInt("ao", 5);
        loadInt("depthMap", 6);

        loadMatrix("inverseViewMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getInverseViewMatrix());
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        Matrix4f inverseTransformationMatrix = Matrix4f.invert(entity.getTransformationMatrix(), null);
        if (inverseTransformationMatrix != null)
            loadMatrix("inverseTransformationMatrix", inverseTransformationMatrix);
        loadBoolean("useAOMap", entity.getComponent(DecalComponent.class).material.hasAmbientOcclusionMap());
    }

    public static DecalShader getInstance() {
        return instance;
    }

}
