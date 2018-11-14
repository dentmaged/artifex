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
import org.anchor.game.client.shaders.DecalShader;
import org.anchor.game.client.shaders.ForwardStaticShader;
import org.anchor.game.client.utils.FrustumCull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;

public class ClientScene extends Scene {

    private Map<Model, List<Entity>> renderables = new HashMap<Model, List<Entity>>();
    private static DecalShader shader = DecalShader.getInstance();

    public void render() {
        Profiler.start("Deferred (Geometry)");
        renderables.clear();

        for (Entity entity : getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded() || entity.hasComponent(SkyComponent.class))
                continue;

            if (!component.material.isBlendingEnabled()) {
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
                MeshComponent component = entity.getComponent(MeshComponent.class);
                ClientShader shader = component.shader;
                component.material.bind();
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
        GL30.glColorMaski(2, false, false, false, true); // normal buffer: surface normals are stored in red and green channels
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
            if (component.material == null || !component.material.isLoaded())
                continue;

            component.material.bind();
            shader.loadEntitySpecificInformation(entity);

            Renderer.render(Renderer.getCubeModel());
        }

        shader.stop();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        Renderer.unbind(Renderer.getCubeModel());
        Profiler.end("Decals");
        GL30.glColorMaski(2, true, true, true, true);
    }

    public void renderBlending() {
        Profiler.start("Blending");
        renderables.clear();

        for (Entity entity : getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded())
                continue;

            if (component.material.isBlendingEnabled() || entity.hasComponent(SkyComponent.class)) {
                if (!FrustumCull.isVisible(entity))
                    continue;

                if (!renderables.containsKey(component.model))
                    renderables.put(component.model, new ArrayList<Entity>());
                renderables.get(component.model).add(entity);
            }
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL40.glBlendFunci(0, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL40.glBlendFunci(3, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (Entry<Model, List<Entity>> entry : renderables.entrySet()) {
            Renderer.bind(entry.getKey());

            for (Entity entity : entry.getValue()) {
                MeshComponent component = entity.getComponent(MeshComponent.class);
                ClientShader shader = component.shader;
                component.material.bind();

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

    public void forwardRender() {
        Profiler.start("Forward");
        renderables.clear();

        for (Entity entity : getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded())
                continue;

            if (component.material.isBlendingEnabled() || entity.hasComponent(SkyComponent.class)) {
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
        GL40.glBlendFunci(0, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL40.glBlendFunci(3, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (Entry<Model, List<Entity>> entry : renderables.entrySet()) {
            Renderer.bind(entry.getKey());

            for (Entity entity : entry.getValue()) {
                entity.getComponent(MeshComponent.class).material.bind();
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
                    MeshComponent component = entity.getComponent(MeshComponent.class);
                    if (component.material == null)
                        continue;

                    component.material.bind();
                    shader.loadEntitySpecificInformation(entity.getTransformationMatrix(), component.material.getNumberOfRows(), component.getTextureOffset());

                    Renderer.render(entry.getKey());
                }

                Renderer.unbind(entry.getKey());
            }
        }

        shader.stop();
        shadows.finish();
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
        component.material.bind();
        shader.loadEntitySpecificInformation(entity);
        Renderer.render(component.model);

        shader.stop();
        Renderer.unbind(component.model);
    }

}
