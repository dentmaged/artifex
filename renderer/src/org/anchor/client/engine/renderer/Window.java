package org.anchor.client.engine.renderer;

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.PixelFormat;

public class Window {

    private static long lastFrameTime;
    private static float delta, timeScale = 1;

    public static void create(String title) {
        try {
            DisplayMode selected = new DisplayMode(Settings.width, Settings.height);

            for (DisplayMode mode : Display.getAvailableDisplayModes())
                if (mode.getWidth() == Settings.width && mode.getHeight() == Settings.height && mode.isFullscreenCapable())
                    selected = mode;

            Display.setFullscreen(Settings.fullscreen);
            Display.setDisplayMode(selected);
            Display.create(new PixelFormat().withDepthBits(24));
            Display.setTitle(title);

            GL11.glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isRunning() {
        return !Display.isCloseRequested();
    }

    public static void endFrame() {
        Display.sync(!Display.isActive() && Settings.lowerFPSFocus ? 3 : Settings.maxFPS);
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
