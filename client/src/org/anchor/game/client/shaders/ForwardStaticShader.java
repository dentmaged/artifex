package org.anchor.game.client.shaders;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.MeshComponent;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class ForwardStaticShader extends ModelShader {

    private static ForwardStaticShader instance = new ForwardStaticShader();

    protected ForwardStaticShader() {
        super("forwardStatic");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("specular", 2);
        loadInt("metallic", 3);
        loadInt("roughness", 4);
        loadInt("ao", 5);

        for (int i = 0; i < Settings.shadowSplits; i++)
            loadInt("shadowMaps[" + i + "]", 13 + i);

        loadMatrix("inverseViewMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getInverseViewMatrix());

        for (int i = 0; i < Settings.shadowSplits; i++) {
            loadMatrix("toShadowMapSpaces[" + i + "]", GameClient.getToShadowMapSpaceMatrix(i));
            loadFloat("shadowDistances[" + i + "]", GameClient.getShadowExtents(i));
            Graphics.bind2DTexture(GameClient.getShadowMap(i), 13 + i);
        }

        loadBoolean("bakedGeneration", Settings.bakedGeneration);

        List<Light> lights = GameClient.getSceneLights();
        Matrix4f viewMatrix = GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix();
        for (int i = 0; i < Settings.maxLights; i++) {
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

                if (light.castsShadows())
                    v.w += 3;

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

        loadFloat("numberOfRows", render.material.getNumberOfRows());
        loadVector("textureOffset", render.getTextureOffset());
        loadVector("uvScale", render.getUVScale());
        loadVector("colour", render.colour);

        loadBoolean("useAOMap", render.material.hasAmbientOcclusionMap());
    }

    public static ForwardStaticShader getInstance() {
        return instance;
    }

}
