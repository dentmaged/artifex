package org.anchor.game.client.app;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Window;
import org.anchor.engine.common.app.App;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

public class AppManager {

    private static App instance;
    public static float gpuTime;
    public static float cpuTime;

    private static final float NANO_TO_MILLI = 1f / 1000000f;

    public static void create(App app) {
        Window.create(app.getTitle());
        instance = app;

        app.init();
        Graphics.checkForErrors();
        int query = GL15.glGenQueries();
        Window.startFrameTime();
        while (Window.isRunning()) {
            try {
                long nano = System.nanoTime();
                app.update();

                GL15.glBeginQuery(GL33.GL_TIME_ELAPSED, query);
                app.render();
                GL15.glEndQuery(GL33.GL_TIME_ELAPSED);

                cpuTime = (System.nanoTime() - nano) * NANO_TO_MILLI;
                gpuTime = GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT) * NANO_TO_MILLI;
                Window.endFrame();
                Graphics.checkForErrors();
            } catch (Throwable t) {
                if (!(t instanceof IllegalAccessError))
                    t.printStackTrace();
                break;
            }
        }
        app.shutdown();

        System.exit(0);
        throw new Error();
    }

    public static float getFrameTimeSeconds() {
        return Window.getFrameTimeSeconds();
    }

    public static App getInstance() {
        return instance;
    }

}
