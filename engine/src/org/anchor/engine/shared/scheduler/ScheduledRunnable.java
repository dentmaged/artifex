package org.anchor.engine.shared.scheduler;

public interface ScheduledRunnable {

    public void finish();

    public void tick(float time, float percentage);

}
