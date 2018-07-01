package org.anchor.game.client;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.types.cubemap.CubemapFramebuffer;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.ReflectionProbeComponent;
import org.anchor.game.client.types.ClientScene;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

public class CubemapGenerator {

    public void generate(ClientScene scene) {
        float fov = Settings.fov;
        Vector3f cameraPosition = new Vector3f(GameClient.getPlayer().getPosition());

        LivingComponent livingComponent = GameClient.getPlayer().getComponent(LivingComponent.class);
        float pitch = livingComponent.pitch;
        float yaw = livingComponent.yaw;

        Settings.fov = 90;
        Settings.width = Settings.reflectionProbeSize;
        Settings.height = Settings.reflectionProbeSize;
        Renderer.refreshProjectionMatrix();

        for (Entity entity : scene.getEntitiesWithComponent(ReflectionProbeComponent.class)) {
            CubemapFramebuffer cubemap = entity.getComponent(ReflectionProbeComponent.class).cubemap;
            cubemap.bindFramebuffer();
            livingComponent.setEyePosition(entity.getPosition());

            for (int i = 0; i < 6; i++) {
                livingComponent.pitch = cubemap.getPitch(i);
                livingComponent.yaw = cubemap.getYaw(i);

                cubemap.startFaceRender(i);
                scene.render();
            }
        }

        Settings.fov = fov;
        Settings.width = Display.getWidth();
        Settings.height = Display.getHeight();
        Renderer.refreshProjectionMatrix();

        GameClient.getPlayer().getPosition().set(cameraPosition);
        livingComponent.pitch = pitch;
        livingComponent.yaw = yaw;
    }

}
