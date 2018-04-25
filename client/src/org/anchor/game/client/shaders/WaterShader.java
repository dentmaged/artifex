package org.anchor.game.client.shaders;

import java.util.List;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.deferred.DeferredShader;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.shadows.ShadowFrustum;
import org.anchor.client.engine.renderer.types.Light;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.WaterComponent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
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
        loadInt("shadowMap", 4);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, GameClient.getSceneFramebuffer().getColourTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, GameClient.getSceneFramebuffer().getDepthTexture());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, DUDV);
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, NORMAL);
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, GameClient.getShadowMap());

        loadFloat("minDiffuse", DeferredShading.minDiffuse);
        loadFloat("density", DeferredShading.density);
        loadFloat("gradient", DeferredShading.gradient);

        loadMatrix("toShadowMapSpace", GameClient.getToShadowMapSpaceMatrix());
        loadFloat("shadowDistance", ShadowFrustum.SHADOW_DISTANCE);

        loadFloat("near", Renderer.NEAR_PLANE);
        loadFloat("far", Renderer.FAR_PLANE);

        Matrix4f inverseViewMatrix = GameClient.getPlayer().getComponent(LivingComponent.class).getInverseViewMatrix();
        loadMatrix("normalMatrix", Matrix4f.transpose(inverseViewMatrix, null));
        loadMatrix("inverseViewMatrix", inverseViewMatrix);

        SkyComponent sky = GameClient.getSky();
        if (sky != null) {
            loadVector("baseColour", sky.baseColour);
            loadVector("topColour", sky.topColour);
        }

        List<Light> lights = GameClient.getSceneLights();
        Matrix4f viewMatrix = GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix();
        for (int i = 0; i < DeferredShader.MAX_LIGHTS; i++) {
            if (i < lights.size()) {
                Vector3f position = lights.get(i).getPosition();
                super.loadVector("lightPosition[" + i + "]", new Vector3f(Matrix4f.transform(viewMatrix, new Vector4f(position.x, position.y, position.z, 1), null)));
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
