package org.anchor.game.client.shaders;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.MeshComponent;

public class NormalShader extends ModelShader {

    private static NormalShader instance = new NormalShader();

    protected NormalShader() {
        super("normal");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("normal", 1);
        loadInt("specular", 2);
        loadInt("metallic", 3);
        loadInt("roughness", 4);
        loadInt("ao", 5);
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        loadBoolean("useAOMap", entity.getComponent(MeshComponent.class).model.getTexture().getAmbientOcclusionMap() != -1);
    }

    public static NormalShader getInstance() {
        return instance;
    }

}
