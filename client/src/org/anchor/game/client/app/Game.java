package org.anchor.game.client.app;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.engine.common.app.App;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.components.IInteractable;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.types.ClientScene;
import org.anchor.game.client.utils.KeyboardUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public abstract class Game extends App {

    public Entity player;
    public ClientScene scene;
    public SkyComponent sky;
    public DeferredShading deferred;
    public Shadows shadows;
    public Framebuffer sceneFBO;
    public LivingComponent livingComponent;

    public static final float REACH_DISTANCE = 4;

    public List<Light> getLights() {
        List<Light> lights = new ArrayList<Light>();
        if (scene == null)
            return lights;

        for (IComponent component : scene.getComponents(LightComponent.class))
            lights.add((Light) component);

        return lights;
    }

    public void checkForInteractions() {
        if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_E)) {
            for (Entity entity : scene.getEntities()) {
                IInteractable interactable = entity.getComponent(IInteractable.class);
                if (interactable == null)
                    continue;

                MeshComponent mesh = entity.getComponent(MeshComponent.class);
                PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

                if (mesh != null && physics == null) {
                    AABB aabb = mesh.getAABB();
                    if (aabb != null) {
                        Vector3f point = aabb.raycast(livingComponent.getEyePosition(), livingComponent.getForwardVector());
                        if (point != null && Vector3f.sub(livingComponent.getEyePosition(), point, null).lengthSquared() <= REACH_DISTANCE)
                            interactable.interact();
                    }
                } else if (physics != null) {
                    Vector3f point = physics.raycast(livingComponent.getEyePosition(), livingComponent.getForwardVector());
                    System.out.println(Vector3f.sub(livingComponent.getEyePosition(), point, null).lengthSquared());
                    if (point != null && Vector3f.sub(livingComponent.getEyePosition(), point, null).lengthSquared() <= REACH_DISTANCE)
                        interactable.interact();
                }
            }
        }
    }

}
