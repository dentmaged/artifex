package org.anchor.engine.shared.events.entity;

import org.anchor.engine.common.events.Event;
import org.anchor.engine.common.events.handler.HandlerList;
import org.anchor.engine.shared.entity.Entity;

public class EntitySpawnEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private Entity entity;

    public EntitySpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
