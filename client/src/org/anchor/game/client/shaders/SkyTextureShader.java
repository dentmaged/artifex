package org.anchor.game.client.shaders;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.SkyComponent;
import org.lwjgl.util.vector.Matrix4f;

public class SkyTextureShader extends ModelShader {

    private static SkyTextureShader instance = new SkyTextureShader();

    protected SkyTextureShader() {
        super("sky-texture");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("skybox", 1);
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        SkyComponent component = GameClient.getSky().entity.getComponent(SkyComponent.class);
        Graphics.bindCubemap(component.getSkybox(), 1);

        loadVector("sunDirection", component.direction);
        loadVector("sunColour", component.sunColour);

        loadVector("baseColour", component.baseColour);
        loadVector("topColour", component.topColour);

        loadBoolean("proceduralSky", Settings.proceduralSky);

        loadMatrix("projectionViewTransformationMatrix", Matrix4f.mul(Renderer.getProjectionMatrix(), Matrix4f.mul(GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix(), entity.getTransformationMatrix(), null), null));
        loadMatrix("inverseViewMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getInverseViewMatrix());
        loadMatrix("inverseTransformationMatrix", Matrix4f.invert(GameClient.getSky().entity.getLiveTransformationMatrix(), null));
    }

    public static SkyTextureShader getInstance() {
        return instance;
    }

}
