package org.anchor.client.engine.renderer.types;

import org.lwjgl.util.vector.Vector3f;

public interface Light {

    public Vector3f getPosition();

    public Vector3f getColour();

    public Vector3f getAttenuation();

}
