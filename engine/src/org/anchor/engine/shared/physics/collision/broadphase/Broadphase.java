package org.anchor.engine.shared.physics.collision.broadphase;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;

public class Broadphase {

    public static List<BroadphaseCollisionResult> collisions(List<Entity> entities) {
        List<BroadphaseCollisionResult> results = new ArrayList<BroadphaseCollisionResult>();
        List<BroadphaseCheck> checks = new ArrayList<BroadphaseCheck>();

        outer: for (Entity one : entities) {
            if (VectorUtils.hasZero(one.getScale()))
                continue;

            PhysicsComponent primary = one.getComponent(PhysicsComponent.class);
            if (!primary.isCollidable())
                continue outer;

            inner: for (Entity two : entities) {
                if (one == two)
                    continue inner;

                if (VectorUtils.hasZero(two.getScale()))
                    continue;

                PhysicsComponent secondary = two.getComponent(PhysicsComponent.class);
                if (!secondary.isCollidable())
                    continue inner;

                if (!primary.canCollideWith(two) || !secondary.canCollideWith(one))
                    continue inner;

                BroadphaseCheck thisCheck = new BroadphaseCheck(one, two);
                for (BroadphaseCheck check : checks) {
                    if (check.equals(thisCheck))
                        continue inner;
                }

                checks.add(thisCheck);
                if (primary.getWholeAABB().collides(secondary.getWholeAABB())) {
                    int x = -1;
                    int y = -1;

                    meshCheckOuter: for (int i = 0; i < primary.getMeshCount(); i++) {
                        AABB a = primary.getAABB(i);

                        for (int j = 0; j < secondary.getMeshCount(); j++) {
                            if (a.collides(secondary.getAABB(j))) {
                                x = i;
                                y = j;

                                break meshCheckOuter;
                            }
                        }
                    }

                    if (x >= 0 && y >= 0)
                        results.add(new BroadphaseCollisionResult(one, two, x, y));
                }
            }
        }

        return results;
    }

    public static List<BroadphaseCollisionResult> collisions(Entity one, List<Entity> entities) {
        List<BroadphaseCollisionResult> results = new ArrayList<BroadphaseCollisionResult>();
        if (VectorUtils.hasZero(one.getScale()))
            return results;

        PhysicsComponent primary = one.getComponent(PhysicsComponent.class);

        if (!primary.isCollidable())
            return results;

        for (Entity two : entities) {
            if (one == two)
                continue;

            if (VectorUtils.hasZero(two.getScale()))
                continue;

            PhysicsComponent secondary = two.getComponent(PhysicsComponent.class);
            if (!secondary.isCollidable())
                continue;

            if (!primary.canCollideWith(two) || !secondary.canCollideWith(one))
                continue;

            if (primary.getWholeAABB().collides(secondary.getWholeAABB())) {
                int x = -1;
                int y = -1;

                meshCheckOuter: for (int i = 0; i < primary.getMeshCount(); i++) {
                    AABB a = primary.getAABB(i);

                    for (int j = 0; j < secondary.getMeshCount(); j++) {
                        if (a.collides(secondary.getAABB(j))) {
                            x = i;
                            y = j;

                            break meshCheckOuter;
                        }
                    }
                }

                if (x >= 0 && y >= 0)
                    results.add(new BroadphaseCollisionResult(one, two, x, y));
            }
        }

        return results;
    }

}
