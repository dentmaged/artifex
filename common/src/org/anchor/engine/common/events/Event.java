package org.anchor.engine.common.events;

import org.anchor.engine.common.events.handler.HandlerList;

public abstract class Event {

    public abstract HandlerList getHandlers();

}
