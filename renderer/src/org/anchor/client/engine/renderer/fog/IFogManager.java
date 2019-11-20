package org.anchor.client.engine.renderer.fog;

import org.lwjgl.util.vector.Vector3f;

public interface IFogManager {

    public boolean isFogEnabled();

    public Vector3f getFogColour();

    public float getFogDensity();

    public float getFogGradient();

    public float getFogSunPower();

    public float getHorizonBlendStart();

    public float getHorizonBlendEnd();

}
