package org.anchor.engine.shared.physics.collision.narrowphase;

import org.anchor.engine.shared.entity.Entity;
import org.lwjgl.util.vector.Vector3f;

public class NarrowphaseCollisionResult {

    private boolean colliding;
    private Entity one, two;

    private Vector3f normal;
    private float overlap;

    public NarrowphaseCollisionResult(boolean colliding, Entity one, Entity two, Vector3f normal, float overlap) {
        if (colliding && normal == null)
            throw new IllegalArgumentException("A collision has occured. Please provide an MTV.");

        this.colliding = colliding;
        this.one = one;
        this.two = two;

        this.normal = normal;
        if (normal != null && normal.lengthSquared() > 0)
            this.normal.normalise();

        this.overlap = overlap;
    }

    public boolean isColliding() {
        return colliding;
    }

    public Entity getOne() {
        return one;
    }

    public Entity getTwo() {
        return two;
    }

    public Entity getOther(Entity other) {
        if (other == one)
            return two;

        return one;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public float getOverlap() {
        return overlap;
    }

}
