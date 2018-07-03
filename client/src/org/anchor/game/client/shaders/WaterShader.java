package org.anchor.game.client.shaders;

import java.util.List;

import org.anchor.client.engine.renderer.Engine;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.deferred.DeferredShader;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.WaterComponent;
import org.lwjgl.util.vector.Matrix4f;
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
                Light light = lights.get(i);

                Vector3f position = light.getPosition();
                Vector3f spotlightDirection = light.getDirection();

                if (lights.get(i).getLightType() == LightType.DIRECTIONAL)
                    position = VectorUtils.mul(light.getDirection(), 20000);

                loadAs3DVector("lightPosition[" + i + "]", Matrix4f.transform(viewMatrix, new Vector4f(position.x, position.y, position.z, 1), null));
                loadVector("lightColour[" + i + "]", light.getColour());
                loadVector("attenuation[" + i + "]", light.getAttenuation());
                loadAs3DVector("lightDirection[" + i + "]", Matrix4f.transform(viewMatrix, new Vector4f(spotlightDirection.x, spotlightDirection.y, spotlightDirection.z, 0), null));
                loadVector("lightCutoff[" + i + "]", Mathf.cos(Mathf.toRadians(light.getCutoff())), Mathf.cos(Mathf.toRadians(light.getOuterCutoff()))); // performance
            } else {
                loadVector("lightPosition[" + i + "]", 0, 0, 0);
                loadVector("lightColour[" + i + "]", 0, 0, 0);
                loadVector("attenuation[" + i + "]", 1, 0, 0);
                loadVector("lightDirection[" + i + "]", 0, 0, 0);
                loadVector("lightCutoff[" + i + "]", 180, 180);
            }
        }
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        loadMatrix("transformationMatrix", entity.getTransformationMatrix());
        loadVector("tiling", SCALE * entity.getScale().x, SCALE * entity.getScale().z);
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
