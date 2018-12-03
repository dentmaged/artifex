package org.anchor.game.client.app;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.fog.Fog;
import org.anchor.client.engine.renderer.ibl.IBL;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.engine.common.app.App;
import org.anchor.engine.common.net.client.Client;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.shared.components.IInteractable;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.types.ClientScene;
import org.lwjgl.util.vector.Vector3f;

public abstract class Game extends App {

    public Entity player;
    public ClientScene scene;
    public SkyComponent sky;
    public DeferredShading deferred;
    public IBL ibl;
    public Fog fog;
    public Shadows shadows;
    public LivingComponent livingComponent;
    public Client client;

    public static final float REACH_DISTANCE = 4;

    public List<Light> getLights() {
        List<Light> lights = new ArrayList<Light>();
        if (scene == null)
            return lights;

        for (IComponent component : scene.getComponents(LightComponent.class))
            lights.add((Light) component);

        return lights;
    }

    public void checkForInteractions(boolean interacting) {
        if (interacting) {
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
                    if (point != null && Vector3f.sub(livingComponent.getEyePosition(), point, null).lengthSquared() <= REACH_DISTANCE)
                        interactable.interact();
                }
            }
        }
    }

}
