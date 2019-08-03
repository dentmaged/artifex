package org.anchor.engine.shared.events;

import java.lang.reflect.Field;

import org.anchor.engine.common.events.Event;
import org.anchor.engine.common.events.handler.HandlerList;

public class MonitorVariableChangeEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    private Object parent, target, previous, current;
    private Field field;

    public MonitorVariableChangeEvent(Object parent, Object target, Field field, Object previous, Object current) {
        this.parent = parent;
        this.target = target;
        this.field = field;
        this.previous = previous;
        this.current = current;
    }

    public Object getParent() {
        return parent;
    }

    public Object getTarget() {
        return target;
    }

    public Object getPrevious() {
        return previous;
    }

    public Object getCurrent() {
        return current;
    }

    public Field getField() {
        return field;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
