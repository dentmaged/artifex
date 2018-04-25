package org.anchor.engine.shared.physics.collision;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.collision.broadphase.Broadphase;
import org.anchor.engine.shared.physics.collision.broadphase.BroadphaseCollisionResult;
import org.anchor.engine.shared.physics.collision.narrowphase.Narrowphase;
import org.anchor.engine.shared.physics.collision.narrowphase.NarrowphaseCollisionResult;

public class CollisionEngine {

    public static List<NarrowphaseCollisionResult> collisions(List<Entity> entities) {
        List<NarrowphaseCollisionResult> results = new ArrayList<NarrowphaseCollisionResult>();

        for (BroadphaseCollisionResult result : Broadphase.collisions(entities)) {
            NarrowphaseCollisionResult narrowphase = Narrowphase.collision(result);
            if (narrowphase != null && narrowphase.getNormal() != null)
                results.add(narrowphase);
        }

        return results;
    }

    public static List<NarrowphaseCollisionResult> collisions(Entity entity, List<Entity> entities) {
        List<NarrowphaseCollisionResult> results = new ArrayList<NarrowphaseCollisionResult>();

        for (BroadphaseCollisionResult result : Broadphase.collisions(entity, entities)) {
            NarrowphaseCollisionResult narrowphase = Narrowphase.collision(result);
            if (narrowphase != null && narrowphase.getNormal() != null)
                results.add(narrowphase);
        }

        return results;
    }

}
