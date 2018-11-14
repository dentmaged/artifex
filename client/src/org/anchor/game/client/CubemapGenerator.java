package org.anchor.game.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.cubemap.CubemapFramebuffer;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.ReflectionProbeComponent;
import org.anchor.game.client.shaders.ForwardStaticShader;
import org.anchor.game.client.shaders.SkyShader;
import org.anchor.game.client.types.ClientScene;
import org.anchor.game.client.types.ClientShader;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class CubemapGenerator {

    public static void generate(ClientScene scene) {
        for (Entity entity : scene.getEntitiesWithComponent(ReflectionProbeComponent.class)) // Performance doesn't matter
            generate(entity);
    }

    public static void generate(Entity entity) {
        float fov = Settings.fov;
        Vector3f cameraPosition = new Vector3f(GameClient.getPlayer().getPosition());

        LivingComponent livingComponent = GameClient.getPlayer().getComponent(LivingComponent.class);
        livingComponent.setEyePosition(entity.getPosition());

        float pitch = livingComponent.pitch;
        float yaw = livingComponent.yaw;

        Settings.fov = 90;
        Settings.width = Settings.reflectionProbeSize;
        Settings.height = Settings.reflectionProbeSize;
        Settings.bakedGeneration = true;
        Renderer.refreshProjectionMatrix();
        GL11.glClearColor(1, 0, 0, 1);

        livingComponent.roll = 180;
        CubemapFramebuffer cubemap = entity.getComponent(ReflectionProbeComponent.class).cubemap;
        cubemap.bindFramebuffer();

        Map<Model, List<Entity>> renderables = new HashMap<Model, List<Entity>>();
        for (Entity render : Engine.scene.getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent component = render.getComponent(MeshComponent.class);
            if (component.shader == null || component.model == null || !component.model.isLoaded())
                continue;

            if (!renderables.containsKey(component.model))
                renderables.put(component.model, new ArrayList<Entity>());
            renderables.get(component.model).add(render);
        }

        cubemap.startMipmapRender(0);
        for (int i = 0; i < 6; i++) {
            livingComponent.pitch = cubemap.getPitch(i);
            livingComponent.yaw = cubemap.getYaw(i);
            livingComponent.updateViewMatrix();

            cubemap.startFaceRender(i);

            ClientShader shader = ForwardStaticShader.getInstance();
            shader.start();
            for (Entry<Model, List<Entity>> entry : renderables.entrySet()) {
                Renderer.bind(entry.getKey());

                inner: for (Entity render : entry.getValue()) {
                    if (render.getComponent(MeshComponent.class).shader == SkyShader.getInstance())
                        continue inner;

                    render.getComponent(MeshComponent.class).material.bind();
                    shader.loadEntitySpecificInformation(render);
                    Renderer.render(entry.getKey());
                }

                Renderer.unbind(entry.getKey());
            }
            shader.stop();
        }

        Settings.fov = fov;
        Settings.width = Display.getWidth();
        Settings.height = Display.getHeight();
        Settings.bakedGeneration = true;
        Renderer.refreshProjectionMatrix();
        GL11.glClearColor(Settings.clearR, Settings.clearG, Settings.clearB, 1);

        livingComponent.roll = 0f;
        GameClient.getPlayer().getPosition().set(cameraPosition);
        livingComponent.pitch = pitch;
        livingComponent.yaw = yaw;
        livingComponent.updateViewMatrix();
    }

}
