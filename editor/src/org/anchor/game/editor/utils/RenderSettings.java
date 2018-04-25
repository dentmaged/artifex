package org.anchor.game.editor.utils;

import org.anchor.client.engine.renderer.bloom.Bloom;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.game.client.components.SkyComponent;

public class RenderSettings {

    public static boolean wireframe;

    public static void set(String key, String value) {
        if (key.equals("Wireframe"))
            wireframe = parseBoolean(value);

        if (key.equals("Show Lightmaps"))
            DeferredShading.showLightmaps = parseBoolean(value);

        if (key.equals("Minimum Diffuse"))
            DeferredShading.minDiffuse = parseFloat(value);

        if (key.equals("Sky Time Scale"))
            SkyComponent.TIME_SCALE = parseFloat(value);

        if (key.equals("Fog Density"))
            DeferredShading.density = parseFloat(value);

        if (key.equals("Fog Gradient"))
            DeferredShading.gradient = parseFloat(value);

        if (key.equals("Exposure"))
            Bloom.exposure = parseFloat(value);
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
