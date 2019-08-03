package org.anchor.engine.shared.events.component;

import org.anchor.engine.common.events.Event;
import org.anchor.engine.common.events.handler.HandlerList;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;

public class AddComponentEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private Entity entity;
    private IComponent component;

    public AddComponentEvent(Entity entity, IComponent component) {
        this.entity = entity;
        this.component = component;
    }

    public Entity getEntity() {
        return entity;
    }

    public IComponent getComponent() {
        return component;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
