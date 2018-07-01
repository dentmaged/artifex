package org.anchor.game.client.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.shadows.ShadowShader;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.game.client.TerrainRenderer;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.WaterComponent;
import org.anchor.game.client.utils.FrustumCull;
import org.lwjgl.opengl.GL11;

public class ClientScene extends Scene {

    private Map<Model, List<Entity>> renderables = new HashMap<Model, List<Entity>>();

    public void render() {
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
    }

    public void renderBlending() {
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
    }

    public void renderReflective() {
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
    }

    public void renderShadows(Shadows shadows) {
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

        Renderer.bind(component.model);
        ClientShader shader = entity.getComponent(MeshComponent.class).shader;
        shader.start();
        shader.loadEntitySpecificInformation(entity);
        Renderer.render(component.model);

        shader.stop();
        Renderer.unbind(component.model);
    }

}
