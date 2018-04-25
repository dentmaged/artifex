package org.anchor.game.client.app;

import org.anchor.client.engine.renderer.Window;
import org.anchor.engine.common.app.App;

public class AppManager {

    private static App instance;

    public static void create(App app) {
        Window.create(app.getTitle());
        instance = app;

        app.init();
        Window.startFrameTime();
        while (Window.isRunning()) {
            app.update();
            app.render();

            Window.endFrame();
        }
        app.shutdown();
    }

    public static float getFrameTimeSeconds() {
        return Window.getFrameTimeSeconds();
    }

    public static App getInstance() {
        return instance;
    }

}
