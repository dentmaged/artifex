package org.anchor.game.server;

import java.lang.reflect.Field;

import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.packet.EntityAddComponentPacket;
import org.anchor.engine.shared.net.packet.EntityComponentVariableChangePacket;
import org.anchor.engine.shared.net.packet.EntityKeyValuePacket;
import org.anchor.engine.shared.net.packet.EntityRemoveComponentPacket;
import org.anchor.engine.shared.net.packet.EntitySpawnPacket;
import org.anchor.game.server.filter.AllPlayersFilter;

public class ServerEngine extends Engine {

    public static int ENTITY_ID = 1;

    @Override
    public void onEntityCreate(Entity entity) {
        entity.setId(ENTITY_ID++);
    }

    @Override
    public void onEntitySpawn(Entity entity) {
        new AllPlayersFilter().sendPacket(new EntitySpawnPacket(entity));
    }

    @Override
    public void onComponentAdd(Entity entity, IComponent component) {
        new AllPlayersFilter().sendPacket(new EntityAddComponentPacket(entity.getId(), component));
    }

    @Override
    public void onComponentRemove(Entity entity, IComponent component) {
        new AllPlayersFilter().sendPacket(new EntityRemoveComponentPacket(entity.getId(), component.getClass()));
    }

    @Override
    public void onEntityKeyValueChange(Entity entity, String key, String value) {
        new AllPlayersFilter().sendPacket(new EntityKeyValuePacket(entity.getId(), key, value));
    }

    @Override
    public void onMonitorVariableChange(Object parent, Object target, Field field, Object previous, Object current) {
        new AllPlayersFilter().sendPacket(new EntityComponentVariableChangePacket((Entity) parent, (IComponent) target, field));
    }

}
