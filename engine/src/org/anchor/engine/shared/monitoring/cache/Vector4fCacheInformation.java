package org.anchor.engine.shared.monitoring.cache;

import org.lwjgl.util.vector.Vector4f;

public class Vector4fCacheInformation extends CacheInformation {

    public float x, y, z, w;

    public Vector4fCacheInformation(Vector4f vector) {
        super(vector);

        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
        this.w = vector.w;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Vector4fCacheInformation))
            return false;
        Vector4fCacheInformation other = (Vector4fCacheInformation) object;

        return x == other.x && y == other.y && z == other.z && w == other.w;
    }

}
