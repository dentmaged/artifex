package org.anchor.game.client.shaders;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.SkyComponent;

public class SkyShader extends ModelShader {

    private static SkyShader instance = new SkyShader();

    protected SkyShader() {
        super("sky");
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        loadVector("sunDirection", entity.getComponent(SkyComponent.class).direction);
        loadVector("sunColour", entity.getComponent(SkyComponent.class).sunColour);

        loadVector("baseColour", entity.getComponent(SkyComponent.class).baseColour);
        loadVector("topColour", entity.getComponent(SkyComponent.class).topColour);
    }

    public static SkyShader getInstance() {
        return instance;
    }

}
