package org.anchor.game.client.shaders;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.PostProcessVolumeComponent;
import org.anchor.game.client.components.SkyComponent;

public class SkyShader extends ModelShader {

    private static SkyShader instance = new SkyShader();

    protected SkyShader() {
        super("sky");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("skybox", 1);
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        SkyComponent component = entity.getComponent(SkyComponent.class);
        Graphics.bind2DTexture(component.getSky(), 1);

        loadVector("sunDirection", component.direction);
        loadVector("sunColour", component.sunColour);

        loadVector("baseColour", component.baseColour);
        loadVector("topColour", component.topColour);

        loadBoolean("proceduralSky", Settings.proceduralSky);
        loadMatrix("transformationMatrix", entity.getTransformationMatrix());

        PostProcessVolumeComponent postProcess = GameClient.getCurrentPostProcessVolume();
        if (postProcess != null) {
            loadFloat("blendFogTransitionStart", postProcess.horizonBlendStart);
            loadFloat("blendFogTransitionEnd", postProcess.horizonBlendEnd);
            loadVector("fogColour", postProcess.fogColour);
            loadFloat("blendFogMultiplier", 1f / (postProcess.horizonBlendEnd - postProcess.horizonBlendStart));
        } else {
            loadFloat("blendFogTransitionStart", 1.05f);
            loadFloat("blendFogTransitionEnd", 1.05f);
        }
    }

    public static SkyShader getInstance() {
        return instance;
    }

}
