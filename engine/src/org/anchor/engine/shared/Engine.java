package org.anchor.engine.shared;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.events.EventBus;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.EntityRaycast;
import org.anchor.engine.shared.utils.Layer;
import org.lwjgl.util.vector.Vector3f;

public class Engine {

    public static Scene scene;
    public static EventBus bus = new EventBus();

    public static int PROTOCOL_VERSION = 1;

    public static Scene getScene() {
        return scene;
    }

    public static List<Entity> getEntities() {
        if (scene == null)
            return new ArrayList<Entity>();

        return scene.getEntities();
    }

    public static List<Entity> getEntitiesWithComponent(Class<? extends IComponent> clazz) {
        if (scene == null)
            return new ArrayList<Entity>();

        return scene.getEntitiesWithComponent(clazz);
    }

    public static <T extends IComponent> List<T> getComponents(Class<T> clazz) {
        if (scene == null)
            return new ArrayList<T>();

        return scene.getComponents(clazz);
    }

    public static List<Terrain> getTerrains() {
        if (scene == null)
            return new ArrayList<Terrain>();

        return scene.getTerrains();
    }

    public static List<Layer> getLayers() {
        if (scene == null)
            return new ArrayList<Layer>();

        return scene.getLayers();
    }

    public static List<String> getLayerNames() {
        if (scene == null)
            return new ArrayList<String>();

        return scene.getLayerNames();
    }

    public static Layer getLayerByName(String name) {
        if (scene == null)
            return null;

        return scene.getLayerByName(name);
    }

    public static Layer getDefaultLayer() {
        if (scene == null)
            return null;

        return scene.getDefaultLayer();
    }

    public static Vector3f getSpawn() {
        return scene.getSpawn();
    }

    public static EntityRaycast raycast(Vector3f origin, Vector3f ray) {
        ray.normalise();

        float distance = Float.MAX_VALUE;
        Entity closest = null;

        for (Entity entity : getEntitiesWithComponent(PhysicsComponent.class)) {
            float dist = entity.getComponent(PhysicsComponent.class).raycastDistance(origin, ray);
            if (dist == -1)
                continue;

            if (dist < distance) {
                distance = dist;
                closest = entity;
            }
        }

        return new EntityRaycast(closest, distance, ray, origin);
    }

}
