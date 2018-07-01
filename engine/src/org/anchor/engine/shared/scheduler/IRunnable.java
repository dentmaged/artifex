package org.anchor.engine.shared.scheduler;

public interface IRunnable {

    public void finish();

    public void tick(float time, float percentage);

}
