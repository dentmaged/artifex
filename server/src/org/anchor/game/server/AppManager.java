package org.anchor.game.server;

import org.anchor.engine.common.app.App;

public class AppManager {

    private static App instance;
    public static float delta, timeScale = 1;
    public static boolean running = true;

    private static final float NANO_TO_SECONDS = 1f / 1000000000f;

    public static void create(App app) {
        instance = app;

        app.init();
        while (running) {
            long nano = System.nanoTime();
            app.update();

            delta = (System.nanoTime() - nano) * NANO_TO_SECONDS;
        }
        app.shutdown();
    }

    public static float getFrameTimeSeconds() {
        return delta * timeScale;
    }

    public static App getInstance() {
        return instance;
    }

}
