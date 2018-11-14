package org.anchor.client.engine.renderer;

public class Settings {

    public static int width = 1280;
    public static int height = 720;
    public static int maxFPS = 300;
    public static boolean fullscreen = false;

    public static float fov = 90;
    public static float nearPlane = 0.1f;
    public static float farPlane = 5000;
    public static int anisotropyLevel = 4;

    public static boolean lowerFPSFocus = true;
    public static boolean bakedGeneration = false;

    public static float clearR = 0;
    public static float clearG = 0;
    public static float clearB = 0;

    public static int maxProbes = 4;
    public static int maxLights = 20;
    public static int maxJoints = 50;
    public static int maxWeights = 3;

    public static float ssaoBias = 0.025f;
    public static float ssaoRadius = 0.5f;

    public static float irradianceScale = 0.75f;
    public static float ambientScale = 0.5f;
    public static float exposureSpeed = 0.001f;

    public static float density = 0.002f;
    public static float gradient = 5;

    public static boolean proceduralSky = false;
    public static String skybox = "skybox/partly-cloudy-day/";

    public static final int shadowSplits = 3;
    public static final int[] shadowExtents = {
            30, 100, 200
    };
    public static final float shadowDistance = 50;
    public static final int shadowResolution = 2048;

    public static final int reflectionProbeSize = 256;

}
