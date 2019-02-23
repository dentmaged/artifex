package org.anchor.client.engine.renderer.types.light;

import org.lwjgl.util.vector.Vector3f;

public interface Light {

    public Vector3f getPosition();

    public Vector3f getColour();

    public Vector3f getAttenuation();

    public Vector3f getDirection();

    public float getCutoff();

    public float getOuterCutoff();

    public boolean isVolumetricLight();

    public LightType getLightType();

    public default float getRadius() {
        return 0;
    }

}
