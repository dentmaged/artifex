package org.anchor.game.client.shaders;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.deferred.DeferredShader;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class ForwardStaticShader extends ModelShader {

    private static ForwardStaticShader instance = new ForwardStaticShader();

    protected ForwardStaticShader() {
        super("forward");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("specular", 2);
        loadInt("metallic", 3);
        loadInt("roughness", 4);
        loadInt("ao", 5);
        loadInt("skybox", 8);
        loadInt("irradianceMap", 9);
        loadInt("prefilter", 10);
        loadInt("brdf", 11);

        for (int i = 0; i < Settings.shadowSplits; i++)
            loadInt("shadowMaps[" + i + "]", 12 + i);

        loadMatrix("inverseViewMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getInverseViewMatrix());
        loadFloat("minDiffuse", Settings.minDiffuse);
        loadFloat("density", Settings.density);
        loadFloat("gradient", Settings.gradient);

        for (int i = 0; i < Settings.shadowSplits; i++) {
            loadMatrix("toShadowMapSpaces[" + i + "]", GameClient.getToShadowMapSpaceMatrix(i));
            loadFloat("shadowDistances[" + i + "]", GameClient.getShadowExtents(i));
            Graphics.bind2DTexture(GameClient.getShadowMap(i), 12 + i);
        }

        SkyComponent sky = GameClient.getSky();
        if (sky != null) {
            loadVector("baseColour", sky.baseColour);
            loadVector("topColour", sky.topColour);
            loadBoolean("proceduralSky", Settings.proceduralSky);

            Graphics.bindCubemap(sky.getSkybox(), 8);
            Graphics.bindCubemap(sky.getIrradiance(), 9);
            Graphics.bindCubemap(sky.getPrefilter(), 10);
        }
        Graphics.bind2DTexture(GameClient.getBRDF(), 11);

        List<Light> lights = GameClient.getSceneLights();
        Matrix4f viewMatrix = GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix();
        for (int i = 0; i < DeferredShader.MAX_LIGHTS; i++) {
            if (i < lights.size()) {
                Light light = lights.get(i);

                Vector3f position = light.getPosition();
                Vector3f direction = light.getDirection();

                Vector4f v = Matrix4f.transform(viewMatrix, new Vector4f(position.x, position.y, position.z, 1), null);
                if (light.getLightType() == LightType.POINT)
                    v.w = 0;
                else if (light.getLightType() == LightType.DIRECTIONAL)
                    v.w = 1;
                else
                    v.w = 2;

                loadVector("lightPosition[" + i + "]", v);
                loadVector("lightColour[" + i + "]", light.getColour());
                loadVector("attenuation[" + i + "]", light.getAttenuation());
                loadAs3DVector("lightDirection[" + i + "]", Matrix4f.transform(viewMatrix, new Vector4f(direction.x, direction.y, direction.z, 0), null));
                loadVector("lightCutoff[" + i + "]", Mathf.cos(Mathf.toRadians(light.getCutoff())), Mathf.cos(Mathf.toRadians(light.getOuterCutoff()))); // performance
            } else {
                loadVector("lightPosition[" + i + "]", 0, 0, 0, 0);
                loadVector("lightColour[" + i + "]", 0, 0, 0);
                loadVector("attenuation[" + i + "]", 1, 0, 0);
                loadVector("lightDirection[" + i + "]", 0, -1, 0);
                loadVector("lightCutoff[" + i + "]", 35, 45);
            }
        }
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        MeshComponent render = entity.getComponent(MeshComponent.class);
        if (render == null)
            return;

        loadMatrix("viewTransformationMatrix", Matrix4f.mul(GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix(), entity.getTransformationMatrix(), null));
        loadMatrix("normalMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getNormalMatrix(entity));

        loadFloat("numberOfRows", render.model.getTexture().getNumberOfRows());
        loadVector("textureOffset", render.getTextureOffset());
        loadVector("colour", render.colour);

        loadBoolean("useAOMap", render.model.getTexture().getAmbientOcclusionMap() != -1);
    }

    public static ForwardStaticShader getInstance() {
        return instance;
    }

}
