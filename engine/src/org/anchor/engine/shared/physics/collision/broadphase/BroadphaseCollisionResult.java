package org.anchor.engine.shared.physics.collision.broadphase;

import org.anchor.engine.shared.entity.Entity;

public class BroadphaseCollisionResult {

    private Entity one, two;
    private int meshOne, meshTwo;

    public BroadphaseCollisionResult(Entity one, Entity two, int meshOne, int meshTwo) {
        this.one = one;
        this.two = two;

        this.meshOne = meshOne;
        this.meshTwo = meshTwo;
    }

    public Entity getOne() {
        return one;
    }

    public Entity getTwo() {
        return two;
    }

    public int getMeshOne() {
        return meshOne;
    }

    public int getMeshTwo() {
        return meshTwo;
    }

}
