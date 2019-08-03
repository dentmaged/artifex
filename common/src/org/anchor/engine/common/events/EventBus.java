package org.anchor.engine.common.events;

import java.lang.reflect.Method;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.events.handler.EventHandler;
import org.anchor.engine.common.events.handler.Handler;
import org.anchor.engine.common.events.handler.HandlerList;

public class EventBus {

    public void registerEvents(Listener listener) {
        try {
            for (Method method : listener.getClass().getMethods()) {
                if (method.getAnnotation(EventHandler.class) == null)
                    continue;

                if (method.getParameterCount() != 1) {
                    Log.warning(method.getName() + " in " + listener.getClass().getCanonicalName() + " has an EventHandler annotation but doesn't have one argument! Skipping.");
                    continue;
                }

                Class<?> argumentClass = method.getParameters()[0].getType();
                if (Event.class.isAssignableFrom(argumentClass))
                    ((HandlerList) argumentClass.getMethod("getHandlerList").invoke(null)).handlers.add(new Handler(listener, method));
                else
                    Log.warning(method.getName() + " in " + listener.getClass().getCanonicalName() + " has an EventHandler annotation and one argument, but its argument is not an Event! Skipping.");
            }
        } catch (Exception e) {
            Log.warning("Error whilst registering listener " + listener.getClass().getCanonicalName() + ". Make sure your Event has a getHandlerList() method, that returns the same as getHandler()!");
            e.printStackTrace();
        }
    }

    public void fireEvent(Event event) {
        event.getHandlers().invoke(event);
    }

}
