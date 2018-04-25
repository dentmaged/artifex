package org.anchor.engine.shared.physics;

import java.util.List;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.collision.CollisionEngine;
import org.anchor.engine.shared.physics.collision.narrowphase.NarrowphaseCollisionResult;
import org.anchor.engine.shared.scene.Scene;
import org.lwjgl.util.vector.Vector3f;

public class PhysicsEngine {

    public static final float TICK_RATE = 60;
    public static final float TICK_DELAY = 1 / TICK_RATE;
    public static final float GRAVITY = -9.98f;

    public void update(Scene scene) {
        List<Entity> entities = scene.getEntitiesWithComponent(PhysicsComponent.class);
        for (Entity entity : entities) {
            PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
            if (physics.gravity)
                physics.velocity.y += GRAVITY * TICK_DELAY;
        }

        List<NarrowphaseCollisionResult> results = CollisionEngine.collisions(entities);
        for (NarrowphaseCollisionResult result : results) {
            PhysicsComponent primary = result.getOne().getComponent(PhysicsComponent.class);
            PhysicsComponent secondary = result.getTwo().getComponent(PhysicsComponent.class);

            Vector3f relative = Vector3f.sub(primary.velocity, secondary.velocity, null);
            float dot = Vector3f.dot(relative, result.getNormal());
            if (dot > 0) {
                dot *= (1 + Math.min(primary.material.getRestition(), secondary.material.getRestition())) / (primary.inverseMass + secondary.inverseMass);
                Vector3f i = VectorUtils.mul(result.getNormal(), dot);

                Vector3f.sub(primary.velocity, VectorUtils.mul(i, primary.inverseMass), primary.velocity);
                Vector3f.add(secondary.velocity, VectorUtils.mul(i, secondary.inverseMass), secondary.velocity);
            }
        }

        for (Entity entity : entities)
            Vector3f.add(entity.getPosition(), VectorUtils.mul(entity.getComponent(PhysicsComponent.class).velocity, TICK_DELAY), entity.getPosition());
    }

}
