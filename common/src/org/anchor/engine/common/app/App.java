package org.anchor.engine.common.app;

public abstract class App {

    public abstract void init();

    public abstract void update();

    public abstract void render();

    public abstract void shutdown();

    public abstract void resize(int width, int height);

    public abstract String getTitle();

}
