package org.anchor.game.client.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.shadows.ShadowShader;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.profiler.Profiler;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.game.client.TerrainRenderer;
import org.anchor.game.client.components.DecalComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.WaterComponent;
import org.anchor.game.client.shaders.DecalShader;
import org.anchor.game.client.shaders.ForwardStaticShader;
import org.anchor.game.client.utils.FrustumCull;
import org.lwjgl.opengl.GL11;

public class ClientScene extends Scene {

    private Map<Model, List<Entity>> renderables = new HashMap<Model, List<Entity>>();
    private static DecalShader shader = DecalShader.getInstance();

    public void render() {
        Profiler.start("Deferred (Geometry)");
        renderables.clear();

        for (Entity entity : getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded() || entity.hasComponent(SkyComponent.class) || entity.hasComponent(WaterComponent.class))
                continue;

            if (!component.model.getTexture().isBlendingEnabled()) {
                if (!FrustumCull.isVisible(entity))
                    continue;

                if (!renderables.containsKey(component.model))
                    renderables.put(component.model, new ArrayList<Entity>());
                renderables.get(component.model).add(entity);
            }
        }

        for (Entry<Model, List<Entity>> entry : renderables.entrySet()) {
            Renderer.bind(entry.getKey());

            for (Entity entity : entry.getValue()) {
                ClientShader shader = entity.getComponent(MeshComponent.class).shader;
                shader.start();
                shader.loadEntitySpecificInformation(entity);

                Renderer.render(entry.getKey());

                shader.stop();
            }

            Renderer.unbind(entry.getKey());
        }

        TerrainRenderer.render(this);
        Profiler.end("Deferred (Geometry)");
    }

    public void renderDecals(int depthMap) {
        Profiler.start("Decals");
        if (!Renderer.getCubeModel().isLoaded())
            return;

        Renderer.bind(Renderer.getCubeModel());
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        shader.start();

        Graphics.bind2DTexture(depthMap, 6);
        for (Entity entity : getEntitiesWithComponent(DecalComponent.class)) {
            DecalComponent component = entity.getComponent(DecalComponent.class);
            if (component.texture == null || !component.texture.isLoaded())
                continue;

            Graphics.bind2DTexture(component.texture.getId(), 0);
            Graphics.bind2DTexture(component.texture.getNormalMap(), 1);
            Graphics.bind2DTexture(component.texture.getSpecularMap(), 2);
            Graphics.bind2DTexture(component.texture.getMetallicMap(), 3);
            Graphics.bind2DTexture(component.texture.getRoughnessMap(), 4);
            Graphics.bind2DTexture(component.texture.getAmbientOcclusionMap(), 5);
            shader.loadEntitySpecificInformation(entity);

            Renderer.render(Renderer.getCubeModel());
        }

        shader.stop();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        Renderer.unbind(Renderer.getCubeModel());
        Profiler.end("Decals");
    }

    public void renderBlending() {
        Profiler.start("Blending");
        renderables.clear();

        for (Entity entity : getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded() || entity.hasComponent(WaterComponent.class))
                continue;

            if (component.model.getTexture().isBlendingEnabled() || entity.hasComponent(SkyComponent.class)) {
                if (!FrustumCull.isVisible(entity))
                    continue;

                if (!renderables.containsKey(component.model))
                    renderables.put(component.model, new ArrayList<Entity>());
                renderables.get(component.model).add(entity);
            }
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (Entry<Model, List<Entity>> entry : renderables.entrySet()) {
            Renderer.bind(entry.getKey());

            for (Entity entity : entry.getValue()) {
                ClientShader shader = entity.getComponent(MeshComponent.class).shader;
                shader.start();
                shader.loadEntitySpecificInformation(entity);

                Renderer.render(entry.getKey());

                shader.stop();
            }

            Renderer.unbind(entry.getKey());
        }
        GL11.glDisable(GL11.GL_BLEND);
        Profiler.end("Blending");
    }

    public void renderReflective() {
        Profiler.start("Reflective");
        renderables.clear();

        for (Entity entity : getEntitiesWithComponent(MeshComponent.class)) {
            if (!FrustumCull.isVisible(entity))
                continue;

            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded())
                continue;

            if (entity.hasComponent(WaterComponent.class)) {
                if (!renderables.containsKey(component.model))
                    renderables.put(component.model, new ArrayList<Entity>());
                renderables.get(component.model).add(entity);
            }
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (Entry<Model, List<Entity>> entry : renderables.entrySet()) {
            Renderer.bind(entry.getKey());

            for (Entity entity : entry.getValue()) {
                ClientShader shader = entity.getComponent(MeshComponent.class).shader;
                shader.start();
                shader.loadEntitySpecificInformation(entity);

                GL11.glDisable(GL11.GL_CULL_FACE);
                Renderer.render(entry.getKey());

                shader.stop();
            }

            Renderer.unbind(entry.getKey());
        }

        GL11.glDisable(GL11.GL_BLEND);
        Profiler.end("Reflective");
    }

    public void forwardRender() {
        Profiler.start("Forward");
        renderables.clear();

        for (Entity entity : getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded() || entity.hasComponent(WaterComponent.class))
                continue;

            if (component.model.getTexture().isBlendingEnabled() || entity.hasComponent(SkyComponent.class)) {
                if (!FrustumCull.isVisible(entity))
                    continue;

                if (!renderables.containsKey(component.model))
                    renderables.put(component.model, new ArrayList<Entity>());
                renderables.get(component.model).add(entity);
            }
        }

        ClientShader shader = ForwardStaticShader.getInstance();
        shader.start();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (Entry<Model, List<Entity>> entry : renderables.entrySet()) {
            Renderer.bind(entry.getKey());

            for (Entity entity : entry.getValue()) {
                shader.loadEntitySpecificInformation(entity);

                Renderer.render(entry.getKey());

            }

            Renderer.unbind(entry.getKey());
        }
        GL11.glDisable(GL11.GL_BLEND);
        shader.stop();
        Profiler.end("Forward");
    }

    public void renderShadows(Shadows shadows) {
        Profiler.start("Shadows");
        renderables.clear();

        for (Entity entity : getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded() || !component.castsShadows)
                continue;

            if (!renderables.containsKey(component.model))
                renderables.put(component.model, new ArrayList<Entity>());
            renderables.get(component.model).add(entity);
        }

        ShadowShader shader = ShadowShader.getInstance();
        shader.start();
        for (int i = 0; i < Settings.shadowSplits; i++) {
            shadows.bind(i);
            shader.load(shadows.getProjectionViewMatrix(i));
            for (Entry<Model, List<Entity>> entry : renderables.entrySet()) {
                Renderer.bind(entry.getKey());

                for (Entity entity : entry.getValue()) {
                    shader.loadEntitySpecificInformation(entity.getTransformationMatrix(), entry.getKey().getTexture().getNumberOfRows(), entity.getComponent(MeshComponent.class).getTextureOffset());

                    Renderer.render(entry.getKey());
                }

                Renderer.unbind(entry.getKey());
            }
        }

        shader.stop();
        Profiler.end("Shadows");
    }

    public boolean isLoaded() {
        boolean loaded = true;
        for (Entity entity : entities) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component == null || component.model == null)
                continue;

            if (!component.model.isLoaded())
                loaded = false;
        }

        return loaded;
    }

    public static void renderEntity(Entity entity) {
        MeshComponent component = entity.getComponent(MeshComponent.class);
        if (component == null || !component.model.isLoaded())
            return;

        renderEntity(entity, component.shader);
    }

    public static void renderEntity(Entity entity, ClientShader shader) {
        MeshComponent component = entity.getComponent(MeshComponent.class);
        if (component == null || !component.model.isLoaded())
            return;

        Renderer.bind(component.model);
        shader.start();
        shader.loadEntitySpecificInformation(entity);
        Renderer.render(component.model);

        shader.stop();
        Renderer.unbind(component.model);
    }

}
