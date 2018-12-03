package org.anchor.engine.shared.scheduler;

import org.anchor.engine.shared.physics.PhysicsEngine;

public class ScheduledEvent {

    private ScheduledRunnable runnable;
    private float time, duration;

    public ScheduledEvent(ScheduledRunnable runnable, float time) {
        this.runnable = runnable;
        this.time = time;
        this.duration = time;
    }

    public void tick() {
        time -= PhysicsEngine.TICK_DELAY;

        if (time > 0) {
            runnable.tick(time, time / duration);
        } else {
            runnable.tick(time, time / duration);
            runnable.finish();
            Scheduler.cancel(this);
        }
    }

}
