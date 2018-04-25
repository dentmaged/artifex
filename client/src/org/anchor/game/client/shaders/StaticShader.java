package org.anchor.game.client.shaders;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.MeshComponent;

public class StaticShader extends ModelShader {

    private static StaticShader instance = new StaticShader();

    protected StaticShader() {
        super("entity");
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);
        loadFloat("reflectivity", entity.getComponent(MeshComponent.class).model.getTexture().getReflectivity());
        loadFloat("shineDamper", entity.getComponent(MeshComponent.class).model.getTexture().getShineDamper());
    }

    public static StaticShader getInstance() {
        return instance;
    }

}
