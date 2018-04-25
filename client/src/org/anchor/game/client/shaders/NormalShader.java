package org.anchor.game.client.shaders;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.MeshComponent;

public class NormalShader extends ModelShader {

    private static NormalShader instance = new NormalShader();

    protected NormalShader() {
        super("normal");
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);
        loadFloat("reflectivity", entity.getComponent(MeshComponent.class).model.getTexture().getReflectivity());
        loadFloat("shineDamper", entity.getComponent(MeshComponent.class).model.getTexture().getShineDamper());

        loadInt("normalMap", 1);
    }

    public static NormalShader getInstance() {
        return instance;
    }

}
