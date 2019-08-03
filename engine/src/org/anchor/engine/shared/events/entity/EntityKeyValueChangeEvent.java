package org.anchor.engine.shared.events.entity;

import org.anchor.engine.common.events.Event;
import org.anchor.engine.common.events.handler.HandlerList;
import org.anchor.engine.shared.entity.Entity;

public class EntityKeyValueChangeEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private Entity entity;
    private String key, value;

    public EntityKeyValueChangeEvent(Entity entity, String key, String value) {
        this.entity = entity;
        this.key = key;
        this.value = value;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
