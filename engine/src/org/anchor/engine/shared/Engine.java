package org.anchor.engine.shared;

import java.lang.reflect.Field;

import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.Side;

public class Engine {

    private static Side side;
    private static Engine instance;

    public static void init(Side side) {
        init(side, new Engine());
    }

    public static void init(Side side, Engine instance) {
        Engine.side = side;
        Engine.instance = instance;
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

}
