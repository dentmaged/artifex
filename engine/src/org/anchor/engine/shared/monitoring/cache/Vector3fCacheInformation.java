package org.anchor.engine.shared.monitoring.cache;

import org.lwjgl.util.vector.Vector3f;

public class Vector3fCacheInformation extends CacheInformation {

    public float x, y, z;

    public Vector3fCacheInformation(Vector3f vector) {
        super(vector);

        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Vector3fCacheInformation))
            return false;
        Vector3fCacheInformation other = (Vector3fCacheInformation) object;

        return x == other.x && y == other.y && z == other.z;
    }

}
