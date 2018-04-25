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

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.shaders.SkyShader;
import org.lwjgl.util.vector.Vector3f;

public class SkyComponent implements IComponent {

    private Entity entity;

    @Property("Light")
    protected Entity light;
    protected LightComponent lightComponent;

    public float interp, interp_night, altitude, azimuth;
    public Vector3f direction, sunColour, originalLightColour, baseColour, topColour;

    public static float TIME_SCALE = 0.001f;
    public static float ALTITUDE_MARGIN = -0.12f;
    public static float TIME = 12;

    private static Vector3f DAWN_BASE = new Vector3f(0.5f, 0.15f, 0.4f), DAWN_TOP = new Vector3f(0.1f, 0.1f, 0.65f);
    private static Vector3f NOON_BASE = new Vector3f(0.34f, 0.54f, 0.88f), NOON_TOP = new Vector3f(0.1f, 0.4f, 1);
    private static Vector3f DUSK_BASE = new Vector3f(0.9f, 0.4f, 0.1f), DUSK_TOP = new Vector3f(0.5f, 0.4f, 0.3f);
    private static Vector3f NIGHT_BASE = new Vector3f(0, 0.01f, 0.05f), NIGHT_TOP = new Vector3f(0, 0, 0.01f);

    private static Vector3f DAWN_SUN = new Vector3f(0.8f, 0.3f, 0.2f);
    private static Vector3f NOON_SUN = new Vector3f(0.5f, 0.35f, 0.2f);
    private static Vector3f DUSK_SUN = new Vector3f(0.9f, 0.3f, 0.1f);
    private static Vector3f NIGHT_SUN = new Vector3f(0, 0, 0);

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;

        entity.getComponent(MeshComponent.class).shader = SkyShader.getInstance();
        entity.getComponent(MeshComponent.class).castsShadows = false;
        entity.getComponent(MeshComponent.class).disableFrustumCulling = true;
    }

    @Override
    public void update() {
        entity.getPosition().set(GameClient.getPlayer().getPosition());
        TIME += AppManager.getFrameTimeSeconds() * TIME_SCALE;

        while (TIME >= 24)
            TIME -= 24;

        while (TIME < 0)
            TIME += 24;

        updateSky();
    }

    protected void updateSky() {
        float latitude = 40f * (float) Math.PI / 180f;
        float solar_hour_angle = (TIME - 12f) * 15 * (float) Math.PI / 180f;

        int sha_sign = 1;
        if (solar_hour_angle < 0)
            sha_sign = -1;

        altitude = (float) Math.asin(Math.cos(latitude) * Math.cos(solar_hour_angle));
        azimuth = sha_sign * (float) Math.acos((float) Math.sin(altitude) * (float) Math.sin(latitude) / ((float) Math.cos(altitude) * (float) Math.cos(latitude)));

        direction = new Vector3f((float) Math.sin(azimuth), (float) Math.sin(altitude), (float) -Math.cos(azimuth));
        direction.normalise();

        float calculatedAltitude = (float) Math.asin(Math.cos(latitude));
        float max_altitude = calculatedAltitude - ALTITUDE_MARGIN;
        float min_altitude = calculatedAltitude + ALTITUDE_MARGIN;

        interp = (max_altitude - altitude + ALTITUDE_MARGIN) / max_altitude;
        interp_night = (float) Math.pow((min_altitude + altitude - ALTITUDE_MARGIN) / min_altitude, 4);

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
            light.getPosition().set(direction);
            light.getPosition().scale(20000);
            if (sunColour.equals(NIGHT_SUN))
                lightComponent.colour.set(0, 0, 0);
            else
                lightComponent.colour.set(originalLightColour);
        }
    }

    @Override
    public void setValue(String key, String value) {

    }

    public void setLight(Entity entity) {
        light = entity;
        lightComponent = entity.getComponent(LightComponent.class);
        originalLightColour = new Vector3f(lightComponent.colour);

        updateSky();
    }

    @Override
    public IComponent copy() {
        SkyComponent copy = new SkyComponent();
        copy.setLight(light);

        return copy;
    }

}
