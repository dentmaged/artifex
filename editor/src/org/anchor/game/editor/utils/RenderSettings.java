package org.anchor.game.editor.utils;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.game.client.ClientGameVariables;

public class RenderSettings {

    public static void set(String key, String value) {
        if (key.equals("Wireframe"))
            ClientGameVariables.r_wireframe.setValue(value);

        if (key.equals("Procedural Skybox"))
            Settings.proceduralSky = parseBoolean(value);

        if (key.equals("Perform Lighting"))
            ClientGameVariables.r_performLighting.setValue(value);

        if (key.equals("Perform SSAO"))
            ClientGameVariables.r_performSSAO.setValue(value);

        if (key.equals("Show Lightmaps"))
            ClientGameVariables.r_showLightmaps.setValue(value);

        if (key.equals("Fog Density"))
            Settings.density = parseFloat(value);

        if (key.equals("Fog Gradient"))
            Settings.gradient = parseFloat(value);

        if (key.equals("Exposure Speed"))
            Settings.exposureSpeed = parseFloat(value);

        if (key.equals("SSAO Bias"))
            Settings.ssaoBias = parseFloat(value);

        if (key.equals("SSAO Radius"))
            Settings.ssaoRadius = parseFloat(value);

        if (key.equals("Volumetric Scattering"))
            Settings.volumetricScattering = parseFloat(value);

        if (key.equals("Ambient Strength"))
            Settings.ambientScale = parseFloat(value);
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
