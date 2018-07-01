package org.anchor.engine.shared.scheduler;

import org.anchor.engine.shared.physics.PhysicsEngine;

public class ScheduledEvent {

    private IRunnable runnable;
    private float time, duration;

    public ScheduledEvent(IRunnable runnable, float time) {
        this.runnable = runnable;
        this.time = time;
        this.duration = time;
    }

    public void tick() {
        time -= PhysicsEngine.TICK_DELAY;

        if (time > 0) {
            runnable.tick(time, time / duration);
        } else {
            runnable.finish();
            Scheduler.cancel(this);
        }
    }

}
