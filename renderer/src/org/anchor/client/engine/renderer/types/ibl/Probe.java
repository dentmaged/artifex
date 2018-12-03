package org.anchor.client.engine.renderer.types.ibl;

import org.lwjgl.util.vector.Vector3f;

public interface Probe {

    public Vector3f getPosition();

    public float getSize();

    public boolean isBaked();

}
