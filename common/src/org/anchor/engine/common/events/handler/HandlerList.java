package org.anchor.engine.common.events.handler;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.events.Event;
import org.anchor.engine.common.events.Listener;

public class HandlerList {

    public List<Handler> handlers = new ArrayList<Handler>();

    public void invoke(Event event) {
        try {
            for (Handler handler : handlers)
                handler.getMethod().invoke(handler.getListener(), event);
        } catch (Exception e) {
            Log.warning("Exception thrown whilst handling event " + event.getClass().getCanonicalName());
            e.printStackTrace();
        }
    }

    public void removeHandler(Listener listener) {
        for (int i = 0; i < handlers.size(); i++) {
            if (handlers.get(i).getListener().equals(listener)) {
                handlers.remove(i);
                break;
            }
        }
    }

}
