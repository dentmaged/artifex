package org.anchor.engine.shared.scheduler;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {

    private static final List<ScheduledEvent> events = new ArrayList<ScheduledEvent>();

    public static void tick() {
        for (int i = 0; i < events.size(); i++)
            events.get(i).tick();
    }

    public static ScheduledEvent schedule(ScheduledRunnable runnable, float duration) {
        ScheduledEvent event = new ScheduledEvent(runnable, duration);
        events.add(event);

        return event;
    }

    public static void cancel(ScheduledEvent event) {
        events.remove(event);
    }

}
