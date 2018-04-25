package org.anchor.client.engine.renderer;

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

public class Window {

    public static int WIDTH = 1280;
    public static int HEIGHT = 720;
    public static final int FPS_CAP = 300;

    private static long lastFrameTime;
    private static float delta, timeScale = 1;

    public static void create(String title) {
        try {
            DisplayMode selected = new DisplayMode(WIDTH, HEIGHT);

            for (DisplayMode mode : Display.getAvailableDisplayModes())
                if (mode.getWidth() == WIDTH && mode.getHeight() == HEIGHT && mode.isFullscreenCapable())
                    selected = mode;

            Display.setDisplayMode(selected);
            Display.create(new PixelFormat().withDepthBits(24));
            Display.setTitle(title);

            GL11.glEnable(GL13.GL_MULTISAMPLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isRunning() {
        return !Display.isCloseRequested();
    }

    public static void endFrame() {
        Display.sync(FPS_CAP);
        Display.update();

        long currentFrameTime = getCurrentTime();
        delta = (currentFrameTime - lastFrameTime) / 1000f;
        lastFrameTime = currentFrameTime;
    }

    public static void startFrameTime() {
        lastFrameTime = getCurrentTime();
    }

    private static long getCurrentTime() {
        return Sys.getTime() * 1000 / Sys.getTimerResolution();
    }

    public static float getFrameTimeSeconds() {
        return delta * timeScale;
    }

}
