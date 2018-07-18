package org.anchor.engine.shared;

import java.lang.reflect.Field;
import java.util.List;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.EntityRaycast;
import org.anchor.engine.shared.utils.Side;
import org.lwjgl.util.vector.Vector3f;

public class Engine {

    private static Side side;
    private static Engine instance;

    public static Scene scene;

    public static int PROTOCOL_VERSION = 1;

    public static void init(Side side) {
        init(side, new Engine());
    }

    public static void init(Side side, Engine instance) {
        Engine.side = side;
        Engine.instance = instance;
    }

    public static List<Entity> getEntities() {
        return scene.getEntities();
    }

    public static List<Entity> getEntitiesWithComponent(Class<? extends IComponent> clazz) {
        return scene.getEntitiesWithComponent(clazz);
    }

    public static List<Terrain> getTerrains() {
        return scene.getTerrains();
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

    public void broadcast(IPacket packet) {
        
    }

    public static Engine getInstance() {
        return instance;
    }

    public static boolean isClientSide() {
        return side == Side.CLIENT;
    }

    public static boolean isServerSide() {
        return side == Side.SERVER;
    }

    public void onEntityCreate(Entity entity) {

    }

    public void onEntityPreSpawn(Entity entity) {

    }

    public void onEntitySpawn(Entity entity) {

    }

    public void onComponentAdd(Entity entity, IComponent component) {

    }

    public void onComponentRemove(Entity entity, IComponent component) {

    }

    public void onEntityKeyValueChange(Entity entity, String key, String value) {

    }

    public void onMonitorVariableChange(Object parent, Object target, Field field, Object previous, Object current) {

    }

    public void onEntityDestroy(Entity entity) {

    }

}
