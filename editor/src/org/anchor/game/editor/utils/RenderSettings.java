package org.anchor.game.editor.utils;

import org.anchor.client.engine.renderer.Settings;

public class RenderSettings {

    public static void set(String key, String value) {
        if (key.equals("Wireframe"))
            Settings.wireframe = parseBoolean(value);

        if (key.equals("Procedural Skybox"))
            Settings.proceduralSky = parseBoolean(value);

        if (key.equals("Perform Lighting"))
            Settings.performLighting = parseBoolean(value);

        if (key.equals("Perform SSAO"))
            Settings.performSSAO = parseBoolean(value);

        if (key.equals("Show Lightmaps"))
            Settings.showLightmaps = parseBoolean(value);

        if (key.equals("Minimum Diffuse"))
            Settings.minDiffuse = parseFloat(value);

        if (key.equals("Fog Density"))
            Settings.density = parseFloat(value);

        if (key.equals("Fog Gradient"))
            Settings.gradient = parseFloat(value);

        if (key.equals("Exposure Speed"))
            Settings.exposureSpeed = parseFloat(value);
    }

    private static boolean parseBoolean(String value) {
        if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("Yes") || value.equalsIgnoreCase("T") || value.equalsIgnoreCase("True") || value.equals("1"))
            return true;

        return false;
    }

    private static float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
        }

        return 0;
    }

}
