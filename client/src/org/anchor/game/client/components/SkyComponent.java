/*
This software includes code from Emerald Engine:

MIT License

Copyright (c) 2018 Lage Ragnarsson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.types.cubemap.BakedCubemap;
import org.anchor.client.engine.renderer.types.cubemap.CubemapRequest;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.shaders.SkyShader;
import org.lwjgl.util.vector.Vector3f;

public class SkyComponent implements IComponent {

    public Entity entity;

    @Property("Light")
    protected Entity light;
    protected LightComponent lightComponent;

    public float interp, interp_night, altitude, azimuth;
    public Vector3f direction = new Vector3f(), sunColour = new Vector3f(), baseColour = new Vector3f(NOON_BASE), topColour = new Vector3f(NOON_TOP);
    public CubemapRequest skybox;
    public BakedCubemap irradiance, prefilter;

    private float calculatedAltitude = Mathf.asin(Mathf.cos(Mathf.toRadians(40)));
    private boolean loaded;

    public static float ALTITUDE_MARGIN = -0.12f;

    private static Vector3f DAWN_BASE = new Vector3f(0.5f, 0.15f, 0.4f), DAWN_TOP = new Vector3f(0.1f, 0.1f, 0.65f);
    private static Vector3f NOON_BASE = new Vector3f(0.34f, 0.54f, 0.88f), NOON_TOP = new Vector3f(0.1f, 0.4f, 1);
    private static Vector3f DUSK_BASE = new Vector3f(0.9f, 0.4f, 0.1f), DUSK_TOP = new Vector3f(0.5f, 0.4f, 0.3f);
    private static Vector3f NIGHT_BASE = new Vector3f(0, 0.01f, 0.05f), NIGHT_TOP = new Vector3f(0, 0, 0.01f);

    private static Vector3f DAWN_SUN = new Vector3f(0.8f, 0.3f, 0.2f);
    private static Vector3f NOON_SUN = new Vector3f(0.5f, 0.35f, 0.2f);
    private static Vector3f DUSK_SUN = new Vector3f(0.9f, 0.3f, 0.1f);
    private static Vector3f NIGHT_SUN = new Vector3f(0, 0, 0);

    @Override
    public void precache(Entity entity) {
        this.entity = entity;

        float scale = Settings.farPlane / 1.5f;
        entity.getScale().set(scale, scale, scale);
        entity.getComponent(MeshComponent.class).shader = SkyShader.getInstance();
        entity.getComponent(MeshComponent.class).castsShadows = false;
        entity.getComponent(MeshComponent.class).disableFrustumCulling = true;
        entity.setValue("backface", "false");
        entity.setHidden(false);

        skybox = Requester.requestCubemap(new String[] { Settings.skybox + "px", Settings.skybox + "nx", Settings.skybox + "py", Settings.skybox + "ny", Settings.skybox + "pz", Settings.skybox + "nz" });
    }

    @Override
    public void spawn() {

    }

    @Override
    public void update() {
        entity.getPosition().set(GameClient.getPlayer().getPosition());

        if (Settings.proceduralSky)
            updateSky();

        if (!loaded && skybox != null && skybox.isLoaded()) {
            loaded = true;

            irradiance = new BakedCubemap(32, "irradianceConvolution", 1);
            prefilter = new BakedCubemap(Settings.reflectionProbeSize, "prefilter", 8);

            irradiance.perform(skybox.getTexture());
            prefilter.perform(skybox.getTexture());
        }
    }

    protected void updateSky() {
        if (light != null)
            direction = lightComponent.getDirection();
        azimuth = Mathf.asin(direction.x);
        altitude = Mathf.asin(direction.y);

        float max_altitude = calculatedAltitude - ALTITUDE_MARGIN;
        float min_altitude = calculatedAltitude + ALTITUDE_MARGIN;

        interp = (max_altitude - altitude + ALTITUDE_MARGIN) / max_altitude;
        interp_night = Mathf.pow((min_altitude + altitude - ALTITUDE_MARGIN) / min_altitude, 4);

        if (altitude >= ALTITUDE_MARGIN && azimuth <= 0)
            sunColour = Vector3f.add(VectorUtils.mul(DAWN_SUN, interp), VectorUtils.mul(NOON_SUN, (1 - interp)), null);
        else if (altitude >= ALTITUDE_MARGIN && azimuth > 0)
            sunColour = Vector3f.add(VectorUtils.mul(DUSK_SUN, interp), VectorUtils.mul(NOON_SUN, (1 - interp)), null);
        else if (altitude < ALTITUDE_MARGIN && azimuth >= 0)
            sunColour = NIGHT_SUN;
        else if (altitude < ALTITUDE_MARGIN && azimuth < 0)
            sunColour = NIGHT_SUN;

        if (altitude >= ALTITUDE_MARGIN && azimuth <= 0) {
            baseColour = Vector3f.add(VectorUtils.mul(DAWN_BASE, interp), VectorUtils.mul(NOON_BASE, (1 - interp)), null);
            topColour = Vector3f.add(VectorUtils.mul(DAWN_TOP, interp), VectorUtils.mul(NOON_TOP, (1 - interp)), null);
        } else if (altitude >= ALTITUDE_MARGIN && azimuth > 0) {
            baseColour = Vector3f.add(VectorUtils.mul(DUSK_BASE, interp), VectorUtils.mul(NOON_BASE, (1 - interp)), null);
            topColour = Vector3f.add(VectorUtils.mul(DUSK_TOP, interp), VectorUtils.mul(NOON_TOP, (1 - interp)), null);
        } else if (altitude < ALTITUDE_MARGIN && azimuth >= 0) {
            baseColour = Vector3f.add(VectorUtils.mul(DUSK_BASE, interp_night), VectorUtils.mul(NIGHT_BASE, (1 - interp_night)), null);
            topColour = Vector3f.add(VectorUtils.mul(DUSK_TOP, interp_night), VectorUtils.mul(NIGHT_TOP, (1 - interp_night)), null);
        } else if (altitude < ALTITUDE_MARGIN && azimuth < 0) {
            baseColour = Vector3f.add(VectorUtils.mul(DAWN_BASE, interp_night), VectorUtils.mul(NIGHT_BASE, (1 - interp_night)), null);
            topColour = Vector3f.add(VectorUtils.mul(DAWN_TOP, interp_night), VectorUtils.mul(NIGHT_TOP, (1 - interp_night)), null);
        }

        if (light != null) {
            if (sunColour.equals(NIGHT_SUN))
                lightComponent.attenuation.set(0, 0, 0);
            else
                lightComponent.attenuation.set(1, 0, 0);
        }
    }

    public int getSkybox() {
        if (skybox.isLoaded())
            return skybox.getTexture();

        return 0;
    }

    public int getIrradiance() {
        if (loaded)
            return irradiance.getTexture();

        return 0;
    }

    public int getPrefilter() {
        if (loaded)
            return prefilter.getTexture();

        return 0;
    }

    @Override
    public void setValue(String key, String value) {

    }

    public void setLight(Entity entity) {
        if (entity.getComponent(SunComponent.class) == null)
            return;

        light = entity;
        lightComponent = entity.getComponent(LightComponent.class);

        updateSky();
    }

    @Override
    public IComponent copy() {
        SkyComponent copy = new SkyComponent();
        copy.setLight(light);
        copy.skybox = skybox;
        copy.irradiance = irradiance;
        copy.prefilter = prefilter;
        copy.loaded = loaded;

        return copy;
    }

}
