package org.anchor.engine.shared.events;

import org.anchor.engine.common.events.Event;
import org.anchor.engine.common.events.handler.HandlerList;

public class EventTemplate extends Event {

    private static HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
