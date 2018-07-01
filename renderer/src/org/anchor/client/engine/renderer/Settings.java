package org.anchor.client.engine.renderer;

public class Settings {

    public static int width = 1280;
    public static int height = 720;
    public static int maxFPS = 300;
    public static boolean fullscreen = false;

    public static float fov = 90;
    public static final float nearPlane = 0.1f;
    public static final float farPlane = 5000;
    public static int anisotropyLevel = 4;

    public static boolean showLightmaps = false;
    public static boolean performLighting = true;
    public static boolean performSSAO = true;
    public static boolean wireframe = false;

    public static float exposureSpeed = 0.01f;
    public static float minDiffuse = 0.05f;

    public static float density = 0.002f;
    public static float gradient = 5;

    public static boolean proceduralSky = false;
    public static String skybox = "skybox/partly-cloudy-day/";

    public static final int shadowSplits = 3;
    public static final int[] shadowExtents = { 30, 100, 200 };
    public static final float shadowDistance = 50;
    public static final int shadowResolution = 2048;

    public static final int reflectionProbeSize = 512;

}
