package org.anchor.game.client.shaders;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.MeshComponent;

public class ForwardStaticShader extends ModelShader {

    private static ForwardStaticShader instance = new ForwardStaticShader();

    protected ForwardStaticShader() {
        super("entity");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("specular", 2);
        loadInt("metallic", 3);
        loadInt("roughness", 4);
        loadInt("ao", 5);
        loadInt("ssao", 6);
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        loadBoolean("useAOMap", entity.getComponent(MeshComponent.class).model.getTexture().getAmbientOcclusionMap() != -1);
    }

    public static ForwardStaticShader getInstance() {
        return instance;
    }

}
