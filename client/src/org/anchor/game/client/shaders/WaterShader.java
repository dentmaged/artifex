package org.anchor.game.client.shaders;

import java.util.List;

import org.anchor.client.engine.renderer.Engine;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.deferred.DeferredShader;
import org.anchor.client.engine.renderer.types.Light;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.WaterComponent;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class WaterShader extends ModelShader {

    private static WaterShader instance = new WaterShader();
    private static float SCALE = 4f / 50f;

    protected WaterShader() {
        super("water");
        texture = "scene";
    }

    public static int DUDV = Loader.getInstance().loadTexture(TextureType.WATER, "dudv");
    public static int NORMAL = Loader.getInstance().loadTexture(TextureType.WATER, "normal");

    @Override
    public void onBind() {
        super.onBind();

        loadInt("depthMap", 1);
        loadInt("dudvMap", 2);
        loadInt("normalMap", 3);
        loadInt("ssao", 6);
        loadInt("skybox", 8);
        loadInt("irradiance", 9);
        loadInt("prefilter", 10);
        loadInt("brdf", 11);
        loadInt("shadowMap", 11 + Settings.shadowSplits);

        Engine.bindColourTexture(GameClient.getSceneFramebuffer(), 0);
        Engine.bindDepthTexture(GameClient.getSceneFramebuffer(), 1);
        Engine.bind2DTexture(DUDV, 2);
        Engine.bind2DTexture(NORMAL, 3);

        Engine.bind2DTexture(GameClient.getAmbientOcclusionTexture(), 6);
        Engine.bind2DTexture(GameClient.getBRDF(), 11);
        Engine.bind2DTexture(GameClient.getShadowMap(Settings.shadowSplits - 1), 11 + Settings.shadowSplits);

        loadFloat("minDiffuse", Settings.minDiffuse);
        loadFloat("density", Settings.density);
        loadFloat("gradient", Settings.gradient);

        loadMatrix("toShadowMapSpace", GameClient.getToShadowMapSpaceMatrix(Settings.shadowSplits - 1));
        loadFloat("shadowDistance", Settings.shadowDistance);

        loadFloat("near", Settings.nearPlane);
        loadFloat("far", Settings.farPlane);

        Matrix4f inverseViewMatrix = GameClient.getPlayer().getComponent(LivingComponent.class).getInverseViewMatrix();
        loadMatrix("normalMatrix", Matrix4f.transpose(inverseViewMatrix, null));
        loadMatrix("inverseViewMatrix", inverseViewMatrix);

        SkyComponent sky = GameClient.getSky();
        if (sky != null) {
            loadVector("baseColour", sky.baseColour);
            loadVector("topColour", sky.topColour);
            loadBoolean("proceduralSky", Settings.proceduralSky);

            Engine.bindCubemap(sky.getSkybox(), 8);
            Engine.bindCubemap(sky.getIrradiance(), 9);
            Engine.bindCubemap(sky.getPrefilter(), 10);
        }

        List<Light> lights = GameClient.getSceneLights();
        Matrix4f viewMatrix = GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix();
        for (int i = 0; i < DeferredShader.MAX_LIGHTS; i++) {
            if (i < lights.size()) {
                Vector3f position = lights.get(i).getPosition();
                float multiplier = 1;
                if (lights.get(i).isDirectionalLight())
                    multiplier = 20000;

                super.loadVector("lightPosition[" + i + "]", new Vector3f(Matrix4f.transform(viewMatrix, new Vector4f(position.x * multiplier, position.y * multiplier, position.z * multiplier, 1), null)));
                super.loadVector("lightColour[" + i + "]", lights.get(i).getColour());
                super.loadVector("attenuation[" + i + "]", lights.get(i).getAttenuation());
            } else {
                super.loadVector("lightPosition[" + i + "]", new Vector3f(0, 0, 0));
                super.loadVector("lightColour[" + i + "]", new Vector3f(0, 0, 0));
                super.loadVector("attenuation[" + i + "]", new Vector3f(1, 0, 0));
            }
        }
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        loadMatrix("transformationMatrix", entity.getTransformationMatrix());
        loadVector("tiling", new Vector2f(SCALE * entity.getScale().x, SCALE * entity.getScale().z));
        loadFloat("height", entity.getPosition().y);

        WaterComponent water = entity.getComponent(WaterComponent.class);
        if (water != null) {
            loadFloat("moveFactor", water.moveFactor);
            loadFloat("waveStrength", water.waveStrength);
            loadVector("colour", water.colour);
        }
    }

    public static WaterShader getInstance() {
        return instance;
    }

}
