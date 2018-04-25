package org.anchor.game.client.utils;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.lwjgl.util.vector.Vector3f;

public class EntityRaycast {

    protected Entity entity;
    protected float distance;
    protected Vector3f point, direction;

    public EntityRaycast(Entity entity, float distance, Vector3f direction, Vector3f origin) {
        this.entity = entity;
        this.distance = distance;
        this.direction = direction;
        this.point = Vector3f.add(origin, VectorUtils.mul(direction, distance), null);
    }

    public Entity getEntity() {
        return entity;
    }

    public float getDistance() {
        return distance;
    }

    public Vector3f getPoint() {
        return point;
    }

    public Vector3f getDirection() {
        return direction;
    }

}
