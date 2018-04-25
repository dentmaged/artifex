package org.anchor.engine.shared.physics.collision.broadphase;

import org.anchor.engine.shared.entity.Entity;

public class BroadphaseCollisionResult {

    private Entity one;
    private Entity two;

    public BroadphaseCollisionResult(Entity one, Entity two) {
        this.one = one;
        this.two = two;
    }

    public Entity getOne() {
        return one;
    }

    public Entity getTwo() {
        return two;
    }

}
