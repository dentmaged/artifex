package org.anchor.engine.shared.events;

import org.anchor.engine.common.events.Event;
import org.anchor.engine.common.events.handler.HandlerList;
import org.anchor.engine.shared.console.GameVariable;

public class GameVariableUpdateEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private GameVariable variable;

    public GameVariableUpdateEvent(GameVariable variable) {
        this.variable = variable;
    }

    public GameVariable getVariable() {
        return variable;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
