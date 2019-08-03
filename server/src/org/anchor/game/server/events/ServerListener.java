package org.anchor.game.server.events;

import org.anchor.engine.common.events.Listener;
import org.anchor.engine.common.events.handler.EventHandler;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.events.GameVariableUpdateEvent;
import org.anchor.engine.shared.events.MonitorVariableChangeEvent;
import org.anchor.engine.shared.events.component.AddComponentEvent;
import org.anchor.engine.shared.events.component.RemoveComponentEvent;
import org.anchor.engine.shared.events.entity.EntityCreateEvent;
import org.anchor.engine.shared.events.entity.EntityDestroyEvent;
import org.anchor.engine.shared.events.entity.EntityKeyValueChangeEvent;
import org.anchor.engine.shared.events.entity.EntitySpawnEvent;
import org.anchor.engine.shared.net.packet.EntityAddComponentPacket;
import org.anchor.engine.shared.net.packet.EntityComponentVariableChangePacket;
import org.anchor.engine.shared.net.packet.EntityDestroyPacket;
import org.anchor.engine.shared.net.packet.EntityKeyValuePacket;
import org.anchor.engine.shared.net.packet.EntityRemoveComponentPacket;
import org.anchor.engine.shared.net.packet.EntitySpawnPacket;
import org.anchor.engine.shared.net.packet.GameVariablePacket;
import org.anchor.game.server.GameServer;
import org.anchor.game.server.filter.AllPlayersFilter;

public class ServerListener implements Listener {

    @EventHandler
    public void onEntityCreate(EntityCreateEvent event) {
        event.getEntity().setId(GameServer.ENTITY_ID++);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        new AllPlayersFilter().sendPacket(new EntitySpawnPacket(event.getEntity()));
    }

    @EventHandler
    public void onComponentAdd(AddComponentEvent event) {
        Entity entity = event.getEntity();

        if (entity.hasSpawned())
            new AllPlayersFilter().sendPacket(new EntityAddComponentPacket(entity.getId(), event.getComponent()));
    }

    @EventHandler
    public void onComponentRemove(RemoveComponentEvent event) {
        Entity entity = event.getEntity();

        if (entity.hasSpawned())
            new AllPlayersFilter().sendPacket(new EntityRemoveComponentPacket(entity.getId(), event.getComponent().getClass()));
    }

    @EventHandler
    public void onEntityKeyValueChange(EntityKeyValueChangeEvent event) {
        Entity entity = event.getEntity();

        if (entity.hasSpawned())
            new AllPlayersFilter().sendPacket(new EntityKeyValuePacket(entity.getId(), event.getKey(), event.getValue()));
    }

    @EventHandler
    public void onMonitorVariableChange(MonitorVariableChangeEvent event) {
        Entity parent = (Entity) event.getParent();

        if (parent.hasSpawned())
            new AllPlayersFilter().sendPacket(new EntityComponentVariableChangePacket(parent, (IComponent) event.getTarget(), event.getField()));
    }

    @EventHandler
    public void onEntityDestroy(EntityDestroyEvent event) {
        new AllPlayersFilter().sendPacket(new EntityDestroyPacket(event.getEntity().getId()));
    }

    @EventHandler
    public void onGameVariableUpdate(GameVariableUpdateEvent event) {
        new AllPlayersFilter().sendPacket(new GameVariablePacket(event.getVariable().getName(), event.getVariable().getValueAsString()));
    }

}
